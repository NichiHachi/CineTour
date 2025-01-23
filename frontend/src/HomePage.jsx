// frontend/src/HomePage.jsx
import React, { useContext } from "react";
import { Link } from "react-router-dom";
import { UserContext } from "./UserContext";

const HomePage = () => {
  const { user } = useContext(UserContext);

  return (
    <div>
      <h1>Bienvenue sur la page d'accueil</h1>
      <nav>
        {user ? (
          <>
            <Link to="/profile">Mon Profil</Link>
            <div>
              <h2>Bienvenue, {user.username}!</h2>
              <p>Email: {user.email}</p>
            </div>
          </>
        ) : (
          <>
            <Link to="/login">Se connecter</Link>
            <p>Vous n'êtes pas connecté.</p>
          </>
        )}
      </nav>
    </div>
  );
};

export default HomePage;