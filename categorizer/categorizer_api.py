import os
import pickle
import io
import json
import re 
from flask import Flask, request, jsonify

# Cloud Service Imports
from google.cloud import storage
import firebase_admin 
from firebase_admin import firestore, credentials, auth # CRITICAL: Added 'auth'
from firebase_admin.exceptions import FirebaseError # CRITICAL: For explicit error handling

# --- 1. CONFIGURATION & INITIALIZATION ---
MODEL_BUCKET = os.environ.get("MODEL_BUCKET", "fintraq-models")
MODEL_OBJECT = os.environ.get("MODEL_OBJECT", "model.pkl")

# Firestore Config
try:
    firebase_admin.initialize_app()
    db = firestore.client()
    print("✅ Firebase client initialized.")
except Exception as e:
    print(f"⚠️ WARNING: Failed to initialize Firebase/Firestore: {e}")
    db = None

# --- 2. CATEGORY MAP LOADING ---
CATEGORY_MAP_FILE = "functions/merchant-map.json" 
CATEGORY_MAP = {}
try:
    with open(CATEGORY_MAP_FILE, "r") as f:
        CATEGORY_MAP = json.load(f)
    print(f"✅ Loaded {len(CATEGORY_MAP)} entries from {CATEGORY_MAP_FILE}.")
except Exception as e:
    print(f"⚠️ WARNING: Failed to load {CATEGORY_MAP_FILE}: {e}")

# --- 3. ML MODEL LOADING ---
def load_model_from_gcs(bucket_name=MODEL_BUCKET, object_name=MODEL_OBJECT):
    client = storage.Client()
    bucket = client.bucket(bucket_name)
    blob = bucket.blob(object_name)
    data = blob.download_as_bytes()
    return pickle.load(io.BytesIO(data))

try:
    MODEL_AND_VECTORIZER = load_model_from_gcs()
    if isinstance(MODEL_AND_VECTORIZER, dict):
        MODEL = MODEL_AND_VECTORIZER.get('model')
        VECTORIZER = MODEL_AND_VECTORIZER.get('vectorizer')
    elif isinstance(MODEL_AND_VECTORIZER, tuple) and len(MODEL_AND_VECTORIZER) == 2:
        MODEL, VECTORIZER = MODEL_AND_VECTORIZER
    else:
        MODEL = MODEL_AND_VECTORIZER
        VECTORIZER = None
    print("✅ Model loaded from GCS")
except Exception as e:
    print(f"⚠️ WARNING: Failed to load model from GCS: {e}. Falling back to rule-only.")
    MODEL = None
    VECTORIZER = None


# --- 4. CATEGORIZATION LOGIC ---
def rule_based_categorize(text_lower, full_map):
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

# --- 5. FLASK APP & ROUTING ---
app = Flask(__name__)

@app.route('/')
def health_check():
    return "✅ Backend is running!", 200

@app.route('/categorize', methods=['POST'])
def categorize():
    try:
        # --- SECURITY CRITICAL: TOKEN VERIFICATION (ADDED LOGIC) ---
        auth_header = request.headers.get('Authorization')
        
        # Check for presence and format
        if not auth_header or not auth_header.startswith('Bearer '):
            return jsonify({'error': 'Unauthorized: Missing or invalid Authorization header'}), 401
        
        id_token = auth_header.split('Bearer ')[1]
        secure_user_id = None
        
        try:
            # Verify the token against Firebase servers
            verified_token = auth.verify_id_token(id_token)
            secure_user_id = verified_token.get('uid') 
            print(f"Authenticated user: {secure_user_id}")
            
        except FirebaseError as e:
            # Token failed verification (expired, invalid signature, etc.)
            return jsonify({'error': f'Unauthorized: Invalid token: {e}'}), 401
        
        # --- INPUT EXTRACTION (Now safe to proceed) ---
        data = request.get_json(force=True, silent=True)
        sms_body = data.get('raw_text') or data.get('message') or data.get('sms_body')

        if not sms_body:
            return jsonify({'error': 'Missing transaction text field'}), 400

        # --- VALIDATION ---
        if not isinstance(sms_body, str) or len(sms_body) > 1024: 
            return jsonify({'error': 'Invalid or oversized input text'}), 400

        # --- CATEGORIZATION LOGIC (Same) ---
        text_lower = re.sub(r'[^\w\s]', '', sms_body).lower() 
        predicted_category, confidence_score = rule_based_categorize(text_lower, CATEGORY_MAP)
        
        # ... (ML Fallback logic) ...
        if predicted_category:
            final_category = predicted_category
            final_confidence = confidence_score
            is_ml_prediction = False
        else:
            if MODEL:
                input_for_model = VECTORIZER.transform([sms_body]) if VECTORIZER else [sms_body]
                final_category = MODEL.predict(input_for_model)[0]
                final_confidence = float(max(MODEL.predict_proba(input_for_model)[0])) if hasattr(MODEL, "predict_proba") else 1.0
                is_ml_prediction = True
            else:
                final_category = "Uncategorized"
                final_confidence = 0.0
                is_ml_prediction = False

        # --- DATA LOGGING (Using secure_user_id) ---
        if db and secure_user_id: # Check that we have a valid db connection AND a verified user ID
            transaction_log = {
                'user_id': secure_user_id, # CRITICAL FIX: Using verified ID from the token
                'raw_text': sms_body,
                'predicted_category': final_category,
                'confidence': final_confidence,
                'is_ml_prediction': is_ml_prediction,
                'timestamp': firestore.SERVER_TIMESTAMP,
                'confirmed_category': final_category 
            }
            db.collection('user_transactions').add(transaction_log)

        return jsonify({
            'suggested_category': final_category,
            'confidence_score': round(final_confidence, 4)
        })

    except Exception as e:
        print(f"❌ ERROR during categorization: {e}")
        return jsonify({'error': 'Internal Server Error during categorization'}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=int(os.environ.get('PORT', 8080)))
