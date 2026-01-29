# Use Python 3.10 slim image
FROM python:3.10-slim

# Set working directory
WORKDIR /app

# Install system dependencies (needed for some python packages)
RUN apt-get update && apt-get install -y \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements file first (for caching)
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the application code
# We copy the 'rag' folder specifically if it exists, and all python files
COPY . .

# Expose the standard port (can be overridden)
EXPOSE 5000

# Set a default command, but this is meant to be overridden in docker-compose or deployment
# Example Usage:
# For LangGraph: CMD ["uvicorn", "langgraph_service:app", "--host", "0.0.0.0", "--port", "5000"]
# For ML Service: CMD ["uvicorn", "ml_service:app", "--host", "0.0.0.0", "--port", "5001"]
CMD ["uvicorn", "langgraph_service:app", "--host", "0.0.0.0", "--port", "5000"]
