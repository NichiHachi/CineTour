import React, { useRef, useState,useContext } from 'react'
import './Searchbar.css'
import Glow from '../Glow/Glow'
import SearchIcon from '@mui/icons-material/Search'
import { useNavigate } from 'react-router-dom'
import axios from 'axios';
import API_ENDPOINTS from '../../resources/api-links';
import { LocationContext } from '../../context/LocationContext';


const Searchbar = () => {
  const [filteredData, setFilteredData] = useState([]);
  const [isNavigating, setIsNavigating] = useState(false);
  const searchRef = useRef(null);
  const resultsRef = useRef(null);
  const navigate = useNavigate();
  const { setLocationData, setImageData } = useContext(LocationContext);

  const handleFilter = async (event) => {
    const searchWord = event.target.value;
    if (searchWord.length === 0) {
      setFilteredData([]);
    } else {
      try {
        const response = await axios.get(API_ENDPOINTS.search(searchWord), {
          withCredentials: true,
        });
        console.log('API Response:', response.data);
        
        if (Array.isArray(response.data)) {
          setFilteredData(response.data);
        } else {
          console.warn('Invalid response format:', response.data);
          setFilteredData([]);
        }
      } catch (error) {
        console.error('Error searching films:', error);
        setFilteredData([]);
      }
    }
  }

  const handleMovieClick = async (imdbId) => {
    if (isNavigating) return;
    setIsNavigating(true);

    console.log('handleMovieClick called with imdbId:', imdbId);
    try {
      const response = await axios.get(API_ENDPOINTS.movieByImdbId(imdbId), {
        withCredentials: true
      });
      console.log('handleMovieClick - Response received', response.data);
      if (response.data) {
        navigate(`/movie/${imdbId}`);
      }
      const responseImage = await axios.post(API_ENDPOINTS.movieImage(imdbId));
      console.log('handleMovieClick - Image response received', responseImage);
      const responseLocation = await fetch(API_ENDPOINTS.importLocationByImdbId(imdbId), {});
      setImageData(responseImage);
      setLocationData(responseLocation);
     

      console.log('handleMovieClick - Location response received', responseLocation);
    } catch (error) {
      console.error('handleMovieClick - Error:', error);
    } finally {
      setIsNavigating(false);
      console.log('handleMovieClick - END');
    }
  }

  return (
    <Glow className="searchbar">
      <div className="search-section">
        <div className="search-input" ref={searchRef}>
          <input
            type="text"
            placeholder="Rechercher un film"
            onChange={handleFilter}
          />
        </div>
        <div className="search-icon">
          <SearchIcon />
        </div>
      </div>
      <div
        className={`results-section ${filteredData.length !== 0 ? 'show' : ''}`}
        ref={resultsRef}
      >
        {filteredData.slice(0, 10).map((value, key) => (
          <div
            className="result"
            onClick={() => handleMovieClick(value.idImdb)}
            key={key}
          >
            <p>{value.title}</p>
          </div>
        ))}
      </div>
    </Glow>
  )
}

export default Searchbar
