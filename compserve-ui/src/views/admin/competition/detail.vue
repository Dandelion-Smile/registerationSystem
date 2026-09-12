<template>
  <div class="min-h-screen bg-gray-50 font-sans text-left">
    <nav class="fixed top-0 left-0 right-0 h-16 bg-[#722ed1] z-[100] flex items-center justify-between px-8 shadow-lg">
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 bg-yellow-400 rounded-lg flex items-center justify-center shadow-inner">
          <i class="fa fa-trophy text-white text-sm"></i>
        </div>
        <div class="flex flex-col">
          <span class="text-white font-black tracking-widest text-lg leading-none mb-1">智启赛途</span>
          <span class="text-white/60 text-[10px] font-bold uppercase tracking-tighter">高校竞赛AI智能平台</span>
        </div>
      </div>

      <div class="flex items-center gap-4">
        <button
          @click="openReviewerPool"
          :disabled="assignmentBusy"
          class="flex items-center gap-2 px-4 py-2 bg-white/10 hover:bg-white/20 text-white rounded-xl border border-white/20 transition-all text-sm font-bold"
          :class="{ 'opacity-50 cursor-not-allowed pointer-events-none': assignmentBusy }"
        >
          <i class="fa fa-users text-yellow-400"></i>
          评审老师池
        </button>

        <button
          @click="handleExport"
          :disabled="assignmentBusy"
          class="flex items-center gap-2 px-5 py-2 bg-white/10 hover:bg-white/20 text-white rounded-xl border border-white/20 transition-all active:scale-95 text-xs font-bold"
          :class="{ 'opacity-50 cursor-not-allowed pointer-events-none': assignmentBusy }"
        >
          <i class="fa fa-cloud-download text-yellow-400"></i>
          导出参赛名单 (Excel)
        </button>

        <el-dropdown trigger="click" placement="bottom-end" @command="handleUserCommand">
          <button class="user-menu-trigger">
            <div class="text-right hidden md:block">
              <p class="text-white text-xs font-black leading-none">系统管理员</p>
              <p class="text-white/50 text-[10px] font-mono">{{ userStore.name || 'admin' }}</p>
            </div>
            <div class="w-8 h-8 bg-white rounded-full flex items-center justify-center border-2 border-white/20">
              <i class="fa fa-user-circle text-[#722ed1]"></i>
            </div>
            <i class="fa fa-chevron-down text-white/80 text-xs"></i>
          </button>
          <template #dropdown>
            <div class="user-dropdown-panel">
              <div class="px-5 py-4 border-b border-gray-100">
                <div class="text-base font-black text-gray-900">系统管理员</div>
                <div class="text-sm text-gray-500 mt-2">{{ userStore.name || 'admin' }}@example.com</div>
              </div>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <div class="user-dropdown-item">
                    <span class="user-dropdown-icon"><i class="fa fa-user-circle-o"></i></span>
                    <span>个人中心</span>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item command="logout">
                  <div class="user-dropdown-item">
                    <span class="user-dropdown-icon"><i class="fa fa-sign-out"></i></span>
                    <span>退出登录</span>
                  </div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </div>
          </template>
        </el-dropdown>
      </div>
    </nav>

    <div class="p-6 pt-24">
      <div class="mb-6">
        <button
          @click="router.back()"
          class="flex items-center gap-2 px-4 py-2 bg-white hover:bg-gray-50 text-[#722ed1] rounded-full shadow-sm border border-gray-100 transition-all"
        >
          <i class="fa fa-arrow-left"></i>
          <span class="text-sm font-semibold">返回列表</span>
        </button>
      </div>

      <div class="grid grid-cols-12 gap-8">
        <div class="col-span-12 space-y-8">
          <div class="relative rounded-[3rem] shadow-xl overflow-hidden h-[400px] mb-8">
            <div class="absolute inset-0">
              <img :src="bannerSrc" class="w-full h-full object-cover" />
              <div class="absolute inset-0 bg-gradient-to-r from-black/70 via-black/50 to-black/30"></div>
            </div>

            <div class="absolute inset-0 flex items-center">
              <div class="container mx-auto px-8">
                <div class="max-w-3xl">
                  <span class="text-white/80 font-black text-xs uppercase tracking-widest mb-3 block">
                    NATIONAL COMPETITION PLATFORM
                  </span>
                  <h1 class="text-4xl md:text-5xl font-black text-white mb-4 tracking-tight drop-shadow-lg">
                    {{ info.competitionName }}
                  </h1>
                  <div class="flex flex-wrap gap-4">
                    <span class="flex items-center gap-2 bg-white/20 backdrop-blur-sm px-4 py-2 rounded-lg text-white text-sm">
                      <i class="fa fa-calendar-check-o"></i>
                      {{ formatDate(info.registerStartTime) }} — {{ formatDate(info.registerEndTime) }}
                    </span>
                    <span class="flex items-center gap-2 bg-[#722ed1] px-4 py-2 rounded-lg text-white text-sm">
                      <i class="fa fa-flash"></i> 正在报名中
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="flex items-center justify-between gap-4 flex-wrap">
            <h3 class="text-2xl font-black text-gray-900 flex items-center gap-3">
              <div class="w-2 h-8 bg-[#722ed1] rounded-full"></div>
              已报名队伍展示
            </h3>

            <div class="flex items-center gap-4 flex-wrap">
              <div class="flex items-center gap-2">
                <input
                  v-model="filterRange.min"
                  type="number"
                  class="w-24 px-3 py-1.5 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#722ed1] text-sm"
                  placeholder="最低分"
                >
                <input
                  v-model="filterRange.max"
                  type="number"
                  class="w-24 px-3 py-1.5 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#722ed1] text-sm"
                  placeholder="最高分"
                >
                <button
                  @click="applyRangeFilter"
                  class="px-3 py-1.5 bg-[#722ed1] text-white rounded-xl font-bold hover:bg-[#5c1ed1] transition-all text-sm"
                >
                  查询
                </button>
              </div>

              <div class="text-sm font-bold text-[#722ed1] bg-purple-50 px-4 py-2 rounded-2xl ring-1 ring-purple-100">
                {{ sortedTeamList.length }} 支队伍
              </div>
            </div>
          </div>

          <div class="bg-white rounded-[2.5rem] border border-gray-100 shadow-sm overflow-hidden text-left">
            <div class="team-table-header">
              <div>序号</div>
              <div>队伍名称</div>
              <div>作品名称</div>
              <div class="text-center">负责人</div>
              <div class="text-center">指导老师</div>
              <div>评审老师</div>
              <div class="text-center">
                <div class="flex items-center justify-center gap-2">
                  <span>平均分</span>
                  <button @click="toggleSort" class="cursor-pointer text-gray-300 hover:text-[#722ed1] transition-colors p-0 border-none bg-transparent">
                    <i class="fa fa-filter" :class="{ 'text-[#722ed1]': currentSort !== '', 'rotate-180': currentSort === 'asc' }"></i>
                  </button>
                </div>
              </div>
              <div class="text-center">详情</div>
            </div>

            <div
              v-for="team in sortedTeamList"
              :key="team.register_id"
              class="team-table-row"
            >
              <div class="text-sm font-bold text-gray-500">{{ team.sortNo }}</div>
              <div class="font-bold text-gray-800">{{ team.team_name }}</div>
              <div class="text-gray-500 text-sm truncate">{{ team.work_name || '无' }}</div>
              <div class="flex justify-center text-center">
                <div class="flex items-center gap-2">
                  <div class="w-6 h-6 bg-purple-100 rounded-full flex items-center justify-center text-[10px] text-[#722ed1] font-black">
                    {{ getLeader(team.team_members).charAt(0) }}
                  </div>
                  <span class="text-sm text-gray-600">{{ getLeader(team.team_members) }}</span>
                </div>
              </div>
              <div class="text-center text-gray-600 text-sm">{{ team.advisorNames || team.teacher_name || '未指定' }}</div>
              <div class="space-y-2">
                <div v-if="team.assignedReviewers?.length" class="flex flex-wrap gap-2">
                  <span
                    v-for="reviewer in team.assignedReviewers"
                    :key="reviewer.reviewerId"
                    class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium"
                    :class="reviewer.reviewed ? 'bg-green-50 text-green-700 ring-1 ring-green-100' : 'bg-purple-50 text-[#722ed1] ring-1 ring-purple-100'"
                  >
                    <i class="fa" :class="reviewer.reviewed ? 'fa-check-circle' : 'fa-user'"></i>
                    {{ reviewer.reviewerName }}
                  </span>
                </div>
                <div v-else class="text-xs text-gray-400">暂未分配</div>
                <button
                  class="text-xs font-semibold text-[#722ed1] hover:underline"
                  @click="openTeamReviewerDialog(team)"
                  :disabled="assignmentBusy"
                >
                  编辑评审老师
                </button>
              </div>
              <div class="text-center">
                <span class="px-3 py-1 bg-white ring-1 ring-purple-100 text-[#722ed1] rounded-full font-mono font-black shadow-sm">
                  {{ team.avg_score ? parseFloat(team.avg_score).toFixed(2) : '0.00' }}
                </span>
              </div>
              <div class="flex justify-center">
                <button @click="handleTeamJump(team)" class="w-10 h-10 rounded-full hover:bg-white hover:shadow-md flex items-center justify-center text-gray-400 hover:text-[#722ed1] transition-all">
                  <i class="fa fa-arrow-right text-lg"></i>
                </button>
              </div>
            </div>

            <div v-if="sortedTeamList.length === 0" class="py-20 text-center text-gray-300 italic">
              暂无参赛队伍数据
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="reviewerPoolVisible" title="评审老师池" width="860px" class="reviewer-pool-dialog" :close-on-click-modal="!assignmentBusy" :show-close="!assignmentBusy" :close-on-press-escape="!assignmentBusy">
      <div class="pool-summary-grid">
        <div class="pool-summary-card">
          <div class="pool-summary-label">池内老师</div>
          <div class="pool-summary-value">{{ reviewerPoolStats.total }}</div>
        </div>
        <div class="pool-summary-card">
          <div class="pool-summary-label">已承担评审</div>
          <div class="pool-summary-value">{{ reviewerPoolStats.active }}</div>
        </div>
        <div class="pool-summary-card">
          <div class="pool-summary-label">人均分配队伍数</div>
          <div class="pool-summary-value">{{ reviewerPoolStats.avgLoad }}</div>
        </div>
      </div>

      <div v-loading="reviewerPoolLoading" class="space-y-3">
        <div v-for="row in reviewerPool" :key="row.userId" class="pool-row">
          <div class="flex items-center gap-3">
            <div class="pool-avatar">{{ (row.teacherName || '教').charAt(0) }}</div>
            <div>
              <div class="text-sm font-semibold text-gray-800">{{ row.teacherName }}</div>
              <div class="text-xs text-gray-400">{{ row.phone || '未填写手机号' }}</div>
            </div>
          </div>
          <div class="pool-load">
            <span class="pool-load-badge">{{ row.assignedCount }} 支队伍</span>
            <span class="text-xs text-gray-400">{{ row.assigned ? '已参与分配' : '待分配' }}</span>
          </div>
          <el-button link type="danger" @click="handleDeleteReviewer(row)" :disabled="assignmentBusy">移出老师池</el-button>
        </div>
      </div>

      <template #footer>
        <div class="flex items-center justify-between w-full">
          <span class="text-sm text-gray-400">这里只展示当前赛事已赋权、可参与评审分配的老师</span>
          <div class="flex gap-2">
            <el-button @click="openAddReviewerDialog" :disabled="assignmentBusy">添加老师</el-button>
            <el-button @click="reviewerPoolVisible = false" :disabled="assignmentBusy">关闭</el-button>
            <el-button type="primary" @click="handleAutoAssign" :loading="autoAssignLoading">开始分配</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="addReviewerVisible"
      title="添加评审老师"
      width="820px"
      class="team-reviewer-dialog"
      :close-on-click-modal="!assignmentBusy && !addReviewerSaving"
      :show-close="!assignmentBusy && !addReviewerSaving"
      :close-on-press-escape="!assignmentBusy && !addReviewerSaving"
      @closed="closeAddReviewerDialog"
    >
      <div class="space-y-4">
        <div class="rounded-2xl bg-gradient-to-r from-[#f7f1ff] to-white border border-[#ede0ff] px-5 py-4">
          <div class="flex items-start justify-between gap-4">
            <div>
              <div class="text-lg font-semibold text-gray-900">把老师加回当前赛事的评审老师池</div>
              <div class="text-sm text-gray-500 mt-1">加入后会重新出现在老师池里，可以参与自动分配，也可以手动分配给队伍。</div>
            </div>
            <div class="text-right">
              <div class="text-xs uppercase tracking-[0.2em] text-gray-400">待选老师</div>
              <div class="text-2xl font-black text-[#722ed1]">{{ reviewerCandidates.length }}</div>
            </div>
          </div>
        </div>

        <div class="rounded-2xl border border-gray-100 p-4 bg-[#fcfbff]">
          <input
            v-model="addReviewerKeyword"
            type="text"
            class="w-full px-4 py-3 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-[#722ed1]"
            placeholder="搜索老师姓名或手机号"
          >
        </div>

        <div class="grid md:grid-cols-2 gap-4">
          <div class="rounded-2xl border border-gray-100 p-4 bg-[#fcfbff]">
            <div class="flex items-center justify-between mb-3">
              <div class="text-sm font-semibold text-gray-800">已选择</div>
              <div class="text-xs text-gray-400">{{ selectedAddReviewerDetails.length }} 位</div>
            </div>
            <div v-if="selectedAddReviewerDetails.length" class="space-y-2 max-h-[420px] overflow-auto pr-1">
              <div
                v-for="reviewer in selectedAddReviewerDetails"
                :key="reviewer.userId"
                class="flex items-center justify-between px-3 py-3 rounded-xl bg-white border border-gray-100 shadow-sm"
              >
                <div class="flex items-center gap-3">
                  <div class="pool-avatar subtle">{{ (reviewer.teacherName || '教').charAt(0) }}</div>
                  <div>
                    <div class="text-sm font-medium text-gray-800">{{ reviewer.teacherName }}</div>
                    <div class="text-xs text-gray-400">{{ reviewer.phone || '未填写手机号' }}</div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="text-sm text-gray-400">还没有选择要加入老师池的老师</div>
          </div>

          <div class="rounded-2xl border border-gray-100 p-4">
            <div class="flex items-center justify-between mb-3">
              <div class="text-sm font-semibold text-gray-800">可添加老师</div>
              <div class="text-xs text-gray-400">指导老师可加入，但不能评审自己指导的队伍</div>
            </div>
            <div v-loading="addReviewerLoading" class="max-h-[420px] overflow-auto pr-1">
              <el-checkbox-group v-model="selectedAddReviewerIds" class="w-full">
                <div v-for="reviewer in filteredReviewerCandidates" :key="reviewer.userId" class="reviewer-option">
                  <el-checkbox :label="reviewer.userId" :disabled="assignmentBusy || addReviewerSaving || reviewer.disabled">
                    <span class="font-medium text-gray-800">{{ reviewer.teacherName }}</span>
                    <span class="text-xs text-gray-400 ml-2">{{ reviewer.phone || '未填写手机号' }}</span>
                  </el-checkbox>
                  <span v-if="reviewer.disabled" class="text-xs text-red-500">{{ reviewer.disabledReason }}</span>
                </div>
              </el-checkbox-group>
              <div v-if="!addReviewerLoading && !filteredReviewerCandidates.length" class="text-sm text-gray-400 py-10 text-center">
                当前没有可加入的老师
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="closeAddReviewerDialog" :disabled="assignmentBusy || addReviewerSaving">取消</el-button>
        <el-button type="primary" @click="submitAddReviewers" :loading="addReviewerSaving">加入老师池</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="teamReviewerVisible" title="调整评审老师" width="860px" class="team-reviewer-dialog" @closed="closeTeamReviewerDialog" :close-on-click-modal="!assignmentBusy" :show-close="!assignmentBusy" :close-on-press-escape="!assignmentBusy">
      <div v-if="editingTeam" class="space-y-4">
        <div class="rounded-2xl bg-gradient-to-r from-[#f7f1ff] to-white border border-[#ede0ff] px-5 py-4">
          <div class="flex items-start justify-between gap-4">
            <div>
              <div class="text-lg font-semibold text-gray-900">{{ editingTeam.team_name }}</div>
              <div class="text-sm text-gray-500 mt-1">指导老师：{{ editingTeam.advisorNames || editingTeam.teacher_name || '未指定' }}</div>
            </div>
            <div class="text-right">
              <div class="text-xs uppercase tracking-[0.2em] text-gray-400">可用老师</div>
              <div class="text-2xl font-black text-[#722ed1]">{{ reviewerPool.length }}</div>
            </div>
          </div>
        </div>

        <div v-if="invalidSelectedReviewers.length" class="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-700">
          检测到历史分配里有 {{ invalidSelectedReviewers.length }} 位老师已不在当前评审老师池中，打开弹窗时已自动移除，请确认后保存。
        </div>

        <div class="grid md:grid-cols-2 gap-4">
          <div class="rounded-2xl border border-gray-100 p-4 bg-[#fcfbff]">
            <div class="flex items-center justify-between mb-3">
              <div class="text-sm font-semibold text-gray-800">当前已选</div>
              <div class="text-xs text-gray-400">{{ selectedReviewerDetails.length }} 位</div>
            </div>
            <div v-if="selectedReviewerDetails.length" class="space-y-2 max-h-[420px] overflow-auto pr-1">
              <div
                v-for="reviewer in selectedReviewerDetails"
                :key="reviewer.userId"
                class="flex items-center justify-between px-3 py-3 rounded-xl bg-white border border-gray-100 shadow-sm"
              >
                <div class="flex items-center gap-3">
                  <div class="pool-avatar subtle">{{ reviewer.teacherName.charAt(0) }}</div>
                  <div class="text-sm font-medium text-gray-800">{{ reviewer.teacherName }}</div>
                  <div class="text-xs text-gray-400">{{ reviewer.phone || '未填写手机号' }}</div>
                </div>
                <span
                  v-if="isLockedReviewer(reviewer.userId)"
                  class="text-xs text-green-700 bg-green-50 ring-1 ring-green-100 px-2 py-1 rounded-full"
                >
                  已评分
                </span>
              </div>
            </div>
            <div v-else class="text-sm text-gray-400">暂未选择评审老师</div>
          </div>

          <div class="rounded-2xl border border-gray-100 p-4">
            <div class="flex items-center justify-between mb-3">
              <div class="text-sm font-semibold text-gray-800">老师池</div>
              <div class="text-xs text-gray-400">仅可从当前赛事老师池中挑选</div>
            </div>
            <el-checkbox-group v-model="selectedReviewerIds" class="w-full">
              <div v-for="reviewer in reviewerPool" :key="reviewer.userId" class="reviewer-option">
                <el-checkbox :label="reviewer.userId" :disabled="assignmentBusy || isReviewerDisabled(reviewer.userId)">
                  <span class="font-medium text-gray-800">{{ reviewer.teacherName }}</span>
                  <span class="text-xs text-gray-400 ml-2">{{ reviewer.phone || '未填写手机号' }}</span>
                </el-checkbox>
                <span v-if="isAdvisorReviewer(reviewer.userId)" class="text-xs text-red-500">不可评审自己指导的队伍</span>
                <span v-else-if="isLockedReviewer(reviewer.userId)" class="text-xs text-green-600">已评分保留</span>
                <span v-else class="text-xs text-gray-400">当前负载 {{ reviewer.assignedCount }}</span>
              </div>
            </el-checkbox-group>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="closeTeamReviewerDialog" :disabled="assignmentBusy">取消</el-button>
        <el-button type="primary" @click="submitTeamReviewers" :loading="teamReviewerSaving">保存</el-button>
      </template>
    </el-dialog>

    <div v-if="assignmentBusy" class="assignment-busy-mask">
      <div class="assignment-busy-card">
        <i class="fa fa-spinner fa-spin text-2xl text-[#722ed1]"></i>
        <div class="text-base font-semibold text-gray-900">{{ assignmentBusyText }}</div>
        <div class="text-sm text-gray-500">正在处理评审分配，请稍候，期间已锁定其他操作。</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'
import {
  getCompetitionDetail,
  listCompetitionReviewers,
  listCompetitionReviewerCandidates,
  addCompetitionReviewers,
  deleteCompetitionReviewer,
  autoAssignCompetitionReviewers,
  updateCompetitionTeamReviewers
} from '@/api/admin/competition'
import request, { download } from '@/utils/request'
import { previewImageSrc } from '@/utils/media'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const info = ref({})
const teamList = ref([])
const loading = ref(false)
const loadError = ref('')

const filterRange = reactive({ min: null, max: null })
const currentSort = ref('')

const reviewerPoolVisible = ref(false)
const reviewerPoolLoading = ref(false)
const reviewerPool = ref([])
const autoAssignLoading = ref(false)
const assignmentBusy = ref(false)
const assignmentBusyText = ref('正在分配评审老师')
const addReviewerVisible = ref(false)
const addReviewerLoading = ref(false)
const addReviewerSaving = ref(false)
const reviewerCandidates = ref([])
const addReviewerKeyword = ref('')
const selectedAddReviewerIds = ref([])

const teamReviewerVisible = ref(false)
const editingTeam = ref(null)
const selectedReviewerIds = ref([])
const teamReviewerSaving = ref(false)
const invalidSelectedReviewers = ref([])

const getPathCompetitionId = () => {
  const match = window.location.pathname.match(/competition-detail\/([^/?#]+)/)
  return match ? decodeURIComponent(match[1]) : undefined
}

const competitionId = computed(() => route.params.id || route.query.competitionId || route.query.id || getPathCompetitionId())
const sortedTeamList = computed(() => teamList.value || [])
const reviewerPoolStats = computed(() => {
  const total = reviewerPool.value.length
  const active = reviewerPool.value.filter(item => Number(item.assignedCount || 0) > 0).length
  const totalLoad = reviewerPool.value.reduce((sum, item) => sum + Number(item.assignedCount || 0), 0)
  return {
    total,
    active,
    avgLoad: total ? (totalLoad / total).toFixed(1) : '0.0'
  }
})

const selectedReviewerDetails = computed(() => {
  const selectedSet = new Set(selectedReviewerIds.value)
  return reviewerPool.value.filter(item => selectedSet.has(item.userId))
})

const filteredReviewerCandidates = computed(() => {
  const keyword = addReviewerKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return reviewerCandidates.value
  }
  return reviewerCandidates.value.filter(item =>
    (item.teacherName || '').toLowerCase().includes(keyword) ||
    (item.phone || '').toLowerCase().includes(keyword)
  )
})

const selectedAddReviewerDetails = computed(() => {
  const selectedSet = new Set(selectedAddReviewerIds.value)
  return reviewerCandidates.value.filter(item => selectedSet.has(item.userId))
})

const formatDate = (t) => (t ? t.split(' ')[0] : 'TBD')

const parseMembers = (s) => {
  try {
    return typeof s === 'string' ? JSON.parse(s) : s
  } catch {
    return []
  }
}

const getLeader = (s) => {
  const m = parseMembers(s)
  if (Array.isArray(m)) {
    return m.find(item => item?.role === 'leader' && item?.name)?.name ||
      m.find(item => item?.name)?.name ||
      '未知'
  }
  if (m && typeof m === 'object') {
    return m.leader || m.name || '未知'
  }
  return '未知'
}

const buildQueryParams = () => ({
  sortType: currentSort.value || undefined,
  minScore: filterRange.min !== null && filterRange.min !== '' ? filterRange.min : undefined,
  maxScore: filterRange.max !== null && filterRange.max !== '' ? filterRange.max : undefined
})

const getCachedCompetition = () => {
  try {
    const cached = JSON.parse(sessionStorage.getItem('adminCompetitionDetailSnapshot') || '{}')
    const cachedId = cached?.competitionId || cached?.competition_id || cached?.id
    return String(cachedId || '') === String(competitionId.value || '') ? cached : {}
  } catch {
    return {}
  }
}

const normalizeDetailPayload = (res) => {
  if (res?.competition || res?.teams) {
    return res
  }
  if (res?.data?.competition || res?.data?.teams) {
    return res.data
  }
  return {}
}

const getCachedDetailPayload = () => {
  const id = competitionId.value
  if (!id) {
    return {}
  }
  try {
    return normalizeDetailPayload(JSON.parse(sessionStorage.getItem(`adminCompetitionDetailPayload:${id}`) || '{}'))
  } catch {
    return {}
  }
}

const applyDetailPayload = (data) => {
  info.value = data.competition || getCachedCompetition()
  teamList.value = (Array.isArray(data.teams) ? data.teams : []).map((team) => ({
    ...team,
    assignedReviewers: team.assignedReviewers || [],
    advisorUserIds: team.advisorUserIds || []
  }))
}

const hasDetailData = (data) => {
  return !!(data?.competition?.competitionId || (Array.isArray(data?.teams) && data.teams.length > 0))
}

const requestDetailFrom = (baseURL, id) => {
  return request({
    baseURL,
    url: `/admin/competition/detail/${id}`,
    method: 'get',
    params: buildQueryParams(),
    headers: { silent: true }
  })
}

const fetchCompetitionDetail = async (id) => {
  let primary = {}
  try {
    primary = normalizeDetailPayload(await getCompetitionDetail(id, buildQueryParams()))
    if (hasDetailData(primary)) {
      return primary
    }
  } catch (error) {
    console.warn('competition detail primary request failed:', error)
  }

  const fallbackBases = ['', '/dev-api', '/prod-api']
  for (const baseURL of fallbackBases) {
    try {
      const fallback = normalizeDetailPayload(await requestDetailFrom(baseURL, id))
      if (hasDetailData(fallback)) {
        return fallback
      }
    } catch (error) {
      console.warn(`competition detail fallback failed: ${baseURL || 'current-origin'}`, error)
    }
  }
  return primary
}

const loadData = async () => {
  const id = competitionId.value
  if (!id) {
    loadError.value = 'competition id missing'
    info.value = {}
    teamList.value = []
    ElMessage.warning('赛事ID缺失，无法加载参赛队伍')
    return
  }

  loading.value = true
  loadError.value = ''
  try {
    const cached = getCachedDetailPayload()
    if (hasDetailData(cached)) {
      applyDetailPayload(cached)
    }
    const data = await fetchCompetitionDetail(id)
    if (hasDetailData(data)) {
      applyDetailPayload(data)
      sessionStorage.setItem(`adminCompetitionDetailPayload:${id}`, JSON.stringify(data))
    } else if (!hasDetailData(cached)) {
      applyDetailPayload({})
    }
  } catch (error) {
    loadError.value = error?.message || String(error || '')
    const cached = getCachedDetailPayload()
    if (hasDetailData(cached)) {
      applyDetailPayload(cached)
    } else {
      info.value = getCachedCompetition()
      teamList.value = []
    }
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadReviewerPool = async () => {
  reviewerPoolLoading.value = true
  try {
    const res = await listCompetitionReviewers(competitionId.value)
    reviewerPool.value = res.data || []
  } finally {
    reviewerPoolLoading.value = false
  }
}

const openReviewerPool = async () => {
  reviewerPoolVisible.value = true
  await loadReviewerPool()
}

const loadReviewerCandidates = async () => {
  addReviewerLoading.value = true
  try {
    const res = await listCompetitionReviewerCandidates(competitionId.value)
    reviewerCandidates.value = res.data || []
  } finally {
    addReviewerLoading.value = false
  }
}

const openAddReviewerDialog = async () => {
  addReviewerVisible.value = true
  addReviewerKeyword.value = ''
  selectedAddReviewerIds.value = []
  await loadReviewerCandidates()
}

const closeAddReviewerDialog = (force = false) => {
  if (!force && (assignmentBusy.value || addReviewerSaving.value)) return
  addReviewerVisible.value = false
  addReviewerKeyword.value = ''
  selectedAddReviewerIds.value = []
}

const handleUserCommand = (command) => {
  if (command === 'profile') {
    router.push('/sys-admin/profile')
    return
  }
  if (command === 'logout') {
    ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logOut().then(() => {
        location.href = '/index'
      })
    }).catch(() => {})
  }
}

const safeFileName = (name) => String(name || '赛事')
  .replace(/[\\/:*?"<>|]/g, '_')
  .replace(/\s+/g, ' ')
  .trim()

const handleExport = () => {
  const competitionName = info.value.competitionName || '赛事'
  if (!confirm(`确认要导出《${competitionName}》的参赛名单吗？`)) {
    return
  }
  download('admin/competition/export', {
    competitionId: competitionId.value,
    sortType: currentSort.value,
    minScore: filterRange.min,
    maxScore: filterRange.max
  }, `${safeFileName(competitionName)}_参赛名单.xlsx`, {
    transformRequest: [(params) => JSON.stringify(params)],
    headers: { 'Content-Type': 'application/json;charset=utf-8' }
  })
  return
  if (confirm(`确认要导出《${info.value.competitionName}》的参赛名单吗？`)) {
    request({
      url: '/admin/competition/export',
      method: 'post',
      data: {
        competitionId: competitionId.value,
        sortType: currentSort.value,
        minScore: filterRange.min,
        maxScore: filterRange.max
      },
      responseType: 'blob'
    }).then((res) => {
      const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      const link = document.createElement('a')
      link.href = window.URL.createObjectURL(blob)
      link.download = `${info.value.competitionName}_参赛名单.xlsx`
      link.click()
    })
  }
}

const toggleSort = () => {
  currentSort.value = currentSort.value === 'desc' ? 'asc' : 'desc'
  loadData()
}

const applyRangeFilter = () => {
  loadData()
}

const handleTeamJump = (team) => {
  router.push(`/admin/competition-team/${competitionId.value}/${team.register_id}/index`)
}

const handleDeleteReviewer = async (reviewer) => {
  try {
    await ElMessageBox.confirm(`确认移除评审老师 ${reviewer.teacherName} 吗？`, '提示', { type: 'warning' })
    assignmentBusy.value = true
    assignmentBusyText.value = '正在重排受影响队伍'
    const res = await deleteCompetitionReviewer(competitionId.value, reviewer.userId)
    const data = res.data || {}
    ElMessage.success(data.message || '操作成功')
    if (data.assignmentResult?.affectedTeamCount) {
      ElMessage.info(`本次只重排了 ${data.assignmentResult.affectedTeamCount} 支受影响的队伍`)
    }
    if (data.assignmentResult?.skippedTeamCount) {
      ElMessage.warning(`自动重分配后仍有 ${data.assignmentResult.skippedTeamCount} 支队伍因候选老师不足被跳过`)
    }
    await Promise.all([loadReviewerPool(), loadData()])
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  } finally {
    assignmentBusy.value = false
    assignmentBusyText.value = '正在分配评审老师'
  }
}

const handleAutoAssign = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入每个队伍要分配的评审老师人数', '开始分配', {
      inputPattern: /^[1-9]\d*$/,
      inputErrorMessage: '请输入大于 0 的整数'
    })
    assignmentBusy.value = true
    assignmentBusyText.value = '正在分配评审老师'
    autoAssignLoading.value = true
    const res = await autoAssignCompetitionReviewers(competitionId.value, { reviewerCountPerTeam: Number(value) })
    const data = res.data || {}
    const skipped = (data.skippedTeams || []).map(item => `${item.teamName}(${item.reason})`).join('，')
    ElMessage.success(`已完成 ${data.successTeamCount || 0} 支队伍分配`)
    if (data.skippedTeamCount) {
      ElMessage.warning(`跳过 ${data.skippedTeamCount} 支队伍${skipped ? `：${skipped}` : ''}`)
    }
    await Promise.all([loadReviewerPool(), loadData()])
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  } finally {
    autoAssignLoading.value = false
    assignmentBusy.value = false
    assignmentBusyText.value = '正在分配评审老师'
  }
}

const submitAddReviewers = async () => {
  if (!selectedAddReviewerIds.value.length) {
    ElMessage.warning('请先选择要加入老师池的老师')
    return
  }
  addReviewerSaving.value = true
  try {
    const res = await addCompetitionReviewers(competitionId.value, {
      reviewerIds: selectedAddReviewerIds.value
    })
    const data = res.data || {}
    ElMessage.success(data.message || '老师已加入评审老师池')
    closeAddReviewerDialog(true)
    await Promise.all([loadReviewerPool(), loadData()])
  } catch (error) {
    console.error(error)
  } finally {
    addReviewerSaving.value = false
  }
}

const openTeamReviewerDialog = async (team) => {
  editingTeam.value = team
  await loadReviewerPool()
  const poolIds = new Set(reviewerPool.value.map(item => item.userId))
  const assignedReviewers = team.assignedReviewers || []
  invalidSelectedReviewers.value = assignedReviewers.filter(item => !poolIds.has(item.reviewerId))
  selectedReviewerIds.value = assignedReviewers
    .map(item => item.reviewerId)
    .filter(id => poolIds.has(id))
  if (invalidSelectedReviewers.value.length) {
    ElMessage.warning('已自动移除不在当前评审老师池中的历史老师，请确认后保存')
  }
  teamReviewerVisible.value = true
}

const isLockedReviewer = (userId) => {
  return (editingTeam.value?.assignedReviewers || []).some(item => item.reviewerId === userId && item.reviewed)
}

const isAdvisorReviewer = (userId) => {
  return (editingTeam.value?.advisorUserIds || []).includes(userId)
}

const isReviewerDisabled = (userId) => {
  return isAdvisorReviewer(userId) || isLockedReviewer(userId)
}

const submitTeamReviewers = async () => {
  if (!editingTeam.value) return
  const lockedIds = (editingTeam.value.assignedReviewers || [])
    .filter(item => item.reviewed)
    .map(item => item.reviewerId)
  const missingLocked = lockedIds.filter(id => !selectedReviewerIds.value.includes(id))
  if (missingLocked.length) {
    ElMessage.warning('已评分的评审老师不能移除')
    return
  }

  teamReviewerSaving.value = true
  try {
    await updateCompetitionTeamReviewers(competitionId.value, editingTeam.value.team_id, {
      registerId: editingTeam.value.register_id,
      reviewerIds: selectedReviewerIds.value
    })
    ElMessage.success('评审老师已更新')
    closeTeamReviewerDialog()
    await Promise.all([loadReviewerPool(), loadData()])
  } catch (error) {
    console.error(error)
  } finally {
    teamReviewerSaving.value = false
  }
}

const closeTeamReviewerDialog = () => {
  if (assignmentBusy.value) return
  teamReviewerVisible.value = false
  invalidSelectedReviewers.value = []
}

watch(
  () => competitionId.value,
  () => {
    loadData()
  },
  { immediate: true }
)

const bannerSrc = computed(() => {
  const p = info.value?.bannerImage || ''
  const placeholder = 'https://placehold.co/1200x600?text=Competition+Banner'
  return p ? previewImageSrc(p) : placeholder
})
</script>

<style scoped>
.team-table-header,
.team-table-row {
  display: grid;
  grid-template-columns: 72px 1.2fr 1.3fr 1fr 1fr 1.7fr 110px 90px;
  gap: 16px;
  align-items: center;
  padding: 20px 32px;
}

.team-table-header {
  background: rgba(249, 250, 251, 0.7);
  border-bottom: 1px solid #f3f4f6;
  font-size: 11px;
  font-weight: 900;
  color: #9ca3af;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.team-table-row {
  border-bottom: 1px solid #f9fafb;
  transition: background-color 0.2s ease;
}

.team-table-row:hover {
  background: rgba(250, 245, 255, 0.7);
}

.user-menu-trigger {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 6px 14px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.1);
  transition: all 0.2s ease;
}

.user-menu-trigger:hover,
.user-menu-trigger:focus {
  background: rgba(255, 255, 255, 0.18);
  outline: none;
}

.user-dropdown-panel {
  min-width: 220px;
  overflow: hidden;
}

.user-dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 180px;
  padding: 6px 0;
  font-size: 15px;
}

.user-dropdown-icon {
  width: 34px;
  height: 34px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  color: #4b5563;
}

.reviewer-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #faf7ff 100%);
  border: 1px solid #f0e7ff;
  margin-bottom: 8px;
}

.pool-summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.pool-summary-card {
  padding: 16px 18px;
  border-radius: 18px;
  background: linear-gradient(135deg, #faf5ff 0%, #ffffff 100%);
  border: 1px solid #efe4ff;
}

.pool-summary-label {
  font-size: 12px;
  color: #8b8fa3;
  margin-bottom: 8px;
}

.pool-summary-value {
  font-size: 28px;
  font-weight: 900;
  color: #722ed1;
}

.pool-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #faf7ff 100%);
  border: 1px solid #f0e7ff;
}

.pool-avatar {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #7c3aed 0%, #a855f7 100%);
  color: #fff;
  font-weight: 800;
}

.pool-avatar.subtle {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: #f2e8ff;
  color: #722ed1;
}

.pool-load {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.pool-load-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 999px;
  background: #f5f0ff;
  color: #722ed1;
  font-size: 12px;
  font-weight: 700;
}

.assignment-busy-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.22);
  backdrop-filter: blur(4px);
  z-index: 2200;
  display: flex;
  align-items: center;
  justify-content: center;
}

.assignment-busy-card {
  min-width: 320px;
  max-width: 420px;
  padding: 24px 28px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(237, 224, 255, 0.95);
  box-shadow: 0 24px 80px rgba(76, 29, 149, 0.18);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  text-align: center;
}
</style>
