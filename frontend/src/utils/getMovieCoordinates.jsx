import axios from "axios";
import API_ENDPOINTS from "../resources/api-links";

export default async function getMovieCoordinates(imdbId) {
  if (!imdbId) return [];

  try {
    const locationsRes = await axios.get(
      API_ENDPOINTS.locationsByImdbId(imdbId),
    );
    const locations = Array.isArray(locationsRes.data) ? locationsRes.data : [];

    const points = [];

    for (const loc of locations) {
      const q = loc.locationString || loc.address || loc.name;
      if (!q) continue;

      try {
        const geoRes = await axios.get(
          "https://nominatim.openstreetmap.org/search",
          {
            params: { format: "json", q, limit: 1 },
            headers: { "Accept-Language": "en" },
          },
        );

        const geo = Array.isArray(geoRes.data) ? geoRes.data[0] : undefined;
        if (geo && geo.lat && geo.lon) {
          points.push({
            latitude: Number(geo.lat),
            longitude: Number(geo.lon),
          });
        }
      } catch (geocodeErr) {}
      await new Promise((r) => setTimeout(r, 150));
    }

    return points;
  } catch (err) {
    return [];
  }
}

/*
Example usage:

import getMovieCoordinates from 'src/utils/getMovieCoordinates'

const myPoints = await getMovieCoordinates('tt1234567')
*/
