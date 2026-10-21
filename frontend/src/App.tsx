import { Routes, Route } from 'react-router-dom'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import AboutPage from './pages/AboutPage'
import JoinGroupPage from './pages/JoinGroupPage'
import VerifyEmailPage from './pages/VerifyEmailPage'

function App() {

  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/join/:token" element={<JoinGroupPage />} />
      <Route path="/verify-email" element={<VerifyEmailPage />} />
      <Route path="/about" element={<AboutPage />} />
    </Routes>
  )
}

export default App