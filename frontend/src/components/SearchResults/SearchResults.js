import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import Searchbar from '../../components/Searchbar/Searchbar';
import GlowContainer from '../../components/GlowContainer/GlowContainer';
import MultiSelectDropdown from '../../components/MultiSelectDropdown/MultiSelectDropdown';
import MultiSelectButtons from '../../components/MultiSelectButtons/MultiSelectButtons';
import RangeSlider from '../../components/RangeSlider/RangeSlider';
import StarRating from '../../components/StarRating/StarRating';
import './SearchResults.css';

const SearchResults = () => {
    const location = useLocation();
    const searchParams = new URLSearchParams(location.search);
    const query = searchParams.get('q') || '';

    const [filters, setFilters] = useState({
        countries: [],
        genres: [],
        yearRange: [1900, 2024],
        rating: 0,
        producers: [],
        actors: []
    });

    const [results, setResults] = useState([]);
    const [countries, setCountries] = useState([]);
    const [genres, setGenres] = useState([]);
    const [producers, setProducers] = useState([]);
    const [actors, setActors] = useState([]);

    useEffect(() => {
        // Charger les données de filtres
        fetchFilterData();
    }, []);

    useEffect(() => {
        // Rechercher avec les filtres actuels
        searchMovies();
    }, [query, filters]);

    const fetchFilterData = async () => {
        // Récupérer les pays, genres, producteurs, acteurs depuis l'API
        // À adapter selon votre API
    };

    const searchMovies = async () => {
        // Effectuer la recherche avec les filtres
        // À adapter selon votre API
    };

    return (
        <div className="search-results-page">
            <div className="navbar-with-search">
                <Navbar />
                <div className="centered-searchbar">
                    <Searchbar />
                </div>
            </div>

            <div className="search-results-container">
                <aside className="filters-sidebar">
                    <h2>Filtres</h2>

                    <MultiSelectDropdown
                        label="Pays"
                        options={countries}
                        selectedValues={filters.countries}
                        onChange={(values) => setFilters({ ...filters, countries: values })}
                    />

                    <MultiSelectButtons
                        label="Genres"
                        options={genres}
                        selectedValues={filters.genres}
                        onChange={(values) => setFilters({ ...filters, genres: values })}
                    />

                    <RangeSlider
                        label="Année de sortie"
                        min={1900}
                        max={2024}
                        value={filters.yearRange}
                        onChange={(range) => setFilters({ ...filters, yearRange: range })}
                    />

                    <StarRating
                        label="Popularité minimum"
                        value={filters.rating}
                        onChange={(rating) => setFilters({ ...filters, rating })}
                    />

                    <MultiSelectDropdown
                        label="Producteur"
                        options={producers}
                        selectedValues={filters.producers}
                        onChange={(values) => setFilters({ ...filters, producers: values })}
                    />

                    <MultiSelectDropdown
                        label="Acteur"
                        options={actors}
                        selectedValues={filters.actors}
                        onChange={(values) => setFilters({ ...filters, actors: values })}
                    />
                </aside>

                <GlowContainer className="results-content">
                    <h1>Résultats pour "{query}"</h1>
                    <div className="results-grid">
                        {results.map((movie) => (
                            <div key={movie.id} className="movie-card">
                                {/* Afficher les résultats */}
                            </div>
                        ))}
                    </div>
                </GlowContainer>
            </div>
        </div>
    );
};

export default SearchResults;
