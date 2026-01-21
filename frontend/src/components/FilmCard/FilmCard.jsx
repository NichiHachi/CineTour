import React, { useState, useEffect } from "react";
import "./FilmCard.css";
import Glow from "../../components/Glow/Glow";
import formatTime from "../../utils/formatTime";

import getPosterByImdbId from "../../utils/getPosterByImdbId";

const FilmCard = ({ movie, onSelect, coordinates, className = "" }) => {
  const [copied, setCopied] = useState(false);
  const [active, setActive] = useState(false);

  const [poster, setPoster] = useState();
  const [noPoster, setNoPoster] = useState(false);

  const handleCopyImdbId = () => {
    if (movie.idImdb) {
      navigator.clipboard.writeText(movie.idImdb);
      setCopied(true);
      setTimeout(() => setCopied(false), 1200);
    }
  };

  useEffect(() => {
    const fetchPosterByImdbId = async (imdbId) => {
      const response = await getPosterByImdbId(movie.idImdb);
      if (response) {
        setPoster(response);
      } else {
        setNoPoster(true);
      }
    };

    fetchPosterByImdbId(movie.idImdb);
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
          ) : poster ? (
            <img src={poster} alt={`Poster of ${movie.title}`} />
          ) : noPoster ? (
            <div className="blank" />
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
