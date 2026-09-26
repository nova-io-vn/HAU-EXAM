import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { Analytics } from '@vercel/analytics/react'
import './styles/tokens.css'
import './styles/globals.css'
import './styles/app-shell.css'
import './styles/auth.css'
import './styles/users.css'
import './styles/notifications.css'
import './styles/questions.css'
import './styles/ai.css'
import './styles/exams.css'
import './styles/ui-consistency.css'
import './styles/admin.css'
import './styles/faculty.css'
import './styles/user.css'
import './styles/subject-admin.css'
import './styles/stabilization.css'
import './styles/final-polish.css'
import './styles/loading.css'
import './styles/platform-settings.css'
import './styles/async-progress.css'
import './styles/public-experience.css'
import './styles/download.css'
import './styles/ux-pass.css'
import './styles/responsive.css'
import 'intro.js/introjs.css'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
    <Analytics />
  </StrictMode>,
)
