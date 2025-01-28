import './RevealText.css'

const RevealText = ({ children, className }) => {
  return (
    <span className={`${className} reveal-text`}>
      {children.split('').map((letter, index) => (
        <span key={index} style={{ '--index': index }} className="letter">
          {letter === ' ' ? '\u00A0' : letter}
        </span>
      ))}
    </span>
  )
}

export default RevealText
