import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiService } from '../services/api';
import type { Restaurant } from '../types/restaurant.types';
import type { Dish } from '../types/dish.types';
import DishForm from '../components/forms/DishForm.tsx';
import './RestaurantManagement.css';

const RestaurantManagement: React.FC = () => {
  const { restaurantId } = useParams<{ restaurantId: string }>();
  const navigate = useNavigate();

  const [restaurant, setRestaurant] = useState<Restaurant | null>(null);
  const [dishes, setDishes] = useState<Dish[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showDishForm, setShowDishForm] = useState(false);
  const [editingDish, setEditingDish] = useState<Dish | null>(null);

  useEffect(() => {
    if (restaurantId) {
      fetchRestaurantData();
    }
  }, [restaurantId]);

  const fetchRestaurantData = async () => {
    try {
      setLoading(true);
      const [restaurantData, dishesData] = await Promise.all([
        apiService.getRestaurantById(parseInt(restaurantId!)),
        apiService.getRestaurantMenu(parseInt(restaurantId!)),
      ]);
      setRestaurant(restaurantData);
      setDishes(dishesData);
      setError(null);
    } catch (err) {
      setError('Failed to load restaurant data');
      console.error('Error fetching data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddDish = () => {
    setEditingDish(null);
    setShowDishForm(true);
  };

  const handleEditDish = (dish: Dish) => {
    setEditingDish(dish);
    setShowDishForm(true);
  };

  const handleDishFormClose = async (success: boolean) => {
    setShowDishForm(false);
    setEditingDish(null);
    if (success) {
      await fetchRestaurantData();
    }
  };

  const handleDeleteDish = async (dishId: number) => {
    if (!confirm('Are you sure you want to delete this dish?')) {
      return;
    }

    try {
      await apiService.deleteDish(dishId);
      setDishes(dishes.filter(d => d.id !== dishId));
    } catch (err) {
      alert('Failed to delete dish');
      console.error('Error deleting dish:', err);
    }
  };

  const handleToggleAvailability = async (dish: Dish) => {
    try {
      const updatedDish = await apiService.updateDish(dish.id, {
        ...dish,
        isAvailable: !dish.isAvailable,
      });
      setDishes(dishes.map(d => d.id === dish.id ? updatedDish : d));
    } catch (err) {
      alert('Failed to update dish availability');
      console.error('Error updating dish:', err);
    }
  };


  if (loading) {
    return (
      <div className="page-loading">
        <div className="spinner"></div>
        <p>Loading restaurant data...</p>
      </div>
    );
  }

  if (error || !restaurant) {
    return (
      <div className="error-page">
        <h2>Error</h2>
        <p>{error || 'Restaurant not found'}</p>
        <button onClick={() => navigate('/dashboard')} className="btn-primary">
          Back to Dashboard
        </button>
      </div>
    );
  }

  return (
    <div className="restaurant-management">
      {/* Header */}
      <div className="page-header">
        <button onClick={() => navigate('/dashboard')} className="btn-back">
          ← Back to Dashboard
        </button>
        <div className="header-info">
          <h1>{restaurant.name}</h1>
          <p className="restaurant-address">📍 {restaurant.address}</p>
        </div>
        <button onClick={handleAddDish} className="btn-add-dish">
          + Add Dish
        </button>
      </div>

      {/* Dish Form Modal */}
      {showDishForm && (
        <div className="modal-overlay" onClick={() => handleDishFormClose(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editingDish ? 'Edit Dish' : 'Add New Dish'}</h2>
              <button onClick={() => handleDishFormClose(false)} className="btn-close">×</button>
            </div>
            <DishForm
              dish={editingDish}
              restaurantId={restaurant.id}
              onClose={handleDishFormClose}
            />
          </div>
        </div>
      )}

      {/* Dishes Grid */}
      {dishes.length === 0 ? (
        <div className="no-dishes">
          <div className="empty-state">
            <div className="empty-icon">🍽️</div>
            <h3>No Dishes Yet</h3>
            <p>Start building your menu by adding your first dish</p>
            <button onClick={handleAddDish} className="btn-primary-large">
              Add Your First Dish
            </button>
          </div>
        </div>
      ) : (
        <div className="dishes-container">
          <div className="dishes-header">
            <h2>Menu ({dishes.length} items)</h2>
          </div>
          <div className="dishes-grid">
            {dishes.map((dish) => (
              <div key={dish.id} className={`dish-card ${!dish.isAvailable ? 'unavailable' : ''}`}>
                {dish.imageUrl && (
                  <div className="dish-image">
                    <img src={dish.imageUrl} alt={dish.name} />
                    {!dish.isAvailable && (
                      <div className="unavailable-overlay">
                        <span>Unavailable</span>
                      </div>
                    )}
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

                  {/* Nutrition Info */}
                  {(dish.baseKcal || dish.baseProteinG || dish.baseFatG || dish.baseCarbsG) && (
                    <div className="nutrition-info">
                      {dish.baseKcal && (
                        <span className="nutrition-item">
                          <strong>{Math.round(dish.baseKcal)}</strong> kcal
                        </span>
                      )}
                      {dish.baseProteinG && (
                        <span className="nutrition-item">
                          <strong>{dish.baseProteinG.toFixed(1)}g</strong> protein
                        </span>
                      )}
                      {dish.baseCarbsG && (
                        <span className="nutrition-item">
                          <strong>{dish.baseCarbsG.toFixed(1)}g</strong> carbs
                        </span>
                      )}
                      {dish.baseFatG && (
                        <span className="nutrition-item">
                          <strong>{dish.baseFatG.toFixed(1)}g</strong> fat
                        </span>
                      )}
                    </div>
                  )}

                  {/* Actions */}
                  <div className="dish-actions">
                    <button
                      onClick={() => handleToggleAvailability(dish)}
                      className={`btn-toggle ${dish.isAvailable ? 'active' : 'inactive'}`}
                      title={dish.isAvailable ? 'Mark as unavailable' : 'Mark as available'}
                    >
                      {dish.isAvailable ? '✓ Available' : '✗ Unavailable'}
                    </button>
                    <div className="action-buttons">
                      <button
                        onClick={() => handleEditDish(dish)}
                        className="btn-edit-dish"
                        title="Edit dish"
                      >
                        ✏️
                      </button>
                      <button
                        onClick={() => handleDeleteDish(dish.id)}
                        className="btn-delete-dish"
                        title="Delete dish"
                      >
                        🗑️
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default RestaurantManagement;