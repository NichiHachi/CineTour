import React, { useState, useEffect, useContext } from "react";
import "./FilmCard.css";
import axios from "axios";
import Glow from "../../components/Glow/Glow";
import API_ENDPOINTS from "../../resources/api-links";
import { LocationContext } from "../../context/LocationContext";
import getMovieCoordinates from "../../utils/getMovieCoordinates";
import formatTime from "../../utils/formatTime";

const FilmCard = ({
  movie,
  onSelect,
  loadingCoordinates,
  coordinates,
  className = "",
}) => {
  const [imageValid, setImageValid] = useState(false);
  const desiredWidth = 380;
  const desiredHeight = 214;
  const [copied, setCopied] = useState(false);
  const [active, setActive] = useState(false);

  const handleCopyImdbId = () => {
    if (movie?.idImdb) {
      navigator.clipboard.writeText(movie.idImdb);
      setCopied(true);
      setTimeout(() => setCopied(false), 1200);
    }
  };

  useEffect(() => {
    if (movie && movie.image) {
      const img = new window.Image();
      img.src = movie.image;
      img.onload = () => {
        if (img.width === desiredWidth && img.height === desiredHeight) {
          setImageValid(false);
        } else {
          setImageValid(true);
        }
      };
      img.onerror = () => setImageValid(false);
      console.log(movie.image);
    }
  }, [movie]);

  const isLoading = !movie;

  return (
    <Glow className={`filmcard ${className} ${active ? "active" : ""}`}>
      <div
        className={`filmcard-section`}
        onClick={() => {
          setActive(!active);
          onSelect && onSelect(movie);
        }}
      >
        <div className="movie-image">
          {isLoading ? (
            <div className="skeleton skeleton-image" />
          ) : imageValid ? (
            <img src={movie.image} alt={movie.title} />
          ) : (
            <div className="skeleton skeleton-image" />
          )}
        </div>

        <div className="movie-content">
          <div className="movie-header">
            {isLoading ? (
              <div className="skeleton skeleton-title" />
            ) : (
              <>
                <div className="movie-title">{movie.title}</div>
                <button
                  className={`movie-imdbid ${copied ? "copied" : ""}`}
                  onClick={handleCopyImdbId}
                >
                  {movie.idImdb}
                </button>
              </>
            )}
          </div>

          <div className="movie-subtitle">
            {isLoading ? (
              <>
                <div className="skeleton skeleton-subtitle" />
                <div className="skeleton skeleton-subtitle" />
              </>
            ) : (
              <>
                <div className="movie-date">{movie.releaseYear}</div>
                <div className="movie-runtime">
                  {formatTime(movie.runtimeMinutes)}
                </div>
              </>
            )}
          </div>

          <div className="movie-genres">
            {isLoading
              ? Array.from({ length: 3 }).map((_, i) => (
                  <div key={i} className="skeleton skeleton-genre" />
                ))
              : movie.genres === null
                ? ""
                : movie.genres.split(",").map((genre) => (
                    <Glow className="genre" key={genre}>
                      <div className="genre-section">{genre}</div>
                    </Glow>
                  ))}
          </div>

          <div className={`movies-coordinates`}>
            <div
              className={`${
                loadingCoordinates
                  ? "skeleton skeleton-coordinates"
                  : "coordinates"
              }`}
            >
              {coordinates
                ? coordinates.length === 0
                  ? "Aucun lieu"
                  : coordinates.length + " lieux"
                : ""}
            </div>
          </div>
        </div>
      </div>
    </Glow>
  );
};

export default FilmCard;
