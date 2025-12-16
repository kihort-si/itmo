import './VerticalAccordion.scss';
import React, { useState } from 'react';
import SliderControl from "../../../assets/elements/slider-control.svg";
import SliderControlArrowRight from "../../../assets/elements/slider-control-arrow-right.svg";

interface VerticalAccordionProps {
  header: React.ReactNode;
  content: React.ReactNode;
  defaultOpen?: boolean;
}

function VerticalAccordion({ header, content, defaultOpen = false }: VerticalAccordionProps) {
  const [isOpen, setIsOpen] = useState(defaultOpen);

  const hasContent = React.Children.count(content) > 0;

  return (
    <div className="va-container">
      <div
        className={`va-header ${isOpen ? 'is-open' : ''}`}
      >
        {header}
      </div>
      <div className={`va-content ${isOpen ? 'is-open' : ''}`}>
        <div className="va-inner">
          {content}
        </div>
      </div>
      {hasContent && (
        <button className="va-control" onClick={() => setIsOpen(!isOpen)} aria-label="Next">
          <img src={SliderControl} alt="" aria-hidden/>
          <img src={SliderControlArrowRight} alt="" aria-hidden className={isOpen ? 'is-open-control' : ''}/>
        </button>
      )}
    </div>
  );
}

export default VerticalAccordion;