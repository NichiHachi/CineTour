import React, { useState, useEffect } from "react";
import "./FilmCard.css";
import axios from "axios";
import Glow from "../../components/Glow/Glow";
import API_ENDPOINTS from "../../resources/api-links";
import { useNavigate } from "react-router-dom";

const FilmCard = ({ imdbId, className = "" }) => {
  const [title, setTitle] = useState(null);
  const [releaseYear, setReleaseYear] = useState(null);
  const [runtimeMinutes, setRuntimeMinutes] = useState(null);
  const [genres, setGenres] = useState(null);
  const [image, setImage] = useState(null);
  const [location, setLocation] = useState(null);

  const navigate = useNavigate();

  const formatRuntime = (totalMinutes) => {
    if (totalMinutes == null || totalMinutes < 0) return "";
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    return `${hours ? `${hours}h ` : ""}${minutes || !hours ? `${minutes}min` : ""}`.trim();
  };

  useEffect(() => {
    if (!imdbId) return;

    let cancelled = false;

    const safeJson = async (res) => {
      try {
        const text = await res.text();
        if (!text) return null;
        return JSON.parse(text);
      } catch {
        return null;
      }
    };

    const fetchMovie = async () => {
      try {
        axios
          .get(API_ENDPOINTS.movieByImdbId(imdbId), { withCredentials: true })
          .then((res) => {
            if (cancelled || !res?.data) return;
            const data = res.data;
            setTitle(data.title ?? null);
            setReleaseYear(data.releaseYear ?? null);
            setRuntimeMinutes(data.runtimeMinutes ?? null);
            setGenres(data.genres ?? null);
          })
          .catch(() => {});

        axios
          .post(API_ENDPOINTS.movieImage(imdbId))
          .then((res) => {
            if (cancelled) return;
            setImage(res?.data ?? null);
          })
          .catch(() => {});

        fetch(API_ENDPOINTS.importLocationByImdbId(imdbId))
          .then((res) => (res.ok ? safeJson(res) : null))
          .then((data) => {
            if (cancelled) return;
            setLocation(data);
          })
          .catch(() => {});
      } catch (err) {
        console.error("Error fetching movie details:", err);
      }
    };

    fetchMovie();
    return () => {
      cancelled = true;
    };
  }, [imdbId]);

  return (
    <Glow className={`filmcard ${className}`}>
      <div
        className="filmcard-section"
        onClick={() => navigate(`/movie/${imdbId}`)}
      >
        <div className="movie-image">
          {!image ? (
            <div className="skeleton skeleton-image" />
          ) : (
            <img src={image} alt={title || "movie poster"} />
          )}
        </div>

        <div className="movie-content">
          <div className="movie-title">
            {!title ? <div className="skeleton skeleton-title" /> : title}
          </div>

          <div className="movie-subtitle">
            {!releaseYear ? (
              <div className="skeleton skeleton-subtitle" />
            ) : (
              <div className="movie-date">{releaseYear}</div>
            )}

            {!runtimeMinutes ? (
              <div className="skeleton skeleton-subtitle" />
            ) : (
              <div className="movie-runtime">
                {formatRuntime(runtimeMinutes)}
              </div>
            )}
          </div>

          <div className="movie-genres">
            {!genres
              ? Array.from({ length: 3 }).map((_, i) => (
                  <div key={i} className="skeleton skeleton-genre" />
                ))
              : genres
                  .split(",")
                  .filter(Boolean)
                  .map((genre) => (
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
