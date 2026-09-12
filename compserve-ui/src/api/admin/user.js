import request from '@/utils/request'

// 管理员端：批量创建教师账号
export function batchCreateTeachers(data) {
  return request({
    url: '/admin/user/teachers/batch',
    method: 'post',
    data: data
  })
}

// 管理员端：查询教师列表
export function listTeachers(query) {
  return request({
    url: '/admin/user/teachers',
    method: 'get',
    params: query
  })
}

// 管理员端：获取教师详情
export function getTeacherInfo(userId) {
  return request({
    url: `/admin/user/teachers/${userId}`,
    method: 'get'
  })
}

// 管理员端：新增单个教师账号
export function insertTeacher(data) {
  return request({
    url: '/admin/user/teachers',
    method: 'post',
    data: data
  })
}

// 管理员端：更新教师账号
export function updateTeacher(data) {
  return request({
    url: '/admin/user/teachers',
    method: 'put',
    data: data
  })
}

// 管理员端：删除教师账号
export function deleteTeachers(userIds) {
  return request({
    url: `/admin/user/teachers/${userIds}`,
    method: 'delete'
  })
}

// 管理员端：重置教师密码
export function resetTeacherPassword(userId, newPassword) {
  return request({
    url: `/admin/user/teachers/${userId}/resetPwd`,
    method: 'put',
    data: { newPassword }
  })
}

// 管理员端：修改教师状态
export function changeTeacherStatus(userId, status) {
  return request({
    url: `/admin/user/teachers/${userId}/status`,
    method: 'put',
    data: { status }
  })
}

// 管理员端：Excel导入教师账号
export function importTeachers(formData) {
  return request({
    url: '/admin/user/teachers/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 管理员端：查询学生列表
export function listStudents(query) {
  return request({
    url: '/admin/user/students',
    method: 'get',
    params: query
  })
}

// 管理员端：更新学生账号
export function updateStudent(data) {
  return request({
    url: '/admin/user/students',
    method: 'put',
    data: data
  })
}

// 管理员端：重置学生密码
export function resetStudentPassword(userId, newPassword) {
  return request({
    url: `/admin/user/students/${userId}/resetPwd`,
    method: 'put',
    data: { newPassword }
  })
}

export function getStudentAvatars(studentNos) {
  return request({
    url: '/admin/user/students/avatars',
    method: 'post',
    data: { studentNos }
  })
}

export function getUserAvatarsByIds(userIds) {
  return request({
    url: '/admin/user/users/avatars',
    method: 'post',
    data: { userIds }
  })
}

export function getStudentAvatarsByNames(names) {
  return request({
    url: '/admin/user/students/avatars-by-names',
    method: 'post',
    data: { names }
  })
}