import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import axios from 'axios'
import Glow from '../Glow/Glow'
import './MovieBanner.css'
import API_ENDPOINTS from '../../resources/api-links';

function MovieBanner() {
  const { imdbId } = useParams()
  const [movie, setMovie] = useState(null)
  const [error, setError] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    const fetchMovieDetails = async () => {
      try {
        const response = await axios.get(API_ENDPOINTS.movieByImdbId(imdbId), {
          withCredentials: true, // Important for sending cookies
        })
        setMovie(response.data)
      } catch (err) {
        if (err.response?.status === 401) {
          navigate('/login')
        } else {
          setError('Failed to load movie details')
          console.error('Error fetching movie details:', err)
        }
      }
    }

    if (imdbId) {
      fetchMovieDetails()
    }
  }, [imdbId, navigate])

  if (error) return <div>{error}</div>
  if (!movie) return <div>Loading...</div>

  return (
    <>
      <div className="movie-banner">
        <div className="movie-header">
          <div className="movie-poster-container">
            <Glow className="movie-poster">
              {movie.image ? (
                <img src={movie.image} alt={`${movie.title} poster`} />
              ) : (
                <div>No image available</div>
              )}
            </Glow>
          </div>
        </div>
        <div className="movie-footer">
          <div className="movie-title-container">
            <div className="movie-title">{movie.title}</div>
          </div>
          <div className="movie-details-container">
            <div className="movie-genres">
              {movie.genres.split(',').map((genre) => (
                <Glow className="tag" key={genre}>
                  <div className="tag-text">{genre}</div>
                </Glow>
              ))}
            </div>
            <div className="move-date">{movie.releaseYear}</div>
            <div className="move-runtime">{movie.runtimeMinutes} minutes</div>
          </div>
        </div>
      </div>
      <Glow className="division"> </Glow>
      <div className="movie-location"></div>
    </>
  )
}

export default MovieBanner
