import React, { useRef, useState } from "react";
import "./Searchbar.css";
import Glow from "../Glow/Glow";
import SearchIcon from "@mui/icons-material/Search";
import { useNavigate } from "react-router-dom";
import RevealText from "../TextEffects/RevealText/RevealText";
import getMovieByImdbId from "../../utils/getMovieByImdbId";
import searchByWord from "../../utils/searchByWord";

const Searchbar = () => {
  const [isNavigating, setIsNavigating] = useState(false);
  const resultsRef = useRef(null);
  const navigate = useNavigate();
  const timer = { current: null };
  const [searchQuery, setSearchQuery] = useState("");
  const [filteredData, setFilteredData] = useState([]);

  const handleFilter = (event) => {
    const searchWord = event.target.value;
    setSearchQuery(searchWord);
    if (timer.current) clearTimeout(timer.current);
    timer.current = setTimeout(async () => {
      if (!searchWord) {
        setFilteredData([]);
        return;
      }
      const response = await searchByWord(searchWord);
      setFilteredData(response);
    }, 1000);
  };

  const handleKeyDown = (e) => {
    if (e.key === "Escape") {
      setFilteredData([]);
    }
    if (e.key === "Enter") {
      handleSearchSubmit();
    }
  };

  const handleSearchSubmit = () => {
    if (searchQuery.trim()) {
      navigate(`/search?title=${encodeURIComponent(searchQuery)}`);
      setFilteredData([]); // Clear dropdown when navigating
    }
  };

  const handleMovieClick = async (imdbId) => {
    navigate(`/movie/${imdbId}`);
  };

  const handleBlur = () => {
    setFilteredData([]);
  };

  return (
    <Glow className="searchbar">
      <div className="search-section">
        <input
          type="text"
          placeholder="Rechercher un film"
          value={searchQuery}
          onChange={handleFilter}
          onKeyDown={handleKeyDown}
          onBlur={handleBlur}
        />
        <div
          className="search-icon"
          onMouseDown={handleSearchSubmit}
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
            onMouseDown={() => handleMovieClick(value.idImdb)}
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
