import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { CookiesProvider } from 'react-cookie';

// Pages
import Card from './components/Card/Card';

// Styles
import './App.css';
import UserForm from './UserForm';
import UserProfile from './UserProfile';
import SearchBar from './components/Search/SearchBar';
import MovieDetails from './pages/Home/MovieDetails/MovieDetails';

function App() {
  return (
    <BrowserRouter>
      <CookiesProvider>
        <Routes>
          <Route path="/" element={<Card />} />
          <Route path="/login" element={<UserForm />} />
          <Route path="/profile" element={<UserProfile />} />
          <Route path="/search" element={<SearchBar />} />
          <Route path="/movie/:imdbId" element={<MovieDetails />} />
        </Routes>
      </CookiesProvider>
    </BrowserRouter>
  );
}

export default App;