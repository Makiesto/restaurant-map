import React from 'react';
import './AllergenWarning.css';

interface AllergenWarningProps {
  dishAllergens: string[];
  userAllergens: string[];
  compact?: boolean;
}

const AllergenWarning: React.FC<AllergenWarningProps> = ({
  dishAllergens,
  userAllergens,
  compact = false
}) => {
  const matchingAllergens = dishAllergens.filter(allergen =>
    userAllergens.includes(allergen)
  );

  if (matchingAllergens.length === 0) {
    return null;
  }

  if (compact) {
    return (
      <div className="allergen-warning-compact">
        <span className="warning-icon">⚠️</span>
        <span className="warning-count">{matchingAllergens.length} allergen{matchingAllergens.length > 1 ? 's' : ''}</span>
      </div>
    );
  }

  return (
    <div className="allergen-warning">
      <div className="warning-header">
        <span className="warning-icon-large">⚠️</span>
        <div>
          <h4>Allergen Warning</h4>
          <p>This dish contains allergens you want to avoid</p>
        </div>
      </div>
      <div className="allergen-list">
        {matchingAllergens.map((allergen, index) => (
          <span key={index} className="allergen-tag-warning">
            {allergen}
          </span>
        ))}
      </div>
    </div>
  );
};

export default AllergenWarning;