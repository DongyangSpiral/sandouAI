import request from './request'

// Team APIs
export const createTeam = (data) => request.post('/team', data)
export const updateTeam = (id, data) => request.put(`/team/${id}`, data)
export const deleteTeam = (id) => request.delete(`/team/${id}`)
export const getTeamList = (params) => request.get('/team/list', { params })
export const getTeamDetail = (id) => request.get(`/team/${id}`)

// Team Member APIs
export const inviteMember = (data) => request.post('/team/invite', data)
export const removeMember = (id) => request.delete(`/team/member/${id}`)
export const updateMemberRole = (data) => request.put('/team/member/role', data)
export const getTeamMembers = (params) => request.get('/team/member/list', { params })

// Team File APIs
export const getTeamFiles = (params) => request.get('/team/file/list', { params })
export const addTeamFile = (data) => request.post('/team/file', data)
export const setTeamFilePermission = (data) => request.put('/team/file/permission', data)
