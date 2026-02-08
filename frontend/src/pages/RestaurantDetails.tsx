import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiService } from '../services/api';
import type { Restaurant } from '../types/restaurant.types';
import type { Dish } from '../types/dish.types';
import ReviewSection from '../components/restaurants/ReviewSection';
import AllergenBadges from '../components/allergens/AllergenBadges';
import AllergenWarning from '../components/allergens/AllergenWarning';
import './RestaurantDetails.css';

const RestaurantDetails: React.FC = () => {
  const { restaurantId } = useParams<{ restaurantId: string }>();
  const navigate = useNavigate();

  const [restaurant, setRestaurant] = useState<Restaurant | null>(null);
  const [dishes, setDishes] = useState<Dish[]>([]);
  const [userAllergens, setUserAllergens] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<string>('all');

  useEffect(() => {
    if (restaurantId) {
      fetchRestaurantData();
      loadUserAllergens();
    }
  }, [restaurantId]);

  const fetchRestaurantData = async () => {
    try {
      setLoading(true);
      const [restaurantData, menuData] = await Promise.all([
        apiService.getRestaurantById(parseInt(restaurantId!)),
        apiService.getRestaurantMenu(parseInt(restaurantId!)),
      ]);
      setRestaurant(restaurantData);
      setDishes(menuData.filter(dish => dish.isAvailable)); // Only show available dishes
      setError(null);
    } catch (err) {
      console.error('Failed to load restaurant:', err);
      setError('Failed to load restaurant details');
    } finally {
      setLoading(false);
    }
  };

  const loadUserAllergens = async () => {
    try {
      const allergens = await apiService.getUserAllergens();
      setUserAllergens(allergens.map(a => a.name));
    } catch (err) {
      // User might not be logged in or have no allergens set
      console.log('No user allergens loaded');
    }
  };

  // Get unique categories from dishes (you can expand this based on your data)
  const categories = ['all', ...new Set(dishes.map(d => d.description?.split(' ')[0] || 'Other'))];

  const filteredDishes = selectedCategory === 'all'
    ? dishes
    : dishes.filter(d => d.description?.toLowerCase().includes(selectedCategory.toLowerCase()));

  // Check if dish has user allergens
  const hasUserAllergens = (dish: Dish) => {
    return dish.allergens?.some(allergen => userAllergens.includes(allergen)) || false;
  };

  if (loading) {
    return (
      <div className="restaurant-loading">
        <div className="spinner"></div>
        <p>Loading restaurant...</p>
      </div>
    );
  }

  if (error || !restaurant) {
    return (
      <div className="restaurant-error">
        <h2>Restaurant Not Found</h2>
        <p>{error || 'The restaurant you\'re looking for doesn\'t exist.'}</p>
        <button onClick={() => navigate('/')} className="btn-back">
          ← Back to Map
        </button>
      </div>
    );
  }

  return (
    <div className="restaurant-details">
      {/* Hero Section */}
      <div className="restaurant-hero">
        {restaurant.imageUrl && (
          <div className="hero-image">
            <img src={restaurant.imageUrl} alt={restaurant.name} />
            <div className="hero-overlay"></div>
          </div>
        )}

        <div className="hero-content">
          <button onClick={() => navigate('/')} className="btn-back-hero">
            ← Back to Map
          </button>

          <div className="hero-info">
            <h1>{restaurant.name}</h1>

            <div className="restaurant-meta">
              {restaurant.rating && (
                <div className="meta-badge rating-badge">
                  <span className="badge-icon">⭐</span>
                  <span className="badge-text">{restaurant.rating.toFixed(1)}</span>
                </div>
              )}

              {restaurant.cuisineType && (
                <div className="meta-badge cuisine-badge">
                  <span className="badge-icon">🍽️</span>
                  <span className="badge-text">{restaurant.cuisineType}</span>
                </div>
              )}

              {restaurant.priceRange && (
                <div className="meta-badge price-badge">
                  <span className="badge-icon">💰</span>
                  <span className="badge-text">
                    {restaurant.priceRange === 'BUDGET' && '$ Budget'}
                    {restaurant.priceRange === 'MODERATE' && '$$ Moderate'}
                    {restaurant.priceRange === 'EXPENSIVE' && '$$$ Expensive'}
                    {restaurant.priceRange === 'LUXURY' && '$$$$ Luxury'}
                  </span>
                </div>
              )}

              {restaurant.isVerified && (
                <div className="meta-badge verified-badge">
                  <span className="badge-icon">✓</span>
                  <span className="badge-text">Verified</span>
                </div>
              )}
            </div>

            <div className="hero-details">
              <div className="detail-item">
                <span className="detail-icon">📍</span>
                <span className="detail-text">{restaurant.address}</span>
              </div>

              {restaurant.phone && (
                <div className="detail-item">
                  <span className="detail-icon">📞</span>
                  <span className="detail-text">{restaurant.phone}</span>
                </div>
              )}

              {restaurant.openingHours && (
                <div className="detail-item">
                  <span className="detail-icon">🕒</span>
                  <span className="detail-text">{restaurant.openingHours}</span>
                </div>
              )}
            </div>

            {restaurant.description && (
              <p className="restaurant-description">{restaurant.description}</p>
            )}

            {/* Dietary Options */}
            {restaurant.dietaryOptions && restaurant.dietaryOptions.length > 0 && (
              <div className="dietary-options">
                <h4>Dietary Options:</h4>
                <div className="dietary-badges">
                  {restaurant.dietaryOptions.map((option, index) => (
                    <span key={index} className="dietary-badge">
                      {option}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="restaurant-content">
        {/* Menu Section */}
        <section className="menu-section">
          <div className="section-header">
            <h2>Menu</h2>
            <span className="item-count">{dishes.length} items</span>
          </div>

          {/* Category Filter */}
          {categories.length > 1 && (
            <div className="category-filter">
              {categories.map((category) => (
                <button
                  key={category}
                  onClick={() => setSelectedCategory(category)}
                  className={`category-btn ${selectedCategory === category ? 'active' : ''}`}
                >
                  {category.charAt(0).toUpperCase() + category.slice(1)}
                </button>
              ))}
            </div>
          )}

          {/* Allergen Notice */}
          {userAllergens.length > 0 && (
            <div className="allergen-notice">
              <span className="notice-icon">⚠️</span>
              <span className="notice-text">
                You have {userAllergens.length} allergen{userAllergens.length !== 1 ? 's' : ''} set.
                Dishes with your allergens will be highlighted.
              </span>
            </div>
          )}

          {/* Dishes Grid */}
          {filteredDishes.length === 0 ? (
            <div className="no-dishes">
              <p>No dishes available in this category</p>
            </div>
          ) : (
            <div className="dishes-grid">
              {filteredDishes.map((dish) => {
                const hasAllergens = hasUserAllergens(dish);

                return (
                  <div
                    key={dish.id}
                    className={`dish-card ${hasAllergens ? 'has-allergens' : ''}`}
                  >
                    {/* Dish Image */}
                    {dish.imageUrl && (
                      <div className="dish-image">
                        <img src={dish.imageUrl} alt={dish.name} />
                        {hasAllergens && (
                          <div className="allergen-warning-badge">
                            ⚠️ Contains Your Allergens
                          </div>
                        )}
                      </div>
                    )}

                    <div className="dish-content">
                      {/* Dish Header */}
                      <div className="dish-header">
                        <h3>{dish.name}</h3>
                        <span className="dish-price">${dish.price.toFixed(2)}</span>
                      </div>

                      {/* Description */}
                      {dish.description && (
                        <p className="dish-description">{dish.description}</p>
                      )}

                      {/* Allergen Warning */}
                      {hasAllergens && dish.allergens && (
                        <AllergenWarning
                          dishAllergens={dish.allergens}
                          userAllergens={userAllergens}
                          compact={true}
                        />
                      )}

                      {/* Allergen Badges */}
                      {dish.allergens && dish.allergens.length > 0 && (
                        <AllergenBadges
                          allergens={dish.allergens}
                          userAllergens={userAllergens}
                          showWarning={false}
                        />
                      )}

                      {/* Nutrition Info */}
                      {(dish.baseKcal || dish.baseProteinG || dish.baseCarbsG || dish.baseFatG) && (
                        <div className="nutrition-info">
                          <h4>Nutrition Information</h4>
                          <div className="nutrition-grid">
                            {dish.baseKcal && (
                              <div className="nutrition-item">
                                <span className="nutrition-label">Calories</span>
                                <span className="nutrition-value">{Math.round(dish.baseKcal)} kcal</span>
                              </div>
                            )}
                            {dish.baseProteinG && (
                              <div className="nutrition-item">
                                <span className="nutrition-label">Protein</span>
                                <span className="nutrition-value">{dish.baseProteinG.toFixed(1)}g</span>
                              </div>
                            )}
                            {dish.baseCarbsG && (
                              <div className="nutrition-item">
                                <span className="nutrition-label">Carbs</span>
                                <span className="nutrition-value">{dish.baseCarbsG.toFixed(1)}g</span>
                              </div>
                            )}
                            {dish.baseFatG && (
                              <div className="nutrition-item">
                                <span className="nutrition-label">Fat</span>
                                <span className="nutrition-value">{dish.baseFatG.toFixed(1)}g</span>
                              </div>
                            )}
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </section>

        {/* Reviews Section */}
        <section className="reviews-section">
          <ReviewSection restaurantId={parseInt(restaurantId!)} />
        </section>
      </div>
    </div>
  );
};

export default RestaurantDetails;