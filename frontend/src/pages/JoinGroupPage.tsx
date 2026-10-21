import { useContext, useEffect, useState } from "react"
import { Link, useParams } from "react-router-dom"
import { AuthContext } from "../auth/AuthContext"
import { useApi } from "../api/apiFetch"
import { defaultGroupIcon } from "../components/GroupIcon"
import "../components/GroupIcon.css"
import HomeLayout from "../components/HomeLayout"
import type { Group } from "../components/GroupSelector"
import "../components/GroupJoinLink.css"

type GroupPreview = { groupName: string, administratorName: string, memberCount: number, hasIcon: boolean }

function JoinGroupForm({ token }: { token: string }) {
  const auth = useContext(AuthContext)
  const apiFetch = useApi()
  const [preview, setPreview] = useState<GroupPreview | null>(null)
  const [previewError, setPreviewError] = useState("")
  const [attempt, setAttempt] = useState(0)
  const [joining, setJoining] = useState(false)
  const [group, setGroup] = useState<Group | null>(null)
  const [error, setError] = useState("")
  const signedIn = Boolean(auth?.accessToken && auth.refreshToken)
  const validToken = /^[A-Za-z0-9_-]{43}$/.test(token)
  const query = `?joinToken=${encodeURIComponent(token)}`

  useEffect(() => {
    if (!validToken) {
      return
    }
    const controller = new AbortController()
    fetch(`/api/groups/join/${token}/preview`, { signal: controller.signal, cache: "no-store" })
      .then(async response => {
        if (!response.ok) {
          throw new Error("Preview unavailable")
        }
        const data: GroupPreview = await response.json()
        if (!controller.signal.aborted) {
          setPreview(data)
        }
      })
      .catch(() => {
        if (!controller.signal.aborted) {
          setPreviewError("Couldn't load this invitation. It may have expired or been revoked. Try again or ask the administrator for a new link.")
        }
      })
    return () => controller.abort()
  }, [token, validToken, attempt])

  async function join() {
    if (joining || !signedIn || !validToken || !preview || group) {
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
      {preview && !group && (
        <section className="join-group-preview" aria-label="Group preview">
          <img className="group-icon" alt="" referrerPolicy="no-referrer"
            src={preview.hasIcon ? `/api/groups/join/${token}/icon` : defaultGroupIcon}
            onError={event => {
              event.currentTarget.onerror = null
              event.currentTarget.src = defaultGroupIcon
            }} />
          <h3>{preview.groupName}</h3>
          <p>Created by {preview.administratorName}</p>
          <p>{preview.memberCount} {preview.memberCount === 1 ? "member" : "members"}</p>
        </section>
      )}
      {!validToken ? <p role="alert">This join link is invalid. Ask the administrator for a new one.</p>
        : group ? <>
          <p role="status">You are a member of {group.groupName}.</p>
          <Link className="button button-primary" to="/">Go to my groups</Link>
        </> : previewError ? <>
          <p role="alert">{previewError}</p>
          <button type="button" onClick={() => {
            setPreviewError("")
            setAttempt(value => value + 1)
          }}>Try again</button>
        </> : !preview ? <p role="status">Loading group preview…</p> : signedIn ? <>
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
