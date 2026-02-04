import React, { useState } from 'react';
import './SearchFilters.css';

export interface FilterOptions {
  searchQuery: string;
  cuisineType: string;
  priceRange: string;
  dietaryOptions: string[];
  minRating: number;
  sortBy: string;
}

interface SearchFiltersProps {
  onFilterChange: (filters: FilterOptions) => void;
  restaurantCount: number;
}

const SearchFilters: React.FC<SearchFiltersProps> = ({ onFilterChange, restaurantCount }) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [filters, setFilters] = useState<FilterOptions>({
    searchQuery: '',
    cuisineType: '',
    priceRange: '',
    dietaryOptions: [],
    minRating: 0,
    sortBy: 'rating',
  });

  const cuisineTypes = [
    'All Cuisines',
    'Italian',
    'Chinese',
    'Japanese',
    'Mexican',
    'Indian',
    'Thai',
    'French',
    'American',
    'Mediterranean',
    'Polish',
    'Other',
  ];

  const priceRanges = [
    { value: '', label: 'Any Price' },
    { value: 'BUDGET', label: '$ Budget' },
    { value: 'MODERATE', label: '$$ Moderate' },
    { value: 'EXPENSIVE', label: '$$$ Expensive' },
    { value: 'LUXURY', label: '$$$$ Luxury' },
  ];

  const dietaryOptionsList = [
    'Vegetarian',
    'Vegan',
    'Gluten-Free',
    'Halal',
    'Kosher',
    'Dairy-Free',
  ];

  const sortOptions = [
    { value: 'rating', label: 'Highest Rated' },
    { value: 'name', label: 'Name (A-Z)' },
    { value: 'newest', label: 'Newest First' },
    { value: 'distance', label: 'Distance' },
  ];

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newFilters = { ...filters, searchQuery: e.target.value };
    setFilters(newFilters);
    onFilterChange(newFilters);
  };

  const handleCuisineChange = (cuisine: string) => {
    const value = cuisine === 'All Cuisines' ? '' : cuisine;
    const newFilters = { ...filters, cuisineType: value };
    setFilters(newFilters);
    onFilterChange(newFilters);
  };

  const handlePriceChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const newFilters = { ...filters, priceRange: e.target.value };
    setFilters(newFilters);
    onFilterChange(newFilters);
  };

  const handleDietaryToggle = (option: string) => {
    const newDietaryOptions = filters.dietaryOptions.includes(option)
      ? filters.dietaryOptions.filter(o => o !== option)
      : [...filters.dietaryOptions, option];

    const newFilters = { ...filters, dietaryOptions: newDietaryOptions };
    setFilters(newFilters);
    onFilterChange(newFilters);
  };

  const handleRatingChange = (rating: number) => {
    const newFilters = { ...filters, minRating: rating };
    setFilters(newFilters);
    onFilterChange(newFilters);
  };

  const handleSortChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const newFilters = { ...filters, sortBy: e.target.value };
    setFilters(newFilters);
    onFilterChange(newFilters);
  };

  const handleClearFilters = () => {
    const clearedFilters: FilterOptions = {
      searchQuery: '',
      cuisineType: '',
      priceRange: '',
      dietaryOptions: [],
      minRating: 0,
      sortBy: 'rating',
    };
    setFilters(clearedFilters);
    onFilterChange(clearedFilters);
  };

  const activeFiltersCount =
    (filters.cuisineType ? 1 : 0) +
    (filters.priceRange ? 1 : 0) +
    filters.dietaryOptions.length +
    (filters.minRating > 0 ? 1 : 0);

  return (
    <div className="search-filters">
      {/* Search Bar */}
      <div className="search-bar">
        <input
          type="text"
          placeholder="Search restaurants..."
          value={filters.searchQuery}
          onChange={handleSearchChange}
          className="search-input"
        />
        <span className="search-icon">🔍</span>
      </div>

      {/* Filter Toggle */}
      <div className="filter-header">
        <button
          onClick={() => setIsExpanded(!isExpanded)}
          className="filter-toggle"
        >
          <span>🎛️ Filters</span>
          {activeFiltersCount > 0 && (
            <span className="filter-badge">{activeFiltersCount}</span>
          )}
          <span className={`toggle-icon ${isExpanded ? 'expanded' : ''}`}>▼</span>
        </button>

        <div className="sort-wrapper">
          <select
            value={filters.sortBy}
            onChange={handleSortChange}
            className="sort-select"
          >
            {sortOptions.map(option => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Results Count */}
      <div className="results-count">
        {restaurantCount} {restaurantCount === 1 ? 'restaurant' : 'restaurants'} found
      </div>

      {/* Expandable Filters */}
      {isExpanded && (
        <div className="filters-panel">
          {/* Cuisine Type */}
          <div className="filter-section">
            <label className="filter-label">Cuisine Type</label>
            <div className="cuisine-buttons">
              {cuisineTypes.map(cuisine => (
                <button
                  key={cuisine}
                  onClick={() => handleCuisineChange(cuisine)}
                  className={`cuisine-btn ${
                    (cuisine === 'All Cuisines' && !filters.cuisineType) ||
                    filters.cuisineType === cuisine
                      ? 'active'
                      : ''
                  }`}
                >
                  {cuisine}
                </button>
              ))}
            </div>
          </div>

          {/* Price Range */}
          <div className="filter-section">
            <label className="filter-label">Price Range</label>
            <select
              value={filters.priceRange}
              onChange={handlePriceChange}
              className="filter-select"
            >
              {priceRanges.map(range => (
                <option key={range.value} value={range.value}>
                  {range.label}
                </option>
              ))}
            </select>
          </div>

          {/* Dietary Options */}
          <div className="filter-section">
            <label className="filter-label">Dietary Options</label>
            <div className="dietary-options">
              {dietaryOptionsList.map(option => (
                <label key={option} className="checkbox-label">
                  <input
                    type="checkbox"
                    checked={filters.dietaryOptions.includes(option)}
                    onChange={() => handleDietaryToggle(option)}
                  />
                  <span>{option}</span>
                </label>
              ))}
            </div>
          </div>

          {/* Minimum Rating */}
          <div className="filter-section">
            <label className="filter-label">Minimum Rating</label>
            <div className="rating-buttons">
              {[0, 1, 2, 3, 4].map(rating => (
                <button
                  key={rating}
                  onClick={() => handleRatingChange(rating)}
                  className={`rating-btn ${filters.minRating === rating ? 'active' : ''}`}
                >
                  {rating === 0 ? 'Any' : `${rating}+ ⭐`}
                </button>
              ))}
            </div>
          </div>

          {/* Clear Filters */}
          {activeFiltersCount > 0 && (
            <button onClick={handleClearFilters} className="btn-clear-filters">
              Clear All Filters
            </button>
          )}
        </div>
      )}
    </div>
  );
};

export default SearchFilters;