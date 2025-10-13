# Dockerfile is located in: /project-root/categorizer/Dockerfile

FROM python:3.11-slim

WORKDIR /app

# Install build tools and system dependencies (optimized for data science/financial libs)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    gfortran \
    libopenblas-dev \
    liblapack-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy Python dependencies from the 'categorizer' folder
COPY categorizer/requirements.txt .
RUN pip install --upgrade pip
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code from the 'categorizer' folder
COPY categorizer/categorize_api.py .

# Copy the 'functions' folder from the project root
COPY functions/ ./functions

# Set environment variable
ENV PORT=8080

# Expose the port (informative)
EXPOSE 8080

# Run the FastAPI app with the Gunicorn/Uvicorn production combo
CMD ["gunicorn", "-w", "4", "-k", "uvicorn.workers.UvicornWorker", "categorize_api:app", "--bind", "0.0.0.0:8080"]
