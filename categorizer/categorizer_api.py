import os
import pickle
import io
import json
import re
from typing import Optional, Dict, Any

# --- FASTAPI / PYDANTIC IMPORTS ---
from fastapi import FastAPI, Request, Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from pydantic import BaseModel
from datetime import datetime

# --- CLOUD SERVICE IMPORTS ---
from google.cloud import storage
import firebase_admin
from firebase_admin import firestore, credentials, auth
from firebase_admin.exceptions import FirebaseError

# --- API KEY IMPORTS ---
# Assuming this file is run at the project root level, we'll try to keep imports simple.
# For Cloud Run, we will rely on the direct OS environment variable.
def get_expected_api_key():
    """Reads the expected API Key from the server's environment."""
    # Matches the recommended env var name
    return os.getenv("FIN_TRAQ_SYSTEM_API_KEY", "")

# --- V2 Pydantic Schemas for Validation ---
class RawSmsIn(BaseModel):
    """V2 DTO for sending raw SMS data to the backend's high-speed IngestionService."""
    # NOTE: user_id is passed via the Bearer token, but we include a placeholder for audit/testing
    user_id: Optional[str] = None 
    raw_text: str
    source_type: str
    local_timestamp: int # Long equivalent in Python

class CategorizationOut(BaseModel):
    """V2 DTO for the synchronous response from the Categorizer."""
    suggested_category: str
    confidence_score: float
    user_id: str # The verified user ID

# --- 1. CONFIGURATION & INITIALIZATION ---
MODEL_BUCKET = os.environ.get("MODEL_BUCKET", "fintraq-models")
MODEL_OBJECT = os.environ.get("MODEL_OBJECT", "model.pkl")
CATEGORY_MAP_FILE = os.path.join("functions", "merchant-map.json")

# 🚨 CRITICAL FIX: Explicitly initialize Firebase using the file path environment variable
# that was passed during Cloud Run deployment, if available.
try:
    # Look for the environment variable we configured in the CI/CD fix
    cred_path = os.environ.get("FIREBASE_CONFIG_PATH")
    if cred_path and os.path.exists(cred_path):
        cred = credentials.Certificate(cred_path)
        firebase_admin.initialize_app(cred)
        print("✅ Firebase client initialized using service account JSON.")
    else:
        # Fallback to default credentials (works if Cloud Run has service account role)
        firebase_admin.initialize_app()
        print("✅ Firebase client initialized using default credentials.")
    db = firestore.client()
except Exception as e:
    print(f"⚠️ WARNING: Failed to initialize Firebase/Firestore: {e}")
    db = None

# --- 2. CATEGORY MAP LOADING ---
CATEGORY_MAP: Dict[str, Any] = {}
try:
    with open(CATEGORY_MAP_FILE, "r") as f:
        CATEGORY_MAP = json.load(f)
    print(f"✅ Loaded {len(CATEGORY_MAP)} entries from {CATEGORY_MAP_FILE}.")
except Exception as e:
    print(f"⚠️ WARNING: Failed to load {CATEGORY_MAP_FILE}: {e}")

# --- 3. ML MODEL LOADING ---
def load_model_from_gcs(bucket_name=MODEL_BUCKET, object_name=MODEL_OBJECT):
    """Loads the model and vectorizer from Google Cloud Storage."""
    client = storage.Client()
    bucket = client.bucket(bucket_name)
    blob = bucket.blob(object_name)
    # Using 'download_as_string' for older Python compatibility, though bytes is fine too
    data = blob.download_as_bytes()
    return pickle.load(io.BytesIO(data))

MODEL = None
VECTORIZER = None
try:
    MODEL_AND_VECTORIZER = load_model_from_gcs()
    if isinstance(MODEL_AND_VECTORIZER, dict):
        MODEL = MODEL_AND_VECTORIZER.get('model')
        VECTORIZER = MODEL_AND_VECTORIZER.get('vectorizer')
    elif isinstance(MODEL_AND_VECTORIZER, tuple) and len(MODEL_AND_VECTORIZER) == 2:
        MODEL, VECTORIZER = MODEL_AND_VECTORIZER
    print("✅ ML Model loaded from GCS")
except Exception as e:
    print(f"⚠️ WARNING: Failed to load model from GCS: {e}. Falling back to rule-only.")

# --- 4. CATEGORIZATION LOGIC ---
def rule_based_categorize(text_lower: str, full_map: Dict[str, Any]):
    """Applies simple rules for high-confidence categorization."""
    if any(x in text_lower for x in ["salary", "credited to your", "has been credited"]):
        return "Income - Salary", 0.99
    if "atm" in text_lower:
        return "Cash Withdrawal", 0.90
    for merchant, category in full_map.items():
        if merchant in text_lower:
            return category, 0.95
    if any(x in text_lower for x in ["upi", "gpay", "paytm", "phonepe", "transfer"]):
        return "UPI/Transfer", 0.70
    return None, None

# --- 5. DEPENDENCY FUNCTIONS (FastAPI) ---

# Authentication Scheme for Bearer Token
oauth2_scheme = HTTPBearer()

async def verify_system_api_key(request: Request):
    """Verifies the CI/CD system X-API-Key."""
    x_api_key = request.headers.get('X-API-Key')
    expected_key = get_expected_api_key()

    if not expected_key:
        # Prevents client from attacking a misconfigured server
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                            detail="Server Misconfiguration: API Key not set.")
    
    if not x_api_key or x_api_key != expected_key:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED,
                            detail="Unauthorized: Invalid or missing X-API-Key.")
    return True # API Key is valid

async def verify_user_token(token: HTTPAuthorizationCredentials = Depends(oauth2_scheme)):
    """Verifies the Firebase ID token for user authentication."""
    if token.scheme != "Bearer":
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED,
                            detail="Unauthorized: Must use Bearer token.")
    
    try:
        # Verify the token against Firebase servers
        verified_token = auth.verify_id_token(token.credentials)
        secure_user_id = verified_token.get('uid')
        print(f"Authenticated user: {secure_user_id}")
        return secure_user_id # Return the verified user ID
    except FirebaseError as e:
        # Token failed verification (expired, invalid signature, etc.)
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED,
                            detail=f"Unauthorized: Invalid token: {e}")
    except Exception as e:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                            detail=f"Token verification failed: {e}")

# --- 6. FASTAPI APP & ROUTING ---
app = FastAPI(title="Fin-Traq V2 Categorizer", version="2.0.1")

@app.get('/')
async def health_check():
    """Simple health check endpoint."""
    return {"status": "✅ Backend is running!"}

@app.post('/categorize', response_model=CategorizationOut, status_code=status.HTTP_200_OK)
async def categorize_transaction(
    # 1. System API Key is verified first (for CI/CD/internal system calls)
    api_key_valid: bool = Depends(verify_system_api_key),
    # 2. User ID is extracted from the Firebase token
    secure_user_id: str = Depends(verify_user_token),
    # 3. Request body is validated by Pydantic
    data: RawSmsIn
):
    """
    Receives raw SMS data, categorizes it using ML/rules, logs the result, 
    and returns the suggested category.
    """
    try:
        sms_body = data.raw_text
        text_lower = re.sub(r'[^\w\s]', '', sms_body).lower()
        predicted_category, confidence_score = rule_based_categorize(text_lower, CATEGORY_MAP)

        # --- ML Fallback logic ---
        if predicted_category:
            final_category = predicted_category
            final_confidence = confidence_score
            is_ml_prediction = False
        else:
            if MODEL:
                # Assuming simple Scikit-Learn or similar API
                input_for_model = VECTORIZER.transform([sms_body]) if VECTORIZER else [sms_body]
                final_category = MODEL.predict(input_for_model)[0]
                final_confidence = float(max(MODEL.predict_proba(input_for_model)[0])) if hasattr(MODEL, "predict_proba") else 1.0
                is_ml_prediction = True
            else:
                final_category = "Uncategorized"
                final_confidence = 0.0
                is_ml_prediction = False
        
        # --- DATA LOGGING (Using secure_user_id) ---
        if db and secure_user_id:
            transaction_log = {
                'user_id': secure_user_id, # CRITICAL FIX: Using verified ID from the token
                'raw_text': sms_body,
                'predicted_category': final_category,
                'confidence': final_confidence,
                'is_ml_prediction': is_ml_prediction,
                'timestamp': firestore.SERVER_TIMESTAMP,
                'confirmed_category': final_category,
                'ingestion_time': datetime.now().isoformat()
            }
            db.collection('user_transactions').add(transaction_log)

        # Return the Pydantic model response
        return CategorizationOut(
            suggested_category=final_category,
            confidence_score=round(final_confidence, 4),
            user_id=secure_user_id
        )

    except HTTPException:
        # Re-raise HTTP exceptions from dependencies
        raise
    except Exception as e:
        print(f"❌ Unhandled ERROR during categorization: {e}")
        # Log unexpected error detail to server logs, but return a generic 500
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, 
                            detail="Internal Server Error during categorization.")

if __name__ == '__main__':
    # This block is for local testing only; Cloud Run uses the Gunicorn CMD
    import uvicorn
    # 🚨 NOTE: Cloud Run expects the app to be run by gunicorn/uvicorn on 0.0.0.0:${PORT}
    uvicorn.run("categorizer_api:app", host='0.0.0.0', port=int(os.environ.get('PORT', 8080)), reload=True)
