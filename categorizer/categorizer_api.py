import os
import pickle
import io
import json
import re # New import for better text cleaning
from flask import Flask, request, jsonify

# Cloud Service Imports
from google.cloud import storage
import firebase_admin 
from firebase_admin import firestore, credentials 

# --- 1. CONFIGURATION & INITIALIZATION ---

# GCS Config
MODEL_BUCKET = os.environ.get("MODEL_BUCKET", "fintraq-models")
MODEL_OBJECT = os.environ.get("MODEL_OBJECT", "model.pkl")

# Firestore Config
try:
    firebase_admin.initialize_app()
    db = firestore.client()
    print("✅ Firestore client initialized.")
except Exception as e:
    print(f"⚠️ WARNING: Failed to initialize Firebase/Firestore: {e}")
    db = None

# --- 2. CATEGORY MAP LOADING (From separate file) ---
CATEGORY_MAP = {}
try:
    # Loads the extensive mapping from a local file for maintainability
    with open("category_map.json", "r") as f:
        CATEGORY_MAP = json.load(f)
    print(f"✅ Loaded {len(CATEGORY_MAP)} entries from category_map.json.")
except FileNotFoundError:
    print("⚠️ WARNING: category_map.json not found. Rule-based mapping will be limited.")
except Exception as e:
    print(f"⚠️ WARNING: Failed to load category_map.json: {e}")


# --- 3. ML MODEL LOADING ---
# ... (load_model_from_gcs function and MODEL loading try/except block remain the same) ...
def load_model_from_gcs(bucket_name=MODEL_BUCKET, object_name=MODEL_OBJECT):
    """Downloads and unpickles the model and vectorizer from GCS."""
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
    """
    Checks the cleaned text against simple high-confidence rules and the full category map.
    Returns (category, confidence) or (None, None) if no rule matches.
    """
    # 1. Simple, High-Confidence Rules (Income/ATM)
    if any(x in text_lower for x in ["salary", "credited to your", "has been credited"]):
        return "Income - Salary", 0.99
        
    if "atm" in text_lower:
        return "Cash Withdrawal", 0.90
        
    # 2. Extensive Map Search (The core hybrid logic)
    for merchant, category in full_map.items():
        if merchant in text_lower:
            return category, 0.95 

    # 3. Default UPI/Generic Payments (Safety net)
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
        data = request.get_json(force=True, silent=True)
        
        sms_body = data.get('raw_text') or data.get('message') or data.get('sms_body')
        user_id = data.get('user_id', 'unknown_user') # Capture User ID

        if not sms_body:
            return jsonify({'error': 'Missing transaction text field'}), 400

        # --- PRE-PROCESSING ---
        # Strip all punctuation and make lowercase for robust rule matching
        text_lower = re.sub(r'[^\w\s]', '', sms_body).lower() 
        
        # --- HYBRID RULE EXECUTION ---
        predicted_category, confidence_score = rule_based_categorize(text_lower, CATEGORY_MAP)
        
        if predicted_category:
            final_category = predicted_category
            final_confidence = confidence_score
            is_ml_prediction = False
        else:
            # --- FALLBACK TO ML ---
            if MODEL:
                if VECTORIZER:
                    input_for_model = VECTORIZER.transform([sms_body])
                else:
                    input_for_model = [sms_body]

                final_category = MODEL.predict(input_for_model)[0]
                
                if hasattr(MODEL, "predict_proba"):
                    probabilities = MODEL.predict_proba(input_for_model)[0]
                    final_confidence = float(max(probabilities))
                else:
                    final_confidence = 1.0
                
                is_ml_prediction = True

            else:
                final_category = "Uncategorized"
                final_confidence = 0.0
                is_ml_prediction = False

        # --- 6. DATA LOGGING (For future ML training) ---
        if db:
            transaction_log = {
                'user_id': user_id,
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
