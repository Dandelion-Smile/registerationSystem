const XFC_TOKEN_KEY = 'XFC-Registration-Token'

export function getXfcToken() {
  return sessionStorage.getItem(XFC_TOKEN_KEY)
}

export function setXfcToken(token) {
  sessionStorage.setItem(XFC_TOKEN_KEY, token)
}

export function removeXfcToken() {
  sessionStorage.removeItem(XFC_TOKEN_KEY)
}
