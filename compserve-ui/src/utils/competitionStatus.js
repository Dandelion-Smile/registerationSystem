export const MEMBER_STATUS = Object.freeze({
  APPROVED: "approved",
  PENDING: "pending",
  REJECTED: "rejected",
  EXPIRED: "expired",
})

export const MEMBER_TYPE = Object.freeze({
  INVITE: "invite",
  APPLY: "apply",
})

export const REVIEW_STATUS = Object.freeze({
  NONE: "",
  PENDING_REVIEW: "pending_review",
  IN_REVIEW: "in_review",
  REVIEWED: "reviewed",
})

export const RECORD_STATUS = Object.freeze({
  DRAFT: "draft",
  READY: "ready",
  SUBMITTED: "submitted",
  IN_REVIEW: "in_review",
  REVIEWED: "reviewed",
})

export const PARTICIPATION_STATUS = Object.freeze({
  NOT_SUBMITTED: "未提交",
  SUBMITTED: "已提交",
  SCORED: "已评分",
})

export function normalizeStatus(value) {
  return String(value || "").trim().toLowerCase()
}

export function isPendingMemberStatus(value) {
  return normalizeStatus(value) === MEMBER_STATUS.PENDING
}

export function isApprovedMemberStatus(value) {
  const normalized = normalizeStatus(value)
  return normalized === "" || normalized === MEMBER_STATUS.APPROVED
}

export function isReviewLockedStatus(code, text = "") {
  const normalizedCode = normalizeStatus(code)
  const normalizedText = String(text || "").trim()
  return (
    normalizedCode === REVIEW_STATUS.IN_REVIEW ||
    normalizedCode === REVIEW_STATUS.REVIEWED ||
    normalizedText === "评审中" ||
    normalizedText === "已评审"
  )
}

export function getMemberStatusLabel(status, type = "") {
  const normalizedStatus = normalizeStatus(status)
  const normalizedType = normalizeStatus(type)
  if (normalizedStatus === MEMBER_STATUS.PENDING) {
    if (normalizedType === MEMBER_TYPE.APPLY) return "待队长审批"
    if (normalizedType === MEMBER_TYPE.INVITE) return "待对方同意"
    return "待审核"
  }
  if (normalizedStatus === MEMBER_STATUS.REJECTED) return "已拒绝"
  if (normalizedStatus === MEMBER_STATUS.EXPIRED) return "已失效"
  if (normalizedStatus === MEMBER_STATUS.APPROVED || normalizedStatus === "") return "已通过"
  return ""
}

export function getProfileRecordStatusInfo(item = {}) {
  const explicitText = String(
    item.recordStatusText || item.reviewStatusText || item.statusText || "",
  ).trim()
  if (explicitText === "待完善信息") return { text: "待完善信息", cls: "status-pending" }
  if (explicitText === "可提交资料") return { text: "可提交资料", cls: "status-reviewing" }
  if (explicitText === "已评审") return { text: "已评审", cls: "status-approved" }
  if (explicitText === "评审中") return { text: "评审中", cls: "status-reviewing" }
  if (explicitText === "待审核") return { text: "待审核", cls: "status-pending" }

  const raw = normalizeStatus(
    item.recordStatus ?? item.reviewStatusCode ?? item.reviewStatus ?? item.status,
  )
  if (raw === RECORD_STATUS.DRAFT) return { text: "待完善信息", cls: "status-pending" }
  if (raw === RECORD_STATUS.READY) return { text: "可提交资料", cls: "status-reviewing" }
  if (raw === RECORD_STATUS.REVIEWED || raw === "approved" || raw === "pass") {
    return { text: "已评审", cls: "status-approved" }
  }
  if (raw === RECORD_STATUS.IN_REVIEW) return { text: "评审中", cls: "status-reviewing" }
  return { text: "待审核", cls: "status-pending" }
}
