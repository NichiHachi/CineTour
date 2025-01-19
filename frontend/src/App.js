import React from 'react'
import SearchBar from './components/search/SearchBar.js'

import { BrowserRouter, Routes, Route } from 'react-router-dom'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/search"
          element={<SearchBar placeholder="Search for a movie" />}
        />
      </Routes>
    </BrowserRouter>
  )
}

export default App
