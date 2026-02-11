import React, { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { apiService } from '../services/api';
import type {AuthResponse, LoginRequest, RegisterRequest} from '../types/auth.types';

interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  role: 'USER' | 'VERIFIED_USER' | 'ADMIN';
  isActive: boolean;
  createdAt: string;
  verifiedAt?: string;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Fetch current user from API
  const fetchCurrentUser = async (authToken: string, skipLogoutOnError = false) => {
    try {
      console.log('Fetching current user from API...');
      const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
      const fullUrl = `${apiUrl}/users/me`;
      console.log('Fetching from:', fullUrl);

      const response = await fetch(fullUrl, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json',
        },
      });

      console.log('Response status:', response.status);

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Failed to fetch user, status:', response.status, 'error:', errorText);

        // Only logout on 401 (unauthorized) and only if not skipping
        if (response.status === 401 && !skipLogoutOnError) {
          console.log('Unauthorized - logging out');
          logout();
          throw new Error('Unauthorized');
        }

        // For other errors, just throw but don't logout
        throw new Error(`Failed to fetch user: ${response.status} - ${errorText}`);
      }

      const userData = await response.json();
      console.log('User fetched from API:', userData);

      setUser(userData);
      // Update localStorage with fresh data
      localStorage.setItem('user', JSON.stringify(userData));

      return userData;
    } catch (error) {
      console.error('Error in fetchCurrentUser:', error);
      // Don't logout here - only logout on 401 above
      throw error;
    }
  };

  // Refresh user data from API
  const refreshUser = async () => {
    const storedToken = localStorage.getItem('token');
    if (!storedToken) {
      console.error('No token found, cannot refresh user');
      throw new Error('No token available');
    }

    console.log('Refreshing user with token...');
    return await fetchCurrentUser(storedToken);
  };

  // Load user from API on mount (if token exists)
  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('token');

      if (storedToken) {
        console.log('Token found, fetching user from API...');
        setToken(storedToken);
        try {
          // Always fetch fresh user data from API instead of using stale localStorage
          await fetchCurrentUser(storedToken);
        } catch (error) {
          console.error('Failed to load user, clearing auth');
          // If API call fails, user will be null (already handled in fetchCurrentUser)
        }
      } else {
        console.log('No token found, user not logged in');
      }

      setIsLoading(false);
    };

    initAuth();
  }, []);

  const login = async (credentials: LoginRequest) => {
    try {
      const response: AuthResponse = await apiService.login(credentials);

      const userData: User = {
        id: response.id,
        email: response.email,
        firstName: response.firstName,
        lastName: response.lastName,
        phoneNumber: response.phoneNumber,
        role: response.role,
        isActive: true,
        createdAt: new Date().toISOString(),
      };

      // Save to state
      setToken(response.token);
      setUser(userData);

      // Save to localStorage
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify(userData));
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  };

  const register = async (data: RegisterRequest) => {
    try {
      await apiService.register(data);
      // Auto-login after registration
      await login({
        email: data.email,
        password: data.password,
      });
    } catch (error) {
      console.error('Registration error:', error);
      throw error;
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  const value: AuthContextType = {
    user,
    token,
    isLoading,
    isAuthenticated: !!token && !!user,
    login,
    register,
    logout,
    refreshUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// Custom hook to use auth context
// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};