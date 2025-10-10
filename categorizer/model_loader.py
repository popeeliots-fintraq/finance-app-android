from google.cloud import storage
import pickle
import io
import os

# Environment variables (can override defaults)
MODEL_BUCKET = os.environ.get("MODEL_BUCKET", "fintraq-models")
MODEL_OBJECT = os.environ.get("MODEL_OBJECT", "model.pkl")

def load_model_from_gcs(bucket_name=MODEL_BUCKET, object_name=MODEL_OBJECT):
    """Load the pickled model from Google Cloud Storage"""
    client = storage.Client()
    bucket = client.bucket(bucket_name)
    blob = bucket.blob(object_name)
    data = blob.download_as_bytes()   # download in-memory
    model = pickle.load(io.BytesIO(data))
    return model

# Usage example (replace any old local load code)
model = load_model_from_gcs()
