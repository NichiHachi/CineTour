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
          Login
        </div>
        <div onClick={() => handleLoginClick(false)} className="userFormName">
          Sign up
        </div>
      </div>
      {loginClicked ? <LoginForm /> : <SignUpForm />}
    </div>
  )
}

export default UserForm
