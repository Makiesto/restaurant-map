export interface Dish {
  id: number;
  name: string;
  description?: string;
  price: number;
  imageUrl?: string;
  isAvailable: boolean;
  baseProteinG?: number;
  baseFatG?: number;
  baseCarbsG?: number;
  baseKcal?: number;
  restaurantId: number;
  restaurantName: string;
  components: ComponentInfo[];
  allergens: string[];
}

export interface ComponentInfo {
  componentId: number;
  componentName: string;
  amount: number;
  isOptional: boolean;
}

export interface CreateDishRequest {
  name: string;
  description?: string;
  price: number;
  imageUrl?: string;
  components: {
    componentId: number;
    amount: number;
    isOptional: boolean;
  }[];
}