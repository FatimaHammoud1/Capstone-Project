# Python AI Service - Quick Start Guide

## 📋 Overview

This service wraps your existing LangGraph multi-agent system and exposes it as a REST API for Spring Boot integration.

## 🚀 Quick Start

### 1. Setup Environment

```bash
# Navigate to ai-service folder
cd backend/ai-service

# Create virtual environment
python -m venv venv

# Activate virtual environment
# Windows:
venv\Scripts\activate
# Mac/Linux:
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt
```

### 2. Configure Environment Variables

```bash
# Copy example env file
copy .env.example .env

# Edit .env and add your API keys
notepad .env
```

Required variables:
- `DEEPSEEK_API_KEY`: Your DeepSeek API key
- `SENDER_EMAIL`: Gmail address for sending emails
- `SENDER_APP_PASSWORD`: Gmail app password (not regular password!)

### 3. Add Career Documents

Place your career guidance documents in:
```
ai-service/rag/uploaded_files/
```

Supported formats: PDF, DOCX, TXT, MD, CSV, JSON, XLSX

### 4. Copy Your LangGraph Code

Copy your existing LangGraph workflow file to:
```
ai-service/langgraph_workflow.py
```

Make sure it exports the `app` variable (your compiled LangGraph).

### 5. Start the Service

```bash
python ai_service.py
```

The service will start on `http://localhost:5000`

## 📡 API Endpoints

### Health Check
```
GET http://localhost:5000/health
```

### Complete Analysis (Main Endpoint)
```
POST http://localhost:5000/api/ai/complete-analysis
Content-Type: application/json

{
  "attemptId": 123,
  "personalityCode": "R-I-A",
  "studentInfo": {
    "name": "أحمد محمد",
    "email": "ahmad@example.com",
    "gender": "MALE"
  },
  "metricScores": {
    "R": 45,
    "I": 42,
    "A": 40,
    "S": 30,
    "E": 28,
    "C": 25
  }
}
```

### Admin: Force Reindex Documents
```
POST http://localhost:5000/api/admin/reindex-documents
```

## 🔄 Integration with Spring Boot

Spring Boot will call this service after calculating the personality code:

```
Student takes test → Spring Boot calculates code → 
Python AI analyzes → Email sent → Results saved
```

## 📁 File Structure

```
ai-service/
├── rag/                          # RAG system
│   ├── uploaded_files/           # Career documents (add your files here)
│   ├── rag_step_1_loading.py    # Document loader
│   ├── rag_step_2_chunking.py   # Text chunker
│   ├── rag_step_3_embeddings.py # Embeddings
│   ├── rag_step_4_vector_db.py  # ChromaDB
│   ├── rag_step_6_similarity.py # Similarity search
│   ├── rag_step_7_prompt.py     # Prompt builder
│   └── rag_step_8_call_llm.py   # LLM caller
│
├── chroma_persist/               # ChromaDB storage (auto-created)
├── langgraph_workflow.py         # Your LangGraph code (copy here)
├── ai_service.py                 # FastAPI server
├── requirements.txt              # Dependencies
├── .env                          # Configuration (create from .env.example)
└── README.md                     # This file
```

## 🧪 Testing

### Test with curl:
```bash
curl -X POST http://localhost:5000/api/ai/complete-analysis \
  -H "Content-Type: application/json" \
  -d "{\"attemptId\":1,\"personalityCode\":\"R-I-A\",\"studentInfo\":{\"name\":\"Test\",\"email\":\"test@test.com\",\"gender\":\"MALE\"},\"metricScores\":{\"R\":45,\"I\":42,\"A\":40}}"
```

### Test with Python:
```python
import requests

response = requests.post(
    "http://localhost:5000/api/ai/complete-analysis",
    json={
        "attemptId": 1,
        "personalityCode": "R-I-A",
        "studentInfo": {
            "name": "أحمد",
            "email": "ahmad@test.com",
            "gender": "MALE"
        },
        "metricScores": {"R": 45, "I": 42, "A": 40}
    }
)

print(response.json())
```

## 🐛 Troubleshooting

### ChromaDB keeps reindexing
- Check if `chroma_persist/index_metadata.json` exists
- Verify file permissions
- Check if documents are actually changing

### Email not sending
- Verify Gmail app password (not regular password)
- Enable "Less secure app access" or use App Password
- Check SMTP settings in .env

### LangGraph errors
- Ensure `langgraph_workflow.py` is in the correct location
- Verify it exports `app` variable
- Check all dependencies are installed

## 📊 Monitoring

Watch the console output for:
- ✅ Document indexing status
- 📧 Email sending status
- 🔄 RAG query results
- ⚠️ Any errors or warnings

## 🔐 Security Notes

- Never commit `.env` file to git
- Keep API keys secure
- Use environment variables for sensitive data
- In production, use proper secrets management

## 📝 Next Steps

1. Add your career documents to `rag/uploaded_files/`
2. Copy your LangGraph code to `langgraph_workflow.py`
3. Configure `.env` with your API keys
4. Start the service and test
5. Integrate with Spring Boot

## 🆘 Need Help?

Check the logs in the console for detailed error messages.
All RAG steps print progress information.
