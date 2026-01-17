import React, { useState } from "react";
import "./Search.css";
import GlowContainer from "../../components/GlowContainer/GlowContainer";
import Panel from "../../components/Panel/Panel";
import Navbar from "../../components/Navbar/Navbar";
import FilmCard from "../../components/FilmCard/FilmCard";

const Search = () => {
  const [showLeftPanel, setShowLeftPanel] = useState(true);
  const [showRightPanel, setShowRightPanel] = useState(true);

  const toggleLeftBar = () => {
    setShowLeftPanel(!showLeftPanel);
  };

  const toggleRightbar = () => {
    setShowRightPanel(!showRightPanel);
  };

  return (
    <>
      <GlowContainer className="search-page">
        <Navbar
          advancedSearch="true"
          toggleLeftBar={toggleLeftBar}
          toggleRightbar={toggleRightbar}
        />
        <div className="dashboard">
          <div className={`left-panel ${!showLeftPanel && "hidden"}`}>
            <Panel>Left</Panel>
          </div>
          <Panel>Center</Panel>
          <div className={`right-panel ${!showRightPanel && "hidden"}`}>
            <div className="film-list">
              <FilmCard imdbId={"tt1757678"}></FilmCard>
              <FilmCard imdbId={"tt1630029"}></FilmCard>
              <FilmCard imdbId={"tt1160419"}></FilmCard>
              <FilmCard imdbId={"tt15239678"}></FilmCard>
              <FilmCard imdbId={"tt175767"}></FilmCard>
              <FilmCard imdbId={"tt163002"}></FilmCard>
              <FilmCard imdbId={"tt116041"}></FilmCard>
              <FilmCard imdbId={"tt1523968"}></FilmCard>
              <FilmCard imdbId={"tt3137850"}></FilmCard>
            </div>
          </div>
        </div>
      </GlowContainer>
    </>
  );
};

export default Search;
