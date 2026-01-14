import React, { useState } from "react";
import { addUser, isUsernameAvailable } from "../../../apis/api";
import GlowContainer from "../../../components/GlowContainer/GlowContainer";
import Glow from "../../../components/Glow/Glow";
import Navbar from "../../../components/Navbar/Navbar";
import "./LoginForm.css";
import Button from "../../../components/Buttons/Button";
import StaggeredText from "../../../components/TextEffects/StaggeredText/StaggeredText";

const SignUpForm = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [isNameAvailable, setIsNameAvailable] = useState(true);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const newUser = { username, password, email };
      const available = await isUsernameAvailable(newUser);
      setIsNameAvailable(available);
      console.log("isNameAvailable:", available);
      if (available) {
        await addUser(newUser);
        setUsername("");
        setPassword("");
        setEmail("");
      }
    } catch (error) {
      console.error("Error adding user:", error);
    }
  };

  return (
    <GlowContainer className="login-form-container">
      <Navbar />
      <Glow className="login-form">
        <div className="login-form-section">
          <h2>Créer un compte</h2>
          <form onSubmit={handleSubmit}>
            <div>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Nom d'utilisateur"
                required
              />
            </div>
            {!isNameAvailable && (
              <div>
                <p style={{ color: "red" }}>
                  Nom d'utilisateur déjà utilisé ou non valide.
                </p>
              </div>
            )}
            <div>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Mot de passe"
                required
              />
            </div>
            <div>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="E-mail"
                required
              />
            </div>

            <Button
              className="button-logout"
              type="submit"
              children={<StaggeredText>Créer un compte</StaggeredText>}
            />
          </form>
        </div>
      </Glow>
    </GlowContainer>
  );
};

export default SignUpForm;
