import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import './Home.css';

const Home: React.FC = () => {
  const { isAuthenticated, user } = useAuth();

  return (
    <div className="home-container">
      <div className="home-content">
        <h1>Welcome to RestaurantMap</h1>

        {isAuthenticated ? (
          <div className="welcome-message">
            <h2>
              Hello, {user?.firstName}! 👋
            </h2>
            <p className="user-status">
              Role: <span className="badge">{user?.role}</span>
            </p>

            <div className="feature-grid">
              <Link to="/map" className="feature-card-link">
                <div className="feature-card">
                  <div className="feature-icon">🗺️</div>
                  <h3>Explore Map</h3>
                  <p>Discover restaurants near you with detailed nutrition info</p>
                </div>
              </Link>

              {user?.role === 'VERIFIED_USER' || user?.role === 'ADMIN' ? (
                <Link to="/dashboard" className="feature-card-link">
                  <div className="feature-card">
                    <div className="feature-icon">🏪</div>
                    <h3>My Restaurants</h3>
                    <p>Manage your restaurant listings and menus</p>
                  </div>
                </Link>
              ) : (
                <div className="feature-card disabled">
                  <div className="feature-icon">🔒</div>
                  <h3>Become Verified</h3>
                  <p>Get verified to add your own restaurants</p>
                </div>
              )}

              {user?.role === 'ADMIN' && (
                <Link to="/admin" className="feature-card-link">
                  <div className="feature-card">
                    <div className="feature-icon">👑</div>
                    <h3>Admin Panel</h3>
                    <p>Approve restaurants and verify users</p>
                  </div>
                </Link>
              )}

              <div className="feature-card disabled">
                <div className="feature-icon">⚠️</div>
                <h3>Allergen Alerts</h3>
                <p>Set your allergens and get instant warnings (Coming Soon)</p>
              </div>
            </div>
          </div>
        ) : (
          <div className="guest-message">
            <p className="subtitle">
              Find restaurants with detailed nutritional information and allergen tracking
            </p>

            <div className="feature-list">
              <div className="feature-item">
                <span className="check-icon">✓</span>
                <span>Interactive restaurant map</span>
              </div>
              <div className="feature-item">
                <span className="check-icon">✓</span>
                <span>Detailed nutrition information for every dish</span>
              </div>
              <div className="feature-item">
                <span className="check-icon">✓</span>
                <span>Track allergens and dietary restrictions</span>
              </div>
              <div className="feature-item">
                <span className="check-icon">✓</span>
                <span>Add and manage your own restaurant</span>
              </div>
            </div>

            <div className="cta-buttons">
              <Link to="/map" className="btn-secondary-large">
                Explore Map
              </Link>
              <Link to="/register" className="btn-primary-large">
                Get Started Free
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Home;