export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  role: 'USER' | 'VERIFIED_USER' | 'ADMIN';
  isActive: boolean;
  emailVerified: boolean;
  emailVerifiedAt?: string;
  createdAt: string;
  verifiedAt?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phoneNumber?: string;
}

export interface AuthResponse {
  token: string;
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  role: 'USER' | 'VERIFIED_USER' | 'ADMIN';
  emailVerified: boolean;
}