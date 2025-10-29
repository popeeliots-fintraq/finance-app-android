import os
import pickle
import io
import json
import re
from typing import Optional, Dict, Any

# --- FASTAPI / PYDANTIC IMPORTS ---
from fastapi import FastAPI, Request, Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from pydantic import BaseModel
from datetime import datetime

# --- CLOUD SERVICE IMPORTS ---
from google.cloud import storage
import firebase_admin
from firebase_admin import firestore, credentials, auth
from firebase_admin.exceptions import FirebaseError

# --- API KEY IMPORTS ---
def get_expected_api_key():
    """Reads the expected API Key from the server's environment."""
    return os.getenv("FIN_TRAQ_SYSTEM_API_KEY", "")

# --- V2 Pydantic Schemas for Validation ---
class RawSmsIn(BaseModel):
    """V2 DTO for sending raw SMS data to the backend's high-speed IngestionService."""
    user_id: Optional[str] = None 
    raw_text: str
    source_type: str
    local_timestamp: int # Long equivalent in Python

class CategorizationOut(BaseModel):
    """V2 DTO for the synchronous response from the Categorizer."""
    suggested_category: str
    confidence_score: float
    user_id: str # The verified user ID

# --- 1. CONFIGURATION & INITIALIZATION ---
MODEL_BUCKET = os.environ.get("MODEL_BUCKET", "fintraq-models")
MODEL_OBJECT = os.environ.get("MODEL_OBJECT", "model.pkl")

# 🚨 CRITICAL FIX: Use the current working directory, which is '/app' inside the container.
# This assumes the Dockerfile copies the 'functions' directory directly into /app.
CATEGORY_MAP_FILE = os.path.join(os.getcwd(), "functions", "merchant-map.json")


# 🚨 CRITICAL FIX: Explicitly initialize Firebase using the file path environment variable
try:
    cred_path = os.environ.get("FIREBASE_CONFIG_PATH")
    if cred_path and os.path.exists(cred_path):
        cred = credentials.Certificate(cred_path)
        firebase_admin.initialize_app(cred)
        print("✅ Firebase client initialized using service account JSON.")
    else:
        # This relies on the Cloud Run service account having appropriate roles (ideal for production)
        firebase_admin.initialize_app()
        print("✅ Firebase client initialized using default credentials (no explicit JSON file found).")
    db = firestore.client()
except Exception as e:
    print(f"⚠️ WARNING: Failed to initialize Firebase/Firestore: {e}")
    db = None

# --- 2. CATEGORY MAP LOADING ---
CATEGORY_MAP: Dict[str, Any] = {}
try:
    print(f"Attempting to load Category Map from: {CATEGORY_MAP_FILE}")
    with open(CATEGORY_MAP_FILE, "r") as f:
        CATEGORY_MAP = json.load(f)
    print(f"✅ Loaded {len(CATEGORY_MAP)} entries from {CATEGORY_MAP_FILE}.")
except FileNotFoundError:
    print(f"❌ CRITICAL ERROR: Category Map file not found at {CATEGORY_MAP_FILE}. Ensure Docker COPY is correct.")
except Exception as e:
    print(f"⚠️ WARNING: Failed to load {CATEGORY_MAP_FILE}: {e}")

# --- 3. ML MODEL LOADING ---
def load_model_from_gcs(bucket_name=MODEL_BUCKET, object_name=MODEL_OBJECT):
    """Loads the model and vectorizer from Google Cloud Storage."""
    print(f"Attempting to load model from GCS: {bucket_name}/{object_name}")
    client = storage.Client()
    bucket = client.bucket(bucket_name)
    blob = bucket.blob(object_name)
    data = blob.download_as_bytes()
    return pickle.load(io.BytesIO(data))

MODEL = None
VECTORIZER = None
try:
    MODEL_AND_VECTORIZER = load_model_from_gcs()
    if isinstance(MODEL_AND_VECTORIZER, dict):
        MODEL = MODEL_AND_VECTORIZER.get('model')
        VECTORIZER = MODEL_AND_VECTORIZER.get('vectorizer')
    elif isinstance(MODEL_AND_VECTORIZER, tuple) and len(MODEL_AND_VECTORIZER) == 2:
        MODEL, VECTORIZER = MODEL_AND_VECTORIZER
    print("✅ ML Model loaded from GCS")
except Exception as e:
    print(f"⚠️ WARNING: Failed to load model from GCS: {e}. Ensure GCS credentials are correct and the file exists. Falling back to rule-only.")

# --- 4. CATEGORIZATION LOGIC ---
def rule_based_categorize(text_lower: str, full_map: Dict[str, Any]):
    """Applies simple rules for high-confidence categorization."""
    if any(x in text_lower for x in ["salary", "credited to your", "has been credited"]):
        return "Income - Salary", 0.99
    if "atm" in text_lower:
        return "Cash Withdrawal", 0.90
    for merchant, category in full_map.items():
        if merchant in text_lower:
            return category, 0.9
