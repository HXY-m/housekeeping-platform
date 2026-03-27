import { request } from './http'

function uploadByEndpoint(endpoint, file) {
  const formData = new FormData()
  formData.append('file', file)

  return request(endpoint, {
    method: 'POST',
    body: formData
  })
}

export function uploadImage(file) {
  return uploadByEndpoint('/api/files/images', file)
}

export function uploadAttachment(file) {
  return uploadByEndpoint('/api/files/attachments', file)
}
