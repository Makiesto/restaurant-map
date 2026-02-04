import React, {useState, useEffect} from 'react';
import {apiService} from '../../services/api.ts';
import type {Dish, CreateDishRequest} from '../../types/dish.types.ts';
import '../common/ModalForm.css';
import axios from "axios";

interface DishFormProps {
    dish: Dish | null;
    restaurantId: number;
    onClose: (success: boolean) => void | Promise<void>;
}

const DishForm: React.FC<DishFormProps> = ({dish, restaurantId, onClose}) => {
    const isEditing = !!dish;

    const [formData, setFormData] = useState<CreateDishRequest>({
        name: '',
        description: '',
        price: 0,
        imageUrl: '',
        isAvailable: true,
        components: [],
    });

    const [errors, setErrors] = useState<{ [key: string]: string }>({});
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        if (dish) {
            setFormData({
                name: dish.name,
                description: dish.description || '',
                price: dish.price,
                isAvailable: dish.isAvailable,
                imageUrl: dish.imageUrl || '',
                components: dish.components?.map(c => ({
                    componentId: c.componentId,
                    amount: c.amount,
                    isOptional: c.isOptional,
                })) || [],
            });
        }
    }, [dish]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const value = e.target.type === 'number' ? parseFloat(e.target.value) : e.target.value;
        setFormData({
            ...formData,
            [e.target.name]: value,
        });
        // Clear error for this field
        if (errors[e.target.name]) {
            setErrors({...errors, [e.target.name]: ''});
        }
    };

    const validate = (): boolean => {
        const newErrors: { [key: string]: string } = {};

        if (!formData.name.trim()) {
            newErrors.name = 'Dish name is required';
        }

        if (!formData.price || formData.price <= 0) {
            newErrors.price = 'Price must be greater than 0';
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
            if (isEditing && dish) {
                await apiService.updateDish(dish.id, formData);
            } else {
                await apiService.createDish(restaurantId, formData);
            }
            onClose(true);
        } catch (err: unknown) {
            let errorMessage = 'Failed to save dish';

            if (axios.isAxiosError(err)) {
                errorMessage = err.response?.data?.message || err.message || errorMessage;
            } else if (err instanceof Error) {
                errorMessage = err.message;
            }

            setErrors({general: errorMessage});
            console.error('Submission error:', err);
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
                <label htmlFor="name">Dish Name *</label>
                <input
                    type="text"
                    id="name"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    disabled={submitting}
                    placeholder="e.g., Margherita Pizza"
                />
                {errors.name && <span className="field-error">{errors.name}</span>}
            </div>

            <div className="form-group">
                <label htmlFor="price">Price *</label>
                <input
                    type="number"
                    id="price"
                    name="price"
                    value={formData.price}
                    onChange={handleChange}
                    disabled={submitting}
                    placeholder="e.g., 12.99"
                    step="0.01"
                    min="0"
                />
                {errors.price && <span className="field-error">{errors.price}</span>}
            </div>

            <div className="form-group">
                <label htmlFor="description">Description</label>
                <textarea
                    id="description"
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    disabled={submitting}
                    placeholder="Describe the dish, ingredients, etc."
                    rows={3}
                />
            </div>

            <div className="form-group">
                <label htmlFor="imageUrl">Image URL</label>
                <input
                    type="url"
                    id="imageUrl"
                    name="imageUrl"
                    value={formData.imageUrl}
                    onChange={handleChange}
                    disabled={submitting}
                    placeholder="https://example.com/image.jpg"
                />
                <small className="help-text">
                    Optional: Add a link to an image of your dish
                </small>
            </div>

            <div className="info-box">
                <strong>📝 Note:</strong> Nutritional information and components can be added later.
                For now, focus on the basic details to get your dish listed!
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
                    {submitting ? 'Saving...' : isEditing ? 'Update Dish' : 'Add Dish'}
                </button>
            </div>
        </form>
    );
};

export default DishForm;