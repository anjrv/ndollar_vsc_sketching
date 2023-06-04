import Head from 'next/head';
import styles from '../styles/Home.module.css';
import { useState, useCallback } from 'react';

export default function Home() {
  // Thoughts: This could also be an array of arrays so we retain the stroke format
  const [points, setPoints] = useState([]);
  const [isDrawing, setIsDrawing] = useState(false);

  // TODO: Make this make real requests with mouse and touch event points
  const parseRequest = useCallback((event) => {
    event.preventDefault();
    fetch(`/api/parseSketch`, {
      method: 'POST',
      body: JSON.stringify(points),
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
  }, []);

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
    setPoints((points) => [...points, resolveLocalMouseTarget(evt)]);
  };

  const handleMouseMove = (evt) => {
    if (!isDrawing) {
      return;
    }

    setPoints((points) => [...points, resolveLocalMouseTarget(evt)]);
  };

  const handleMouseUp = () => {
    setIsDrawing(false);

    // Format example
    // console.log(JSON.stringify(points));
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
              onClick={() => setPoints([])}
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
            {points.length > 0 && (
              <svg className={styles.sketch}>
                <polyline
                  points={`${points.reduce(accumulatePolyline, '')}`}
                  stroke='red'
                  strokeWidth='2'
                  fill='none'
                ></polyline>
              </svg>
            )}
          </div>
        </form>
      </main>
    </div>
  );
}
