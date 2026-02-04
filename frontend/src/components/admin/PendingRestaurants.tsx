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
            setRestaurants(data);
        } catch (err) {
            setError('Failed to load pending restaurants');
            console.error('Error fetching pending restaurants:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleApprove = async (restaurant: Restaurant) => {
        if (!confirm(`Approve "${restaurant.name}"? This will make it visible on the map.`)) {
            return;
        }

        setProcessingId(restaurant.id);
        try {
            await apiService.approveRestaurant(restaurant.id);
            setSuccessMessage(`"${restaurant.name}" has been approved!`);
            setRestaurants(restaurants.filter(r => r.id !== restaurant.id));
            setTimeout(() => setSuccessMessage(null), 5000);
        } catch (err: unknown) {
            let errorMessage = 'Failed to approve restaurant';

            if (axios.isAxiosError(err)) {
                errorMessage = err.response?.data?.message || err.message;
            }

            alert(errorMessage);
            console.error('Approval error:', err);
        } finally {
            setProcessingId(null);
        }
    };

    const handleReject = async (restaurant: Restaurant) => {
        const reason = prompt(`Reject "${restaurant.name}"? Enter a reason (optional):`);
        if (reason === null) return; // User cancelled

        setProcessingId(restaurant.id);
        try {
            await apiService.rejectRestaurant(restaurant.id);
            setSuccessMessage(`"${restaurant.name}" has been rejected.`);
            setRestaurants(restaurants.filter(r => r.id !== restaurant.id));
            setTimeout(() => setSuccessMessage(null), 5000);
        } catch (err: unknown) {
            let errorMessage = 'Failed to reject restaurant';

            if (axios.isAxiosError(err)) {
                errorMessage = err.response?.data?.message || err.message;
            }

            alert(errorMessage);
            console.error('Rejection error:', err);
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
                <h2>Pending Restaurant Approvals</h2>
                <button onClick={fetchPendingRestaurants} className="btn-refresh">
                    🔄 Refresh
                </button>
            </div>

            {error && <div className="admin-error">{error}</div>}
            {successMessage && (
                <div className="success-message">
                    ✓ {successMessage}
                </div>
            )}

            {restaurants.length === 0 ? (
                <div className="admin-empty">
                    <div className="admin-empty-icon">✅</div>
                    <h3>All Caught Up!</h3>
                    <p>No restaurants pending approval at the moment.</p>
                </div>
            ) : (
                <div className="restaurants-grid">
                    {restaurants.map((restaurant) => (
                        <div key={restaurant.id} className="pending-restaurant-card">
                            <div className="card-header">
                                <h3>{restaurant.name}</h3>
                                <span className="pending-badge">Pending Review</span>
                            </div>

                            <div className="card-body">
                                <div className="info-row">
                                    <span className="info-label">📍 Address:</span>
                                    <span className="info-value">{restaurant.address}</span>
                                </div>

                                {restaurant.phone && (
                                    <div className="info-row">
                                        <span className="info-label">📞 Phone:</span>
                                        <span className="info-value">{restaurant.phone}</span>
                                    </div>
                                )}

                                {restaurant.openingHours && (
                                    <div className="info-row">
                                        <span className="info-label">🕒 Hours:</span>
                                        <span className="info-value">{restaurant.openingHours}</span>
                                    </div>
                                )}

                                {restaurant.description && (
                                    <div className="description-box">
                                        <span className="info-label">Description:</span>
                                        <p>{restaurant.description}</p>
                                    </div>
                                )}

                                <div className="meta-info">
                  <span className="meta-item">
                    👤 Owner: {restaurant.owner.username}
                  </span>
                                    <span className="meta-item">
                    📅 Submitted: {new Date(restaurant.createdAt).toLocaleDateString()}
                  </span>
                                </div>
                            </div>

                            <div className="card-actions">
                                <button
                                    onClick={() => handleApprove(restaurant)}
                                    className="btn-approve"
                                    disabled={processingId === restaurant.id}
                                >
                                    {processingId === restaurant.id ? 'Processing...' : '✓ Approve'}
                                </button>
                                <button
                                    onClick={() => handleReject(restaurant)}
                                    className="btn-reject"
                                    disabled={processingId === restaurant.id}
                                >
                                    {processingId === restaurant.id ? 'Processing...' : '✗ Reject'}
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default PendingRestaurants;