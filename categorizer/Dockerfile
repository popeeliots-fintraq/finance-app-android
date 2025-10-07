# Use the official lightweight Python image
FROM python:3.11-slim

# Set the working directory in the container
WORKDIR /app

# Copy the requirements file and install dependencies
COPY categorizer/requirements.txt requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Copy the application code and the pre-trained model file
# **IMPORTANT:** You must ensure 'model.pkl' exists in your repo!
COPY categorizer/categorizer_api.py .
COPY categorizer/model.pkl .

# Expose the port (Cloud Run automatically injects $PORT)
ENV PORT 8080

# Run the app using gunicorn to ensure high performance
# The entry point is the Gunicorn process, which hosts the Flask app
CMD exec gunicorn --bind :$PORT --workers 1 --threads 8 --timeout 0 categorizer_api:app
