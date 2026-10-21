import { useContext, useState } from "react"
import { Link, useSearchParams } from "react-router-dom"
import { AuthContext } from "../auth/AuthContext"
import HomeLayout from "../components/HomeLayout"
import "../components/EmailVerification.css"

function VerificationForm({ token }: { token: string }) {
  const auth = useContext(AuthContext)
  const [status, setStatus] = useState<"ready" | "verifying" | "success" | "error">("ready")
  const [error, setError] = useState("")
  const validToken = /^[A-Za-z0-9_-]{43}$/.test(token)
  const signedIn = Boolean(auth?.accessToken && auth.refreshToken)

  async function verify() {
    if (!validToken || status === "verifying" || status === "success") {
      return
    }
    setStatus("verifying")
    setError("")
    try {
      const response = await fetch("/api/auth/email-verification/verify", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token })
      })
      if (response.ok) {
        setStatus("success")
      } else {
        setStatus("error")
        setError([400, 403, 410].includes(response.status)
          ? "This verification link is invalid, expired, or has already been used."
          : "We couldn't verify your email right now. Please try again.")
      }
    } catch {
      setStatus("error")
      setError("Unable to connect. Please check your connection and try again.")
    }
  }

  return (
    <div className="verification-page">
      <h2>{status === "success" ? "Email verified" : "Verify your email"}</h2>
      {status === "success" ? (
        <>
          <p role="status">Your email address has been confirmed.</p>
          <Link className="button button-primary" to={signedIn ? "/" : "/login"}>
            {signedIn ? "Go home" : "Log in"}
          </Link>
        </>
      ) : (
        <>
          <p>{validToken
            ? "Confirm your email address to complete verification. You don't need to log in."
            : "This link is missing a valid verification token."}</p>
          {error && <p className="verification-error" role="alert">{error}</p>}
          {validToken && (
            <button className="button button-primary" type="button" onClick={() => void verify()}
              disabled={status === "verifying"}>
              {status === "verifying" ? "Verifying…" : "Verify email"}
            </button>
          )}
          <p className="verification-help">
            Need a new link? <Link to={signedIn ? "/" : "/login"}>
              {signedIn ? "Go home" : "Log in"}
            </Link> to request another verification email.
          </p>
        </>
      )}
    </div>
  )
}

function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get("token") ?? ""
  return (
    <HomeLayout>
      <VerificationForm key={token} token={token} />
    </HomeLayout>
  )
}

export default VerifyEmailPage
