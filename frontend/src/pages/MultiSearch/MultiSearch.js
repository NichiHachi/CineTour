import Searchbar from '../../components/Searchbar/Searchbar'
import './MultiSearch.css'
import GlowContainer from '../../components/GlowContainer/GlowContainer'
import Navbar from '../../components/Navbar/Navbar'
import RevealText from '../../components/TextEffects/RevealText/RevealText'
import RangeSlider from "../../components/RangeSlider/RangeSlider";
import {useState} from "react";

const MultiSearch = () => {
    /*exemple d'utilisation du RangeSlider pour sélectionner une plage d'années*/
    const [yearRange, setYearRange] = useState({ min: 1900, max: 2024 })
    // eslint-disable-next-line no-unused-vars
    console.log("Selected year range:", yearRange);
    return (
        <>
            <GlowContainer className="home">
                <Navbar/>
                <h1>
                    MULTISEARCH : Passer vos prochaines vacances dans un décor de{' '}
                    <RevealText>cinéma</RevealText>
                </h1>
                <Searchbar/>
                {/*Exemple d'utilisation du RangeSlider pour sélectionner une plage d'années*/}
                <RangeSlider
                    min={1900}
                    max={2024}
                    step={1}
                    initialMinValue={1900}
                    initialMaxValue={2024}
                    label="Année de sortie"
                    onChange={setYearRange}
                />
            </GlowContainer>
        </>
    )
}

export default MultiSearch
