import os
import pickle
import io
from flask import Flask, request, jsonify
from google.cloud import storage

# --- CONFIGURATION ---
# Use environment variables for flexibility in deployment
MODEL_BUCKET = os.environ.get("MODEL_BUCKET", "fintraq-models")
MODEL_OBJECT = os.environ.get("MODEL_OBJECT", "model.pkl")

# --- MODEL LOADING FUNCTION ---
def load_model_from_gcs(bucket_name=MODEL_BUCKET, object_name=MODEL_OBJECT):
    """Downloads and unpickles the model and vectorizer from GCS."""
    client = storage.Client()
    bucket = client.bucket(bucket_name)
    blob = bucket.blob(object_name)
    data = blob.download_as_bytes()
    return pickle.load(io.BytesIO(data))

# --- UNIVERSAL MODEL LOADER ---
try:
    MODEL_AND_VECTORIZER = load_model_from_gcs()

    # Determine if the loaded object is a dictionary or a tuple containing model/vectorizer
    if isinstance(MODEL_AND_VECTORIZER, dict):
        MODEL = MODEL_AND_VECTORIZER.get('model')
        VECTORIZER = MODEL_AND_VECTORIZER.get('vectorizer')
    elif isinstance(MODEL_AND_VECTORIZER, tuple) and len(MODEL_AND_VECTORIZER) == 2:
        MODEL, VECTORIZER = MODEL_AND_VECTORIZER
    else:
        MODEL = MODEL_AND_VECTORIZER
        VECTORIZER = None  # Treat as a pipeline or single object

    print("✅ Model loaded from GCS")
    print("✅ Model type:", type(MODEL))
    if VECTORIZER:
        print("✅ Vectorizer type:", type(VECTORIZER))

except Exception as e:
    print(f"⚠️ WARNING: Failed to load model from GCS: {e}. Defaulting to fallback mode.")
    MODEL = None
    VECTORIZER = None

app = Flask(__name__)

# ----------------------------------------------------------------------
# --- API ENDPOINTS ---
# ----------------------------------------------------------------------

@app.route('/')
def health_check():
    """Health check endpoint to confirm the service is running."""
    return "Backend is running!", 200

@app.route('/categorize', methods=['POST'])
def categorize():
    """Endpoint for categorizing a transaction based on SMS body."""
    try:
        # **🔥 CRITICAL FIX HERE: Using force=True to bypass persistent 400 errors 🔥**
        # This tells Flask to attempt to parse the request body as JSON regardless of the Content-Type header.
        data = request.get_json(force=True, silent=True)
        
        if not data:
            return jsonify({'error': 'Invalid JSON or empty request body'}), 400

        sms_body = data.get('sms_body', '')
        if not sms_body:
            return jsonify({'error': 'Missing sms_body field'}), 400

        # --- MODEL INFERENCE LOGIC ---
        if MODEL:
            # 1. Vectorize/Transform the input text
            if VECTORIZER:
                sms_vectorized = VECTORIZER.transform([sms_body])
                input_for_model = sms_vectorized
            else:
                input_for_model = [sms_body]
            
            # 2. Predict Category and Probabilities
            predicted_category = MODEL.predict(input_for_model)[0]
            
            if hasattr(MODEL, "predict_proba"):
                probabilities = MODEL.predict_proba(input_for_model)[0]
                confidence_score = float(max(probabilities))
            else:
                # For models without predict_proba (like some SVCs), assume 1.0 confidence
                confidence_score = 1.0 
        else:
            # Fallback mode if model load failed
            predicted_category = "Uncategorized"
            confidence_score = 0.0

        # 3. Return the result
        return jsonify({
            'suggested_category': predicted_category,
            'confidence_score': round(confidence_score, 4)
        })

    except Exception as e:
        # Log the error and return a generic 500 response
        print(f"ERROR during categorization: {e}")
        return jsonify({'error': 'Internal Server Error during request processing'}), 500

if __name__ == '__main__':
    # Start the server (as required for Cloud Run/container environments)
    app.run(host='0.0.0.0', port=int(os.environ.get('PORT', 8080)))
