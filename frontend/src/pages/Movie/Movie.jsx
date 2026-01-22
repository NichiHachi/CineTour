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
import { getNearestMovies } from "../../utils/nearbyMovies";

import ThreeGlobe from "../../components/ThreeGlobe/ThreeGlobe";

import API_ENDPOINTS from "../../resources/api-links";

const Movie = () => {
  const [copied, setCopied] = useState(false);
  const [poster, setPoster] = useState();
  const [noPoster, setNoPoster] = useState(false);
  const [movie, setMovie] = useState(null);
  const [coordinates, setCoordinates] = useState([]);
  const [recommandationMovies, setRecommandationMovies] = useState([]);
  const [genreRecommendations, setGenreRecommendations] = useState([]);
  const [nearbyRecommendations, setNearbyRecommendations] = useState([]);
  const [actorRecommendations, setActorRecommendations] = useState([]);
  const [directorRecommendations, setDirectorRecommendations] = useState([]);
  const [eraRecommendations, setEraRecommendations] = useState([]);

  const { imdbId } = useParams();
  const navigate = useNavigate();

  const handleClickImdbId = () => {
    if (movie?.idImdb) {
      navigator.clipboard.writeText(movie.idImdb);
      setCopied(true);
      setTimeout(() => setCopied(false), 1200);
    }
  };

  // Reset all states when imdbId changes
  useEffect(() => {
    setMovie(null);
    setPoster(null);
    setNoPoster(false);
    setCoordinates([]);
    setGenreRecommendations([]);
    setNearbyRecommendations([]);
    setRecommandationMovies([]);
    setActorRecommendations([]);
    setDirectorRecommendations([]);
    setEraRecommendations([]);
  }, [imdbId]);

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
            id: loc.id,
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

  // Fetch genre-based recommendations
  useEffect(() => {
    if (!movie) return;

    const fetchGenreRecommendations = async () => {
      try {
        const response = await axios.get(
          API_ENDPOINTS.relatedMoviesByGenre(movie.idImdb, 6, 3),
        );
        setGenreRecommendations(response.data || []);
      } catch (error) {
        console.error("Error fetching genre recommendations:", error);
        setGenreRecommendations([]);
      }
    };

    fetchGenreRecommendations();
  }, [movie]);

  //Fetch global recommendations
  useEffect(() => {
    if (!movie) return;

    const fetchRecommandationMovies = async () => {
      try {
        const response = await axios.get(
          API_ENDPOINTS.relatedMovies(movie.idImdb, 6, 3),
        );
        // Extract the movie objects from the response
        const movies = (response.data || []).map((item) => item.movie);
        setRecommandationMovies(movies);
      } catch (error) {
        console.error("Error fetching recommendations:", error);
        setRecommandationMovies([]);
      }
    };
    fetchRecommandationMovies();
  }, [movie]);

  // Fetch nearby recommendations
  useEffect(() => {
    if (!movie || coordinates.length === 0) return;

    const fetchNearbyRecommendations = async () => {
      try {
        const movies = await getNearestMovies(coordinates, movie.idImdb, 3);
        setNearbyRecommendations(movies || []);
      } catch (error) {
        console.error("Error fetching nearby recommendations:", error);
        setNearbyRecommendations([]);
      }
    };

    fetchNearbyRecommendations();
  }, [movie, coordinates]);

  // Fetch actor-based recommendations
  useEffect(() => {
    if (!movie) return;

    const fetchActorRecommendations = async () => {
      try {
        const response = await axios.get(
          API_ENDPOINTS.relatedMoviesByActors(movie.idImdb, 3),
        );
        setActorRecommendations(response.data || []);
      } catch (error) {
        console.error("Error fetching actor recommendations:", error);
        setActorRecommendations([]);
      }
    };

    fetchActorRecommendations();
  }, [movie]);

  // Fetch director-based recommendations
  useEffect(() => {
    if (!movie) return;

    const fetchDirectorRecommendations = async () => {
      try {
        const response = await axios.get(
          API_ENDPOINTS.relatedMoviesByDirectors(movie.idImdb, 3),
        );
        setDirectorRecommendations(response.data || []);
      } catch (error) {
        console.error("Error fetching director recommendations:", error);
        setDirectorRecommendations([]);
      }
    };

    fetchDirectorRecommendations();
  }, [movie]);

  // Fetch era-based recommendations
  useEffect(() => {
    if (!movie) return;

    const fetchEraRecommendations = async () => {
      try {
        const response = await axios.get(
          API_ENDPOINTS.relatedMoviesByEra(movie.idImdb, 5, 6, 3),
        );
        setEraRecommendations(response.data || []);
      } catch (error) {
        console.error("Error fetching era recommendations:", error);
        setEraRecommendations([]);
      }
    };

    fetchEraRecommendations();
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
            <h3>Globales :</h3>
            <div className="movie-page-recommandation-list">
              {recommandationMovies.length === 0 ? (
                <p>Aucun film trouvé</p>
              ) : (
                recommandationMovies
                  .slice(0, 3)
                  .map((film) => <FilmCard key={film.idImdb} movie={film} />)
              )}
            </div>
            <h3>A proximité :</h3>
            <div className="movie-page-recommandation-list">
              {nearbyRecommendations.length === 0 ? (
                <p>Aucun film à proximité</p>
              ) : (
                nearbyRecommendations
                  .slice(0, 3)
                  .map((film) => <FilmCard key={film.idImdb} movie={film} />)
              )}
            </div>
            <h3>Dans le même genre :</h3>
            <div className="movie-page-recommandation-list">
              {genreRecommendations.length === 0 ? (
                <p>Aucun film trouvé</p>
              ) : (
                genreRecommendations
                  .slice(0, 3)
                  .map((film) => <FilmCard key={film.idImdb} movie={film} />)
              )}
            </div>
            <h3>Avec les mêmes acteurs :</h3>
            <div className="movie-page-recommandation-list">
              {actorRecommendations.length === 0 ? (
                <p>Aucun film trouvé</p>
              ) : (
                actorRecommendations
                  .slice(0, 3)
                  .map((film) => <FilmCard key={film.idImdb} movie={film} />)
              )}
            </div>
            <h3>Du même réalisateur :</h3>
            <div className="movie-page-recommandation-list">
              {directorRecommendations.length === 0 ? (
                <p>Aucun film trouvé</p>
              ) : (
                directorRecommendations
                  .slice(0, 3)
                  .map((film) => <FilmCard key={film.idImdb} movie={film} />)
              )}
            </div>
            <h3>De la même époque :</h3>
            <div className="movie-page-recommandation-list">
              {eraRecommendations.length === 0 ? (
                <p>Aucun film trouvé</p>
              ) : (
                eraRecommendations
                  .slice(0, 3)
                  .map((film) => <FilmCard key={film.idImdb} movie={film} />)
              )}
            </div>
          </div>
        </div>
      </div>
    </GlowContainer>
  );
};

export default Movie;
