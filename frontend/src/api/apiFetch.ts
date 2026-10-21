import { useCallback, useContext } from "react"
import { AuthContext } from "../auth/AuthContext"

export function useApi() {
    const auth = useContext(AuthContext)

    //gets called after login/registration
const apiFetch = useCallback(async (resource : string, requestInit? : RequestInit) => {

    if (!auth) {
        console.log("apiFetch called with auth undefined")
        return
    }

    if (auth.accessToken === undefined || auth.refreshToken === undefined || auth.accessToken === null || auth.refreshToken === null) {
        console.log("apiFetch called with empty token")
        return
    }
    
    const request = await httpRequest(resource, auth.accessToken, requestInit)

    if (request.status == 401) {
        const authorizationHeader = 'Bearer ' + auth.refreshToken
        const refreshResponse = await fetch("/api/auth/refresh", { method: 'POST', headers: { authorization: authorizationHeader } })

        if (refreshResponse.ok) {
            const data = await refreshResponse.json()
            auth.setAccessToken(data.accessToken)
            return httpRequest(resource, data.accessToken, requestInit)
        } else {
            console.log("Attempt to refresh token failed")
            auth.setAccessToken(null)
            auth.setRefreshToken(null)
            return
        }

    } else {
        return request
    }
    
}, [auth])

    return apiFetch
}

async function httpRequest(resource : string, accessToken : string, requestInit? : RequestInit) {
    const authorizationHeader = 'Bearer ' + accessToken 

    if (!requestInit) {
        return fetch(resource, {headers:{Authorization: authorizationHeader}})
    } else {
        return fetch(resource, {...requestInit, headers : {...requestInit.headers, Authorization: authorizationHeader}})
    }

}

 