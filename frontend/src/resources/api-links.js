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
};
export default API_ENDPOINTS;
