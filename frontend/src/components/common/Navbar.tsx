import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext.tsx';
import './Navbar.css';

const Navbar: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-logo">
          🗺️ Restaurant Map
        </Link>

        <div className="navbar-menu">
          <Link to="/" className="navbar-link">
            Map
          </Link>

          {user ? (
            <>
              <Link to="/dashboard" className="navbar-link">
                My Restaurants
              </Link>

              {user.role === 'ADMIN' && (
                <Link to="/admin" className="navbar-link admin-link">
                  Admin Panel
                </Link>
              )}

              <div className="navbar-user">
                <span className="user-name">{user.email}</span>
                <button onClick={handleLogout} className="btn-logout">
                  Logout
                </button>
              </div>
            </>
          ) : (
            <>
              <Link to="/login" className="navbar-link">
                Login
              </Link>
              <Link to="/register" className="btn-register">
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;