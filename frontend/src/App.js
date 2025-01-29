import React from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { CookiesProvider } from 'react-cookie'

// Pages
import Home from './pages/Home/Home'
import UserForm from './pages/User/Connection/UserForm'
import UserProfile from './pages/User/Profile/UserProfile'
import Search from './pages/Search/Search'
import Movie from './pages/Movie/Movie'
import Map from './components/Map/Map.jsx'
import NotFound from './pages/NotFound/NotFound'

// Styles
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <CookiesProvider>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<UserForm onUserLogin={true} />} />
          <Route path="/signup" element={<UserForm onUserLogin={false} />} />
          <Route path="/profile" element={<UserProfile />} />
          <Route path="/search" element={<Search />} />
          <Route path="/movie/:imdbId" element={<Movie />} />
          <Route path="/map" element={<Map height="500px" width="500px" />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </CookiesProvider>
    </BrowserRouter>
  )
}

export default App;
