import React from 'react'
import './Navbar.css'
import Button from '../Buttons/Button'
import Glow from '../Glow/Glow'
import StaggeredText from '../TextEffects/StaggeredText/StaggeredText'

function Navbar({ isConnected }) {
  return (
    <Glow className="navbar">
      <div className="navbar-section">
        <div className="navbar-left">
          <h1 className="logo">
            <a href="/">CineTour</a>
          </h1>
        </div>

        <div className="navbar-right">
          {isConnected ? (
            ''
          ) : (
            <>
              <Button
                children={
                  <a href="/login">
                    <StaggeredText>Se connecter</StaggeredText>
                  </a>
                }
              />
              <Button
                children={
                  <a href="/signup">
                    <StaggeredText>S'inscrire</StaggeredText>
                  </a>
                }
              />
            </>
          )}
        </div>
      </div>
    </Glow>
  )
}

export default Navbar
