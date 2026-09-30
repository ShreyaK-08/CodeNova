import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import api from '../../services/api';
import adminService from '../../services/adminService';
import { useAuth } from '../../context/AuthContext';
import { isAdminRole, getRoleLabel } from '../../utils/roleUtils';
import { AlertCircle, CheckCircle, Trash2 } from 'lucide-react';

const UserManagement = () => {
  const { t } = useTranslation();
  const { user: currentUser } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [deletingId, setDeletingId] = useState(null);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const res = await api.get('/admin/users');
      setUsers(res.data);
    } catch (err) {
      setError('Failed to fetch user accounts.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleDelete = async (targetUser) => {
    if (!window.confirm(`Delete user "${targetUser.username}"? This also removes their submissions and cannot be undone.`)) {
      return;
    }

    setError('');
    setSuccess('');
    setDeletingId(targetUser.id);

    try {
      await adminService.deleteUser(targetUser.id);
      setUsers((prev) => prev.filter((u) => u.id !== targetUser.id));
      setSuccess(`User "${targetUser.username}" was deleted.`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete user.');
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.875rem', fontWeight: 800 }}>{t('admin.userManagement', 'User Management')}</h1>
        <p style={{ color: 'var(--text-muted)' }}>{t('admin.userManagementSubtitle', 'Registered students, candidates, and administrative personnel.')}</p>
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

      <div className="card">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>{t('common.loading', 'Loading...')}</div>
        ) : users.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th style={{ width: '60px' }}>{t('admin.id', 'ID')}</th>
                  <th>{t('profile.fullName', 'Name')}</th>
                  <th>{t('profile.username', 'Username')}</th>
                  <th>{t('admin.email', 'Email')}</th>
                  <th>{t('admin.role', 'Role')}</th>
                  <th>{t('profile.bioSummary', 'Skills / Bio')}</th>
                  <th>{t('admin.joined', 'Registered')}</th>
                  <th style={{ width: '90px' }}>{t('common.action', 'Action')}</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td>{u.id}</td>
                    <td style={{ fontWeight: 600 }}>{u.name}</td>
                    <td>@{u.username}</td>
                    <td>{u.email}</td>
                    <td>
                      <span className={`badge ${isAdminRole(u.role) ? 'badge-admin' : 'badge-user'}`}>
                        {getRoleLabel(u.role)}
                      </span>
                    </td>
                    <td style={{ maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', color: 'var(--text-muted)' }}>
                      {u.skills || u.bio || '-'}
                    </td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                      {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : 'N/A'}
                    </td>
                    <td>
                      {isAdminRole(u.role) || u.id === currentUser?.id ? (
                        <span style={{ color: 'var(--text-subtle)', fontSize: '0.8rem' }}>—</span>
                      ) : (
                        <button
                          onClick={() => handleDelete(u)}
                          disabled={deletingId === u.id}
                          className="btn btn-outline btn-sm"
                          style={{ color: '#f87171', borderColor: '#f87171', padding: '0.35rem 0.6rem' }}
                          title="Delete user"
                        >
                          <Trash2 size={14} />
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>
            {t('common.noData', 'No registered users found.')}
          </div>
        )}
      </div>
    </div>
  );
};

export default UserManagement;
