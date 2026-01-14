import axios from "axios";
import API_ENDPOINTS from "../resources/api-links";

/**
 * Minimal helper to get geocoded coordinates for a movie's shooting locations.
 *
 * Returns an array like:
 * [
 *   { latitude: 34.0522, longitude: -118.2437 },
 *   { latitude: 51.5074, longitude: -0.1278 }
 * ]
 *
 * Notes:
 * - Uses your existing API endpoint: API_ENDPOINTS.locationsByImdbId(imdbId)
 * - Geocodes each locationString via Nominatim (first result only).
 * - Adds a tiny delay between requests to be polite to Nominatim (adjust as needed).
 * - Returns [] on error or when no locations found.
 */
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
      } catch (geocodeErr) {
        // ignore individual geocode failures and continue
        // console.warn('geocode failed for', q, geocodeErr)
      }

      // small delay to avoid hammering Nominatim (increase if you have many locations)
      await new Promise((r) => setTimeout(r, 150));
    }

    return points;
  } catch (err) {
    // console.error('failed to fetch movie locations', err)
    return [];
  }
}

/*
Example usage:

import getMovieCoordinates from 'src/utils/getMovieCoordinates'

const myPoints = await getMovieCoordinates('tt1234567')
*/
