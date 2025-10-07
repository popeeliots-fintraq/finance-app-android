import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
import pickle

# --- 1. Load Data ---
df = pd.read_csv('transactions.csv')

# --- 2. Preprocessing and Feature Engineering (Phase 1 Complete) ---
# Clean the text (simple cleaning for now)
df['Description'] = df['Description'].str.lower().str.replace(r'[^a-z\s]', '', regex=True)

# Define the feature (text) and the target (category)
X = df['Description']
y = df['Category']

# Use TfidfVectorizer to convert text into numerical features
vectorizer = TfidfVectorizer(stop_words='english')
X_vectorized = vectorizer.fit_transform(X)

# --- 3. Model Training ---
model = LogisticRegression()
model.fit(X_vectorized, y)

# --- 4. Serialization (Creating model.pkl) ---
# Save both the vectorizer and the trained model as a single object
with open('model.pkl', 'wb') as file:
    pickle.dump({
        'model': model,
        'vectorizer': vectorizer
    }, file)

print("Model training complete. 'model.pkl' created successfully in the categorizer folder.")
