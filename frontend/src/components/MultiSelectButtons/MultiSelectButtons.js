import './MultiSelectButtons.css';

const MultiSelectButtons = ({
                                options = [],
                                selectedValues = [],
                                onChange,
                                label
                            }) => {
    // Fonction pour gérer la sélection/désélection d'une valeur
    const handleToggle = (value) => {
        const newSelected = selectedValues.includes(value)
            ? selectedValues.filter(v => v !== value)
            : [...selectedValues, value];
        onChange(newSelected);
    };

    // Fonction pour gérer la suppression d'une valeur sélectionnée
    const handleRemove = (value) => {
        onChange(selectedValues.filter(v => v !== value));
    };

    return (
        <div className="multi-select-buttons-container">
            {label && <label className="multi-select-buttons-label">{label}</label>}
            {/* Bannière affichant les éléments sélectionnés */}
            {selectedValues.length > 0 && (
                <div className="selected-banner">
                    <span className="selected-count">
                        {selectedValues.length} sélectionné{selectedValues.length > 1 ? 's' : ''}
                    </span>
                    {/* Liste des éléments sélectionnés avec bouton de suppression */}
                    <div className="selected-tags">
                        {selectedValues.map(value => (
                            <span key={value} className="selected-tag">
                                {value}
                                <button
                                    onClick={() => handleRemove(value)}
                                    className="remove-btn"
                                    aria-label={`Supprimer ${value}`}
                                >
                                    ×
                                </button>
                            </span>
                        ))}
                    </div>
                </div>
            )}

            <div className="buttons-grid">
                {options.map(option => (
                    <button
                        key={option}
                        className={`select-button ${selectedValues.includes(option) ? 'selected' : ''}`}
                        onClick={() => handleToggle(option)}
                    >
                        {option}
                    </button>
                ))}
            </div>
        </div>
    );
};

export default MultiSelectButtons;
