import os
import pickle
from flask import Flask, request, jsonify

# --- IMPORTANT: PLACEHOLDER DATA ---
# In a real app, you would load your trained model and vectorizer here.
# For this step, we assume you have committed a 'model.pkl' file 
# that contains your trained Scikit-learn model and necessary TfidfVectorizer.
# You will need to generate and commit this file later.
try:
    with open('model.pkl', 'rb') as f:
        # Load both the model and the vectorizer (needed for text transformation)
        MODEL_AND_VECTORIZER = pickle.load(f)
    MODEL = MODEL_AND_VECTORIZER['model']
    VECTORIZER = MODEL_AND_VECTORIZER['vectorizer']
    print("Model and Vectorizer loaded successfully.")
except Exception as e:
    # This will raise an error if model.pkl is missing, which is expected for now.
    print(f"WARNING: Failed to load model.pkl: {e}. API will return default category.")

app = Flask(__name__)

@app.route('/categorize', methods=['POST'])
def categorize():
    try:
        data = request.json
        sms_body = data.get('sms_body', '')

        if not sms_body:
            return jsonify({'error': 'Missing sms_body field'}), 400

        # --- ACTUAL CATEGORIZATION LOGIC ---
        if 'MODEL' in globals() and 'VECTORIZER' in globals():
            # 1. Transform the incoming text
            sms_vectorized = VECTORIZER.transform([sms_body])
            
            # 2. Predict the category
            predicted_category = MODEL.predict(sms_vectorized)[0]
            
            # 3. Get confidence score
            probabilities = MODEL.predict_proba(sms_vectorized)[0]
            confidence_score = float(max(probabilities))
        else:
            # Fallback if the model failed to load (e.g., model.pkl is missing)
            predicted_category = "Uncategorized"
            confidence_score = 0.0

        return jsonify({
            'suggested_category': predicted_category,
            'confidence_score': round(confidence_score, 4)
        })

    except Exception as e:
        # Cloud Run requires a 500 on internal errors
        return jsonify({'error': f'Internal Server Error: {e}'}), 500

if __name__ == '__main__':
    # Use Gunicorn in production/Cloud Run; Flask is for local testing (if you could)
    app.run(debug=True, host='0.0.0.0', port=int(os.environ.get('PORT', 8080)))
