// three-countries.js

import React, { useEffect, useState, useRef } from "react";
import GeoJsonGeometry from "three-geojson-geometry";

import geoJson from "./countries.json";

const ThreeCountries = () => {
  const [color, setColor] = useState("#000");
  const colorRef = useRef(null);

  // Update color when prefers-color-scheme changes
  useEffect(() => {
    const updateColor = () => {
      const cssColor = getComputedStyle(document.documentElement)
        .getPropertyValue("--color-text")
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
      {geoJson.features.map((data, index) => {
        const { geometry } = data;
        return (
          <lineSegments key={index} geometry={new GeoJsonGeometry(geometry, 1)}>
            <lineBasicMaterial color={color} ref={colorRef} />
          </lineSegments>
        );
      })}
    </group>
  );
};

export default ThreeCountries;
