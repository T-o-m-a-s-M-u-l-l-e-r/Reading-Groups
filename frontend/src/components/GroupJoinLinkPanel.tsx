import { useEffect, useState } from "react"
import { useApi } from "../api/apiFetch"
import { useCurrentUser } from "../auth/useCurrentUser"
import type { Group } from "./GroupSelector"
import "./GroupJoinLink.css"

type LinkMetadata = { expiresAt: string, expired: boolean }

function GroupJoinLinkPanel({ group }: { group: Group }) {
  const { user } = useCurrentUser()
  const apiFetch = useApi()
  const [validity, setValidity] = useState("ONE_DAY")
  const [metadata, setMetadata] = useState<LinkMetadata | null>(null)
  const [url, setUrl] = useState("")
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState("")
  const [error, setError] = useState("")
  const isAdministrator = user?.userId === group.administratorUserId

  useEffect(() => {
    if (!isAdministrator) {
      return
    }
    const controller = new AbortController()
    apiFetch(`/api/groups/${group.id}/join-link`, { signal: controller.signal })
      .then(async response => {
        if (response?.ok) {
          const data: LinkMetadata = await response.json()
          if (!controller.signal.aborted) {
            setMetadata(data)
          }
        } else if (response?.status !== 404 && !controller.signal.aborted) {
          setError("Couldn't check the existing link. You can still create a replacement.")
        }
      })
      .catch(() => {
        if (!controller.signal.aborted) {
          setError("Couldn't check the existing link. Please try again.")
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      })
    return () => controller.abort()
  }, [apiFetch, group.id, isAdministrator])

  async function updateLink(action: "create" | "revoke") {
    if (busy || loading) {
      return
    }
    setBusy(true)
    setError("")
    setMessage("")
    try {
      const response = await apiFetch(`/api/groups/${group.id}/join-link`, action === "create" ? {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ validity })
      } : { method: "DELETE" })
      if (!response?.ok) {
        setError("Couldn't update the join link. Please try again.")
        return
      }
      if (action === "create") {
        const data: { url: string, expiresAt: string } = await response.json()
        setUrl(data.url)
        setMetadata({ expiresAt: data.expiresAt, expired: false })
        setMessage("New link created. Any previous link has stopped working.")
      } else {
        setUrl("")
        setMetadata(null)
        setMessage("Join link revoked.")
      }
    } catch {
      setError("Unable to connect. Please try again.")
    } finally {
      setBusy(false)
    }
  }

  async function copyLink() {
    try {
      await navigator.clipboard.writeText(url)
      setMessage("Link copied.")
    } catch {
      setError("Couldn't copy automatically. Select and copy the link below.")
    }
  }

  if (!isAdministrator) {
    return null
  }

  return (
    <section className="group-join-link" aria-label="Group join link">
      <h4>Invite to {group.groupName}</h4>
      {loading && <p role="status">Checking join link…</p>}
      {metadata && <p>{metadata.expired ? "Link expired" : "Link expires"}: {new Date(metadata.expiresAt).toLocaleString()}</p>}
      <p>Creating a link replaces the previous one. Save it now; it cannot be retrieved later.</p>
      <label htmlFor="join-link-validity">Link valid for</label>
      <select id="join-link-validity" value={validity} disabled={busy || loading} onChange={event => setValidity(event.target.value)}>
        <option value="ONE_HOUR">1 hour</option>
        <option value="ONE_DAY">24 hours</option>
        <option value="ONE_WEEK">1 week</option>
      </select>
      <div className="join-link-actions">
        <button type="button" disabled={busy || loading} onClick={() => void updateLink("create")}>Create new link</button>
        <button type="button" disabled={busy || loading} onClick={() => void updateLink("revoke")}>Revoke link</button>
      </div>
      {url && <>
        <label htmlFor="group-join-url">Share this link</label>
        <input id="group-join-url" value={url} readOnly onFocus={event => event.target.select()} />
        <button type="button" onClick={() => void copyLink()}>Copy link</button>
      </>}
      {message && <p role="status">{message}</p>}
      {error && <p role="alert">{error}</p>}
    </section>
  )
}

export default GroupJoinLinkPanel
