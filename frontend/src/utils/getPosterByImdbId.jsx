import axios from "axios";

import API_ENDPOINTS from "../resources/api-links";

const getPosterByImdbId = async (imdbId) => {
  try {
    console.log("Fetching poster of " + imdbId);
    const response = await axios.get(API_ENDPOINTS.posterByImdbId(imdbId));
    console.log("Poster of " + imdbId + " found: " + response.data);
    return response.data;
  } catch (error) {
    console.error("Error fetching poster:", error);

    return [];
  }
};

export default getPosterByImdbId;
