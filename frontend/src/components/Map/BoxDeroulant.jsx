import React from 'react'
import axios from 'axios'
import './BoxDeroulant.css'
import Glow from '../Glow/Glow'

const BoxDeroulant = ({
  name,
  data,
  goToMarqueur,
  onToggle,
  isVisible,
  setTourismSite,
}) => {
  const handleClick = (e) => {
    onToggle(name)
    goToMarqueur(data.coordinatesCountry, 5)
  }

  const overpassURL = 'https://overpass-api.de/api/interpreter'

  const tourismRequest = (lat, lon, searchRadius) => {
    const query = `
            [out:json][timeout:25];
            (
                node["tourism"](around:${searchRadius},${lat},${lon});
                node["historic"](around:${searchRadius},${lat},${lon});
                node["leisure"](around:${searchRadius},${lat},${lon});
                node["tourism"](around:${searchRadius},${lat},${lon});
                node["historic"](around:${searchRadius},${lat},${lon});            node["leisure"="amusement_arcade"](around:${searchRadius},${lat},${lon});
                node["leisure"="beach_resort"](around:${searchRadius},${lat},${lon});
                node["leisure"="bird_hide"](around:${searchRadius},${lat},${lon});
                node["leisure"="bowling_alley"](around:${searchRadius},${lat},${lon});
                node["leisure"="dance"](around:${searchRadius},${lat},${lon});
                node["leisure"="dog_park"](around:${searchRadius},${lat},${lon});
                node["leisure"="escape_game"](around:${searchRadius},${lat},${lon});
                node["leisure"="fishing"](around:${searchRadius},${lat},${lon});
                node["leisure"="fitness_centre"](around:${searchRadius},${lat},${lon});
                node["leisure"="garden"](around:${searchRadius},${lat},${lon});
                node["leisure"="golf_course"](around:${searchRadius},${lat},${lon});
                node["leisure"="hackerspace"](around:${searchRadius},${lat},${lon});
                node["leisure"="ice_rink"](around:${searchRadius},${lat},${lon});
                node["leisure"="marina"](around:${searchRadius},${lat},${lon});
                node["leisure"="nature_reserve"](around:${searchRadius},${lat},${lon});
                node["leisure"="park"](around:${searchRadius},${lat},${lon});
                node["leisure"="playground"](around:${searchRadius},${lat},${lon});
                node["leisure"="resort"](around:${searchRadius},${lat},${lon});
                node["leisure"="sauna"](around:${searchRadius},${lat},${lon});
                node["leisure"="stadium"](around:${searchRadius},${lat},${lon});
                node["leisure"="swimming_pool"](around:${searchRadius},${lat},${lon});
                node["leisure"="water_park"](around:${searchRadius},${lat},${lon});
                node["leisure"="wildlife_hide"](around:${searchRadius},${lat},${lon});
                node["natural"="beach"](around:${searchRadius},${lat},${lon});
                node["natural"="peak"](around:${searchRadius},${lat},${lon});
                node["natural"="volcano"](around:${searchRadius},${lat},${lon});
                node["natural"="cave_entrance"](around:${searchRadius},${lat},${lon});
                node["natural"="geyser"](around:${searchRadius},${lat},${lon});
                node["natural"="hot_spring"](around:${searchRadius},${lat},${lon});
                node["natural"="arch"](around:${searchRadius},${lat},${lon});
                node["natural"="cliff"](around:${searchRadius},${lat},${lon});
                node["natural"="dune"](around:${searchRadius},${lat},${lon});
            );
            out body;
            >;
            out skel qt;
        `
    return `${overpassURL}?data=${encodeURIComponent(query)}`
  }

  const fetchTourism = async (coordinates) => {
    try {
      const response = await axios.get(
        tourismRequest(coordinates[0], coordinates[1], 1500)
      ) // 750m pour tout voir sur un zoom 15
      return response.data.elements
    } catch (error) {
      console.error('Error fetching tourism site:', error)
      return []
    }
  }

  return (
    <Glow className="country-box">
      <div onClick={handleClick} className="country-name">
        {name}
      </div>
      <div className="country-locations">
        {isVisible &&
          Object.keys(data)
            .filter((key) => key !== 'coordinatesCountry')
            .map((key, index) => (
              <div
                key={index}
                className="country-location"
                onClick={async () => {
                  const tourismData = await fetchTourism(data[key].coordinates)
                  setTourismSite(tourismData)
                  goToMarqueur(data[key].coordinates)
                }}
              >
                {data[key].address}
              </div>
            ))}
      </div>
    </Glow>
  )
}

export default BoxDeroulant
