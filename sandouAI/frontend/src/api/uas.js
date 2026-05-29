import request from './request'

export function passwordLogin(data) {
  return request.post('/uas/auth/password', data)
}

export function sendSms(data) {
  return request.post('/uas/auth/sms/send', data)
}

export function smsLogin(data) {
  return request.post('/uas/auth/sms/login', data)
}

export function getCorps(data) {
  return request.post('/uas/auth/enterprise/corps', data)
}

export function enterpriseLogin(data) {
  return request.post('/uas/auth/enterprise/login', data)
}

export function getUasUserList(params) {
  return request.get('/uas/user/list', { params })
}

export function getUasUser(id) {
  return request.get(`/uas/user/${id}`)
}

export function addUasUser(data) {
  return request.post('/uas/user', data)
}

export function updateUasUser(data) {
  return request.put('/uas/user', data)
}

export function deleteUasUser(id) {
  return request.delete(`/uas/user/${id}`)
}

export function batchDeleteUasUsers(ids) {
  return request.post('/uas/user/batchDelete', ids)
}

export function getCorpList(params) {
  return request.get('/uas/corp/list', { params })
}

export function getCorp(id) {
  return request.get(`/uas/corp/${id}`)
}

export function addCorp(data) {
  return request.post('/uas/corp', data)
}

export function updateCorp(data) {
  return request.put('/uas/corp', data)
}

export function deleteCorp(id) {
  return request.delete(`/uas/corp/${id}`)
}

export function batchDeleteCorps(ids) {
  return request.post('/uas/corp/batchDelete', ids)
}

export function getAppList(params) {
  return request.get('/uas/app/list', { params })
}

export function getApp(id) {
  return request.get(`/uas/app/${id}`)
}

export function addApp(data) {
  return request.post('/uas/app', data)
}

export function updateApp(data) {
  return request.put('/uas/app', data)
}

export function deleteApp(id) {
  return request.delete(`/uas/app/${id}`)
}

export function getLogList(params) {
  return request.get('/uas/log/list', { params })
}
