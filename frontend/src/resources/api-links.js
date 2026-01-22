const isProduction = process.env.NODE_ENV === "production";

const API_ENDPOINTS = {
  search: (params) => {
    const baseUrl = isProduction ? `/api/search` : `/search`;
    if (typeof params === "string") {
      // Backward compatibility for simple string queries
      return `${baseUrl}?title=${params}`;
    }
    // Build query string from params object
    const queryString = new URLSearchParams(params).toString();
    return `${baseUrl}?${queryString}`;
  },
  movieByImdbId: (imdbId) =>
    isProduction ? `/api/movieByImdbId/${imdbId}` : `/movieByImdbId/${imdbId}`,
  locationsByImdbId: (imdbId) =>
    isProduction
      ? `/api/locationByImdbId/${imdbId}`
      : `/locationByImdbId/${imdbId}`,
  posterByImdbId: (imdbId) =>
    isProduction
      ? `/api/posterByImdbId/${imdbId}`
      : `/posterByImdbId/${imdbId}`,
  allUsers: isProduction ? "/api/users/all" : "/users/all",
  userById: (id) => (isProduction ? `/api/users/${id}` : `/users/${id}`),
  addUser: isProduction ? "/api/users/add" : "/users/add",
  isUserNotExists: isProduction
    ? "/api/users/isUserNotExist"
    : "/users/isUserNotExist",
  login: isProduction ? "/api/users/login" : "/users/login",
  logout: isProduction ? "/api/users/logout" : "/users/logout",
  profile: (cookiesUsername) =>
    isProduction
      ? `/api/users/profile/${cookiesUsername}`
      : `/users/profile/${cookiesUsername}`,
  rating: (imdbId) =>
    isProduction
      ? `/api/ratingByImdbId/${imdbId}`
      : `/ratingByImdbId/${imdbId}`,
  directors: (imdbId) =>
    isProduction
      ? `/api/directorsByImdbId/${imdbId}`
      : `/directorsByImdbId/${imdbId}`,
  principals: (imdbId) =>
    isProduction
      ? `/api/principalsByImdbId/${imdbId}`
      : `/principalsByImdbId/${imdbId}`,
  person: (nconst) =>
    isProduction
      ? `/api/personByNconst/${nconst}`
      : `/personByNconst/${nconst}`,

  // Recommandations de films (Neo4j)
  relatedMovies: (imdbId, minRating, limit) => {
    const params = new URLSearchParams();
    if (minRating) params.append("minRating", minRating);
    if (limit) params.append("limit", limit);
    const query = params.toString() ? `?${params.toString()}` : "";
    return isProduction
      ? `/api/neo4j/recommendations/relatedMovies/${imdbId}${query}`
      : `/api/neo4j/recommendations/relatedMovies/${imdbId}${query}`;
  },
  relatedMoviesDetailed: (imdbId, minRating, limit) => {
    const params = new URLSearchParams();
    if (minRating) params.append("minRating", minRating);
    if (limit) params.append("limit", limit);
    const query = params.toString() ? `?${params.toString()}` : "";
    return isProduction
      ? `/api/neo4j/recommendations/relatedMovies/${imdbId}/detailed${query}`
      : `/api/neo4j/recommendations/relatedMovies/${imdbId}/detailed${query}`;
  },
  relatedMoviesOnly: (imdbId, minRating, limit) => {
    const params = new URLSearchParams();
    if (minRating) params.append("minRating", minRating);
    if (limit) params.append("limit", limit);
    const query = params.toString() ? `?${params.toString()}` : "";
    return isProduction
      ? `/api/neo4j/recommendations/relatedMovies/${imdbId}/movies${query}`
      : `/api/neo4j/recommendations/relatedMovies/${imdbId}/movies${query}`;
  },
  relatedMoviesIds: (imdbId, minRating, limit) => {
    const params = new URLSearchParams();
    if (minRating) params.append("minRating", minRating);
    if (limit) params.append("limit", limit);
    const query = params.toString() ? `?${params.toString()}` : "";
    return isProduction
      ? `/api/neo4j/recommendations/relatedMovies/${imdbId}/ids${query}`
      : `/api/neo4j/recommendations/relatedMovies/${imdbId}/ids${query}`;
  },
  relatedMoviesByDirectors: (imdbId, limit) => {
    const query = limit ? `?limit=${limit}` : "";
    return isProduction
      ? `/api/neo4j/recommendations/relatedMoviesByDirectors/${imdbId}${query}`
      : `/api/neo4j/recommendations/relatedMoviesByDirectors/${imdbId}${query}`;
  },
  relatedMoviesByActors: (imdbId, limit) => {
    const query = limit ? `?limit=${limit}` : "";
    return isProduction
      ? `/api/neo4j/recommendations/relatedMoviesByActors/${imdbId}${query}`
      : `/api/neo4j/recommendations/relatedMoviesByActors/${imdbId}${query}`;
  },
  relatedMoviesByGenre: (imdbId, minRating, limit) => {
    const params = new URLSearchParams();
    if (minRating) params.append("minRating", minRating);
    if (limit) params.append("limit", limit);
    const query = params.toString() ? `?${params.toString()}` : "";
    return isProduction
      ? `/api/neo4j/recommendations/relatedMoviesByGenre/${imdbId}${query}`
      : `/api/neo4j/recommendations/relatedMoviesByGenre/${imdbId}${query}`;
  },
  relatedMoviesByEra: (imdbId, yearRange, minRating, limit) => {
    const params = new URLSearchParams();
    if (yearRange) params.append("yearRange", yearRange);
    if (minRating) params.append("minRating", minRating);
    if (limit) params.append("limit", limit);
    const query = params.toString() ? `?${params.toString()}` : "";
    return isProduction
      ? `/api/neo4j/recommendations/relatedMoviesByEra/${imdbId}${query}`
      : `/api/neo4j/recommendations/relatedMoviesByEra/${imdbId}${query}`;
  },
  nearbyMovies: (locationIds, excludeImdbId, minMovies) => {
    const params = new URLSearchParams();
    locationIds.forEach((id) => params.append("locationIds", id));
    params.append("excludeImdbId", excludeImdbId);
    if (minMovies) params.append("minMovies", minMovies);
    const query = params.toString() ? `?${params.toString()}` : "";
    return isProduction
      ? `/api/neo4j/locations/rng/nearby${query}`
      : `/api/neo4j/locations/rng/nearby${query}`;
  },
};
export default API_ENDPOINTS;
