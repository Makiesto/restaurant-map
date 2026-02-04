import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { useNavigate } from 'react-router-dom';
import { apiService } from '../../services/api';
import type { Restaurant } from '../../types/restaurant.types';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import './Map.css';

// Fix for default marker icons in react-leaflet
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';

const DefaultIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

L.Marker.prototype.options.icon = DefaultIcon;

const Map: React.FC = () => {
  const navigate = useNavigate();
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedRestaurant, setSelectedRestaurant] = useState<Restaurant | null>(null);

  // Default center - Kraków, Poland
  const defaultCenter: [number, number] = [50.0647, 19.9450];
  const defaultZoom = 13;

  useEffect(() => {
    fetchRestaurants();
  }, []);

  const fetchRestaurants = async () => {
    try {
      setLoading(true);
      const data = await apiService.getApprovedRestaurants();
      setRestaurants(data);
      setError(null);
    } catch (err) {
      setError('Failed to load restaurants');
      console.error('Error fetching restaurants:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRestaurantClick = (restaurant: Restaurant) => {
    setSelectedRestaurant(restaurant);
  };

  const handleViewDetails = (restaurantId: number) => {
    navigate(`/restaurant/${restaurantId}`);
  };

  if (loading) {
    return (
      <div className="map-loading">
        <div className="spinner"></div>
        <p>Loading restaurants...</p>
      </div>
    );
  }

  return (
    <div className="map-container">
      {/* Sidebar */}
      <div className="map-sidebar">
        <div className="sidebar-header">
          <h2>
            <span>🍽️</span>
            Restaurants
          </h2>
          <p className="location-count">{restaurants.length} locations</p>
        </div>

        {restaurants.length === 0 ? (
          <div className="empty-state">
            <h3>No restaurants found</h3>
            <p>Be the first to add one!</p>
          </div>
        ) : (
          <div className="restaurant-list">
            {restaurants.map((restaurant) => (
              <div
                key={restaurant.id}
                className={`restaurant-card ${
                  selectedRestaurant?.id === restaurant.id ? 'selected' : ''
                }`}
                onClick={() => handleRestaurantClick(restaurant)}
              >
                <h3>{restaurant.name}</h3>
                <p className="restaurant-address">
                  📍 {restaurant.address}
                </p>
                {restaurant.rating && (
                  <div className="restaurant-rating">
                    ⭐ {restaurant.rating.toFixed(1)}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Map */}
      <div className="map-view">
        {error ? (
          <div className="map-error">
            <p>{error}</p>
          </div>
        ) : (
          <MapContainer
            center={defaultCenter}
            zoom={defaultZoom}
            style={{ height: '100%', width: '100%' }}
          >
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />

            {restaurants.map((restaurant) => (
              <Marker
                key={restaurant.id}
                position={[restaurant.latitude, restaurant.longitude]}
                eventHandlers={{
                  click: () => handleRestaurantClick(restaurant),
                }}
              >
                <Popup>
                  <div className="popup-content">
                    <h3>{restaurant.name}</h3>
                    <p>📍 {restaurant.address}</p>
                    {restaurant.rating && (
                      <div className="popup-rating">
                        ⭐ {restaurant.rating.toFixed(1)}
                      </div>
                    )}
                    <button
                      onClick={() => handleViewDetails(restaurant.id)}
                      className="btn-view-details"
                    >
                      View Details
                    </button>
                  </div>
                </Popup>
              </Marker>
            ))}
          </MapContainer>
        )}
      </div>
    </div>
  );
};

export default Map;