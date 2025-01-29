const isProduction = process.env.NODE_ENV === 'production';

const API_ENDPOINTS = {
    search: (searchWord) => isProduction ? `/api/search?title=${searchWord}` : `/search?title=${searchWord}`,
    movieByImdbId: (imdbId) => isProduction ? `/api/movieByImdbId/${imdbId}` : `/movieByImdbId/${imdbId}`,
    movieImage: (imdbId) => isProduction ? `/api/addMovieImage/${imdbId}` : `/addMovieImage/${imdbId}`,
    importLocationByImdbId: (imdbId) => isProduction ? `/api/importLocationByImdbId/${imdbId}` : `/importLocationByImdbId/${imdbId}`,
    locationsByImdbId: (imdbId) => isProduction ? `/api/locationByImdbId/${imdbId}` : `/locationByImdbId/${imdbId}`,
    allUsers: isProduction ? '/api/users/all' : '/users/all',
    userById: (id) => isProduction ? `/api/users/${id}` : `/users/${id}`,
    addUser: isProduction ? '/api/users/add' : '/users/add',
    isUserNotExists: isProduction ? '/api/users/isUserNotExist' : '/users/isUserNotExist',
    login: isProduction ? '/api/users/login' : '/users/login',
    logout: isProduction ? '/api/users/logout' : '/users/logout',
    profile: (cookiesUsername) => isProduction ? `/api/users/profile/${cookiesUsername}` : `/users/profile/${cookiesUsername}`,
};

export default API_ENDPOINTS;