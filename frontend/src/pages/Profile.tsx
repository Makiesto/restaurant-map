import React, {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {useAuth} from '../context/AuthContext';
import {apiService} from '../services/api';
import AllergenManager from '../components/allergens/AllergenManager';
import type {Restaurant} from '../types/restaurant.types';
import type {Review} from '../types/review.types';
import './Profile.css';
import EditProfileModal from '../components/modals/EditProfileModal';
import ChangePasswordModal from '../components/modals/ChangePasswordModal';

type TabType = 'account' | 'allergens' | 'activity';

const Profile: React.FC = () => {
    const {user, logout, refreshUser} = useAuth();
    const navigate = useNavigate();

    // Debug: Log user changes
    useEffect(() => {
        console.log('Profile page - user updated:', user);
    }, [user]);
    const [activeTab, setActiveTab] = useState<TabType>('account');
    const [showAllergenManager, setShowAllergenManager] = useState(false);
    const [loading, setLoading] = useState(true);
    const [userAllergens, setUserAllergens] = useState<string[]>([]);
    const [myRestaurants, setMyRestaurants] = useState<Restaurant[]>([]);
    const [myReviews, setMyReviews] = useState<Review[]>([]);
    const [showEditProfile, setShowEditProfile] = useState(false);
    const [showChangePassword, setShowChangePassword] = useState(false);

    useEffect(() => {
        loadUserData();
    }, []);

    const loadUserData = async () => {
        try {
            setLoading(true);
            const [allergens, restaurants, reviews] = await Promise.all([
                apiService.getUserAllergens().catch(() => []),
                apiService.getMyRestaurants().catch(() => []),
                apiService.getMyReviews().catch(() => []),
            ]);

            setUserAllergens(allergens.map(a => a.name));
            setMyRestaurants(restaurants);
            setMyReviews(reviews);
        } catch (error) {
            console.error('Failed to load user data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleAllergenManagerClose = () => {
        setShowAllergenManager(false);
        // Reload allergens after closing
        loadUserData();
    };

    const handleSaveProfile = async (updated: { email: string; phoneNumber?: string }) => {
        console.log('=== SAVE PROFILE START ===');
        console.log('handleSaveProfile received:', updated);
        console.log('Current user before save:', user);

        if (!updated.email) {
            throw new Error('Email is required');
        }

        const emailChanged = updated.email !== user?.email;

        const result = await apiService.updateProfile({
            email: updated.email,
            phoneNumber: updated.phoneNumber,
        });

        console.log('API returned updated user:', result);

        if (emailChanged) {
            // Email changed - must log out and log back in with new email
            alert('Email updated successfully! You will be logged out. Please log in again with your new email address.');
            logout();
            navigate('/login');
        } else {
            // Only phone number changed - refresh user data without page reload
            console.log('Phone number updated, refreshing user data...');
            try {
                // Small delay to ensure DB transaction completes
                await new Promise(resolve => setTimeout(resolve, 500));
                await refreshUser();
                console.log('User refreshed successfully');
                setShowEditProfile(false);
            } catch (error) {
                console.error('Failed to refresh user:', error);
                // Don't close modal on error, user can try again
                alert('Profile updated but failed to refresh. Please reload the page manually.');
            }
        }
    };

    const handleChangePassword = async (currentPassword: string, newPassword: string) => {
        await apiService.changePassword({currentPassword, newPassword});
        // Log user out after password change for security
        logout();
        navigate('/login');
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
        });
    };

    const getInitials = () => {
        if (!user) return '?';
        return `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`.toUpperCase();
    };

    const getRoleDisplay = () => {
        if (!user) return 'User';
        switch (user.role) {
            case 'ADMIN':
                return 'Administrator';
            case 'VERIFIED_USER':
                return 'Verified User';
            default:
                return 'User';
        }
    };

    const renderAccountTab = () => (
        <>
            {/* Account Information */}
            <div className="profile-section">
                <div className="section-header">
                    <h2>Account Information</h2>
                    <div className="section-header-actions">
                        <button className="btn-edit" onClick={() => setShowEditProfile(true)}>
                            ✏️ Edit Profile
                        </button>
                    </div>
                </div>

                <div className="info-grid">
                    <div className="info-item">
                        <span className="info-label">
                            <span className="info-icon">👤</span>
                            First Name
                        </span>
                        <span className="info-value">{user?.firstName}</span>
                    </div>

                    <div className="info-item">
                        <span className="info-label">
                            <span className="info-icon">👤</span>
                            Last Name
                        </span>
                        <span className="info-value">{user?.lastName}</span>
                    </div>

                    <div className="info-item">
                        <span className="info-label">
                            <span className="info-icon">✉️</span>
                            Email Address
                        </span>
                        <span className="info-value">{user?.email}</span>
                    </div>

                    <div className="info-item">
                        <span className="info-label">
                            <span className="info-icon">📱</span>
                            Phone Number
                        </span>
                        <span className={`info-value ${!user?.phoneNumber ? 'empty' : ''}`}>
              {user?.phoneNumber || 'Not provided'}
            </span>
                    </div>

                    <div className="info-item">
                        <span className="info-label">
                            <span className="info-icon">🏷️</span>
                            Account Type
                        </span>
                        <span className="info-value">{getRoleDisplay()}</span>
                    </div>

                    <div className="info-item">
                        <span className="info-label">
                            <span className="info-icon">📅</span>
                            Member Since
                        </span>
                        <span className="info-value">
              {user?.createdAt ? formatDate(user.createdAt) : 'Unknown'}
            </span>
                    </div>

                    {user?.verifiedAt && (
                        <div className="info-item">
                            <span className="info-label">
                                <span className="info-icon">✅</span>
                                Verified On
                            </span>
                            <span className="info-value">{formatDate(user.verifiedAt)}</span>
                        </div>
                    )}

                    <div className="info-item">
                        <span className="info-label">
                            <span className="info-icon">🔒</span>
                            Account Status
                        </span>
                        <span className="info-value">
              {user?.isActive ? '✅ Active' : '❌ Inactive'}
            </span>
                    </div>
                </div>
            </div>

            {/* Quick Actions */}
            <div className="profile-section">
                <div className="section-header">
                    <h2>Quick Actions</h2>
                </div>

                <div className="action-cards">
                    <div className="action-card" onClick={() => setActiveTab('allergens')}>
                        <div className="action-icon-large">🛡️</div>
                        <div className="action-content">
                            <h3>Manage Allergens</h3>
                            <p>
                                {userAllergens.length > 0
                                    ? `${userAllergens.length} allergen${userAllergens.length !== 1 ? 's' : ''} set`
                                    : 'No allergens set'}
                            </p>
                        </div>
                    </div>

                    <div className="action-card" onClick={() => navigate('/dashboard')}>
                        <div className="action-icon-large">🏪</div>
                        <div className="action-content">
                            <h3>My Restaurants</h3>
                            <p>
                                {myRestaurants.length > 0
                                    ? `${myRestaurants.length} restaurant${myRestaurants.length !== 1 ? 's' : ''}`
                                    : 'No restaurants yet'}
                            </p>
                        </div>
                    </div>

                    <div className="action-card" onClick={() => setActiveTab('activity')}>
                        <div className="action-icon-large">⭐</div>
                        <div className="action-content">
                            <h3>My Reviews</h3>
                            <p>
                                {myReviews.length > 0
                                    ? `${myReviews.length} review${myReviews.length !== 1 ? 's' : ''} written`
                                    : 'No reviews yet'}
                            </p>
                        </div>
                    </div>

                    <div className="action-card" onClick={() => setShowChangePassword(true)}>
                        <div className="action-icon-large">🔐</div>
                        <div className="action-content">
                            <h3>Change Password</h3>
                            <p>Update your account security</p>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );

    const renderAllergensTab = () => (
        <div className="profile-section">
            <div className="section-header">
                <h2>Allergen Preferences</h2>
                <div className="section-header-actions">
                    <button className="btn-manage" onClick={() => setShowAllergenManager(true)}>
                        🛡️ Manage Allergens
                    </button>
                </div>
            </div>

            {userAllergens.length === 0 ? (
                <div className="no-allergens">
                    <p>You haven't set any allergen preferences yet.</p>
                    <p>Click "Manage Allergens" to get started.</p>
                </div>
            ) : (
                <>
                    <div className="allergen-preview">
                        {userAllergens.map((allergen, index) => (
                            <span key={index} className="allergen-badge-mini has-allergens">
                <span className="allergen-icon-mini">⚠️</span>
                                {allergen}
              </span>
                        ))}
                    </div>

                    <div className="allergen-info">
                        <span className="allergen-info-icon">💡</span>
                        <p>
                            Dishes containing these allergens will be highlighted with warnings when you browse
                            restaurant menus.
                        </p>
                    </div>
                </>
            )}
        </div>
    );

    const renderActivityTab = () => (
        <>
            {/* Activity Stats */}
            <div className="profile-section">
                <div className="section-header">
                    <h2>Activity Overview</h2>
                </div>

                <div className="stats-grid">
                    <div className="stat-card-mini">
                        <div className="stat-value">{myRestaurants.length}</div>
                        <div className="stat-label">Restaurants</div>
                    </div>

                    <div className="stat-card-mini">
                        <div className="stat-value">{myReviews.length}</div>
                        <div className="stat-label">Reviews</div>
                    </div>

                    <div className="stat-card-mini">
                        <div className="stat-value">
                            {myReviews.length > 0
                                ? (myReviews.reduce((sum, r) => sum + r.rating, 0) / myReviews.length).toFixed(1)
                                : '0.0'}
                        </div>
                        <div className="stat-label">Avg Rating Given</div>
                    </div>

                    <div className="stat-card-mini">
                        <div className="stat-value">{userAllergens.length}</div>
                        <div className="stat-label">Allergens</div>
                    </div>
                </div>
            </div>

            {/* Recent Reviews */}
            <div className="profile-section">
                <div className="section-header">
                    <h2>Recent Reviews</h2>
                </div>

                {myReviews.length === 0 ? (
                    <div className="empty-state">
                        <div className="empty-state-icon">⭐</div>
                        <p>You haven't written any reviews yet.</p>
                    </div>
                ) : (
                    <div style={{display: 'flex', flexDirection: 'column', gap: '16px'}}>
                        {myReviews.slice(0, 5).map((review) => (
                            <div key={review.id} className="review-item">
                                <div className="review-header">
                                    <h4>{review.restaurantName}</h4>
                                    <div className="review-rating">
                                        {'⭐'.repeat(review.rating)}
                                    </div>
                                </div>
                                {review.comment && (
                                    <p className="review-comment">{review.comment}</p>
                                )}
                                <span className="review-date">{formatDate(review.createdAt)}</span>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Restaurant List */}
            {myRestaurants.length > 0 && (
                <div className="profile-section">
                    <div className="section-header">
                        <h2>My Restaurants</h2>
                    </div>

                    <div style={{display: 'flex', flexDirection: 'column', gap: '16px'}}>
                        {myRestaurants.map((restaurant) => (
                            <div
                                key={restaurant.id}
                                className="restaurant-item"
                                onClick={() => navigate(`/restaurant/${restaurant.id}`)}
                            >
                                <div style={{display: 'flex', justifyContent: 'space-between', marginBottom: '8px'}}>
                                    <h4>{restaurant.name}</h4>
                                    <span className={`restaurant-status-badge status-${restaurant.status.toLowerCase()}`}>
                                        {restaurant.status}
                                    </span>
                                </div>
                                <p className="restaurant-status">📍 {restaurant.address}</p>
                                {restaurant.rating && (
                                    <p className="restaurant-status">⭐ {restaurant.rating.toFixed(1)}</p>
                                )}
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </>
    );

    if (loading) {
        return (
            <div className="profile-loading">
                <div className="spinner"></div>
                <p>Loading profile...</p>
            </div>
        );
    }

    if (!user) {
        return (
            <div className="profile-loading">
                <p>Please log in to view your profile.</p>
            </div>
        );
    }

    return (
        <div className="profile-page">
            {/* Profile Header */}
            <div className="profile-header">
                <div className="profile-header-content">
                    <div className="profile-avatar-container">
                        <div className="profile-avatar">{getInitials()}</div>
                    </div>
                    <div className="profile-info">
                        <h1>
                            {user.firstName} {user.lastName}
                        </h1>
                        <p className="profile-email">{user.email}</p>

                        <div className="profile-badges">
                            <div className="profile-badge">
                                <span className="badge-icon">👤</span>
                                <span>{getRoleDisplay()}</span>
                            </div>

                            {user.verifiedAt && (
                                <div className="profile-badge badge-verified">
                                    <span className="badge-icon">✅</span>
                                    <span>Verified</span>
                                </div>
                            )}

                            {user.role === 'ADMIN' && (
                                <div className="profile-badge badge-admin">
                                    <span className="badge-icon">👑</span>
                                    <span>Admin</span>
                                </div>
                            )}

                            {userAllergens.length > 0 && (
                                <div className="profile-badge">
                                    <span className="badge-icon">🛡️</span>
                                    <span>{userAllergens.length} Allergen{userAllergens.length !== 1 ? 's' : ''}</span>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Profile Content */}
            <div className="profile-content">
                {/* Sidebar Navigation */}
                <div className="profile-sidebar">
                    <nav className="profile-nav">
                        <div
                            className={`profile-nav-item ${activeTab === 'account' ? 'active' : ''}`}
                            onClick={() => setActiveTab('account')}
                        >
                            <span className="nav-icon">👤</span>
                            <span>Account</span>
                        </div>

                        <div
                            className={`profile-nav-item ${activeTab === 'allergens' ? 'active' : ''}`}
                            onClick={() => setActiveTab('allergens')}
                        >
                            <span className="nav-icon">🛡️</span>
                            <span>Allergens</span>
                        </div>

                        <div
                            className={`profile-nav-item ${activeTab === 'activity' ? 'active' : ''}`}
                            onClick={() => setActiveTab('activity')}
                        >
                            <span className="nav-icon">📊</span>
                            <span>Activity</span>
                        </div>
                    </nav>
                </div>

                {/* Main Content */}
                <div className="profile-main">
                    {activeTab === 'account' && renderAccountTab()}
                    {activeTab === 'allergens' && renderAllergensTab()}
                    {activeTab === 'activity' && renderActivityTab()}
                </div>
            </div>

            {/* Allergen Manager Modal */}
            {showAllergenManager && (
                <div className="modal-overlay" onClick={handleAllergenManagerClose}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <AllergenManager onClose={handleAllergenManagerClose}/>
                    </div>
                </div>
            )}

            {/* Edit Profile Modal */}
            {showEditProfile && user && (
                <EditProfileModal
                    user={user}
                    onClose={() => setShowEditProfile(false)}
                    onSave={handleSaveProfile}
                    onChangePasswordClick={() => {
                        setShowEditProfile(false);
                        setShowChangePassword(true);
                    }}
                />
            )}

            {/* Change Password Modal */}
            {showChangePassword && (
                <ChangePasswordModal
                    onClose={() => setShowChangePassword(false)}
                    onSave={handleChangePassword}
                />
            )}
        </div>
    );
};

export default Profile;