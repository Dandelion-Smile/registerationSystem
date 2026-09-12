import request from '@/utils/request'

/**
 * 查询所有学院名称
 */
export function getColleges() {
  return request({
    url: '/system/college-major/colleges',
    method: 'get'
  })
}

/**
 * 根据学院名称查询专业列表
 */
export function getMajorsByCollege(collegeName) {
  return request({
    url: '/system/college-major/majors',
    method: 'get',
    params: { collegeName }
  })
}

/**
 * 查询学院专业列表
 */
export function getCollegeMajorList(params) {
  return request({
    url: '/system/college-major/list',
    method: 'get',
    params
  })
}

/**
 * 查询学院专业详细
 */
export function getCollegeMajor(id) {
  return request({
    url: '/system/college-major/' + id,
    method: 'get'
  })
}

/**
 * 新增学院专业
 */
export function addCollegeMajor(data) {
  return request({
    url: '/system/college-major/add',
    method: 'post',
    data
  })
}

/**
 * 修改学院专业
 */
export function updateCollegeMajor(data) {
  return request({
    url: '/system/college-major/edit',
    method: 'put',
    data
  })
}

/**
 * 删除学院专业
 */
export function deleteCollegeMajor(id) {
  return request({
    url: '/system/college-major/remove/' + id,
    method: 'delete'
  })
}

/**
 * 批量删除学院专业
 */
export function deleteCollegeMajorByIds(ids) {
  return request({
    url: '/system/college-major/batchRemove',
    method: 'delete',
    data: ids
  })
}
