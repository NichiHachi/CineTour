import { useEffect } from 'react'

const useMousePosition = (containerRef) => {
  useEffect(() => {
    const container = containerRef.current

    const handleMouseMove = (e) => {
      const cards = container.getElementsByClassName('card')
      for (const card of cards) {
        const rect = card.getBoundingClientRect()
        const x = e.clientX - rect.left
        const y = e.clientY - rect.top
        card.style.setProperty('--mouse-x', `${x}px`)
        card.style.setProperty('--mouse-y', `${y}px`)
      }
    }

    if (container) {
      container.addEventListener('mousemove', handleMouseMove)
    }

    return () => {
      if (container) {
        container.removeEventListener('mousemove', handleMouseMove)
      }
    }
  }, [containerRef])
}

export default useMousePosition