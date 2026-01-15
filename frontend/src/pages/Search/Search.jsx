import React from "react";
import Searchbar from "../../components/Searchbar/Searchbar";
import "./Search.css";
import GlowContainer from "../../components/GlowContainer/GlowContainer";
import Navbar from "../../components/Navbar/Navbar";
import RevealText from "../../components/TextEffects/RevealText/RevealText";

const Search = () => {
  return (
    <>
      <GlowContainer className="search-page">
        <Navbar />
      </GlowContainer>
    </>
  );
};

export default Search;
