import { Link } from "react-router-dom"
import HomeLayout from "../components/HomeLayout"
import RegisterForm from "../components/RegisterForm"
import { useState } from "react"
import { useJoinDestination } from "../auth/useJoinDestination"

function RegisterPage() {
  const { query } = useJoinDestination()
  const [loading, setLoading] = useState(false)

  return (
    <HomeLayout>
      <div className="auth-form">
        <br />
        <h2>Create your account</h2>

        <RegisterForm loading={loading} setLoading={setLoading} />

        <p className="auth-switch">
          Already have an account? <Link to={`/login${query}`}>Login</Link>
        </p>
      </div>
    </HomeLayout>
  )
}

export default RegisterPage