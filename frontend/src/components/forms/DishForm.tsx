import React, {useState, useEffect} from 'react';
import {apiService} from '../../services/api.ts';
import type {Dish, CreateDishRequest} from '../../types/dish.types.ts';
import ImageUploadField from './ImageUploadField';
import DishAllergenManager from '../allergens/DishAllergenManager';
import IngredientManager from '../nutrition/IngredientManager';
import {type Ingredient, nutritionApiService } from '../../services/nutrionApi';
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
        allergens: [],
        baseKcal: 0,
        baseProteinG: 0,
        baseCarbsG: 0,
        baseFatG: 0,
        ingredients: [],
    });

    const [ingredients, setIngredients] = useState<Ingredient[]>([]);
    const [errors, setErrors] = useState<{ [key: string]: string }>({});
    const [submitting, setSubmitting] = useState(false);
    const [showNutrition, setShowNutrition] = useState(false);

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
                allergens: dish.allergens || [],
                baseKcal: dish.baseKcal || 0,
                baseProteinG: dish.baseProteinG || 0,
                baseCarbsG: dish.baseCarbsG || 0,
                baseFatG: dish.baseFatG || 0,
                ingredients: dish.ingredients || [],
            });

            // Load saved ingredients if available
            if (dish.ingredients) {
                setIngredients(dish.ingredients);
                setShowNutrition(true);
            }
        }
    }, [dish]);

    // Auto-update nutrition when ingredients change
    useEffect(() => {
        if (ingredients.length > 0) {
            const totalNutrition = nutritionApiService.calculateTotalNutrition(ingredients);

            setFormData(prev => ({
                ...prev,
                baseKcal: totalNutrition.calories,
                baseProteinG: totalNutrition.protein,
                baseCarbsG: totalNutrition.carbs,
                baseFatG: totalNutrition.fat,
            }));
        } else {
            // Reset nutrition if no ingredients
            setFormData(prev => ({
                ...prev,
                baseKcal: 0,
                baseProteinG: 0,
                baseCarbsG: 0,
                baseFatG: 0,
            }));
        }
    }, [ingredients]);

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

    const handleImageChange = (url: string) => {
        setFormData({
            ...formData,
            imageUrl: url,
        });
        if (errors.imageUrl) {
            setErrors({...errors, imageUrl: ''});
        }
    };

    const handleAllergenChange = (allergens: string[]) => {
        setFormData(prev => ({
            ...prev,
            allergens,
        }));
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
            // Prepare dish data with ingredients for saving
            const dishData = {
                ...formData,
                // Save ingredients as a simplified format (without full nutrition object)
                ingredients: ingredients.map(ing => ({
                    id: ing.id,
                    name: ing.name,
                    amount: ing.amount,
                    unit: ing.unit,
                    fdcId: ing.fdcId,
                })),
            };

            if (isEditing && dish) {
                await apiService.updateDish(dish.id, dishData);
            } else {
                await apiService.createDish(restaurantId, dishData);
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
            <div className="form-header">
                <h2>{isEditing ? 'Edit Dish' : 'Add New Dish'}</h2>
                <button
                    type="button"
                    onClick={() => onClose(false)}
                    className="btn-close-modal"
                    disabled={submitting}
                >
                    ✕
                </button>
            </div>

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

            {/* Image Upload */}
            <ImageUploadField
                label="Dish Image"
                value={formData.imageUrl || ''}
                onChange={handleImageChange}
                folder="dishes"
                disabled={submitting}
            />

            {/* Allergen Manager */}
            <DishAllergenManager
                selectedAllergens={formData.allergens}
                onChange={handleAllergenChange}
            />

            {/* Nutrition Calculator Section */}
            <div className="nutrition-section">
                <div className="section-toggle">
                    <button
                        type="button"
                        onClick={() => setShowNutrition(!showNutrition)}
                        className="btn-toggle-section"
                    >
                        {showNutrition ? '▼' : '▶'} Nutrition Calculator (Optional)
                        {ingredients.length > 0 && (
                            <span className="ingredient-count"> • {ingredients.length} ingredient{ingredients.length !== 1 ? 's' : ''}</span>
                        )}
                    </button>
                </div>

                {showNutrition && (
                    <div className="nutrition-content">
                        <IngredientManager
                            ingredients={ingredients}
                            onIngredientsChange={setIngredients}
                        />

                        {ingredients.length > 0 && (
                            <div className="calculated-nutrition-display">
                                <h4>✅ Auto-Calculated Nutrition (will be saved with dish)</h4>
                                <div className="nutrition-preview">
                                    <div className="nutrition-item-preview">
                                        <span className="label">Calories:</span>
                                        <span className="value">{Math.round(formData.baseKcal || 0)} kcal</span>
                                    </div>
                                    <div className="nutrition-item-preview">
                                        <span className="label">Protein:</span>
                                        <span className="value">{(formData.baseProteinG || 0).toFixed(1)}g</span>
                                    </div>
                                    <div className="nutrition-item-preview">
                                        <span className="label">Carbs:</span>
                                        <span className="value">{(formData.baseCarbsG || 0).toFixed(1)}g</span>
                                    </div>
                                    <div className="nutrition-item-preview">
                                        <span className="label">Fat:</span>
                                        <span className="value">{(formData.baseFatG || 0).toFixed(1)}g</span>
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </div>

            {!showNutrition && (
                <div className="info-box">
                    <strong>💡 Tip:</strong> Click "Nutrition Calculator" above to automatically calculate calories and macros by adding ingredients!
                </div>
            )}

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