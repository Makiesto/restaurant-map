// src/services/nutritionApi.ts
import axios from 'axios';

// For Vite - use import.meta.env instead of process.env
// Get your free API key at: https://fdc.nal.usda.gov/api-key-signup.html
const USDA_API_KEY = import.meta.env.VITE_USDA_API_KEY || 'DEMO_KEY';
const USDA_BASE_URL = 'https://api.nal.usda.gov/fdc/v1';

// Warn if using demo key
if (USDA_API_KEY === 'DEMO_KEY') {
  console.warn('⚠️ Using DEMO_KEY for USDA API. Set VITE_USDA_API_KEY in .env file for higher rate limits.');
}

export interface NutritionData {
  calories: number;
  protein: number;
  carbs: number;
  fat: number;
  fiber?: number;
  sugar?: number;
}

export interface FoodSearchResult {
  fdcId: number;
  description: string;
  brandOwner?: string;
  dataType: string;
}

export interface Ingredient {
  id: string;
  name: string;
  amount: number;
  unit: string;
  fdcId?: number;
  nutrition?: NutritionData;
}

interface FoodNutrient {
  nutrient?: {
    id: number;
  };
  amount?: number;
}

interface FoodDetailsResponse {
  foodNutrients?: FoodNutrient[];
}

interface SearchResponse {
  foods?: FoodSearchResult[];
}

class NutritionApiService {
  /**
   * Search for food items
   */
  async searchFood(query: string): Promise<FoodSearchResult[]> {
    try {
      const response = await axios.get<SearchResponse>(`${USDA_BASE_URL}/foods/search`, {
        params: {
          api_key: USDA_API_KEY,
          query: query,
          pageSize: 10,
          dataType: ['Survey (FNDDS)', 'Foundation', 'SR Legacy'], // Most reliable data types
        },
      });

      return response.data.foods || [];
    } catch (error) {
      console.error('Error searching food:', error);
      throw new Error('Failed to search food database');
    }
  }

  /**
   * Get detailed nutrition data for a specific food item
   */
  async getFoodDetails(fdcId: number): Promise<NutritionData> {
    try {
      const response = await axios.get<FoodDetailsResponse>(`${USDA_BASE_URL}/food/${fdcId}`, {
        params: {
          api_key: USDA_API_KEY,
        },
      });

      const nutrients = response.data.foodNutrients || [];

      // Extract macro nutrients (per 100g)
      const getNutrient = (nutrientId: number): number => {
        const nutrient = nutrients.find((n: FoodNutrient) => n.nutrient?.id === nutrientId);
        return nutrient?.amount || 0;
      };

      return {
        calories: getNutrient(1008), // Energy (kcal)
        protein: getNutrient(1003), // Protein
        carbs: getNutrient(1005), // Carbohydrates
        fat: getNutrient(1004), // Total lipid (fat)
        fiber: getNutrient(1079), // Fiber
        sugar: getNutrient(2000), // Total sugars
      };
    } catch (error) {
      console.error('Error fetching food details:', error);
      throw new Error('Failed to fetch food nutrition data');
    }
  }

  /**
   * Calculate nutrition for a specific amount of food
   */
  calculateNutritionForAmount(
    nutrition: NutritionData,
    amount: number,
    unit: string
  ): NutritionData {
    // Nutrition data from API is per 100g
    // Convert different units to grams
    let grams = amount;

    switch (unit.toLowerCase()) {
      case 'kg':
        grams = amount * 1000;
        break;
      case 'g':
        grams = amount;
        break;
      case 'oz':
        grams = amount * 28.35;
        break;
      case 'lb':
        grams = amount * 453.592;
        break;
      case 'ml':
      case 'l':
        // For liquids, assume 1ml = 1g (approximate for most liquids)
        grams = unit === 'l' ? amount * 1000 : amount;
        break;
      case 'cup':
        grams = amount * 240; // Approximate
        break;
      case 'tbsp':
        grams = amount * 15;
        break;
      case 'tsp':
        grams = amount * 5;
        break;
      default:
        grams = amount; // Default to grams
    }

    // Calculate nutrition for the specified amount (nutrition is per 100g)
    const multiplier = grams / 100;

    return {
      calories: nutrition.calories * multiplier,
      protein: nutrition.protein * multiplier,
      carbs: nutrition.carbs * multiplier,
      fat: nutrition.fat * multiplier,
      fiber: nutrition.fiber ? nutrition.fiber * multiplier : undefined,
      sugar: nutrition.sugar ? nutrition.sugar * multiplier : undefined,
    };
  }

  /**
   * Calculate total nutrition from multiple ingredients
   */
  calculateTotalNutrition(ingredients: Ingredient[]): NutritionData {
    const total: NutritionData = {
      calories: 0,
      protein: 0,
      carbs: 0,
      fat: 0,
      fiber: 0,
      sugar: 0,
    };

    ingredients.forEach((ingredient) => {
      if (ingredient.nutrition) {
        const calculated = this.calculateNutritionForAmount(
          ingredient.nutrition,
          ingredient.amount,
          ingredient.unit
        );

        total.calories += calculated.calories;
        total.protein += calculated.protein;
        total.carbs += calculated.carbs;
        total.fat += calculated.fat;
        if (total.fiber !== undefined && calculated.fiber !== undefined) {
          total.fiber += calculated.fiber;
        }
        if (total.sugar !== undefined && calculated.sugar !== undefined) {
          total.sugar += calculated.sugar;
        }
      }
    });

    return total;
  }
}

export const nutritionApiService = new NutritionApiService();