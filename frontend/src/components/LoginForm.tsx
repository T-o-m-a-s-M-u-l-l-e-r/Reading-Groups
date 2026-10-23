import { useContext, useState } from "react"
import { useNavigate } from "react-router-dom"
import { AuthContext } from "../auth/AuthContext"
import { useJoinDestination } from "../auth/useJoinDestination"
import ErrorDialog from "./ErrorDialog"

type LoginFormProps = {
  setLoading: (loading: boolean) => void
  loading: boolean
}

function LoginForm({ loading, setLoading }: LoginFormProps) {
  const navigate = useNavigate()
  const { destination } = useJoinDestination()
  const auth = useContext(AuthContext)
  const [submissionError, setSubmissionError] = useState("")

  async function handleSubmit(event: React.SubmitEvent<HTMLFormElement>) {

    event.preventDefault()
    if (loading) {
      return
    }
    setLoading(true)

    try {
      const response = await fetch("/api/auth/login", {
        method: 'POST',
        headers: {
          'content-type': 'application/json'
        },
        body: JSON.stringify({ login, password })
      })

      if (!response.ok) {
        if ([400, 401, 403, 404].includes(response.status)) {
          setSubmissionError("Incorrect username, email address, or password. Please try again.")
        } else if (response.status === 429) {
          setSubmissionError("Too many login attempts. Please wait and try again.")
        } else {
          setSubmissionError("Unable to log in right now. Please try again later.")
        }
        return
      }

      const data = await response.json()

      if (auth === null) {
        setSubmissionError("Unable to sign you in. Please try again.")
        return
      }

      auth.setAccessToken(data.accessToken)
      auth.setRefreshToken(data.refreshToken)
      navigate(destination)
    } catch {
      setSubmissionError("Unable to connect. Please check your connection and try again.")
    } finally {
      setLoading(false)
    }

  }

  const [login, setLogin] = useState("")
  const [password, setPassword] = useState("")

  return (
    <>
      <form onSubmit={handleSubmit}>
        <label htmlFor="login">Login</label>
        <input id="login" type="text" autoComplete="username" value={login} required disabled={loading}
          onChange={event => setLogin(event.target.value)} />

        <label htmlFor="password">Password</label>
        <input id="password" type="password" autoComplete="current-password" value={password} required disabled={loading}
          onChange={event => setPassword(event.target.value)} />

        <button type="submit" disabled={loading} className="button button-primary">
          {loading ? "Logging in..." : "Login"}
        </button>
      </form>
      <ErrorDialog title="Login unsuccessful" message={submissionError} onClose={() => setSubmissionError("")} />
    </>
  )
}

export default LoginForm
