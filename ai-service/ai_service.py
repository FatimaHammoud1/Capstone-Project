"""
FastAPI Service Wrapper for LangGraph Multi-Agent System

This service exposes your LangGraph workflow as a REST API for Spring Boot integration.
"""
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Dict, Optional
import os
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

app = FastAPI(
    title="Personality Test AI Service",
    description="AI-powered career guidance and job matching",
    version="1.0.0"
)

# CORS configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify exact origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ============================================================================
# REQUEST/RESPONSE MODELS
# ============================================================================

class StudentInfo(BaseModel):
    name: str
    email: str
    gender: str

class CompleteAIRequest(BaseModel):
    attemptId: int
    personalityCode: str  # e.g., "R-I-A"
    studentInfo: StudentInfo
    metricScores: Dict[str, int]

class CompleteAIResponse(BaseModel):
    personalityCode: str
    careerRecommendations: str
    learningPath: str
    jobMatches: str
    emailSent: bool

# ============================================================================
# LANGGRAPH INTEGRATION
# ============================================================================

# Import your LangGraph workflow
# IMPORTANT: Make sure your langgraph_workflow.py file is in this directory
# and exports 'app' variable (the compiled LangGraph)
try:
    from langgraph_workflow import app as langgraph_app
    print("✅ LangGraph workflow loaded successfully")
except ImportError as e:
    print(f"⚠️  Warning: Could not import LangGraph workflow: {e}")
    print("   Please ensure langgraph_workflow.py is in the ai-service directory")
    langgraph_app = None

# ============================================================================
# API ENDPOINTS
# ============================================================================

@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "service": "Personality Test AI Service",
        "status": "running",
        "version": "1.0.0",
        "endpoints": {
            "health": "/health",
            "complete_analysis": "/api/ai/complete-analysis",
            "reindex": "/api/admin/reindex-documents"
        }
    }

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "service": "AI Analysis Service",
        "langgraph_loaded": langgraph_app is not None
    }

@app.post("/api/ai/complete-analysis", response_model=CompleteAIResponse)
async def complete_analysis(request: CompleteAIRequest):
    """
    Complete AI analysis pipeline:
    1. RAG retrieves career info based on personality code
    2. Learning path agent suggests universities/courses
    3. Job matching agent finds relevant jobs
    4. LLM formats comprehensive email
    5. Email sent to student
    
    Args:
        request: CompleteAIRequest with personality code and student info
        
    Returns:
        CompleteAIResponse with career recommendations, learning path, jobs, and email status
    """
    if langgraph_app is None:
        raise HTTPException(
            status_code=500,
            detail="LangGraph workflow not loaded. Please check langgraph_workflow.py"
        )
    
    try:
        print(f"\n{'='*60}")
        print(f"🚀 Starting Complete AI Analysis")
        print(f"   Attempt ID: {request.attemptId}")
        print(f"   Personality Code: {request.personalityCode}")
        print(f"   Student: {request.studentInfo.name}")
        print(f"{'='*60}\n")
        
        # Prepare initial state for LangGraph
        initial_state = {
            "query": "complete_analysis",  # Trigger complete workflow
            "code": request.personalityCode,
            "student_info": {
                "name": request.studentInfo.name,
                "email": request.studentInfo.email,
                "gender": request.studentInfo.gender,
                "age": None,
                "location": None
            },
            "scores": request.metricScores,
            "answers": None,  # Already calculated by Spring Boot
            
            # Workflow outputs (will be populated)
            "rag_output": None,
            "api_results": None,
            "job_answer": None,
            "email_status": None,
            
            # Auto-approve email for this flow
            "human_email_decision": "APPROVE",
        }
        
        # Execute LangGraph workflow
        config = {
            "configurable": {
                "thread_id": f"complete_{request.attemptId}"
            }
        }
        
        print("🔄 Executing LangGraph workflow...")
        result = langgraph_app.invoke(initial_state, config)
        
        # Extract results
        final_answer = result.get("final_answer", {})
        
        print("\n✅ Workflow completed successfully")
        print(f"   Career Recommendations: {len(final_answer.get('rag_output', ''))} chars")
        print(f"   Learning Path: {len(final_answer.get('api_results', ''))} chars")
        print(f"   Job Matches: Available" if final_answer.get('job_answer') else "   Job Matches: None")
        print(f"   Email Status: {final_answer.get('email_status', 'Not sent')}")
        
        # Format response
        import json
        response = CompleteAIResponse(
            personalityCode=request.personalityCode,
            careerRecommendations=final_answer.get("rag_output", ""),
            learningPath=final_answer.get("api_results", ""),
            jobMatches=json.dumps(final_answer.get("job_answer", {}), ensure_ascii=False),
            emailSent=any(x in final_answer.get("email_status", "").lower() for x in ["success", "sent", "تم"])
        )
        
        return response
        
    except Exception as e:
        print(f"\n❌ Error in complete analysis: {str(e)}")
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/admin/reindex-documents")
async def force_reindex():
    """
    Admin endpoint to force document reindexing.
    Call this after uploading new career documents.
    
    Returns:
        Success message
    """
    try:
        import os
        from rag.rag_step_4_vector_db import get_db_collection
        
        my_rag_collection = get_db_collection()
        
        # Delete existing index
        all_ids = [f"chunk_{i}" for i in range(my_rag_collection.count())]
        if all_ids:
            my_rag_collection.delete(ids=all_ids)
            print(f"🗑️  Deleted {len(all_ids)} chunks from index")
        
        # Delete metadata to force reindex
        metadata_file = "./chroma_persist/index_metadata.json"
        if os.path.exists(metadata_file):
            os.remove(metadata_file)
            print("🗑️  Deleted index metadata")
        
        return {
            "success": True,
            "message": "Index cleared. Next query will trigger reindexing.",
            "chunks_deleted": len(all_ids)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# ============================================================================
# STARTUP
# ============================================================================

if __name__ == "__main__":
    import uvicorn
    
    print("\n" + "="*60)
    print("🚀 Starting AI Service")
    print("="*60)
    print(f"   Service: Personality Test AI")
    print(f"   Host: 0.0.0.0")
    print(f"   Port: 5000")
    print(f"   Docs: http://localhost:5000/docs")
    print("="*60 + "\n")
    
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=5000,
        log_level="info"
    )
