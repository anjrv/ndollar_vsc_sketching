import Head from 'next/head';
import styles from '../styles/Home.module.css';
import { useState, useCallback } from 'react';

export default function Home() {
  const [strokes, setStrokes] = useState([]);
  const [currentPoints, setCurrentPoints] = useState([]);
  const [isDrawing, setIsDrawing] = useState(false);

  // TODO: Make this make real requests with mouse and touch event points
  const parseRequest = useCallback(
    (event) => {
      event.preventDefault();
      console.log(strokes);
      fetch(`/api/parseSketch`, {
        method: 'POST',
        body: JSON.stringify(strokes),
      })
        .then((res) => {
          if (res.ok) {
            return res.json();
          } else {
            throw new Error(JSON.stringify(res));
          }
        })
        .then((data) => {
          console.log(JSON.stringify(data));
        })
        .catch((err) => console.error(err));
    },
    [strokes]
  );

  const templateRequest = useCallback(
    (event) => {
      event.preventDefault();

      const key = prompt('Enter the name of this shape');

      fetch(`/api/addTemplate`, {
        method: 'POST',
        body: JSON.stringify({ strokes, key }),
      })
        .then((res) => {
          if (!res.ok) {
            throw new Error(JSON.stringify(res));
          }
        })
        .catch((err) => console.error(err));
    },
    [strokes]
  );

  const resolveLocalMouseTarget = (evt) => {
    const clientRect = evt.target.getBoundingClientRect();

    return {
      x: evt.clientX - clientRect.left,
      y: evt.clientY - clientRect.top,
    };
  };

  const handleMouseDown = (evt) => {
    if (evt.button != 0) {
      return;
    }

    setIsDrawing(true);
    setCurrentPoints((points) => [...points, resolveLocalMouseTarget(evt)]);
  };

  const handleMouseMove = (evt) => {
    if (!isDrawing) {
      return;
    }

    setCurrentPoints((points) => [...points, resolveLocalMouseTarget(evt)]);
  };

  const handleMouseUp = () => {
    setIsDrawing(false);
    setStrokes((strokes) => [...strokes, { points: [...currentPoints] }]);
    setCurrentPoints([]);
  };

  const accumulatePolyline = (pointString, point) => {
    return `${pointString} ${point.x} ${point.y},`;
  };

  // TODO: Spec out pen events on touch capable device
  // const handleTouchStart = (evt) => {
  //   let touch = evt.touches[0];

  //   setIsDrawing(true);
  // };

  // const handleTouchMove = (evt) => {};

  // const handleTouchEnd = () => {
  //   setIsDrawing(false);
  // };

  return (
    <div className={styles.container}>
      <Head>
        <title>Sketching Client Example</title>
        <link rel='icon' href='/favicon.ico' />
      </Head>
      <main>
        <form onSubmit={(e) => parseRequest(e)}>
          <div>
            <button className={styles.button} type='submit'>
              Poke Server
            </button>
            <button
              className={styles.button}
              type='button'
              onClick={(e) => templateRequest(e)}
            >
              Add as template
            </button>
            <button
              className={styles.button}
              type='button'
              onClick={() => {
                setCurrentPoints([]);
                setStrokes([]);
              }}
            >
              Clear
            </button>
          </div>
          <div
            className={styles.canvas}
            onMouseDown={handleMouseDown}
            onMouseMove={handleMouseMove}
            onMouseUp={handleMouseUp}
            // TODO: Enable when above methods are sorted
            // onTouchStart={handleTouchStart}
            // onTouchMove={handleTouchMove}
            // onTouchEnd={handleTouchEnd}
          >
            {(currentPoints.length > 0 || strokes.length > 0) && (
              <svg className={styles.sketch}>
                <polyline
                  points={`${currentPoints.reduce(accumulatePolyline, '')}`}
                  stroke='red'
                  strokeWidth='2'
                  fill='none'
                ></polyline>
                {strokes.map((stroke, idx) => (
                  <polyline
                    key={idx}
                    points={`${stroke.points.reduce(accumulatePolyline, '')}`}
                    stroke='red'
                    strokeWidth='2'
                    fill='none'
                  ></polyline>
                ))}
              </svg>
            )}
          </div>
        </form>
      </main>
    </div>
  );
}
