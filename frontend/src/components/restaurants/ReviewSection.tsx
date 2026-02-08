import React, {useEffect, useState} from 'react';
import {Link} from 'react-router-dom';
import {useAuth} from '../../context/AuthContext.tsx';
import {apiService} from '../../services/api.ts';
import type {Review, ReviewStats, CreateReviewRequest} from '../../types/review.types.ts';
import './ReviewSection.css';
import axios from "axios";

interface ReviewSectionProps {
    restaurantId: number;
}

const ReviewSection: React.FC<ReviewSectionProps> = ({restaurantId}) => {
    const {isAuthenticated} = useAuth();
    const [reviews, setReviews] = useState<Review[]>([]);
    const [stats, setStats] = useState<ReviewStats | null>(null);
    const [myReview, setMyReview] = useState<Review | null>(null);
    const [isWriting, setIsWriting] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string>('');

    const [formData, setFormData] = useState<CreateReviewRequest>({
        rating: 0,
        comment: '',
    });

    useEffect(() => {
        fetchReviews();
    }, [restaurantId]);

    const fetchReviews = async () => {
        try {
            setLoading(true);
            const [reviewsData, statsData] = await Promise.all([
                apiService.getRestaurantReviews(restaurantId),
                apiService.getRestaurantStats(restaurantId),
            ]);
            setReviews(reviewsData);
            setStats(statsData);

            // Fetch user's review if authenticated
            if (isAuthenticated) {
                try {
                    const userReview = await apiService.getMyReviewForRestaurant(restaurantId);
                    setMyReview(userReview);
                } catch (err) {
                    // User hasn't reviewed yet, this is fine
                    console.log('No existing review found');
                }
            }
        } catch (error) {
            console.error('Error fetching reviews:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleStartWriting = () => {
        setIsWriting(true);
        setIsEditing(false);
        setFormData({rating: 0, comment: ''});
        setError('');
    };

    const handleStartEditing = () => {
        if (myReview) {
            setIsEditing(true);
            setIsWriting(true);
            setFormData({
                rating: myReview.rating,
                comment: myReview.comment || '',
            });
            setError('');
        }
    };

    const handleCancel = () => {
        setIsWriting(false);
        setIsEditing(false);
        setFormData({rating: 0, comment: ''});
        setError('');
    };

    const handleSubmit = async () => {
        // Clear previous errors
        setError('');

        // Validate rating
        if (formData.rating === 0) {
            setError('Please select a rating (1-5 stars)');
            return;
        }

        // Validate rating range
        if (formData.rating < 1 || formData.rating > 5) {
            setError('Rating must be between 1 and 5 stars');
            return;
        }

        console.log('Submitting review:', {
            restaurantId,
            rating: formData.rating,
            comment: formData.comment,
            isEditing,
        });

        try {
            setSubmitting(true);
            setError('');

            if (isEditing && myReview) {
                console.log('Updating existing review:', myReview.id);
                await apiService.updateReview(myReview.id, formData);
            } else {
                console.log('Creating new review for restaurant:', restaurantId);
                await apiService.createReview(restaurantId, formData);
            }

            // Success! Refresh reviews and reset form
            await fetchReviews();
            handleCancel();
        } catch (err: unknown) {
            console.error('Review submission error:', err);

            let errorMessage = 'Failed to submit review';

            if (axios.isAxiosError(err)) {
                console.error('Error details:', {
                    status: err.response?.status,
                    statusText: err.response?.statusText,
                    data: err.response?.data,
                });

                // Get detailed error message from backend
                if (err.response?.data?.message) {
                    errorMessage = err.response.data.message;
                } else if (err.response?.data?.error) {
                    errorMessage = err.response.data.error;
                } else if (err.response?.status === 500) {
                    errorMessage = 'Server error. Please check:\n' +
                        '1. You are logged in\n' +
                        '2. The restaurant exists\n' +
                        '3. You haven\'t already reviewed this restaurant';
                } else if (err.response?.status === 409) {
                    errorMessage = 'You have already reviewed this restaurant. Please edit your existing review instead.';
                } else if (err.response?.status === 401) {
                    errorMessage = 'Please login to leave a review';
                } else if (err.response?.status === 400) {
                    errorMessage = 'Invalid review data. Please check your rating and try again.';
                } else {
                    errorMessage = err.message || errorMessage;
                }
            } else if (err instanceof Error) {
                errorMessage = err.message;
            }

            setError(errorMessage);
        } finally {
            setSubmitting(false);
        }
    };

    const handleDelete = async () => {
        if (!myReview || !confirm('Are you sure you want to delete your review?')) {
            return;
        }

        try {
            setError('');
            await apiService.deleteReview(myReview.id);
            await fetchReviews();
        } catch (err: unknown) {
            let errorMessage = 'Failed to delete review';

            if (axios.isAxiosError(err)) {
                errorMessage = err.response?.data?.message || err.message;
            } else if (err instanceof Error) {
                errorMessage = err.message;
            }

            setError(errorMessage);
            console.error('Delete error:', err);
        }
    };

    const renderStars = (rating: number, interactive: boolean = false) => {
        return (
            <div className="star-rating">
                {[1, 2, 3, 4, 5].map((star) => (
                    <span
                        key={star}
                        className={`star ${star <= rating ? 'filled' : ''}`}
                        onClick={() => interactive && setFormData({...formData, rating: star})}
                        style={{cursor: interactive ? 'pointer' : 'default'}}
                    >
            ★
          </span>
                ))}
            </div>
        );
    };

    const getPercentage = (count: number) => {
        if (!stats || stats.totalReviews === 0) return 0;
        return (count / stats.totalReviews) * 100;
    };

    if (loading) {
        return (
            <div className="review-section">
                <h2>Reviews</h2>
                <div style={{textAlign: 'center', padding: '40px'}}>
                    <div className="spinner"></div>
                </div>
            </div>
        );
    }

    return (
        <div className="review-section">
            <h2>Reviews & Ratings</h2>

            {/* Stats */}
            {stats && stats.totalReviews > 0 && (
                <div className="review-stats">
                    <div className="stats-header">
                        <div className="average-rating">
                            <p className="rating-number">{stats.averageRating.toFixed(1)}</p>
                            <div className="rating-stars">★★★★★</div>
                            <p className="total-reviews">
                                Based on {stats.totalReviews} review{stats.totalReviews !== 1 ? 's' : ''}
                            </p>
                        </div>

                        <div className="rating-breakdown">
                            {[
                                {label: '5 stars', count: stats.fiveStars},
                                {label: '4 stars', count: stats.fourStars},
                                {label: '3 stars', count: stats.threeStars},
                                {label: '2 stars', count: stats.twoStars},
                                {label: '1 star', count: stats.oneStar},
                            ].map((item) => (
                                <div key={item.label} className="rating-bar">
                                    <span className="rating-label">{item.label}</span>
                                    <div className="bar-container">
                                        <div
                                            className="bar-fill"
                                            style={{width: `${getPercentage(item.count)}%`}}
                                        ></div>
                                    </div>
                                    <span className="rating-count">{item.count}</span>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            )}

            {/* Write/Edit Review */}
            {isAuthenticated ? (
                <div className="write-review-section">
                    {/* Show error at the top if present */}
                    {error && (
                        <div className="review-error-message">
                            <strong>⚠️ Error:</strong> {error}
                        </div>
                    )}

                    {!isWriting && !myReview && (
                        <>
                            <h3>Share Your Experience</h3>
                            <button onClick={handleStartWriting} className="btn-submit-review">
                                Write a Review
                            </button>
                        </>
                    )}

                    {!isWriting && myReview && (
                        <>
                            <h3>Your Review</h3>
                            <div className="review-card">
                                <div className="review-header">
                                    <div className="review-user-info">
                                        <h4 className="reviewer-name">You</h4>
                                        <p className="review-date">
                                            {new Date(myReview.createdAt).toLocaleDateString()}
                                        </p>
                                    </div>
                                    <div className="review-rating-display">
                                        {'★'.repeat(myReview.rating)}{'☆'.repeat(5 - myReview.rating)}
                                    </div>
                                </div>
                                {myReview.comment && (
                                    <p className="review-comment">{myReview.comment}</p>
                                )}
                                <div className="review-actions-buttons">
                                    <button onClick={handleStartEditing} className="btn-edit-review">
                                        Edit
                                    </button>
                                    <button onClick={handleDelete} className="btn-delete-review">
                                        Delete
                                    </button>
                                </div>
                            </div>
                        </>
                    )}

                    {isWriting && (
                        <>
                            <h3>{isEditing ? 'Edit Your Review' : 'Write a Review'}</h3>
                            <div className="rating-selector">
                                <label>Your Rating: {formData.rating > 0 ? `${formData.rating} star${formData.rating !== 1 ? 's' : ''}` : 'Click to select'}</label>
                                {renderStars(formData.rating, true)}
                            </div>
                            <textarea
                                className="review-textarea"
                                placeholder="Share your experience... (optional)"
                                value={formData.comment}
                                onChange={(e) => setFormData({...formData, comment: e.target.value})}
                                disabled={submitting}
                            />
                            <div className="review-actions">
                                <button
                                    onClick={handleSubmit}
                                    className="btn-submit-review"
                                    disabled={submitting || formData.rating === 0}
                                >
                                    {submitting ? 'Submitting...' : isEditing ? 'Update Review' : 'Submit Review'}
                                </button>
                                <button
                                    onClick={handleCancel}
                                    className="btn-cancel-review"
                                    disabled={submitting}
                                >
                                    Cancel
                                </button>
                            </div>
                        </>
                    )}
                </div>
            ) : (
                <div className="login-prompt">
                    <p>Sign in to leave a review</p>
                    <Link to="/login" className="btn-login">
                        Sign In
                    </Link>
                </div>
            )}

            {/* Reviews List */}
            {reviews.length === 0 ? (
                <div className="no-reviews">
                    <p>No reviews yet</p>
                    <p className="text-muted">Be the first to review this restaurant!</p>
                </div>
            ) : (
                <div className="reviews-list">
                    {reviews
                        .filter((review) => !myReview || review.id !== myReview.id)
                        .map((review) => (
                            <div key={review.id} className="review-card">
                                <div className="review-header">
                                    <div className="review-user-info">
                                        <h4 className="reviewer-name">
                                            {review.userName}
                                            {review.isVerified && (
                                                <span className="verified-badge">✓ Verified</span>
                                            )}
                                        </h4>
                                        <p className="review-date">
                                            {new Date(review.createdAt).toLocaleDateString()}
                                        </p>
                                    </div>
                                    <div className="review-rating-display">
                                        {'★'.repeat(review.rating)}{'☆'.repeat(5 - review.rating)}
                                    </div>
                                </div>
                                {review.comment && (
                                    <p className="review-comment">{review.comment}</p>
                                )}
                            </div>
                        ))}
                </div>
            )}
        </div>
    );
};

export default ReviewSection;