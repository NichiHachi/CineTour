import axios from "axios";

import API_ENDPOINTS from "../resources/api-links";

const getMovieByImdbId = async (imdbId) => {
  try {
    console.log("Fetching movie " + imdbId);
    const response = await axios.get(API_ENDPOINTS.movieByImdbId(imdbId));
    console.log("Movie " + imdbId + " found: " + response.data);
    return response.data;
  } catch (error) {
    console.error("Error fetching movie " + imdbId + ":", error);

    return [];
  }
};

export default getMovieByImdbId;
