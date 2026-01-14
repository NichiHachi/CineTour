// three-scene.js

import React, { useRef, useState, useEffect } from "react";
import { Canvas } from "@react-three/fiber";
import { OrbitControls } from "@react-three/drei";
import ThreeMesh from "../../components/ThreeMesh/ThreeMesh";
import ThreeGraticule from "../../components/ThreeGraticule/ThreeGraticule";
import ThreeCountry from "../../components/ThreeCountry/ThreeCountry";
import ThreePoints from "../../components/ThreePoints/ThreePoints";
import "./Earth.css";

const GlobeUpdater = ({ width, height, points }) => {
  const orbitRef = useRef(null);

  return (
    <>
      <OrbitControls
        ref={orbitRef}
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
      <ThreePoints points={points} />
    </>
  );
};

const ThreeGlobe = ({ points = [] }) => {
  const canvasRef = useRef(null);
  const [width, setWidth] = useState(0);
  const [height, setHeight] = useState(0);

  useEffect(() => {
    const updateDimensions = () => {
      if (canvasRef.current) {
        setWidth(canvasRef.current.clientWidth);
        setHeight(canvasRef.current.clientHeight);
      }
    };

    updateDimensions();
    window.addEventListener("resize", updateDimensions);

    return () => {
      window.removeEventListener("resize", updateDimensions);
    };
  }, []);

  return (
    <Canvas
      camera={{
        fov: 40,
        position: [0, 0, 2],
      }}
      style={{}}
      className="canvas"
      ref={canvasRef}
    >
      {width > 0 && height > 0 && (
        <GlobeUpdater width={width} height={height} points={points} />
      )}
    </Canvas>
  );
};

export default ThreeGlobe;
