export const appEntry = import.meta.env.VITE_APP_ENTRY || 'portal'
export const isDriveEntry = appEntry === 'drive'
export const isPortalEntry = !isDriveEntry

function defaultOrigin(port) {
  if (typeof window === 'undefined') return `http://localhost:${port}`
  return `${window.location.protocol}//${window.location.hostname}:${port}`
}

export const portalOrigin = import.meta.env.VITE_PORTAL_ORIGIN || defaultOrigin(5173)
export const driveOrigin = import.meta.env.VITE_DRIVE_ORIGIN || defaultOrigin(5174)

export function portalUrl(path = '/') {
  return `${portalOrigin}${path.startsWith('/') ? path : `/${path}`}`
}

export function driveUrl(path = '/') {
  return `${driveOrigin}${path.startsWith('/') ? path : `/${path}`}`
}

export function goPortal(path = '/') {
  window.location.href = portalUrl(path)
}

export function goDrive(path = '/') {
  window.location.href = driveUrl(path)
}
