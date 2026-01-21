const isProduction = process.env.NODE_ENV === "production";

const API_ENDPOINTS = {
  search: (searchWord) =>
    isProduction
      ? `/api/search?title=${searchWord}`
      : `/search?title=${searchWord}`,
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
      : `/neo4j/recommendations/relatedMovies/${imdbId}${query}`;
  },
  relatedMoviesDetailed: (imdbId, minRating, limit) => {
    const params = new URLSearchParams();
    if (minRating) params.append("minRating", minRating);
    if (limit) params.append("limit", limit);
    const query = params.toString() ? `?${params.toString()}` : "";
    return isProduction
      ? `/api/neo4j/recommendations/relatedMovies/${imdbId}/detailed${query}`
      : `/neo4j/recommendations/relatedMovies/${imdbId}/detailed${query}`;
  },
  relatedMoviesOnly: (imdbId, minRating, limit) => {
    const params = new URLSearchParams();
    if (minRating) params.append("minRating", minRating);
    if (limit) params.append("limit", limit);
    const query = params.toString() ? `?${params.toString()}` : "";
    return isProduction
      ? `/api/neo4j/recommendations/relatedMovies/${imdbId}/movies${query}`
      : `/neo4j/recommendations/relatedMovies/${imdbId}/movies${query}`;
  },
  relatedMoviesIds: (imdbId, minRating, limit) => {
    const params = new URLSearchParams();
    if (minRating) params.append("minRating", minRating);
    if (limit) params.append("limit", limit);
    const query = params.toString() ? `?${params.toString()}` : "";
    return isProduction
      ? `/api/neo4j/recommendations/relatedMovies/${imdbId}/ids${query}`
      : `/neo4j/recommendations/relatedMovies/${imdbId}/ids${query}`;
  },
  relatedMoviesByDirectors: (imdbId, limit) => {
    const query = limit ? `?limit=${limit}` : "";
    return isProduction
      ? `/api/neo4j/recommendations/relatedMoviesByDirectors/${imdbId}${query}`
      : `/neo4j/recommendations/relatedMoviesByDirectors/${imdbId}${query}`;
  },
  relatedMoviesByActors: (imdbId, limit) => {
    const query = limit ? `?limit=${limit}` : "";
    return isProduction
      ? `/api/neo4j/recommendations/relatedMoviesByActors/${imdbId}${query}`
      : `/neo4j/recommendations/relatedMoviesByActors/${imdbId}${query}`;
  },
  relatedMoviesByGenre: (imdbId, minRating, limit) => {
    const params = new URLSearchParams();
    if (minRating) params.append("minRating", minRating);
    if (limit) params.append("limit", limit);
    const query = params.toString() ? `?${params.toString()}` : "";
    return isProduction
      ? `/api/neo4j/recommendations/relatedMoviesByGenre/${imdbId}${query}`
      : `/neo4j/recommendations/relatedMoviesByGenre/${imdbId}${query}`;
  },
  relatedMoviesByEra: (imdbId, yearRange, minRating, limit) => {
    const params = new URLSearchParams();
    if (yearRange) params.append("yearRange", yearRange);
    if (minRating) params.append("minRating", minRating);
    if (limit) params.append("limit", limit);
    const query = params.toString() ? `?${params.toString()}` : "";
    return isProduction
      ? `/api/neo4j/recommendations/relatedMoviesByEra/${imdbId}${query}`
      : `/neo4j/recommendations/relatedMoviesByEra/${imdbId}${query}`;
  },
};

export default API_ENDPOINTS;
