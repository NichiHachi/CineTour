// three-mesh.js

import React from "react";

const ThreeMesh = () => {
  return (
    <mesh>
      <sphereGeometry args={[1, 32]} />
      <meshPhongMaterial color="#191919" transparent={true} opacity={0.8} />
    </mesh>
  );
};

export default ThreeMesh;
