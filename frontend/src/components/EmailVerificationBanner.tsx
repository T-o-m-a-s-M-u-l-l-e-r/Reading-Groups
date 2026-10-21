import { useEffect, useState } from "react"
import { useApi } from "../api/apiFetch"
import { useCurrentUser } from "../auth/useCurrentUser"
import "./EmailVerification.css"

function EmailVerificationBanner() {
  const { user, loading, error, refreshUser } = useCurrentUser()
  const apiFetch = useApi()
  const [sending, setSending] = useState(false)
  const [message, setMessage] = useState("")
  const [sendError, setSendError] = useState("")
  const [now, setNow] = useState(() => Date.now())
  const availableAt = user?.emailVerificationResendAvailableAt
  const remaining = availableAt ? Math.max(0, Math.ceil((Date.parse(availableAt) - now) / 1000)) : 0

  useEffect(() => {
    if (!availableAt) {
      return
    }
    const interval = window.setInterval(() => setNow(Date.now()), 1000)
    return () => window.clearInterval(interval)
  }, [availableAt])

  async function resend() {
    if (sending || remaining > 0) {
      return
    }
    setSending(true)
    setMessage("")
    setSendError("")
    try {
      const response = await apiFetch("/api/auth/email-verification/resend", { method: "POST" })
      if (response?.ok) {
        setMessage("Verification email requested. Check your inbox and spam folder.")
      } else if (response?.status === 429) {
        setSendError("Please wait before requesting another verification email.")
      } else {
        setSendError("We couldn't request another email. Please check your account status and try again.")
      }
      setNow(Date.now())
      await refreshUser()
    } catch {
      setSendError("Unable to connect. Please try again.")
    } finally {
      setSending(false)
    }
  }

  if (loading) {
    return <section className="verification-banner" role="status">Checking email verification status…</section>
  }
  if (error) {
    return (
      <section className="verification-banner">
        <p role="alert">{error}</p>
        <button type="button" onClick={() => void refreshUser()}>Try again</button>
      </section>
    )
  }
  if (!user || user.emailVerified) {
    return null
  }

  return (
    <section className="verification-banner" aria-label="Email verification">
      <div>
        <strong>Verify your email address</strong>
        <p>Check the email sent to {user.email} and confirm your address.</p>
        {message && <p role="status">{message}</p>}
        {sendError && <p role="alert">{sendError}</p>}
      </div>
      <div className="verification-actions">
        <button type="button" onClick={() => void resend()} disabled={sending || remaining > 0}>
          {sending ? "Requesting…" : remaining > 0 ? `Resend in ${remaining}s` : "Resend verification email"}
        </button>
        <button type="button" onClick={() => void refreshUser()} disabled={sending}>
          I've verified my email
        </button>
      </div>
    </section>
  )
}

export default EmailVerificationBanner
