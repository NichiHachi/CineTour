import axios from 'axios';
import API_ENDPOINTS from '../resources/api-links';

export const getNearestMovies = async (locations, actualImdbId, minMovies = 5) => {
  try {
    const locationIds = locations
      .filter(loc => loc && loc.id)
      .map(loc => loc.id);

    if (locationIds.length === 0) {
      return [];
    }

    const nearbyImdbIds = await axios.get(
      API_ENDPOINTS.nearbyMovies(locationIds, actualImdbId, minMovies)
    );

    if (!nearbyImdbIds.data || nearbyImdbIds.data.length === 0) {
      return [];
    }

    const moviePromises = nearbyImdbIds.data.map(async (imdbId) => {
      try {
        const movieResponse = await axios.get(API_ENDPOINTS.movieByImdbId(imdbId));
        return movieResponse.data;
      } catch (error) {
        console.error(`Erreur film ${imdbId} :`, error);
        return null;
      }
    });

    const movies = await Promise.all(moviePromises);
    return movies.filter(movie => movie !== null);

  } catch (error) {
    console.error('Erreur films proches :', error);
    throw error;
  }
};
