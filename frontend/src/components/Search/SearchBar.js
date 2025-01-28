import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import './SearchBar.css';
import SearchIcon from '@mui/icons-material/Search';
import useMousePosition from '../../utils/useMousePosition';
import axios from 'axios';
import API_ENDPOINTS from '../../resources/api-links';

function SearchBar({ placeholder }) {
  const [filteredData, setFilteredData] = useState([]);
  const [isNavigating, setIsNavigating] = useState(false);
  const searchRef = useRef(null);
  const resultsRef = useRef(null);
  const navigate = useNavigate();

  const handleFilter = async (event) => {
    const searchWord = event.target.value;
    if (searchWord.length === 0) {
      setFilteredData([]);
    } else {
      try {
        console.log('Searching for:', searchWord); // Debug request

        const response = await axios.get(API_ENDPOINTS.search(searchWord), {
          withCredentials: true,
        });

        console.log('Raw response:', response); // Debug full response
        console.log('Response headers:', response.headers); // Debug headers

        if (response.data && Array.isArray(response.data)) {
          console.log('Valid movies array:', response.data);
          setFilteredData(response.data);
        } else {
          console.warn('Invalid response format:', response.data);
          setFilteredData([]);
        }
      } catch (error) {
        console.error('Search error details:', {
          message: error.message,
          response: error.response?.data,
          status: error.response?.status
        });
        setFilteredData([]);
      }
    }
  };

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
      console.log('handleMovieClick - Location response received', responseLocation);
    } catch (error) {
      console.error('handleMovieClick - Error:', error);
    } finally {
      setIsNavigating(false);
      console.log('handleMovieClick - END');
    }
  };

  useMousePosition(searchRef, [filteredData]);
  useMousePosition(resultsRef, [filteredData]);

  return (
    <div className="search">
      <div className="searchInputs" ref={searchRef}>
        <div className="searchInputs-content">
          <input
            type="text"
            placeholder={placeholder}
            onChange={handleFilter}
          />
          <div className="searchIcon">
            <SearchIcon />
          </div>
        </div>
      </div>
      {filteredData.length !== 0 && (
        <div className="dataResult" ref={resultsRef}>
          <div className="dataResult-content">
            {filteredData.slice(0, 5).map((value, key) => (
              <div
                className="dataItem"
                onClick={() => !isNavigating && handleMovieClick(value.idImdb)}
                key={key}
                role="button"
                tabIndex={0}
              >
                <p>{value.title}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

export default SearchBar;
