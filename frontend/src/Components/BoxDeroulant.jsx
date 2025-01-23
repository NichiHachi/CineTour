import React, { useState } from "react";

const BoxDeroulant = ({ name, data }) => {
  const [visible, isVisible] = useState(false);

  const handleClick = (e) => {
    isVisible(!visible);
  };

  return (
    <div style={{ border: "1px solid white", color: "white" }}>
      <div onClick={handleClick} style={{ border: "1px solid white" }}>
        {name}
      </div>
      {visible &&
        data.map((item, index) => (
          <div key={index} style={{ border: "1px solid white" }}>
            {item}
          </div>
        ))}
    </div>
  );
};

export default BoxDeroulant;
