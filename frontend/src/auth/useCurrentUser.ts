import { useCallback, useEffect, useState } from "react"
import { useApi } from "../api/apiFetch"

export type CurrentUser = {
  userId: number
  username: string
  email: string
  emailVerified: boolean
  emailVerificationResendAvailableAt: string | null
}

export function useCurrentUser() {
  const apiFetch = useApi()
  const [user, setUser] = useState<CurrentUser | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")

  const refreshUser = useCallback((signal?: AbortSignal) => {
    return apiFetch("/api/users/me", { signal })
      .then(async response => {
        if (!response?.ok) {
          throw new Error("Could not load your account.")
        }
        const currentUser: CurrentUser = await response.json()
        if (!signal?.aborted) {
          setUser(currentUser)
          setError("")
          setLoading(false)
        }
        return currentUser
      })
      .catch(() => {
        if (!signal?.aborted) {
          setError("We couldn't load your account status. Please try again.")
          setLoading(false)
        }
        return null
      })
  }, [apiFetch])

  useEffect(() => {
    const controller = new AbortController()
    void refreshUser(controller.signal)
    function handleFocus() {
      void refreshUser(controller.signal)
    }
    window.addEventListener("focus", handleFocus)
    return () => {
      controller.abort()
      window.removeEventListener("focus", handleFocus)
    }
  }, [refreshUser])

  return { user, loading, error, refreshUser }
}
