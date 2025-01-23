import React, { useState } from "react";
import Map from "./Map";
import BoxDeroulant from "./BoxDeroulant";

const LocationResultatZone = () => {
  const [pays, setPays] = useState([]);
  return (
    <div>
      <Map height="500px" width="500px" setPays={setPays} />
      {Object.keys(pays).length > 0 &&
        Object.keys(pays).map((key, index) => (
          <BoxDeroulant key={index} name={key} data={pays[key]} />
        ))}
    </div>
  );
};
export default LocationResultatZone;
