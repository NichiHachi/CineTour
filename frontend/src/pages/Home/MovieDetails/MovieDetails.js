import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';

function MovieDetails() {
  const { imdbId } = useParams();
  const [movie, setMovie] = useState(null);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchMovieDetails = async () => {
      try {
        const response = await axios.get(`/api/movieByImdbId/${imdbId}`, {
          withCredentials: true // Important for sending cookies
        });
        setMovie(response.data);
    } catch (err) {
        if (err.response?.status === 401) {
          navigate('/login');
        } else {
          setError('Failed to load movie details');
          console.error('Error fetching movie details:', err);
        }
      }
    };

    if (imdbId) {
      fetchMovieDetails();
    }
  }, [imdbId, navigate]);

  if (error) return <div>{error}</div>;
  if (!movie) return <div>Loading...</div>;

  return (
    <div className="movie-details">
      {movie.image ? (
        <img 
          src={movie.image} 
          alt={`${movie.title} poster`}
        />
      ) : (
        <div>No image available</div> )}
      <h2>{movie.title}</h2>
      <p><strong>Year:</strong> {movie.releaseYear}</p>
      <p><strong>Runtime:</strong> {movie.runtimeMinutes} minutes</p>
      <p><strong>Genres:</strong> {movie.genres}</p>
    </div>
  );
}

export default MovieDetails;
