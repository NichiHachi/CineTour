import React, { useState } from 'react'
import SignUpForm from './SignUpForm'
import LoginForm from './LoginForm'
import './UserForm.css'

const UserForm = () => {
  const [loginClicked, setLoginClicked] = useState(true)

  const handleLoginClick = (isLog) => {
    setLoginClicked(isLog)
  }

  return (
    <div className="userForm">
      <div className="chooseFileDiv">
        <div onClick={() => handleLoginClick(true)} className="userFormName">
          Se connecter
        </div>
        <div onClick={() => handleLoginClick(false)} className="userFormName">
          Créer un compte
        </div>
      </div>
      {loginClicked ? <LoginForm /> : <SignUpForm />}
    </div>
  )
}

export default UserForm
