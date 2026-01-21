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

import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import API_ENDPOINTS from "../../resources/api-links";

const Search = () => {
  const location = useLocation();
  const navigate = useNavigate();

  // Panels
  const [showLeftPanel, setShowLeftPanel] = useState(true);
  const [showRightPanel, setShowRightPanel] = useState(true);

  const toggleLeftBar = () => setShowLeftPanel(!showLeftPanel);
  const toggleRightbar = () => setShowRightPanel(!showRightPanel);

  // Available filter options
  const [availableGenres, setAvailableGenres] = useState([]);
  const [availableCountries, setAvailableCountries] = useState([]);
  const [availableProducers, setAvailableProducers] = useState([]);
  const [availableActors, setAvailableActors] = useState([]);

  // Pagination info
  const [pageInfo, setPageInfo] = useState({
    totalPages: 0,
    totalElements: 0,
    currentPage: 0,
    pageSize: 5,
  });

  // Active filters
  const [filters, setFilters] = useState({
    title: "",
    genres: [],
    countries: [],
    yearRange: [1900, 2050],
    minRating: 0,
    producers: [],
    actors: [],
    page: 0,
    size: 5,
  });

  // Results
  const [results, setResults] = useState([]);
  const [selectedMovies, setSelectedMovies] = useState([]);
  const [allCoordinates, setAllCoordinates] = useState({});

  // Parse URL parameters and initialize filters
  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);

    const title = searchParams.get("title") || "";
    const fromYear = searchParams.get("fromYear");
    const toYear = searchParams.get("toYear");
    const minRating = searchParams.get("minRating");
    const page = searchParams.get("page");
    const size = searchParams.get("size");

    // Handle multiple genre values
    const genres = searchParams.getAll("genres");
    const countries = searchParams.getAll("countries");
    const producers = searchParams.getAll("producers");
    const actors = searchParams.getAll("actors");

    setFilters({
      title,
      genres,
      countries,
      yearRange: [
        fromYear ? parseInt(fromYear) : 1900,
        toYear ? parseInt(toYear) : 2050,
      ],
      minRating: minRating ? parseFloat(minRating) : 0,
      producers,
      actors,
      page: page ? parseInt(page) : 0,
      size: size ? parseInt(size) : 5,
    });
  }, [location.search]);

  // Fetch movies when filters change
  const timer = useRef(null);

  useEffect(() => {
    // Fetch movies based on current filters
    const fetchMovies = async () => {
      try {
        // Build params object for API
        const params = {
          title: filters.title,
          page: filters.page,
          size: filters.size,
        };

        // Only add year parameters if they differ from defaults
        if (filters.yearRange[0] !== 1900) {
          params.fromYear = filters.yearRange[0];
        }
        if (filters.yearRange[1] !== 2050) {
          params.toYear = filters.yearRange[1];
        }

        // Only add minRating if it's greater than 0
        if (filters.minRating > 0) {
          params.minRating = filters.minRating;
        }

        // Add array parameters
        filters.genres.forEach((genre) => {
          if (!params.genres) params.genres = [];
          params.genres.push(genre);
        });
        filters.countries.forEach((country) => {
          if (!params.countries) params.countries = [];
          params.countries.push(country);
        });

        console.log("Fetching with params:", params);

        const response = await axios.get(API_ENDPOINTS.search(params), {
          withCredentials: true,
        });

        // Handle pageable response
        let moviesArray = [];
        if (response.data) {
          // Check if it's a pageable object (Spring Boot Page)
          if (response.data.content && Array.isArray(response.data.content)) {
            moviesArray = response.data.content;

            // Extract pagination info
            setPageInfo({
              totalPages: response.data.totalPages || 0,
              totalElements: response.data.totalElements || 0,
              currentPage: response.data.number || 0,
              pageSize: response.data.size || 5,
            });
          }
          // Fallback for direct array response
          else if (Array.isArray(response.data)) {
            moviesArray = response.data;
            setPageInfo({
              totalPages: 1,
              totalElements: response.data.length,
              currentPage: 0,
              pageSize: response.data.length,
            });
          }
        }

        setResults(moviesArray);

        // Extract available filter options from results
        const allGenres = [
          ...new Set(
            moviesArray.flatMap((m) =>
              (m.genres || "").split(",").map((g) => g.trim()),
            ),
          ),
        ];

        const allCountries = [
          ...new Set(moviesArray.map((m) => m.country).filter(Boolean)),
        ];
        const allProducers = [
          ...new Set(moviesArray.flatMap((m) => m.producers || [])),
        ];
        const allActors = [
          ...new Set(moviesArray.flatMap((m) => m.actors || [])),
        ];

        setAvailableGenres(allGenres);
        setAvailableCountries(allCountries);
        setAvailableProducers(allProducers);
        setAvailableActors(allActors);
      } catch (err) {
        console.error("Error fetching movies:", err);
        setResults([]);
        setSelectedMovies([]);
        setPageInfo({
          totalPages: 0,
          totalElements: 0,
          currentPage: 0,
          pageSize: 5,
        });
      }
    };

    if (!filters.title) return;

    if (timer.current) clearTimeout(timer.current);

    timer.current = setTimeout(() => {
      fetchMovies();
    }, 300);

    return () => clearTimeout(timer.current);
  }, [filters]);

  // Update URL when filters change
  const updateURL = (newFilters) => {
    const params = new URLSearchParams();

    // Always add title
    if (newFilters.title) {
      params.set("title", newFilters.title);
    }

    // Only add year parameters if they differ from defaults
    if (newFilters.yearRange[0] !== 1900) {
      params.set("fromYear", newFilters.yearRange[0].toString());
    }
    if (newFilters.yearRange[1] !== 2050) {
      params.set("toYear", newFilters.yearRange[1].toString());
    }

    // Only add minRating if greater than 0
    if (newFilters.minRating > 0) {
      params.set("minRating", newFilters.minRating.toString());
    }

    // Only add page if not 0
    if (newFilters.page > 0) {
      params.set("page", newFilters.page.toString());
    }

    // Only add size if not default
    if (newFilters.size !== 5) {
      params.set("size", newFilters.size.toString());
    }

    // Add array parameters
    newFilters.genres.forEach((genre) => params.append("genres", genre));
    newFilters.countries.forEach((country) =>
      params.append("countries", country),
    );
    newFilters.producers.forEach((producer) =>
      params.append("producers", producer),
    );
    newFilters.actors.forEach((actor) => params.append("actors", actor));

    navigate(`/search?${params.toString()}`, { replace: true });
  };

  // Handle filter changes
  const handleFilterChange = (key, value) => {
    const newFilters = { ...filters, [key]: value, page: 0 }; // Reset to page 0 when filters change
    setFilters(newFilters);
    updateURL(newFilters);
  };

  // Handle page change
  const handlePageChange = (newPage) => {
    const newFilters = { ...filters, page: newPage };
    setFilters(newFilters);
    updateURL(newFilters);
  };

  // Load coordinates for results
  useEffect(() => {
    if (!results.length) return;

    const loadAllCoordinates = async () => {
      results.forEach(async (movie) => {
        try {
          const response = await axios.get(
            API_ENDPOINTS.locationsByImdbId(movie.idImdb),
          );

          const locations = Array.isArray(response.data) ? response.data : [];
          const coords = [];

          for (const location of locations) {
            if (location && location.latitude && location.longitude) {
              coords.push({
                latitude: Number(location.latitude),
                longitude: Number(location.longitude),
              });
            }
          }

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

  // Handle card click for globe visualization
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
              options={availableGenres}
              selectedValues={filters.genres}
              onChange={(values) => handleFilterChange("genres", values)}
            />
            <RangeSlider
              label="Année de sortie"
              min={1900}
              max={2050}
              value={filters.yearRange}
              onChange={(range) => handleFilterChange("yearRange", range)}
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
