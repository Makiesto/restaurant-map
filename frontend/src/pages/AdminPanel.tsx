import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Navigate } from 'react-router-dom';
import PendingRestaurants from '../components/admin/PendingRestaurants';
import UserManagement from '../components/admin/UserManagement';
import ReviewModeration from '../components/admin/ReviewModeration';
import AdminStats from '../components/admin/AdminStats.tsx';
import './AdminPanel.css';

type TabType = 'overview' | 'restaurants' | 'users' | 'reviews';

const AdminPanel: React.FC = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<TabType>('overview');

  // Check if user is admin
  if (!user || user.role !== 'ADMIN') {
    return <Navigate to="/" replace />;
  }

  const renderTabContent = () => {
    switch (activeTab) {
      case 'overview':
        return <AdminStats />;
      case 'restaurants':
        return <PendingRestaurants />;
      case 'users':
        return <UserManagement />;
      case 'reviews':
        return <ReviewModeration />;
      default:
        return <AdminStats />;
    }
  };

  return (
    <div className="admin-panel">
      {/* Header */}
      <div className="admin-header">
        <div className="header-content">
          <h1>Admin Panel</h1>
          <p className="header-subtitle">Manage restaurants, users, and reviews</p>
        </div>
        <div className="admin-badge">
          <span className="badge-icon">👑</span>
          <span className="badge-text">Administrator</span>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="admin-tabs">
        <button
          className={`tab-button ${activeTab === 'overview' ? 'active' : ''}`}
          onClick={() => setActiveTab('overview')}
        >
          <span className="tab-icon">📊</span>
          <span className="tab-label">Overview</span>
        </button>
        <button
          className={`tab-button ${activeTab === 'restaurants' ? 'active' : ''}`}
          onClick={() => setActiveTab('restaurants')}
        >
          <span className="tab-icon">🏪</span>
          <span className="tab-label">Restaurants</span>
        </button>
        <button
          className={`tab-button ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => setActiveTab('users')}
        >
          <span className="tab-icon">👥</span>
          <span className="tab-label">Users</span>
        </button>
        <button
          className={`tab-button ${activeTab === 'reviews' ? 'active' : ''}`}
          onClick={() => setActiveTab('reviews')}
        >
          <span className="tab-icon">⭐</span>
          <span className="tab-label">Reviews</span>
        </button>
      </div>

      {/* Content Area */}
      <div className="admin-content">
        {renderTabContent()}
      </div>
    </div>
  );
};

export default AdminPanel;