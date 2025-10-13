FROM python:3.11-slim

WORKDIR /app

# Install build tools and system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    gfortran \
    libopenblas-dev \
    liblapack-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy Python dependencies
COPY finance-app-android/categorizer/requirements.txt .
RUN pip install --upgrade pip
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code and functions
COPY finance-app-android/categorizer/categorize_api.py .
COPY functions/ ./functions

# Set environment variable
ENV PORT=8080

# Expose the port
EXPOSE 8080

# Run the FastAPI app with uvicorn
CMD ["uvicorn", "categorize_api:app", "--host", "0.0.0.0", "--port", "8080"]
