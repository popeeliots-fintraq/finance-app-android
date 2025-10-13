FROM python:3.11-slim

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    gfortran \
    libopenblas-dev \
    liblapack-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy Python dependencies
COPY categorizer/requirements.txt .
RUN pip install --upgrade pip
RUN pip install --no-cache-dir -r requirements.txt

# Copy API and functions
COPY categorizer/categorize_api.py .
COPY functions ./functions

# Expose port
ENV PORT=8080

# Run app
CMD ["uvicorn", "categorize_api:app", "--host", "0.0.0.0", "--port", "8080"]
