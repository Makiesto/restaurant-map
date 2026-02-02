export interface Review {
  id: number;
  restaurantId: number;
  restaurantName: string;
  userId: number;
  userName: string;
  rating: number;
  comment?: string;
  isVerified: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateReviewRequest {
  rating: number;
  comment?: string;
}

export interface ReviewStats {
  averageRating: number;
  totalReviews: number;
  fiveStars: number;
  fourStars: number;
  threeStars: number;
  twoStars: number;
  oneStar: number;
}