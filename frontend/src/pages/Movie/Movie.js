import React from 'react'
import Navbar from '../../components/Navbar/Navbar'
import GlowContainer from '../../components/GlowContainer/GlowContainer'
import MovieBanner from '../../components/MovieBanner/MovieBanner'
import './Movie.css'

function Movie() {
  return (
    <GlowContainer className="movie">
      <Navbar />
      <MovieBanner />
    </GlowContainer>
  )
}

export default Movie
