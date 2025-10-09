import os
from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/')
def health_check():
    return "Backend is running!", 200

@app.route('/categorize', methods=['POST'])
def categorize():
    data = request.get_json()
    # your model logic here
    return jsonify({"category": "Test"})

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8080))
    app.run(host="0.0.0.0", port=port)
