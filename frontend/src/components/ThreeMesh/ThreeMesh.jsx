// three-mesh.js

import React, { useRef, useState, useEffect } from "react";

const ThreeMesh = () => {
  const [color, setColor] = useState("#000");
  const colorRef = useRef(null);

  // Update color when prefers-color-scheme changes
  useEffect(() => {
    const updateColor = () => {
      const cssColor = getComputedStyle(document.documentElement)
        .getPropertyValue("--color-bg-light")
        .trim();
      setColor(cssColor);
    };

    const mq = window.matchMedia("(prefers-color-scheme: dark)");

    updateColor();
    mq.addEventListener("change", updateColor);

    return () => mq.removeEventListener("change", updateColor);
  }, []);

  // Update Three.js lights when color changes
  useEffect(() => {
    if (colorRef.current) colorRef.current.color.set(color);
  }, [color]);

  return (
    <mesh>
      <sphereGeometry args={[1, 64, 64]} />
      <meshPhongMaterial
        color={color}
        transparent={false}
        opacity={0.1}
        ref={colorRef}
      />
    </mesh>
  );
};

export default ThreeMesh;
