import React, { useEffect, useState, useRef } from "react";
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
import countries from "i18n-iso-countries";

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

const Map = ({ height, width, setPays }) => {
  const [markers, setMarkers] = useState([]);
  const [paysToHighlight, setPaysToHighlight] = useState([]);
  const [filteredData, setFilteredData] = useState(null);
  const mapRef = useRef();

  const zoomToMarker = (position) => {
    const map = mapRef.current;
    if (map) {
      map.setView(position, 6);
    }
  };

  useEffect(() => {
    const addresses = [
      "Paris, France",
      "London, United Kingdom",
      "Lyon, France",
      "France",
    ];
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
      await updateCountryCodes(newMarkers);
    };
    geocodeAddresses();
  }, []);

  const updateCountryCodes = async (markers) => {
    const countryCodes = [];
    const pays = [];
    for (const [lat, lon] of markers) {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`
      );
      const data = await response.json();
      if (data.address && data.address.country_code) {
        const countryCode = countries.alpha2ToAlpha3(
          data.address.country_code.toUpperCase()
        );

        if (countryCode) {
          countryCodes.push(countryCode);
          if (!pays[data.address.country]) {
            pays[countryCode] = [];
          }
          pays[countryCode].push(data.display_name);
        }
      }
    }
    for (const [lat, lon] of markers) {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`
      );
      const data = await response.json();
      if (data.address && data.address.country_code) {
        const countryCode = countries.alpha2ToAlpha3(
          data.address.country_code.toUpperCase()
        );

        if (countryCode) {
          countryCodes.push(countryCode);
        }
      }
    }
    const uniqueCountryCodes = [...new Set(countryCodes)];
    const uniquePays = [...new Set(pays)];
    console.log(uniqueCountryCodes);
    setPaysToHighlight(uniqueCountryCodes);
  };

  const customIcon = new L.Icon({
    iconUrl: customMarkerIcon,
    iconSize: [41, 41], // Taille de l'icône
    iconAnchor: [20, 41], // Point d'ancrage de l'icône (centré en bas)
    popupAnchor: [1, -34], // Point d'ancrage de la popup par rapport à l'icône
    shadowSize: [41, 41], // Taille de l'ombre de l'icône
  });

  useEffect(() => {
    if (stateData && stateData.features && paysToHighlight.length > 0) {
      console.log(paysToHighlight);
      const filteredFeatures = stateData.features.filter((feature) =>
        paysToHighlight.includes(feature.properties.filename.split(".")[0])
      );
      setFilteredData({
        ...stateData,
        features: filteredFeatures,
      });
    }
  }, [paysToHighlight]);
  console.log(filteredData);

  return (
    <div style={{ height: "100vh", width: "100vw" }}>
      <MapContainer
        center={[51.505, -0.09]}
        zoom={6}
        style={{ height: height, width: width }}
        ref={mapRef}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        />
        {markers.map((position, idx) => (
          <Marker key={idx} position={position} icon={customIcon} />
        ))}
        <SearchField />
        {filteredData && <GeoJSON data={filteredData} />}
      </MapContainer>
      <div>
        {markers.map((position, idx) => (
          <button key={idx} onClick={() => zoomToMarker(position)}>
            Go to Marker {idx + 1}
          </button>
        ))}
      </div>
    </div>
  );
};

export default Map;
