import './StaggeredText.css'

const StaggeredText = ({ children, className }) => {
  return (
    <div className="staggered-text-container">
      <div className={`${className} staggered-text`}>
        <div className="first">
          {children.split('').map((letter, index) => (
            <span key={index} style={{ '--index': index }} className="letter">
              {letter === ' ' ? '\u00A0' : letter}
            </span>
          ))}
        </div>
        <div className="second">
          {children.split('').map((letter, index) => (
            <span key={index} style={{ '--index': index }} className="letter">
              {letter === ' ' ? '\u00A0' : letter}
            </span>
          ))}
        </div>
      </div>
    </div>
  )
}

export default StaggeredText
