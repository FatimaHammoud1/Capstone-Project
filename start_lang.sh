#!/bin/bash
# Make sure you are in ai-service folder
cd ai-service/langgraph_service

# Run LangGraph FastAPI service
uvicorn langgraph_service:app --host 0.0.0.0 --port $PORT
