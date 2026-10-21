import { useEffect, useRef, useState } from "react"
import { useApi } from "../api/apiFetch"
import { defaultGroupIcon } from "./GroupIcon"
import "./GroupIcon.css"

type CreateGroupButtonProps = {
  onGroupCreated: () => void
}

function CreateGroupButton({ onGroupCreated }: CreateGroupButtonProps) {
  const [showModal, setShowModal] = useState(false)
  const [groupName, setGroupName] = useState("")
  const [readingText, setReadingText] = useState<File | null>(null)
  const iconInput = useRef<HTMLInputElement>(null)
  const [icon, setIcon] = useState<File | null>(null)
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)

  const apiFetch = useApi()

  async function handleSubmit(event: React.SubmitEvent<HTMLFormElement>) {
    event.preventDefault()

    if (readingText === null) {
      return
    }

    const formData = new FormData()

    formData.append("groupName", groupName)
    formData.append("readingText", readingText)

    if (icon) {
      formData.append("icon", icon)
    }

    setError("")
    setLoading(true)

    try {
      const response = await apiFetch("/api/groups", {
        method: "POST",
        body: formData
      })

      if (response?.ok) {
        onGroupCreated()
        setShowModal(false)
        setGroupName("")
        setReadingText(null)
        setIcon(null)
      } else {
        setError("Couldn't create the group. Check the PDF and icon, then try again.")
      }
    } catch {
      setError("Unable to connect. Please try again.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <button
        type="button"
        className="create-group-button"
        onClick={() => setShowModal(true)}
      >
        Create Group
      </button>

      {showModal && (
        <div className="modal-overlay">
          <div className="modal group-creation-modal">
            <h2>Create a group</h2>

            <form onSubmit={handleSubmit}>
              <label htmlFor="groupName">Group name</label>

              <input
                id="groupName"
                type="text"
                value={groupName}
                onChange={event => setGroupName(event.target.value)}
                required
              />

              <label htmlFor="readingText">Reading text</label>

              <input
                id="readingText"
                type="file"
                accept="application/pdf"
                onChange={event =>
                  setReadingText(event.target.files?.[0] ?? null)
                }
                required
              />

              <label htmlFor="groupIcon">Group icon (optional)</label>
              <input
                ref={iconInput}
                id="groupIcon"
                type="file"
                accept="image/png,image/jpeg"
                onChange={event => setIcon(event.target.files?.[0] ?? null)}
              />
              <p>PNG or JPEG, up to 1 MiB and 4096 pixels per side. Images are cropped to a square.</p>
              <IconPreview file={icon} />
              {icon && <button type="button" onClick={() => {
                setIcon(null)
                if (iconInput.current) {
                  iconInput.current.value = ""
                }
              }}>Use default icon</button>}
              {error && <p role="alert">{error}</p>}

              <div className="modal-actions">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                >
                  Cancel
                </button>

                <button type="submit" disabled={loading}>
                  {loading ? "Creating..." : "Create"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  )
}

function IconPreview({ file }: { file: File | null }) {
  const [source, setSource] = useState(defaultGroupIcon)
  useEffect(() => {
    if (!file) {
      return
    }
    const objectUrl = URL.createObjectURL(file)
    // Image loading uses an object URL; revoke it when the selection changes.
    const image = new Image()
    image.onload = () => setSource(objectUrl)
    image.onerror = () => setSource(defaultGroupIcon)
    image.src = objectUrl
    return () => {
      image.onload = null
      image.onerror = null
      URL.revokeObjectURL(objectUrl)
    }
  }, [file])
  return <img className="group-icon group-icon-preview" src={file ? source : defaultGroupIcon} alt="Group icon preview" />
}

export default CreateGroupButton