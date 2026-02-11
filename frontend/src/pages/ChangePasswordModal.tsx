import React, { useState, useEffect } from 'react';
import './ChangePasswordModal.css';

interface ChangePasswordModalProps {
  onClose: () => void;
  onSave: (currentPassword: string, newPassword: string) => Promise<void>;
}

interface PasswordStrength {
  score: number;      // 0-4
  label: string;
  color: string;
  width: string;
}

const getStrength = (password: string): PasswordStrength => {
  if (!password) return { score: 0, label: '', color: '#e2e8f0', width: '0%' };

  let score = 0;
  if (password.length >= 8)  score++;
  if (password.length >= 12) score++;
  if (/[A-Z]/.test(password) && /[a-z]/.test(password)) score++;
  if (/\d/.test(password)) score++;
  if (/[^A-Za-z0-9]/.test(password)) score++;

  // Cap at 4 "levels"
  const capped = Math.min(score, 4);

  const levels: Omit<PasswordStrength, 'score'>[] = [
    { label: 'Too short',  color: '#fc8181', width: '15%' },
    { label: 'Weak',       color: '#f6ad55', width: '35%' },
    { label: 'Fair',       color: '#fbbf24', width: '60%' },
    { label: 'Strong',     color: '#68d391', width: '85%' },
    { label: 'Very strong',color: '#48bb78', width: '100%'},
  ];

  return { score: capped, ...levels[capped] };
};

const ChangePasswordModal: React.FC<ChangePasswordModalProps> = ({ onClose, onSave }) => {
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew]         = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const [saving, setSaving]   = useState(false);
  const [error, setError]     = useState('');
  const [success, setSuccess] = useState(false);

  const strength = getStrength(newPassword);

  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onClose]);

  const clearError = () => {
    if (error) setError('');
  };

  const validate = (): string => {
    if (!currentPassword) return 'Please enter your current password';
    if (!newPassword)      return 'Please enter a new password';
    if (newPassword.length < 8) return 'New password must be at least 8 characters';
    if (newPassword === currentPassword) return 'New password must be different from current password';
    if (newPassword !== confirmPassword) return 'Passwords do not match';
    return '';
  };

  const handleSave = async () => {
    const validationError = validate();
    if (validationError) { setError(validationError); return; }

    setSaving(true);
    setError('');
    try {
      await onSave(currentPassword, newPassword);
      setSuccess(true);
      setTimeout(() => onClose(), 1500);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : 'Failed to change password. Please try again.';
      setError(message);
    } finally {
      setSaving(false);
    }
  };

  const isReady = currentPassword && newPassword && confirmPassword && !success;

  return (
    <div className="change-pw-overlay" onClick={onClose}>
      <div className="change-pw-modal" onClick={(e) => e.stopPropagation()}>

        {/* Header */}
        <div className="change-pw-header">
          <div className="change-pw-header-inner">
            <div className="change-pw-header-text">
              <h2>Change Password</h2>
              <p>Keep your account secure</p>
            </div>
            <button className="change-pw-close" onClick={onClose} aria-label="Close">✕</button>
          </div>
        </div>

        {/* Body */}
        <div className="change-pw-body">

          {/* Security note */}
          <div className="change-pw-security-badge">
            <span className="security-icon">🔐</span>
            <p>Use a mix of letters, numbers, and symbols for a stronger password.</p>
          </div>

          {/* Banners */}
          {error && (
            <div className="pw-error-banner">
              <span>⚠️</span> {error}
            </div>
          )}
          {success && (
            <div className="pw-success-banner">
              <span>✅</span> Password changed successfully!
            </div>
          )}

          {/* Current password */}
          <div className="pw-form-group">
            <label htmlFor="currentPw">Current Password</label>
            <div className="pw-input-wrapper">
              <input
                id="currentPw"
                type={showCurrent ? 'text' : 'password'}
                value={currentPassword}
                onChange={(e) => { setCurrentPassword(e.target.value); clearError(); }}
                disabled={saving || success}
                placeholder="Enter current password"
                autoComplete="current-password"
              />
              <button
                className="pw-toggle-btn"
                type="button"
                onClick={() => setShowCurrent(!showCurrent)}
                aria-label="Toggle visibility"
              >
                {showCurrent ? '🙈' : '👁️'}
              </button>
            </div>
          </div>

          <hr className="pw-divider" />

          {/* New password */}
          <div className="pw-form-group">
            <label htmlFor="newPw">New Password</label>
            <div className="pw-input-wrapper">
              <input
                id="newPw"
                type={showNew ? 'text' : 'password'}
                value={newPassword}
                onChange={(e) => { setNewPassword(e.target.value); clearError(); }}
                disabled={saving || success}
                placeholder="Min. 8 characters"
                autoComplete="new-password"
              />
              <button
                className="pw-toggle-btn"
                type="button"
                onClick={() => setShowNew(!showNew)}
                aria-label="Toggle visibility"
              >
                {showNew ? '🙈' : '👁️'}
              </button>
            </div>
            {/* Strength meter */}
            {newPassword.length > 0 && (
              <>
                <div className="pw-strength-bar-track">
                  <div
                    className="pw-strength-bar-fill"
                    style={{ width: strength.width, backgroundColor: strength.color }}
                  />
                </div>
                <span className="pw-strength-label" style={{ color: strength.color }}>
                  {strength.label}
                </span>
              </>
            )}
          </div>

          {/* Confirm password */}
          <div className="pw-form-group">
            <label htmlFor="confirmPw">Confirm New Password</label>
            <div className="pw-input-wrapper">
              <input
                id="confirmPw"
                type={showConfirm ? 'text' : 'password'}
                value={confirmPassword}
                onChange={(e) => { setConfirmPassword(e.target.value); clearError(); }}
                disabled={saving || success}
                placeholder="Re-enter new password"
                autoComplete="new-password"
              />
              <button
                className="pw-toggle-btn"
                type="button"
                onClick={() => setShowConfirm(!showConfirm)}
                aria-label="Toggle visibility"
              >
                {showConfirm ? '🙈' : '👁️'}
              </button>
            </div>
            {/* Match indicator */}
            {confirmPassword.length > 0 && (
              <span
                className="pw-strength-label"
                style={{ color: newPassword === confirmPassword ? '#48bb78' : '#fc8181' }}
              >
                {newPassword === confirmPassword ? '✓ Passwords match' : '✗ Passwords do not match'}
              </span>
            )}
          </div>

        </div>

        {/* Footer */}
        <div className="change-pw-footer">
          <button className="btn-pw-cancel" onClick={onClose} disabled={saving}>
            Cancel
          </button>
          <button
            className="btn-pw-save"
            onClick={handleSave}
            disabled={saving || !isReady}
          >
            {saving ? 'Updating…' : success ? '✓ Updated!' : 'Update Password'}
          </button>
        </div>

      </div>
    </div>
  );
};

export default ChangePasswordModal;