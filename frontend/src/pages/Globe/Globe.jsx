// three-scene.js

import React, { useRef, useState, useEffect, useCallback } from "react";
import { Canvas, useFrame } from "@react-three/fiber";
import { OrbitControls } from "@react-three/drei";
import ThreeMesh from "../../components/ThreeMesh/ThreeMesh";
import ThreeGraticule from "../../components/ThreeGraticule/ThreeGraticule";
import ThreeCountry from "../../components/ThreeCountry/ThreeCountry";
import ThreePoints from "../../components/ThreePoints/ThreePoints";
import "./Globe.css";
import { Vector2 } from "three";

const points = [
  { latitude: 37.7749, longitude: -122.4194 }, // San Francisco
  { latitude: 48.8566, longitude: 2.3522 }, // Paris
  { latitude: -33.8688, longitude: 151.2093 }, // Sydney
];

const GlobeUpdater = ({ width, height }) => {
  const orbitRef = useRef(null);
  const mouse = useRef(new Vector2());
  const windowHalf = useRef(new Vector2(width / 2, height / 2));

  useEffect(() => {
    windowHalf.current.set(width / 2, height / 2);
  }, [width, height]);

  const onMouseMove = useCallback(
    (event) => {
      if (width > 0 && height > 0) {
        mouse.current.x =
          (event.clientX - windowHalf.current.x) / windowHalf.current.x;
        mouse.current.y =
          (event.clientY - windowHalf.current.y) / windowHalf.current.y;
      }
    },
    [width, height],
  );

  useEffect(() => {
    document.addEventListener("mousemove", onMouseMove, false);
    return () => {
      document.removeEventListener("mousemove", onMouseMove, false);
    };
  }, [onMouseMove]);

  useFrame(() => {
    if (orbitRef.current) {
      const lerpFactor = 0.1;
      const rotationStrength = 0.2;

      const initialLongitude = 151.2093;
      const initialLatitude = -33.8688;

      const initialAzimuthalAngle = initialLongitude * (Math.PI / 180);
      const initialPolarAngle = (90 - initialLatitude) * (Math.PI / 180);

      const targetAzimuthal =
        initialAzimuthalAngle + mouse.current.x * rotationStrength;

      const targetPolar =
        initialPolarAngle + mouse.current.y * rotationStrength;

      orbitRef.current.setAzimuthalAngle(
        orbitRef.current.getAzimuthalAngle() +
          (targetAzimuthal - orbitRef.current.getAzimuthalAngle()) * lerpFactor,
      );
      orbitRef.current.setPolarAngle(
        orbitRef.current.getPolarAngle() +
          (targetPolar - orbitRef.current.getPolarAngle()) * lerpFactor,
      );

      orbitRef.current.update();
    }
  });

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

const ThreeGlobe = () => {
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
      style={{
        cursor: "move",
      }}
      className="canvas"
      ref={canvasRef}
    >
      {width > 0 && height > 0 && (
        <GlobeUpdater width={width} height={height} />
      )}
    </Canvas>
  );
};

export default ThreeGlobe;
