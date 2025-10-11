import os
import pickle
import io
from flask import Flask, request, jsonify
from google.cloud import storage

# --- CONFIGURATION ---
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
    return "Backend is running!", 200

@app.route('/categorize', methods=['POST'])
def categorize():
    try:
        data = request.get_json(force=True, silent=True)
        print(f"Received request data: {data}")

        if not data:
            return jsonify({'error': 'Invalid JSON or empty request body'}), 400

        # Changed key to 'message' to match test input
        sms_body = data.get('message', '')
        if not sms_body:
            return jsonify({'error': 'Missing message field'}), 400

        if MODEL:
            if VECTORIZER:
                sms_vectorized = VECTORIZER.transform([sms_body])
                input_for_model = sms_vectorized
            else:
                input_for_model = [sms_body]

            predicted_category = MODEL.predict(input_for_model)[0]

            if hasattr(MODEL, "predict_proba"):
                probabilities = MODEL.predict_proba(input_for_model)[0]
                confidence_score = float(max(probabilities))
            else:
                confidence_score = 1.0
        else:
            predicted_category = "Uncategorized"
            confidence_score = 0.0

        return jsonify({
            'suggested_category': predicted_category,
            'confidence_score': round(confidence_score, 4)
        })

    except Exception as e:
        print(f"ERROR during categorization: {e}")
        return jsonify({'error': 'Internal Server Error during request processing'}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=int(os.environ.get('PORT', 8080)))
