import React, { useState, useEffect, useRef } from "react";
import "./Search.css";
import GlowContainer from "../../components/GlowContainer/GlowContainer";
import Panel from "../../components/Panel/Panel";
import Navbar from "../../components/Navbar/Navbar";
import FilmCard from "../../components/FilmCard/FilmCard";
import ThreeGlobe from "../../components/ThreeGlobe/ThreeGlobe";

import MultiSelectButtons from "../../components/MultiSelectButtons/MultiSelectButtons";
import RangeSlider from "../../components/RangeSlider/RangeSlider";
import StarRating from "../../components/StarRating/StarRating";

import { useLocation } from "react-router-dom";
import axios from "axios";
import API_ENDPOINTS from "../../resources/api-links";
import getLocationsByImdbId from "../../utils/getLocationsByImdbId";

const Search = () => {
  // Panels
  const [showLeftPanel, setShowLeftPanel] = useState(true);
  const [showRightPanel, setShowRightPanel] = useState(true);

  const toggleLeftBar = () => setShowLeftPanel(!showLeftPanel);
  const toggleRightbar = () => setShowRightPanel(!showRightPanel);

  // Filters
  const [countries, setCountries] = useState([]);
  const [genres, setGenres] = useState([]);
  const [producers, setProducers] = useState([]);
  const [actors, setActors] = useState([]);
  const [filters, setFilters] = useState({
    countries: [],
    genres: [],
    yearRange: [1900, 2024],
    rating: 0,
    producers: [],
    actors: [],
  });

  // Coordinates
  const [selectedMovies, setSelectedMovies] = useState([]);
  const [results, setResults] = useState([]);

  const [allCoordinates, setAllCoordinates] = useState({});

  useEffect(() => {
    if (!results.length) return;

    const loadAllCoordinates = async () => {
      results.forEach(async (movie) => {
        try {
          const coords = await getLocationsByImdbId(movie.idImdb);
          setAllCoordinates((prev) => ({
            ...prev,
            [movie.idImdb]: coords,
          }));
        } catch {
          setAllCoordinates((prev) => ({
            ...prev,
            [movie.idImdb]: [],
          }));
        }
      });
    };

    loadAllCoordinates();
  }, [results]);

  // Show coordinates on filmcard clicked
  const handleCardClick = (movie) => {
    setSelectedMovies((prev) =>
      prev.includes(movie.idImdb)
        ? prev.filter((id) => id !== movie.idImdb)
        : [...prev, movie.idImdb],
    );
  };

  const movieCoordinates = selectedMovies.flatMap(
    (id) => allCoordinates[id] || [],
  );

  // Query
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const query = searchParams.get("q");

  // Debounced query effect
  const timer = useRef(null);

  useEffect(() => {
    if (!query) return;

    if (timer.current) clearTimeout(timer.current);

    timer.current = setTimeout(() => {
      setAllCoordinates([]);
      fetchMovies(query);
    }, 0);

    return () => clearTimeout(timer.current);
  }, [query]);

  // Fetch movies
  const fetchMovies = async (q) => {
    if (!q) return;

    try {
      const response = await axios.get(API_ENDPOINTS.search(q), {
        withCredentials: true,
      });
      if (!Array.isArray(response.data)) return;

      setResults(response.data);

      // Extract filters
      setCountries([...new Set(response.data.map((m) => m.country))]);
      setGenres([...new Set(response.data.flatMap((m) => m.genres))]);
      setProducers([...new Set(response.data.flatMap((m) => m.producers))]);
      setActors([...new Set(response.data.flatMap((m) => m.actors))]);
    } catch (err) {
      console.error(err);
      setResults([]);
      setSelectedMovies([]);
    }
  };

  return (
    <GlowContainer className="search-page">
      <Navbar
        advancedSearch="true"
        searchBar="true"
        toggleLeftBar={toggleLeftBar}
        toggleRightbar={toggleRightbar}
      />
      <div className="dashboard">
        <div className={`left-panel ${!showLeftPanel && "hidden"}`}>
          <Panel>
            <MultiSelectButtons
              label="Genres"
              options={genres}
              selectedValues={filters.genres}
              onChange={(values) => setFilters({ ...filters, genres: values })}
            />
            <RangeSlider
              label="Année de sortie"
              min={1900}
              max={2024}
              value={filters.yearRange}
              onChange={(range) => setFilters({ ...filters, yearRange: range })}
            />
            <StarRating
              label="Popularité minimum"
              value={filters.rating}
              onChange={(rating) => setFilters({ ...filters, rating })}
            />
          </Panel>
        </div>

        <div className="center-panel"></div>

        <div className="globe">
          <ThreeGlobe points={movieCoordinates} />
        </div>

        <div className={`right-panel ${!showRightPanel && "hidden"}`}>
          <div className="film-list">
            {results.map((movie) => (
              <FilmCard
                key={movie.idImdb}
                movie={movie}
                onSelect={handleCardClick}
                coordinates={allCoordinates[movie.idImdb]}
              />
            ))}
          </div>
        </div>
      </div>
    </GlowContainer>
  );
};

export default Search;
