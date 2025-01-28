import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from 'axios';

const LoginForm = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isPasswordCorrect, setIsPasswordCorrect] = useState(true);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      console.log('Attempting login with:', { username }); // Debug log
      
      const response = await axios.post('/api/users/login', {
        username,
        password
      }, {
        withCredentials: true
      });

      console.log('Login response:', response.data); // Debug log
      
      if (response.data) {
        setIsPasswordCorrect(true);
        navigate("/profile");
      } else {
        setIsPasswordCorrect(false);
      }
    } catch (error) {
      console.error("Login error:", error.response?.data || error.message);
      setIsPasswordCorrect(false);
    }
  };

  return (
    <div className="login-form">
      <form onSubmit={handleSubmit}>
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
        <button type="submit">Login</button>
      </form>
    </div>
  );
};

export default LoginForm;
