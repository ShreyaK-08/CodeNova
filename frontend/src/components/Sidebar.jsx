import React from 'react';
import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { isAdminRole } from '../utils/roleUtils';
import {
  LayoutDashboard,
  Code,
  FileCheck,
  TrendingUp,
  Award,
  User,
  Settings,
  Users,
  Database,
  ListOrdered,
  BookOpen,
  BarChart2,
  Trophy,
  Flag,
  ClipboardList,
  ShieldCheck,
  LifeBuoy,
  MessageSquare,
  HelpCircle,
  LogOut
} from 'lucide-react';

const Sidebar = () => {
  const { logout, user } = useAuth();
  const { t } = useTranslation();
  const isAdmin = isAdminRole(user?.role);

  const userNavItems = [
    { to: '/dashboard', label: t('common.dashboard', 'Dashboard'), icon: LayoutDashboard },
    { to: '/problems', label: t('common.problems', 'Problems'), icon: Code },
    { to: '/contests', label: t('common.contests', 'Contests'), icon: Flag },
    { to: '/assessments', label: t('common.assessments', 'Assessments'), icon: ClipboardList },
    { to: '/submissions', label: t('common.mySubmissions', 'My Submissions'), icon: FileCheck },
    { to: '/progress', label: t('common.progressDashboard', 'Progress Dashboard'), icon: TrendingUp },
    { to: '/leaderboard', label: t('common.leaderboard', 'Leaderboard'), icon: Award },
    { to: '/certificates', label: t('common.certificates', 'Certificates'), icon: Trophy },
    { to: '/profile', label: t('common.profile', 'Profile'), icon: User },
    { to: '/support', label: t('common.support', 'Support'), icon: LifeBuoy },
    { to: '/feedback', label: t('common.feedback', 'Feedback'), icon: MessageSquare },
    { to: '/settings', label: t('common.settings', 'Settings'), icon: Settings },
  ];

  const adminNavItems = [
    { to: '/admin/dashboard', label: t('common.dashboard', 'Dashboard'), icon: LayoutDashboard },
    { to: '/admin/users', label: t('common.userManagement', 'User Management'), icon: Users },
    { to: '/admin/problems', label: t('common.problemManagement', 'Problem Management'), icon: Code },
    { to: '/admin/testcases', label: t('common.testCaseManagement', 'Test Case Management'), icon: Database },
    { to: '/admin/submissions', label: t('common.userSubmissions', 'User Submissions'), icon: FileCheck },
    { to: '/admin/leaderboards', label: t('common.leaderboard', 'Leaderboards'), icon: ListOrdered },
    { to: '/admin/editorials', label: t('common.hintsEditorials', 'Hints & Editorials'), icon: BookOpen },
    { to: '/admin/certificates', label: t('common.certificates', 'Certificates'), icon: Trophy },
    { to: '/admin/contests', label: t('common.contests', 'Contest Management'), icon: Flag },
    { to: '/admin/assessments', label: t('common.assessmentMonitoring', 'Assessment Monitoring'), icon: ClipboardList },
    { to: '/admin/assessment-host-verification', label: t('common.hostVerification', 'Host Verification'), icon: ShieldCheck },
    { to: '/admin/support-feedback', label: t('common.supportFeedback', 'Support & Feedback'), icon: HelpCircle },
    { to: '/admin/reports', label: t('common.reportsAnalytics', 'Reports & Analytics'), icon: BarChart2 },
    { to: '/admin/settings', label: t('common.settings', 'Settings'), icon: Settings },
  ];

  const items = isAdmin ? adminNavItems : userNavItems;

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <span style={{ fontSize: '0.8rem', fontWeight: 700, letterSpacing: '0.05em', color: 'var(--text-subtle)', textTransform: 'uppercase' }}>
          {isAdmin ? t('common.adminPortal', 'Admin Portal') : t('common.studentPortal', 'Student Portal')}
        </span>
      </div>

      <nav className="sidebar-nav">
        {items.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            >
              <Icon size={18} />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      <div style={{ padding: '1rem', borderTop: '1px solid var(--border-color)' }}>
        <button
          onClick={logout}
          className="nav-item"
          style={{ width: '100%', background: 'transparent', border: 'none', cursor: 'pointer', textAlign: 'left' }}
        >
          <LogOut size={18} color="var(--danger)" />
          <span style={{ color: 'var(--danger)' }}>{t('common.logout', 'Logout')}</span>
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;

