import React, { useState, useRef, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { isAdminRole, getRoleLabel } from '../utils/roleUtils';
import Logo from './Logo';
import LanguageSelector from './LanguageSelector';
import { LogOut, User as UserIcon, Shield, Search, Bell, ChevronDown } from 'lucide-react';

const Navbar = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchTerm, setSearchTerm] = useState('');
  const [showNotifications, setShowNotifications] = useState(false);
  const [showProfileMenu, setShowProfileMenu] = useState(false);
  const notificationsRef = useRef(null);
  const profileRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (notificationsRef.current && !notificationsRef.current.contains(e.target)) {
        setShowNotifications(false);
      }
      if (profileRef.current && !profileRef.current.contains(e.target)) {
        setShowProfileMenu(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (searchTerm.trim()) {
      navigate(`/problems?search=${encodeURIComponent(searchTerm.trim())}`);
    } else {
      navigate('/problems');
    }
  };

  const isAdmin = isAdminRole(user?.role);
  const isLoginPage = location.pathname === '/login';
  const isRegisterPage = location.pathname === '/register';

  return (
    <header className="top-navbar">
      <Link to="/" className="brand-logo">
        <Logo size={26} />
        <span>CodeNova</span>
        {user && <span className="brand-badge">{getRoleLabel(user.role)}</span>}
      </Link>

      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
        {isAuthenticated ? (
          <>
            {!isAdmin && (
              <form className="navbar-search" onSubmit={handleSearchSubmit}>
                <Search size={16} />
                <input
                  type="text"
                  placeholder={t('common.searchProblems', 'Search problems...')}
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </form>
            )}

            <LanguageSelector />

            <div style={{ position: 'relative' }} ref={notificationsRef}>
              <button
                className="navbar-icon-btn"
                onClick={() => setShowNotifications((v) => !v)}
                aria-label={t('common.notifications', 'Notifications')}
              >
                <Bell size={18} />
              </button>
              {showNotifications && (
                <div className="navbar-dropdown">
                  <div className="navbar-dropdown-header">{t('common.notifications', 'Notifications')}</div>
                  <div className="empty-state" style={{ padding: '1.75rem 1rem' }}>
                    <Bell size={28} />
                    <p>{t('common.noNotifications', 'No new notifications')}</p>
                  </div>
                </div>
              )}
            </div>

            <div style={{ position: 'relative' }} ref={profileRef}>
              <button className="navbar-profile-trigger" onClick={() => setShowProfileMenu((v) => !v)}>
                <div className="avatar-circle" style={{ width: '30px', height: '30px', fontSize: '0.8rem' }}>
                  {(user?.name || user?.username || 'U').charAt(0).toUpperCase()}
                </div>
                <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>{user?.name || user?.username}</span>
                <ChevronDown size={14} color="var(--text-muted)" />
              </button>
              {showProfileMenu && (
                <div className="navbar-dropdown" style={{ width: '200px' }}>
                  <Link to="/profile" className="navbar-dropdown-item" onClick={() => setShowProfileMenu(false)}>
                    {isAdmin ? <Shield size={16} /> : <UserIcon size={16} />}
                    <span>{t('common.profile', 'Profile')}</span>
                  </Link>
                  <div className="navbar-dropdown-item" onClick={handleLogout} style={{ color: 'var(--danger)' }}>
                    <LogOut size={16} />
                    <span>{t('common.logout', 'Logout')}</span>
                  </div>
                </div>
              )}
            </div>
          </>
        ) : (
          <>
            <LanguageSelector />
            <Link
              to="/login"
              className={`btn btn-sm ${isLoginPage ? 'btn-primary' : 'btn-outline'}`}
              style={{ fontWeight: isLoginPage ? 700 : 500 }}
            >
              {t('common.login', 'Log In')}
            </Link>
            <Link
              to="/register"
              className={`btn btn-sm ${isRegisterPage ? 'btn-primary' : 'btn-outline'}`}
              style={{ fontWeight: isRegisterPage ? 700 : 500 }}
            >
              {t('common.register', 'Register')}
            </Link>
          </>
        )}
      </div>
    </header>
  );
};

export default Navbar;

