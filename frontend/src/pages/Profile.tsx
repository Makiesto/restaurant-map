import React, {useState, useEffect} from 'react';
import {useAuth} from '../context/AuthContext';
import {apiService} from '../services/api';
import AllergenManager from '../components/allergens/AllergenManager';
import type {Restaurant} from '../types/restaurant.types';
import type {Review} from '../types/review.types';
import './Profile.css';
import EditProfileModal from './EditProfileModal';
import ChangePasswordModal from './ChangePasswordModal';
import type {User} from "../types/auth.types.ts";

type TabType = 'account' | 'allergens' | 'activity';

const Profile: React.FC = () => {
    const {user} = useAuth();
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

    const handleSaveProfile = async (updated: Partial<User>) => {
        await apiService.updateProfile({
            firstName: updated.firstName!,
            lastName: updated.lastName!,
            phoneNumber: updated.phoneNumber,
        });

        await loadUserData();

    };

    const handleChangePassword = async (currentPassword: string, newPassword: string) => {
        await apiService.changePassword({currentPassword, newPassword});
        await apiService.logout();
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
                        <button className="btn btn-primary" onClick={() => setShowEditProfile(true)}>
                            Edit Profile
                        </button>
                    </div>
                </div>

                <div className="info-grid">
                    <div className="info-item">
                        <span className="info-label">First Name</span>
                        <span className="info-value">{user?.firstName}</span>
                    </div>

                    <div className="info-item">
                        <span className="info-label">Last Name</span>
                        <span className="info-value">{user?.lastName}</span>
                    </div>

                    <div className="info-item">
                        <span className="info-label">Email Address</span>
                        <span className="info-value">{user?.email}</span>
                    </div>

                    <div className="info-item">
                        <span className="info-label">Phone Number</span>
                        <span className="info-value empty">
              {user?.phoneNumber || 'Not provided'}
            </span>
                    </div>

                    <div className="info-item">
                        <span className="info-label">Account Type</span>
                        <span className="info-value">{getRoleDisplay()}</span>
                    </div>

                    <div className="info-item">
                        <span className="info-label">Member Since</span>
                        <span className="info-value">
              {user?.createdAt ? formatDate(user.createdAt) : 'Unknown'}
            </span>
                    </div>

                    {user?.verifiedAt && (
                        <div className="info-item">
                            <span className="info-label">Verified On</span>
                            <span className="info-value">{formatDate(user.verifiedAt)}</span>
                        </div>
                    )}

                    <div className="info-item">
                        <span className="info-label">Account Status</span>
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

                    <div className="action-card" onClick={() => window.location.href = '/dashboard'}>
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
                        Manage Allergens
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

                    <div style={{
                        marginTop: '24px',
                        padding: '16px',
                        background: 'var(--color-bg)',
                        borderRadius: '4px'
                    }}>
                        <p style={{margin: 0, fontSize: '13px', color: 'var(--color-text-muted)', lineHeight: '1.6'}}>
                            💡 Dishes containing these allergens will be highlighted with warnings when you browse
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
                    <p style={{color: 'var(--color-text-muted)', fontSize: '14px', margin: 0}}>
                        You haven't written any reviews yet.
                    </p>
                ) : (
                    <div style={{display: 'flex', flexDirection: 'column', gap: '16px'}}>
                        {myReviews.slice(0, 5).map((review) => (
                            <div
                                key={review.id}
                                style={{
                                    padding: '16px',
                                    background: 'var(--color-bg)',
                                    borderRadius: '4px',
                                    border: '1px solid var(--color-border)',
                                }}
                            >
                                <div style={{display: 'flex', justifyContent: 'space-between', marginBottom: '8px'}}>
                                    <strong style={{fontSize: '14px', color: 'var(--color-text)'}}>
                                        {review.restaurantName}
                                    </strong>
                                    <span style={{fontSize: '14px'}}>
                    {'⭐'.repeat(review.rating)}
                  </span>
                                </div>
                                {review.comment && (
                                    <p style={{
                                        margin: '0 0 8px 0',
                                        fontSize: '13px',
                                        color: 'var(--color-text-muted)',
                                        lineHeight: '1.5'
                                    }}>
                                        {review.comment}
                                    </p>
                                )}
                                <span style={{fontSize: '11px', color: 'var(--color-text-subtle)'}}>
                  {formatDate(review.createdAt)}
                </span>
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
                                style={{
                                    padding: '16px',
                                    background: 'var(--color-bg)',
                                    borderRadius: '4px',
                                    border: '1px solid var(--color-border)',
                                    cursor: 'pointer',
                                    transition: 'var(--transition)',
                                }}
                                onClick={() => window.location.href = `/restaurant/${restaurant.id}`}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.borderColor = 'var(--color-accent)';
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.borderColor = 'var(--color-border)';
                                }}
                            >
                                <div style={{display: 'flex', justifyContent: 'space-between', marginBottom: '8px'}}>
                                    <strong style={{fontSize: '14px', color: 'var(--color-text)'}}>
                                        {restaurant.name}
                                    </strong>
                                    <span style={{
                                        fontSize: '11px',
                                        padding: '4px 8px',
                                        borderRadius: '4px',
                                        background: restaurant.status === 'APPROVED' ? 'rgba(72, 187, 120, 0.1)' :
                                            restaurant.status === 'PENDING' ? 'rgba(246, 173, 85, 0.1)' :
                                                'rgba(252, 129, 129, 0.1)',
                                        color: restaurant.status === 'APPROVED' ? '#38a169' :
                                            restaurant.status === 'PENDING' ? '#dd6b20' :
                                                '#c53030',
                                    }}>
                    {restaurant.status}
                  </span>
                                </div>
                                <p style={{margin: 0, fontSize: '12px', color: 'var(--color-text-muted)'}}>
                                    📍 {restaurant.address}
                                </p>
                                {restaurant.rating && (
                                    <p style={{
                                        margin: '8px 0 0 0',
                                        fontSize: '12px',
                                        color: 'var(--color-text-muted)'
                                    }}>
                                        ⭐ {restaurant.rating.toFixed(1)}
                                    </p>
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
                    <div className="profile-avatar">{getInitials()}</div>
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

            {showEditProfile && user && (
                <EditProfileModal
                    user={user}
                    onClose={() => setShowEditProfile(false)}
                    onSave={handleSaveProfile}
                    onChangePasswordClick={() => setShowChangePassword(true)}
                />
            )}

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