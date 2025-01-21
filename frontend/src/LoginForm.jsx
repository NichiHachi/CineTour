import React, { useState } from "react";
import { correctPassword } from "./api";
import { useHistory } from "react-router-dom";

const LoginForm = ({ onUserAdded }) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isPasswordCorrect, setIsPasswordCorrect] = useState(true);
  const history = useHistory();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const user = { username, password };
      const correct = await correctPassword(user);
      if (correct.username === user.username) {
        onUserAdded(correct);
        setIsPasswordCorrect(true);
        history.push("/");
      } else {
        setIsPasswordCorrect(false);
      }
    } catch (error) {
      console.error("Error checking password:", error);
    }
  };

  return (
    <>
      <form onSubmit={handleSubmit}>
        <div>
          <label>Username:</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div>
          <label>Password:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit">Se connecter</button>
      </form>
      {isPasswordCorrect ? (
        <></>
      ) : (
        <div>
          <p style={{ color: "red" }}>
            Nom d'utilisateur ou mot de passe incorrect
          </p>
        </div>
      )}
    </>
  );
};
export default LoginForm;
