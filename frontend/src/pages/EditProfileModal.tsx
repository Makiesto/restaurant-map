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
  onSave: (updated: Partial<User>) => Promise<void>;
  onChangePasswordClick: () => void;
}

const EditProfileModal: React.FC<EditProfileModalProps> = ({
  user,
  onClose,
  onSave,
  onChangePasswordClick,
}) => {
  const [formData, setFormData] = useState({
    firstName: user.firstName,
    lastName: user.lastName,
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
    `${formData.firstName.charAt(0)}${formData.lastName.charAt(0)}`.toUpperCase();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
    setSuccess(false);
  };

  const validate = (): string => {
    if (!formData.firstName.trim()) return 'First name is required';
    if (!formData.lastName.trim()) return 'Last name is required';
    if (formData.firstName.trim().length < 2) return 'First name must be at least 2 characters';
    if (formData.lastName.trim().length < 2) return 'Last name must be at least 2 characters';
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
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
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
    formData.firstName.trim() !== user.firstName ||
    formData.lastName.trim() !== user.lastName ||
    formData.phoneNumber.trim() !== (user.phoneNumber || '');

  return (
    <div className="edit-profile-overlay" onClick={onClose}>
      <div className="edit-profile-modal" onClick={(e) => e.stopPropagation()}>

        {/* Header */}
        <div className="edit-profile-header">
          <div className="edit-profile-header-inner">
            <div>
              <h2>Edit Profile</h2>
              <p>Update your personal information</p>
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
              <strong>{formData.firstName} {formData.lastName}</strong>
              <p>{user.email}</p>
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

          {/* Name row */}
          <div className="edit-form-row">
            <div className="edit-form-group">
              <label htmlFor="firstName">First Name</label>
              <input
                id="firstName"
                name="firstName"
                type="text"
                value={formData.firstName}
                onChange={handleChange}
                disabled={saving}
                placeholder="John"
                autoComplete="given-name"
              />
            </div>
            <div className="edit-form-group">
              <label htmlFor="lastName">Last Name</label>
              <input
                id="lastName"
                name="lastName"
                type="text"
                value={formData.lastName}
                onChange={handleChange}
                disabled={saving}
                placeholder="Doe"
                autoComplete="family-name"
              />
            </div>
          </div>

          {/* Email — read-only */}
          <div className="edit-form-group">
            <label htmlFor="email">Email Address</label>
            <input
              id="email"
              type="email"
              value={user.email}
              readOnly
              className="readonly-field"
              tabIndex={-1}
            />
            <span className="field-hint">Email cannot be changed</span>
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
          </div>

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