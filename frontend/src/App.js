import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { CookiesProvider } from "react-cookie";
import { LocationProvider } from "./context/LocationContext";

// Pages
import Home from "./pages/Home/Home";
import UserProfile from "./pages/User/Profile/UserProfile";
import Movie from "./pages/Movie/Movie";
import Map from "./components/Map/Map.jsx";
import NotFound from "./pages/NotFound/NotFound";
import LoginForm from "./pages/User/Connection/LoginForm.js";
import SignUpForm from "./pages/User/Connection/SignUpForm.js";
import Globe from "./pages/Globe/Globe.jsx";

// Styles
import "./App.css";
import SearchResults from "./components/SearchResults/SearchResults";

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
            <Route path="/movie/:imdbId" element={<Movie />} />
            <Route path="/map" element={<Map height="500px" width="500px" />} />
            <Route path="*" element={<NotFound />} />
            <Route path="/globe" element={<Globe />} />
            <Route path="/search-results" element={<SearchResults />} />
          </Routes>
        </LocationProvider>
      </CookiesProvider>
    </BrowserRouter>
  );
}

export default App;
