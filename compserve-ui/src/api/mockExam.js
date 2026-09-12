import request from '@/utils/request'

// 学生端：获取模拟考试大厅数据（推荐考试、分类、全部考试）
export function getMockExamHall(params) {
  return request({
    url: '/student/mock-exam/hall',
    method: 'get',
    params
  })
}

// 学生端：获取当前用户的模拟考试记录
export function getMyMockExams(params) {
  return request({
    url: '/student/mock-exam/my',
    method: 'get',
    params
  })
}

// 学生端：根据考试ID获取试卷与题目
export function getMockExamDetail(examId) {
  return request({
    url: `/student/mock-exam/${examId}`,
    method: 'get'
  })
}

// 学生端：提交模拟考试试卷并判分
export function submitMockExam(examId, data) {
  return request({
    url: `/student/mock-exam/${examId}/submit`,
    method: 'post',
    data
  })
}

// 学生端：根据记录ID查看一次考试详情（含答题记录）
export function getMockExamRecordDetail(recordId) {
  return request({
    url: `/student/mock-exam/record/${recordId}`,
    method: 'get'
  })
}


