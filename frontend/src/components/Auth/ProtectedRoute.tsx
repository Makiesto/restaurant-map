import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: 'USER' | 'VERIFIED_USER' | 'ADMIN';
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requiredRole
}) => {
  const { user, isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole) {
    // Check role hierarchy: ADMIN > VERIFIED_USER > USER
    const roleHierarchy = { USER: 0, VERIFIED_USER: 1, ADMIN: 2 };
    const userRoleLevel = roleHierarchy[user!.role];
    const requiredRoleLevel = roleHierarchy[requiredRole];

    if (userRoleLevel < requiredRoleLevel) {
      return <Navigate to="/" replace />;
    }
  }

  return <>{children}</>;
};