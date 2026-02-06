import React, { useState, useEffect } from 'react';
import { apiService } from '../../services/api';
import { COMMON_ALLERGENS } from '../../types/allergen.types';
import './AllergenManager.css';

interface AllergenManagerProps {
  onClose: () => void;
}

const AllergenManager: React.FC<AllergenManagerProps> = ({ onClose }) => {
  const [selectedAllergens, setSelectedAllergens] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    loadUserAllergens();
  }, []);

  const loadUserAllergens = async () => {
    try {
      setLoading(true);
      const allergens = await apiService.getUserAllergens();
      setSelectedAllergens(allergens.map(a => a.name));
    } catch (err) {
      console.error('Failed to load allergens:', err);
      // Start with empty if loading fails
      setSelectedAllergens([]);
    } finally {
      setLoading(false);
    }
  };

  const toggleAllergen = (allergen: string) => {
    setSelectedAllergens(prev =>
      prev.includes(allergen)
        ? prev.filter(a => a !== allergen)
        : [...prev, allergen]
    );
  };

  const handleSave = async () => {
    try {
      setSaving(true);
      setError('');
      await apiService.updateUserAllergens(selectedAllergens);
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save allergen preferences');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="allergen-manager-loading">
        <div className="spinner"></div>
        <p>Loading your allergen preferences...</p>
      </div>
    );
  }

  return (
    <div className="allergen-manager">
      <div className="allergen-header">
        <div>
          <h2>🛡️ Manage Allergens</h2>
          <p className="allergen-subtitle">
            Select allergens you want to avoid. We'll warn you when viewing dishes that contain them.
          </p>
        </div>
      </div>

      {error && (
        <div className="allergen-error">
          ⚠️ {error}
        </div>
      )}

      <div className="allergen-info-box">
        <strong>ℹ️ How it works:</strong>
        <ul>
          <li>Select all allergens you need to avoid</li>
          <li>Dishes containing your allergens will show clear warnings</li>
          <li>You can update your preferences anytime</li>
        </ul>
      </div>

      <div className="allergen-grid">
        {COMMON_ALLERGENS.map((allergen) => {
          const isSelected = selectedAllergens.includes(allergen);
          return (
            <button
              key={allergen}
              onClick={() => toggleAllergen(allergen)}
              className={`allergen-button ${isSelected ? 'selected' : ''}`}
              disabled={saving}
            >
              <span className="allergen-icon">
                {isSelected ? '✓' : '○'}
              </span>
              <span className="allergen-name">{allergen}</span>
            </button>
          );
        })}
      </div>

      <div className="allergen-summary">
        <strong>Selected:</strong> {selectedAllergens.length > 0 ? selectedAllergens.join(', ') : 'None'}
      </div>

      <div className="allergen-actions">
        <button
          onClick={onClose}
          className="btn-cancel"
          disabled={saving}
        >
          Cancel
        </button>
        <button
          onClick={handleSave}
          className="btn-save"
          disabled={saving}
        >
          {saving ? 'Saving...' : 'Save Preferences'}
        </button>
      </div>
    </div>
  );
};

export default AllergenManager;