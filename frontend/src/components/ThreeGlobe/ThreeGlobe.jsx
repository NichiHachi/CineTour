// three-scene.js

import React, { useRef, useState, useEffect } from "react";
import { Canvas } from "@react-three/fiber";
import { OrbitControls } from "@react-three/drei";
import ThreeMesh from "../ThreeMesh/ThreeMesh";
import ThreeGraticule from "../ThreeGraticule/ThreeGraticule";
import ThreeCountry from "../ThreeCountry/ThreeCountry";
import ThreePoints from "../ThreePoints/ThreePoints";
import "./ThreeGlobe.css";

const GlobeUpdater = ({ points }) => {
  const orbitRef = useRef(null);
  const topLightRef = useRef(null);
  const bottomLightRef = useRef(null);

  const [color, setColor] = useState("#000");

  // Update color when prefers-color-scheme changes
  useEffect(() => {
    const updateColor = () => {
      const cssColor = getComputedStyle(document.documentElement)
        .getPropertyValue("--color-bg-light")
        .trim();
      setColor(cssColor);
    };

    const mq = window.matchMedia("(prefers-color-scheme: dark)");

    updateColor();
    mq.addEventListener("change", updateColor);

    return () => mq.removeEventListener("change", updateColor);
  }, []);

  // Update Three.js lights when color changes
  useEffect(() => {
    if (topLightRef.current) topLightRef.current.color.set(color);
    if (bottomLightRef.current) bottomLightRef.current.color.set(color);
  }, [color]);

  return (
    <>
      <OrbitControls
        ref={orbitRef}
        enableRotate={true}
        enableZoom={true}
        enablePan={false}
        maxDistance={5}
        minDistance={1.5}
      />
      <ambientLight intensity={3} />
      <pointLight
        ref={topLightRef}
        position={[0, 2, 0]}
        intensity={20}
        color={color}
      />
      <pointLight
        ref={bottomLightRef}
        position={[0, -2, 0]}
        intensity={20}
        color={color}
      />
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
