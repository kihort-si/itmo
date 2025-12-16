import Star from "../../assets/elements/star.svg";
import SemiStar from "../../assets/elements/semi-star.svg";
import NullStar from "../../assets/elements/null-star.svg";
import './Stars.scss';

interface StarsProps {
  count: number;
  onChange?: (count: number) => void;
}

function Stars({ count, onChange }: StarsProps) {
  const handleStarClick = (starValue: number) => {
    if (onChange) {
      onChange(starValue);
    }
  };

  const renderStar = (position: number) => {
    const isClickable = !!onChange;
    const starElement = count >= position
      ? <img src={Star} alt="star" />
      : count >= position - 0.5
        ? <img src={SemiStar} alt="half-star" />
        : <img src={NullStar} alt="empty-star" />;

    return (
      <div
        className={`star ${isClickable ? 'clickable' : ''}`}
        onClick={() => isClickable && handleStarClick(position)}
        key={position}
      >
        {starElement}
      </div>
    );
  };

  return (
    <div className="stars-container">
      {[1, 2, 3, 4, 5].map(renderStar)}
    </div>
  );
}

export default Stars;