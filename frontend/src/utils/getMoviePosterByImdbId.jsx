import axios from "axios";
import API_ENDPOINTS from "../resources/api-links";

const getMoviePosterByImdbId = async (imdbId) => {
  try {
    const response = await axios.get(API_ENDPOINTS.posterByImdbId(imdbId));
    return response.data;
  } catch (error) {
    console.error("Error fetching locations:", error);
    return [];
  }
};

export default getMoviePosterByImdbId;
