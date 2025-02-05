import React from 'react'
import './Button.css'
import Glow from '../Glow/Glow'

const Button = ({
  onClick,
  children,
  type = 'button',
  disabled = false,
  className = '',
}) => {
  return (
    <Glow className="button">
      <div className="button-section">{children}</div>
    </Glow>
  )
}

export default Button
