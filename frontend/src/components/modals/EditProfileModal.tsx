import React, { useState, useEffect } from 'react';
import './EditProfileModal.css';

interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  role: 'USER' | 'VERIFIED_USER' | 'ADMIN';
  isActive: boolean;
  createdAt: string;
  verifiedAt?: string;
}

interface EditProfileModalProps {
  user: User;
  onClose: () => void;
  onSave: (updated: { email?: string; phoneNumber?: string }) => Promise<void>;
  onChangePasswordClick: () => void;
}

const EditProfileModal: React.FC<EditProfileModalProps> = ({
  user,
  onClose,
  onSave,
  onChangePasswordClick,
}) => {
  const [formData, setFormData] = useState({
    email: user.email,
    phoneNumber: user.phoneNumber || '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  // Close on Escape
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onClose]);

  const getInitials = () =>
    `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`.toUpperCase();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
    setSuccess(false);
  };

  const validate = (): string => {
    if (!formData.email.trim()) return 'Email is required';

    // Basic email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) return 'Invalid email format';

    return '';
  };

  const handleSave = async () => {
    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    setSaving(true);
    setError('');
    try {
      await onSave({
        email: formData.email.trim() !== user.email ? formData.email.trim() : undefined,
        phoneNumber: formData.phoneNumber.trim() || undefined,
      });
      setSuccess(true);
      setTimeout(() => onClose(), 1200);
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : 'Failed to update profile. Please try again.';
      setError(message);
    } finally {
      setSaving(false);
    }
  };

  const hasChanges =
    formData.email.trim() !== user.email ||
    formData.phoneNumber.trim() !== (user.phoneNumber || '');

  return (
    <div className="edit-profile-overlay" onClick={onClose}>
      <div className="edit-profile-modal" onClick={(e) => e.stopPropagation()}>

        {/* Header */}
        <div className="edit-profile-header">
          <div className="edit-profile-header-inner">
            <div>
              <h2>Edit Profile</h2>
              <p>Update your account information</p>
            </div>
            <button className="edit-profile-close" onClick={onClose} aria-label="Close">
              ✕
            </button>
          </div>
        </div>

        {/* Body */}
        <div className="edit-profile-body">

          {/* Avatar preview */}
          <div className="edit-profile-avatar-row">
            <div className="edit-avatar-preview">{getInitials()}</div>
            <div className="edit-avatar-info">
              <strong>{user.firstName} {user.lastName}</strong>
              <p>Account ID: #{user.id}</p>
            </div>
          </div>

          {/* Banners */}
          {error && (
            <div className="edit-error-banner">
              <span>⚠️</span> {error}
            </div>
          )}
          {success && (
            <div className="edit-success-banner">
              <span>✅</span> Profile updated successfully!
            </div>
          )}

          {/* Name row - READ ONLY */}
          <div className="edit-form-row">
            <div className="edit-form-group">
              <label htmlFor="firstName">First Name</label>
              <input
                id="firstName"
                type="text"
                value={user.firstName}
                readOnly
                className="readonly-field"
                tabIndex={-1}
              />
              <span className="field-hint">First name cannot be changed</span>
            </div>
            <div className="edit-form-group">
              <label htmlFor="lastName">Last Name</label>
              <input
                id="lastName"
                type="text"
                value={user.lastName}
                readOnly
                className="readonly-field"
                tabIndex={-1}
              />
              <span className="field-hint">Last name cannot be changed</span>
            </div>
          </div>

          {/* Email - EDITABLE */}
          <div className="edit-form-group">
            <label htmlFor="email">Email Address</label>
            <input
              id="email"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              disabled={saving}
              placeholder="your.email@example.com"
              autoComplete="email"
            />
            <span className="field-hint">You can change your email address</span>
          </div>

          {/* Phone */}
          <div className="edit-form-group">
            <label htmlFor="phoneNumber">Phone Number</label>
            <input
              id="phoneNumber"
              name="phoneNumber"
              type="tel"
              value={formData.phoneNumber}
              onChange={handleChange}
              disabled={saving}
              placeholder="+1 (555) 000-0000"
              autoComplete="tel"
            />
            <span className="field-hint">Optional</span>
          </div>

          {/* Info box about email change */}
          {formData.email !== user.email && (
            <div className="edit-info-box">
              <span className="info-icon">ℹ️</span>
              <p>
                <strong>Note:</strong> After changing your email, you'll need to log in with your new email address.
              </p>
            </div>
          )}

          {/* Change password link */}
          <button
            className="edit-password-link"
            onClick={() => { onClose(); onChangePasswordClick(); }}
            type="button"
          >
            <div className="edit-password-link-left">
              <span className="edit-password-link-icon">🔒</span>
              <div className="edit-password-link-text">
                <strong>Change Password</strong>
                <span>Update your account password</span>
              </div>
            </div>
            <span className="edit-password-link-arrow">›</span>
          </button>
        </div>

        {/* Footer */}
        <div className="edit-profile-footer">
          <button className="btn-edit-cancel" onClick={onClose} disabled={saving}>
            Cancel
          </button>
          <button
            className="btn-edit-save"
            onClick={handleSave}
            disabled={saving || !hasChanges || success}
          >
            {saving ? 'Saving…' : success ? '✓ Saved!' : 'Save Changes'}
          </button>
        </div>

      </div>
    </div>
  );
};

export default EditProfileModal;