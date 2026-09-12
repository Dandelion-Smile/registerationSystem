import request from '@/utils/request'

// 管理员端：查询评分结果列表
export function listScoreResults(competitionId) {
  return request({
    url: '/admin/score/result/list',
    method: 'get',
    params: { competitionId }
  })
}

// 管理员端：查询竞赛排名
export function getCompetitionRanking(competitionId, topN) {
  return request({
    url: `/admin/score/ranking/${competitionId}`,
    method: 'get',
    params: { topN }
  })
}

// 管理员端：确认竞赛排名
export function confirmRanking(data) {
  return request({
    url: '/admin/score/ranking/confirm',
    method: 'post',
    data: data
  })
}
