import "./MultiSelectButtons.css";
import Glow from "../Glow/Glow";

const MultiSelectButtons = ({
  options = [],
  selectedValues = [],
  onChange,
  label,
}) => {
  // Fonction pour gérer la sélection/désélection d'une valeur
  const handleToggle = (value) => {
    const newSelected = selectedValues.includes(value)
      ? selectedValues.filter((v) => v !== value)
      : [...selectedValues, value];
    onChange(newSelected);
  };

  return (
    <div className="multi-select-buttons-content">
      {label && <label className="multi-select-buttons-label">{label}</label>}
      <div className="buttons-grid">
        {options.map((option) => (
          <button
            key={option}
            className={`select-button ${selectedValues.includes(option) ? "selected" : ""}`}
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
