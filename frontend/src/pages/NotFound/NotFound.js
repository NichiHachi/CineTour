import React from 'react'
import './NotFound.css'
import GlowContainer from '../../components/GlowContainer/GlowContainer'
import RevealText from '../../components/TextEffects/RevealText/RevealText'
import Button from '../../components/Buttons/Button'
import StaggeredText from '../../components/TextEffects/StaggeredText/StaggeredText'

const Home = () => {
  return (
    <>
      <GlowContainer className="home">
        <h1>
          <RevealText>404 Not Found :(</RevealText>
        </h1>
        <Button
          children={
            <a href="/">
              <StaggeredText>Retour</StaggeredText>
            </a>
          }
        />
      </GlowContainer>
    </>
  )
}

export default Home
