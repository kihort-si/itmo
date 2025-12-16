import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './_index.scss'
import App from './App.tsx'
import { Provider } from 'react-redux';
import store from "./redux/store.ts";

createRoot(document.getElementById('root')!).render(
  <Provider store={store}>
    <StrictMode>
      <App />
    </StrictMode>
  </Provider>
)
