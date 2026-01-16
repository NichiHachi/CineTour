import React from "react";
import Searchbar from "../../components/Searchbar/Searchbar";
import "./Home.css";
import GlowContainer from "../../components/GlowContainer/GlowContainer";
import Navbar from "../../components/Navbar/Navbar";
import RevealText from "../../components/TextEffects/RevealText/RevealText";

const Home = () => {
  return (
    <>
      <GlowContainer className="home">
        <Navbar />
        <h1>
          Passer vos prochaines vacances dans un décor de{" "}
          <RevealText delay={0.3} speed={0.03}>
            cinéma
          </RevealText>
        </h1>
        <Searchbar />
      </GlowContainer>
    </>
  );
};

export default Home;
