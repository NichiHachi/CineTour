// three-graticule.js

import React, { useEffect, useState } from "react";
import GeoJsonGeometry from "three-geojson-geometry";
import { geoGraticule10 } from "d3-geo";

const ThreeGraticule = () => {
  const [color, setColor] = useState("#000");

  useEffect(() => {
    const cssColor = getComputedStyle(document.documentElement)
      .getPropertyValue("--color-text-muted")
      .trim();
    setColor(cssColor);
  }, []);

  return (
    <group>
      <lineSegments geometry={new GeoJsonGeometry(geoGraticule10(), 1)}>
        <lineBasicMaterial color={color} transparent={true} opacity={0.5} />
      </lineSegments>
    </group>
  );
};

export default ThreeGraticule;
