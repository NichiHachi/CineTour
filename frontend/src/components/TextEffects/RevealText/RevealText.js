import "./RevealText.css";

const RevealText = ({ children, className, delay = 0.5, speed = 0.05 }) => {
  let text = "";
  if (typeof children === "number") {
    text = children.toString();
  } else if (children) {
    text = children;
  }

  if (!text || text.length === 0) {
    return <span className={`${className} reveal-text`} />;
  }
  return (
    <span className={`${className} reveal-text`}>
      {text.split("").map((letter, index) => (
        <span
          key={index}
          style={{ "--index": index, "--delay": delay, "--speed": speed }}
          className="letter"
        >
          {letter === " " ? "\u00A0" : letter}
        </span>
      ))}
    </span>
  );
};

export default RevealText;
