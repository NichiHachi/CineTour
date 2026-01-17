import React, { useState, useEffect, useRef, useContext } from "react";
import "./Search.css";
import GlowContainer from "../../components/GlowContainer/GlowContainer";
import Panel from "../../components/Panel/Panel";
import Navbar from "../../components/Navbar/Navbar";
import FilmCard from "../../components/FilmCard/FilmCard";
import ThreeGlobe from "../../components/Earth/Earth";

import MultiSelectButtons from "../../components/MultiSelectButtons/MultiSelectButtons";
import RangeSlider from "../../components/RangeSlider/RangeSlider";
import StarRating from "../../components/StarRating/StarRating";

import getMovieCoordinates from "../../utils/getMovieCoordinates";

import { useLocation } from "react-router-dom";
import axios from "axios";
import API_ENDPOINTS from "../../resources/api-links";
import { LocationContext } from "../../context/LocationContext";

const Search = () => {
  const [showLeftPanel, setShowLeftPanel] = useState(true);
  const [showRightPanel, setShowRightPanel] = useState(true);
  const [movieCoordinates, setMovieCoordinates] = useState([]);
  const [results, setResults] = useState([]);
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

  const { setLocationData, setImageData } = useContext(LocationContext);

  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const query = searchParams.get("q");

  const timer = useRef(null);

  const fetchMovies = async (q) => {
    if (!q) return;

    try {
      const response = await axios.get(API_ENDPOINTS.search(q), {
        withCredentials: true,
      });
      if (!Array.isArray(response.data)) return;

      setResults(response.data);

      // Fetch coordinates in parallel
      const coordinates = await Promise.all(
        response.data.map((movie) => getMovieCoordinates(movie.idImdb)),
      );
      setMovieCoordinates(coordinates.flat());

      // Extract filters
      setCountries([...new Set(response.data.map((m) => m.country))]);
      setGenres([...new Set(response.data.flatMap((m) => m.genres))]);
      setProducers([...new Set(response.data.flatMap((m) => m.producers))]);
      setActors([...new Set(response.data.flatMap((m) => m.actors))]);
    } catch (err) {
      console.error(err);
      setResults([]);
      setMovieCoordinates([]);
    }
  };

  // Debounced query effect
  useEffect(() => {
    if (!query) return;

    if (timer.current) clearTimeout(timer.current);

    timer.current = setTimeout(() => {
      fetchMovies(query);
    }, 100);

    return () => clearTimeout(timer.current);
  }, [query]);

  const toggleLeftBar = () => setShowLeftPanel(!showLeftPanel);
  const toggleRightbar = () => setShowRightPanel(!showRightPanel);

  const handleMovieClick = async (imdbId) => {
    try {
      const responseImage = await axios.post(API_ENDPOINTS.movieImage(imdbId));
      const responseLocation = await fetch(
        API_ENDPOINTS.importLocationByImdbId(imdbId),
      );

      setImageData(responseImage);
      setLocationData(responseLocation);
    } catch (err) {
      console.error("Error fetching movie details:", err);
    }
  };

  return (
    <GlowContainer className="search-page">
      <Navbar
        advancedSearch="true"
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
            <FilmCard
              key={"tt1160419"}
              imdbId={"tt1160419"}
              onClick={() => handleMovieClick("tt1160419")}
            />
            {results.map((movie) => (
              <FilmCard
                key={movie.idImdb}
                imdbId={movie.idImdb}
                onClick={() => handleMovieClick(movie.idImdb)}
              />
            ))}
          </div>
        </div>
      </div>
    </GlowContainer>
  );
};

export default Search;
