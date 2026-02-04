import React, { useEffect, useState } from 'react';
import { apiService } from '../../services/api';
import type { Review } from '../../types/review.types';
import './ReviewModeration.css';

const ReviewModeration: React.FC = () => {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'all' | 'unverified'>('unverified');
  const [processingId, setProcessingId] = useState<number | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    fetchReviews();
  }, []);

  const fetchReviews = async () => {
    try {
      setLoading(true);
      // Note: This would need a dedicated admin endpoint
      // For now, this is a placeholder
      setReviews([]);
    } catch (err) {
      console.error('Failed to fetch reviews:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleVerify = async (review: Review) => {
    if (!confirm('Verify this review?')) {
      return;
    }

    setProcessingId(review.id);
    try {
      await apiService.verifyReview(review.id);
      setSuccessMessage('Review verified successfully!');
      setReviews(reviews.filter(r => r.id !== review.id));
      setTimeout(() => setSuccessMessage(null), 5000);
    } catch (err: unknown) {
      console.error('Failed to verify review:', err);
    } finally {
      setProcessingId(null);
    }
  };

  const handleDelete = async (review: Review) => {
    if (!confirm('Delete this review? This action cannot be undone.')) {
      return;
    }

    setProcessingId(review.id);
    try {
      await apiService.deleteReview(review.id);
      setSuccessMessage('Review deleted successfully.');
      setReviews(reviews.filter(r => r.id !== review.id));
      setTimeout(() => setSuccessMessage(null), 5000);
    } catch (err: unknown) {
      console.error('Failed to delete review:', err);
    } finally {
      setProcessingId(null);
    }
  };

  const filteredReviews = filter === 'unverified'
    ? reviews.filter(r => !r.isVerified)
    : reviews;

  if (loading) {
    return (
      <div className="admin-loading">
        <div className="spinner"></div>
        <p>Loading reviews...</p>
      </div>
    );
  }

  return (
    <div className="review-moderation">
      <div className="section-header">
        <h2>Review Moderation</h2>
        <div className="filter-buttons">
          <button
            className={`filter-btn ${filter === 'unverified' ? 'active' : ''}`}
            onClick={() => setFilter('unverified')}
          >
            Unverified Only
          </button>
          <button
            className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
            onClick={() => setFilter('all')}
          >
            All Reviews
          </button>
        </div>
      </div>

      {successMessage && (
        <div className="success-message">
          ✓ {successMessage}
        </div>
      )}

      <div className="info-banner">
        <strong>ℹ️ Coming Soon:</strong> Review moderation features including verification,
        flagging, and content filtering will be enhanced in the next update.
      </div>

      {filteredReviews.length === 0 ? (
        <div className="admin-empty">
          <div className="admin-empty-icon">⭐</div>
          <h3>No Reviews to Moderate</h3>
          <p>
            {filter === 'unverified'
              ? 'All reviews are verified!'
              : 'No reviews found.'}
          </p>
        </div>
      ) : (
        <div className="reviews-grid">
          {filteredReviews.map((review) => (
            <div key={review.id} className="review-card">
              <div className="review-header">
                <div className="review-info">
                  <h4>{review.user.username}</h4>
                  <div className="rating-stars">
                    {'⭐'.repeat(review.rating)}
                  </div>
                </div>
                {!review.isVerified && (
                  <span className="unverified-badge">Unverified</span>
                )}
              </div>

              {review.comment && (
                <p className="review-comment">{review.comment}</p>
              )}

              <div className="review-meta">
                <span className="meta-item">
                  📅 {new Date(review.createdAt).toLocaleDateString()}
                </span>
              </div>

              <div className="review-actions">
                {!review.isVerified && (
                  <button
                    onClick={() => handleVerify(review)}
                    className="btn-verify"
                    disabled={processingId === review.id}
                  >
                    {processingId === review.id ? 'Processing...' : '✓ Verify'}
                  </button>
                )}
                <button
                  onClick={() => handleDelete(review)}
                  className="btn-delete-review"
                  disabled={processingId === review.id}
                >
                  {processingId === review.id ? 'Processing...' : '🗑️ Delete'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ReviewModeration;