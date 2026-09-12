import request from '@/utils/request'

// 管理员端：查询模拟试卷列表
export function listMockPaper(query) {
  return request({
    url: '/admin/mock-paper/list',
    method: 'get',
    params: query
  })
}

// 管理员端：查询试卷详情
export function getMockPaper(paperId) {
  return request({
    url: `/admin/mock-paper/${paperId}`,
    method: 'get'
  })
}

// 管理员端：新增试卷
export function addMockPaper(data) {
  return request({
    url: '/admin/mock-paper',
    method: 'post',
    data
  })
}

// 管理员端：修改试卷
export function updateMockPaper(data) {
  return request({
    url: '/admin/mock-paper',
    method: 'put',
    data
  })
}

// 管理员端：删除试卷
export function delMockPaper(ids) {
  return request({
    url: `/admin/mock-paper/${ids}`,
    method: 'delete'
  })
}

// 管理员端：查询试卷下已分配题目关联列表
export function getPaperQuestions(paperId) {
  return request({
    url: `/admin/mock-paper/${paperId}/questions`,
    method: 'get'
  })
}

// 管理员端：保存试卷题目关联（覆盖式）
export function savePaperQuestions(paperId, data) {
  return request({
    url: `/admin/mock-paper/${paperId}/questions`,
    method: 'post',
    data
  })
}

