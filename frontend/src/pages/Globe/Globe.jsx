// three-scene.js

import React from "react";
import { Canvas } from "@react-three/fiber";
import { OrbitControls } from "@react-three/drei";
import ThreeMesh from "../../components/ThreeMesh/ThreeMesh";
import ThreeGraticule from "../../components/ThreeGraticule/ThreeGraticule";
import ThreeCountry from "../../components/ThreeCountry/ThreeCountry";
import "./Globe.css";

const ThreeScene = () => {
  return (
    <Canvas
      camera={{
        fov: 75,
        position: [0, 0, 2],
      }}
      style={{
        cursor: "move",
      }}
      className="canvas"
    >
      <OrbitControls
        enableRotate={true}
        enableZoom={true}
        enablePan={false}
        maxDistance={3}
        minDistance={1.5}
      />
      <ambientLight intensity={1.3} />
      <pointLight position={[-10, -10, -10]} intensity={0.4} />
      <ThreeMesh />
      <ThreeGraticule />
      <ThreeCountry />
    </Canvas>
  );
};

export default ThreeScene;
