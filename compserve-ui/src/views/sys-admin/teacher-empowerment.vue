<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">教师赋权管理</h2>
        <p class="text-gray-500 mt-1">为竞赛分配评审教师，确保赛事评审工作的专业性与公正性</p>
      </div>
    </div>

    <!-- 赋权操作卡片 -->
    <section class="admin-card fade-in">
      <div class="admin-card-header flex items-center gap-2">
        <div class="p-1.5 bg-admin-100 rounded-lg text-admin-700">
          <i class="fa fa-user-plus"></i>
        </div>
        <h3 class="text-xl font-semibold text-gray-800">分配评审权限</h3>
      </div>
      <div class="admin-card-body">
        <el-form :model="grantForm" :rules="grantRules" ref="grantFormRef" label-position="top" class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <el-form-item label="选择竞赛" prop="competitionId">
            <el-select
              v-model="grantForm.competitionId"
              placeholder="请选择需要分配评审的竞赛"
              filterable
              class="w-full admin-el-select"
              @change="handleCompetitionChange"
            >
              <el-option
                v-for="comp in competitionList"
                :key="comp.competitionId"
                :label="comp.competitionName"
                :value="comp.competitionId"
              >
                <div class="flex flex-col py-1">
                  <span class="font-bold text-gray-800">{{ comp.competitionName }}</span>
                  <span class="text-xs text-gray-400">{{ comp.competitionType }}</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="选择教师" prop="teacherIds">
            <el-select
              v-model="grantForm.teacherIds"
              placeholder="请选择评审教师（可多选）"
              multiple
              filterable
              collapse-tags
              collapse-tags-tooltip
              class="w-full admin-el-select"
            >
              <el-option
                v-for="teacher in availableTeacherList"
                :key="teacher.userId"
                :label="teacher.nickName || teacher.userName"
                :value="teacher.userId"
              >
                <div class="flex justify-between items-center w-full">
                  <span>{{ teacher.nickName || teacher.userName }} ({{ teacher.userName }})</span>
                  <span v-if="isTeacherDisabled(teacher.userId)" class="text-xs text-amber-600 bg-amber-50 px-2 py-0.5 rounded">指导老师，可赋权</span>
                </div>
              </el-option>
            </el-select>
            <div v-if="grantForm.competitionId" class="mt-2 text-xs text-gray-500">
              可添加 {{ availableTeacherList.length }} 位老师
              <span v-if="currentCompetitionPermissionList.length">
                ，当前已赋权 {{ currentCompetitionPermissionList.length }} 位
              </span>
            </div>
          </el-form-item>

          <div class="md:col-span-2 flex justify-end gap-3 pt-2">
            <button type="button" class="btn-admin-outline px-6 py-2" @click="resetGrantForm">重置表单</button>
            <button type="button" class="btn-admin-primary px-8 py-2 flex items-center gap-2" :disabled="granting" @click="handleGrant">
              <i class="fa fa-check-circle"></i>
              确认赋权
            </button>
          </div>
        </el-form>

        <div v-if="grantForm.competitionId" class="mt-6 rounded-2xl border border-admin-100 bg-admin-50/40 p-5">
          <div class="flex items-center justify-between gap-3 mb-4">
            <div>
              <div class="text-sm font-semibold text-gray-900">当前赛事已赋权老师</div>
              <div class="text-xs text-gray-500 mt-1">
                {{ currentCompetitionName || '已选赛事' }}：
                <span class="text-admin-700 font-medium">{{ currentCompetitionPermissionList.length }}</span>
                位
              </div>
            </div>
            <div v-if="currentCompetitionPermissionList.length" class="text-xs text-gray-400">
              选中赛事后自动展示
            </div>
          </div>

          <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-3">
            <div v-for="i in 3" :key="`selected-loading-${i}`" class="h-16 rounded-xl bg-white/80 animate-pulse border border-gray-100"></div>
          </div>

          <div v-else-if="currentCompetitionPermissionList.length === 0" class="rounded-xl bg-white/80 border border-dashed border-gray-200 px-4 py-6 text-center text-sm text-gray-500">
            当前赛事还没有赋权老师
          </div>

          <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-3">
            <div
              v-for="teacher in currentCompetitionPermissionList"
              :key="`selected-${teacher.competitionId}-${teacher.userId}`"
              class="rounded-xl bg-white border border-gray-100 px-4 py-3 shadow-sm"
            >
              <div class="flex items-start justify-between gap-3">
                <div class="flex items-center gap-3 min-w-0">
                  <div class="w-9 h-9 rounded-full bg-admin-100 text-admin-700 flex items-center justify-center font-bold text-sm shrink-0">
                    {{ (teacher.teacherName || teacher.teacherUserName || '教').charAt(0) }}
                  </div>
                  <div class="min-w-0">
                    <div class="flex items-center gap-2">
                      <div class="text-sm font-semibold text-gray-900 truncate">{{ teacher.teacherName }}</div>
                      <span v-if="teacher.advisorConflict" class="inline-flex items-center rounded-full bg-red-50 px-2 py-0.5 text-[11px] font-medium text-red-600">
                        指导老师冲突
                      </span>
                    </div>
                    <div class="text-xs text-gray-500 truncate">{{ teacher.teacherUserName }}</div>
                    <div class="text-xs text-gray-400 mt-1">{{ teacher.teacherPhone || '未填写手机号' }}</div>
                    <div v-if="teacher.advisorConflictMessage" class="text-[11px] text-red-500 mt-1">
                      {{ teacher.advisorConflictMessage }}
                    </div>
                  </div>
                </div>
                <button
                  type="button"
                  class="teacher-remove-btn"
                  title="移除赋权"
                  @click="handleRevoke(teacher)"
                >
                  <i class="fa fa-times"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 已赋权列表 -->
    <section class="admin-card fade-in">
      <div class="admin-card-header flex flex-col lg:flex-row lg:items-center justify-between gap-4">
        <h3 class="text-xl font-semibold flex items-center gap-2 text-gray-800">
          <div class="p-1.5 bg-admin-100 rounded-lg text-admin-700">
            <i class="fa fa-list-ul"></i>
          </div>
          赋权记录列表
        </h3>
        <div class="flex flex-col lg:flex-row items-center gap-3">
          <el-select
            v-model="queryParams.competitionId"
            placeholder="按竞赛筛选"
            clearable
            filterable
            class="w-full lg:w-64 admin-el-select"
            @change="handleQuery"
          >
            <el-option
              v-for="comp in competitionList"
              :key="comp.competitionId"
              :label="comp.competitionName"
              :value="comp.competitionId"
            />
          </el-select>
          <input
            v-model="queryParams.teacherName"
            type="text"
            placeholder="搜索教师姓名..."
            class="admin-input w-full lg:w-48"
            @keyup.enter="handleQuery"
          >
          <button class="btn-admin-outline px-4 py-2" @click="handleQuery">搜索</button>
        </div>
      </div>

      <div class="admin-card-body p-0">
        <div v-if="loading" class="p-8">
          <div class="space-y-4 animate-pulse">
            <div v-for="i in 5" :key="i" class="h-12 bg-gray-100 rounded-lg w-full"></div>
          </div>
        </div>

        <div v-else-if="permissionList.length === 0" class="p-12 text-center">
          <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300">
            <i class="fa fa-shield text-3xl"></i>
          </div>
          <p class="text-gray-500">暂无赋权记录，请在上方分配权限</p>
        </div>

        <div v-else class="admin-table-container">
          <table class="admin-table">
            <thead>
              <tr>
                <th scope="col">竞赛信息</th>
                <th scope="col">评审教师</th>
                <th scope="col">联系方式</th>
                <th scope="col">赋权时间</th>
                <th scope="col" class="text-right">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in permissionList" :key="`${row.competitionId}-${row.userId}`" class="table-row-hover">
                <td>
                  <div class="text-sm font-bold text-gray-900">{{ row.competitionName }}</div>
                  <div class="text-xs text-gray-400">{{ row.competitionType }}</div>
                </td>
                <td>
                  <div class="flex items-center gap-2">
                    <div class="w-8 h-8 rounded-full bg-admin-100 text-admin-600 flex items-center justify-center font-bold text-xs">
                      {{ (row.teacherName || row.teacherUserName || '教').charAt(0) }}
                    </div>
                    <div>
                      <div class="flex items-center gap-2">
                        <div class="text-sm font-medium text-gray-900">{{ row.teacherName }}</div>
                        <span v-if="row.advisorConflict" class="inline-flex items-center rounded-full bg-red-50 px-2 py-0.5 text-[11px] font-medium text-red-600">
                          赋权已失效
                        </span>
                      </div>
                      <div class="text-xs text-gray-500">{{ row.teacherUserName }}</div>
                      <div v-if="row.advisorConflictMessage" class="text-[11px] text-red-500 mt-1">
                        {{ row.advisorConflictMessage }}
                      </div>
                    </div>
                  </div>
                </td>
                <td>
                  <div class="text-xs text-gray-500">
                    <div><i class="fa fa-phone mr-1"></i> {{ row.teacherPhone || '未填写' }}</div>
                    <div><i class="fa fa-envelope-o mr-1"></i> {{ row.teacherEmail || '未填写' }}</div>
                  </div>
                </td>
                <td>
                  <span class="text-sm text-gray-500">{{ parseTime(row.createTime, '{y}-{m}-{d}') }}</span>
                </td>
                <td class="text-right">
                  <button class="table-icon-btn table-icon-btn-ban" title="撤销权限" @click="handleRevoke(row)">
                    <i class="fa fa-user-times"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCompetitions, listTeachers, grantPermission, revokePermission, listPermissions } from '@/api/admin/permission'
import { parseTime } from '@/utils/ruoyi'

const loading = ref(false)
const granting = ref(false)
const competitionList = ref([])
const teacherList = ref([])
const permissionList = ref([])
const disabledTeacherIds = ref([])

const grantForm = reactive({
  competitionId: null,
  teacherIds: []
})

const grantRules = {
  competitionId: [{ required: true, message: '请选择竞赛', trigger: 'change' }],
  teacherIds: [{ required: true, message: '请选择至少一位教师', trigger: 'change' }]
}

const grantFormRef = ref(null)

const queryParams = reactive({
  competitionId: null,
  teacherName: ''
})

const currentCompetitionPermissionList = computed(() => {
  if (!grantForm.competitionId) return []
  return permissionList.value.filter(item => item.competitionId === grantForm.competitionId)
})

const currentCompetitionName = computed(() => {
  const matched = competitionList.value.find(item => item.competitionId === grantForm.competitionId)
  return matched?.competitionName || ''
})

const currentCompetitionPermissionUserIds = computed(() =>
  currentCompetitionPermissionList.value
    .map(item => Number(item.userId))
    .filter(item => !Number.isNaN(item))
)

const availableTeacherList = computed(() => {
  if (!grantForm.competitionId) return teacherList.value
  const grantedUserIds = new Set(currentCompetitionPermissionUserIds.value)
  const selectedUserIds = new Set((grantForm.teacherIds || []).map(item => Number(item)).filter(item => !Number.isNaN(item)))
  return teacherList.value.filter(item => {
    const userId = Number(item.userId)
    if (Number.isNaN(userId)) return false
    return !grantedUserIds.has(userId) && !selectedUserIds.has(userId)
  })
})

function loadData() {
  listCompetitions().then(res => competitionList.value = res.data || [])
  listTeachers().then(res => teacherList.value = res.data || [])
  getList()
}

function getList() {
  loading.value = true
  listPermissions({ competitionId: queryParams.competitionId || undefined }).then(res => {
    let list = res.data || []
    if (queryParams.teacherName) {
      const kw = queryParams.teacherName.toLowerCase()
      list = list.filter(i => (i.teacherName || '').toLowerCase().includes(kw) || (i.teacherUserName || '').toLowerCase().includes(kw))
    }
    permissionList.value = list
  }).finally(() => loading.value = false)
}

function handleCompetitionChange(val) {
  grantForm.teacherIds = []
  disabledTeacherIds.value = []
  queryParams.competitionId = val || null
  getList()
}

const isTeacherDisabled = (id) => disabledTeacherIds.value.includes(id)

function handleGrant() {
  grantFormRef.value.validate(valid => {
    if (!valid) return
    ElMessageBox.confirm('确认要为选定教师分配评审权限吗？', '提示').then(() => {
      granting.value = true
      grantPermission(grantForm).then(res => {
        ElMessage.success(`成功为 ${res.data} 位教师赋权`)
        resetGrantForm()
        getList()
      }).finally(() => granting.value = false)
    })
  })
}

function handleRevoke(row) {
  ElMessageBox.confirm(`确认撤销教师 "${row.teacherName}" 的评审权限？`, '警告', { type: 'warning' }).then(() => {
    revokePermission(row.competitionId, row.userId).then(() => {
      ElMessage.success('撤销成功')
      getList()
    })
  })
}

function resetGrantForm() {
  grantForm.competitionId = null
  grantForm.teacherIds = []
  disabledTeacherIds.value = []
  queryParams.competitionId = null
  getList()
}

function handleQuery() { getList() }

onMounted(loadData)
</script>

<style scoped>
.admin-card { @apply bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden; }
.admin-card-header { @apply px-6 py-5 border-b border-gray-100; }
.admin-card-body { @apply p-6; }
.btn-admin-primary { @apply bg-admin-600 hover:bg-admin-700 text-white font-medium rounded-lg transition-colors shadow-sm disabled:opacity-50; }
.btn-admin-outline { @apply border border-gray-200 text-gray-700 hover:bg-gray-50 font-medium rounded-lg transition-colors; }
.admin-input { @apply h-10 border border-gray-200 rounded-lg px-3 text-sm focus:outline-none focus:border-admin-500 focus:ring-2 focus:ring-admin-100; }
.admin-table { @apply min-w-full; }
.admin-table thead tr { @apply bg-gray-50; }
.admin-table th { @apply px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider; }
.admin-table td { @apply px-6 py-4 border-t border-gray-100; }
.table-row-hover { @apply transition-colors hover:bg-gray-50/50; }
.table-icon-btn { @apply w-8 h-8 flex items-center justify-center rounded-lg transition-colors; }
.table-icon-btn-ban { @apply text-red-600 hover:bg-red-50; }
.teacher-remove-btn { @apply w-8 h-8 rounded-lg border border-red-100 text-red-500 hover:bg-red-50 hover:text-red-600 transition-colors shrink-0 flex items-center justify-center; }
.fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

:deep(.admin-el-select .el-input__wrapper) {
  @apply rounded-lg border-gray-200 shadow-none ring-0 !important;
  height: 40px;
}
</style>
