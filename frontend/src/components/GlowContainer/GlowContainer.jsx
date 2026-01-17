import React, { useRef } from 'react'
import './GlowContainer.css'
import useMousePosition from '../../utils/useMousePosition'

const GlowContainer = ({ children, className = '' }) => {
  const mouseRef = useRef(null)
  useMousePosition(mouseRef)

  return (
    <div ref={mouseRef}>
      <div className={`glow-container ${className}`}>{children}</div>
    </div>
  )
}

export default GlowContainer
