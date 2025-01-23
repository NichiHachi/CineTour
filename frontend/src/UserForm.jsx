import React, { useState, useContext } from "react";
import SignUpForm from "./SignUpForm";
import LoginForm from "./LoginForm";
import "./UserForm.scss";
import { UserContext } from "./UserContext";

const UserForm = ({ onUserLogin }) => {
  const [loginClicked, setLoginClicked] = useState(true);
  const { setUser } = useContext(UserContext);

  const handleLoginClick = (isLog) => {
    setLoginClicked(isLog);
  };

  return (
    <div className="userForm">
      <div className="chooseFileDiv">
        <div onClick={() => handleLoginClick(true)} className="userFormName">
          Login
        </div>
        <div onClick={() => handleLoginClick(false)} className="userFormName">
          Sign up
        </div>
      </div>
      {loginClicked ? <LoginForm onUserAdded={setUser} /> : <SignUpForm />}
    </div>
  );
};
export default UserForm;
