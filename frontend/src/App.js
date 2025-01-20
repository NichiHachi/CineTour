import React from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'

// Pages
import Home from './pages/Home/Home'
import Searchbar from './components/Search/SearchBar'
// import NotFound from './pages/NotFound/NotFound'

// Contexts
import { ThemeProvider } from './utils/ThemeContext'

// Styles
import './App.css'

function App() {
  return (
    <ThemeProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route
            path="/search"
            element={<Searchbar placeholder={'Rechercher un film'} />}
          />
          {/* <Route path="*" element={<NotFound />} /> */}
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  )
}

export default App
