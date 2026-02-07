// src/components/nutrition/IngredientManager.tsx
import React, { useState } from 'react';
import { nutritionApiService, type Ingredient, type FoodSearchResult } from '../../services/nutrionApi';
import './IngredientManager.css';

interface IngredientManagerProps {
  ingredients: Ingredient[];
  onIngredientsChange: (ingredients: Ingredient[]) => void;
}

const IngredientManager: React.FC<IngredientManagerProps> = ({
  ingredients,
  onIngredientsChange,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<FoodSearchResult[]>([]);
  const [searching, setSearching] = useState(false);
  const [amount, setAmount] = useState<number>(100);
  const [unit, setUnit] = useState<string>('g');
  const [showSearch, setShowSearch] = useState(false);

  const units = ['g', 'kg', 'oz', 'lb', 'ml', 'l', 'cup', 'tbsp', 'tsp'];

  const handleSearch = async () => {
    if (!searchQuery.trim()) return;

    setSearching(true);
    try {
      const results = await nutritionApiService.searchFood(searchQuery);
      setSearchResults(results);
      setShowSearch(true);
    } catch (error) {
      alert('Failed to search foods. Please try again.');
      console.error(error);
    } finally {
      setSearching(false);
    }
  };

  const handleAddIngredient = async (food: FoodSearchResult) => {
    try {
      // Fetch detailed nutrition data
      const nutritionData = await nutritionApiService.getFoodDetails(food.fdcId);

      const newIngredient: Ingredient = {
        id: Date.now().toString(),
        name: food.description,
        amount: amount,
        unit: unit,
        fdcId: food.fdcId,
        nutrition: nutritionData,
      };

      onIngredientsChange([...ingredients, newIngredient]);

      // Reset form
      setSearchQuery('');
      setSearchResults([]);
      setShowSearch(false);
      setAmount(100);
      setUnit('g');
    } catch (error) {
      alert('Failed to add ingredient. Please try again.');
      console.error(error);
    }
  };

  const handleRemoveIngredient = (id: string) => {
    onIngredientsChange(ingredients.filter((ing) => ing.id !== id));
  };

  const handleUpdateAmount = (id: string, newAmount: number) => {
    onIngredientsChange(
      ingredients.map((ing) =>
        ing.id === id ? { ...ing, amount: newAmount } : ing
      )
    );
  };

  const handleUpdateUnit = (id: string, newUnit: string) => {
    onIngredientsChange(
      ingredients.map((ing) =>
        ing.id === id ? { ...ing, unit: newUnit } : ing
      )
    );
  };

  const totalNutrition = nutritionApiService.calculateTotalNutrition(ingredients);

  return (
    <div className="ingredient-manager">
      <div className="ingredient-header">
        <h3>Ingredients & Nutrition Calculator</h3>
        <p className="ingredient-subtitle">
          Add ingredients to automatically calculate nutritional information
        </p>
      </div>

      {/* Add Ingredient Section */}
      <div className="add-ingredient-section">
        <div className="search-box">
          <input
            type="text"
            placeholder="Search for ingredient (e.g., chicken breast, rice, broccoli)"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            className="ingredient-search-input"
          />
          <button
            onClick={handleSearch}
            disabled={searching || !searchQuery.trim()}
            className="btn-search"
          >
            {searching ? 'Searching...' : '🔍 Search'}
          </button>
        </div>

        <div className="amount-input-group">
          <label>Amount:</label>
          <input
            type="number"
            value={amount}
            onChange={(e) => setAmount(parseFloat(e.target.value) || 0)}
            min="0"
            step="0.1"
            className="amount-input"
          />
          <select
            value={unit}
            onChange={(e) => setUnit(e.target.value)}
            className="unit-select"
          >
            {units.map((u) => (
              <option key={u} value={u}>
                {u}
              </option>
            ))}
          </select>
        </div>

        {/* Search Results */}
        {showSearch && searchResults.length > 0 && (
          <div className="search-results">
            <div className="search-results-header">
              <h4>Select an ingredient:</h4>
              <button
                onClick={() => setShowSearch(false)}
                className="btn-close-search"
              >
                ✕
              </button>
            </div>
            <div className="results-list">
              {searchResults.map((food) => (
                <div
                  key={food.fdcId}
                  className="result-item"
                  onClick={() => handleAddIngredient(food)}
                >
                  <span className="result-name">{food.description}</span>
                  {food.brandOwner && (
                    <span className="result-brand">{food.brandOwner}</span>
                  )}
                  <span className="result-type">{food.dataType}</span>
                </div>
              ))}
            </div>
          </div>
        )}

        {showSearch && searchResults.length === 0 && !searching && (
          <div className="no-results">
            <p>No results found. Try a different search term.</p>
          </div>
        )}
      </div>

      {/* Ingredients List */}
      {ingredients.length > 0 && (
        <div className="ingredients-list">
          <h4>Added Ingredients ({ingredients.length})</h4>
          <div className="ingredients-table">
            {ingredients.map((ingredient) => {
              const calculated = nutritionApiService.calculateNutritionForAmount(
                ingredient.nutrition!,
                ingredient.amount,
                ingredient.unit
              );

              return (
                <div key={ingredient.id} className="ingredient-row">
                  <div className="ingredient-info">
                    <span className="ingredient-name">{ingredient.name}</span>
                    <div className="ingredient-amount-controls">
                      <input
                        type="number"
                        value={ingredient.amount}
                        onChange={(e) =>
                          handleUpdateAmount(
                            ingredient.id,
                            parseFloat(e.target.value) || 0
                          )
                        }
                        min="0"
                        step="0.1"
                        className="amount-input-small"
                      />
                      <select
                        value={ingredient.unit}
                        onChange={(e) =>
                          handleUpdateUnit(ingredient.id, e.target.value)
                        }
                        className="unit-select-small"
                      >
                        {units.map((u) => (
                          <option key={u} value={u}>
                            {u}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>

                  <div className="ingredient-nutrition">
                    <span className="nutrition-item">
                      {Math.round(calculated.calories)} kcal
                    </span>
                    <span className="nutrition-item">
                      P: {calculated.protein.toFixed(1)}g
                    </span>
                    <span className="nutrition-item">
                      C: {calculated.carbs.toFixed(1)}g
                    </span>
                    <span className="nutrition-item">
                      F: {calculated.fat.toFixed(1)}g
                    </span>
                  </div>

                  <button
                    onClick={() => handleRemoveIngredient(ingredient.id)}
                    className="btn-remove-ingredient"
                    title="Remove ingredient"
                  >
                    🗑️
                  </button>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Total Nutrition Summary */}
      {ingredients.length > 0 && (
        <div className="nutrition-summary">
          <h4>Total Nutritional Information</h4>
          <div className="nutrition-grid">
            <div className="nutrition-card">
              <span className="nutrition-label">Calories</span>
              <span className="nutrition-value">
                {Math.round(totalNutrition.calories)} kcal
              </span>
            </div>
            <div className="nutrition-card">
              <span className="nutrition-label">Protein</span>
              <span className="nutrition-value">
                {totalNutrition.protein.toFixed(1)}g
              </span>
            </div>
            <div className="nutrition-card">
              <span className="nutrition-label">Carbs</span>
              <span className="nutrition-value">
                {totalNutrition.carbs.toFixed(1)}g
              </span>
            </div>
            <div className="nutrition-card">
              <span className="nutrition-label">Fat</span>
              <span className="nutrition-value">
                {totalNutrition.fat.toFixed(1)}g
              </span>
            </div>
          </div>
          <p className="nutrition-note">
            💡 These values will be automatically saved with your dish
          </p>
        </div>
      )}

      {ingredients.length === 0 && (
        <div className="empty-ingredients">
          <p>🥗 No ingredients added yet</p>
          <p className="empty-subtitle">
            Search and add ingredients to calculate nutrition automatically
          </p>
        </div>
      )}
    </div>
  );
};

export default IngredientManager;