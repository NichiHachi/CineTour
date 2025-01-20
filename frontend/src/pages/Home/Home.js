import React from 'react'
import styled from 'styled-components'
import ThemeToggle from '../../components/Buttons/ThemeToggle/ThemeToggle'

const Home = () => {
  return (
    <HomeContainer>
      <Header>
        <Title>CineTour</Title>
        <ThemeToggle />
      </Header>

      <HeroSection>
        <HeroText>
          <h1>Passez vos prochaines vacances dans un décor de cinéma</h1>
          <p>Recherchez où ont été tournées vos films préférés.</p>
        </HeroText>
      </HeroSection>
    </HomeContainer>
  )
}

const HomeContainer = styled.div`
  min-height: 100vh;
  padding: 20px;
  background-color: ${({ theme }) => theme.background};
  color: ${({ theme }) => theme.text};
  transition: all 0.3s ease;
`

const Header = styled.header`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;

  @media (max-width: 768px) {
    flex-direction: column;
    gap: 20px;
  }
`

const Title = styled.h1`
  font-size: 2rem;
  color: ${({ theme }) => theme.primary};
  margin: 0;
`

const HeroSection = styled.section`
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  padding: 40px 20px;
  background: ${({ theme }) => theme.gradient};
  margin: 20px 0;
  border-radius: 15px;
`

const HeroText = styled.div`
  text-align: center;
  max-width: 800px;

  h1 {
    font-size: 3rem;
    margin-bottom: 20px;
    color: ${({ theme }) => theme.primary};

    @media (max-width: 768px) {
      font-size: 2rem;
    }
  }

  p {
    font-size: 1.2rem;
    color: ${({ theme }) => theme.text};
    opacity: 0.9;

    @media (max-width: 768px) {
      font-size: 1rem;
    }
  }
`

export default Home
