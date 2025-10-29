# Dockerfile is located in: /project-root/categorizer/Dockerfile

# --- STAGE 1: Builder ---
FROM python:3.11 AS builder

WORKDIR /app

# Add permissions fix to ensure build essentials can read files
RUN chmod -R a+rX /app

# Install build tools and system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    gfortran \
    libopenblas-dev \
    liblapack-dev \
    && rm -rf /var/lib/apt/lists/*

# CRITICAL FIX 1: Paths relative to the project root
COPY categorizer/requirements.txt .
RUN pip install --upgrade pip
RUN pip wheel --no-cache-dir --wheel-dir=/wheels -r requirements.txt


# --- STAGE 2: Final Image ---
FROM python:3.11-slim

WORKDIR /app

# Copy compiled wheels from the builder stage
COPY --from=builder /wheels /wheels
RUN pip install --no-cache /wheels/*
RUN rm -rf /wheels

# CRITICAL FIX 2: Paths relative to the project root
COPY categorizer/categorizer_api.py .

# CRITICAL FIX 3: Paths relative to the project root
COPY functions/ ./functions

# Set environment variable and port
ENV PORT=8080
EXPOSE 8080

# 🚨 CRITICAL FIX: Use the $PORT environment variable and Gunicorn with Uvicorn workers
# This forces the application to listen on the port (e.g., 8080) dynamically assigned by Cloud Run,
# and increases stability with multiple workers.
CMD ["gunicorn", "-w", "4", "-k", "uvicorn.workers.UvicornWorker", "--bind", "0.0.0.0:${PORT}", "categorizer_api:app"]
