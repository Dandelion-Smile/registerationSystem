import request from '@/utils/request'


export function getCompetitionDetail(competitionId, params = {}) {
  return request({
    url: '/admin/competition/detail/' + competitionId,
    method: 'get',
    params
  })
}

export function getCompetitionStats() {
  return request({
    url: '/admin/competition/stats',
    method: 'get'
  })
}

export function getCompetitionTypes() {
  return request({
    url: '/admin/competition/types',
    method: 'get'
  })
}

// 管理员端：查询竞赛列表
export function listCompetition(query) {
  return request({
    url: '/admin/competition/list',
    method: 'get',
    params: query
  })
}

// 管理员端：查询竞赛详情
export function getCompetition(competitionId) {
  return request({
    url: `/admin/competition/${competitionId}`,
    method: 'get'
  })
}

// 管理员端：新增竞赛
export function addCompetition(data) {
  return request({
    url: '/admin/competition',
    method: 'post',
    data: data
  })
}

// 管理员端：修改竞赛
export function updateCompetition(data) {
  return request({
    url: '/admin/competition',
    method: 'put',
    data: data
  })
}

// 管理员端：删除竞赛
export function delCompetition(competitionIds) {
  return request({
    url: `/admin/competition/${competitionIds}`,
    method: 'delete'
  })
}

export function listCompetitionReviewers(competitionId) {
  return request({
    url: `/admin/competition/${competitionId}/reviewers`,
    method: 'get'
  })
}

export function listCompetitionReviewerCandidates(competitionId) {
  return request({
    url: `/admin/competition/${competitionId}/reviewers/candidates`,
    method: 'get'
  })
}

export function addCompetitionReviewers(competitionId, data) {
  return request({
    url: `/admin/competition/${competitionId}/reviewers`,
    method: 'post',
    data
  })
}

export function deleteCompetitionReviewer(competitionId, teacherId) {
  return request({
    url: `/admin/competition/${competitionId}/reviewers/${teacherId}`,
    method: 'delete'
  })
}

export function autoAssignCompetitionReviewers(competitionId, data) {
  return request({
    url: `/admin/competition/${competitionId}/review-assignments/auto`,
    method: 'post',
    data
  })
}

export function updateCompetitionTeamReviewers(competitionId, teamId, data) {
  return request({
    url: `/admin/competition/${competitionId}/teams/${teamId}/reviewers`,
    method: 'put',
    data
  })
}
