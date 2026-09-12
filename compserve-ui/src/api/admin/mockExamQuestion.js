import request from '@/utils/request'

// 管理员端：查询模拟题库列表
export function listMockQuestion(query) {
  return request({
    url: '/admin/mock-question/list',
    method: 'get',
    params: query
  })
}

// 管理员端：查询单个题目详情
export function getMockQuestion(questionId) {
  return request({
    url: `/admin/mock-question/${questionId}`,
    method: 'get'
  })
}

// 管理员端：新增题目
export function addMockQuestion(data) {
  return request({
    url: '/admin/mock-question',
    method: 'post',
    data
  })
}

// 管理员端：修改题目
export function updateMockQuestion(data) {
  return request({
    url: '/admin/mock-question',
    method: 'put',
    data
  })
}

// 管理员端：删除题目
export function delMockQuestion(ids) {
  return request({
    url: `/admin/mock-question/${ids}`,
    method: 'delete'
  })
}

