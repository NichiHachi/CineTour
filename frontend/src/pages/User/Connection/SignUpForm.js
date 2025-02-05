import React, { useState } from 'react'
import { addUser, isUsernameAvailable } from '../../../apis/api'
import GlowContainer from '../../../components/GlowContainer/GlowContainer'
import Glow from '../../../components/Glow/Glow'
import Navbar from '../../../components/Navbar/Navbar'
import './LoginForm.css'
import Button from '../../../components/Buttons/Button'

const SignUpForm = () => {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [email, setEmail] = useState('')
  const [isNameAvailable, setIsNameAvailable] = useState(true)

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      const newUser = { username, password, email }
      const available = await isUsernameAvailable(newUser)
      setIsNameAvailable(available)
      console.log('isNameAvailable:', available)
      if (available) {
        await addUser(newUser)
        setUsername('')
        setPassword('')
        setEmail('')
      }
    } catch (error) {
      console.error('Error adding user:', error)
    }
  }

  return (
    <GlowContainer className="login-form-container">
      <Navbar />
      <Glow className="login-form">
        <div className="login-form-content">
          <h2>Créer un compte</h2>
          <form onSubmit={handleSubmit}>
            <div>
              <label>Nom d'utilisateur : </label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>
            {!isNameAvailable && (
              <div>
                <p style={{ color: 'red' }}>
                  Nom d'utilisateur déjà utilisé ou non valide.
                </p>
              </div>
            )}
            <div>
              <label>Mot de passe : </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <div>
              <label>Email : </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <div className="profile-footer">
              <Button
                className="button-logout"
                children={<button type="submit">Créer un compte</button>}
              />
            </div>
          </form>
        </div>
      </Glow>
    </GlowContainer>
  )
}

export default SignUpForm
