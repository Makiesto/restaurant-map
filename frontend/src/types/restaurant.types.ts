export interface Restaurant {
  id: number;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  phone?: string;
  openingHours?: string;
  description?: string;
  imageUrl?: string;
  rating?: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';

  // Verification fields
  isVerified: boolean;
  verifiedAt?: string;

  owner: {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
  };
  cuisineType?: string;
  priceRange?: 'BUDGET' | 'MODERATE' | 'EXPENSIVE' | 'LUXURY';
  dietaryOptions?: string[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateRestaurantRequest {
  name: string;
  address: string;
  phone?: string;
  openingHours?: string;
  description?: string;
  imageUrl?: string;
  cuisineType?: string;
  priceRange?: string;
  dietaryOptions?: string[];
}

export interface UpdateRestaurantRequest {
  name?: string;
  address?: string;
  phone?: string;
  openingHours?: string;
  description?: string;
  imageUrl?: string;
  cuisineType?: string;
  priceRange?: string;
  dietaryOptions?: string[];
}