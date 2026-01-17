import React, { useState, useEffect } from "react";
import "./FilmCard.css";
import axios from "axios";
import Glow from "../../components/Glow/Glow";
import API_ENDPOINTS from "../../resources/api-links";

const FilmCard = ({ imdbId, className = "" }) => {
  const [movie, setMovie] = useState(null);

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

  useEffect(() => {
    const FetchMovieDetails = async () => {
      try {
        const response = await axios.get(API_ENDPOINTS.movieByImdbId(imdbId), {
          withCredentials: true, // Important for sending cookies
        });
        setMovie(response.data);
      } catch (err) {
        console.error("Error fetching movie details:", err);
      }
    };

    if (imdbId) {
      FetchMovieDetails();
    }
  }, [imdbId]);

  const isLoading = !movie;

  return (
    <Glow className={`filmcard ${className}`}>
      <div className="filmcard-section">
        <div className="movie-image">
          {isLoading ? (
            <div className="skeleton skeleton-image" />
          ) : movie.image ? (
            <img src={movie.image} alt={movie.title} />
          ) : (
            <div className="skeleton skeleton-image" />
          )}
        </div>

        <div className="movie-content">
          <div className="movie-title">
            {isLoading ? (
              <div className="skeleton skeleton-title" />
            ) : (
              movie.title
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
            {isLoading
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
