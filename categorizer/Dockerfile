# Dockerfile is located in: /project-root/categorizer/Dockerfile

# --- STAGE 1: Builder (Handles the complex compilation) ---
# Use a full Python image to install all system dependencies and compile Python wheels
FROM python:3.11 AS builder

WORKDIR /app

# Install build tools and system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    gfortran \
    libopenblas-dev \
    liblapack-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy Python dependencies from the 'categorizer' folder (relative to project root)
COPY categorizer/requirements.txt .
RUN pip install --upgrade pip
# Install packages into wheels for a clean transfer to the next stage
RUN pip wheel --no-cache-dir --wheel-dir=/wheels -r requirements.txt


# --- STAGE 2: Final Image (The small, runnable image) ---
# Use the official Python slim image for a smaller, final container size
FROM python:3.11-slim

WORKDIR /app

# Copy compiled wheels from the builder stage
COPY --from=builder /wheels /wheels
# Install packages from the pre-compiled wheels
RUN pip install --no-cache /wheels/*

# Clean up wheels to save space
RUN rm -rf /wheels

# Copy application code from the 'categorizer' folder (relative to project root)
COPY categorizer/categorize_api.py .

# Copy the 'functions' folder from the project root
COPY functions/ ./functions

# Set environment variable and port
ENV PORT=8080
EXPOSE 8080

# Production CMD using Gunicorn and Uvicorn workers
CMD ["gunicorn", "-w", "4", "-k", "uvicorn.workers.UvicornWorker", "categorize_api:app", "--bind", "0.0.0.0:8080"]
