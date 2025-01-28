import React from 'react'
import Searchbar from '../../components/Searchbar/Searchbar'
import './Home.css'
import GlowContainer from '../../components/GlowContainer/GlowContainer'
import Navbar from '../../components/Navbar/Navbar'
import RevealText from '../../components/TextEffects/RevealText/RevealText'

const Home = () => {
  return (
    <>
      <GlowContainer className="home">
        <Navbar />
        <h1>
          Passer vos prochaines vacances dans un décor de{' '}
          <RevealText>cinéma</RevealText>
        </h1>
        <Searchbar />
      </GlowContainer>
    </>
  )
}

export default Home
