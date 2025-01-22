import React, { useEffect, useState } from "react";
import {
  MapContainer,
  TileLayer,
  Marker,
  GeoJSON,
  useMap,
} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import { GeoSearchControl, OpenStreetMapProvider } from "leaflet-geosearch";
import "leaflet-geosearch/dist/geosearch.css";
import L from "leaflet";
import customMarkerIcon from "./285659_marker_map_icon.png";
import stateData from "./custom.geo.json";

const SearchField = () => {
  const map = useMap();

  useEffect(() => {
    const provider = new OpenStreetMapProvider();
    const searchControl = new GeoSearchControl({
      provider: provider,
      style: "bar",
      autoComplete: true,
    });

    map.addControl(searchControl);
    return () => map.removeControl(searchControl);
  }, [map]);

  return null;
};

const Map = () => {
  const [markers, setMarkers] = useState([]);

  useEffect(() => {
    const addresses = ["Paris, France", "London, United Kingdom"]; // Remplacez par vos adresses
    const geocodeAddresses = async () => {
      const newMarkers = [];
      for (const address of addresses) {
        const response = await fetch(
          `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(
            address
          )}`
        );
        const data = await response.json();
        if (data.length > 0) {
          const { lat, lon } = data[0];
          newMarkers.push([lat, lon]);
        }
      }
      setMarkers(newMarkers);
    };
    geocodeAddresses();
  }, []);

  const customIcon = new L.Icon({
    iconUrl: customMarkerIcon,
    iconSize: [41, 41], // Taille de l'icône
    iconAnchor: [20, 41], // Point d'ancrage de l'icône (centré en bas)
    popupAnchor: [1, -34], // Point d'ancrage de la popup par rapport à l'icône
    shadowSize: [41, 41], // Taille de l'ombre de l'icône
  });
  const paysToHighlight = ["FRA", "USA"]; // Remplacez par vos pays

  const filteredData = {
    ...stateData,
    features: stateData.features.filter((feature) =>
      paysToHighlight.includes(feature.properties.filename.split(".")[0])
    ),
  };
  console.log(filteredData);

  return (
    <div style={{ height: "100vh", width: "100vw" }}>
      <MapContainer
        center={[51.505, -0.09]}
        zoom={13}
        style={{ height: "100%", width: "100%" }}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        />
        {markers.map((position, idx) => (
          <Marker key={idx} position={position} icon={customIcon} />
        ))}
        <SearchField />
        <GeoJSON data={filteredData} />
      </MapContainer>
    </div>
  );
};

export default Map;
