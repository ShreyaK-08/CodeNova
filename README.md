# CodeNova — Online Coding Practice & Skill Assessment Management Platform

[![Live Demo](https://img.shields.io/badge/Live%20Demo-Vercel-black.svg?style=for-the-badge&logo=vercel)](https://code-nova-psi.vercel.app)
[![API Status](https://img.shields.io/badge/Backend%20API-Railway-0B0D0E.svg?style=for-the-badge&logo=railway)](https://codenova-production-c3c6.up.railway.app)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.x-61DAFB.svg?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)

A full-stack, enterprise-grade online coding practice, proctored skill assessment, contest management, and support ticketing platform. Built with a modern React 18 single-page application, Spring Boot 3.3.4 REST API, MySQL database, and a multi-compiler server-side code execution engine.

---

## 🌐 Live Platform & Access Links

| Service | Platform | Live URL | Status |
|---|---|---|---|
| **Frontend Web App** | **Vercel** | **[https://code-nova-psi.vercel.app](https://code-nova-psi.vercel.app)** | 🟢 **Active & Live** |
| **Backend REST API** | **Railway** | **[https://codenova-production-c3c6.up.railway.app](https://codenova-production-c3c6.up.railway.app)** | 🟢 **Active & Live** |
| **MySQL Database** | **Railway Cloud** | `mysql.railway.internal:3306` | 🟢 **Connected** |

---

## 🔑 Demo Login Credentials

You can test all user roles directly on the live platform:

| Role | Username | Password | Features Accessible |
|---|---|---|---|
| **Platform Admin** | `admin` | `Admin@123` | Full administrative control, problem creation, host verification approvals, platform reports, analytics CSV export |
| **Student / Candidate** | `shreya` | `Demo@123` | Monaco editor practice, proctored assessments, contests, submission history, profile certificates |
| **Student / Candidate** | `likhil` | `Demo@123` | Interactive problem solving, contest attempts, support ticketing dialogue |
| **New Candidate** | *Self-Register* | *Your choice* | Instant registration via **[Sign Up](https://code-nova-psi.vercel.app/register)** with automated account verification |

---

## Table of Contents

- [Live Platform & Access Links](#-live-platform--access-links)
- [Demo Login Credentials](#-demo-login-credentials)
- [Project Overview](#project-overview)
- [Key Features & Modules](#key-features--modules)
- [Technology Stack](#technology-stack)
- [Architecture Overview](#architecture-overview)
- [Project Structure](#project-structure)
- [Local Installation & Setup](#local-installation--setup)
- [API Overview](#api-overview)
- [Execution & Proctoring Engine](#execution--proctoring-engine)
- [Support Ticketing & Email System](#support-ticketing--email-system)
- [Author](#author)

---

## Project Overview

**CodeNova** is an end-to-end coding education and assessment ecosystem designed to streamline developer practice, skill evaluations, and competitive programming. Unlike basic mock judges, CodeNova features:
1. **Real Server-Side Execution**: Evaluates candidate code against hidden and visible test cases using native compilers and reflective Java test drivers.
2. **Proctored Skill Assessments & Contests**: Timed, proctored environments with automated scoring, deadline enforcement, and anti-cheat event monitoring.
3. **Multi-Role Experience**: Distinct interfaces and permissions for **Candidates/Students**, **Verified Organization Hosts**, and **Platform Administrators**.
4. **Interactive Support Ticketing**: Threaded candidate-admin support desk with internal staff notes and asynchronous transactional notifications.
5. **Global Accessibility**: Full internationalization supporting 105+ world languages and an AI-powered conversational pair programmer.

---

## Key Features & Modules

### 1. Developer Practice & Problem Solving
- **Interactive Monaco Editor**: Industry-standard code editor with multi-language syntax highlighting, bracket matching, code folding, and starter templates.
- **Run vs. Submit**: "Run" validates visible sample cases without database persistence; "Submit" runs full hidden test suites and persists submission history.
- **Languages Supported**: Java 17+ (Solution-class reflective execution), Python 3, C++, and JavaScript (Node.js).
- **Progress Tracking**: Personal submission history, performance metrics, problem difficulty breakdown, and streak heatmaps.

### 2. Proctored Skill Assessments
- **Timed MCQ & Programming Evaluations**: Single/multiple-choice questions and coding challenges with automated evaluation.
- **Atomic Answer Recording**: Non-blocking answer saves with immediate candidate state preservation.
- **Anti-Cheat Monitoring**: Tracks browser tab-switches and fullscreen exits with automated attempt termination upon threshold violations.
- **Instant Result Summaries**: Detailed scoring breakdowns, answer keys (post-submission), and performance badges.

### 3. Contests & Leaderboards
- **Competitive Contests**: Scheduled programming contests with real-time countdown clocks and synchronized registration.
- **Live Leaderboard**: Real-time ranking with dynamic scoring based on speed, accuracy, and penalties.

### 4. Organization Host Verification
- **Host Application**: Educational institutions and corporate organizations apply for host privileges with document verification.
- **Admin Review Lifecycle**: Admins verify or reject host requests with structured audit notes.
- **Hosted Assessments**: Verified hosts create private assessments and monitor candidate participation analytics.

### 5. Enterprise Support & Feedback Desk
- **Multi-State Ticket Lifecycle**: `OPEN` ➔ `IN_PROGRESS` ➔ `WAITING_FOR_YOU` ➔ `RESOLVED` ➔ `CLOSED`.
- **Threaded Communication**: Candidate-staff dialogue with screenshot attachment support.
- **Internal Staff Notes**: Private admin notes hidden from candidate view.

### 6. Administration, Reports & Analytics
- **Platform Analytics**: Dynamic date-filtered charts for user registrations, submission verdicts, language distribution, and contest metrics.
- **CSV Data Export**: One-click real-time metric export.
- **Content Management**: Full CRUD for problems, test cases, editorials, hints, and certificates.

---

## Technology Stack

| Layer | Technology | Description |
|---|---|---|
| **Frontend Client** | React 18, Vite | High-performance single page application |
| **Routing & UI** | React Router v6, Lucide React, Tailwind CSS | Responsive design system |
| **Code Editor** | Monaco Editor (`@monaco-editor/react`) | Full-featured IDE code editing in browser |
| **Backend REST API** | Spring Boot 3.3.4 (Java 17) | Enterprise grade microservice and RESTful APIs |
| **Security & Auth** | Spring Security 6, JJWT (0.12.6) | Stateless token-based JWT authentication & role-based RBAC |
| **Data Persistence** | Spring Data JPA, Hibernate 6, HikariCP | ORM database mapping with connection pooling |
| **Database** | MySQL 8.4 | Relational database storage |
| **Deployment** | Vercel (Frontend) + Railway (Backend + DB) | Cloud hosted production deployment |

---

## Project Structure

```
CodeNova/
├── backend/
│   ├── src/main/java/com/oj/platform/
│   │   ├── config/              # Security, CORS, DataSource, and Database Seeders
│   │   ├── controller/          # REST API endpoints (Auth, Problem, Contest, Assessment, Admin)
│   │   ├── dto/                 # Request & response data transfer objects
│   │   ├── entity/              # JPA entity models (User, Problem, Submission, Contest, etc.)
│   │   ├── repository/          # Spring Data JPA repositories
│   │   ├── security/            # JWT token provider and authentication filters
│   │   └── service/             # Business logic and execution engines
│   ├── src/main/resources/      # application.properties
│   ├── Dockerfile               # Production multi-stage Docker container
│   └── pom.xml                  # Maven build configuration
│
├── frontend/
│   ├── src/
│   │   ├── components/          # Reusable UI widgets, Modals, Navbar, Footer
│   │   ├── context/             # AuthContext, ThemeContext, NotificationContext
│   │   ├── i18n/                # Multi-language translation bundles (105+ languages)
│   │   ├── pages/               # Views (Home, Problems, Editor, Assessments, Contests, Admin)
│   │   └── services/            # Axios API clients
│   ├── package.json             # NPM dependencies
│   ├── tailwind.config.js       # Tailwind CSS configuration
│   └── vite.config.js           # Vite frontend tooling
│
└── README.md
```

---

## Local Installation & Setup

### 1. Prerequisites
- **Java**: JDK 17 or higher
- **Node.js**: v18 or higher
- **MySQL**: 8.0+ running on `localhost:3306`

### 2. Backend Setup
```bash
cd backend
./mvnw spring-boot:run
```
*Backend runs on `http://localhost:8080` with automatic schema migration and seed data.*

### 3. Frontend Setup
```bash
cd frontend
npm install
npm run dev
```
*Frontend runs on `http://localhost:5173`.*

---

## API Overview

### Authentication & Users
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register new user account | Public |
| `POST` | `/api/auth/login` | Authenticate user & return JWT token | Public |
| `GET` | `/api/auth/me` | Fetch authenticated user profile | Candidate / Admin |
| `PUT` | `/api/users/settings` | Update language & notification settings | Candidate / Admin |

### Coding Problems & Submissions
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/problems` | List all active coding problems | Public |
| `GET` | `/api/problems/{id}` | Get problem details and starter template | Public |
| `POST` | `/api/submissions/run` | Execute code against sample test cases (No DB save) | Candidate |
| `POST` | `/api/submissions` | Full graded submission against hidden test cases | Candidate |
| `GET` | `/api/submissions/my` | Get candidate's personal submission history | Candidate |

### Assessments & Contests
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/assessments` | List published skill assessments | Public |
| `POST` | `/api/assessments/{id}/start` | Start timed assessment attempt | Candidate |
| `POST` | `/api/assessments/{id}/attempts/{attemptId}/answer` | Record atomic MCQ / True-False answer | Candidate |
| `POST` | `/api/assessments/{id}/attempts/{attemptId}/submit` | Final assessment grading & submit | Candidate |
| `GET` | `/api/contests` | List active, upcoming, and past contests | Public |
| `POST` | `/api/contests/{id}/register` | Register for competitive contest | Candidate |
| `GET` | `/api/leaderboard` | Get global platform & contest leaderboard | Public |

### Support, Feedback & Admin
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/support/tickets/my` | List candidate support tickets | Candidate |
| `POST` | `/api/support/tickets` | Create new support ticket | Candidate |
| `POST` | `/api/support/tickets/{id}/messages`| Add message/reply to support ticket thread | Candidate / Admin |
| `GET` | `/api/admin/support/tickets` | List and filter all platform tickets | **Admin Only** |
| `PATCH`| `/api/admin/support/tickets/{id}/status` | Update ticket status (`IN_PROGRESS`, `RESOLVED`, etc.) | **Admin Only** |
| `GET` | `/api/admin/reports` | Get platform aggregate analytics & metrics | **Admin Only** |

---

## Execution & Proctoring Engine

1. **Solution-Class Model**: Students only implement algorithm methods (e.g. `public int[] twoSum(int[] nums, int target)`).
2. **Dynamic Driver Generation**: The backend generates a runtime `Main.java` driver that reflects on declared parameter types, parses test inputs accurately, and compares output with normalized formatting.
3. **Proctoring Integrity**: Browser event listeners detect tab switches (`visibilitychange`) and fullscreen cancellations (`fullscreenchange`). The server terminates the attempt if violations exceed safety limits.

---

## Support Ticketing & Email System

- **Safe Non-Blocking SMTP**: Asynchronous delivery via `CompletableFuture` ensures API endpoints respond instantly even during mail server latencies.
- **Dynamic HTML Templates**: Responsive email layouts with XSS-sanitized inputs.
- **Lifecycle Events**:
  - `Ticket Created` ➔ Confirmation with ticket ID (`SUP-100X`).
  - `Staff Reply` ➔ Direct response summary sent to candidate.
  - `Resolved / Closed` ➔ Resolution notice with re-open link.
  - `Assessment & Contest Results` ➔ Score, percentage, and badge delivery.

---

## Author

Developed by **Shreya K** as part of the **Online Coding Practice and Skill Assessment Management Platform** initiative.
