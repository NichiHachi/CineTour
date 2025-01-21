import React, { useState } from "react";
import { addUser, isUsernameAvailable } from "./api";

const SignUpForm = ({ onUserAdded }) => {
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
      {!isNameAvailable && (
        <div>
          <p style={{ color: "red" }}>
            Username is already taken. Please choose a different username.
          </p>
        </div>
      )}
      <div>
        <label>Password:</label>
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
      </div>
      <div>
        <label>Email:</label>
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
      </div>
      <button type="submit">Add User</button>
    </form>
  );
};

export default SignUpForm;
