import request from './request'

export function getDeptList() { return request.get('/system/dept/list') }
export function getDept(id) { return request.get(`/system/dept/${id}`) }
export function addDept(data) { return request.post('/system/dept', data) }
export function updateDept(data) { return request.put('/system/dept', data) }
export function deleteDept(id) { return request.delete(`/system/dept/${id}`) }

export function getPostList() { return request.get('/system/post/list') }
export function addPost(data) { return request.post('/system/post', data) }
export function updatePost(data) { return request.put('/system/post', data) }
export function deletePost(id) { return request.delete(`/system/post/${id}`) }

export function getDictTypeList() { return request.get('/system/dict/type/list') }
export function addDictType(data) { return request.post('/system/dict/type', data) }
export function updateDictType(data) { return request.put('/system/dict/type', data) }
export function deleteDictType(id) { return request.delete(`/system/dict/type/${id}`) }

export function getDictDataList(dictType) { return request.get('/system/dict/data/list', { params: { dictType } }) }
export function addDictData(data) { return request.post('/system/dict/data', data) }
export function updateDictData(data) { return request.put('/system/dict/data', data) }
export function deleteDictData(id) { return request.delete(`/system/dict/data/${id}`) }

export function getConfigList() { return request.get('/system/config/list') }
export function addConfig(data) { return request.post('/system/config', data) }
export function updateConfig(data) { return request.put('/system/config', data) }
export function deleteConfig(id) { return request.delete(`/system/config/${id}`) }

export function getNoticeList() { return request.get('/system/notice/list') }
export function addNotice(data) { return request.post('/system/notice', data) }
export function updateNotice(data) { return request.put('/system/notice', data) }
export function deleteNotice(id) { return request.delete(`/system/notice/${id}`) }

export function getOperlogList(params) { return request.get('/system/operlog/list', { params }) }
export function deleteOperlog(id) { return request.delete(`/system/operlog/${id}`) }
export function cleanOperlog() { return request.get('/system/operlog/clean') }
