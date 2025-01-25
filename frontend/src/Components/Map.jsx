import React, { useEffect, useState, useRef } from "react";
import {
  MapContainer,
  TileLayer,
  Marker,
  GeoJSON, Popup,
} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import markerIcon from "../assets/icons/marker_map.png";
import attractionIcon from "../assets/icons/attraction.png";
import museumIcon from "../assets/icons/museum.png";
import themeParkIcon from "../assets/icons/theme_park.png";
import zooIcon from "../assets/icons/zoo.png";
import aquariumIcon from "../assets/icons/aquarium.png";
import artworkIcon from "../assets/icons/artwork.png";
import viewpointIcon from "../assets/icons/viewpoint.png";
import informationIcon from "../assets/icons/information.png";
import hotelIcon from "../assets/icons/hotel.png";
import guestHouseIcon from "../assets/icons/guest_house.png";
import campSiteIcon from "../assets/icons/camp_site.png";
import caravanSiteIcon from "../assets/icons/caravan_site.png";
import picnicSiteIcon from "../assets/icons/picnic_site.png";
import alpineHutIcon from "../assets/icons/alpine_hut.png";
import wildernessHutIcon from "../assets/icons/wilderness_hut.png";
import chaletIcon from "../assets/icons/chalet.png";
import galleryIcon from "../assets/icons/gallery.png";
import waterParcIcon from "../assets/icons/water_parc.png";
import apartmentIcon from "../assets/icons/apartment.png";
import stateData from "./custom.geo.json";
import countries from "i18n-iso-countries";
import axios from 'axios';
import BoxDeroulant from "./BoxDeroulant";

const Map = ({ height, width }) => {
  const [markers, setMarkers] = useState([]);
  const [paysToHighlight, setPaysToHighlight] = useState([]);
  const [filteredData, setFilteredData] = useState(null);
  const mapRef = useRef();
  const [pays, setPays] = useState([]);
  const [countryPointCount, setCountryPointCount] = useState([]);
  const [maxCount, setMaxCount] = useState(1);
  const [minCount, setMinCount] = useState(0);
  const [visibleBox, setVisibleBox] = useState(null);
  const [tourismSite, setTourismSite] = useState([]);

  const zoomToMarker = (position, zoomLevel = 15) => {
    const map = mapRef.current;
    if (map) {
      map.setView(position, zoomLevel);
    }
  };

  const fetchLocations = async (idFilm) => {
    try {
      const response = await axios.get(`/locationByImdbId/${idFilm}`);
      return await response.data;
    } catch (error) {
      console.error("Error fetching locations:", error);
      return [];
    }
  };

  useEffect(() => {
    const idFilm = "tt9218128";
    const geocodeAddresses = async () => {
      const locations = await fetchLocations(idFilm);
      const newMarkers = [];
      const newPays = {};
      for (const location of locations) {
        const response = await fetch(
            `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(
                location.locationString
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
    const newCountryPointCount = {};

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

          if (!newCountryPointCount[countryCode]) {
            newCountryPointCount[countryCode] = 0;
          }

          newCountryPointCount[countryCode]++;
        }
      }
    }

    if (Object.keys(newCountryPointCount).length < 2) {
      setMaxCount(1);
      setMinCount(0);
    } else {
      setMaxCount(Math.max(...Object.values(newCountryPointCount)));
      setMinCount(Math.min(...Object.values(newCountryPointCount)));
    }

    setCountryPointCount(newCountryPointCount);
    const uniqueCountryCodes = [...new Set(countryCodes)];
    setPaysToHighlight(uniqueCountryCodes);
  };

  const customIcon = new L.Icon({
    iconUrl: markerIcon,
    iconSize: [41, 41], // Taille de l'icône
    iconAnchor: [20, 41], // Point d'ancrage de l'icône (centré en bas)
    popupAnchor: [1, -34], // Point d'ancrage de la popup par rapport à l'icône
    shadowSize: [41, 41], // Taille de l'ombre de l'icône
  });

  const iconMapping = {
    attraction: attractionIcon,
    museum: museumIcon,
    theme_park: themeParkIcon,
    zoo: zooIcon,
    aquarium: aquariumIcon,
    artwork: artworkIcon,
    viewpoint: viewpointIcon,
    information: informationIcon,
    hotel: hotelIcon,
    guest_house: guestHouseIcon,
    camp_site: campSiteIcon,
    caravan_site: caravanSiteIcon,
    picnic_site: picnicSiteIcon,
    alpine_hut: alpineHutIcon,
    wilderness_hut: wildernessHutIcon,
    chalet: chaletIcon,
    gallery: galleryIcon,
    water_park: waterParcIcon,
    apartment: apartmentIcon,
  };

  const getIcon = (tourismType) => {
    const iconSize = tourismType === 'hotel' ? [12, 12] : [20, 20];
    const iconAnchor = [iconSize[0] / 2, iconSize[1] / 2];
    const shadowSize = tourismType === 'hotel' ? [6, 6] : [10, 10];

    return new L.Icon({
      iconUrl: iconMapping[tourismType] || markerIcon,
      iconSize: iconSize,
      iconAnchor: iconAnchor,
      popupAnchor: [0, -iconSize[1] / 2],
      shadowSize: shadowSize,
    });
  };

  L.Marker.prototype.options.icon = new L.Icon({
    iconUrl: '',
    iconSize: [0, 0],
    iconAnchor: [0, 0],
    popupAnchor: [0, 0],
    shadowSize: [0, 0],
  });

  const countryStyle = (feature) => {
    const countryCode = feature.properties.filename.split(".")[0];
    const pointCount = countryPointCount[countryCode];
    const percent = (pointCount - minCount) / (maxCount - minCount);

    const colorGradient = [
      "#fdd71b",
      "#fdc500",
      "#fcb400",
      "#fba100",
      "#f98f00",
      "#f67b00",
      "#f26700",
      "#ed5104",
      "#e73711",
      "#e00b19"
    ]
    return {
      fillColor: colorGradient[percent*9 | 0],
      weight: 2,
      opacity: 0.25+percent*0.05,
      color: colorGradient[Math.min(percent*9 | 0, 9)],
      dashArray: '8',
      fillOpacity: 0.15+percent*0.05
    };
  }

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

  const handleToggle = (name) => {
    setVisibleBox(visibleBox === name ? null : name);
  };

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
          {tourismSite.map((site, idx) => {
            if (site.type === "node") {
              return (
                  <Marker
                      key={idx}
                      position={[site.lat, site.lon]}
                      icon={getIcon(site.tags.tourism)}
                  >
                  {site.tags.name && (
                      <Popup>
                        <div>
                          <p>{site.tags.name}</p>
                        </div>
                      </Popup>
                  )}
                  </Marker>
              );
            }
            return null;
          })}
          {filteredData && <GeoJSON data={filteredData}  style={countryStyle} />}
        </MapContainer>
        <div style={{ display: "flex", flexDirection: "column", margin: "10px" }}>
          {Object.keys(pays).length > 0 &&
              Object.keys(pays).map((key, index) => (
                  <BoxDeroulant
                      key={index}
                      name={key}
                      data={pays[key]}
                      goToMarqueur={zoomToMarker}
                      onToggle={handleToggle}
                      isVisible={visibleBox === key}
                      setTourismSite={setTourismSite}
                  />
              ))}
        </div>
      </div>
  );
};

export default Map;