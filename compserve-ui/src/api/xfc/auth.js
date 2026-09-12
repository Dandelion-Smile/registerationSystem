import request from '@/utils/request'
import { getXfcToken } from '@/utils/xfcAuth'

const xfcHeaders = () => ({
  isToken: false,
  'X-XFC-Authorization': `Bearer ${getXfcToken() || ''}`
})

export function xfcLogin(username, password) {
  return request({
    url: '/api/xfc/auth/login',
    method: 'post',
    headers: { isToken: false, repeatSubmit: false },
    data: { username, password }
  })
}

export function getXfcProfile() {
  return request({ url: '/api/xfc/profile', method: 'get', headers: xfcHeaders() })
}

export function getXfcCurrentDraft() {
  return request({ url: '/api/xfc/registration/current', method: 'get', headers: xfcHeaders() })
}

export function saveXfcDraft(data) {
  return request({ url: '/api/xfc/registration/draft', method: 'put', headers: xfcHeaders(), data })
}

export function uploadXfcMaterial(materialType, file) {
  const data = new FormData()
  data.append('file', file)
  return request({ url: `/api/xfc/registration/materials/${materialType}`, method: 'post', headers: { ...xfcHeaders(), 'Content-Type': 'multipart/form-data', repeatSubmit: false }, data })
}

export function generateXfcPdf() {
  return request({ url: '/api/xfc/registrations/current/pdf', method: 'post', headers: { ...xfcHeaders(), repeatSubmit: false } })
}

export function downloadXfcPdf(fileId) {
  return request({ url: `/api/xfc/registrations/${fileId}/pdf`, method: 'get', responseType: 'blob', headers: xfcHeaders() })
}
