// frontend/src/context/LocationContext.js
import React, { createContext, useState } from "react";

export const LocationContext = createContext();

export const LocationProvider = ({ children }) => {
  const [locationData, setLocationData] = useState([]);
  const [imageData, setImageData] = useState([]);

  return (
    <LocationContext.Provider
      value={{ locationData, setLocationData, imageData, setImageData }}
    >
      {children}
    </LocationContext.Provider>
  );
};
