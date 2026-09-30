import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import userService from '../../services/userService';
import { isAdminRole, getRoleLabel } from '../../utils/roleUtils';
import { User, Mail, Shield, Github, Linkedin, Code, Calendar, Edit3, CheckCircle, AlertCircle, Save, X, KeyRound, Trophy, Lock } from 'lucide-react';
import certificateService from '../../services/certificateService';
import { Link } from 'react-router-dom';

const Profile = () => {
  const { t } = useTranslation();
  const { user, updateUser } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    bio: '',
    skills: '',
    githubUrl: '',
    linkedinUrl: '',
    avatarUrl: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [saving, setSaving] = useState(false);

  const [showPasswordForm, setShowPasswordForm] = useState(false);
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmNewPassword: '',
  });
  const [passwordError, setPasswordError] = useState('');
  const [passwordSuccess, setPasswordSuccess] = useState('');
  const [changingPassword, setChangingPassword] = useState(false);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const data = await userService.getProfile();
      setProfile(data);
      setFormData({
        name: data.name || '',
        email: data.email || '',
        bio: data.bio || '',
        skills: data.skills || '',
        githubUrl: data.githubUrl || '',
        linkedinUrl: data.linkedinUrl || '',
        avatarUrl: data.avatarUrl || '',
      });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load profile details');
    } finally {
      setLoading(false);
    }
  };

  const [certProgress, setCertProgress] = useState(null);

  useEffect(() => {
    fetchProfile();
    certificateService.getProgress().then(setCertProgress).catch(() => {});
  }, []);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setSaving(true);

    try {
      const updated = await userService.updateProfile(formData);
      setProfile(updated);
      updateUser({ ...user, name: updated.name, email: updated.email });
      setSuccess('Profile updated successfully and saved to the database!');
      setIsEditing(false);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update profile.');
    } finally {
      setSaving(false);
    }
  };

  const handlePasswordChange = (e) => {
    setPasswordData({ ...passwordData, [e.target.name]: e.target.value });
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setPasswordError('');
    setPasswordSuccess('');

    if (passwordData.newPassword !== passwordData.confirmNewPassword) {
      setPasswordError('New password and confirmation do not match.');
      return;
    }
    if (passwordData.newPassword.length < 6) {
      setPasswordError('New password must be at least 6 characters.');
      return;
    }

    setChangingPassword(true);
    try {
      await userService.changePassword(passwordData);
      setPasswordSuccess('Password changed successfully. Use your new password next time you log in.');
      setPasswordData({ currentPassword: '', newPassword: '', confirmNewPassword: '' });
      setShowPasswordForm(false);
    } catch (err) {
      setPasswordError(err.response?.data?.message || 'Failed to change password.');
    } finally {
      setChangingPassword(false);
    }
  };

  if (loading) {
    return <div style={{ padding: '3rem', textAlign: 'center' }}>Loading user profile from database...</div>;
  }

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 800 }}>{t('profile.title', 'User Profile')}</h1>
          <p style={{ color: 'var(--text-muted)' }}>{t('profile.subtitle', 'Manage your personal details, skills, and coding accounts')}</p>
        </div>
        {!isEditing && (
          <button onClick={() => setIsEditing(true)} className="btn btn-primary btn-sm">
            <Edit3 size={16} />
            <span>{t('profile.editProfile', 'Edit Profile')}</span>
          </button>
        )}
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div className="alert alert-success">
          <CheckCircle size={18} />
          <span>{success}</span>
        </div>
      )}

      {isEditing ? (
        /* Edit Profile Mode */
        <div className="card">
          <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Edit3 size={20} color="#3b82f6" />
            <span>{t('profile.personalInfo', 'Edit Profile Information')}</span>
          </h3>

          <form onSubmit={handleSave}>
            <div className="grid-cols-2">
              <div className="form-group">
                <label>{t('profile.fullName', 'Full Name')} *</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  required
                  className="form-control"
                />
              </div>

              <div className="form-group">
                <label>{t('profile.email', 'Email Address')} *</label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                  className="form-control"
                />
              </div>
            </div>

            <div className="form-group">
              <label>{t('profile.role', 'Role')} (Read Only)</label>
              <input
                type="text"
                value={getRoleLabel(profile?.role)}
                disabled
                className="form-control"
                style={{ opacity: 0.6, cursor: 'not-allowed' }}
              />
              <span style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>{t('profile.roleReadOnlyDesc', 'Account role cannot be changed by user')}</span>
            </div>

            <div className="form-group">
              <label>{t('profile.bio', 'Bio / Summary')}</label>
              <textarea
                name="bio"
                value={formData.bio}
                onChange={handleChange}
                rows={3}
                className="form-control"
                placeholder="Brief summary of your coding background and goals..."
              />
            </div>

            <div className="form-group">
              <label>{t('profile.skills', 'Skills & Technologies')}</label>
              <input
                type="text"
                name="skills"
                value={formData.skills}
                onChange={handleChange}
                className="form-control"
                placeholder="e.g. Java, Python, C++, React, Algorithms"
              />
            </div>

            <div className="grid-cols-2">
              <div className="form-group">
                <label>{t('profile.githubUrl', 'GitHub Profile URL')}</label>
                <input
                  type="text"
                  name="githubUrl"
                  value={formData.githubUrl}
                  onChange={handleChange}
                  className="form-control"
                  placeholder="https://github.com/yourhandle"
                />
              </div>

              <div className="form-group">
                <label>{t('profile.linkedinUrl', 'LinkedIn Profile URL')}</label>
                <input
                  type="text"
                  name="linkedinUrl"
                  value={formData.linkedinUrl}
                  onChange={handleChange}
                  className="form-control"
                  placeholder="https://linkedin.com/in/yourhandle"
                />
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem' }}>
              <button
                type="button"
                onClick={() => setIsEditing(false)}
                className="btn btn-outline"
                disabled={saving}
              >
                <X size={16} />
                <span>{t('profile.cancel', 'Cancel')}</span>
              </button>
              <button
                type="submit"
                className="btn btn-primary"
                disabled={saving}
              >
                <Save size={16} />
                <span>{saving ? t('common.saving', 'Saving...') : t('profile.saveChanges', 'Save Profile Changes')}</span>
              </button>
            </div>
          </form>
        </div>
      ) : (
        /* View Profile Mode */
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
            <div
              style={{
                width: '72px',
                height: '72px',
                borderRadius: '50%',
                backgroundColor: 'rgba(59, 130, 246, 0.2)',
                color: '#3b82f6',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '1.75rem',
                fontWeight: 700,
              }}
            >
              {profile?.name ? profile.name.charAt(0).toUpperCase() : 'U'}
            </div>

            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.25rem' }}>
                <h2 style={{ fontSize: '1.4rem', fontWeight: 700 }}>{profile?.name}</h2>
                <span className={`badge ${isAdminRole(profile?.role) ? 'badge-admin' : 'badge-user'}`}>
                  {getRoleLabel(profile?.role)}
                </span>
              </div>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>@{profile?.username}</p>
            </div>
          </div>

          <div className="card">
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1.25rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
              Account Information
            </h3>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <Mail size={18} color="var(--text-muted)" />
                <span style={{ color: 'var(--text-muted)', width: '120px' }}>Email:</span>
                <span style={{ fontWeight: 600 }}>{profile?.email}</span>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <User size={18} color="var(--text-muted)" />
                <span style={{ color: 'var(--text-muted)', width: '120px' }}>Username:</span>
                <span style={{ fontWeight: 600 }}>{profile?.username}</span>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <Shield size={18} color="var(--text-muted)" />
                <span style={{ color: 'var(--text-muted)', width: '120px' }}>Role:</span>
                <span style={{ fontWeight: 600 }}>{getRoleLabel(profile?.role)}</span>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <Calendar size={18} color="var(--text-muted)" />
                <span style={{ color: 'var(--text-muted)', width: '120px' }}>Joined:</span>
                <span>{profile?.createdAt ? new Date(profile.createdAt).toLocaleDateString() : 'Recent'}</span>
              </div>
            </div>
          </div>

          {certProgress && (
            <div className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
                <h3 style={{ fontSize: '1.1rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <Trophy size={18} color="#fbbf24" /> Achievements
                </h3>
                <Link to="/certificates" className="btn btn-outline btn-sm">View All</Link>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem', marginBottom: '1rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Problems Solved</span>
                  <span style={{ fontWeight: 700 }}>{certProgress.solvedCount}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Certificates Earned</span>
                  <span style={{ fontWeight: 700 }}>{certProgress.earnedCertificates?.length || 0}</span>
                </div>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                {(certProgress.milestoneCards || []).map((card) => (
                  <div key={card.milestone} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '0.9rem' }}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                      {card.status === 'EARNED' ? (
                        <CheckCircle size={15} color="var(--success)" />
                      ) : (
                        <Lock size={13} color="var(--text-subtle)" />
                      )}
                      🏆 {card.milestone} Problems Solved
                    </span>
                    {card.status === 'EARNED' ? (
                      <span style={{ color: 'var(--success)', fontWeight: 600 }}>✓ Earned</span>
                    ) : card.status === 'IN_PROGRESS' ? (
                      <Link to="/certificates" style={{ color: 'var(--primary)', fontWeight: 600, fontSize: '0.85rem' }}>
                        {certProgress.solvedCount} / {card.milestone}
                      </Link>
                    ) : (
                      <span style={{ color: 'var(--text-subtle)' }}>🔒 Locked</span>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="card">
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1.25rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
              Bio & Developer Details
            </h3>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <span style={{ color: 'var(--text-muted)', display: 'block', marginBottom: '0.25rem', fontSize: '0.875rem' }}>Bio:</span>
                <p style={{ color: profile?.bio ? 'var(--text-main)' : 'var(--text-subtle)', fontStyle: profile?.bio ? 'normal' : 'italic' }}>
                  {profile?.bio || 'No bio provided yet. Click "Edit Profile" to add one!'}
                </p>
              </div>

              <div>
                <span style={{ color: 'var(--text-muted)', display: 'block', marginBottom: '0.25rem', fontSize: '0.875rem' }}>Skills:</span>
                <p style={{ color: profile?.skills ? 'var(--text-main)' : 'var(--text-subtle)', fontStyle: profile?.skills ? 'normal' : 'italic' }}>
                  {profile?.skills || 'No skills listed yet.'}
                </p>
              </div>

              <div style={{ display: 'flex', gap: '1.5rem', marginTop: '0.5rem', flexWrap: 'wrap' }}>
                {profile?.githubUrl && (
                  <a href={profile.githubUrl} target="_blank" rel="noreferrer" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--primary)' }}>
                    <Github size={18} />
                    <span>GitHub Profile</span>
                  </a>
                )}
                {profile?.linkedinUrl && (
                  <a href={profile.linkedinUrl} target="_blank" rel="noreferrer" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#0a66c2' }}>
                    <Linkedin size={18} />
                    <span>LinkedIn Profile</span>
                  </a>
                )}
              </div>
            </div>
          </div>

          <div className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: showPasswordForm ? '1.25rem' : 0, borderBottom: showPasswordForm ? '1px solid var(--border-color)' : 'none', paddingBottom: showPasswordForm ? '0.75rem' : 0 }}>
              <h3 style={{ fontSize: '1.1rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <KeyRound size={18} color="#3b82f6" />
                <span>{t('profile.changePassword', 'Change Password')}</span>
              </h3>
              {!showPasswordForm && (
                <button onClick={() => setShowPasswordForm(true)} className="btn btn-outline btn-sm">
                  {t('profile.changePassword', 'Change Password')}
                </button>
              )}
            </div>

            {passwordSuccess && !showPasswordForm && (
              <div className="alert alert-success" style={{ marginTop: '1rem' }}>
                <CheckCircle size={18} />
                <span>{passwordSuccess}</span>
              </div>
            )}

            {showPasswordForm && (
              <form onSubmit={handlePasswordSubmit}>
                {passwordError && (
                  <div className="alert alert-error">
                    <AlertCircle size={18} />
                    <span>{passwordError}</span>
                  </div>
                )}

                <div className="form-group">
                  <label>{t('profile.currentPassword', 'Current Password')} *</label>
                  <input
                    type="password"
                    name="currentPassword"
                    value={passwordData.currentPassword}
                    onChange={handlePasswordChange}
                    required
                    className="form-control"
                  />
                </div>

                <div className="grid-cols-2">
                  <div className="form-group">
                    <label>{t('profile.newPassword', 'New Password')} *</label>
                    <input
                      type="password"
                      name="newPassword"
                      value={passwordData.newPassword}
                      onChange={handlePasswordChange}
                      required
                      minLength={6}
                      className="form-control"
                      placeholder="Minimum 6 characters"
                    />
                  </div>

                  <div className="form-group">
                    <label>{t('profile.confirmNewPassword', 'Confirm New Password')} *</label>
                    <input
                      type="password"
                      name="confirmNewPassword"
                      value={passwordData.confirmNewPassword}
                      onChange={handlePasswordChange}
                      required
                      className="form-control"
                    />
                  </div>
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '0.5rem' }}>
                  <button
                    type="button"
                    onClick={() => {
                      setShowPasswordForm(false);
                      setPasswordError('');
                      setPasswordData({ currentPassword: '', newPassword: '', confirmNewPassword: '' });
                    }}
                    className="btn btn-outline"
                    disabled={changingPassword}
                  >
                    <X size={16} />
                    <span>{t('profile.cancel', 'Cancel')}</span>
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={changingPassword}>
                    <Save size={16} />
                    <span>{changingPassword ? t('common.saving', 'Updating...') : t('profile.updatePassword', 'Update Password')}</span>
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default Profile;
