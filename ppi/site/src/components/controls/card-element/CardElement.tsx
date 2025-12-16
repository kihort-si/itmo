import './CardElement.scss';
import Stars from "../../stars/Stars.tsx";
import {useEffect, useRef, useState} from "react";

interface CardElementProps {
  title: string;
  description: string;
  imageUrl: string;
  onClick?: () => void;
  button: boolean;
  buttonLabel?: string;
  buttonIconUrl?: string;
  onButtonClick?: () => void;
  starsCount?: number;
  infoDecoration?: string;
  buttonChanged?: boolean;
  buttonChangedLabel?: string;
  buttonChangedIconUrl?: string;
  isButtonActive?: boolean;
  isAuthenticated?: boolean;
  onUnauthorized?: () => void;
  buttonClicked?: boolean;
}

function CardElement({
  title,
  description,
  imageUrl,
  onClick,
  button,
  buttonLabel,
  buttonIconUrl,
  onButtonClick,
  starsCount,
  infoDecoration,
  buttonChanged,
  buttonChangedLabel,
  buttonChangedIconUrl,
  isAuthenticated = true,
  onUnauthorized,
  buttonClicked = false
}: CardElementProps) {
  const [clicked, setClicked] = useState(buttonClicked);
  const [buttonImg, setButtonImg] = useState(buttonIconUrl);
  const [buttonLbl, setButtonLbl] = useState(buttonLabel);
  const [isOverflowing, setIsOverflowing] = useState(false);
  const textRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const el = textRef.current;
    if (!el) return;

    const checkOverflow = () => {
      setIsOverflowing(el.scrollWidth > el.clientWidth + 1);
    };

    checkOverflow();
    window.addEventListener('resize', checkOverflow);
    return () => window.removeEventListener('resize', checkOverflow);
  }, [title, description]);

  const handleClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    
    if (!isAuthenticated) {
      onUnauthorized?.();
      return;
    }

    if (buttonChanged) {
      setClicked(!clicked);
      setButtonImg(buttonImg === buttonIconUrl ? buttonChangedIconUrl : buttonIconUrl);
      setButtonLbl(buttonLbl === buttonLabel ? buttonChangedLabel : buttonLabel);
    }
    onButtonClick?.();
  }

  return (
    <div className="card-element" onClick={onClick} style={onClick ? { cursor: 'pointer' } : undefined}>
      <div className="top-card-container">
        <img src={imageUrl} alt={title} className="card-image" />
      </div>
      <div className="card-info">
        {starsCount || infoDecoration ?
          <div className="card-info-decoration">
            {starsCount ? <Stars count={starsCount}/> : null}
            {infoDecoration ? <span className="info-decoration-text">{infoDecoration}</span> : null}
          </div> : null}
        <div ref={textRef}
             className={`card-text-line ${isOverflowing ? 'card-text-line--marquee' : ''}`}
        >
          <span className="card-title">{title}</span>
          <span className="card-description">{description}</span>
        </div>
        {button && (
          <button className={`card-button ${clicked ? 'card-button--clicked' : ''}`} onClick={handleClick}>
            {buttonIconUrl && <img src={buttonImg} alt="icon" className="button-icon" />}
            {buttonLbl}
          </button>
        )}
      </div>
    </div>
  );
}

export default CardElement;