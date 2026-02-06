import React from 'react';
import { COMMON_ALLERGENS } from '../../types/allergen.types';
import './DishAllergenManager.css';

interface DishAllergenManagerProps {
  selectedAllergens: string[];
  onChange: (allergens: string[]) => void;
}

const DishAllergenManager: React.FC<DishAllergenManagerProps> = ({
  selectedAllergens,
  onChange,
}) => {
  const toggleAllergen = (allergen: string) => {
    const newAllergens = selectedAllergens.includes(allergen)
      ? selectedAllergens.filter(a => a !== allergen)
      : [...selectedAllergens, allergen];
    onChange(newAllergens);
  };

  return (
    <div className="dish-allergen-manager">
      <label className="allergen-section-label">
        🛡️ Allergen Information
      </label>
      <p className="allergen-helper-text">
        Select all allergens present in this dish. This helps customers make safe choices.
      </p>

      <div className="allergen-quick-grid">
        {COMMON_ALLERGENS.map((allergen) => {
          const isSelected = selectedAllergens.includes(allergen);
          return (
            <button
              key={allergen}
              type="button"
              onClick={() => toggleAllergen(allergen)}
              className={`allergen-quick-button ${isSelected ? 'selected' : ''}`}
            >
              <span className="allergen-check">
                {isSelected ? '✓' : ''}
              </span>
              <span className="allergen-label">{allergen}</span>
            </button>
          );
        })}
      </div>

      {selectedAllergens.length > 0 && (
        <div className="allergen-summary-box">
          <strong>Contains:</strong> {selectedAllergens.join(', ')}
        </div>
      )}
    </div>
  );
};

export default DishAllergenManager;