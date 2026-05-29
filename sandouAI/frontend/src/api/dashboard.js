import request from './request'

export function getDashboardStats() {
  return request.get('/dashboard/stats')
}

export function getDashboardChart() {
  return request.get('/dashboard/chart')
}
