import { request } from './http'

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request('/api/files/images', {
    method: 'POST',
    body: formData
  })
}
