import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiService } from '../services/api';
import './EmailVerificationBanner.css';
import { AxiosError } from 'axios';

const EmailVerificationBanner: React.FC = () => {
  const { user } = useAuth();
  const [sending, setSending] = useState(false);
  const [message, setMessage] = useState('');
  const [dismissed, setDismissed] = useState(false);

  // Don't show if user is verified or banner is dismissed
  if (!user || user.emailVerified || dismissed) {
    return null;
  }

  const handleResend = async () => {
    setSending(true);
    setMessage('');

    try {
      await apiService.resendVerificationEmail(user.email);
      setMessage('✅ Verification email sent! Check your inbox.');
    } catch (error) {
      const errorMessage = error instanceof AxiosError
        ? error.response?.data?.message || error.message
        : 'Failed to send email';
      setMessage('❌ ' + errorMessage);
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="email-verification-banner">
      <div className="banner-content">
        <div className="banner-icon">⚠️</div>
        <div className="banner-text">
          <strong>Please verify your email address</strong>
          <span>We sent a verification link to {user.email}</span>
        </div>
        <div className="banner-actions">
          <button
            className="btn-resend"
            onClick={handleResend}
            disabled={sending}
          >
            {sending ? 'Sending...' : 'Resend Email'}
          </button>
          <button
            className="btn-dismiss"
            onClick={() => setDismissed(true)}
            aria-label="Dismiss"
          >
            ✕
          </button>
        </div>
      </div>
      {message && (
        <div className="banner-message">{message}</div>
      )}
    </div>
  );
};

export default EmailVerificationBanner;