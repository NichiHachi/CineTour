import React, { useState } from 'react';
import './StarRating.css';

const StarRating = ({ value, onChange, label }) => {
    const [hoveredStar, setHoveredStar] = useState(0);

    const handleClick = (rating) => {
        onChange(rating === value ? 0 : rating);
    };

    return (
        <div className="star-rating">
            {label && <label className="star-rating-label">{label}</label>}
            <div className="stars-container">
                {[1, 2, 3, 4, 5].map((star) => (
                    <span
                        key={star}
                        className={`star ${star <= (hoveredStar || value) ? 'filled' : ''}`}
                        onClick={() => handleClick(star)}
                        onMouseEnter={() => setHoveredStar(star)}
                        onMouseLeave={() => setHoveredStar(0)}
                    >
            ★
          </span>
                ))}
            </div>
        </div>
    );
};

export default StarRating;
