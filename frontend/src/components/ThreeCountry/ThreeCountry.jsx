// three-countries.js

import React, { useEffect, useState } from "react";
import GeoJsonGeometry from "three-geojson-geometry";

import geoJson from "./countries.json";

const ThreeCountries = () => {
  const [color, setColor] = useState("#000");

  useEffect(() => {
    const cssColor = getComputedStyle(document.documentElement)
      .getPropertyValue("--color-text")
      .trim();
    setColor(cssColor);
  }, []);

  return (
    <group>
      {geoJson.features.map((data, index) => {
        const { geometry } = data;
        return (
          <lineSegments key={index} geometry={new GeoJsonGeometry(geometry, 1)}>
            <lineBasicMaterial color={color} />
          </lineSegments>
        );
      })}
    </group>
  );
};

export default ThreeCountries;
