import React from 'react';
import './AllergenBadges.css';

interface AllergenBadgesProps {
  allergens: string[];
  userAllergens?: string[];
  showWarning?: boolean;
}

const AllergenBadges: React.FC<AllergenBadgesProps> = ({
  allergens,
  userAllergens = [],
  showWarning = true
}) => {
  if (!allergens || allergens.length === 0) {
    return null;
  }

  const hasUserAllergens = allergens.some(a => userAllergens.includes(a));

  return (
    <div className="allergen-badges-container">
      <div className="allergen-badges-label">Contains:</div>
      <div className="allergen-badges">
        {allergens.map((allergen, index) => {
          const isUserAllergen = userAllergens.includes(allergen);
          return (
            <span
              key={index}
              className={`allergen-badge ${isUserAllergen ? 'user-allergen' : ''}`}
              title={isUserAllergen ? 'You marked this as an allergen to avoid' : ''}
            >
              {isUserAllergen && <span className="badge-warning-icon">⚠️</span>}
              {allergen}
            </span>
          );
        })}
      </div>
      {showWarning && hasUserAllergens && (
        <div className="allergen-user-warning">
          ⚠️ Contains allergens you want to avoid
        </div>
      )}
    </div>
  );
};

export default AllergenBadges;