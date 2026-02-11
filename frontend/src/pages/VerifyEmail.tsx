import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { apiService } from '../services/api';
import './VerifyEmail.css';

const VerifyEmail: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState<'verifying' | 'success' | 'error'>('verifying');
  const [message, setMessage] = useState('');

  useEffect(() => {
    const verifyEmail = async () => {
      const token = searchParams.get('token');

      if (!token) {
        setStatus('error');
        setMessage('Invalid verification link. Token is missing.');
        return;
      }

      try {
        await apiService.verifyEmail(token);
        setStatus('success');
        setMessage('Your email has been verified successfully!');

        // Redirect to login after 3 seconds
        setTimeout(() => {
          navigate('/login');
        }, 3000);
      } catch (error: any) {
        setStatus('error');
        setMessage(error.response?.data?.message || error.message || 'Verification failed. The link may be invalid or expired.');
      }
    };

    verifyEmail();
  }, [searchParams, navigate]);

  return (
    <div className="verify-email-container">
      <div className="verify-email-card">
        {status === 'verifying' && (
          <>
            <div className="verify-icon verifying">⏳</div>
            <h1>Verifying Your Email...</h1>
            <p>Please wait while we verify your email address.</p>
            <div className="spinner"></div>
          </>
        )}

        {status === 'success' && (
          <>
            <div className="verify-icon success">✅</div>
            <h1>Email Verified!</h1>
            <p>{message}</p>
            <p className="redirect-message">Redirecting you to login...</p>
          </>
        )}

        {status === 'error' && (
          <>
            <div className="verify-icon error">❌</div>
            <h1>Verification Failed</h1>
            <p>{message}</p>
            <div className="verify-actions">
              <button className="btn-primary" onClick={() => navigate('/login')}>
                Go to Login
              </button>
              <button className="btn-secondary" onClick={() => navigate('/resend-verification')}>
                Resend Verification Email
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default VerifyEmail;