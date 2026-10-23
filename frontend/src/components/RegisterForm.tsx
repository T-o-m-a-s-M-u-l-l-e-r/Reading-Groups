import { useContext, useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { AuthContext } from "../auth/AuthContext"
import { useJoinDestination } from "../auth/useJoinDestination"
import ErrorDialog from "./ErrorDialog"
import "./RegisterForm.css"

type RegisterFormProps = {
  loading: boolean,
  setLoading: (loading: boolean) => void
}

type RegistrationRules = {
  usernameMinLength: number,
  usernameMaxLength: number,
  usernamePattern: string,
  emailMaxLength: number,
  emailPattern: string,
  passwordMinLength: number,
  passwordMaxUtf8Bytes: number
}

function RegisterForm({ loading, setLoading }: RegisterFormProps) {
  const navigate = useNavigate()
  const { destination } = useJoinDestination()
  const auth = useContext(AuthContext)
  const [username, setUsername] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [emailFormatValid, setEmailFormatValid] = useState(true)
  const [rules, setRules] = useState<RegistrationRules | null>(null)
  const [rulesError, setRulesError] = useState("")
  const [rulesAttempt, setRulesAttempt] = useState(0)
  const [submissionError, setSubmissionError] = useState("")

  useEffect(() => {
    const controller = new AbortController()
    fetch("/api/registration-rules", { signal: controller.signal })
      .then(async response => {
        if (!response.ok) {
          throw new Error("Could not load registration rules")
        }
        const data: RegistrationRules = await response.json()
        if (!controller.signal.aborted) {
          setRules(data)
        }
      })
      .catch(() => {
        if (!controller.signal.aborted) {
          setRulesError("Couldn't load registration requirements. Please try again.")
        }
      })
    return () => controller.abort()
  }, [rulesAttempt])

  let usernameError = ""
  if (!username.trim()) {
    usernameError = "Username cannot be empty."
  } else if (rules) {
    if (username.length < rules.usernameMinLength) {
      usernameError = `Username must be at least ${rules.usernameMinLength} characters.`
    } else if (username.length > rules.usernameMaxLength) {
      usernameError = `Username must not exceed ${rules.usernameMaxLength} characters.`
    } else if (new RegExp(rules.usernamePattern).exec(username)?.[0] !== username) {
      usernameError = "Username contains characters that aren't allowed."
    }
  }

  let emailError = ""
  if (!email.trim()) {
    emailError = "Email cannot be empty."
  } else if (!emailFormatValid || (rules && new RegExp(rules.emailPattern).exec(email)?.[0] !== email)) {
    emailError = "Enter a valid email address."
  } else if (rules && email.length > rules.emailMaxLength) {
    emailError = `Email must not exceed ${rules.emailMaxLength} characters.`
  }

  let passwordError = ""
  if (!password.trim()) {
    passwordError = "Password cannot be empty."
  } else if (rules) {
    if (password.length < rules.passwordMinLength) {
      passwordError = `Password must be at least ${rules.passwordMinLength} characters.`
    } else if (new TextEncoder().encode(password).length > rules.passwordMaxUtf8Bytes) {
      passwordError = `Password is too long (maximum ${rules.passwordMaxUtf8Bytes} UTF-8 bytes).`
    }
  }

  const invalid = Boolean(usernameError || emailError || passwordError)

  async function handleSubmit(event: React.SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    if (loading || !rules || invalid) {
      return
    }
    setLoading(true)

    try {
      const response = await fetch("/api/users", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, username, password })
      })

      if (!response.ok) {
        const body = await response.text()
        let message = `Registration failed (${response.status}). Please try again.`
        try {
          const error: { message?: string, fieldErrors?: Record<string, string> } = JSON.parse(body)
          const fieldMessages = Object.values(error.fieldErrors ?? {}).filter(value => typeof value === "string")
          if (fieldMessages.length > 0) {
            message = fieldMessages.join("\n")
          } else if (typeof error.message === "string") {
            message = error.message
          }
        } catch {
          // Duplicate-account responses currently use plain text.
          if (body && response.headers.get("content-type")?.includes("text/plain")) {
            message = body
          }
        }
        setSubmissionError(message)
        return
      }

      const data = await response.json()
      if (auth === null) {
        setSubmissionError("Your account was created, but you couldn't be signed in. Please log in.")
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

  return (
    <>
      <form onSubmit={handleSubmit} noValidate>
        {!rules && !rulesError && <p role="status">Loading registration requirements…</p>}
        {rulesError && <div>
          <p role="alert">{rulesError}</p>
          <button type="button" onClick={() => {
            setRulesError("")
            setRulesAttempt(value => value + 1)
          }}>Try again</button>
        </div>}

        <label htmlFor="username">Username</label>
        <input id="username" type="text" autoComplete="username" value={username} required disabled={loading}
          aria-invalid={Boolean(usernameError)} aria-describedby={usernameError ? "username-error" : undefined}
          onChange={event => setUsername(event.target.value)} />
        {usernameError && <p id="username-error" className="registration-field-error" aria-live="polite">{usernameError}</p>}

        <label htmlFor="email">Email</label>
        <input id="email" type="email" autoComplete="email" value={email} required disabled={loading}
          aria-invalid={Boolean(emailError)} aria-describedby={emailError ? "email-error" : undefined}
          onChange={event => {
            setEmail(event.target.value)
            setEmailFormatValid(!event.target.validity.typeMismatch)
          }} />
        {emailError && <p id="email-error" className="registration-field-error" aria-live="polite">{emailError}</p>}

        <label htmlFor="password">Password</label>
        <input id="password" type="password" autoComplete="new-password" value={password} required disabled={loading}
          aria-invalid={Boolean(passwordError)} aria-describedby={passwordError ? "password-error" : undefined}
          onChange={event => setPassword(event.target.value)} />
        {passwordError && <p id="password-error" className="registration-field-error" aria-live="polite">{passwordError}</p>}

        <button type="submit" disabled={loading || !rules || invalid} className="button button-primary">
          {loading ? "Creating account..." : "Create account"}
        </button>
      </form>

      <ErrorDialog title="Registration unsuccessful" message={submissionError}
        onClose={() => setSubmissionError("")} />
    </>
  )
}

export default RegisterForm
