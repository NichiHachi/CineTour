import React, { useState } from "react";

const BoxDeroulant = ({ name, data, goToMarqueur, onToggle, isVisible }) => {
  const handleClick = (e) => {
    onToggle(name);
    goToMarqueur(data.coordinatesCountry, 5);
  };

  return (
      <div style={{ border: "1px solid white", color: "white" }}>
        <div onClick={handleClick} style={{ border: "1px solid white" }}>
          {name}
        </div>
        {isVisible &&
            Object.keys(data)
                .filter((key) => key !== "coordinatesCountry")
                .map((key, index) => (
                    <div
                        key={index}
                        style={{ border: "1px solid white" }}
                        onClick={() => goToMarqueur(data[key].coordinates)}
                    >
                      {data[key].address}
                    </div>
                ))}
      </div>
  );
};

export default BoxDeroulant;