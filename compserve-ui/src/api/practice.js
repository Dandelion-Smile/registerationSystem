import request from '@/utils/request'

// 学生端：刷题训练概览（统计 + 正确率趋势）
export function getPracticeOverview(params) {
  return request({
    url: '/student/practice/overview',
    method: 'get',
    params
  })
}

// 学生端：推荐题目列表
export function getPracticeRecommend(params) {
  return request({
    url: '/student/practice/recommend',
    method: 'get',
    params
  })
}

// 学生端：提交刷题训练答案并判题
export function submitPracticeAnswer(data) {
  return request({
    url: '/student/practice/submit',
    method: 'post',
    data
  })
}

// 学生端：查询当前用户的刷题训练记录列表
export function getPracticeRecords() {
  return request({
    url: '/student/practice/records',
    method: 'get'
  })
}

