import React from "react";
import axios from "axios";

const BoxDeroulant = ({ name, data, goToMarqueur, onToggle, isVisible, setTourismSite }) => {
    const handleClick = (e) => {
        onToggle(name);
        goToMarqueur(data.coordinatesCountry, 5);
    };

    const overpassURL = "https://overpass-api.de/api/interpreter";

    const tourismRequest = (lat, lon, searchRadius) => {
        const query = `
            [out:json][timeout:25];
            (
                node["tourism"](around:${searchRadius},${lat},${lon});
            );
            out body;
            >;
            out skel qt;
        `;
        return `${overpassURL}?data=${encodeURIComponent(query)}`;
    };

    const fetchTourism = async (coordinates) => {
        try {
            const response = await axios.get(tourismRequest(coordinates[0], coordinates[1], 1000)); // 750m pour tout voir sur un zoom 15
            return response.data.elements;
        } catch (error) {
            console.error("Error fetching tourism site:", error);
            return [];
        }
    };

    return (
        <div style={{ border: "1px solid white", color: "white" }}>
            <div onClick={handleClick} style={{ border: "1px solid white" }}>
                {name}
            </div>
            {isVisible &&
                Object.keys(data)
                    .filter((key) => key !== "coordinatesCountry")
                    .map((key, index) => (
                        <div
                            key={index}
                            style={{ border: "1px solid white" }}
                            onClick={async () => {
                                const tourismData = await fetchTourism(data[key].coordinates);
                                console.log(tourismData);
                                setTourismSite(tourismData);
                                goToMarqueur(data[key].coordinates);
                            }}
                        >
                            {data[key].address}
                        </div>
                    ))}
        </div>
    );
};

export default BoxDeroulant;