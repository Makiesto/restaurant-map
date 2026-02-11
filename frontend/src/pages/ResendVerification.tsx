import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiService } from '../services/api';
import './ResendVerification.css';

const ResendVerification: React.FC = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess(false);

    if (!email.trim()) {
      setError('Please enter your email address');
      return;
    }

    setLoading(true);

    try {
      await apiService.resendVerificationEmail(email);
      setSuccess(true);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to send verification email');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="resend-verification-container">
      <div className="resend-verification-card">
        <h1>📧 Resend Verification Email</h1>
        <p className="subtitle">
          Enter your email address and we'll send you a new verification link.
        </p>

        {!success ? (
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="email">Email Address</label>
              <input
                type="email"
                id="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="your.email@example.com"
                disabled={loading}
                autoFocus
              />
            </div>

            {error && (
              <div className="error-message">
                <span>⚠️</span> {error}
              </div>
            )}

            <button type="submit" className="btn-submit" disabled={loading}>
              {loading ? 'Sending...' : 'Send Verification Email'}
            </button>

            <div className="back-link">
              <button type="button" onClick={() => navigate('/login')} className="link-button">
                ← Back to Login
              </button>
            </div>
          </form>
        ) : (
          <div className="success-message">
            <div className="success-icon">✅</div>
            <h2>Email Sent!</h2>
            <p>
              We've sent a verification link to <strong>{email}</strong>.
              Please check your inbox and click the link to verify your email.
            </p>
            <p className="note">
              Don't forget to check your spam folder if you don't see the email.
            </p>
            <button className="btn-submit" onClick={() => navigate('/login')}>
              Go to Login
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default ResendVerification;