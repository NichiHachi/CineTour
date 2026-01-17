// three-points.js

import React, { useEffect, useState, useMemo } from "react";

const ThreePoints = ({ points }) => {
  const [color, setColor] = useState("#ff0000");

  useEffect(() => {
    const cssColor = getComputedStyle(document.documentElement)
      .getPropertyValue("--color-point")
      .trim();
    setColor(cssColor);
  }, []);

  const pointMeshes = useMemo(() => {
    return points.map(({ latitude, longitude }, index) => {
      const radius = 1;
      const phi = (90 - latitude) * (Math.PI / 180);
      const theta = (longitude + 90) * (Math.PI / 180);

      const x = -radius * Math.sin(phi) * Math.cos(theta);
      const y = radius * Math.cos(phi);
      const z = radius * Math.sin(phi) * Math.sin(theta);

      return (
        <mesh key={index} position={[x, y, z]}>
          <sphereGeometry args={[0.008, 16, 16]} />
          <meshBasicMaterial color={color} />
        </mesh>
      );
    });
  }, [points, color]);

  return <group>{pointMeshes}</group>;
};

export default ThreePoints;
