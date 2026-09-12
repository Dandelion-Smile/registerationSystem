import request from '@/utils/request'

// 教师端：获取公开竞赛列表
export function listTeacherCompetitions(params) {
  return request({
    url: '/teacher/competition/list',
    method: 'get',
    params: params
  })
}

// 教师端：获取竞赛统计数据
export function getCompetitionStats() {
  return request({
    url: '/teacher/competition/stats',
    method: 'get'
  })
}

// 教师端：获取竞赛详情
export function getTeacherCompetitionDetail(competitionId) {
  return request({
    url: `/teacher/competition/${competitionId}`,
    method: 'get'
  })
}

// 教师端：获取可评审竞赛列表（已赋权的竞赛）
export function getReviewableCompetitions() {
  return request({
    url: '/teacher/review/competitions',
    method: 'get'
  })
}

// 教师端：获取竞赛的队伍列表（用于评审）
export function getReviewTeams(competitionId) {
  return request({
    url: `/teacher/review/${competitionId}/teams`,
    method: 'get'
  })
}

// 教师端：获取队伍详情（包含材料路径）
export function getReviewTeamDetail(competitionId, participationId) {
  return request({
    url: `/teacher/review/${competitionId}/participations/${participationId}`,
    method: 'get'
  })
}

// 教师端：获取队伍的评分记录
export function getTeamScores(competitionId, participationId) {
  return request({
    url: `/teacher/review/${competitionId}/participations/${participationId}/scores`,
    method: 'get'
  })
}

// 教师端：提交评分
export function submitScore(data) {
  return request({
    url: '/teacher/review/score',
    method: 'post',
    data: data
  })
}

// 教师端：修改评分
export function updateScore(scoreId, data) {
  return request({
    url: `/teacher/review/score/${scoreId}`,
    method: 'put',
    data: data
  })
}

// 教师端：删除评分
export function deleteScore(scoreId) {
  return request({
    url: `/teacher/review/score/${scoreId}`,
    method: 'delete'
  })
}

// 教师端：获取评审统计信息
export function getReviewStats(competitionId) {
  return request({
    url: `/teacher/review/${competitionId}/stats`,
    method: 'get'
  })
}

// 教师端：获取我的队伍赛事列表
export function getMyTeamCompetitions() {
  return request({
    url: '/teacher/my-teams/competitions',
    method: 'get'
  })
}

// 教师端：根据手机号获取教师信息
export function searchTeachers(params) {
  return request({
    url: `/teacher/info/search`,
    method: 'get',
    params
  })
}

// 教师端：根据手机号获取单个教师详细信息
export function getTeacherByPhone(phone) {
  return request({
    url: `/teacher/info/by-phone/${phone}`,
    method: 'get'
  })
}

// 教师端：更新教师信息（同步更新 tb_teacher_team 表）
export function updateTeacherInfo(data) {
  return request({
    url: `/teacher/info/update`,
    method: 'put',
    data: data
  })
}
