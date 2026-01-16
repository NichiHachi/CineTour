import "./FiltersTab.css";

const FiltersTab = ({ children, className }) => {
  return (
    <div className={`${className} glow`}>
      <div className="glow-content">{children}</div>
    </div>
  );
};

export default FiltersTab;
