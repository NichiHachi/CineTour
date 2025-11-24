import {useState, useRef, useEffect} from 'react';
import './MultiSelectDropdown.css';

const MultiSelectDropdown = ({
                                 options = [],
                                 selectedValues = [],
                                 onChange,
                                 placeholder = "Sélectionner...",
                                 label
                             }) => {
    const [isOpen, setIsOpen] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const dropdownRef = useRef(null);

    // Filtrer les options en fonction du terme de recherche
    const filteredOptions = options.filter(option =>
        option.toLowerCase().includes(searchTerm.toLowerCase())
    );

    // Gérer la sélection/désélection des options
    const handleToggle = (value) => {
        const newSelected = selectedValues.includes(value)
            ? selectedValues.filter(v => v !== value)
            : [...selectedValues, value];
        onChange(newSelected);
    };

    // Gérer la suppression d'une valeur sélectionnée
    const handleRemove = (value) => {
        onChange(selectedValues.filter(v => v !== value));
    };

    // Fermer le dropdown si on clique en dehors
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    return (
        <div className="multi-select-container" ref={dropdownRef}>
            {label && <label className="multi-select-label">{label}</label>}
            {/* Le champ d'entrée pour afficher les valeurs sélectionnées */}
            <div className="multi-select-input" onClick={() => setIsOpen(!isOpen)}>
                {/* Afficher les valeurs sélectionnées ou le placeholder */}
                <div className="selected-values">
                    {selectedValues.length === 0 ? (
                        <span className="placeholder">{placeholder}</span>
                    ) : (
                        selectedValues.map(value => (
                            <span key={value} className="selected-tag">
                                {value}
                                <button
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        handleRemove(value);
                                    }}
                                    className="remove-btn"
                                >
                                    ×
                                </button>
                            </span>
                        ))
                    )}
                </div>
                {/* Icône de flèche pour indiquer l'état du dropdown */}
                <span className={`arrow ${isOpen ? 'open' : ''}`}>▼</span>
            </div>

            {isOpen && (
                <div className="dropdown-menu">
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Rechercher..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        onClick={(e) => e.stopPropagation()}
                    />
                    {/* Liste des options filtrées */}
                    <div className="options-list">
                        {filteredOptions.length === 0 ? (
                            <div className="no-results">Aucun résultat</div>
                        ) : (
                            filteredOptions.map(option => (
                                <div
                                    key={option}
                                    className={`option ${selectedValues.includes(option) ? 'selected' : ''}`}
                                    onClick={() => handleToggle(option)}
                                >
                                    <input
                                        type="checkbox"
                                        checked={selectedValues.includes(option)}
                                        readOnly
                                    />
                                    <span>{option}</span>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default MultiSelectDropdown;
