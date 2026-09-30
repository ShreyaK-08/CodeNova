# CodeNova — College Demo & Submission Checklist

This checklist provides the exact, step-by-step procedure to demonstrate the full capabilities of CodeNova during your presentation or evaluation.

---

## Prerequisites & Environment Configuration

Ensure MySQL is running and configured (default port `3306`, database `coding_platform`).

### Optional AI Key Configuration
To enable live LLM generation with Groq, Google Gemini, or OpenAI, provide the environment variable before starting the backend:

```powershell
# Windows PowerShell example:
$env:AI_API_KEY="your_groq_or_gemini_api_key"
$env:AI_MODEL="llama-3.3-70b-versatile"
```

> **Note:** If `AI_API_KEY` is omitted, CodeNova automatically uses its built-in rule-based knowledge engine to accurately answer platform questions, explain errors, and provide hints without failing.

---

## Step-by-Step Demo Flow

### 1. Start Services
- **Start MySQL**: Verify MySQL service is active.
- **Start Backend**:
  ```powershell
  cd backend
  ./mvnw spring-boot:run
  ```
  *(Backend listens on `http://localhost:8080`)*
- **Start Frontend**:
  ```powershell
  cd frontend
  npm run dev
  ```
  *(Frontend launches on `http://localhost:5173`)*

---

### 2. Authentication & Navigation
- **Login**:
  - Open `http://localhost:5173/login`.
  - Sign in as student:
    - **Username**: `alexj` (or `shreya`)
    - **Password**: `password123`
- **Dashboard**: Review solved count, difficulty breakdown, and recent activity.

---

### 3. Coding Problems & Monaco Editor
- **Open Problems**: Navigate to `/problems` from the sidebar.
- **Select Problem**: Click on a problem (e.g., "Two Sum").
- **Inspect Monaco Editor**: Point out syntax highlighting, language selector (Java, Python, C++, JavaScript), and starter code.
- **Run Code**: Click **Run** to execute visible sample test cases.
- **Submit Code**: Click **Submit** to run full test suite with server-side driver evaluation.

---

### 4. Contests & Exam Security
- **Open Contests**: Navigate to `/contests`.
- **Register for Contest**: Click **Register** on an active or upcoming contest.
- **Enter Contest**: Open the contest coding interface (`/contests/:id/code`).
- **Contest Timer**: Point out the server-synchronized countdown timer.
- **Security Monitoring**:
  - Explain that exiting fullscreen or switching tabs logs an exam violation.
  - Mention that exceeding 3 violations terminates the attempt.
  - Honestly highlight that the platform uses browser focus/fullscreen events and **does not** spy via webcams or screen recording.

---

### 5. Skill Assessments
- **Open Assessments**: Navigate to `/assessments`.
- **Start Assessment**: Select an assessment and click **Start Attempt**.
- **Answer Question**: Choose options for MCQs and navigate with **Previous** / **Next**.
- **Timer**: Note the server-enforced deadline countdown.
- **Submit Assessment**: Click **Submit Assessment** before the deadline.
- **View Results**: Review score, pass/fail status, percentage, and detailed answer explanations.

---

## 6. Multi-Language (i18n) Support
- **Open Language Selector**: Click the globe icon in the top navbar.
- **Search Languages**: Type in the search box to demonstrate filtering from 105+ world languages.
- **Switch to Kannada**:
  - Select **ಕನ್ನಡ (Kannada)**.
  - Point out that the Navbar, Sidebar, buttons, and tabs immediately translate into natural Kannada.
- **Switch to Hindi**:
  - Open the selector and choose **हिन्दी (Hindi)**.
  - Point out that UI labels translate to Hindi.
- **Persistence Check**: Refresh the page (`F5`) to prove the selected language survives browser refresh via `localStorage`.
- **Switch back to English**: Select **English** to return to standard view.

---

## 7. AI Chatbot Pair Programmer
- **Open Chatbot**: Click the floating **CodeNova AI** button at bottom-right.
- **Platform Overview**:
  - Ask: *"What can I do on this website?"*
  - The AI assistant explains problems, contests, assessments, and certifications.
- **Coding Problem Context**:
  - Navigate to `/problems/1` with editor open.
  - Click the **Give Hint** quick action button.
  - The AI assistant opens with attached problem context and provides conceptual hints without giving away the full code.
- **Code & Error Explanation**:
  - Click **Explain Code** to receive a structured breakdown of the active Monaco editor code.
  - If a runtime or syntax error occurs, click **Explain Error** to diagnose the root cause.
- **Multilingual AI Responses**:
  - With Kannada or Hindi selected in the language selector, ask a question and observe that the AI responds in the selected human language.

---

## 8. Business / Organization Assessment Host Verification
- **Host Assessment Access**:
  - Log in as a student or host candidate (`alexj` or `shreya` or demo account).
  - Click **Host Assessment** from the assessments screen or navigate to `/assessments/host`.
- **Complete Business Verification Form**:
  - Note the professional business verification form with 10 required fields and 3 optional document uploads:
    - **Legal Organization Name**: e.g., `Acme Technologies Pvt Ltd`
    - **Organization Type**: e.g., `Private Company`
    - **Registration / Incorporation Number**: e.g., `U72200KA2022PTC123456`
    - **Official Corporate Email**: e.g., `contact@acmetech.com`
    - **Country**: e.g., `India`
    - **Website URL**: e.g., `https://acmetech.example.com`
    - **Representative Name**: e.g., `Shreya K`
    - **Contact Phone**: e.g., `+91 9876543210`
    - **Purpose**: e.g., `Campus Recruitment & Skill Screening`
    - **Primary Business Document**: Attach sample PDF/PNG/JPG certificate of incorporation or registration (max 5 MB).
    - *(Optional)* Attach GST certificate or authorization letter.
  - Click **Submit Business Verification Request**.
  - Notice the page transitions to **Verification Request Under Review** status.

---

## 9. Admin Review, Document Streaming & Direct Verification
- **Admin Login**:
  - Log in as `admin` (Password: `admin123` or your configured admin password).
- **Admin Host Verification Table**:
  - Navigate to **Host Verification** in the admin sidebar (`/admin/assessment-host-verification`).
  - View the newly submitted business request with organization name, type, registration number, country, and website link.
  - Click the **Document Preview / Download** buttons to view the uploaded primary registration document and optional certificates securely.
- **Admin Verification Decision**:
  - **Option A (Email Approval)**: Click **Approve**. A 6-digit one-time code (e.g., `CN-XXXXXX`) is generated and dispatched via email.
  - **Option B (Direct Verify)**: Click **Direct Verify**. The host is immediately verified without requiring code entry (ideal for offline evaluations and fast demos!).

---

## 10. Persistent Host Status & Multiple Assessment Authoring
- **Verification Completion**:
  - Log back in as the user.
  - If Option A was used: Enter the verification code received via email (or check the local backend console log).
  - Status immediately transitions to **VERIFIED**.
- **First Assessment Creation**:
  - Open `/assessments/host`. The Assessment Builder is immediately displayed with no verification prompts!
  - Create a new assessment:
    - **Title**: `Full-Stack Developer Assessment`
    - **Duration**: `45 minutes`
    - **Passing Marks**: `10`
  - Add multiple-choice or coding questions and options.
  - Click **Publish Assessment**.
- **Persistent Verification Test (Zero Re-Verification)**:
  - Return to `/assessments/host` or refresh the browser.
  - Click **+ Create Assessment** to create a *second* assessment (`Algorithms & Data Structures`).
  - Notice: The host is **NEVER** asked for verification or a code again. The user remains a verified host permanently!

---

## 11. Candidate Submissions & Admin Monitoring
- **Candidate Assessment Participation**:
  - Log in as a candidate user (e.g., `alexj`).
  - Go to `/assessments`, start the hosted assessment, answer questions, and submit before time expires.
- **Host Viewing Candidate Submissions**:
  - Log back in as the hosting user.
  - In `/assessments/host`, locate the assessment and click **Candidate Submissions** (`View Attempts`).
  - Review the candidate list: Candidate Full Name, Username, Email, Score, Status, Pass/Fail, and Submission Timestamp.
  - Click **Export to CSV** to download the structured grading report.
- **Admin Assessment Monitoring (Read-Only)**:
  - Log back in as `admin`.
  - Open **Assessment Monitoring** (`/admin/assessments`).
  - Notice the table indicates the assessment is hosted by a user (`Created By: shreya`, `Organization: Acme Technologies Pvt Ltd`).
  - Notice that edit/delete/archive action buttons are disabled or restricted to read-only **View Details**.
  - Clicking **View Details** allows monitoring questions and metadata without permission to alter or delete the host's assessment.
- **Security Check (User Isolation)**:
  - Attempting to edit or access another host's assessment via API or URL results in HTTP `403 Forbidden`. User assessments remain strictly private and isolated.
