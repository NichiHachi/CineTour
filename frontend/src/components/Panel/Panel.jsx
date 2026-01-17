import "./Panel.css";
import Glow from "../Glow/Glow";

const FiltersTab = ({ children, className }) => {
  return <Glow className={`${className} panel`}>{children}</Glow>;
};

export default FiltersTab;
