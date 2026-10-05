# Infosys Springboard Virtual Internship 7.0 Completion Report

---

### Team Details
- **Batch Number:** Batch 2026-AUG
- **Start Date:** August 19, 2026
- **Internship Duration:** 8 Weeks
- **Names:** Shreya K

---

## 1. Project Title
**Development of an Online Coding Practice and Skill Assessment Management Platform**

---

## 2. Project Objective
The primary objective of this project is to build an enterprise-grade, full-stack online coding practice, skill evaluation, and competitive assessment platform. Developers preparing for technical interviews and competitive programming require an accessible platform to practice algorithm and data structure problems, execute code in real time against validated test cases, and track their improvement over time. 

The platform delivers:
1. **Isolated Multi-Language Code Execution:** A sandboxed execution engine supporting Java 17, Python 3, C++, and JavaScript (Node.js) with strict execution timeouts (2000 ms) and memory boundaries (256 MB) to prevent malicious access and infinite loops.
2. **Proctored Skill Assessments & Contests:** Timed MCQ and programming assessments featuring anti-cheat monitoring (fullscreen exit and tab-switching violation tracking).
3. **Progress Telemetry & Verifiable Certification:** Personal progress dashboards displaying difficulty distribution, submission heatmaps, language proficiencies, and automated skill certification.
4. **Interactive Support & Administrative Analytics:** Threaded support ticket desk, real-time leaderboards, problem bank management, and platform analytics with CSV data export.

---

## 3. Project Description in Detail

### 3.1 Approach & System Architecture
The platform is designed following a decoupled three-tier architecture:
- **Frontend Presentation Layer:** A responsive single-page application built using React 18 and Vite. It integrates the Microsoft Monaco Editor (the editor engine behind VS Code) with syntax highlighting, language selection, split-pane problem-solving views, custom input execution, verdict banners, and multi-language internationalization (105+ world languages).
- **Backend Application Layer:** A Spring Boot 3.3.4 RESTful API implementing Spring Security 6 with stateless JWT authentication, role-based access control (`ROLE_ADMIN`, `ROLE_USER`, `ROLE_HOST`), and asynchronous execution dispatchers.
- **Execution & Evaluation Engine:** Submitted code is dynamically evaluated using reflective test drivers and isolated subprocess execution with execution watchdogs, capturing compilation errors, runtime exceptions, execution time (ms), memory usage, and per-test-case pass/fail comparisons with whitespace normalization.
- **Data Persistence Layer:** MySQL 8.4 relational database managed via Spring Data JPA and Hibernate ORM, using HikariCP connection pooling and automated schema migration.

### 3.2 Technologies Used
| Layer | Technology | Description |
|---|---|---|
| **Frontend Framework** | React 18, Vite | Component-driven, responsive user interface |
| **Code Editor** | Monaco Editor (`@monaco-editor/react`) | Professional in-browser IDE experience |
| **Backend Framework** | Spring Boot 3.3.4 (Java 17) | Enterprise-grade REST microservices |
| **Security & Auth** | Spring Security 6, JJWT 0.12.6, BCrypt | Stateless token authentication & RBAC |
| **Database & ORM** | MySQL 8.4, Hibernate 6, HikariCP | Relational data persistence & connection pooling |
| **Execution Toolchain** | Java 17, GCC/G++, Python 3, Node.js | Multi-compiler isolated evaluation engine |
| **Deployment** | Vercel (Frontend) + Railway (Backend & DB) | Cloud-hosted production infrastructure |

### 3.3 Real-World Impact
- **Candidate Empowerment:** Provides deterministic evaluation results, step-by-step hints, and performance analytics to accelerate programming proficiency.
- **Organizational Efficiency:** Allows verified organizations and administrators to create tailored assessments, conduct proctored evaluations, evaluate submissions at scale, and export audit-ready reports.

---

## 4. Timeline Overview

| Week | Activities Planned | Activities Completed |
|---|---|---|
| **Week 1** *(Aug 19 – Aug 25)* | Kickoff orientation, prerequisite review, database schema modeling, and project setup. | Designed relational MySQL schema for users, problems, test cases, submissions, and contest entities; initialized Spring Boot 3 and React 18 repository. |
| **Week 2** *(Aug 26 – Sep 01)* | User Authentication (JWT), rate limiting, and core code compilation/execution engine. | Implemented Spring Security 6 JWT authentication, BCrypt password hashing, compiler diagnostic capture, and submitted **Milestone 1** (Sep 01). |
| **Week 3** *(Sep 02 – Sep 08)* | Test case evaluation, whitespace output normalization, and problem management APIs. | Built hidden vs. visible test case runner, time limit watchdogs, problem repository endpoints, and admin management tools. |
| **Week 4** *(Sep 09 – Sep 15)* | Submission lifecycle management, dynamic leaderboards, and contest scheduling. | Implemented submission verdict states (`ACCEPTED`, `WRONG_ANSWER`, `TLE`, `RUNTIME_ERROR`), weighted leaderboard scoring, and submitted **Milestone 2** (Sep 15). |
| **Week 5** *(Sep 16 – Sep 22)* | Monaco Editor integration, split-pane problem interface, and proctoring event listeners. | Integrated Monaco Editor, problem description markdown renderer, run/submit tabs, and anti-cheat event monitors. |
| **Week 6** *(Sep 23 – Sep 29)* | Skill assessment engine, automated certification, and support ticketing system. | Built MCQ + Coding timed assessment runner, automated 50-problem milestone certificate generator with public verification portal, and submitted **Milestone 3** (Sep 29). |
| **Week 7** *(Sep 30 – Oct 06)* | Cloud deployment, end-to-end integration, performance tuning, and technical review. | Deployed frontend to Vercel and backend with MySQL to Railway; completed end-to-end integration testing and technical review. |
| **Week 8** *(Oct 07 – Oct 13)* | Documentation finalization, presentation slide deck, mock demo rehearsals, and project closure. | Completed individual and team documentation (Oct 08), finalized presentation PPT (Oct 09), conducted mock presentations, and attended project closure (Oct 13). |

---

## 5a. Key Milestones

| Milestone | Description | Date Achieved |
|---|---|---|
| **Project Kickoff** | Orientation, requirement analysis, and tech stack setup. | **August 19, 2026** |
| **Milestone 1 Submission** | User Auth, Database Schema, and Sandboxed Code Execution Engine. | **September 01, 2026** |
| **Milestone 2 Submission** | Test Case Management, Problem Bank CRUD, and Leaderboard System. | **September 15, 2026** |
| **Milestone 3 Submission** | React Monaco Editor, Problem Browser, and Skill Assessments. | **September 29, 2026** |
| **Technical Review & Deployment** | Full Cloud Deployment (Vercel & Railway) & Technical Completion. | **September 30, 2026** |
| **Documentation Submission** | Individual and Team Project Completion Documentation. | **October 08, 2026** |
| **Final Presentation Submission** | Finalized Project Presentation Deck & Live Demo. | **October 09, 2026** |
| **Project Closure** | Final Project Evaluation and Closure. | **October 13, 2026** |

---

## 5b. Project Execution Details

1. **System Flow & Architecture:**
   - When a candidate submits code through the Monaco Editor, an authenticated HTTP request is verified via JWT filter.
   - The execution service spawns an isolated subprocess with runtime constraints (2000 ms timeout, 256 MB memory).
   - Code is compiled; any syntax errors return an immediate compilation diagnostic.
   - Output from compiled binaries is compared against stored test cases (visible and hidden) using whitespace-normalized matching.
   - The transaction persists execution metrics (runtime in ms, memory, verdict) and dynamically recalculates user score and leaderboard rank.

2. **Cloud Deployment & DevOps:**
   - **Frontend:** Hosted on Vercel with automated single-page application routing rewrites and asset optimization.
   - **Backend:** Containerized on Railway with multi-compiler tools (Java 17, GCC, Python 3, Node.js) and dynamic port allocation.
   - **Database:** Managed MySQL 8.4 cloud instance on Railway with automated table initialization and seed catalogs.

---

## 6. Snapshots / Screenshots & Visual References

### Live Platform URLs:
- **Live Frontend Web Application:** `https://code-nova-psi.vercel.app`
- **Live Backend REST API:** `https://codenova-production-c3c6.up.railway.app`
- **Visual Demonstration Archive:** Complete categorized archive of 80+ high-resolution screenshots on Google Drive: `https://drive.google.com/drive/folders/11YsT042SkuQ-w6gSjmh5KoVbhFb7c7ry`

### Core Functional Modules:
1. **Interactive Coding Workspace:** Monaco Code Editor with language selection (Java, Python, C++, JS), test case tabs, run sample cases, and submission terminal.
2. **My Submissions & Execution Auditing:** Tabular audit trail showing execution verdicts (`Accepted`, `Wrong Answer`, `TLE`), execution runtime (ms), memory, and source code review modal.
3. **Proctored Assessments & Contests:** Timed evaluation interface with countdown clocks, atomic answer saving, and tab-switch anti-cheat violation tracking.
4. **Progress & Verifiable Certificates:** Visual breakdown of solved problems by difficulty (Easy, Medium, Hard), submission heatmaps, and public certificate verification portal.
5. **Admin Console & Platform Analytics:** Full CRUD for problems and test cases, host verification approvals, platform telemetry, and CSV report export.

---

## 7. Challenges Faced & Resolutions

| # | Challenge Encountered | Root Cause | Resolution / Mitigation |
|---|---|---|---|
| **1** | **Infinite Loops & Resource Exhaustion** | Candidate submissions containing unbounded loops or excessive memory consumption blocking server threads. | Implemented subprocess execution with Java `ProcessBuilder` and a strict 2000 ms watchdog timer to forcefully terminate runaway processes. |
| **2** | **Dynamic Cloud Database URL Resolution** | Cloud hosting environment injecting standard URI format (`mysql://`) rather than JDBC format (`jdbc:mysql://`). | Developed a smart `DataSourceConfig` Spring Bean that dynamically parses cloud URI components (host, port, credentials) and configures HikariCP. |
| **3** | **Proctoring Integrity in Browser** | Candidates navigating away from browser tabs during timed skill assessments. | Integrated JavaScript event listeners for `visibilitychange` and `fullscreenchange` with server-side violation logging and auto-termination after 3 strikes. |
| **4** | **Seamless Reviewer Access** | Strict email verification blocking evaluator signups during rapid demonstration. | Enhanced registration workflow with automatic account verification on signup, allowing reviewers to register and test all features instantly. |

---

## 8. Learnings & Skills Acquired

- **Full-Stack Engineering:** Architecture and development of full-stack systems using **Spring Boot 3** and **React 18**.
- **Security & Authorization:** Implementing stateless **JWT authentication**, password hashing via **BCrypt**, and multi-role **Role-Based Access Control**.
- **Code Execution Engines:** Designing multi-language compilers, reflective test drivers, and subprocess sandboxing.
- **Database Architecture:** Relational data modeling, indexing, and connection pooling using **Spring Data JPA & Hibernate**.
- **Cloud Deployment & CI/CD:** Managing production cloud deployments on **Vercel** and **Railway**.
- **Professional Practices:** Agile milestone adherence, technical documentation, and structured project presentation.

---

## 9. Testimonials from Team

> *"Developing this Online Coding Practice and Skill Assessment Management Platform from foundational architecture to a fully deployed cloud ecosystem has provided invaluable engineering experience. Building isolated code execution tools and proctored assessment workflows gave us deep, practical knowledge of scalable enterprise software development, resilient architecture, and secure system design."*

---

## 10. Conclusion
The **Online Coding Practice and Skill Assessment Management Platform** successfully fulfills all objectives set forth for the Infosys Springboard Virtual Internship 7.0. The platform delivers a robust, secure, and production-ready coding practice and evaluation system. The technical competencies, problem-solving skills, and architectural knowledge gained throughout this 8-week internship provide a strong foundation for enterprise software engineering careers.

---

## 11. Acknowledgements
We express our sincere gratitude to **Infosys Springboard** for providing this virtual internship platform and learning opportunity. We extend our heartfelt appreciation to our **Internship Mentors, Coordinators, and Technical Leads** for their continuous technical guidance, regular status review calls, and constructive feedback throughout the internship milestones.
