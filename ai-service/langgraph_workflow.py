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
        timeout=60,
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
    
    folder_path = "./rag/uploaded_files/"
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
    
    # Query RAG
    personality_code = state.get("code", "")
    if not personality_code:
        return {"rag_output": "رمز الشخصية غير متوفر."}
    
    try:
        query = f"""بناءً على رمز الشخصية {personality_code} حسب نظرية هولند:
1. اشرح سمات الشخصية بالتفصيل
2. قدم توصيات مهنية مفصلة
3. نظم الإجابة تحت عنوانين: "سمات الشخصية" و "التوصيات المهنية"
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


def learn_agent(state: AppState):
    """
    Learning path agent using DeepSeek.
    """
    print("\n📚 Learning Path Agent")
    
    code = state.get("code", "")
    
    # Create agent with tools
    agent = create_agent(
        model=llm,
        tools=[universities_tool, courses_tool],
        system_prompt=f"""
أنت خبير إرشاد أكاديمي متخصص في الأنظمة التعليمية.

مهمتك: تقديم خطة تعليمية شاملة لرمز الشخصية {code}.

التعليمات:
1. استخدم أدوات (Universities Tool) وَ (Courses Tool) للحصول على معلومات دقيقة.
2. قدم قائمة بـ 3 جامعات على الأقل، مع تحديد الكلية/التخصص المناسب لكل جامعة ورابط الموقع الرسمي.
3. قدم قائمة بـ 3 دورات تدريبية عبر الإنترنت على الأقل، مع ذكر المنصة والرابط المباشر.
4. يجب أن تكون الإجابة كاملة باللغة العربية، منظمة بوضوح، وتحتوي على روابط فعلية.
5. اربط بين السمات الشخصية (S, C, I, etc.) وبين سبب اختيارك لهذه التخصصات.
"""
    )
    
    result = agent.invoke({"messages": f"توصيات تعليمية لرمز {code}"})
    api_results = result["messages"][-1].content
    
    return {"api_results": api_results}


def node_fetch_jobs(state: AppState):
    """
    Fetch jobs from API.
    """
    print("\n💼 Fetching Jobs")
    
    try:
        url = "https://jobicy.com/api/v2/remote-jobs?count=10&geo=canada&industry=dev"
        r = requests.get(url, timeout=20)
        r.raise_for_status()
        data = r.json()
        return {"job_answer": {"jobs": data.get("jobs", [])[:5]}}
    except Exception as e:
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
        jobs_text = "\n\n## 💼 فرص العمل:\n\n"
        for i, job in enumerate(job_list[:5], 1):
            jobs_text += f"{i}. {job.get('title', 'N/A')} - {job.get('company', 'N/A')}\n"
    
    # System prompt
    system_prompt = f"""
أنت مساعد ذكي رائد في الإرشاد المهني والأكاديمي.

مهمتك: صياغة بريد إلكتروني ملهم وشامل للطالب: "{student_name}".

المحتوى المطلوب تضمينه:
1. مقدمة ترحيبية مهنية.
2. تحليل معمق لرمز الشخصية: {code}.
3. التوصيات المهنية (من RAG): {rag}
4. الخطة التعليمية (الجامعات والدورات مع الروابط): {learning}
5. فرص العمل المتاحة: {jobs_text if jobs_text else "سيتم تحديث فرص العمل قريباً."}

التعليمات الهامة:
- يجب أن يكون البريد بالكامل باللغة العربية.
- تأكد من ظهور روابط المواقع (URLs) بشكل واضح وقابل للضغط.
- استخدم تنسيق Markdown لتحسين المظهر (عناوين، قوائم، نقاط).
- جهّز JSON بالصيغة التالية:
  {{
    "recipient_email": "{recipient_email}",
    "subject": "نتائج تحليلك المهني والأكاديمي الشامل - مشروع Capstone",
    "body": "..."
  }}
- بعد تجهيز الـ JSON، استدعِ أداة (send_email_tool) فوراً لإرسال البريد.
"""
    
    # Create agent
    agent = create_agent(
        model=llm,
        tools=[send_email_tool],
        system_prompt=system_prompt
    )
    
    result = agent.invoke({"messages": "أنشئ وأرسل البريد الآن"})
    final_message = result["messages"][-1].content
    
    return {"email_status": final_message}


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
graph.add_node("learn", learn_agent)
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
