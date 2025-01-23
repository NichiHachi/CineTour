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
import BoxDeroulant from "./BoxDeroulant";

const Map = ({ height, width }) => {
  const [markers, setMarkers] = useState([]);
  const [paysToHighlight, setPaysToHighlight] = useState([]);
  const [filteredData, setFilteredData] = useState(null);
  const mapRef = useRef();
  const [pays, setPays] = useState([]);

  const zoomToMarker = (position) => {
    const map = mapRef.current;
    if (map) {
      map.setView(position, 5);
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
      const newPays = {};
      for (const address of addresses) {
        const response = await fetch(
          `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(
            address
          )}`
        );
        const data = await response.json();
        if (data.length > 0) {
          const { lat, lon, display_name } = data[0];
          newMarkers.push([lat, lon]);
          const country = display_name
            .split(",")
            [display_name.split(",").length - 1].trim();

          if (!newPays[country]) {
            newPays[country] = [];
          }
          newPays[country].push({
            address: display_name,
            coordinates: [lat, lon],
          });
        }
      }
      setMarkers(newMarkers);

      await fetchCountryCoordinates(newPays);

      await updateCountryCodes(newMarkers);
    };
    geocodeAddresses();
  }, []);

  const fetchCountryCoordinates = async (newPays) => {
    const updatedPays = { ...newPays };
    for (const country in newPays) {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(
          country
        )}`
      );
      const data = await response.json();
      if (data.length > 0) {
        const { lat, lon } = data[0];
        updatedPays[country] = {
          ...updatedPays[country],
          coordinatesCountry: [lat, lon],
        };
      }
    }
    setPays(updatedPays);
  };

  const updateCountryCodes = async (markers) => {
    const countryCodes = [];
    for (const [lat, lon] of markers) {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`
      );
      const data = await response.json();
      if (data.address && data.address.country_code) {
        console.log(data);
        const countryCode = countries.alpha2ToAlpha3(
          data.address.country_code.toUpperCase()
        );

        if (countryCode) {
          countryCodes.push(countryCode);
        }
      }
    }
    const uniqueCountryCodes = [...new Set(countryCodes)];
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
      const filteredFeatures = stateData.features.filter((feature) =>
        paysToHighlight.includes(feature.properties.filename.split(".")[0])
      );
      setFilteredData({
        ...stateData,
        features: filteredFeatures,
      });
    }
  }, [paysToHighlight]);

  return (
    <div style={{ height: height, width: height, margin: "10px" }}>
      <MapContainer
        center={[0, 0]}
        zoom={1}
        style={{ height: height, width: width }}
        ref={mapRef}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        />
        {markers.map((position, idx) => (
          <Marker
            key={idx}
            position={position}
            icon={customIcon}
            eventHandlers={{
              click: () => {
                zoomToMarker(position);
              },
            }}
          />
        ))}
        {filteredData && <GeoJSON data={filteredData} />}
      </MapContainer>
      <div style={{ display: "flex", flexDirection: "column", margin: "10px" }}>
        {Object.keys(pays).length > 0 &&
          Object.keys(pays).map((key, index) => (
            <BoxDeroulant
              key={index}
              name={key}
              data={pays[key]}
              goToMarqueur={zoomToMarker}
            />
          ))}
      </div>
    </div>
  );
};

export default Map;
