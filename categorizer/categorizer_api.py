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
    print("✅ Model
