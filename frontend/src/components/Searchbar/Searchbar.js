import React, { useRef, useState, useContext } from "react";
import "./Searchbar.css";
import Glow from "../Glow/Glow";
import SearchIcon from "@mui/icons-material/Search";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import API_ENDPOINTS from "../../resources/api-links";
import { LocationContext } from "../../context/LocationContext";
import RevealText from "../../components/TextEffects/RevealText/RevealText";

const Searchbar = () => {
  const [filteredData, setFilteredData] = useState([]);
  const [isNavigating, setIsNavigating] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const resultsRef = useRef(null);
  const navigate = useNavigate();
  const { setLocationData, setImageData } = useContext(LocationContext);

  const handleFilter = async (event) => {
    const searchWord = event.target.value;
    setSearchQuery(searchWord);

    if (searchWord.length === 0) {
      setFilteredData([]);
    } else {
      try {
        const response = await axios.get(API_ENDPOINTS.search(searchWord), {
          withCredentials: true,
        });
        console.log("API Response:", response.data);

        if (Array.isArray(response.data)) {
          setFilteredData(response.data);
        } else {
          console.warn("Invalid response format:", response.data);
          setFilteredData([]);
        }
      } catch (error) {
        console.error("Error searching films:", error);
        setFilteredData([]);
      }
    }
  };

  const handleSearchSubmit = () => {
    if (searchQuery.trim()) {
      navigate(`/search-results?q=${encodeURIComponent(searchQuery)}`);
      setFilteredData([]);
    }
  };

  const handleKeyPress = (event) => {
    if (event.key === "Enter") {
      handleSearchSubmit();
    }
  };

  const handleMovieClick = async (imdbId) => {
    if (isNavigating) return;
    setIsNavigating(true);

    console.log("handleMovieClick called with imdbId:", imdbId);
    try {
      const response = await axios.get(API_ENDPOINTS.movieByImdbId(imdbId), {
        withCredentials: true,
      });
      console.log("handleMovieClick - Response received", response.data);
      if (response.data) {
        navigate(`/movie/${imdbId}`);
      }
      const responseImage = await axios.post(API_ENDPOINTS.movieImage(imdbId));
      console.log("handleMovieClick - Image response received", responseImage);
      const responseLocation = await fetch(
        API_ENDPOINTS.importLocationByImdbId(imdbId),
        {},
      );
      setImageData(responseImage);
      setLocationData(responseLocation);

      console.log(
        "handleMovieClick - Location response received",
        responseLocation,
      );
    } catch (error) {
      console.error("handleMovieClick - Error:", error);
    } finally {
      setIsNavigating(false);
      console.log("handleMovieClick - END");
    }
  };

  return (
    <Glow className="searchbar">
      <div className="search-section">
        <input
          type="text"
          placeholder="Rechercher un film"
          onChange={handleFilter}
          onKeyPress={handleKeyPress}
        />
        <div
          className="search-icon"
          onClick={handleSearchSubmit}
          style={{ cursor: "pointer" }}
        >
          <SearchIcon />
        </div>
      </div>
      <div
        className={`results-section ${filteredData.length !== 0 ? "show" : ""}`}
        ref={resultsRef}
      >
        {filteredData.slice(0, 10).map((value, key) => (
          <div
            className="result"
            onClick={() => handleMovieClick(value.idImdb)}
            key={key}
          >
            <RevealText delay={key * 0.05} speed={0.005}>
              {value.title}
            </RevealText>
            <div className="left-side">
              <RevealText delay={key * 0.05} speed={0.05}>
                {value.releaseYear}
              </RevealText>
            </div>
          </div>
        ))}
      </div>
    </Glow>
  );
};
export default Searchbar;
