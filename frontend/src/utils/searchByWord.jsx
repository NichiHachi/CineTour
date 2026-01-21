import axios from "axios";
import API_ENDPOINTS from "../resources/api-links";

const searchByWord = async (word) => {
  try {
    console.log("Searching " + word);
    const response = await axios.get(API_ENDPOINTS.search(word));

    // Handle pageable response
    let arrayResponse = [];
    if (response.data) {
      // Check if it's a pageable object
      if (response.data.content && Array.isArray(response.data.content)) {
        arrayResponse = response.data.content;
      }
      // Fallback for direct array response
      else if (Array.isArray(response.data)) {
        arrayResponse = response.data;
      }
    }

    console.log("Search " + word + " found: ", arrayResponse);
    return arrayResponse;
  } catch (error) {
    console.error("Error searching " + word + ":", error);
    return [];
  }
};

export default searchByWord;
