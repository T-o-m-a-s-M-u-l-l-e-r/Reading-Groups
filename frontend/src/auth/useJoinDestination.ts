import { useSearchParams } from "react-router-dom"

export function useJoinDestination() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get("joinToken") ?? ""
  const validToken = /^[A-Za-z0-9_-]{43}$/.test(token)
  return {
    destination: validToken ? `/join/${token}` : "/",
    query: validToken ? `?joinToken=${encodeURIComponent(token)}` : ""
  }
}
