import { useContext, useState } from "react"
import { useNavigate } from "react-router-dom"
import { AuthContext } from "../auth/AuthContext"
import { useJoinDestination } from "../auth/useJoinDestination"

type RegisterFormProps = {
  loading: boolean,
  setLoading: (loading: boolean) => void
}

function RegisterForm({ loading, setLoading }: RegisterFormProps) {
  const navigate = useNavigate()
  const { destination } = useJoinDestination()
  const auth = useContext(AuthContext)
  const [username, setUsername] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")

  async function handleSubmit(event: React.SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    setLoading(true)

    try {
      const response = await fetch("/api/users", {
        method: 'POST',
        headers: {
          'content-type': 'application/json'
        },
        body: JSON.stringify({ email, username, password })
      })

      if (!response.ok) {
        console.log("Registration failed:", response.status)
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

  return (
    <form onSubmit={handleSubmit}>
      <label htmlFor="username">Username</label>
      <input
        id="username"
        type="text"
        value={username}
        onChange={(event) => setUsername(event.target.value)}
        required
      />

      <label htmlFor="email">Email</label>
      <input
        id="email"
        type="email"
        value={email}
        onChange={(event) => setEmail(event.target.value)}
        required
      />

      <label htmlFor="password">Password</label>
      <input
        id="password"
        type="password"
        value={password}
        onChange={(event) => setPassword(event.target.value)}
        required
      />

      <button type="submit" disabled={loading} className="button button-primary">
        {loading ? "Creating account..." : "Create account"}
      </button>
    </form>


  )
}

export default RegisterForm