import React, { useState } from "react";
import "./Search.css";
import GlowContainer from "../../components/GlowContainer/GlowContainer";
import Panel from "../../components/Panel/Panel";
import Navbar from "../../components/Navbar/Navbar";

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
            <Panel className={``}>Test</Panel>
          </div>
          <Panel className={"center-panel"}>Test</Panel>
          <div className={`right-panel ${!showRightPanel && "hidden"}`}>
            <Panel className={``}>Test</Panel>
          </div>
        </div>
      </GlowContainer>
    </>
  );
};

export default Search;
