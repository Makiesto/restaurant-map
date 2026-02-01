import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import { apiService } from '../services/api';
import type { Restaurant } from '../types/restaurant.types';
import 'leaflet/dist/leaflet.css';
import './Map.css';

// Fix for default marker icons in React-Leaflet
const defaultIconPrototype = L.Icon.Default.prototype as L.Icon.Default & { _getIconUrl?: string };
delete defaultIconPrototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

// Custom restaurant marker icon
const restaurantIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

// Component to handle map centering
const MapController: React.FC<{ center: [number, number] }> = ({ center }) => {
  const map = useMap();

  useEffect(() => {
    map.setView(center, map.getZoom());
  }, [center, map]);

  return null;
};

const Map: React.FC = () => {
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedRestaurant, setSelectedRestaurant] = useState<Restaurant | null>(null);
  const [mapCenter, setMapCenter] = useState<[number, number]>([50.0647, 19.9450]); // Krakow, Poland

  useEffect(() => {
    fetchRestaurants();
    getUserLocation();
  }, []);

  const getUserLocation = () => {
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setMapCenter([position.coords.latitude, position.coords.longitude]);
        },
        (error) => {
          console.error('Location access error:', error);
          console.log('Using default location (Kraków)');
        }
      );
    }
  };

  const fetchRestaurants = async () => {
    try {
      setLoading(true);
      const data = await apiService.getApprovedRestaurants();
      setRestaurants(data);
      setError(null);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load restaurants';
      setError(message);
      console.error('Error fetching restaurants:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkerClick = (restaurant: Restaurant) => {
    setSelectedRestaurant(restaurant);
    setMapCenter([restaurant.latitude, restaurant.longitude]);
  };

  const handleViewDetails = (restaurantId: number) => {
    window.location.href = `/restaurant/${restaurantId}`;
  };

  if (loading) {
    return (
      <div className="map-loading">
        <div className="spinner"></div>
        <p>Loading restaurants...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="map-error">
        <p>{error}</p>
        <button onClick={fetchRestaurants} className="btn-retry">
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="map-wrapper">
      <div className="map-sidebar">
        <div className="sidebar-header">
          <h2>🍽️ Restaurants</h2>
          <p className="restaurant-count">{restaurants.length} locations</p>
        </div>

        <div className="restaurant-list">
          {restaurants.length === 0 ? (
            <div className="no-restaurants">
              <p>No restaurants found</p>
              <p className="text-muted">Be the first to add one!</p>
            </div>
          ) : (
            restaurants.map((restaurant) => (
              <div
                key={restaurant.id}
                className={`restaurant-card ${selectedRestaurant?.id === restaurant.id ? 'active' : ''}`}
                onClick={() => handleMarkerClick(restaurant)}
              >
                <div className="restaurant-info">
                  <h3>{restaurant.name}</h3>
                  <p className="address">📍 {restaurant.address}</p>
                  {restaurant.phone && (
                    <p className="phone">📞 {restaurant.phone}</p>
                  )}
                  {restaurant.rating && (
                    <div className="rating">
                      ⭐ {restaurant.rating.toFixed(1)}
                    </div>
                  )}
                </div>
                <button
                  className="btn-view-details"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleViewDetails(restaurant.id);
                  }}
                >
                  View Menu →
                </button>
              </div>
            ))
          )}
        </div>
      </div>

      <div className="map-container">
        <MapContainer
          center={mapCenter}
          zoom={13}
          scrollWheelZoom={true}
          style={{ height: '100%', width: '100%' }}
        >
          <MapController center={mapCenter} />

          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />

          {restaurants.map((restaurant) => (
            <Marker
              key={restaurant.id}
              position={[restaurant.latitude, restaurant.longitude]}
              icon={restaurantIcon}
              eventHandlers={{
                click: () => handleMarkerClick(restaurant),
              }}
            >
              <Popup>
                <div className="marker-popup">
                  <h3>{restaurant.name}</h3>
                  <p className="popup-address">{restaurant.address}</p>
                  {restaurant.description && (
                    <p className="popup-description">{restaurant.description}</p>
                  )}
                  {restaurant.openingHours && (
                    <p className="popup-hours">
                      <strong>Hours:</strong> {restaurant.openingHours}
                    </p>
                  )}
                  {restaurant.phone && (
                    <p className="popup-phone">
                      <strong>Phone:</strong> {restaurant.phone}
                    </p>
                  )}
                  <button
                    className="btn-popup-details"
                    onClick={() => handleViewDetails(restaurant.id)}
                  >
                    View Full Menu
                  </button>
                </div>
              </Popup>
            </Marker>
          ))}
        </MapContainer>
      </div>
    </div>
  );
};

export default Map;