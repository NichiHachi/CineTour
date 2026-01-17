// three-mesh.js

import React, { useEffect, useState } from "react";

const ThreeMesh = () => {
  const [color, setColor] = useState("#000");

  useEffect(() => {
    const cssColor = getComputedStyle(document.documentElement)
      .getPropertyValue("--color-bg-light")
      .trim();
    setColor(cssColor);
  }, []);

  return (
    <mesh>
      <sphereGeometry args={[1, 64, 64]} />
      <meshPhongMaterial color={color} transparent={false} opacity={0.1} />
    </mesh>
  );
};

export default ThreeMesh;
