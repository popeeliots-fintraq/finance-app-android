import os
import pickle
import io
from flask import Flask, request, jsonify
from google.cloud import storage

MODEL_BUCKET = os.environ.get("MODEL_BUCKET", "fintraq-models")
MODEL_OBJECT = os.environ.get("MODEL_OBJECT", "model.pkl")

def load_model_from_gcs(bucket_name=MODEL_BUCKET, object_name=MODEL_OBJECT):
    client = storage.Client()
    bucket = client.bucket(bucket_name)
    blob = bucket.blob(object_name)
    data = blob.download_as_bytes()
    return pickle.load(io.BytesIO(data))

# ✅ UNIVERSAL MODEL LOADER
try:
    MODEL_AND_VECTORIZER = load_model_from_gcs()

    if isinstance(MODEL_AND_VECTORIZER, dict):
        MODEL = MODEL_AND_VECTORIZER.get('model')
        VECTORIZER = MODEL_AND_VECTORIZER.get('vectorizer')
    elif isinstance(MODEL_AND_VECTORIZER, tuple) and len(MODEL_AND_VECTORIZER) == 2:
        MODEL, VECTORIZER = MODEL_AND_VECTORIZER
    else:
        MODEL = MODEL_AND_VECTORIZER
        VECTORIZER = None  # Probably a Pipeline

    print("✅ Model loaded from GCS")
    print("✅ Model type:", type(MODEL))
    if VECTORIZER:
        print("✅ Vectorizer type:", type(VECTORIZER))

except Exception as e:
    print(f"⚠️ WARNING: Failed to load model from GCS: {e}. Defaulting to fallback mode.")
    MODEL = None
    VECTORIZER = None

app = Flask(__name__)

@app.route('/')
def health_check():
    return "Backend is running!", 200

@app.route('/categorize', methods=['POST'])
def categorize():
    try:
        data = request.get_json(silent=True)
        if not data:
            return jsonify({'error': 'Invalid JSON or empty request body'}), 400

        sms_body = data.get('sms_body', '')
        if not sms_body:
            return jsonify({'error': 'Missing sms_body field'}), 400

        if MODEL:
            if VECTORIZER:
                sms_vectorized = VECTORIZER.transform([sms_body])
                predicted_category = MODEL.predict(sms_vectorized)[0]
                probabilities = MODEL.predict_proba(sms_vectorized)[0]
            else:
                predicted_category = MODEL.predict([sms_body])[0]
                if hasattr(MODEL, "predict_proba"):
                    probabilities = MODEL.predict_proba([sms_body])[0]
                else:
                    probabilities = [1.0]

            confidence_score = float(max(probabilities))
        else:
            predicted_category = "Uncategorized"
            confidence_score = 0.0

        return jsonify({
            'suggested_category': predicted_category,
            'confidence_score': round(confidence_score, 4)
        })

    except Exception as e:
        return jsonify({'error': f'Internal Server Error: {e}'}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=int(os.environ.get('PORT', 8080)))
