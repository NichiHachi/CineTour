import React from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { CookiesProvider } from 'react-cookie'
import { LocationProvider } from './context/LocationContext'

// Pages
import Home from './pages/Home/Home'
import UserProfile from './pages/User/Profile/UserProfile'
import Search from './pages/Search/Search'
import Movie from './pages/Movie/Movie'
import Map from './components/Map/Map.jsx'
import NotFound from './pages/NotFound/NotFound'
import LoginForm from './pages/User/Connection/LoginForm.js'
import SignUpForm from './pages/User/Connection/SignUpForm.js'

// Styles
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <CookiesProvider>
        <LocationProvider>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<LoginForm />} />
            <Route path="/signup" element={<SignUpForm />} />
            <Route path="/profile" element={<UserProfile />} />
            <Route path="/search" element={<Search />} />
            <Route path="/movie/:imdbId" element={<Movie />} />
            <Route path="/map" element={<Map height="500px" width="500px" />} />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </LocationProvider>
      </CookiesProvider>
    </BrowserRouter>
  )
}

export default App
