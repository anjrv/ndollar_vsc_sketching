import Head from 'next/head';
import styles from '../styles/Home.module.css';
import { useCallback } from 'react';

export default function Home() {
  // TODO: Make this make real requests with mouse and touch event points
  const parseRequest = useCallback((event) => {
    event.preventDefault();
    fetch(`/api/parseSketch`)
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

  return (
    <div className={styles.container}>
      <Head>
        <title>Sketching Client Example</title>
        <link rel='icon' href='/favicon.ico' />
      </Head>
      <main>
        <form onSubmit={(e) => parseRequest(e)}>
          <button type='submit'>Poke Server</button>
        </form>
      </main>
    </div>
  );
}
