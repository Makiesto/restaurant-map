import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

const Navbar: React.FC = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-logo">
          🍽️ RestaurantMap
        </Link>

        <div className="navbar-menu">
          <Link to="/map" className="navbar-link">
            Map
          </Link>

          {isAuthenticated ? (
            <>
              {/* Show for verified users and admins */}
              {user && (user.role === 'VERIFIED_USER' || user.role === 'ADMIN') && (
                <Link to="/dashboard" className="navbar-link">
                  My Restaurants
                </Link>
              )}

              {/* Show for admins only */}
              {user && user.role === 'ADMIN' && (
                <Link to="/admin" className="navbar-link">
                  Admin Panel
                </Link>
              )}

              <div className="navbar-user">
                <div className="user-info">
                  <span className="user-name">
                    {user?.firstName} {user?.lastName}
                  </span>
                  <span className="user-role">{user?.role}</span>
                </div>
                <button onClick={handleLogout} className="btn-logout">
                  Logout
                </button>
              </div>
            </>
          ) : (
            <div className="navbar-auth">
              <Link to="/login" className="navbar-link">
                Login
              </Link>
              <Link to="/register" className="btn-register">
                Sign Up
              </Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;