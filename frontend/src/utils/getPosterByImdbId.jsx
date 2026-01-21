import axios from "axios";
import API_ENDPOINTS from "../resources/api-links";

const getPosterByImdbId = async (imdbId) => {
  console.log("getPosterByImdbId:" + imdbId);
  try {
    const response = await axios.get(API_ENDPOINTS.posterByImdbId(imdbId));
    return response.data;
  } catch (error) {
    console.error("Error fetching poster:", error);
    return [];
  }
};

export default getPosterByImdbId;
