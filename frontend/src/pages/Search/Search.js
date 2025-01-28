import React from 'react'
import Searchbar from '../../components/Searchbar/Searchbar'
import './Search.css'
import GlowContainer from '../../components/GlowContainer/GlowContainer'
import Navbar from '../../components/Navbar/Navbar'

const Search = () => {
  return (
    <GlowContainer className="search">
      <Navbar />
      <Searchbar />
    </GlowContainer>
  )
}

export default Search
