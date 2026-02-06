export interface Allergen {
  id: number;
  name: string;
  severityLevel?: string;
}

export interface UserAllergenPreferences {
  allergenIds: number[];
}

export const COMMON_ALLERGENS = [
  'Peanuts',
  'Tree Nuts',
  'Milk',
  'Eggs',
  'Fish',
  'Shellfish',
  'Soy',
  'Wheat',
  'Gluten',
  'Sesame',
  'Mustard',
  'Celery',
  'Lupin',
  'Sulphites',
] as const;

export type AllergenName = typeof COMMON_ALLERGENS[number];