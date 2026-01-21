import axios from "axios";
import API_ENDPOINTS from "../resources/api-links";

const getLocationsByImdbId = async (imdbId) => {
  try {
    console.log("getLocationsByImdbId:" + imdbId);
    const response = await axios.get(API_ENDPOINTS.locationsByImdbId(imdbId));

    const locations = Array.isArray(response.data) ? response.data : [];
    const coords = [];

    for (const location of locations) {
      if (location && location.latitude && location.longitude) {
        coords.push({
          latitude: Number(location.latitude),
          longitude: Number(location.longitude),
        });
      }
    }
    return coords;
  } catch (error) {
    console.error("Error fetching locations:", error);
    return [];
  }
};

export default getLocationsByImdbId;
