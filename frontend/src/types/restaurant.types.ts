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
  owner: {
    id: number;
    username: string;
    email: string;
  };
  cuisineType?: string;
  priceRange?: 'budget' | 'moderate' | 'expensive' | 'luxury';
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