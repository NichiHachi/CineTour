import React, { useEffect, useState, useRef } from "react";
import {
  MapContainer,
  TileLayer,
  Marker,
  GeoJSON, Popup,
} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import "leaflet.markercluster/dist/MarkerCluster.css";
import "leaflet.markercluster/dist/MarkerCluster.Default.css";
import "leaflet.markercluster";
import MarkerClusterGroup from "react-leaflet-markercluster";
import L from "leaflet";
import markerIcon from "../../assets/icons/marker_map.png";
import attractionIcon from "../../assets/icons/attraction.png";
import museumIcon from "../../assets/icons/museum.png";
import themeParkIcon from "../../assets/icons/theme_park.png";
import zooIcon from "../../assets/icons/zoo.png";
import aquariumIcon from "../../assets/icons/aquarium.png";
import artworkIcon from "../../assets/icons/artwork.png";
import viewpointIcon from "../../assets/icons/viewpoint.png";
import informationIcon from "../../assets/icons/information.png";
import hotelIcon from "../../assets/icons/hotel.png";
import guestHouseIcon from "../../assets/icons/guest_house.png";
import campSiteIcon from "../../assets/icons/camp_site.png";
import caravanSiteIcon from "../../assets/icons/caravan_site.png";
import picnicSiteIcon from "../../assets/icons/picnic_site.png";
import alpineHutIcon from "../../assets/icons/alpine_hut.png";
import wildernessHutIcon from "../../assets/icons/wilderness_hut.png";
import chaletIcon from "../../assets/icons/chalet.png";
import galleryIcon from "../../assets/icons/gallery.png";
import waterParcIcon from "../../assets/icons/water_parc.png";
import apartmentIcon from "../../assets/icons/apartment.png";
import beachIcon from "../../assets/icons/beach.png";
import peakIcon from "../../assets/icons/peak.png";
import volcanoIcon from "../../assets/icons/volcano.png";
import caveEntranceIcon from "../../assets/icons/cave_entrance.png";
import geyserIcon from "../../assets/icons/geyser.png";
import hotSpringIcon from "../../assets/icons/hot_spring.png";
import archIcon from "../../assets/icons/arch.png";
import cliffIcon from "../../assets/icons/cliff.png";
import duneIcon from "../../assets/icons/dune.png";
import amusementArcadeIcon from "../../assets/icons/amusement_arcade.png";
import beachResortIcon from "../../assets/icons/beach.png";
import birdHideIcon from "../../assets/icons/bird_hide.png";
import bowlingAlleyIcon from "../../assets/icons/bowling_alley.png";
import danceIcon from "../../assets/icons/dance.png";
import dogParkIcon from "../../assets/icons/dog_park.png";
import escapeGameIcon from "../../assets/icons/escape_game.png";
import fishingIcon from "../../assets/icons/fishing.png";
import fitnessCentreIcon from "../../assets/icons/fitness_centre.png";
import gardenIcon from "../../assets/icons/garden.png";
import golfCourseIcon from "../../assets/icons/golf_course.png";
import hackerspaceIcon from "../../assets/icons/hackerspace.png";
import iceRinkIcon from "../../assets/icons/ice_rink.png";
import marinaIcon from "../../assets/icons/marina.png";
import natureReserveIcon from "../../assets/icons/nature_reserve.png";
import parkIcon from "../../assets/icons/park.png";
import playgroundIcon from "../../assets/icons/playground.png";
import resortIcon from "../../assets/icons/resort.png";
import saunaIcon from "../../assets/icons/sauna.png";
import stadiumIcon from "../../assets/icons/stadium.png";
import swimmingPoolIcon from "../../assets/icons/swimming_pool.png";
import wildlifeHideIcon from "../../assets/icons/wildlife_hide.png";
import castleIcon from "../../assets/icons/castle.png";
import archeologyIcon from "../../assets/icons/archeology.png";
import manorIcon from "../../assets/icons/manor.png";
import memorialIcon from "../../assets/icons/memorial.png";
import monumentIcon from "../../assets/icons/monument.png";
import shrineIcon from "../../assets/icons/shrine.png";
import crossIcon from "../../assets/icons/cross.png";
import fortIcon from "../../assets/icons/fort.png";
import cityGateIcon from "../../assets/icons/city_gate.png";
import stateData from "./custom.geo.json";
import countries from "i18n-iso-countries";
import defaultLeisureIcon from "../../assets/icons/default_leisure.png";
import defaultNaturalIcon from "../../assets/icons/default_natural.png";
import defaultHistoricIcon from "../../assets/icons/default_historic.png";
import defaultTourismIcon from "../../assets/icons/default_tourism.png";
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

  const zoomToMarker = (position, zoomLevel = 14) => {
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
    const idFilm = "tt0111161";
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
    beach: beachIcon,
    peak: peakIcon,
    volcano: volcanoIcon,
    cave_entrance: caveEntranceIcon,
    geyser: geyserIcon,
    hot_spring: hotSpringIcon,
    arch: archIcon,
    cliff: cliffIcon,
    dune: duneIcon,
    amusement_arcade: amusementArcadeIcon,
    beach_resort: beachResortIcon,
    bird_hide: birdHideIcon,
    bowling_alley: bowlingAlleyIcon,
    dance: danceIcon,
    dog_park: dogParkIcon,
    escape_game: escapeGameIcon,
    fishing: fishingIcon,
    fitness_centre: fitnessCentreIcon,
    garden: gardenIcon,
    golf_course: golfCourseIcon,
    hackerspace: hackerspaceIcon,
    ice_rink: iceRinkIcon,
    marina: marinaIcon,
    nature_reserve: natureReserveIcon,
    park: parkIcon,
    playground: playgroundIcon,
    resort: resortIcon,
    sauna: saunaIcon,
    stadium: stadiumIcon,
    swimming_pool: swimmingPoolIcon,
    wildlife_hide: wildlifeHideIcon,
    castle: castleIcon,
    archeology: archeologyIcon,
    manor: manorIcon,
    memorial: memorialIcon,
    monument: monumentIcon,
    shrine: shrineIcon,
    cross: crossIcon,
    fort: fortIcon,
    city_gate: cityGateIcon,
  };

  const getIcon = (tags) => {
    // const iconSize = tags.tourism === 'hotel' ? [12, 12] : [20, 20];
    // const iconAnchor = [iconSize[0] / 2, iconSize[1] / 2];
    // const shadowSize = tags.tourism === 'hotel' ? [6, 6] : [10, 10];

    const iconSize = [20, 20];
    const iconAnchor = [iconSize[0] / 2, iconSize[1] / 2];
    const shadowSize = [10, 10];

    let iconUrl = markerIcon;

    if (tags.tourism) {
      iconUrl = iconMapping[tags.tourism] || defaultTourismIcon;
    } else if (tags.historic) {
      iconUrl = iconMapping[tags.historic] || defaultHistoricIcon;
    } else if (tags.leisure){
      iconUrl = iconMapping[tags.leisure] || defaultLeisureIcon;
    } else if(tags.natural){
      iconUrl = iconMapping[tags.natural] || defaultNaturalIcon
    }

    return new L.Icon({
      iconUrl: iconUrl,
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

  const createClusterIcon = (cluster) => {
    const markers = cluster.getAllChildMarkers();
    const iconUrls = markers.map(marker => marker.options.icon.options.iconUrl);
    const uniqueIconUrls = [...new Set(iconUrls)];

    const iconHtml = uniqueIconUrls.map(url => `<img src="${url}" style="width: 20px; height: 20px;" />`).join('');

    return L.divIcon({
      html: `<div style="display: flex; flex-wrap: wrap; justify-content: center; align-items: center;">${iconHtml}</div>`,
      className: 'custom-cluster-icon',
      iconSize: L.point(40, 40, true),
      iconAnchor: [20, 20],
    });
  };

  const createClusterMarkerIcon = (cluster) => {
    const markers = cluster.getAllChildMarkers();
    const iconUrl = markers[0].options.icon.options.iconUrl;

    // Create a custom icon based on the first icon inside the cluster
    const iconHtml = `<img src="${iconUrl}" style="width: 40px; height: 40px;" />`;

    return L.divIcon({
      html: `<div style="display: flex; justify-content: center; align-items: center;">${iconHtml}</div>`,
      className: 'custom-cluster-icon',
      iconSize: L.point(40, 40, true),
    });
  };

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
          <MarkerClusterGroup maxClusterRadius={20} iconCreateFunction={createClusterMarkerIcon}>
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
          </MarkerClusterGroup>
          <MarkerClusterGroup maxClusterRadius={20} iconCreateFunction={createClusterIcon}>
          {tourismSite.map((site, idx) => {
            if (site.type === "node") {
              return (
                  <Marker
                      key={idx}
                      position={[site.lat, site.lon]}
                      icon={getIcon(site.tags)}
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
          </MarkerClusterGroup>
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