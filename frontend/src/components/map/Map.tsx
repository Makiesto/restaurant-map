import React, { useEffect, useState, useMemo } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { useNavigate } from 'react-router-dom';
import { apiService } from '../../services/api';
import type { Restaurant } from '../../types/restaurant.types';
import SearchFilters, {type FilterOptions } from './SearchFilters';
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
  const [filters, setFilters] = useState<FilterOptions>({
    searchQuery: '',
    cuisineType: '',
    priceRange: '',
    dietaryOptions: [],
    minRating: 0,
    sortBy: 'rating',
  });

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

  // Filter and sort restaurants
  const filteredRestaurants = useMemo(() => {
    let filtered = [...restaurants];

    // Search by name or address
    if (filters.searchQuery) {
      const query = filters.searchQuery.toLowerCase();
      filtered = filtered.filter(r =>
        r.name.toLowerCase().includes(query) ||
        r.address.toLowerCase().includes(query) ||
        r.description?.toLowerCase().includes(query)
      );
    }

    // Filter by cuisine type
    if (filters.cuisineType) {
      filtered = filtered.filter(r =>
        r.cuisineType?.toLowerCase() === filters.cuisineType.toLowerCase()
      );
    }

    // Filter by price range
    if (filters.priceRange) {
      filtered = filtered.filter(r => r.priceRange === filters.priceRange);
    }

    // Filter by dietary options (restaurant must have ALL selected options)
    if (filters.dietaryOptions.length > 0) {
      filtered = filtered.filter(r =>
        filters.dietaryOptions.every(option =>
          r.dietaryOptions?.includes(option)
        )
      );
    }

    // Filter by minimum rating
    if (filters.minRating > 0) {
      filtered = filtered.filter(r =>
        r.rating && r.rating >= filters.minRating
      );
    }

    // Sort
    switch (filters.sortBy) {
      case 'rating':
        filtered.sort((a, b) => (b.rating || 0) - (a.rating || 0));
        break;
      case 'name':
        filtered.sort((a, b) => a.name.localeCompare(b.name));
        break;
      case 'newest':
        filtered.sort((a, b) =>
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        break;
      case 'distance':
        // Would need user location for this
        // For now, keep current order
        break;
    }

    return filtered;
  }, [restaurants, filters]);

  const handleFilterChange = (newFilters: FilterOptions) => {
    setFilters(newFilters);
    // Reset selected restaurant if it's no longer in filtered results
    if (selectedRestaurant && !filteredRestaurants.find(r => r.id === selectedRestaurant.id)) {
      setSelectedRestaurant(null);
    }
  };

  const handleMarkerClick = (restaurant: Restaurant) => {
    setSelectedRestaurant(restaurant);
  };

  const handleViewDetails = (restaurantId: number) => {
    navigate(`/restaurant/${restaurantId}`);
  };

  const handleCardClick = (restaurant: Restaurant) => {
    // Navigate to restaurant details page
    navigate(`/restaurant/${restaurant.id}`);
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
        </div>

        {/* Search and Filters */}
        <SearchFilters
          onFilterChange={handleFilterChange}
          restaurantCount={filteredRestaurants.length}
        />

        {/* Restaurant List */}
        {filteredRestaurants.length === 0 ? (
          <div className="empty-state">
            <h3>No restaurants found</h3>
            <p>Try adjusting your filters</p>
          </div>
        ) : (
          <div className="restaurant-list">
            {filteredRestaurants.map((restaurant) => (
              <div
                key={restaurant.id}
                className={`restaurant-card ${
                  selectedRestaurant?.id === restaurant.id ? 'selected' : ''
                }`}
                onClick={() => handleCardClick(restaurant)}
                style={{ cursor: 'pointer' }}
              >
                <h3>{restaurant.name}</h3>

                {/* Cuisine Type */}
                {restaurant.cuisineType && (
                  <span className="cuisine-tag">
                    {restaurant.cuisineType}
                  </span>
                )}

                <p className="restaurant-address">
                  📍 {restaurant.address}
                </p>

                {/* Price Range */}
                {restaurant.priceRange && (
                  <p className="restaurant-address">
                    💰 {restaurant.priceRange === 'BUDGET' && '$ Budget'}
                    {restaurant.priceRange === 'MODERATE' && '$$ Moderate'}
                    {restaurant.priceRange === 'EXPENSIVE' && '$$$ Expensive'}
                    {restaurant.priceRange === 'LUXURY' && '$$$$ Luxury'}
                  </p>
                )}

                {/* Rating */}
                {restaurant.rating && (
                  <div className="restaurant-rating">
                    ⭐ {restaurant.rating.toFixed(1)}
                  </div>
                )}

                {/* Dietary Options Preview */}
                {restaurant.dietaryOptions && restaurant.dietaryOptions.length > 0 && (
                  <p className="restaurant-address">
                    🌿 {restaurant.dietaryOptions.slice(0, 2).join(', ')}
                    {restaurant.dietaryOptions.length > 2 && ' +more'}
                  </p>
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

            {filteredRestaurants.map((restaurant) => (
              <Marker
                key={restaurant.id}
                position={[restaurant.latitude, restaurant.longitude]}
                eventHandlers={{
                  click: () => handleMarkerClick(restaurant),
                }}
              >
                <Popup>
                  <div className="popup-content">
                    <h3>{restaurant.name}</h3>

                    {restaurant.cuisineType && (
                      <p>🍽️ {restaurant.cuisineType}</p>
                    )}

                    <p>📍 {restaurant.address}</p>

                    {restaurant.rating && (
                      <div className="popup-rating">
                        ⭐ {restaurant.rating.toFixed(1)}
                      </div>
                    )}

                    {restaurant.priceRange && (
                      <p>
                        💰 {restaurant.priceRange === 'BUDGET' && '$ Budget'}
                        {restaurant.priceRange === 'MODERATE' && '$$ Moderate'}
                        {restaurant.priceRange === 'EXPENSIVE' && '$$$ Expensive'}
                        {restaurant.priceRange === 'LUXURY' && '$$$$ Luxury'}
                      </p>
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