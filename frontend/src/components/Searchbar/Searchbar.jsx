import React, { useRef, useState } from "react";
import "./Searchbar.css";
import Glow from "../Glow/Glow";
import SearchIcon from "@mui/icons-material/Search";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import API_ENDPOINTS from "../../resources/api-links";
import RevealText from "../TextEffects/RevealText/RevealText";

const Searchbar = () => {
  const [filteredData, setFilteredData] = useState([]);
  const [isNavigating, setIsNavigating] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const navigate = useNavigate();

  const debounceRef = useRef(null);
  const abortRef = useRef(null);
  const requestIdRef = useRef(0);

  const handleFilter = (event) => {
    const value = event.target.value;
    setSearchQuery(value);

    // Cancel pending debounce
    if (debounceRef.current) clearTimeout(debounceRef.current);

    // Cancel in-flight request
    if (abortRef.current) abortRef.current.abort();

    if (!value.trim()) {
      setFilteredData([]);
      return;
    }

    debounceRef.current = setTimeout(async () => {
      const requestId = ++requestIdRef.current;
      const controller = new AbortController();
      abortRef.current = controller;

      try {
        const response = await axios.get(API_ENDPOINTS.search(value), {
          withCredentials: true,
          signal: controller.signal,
        });

        // Ignore stale responses
        if (requestId !== requestIdRef.current) return;

        setFilteredData(Array.isArray(response.data) ? response.data : []);
      } catch (err) {
        if (err.name !== "CanceledError" && err.name !== "AbortError") {
          console.error("Search error:", err);
          setFilteredData([]);
        }
      }
    }, 0);
  };

  const handleSearchSubmit = () => {
    if (!searchQuery.trim()) return;
    if (abortRef.current) abortRef.current.abort();
    if (debounceRef.current) clearTimeout(debounceRef.current);
    setFilteredData([]);
    navigate(`/search?q=${encodeURIComponent(searchQuery)}`);
  };

  const handleKeyPress = (event) => {
    if (event.key === "Enter") handleSearchSubmit();
    if (event.key === "Escape") {
      if (abortRef.current) abortRef.current.abort();
      setFilteredData([]);
    }
  };

  const handleMovieClick = async (imdbId) => {
    if (isNavigating) return;
    setIsNavigating(true);
    try {
      const response = await axios.get(API_ENDPOINTS.movieByImdbId(imdbId), {
        withCredentials: true,
      });
      if (response.data) navigate(`/movie/${imdbId}`);
    } catch (error) {
      console.error("handleMovieClick error:", error);
    } finally {
      setIsNavigating(false);
    }
  };

  const handleBlur = () => setFilteredData([]);

  return (
    <Glow className="searchbar">
      <div className="search-section">
        <input
          type="text"
          placeholder="Rechercher un film"
          onChange={handleFilter}
          onKeyDown={handleKeyPress}
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

      <div className={`results-section ${filteredData.length ? "show" : ""}`}>
        {filteredData.slice(0, 10).map((value, key) => (
          <div
            className="result"
            onMouseDown={() => handleMovieClick(value.idImdb)}
            key={value.idImdb}
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
