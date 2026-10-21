import { useContext, useState } from "react"
import { Link, useParams } from "react-router-dom"
import { AuthContext } from "../auth/AuthContext"
import { useApi } from "../api/apiFetch"
import HomeLayout from "../components/HomeLayout"
import type { Group } from "../components/GroupSelector"
import "../components/GroupJoinLink.css"

function JoinGroupForm({ token }: { token: string }) {
  const auth = useContext(AuthContext)
  const apiFetch = useApi()
  const [joining, setJoining] = useState(false)
  const [group, setGroup] = useState<Group | null>(null)
  const [error, setError] = useState("")
  const signedIn = Boolean(auth?.accessToken && auth.refreshToken)
  const validToken = /^[A-Za-z0-9_-]{43}$/.test(token)
  const query = `?joinToken=${encodeURIComponent(token)}`

  async function join() {
    if (joining || !signedIn || !validToken || group) {
      return
    }
    setJoining(true)
    setError("")
    try {
      const response = await apiFetch(`/api/groups/join/${token}`, { method: "POST" })
      if (response?.ok) {
        setGroup(await response.json())
      } else {
        setError("Couldn't join the group. The link may have expired or been revoked. Ask the administrator for a new link, or try again.")
      }
    } catch {
      setError("Unable to connect. Please try again.")
    } finally {
      setJoining(false)
    }
  }

  return (
    <div className="join-group-page">
      <h2>{group ? "Group joined" : "Join a reading group"}</h2>
      {!validToken ? <p role="alert">This join link is invalid. Ask the administrator for a new one.</p>
        : group ? <>
          <p role="status">You are a member of {group.groupName}.</p>
          <Link className="button button-primary" to="/">Go to my groups</Link>
        </> : signedIn ? <>
          <p>Use this invitation to join the group.</p>
          {error && <p role="alert">{error}</p>}
          <button className="button button-primary" type="button" disabled={joining} onClick={() => void join()}>
            {joining ? "Joining…" : "Join group"}
          </button>
        </> : <>
          <p>Log in or create an account to continue with this invitation.</p>
          <div className="join-link-actions">
            <Link className="button button-primary" to={`/login${query}`}>Log in</Link>
            <Link className="button button-primary" to={`/register${query}`}>Register</Link>
          </div>
        </>}
    </div>
  )
}

function JoinGroupPage() {
  const { token = "" } = useParams()
  return <HomeLayout><JoinGroupForm key={token} token={token} /></HomeLayout>
}

export default JoinGroupPage
