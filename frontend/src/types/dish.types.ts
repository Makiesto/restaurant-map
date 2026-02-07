// src/types/dish.types.ts

export interface DishComponent {
  componentId: number;
  componentName?: string;
  amount: number;
  isOptional: boolean;
}

export interface DishComponentInput {
  componentId: number;
  amount: number;
  isOptional: boolean;
}

// Ingredient structure for saving with dish
export interface SavedIngredient {
  id: string;
  name: string;
  amount: number;
  unit: string;
  fdcId?: number;
}

export interface Dish {
  id: number;
  restaurantId: number;
  name: string;
  description?: string;
  price: number;
  imageUrl?: string;
  isAvailable: boolean;
  components?: DishComponent[];
  allergens?: string[];

  baseKcal?: number;
  baseProteinG?: number;
  baseCarbsG?: number;
  baseFatG?: number;

  // Saved ingredients for future editing
  ingredients?: SavedIngredient[];
}

export interface CreateDishRequest {
  name: string;
  description?: string;
  price: number;
  imageUrl?: string;
  isAvailable: boolean;
  components?: DishComponentInput[];
  allergens: string[];

  baseKcal?: number;
  baseProteinG?: number;
  baseCarbsG?: number;
  baseFatG?: number;

  // Saved ingredients
  ingredients?: SavedIngredient[];
}

export interface UpdateDishRequest {
  name?: string;
  description?: string;
  price?: number;
  imageUrl?: string;
  isAvailable?: boolean;
  components?: DishComponentInput[];
  allergens?: string[];

  baseKcal?: number;
  baseProteinG?: number;
  baseCarbsG?: number;
  baseFatG?: number;

  // Saved ingredients
  ingredients?: SavedIngredient[];
}