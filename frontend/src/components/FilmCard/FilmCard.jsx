import React, { useState, useEffect, useContext } from "react";
import "./FilmCard.css";
import axios from "axios";
import Glow from "../../components/Glow/Glow";
import API_ENDPOINTS from "../../resources/api-links";
import { LocationContext } from "../../context/LocationContext";

const FilmCard = ({ movie, className = "" }) => {
  const [imageValid, setImageValid] = useState(false);
  const desiredWidth = 380;
  const desiredHeight = 214;
  const [copied, setCopied] = useState(false);
  const { setLocationData, setImageData } = useContext(LocationContext);

  const formatRuntime = (totalMinutes) => {
    if (
      totalMinutes === null ||
      totalMinutes === undefined ||
      totalMinutes < 0
    ) {
      return "";
    }
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;

    let runtimeString = "";
    if (hours > 0) {
      runtimeString += `${hours}h `;
    }
    if (minutes > 0 || hours === 0) {
      // If there are no hours, still show minutes if they exist
      runtimeString += `${minutes}min`;
    }

    return runtimeString.trim();
  };

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
    <Glow className={`filmcard ${className}`}>
      <div className="filmcard-section">
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
                  {formatRuntime(movie.runtimeMinutes)}
                </div>
              </>
            )}
          </div>

          <div className="movie-genres">
            {isLoading || movie.genres === null
              ? Array.from({ length: 3 }).map((_, i) => (
                  <div key={i} className="skeleton skeleton-genre" />
                ))
              : movie.genres.split(",").map((genre) => (
                  <Glow className="genre" key={genre}>
                    <div className="genre-section">{genre}</div>
                  </Glow>
                ))}
          </div>
        </div>
      </div>
    </Glow>
  );
};

export default FilmCard;
