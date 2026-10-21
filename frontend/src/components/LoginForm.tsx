import { useContext, useState } from "react"
import { useNavigate } from "react-router-dom"
import { AuthContext } from "../auth/AuthContext"
import { useJoinDestination } from "../auth/useJoinDestination"

type LoginFormProps = {
  setLoading: (loading: boolean) => void
  loading: boolean
}

function LoginForm({ loading, setLoading }: LoginFormProps) {
  const navigate = useNavigate()
  const { destination } = useJoinDestination()
  const auth = useContext(AuthContext)

  async function handleSubmit(event: React.SubmitEvent<HTMLFormElement>) {

    setLoading(true)
    event.preventDefault()

    try {
      const response = await fetch("/api/auth/login", {
        method: 'POST',
        headers: {
          'content-type': 'application/json'
        },
        body: JSON.stringify({ login, password })
      })

      if (!response.ok) {
        console.log("Login failed:", response.status)
        return
      }

      const data = await response.json()

      if (auth === null) {
        console.log("Auth object is null")
        return
      }

      auth.setAccessToken(data.accessToken)
      auth.setRefreshToken(data.refreshToken)
      navigate(destination)
    } finally {
      setLoading(false)
    }

  }

  const [login, setLogin] = useState("")
  const [password, setPassword] = useState("")

  return (
    <form onSubmit={handleSubmit}>
      <label htmlFor="login">Login </label>
      <input id="login" type="text" value={login} onChange={event => setLogin(event?.target.value)} />

      <label htmlFor="password">Password </label>
      <input id="password" type="password" value={password} onChange={event => setPassword(event?.target.value)} />


      <button type="submit" disabled={loading} className="button button-primary">
        {loading ? "Logging in..." : "Login"}
      </button>

    </form>
  )
}

export default LoginForm