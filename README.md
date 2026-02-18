
# 🎓 Career Consultation Platform - Backend & AI Services

A comprehensive digital platform that integrates AI-based personality testing, career guidance, exhibition management, and financial aid services. This repository contains the **Spring Boot backend** and **AI microservices** components.

> **Note:** This repository contains the backend and AI services only. The Flutter mobile and React web frontends are maintained in separate repositories by team members.

---

## 📋 Table of Contents

- [Overview](#overview)
- [System Architecture](#system-architecture)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [AI Services](#ai-services)
- [Testing](#testing)
- [Deployment](#deployment)
- [Team](#team)

---

## 🌟 Overview

The Career Consultation Platform is a senior capstone project developed in collaboration with the **Islamic Career Guidance and Counseling Association**. The platform digitizes traditional career counseling services, enabling students to:

- Complete **personality assessments** based on Holland's RIASEC theory
- Receive **AI-powered career recommendations** using RAG and LangChain
- Access personalized **job suggestions and learning paths**
- Register for **career exhibitions** organized by educational institutions
- Apply for **financial aid** with transparent approval workflows


---

## 🏗️ System Architecture

The system follows a microservices architecture with clear separation of concerns:

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Applications                      │
│              (Flutter Mobile & React Web - Separate Repos)   │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  │ REST API
                  │
┌─────────────────▼───────────────────────────────────────────┐
│              Spring Boot Backend (This Repo)                 │
│  • User Authentication & Authorization (JWT + OAuth2)        │
│  • Test Management & Student Attempts                        │
│  • Exhibition Management & Booth Allocation                  │
│  • Financial Aid Processing                                  │
│  • Asynchronous AI Task Orchestration                        │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  │ REST API Calls
                  │
┌─────────────────▼───────────────────────────────────────────┐
│              AI Microservices (This Repo)                    │
│                                                               │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  1. ML Service (FastAPI)                            │   │
│  │     • SVM Personality Classification                │   │
│  │     • Trained on 139 RIASEC personality codes       │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                               │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  2. RAG Service (FastAPI)                           │   │
│  │     • Document Loading & Chunking                   │   │
│  │     • Sentence Transformers Embeddings              │   │
│  │     • ChromaDB Vector Storage                       │   │
│  │     • Semantic Similarity Search                    │   │
│  │     • LLM-Powered Career Recommendations            │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                               │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  3. LangGraph Multi-Agent Workflow (FastAPI)        │   │
│  │     • RAG Agent: Career document analysis           │   │
│  │     • Learning Agent: University/course matching    │   │
│  │     • Job Agent: Job recommendations                │   │
│  │     • Email Agent: Automated report delivery        │   │
│  └─────────────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────────┘
                  │
                  │
┌─────────────────▼───────────────────────────────────────────┐
│                    Data Layer                                │
│  • MySQL 8.0 Database                                        │
│  • Firebase Cloud Storage (Documents)                        │
│  • Railway Volumes (RAG Document Persistence)                │
└─────────────────────────────────────────────────────────────┘
```

---

## ✨ Key Features

### 🧠 AI-Powered Personality Assessment

- **RIASEC-based testing** with customizable questions
- **SVM machine learning model** for personality code prediction
- **Multi-agent AI analysis** using LangGraph orchestration
- **RAG-based career recommendations** from organizational documents

### 📊 Exhibition Management

- **Full lifecycle management**: creation, venue requests, participant invitations
- **Multi-stakeholder coordination**: universities, schools, activity providers, municipalities
- **Booth allocation** with zone and number assignment
- **Attendance tracking** and student feedback collection
- **Financial analytics**: expenses, revenues, net profit tracking

### 💰 Financial Aid System

- **Student applications** with document upload (ID, grades, fee proof)
- **Donor budget management** with automatic allocation
- **Approval workflows** with transparent tracking
- **Automatic refund** on cancellation

### 🔐 Security & Authentication

- **JWT-based stateless authentication**
- **OAuth2 Google Sign-In**
- **Role-based access control** (Student, Organization Owner, Admin, etc.)
- **BCrypt password encryption**

---

## 🛠️ Technology Stack

### Backend (Spring Boot)

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Programming Language |
| Spring Boot | 3.5.5 | Backend Framework |
| Spring Web | - | RESTful APIs |
| Spring Data JPA | - | ORM & Database Access |
| Spring Security | - | Authentication & Authorization |
| MySQL | 8.0 | Relational Database |
| MapStruct | 1.5.5 | DTO Mapping |
| Lombok | 1.18.32 | Boilerplate Reduction |
| SpringDoc OpenAPI | 2.7.0 | API Documentation |

### AI Services (Python/FastAPI)

| Technology | Version | Purpose |
|------------|---------|---------|
| Python | 3.11+ | Programming Language |
| FastAPI | 0.109.0 | AI Microservices Framework |
| scikit-learn | - | SVM Classification |
| LangChain | 0.1.6 | LLM Orchestration |
| LangGraph | 0.0.20 | Multi-Agent Workflows |
| sentence-transformers | 2.3.1 | Text Embeddings |
| ChromaDB | 0.4.22 | Vector Database |
| PyPDF2 | 3.0.1 | PDF Processing |
| python-docx | 1.1.0 | DOCX Processing |

### Testing & DevOps

| Technology | Purpose |
|------------|---------|
| JUnit 5 | Unit Testing Framework |
| Mockito | Mocking & Test Isolation |
| Railway | Cloud Deployment |
| GitHub | Version Control |
| Firebase Storage | Document Management |

---

## 📁 Project Structure

```
career-consultation-platform/
│
├── backend/                          # Spring Boot Application
│   ├── src/main/java/
│   │   └── com/career/platform/
│   │       ├── config/              # Security, CORS, Async configs
│   │       ├── controller/          # REST API endpoints
│   │       ├── service/             # Business logic
│   │       ├── repository/          # JPA repositories
│   │       ├── model/               # Entity classes
│   │       ├── dto/                 # Data Transfer Objects
│   │       └── exception/           # Custom exceptions
│   ├── src/test/java/               # JUnit & Mockito tests
│   └── pom.xml                      # Maven dependencies
│
├── ai-services/                      # Python AI Microservices
│   ├── ml_service/                  # SVM Personality Classifier
│   │   ├── ml_service.py           # FastAPI ML endpoint
│   │   ├── model/                  # Trained SVM artifacts
│   │   └── requirements.txt
│   │
│   ├── rag_service/                 # RAG Career Recommendations
│   │   ├── rag/
│   │   │   ├── rag_step_1_loading.py
│   │   │   ├── rag_step_2_chunking.py
│   │   │   ├── rag_step_3_embeddings.py
│   │   │   ├── rag_step_4_vector_db.py
│   │   │   ├── rag_step_6_similarity.py
│   │   │   ├── rag_step_7_prompt.py
│   │   │   └── rag_step_8_call_llm.py
│   │   └── requirements.txt
│   │
│   └── langgraph_service/           # Multi-Agent Workflow
│       ├── langgraph_workflow.py   # Agent orchestration
│       ├── langgraph_service.py    # FastAPI endpoint
│       └── requirements.txt
│
└── README.md                         # This file
```

---

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed:

- **Java 21** or higher
- **Maven 3.8+**
- **Python 3.11+**
- **MySQL 8.0**
- **Git**

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/your-username/career-consultation-platform.git
cd career-consultation-platform
```

### 2️⃣ Setup MySQL Database

```sql
CREATE DATABASE career_consultation;
CREATE USER 'career_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON career_consultation.* TO 'career_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3️⃣ Configure Spring Boot

Edit `backend/src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/career_consultation
spring.datasource.username=career_user
spring.datasource.password=your_password

# JWT Configuration
jwt.secret=your-secret-key-here-min-256-bits
jwt.expiration=3600000

# AI Service URLs
ai.ml.service.url=http://localhost:8001
ai.rag.service.url=http://localhost:8002
ai.langgraph.service.url=http://localhost:8003
```

### 4️⃣ Run Spring Boot Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend will be available at: `http://localhost:8080`

### 5️⃣ Setup Python AI Services

#### ML Service (Port 8001)

```bash
cd ai-services/ml_service
pip install -r requirements.txt
uvicorn ml_service:app --host 0.0.0.0 --port 8001
```

#### RAG Service (Port 8002)

```bash
cd ai-services/rag_service
pip install -r requirements.txt
uvicorn rag_service:app --host 0.0.0.0 --port 8002
```

#### LangGraph Service (Port 8003)

```bash
cd ai-services/langgraph_service
pip install -r requirements.txt
uvicorn langgraph_service:app --host 0.0.0.0 --port 8003
```

---

## 📚 API Documentation

Once the backend is running, access interactive API documentation:

**Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Sample API Endpoints

#### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login with credentials (returns JWT) |
| POST | `/api/auth/google` | OAuth2 Google Sign-In |
| POST | `/api/auth/refresh` | Refresh access token |

#### Personality Tests

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tests` | Get all active tests |
| POST | `/api/tests` | Create new test (ORG_OWNER only) |
| POST | `/api/tests/{id}/attempt` | Start test attempt |
| POST | `/api/attempts/{id}/submit` | Submit answers |
| POST | `/api/attempts/{id}/finalize` | Finalize & get results |

#### AI Analysis

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/ai/predict-personality` | Get ML personality prediction |
| POST | `/api/ai/analyze` | Trigger full AI analysis workflow |

#### Exhibitions

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/exhibitions` | Create exhibition |
| POST | `/api/exhibitions/{id}/venue-request` | Request venue |
| POST | `/api/exhibitions/{id}/invite-university` | Invite university |
| GET | `/api/exhibitions/{id}/registrations` | View registrations |
| POST | `/api/exhibitions/{id}/attendance` | Mark attendance |

#### Financial Aid

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/financial-aid/apply` | Submit aid request |
| GET | `/api/financial-aid/requests` | View all requests (ORG_OWNER) |
| POST | `/api/financial-aid/{id}/review` | Approve/reject request |

---

## 🤖 AI Services

### 1. ML Personality Classifier

**Service:** `ml_service.py`  
**Endpoint:** `POST http://localhost:8001/predict`

Predicts RIASEC personality code using trained SVM model.

**Request:**

```json
{
  "answers": {
    "Q1": 4,
    "Q2": 2,
    "Q3": 5
  }
}
```

**Response:**

```json
{
  "predicted_code": "R-I-A",
  "confidence": 0.87
}
```

### 2. RAG Career Recommender

**Modular Pipeline:**

1. **Loading** → Load PDFs/DOCX from uploaded documents
2. **Chunking** → Split into manageable chunks with metadata
3. **Embeddings** → Generate vectors using sentence-transformers
4. **Vector DB** → Store in ChromaDB with smart caching
5. **Similarity Search** → Retrieve relevant career documents
6. **Prompt Construction** → Build context-aware prompts
7. **LLM Generation** → Generate personalized recommendations

### 3. LangGraph Multi-Agent Workflow

**Service:** `langgraph_service.py`  
**Endpoint:** `POST http://localhost:8003/analyze`

Orchestrates 4 specialized agents:

- **RAG Agent:** Retrieves career info from documents
- **Learning Agent:** Suggests universities/courses (with fallback)
- **Job Agent:** Matches relevant job opportunities
- **Email Agent:** Sends comprehensive report to student

**Request:**

```json
{
  "student_email": "student@example.com",
  "personality_code": "R-I-A",
  "metrics": {
    "realistic": 85,
    "investigative": 72,
    "artistic": 68
  }
}
```

**Response:**

```json
{
  "status": "success",
  "career_recommendations": "Based on your R-I-A profile...",
  "learning_paths": [
    {
      "university": "Example University",
      "courses": ["Engineering", "Computer Science"]
    }
  ],
  "job_matches": [
    {
      "title": "Software Engineer",
      "match_score": 0.92
    }
  ],
  "email_sent": true
}
```

---

## 🧪 Testing

### Run Backend Unit Tests

```bash
cd backend
mvn test
```

### Test Coverage

- ✅ **FinancialAidService**
  - Request creation
  - Approval with budget deduction
  - Cancellation with refund
  - Authorization checks
  - Budget validation
  
- ✅ **TestAttemptService**
  - Attempt creation
  - Answer submission
  - Gender-based question filtering
  - Finalization logic
  - Validation rules

- ✅ **Authentication**
  - JWT token generation
  - Token validation
  - OAuth2 integration

- ✅ **Repository Layer**
  - CRUD operations
  - Custom queries
  - Mockito isolation

### View Test Results

Test reports are generated in `backend/target/surefire-reports/`

---

## 🚢 Deployment

### Production Environment: Railway

**Deployed Services:**

| Service | Type | Port | Description |
|---------|------|------|-------------|
| Spring Boot Backend | Java | 8080 | Main REST API |
| ML Service | Python/FastAPI | 8001 | SVM Classifier |
| RAG Service | Python/FastAPI | 8002 | Career Recommendations |
| LangGraph Service | Python/FastAPI | 8003 | Multi-Agent Workflow |
| MySQL Database | Database | 3306 | Data Persistence |
| Railway Volume | Storage | - | RAG Document Storage |

### CI/CD Pipeline

- ✅ Auto-deploy on push to `main` branch
- ✅ GitHub integration
- ✅ Environment variable management
- ✅ Health check monitoring

### Environment Variables

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...

# Security
JWT_SECRET=...
JWT_EXPIRATION=3600000

# AI Services
AI_ML_SERVICE_URL=https://...
AI_RAG_SERVICE_URL=https://...
AI_LANGGRAPH_SERVICE_URL=https://...

# Storage
FIREBASE_CREDENTIALS=...
```

---


## 📄 License

This project is part of a senior capstone requirement for the Bachelor of Science degree in Computer Sciences. All rights reserved.



