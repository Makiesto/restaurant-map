import React, { useEffect, useState } from 'react';
import { apiService } from '../../services/api';
import './AdminStats.css';

interface AdminStatsData {
  totalUsers: number;
  totalReviews: number;
  totalRestaurants: number;
  pendingRestaurants: number;
  verifiedReviews: number;
  unverifiedReviews: number;
  verifiedUsers: number;
  unverifiedUsers: number;
}

const AdminStats: React.FC = () => {
  const [stats, setStats] = useState<AdminStatsData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      setLoading(true);
      setError(null);

      // Use the dedicated admin stats endpoint (previously unused).
      // Cast explicitly — the API AdminStats type is missing verifiedReviews,
      // verifiedUsers, unverifiedUsers, so we fill those with safe defaults.
      const raw = await apiService.getAdminStats() as unknown as Record<string, number>;
      setStats({
        totalUsers: raw.totalUsers ?? 0,
        totalReviews: raw.totalReviews ?? 0,
        totalRestaurants: raw.totalRestaurants ?? 0,
        pendingRestaurants: raw.pendingRestaurants ?? 0,
        verifiedReviews: raw.verifiedReviews ?? 0,
        unverifiedReviews: raw.unverifiedReviews ?? 0,
        verifiedUsers: raw.verifiedUsers ?? 0,
        unverifiedUsers: raw.unverifiedUsers ?? 0,
      });
    } catch (err) {
      console.error('Failed to fetch admin stats:', err);

      // Fallback: aggregate from separate endpoints
      try {
        const [restaurantsData, pendingData] = await Promise.all([
          apiService.getApprovedRestaurants(),
          apiService.getPendingRestaurants(),
        ]);

        setStats({
          totalUsers: 0,
          totalReviews: 0,
          totalRestaurants: restaurantsData.length,
          pendingRestaurants: pendingData.length,
          verifiedReviews: 0,
          unverifiedReviews: 0,
          verifiedUsers: 0,
          unverifiedUsers: 0,
        });
      } catch (fallbackErr) {
        console.error('Fallback stats fetch also failed:', fallbackErr);
        setError('Failed to load statistics. Please try again.');
      }
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

  if (error || !stats) {
    return (
      <div className="admin-error-state">
        <div style={{ fontSize: 48, marginBottom: 16 }}>📊</div>
        <h3>Could not load statistics</h3>
        <p>{error}</p>
        <button onClick={fetchStats} className="btn-retry">
          🔄 Try Again
        </button>
      </div>
    );
  }

  return (
    <div className="admin-stats">
      <div className="section-header">
        <h2>📊 Platform Statistics</h2>
        <button onClick={fetchStats} className="btn-refresh">
          🔄 Refresh
        </button>
      </div>


      {/* Detailed breakdown cards */}
      <div className="stats-detail-grid">
        <div className="stat-detail-card">
          <div className="stat-detail-icon">👥</div>
          <div className="stat-detail-content">
            <h4>Users</h4>
            <div className="stat-detail-row">
              <span>Total</span>
              <strong>{stats.totalUsers.toLocaleString()}</strong>
            </div>
            <div className="stat-detail-row">
              <span>✅ Verified</span>
              <strong className="text-green">{stats.verifiedUsers.toLocaleString()}</strong>
            </div>
            <div className="stat-detail-row">
              <span>⏳ Unverified</span>
              <strong className="text-orange">{stats.unverifiedUsers.toLocaleString()}</strong>
            </div>
          </div>
        </div>

        <div className="stat-detail-card">
          <div className="stat-detail-icon">⭐</div>
          <div className="stat-detail-content">
            <h4>Reviews</h4>
            <div className="stat-detail-row">
              <span>Total</span>
              <strong>{stats.totalReviews.toLocaleString()}</strong>
            </div>
            <div className="stat-detail-row">
              <span>✅ Verified</span>
              <strong className="text-green">{stats.verifiedReviews.toLocaleString()}</strong>
            </div>
            <div className="stat-detail-row">
              <span>⏳ Unverified</span>
              <strong className="text-orange">{stats.unverifiedReviews.toLocaleString()}</strong>
            </div>
          </div>
        </div>

        <div className="stat-detail-card">
          <div className="stat-detail-icon">🏪</div>
          <div className="stat-detail-content">
            <h4>Restaurants</h4>
            <div className="stat-detail-row">
              <span>Total</span>
              <strong>{stats.totalRestaurants.toLocaleString()}</strong>
            </div>
            <div className="stat-detail-row">
              <span>✅ Approved</span>
              <strong className="text-green">
                {(stats.totalRestaurants - stats.pendingRestaurants).toLocaleString()}
              </strong>
            </div>
            <div className="stat-detail-row">
              <span>⏳ Pending</span>
              <strong className="text-orange">{stats.pendingRestaurants.toLocaleString()}</strong>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminStats;