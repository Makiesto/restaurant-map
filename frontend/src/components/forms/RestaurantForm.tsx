import React, {useState, useEffect} from 'react';
import {apiService} from '../../services/api';
import type {Restaurant, CreateRestaurantRequest} from '../../types/restaurant.types';
import '../common/ModalForm.css';
import axios from "axios";

interface RestaurantFormProps {
    restaurant: Restaurant | null;
    onClose: (success: boolean) => void;
}

const RestaurantForm: React.FC<RestaurantFormProps> = ({restaurant, onClose}) => {
    const isEditing = !!restaurant;

    const [formData, setFormData] = useState<CreateRestaurantRequest>({
        name: '',
        address: '',
        phone: '',
        description: '',
        openingHours: '',
        cuisineType: '',
        priceRange: '',
        dietaryOptions: [],
    });

    const [errors, setErrors] = useState<{ [key: string]: string }>({});
    const [submitting, setSubmitting] = useState(false);

    const cuisineTypes = [
        'Italian', 'Chinese', 'Japanese', 'Mexican', 'Indian',
        'Thai', 'French', 'American', 'Mediterranean', 'Polish', 'Other'
    ];

    const dietaryOptionsList = [
        'Vegetarian', 'Vegan', 'Gluten-Free', 'Halal', 'Kosher', 'Dairy-Free'
    ];

    useEffect(() => {
        if (restaurant) {
            setFormData({
                name: restaurant.name,
                address: restaurant.address,
                phone: restaurant.phone || '',
                description: restaurant.description || '',
                openingHours: restaurant.openingHours || '',
                cuisineType: restaurant.cuisineType || '',
                priceRange: restaurant.priceRange || '',
                dietaryOptions: restaurant.dietaryOptions || [],
            });
        }
    }, [restaurant]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
        // Clear error for this field
        if (errors[e.target.name]) {
            setErrors({...errors, [e.target.name]: ''});
        }
    };

    const handleDietaryToggle = (option: string) => {
        const newOptions = formData.dietaryOptions?.includes(option)
            ? formData.dietaryOptions.filter(o => o !== option)
            : [...(formData.dietaryOptions || []), option];

        setFormData({
            ...formData,
            dietaryOptions: newOptions,
        });
    };

    const validate = (): boolean => {
        const newErrors: { [key: string]: string } = {};

        if (!formData.name.trim()) {
            newErrors.name = 'Restaurant name is required';
        }

        if (!formData.address.trim()) {
            newErrors.address = 'Address is required';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validate()) {
            return;
        }

        setSubmitting(true);

        try {
            if (isEditing && restaurant) {
                await apiService.updateRestaurant(restaurant.id, formData);
            } else {
                await apiService.createRestaurant(formData);
            }
            onClose(true);
        } catch (err: unknown) {
            let errorMessage = 'Failed to save restaurant';

            if (axios.isAxiosError(err)) {
                errorMessage = err.response?.data?.message || err.message || errorMessage;
            } else if (err instanceof Error) {
                errorMessage = err.message;
            }

            setErrors({general: errorMessage});
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="modal-form">
            {errors.general && (
                <div className="error-message">{errors.general}</div>
            )}

            <div className="form-group">
                <label htmlFor="name">Restaurant Name *</label>
                <input
                    type="text"
                    id="name"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    disabled={submitting}
                    placeholder="e.g., Joe's Pizza"
                />
                {errors.name && <span className="field-error">{errors.name}</span>}
            </div>

            <div className="form-group">
                <label htmlFor="address">Address *</label>
                <input
                    type="text"
                    id="address"
                    name="address"
                    value={formData.address}
                    onChange={handleChange}
                    disabled={submitting}
                    placeholder="e.g., 123 Main St, Kraków, Poland"
                />
                {errors.address && <span className="field-error">{errors.address}</span>}
            </div>

            <div className="form-row">
                <div className="form-group">
                    <label htmlFor="cuisineType">Cuisine Type</label>
                    <select
                        id="cuisineType"
                        name="cuisineType"
                        value={formData.cuisineType}
                        onChange={handleChange}
                        disabled={submitting}
                    >
                        <option value="">Select cuisine...</option>
                        {cuisineTypes.map(type => (
                            <option key={type} value={type}>{type}</option>
                        ))}
                    </select>
                </div>

                <div className="form-group">
                    <label htmlFor="priceRange">Price Range</label>
                    <select
                        id="priceRange"
                        name="priceRange"
                        value={formData.priceRange}
                        onChange={handleChange}
                        disabled={submitting}
                    >
                        <option value="">Select price...</option>
                        <option value="BUDGET">$ Budget</option>
                        <option value="MODERATE">$$ Moderate</option>
                        <option value="EXPENSIVE">$$$ Expensive</option>
                        <option value="LUXURY">$$$$ Luxury</option>
                    </select>
                </div>
            </div>

            <div className="form-group">
                <label htmlFor="phone">Phone Number</label>
                <input
                    type="tel"
                    id="phone"
                    name="phone"
                    value={formData.phone}
                    onChange={handleChange}
                    disabled={submitting}
                    placeholder="e.g., +48 123 456 789"
                />
            </div>

            <div className="form-group">
                <label htmlFor="openingHours">Opening Hours</label>
                <input
                    type="text"
                    id="openingHours"
                    name="openingHours"
                    value={formData.openingHours}
                    onChange={handleChange}
                    disabled={submitting}
                    placeholder="e.g., Mon-Fri: 9:00-22:00, Sat-Sun: 10:00-23:00"
                />
            </div>

            <div className="form-group">
                <label>Dietary Options</label>
                <div className="dietary-checkboxes">
                    {dietaryOptionsList.map(option => (
                        <label key={option} className="checkbox-label-inline">
                            <input
                                type="checkbox"
                                checked={formData.dietaryOptions?.includes(option)}
                                onChange={() => handleDietaryToggle(option)}
                                disabled={submitting}
                            />
                            <span>{option}</span>
                        </label>
                    ))}
                </div>
            </div>

            <div className="form-group">
                <label htmlFor="description">Description</label>
                <textarea
                    id="description"
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    disabled={submitting}
                    placeholder="Tell customers about your restaurant..."
                    rows={4}
                />
            </div>

            <div className="modal-actions">
                <button
                    type="button"
                    onClick={() => onClose(false)}
                    className="btn-cancel"
                    disabled={submitting}
                >
                    Cancel
                </button>
                <button
                    type="submit"
                    className="btn-submit"
                    disabled={submitting}
                >
                    {submitting ? 'Saving...' : isEditing ? 'Update Restaurant' : 'Add Restaurant'}
                </button>
            </div>
        </form>
    );
};

export default RestaurantForm;