import React, { useState, useEffect } from 'react'
import { useCookies } from 'react-cookie'
import axios from 'axios'
import API_ENDPOINTS from '../../../resources/api-links'
import './LoginForm.css'
import GlowContainer from '../../../components/GlowContainer/GlowContainer'
import Glow from '../../../components/Glow/Glow'
import Button from '../../../components/Buttons/Button'
import Navbar from '../../../components/Navbar/Navbar'

const LoginForm = () => {
  const [cookies] = useCookies(['username'])
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [isPasswordCorrect, setIsPasswordCorrect] = useState(true)
  const [isAlreadyLoggedIn, setIsAlreadyLoggedIn] = useState(false)

  useEffect(() => {
    console.log('Cookies:', cookies)
  }, [cookies])

  const handleLogin = async (e) => {
    e.preventDefault()
    try {
      console.log('Attempting login with:', { username })

      const response = await axios.post(
        API_ENDPOINTS.login,
        { username, password },
        { withCredentials: true }
      )

      console.log('Login response:', response.data)

      if (response.data) {
        setIsPasswordCorrect(true)
        setIsAlreadyLoggedIn(false)
        console.log('Navigating to profile...')
        window.location.replace('/profile')
      } else {
        setIsPasswordCorrect(false)
      }
    } catch (error) {
      console.error('Login error:', error.response?.data || error.message)
      if (error.response?.status === 403) {
        setIsAlreadyLoggedIn(true)
        setIsPasswordCorrect(true)
      } else {
        setIsPasswordCorrect(false)
        setIsAlreadyLoggedIn(false)
      }
    }
  }

  const handleLogout = async () => {
    try {
      await axios.post(API_ENDPOINTS.logout, {}, { withCredentials: true })

      window.location.replace('/')
    } catch (error) {
      console.error('Logout failed:', error)
    }
  }

  return (
    <>
      {cookies.username ? (
        <GlowContainer className="login-form-container">
          <h1>Vous êtes déjà connecté, {cookies.username} !</h1>
        </GlowContainer>
      ) : (
        <GlowContainer className="login-form-container">
          <Navbar />
          <Glow className="login-form">
            <div className="login-form-content">
              <h2>Connexion</h2>

              <form onSubmit={handleLogin}>
                <div>
                  <label>Nom d'utilisateur : </label>
                  <input
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                  />
                </div>
                <div>
                  <label>Mot de passe : </label>
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                </div>
                {!isPasswordCorrect && (
                  <p>
                    Mot de passe incorrect ou nom d'utilisateur introuvable.
                  </p>
                )}
                {isAlreadyLoggedIn && (
                  <p>Vous être déjà connecté, veuillez vous déconnecter.</p>
                )}
                <div className="profile-footer">
                  <Button
                    className="button-logout"
                    children={<button type="submit">Se connecter</button>}
                  />
                </div>
              </form>
            </div>
          </Glow>
        </GlowContainer>
      )}
    </>
  )
}

export default LoginForm
