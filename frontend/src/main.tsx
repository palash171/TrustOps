import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

/**
 * This is the frontend starting point.
 * index.html creates an empty div with id="root".
 * React finds that div and places the App component inside it.
 */
createRoot(document.getElementById('root')!).render(
  // StrictMode runs extra development checks. It does not appear on the screen.
  <StrictMode>
    <App />
  </StrictMode>,
)
