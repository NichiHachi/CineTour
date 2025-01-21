// frontend/src/HomePage.jsx
import React, { useContext } from "react";
import { UserContext } from "./UserContext";

const HomePage = () => {
  const { user } = useContext(UserContext);

  return (
    <div>
      <h1>Bienvenue sur la page d'accueil</h1>
      <p>Ceci est un exemple de page d'accueil.</p>
      {user ? (
        <div>
          <h2>Bienvenue, {user.username}!</h2>
          <p>Email: {user.email}</p>
        </div>
      ) : (
        <p>Vous n'êtes pas connecté.</p>
      )}
    </div>
  );
};

export default HomePage;
