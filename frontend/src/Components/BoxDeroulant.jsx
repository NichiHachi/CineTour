import React, { useState } from "react";

const BoxDeroulant = ({ name, data, goToMarqueur }) => {
  const [visible, isVisible] = useState(false);

  const handleClick = (e) => {
    isVisible(!visible);
    goToMarqueur(data.coordinatesCountry);
  };

  console.log(name, data);

  return (
    <div style={{ border: "1px solid white", color: "white" }}>
      <div onClick={handleClick} style={{ border: "1px solid white" }}>
        {name}
      </div>
      {visible &&
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
