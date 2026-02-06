export interface AdminStats {
    totalUsers: number;
    totalRestaurants: number;
    pendingRestaurants: number;
    approvedRestaurants: number;
    rejectedRestaurants: number;
    verifiedRestaurants: number;
    unverifiedRestaurants: number;
    totalReviews: number;
    unverifiedReviews: number;
}

export interface VerifyRestaurantRequest {
    notes?: string;
}