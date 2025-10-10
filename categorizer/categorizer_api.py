import os
import pickle
import io  # ✅ ADDED: required for in-memory bytes
from flask import Flask, request, jsonify
from google.cloud import storage  # ✅ ADDED: required for GCS access

# ✅ REPLACED local model loading with GCS loader
MODEL_BUCKET = os.environ.get("MODEL_BUCKET", "fintraq-models")  # ✅ ADDED
MODEL_OBJECT = os.environ.get("MODEL_OBJECT", "model.pkl")       # ✅ ADDED

def load_model_from_gcs(bucket_name=MODEL_BUCKET, object_name=MODEL_OBJECT):
    """
    Load the model and vectorizer from GCS into memory
    """
    client = storage.Client()
    bucket = client.bucket(bucket_name)
    blob = bucket.blob(object_name)
    data = blob.download_as_bytes()  # ✅ ADDED: download model in memory
    model_and_vectorizer = pickle.load(io.BytesIO(data))  # ✅ ADDED
    return model_and_vectorizer

# Load model & vectorizer
try:
    MODEL_AND_VECTORIZER = load_model_from_gcs()  # ✅ CHANGED: use GCS loader
    MODEL = MODEL_AND_VECTORIZER['model']
    VECTORIZER = MODEL_AND_VECTORIZER['vectorizer']
    print("Model and Vectorizer loaded successfully from GCS.")  # ✅ CHANGED
    print("✅ Model Classes:", MODEL.classes_)
except Exception as e:
    print(f"⚠️ WARNING: Failed to load model from GCS: {e}. API will return default category.")  # ✅ CHANGED
    MODEL = None
    VECTORIZER = None

app = Flask(__name__)

# Health check endpoint
@app.route('/')
def health_check():
    return "Backend is running!", 200

# Categorization endpoint
@app.route('/categorize', methods=['POST'])
def categorize():
    try:
        # Use silent=True to avoid crashing on invalid JSON
        data = request.get_json(silent=True) 
        
        # Check for invalid JSON first
        if not data:
             return jsonify({'error': 'Invalid JSON or empty request body'}), 400
             
        sms_body = data.get('sms_body', '')

        if not sms_body:
            return jsonify({'error': 'Missing sms_body field'}), 400

        # ✅ REMOVED: get_model_and_vectorizer() call

        if MODEL and VECTORIZER:
            # 1. Vectorize the input SMS text
            sms_vectorized = VECTORIZER.transform([sms_body])
            
            # 2. Predict the category
            predicted_category = MODEL.predict(sms_vectorized)[0]
            
            # 3. Calculate confidence
            probabilities = MODEL.predict_proba(sms_vectorized)[0]
            confidence_score = float(max(probabilities))
        else:
            # Fallback when the model failed to load
            predicted_category = "Uncategorized"
            confidence_score = 0.0

        return jsonify({
            'suggested_category': predicted_category,
            'confidence_score': round(confidence_score, 4)
        })

    except Exception as e:
        # Catch any unexpected errors during processing
        return jsonify({'error': f'Internal Server Error: {e}'}), 500

if __name__ == '__main__':
    # Removed 'debug=True' for better production readiness 
    app.run(host='0.0.0.0', port=int(os.environ.get('PORT', 8080)))
