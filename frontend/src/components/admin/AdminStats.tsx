import React, { useEffect, useState } from 'react';
import { apiService } from '../../services/api';
import type { Restaurant } from '../../types/restaurant.types';
import './AdminStats.css';

interface Stats {
  totalRestaurants: number;
  pendingRestaurants: number;
  approvedRestaurants: number;
  rejectedRestaurants: number;
  totalUsers: number;
  totalReviews: number;
  unverifiedReviews: number;
}

const AdminStats: React.FC = () => {
  const [stats, setStats] = useState<Stats | null>(null);
  const [loading, setLoading] = useState(true);
  const [recentRestaurants, setRecentRestaurants] = useState<Restaurant[]>([]);

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      setLoading(true);
      const [pending, approved] = await Promise.all([
        apiService.getPendingRestaurants(),
        apiService.getApprovedRestaurants(),
      ]);

      // Mock stats - in real app, you'd have dedicated endpoints
      setStats({
        totalRestaurants: pending.length + approved.length,
        pendingRestaurants: pending.length,
        approvedRestaurants: approved.length,
        rejectedRestaurants: 0, // Would need backend endpoint
        totalUsers: 0, // Would need backend endpoint
        totalReviews: 0, // Would need backend endpoint
        unverifiedReviews: 0, // Would need backend endpoint
      });

      setRecentRestaurants(pending.slice(0, 5));
    } catch (err) {
      console.error('Failed to fetch stats:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="admin-loading">
        <div className="spinner"></div>
        <p>Loading statistics...</p>
      </div>
    );
  }

  if (!stats) {
    return (
      <div className="admin-error">
        Failed to load statistics. Please try again.
      </div>
    );
  }

  return (
    <div className="admin-stats-container">
      {/* Stats Grid */}
      <div className="stats-grid">
        <div className="stat-card stat-primary">
          <div className="stat-icon">🏪</div>
          <div className="stat-content">
            <h3>{stats.totalRestaurants}</h3>
            <p>Total Restaurants</p>
          </div>
        </div>

        <div className="stat-card stat-warning">
          <div className="stat-icon">⏳</div>
          <div className="stat-content">
            <h3>{stats.pendingRestaurants}</h3>
            <p>Pending Approval</p>
          </div>
        </div>

        <div className="stat-card stat-success">
          <div className="stat-icon">✅</div>
          <div className="stat-content">
            <h3>{stats.approvedRestaurants}</h3>
            <p>Approved</p>
          </div>
        </div>

        <div className="stat-card stat-danger">
          <div className="stat-icon">❌</div>
          <div className="stat-content">
            <h3>{stats.rejectedRestaurants}</h3>
            <p>Rejected</p>
          </div>
        </div>

        <div className="stat-card stat-info">
          <div className="stat-icon">👥</div>
          <div className="stat-content">
            <h3>{stats.totalUsers}</h3>
            <p>Total Users</p>
          </div>
        </div>

        <div className="stat-card stat-rating">
          <div className="stat-icon">⭐</div>
          <div className="stat-content">
            <h3>{stats.totalReviews}</h3>
            <p>Total Reviews</p>
          </div>
        </div>
      </div>

      {/* Recent Activity */}
      {recentRestaurants.length > 0 && (
        <div className="recent-activity">
          <h2>Recent Submissions</h2>
          <div className="activity-list">
            {recentRestaurants.map((restaurant) => (
              <div key={restaurant.id} className="activity-item">
                <div className="activity-icon">🏪</div>
                <div className="activity-details">
                  <h4>{restaurant.name}</h4>
                  <p>{restaurant.address}</p>
                  <span className="activity-time">
                    {new Date(restaurant.createdAt).toLocaleDateString()}
                  </span>
                </div>
                <span className="activity-status status-pending">Pending</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Quick Actions */}
      <div className="quick-actions">
        <h2>Quick Actions</h2>
        <div className="actions-grid">
          <button className="action-card">
            <span className="action-icon">🏪</span>
            <span className="action-label">Review Restaurants</span>
            <span className="action-count">{stats.pendingRestaurants} pending</span>
          </button>
          <button className="action-card">
            <span className="action-icon">⭐</span>
            <span className="action-label">Moderate Reviews</span>
            <span className="action-count">{stats.unverifiedReviews} unverified</span>
          </button>
          <button className="action-card">
            <span className="action-icon">👥</span>
            <span className="action-label">Manage Users</span>
            <span className="action-count">{stats.totalUsers} total</span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default AdminStats;