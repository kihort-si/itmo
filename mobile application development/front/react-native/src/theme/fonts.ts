

import * as Font from 'expo-font';
import { useEffect, useState } from 'react';

export function useAppFonts(): boolean {
  const [loaded, setLoaded] = useState(false);
  useEffect(() => {
    (async () => {
      try {

        await Font.loadAsync({

        });
      } catch {

      } finally {
        setLoaded(true);
      }
    })();
  }, []);
  return loaded;
}
