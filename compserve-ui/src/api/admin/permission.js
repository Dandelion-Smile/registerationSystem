import request from '@/utils/request'

// 管理员端：获取所有竞赛列表（用于赋权选择）
export function listCompetitions() {
  return request({
    url: '/admin/permission/competitions',
    method: 'get'
  })
}

// 管理员端：获取所有教师列表（用于赋权选择）
export function listTeachers() {
  return request({
    url: '/admin/permission/teachers',
    method: 'get'
  })
}

// 管理员端：批量赋权
export function grantPermission(data) {
  return request({
    url: '/admin/permission/grant',
    method: 'post',
    data: data
  })
}

// 管理员端：撤销权限
export function revokePermission(competitionId, teacherId) {
  return request({
    url: `/admin/permission/revoke/${competitionId}/${teacherId}`,
    method: 'delete'
  })
}

// 管理员端：查询已赋权列表
export function listPermissions(params) {
  return request({
    url: '/admin/permission/list',
    method: 'get',
    params: params
  })
}

// 管理员端：检查教师是否是指导老师
export function checkAdvisor(teacherId, competitionId) {
  return request({
    url: '/admin/permission/check-advisor',
    method: 'get',
    params: {
      teacherId,
      competitionId
    }
  })
}
