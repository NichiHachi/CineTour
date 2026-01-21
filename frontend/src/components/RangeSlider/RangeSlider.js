import React, { useState, useRef, useEffect } from "react";
import "./RangeSlider.css";
import Glow from "../Glow/Glow";

const RangeSlider = ({
  min = 1900,
  max = 2100,
  step = 1,
  value, // Expects [minValue, maxValue]
  onChange,
  label = "Range",
}) => {
  const [minValue, setMinValue] = useState(
    value && value[0] != null ? value[0] : min,
  );
  const [maxValue, setMaxValue] = useState(
    value && value[1] != null ? value[1] : max,
  );

  const minRef = useRef(null);
  const maxRef = useRef(null);
  const rangeRef = useRef(null);
  const debounceTimer = useRef(null);

  // Update local state when prop changes
  useEffect(() => {
    if (value) {
      setMinValue(Number(value[0]) || min);
      setMaxValue(Number(value[1]) || max);
    }
  }, [value, max, min]);

  // Update the visual range indicator
  useEffect(() => {
    const updateRange = () => {
      if (rangeRef.current) {
        const percentMin = ((minValue - min) / (max - min)) * 100;
        const percentMax = ((maxValue - min) / (max - min)) * 100;
        rangeRef.current.style.left = `${percentMin}%`;
        rangeRef.current.style.width = `${percentMax - percentMin}%`;
      }
    };
    updateRange();
  }, [minValue, maxValue, min, max]);

  // Debounced onChange callback
  const triggerChange = (newMin, newMax) => {
    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current);
    }

    debounceTimer.current = setTimeout(() => {
      if (onChange) {
        onChange([newMin, newMax]); // Return as array
      }
    }, 1000);
  };

  // Handle min value change
  const handleMinChange = (e) => {
    const value = Math.min(Number(e.target.value), maxValue - step);
    setMinValue(value);
    triggerChange(value, maxValue);
  };

  // Handle max value change
  const handleMaxChange = (e) => {
    const value = Math.max(Number(e.target.value), minValue + step);
    setMaxValue(value);
    triggerChange(minValue, value);
  };

  return (
    <div className="range-slider-content">
      <div className="range-slider-header">
        <span className="range-slider-label">{label}</span>
        <span className="range-slider-values">
          {minValue} - {maxValue}
        </span>
      </div>

      <div className="range-slider-wrapper">
        <div className="range-slider-track">
          <div className="range-slider-range" ref={rangeRef}></div>
        </div>

        <input
          type="range"
          ref={minRef}
          min={min}
          max={max}
          step={step}
          value={minValue}
          onChange={handleMinChange}
          className="range-slider-input range-slider-input-min"
        />

        <input
          type="range"
          ref={maxRef}
          min={min}
          max={max}
          step={step}
          value={maxValue}
          onChange={handleMaxChange}
          className="range-slider-input range-slider-input-max"
        />
      </div>
    </div>
  );
};

export default RangeSlider;
