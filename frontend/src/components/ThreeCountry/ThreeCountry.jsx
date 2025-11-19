// three-countries.js

import React from "react";
import GeoJsonGeometry from "three-geojson-geometry";

import geoJson from "./countries.json";

const ThreeCountries = () => {
  return (
    <group>
      {geoJson.features.map((data, index) => {
        const { geometry } = data;
        return (
          <lineSegments key={index} geometry={new GeoJsonGeometry(geometry, 1)}>
            <lineBasicMaterial color="#5c5c5c" />
          </lineSegments>
        );
      })}
    </group>
  );
};

export default ThreeCountries;
