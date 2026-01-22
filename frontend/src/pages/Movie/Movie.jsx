import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate, useParams } from "react-router-dom";
import "./Movie.css";
import Glow from "../../components/Glow/Glow";
import GlowContainer from "../../components/GlowContainer/GlowContainer";
import Navbar from "../../components/Navbar/Navbar";
import FilmCard from "../../components/FilmCard/FilmCard";
import formatTime from "../../utils/formatTime";
import getPosterByImdbId from "../../utils/getPosterByImdbId";
import getMovieByImdbId from "../../utils/getMovieByImdbId";

import ThreeGlobe from "../../components/ThreeGlobe/ThreeGlobe";

import API_ENDPOINTS from "../../resources/api-links";

const Movie = () => {
  const [copied, setCopied] = useState(false);
  const [poster, setPoster] = useState();
  const [noPoster, setNoPoster] = useState(false);
  const [movie, setMovie] = useState(null);
  const [coordinates, setCoordinates] = useState([]);

  const { imdbId } = useParams();
  const navigate = useNavigate();

  const handleClickImdbId = () => {
    if (movie?.idImdb) {
      navigator.clipboard.writeText(movie.idImdb);
      setCopied(true);
      setTimeout(() => setCopied(false), 1200);
    }
  };

  useEffect(() => {
    const fetchMovie = async () => {
      const movieData = await getMovieByImdbId(imdbId);
      if (movieData) setMovie(movieData);

      const posterData = await getPosterByImdbId(imdbId);
      if (posterData) setPoster(posterData);
      else setNoPoster(true);
    };

    fetchMovie();
  }, [imdbId]);

  // Fetch coordinates once movie is loaded
  useEffect(() => {
    if (!movie) return;

    const fetchCoordinates = async () => {
      try {
        const response = await axios.get(
          API_ENDPOINTS.locationsByImdbId(movie.idImdb),
        );
        const locations = Array.isArray(response.data) ? response.data : [];
        console.log(response.data);
        const coords = locations
          .filter((loc) => loc?.latitude && loc?.longitude)
          .map((loc) => ({
            latitude: Number(loc.latitude),
            longitude: Number(loc.longitude),
            locationString: String(loc.locationString),
            description: String(loc.description),
          }));
        setCoordinates(coords);
      } catch {
        setCoordinates([]);
      }
    };

    fetchCoordinates();
  }, [movie]);

  const isLoading = !movie;

  return (
    <GlowContainer className="movie-page">
      <Navbar searchBar="true" />

      <div className="movie-page-section">
        <div className="movie-page-background">
          {isLoading || !movie.backdropPath ? (
            <div className="skeleton skeleton-background" />
          ) : (
            <img
              className="movie-page-background-img"
              src={movie.backdropPath}
              alt={`Background of ${movie.title}`}
            />
          )}
          <div className="movie-page-background-gradient" />
        </div>

        <div className="movie-page-main">
          <div className="movie-page-header">
            <Glow className="movie-page-image">
              {isLoading ? (
                <div className="skeleton skeleton-image" />
              ) : poster ? (
                <img src={poster} alt={`Poster of ${movie.title}`} />
              ) : noPoster ? (
                <div className="blank" />
              ) : (
                <div className="skeleton skeleton-image" />
              )}
            </Glow>

            <div className="movie-page-info">
              <div className="movie-page-title-section">
                {isLoading ? (
                  <div className="skeleton skeleton-title" />
                ) : (
                  <>
                    <div className="movie-page-title">{movie.title}</div>
                    <button
                      className={`movie-imdbid ${copied ? "copied" : ""}`}
                      onClick={handleClickImdbId}
                    >
                      {movie.idImdb}
                    </button>
                  </>
                )}
              </div>

              <div className="movie-page-subtitle">
                {isLoading ? (
                  <>
                    <div className="skeleton skeleton-subtitle" />
                    <div className="skeleton skeleton-subtitle" />
                  </>
                ) : (
                  <>
                    <div className="movie-page-release">
                      {movie.releaseYear}
                    </div>
                    <div className="movie-page-runtime">
                      {formatTime(movie.runtimeMinutes)}
                    </div>
                  </>
                )}
              </div>

              <div className="movie-page-genres">
                {isLoading
                  ? Array.from({ length: 3 }).map((_, i) => (
                      <div key={i} className="skeleton skeleton-genre" />
                    ))
                  : movie.genres?.split(",").map((genre) => (
                      <Glow className="movie-page-genre" key={genre}>
                        <div className="genre-section">{genre}</div>
                      </Glow>
                    ))}
              </div>

              <div className="movie-page-overview">
                {isLoading ? (
                  <div className="skeleton skeleton-description" />
                ) : (
                  movie.overview
                )}
              </div>
            </div>
          </div>

          <div className="movie-page-geo">
            <Glow className="movie-page-globe-container">
              <div className="movie-page-globe">
                <ThreeGlobe points={coordinates} />
              </div>
            </Glow>
            <div className="movie-coordinates">
              {coordinates === null ? (
                <div className="skeleton skeleton-coordinates" />
              ) : coordinates.length === 0 ? (
                <div className="coordinates">Aucun lieu</div>
              ) : (
                coordinates.map((coord, index) => (
                  <Glow key={index} className="movie-location">
                    <div className="movie-location-content">
                      <div className="movie-location-title">
                        {coord.locationString}
                      </div>
                      <div className="movie-location-coords">
                        {coord.latitude}, {coord.longitude}
                      </div>
                      <div className="movie-location-description">
                        {coord.description}
                      </div>
                    </div>
                  </Glow>
                ))
              )}
            </div>
          </div>

          <div className="movie-page-recommandation">
            <h2>Recommandation</h2>
            <h3>A proximité :</h3>
            <div className="movie-page-recommandation-list">
              <FilmCard />
              <FilmCard />
              <FilmCard />
            </div>
            <h3>Dans le même genre :</h3>
            <div className="movie-page-recommandation-list">
              <FilmCard />
              <FilmCard />
              <FilmCard />
            </div>
          </div>
        </div>
      </div>
    </GlowContainer>
  );
};

export default Movie;
