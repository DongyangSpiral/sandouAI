import request from './request'

export function systemLogin(data) {
  return request.post('/system/login', data)
}

export function getUserList(params) {
  return request.get('/system/user/list', { params })
}

export function getUser(id) {
  return request.get(`/system/user/${id}`)
}

export function addUser(data) {
  return request.post('/system/user', data)
}

export function updateUser(data) {
  return request.put('/system/user', data)
}

export function deleteUser(id) {
  return request.delete(`/system/user/${id}`)
}

export function getUserRoles(userId) {
  return request.get(`/system/user/roles/${userId}`)
}

export function assignUserRoles(data) {
  return request.post('/system/user/assignRoles', data)
}

export function getRoleList(params) {
  return request.get('/system/role/list', { params })
}

export function getAllRoles() {
  return request.get('/system/role/all')
}

export function addRole(data) {
  return request.post('/system/role', data)
}

export function updateRole(data) {
  return request.put('/system/role', data)
}

export function deleteRole(id) {
  return request.delete(`/system/role/${id}`)
}

export function getRole(id) {
  return request.get(`/system/role/${id}`)
}

export function getRoleMenuIds(roleId) {
  return request.get(`/system/role/menus/${roleId}`)
}

export function assignRoleMenus(data) {
  return request.post('/system/role/assignMenus', data)
}

export function getMenuTree() {
  return request.get('/system/menu/tree')
}

export function getMenuList() {
  return request.get('/system/menu/list')
}

export function addMenu(data) {
  return request.post('/system/menu', data)
}

export function updateMenu(data) {
  return request.put('/system/menu', data)
}

export function deleteMenu(id) {
  return request.delete(`/system/menu/${id}`)
}
