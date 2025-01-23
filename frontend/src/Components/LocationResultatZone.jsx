import React, { useState } from "react";
import Map from "./Map";
import BoxDeroulant from "./BoxDeroulant";

const LocationResultatZone = () => {
  const [pays, setPays] = useState([]);
  return (
    <div>
      <Map height="500px" width="500px" setPays={setPays} />
      {pays.length > 0 && <BoxDeroulant name={pays} data={pays} />}
    </div>
  );
};
export default LocationResultatZone;
