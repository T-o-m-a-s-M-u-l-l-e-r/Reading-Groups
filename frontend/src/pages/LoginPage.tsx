import { Link } from "react-router-dom"
import HomeLayout from "../components/HomeLayout"
import LoginForm from "../components/LoginForm"
import { useState } from "react"
import { useJoinDestination } from "../auth/useJoinDestination"

function LoginPage() {
  const { query } = useJoinDestination()
  const [loading, setLoading] = useState(false)

  return (
    <HomeLayout>
      <div className="auth-form">
        <br />

        <h2>Welcome back</h2>

        <LoginForm setLoading={setLoading} loading={loading} />

        <p className="auth-switch">
          Don't have an account? <Link to={`/register${query}`}>Register</Link>
        </p>
      </div>
    </HomeLayout>
  )
}

export default LoginPage