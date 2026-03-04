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
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchReviews();
  }, []);

  // NOTE: This requires a backend endpoint GET /admin/reviews
  // Until that exists, we aggregate reviews from approved restaurants as a fallback.
  const fetchReviews = async () => {
    try {
      setLoading(true);
      setError(null);

      // Try dedicated admin reviews endpoint first
      try {
        const response = await fetch('/api/admin/reviews', {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`,
          },
        });
        if (response.ok) {
          const data = await response.json();
          setReviews(data);
          return;
        }
      } catch {
        // fall through to aggregation approach
      }

      // Fallback: pull reviews from all approved restaurants
      const restaurants = await apiService.getApprovedRestaurants();
      const allReviews: Review[] = [];
      await Promise.allSettled(
        restaurants.map(async (r) => {
          try {
            const rReviews = await apiService.getRestaurantReviews(r.id);
            allReviews.push(...rReviews);
          } catch {
            // skip failed restaurants silently
          }
        })
      );
      setReviews(allReviews);
    } catch (err) {
      console.error('Failed to fetch reviews:', err);
      setError('Failed to load reviews. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerify = async (review: Review) => {
    if (!window.confirm('Verify this review as legitimate?')) return;

    setProcessingId(review.id);
    try {
      await apiService.verifyReview(review.id);
      setSuccessMessage('✅ Review verified successfully!');
      setReviews(prev =>
        prev.map(r => r.id === review.id ? { ...r, isVerified: true } : r)
      );
      setTimeout(() => setSuccessMessage(null), 5000);
    } catch (err) {
      console.error('Failed to verify review:', err);
      alert('Failed to verify review. Please try again.');
    } finally {
      setProcessingId(null);
    }
  };

  const handleDelete = async (review: Review) => {
    if (!window.confirm('Delete this review? This action cannot be undone.')) return;

    setProcessingId(review.id);
    try {
      await apiService.deleteReview(review.id);
      setSuccessMessage('🗑️ Review deleted successfully.');
      setReviews(prev => prev.filter(r => r.id !== review.id));
      setTimeout(() => setSuccessMessage(null), 5000);
    } catch (err) {
      console.error('Failed to delete review:', err);
      alert('Failed to delete review. Please try again.');
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
        <h2>⭐ Review Moderation</h2>
        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
          <div className="filter-buttons">
            <button
              className={`filter-btn ${filter === 'unverified' ? 'active' : ''}`}
              onClick={() => setFilter('unverified')}
            >
              Unverified Only
              {reviews.filter(r => !r.isVerified).length > 0 && (
                <span style={{
                  marginLeft: '6px',
                  background: filter === 'unverified' ? 'rgba(255,255,255,0.3)' : '#667eea',
                  color: 'white',
                  borderRadius: '10px',
                  padding: '1px 7px',
                  fontSize: '11px',
                  fontWeight: 700,
                }}>
                  {reviews.filter(r => !r.isVerified).length}
                </span>
              )}
            </button>
            <button
              className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
              onClick={() => setFilter('all')}
            >
              All Reviews
            </button>
          </div>
          <button
            onClick={fetchReviews}
            style={{
              padding: '10px 16px',
              background: '#3498db',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer',
              fontWeight: 600,
              fontSize: '14px',
            }}
          >
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

      {reviews.length > 0 && (
        <div style={{
          background: '#e3f2fd',
          padding: '10px 16px',
          borderRadius: '8px',
          marginBottom: '20px',
          color: '#1976d2',
          fontSize: '14px',
          borderLeft: '4px solid #2196f3',
        }}>
          📊 Showing <strong>{filteredReviews.length}</strong> of <strong>{reviews.length}</strong> review{reviews.length !== 1 ? 's' : ''}
          {filter === 'unverified' && ` · ${reviews.filter(r => !r.isVerified).length} unverified`}
        </div>
      )}

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
                  <h4>{review.user?.username || review.userName || 'Anonymous'}</h4>
                  {review.restaurantName && (
                    <p style={{ margin: '2px 0 6px', fontSize: '13px', color: '#718096' }}>
                      🏪 {review.restaurantName}
                    </p>
                  )}
                  <div className="rating-stars">
                    {'⭐'.repeat(review.rating)}{'☆'.repeat(5 - review.rating)}
                  </div>
                </div>
                {!review.isVerified && (
                  <span className="unverified-badge">Unverified</span>
                )}
                {review.isVerified && (
                  <span style={{
                    background: '#d1fae5',
                    color: '#065f46',
                    padding: '4px 10px',
                    borderRadius: '10px',
                    fontSize: '11px',
                    fontWeight: 700,
                    textTransform: 'uppercase',
                  }}>✓ Verified</span>
                )}
              </div>

              {review.comment && (
                <p className="review-comment">{review.comment}</p>
              )}

              <div className="review-meta">
                <span className="meta-item">
                  📅 {new Date(review.createdAt).toLocaleDateString('en-US', {
                    year: 'numeric', month: 'short', day: 'numeric'
                  })}
                </span>
                <span className="meta-item">ID: #{review.id}</span>
              </div>

              <div className="review-actions">
                {!review.isVerified && (
                  <button
                    onClick={() => handleVerify(review)}
                    className="btn-verify"
                    disabled={processingId === review.id}
                  >
                    {processingId === review.id ? '⏳ Processing...' : '✓ Verify'}
                  </button>
                )}
                <button
                  onClick={() => handleDelete(review)}
                  className="btn-delete-review"
                  disabled={processingId === review.id}
                >
                  {processingId === review.id ? '⏳ Processing...' : '🗑️ Delete'}
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