import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './styles/tokens.css'
import './styles/globals.css'
import './styles/app-shell.css'
import './styles/auth.css'
import './styles/users.css'
import './styles/notifications.css'
import './styles/questions.css'
import './styles/ai.css'
import './styles/exams.css'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
