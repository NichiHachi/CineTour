// three-graticule.js

import React, { useEffect, useRef, useState } from "react";
import GeoJsonGeometry from "three-geojson-geometry";
import { geoGraticule10 } from "d3-geo";

const ThreeGraticule = () => {
  const [color, setColor] = useState("#000");
  const colorRef = useRef(null);

  // Update color when prefers-color-scheme changes
  useEffect(() => {
    const updateColor = () => {
      const cssColor = getComputedStyle(document.documentElement)
        .getPropertyValue("--color-text-muted")
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
    <group>
      <lineSegments geometry={new GeoJsonGeometry(geoGraticule10(), 1)}>
        <lineBasicMaterial
          color={color}
          transparent={true}
          opacity={0.5}
          ref={colorRef}
        />
      </lineSegments>
    </group>
  );
};

export default ThreeGraticule;
