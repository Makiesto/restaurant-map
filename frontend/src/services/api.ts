import axios, {type AxiosInstance, AxiosError } from 'axios';
import type {AuthResponse, LoginRequest, RegisterRequest, User} from '../types/auth.types';
import type {Restaurant, CreateRestaurantRequest} from '../types/restaurant.types';
import type {Dish, CreateDishRequest} from '../types/dish.types';
import type {Review, CreateReviewRequest, ReviewStats} from '../types/review.types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Add token to requests
    this.api.interceptors.request.use((config) => {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    // Handle 401 errors
    this.api.interceptors.response.use(
      (response) => response,
      (error: AxiosError) => {
        if (error.response?.status === 401) {
          localStorage.removeItem('token');
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  // Auth
  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await this.api.post<AuthResponse>('/auth/login', data);
    return response.data;
  }

  async register(data: RegisterRequest): Promise<User> {
    const response = await this.api.post<User>('/auth/register', data);
    return response.data;
  }

  // Restaurants
  async getApprovedRestaurants(): Promise<Restaurant[]> {
    const response = await this.api.get<Restaurant[]>('/restaurants');
    return response.data;
  }

  async getRestaurantById(id: number): Promise<Restaurant> {
    const response = await this.api.get<Restaurant>(`/restaurants/${id}`);
    return response.data;
  }

  async getMyRestaurants(): Promise<Restaurant[]> {
    const response = await this.api.get<Restaurant[]>('/restaurants/my');
    return response.data;
  }

  async createRestaurant(data: CreateRestaurantRequest): Promise<Restaurant> {
    const response = await this.api.post<Restaurant>('/restaurants', data);
    return response.data;
  }

  async updateRestaurant(id: number, data: CreateRestaurantRequest): Promise<Restaurant> {
    const response = await this.api.put<Restaurant>(`/restaurants/${id}`, data);
    return response.data;
  }

  async deleteRestaurant(id: number): Promise<void> {
    await this.api.delete(`/restaurants/${id}`);
  }

  // Dishes
  async getRestaurantMenu(restaurantId: number): Promise<Dish[]> {
    const response = await this.api.get<Dish[]>(`/restaurants/${restaurantId}/menu`);
    return response.data;
  }

  async createDish(restaurantId: number, data: CreateDishRequest): Promise<Dish> {
    const response = await this.api.post<Dish>(`/restaurants/${restaurantId}/dishes`, data);
    return response.data;
  }

  // Reviews
  async getRestaurantReviews(restaurantId: number): Promise<Review[]> {
    const response = await this.api.get<Review[]>(`/restaurants/${restaurantId}/reviews`);
    return response.data;
  }

  async getRestaurantStats(restaurantId: number): Promise<ReviewStats> {
    const response = await this.api.get<ReviewStats>(`/restaurants/${restaurantId}/reviews/stats`);
    return response.data;
  }

  async getMyReviewForRestaurant(restaurantId: number): Promise<Review | null> {
    try {
      const response = await this.api.get<Review>(`/restaurants/${restaurantId}/reviews/my`);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  }

  async getMyReviews(): Promise<Review[]> {
    const response = await this.api.get<Review[]>('/reviews/my');
    return response.data;
  }

  async createReview(restaurantId: number, data: CreateReviewRequest): Promise<Review> {
    const response = await this.api.post<Review>(`/restaurants/${restaurantId}/reviews`, data);
    return response.data;
  }

  async updateReview(reviewId: number, data: CreateReviewRequest): Promise<Review> {
    const response = await this.api.put<Review>(`/reviews/${reviewId}`, data);
    return response.data;
  }

  async deleteReview(reviewId: number): Promise<void> {
    await this.api.delete(`/reviews/${reviewId}`);
  }

  // Admin
  async approveRestaurant(id: number): Promise<Restaurant> {
    const response = await this.api.put<Restaurant>(`/admin/restaurants/${id}/approve`);
    return response.data;
  }

  async rejectRestaurant(id: number): Promise<Restaurant> {
    const response = await this.api.put<Restaurant>(`/admin/restaurants/${id}/reject`);
    return response.data;
  }

  async getPendingRestaurants(): Promise<Restaurant[]> {
    const response = await this.api.get<Restaurant[]>('/admin/restaurants/pending');
    return response.data;
  }

  async verifyReview(reviewId: number): Promise<Review> {
    const response = await this.api.put<Review>(`/admin/reviews/${reviewId}/verify`);
    return response.data;
  }
}

export const apiService = new ApiService();