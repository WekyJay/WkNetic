import request from './axios'

export interface UploadResult {
  url: string
  fileName: string
  size?: string
}

/**
 * 上传图片
 */
export const uploadImage = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<UploadResult>('/api/v1/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量上传图片
 */
export const uploadImages = (files: File[]) => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  return request.post<UploadResult[]>('/api/v1/upload/images', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传附件
 */
export const uploadFile = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<UploadResult>('/api/v1/upload/file', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传头像
 */
export const uploadAvatar = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<UploadResult>('/api/v1/upload/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除文件
 */
export const deleteFile = (url: string) => {
  return request.delete<void>('/api/v1/upload', {
    params: { url }
  })
}
