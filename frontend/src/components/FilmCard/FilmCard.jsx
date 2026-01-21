import React, { useState, useEffect } from "react";
import "./FilmCard.css";
import Glow from "../../components/Glow/Glow";
import formatTime from "../../utils/formatTime";
import { useNavigate } from "react-router-dom";

import getMoviePosterByImdbId from "../../utils/getMoviePosterByImdbId";

const FilmCard = ({ movie, onSelect, coordinates, className = "" }) => {
  // Click redirection
  const navigate = useNavigate();
  const [copied, setCopied] = useState(false);
  const [active, setActive] = useState(false);

  const handleClick = () => {
    if (movie.idImdb) {
      navigator.clipboard.writeText(movie.idImdb);
      setCopied(true);
      setTimeout(() => setCopied(false), 1200);
      navigate(`/movie/${movie.idImdb}`);
    }
  };

  // Image fetching
  const [moviePoster, setMoviePoster] = useState(null);
  const [isFetchingPoster, setIsFetchingPoster] = useState(false);

  useEffect(() => {
    let cancelled = false;

    const fetchMovie = async () => {
      setIsFetchingPoster(true);
      const data = await getMoviePosterByImdbId(movie.idImdb);
      if (!cancelled) {
        setMoviePoster(data);
        setIsFetchingPoster(false);
      }
    };

    if (movie && !movie.posterPath) {
      fetchMovie();
    } else {
      setMoviePoster(null);
    }

    return () => {
      cancelled = true;
    };
  }, [movie]);

  const isLoading = !movie;
  const posterSrc = movie?.posterPath || moviePoster?.posterPath;
  const showSkeleton = !movie || isFetchingPoster;

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
          {showSkeleton ? (
            <div className="skeleton skeleton-image" />
          ) : posterSrc ? (
            <img src={posterSrc} alt={movie.title} />
          ) : (
            <div className="empty-image" />
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
                  onClick={handleClick}
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
                !coordinates ? "skeleton skeleton-coordinates" : "coordinates"
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
