﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <div class="student-page">
    <StudentNavbar v-if="!isTeacher" />
    <header v-else class="header-nav">
      <div class="nav-container">
        <div class="logo-section">
          <i class="fas fa-trophy"></i>
          <span class="logo-text">智启赛途</span>
        </div>
        <nav class="main-nav">
          <router-link :to="{ path: '/teacher', query: { menu: 'competition' } }" class="nav-item" :class="{ active: route.path === '/teacher' && (route.query.menu === 'competition' || !route.query.menu) }">赛事管理</router-link>
          <router-link :to="{ path: '/teacher', query: { menu: 'review' } }" class="nav-item" :class="{ active: route.path === '/teacher' && route.query.menu === 'review' }">评审管理</router-link>
          <router-link :to="{ path: '/teacher', query: { menu: 'my-teams' } }" class="nav-item" :class="{ active: route.path === '/teacher' && route.query.menu === 'my-teams' }">我的队伍赛事</router-link>
        </nav>
        <div class="nav-actions">
          <el-dropdown @command="handleCommand" class="avatar-container" trigger="hover">
            <div class="avatar-wrapper">
              <img :src="userStore.avatar" class="user-avatar-circle" />
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <router-link :to="profileCenterPath">
                  <el-dropdown-item>个人中心</el-dropdown-item>
                </router-link>
                <el-dropdown-item divided command="logout">
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="main-content">
      <section class="profile-header">
        <div class="avatar-wrap">
          <userAvatar />
        </div>
        <div class="profile-info">
          <h1 class="name">{{ isTeacher ? teacherProfile.name : student.name }}</h1>
          <div v-if="!isTeacher" class="tags">
            <span class="tag chip"><i class="fa fa-university"></i><span class="tag-text">学院：{{ student.college }}</span></span>
            <span class="tag chip"><i class="fa fa-book-open"></i><span class="tag-text">专业：{{ student.major }}</span></span>
             <span class="tag chip copyable" @click="copyText(student.clazz, '班级：')" title="点击复制班级"><i class="fa fa-users"></i><span class="tag-text">班级：{{ student.clazz }}</span></span>
            <span class="tag chip copyable" @click="copyText(student.studentNo, '学号：')" title="点击复制学号"><i class="fa fa-id-badge"></i><span class="tag-text">学号：{{ student.studentNo }}</span></span>
          </div>
          <div v-else class="tags">
            <span class="tag chip"><i class="fa fa-user"></i><span class="tag-text">用户名：{{ state.user.userName || '未填写' }}</span></span>
            <span class="tag chip"><i class="fa fa-briefcase"></i><span class="tag-text">职位：{{ teacherProfile.position }}</span></span>
            <span class="tag chip"><i class="fa fa-graduation-cap"></i><span class="tag-text">职称：{{ teacherProfile.title }}</span></span>
            <span class="tag chip"><i class="fa fa-building"></i><span class="tag-text">工作单位：{{ teacherProfile.workUnit }}</span></span>
            <span class="tag chip"><i class="fa fa-envelope"></i><span class="tag-text">邮箱：{{ state.user.email || '未填写' }}</span></span>
            <span class="tag chip"><i class="fa fa-phone"></i><span class="tag-text">手机号：{{ state.user.phonenumber || '未填写' }}</span></span>
          </div>
          <div class="meta">
            <span><i class="fa fa-users"></i> 角色：{{ state.roleGroup }}</span>
            <span v-if="state.user.createTime"><i class="fa fa-clock"></i> 创建时间：{{ state.user.createTime }}</span>
          </div>
          <div class="actions">
            <button class="btn primary" @click="openEdit"><i class="fa fa-edit"></i> 编辑资料</button>
            <button class="btn" @click="openPwd"><i class="fa fa-key"></i> 修改密码</button>
          </div>
          <div class="profile-completeness">
            <div class="complete-label">资料完善度</div>
            <div class="complete-bar">
              <div class="complete-fill" :style="{ width: completeness + '%' }"></div>
            </div>
            <div class="complete-percent">{{ completeness }}%</div>
          </div>
        </div>
      </section>

      <section class="summary-cards">
        <div class="summary-card">
          <div class="summary-icon last"><i class="fa fa-sign-in-alt"></i></div>
          <div class="summary-info">
            <div class="summary-label">最近登录时间</div>
            <div class="summary-value">{{ lastLoginDisplay }}</div>
          </div>
        </div>
        <div class="summary-card">
          <div class="summary-icon secure"><i class="fa fa-shield-alt"></i></div>
          <div class="summary-info">
            <div class="summary-label">安全设置状态</div>
            <div class="summary-value">{{ securityStatus }}</div>
          </div>
        </div>
        <div class="summary-card">
          <div class="summary-icon list"><i class="fa fa-list-ul"></i></div>
          <div class="summary-info">
            <div class="summary-label">{{ thirdSummaryLabel }}</div>
            <div class="summary-value">{{ thirdSummaryValue }}</div>
          </div>
        </div>
      </section>

      <div class="content-wrapper">
        <aside class="side-nav">
          <div
            v-for="item in sideNav"
            :key="item.key"
            class="side-nav-item"
            :class="{ active: activeSection === item.key }"
            @click="activeSection = item.key"
          >
            <i :class="item.icon"></i>
            <span>{{ item.label }}</span>
            <span v-if="item.key === 'messages' && unreadCount > 0 && activeSection !== 'messages'" class="red-dot"></span>
            <span v-if="item.key === 'messages' && unreadCount > 0" class="badge">{{ unreadCount }}</span>
            <i v-if="activeSection === item.key" class="fas fa-angle-right side-arrow"></i>
          </div>
        </aside>
        <section class="right-content">
          <div v-if="activeSection === 'role'" class="info-card theme-role">
            <h3><i class="fa fa-shield-alt"></i> 角色信息</h3>
            <div class="role-group">{{ state.roleGroup }}</div>
            <div class="post-group" v-if="state.postGroup">岗位：{{ state.postGroup }}</div>
          </div>
          <div v-else-if="activeSection === 'account'" class="info-card theme-account">
            <h3><i class="fa fa-id-card"></i> 账户信息</h3>
            <ul class="info-list">
              <li>
                <span class="label">用户名</span>
                <span class="value">{{ state.user.userName }}</span>
              </li>
              <li>
                <span class="label">性别</span>
                <span class="value">{{ sexLabel }}</span>
              </li>
              <li>
                <span class="label">邮箱</span>
                <span class="value">{{ state.user.email || '未填写' }}</span>
              </li>
              <li>
                <span class="label">手机</span>
                <span class="value">{{ state.user.phonenumber || '未填写' }}</span>
              </li>
              <li v-if="state.user.dept && !isTeacher">
                <span class="label">部门</span>
                <span class="value">{{ state.user.dept.deptName }}</span>
              </li>
            </ul>
          </div>
          <div v-else-if="activeSection === 'academic' && !isTeacher" class="info-card theme-academic">
            <h3><i class="fa fa-id-badge"></i> 学籍信息</h3>
            <ul class="info-list">
              <li>
                <span class="label">姓名</span>
                <span class="value">{{ student.name }}</span>
              </li>
              <li>
                <span class="label">学号</span>
                <span class="value">{{ student.studentNo }}</span>
              </li>
              <li>
                <span class="label">学院</span>
                <span class="value">{{ student.college }}</span>
              </li>
              <li>
                <span class="label">专业</span>
                <span class="value">{{ student.major }}</span>
              </li>
              <li>
                <span class="label">班级</span>
                <span class="value">{{ student.clazz }}</span>
              </li>
            </ul>
          </div>
          <div v-else-if="activeSection === 'teacher' && isTeacher" class="info-card theme-academic">
            <h3><i class="fa fa-briefcase"></i> 教师信息</h3>
            <ul class="info-list">
              <li>
                <span class="label">工号</span>
                <span class="value">{{ teacherProfile.workNo }}</span>
              </li>
              <li>
                <span class="label">职位</span>
                <span class="value">{{ teacherProfile.position }}</span>
              </li>
              <li>
                <span class="label">职称</span>
                <span class="value">{{ teacherProfile.title }}</span>
              </li>
              <li>
                <span class="label">工作单位</span>
                <span class="value">{{ teacherProfile.workUnit }}</span>
              </li>
              <li>
                <span class="label">政治面貌</span>
                <span class="value">{{ teacherProfile.politicalStatus }}</span>
              </li>
              <li>
                <span class="label">出生年月</span>
                <span class="value">{{ teacherProfile.birthDate }}</span>
              </li>
            </ul>
          </div>
          <div v-else-if="activeSection === 'registered' && !isTeacher" class="theme-registered">
            <StudentRegisteredCompetitionList
              :items="registeredCompetitions"
              @detail="gotoCompetition"
              @cancel="onCancel"
            />
          </div>
          <div v-else-if="activeSection === 'messages' && !isTeacher" class="info-card theme-registered">
            <h3><i class="fa fa-bell"></i> 我的消息 <span v-if="unreadCount > 0" class="count-badge">{{ unreadCount }}</span></h3>
            <div class="invitation-list">
              <div v-for="invitation in invitations" :key="invitation.id" class="invitation-item">
                <div class="invitation-info">
                  <div class="team-name">{{ invitation.teamName }}</div>
                  <div class="invitation-meta">
                    <span class="meta-pill">
                      {{
                        invitation.type === 'apply'
                          ? '入队申请'
                          : invitation.type === 'apply-result'
                            ? '申请结果'
                            : '队伍邀请'
                      }}
                    </span>
                    <span class="meta-user" v-if="invitation.type === 'apply' && (invitation.applicantName || invitation.memberName)">申请人: {{ invitation.applicantName || invitation.memberName }}</span>
                    <span class="meta-user" v-if="invitation.inviterName && invitation.type !== 'apply'">邀请人: {{ invitation.inviterName }}</span>
                    <span class="meta-comp" v-if="invitation.competitionName">竞赛: {{ invitation.competitionName }}</span>
                    <span class="meta-id" v-else>竞赛ID: {{ invitation.competitionId }}</span>
                    <span v-if="invitation.type === 'apply-result'" class="meta-user">
                      {{ invitation.status === 'approved' ? '结果：已通过' : '结果：已驳回' }}
                    </span>
                  </div>
                </div>
                <div class="invitation-actions" v-if="invitation.status === 'pending'">
                  <button class="btn primary" @click="approveInvitation(invitation.teamId, invitation.memberStudentNo)">同意</button>
                  <button class="btn" @click="rejectInvitation(invitation.teamId, invitation.memberStudentNo)">拒绝</button>
                </div>
              </div>
              <div v-if="invitations.length === 0" class="invitation-empty">暂无待处理的消息</div>
            </div>
          </div>
        </section>
      </div>

      <el-dialog v-model="editDialogVisible" title="编辑个人资料" width="820px" :close-on-click-modal="false" align-center class="center-dialog">
        <div class="dialog-columns">
          <div class="section-card">
            <div class="section-title"><i class="fa fa-user-circle"></i> 账户信息</div>
            <el-form ref="editRef" :model="editForm" :rules="editRules" label-width="90px">
              <el-form-item label="用户名" prop="userName">
                <el-input v-model="editForm.userName" disabled />
              </el-form-item>
              <el-form-item label="姓名" prop="realName">
                <el-input v-model="editForm.realName" maxlength="30" />
              </el-form-item>
              <el-form-item label="性别" prop="sex">
                <el-radio-group v-model="editForm.sex">
                  <el-radio value="0">男</el-radio>
                  <el-radio value="1">女</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="手机号" prop="phonenumber" required>
                <el-input v-model="editForm.phonenumber" maxlength="11" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email" required>
                <el-input v-model="editForm.email" maxlength="50" />
              </el-form-item>
            </el-form>
          </div>
          <div class="section-card">
            <div class="section-title"><i class="fa fa-id-badge"></i> {{ isTeacher ? '教师信息' : '学籍信息' }}</div>
            <el-form ref="academicRef" :model="editForm" :rules="editRules" label-width="90px">
              <template v-if="isTeacher">
                <el-form-item label="工号" prop="workNo">
                  <el-input v-model="editForm.workNo" disabled />
                </el-form-item>
                <el-form-item label="职位" prop="position">
                  <el-input v-model="editForm.position" maxlength="30" />
                </el-form-item>
                <el-form-item label="职称" prop="title">
                  <el-input v-model="editForm.title" maxlength="30" />
                </el-form-item>
                <el-form-item label="工作单位" prop="workUnit">
                  <el-input v-model="editForm.workUnit" maxlength="100" />
                </el-form-item>
                <el-form-item label="政治面貌" prop="politicalStatus">
                  <el-select v-model="editForm.politicalStatus" placeholder="请选择政治面貌">
                    <el-option value="中共党员">中共党员</el-option>
                    <el-option value="中共预备党员">中共预备党员</el-option>
                    <el-option value="共青团员">共青团员</el-option>
                    <el-option value="群众">群众</el-option>
                    <el-option value="民主党派">民主党派</el-option>
                    <el-option value="其他">其他</el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="出生年月" prop="birthDate">
                  <el-date-picker v-model="editForm.birthDate" type="month" placeholder="选择出生年月" style="width: 100%;" />
                </el-form-item>
              </template>
              <template v-else>
                <el-form-item label="学号" prop="studentNo">
                  <el-input v-model="editForm.studentNo" disabled />
                </el-form-item>
                <el-form-item label="学院" prop="college" required>
                  <el-select v-model="editForm.college" placeholder="请选择学院" filterable @change="onProfileCollegeChange">
                    <el-option v-for="c in colleges" :key="c" :label="c" :value="c" />
                  </el-select>
                </el-form-item>
                <el-form-item label="专业" prop="major" required>
                  <el-select v-model="editForm.major" placeholder="请先选择学院" filterable :disabled="!editForm.college">
                    <el-option v-for="m in profileMajorOptions" :key="m" :label="m" :value="m" />
                  </el-select>
                </el-form-item>
                <el-form-item label="班级" prop="className" required>
                  <el-input v-model="editForm.className" maxlength="30" />
                </el-form-item>
              </template>
            </el-form>
          </div>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="editDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="saving" @click="submitEdit">保存修改</el-button>
          </div>
        </template>
      </el-dialog>

      <el-dialog v-model="pwdDialogVisible" title="修改密码" width="560px" :close-on-click-modal="false" align-center class="center-dialog">
        <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
          <el-form-item label="旧密码" prop="oldPassword" required>
            <el-input v-model="pwdForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword" required>
            <el-input v-model="pwdForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword" required>
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="pwdDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitPwd">保存</el-button>
          </div>
        </template>
      </el-dialog>
    </main>
  </div>
</template>

<script setup name="StudentProfile">
import StudentNavbar from "@/components/StudentNavbar.vue"
import StudentRegisteredCompetitionList from "@/components/StudentRegisteredCompetitionList.vue"
import userAvatar from "@/views/system/user/profile/userAvatar.vue"
import { getUserProfile, updateUserProfile, updateUserPwd } from "@/api/system/user"
import { getRecentRegistrations, getTeamInvitations, approveTeamInvitation, rejectTeamInvitation } from "@/api/competition"
import { getTeacherByPhone, updateTeacherInfo } from "@/api/teacher"
import { getColleges, getMajorsByCollege } from "@/api/college-major"
import { formatDate as formatDateText } from "@/utils/index"
import { ElMessageBox, ElMessage } from "element-plus"
import useUserStore from "@/store/modules/user"
import { watch } from 'vue'
import { getProfileRecordStatusInfo } from "@/utils/competitionStatus"
import { sortRegisteredCompetitions } from "@/utils/registeredCompetition"
const router = useRouter()
const route = useRoute()

const activeSection = ref(route.query.tab || 'role')
const state = reactive({
  user: {},
  roleGroup: {},
  postGroup: {}
})
const userStore = useUserStore()
const isTeacher = computed(() => {
  const roles = userStore.roles || []
  return roles.includes('teacher') || route.path.startsWith('/teacher')
})
const sideNav = computed(() => {
  if (isTeacher.value) {
    return [
      { key: 'role', label: '角色信息', icon: 'fa fa-shield-alt' },
      { key: 'account', label: '账户信息', icon: 'fa fa-id-card' },
      { key: 'teacher', label: '教师信息', icon: 'fa fa-briefcase' }
    ]
  }
  const base = [
    { key: 'role', label: '角色信息', icon: 'fa fa-shield-alt' },
    { key: 'account', label: '账户信息', icon: 'fa fa-id-card' },
    { key: 'academic', label: '学籍信息', icon: 'fa fa-id-badge' }
  ]
  return [
    ...base,
    { key: 'registered', label: '已报名的竞赛', icon: 'fa fa-list-ul' },
    { key: 'messages', label: '我的消息', icon: 'fa fa-bell' }
  ]
})
const profileCenterPath = computed(() => isTeacher.value ? '/teacher/profile' : '/student-center/profile')
const homeLink = computed(() => {
  const roles = userStore.roles || []
  if (roles.includes('sys_admin') || roles.includes('admin') || roles.includes('sys admin')) {
    return '/sys-admin/dashboard'
  }
  return '/student'
})
const intelligentFeatures = ref([
  { name: '报名管理智能助手', icon: 'fas fa-robot' },
  { name: '竞赛日程智能提醒', icon: 'fas fa-bell' },
  { name: '参赛资料智能审查', icon: 'fas fa-search' },
  { name: '队伍匹配智能推荐', icon: 'fas fa-users' }
])

const lastLoginDisplay = computed(() => {
  const ts = state.user && state.user.loginDate
  return ts ? formatDateText(ts) : '—'
})

const securityStatus = computed(() => {
  const hasPhone = !!(state.user && state.user.phonenumber)
  const hasEmail = !!(state.user && state.user.email)
  if (hasPhone && hasEmail) return '已绑定手机与邮箱'
  if (hasPhone) return '已绑定手机'
  if (hasEmail) return '已绑定邮箱'
  return '未绑定联系方式'
})

const sexLabel = computed(() => {
  const s = state.user && state.user.sex
  if (s === '0' || s === 0) return '男'
  if (s === '1' || s === 1) return '女'
  return '—'
})

const registeredCount = ref(0)
const invitations = ref([])
const thirdSummaryLabel = computed(() => isTeacher.value ? '账号角色' : '已报名数量')
const thirdSummaryValue = computed(() => isTeacher.value ? (state.roleGroup || '-') : registeredCount.value)
const unreadCount = computed(() => {
  return invitations.value.filter(it => !it.read && (it.status === 'pending' || it.status === 'PENDING')).length
})
const teacherInfo = ref({})

const teacherProfile = computed(() => {
  const u = state.user || {}
  const t = teacherInfo.value || {}
  return {
    name: t.teacherName || u.nickName || u.studentName || '未填写',
    workNo: t.userName || u.userName || '未填写',
    politicalStatus: t.politicalStatus || u.politicalStatus || '未填写',
    birthDate: t.birthDate || u.birthDate || '未填写',
    position: t.position || u.position || state.postGroup || '未填写',
    title: t.title || u.title || u.remark || '未填写',
    department: (u.dept && u.dept.deptName) || u.collegeName || '未填写',
    workUnit: t.workUnit || u.workUnit || ((u.dept && u.dept.deptName) || u.collegeName || '未填写')
  }
})
const completeness = computed(() => {
  const u = state.user || {}
  if (isTeacher.value) {
    const items = [
      u.nickName,
      u.position || state.postGroup,
      u.title || u.remark,
      u.workUnit || (u.dept && u.dept.deptName),
      u.email,
      u.phonenumber
    ]
    const total = items.length
    const filled = items.filter(v => v !== undefined && v !== null && String(v).trim() !== '').length
    return Math.max(0, Math.min(100, Math.round((filled / total) * 100)))
  }
  const items = [
    u.studentName,
    u.studentNo,
    u.collegeName || (u.dept && u.dept.deptName),
    u.majorName,
    u.className,
    u.email,
    u.phonenumber
  ]
  const total = items.length
  const filled = items.filter(v => v !== undefined && v !== null && String(v).trim() !== '').length
  return Math.max(0, Math.min(100, Math.round((filled / total) * 100)))
})

const registeredCompetitions = ref([])
function loadRegisteredCount() {
  return getRecentRegistrations(0).then(res => {
    const list = sortRegisteredCompetitions(Array.isArray(res.data) ? res.data : [])
    registeredCount.value = Array.isArray(list) ? list.length : 0
  }).catch(() => {
    registeredCount.value = 0
  })
}
function deriveLevel(name) {
  const text = name || ''
  if (text.includes('国') || text.includes('国家')) return { code: 'national', label: '国赛' }
  if (text.includes('省') || text.includes('市')) return { code: 'provincial', label: '省赛' }
  if (text.includes('校')) return { code: 'school', label: '校赛' }
  return { code: '', label: '竞赛' }
}
function getStatusInfo(item) {
  return getProfileRecordStatusInfo(item)
}
function loadRegistered() {
  return getRecentRegistrations(0).then(res => {
    const list = sortRegisteredCompetitions(Array.isArray(res.data) ? res.data : [])
    registeredCount.value = Array.isArray(list) ? list.length : 0
    registeredCompetitions.value = list.map(it => {
      const lvl = deriveLevel(it.competitionName)
      const st = getStatusInfo(it)
      return {
        id: it.registerId || it.id || `${it.competitionId}-${it.teamId}-${it.registerTime}`,
        competitionId: it.competitionId || null,
        name: it.competitionName || '未知竞赛',
        teamName: it.teamName || null,
        teamId: it.teamId || null,
        registerId: it.registerId || null,
        displayRegisterId: it.displayRegisterId || null,
        registerTime: it.registerTime || '',
        canCancel: !!it.canCancel,
        levelCode: lvl.code,
        levelLabel: lvl.label,
        recordStatus: it.recordStatus || '',
        reviewStatusCode: it.reviewStatusCode || it.reviewStatus || '',
        statusText: st.text,
        statusClass: st.cls
      }
    })
  }).catch(() => {
    registeredCount.value = 0
    registeredCompetitions.value = []
  })
}
function shouldUseSubmissionDetail(item) {
  const displayText = String(item?.statusText || '').trim()
  const recordStatus = String(item?.recordStatus || '').trim().toLowerCase()
  const reviewStatusCode = String(item?.reviewStatusCode || '').trim().toLowerCase()
  return (
    ['待审核', '评审中', '已评审'].includes(displayText) ||
    ['submitted', 'in_review', 'reviewed'].includes(recordStatus) ||
    ['pending_review', 'in_review', 'reviewed'].includes(reviewStatusCode)
  )
}
function gotoCompetition(item) {
  if (item?.registerId && shouldUseSubmissionDetail(item)) {
    router.push({
      path: `/student-work-detail/${item.registerId}`,
      query: {
        competitionId: item.competitionId || '',
        name: item.name || ''
      }
    })
    return
  }
  const id = item.competitionId
  const name = item.name
  const teamId = item.teamId || item.displayTeamId
  if (!id) return
  router.push({
    path: `/competition/register/${id}`,
    query: {
      name,
      teamId,
      registerId: item.registerId || item.displayRegisterId || ''
    }
  })
}
async function onCancel(item) {
  if (!item.registerId) return
  try {
    await ElMessageBox.confirm('确认取消该竞赛的报名？', '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    const { cancelRegistration } = await import('@/api/competition')
    await cancelRegistration(item.registerId)
    ElMessage.success('已取消报名')
    loadRegistered()
  } catch (e) {
    const msg = (e && e.msg) || (e && e.message) || (e && e.response && e.response.data && e.response.data.msg) || '取消失败'
    ElMessage.error(msg)
  }
}

async function getUser() {
  const response = await getUserProfile()
  state.user = response.data
  state.roleGroup = response.roleGroup
  state.postGroup = response.postGroup
  
  // 如果是教师，从 tb_teacher_team 表获取教师信息
  if (isTeacher.value && state.user.phonenumber) {
    try {
      const teacherRes = await getTeacherByPhone(state.user.phonenumber)
      if (teacherRes && teacherRes.data) {
        teacherInfo.value = teacherRes.data
      }
    } catch (error) {
      console.error('获取教师信息失败:', error)
    }
  }
  
  return response
}

onMounted(() => {
  getUser()
  loadCollegeMajorData()
  if (!isTeacher.value) {
    loadRegistered()
  }
  
  if (!sideNav.value.some(item => item.key === activeSection.value)) {
    activeSection.value = 'role'
  }
  if (!isTeacher.value && activeSection.value === 'registered') {
    loadRegistered()
  } else if (!isTeacher.value && activeSection.value === 'messages') {
    loadInvitations()
  }
})
watch(() => route.query.tab, (newTab) => {
  if (newTab && sideNav.value.some(item => item.key === newTab)) {
    activeSection.value = newTab
  }
})

watch(activeSection, (val) => {
  if (!isTeacher.value && val === 'registered') {
    loadRegistered()
  }
  if (!isTeacher.value && val === 'messages') {
    loadInvitations()
  }
})

function handleCommand(command) {
  if (command === "logout") {
    ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logOut().then(() => {
        location.href = '/login'
      })
    }).catch(() => {})
  }
}

const student = computed(() => {
  const defaultText = '未填写'
  const name = (state.user && (state.user.studentName || state.user.nickName)) || defaultText
  const studentNo = (state.user && (state.user.studentNo || state.user.studentId || state.user.sno || state.user.userName)) || defaultText
  const college = (state.user && (state.user.collegeName || (state.user.dept && state.user.dept.deptName))) || defaultText
  const major = (state.user && (state.user.majorName || state.user.major || state.roleGroup)) || defaultText
  const clazz = (state.user && (state.user.className || state.user.classNo || state.user.clazz || state.user.phonenumber)) || defaultText
  return { name, studentNo, college, major, clazz }
})

const editRef = ref()
const academicRef = ref()
const pwdRef = ref()
const saving = ref(false)

const editDialogVisible = ref(false)
const editForm = reactive({
  userName: '',
  workNo: '',
  realName: '',
  position: '',
  title: '',
  workUnit: '',
  politicalStatus: '',
  birthDate: '',
  studentNo: '',
  college: '',
  major: '',
  className: '',
  sex: '0',
  phonenumber: '',
  email: ''
})
import { ref } from 'vue'

const collegeMajorMap = ref({
  '粮油食品学院': ['粮食工程', '食品科学与工程', '食品质量与安全'],
  '计算机学院': ['计算机科学与技术', '软件工程', '网络工程', '数据科学与大数据技术'],
  '电子信息学院': ['电子信息工程', '通信工程', '微电子科学与工程', '集成电路设计与集成系统'],
  '数学学院': ['数学与应用数学', '信息与计算科学', '统计学', '应用统计学'],
  '人工智能学院': ['人工智能', '智能科学与技术', '机器人工程', '模式识别与智能系统']
})
const colleges = computed(() => Object.keys(collegeMajorMap.value))

async function loadCollegeMajorData() {
  try {
    const collegeRes = await getColleges()
    if (collegeRes && collegeRes.data && Array.isArray(collegeRes.data)) {
      const newMap = {}
      for (const college of collegeRes.data) {
        const majorRes = await getMajorsByCollege(college)
        if (majorRes && majorRes.data && Array.isArray(majorRes.data)) {
          newMap[college] = majorRes.data
        }
      }
      if (Object.keys(newMap).length > 0) {
        collegeMajorMap.value = newMap
      }
    }
  } catch (error) {
    console.error('加载学院专业数据失败:', error)
  }
}
const profileMajorOptions = computed(() => collegeMajorMap.value[editForm.college] || [])
const editRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  position: [{ required: true, message: '请输入职位', trigger: 'blur' }],
  title: [{ required: true, message: '请输入职称', trigger: 'blur' }],
  workUnit: [{ required: true, message: '请输入工作单位', trigger: 'blur' }],
  college: [{ required: true, message: '请选择学院', trigger: 'change' }],
  major: [{ required: true, message: '请选择专业', trigger: 'change' }],
  className: [{ required: true, message: '请输入班级', trigger: 'blur' }],
  phonenumber: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ]
}

function normalizeInvitation(it) {
  return {
    id: it.id || `${it.teamId}-${it.studentNo || it.userId || ''}-${it.type || ''}`,
    teamId: it.teamId,
    teamName: it.teamName || it.name || '未知队伍',
    competitionId: it.competitionId || it.cid || '',
    competitionName: it.competitionName || '',
    type: it.type || (it.apply ? 'apply' : 'invite'),
    memberName: it.memberName || it.inviterName || it.applicantName || '未知成员',
    memberStudentNo: it.memberStudentNo || it.studentNo || '',
    applicantName: it.applicantName || '',
    inviterName: it.inviterName || '',
    status: it.status || 'pending',
    read: it.read || false,
    createdAt: it.createdAt || it.createTime || ''
  }
}

function loadInvitations() {
  getTeamInvitations().then(res => {
    const list = Array.isArray(res.data) ? res.data : []
    invitations.value = list
      .map(normalizeInvitation)
      .sort((a, b) => {
        const order = { pending: 0, approved: 1, rejected: 1 }
        const oa = order[a.status] ?? 2
        const ob = order[b.status] ?? 2
        if (oa !== ob) return oa - ob
        return String(b.createdAt || '').localeCompare(String(a.createdAt || ''))
      })
  }).catch(() => {
    invitations.value = []
  })
}

async function approveInvitation(teamId, studentNo) {
  const uid = (state.user && state.user.userId) || (userStore && userStore.userId)
  try {
    await approveTeamInvitation(teamId, studentNo, uid)
    ElMessage.success('已同意')
    loadInvitations()
  } catch (e) {
    const msg = (e && e.msg) || (e && e.message) || (e && e.response && e.response.data && e.response.data.msg) || '操作失败'
    ElMessage.error(msg)
  }
}

async function rejectInvitation(teamId, studentNo) {
  const uid = (state.user && state.user.userId) || (userStore && userStore.userId)
  try {
    await rejectTeamInvitation(teamId, studentNo, uid)
    ElMessage.success('已拒绝')
    loadInvitations()
  } catch (e) {
    const msg = (e && e.msg) || (e && e.message) || (e && e.response && e.response.data && e.response.data.msg) || '操作失败'
    ElMessage.error(msg)
  }
}

function openEdit() {
  editForm.userName = state.user.userName || ''
  
  // 如果是教师，优先从 tb_teacher_team 表获取信息
  if (isTeacher.value) {
    const t = teacherInfo.value || {}
    editForm.workNo = t.userName || state.user.userName || '' // 工号与用户名一致
    editForm.realName = t.teacherName || state.user.nickName || ''
    editForm.position = t.position || state.user.position || state.postGroup || ''
    editForm.title = t.title || state.user.title || state.user.remark || ''
    editForm.workUnit = t.workUnit || state.user.workUnit || (state.user.dept && state.user.dept.deptName) || state.user.collegeName || ''
    editForm.politicalStatus = t.politicalStatus || state.user.politicalStatus || ''
    editForm.birthDate = t.birthDate || state.user.birthDate || ''
    editForm.sex = t.sex || String(state.user.sex ?? '0')
    editForm.phonenumber = t.phone || state.user.phonenumber || ''
    editForm.email = t.email || state.user.email || ''
  } else {
    editForm.realName = state.user.studentName || ''
    editForm.studentNo = state.user.studentNo || state.user.studentId || state.user.sno || state.user.userName || ''
    editForm.college = state.user.collegeName || (state.user.dept && state.user.dept.deptName) || ''
    editForm.major = state.user.majorName || state.user.major || ''
    editForm.className = state.user.className || state.user.classNo || state.user.clazz || ''
    editForm.sex = String(state.user.sex ?? '0')
    editForm.phonenumber = state.user.phonenumber || ''
    editForm.email = state.user.email || ''
  }
  
  editDialogVisible.value = true
}

function onProfileCollegeChange() {
  editForm.major = ''
}

async function submitEdit() {
  if (!editRef?.value || (!isTeacher.value && !academicRef?.value)) {
    ElMessage.error('表单未渲染完成，请稍后重试')
    return
  }
  const v1 = await editRef.value.validate()
  const v2 = isTeacher.value ? true : await academicRef.value.validate()
  if (!v1 || !v2) {
    ElMessage.error('请完善必填信息')
    return
  }
  const payload = isTeacher.value
    ? {
        nickName: editForm.realName,
        position: editForm.position,
        title: editForm.title,
        workUnit: editForm.workUnit,
        phonenumber: editForm.phonenumber,
        email: editForm.email,
        sex: editForm.sex,
        politicalStatus: editForm.politicalStatus,
        birthDate: editForm.birthDate
      }
    : {
        phonenumber: editForm.phonenumber,
        email: editForm.email,
        sex: editForm.sex,
        studentName: editForm.realName,
        studentNo: editForm.studentNo,
        collegeName: editForm.college,
        majorName: editForm.major,
        className: editForm.className
      }
  saving.value = true
  try {
    await updateUserProfile(payload)
    
    // 如果是教师，同步更新 tb_teacher_team 表
    if (isTeacher.value) {
      const teacherTeamPayload = {
        phone: editForm.phonenumber,
        userName: editForm.userName, // 用户名（工号）
        teacherName: editForm.realName,
        sex: editForm.sex,
        email: editForm.email,
        workUnit: editForm.workUnit,
        title: editForm.title,
        position: editForm.position,
        politicalStatus: editForm.politicalStatus,
        birthDate: editForm.birthDate,
        id: teacherInfo.value.id
      }
      try {
        await updateTeacherInfo(teacherTeamPayload)
      } catch (teacherErr) {
        console.error('同步更新教师团队信息失败:', teacherErr)
      }
    }
    
    state.user.phonenumber = payload.phonenumber
    state.user.email = payload.email
    state.user.sex = payload.sex
    if (isTeacher.value) {
      state.user.nickName = editForm.realName
      state.user.position = editForm.position
      state.user.title = editForm.title
      state.user.workUnit = editForm.workUnit
      state.user.politicalStatus = editForm.politicalStatus
      state.user.birthDate = editForm.birthDate
    } else {
      state.user.studentName = editForm.realName
      state.user.studentNo = editForm.studentNo
      state.user.collegeName = editForm.college
      state.user.majorName = editForm.major
      state.user.className = editForm.className
    }
    await getUser()
    userStore.nickName = state.user.nickName || state.user.studentName || userStore.nickName
    userStore.name = state.user.userName || userStore.name
    ElMessage.success('修改成功')
    editDialogVisible.value = false
  } catch (err) {
    const msg = (err && err.msg) || (err && err.message) || (err && err.response && err.response.data && err.response.data.msg) || '保存失败'
    ElMessage.error(msg)
  } finally {
    saving.value = false
  }
}

const pwdDialogVisible = ref(false)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const equalToPassword = (rule, value, callback) => {
  if (pwdForm.newPassword !== value) callback(new Error('两次输入的密码不一致'))
  else callback()
}
const pwdRules = {
  oldPassword: [{ required: true, message: '旧密码不能为空', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '新密码不能为空', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' },
    { pattern: /^[^<>"'|\\\\]+$/, message: '不能包含非法字符：< > \" \' \\\\ |', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '确认密码不能为空', trigger: 'blur' },
    { validator: equalToPassword, trigger: 'blur' }
  ]
}

function openPwd() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdDialogVisible.value = true
}

function submitPwd() {
  if (!pwdRef?.value) return
  pwdRef.value.validate(valid => {
    if (!valid) return
    updateUserPwd(pwdForm.oldPassword, pwdForm.newPassword).then(() => {
      ElMessage.success('修改成功，请重新登录')
      pwdDialogVisible.value = false
      userStore.logOut().then(() => {
        router.push('/login')
      })
    }).catch((e) => {
      const msg = (e && e.msg) || (e && e.message) || (e && e.response && e.response.data && e.response.data.msg) || '修改失败'
      ElMessage.error(msg)
    })
  })
}

async function copyText(value, label = '') {
  if (!value || value === '未填写') return
  try {
    await navigator.clipboard.writeText(String(value))
    ElMessage.success(`${label}${value} 已复制`)
  } catch (e) {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped>
.student-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f9fafb;
}
.header-nav {
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}
.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  height: 64px;
  gap: 32px;
}
.logo-section {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
  font-size: 20px;
  font-weight: 600;
}
.logo-text { font-size: 18px; }
.main-nav {
  display: flex;
  align-items: center;
  gap: 24px;
  flex: 1;
}
.nav-item {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 0;
  position: relative;
  cursor: pointer;
  transition: all 0.3s;
}
.nav-item:hover,
.nav-item.active { color: white; }
.nav-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}
.avatar-container {
  display: flex;
  align-items: center;
}
.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
}
.user-avatar-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid #ffffff;
  box-shadow: 0 0 0 2px rgba(255,255,255,0.3);
}
.dropdown { position: relative; }
.dropdown::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  height: 20px;
  background: transparent;
}
.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  margin-top: 8px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 12px;
  min-width: 200px;
  display: none;
  z-index: 1000;
}
.dropdown:hover .dropdown-menu { display: block; }
.dropdown-header {
  color: #6366f1;
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 8px;
  padding: 4px 8px;
}
.dropdown-menu a {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  color: #4b5563;
  text-decoration: none;
  border-radius: 4px;
  font-size: 13px;
  transition: all 0.2s;
}
.dropdown-menu a:hover,
.dropdown-menu a.active {
  background: #f3f4f6;
  color: #6366f1;
}
 

.main-content {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}
.profile-header {
  display: flex;
  gap: 24px;
  background: #ffffff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 10px 28px rgba(99, 102, 241, 0.10);
}
.summary-cards {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
.summary-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 6px 18px rgba(99, 102, 241, 0.10);
}
.summary-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.summary-icon.last { background: linear-gradient(135deg, #7c3aed, #4f46e5); }
.summary-icon.secure { background: linear-gradient(135deg, #10b981, #22c55e); }
.summary-icon.list { background: linear-gradient(135deg, #06b6d4, #3b82f6); }
.summary-info { display: flex; flex-direction: column; gap: 4px; }
.summary-label { color: #6b7280; font-size: 13px; }
.summary-value { color: #111827; font-size: 16px; font-weight: 600; }
.avatar-wrap {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  overflow: hidden;
  border: 4px solid #8b5cf6;
  box-shadow: 0 0 0 6px #eef2ff, 0 12px 28px rgba(99, 102, 241, 0.20);
  display: flex;
  align-items: center;
  justify-content: center;
}
.profile-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.name {
  font-size: 24px;
  color: #111827;
  letter-spacing: 0.3px;
  font-weight: 700;
}
.tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.tag {
  background: #eef2ff;
  color: #4f46e5;
  border: 1px solid #e0e7ff;
  padding: 8px 12px;
  border-radius: 16px;
  font-size: 13px;
  display: inline-flex;
  gap: 6px;
  align-items: center;
  font-weight: 600;
}
.meta {
  color: #6b7280;
  display: flex;
  gap: 16px;
  font-size: 13px;
}
.actions {
  margin-top: 8px;
  display: flex;
  gap: 10px;
}
.btn {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  color: #374151;
  border-radius: 8px;
  padding: 10px 16px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn:hover { background: #f9fafb; }
.btn.primary {
  background: #7c3aed;
  color: white;
  border: none;
  box-shadow: 0 8px 16px rgba(124, 58, 237, 0.25);
}
.btn.primary:hover { background: #6d28d9; }
.profile-completeness {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.complete-label {
  color: #4f46e5;
  font-weight: 600;
  font-size: 13px;
}
.complete-bar {
  flex: 1;
  height: 10px;
  background: #f3f4f6;
  border-radius: 999px;
  box-shadow: inset 0 0 0 1px #e5e7eb;
}
.complete-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
}
.complete-percent {
  color: #111827;
  font-weight: 700;
  font-size: 13px;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 20px;
  align-items: start;
  margin-top: 28px;
}
.side-nav {
  background: #f3f4f6;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 6px 18px rgba(99, 102, 241, 0.10);
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.side-nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  background: #ffffff;
}
.side-nav-item:hover { background: #eef2ff; }
.side-nav-item.active {
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  color: #ffffff;
  box-shadow: 0 6px 18px rgba(99, 102, 241, 0.18);
}
.side-nav-item.active i { color: #ffffff; }
.side-arrow { margin-left: auto; }
.info-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.08);
  border: 1px solid #e5e7eb;
  font-family: inherit;
}
.right-content {
  background: transparent;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.info-card h3 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  color: #4338ca;
  font-weight: 700;
  margin-bottom: 12px;
  letter-spacing: 0.2px;
}
.info-card h3 i {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: #eef2ff;
  border: 1px solid #e0e7ff;
  color: #4f46e5;
}
.info-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}
.info-list li {
  display: flex;
  justify-content: space-between;
  color: #374151;
  font-size: 14px;
  padding: 8px 0;
  border-top: 1px dashed #eef2ff;
  line-height: 1.6;
}
.info-list .label { color: #6b7280; letter-spacing: 0.3px; }
.info-list .value {
  color: #111827;
  font-weight: 400;
  letter-spacing: 0.1px;
}
.role-group, .post-group {
  background: #f5f3ff;
  color: #6d28d9;
  border: 1px solid #ede9fe;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 14px;
}
.theme-role { border-color: #c7d2fe; box-shadow: 0 8px 24px rgba(79, 70, 229, 0.08); }
.theme-role h3 i { background: #eef2ff; border-color: #dbeafe; color: #4f46e5; }
.theme-account { border-color: #c7d2fe; box-shadow: 0 8px 24px rgba(79, 70, 229, 0.08); }
.theme-account h3 i { background: #eef2ff; border-color: #dbeafe; color: #4f46e5; }
.theme-academic { border-color: #c7d2fe; box-shadow: 0 8px 24px rgba(79, 70, 229, 0.08); }
.theme-academic h3 i { background: #eef2ff; border-color: #dbeafe; color: #4f46e5; }
.theme-registered { border-color: #c7d2fe; box-shadow: 0 8px 24px rgba(79, 70, 229, 0.08); }
.theme-registered h3 i { background: #eef2ff; border-color: #dbeafe; color: #4f46e5; }
.card-block { display: none; }
.dialog-columns {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.section-card {
  background: #ffffff;
  border-radius: 10px;
  box-shadow: 0 6px 18px rgba(99, 102, 241, 0.08);
  padding: 14px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #4f46e5;
  margin-bottom: 10px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
:deep(.center-dialog) {
  margin: 0 !important;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}

.badge {
  margin-left: auto;
  background: #ef4444;
  color: #ffffff;
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 12px;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
}
.red-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ef4444;
  position: absolute;
  right: 56px;
  top: 50%;
  transform: translateY(-50%);
  box-shadow: 0 0 0 2px #ffffff;
}
.count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #ef4444;
  color: #ffffff;
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 12px;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
}
.invitation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.invitation-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: 12px;
  background: #f3f4f6;
}
.invitation-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.team-name {
  color: #111827;
  font-size: 15px;
  font-weight: 600;
}
.invitation-meta {
  display: flex;
  gap: 10px;
  align-items: center;
  color: #6b7280;
  font-size: 13px;
}
.meta-pill {
  background: #eef2ff;
  color: #4f46e5;
  border: 1px solid #e0e7ff;
  border-radius: 999px;
  padding: 4px 8px;
  font-weight: 600;
}
.invitation-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
.invitation-empty {
  color: #9ca3af;
  font-size: 13px;
}


@media (max-width: 992px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }
  .profile-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
}
</style>
