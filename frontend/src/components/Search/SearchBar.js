import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import './SearchBar.css';
import SearchIcon from '@mui/icons-material/Search';
import useMousePosition from '../../utils/useMousePosition';
import axios from 'axios';

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
        const response = await fetch(`/search?title=${searchWord}`);
        const data = await response.json();
        setFilteredData(data);
      } catch (error) {
        console.error('Error searching films:', error);
      }
    }
  };
  
  const handleMovieClick = async (imdbId) => {
    if (isNavigating) return;
    setIsNavigating(true);
  
    console.log('handleMovieClick called with imdbId:', imdbId);
    try {
      const response = await axios.get(`/movieByImdbId/${imdbId}`, {
        withCredentials: true
      });
      console.log('handleMovieClick - Response received', response.data);
      if (response.data) {
        navigate(`/movie/${imdbId}`);
      }
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