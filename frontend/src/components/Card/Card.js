import React, { useRef } from 'react'
import './Card.css'
import useMousePosition from '../../utils/useMousePosition'

const CardItem = ({ icon, title, subtitle }) => {
  return (
    <div className="card">
      <div className="card-content">
        <div className="card-image">
          <i className={`fa-duotone fa-${icon}`}></i>
        </div>
        <div className="card-info-wrapper">
          <div className="card-info">
            <i className={`fa-duotone fa-${icon}`}></i>
            <div className="card-info-title">
              <h3>{title}</h3>
              <h4>{subtitle}</h4>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

const Card = () => {
  const cardData = [
    {
      icon: 'apartment',
      title: 'Apartments',
      subtitle: 'Places to be apart. Wait, what?',
    },
    {
      icon: 'blender-phone',
      title: 'Blender Phones',
      subtitle: 'These absolutely deserve to exist.',
    },
    {
      icon: 'person-to-portal',
      title: 'Adios',
      subtitle: 'See you...',
    },
    {
      icon: 'person-from-portal',
      title: 'I mean hello',
      subtitle: '...over here.',
    },
    {
      icon: 'otter',
      title: 'Otters',
      subtitle: 'Look at me, imma cute lil fella.',
    },
  ]

  const ref = useRef(null)
  useMousePosition(ref)

  return (
    <div id="cards" ref={ref}>
      {cardData.map((card, index) => (
        <CardItem
          key={index}
          icon={card.icon}
          title={card.title}
          subtitle={card.subtitle}
        />
      ))}
    </div>
  )
}

export default Card
