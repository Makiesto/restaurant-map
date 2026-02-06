import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useAuth} from '../context/AuthContext';
import {apiService} from '../services/api';
import type {Restaurant} from '../types/restaurant.types';
import type {Dish} from '../types/dish.types';
import RestaurantForm from '../components/forms/RestaurantForm.tsx';
import DishForm from '../components/forms/DishForm.tsx';
import './Dashboard.css';
import axios from "axios";

const Dashboard: React.FC = () => {
    const {user} = useAuth();
    const navigate = useNavigate();

    const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
    const [selectedRestaurant, setSelectedRestaurant] = useState<Restaurant | null>(null);
    const [dishes, setDishes] = useState<Dish[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [showRestaurantForm, setShowRestaurantForm] = useState(false);
    const [showDishForm, setShowDishForm] = useState(false);
    const [editingRestaurant, setEditingRestaurant] = useState<Restaurant | null>(null);
    const [editingDish, setEditingDish] = useState<Dish | null>(null);

    // Check if user is admin
    const isAdmin = user?.role === 'ADMIN';

    useEffect(() => {
        fetchMyRestaurants();
    }, []);

    useEffect(() => {
        if (selectedRestaurant) {
            fetchRestaurantDishes(selectedRestaurant.id);
        }
    }, [selectedRestaurant]);

    const fetchMyRestaurants = async () => {
        try {
            setLoading(true);
            const data = await apiService.getMyRestaurants();

            // Filter out admin's own restaurants from dashboard view
            const filteredRestaurants = isAdmin ? [] : data;

            setRestaurants(filteredRestaurants);
            if (filteredRestaurants.length > 0 && !selectedRestaurant) {
                setSelectedRestaurant(filteredRestaurants[0]);
            }
        } catch (err) {
            setError('Failed to load restaurants');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const fetchRestaurantDishes = async (restaurantId: number) => {
        try {
            const data = await apiService.getRestaurantMenu(restaurantId);
            setDishes(data);
        } catch (err) {
            console.error('Failed to load dishes:', err);
        }
    };

    const handleAddRestaurant = () => {
        setEditingRestaurant(null);
        setShowRestaurantForm(true);
    };

    const handleEditRestaurant = (restaurant: Restaurant) => {
        setEditingRestaurant(restaurant);
        setShowRestaurantForm(true);
    };

    const handleDeleteRestaurant = async (restaurant: Restaurant) => {
        if (!confirm(`Are you sure you want to delete "${restaurant.name}"? This will also delete all dishes.`)) {
            return;
        }

        try {
            await apiService.deleteRestaurant(restaurant.id);
            await fetchMyRestaurants();
            if (selectedRestaurant?.id === restaurant.id) {
                setSelectedRestaurant(restaurants[0] || null);
            }
        } catch (err: unknown) {
            let errorMessage = 'Failed to delete restaurant';
            if (axios.isAxiosError(err)) {
                errorMessage = err.response?.data?.message || err.message;
            }
            alert(errorMessage);
        }
    };

    const handleRestaurantFormClose = async (success: boolean) => {
        setShowRestaurantForm(false);
        setEditingRestaurant(null);
        if (success) {
            if (isAdmin) {
                // For admins, show success message but don't show in dashboard
                alert('✅ Restaurant created successfully! It will be reviewed by the admin team.');
            } else {
                await fetchMyRestaurants();
            }
        }
    };

    const handleAddDish = () => {
        if (!selectedRestaurant) {
            alert('Please select a restaurant first');
            return;
        }
        setEditingDish(null);
        setShowDishForm(true);
    };

    const handleEditDish = (dish: Dish) => {
        setEditingDish(dish);
        setShowDishForm(true);
    };

    const handleDeleteDish = async (dish: Dish) => {
        if (!confirm(`Are you sure you want to delete "${dish.name}"?`)) {
            return;
        }

        try {
            await apiService.deleteDish(dish.id);
            if (selectedRestaurant) {
                await fetchRestaurantDishes(selectedRestaurant.id);
            }
        } catch (err: unknown) {
            let errorMessage = 'Failed to delete dish';
            if (axios.isAxiosError(err)) {
                errorMessage = err.response?.data?.message || err.message;
            }
            alert(errorMessage);
        }
    };

    const handleDishFormClose = async (success: boolean) => {
        setShowDishForm(false);
        setEditingDish(null);
        if (success && selectedRestaurant) {
            await fetchRestaurantDishes(selectedRestaurant.id);
        }
    };

    const getStatusBadge = (status: string) => {
        const statusConfig = {
            PENDING: {className: 'status-pending', label: 'Pending Review'},
            APPROVED: {className: 'status-approved', label: 'Approved'},
            REJECTED: {className: 'status-rejected', label: 'Rejected'},
        };
        const config = statusConfig[status as keyof typeof statusConfig];
        return <span className={`status-badge ${config.className}`}>{config.label}</span>;
    };

    if (loading) {
        return (
            <div className="dashboard-loading">
                <div className="spinner"></div>
                <p>Loading your restaurants...</p>
            </div>
        );
    }

    // Show empty state for admins (restaurants not shown)
    if (isAdmin) {
        return (
            <div className="dashboard">
                <div className="dashboard-header">
                    <div className="header-content">
                        <h1>My Restaurants</h1>
                        <p className="subtitle">📋 Admin restaurant management is handled through the Admin Panel</p>
                    </div>
                    <button onClick={handleAddRestaurant} className="btn-add-restaurant">
                        + Add Restaurant
                    </button>
                </div>

                <div className="empty-state">
                    <div className="empty-icon"></div>
                    <h2>Admin Dashboard</h2>
                    <p>Restaurants created by admins are not displayed here.</p>
                    <p>To manage restaurant approvals and verifications, use the Admin Panel.</p>
                    <button onClick={() => navigate('/admin')} className="btn-primary-large">
                        Go to Admin Panel
                    </button>
                </div>

                {/* Restaurant Form Modal for admins */}
                {showRestaurantForm && (
                    <RestaurantForm
                        restaurant={editingRestaurant}
                        onClose={handleRestaurantFormClose}
                    />
                )}
            </div>
        );
    }

    // Regular view for non-admin users
    return (
        <div className="dashboard">
            <div className="dashboard-header">
                <div className="header-content">
                    <h1>My Restaurants</h1>
                    <p className="subtitle">Manage your restaurant listings and menus</p>
                </div>
                <button onClick={handleAddRestaurant} className="btn-add-restaurant">
                    + Add Restaurant
                </button>
            </div>

            {error && <div className="error-banner">{error}</div>}

            {restaurants.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-icon">🏪</div>
                    <h2>No Restaurants Yet</h2>
                    <p>Add your first restaurant to get started</p>
                    <button onClick={handleAddRestaurant} className="btn-primary-large">
                        + Add Your First Restaurant
                    </button>
                </div>
            ) : (
                <div className="dashboard-content">
                    {/* Restaurant List Sidebar */}
                    <div className="restaurant-sidebar">
                        <div className="sidebar-header">
                            <h3>Your Restaurants ({restaurants.length})</h3>
                        </div>
                        <div className="restaurant-list">
                            {restaurants.map((restaurant) => (
                                <div
                                    key={restaurant.id}
                                    className={`restaurant-item ${selectedRestaurant?.id === restaurant.id ? 'active' : ''}`}
                                    onClick={() => setSelectedRestaurant(restaurant)}
                                >
                                    <div className="restaurant-item-header">
                                        <h4>{restaurant.name}</h4>
                                        {getStatusBadge(restaurant.status)}
                                    </div>
                                    <p className="restaurant-item-address">{restaurant.address}</p>
                                    {restaurant.rating && (
                                        <div className="restaurant-item-rating">
                                            ⭐ {restaurant.rating.toFixed(1)}
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Main Content */}
                    <div className="dashboard-main">
                        {selectedRestaurant && (
                            <>
                                {/* Restaurant Details Card */}
                                <div className="restaurant-details-card">
                                    <div className="card-header">
                                        <div>
                                            <h2>{selectedRestaurant.name}</h2>
                                            {getStatusBadge(selectedRestaurant.status)}
                                        </div>
                                        <div className="card-actions">
                                            <button
                                                onClick={() => navigate(`/restaurant/${selectedRestaurant.id}`)}
                                                className="btn-view"
                                            >
                                                👁️ View Public Page
                                            </button>
                                            <button
                                                onClick={() => handleEditRestaurant(selectedRestaurant)}
                                                className="btn-edit"
                                            >
                                                ✏️ Edit
                                            </button>
                                            <button
                                                onClick={() => handleDeleteRestaurant(selectedRestaurant)}
                                                className="btn-delete"
                                            >
                                                🗑️ Delete
                                            </button>
                                        </div>
                                    </div>

                                    <div className="restaurant-info-grid">
                                        <div className="info-item">
                                            <span className="info-label">Address</span>
                                            <span className="info-value">{selectedRestaurant.address}</span>
                                        </div>
                                        {selectedRestaurant.phone && (
                                            <div className="info-item">
                                                <span className="info-label">Phone</span>
                                                <span className="info-value">{selectedRestaurant.phone}</span>
                                            </div>
                                        )}
                                        {selectedRestaurant.openingHours && (
                                            <div className="info-item">
                                                <span className="info-label">Opening Hours</span>
                                                <span className="info-value">{selectedRestaurant.openingHours}</span>
                                            </div>
                                        )}
                                        {selectedRestaurant.description && (
                                            <div className="info-item full-width">
                                                <span className="info-label">Description</span>
                                                <span className="info-value">{selectedRestaurant.description}</span>
                                            </div>
                                        )}
                                    </div>

                                    {selectedRestaurant.status === 'PENDING' && (
                                        <div className="status-info pending">
                                            ⏳ Your restaurant is awaiting admin approval
                                        </div>
                                    )}
                                    {selectedRestaurant.status === 'REJECTED' && (
                                        <div className="status-info rejected">
                                            ❌ Your restaurant was rejected. Please contact support or update your
                                            listing.
                                        </div>
                                    )}
                                </div>

                                {/* Menu Section */}
                                <div className="menu-section">
                                    <div className="section-header">
                                        <h3>Menu ({dishes.length} items)</h3>
                                        <button onClick={handleAddDish} className="btn-add-dish">
                                            + Add Dish
                                        </button>
                                    </div>

                                    {dishes.length === 0 ? (
                                        <div className="empty-menu">
                                            <p>No dishes yet</p>
                                            <button onClick={handleAddDish} className="btn-secondary">
                                                Add Your First Dish
                                            </button>
                                        </div>
                                    ) : (
                                        <div className="dishes-grid">
                                            {dishes.map((dish) => (
                                                <div key={dish.id} className="dish-card-mini">
                                                    <div className="dish-card-header">
                                                        <div>
                                                            <h4>{dish.name}</h4>
                                                            {!dish.isAvailable && (
                                                                <span className="unavailable-tag">Unavailable</span>
                                                            )}
                                                        </div>
                                                        <span className="dish-price">${dish.price.toFixed(2)}</span>
                                                    </div>

                                                    {dish.description && (
                                                        <p className="dish-description-mini">{dish.description}</p>
                                                    )}

                                                    {dish.baseKcal && (
                                                        <div className="nutrition-mini">
                                                            <span>🔥 {Math.round(dish.baseKcal)} kcal</span>
                                                            {dish.baseProteinG &&
                                                                <span>💪 {dish.baseProteinG.toFixed(1)}g protein</span>}
                                                        </div>
                                                    )}

                                                    <div className="dish-card-actions">
                                                        <button
                                                            onClick={() => handleEditDish(dish)}
                                                            className="btn-edit-small"
                                                        >
                                                            Edit
                                                        </button>
                                                        <button
                                                            onClick={() => handleDeleteDish(dish)}
                                                            className="btn-delete-small"
                                                        >
                                                            Delete
                                                        </button>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            </>
                        )}
                    </div>
                </div>
            )}

            {/* Restaurant Form Modal */}
            {showRestaurantForm && (
                <RestaurantForm
                    restaurant={editingRestaurant}
                    onClose={handleRestaurantFormClose}
                />
            )}

            {/* Dish Form Modal */}
            {showDishForm && selectedRestaurant && (
                <DishForm
                    dish={editingDish}
                    restaurantId={selectedRestaurant.id}
                    onClose={handleDishFormClose}
                />
            )}
        </div>
    );
};

export default Dashboard;