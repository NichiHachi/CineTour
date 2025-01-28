import './Glow.css'

const Glow = ({ children, className }) => {
  return (
    <div className={`${className} glow`}>
      <div className="glow-content">{children}</div>
    </div>
  )
}

export default Glow
