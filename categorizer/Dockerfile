# -------------------------------------------------
# Corrected Dockerfile for Cloud Run Categorizer
# -------------------------------------------------

# Use official Python 3.11 slim image
FROM python:3.11-slim

# Set the working directory in the container
WORKDIR /app

# Install system dependencies required for numpy/scikit-learn
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    gfortran \
    libopenblas-dev \
    liblapack-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements file and install Python dependencies
COPY requirements.txt .
RUN pip install --upgrade pip
RUN pip install --no-cache-dir -r requirements.txt

# ✅ Copy the actual API file
COPY categorize_api.py .

# ✅ Copy merchant map from /functions/ folder into container
COPY ../functions /app/functions

# Expose port (Cloud Run injects $PORT automatically)
ENV PORT=8080

# ✅ Run the Flask app via Gunicorn (Correct entrypoint reference)
CMD exec gunicorn --bind :$PORT --workers 1 --threads 8 --timeout 0 categorize_api:app
