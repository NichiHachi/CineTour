// frontend/src/context/LocationContext.js
import React, { createContext, useState } from "react";

export const movieContext = createContext();

export const MovieProvider = ({ children }) => {
  const [movie, setMovie] = useState([]);

  return (
    <MovieProvider.Provider value={{ movie, setMovie }}>
      {children}
    </MovieProvider.Provider>
  );
};
