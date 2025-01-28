import React, { useState, useEffect } from "react";
import { useCookies } from "react-cookie";
import axios from "axios";
import API_ENDPOINTS from "../../../resources/api-links";

const LoginForm = () => {
  const [cookies] = useCookies(["username"]);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isPasswordCorrect, setIsPasswordCorrect] = useState(true);
  const [isAlreadyLoggedIn, setIsAlreadyLoggedIn] = useState(false);

  useEffect(() => {
    console.log("Cookies:", cookies);
  }, [cookies]);

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      console.log("Attempting login with:", { username });

      const response = await axios.post(
        API_ENDPOINTS.login,
        { username, password },
        { withCredentials: true }
      );

      console.log("Login response:", response.data);

      if (response.data) {
        setIsPasswordCorrect(true);
        setIsAlreadyLoggedIn(false);
        console.log("Navigating to profile...");
        window.location.replace("/profile");
      } else {
        setIsPasswordCorrect(false);
      }
    } catch (error) {
      console.error("Login error:", error.response?.data || error.message);
      if (error.response?.status === 403) {
        setIsAlreadyLoggedIn(true);
        setIsPasswordCorrect(true);
      } else {
        setIsPasswordCorrect(false);
        setIsAlreadyLoggedIn(false);
      }
    }
  };

  const handleLogout = async () => {
    try {
      await axios.post(API_ENDPOINTS.logout, {}, { withCredentials: true });

      window.location.replace("/");
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  return (
    <div className="login-form">
      {cookies.username ? (
        <div>
          <p>Welcome, {cookies.username}!</p>
          <button onClick={handleLogout}>Logout</button>
        </div>
      ) : (
        <form onSubmit={handleLogin}>
          <div>
            <label>Username:</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
          </div>
          <div>
            <label>Password:</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          {!isPasswordCorrect && <p>Incorrect username or password</p>}
          {isAlreadyLoggedIn && <p>You are already logged in. Please logout first.</p>}
          <button type="submit">Login</button>
        </form>
      )}
    </div>
  );
};

export default LoginForm;