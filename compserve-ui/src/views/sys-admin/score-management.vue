<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">评分结果管理</h2>
        <p class="text-gray-500 mt-1">查看各赛道评分详情，监控评审进度与评分质量</p>
      </div>
    </div>

    <!-- 统计概览卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
      <div class="admin-card stat-card border-l-blue-500">
        <div class="p-6 flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">已评分项目</p>
            <p class="mt-2 text-3xl font-bold text-gray-800">{{ scoreList.length }}</p>
          </div>
          <div class="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
            <i class="fa fa-check-square-o text-xl"></i>
          </div>
        </div>
      </div>
      <div class="admin-card stat-card border-l-amber-500">
        <div class="p-6 flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">平均分</p>
            <p class="mt-2 text-3xl font-bold text-gray-800">{{ averageScore }}</p>
          </div>
          <div class="w-12 h-12 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center">
            <i class="fa fa-line-chart text-xl"></i>
          </div>
        </div>
      </div>
      <div class="admin-card stat-card border-l-purple-500">
        <div class="p-6 flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">参与评审人</p>
            <p class="mt-2 text-3xl font-bold text-gray-800">{{ uniqueReviewers }}</p>
          </div>
          <div class="w-12 h-12 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center">
            <i class="fa fa-users text-xl"></i>
          </div>
        </div>
      </div>
    </div>

    <section class="admin-card fade-in">
      <div class="admin-card-header flex flex-col lg:flex-row lg:items-center justify-between gap-4">
        <h3 class="text-xl font-semibold flex items-center gap-2 text-gray-800">
          <div class="p-1.5 bg-admin-100 rounded-lg text-admin-700">
            <i class="fa fa-star"></i>
          </div>
          评分明细列表
        </h3>
        <div class="flex flex-col lg:flex-row items-center gap-3">
          <el-select
            v-model="queryParams.competitionId"
            placeholder="按竞赛筛选评分结果"
            clearable
            filterable
            class="w-full lg:w-72 admin-el-select"
            @change="handleQuery"
          >
            <el-option
              v-for="comp in competitionList"
              :key="comp.competitionId"
              :label="comp.competitionName"
              :value="comp.competitionId"
            />
          </el-select>
          <button class="btn-admin-outline px-6 py-2" @click="resetQuery">重置筛选</button>
        </div>
      </div>

      <div class="admin-card-body p-0">
        <div v-if="loading" class="p-8">
          <div class="space-y-4 animate-pulse">
            <div v-for="i in 5" :key="i" class="h-12 bg-gray-100 rounded-lg w-full"></div>
          </div>
        </div>

        <div v-else-if="scoreList.length === 0" class="p-12 text-center">
          <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300">
            <i class="fa fa-file-text-o text-3xl"></i>
          </div>
          <p class="text-gray-500">暂无评分记录，请确保评审工作已开始</p>
        </div>

        <div v-else class="admin-table-container">
          <table class="admin-table">
            <thead>
              <tr>
                <th scope="col">竞赛/队伍信息</th>
                <th scope="col">评审人</th>
                <th scope="col">评分</th>
                <th scope="col">评审意见</th>
                <th scope="col">评分时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, index) in scoreList" :key="index" class="table-row-hover">
                <td>
                  <div class="text-sm font-bold text-gray-900 line-clamp-1">{{ row.teamName }}</div>
                  <div class="text-xs text-gray-400 line-clamp-1">{{ row.competitionName }}</div>
                </td>
                <td>
                  <div class="flex items-center gap-2">
                    <div class="w-7 h-7 rounded-full bg-gray-100 text-gray-600 flex items-center justify-center text-xs font-bold">
                      {{ (row.reviewerName || '评').charAt(0) }}
                    </div>
                    <span class="text-sm text-gray-700">{{ row.reviewerName }}</span>
                  </div>
                </td>
                <td>
                  <div class="flex items-center gap-1.5">
                    <span class="text-lg font-bold text-blue-600">{{ row.score }}</span>
                    <span class="text-xs text-gray-400">/ 100</span>
                  </div>
                </td>
                <td>
                  <div class="text-xs text-gray-600 max-w-xs line-clamp-2" :title="row.comment">
                    {{ row.comment || '暂无评语' }}
                  </div>
                </td>
                <td>
                  <span class="text-xs text-gray-500">{{ parseTime(row.scoreTime, '{y}-{m}-{d} {h}:{i}') }}</span>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { listScoreResults } from '@/api/admin/score'
import { listCompetition } from '@/api/admin/competition'
import { parseTime } from '@/utils/ruoyi'

const loading = ref(false)
const scoreList = ref([])
const competitionList = ref([])

const queryParams = reactive({
  competitionId: null
})

const averageScore = computed(() => {
  if (scoreList.value.length === 0) return 0
  const sum = scoreList.value.reduce((acc, cur) => acc + (cur.score || 0), 0)
  return (sum / scoreList.value.length).toFixed(1)
})

const uniqueReviewers = computed(() => {
  const reviewers = new Set(scoreList.value.map(s => s.reviewerName))
  return reviewers.size
})

function loadData() {
  listCompetition({ pageNum: 1, pageSize: 1000 }).then(res => {
    competitionList.value = res.rows || []
  })
  getList()
}

function getList() {
  loading.value = true
  listScoreResults(queryParams.competitionId).then(res => {
    scoreList.value = res.data || []
  }).finally(() => loading.value = false)
}

function handleQuery() { getList() }
function resetQuery() { queryParams.competitionId = null; getList(); }

onMounted(loadData)
</script>

<style scoped>
.admin-card { @apply bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden; }
.admin-card-header { @apply px-6 py-5 border-b border-gray-100; }
.admin-card-body { @apply p-6; }
.stat-card { @apply border-l-4 transition-transform hover:-translate-y-1; }
.btn-admin-outline { @apply border border-gray-200 text-gray-700 hover:bg-gray-50 font-medium rounded-lg transition-colors px-4 py-2; }
.admin-table { @apply min-w-full; }
.admin-table thead tr { @apply bg-gray-50; }
.admin-table th { @apply px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider; }
.admin-table td { @apply px-6 py-4 border-t border-gray-100; }
.table-row-hover { @apply transition-colors hover:bg-gray-50/50; }
.fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

:deep(.admin-el-select .el-input__wrapper) {
  @apply rounded-lg border-gray-200 shadow-none ring-0 !important;
  height: 40px;
}
</style>
