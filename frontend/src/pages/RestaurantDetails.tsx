import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiService } from '../services/api';
import type { Restaurant } from '../types/restaurant.types';
import type { Dish } from '../types/dish.types';
import ReviewSection from '../components/restaurants/ReviewSection.tsx';
import './RestaurantDetails.css';

const RestaurantDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [restaurant, setRestaurant] = useState<Restaurant | null>(null);
  const [menu, setMenu] = useState<Dish[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      fetchRestaurantData(parseInt(id));
    }
  }, [id]);

  const fetchRestaurantData = async (restaurantId: number) => {
    try {
      setLoading(true);
      const [restaurantData, menuData] = await Promise.all([
        apiService.getRestaurantById(restaurantId),
        apiService.getRestaurantMenu(restaurantId)
      ]);
      setRestaurant(restaurantData);
      setMenu(menuData);
      setError(null);
    } catch (err) {
      setError('Failed to load restaurant details');
      console.error('Error fetching restaurant:', err);
    } finally {
      setLoading(false);
    }
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
        <p>{error || 'This restaurant does not exist'}</p>
        <button onClick={() => navigate('/')} className="btn-back">
          ← Back to Map
        </button>
      </div>
    );
  }

  return (
    <div className="restaurant-details">
      <div className="details-header">
        <button onClick={() => navigate('/')} className="btn-back-inline">
          ← Back to Map
        </button>

        <div className="restaurant-hero">
          <h1>{restaurant.name}</h1>
          <p className="hero-address">📍 {restaurant.address}</p>

          <div className="restaurant-meta">
            {restaurant.rating && (
              <div className="meta-item">
                <span className="meta-label">Rating</span>
                <span className="meta-value">⭐ {restaurant.rating.toFixed(1)}</span>
              </div>
            )}
            {restaurant.phone && (
              <div className="meta-item">
                <span className="meta-label">Phone</span>
                <span className="meta-value">📞 {restaurant.phone}</span>
              </div>
            )}
            {restaurant.openingHours && (
              <div className="meta-item">
                <span className="meta-label">Hours</span>
                <span className="meta-value">🕒 {restaurant.openingHours}</span>
              </div>
            )}
          </div>

          {restaurant.description && (
            <p className="restaurant-description">{restaurant.description}</p>
          )}
        </div>
      </div>

      <div className="menu-section">
        <h2>Menu</h2>

        {menu.length === 0 ? (
          <div className="no-menu">
            <p>No menu items available yet</p>
            <p className="text-muted">Check back soon!</p>
          </div>
        ) : (
          <div className="menu-grid">
            {menu.map((dish) => (
              <div key={dish.id} className="dish-card">
                {dish.imageUrl && (
                  <div className="dish-image">
                    <img src={dish.imageUrl} alt={dish.name} />
                  </div>
                )}

                <div className="dish-content">
                  <div className="dish-header">
                    <h3>{dish.name}</h3>
                    <span className="dish-price">${dish.price.toFixed(2)}</span>
                  </div>

                  {dish.description && (
                    <p className="dish-description">{dish.description}</p>
                  )}

                  {(dish.baseKcal || dish.baseProteinG || dish.baseCarbsG || dish.baseFatG) && (
                    <div className="nutrition-info">
                      <h4>Nutrition (per serving)</h4>
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

                  {dish.allergens && dish.allergens.length > 0 && (
                    <div className="allergens">
                      <h4>⚠️ Allergens</h4>
                      <div className="allergen-tags">
                        {dish.allergens.map((allergen, index) => (
                          <span key={index} className="allergen-tag">
                            {allergen}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}

                  {!dish.isAvailable && (
                    <div className="unavailable-badge">
                      Currently Unavailable
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Reviews Section */}
        <ReviewSection restaurantId={restaurant.id} />
      </div>
    </div>
  );
};

export default RestaurantDetails;