import { useEffect } from 'react'

const useMousePosition = (ref, dependency) => {
  useEffect(() => {
    const element = ref.current

    const handleMouseMove = (e) => {
      const rect = element.getBoundingClientRect()
      const x = e.clientX - rect.left
      const y = e.clientY - rect.top
      element.style.setProperty('--mouse-x', `${x}px`)
      element.style.setProperty('--mouse-y', `${y}px`)
    }

    if (element) {
      element.addEventListener('mousemove', handleMouseMove)
    }

    return () => {
      if (element) {
        element.removeEventListener('mousemove', handleMouseMove)
      }
    }
  }, [ref, dependency])
}

export default useMousePosition
