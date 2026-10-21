import { useEffect, useState } from "react"
import { useApi } from "../api/apiFetch"

export const defaultGroupIcon = "/images/default-group-icon.svg"

function GroupIcon({ groupId, hasIcon }: { groupId: number, hasIcon: boolean }) {
  const apiFetch = useApi()
  const [source, setSource] = useState(defaultGroupIcon)

  useEffect(() => {
    if (!hasIcon) {
      return
    }
    const controller = new AbortController()
    let objectUrl: string | null = null
    apiFetch(`/api/groups/${groupId}/icon`, { signal: controller.signal })
      .then(async response => {
        if (response?.ok) {
          const blob = await response.blob()
          if (!controller.signal.aborted) {
            objectUrl = URL.createObjectURL(blob)
            setSource(objectUrl)
          }
        }
      })
      .catch(() => {
        // Keep the default icon if the image cannot be loaded.
      })
    return () => {
      controller.abort()
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl)
      }
    }
  }, [apiFetch, groupId, hasIcon])

  return <img className="group-icon" src={source} alt="" onError={() => setSource(defaultGroupIcon)} />
}

export default GroupIcon
