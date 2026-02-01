#!/bin/bash
# Make sure you are in ai-service folder
cd ai-service/ml_service

# Run ML FastAPI service
uvicorn main:app --host 0.0.0.0 --port $PORT
