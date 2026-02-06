import React, {useEffect, useState} from 'react';
import {apiService} from '../../services/api';
import type {Restaurant} from '../../types/restaurant.types';
import './PendingRestaurants.css';
import axios from "axios";

const PendingRestaurants: React.FC = () => {
    const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [processingId, setProcessingId] = useState<number | null>(null);

    useEffect(() => {
        fetchPendingRestaurants();
    }, []);

    const fetchPendingRestaurants = async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await apiService.getPendingRestaurants();
            console.log('📋 Pending restaurants loaded:', data);
            setRestaurants(data);
        } catch (err: unknown) {
            let errorMsg = 'An unexpected error occurred';

            if (axios.isAxiosError(err)) {
                errorMsg = err.response?.data?.message || 'Failed to load pending restaurants';
            } else if (err instanceof Error) {
                errorMsg = err.message;
            }

            setError(errorMsg);
            console.error('❌ Error fetching pending restaurants:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleApprove = async (restaurant: Restaurant) => {
        if (!window.confirm(`Approve "${restaurant.name}"?\n\nThis will make it visible on the map.`)) {
            return;
        }

        setProcessingId(restaurant.id);
        setError(null);

        try {
            await apiService.approveRestaurant(restaurant.id);
            setSuccessMessage(`✅ "${restaurant.name}" has been approved!`);

            // Remove from pending list
            setRestaurants(prev => prev.filter(r => r.id !== restaurant.id));

            // Clear success message after 5 seconds
            setTimeout(() => setSuccessMessage(null), 5000);
        } catch (err: unknown) {
            let errorMsg = 'Failed to approve restaurant';

            if (axios.isAxiosError(err)) {
                errorMsg = err.response?.data?.message || errorMsg;
            } else if (err instanceof Error) {
                errorMsg = err.message;
            }

            alert(`❌ Error: ${errorMsg}`);
            console.error('Approval error:', err);
        } finally {
            setProcessingId(null);
        }
    };

    const handleReject = async (restaurant: Restaurant) => {
        const reason = window.prompt(
            `Reject "${restaurant.name}"?\n\nEnter a reason (optional):`,
            ''
        );

        if (reason === null) return; // User cancelled

        setProcessingId(restaurant.id);
        setError(null);

        try {
            await apiService.rejectRestaurant(restaurant.id);
            setSuccessMessage(`🚫 "${restaurant.name}" has been rejected.`);

            // Remove from pending list
            setRestaurants(prev => prev.filter(r => r.id !== restaurant.id));

            // Clear success message after 5 seconds
            setTimeout(() => setSuccessMessage(null), 5000);
        } catch (err: unknown) {
            let errorMsg = 'Failed to reject restaurant';

            if (axios.isAxiosError(err)) {
                errorMsg = err.response?.data?.message || errorMsg;
            } else if (err instanceof Error) {
                errorMsg = err.message;
            }

            alert(`❌ Error: ${errorMsg}`);
            console.error('Rejection error:', err);
        } finally {
            setProcessingId(null);
        }
    };

    const handleVerify = async (restaurant: Restaurant) => {
        const notes = window.prompt(
            `Verify "${restaurant.name}" as a legitimate business?\n\nOptional notes:`,
            ''
        );

        if (notes === null) return; // User cancelled

        setProcessingId(restaurant.id);

        try {
            await apiService.verifyRestaurant(restaurant.id, { notes: notes || undefined });
            setSuccessMessage(`✓ "${restaurant.name}" has been verified!`);

            // Update local state
            setRestaurants(prev => prev.map(r =>
                r.id === restaurant.id
                    ? { ...r, isVerified: true, verifiedAt: new Date().toISOString() }
                    : r
            ));

            setTimeout(() => setSuccessMessage(null), 5000);
        } catch (err: unknown) {
            let errorMsg = 'Failed to verify restaurant';

            if (axios.isAxiosError(err)) {
                errorMsg = err.response?.data?.message || errorMsg;
            }

            alert(`❌ Error: ${errorMsg}`);
            console.error('Verification error:', err);
        } finally {
            setProcessingId(null);
        }
    };

    if (loading) {
        return (
            <div className="admin-loading">
                <div className="spinner"></div>
                <p>Loading pending restaurants...</p>
            </div>
        );
    }

    return (
        <div className="pending-restaurants">
            <div className="section-header">
                <h2>🍽️ Pending Restaurant Approvals</h2>
                <button onClick={fetchPendingRestaurants} className="btn-refresh" title="Refresh list">
                    🔄 Refresh
                </button>
            </div>

            {error && (
                <div className="admin-error">
                    ❌ {error}
                </div>
            )}

            {successMessage && (
                <div className="success-message">
                    {successMessage}
                </div>
            )}

            {restaurants.length === 0 ? (
                <div className="admin-empty">
                    <div className="admin-empty-icon">✅</div>
                    <h3>All Caught Up!</h3>
                    <p>No restaurants pending approval at the moment.</p>
                    <small>New restaurants will appear here when users submit them.</small>
                </div>
            ) : (
                <>
                    <div className="pending-count">
                        📊 <strong>{restaurants.length}</strong> restaurant{restaurants.length !== 1 ? 's' : ''} awaiting
                        review
                    </div>

                    <div className="restaurants-grid">
                        {restaurants.map((restaurant) => (
                            <div key={restaurant.id} className="pending-restaurant-card">
                                <div className="card-header">
                                    <div>
                                        <h3>{restaurant.name}</h3>
                                        <span className="restaurant-id">ID: #{restaurant.id}</span>
                                    </div>
                                    <div className="badge-group">
                                        <span className="pending-badge">⏳ Pending Review</span>
                                        {restaurant.isVerified ? (
                                            <span className="verified-badge">✓ Verified</span>
                                        ) : (
                                            <span className="unverified-badge">⚠️ Unverified</span>
                                        )}
                                    </div>
                                </div>

                                <div className="card-body">
                                    <div className="info-section">
                                        <div className="info-row">
                                            <span className="info-label">📍 Address</span>
                                            <span className="info-value">{restaurant.address}</span>
                                        </div>

                                        {restaurant.phone && (
                                            <div className="info-row">
                                                <span className="info-label">📞 Phone</span>
                                                <span className="info-value">{restaurant.phone}</span>
                                            </div>
                                        )}

                                        {restaurant.cuisineType && (
                                            <div className="info-row">
                                                <span className="info-label">🍽️ Cuisine</span>
                                                <span className="info-value">{restaurant.cuisineType}</span>
                                            </div>
                                        )}

                                        {restaurant.priceRange && (
                                            <div className="info-row">
                                                <span className="info-label">💰 Price Range</span>
                                                <span className="info-value price-range">
                                                    {restaurant.priceRange === 'BUDGET' && '$ Budget'}
                                                    {restaurant.priceRange === 'MODERATE' && '$$ Moderate'}
                                                    {restaurant.priceRange === 'EXPENSIVE' && '$$$ Expensive'}
                                                    {restaurant.priceRange === 'LUXURY' && '$$$$ Luxury'}
                                                </span>
                                            </div>
                                        )}

                                        {restaurant.openingHours && (
                                            <div className="info-row">
                                                <span className="info-label">🕒 Hours</span>
                                                <span className="info-value">{restaurant.openingHours}</span>
                                            </div>
                                        )}
                                    </div>

                                    {restaurant.description && (
                                        <div className="description-box">
                                            <span className="info-label">📝 Description</span>
                                            <p>{restaurant.description}</p>
                                        </div>
                                    )}

                                    <div className="meta-info">
                                        <div className="meta-item">
                                            <span className="meta-label">👤 Submitted by:</span>
                                            <span className="meta-value">
                                                {restaurant.owner?.email || 'Unknown User'}
                                            </span>
                                        </div>
                                        <div className="meta-item">
                                            <span className="meta-label">📅 Date:</span>
                                            <span className="meta-value">
                                                {new Date(restaurant.createdAt).toLocaleDateString('en-US', {
                                                    year: 'numeric',
                                                    month: 'short',
                                                    day: 'numeric'
                                                })}
                                            </span>
                                        </div>
                                        {restaurant.verifiedAt && (
                                            <div className="meta-item">
                                                <span className="meta-label">✓ Verified:</span>
                                                <span className="meta-value">
                                                    {new Date(restaurant.verifiedAt).toLocaleDateString('en-US', {
                                                        year: 'numeric',
                                                        month: 'short',
                                                        day: 'numeric'
                                                    })}
                                                </span>
                                            </div>
                                        )}
                                    </div>
                                </div>

                                <div className="card-actions">
                                    <button
                                        onClick={() => handleApprove(restaurant)}
                                        className="btn-approve"
                                        disabled={processingId === restaurant.id}
                                        title="Approve and make visible on map"
                                    >
                                        {processingId === restaurant.id ? (
                                            <>⏳ Processing...</>
                                        ) : (
                                            <>✓ Approve</>
                                        )}
                                    </button>
                                    {!restaurant.isVerified && (
                                        <button
                                            onClick={() => handleVerify(restaurant)}
                                            className="btn-verify"
                                            disabled={processingId === restaurant.id}
                                            title="Mark as verified (not spam)"
                                        >
                                            {processingId === restaurant.id ? (
                                                <>⏳ Processing...</>
                                            ) : (
                                                <>✓ Verify</>
                                            )}
                                        </button>
                                    )}
                                    <button
                                        onClick={() => handleReject(restaurant)}
                                        className="btn-reject"
                                        disabled={processingId === restaurant.id}
                                        title="Reject this restaurant"
                                    >
                                        {processingId === restaurant.id ? (
                                            <>⏳ Processing...</>
                                        ) : (
                                            <>✗ Reject</>
                                        )}
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                </>
            )}
        </div>
    );
};

export default PendingRestaurants;