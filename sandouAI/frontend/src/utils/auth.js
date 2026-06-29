const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'

function readCookie(name) {
  const prefix = `${name}=`
  return document.cookie
    .split(';')
    .map((item) => item.trim())
    .find((item) => item.startsWith(prefix))
    ?.slice(prefix.length)
}

function writeCookie(name, value) {
  document.cookie = `${name}=${encodeURIComponent(value)}; path=/; max-age=7200; SameSite=Lax`
}

function removeCookie(name) {
  document.cookie = `${name}=; path=/; max-age=0; SameSite=Lax`
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || decodeURIComponent(readCookie(TOKEN_KEY) || '')
}

export function setAuth(token, userInfo) {
  localStorage.setItem(TOKEN_KEY, token)
  writeCookie(TOKEN_KEY, token)
  if (userInfo) {
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
  }
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_INFO_KEY)
  removeCookie(TOKEN_KEY)
}
