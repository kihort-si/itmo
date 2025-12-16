import React, {useMemo, useRef, useState, useEffect} from "react";
import "./HorizontalSlider.scss";
import SliderControl from "../../../assets/elements/slider-control.svg";
import SliderControlArrowLeft from "../../../assets/elements/slider-control-arrow-left.svg";
import SliderControlArrowRight from "../../../assets/elements/slider-control-arrow-right.svg";

interface HorizontalSliderProps {
  maximumElements?: number;
  elementsOnPage?: number;
  children: React.ReactNode;
}

export default function HorizontalSlider({maximumElements = 12, elementsOnPage = 4, children}: HorizontalSliderProps) {
  const items = useMemo(
    () => React.Children.toArray(children).slice(0, maximumElements).filter(Boolean),
    [children, maximumElements]
  );

  const pages = useMemo(() => {
    const arr: React.ReactNode[][] = [];
    for (let i = 0; i < items.length; i += elementsOnPage) {
      arr.push(items.slice(i, i + elementsOnPage));
    }
    return arr.length ? arr : [[]];
  }, [items, elementsOnPage]);

  const realCount: number = pages.length;

  const [idx, setIdx] = useState(1);
  const [withTransition, setWithTransition] = useState(true);
  const trackRef = useRef<HTMLDivElement>(null);

  useEffect((): void => {
    setIdx(1);
  }, [realCount]);

  const nextPage = (): void => {
    if (idx === realCount) {
      setIdx(1);
    } else {
      setIdx(i => i + 1);
    }
  }
  const prevPage = (): void => {
    if (idx === 1) {
      setIdx(realCount);
    } else {
      setIdx(i => i - 1);
    }
  }

  useEffect(() => {
    const el = trackRef.current;
    if (!el) return;

    const onEnd = (): void => {
      if (idx === realCount + 1) {
        setWithTransition(false);
        setIdx(1);
        void el.offsetWidth;
        setWithTransition(true);
      } else if (idx === 0) {
        setWithTransition(false);
        setIdx(realCount);
        void el.offsetWidth;
        setWithTransition(true);
      }
    };
    el.addEventListener("transitionend", onEnd);
    return (): void => el.removeEventListener("transitionend", onEnd);
  }, [idx, realCount]);

  const extended = useMemo(() => {
    const first = pages[0] ?? [];
    const last = pages[realCount - 1] ?? [];
    return [last, ...pages, first];
  }, [pages, realCount]);

  const translate = `translateX(-${idx * 100}%)`;

  const leftArrowRef = React.useRef<HTMLButtonElement>(null);
  const rightArrowRef = React.useRef<HTMLButtonElement>(null);

  const handleLeftClick = (): void => {
    prevPage()
    const el = leftArrowRef.current!;
    el.classList.remove('move-arrow');
    void el.offsetWidth;
    el.classList.add('move-arrow');
  }

  const handleRightClick = (): void => {
    nextPage()
    const el = rightArrowRef.current!;
    el.classList.remove('move-arrow');
    void el.offsetWidth;
    el.classList.add('move-arrow');
  }

  return (
    <div className="hs-container">
      <button ref={leftArrowRef} className="hs-control is-left" onClick={handleLeftClick} aria-label="Prev">
        <img src={SliderControl} alt="" aria-hidden/>
        <img src={SliderControlArrowLeft} alt="" aria-hidden/>
      </button>
      <div className="hs-viewport">
        <div
          ref={trackRef}
          className="hs-track"
          style={{
            transform: translate,
            transition: withTransition ? "transform .4s ease" : "none",
          }}
        >
          {extended.map((pageItems, pageIndex) => (
            <div className="hs-slide" key={`p-${pageIndex}`}>
              {pageItems.map((el, i) => (
                <div className="hs-item" key={(React.isValidElement(el) && el.key) || `i-${i}`}>
                  {el}
                </div>
              ))}
            </div>
          ))}
        </div>
      </div>
      <button ref={rightArrowRef} className="hs-control is-right" onClick={handleRightClick} aria-label="Next">
        <img src={SliderControl} alt="" aria-hidden/>
        <img src={SliderControlArrowRight} alt="" aria-hidden/>
      </button>
    </div>
  );
}
