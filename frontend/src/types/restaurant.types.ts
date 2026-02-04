export interface Restaurant {
  id: number;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  phone?: string;
  description?: string;
  openingHours?: string;
  rating?: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  owner: {
    id: number;
    username: string;
    email?: string;
  };
  createdAt: string;
  updatedAt: string;
}

export interface CreateRestaurantRequest {
  name: string;
  address: string;
  phone?: string;
  description?: string;
  openingHours?: string;
}