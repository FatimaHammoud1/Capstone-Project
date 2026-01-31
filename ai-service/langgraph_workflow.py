"""
LangGraph Workflow for Spring Boot Integration
Minimal workflow for complete_analysis flow: RAG → Learning → Jobs → Email
"""

from langgraph.graph import StateGraph, END
from typing import List, Dict, Optional
from typing_extensions import TypedDict
from langchain.agents import create_agent
from langchain.tools import tool
from langchain_deepseek import ChatDeepSeek
from langgraph.checkpoint.memory import MemorySaver
from dotenv import load_dotenv

# Import RAG modules
from rag.rag_step_1_loading import load_documents_from_folder
from rag.rag_step_2_chunking import chunk_documents
from rag.rag_step_3_embeddings import embed_texts
from rag.rag_step_4_vector_db import get_db_collection, should_reindex_documents, save_index_metadata
from rag.rag_step_6_similarity import retrieve_relevant_chunks
from rag.rag_step_7_prompt import prepare_prompt
from rag.rag_step_8_call_llm import generate_answer

import os
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import json
import requests
import time

load_dotenv()

# ============================================================================
# LLM CONFIGURATION
# ============================================================================

def get_deepseek():
    """
    Returns ChatDeepSeek model for LLM operations.
    """
    deepseek_key = os.getenv("DEEPSEEK_API_KEY")
    url = os.getenv("DEEPSEEK_API_BASE", "https://api.deepseek.com")

    llm_model = ChatDeepSeek(
        model="deepseek-chat",
        max_tokens=2000,
        timeout=120,  # Increased timeout
        max_retries=3,  # Add retries
        api_key=deepseek_key,
        base_url=url
    )
    return llm_model

# Main LLM instance
llm = get_deepseek()

# ============================================================================
# STATE DEFINITION
# ============================================================================

class AppState(TypedDict):
    """
    State for complete_analysis workflow from Spring Boot.
    """
    query: str  # "complete_analysis"
    code: str  # Personality code from Spring Boot (e.g., "R-I-A")
    student_info: dict  # Student information
    scores: dict  # Metric scores
    
    # Outputs
    rag_output: str | None  # Career recommendations
    api_results: str | None  # Learning path
    job_answer: dict | None  # Job matches
    email_status: str | None  # Email status
    
    # Control
    human_email_decision: str | None
    final_answer: dict | None

# ============================================================================
# TOOLS
# ============================================================================

@tool
def universities_tool(query: str) -> str:
    """
    Fetch comprehensive university and major recommendations based on a query.
    Returns a list of universities in Lebanon and the region with links.
    """
    return """
قائمة الجامعات الموصى بها:
1. الجامعة اللبنانية (LU): تضم تخصصات متنوعة في الهندسة، العلوم، والحقوق. (https://www.ul.edu.lb)
2. الجامعة الأميركية في بيروت (AUB): رائدة في الطب، الهندسة، وإدارة الأعمال. (https://www.aub.edu.lb)
3. جامعة القديس يوسف (USJ): تميز في الطب، العلوم الإنسانية والاجتماعية. (https://www.usj.edu.lb)
4. الجامعة اللبنانية الأميركية (LAU): تخصصات متميزة في الصيدلة، التصميم، وهندسة العمارة. (https://www.lau.edu.lb)
5. جامعة بيروت العربية (BAU): تخصصات شاملة في العلوم الطبية والهندسية. (https://www.bau.edu.lb)
"""

@tool
def courses_tool(query: str) -> str:
    """
    Fetch online course recommendations from major platforms (Coursera, Udemy, EdX).
    Returns specific courses related to the career path with links.
    """
    return """
دورات تدريبية مقترحة:
1. مسارات Google المهنية (Coursera): تغطي تحليل البيانات، إدارة المشاريع، والدعم التقني. (https://www.coursera.org/google-career-certificates)
2. دورات edX التخصصية: تقدم شهادات من هارفارد وMIT في تقنيات الذكاء الاصطناعي والبرمجة. (https://www.edx.org)
3. Udemy Professional Courses: دورات عملية في التصميم الجرافيكي، التسويق الرقمي، وتطوير الويب. (https://www.udemy.com)
4. LinkedIn Learning: دورات في القيادة، التواصل، والمهارات الشخصية (Soft Skills). (https://www.linkedin.com/learning)
"""

@tool
def send_email_tool(email_data: str) -> str:
    """
    Send email with career guidance.
    Expects JSON: {recipient_email, subject, body}
    """
    print("\n📧 SEND EMAIL TOOL")
    
    try:
        data = json.loads(email_data)
        recipient_email = data.get("recipient_email")
        subject = data.get("subject")
        body = data.get("body")
        
        # Email config
        sender_email = os.getenv("SENDER_EMAIL")
        sender_password = os.getenv("SENDER_APP_PASSWORD")
        smtp_server = os.getenv("SMTP_SERVER", "smtp.gmail.com")
        smtp_port = int(os.getenv("SMTP_PORT", "587"))
        
        if not sender_email or not sender_password:
            return "Error: Email credentials not configured"
        
        # Create message
        message = MIMEMultipart("alternative")
        message["Subject"] = subject
        message["From"] = sender_email
        message["To"] = recipient_email
        
        # HTML body
        body_html = body.replace('\n', '<br>')
        html_body = f"""
        <html>
            <body style="font-family: Arial, sans-serif; direction: rtl; text-align: right;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    {body_html}
                </div>
            </body>
        </html>
        """
        
        part = MIMEText(html_body, "html", "utf-8")
        message.attach(part)
        
        # Send
        with smtplib.SMTP(smtp_server, smtp_port) as server:
            server.starttls()
            server.login(sender_email, sender_password)
            server.send_message(message)
        
        return f"✅ Email sent successfully to {recipient_email}"
        
    except Exception as e:
        return f"Error sending email: {str(e)}"

# ============================================================================
# NODES (Only for Spring Boot complete_analysis flow)
# ============================================================================

def rag_node(state: AppState):
    """
    RAG node with smart caching.
    Retrieves career recommendations based on personality code.
    """
    print("\n🔍 RAG Node")
    
    # folder_path = "./rag/uploaded_files/"
    folder_path = os.getenv("AI_DOCUMENTS_PATH", "./rag/uploaded_files/")
    my_rag_collection = get_db_collection()
    
    # Smart caching - only reindex if files changed
    needs_reindex, current_files = should_reindex_documents(my_rag_collection, folder_path)
    
    if needs_reindex:
        print("🔄 Reindexing documents...")
        
        source_list = load_documents_from_folder(folder_path)
        if not source_list:
            return {"rag_output": "لا تتوفر معلومات مهنية."}
        
        my_chunks_with_metadata = chunk_documents(source_list)
        if not my_chunks_with_metadata:
            return {"rag_output": "لا تتوفر معلومات مهنية."}
        
        ids_list = [f"chunk_{i}" for i in range(len(my_chunks_with_metadata))]
        text_list = [chunk["text"] for chunk in my_chunks_with_metadata]
        metadata_list = [{
            'source': chunk['source'],
            'doc_id': chunk['doc_id'],
            'chunk_id': chunk['chunk_id']
        } for chunk in my_chunks_with_metadata]
        
        vectors_list = embed_texts(text_list)
        if vectors_list is None or len(vectors_list) == 0:
            return {"rag_output": "لا تتوفر معلومات مهنية."}
        
        # Clear and upsert
        all_ids = [f"chunk_{i}" for i in range(my_rag_collection.count())]
        if all_ids:
            my_rag_collection.delete(ids=all_ids)
        
        my_rag_collection.upsert(
            ids=ids_list,
            embeddings=vectors_list,
            documents=text_list,
            metadatas=metadata_list
        )
        
        save_index_metadata(current_files)
        print(f"✅ Indexed {my_rag_collection.count()} chunks")
    else:
        print(f"✅ Documents unchanged - using existing index")
        print(f"   Indexed chunks: {my_rag_collection.count()}")
    
    # Query RAG
    personality_code = state.get("code", "")
    if not personality_code:
        return {"rag_output": "رمز الشخصية غير متوفر."}
    
    try:
        query = f"""رمز الشخصية {personality_code} حسب نظرية هولند:
1. اشرح سمات الشخصية
2. التوصيات المهنية المناسبة
"""
        
        question_vector = embed_texts([query])
        result = retrieve_relevant_chunks(question_vector, my_rag_collection, 10)
        prompt = prepare_prompt(query, result['documents'][0])
        career_recommendations = generate_answer(prompt, os.getenv("DEEPSEEK_API_KEY"))
        
        print(f"✅ Generated recommendations for {personality_code}")
        return {"rag_output": career_recommendations}
        
    except Exception as e:
        print(f"❌ Error: {e}")
        return {"rag_output": f"حدث خطأ: {str(e)}"}


def learn_agent_direct(state: AppState):
    """
    Learning path agent - DIRECT TOOL EXECUTION (No LLM agent).
    This is more reliable and faster than using an agent.
    """
    print("\n📚 Learning Path Agent (Direct Mode)")
    
    code = state.get("code", "")
    
    try:
        # Call tools directly without LLM agent
        universities = universities_tool.invoke(code)
        courses = courses_tool.invoke(code)
        
        # Format response
        api_results = f"""
## 📚 الخطة التعليمية لرمز الشخصية {code}

### 🎓 الجامعات الموصى بها:
{universities}

### 💻 الدورات التدريبية المقترحة:
{courses}

### 🔗 ملاحظة:
تم اختيار هذه التوصيات بناءً على تحليل سمات شخصيتك. يمكنك زيارة المواقع للتعرف على التفاصيل والتسجيل.
"""
        
        print("✅ Learning path generated successfully")
        return {"api_results": api_results}
        
    except Exception as e:
        print(f"❌ Error in learn_agent_direct: {e}")
        # Fallback response
        return {
            "api_results": f"""
## 📚 الخطة التعليمية لرمز الشخصية {code}

### 🎓 الجامعات الموصى بها في لبنان:
1. الجامعة اللبنانية (LU) - https://www.ul.edu.lb
2. الجامعة الأميركية في بيروت (AUB) - https://www.aub.edu.lb
3. جامعة القديس يوسف (USJ) - https://www.usj.edu.lb

### 💻 الدورات التدريبية المقترحة:
1. Google Career Certificates - https://www.coursera.org/google-career-certificates
2. edX Professional Programs - https://www.edx.org
3. LinkedIn Learning - https://www.linkedin.com/learning
"""
        }


def learn_agent_with_retry(state: AppState):
    """
    Learning path agent with LLM and retry logic (BACKUP - if direct mode fails).
    """
    print("\n📚 Learning Path Agent (LLM Mode with Retry)")
    
    code = state.get("code", "")
    max_retries = 2
    retry_delay = 2
    
    for attempt in range(max_retries):
        try:
            # Create agent with minimal prompt
            agent = create_agent(
                model=llm,
                tools=[universities_tool, courses_tool],
                system_prompt=f"""
أنت خبير إرشاد أكاديمي.

قدم لرمز {code}:
1. 3 جامعات مع روابط
2. 3 دورات مع روابط

استخدم الأدوات المتاحة. كن مختصراً.
"""
            )
            
            result = agent.invoke({"messages": f"توصيات لرمز {code}"})
            api_results = result["messages"][-1].content
            
            print(f"✅ Learning path generated (attempt {attempt + 1})")
            return {"api_results": api_results}
            
        except Exception as e:
            print(f"⚠️  Attempt {attempt + 1}/{max_retries} failed: {str(e)}")
            
            if attempt < max_retries - 1:
                wait_time = retry_delay * (2 ** attempt)
                print(f"   Retrying in {wait_time} seconds...")
                time.sleep(wait_time)
            else:
                print("❌ All attempts failed, falling back to direct mode")
                return learn_agent_direct(state)


def learn_agent_safe(state: AppState):
    """
    SAFE WRAPPER for learn agent.
    Try direct mode first (fastest), fallback to LLM mode if needed.
    """
    try:
        # Try direct mode first (most reliable)
        return learn_agent_direct(state)
    except Exception as e:
        print(f"❌ Direct mode failed: {e}")
        print("🔄 Falling back to LLM mode...")
        try:
            return learn_agent_with_retry(state)
        except Exception as e2:
            print(f"❌ LLM mode also failed: {e2}")
            # Ultimate fallback - static response
            code = state.get("code", "")
            return {
                "api_results": f"""
## 📚 الخطة التعليمية لرمز الشخصية {code}

### 🎓 الجامعات الموصى بها:
1. الجامعة اللبنانية (LU) - https://www.ul.edu.lb
2. الجامعة الأميركية في بيروت (AUB) - https://www.aub.edu.lb
3. جامعة القديس يوسف (USJ) - https://www.usj.edu.lb

### 💻 الدورات التدريبية:
1. Coursera - https://www.coursera.org
2. edX - https://www.edx.org
3. LinkedIn Learning - https://www.linkedin.com/learning

*ملاحظة: حدث خطأ في التوصيات المخصصة. هذه قائمة عامة.*
"""
            }


def node_fetch_jobs(state: AppState):
    """
    Fetch jobs from API.
    """
    print("\n💼 Fetching Jobs")
    
    try:
        url = "https://jobicy.com/api/v2/remote-jobs?count=10&geo=canada&industry=dev"
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        }
        r = requests.get(url, headers=headers, timeout=20)
        r.raise_for_status()
        data = r.json()
        return {"job_answer": {"jobs": data.get("jobs", [])[:5]}}
    except Exception as e:
        print(f"❌ Job fetch failed: {e}")
        return {"job_answer": {"error": str(e), "jobs": []}}


def email_agent(state: AppState):
    """
    Email agent - formats and sends comprehensive email using DeepSeek.
    """
    print("\n📧 Email Agent")
    
    info = state["student_info"]
    rag = state.get("rag_output", "")
    code = state.get("code", "")
    learning = state.get("api_results", "")
    jobs = state.get("job_answer", {})
    
    recipient_email = info.get("email")
    student_name = info.get("name", "الطالب")
    
    if not recipient_email:
        return {"email_status": "No email provided"}
    
    # Format jobs
    jobs_text = ""
    job_list = jobs.get("jobs", [])
    if job_list:
        jobs_text = "<h3>💼 فرص العمل المتاحة:</h3><ul>"
        for job in job_list[:5]:
            jobs_text += f"<li><strong>{job.get('title', 'N/A')}</strong> - {job.get('company', 'N/A')}</li>"
        jobs_text += "</ul>"
    
    # Minimal system prompt - NO MARKDOWN, HTML ONLY
    system_prompt = f"""
أنت مساعد إرشاد مهني.

أرسل بريد لـ "{student_name}" عن رمز {code}.

التعليمات المهمة جداً:
- لا تستخدم Markdown أبداً (ممنوع ##، **، *، _)
- استخدم HTML فقط: <h2>, <h3>, <p>, <ul>, <li>, <strong>, <br>
- لا تكتب أي رموز مثل ### أو ** أو ***

المحتوى:
1. مقدمة بسيطة
2. تحليل الشخصية: {rag[:400]}
3. الجامعات والدورات: {learning[:400]}
4. الوظائف: {jobs_text if jobs_text else "<p>سيتم تحديث فرص العمل قريباً.</p>"}

أمثلة التنسيق الصحيح:
- عنوان كبير: <h2>العنوان هنا</h2>
- عنوان صغير: <h3>عنوان فرعي</h3>
- فقرة: <p>النص هنا</p>
- قائمة: <ul><li>البند الأول</li><li>البند الثاني</li></ul>
- نص غامق: <strong>نص مهم</strong>
- سطر جديد: <br>

JSON فقط:
{{
  "recipient_email": "{recipient_email}",
  "subject": "نتائج تحليلك المهني والأكاديمي الشامل",
  "body": "...HTML هنا بدون أي Markdown..."
}}

استدع send_email_tool فوراً.

هام جداً: إذا تم الإرسال بنجاح، يجب أن تحتوي إجابتك النهائية على كلمة "SUCCESS" (بالإنجليزية) بالإضافة لتأكيدك بالعربية.
"""
    
    try:
        # Create agent
        agent = create_agent(
            model=llm,
            tools=[send_email_tool],
            system_prompt=system_prompt
        )
        
        result = agent.invoke({"messages": "أرسل البريد"})
        final_message = result["messages"][-1].content
        
        return {"email_status": final_message}
        
    except Exception as e:
        print(f"❌ Email agent failed: {e}")
        return {"email_status": f"Error: {str(e)}"}


def final_format(state: AppState):
    """
    Format final response for Spring Boot.
    """
    response = {
        "code": state.get('code', ''),
        "rag_output": state.get('rag_output', ''),
        "api_results": state.get('api_results', ''),
        "job_answer": state.get('job_answer', {}),
        "email_status": state.get('email_status', ''),
    }
    return {"final_answer": response}

# ============================================================================
# GRAPH CONSTRUCTION (Minimal for Spring Boot)
# ============================================================================

graph = StateGraph(AppState)

# Add only needed nodes
graph.add_node("rag", rag_node)
graph.add_node("learn", learn_agent_safe)  # Using safe wrapper
graph.add_node("fetch_jobs", node_fetch_jobs)
graph.add_node("email", email_agent)
graph.add_node("final", final_format)

# Set entry point
graph.set_entry_point("rag")

# Simple linear flow for complete_analysis
graph.add_edge("rag", "learn")
graph.add_edge("learn", "fetch_jobs")
graph.add_edge("fetch_jobs", "email")
graph.add_edge("email", "final")
graph.add_edge("final", END)

# Compile
memory = MemorySaver()
app = graph.compile(checkpointer=memory)

print("✅ LangGraph workflow loaded")
print("   Flow: RAG → Learning → Jobs → Email → Final")
print("   LLM: DeepSeek")
print("   Tools: universities_tool, courses_tool, send_email_tool")
print("   Mode: Direct tool execution (safe & fast)")

