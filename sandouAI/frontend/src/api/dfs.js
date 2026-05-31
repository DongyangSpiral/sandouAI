import request from './request'

// File APIs
export const uploadFile = (data) => {
  return request({
    url: '/file/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const batchUploadFile = (data) => {
  return request({
    url: '/file/batchUpload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const deleteFile = (id) => request.delete(`/file/${id}`)
export const renameFile = (data) => request.put('/file/rename', data)
export const moveFile = (data) => request.post('/file/move', data)
export const copyFile = (data) => request.post('/file/copy', data)
export const getFileList = (params) => request.get('/file/list', { params })
export const getFileDetail = (id) => request.get(`/file/${id}`)
export const downloadFile = (id) => {
  return request({
    url: `/file/download/${id}`,
    method: 'get',
    responseType: 'blob'
  })
}

// Folder APIs
export const createFolder = (data) => request.post('/folder', data)
export const renameFolder = (data) => request.put('/folder/rename', data)
export const deleteFolder = (id) => request.delete(`/folder/${id}`)
export const getFolderTree = () => request.get('/folder/tree')
export const moveFolder = (data) => request.post('/folder/move', data)
export const getFolderContent = (params) => request.get('/folder/content', { params })

// Share APIs
export const createShare = (data) => request.post('/share', data)
export const accessShare = (code, data) => request.post(`/share/access/${code}`, data)
export const getShareList = (params) => request.get('/share/list', { params })
export const cancelShare = (id) => request.delete(`/share/${id}`)
export const getPublicShareList = (code, params) => request.get(`/share/public/list/${code}`, { params })
export const downloadPublicShareFile = (code, params) => {
  return request({
    url: `/share/public/download/${code}`,
    method: 'get',
    params,
    responseType: 'blob'
  })
}
