import React from "react";
import Navbar from "../../components/Navbar/Navbar";
import GlowContainer from "../../components/GlowContainer/GlowContainer";
import MovieBanner from "../../components/MovieBanner/MovieBanner";
import Map from "../../components/Map/Map";
import "./Movie.css";

function Movie() {
  return (
    <GlowContainer className="movie">
      <Navbar searchBar="true" />
    </GlowContainer>
  );
}

export default Movie;
