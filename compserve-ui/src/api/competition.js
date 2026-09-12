import request from "@/utils/request";

// 瀛︾敓绔細鑾峰彇绔炶禌鍒楄〃锛堟敮鎸佸垎椤靛拰绛涢€夛級
export function listStudentCompetitions(pageNum, pageSize, filters) {
  return request({
    url: "/student/competition/list",
    method: "get",
    params: {
      pageNum: pageNum || 1,
      pageSize: pageSize || 10,
      keyword: filters?.keyword || "",
      filterType: filters?.type || "",
      filterLevel: filters?.level || "",
      filterStatus: filters?.status || "",
      filterParticipants: filters?.participants || "",
    },
  });
}

// 瀛︾敓绔細鏌ヨ褰撳墠鐢ㄦ埛鍦ㄦ煇涓珵璧涗笅鐨勬姤鍚嶇姸鎬?
export function getCompetitionTypes() {
  return request({
    url: "/student/competition/types",
    method: "get",
  });
}

export function getCompetitionStatus(competitionId, params = {}) {
  const query = params && typeof params === "object" ? params : { teamId: params };
  return request({
    url: `/student/competition/${competitionId}/status`,
    method: "get",
    params: query,
  });
}

// 瀛︾敓绔細鑾峰彇绔炶禌璇︽儏
export function getCompetitionDetail(competitionId) {
  return request({
    url: `/student/competition/${competitionId}`,
    method: "get",
  });
}

// 瀛︾敓绔細鎶ュ悕绔炶禌
export function registerCompetition(data) {
  return request({
    url: "/student/competition/register",
    method: "post",
    data,
  });
}

// 瀛︾敓绔細鑾峰彇绔炶禌鐨勯槦浼嶅垪琛?
export function saveCompetitionTeamName(data) {
  return request({
    url: "/student/competition/team/name",
    method: "post",
    data,
  });
}

export function getCompetitionTeams(competitionId) {
  return request({
    url: `/student/competition/${competitionId}/teams`,
    method: "get",
  });
}

// 瀛︾敓绔細鏌ヨ宸蹭笂浼犵殑鍙傝禌鏉愭枡淇℃伅
export function getUploadedMaterials(registerId) {
  return request({
    url: `/student/competition/material/${registerId}`,
    method: "get",
  });
}

export function getStudentWorkDetail(registerId) {
  return request({
    url: `/student/competition/work-detail/${registerId}`,
    method: "get",
  });
}

// 瀛︾敓绔細鏌ヨ褰撳墠鐢ㄦ埛鏈€杩慛鏉℃姤鍚嶈褰?
export function getRecentRegistrations(limit = 10) {
  return request({
    url: "/student/competition/recent-registrations",
    method: "get",
    params: { limit },
  });
}

// 瀛︾敓绔細鎻愪氦鏉愭枡
export function getRegistrationPage(pageNum = 1, pageSize = 10) {
  return request({
    url: "/student/competition/registered-page",
    method: "get",
    params: { pageNum, pageSize },
  });
}

export function submitMaterials(data) {
  return request({
    url: "/student/competition/material",
    method: "post",
    data,
  });
}

export function cancelRegistration(registerId) {
  return request({
    url: `/student/competition/register/${registerId}`,
    method: "delete",
  });
}

export function getTeamInvitations() {
  return request({
    url: "/student/competition/team/invitations",
    method: "get",
    headers: { silent: true },
    validateStatus: () => true,
  });
}

export function approveTeamInvitation(teamId, studentNo, userId) {
  return request({
    url: "/student/competition/team/approve",
    method: "post",
    data: { teamId, studentNo, userId },
    headers: { silent: true },
    validateStatus: () => true,
  });
}

export function rejectTeamInvitation(teamId, studentNo, userId) {
  return request({
    url: "/student/competition/team/reject",
    method: "post",
    data: { teamId, studentNo, userId },
    headers: { silent: true },
    validateStatus: () => true,
  });
}

export function inviteTeamMember(teamId, studentNo) {
  return request({
    url: "/student/competition/team/invite",
    method: "post",
    data: { teamId, studentNo },
  });
}

export function applyJoinTeam(teamId) {
  return request({
    url: "/student/competition/team/apply",
    method: "post",
    data: { teamId },
  });
}

export function removeTeamMember(teamId, studentNo) {
  return request({
    url: "/student/competition/team/remove",
    method: "post",
    data: { teamId, studentNo },
  });
}
