import React from 'react'
import SearchBar from './Components/SearchBar'
import FilmData from './Data.json'

function App() {
  return (
    <div className="App">
      <SearchBar placeholder={'Search for a film'} data={FilmData} />
    </div>
  )
}

export default App
