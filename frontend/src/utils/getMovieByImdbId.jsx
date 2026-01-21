import axios from "axios";
import API_ENDPOINTS from "../resources/api-links";

const getMovieByImdbId = async (imdbId) => {
  try {
    const response = await axios.get(API_ENDPOINTS.movieByImdbId(imdbId));
    return response.data;
  } catch (error) {
    console.error("Error fetching locations:", error);
    return [];
  }
};

export default getMovieByImdbId;
