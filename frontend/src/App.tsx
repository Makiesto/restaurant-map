import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Map from './components/Map';
import RestaurantDetails from './pages/RestaurantDetails';
import './App.css';

function App() {
  return (
    <Router>
      <AuthProvider>
        <div className="app">
          <Navbar />
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<Home />} />
            <Route path="/map" element={<Map />} />
            <Route path="/restaurant/:id" element={<RestaurantDetails />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            {/* Protected Routes - Require Authentication */}
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <div style={{ padding: '40px', textAlign: 'center' }}>
                    <h1>Dashboard</h1>
                    <p>Your restaurants will appear here (Coming Soon)</p>
                  </div>
                </ProtectedRoute>
              }
            />

            {/* Admin Only Routes */}
            <Route
              path="/admin"
              element={
                <ProtectedRoute requiredRole="ADMIN">
                  <div style={{ padding: '40px', textAlign: 'center' }}>
                    <h1>Admin Panel</h1>
                    <p>Pending approvals will appear here (Coming Soon)</p>
                  </div>
                </ProtectedRoute>
              }
            />

            {/* 404 Not Found */}
            <Route
              path="*"
              element={
                <div style={{
                  padding: '40px',
                  textAlign: 'center',
                  minHeight: 'calc(100vh - 64px)',
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'center',
                  alignItems: 'center'
                }}>
                  <h1 style={{ fontSize: '72px', margin: '0' }}>404</h1>
                  <p style={{ fontSize: '24px', color: '#718096' }}>Page Not Found</p>
                  <a href="/" style={{ color: '#667eea', marginTop: '20px' }}>
                    Go back home
                  </a>
                </div>
              }
            />
          </Routes>
        </div>
      </AuthProvider>
    </Router>
  );
}

export default App;