import React, { useState, useEffect } from "react";
import "./Search.css";
import GlowContainer from "../../components/GlowContainer/GlowContainer";
import Panel from "../../components/Panel/Panel";
import Navbar from "../../components/Navbar/Navbar";
import FilmCard from "../../components/FilmCard/FilmCard";
import ThreeGlobe from "../../components/Earth/Earth";

import MultiSelectDropdown from "../../components/MultiSelectDropdown/MultiSelectDropdown";
import MultiSelectButtons from "../../components/MultiSelectButtons/MultiSelectButtons";
import RangeSlider from "../../components/RangeSlider/RangeSlider";
import StarRating from "../../components/StarRating/StarRating";

import getMovieCoordinates from "../../utils/getMovieCoordinates";

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

  const toggleLeftBar = () => {
    setShowLeftPanel(!showLeftPanel);
  };

  const toggleRightbar = () => {
    setShowRightPanel(!showRightPanel);
  };

  const searchMovies = async () => {
    const fetchedCoordinates = await getMovieCoordinates("tt1160419");
    setMovieCoordinates(fetchedCoordinates);
  };

  useEffect(() => {
    searchMovies();
  }, []);

  return (
    <>
      <GlowContainer className="search-page">
        <Navbar
          advancedSearch="true"
          toggleLeftBar={toggleLeftBar}
          toggleRightbar={toggleRightbar}
        />
        <div className="dashboard">
          <div className={`left-panel ${!showLeftPanel && "hidden"}`}>
            <Panel>
              <MultiSelectDropdown
                label="Pays"
                options={countries}
                selectedValues={filters.countries}
                onChange={(values) =>
                  setFilters({ ...filters, countries: values })
                }
              />

              <MultiSelectButtons
                label="Genres"
                options={genres}
                selectedValues={filters.genres}
                onChange={(values) =>
                  setFilters({ ...filters, genres: values })
                }
              />

              <RangeSlider
                label="Année de sortie"
                min={1900}
                max={2024}
                value={filters.yearRange}
                onChange={(range) =>
                  setFilters({ ...filters, yearRange: range })
                }
              />

              <StarRating
                label="Popularité minimum"
                value={filters.rating}
                onChange={(rating) => setFilters({ ...filters, rating })}
              />

              <MultiSelectDropdown
                label="Producteur"
                options={producers}
                selectedValues={filters.producers}
                onChange={(values) =>
                  setFilters({ ...filters, producers: values })
                }
              />

              <MultiSelectDropdown
                label="Acteur"
                options={actors}
                selectedValues={filters.actors}
                onChange={(values) =>
                  setFilters({ ...filters, actors: values })
                }
              />
            </Panel>
          </div>
          <div className="center-panel"></div>
          <div className="globe">
            <ThreeGlobe points={movieCoordinates} />
          </div>
          <div className={`right-panel ${!showRightPanel && "hidden"}`}>
            <div className="film-list">
              <FilmCard imdbId={"tt1757678"}></FilmCard>
              <FilmCard imdbId={"tt1630029"}></FilmCard>
              <FilmCard imdbId={"tt1160419"}></FilmCard>
              <FilmCard imdbId={"tt15239678"}></FilmCard>
              <FilmCard imdbId={"tt175767"}></FilmCard>
              <FilmCard imdbId={"tt163002"}></FilmCard>
              <FilmCard imdbId={"tt116041"}></FilmCard>
              <FilmCard imdbId={"tt1523968"}></FilmCard>
              <FilmCard imdbId={"tt3137850"}></FilmCard>
            </div>
          </div>
        </div>
      </GlowContainer>
    </>
  );
};

export default Search;
