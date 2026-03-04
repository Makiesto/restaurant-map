import React, { useEffect, useState } from 'react';
import { apiService } from '../../services/api';
import type { User } from '../../types/auth.types';
import './UserManagement.css';

const UserManagement: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [processingId, setProcessingId] = useState<number | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await apiService.getAllUsers();
      setUsers(data);
    } catch (err) {
      console.error('Failed to fetch users:', err);
      setError('Failed to load users. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyUser = async (user: User) => {
    if (!window.confirm(`Verify "${user.email}" as a legitimate user?`)) return;

    setProcessingId(user.id);
    try {
      await apiService.verifyUser(user.id);
      setSuccessMessage(`✅ "${user.email}" has been verified!`);
      setUsers(prev =>
        prev.map(u =>
          u.id === user.id ? { ...u, verifiedAt: new Date().toISOString() } : u
        )
      );
      setTimeout(() => setSuccessMessage(null), 5000);
    } catch (err) {
      console.error('Failed to verify user:', err);
      alert('Failed to verify user. Please try again.');
    } finally {
      setProcessingId(null);
    }
  };

  const filteredUsers = users.filter(user =>
    user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (`${user.firstName} ${user.lastName}`).toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) {
    return (
      <div className="admin-loading">
        <div className="spinner"></div>
        <p>Loading users...</p>
      </div>
    );
  }

  return (
    <div className="user-management">
      <div className="section-header">
        <h2>👥 User Management</h2>
        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
          <div className="search-box">
            <input
              type="text"
              placeholder="Search by name or email..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="search-input"
            />
          </div>
          <button onClick={fetchUsers} className="btn-refresh">
            🔄 Refresh
          </button>
        </div>
      </div>

      {error && (
        <div className="admin-error">❌ {error}</div>
      )}

      {successMessage && (
        <div className="success-message">{successMessage}</div>
      )}

      {filteredUsers.length === 0 && !error ? (
        <div className="admin-empty">
          <div className="admin-empty-icon">👥</div>
          <h3>{searchTerm ? 'No users match your search' : 'No Users Found'}</h3>
          <p>{searchTerm ? 'Try a different search term.' : 'No users are registered yet.'}</p>
        </div>
      ) : (
        <>
          <div className="pending-count">
            📊 Showing <strong>{filteredUsers.length}</strong> of <strong>{users.length}</strong> user{users.length !== 1 ? 's' : ''}
          </div>

          <div className="users-table-wrapper">
            <table className="users-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Verified</th>
                  <th>Status</th>
                  <th>Joined</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.map((user) => (
                  <tr key={user.id}>
                    <td>#{user.id}</td>
                    <td>{user.firstName} {user.lastName}</td>
                    <td>{user.email}</td>
                    <td>
                      <span className={`role-badge role-${user.role.toLowerCase()}`}>
                        {user.role === 'ADMIN' && '👑 '}
                        {user.role === 'VERIFIED_USER' && '✅ '}
                        {user.role === 'USER' && '👤 '}
                        {user.role}
                      </span>
                    </td>
                    <td>
                      <span className={`verify-badge ${user.verifiedAt ? 'verified' : 'unverified'}`}>
                        {user.verifiedAt ? '✓ Verified' : '✗ Unverified'}
                      </span>
                    </td>
                    <td>
                      <span className={`status-dot ${user.isActive ? 'active' : 'inactive'}`}>
                        {user.isActive ? '🟢 Active' : '🔴 Inactive'}
                      </span>
                    </td>
                    <td>{new Date(user.createdAt).toLocaleDateString('en-US', {
                      year: 'numeric', month: 'short', day: 'numeric'
                    })}</td>
                    <td>
                      {!user.verifiedAt && user.role !== 'ADMIN' && (
                        <button
                          className="btn-verify"
                          onClick={() => handleVerifyUser(user)}
                          disabled={processingId === user.id}
                          style={{ padding: '6px 14px', fontSize: '13px' }}
                        >
                          {processingId === user.id ? '⏳' : '✓ Verify'}
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
};

export default UserManagement;