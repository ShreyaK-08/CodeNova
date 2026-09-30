import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';
import ProtectedRoute from './components/ProtectedRoute';
import AIChatbot from './components/AIChatbot';

import LandingPage from './pages/LandingPage';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import VerifyEmail from './pages/auth/VerifyEmail';
import GoogleCallback from './pages/auth/GoogleCallback';
import UserDashboard from './pages/user/UserDashboard';
import ProgressDashboard from './pages/user/ProgressDashboard';
import Profile from './pages/user/Profile';
import ProblemsList from './pages/user/ProblemsList';
import Contests from './pages/user/Contests';
import ContestDetail from './pages/user/ContestDetail';
import ContestCoding from './pages/user/ContestCoding';
import Assessments from './pages/user/Assessments';
import AssessmentAttempt from './pages/user/AssessmentAttempt';
import ProblemDetail from './pages/user/ProblemDetail';
import MySubmissions from './pages/user/MySubmissions';
import Leaderboard from './pages/user/Leaderboard';

import AdminDashboard from './pages/admin/AdminDashboard';
import ProblemManagement from './pages/admin/ProblemManagement';
import TestCaseManagement from './pages/admin/TestCaseManagement';
import UserManagement from './pages/admin/UserManagement';
import AdminSubmissions from './pages/admin/AdminSubmissions';
import AdminEditorials from './pages/admin/AdminEditorials';
import AdminCertificates from './pages/admin/AdminCertificates';
import ContestManagement from './pages/admin/ContestManagement';
import AssessmentManagement from './pages/admin/AssessmentManagement';
import AssessmentHostVerification from './pages/admin/AssessmentHostVerification';
import HostAssessment from './pages/user/HostAssessment';
import HostContestResults from './pages/user/HostContestResults';

import Certificates from './pages/user/Certificates';
import VerifyCertificate from './pages/VerifyCertificate';
import Support from './pages/user/Support';
import SupportTicketDetail from './pages/user/SupportTicketDetail';
import Feedback from './pages/user/Feedback';
import Settings from './pages/user/Settings';
import AdminSupportFeedback from './pages/admin/AdminSupportFeedback';
import AdminSupportTicketDetail from './pages/admin/AdminSupportTicketDetail';
import AdminSettings from './pages/admin/AdminSettings';
import AdminReports from './pages/admin/AdminReports';

import PlaceholderPage from './pages/PlaceholderPage';
import { Award, ListOrdered, BarChart2 } from 'lucide-react';

const AppLayout = ({ children, withSidebar = false }) => {
  const { isAuthenticated } = useAuth();
  const showSidebar = withSidebar && isAuthenticated;

  return (
    <div className="app-container">
      {showSidebar && <Sidebar />}
      <div className="main-content">
        <Navbar />
        <main className="content-body">{children}</main>
      </div>
      <AIChatbot />
    </div>
  );
};

function App() {
  return (
    <ThemeProvider>
    <AuthProvider>
      <Router>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<AppLayout><LandingPage /></AppLayout>} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/verify-email" element={<AppLayout><VerifyEmail /></AppLayout>} />
          <Route path="/oauth2/callback/google" element={<GoogleCallback />} />
          <Route path="/verify-certificate/:code" element={<AppLayout><VerifyCertificate /></AppLayout>} />

          {/* User Protected Routes */}
          <Route element={<ProtectedRoute allowedRoles={['USER', 'ROLE_USER', 'ADMIN', 'ROLE_ADMIN']} />}>
            <Route path="/dashboard" element={<AppLayout withSidebar><UserDashboard /></AppLayout>} />
            <Route path="/problems" element={<AppLayout withSidebar><ProblemsList /></AppLayout>} />
            <Route path="/problems/:id" element={<AppLayout withSidebar><ProblemDetail /></AppLayout>} />
            <Route path="/contests" element={<AppLayout withSidebar><Contests /></AppLayout>} />
            <Route path="/contests/:id" element={<AppLayout withSidebar><ContestDetail /></AppLayout>} />
            <Route path="/contests/:id/code" element={<AppLayout withSidebar><ContestCoding /></AppLayout>} />
            <Route path="/assessments" element={<AppLayout withSidebar><Assessments /></AppLayout>} />
            <Route path="/assessments/host" element={<AppLayout withSidebar><HostAssessment /></AppLayout>} />
            <Route path="/host-assessment" element={<AppLayout withSidebar><HostAssessment /></AppLayout>} />
            <Route path="/assessments/:id/attempt" element={<AppLayout withSidebar><AssessmentAttempt /></AppLayout>} />
            <Route path="/host-contest-results/:id" element={<AppLayout withSidebar><HostContestResults /></AppLayout>} />
            <Route path="/submissions" element={<AppLayout withSidebar><MySubmissions /></AppLayout>} />
            <Route path="/certificates" element={<AppLayout withSidebar><Certificates /></AppLayout>} />
            <Route path="/profile" element={<AppLayout withSidebar><Profile /></AppLayout>} />
            <Route path="/support" element={<AppLayout withSidebar><Support /></AppLayout>} />
            <Route path="/support/tickets/:ticketId" element={<AppLayout withSidebar><SupportTicketDetail /></AppLayout>} />
            <Route path="/feedback" element={<AppLayout withSidebar><Feedback /></AppLayout>} />
            <Route path="/progress" element={<AppLayout withSidebar><ProgressDashboard /></AppLayout>} />
            <Route path="/leaderboard" element={<AppLayout withSidebar><Leaderboard /></AppLayout>} />
            <Route path="/settings" element={<AppLayout withSidebar><Settings /></AppLayout>} />
          </Route>

          {/* Admin Protected Routes */}
          <Route element={<ProtectedRoute allowedRoles={['ADMIN', 'ROLE_ADMIN']} />}>
            <Route path="/admin/dashboard" element={<AppLayout withSidebar><AdminDashboard /></AppLayout>} />
            <Route path="/admin/users" element={<AppLayout withSidebar><UserManagement /></AppLayout>} />
            <Route path="/admin/problems" element={<AppLayout withSidebar><ProblemManagement /></AppLayout>} />
            <Route path="/admin/testcases" element={<AppLayout withSidebar><TestCaseManagement /></AppLayout>} />
            <Route path="/admin/submissions" element={<AppLayout withSidebar><AdminSubmissions /></AppLayout>} />
            <Route path="/admin/leaderboards" element={<AppLayout withSidebar><Leaderboard /></AppLayout>} />
            <Route path="/admin/editorials" element={<AppLayout withSidebar><AdminEditorials /></AppLayout>} />
            <Route path="/admin/certificates" element={<AppLayout withSidebar><AdminCertificates /></AppLayout>} />
            <Route path="/admin/contests" element={<AppLayout withSidebar><ContestManagement /></AppLayout>} />
            <Route path="/admin/assessments" element={<AppLayout withSidebar><AssessmentManagement /></AppLayout>} />
            <Route path="/admin/assessment-host-verification" element={<AppLayout withSidebar><AssessmentHostVerification /></AppLayout>} />
            <Route path="/admin/support" element={<AppLayout withSidebar><AdminSupportFeedback /></AppLayout>} />
            <Route path="/admin/support/tickets/:ticketId" element={<AppLayout withSidebar><AdminSupportTicketDetail /></AppLayout>} />
            <Route path="/admin/support-feedback" element={<AppLayout withSidebar><AdminSupportFeedback /></AppLayout>} />
            <Route path="/admin/support-feedback/tickets/:ticketId" element={<AppLayout withSidebar><AdminSupportTicketDetail /></AppLayout>} />
            <Route path="/admin/reports" element={<AppLayout withSidebar><AdminReports /></AppLayout>} />
            <Route path="/admin/settings" element={<AppLayout withSidebar><AdminSettings /></AppLayout>} />
          </Route>

          {/* Catch-all */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
