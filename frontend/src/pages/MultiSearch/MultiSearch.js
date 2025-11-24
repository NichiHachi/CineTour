import Searchbar from '../../components/Searchbar/Searchbar'
import './MultiSearch.css'
import GlowContainer from '../../components/GlowContainer/GlowContainer'
import Navbar from '../../components/Navbar/Navbar'
import RevealText from '../../components/TextEffects/RevealText/RevealText'
import RangeSlider from "../../components/RangeSlider/RangeSlider";
import {useState} from "react";
import MultiSelectDropdown from "../../components/MultiSelectDropdown/MultiSelectDropdown";
import MultiSelectButtons from "../../components/MultiSelectButtons/MultiSelectButtons";

const MultiSearch = () => {
    /*exemple d'utilisation du RangeSlider pour sélectionner une plage d'années*/
    const [numberFilm, setNumberFilm] = useState({ min: 1, max: 20 })
    // eslint-disable-next-line no-unused-vars
    console.log("Selected year range:", numberFilm);

    /*exemple d'utilisation du MultiSelectDropdown pour sélectionner plusieurs genres*/
    const [selectedCountries, setSelectedCountries] = useState([])
    const countries=[
        "France", "États-Unis", "Royaume-Uni", "Allemagne", "Italie",
        "Espagne", "Japon", "Corée du Sud", "Canada", "Australie"
    ]
    console.log("Selected countries:", selectedCountries);

    /*exemple d'utilisation du MultiSelectButtons pour sélectionner plusieurs genres*/
    const [selectedGenres, setSelectedGenres] = useState([])
    const genres = [
        "Action", "Aventure", "Comédie", "Drame", "Horreur",
        "Science-Fiction", "Thriller", "Romance", "Animation", "Documentaire"
    ];
    console.log("Selected genres:", selectedGenres);

    return (
        <>
            <GlowContainer className="home">
                <Navbar/>
                <h1>
                    MULTISEARCH : Passer vos prochaines vacances dans un décor de{' '}
                    <RevealText>cinéma</RevealText>
                </h1>
                <Searchbar/>
                <div className="test_multi_search">
                    {/*Exemple d'utilisation du RangeSlider pour sélectionner une plage d'années*/}
                    <RangeSlider
                        min={1}
                        max={20}
                        step={1}
                        initialMinValue={1}
                        initialMaxValue={20}
                        label="Nombre de films à afficher"
                        onChange={setNumberFilm}
                    />

                    {/*Exemple d'utilisation du MultiSelectDropdown pour sélectionner plusieurs pays*/}
                    <MultiSelectDropdown
                        options={countries}
                        selectedValues={selectedCountries}
                        onChange={setSelectedCountries}
                        placeholder="Sélectionner des pays"
                        label="Pays de production"
                    />

                    {/*Exemple d'utilisation du MultiSelectButtons pour sélectionner plusieurs genres*/}
                    <MultiSelectButtons
                        options={genres}
                        selectedValues={selectedGenres}
                        onChange={setSelectedGenres}
                        label="Genres"
                    />
                </div>
            </GlowContainer>
        </>
    )
}

export default MultiSearch
