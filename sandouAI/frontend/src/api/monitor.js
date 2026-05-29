import request from './request'

export function getServerInfo() { return request.get('/monitor/server') }
export function getCacheInfo() { return request.get('/monitor/cache') }
export function getOnlineUsers() { return request.get('/monitor/online/list') }
export function getJobList() { return request.get('/monitor/job/list') }
export function executeJob(id) { return request.post(`/monitor/job/execute/${id}`) }
export function getGenTables() { return request.get('/tool/gen/tables') }
export function getGenColumns(tableName) { return request.get('/tool/gen/columns', { params: { tableName } }) }
export function generateCode(data) { return request.post('/tool/gen/generate', data) }
