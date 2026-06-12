import request from './request'

export const aiSummarize = (params) => request.get('/ai/summarize', { params })

export const aiAnalyze = (data) => request.post('/ai/analyze', data)
