import React from 'react';
import type { Restaurant } from '../types/restaurant.types';
import './DashboardStats.css';

interface DashboardStatsProps {
  restaurants: Restaurant[];
}

const DashboardStats: React.FC<DashboardStatsProps> = ({ restaurants }) => {
  const approvedCount = restaurants.filter(r => r.status === 'APPROVED').length;
  const pendingCount = restaurants.filter(r => r.status === 'PENDING').length;
  const rejectedCount = restaurants.filter(r => r.status === 'REJECTED').length;

  const totalRatings = restaurants.reduce((sum, r) => sum + (r.rating || 0), 0);
  const averageRating = restaurants.length > 0 ? totalRatings / restaurants.length : 0;

  return (
    <div className="dashboard-stats">
      <div className="stat-card">
        <div className="stat-icon">🏪</div>
        <div className="stat-content">
          <h3>{restaurants.length}</h3>
          <p>Total Restaurants</p>
        </div>
      </div>

      <div className="stat-card stat-success">
        <div className="stat-icon">✅</div>
        <div className="stat-content">
          <h3>{approvedCount}</h3>
          <p>Approved</p>
        </div>
      </div>

      <div className="stat-card stat-warning">
        <div className="stat-icon">⏳</div>
        <div className="stat-content">
          <h3>{pendingCount}</h3>
          <p>Pending</p>
        </div>
      </div>

      {rejectedCount > 0 && (
        <div className="stat-card stat-danger">
          <div className="stat-icon">❌</div>
          <div className="stat-content">
            <h3>{rejectedCount}</h3>
            <p>Rejected</p>
          </div>
        </div>
      )}

      {averageRating > 0 && (
        <div className="stat-card stat-rating">
          <div className="stat-icon">⭐</div>
          <div className="stat-content">
            <h3>{averageRating.toFixed(1)}</h3>
            <p>Avg. Rating</p>
          </div>
        </div>
      )}
    </div>
  );
};

export default DashboardStats;