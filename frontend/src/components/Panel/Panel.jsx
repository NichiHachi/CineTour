import "./Panel.css";
import Glow from "../Glow/Glow";

const FiltersTab = ({ children, className }) => {
  return (
    <Glow className={`panel ${className}`}>
      <div className="panel-section">{children}</div>
    </Glow>
  );
};

export default FiltersTab;
