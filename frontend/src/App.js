import React from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'

// Pages
import Card from './components/Card/Card'

// Styles
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Card />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
