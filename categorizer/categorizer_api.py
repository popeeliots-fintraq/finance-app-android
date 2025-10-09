import os
import pickle
from flask import Flask, request, jsonify

# Define the path to your model file
MODEL_PATH = "model.pkl"

# Try to load the model and vectorizer
try:
    with open(MODEL_PATH, 'rb') as f:
        MODEL_AND_VECTORIZER = pickle.load(f)
    MODEL = MODEL_AND_VECTORIZER['model']
    VECTORIZER = MODEL_AND_VECTORIZER['vectorizer']
    print("Model and Vectorizer loaded successfully.")
except Exception as e:
    print(f"WARNING: Failed to load model.pkl from {MODEL_PATH}: {e}. API will return default category.")
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
        data = request.get_json()
        sms_body = data.get('sms_body', '')

        if not sms_body:
            return jsonify({'error': 'Missing sms_body field'}), 400

        if MODEL and VECTORIZER:
            sms_vectorized = VECTORIZER.transform([sms_body])
            predicted_category = MODEL.predict(sms_vectorized)[0]
            probabilities = MODEL.predict_proba(sms_vectorized)[0]
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
    app.run(debug=True, host='0.0.0.0', port=int(os.environ.get('PORT', 8080)))
