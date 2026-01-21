import axios from "axios";

import API_ENDPOINTS from "../resources/api-links";

const searchByWord = async (word) => {
  try {
    console.log("Searching " + word);
    const response = await axios.get(API_ENDPOINTS.search(word));
    const arrayResponse = Array.isArray(response.data) ? response.data : [];
    console.log("Search " + word + " found: " + arrayResponse);
    return arrayResponse;
  } catch (error) {
    console.error("Error searching " + word + ":", error);

    return [];
  }
};

export default searchByWord;
