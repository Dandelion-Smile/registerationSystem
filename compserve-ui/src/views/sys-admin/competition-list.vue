<template>
  <div class="space-y-8">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">智能赛事管理</h2>
        <p class="text-gray-500 mt-1">全生命周期赛事管理，高效处理赛事事务</p>
      </div>
      <div class="flex gap-3">
        <button class="btn-admin-primary flex items-center gap-2">
          <i class="fa fa-eye"></i>
          <span>查看已发布赛事信息</span>
        </button>
      </div>
    </div>

    <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
      <h4 class="text-sm font-bold text-gray-700 mb-4 flex items-center gap-2">
        <i class="fa fa-bar-chart text-admin-500"></i>
        赛事状态分布概览
      </h4>
      <div ref="chartRef" class="w-full" style="height: 180px;"></div>
    </div>

    <section class="space-y-6">
      <div class="flex items-center justify-between">
        <h3 class="text-xl font-semibold flex items-center gap-2 text-gray-800">
          <div class="p-1.5 bg-admin-100 rounded-lg text-admin-600">
            <i class="fa fa-list-ol"></i>
          </div>
          所有赛事列表 ({{ total }} 条)
        </h3>
        <div class="relative">
          <input
              v-model="queryParams.competitionName"
              type="text"
              placeholder="搜索赛事名称..."
              class="admin-input pl-10 pr-4 py-2 w-64"
              @keyup.enter="handleQuery"
          >
          <i class="fa fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"></i>
        </div>
      </div>

      <div class="flex items-center gap-2 p-1 bg-gray-100 w-fit rounded-xl border border-gray-200">
        <button
            v-for="tab in statusTabs"
            :key="tab.value"
            @click="handleStatusTabChange(tab.value)"
            :class="[
            'px-6 py-2 rounded-lg text-sm font-medium transition-all duration-200',
            queryParams.status === tab.value
              ? 'bg-white text-admin-600 shadow-sm'
              : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'
          ]"
        >
          {{ tab.label }}
        </button>
      </div>

      <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="i in 3" :key="i" class="bg-white h-64 animate-pulse rounded-xl border"></div>
      </div>

      <div v-else-if="competitionList.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-100 p-12 text-center">
        <div class="w-24 h-24 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-6 text-gray-300">
          <i class="fa fa-inbox text-4xl"></i>
        </div>
        <h3 class="text-lg font-medium text-gray-900 mb-2">暂无赛事</h3>
        <p class="text-gray-500">该分类下没有查询到记录</p>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
            v-for="item in competitionList"
            :key="item.competitionId"
            @click="handleDetail(item)"
            class="admin-card cursor-pointer group hover:-translate-y-1 transition-all duration-300"
        >
          <div class="relative">
            <div class="h-40 bg-gray-200 bg-cover bg-center" :style="{ backgroundImage: `url(${previewImageSrc(item.bannerImage) || 'https://placehold.co/800x600?text=Event'})` }"></div>
            <div class="absolute top-0 right-0">
              <div class="bg-admin-500 text-white text-xs font-bold px-3 py-1 rounded-bl-lg">
                {{ item.competitionType }}
              </div>
            </div>
          </div>

          <div class="p-6">
            <div class="flex justify-between items-start mb-3">
              <h4 class="text-lg font-bold text-gray-800 line-clamp-1">{{ item.competitionName }}</h4>
            </div>

            <div class="flex items-center justify-between mb-4">
              <span class="admin-badge" :class="getStatusInfo(item).class">
                {{ getStatusInfo(item).label }}
              </span>

              <div class="flex items-center gap-1.5 text-orange-600 font-bold bg-orange-50 px-2.5 py-1 rounded-lg text-xs border border-orange-100">
                <i class="fa fa-users text-orange-400"></i>
                <span>{{ item.teamCount || 0 }} 支队伍</span>
              </div>
            </div>

            <p class="text-sm text-gray-600 mb-4 line-clamp-2 h-10">{{ item.description }}</p>

            <div class="flex flex-col space-y-1 text-xs text-gray-500 mb-4">
              <span>报名开始：{{ formatTime(item.registerStartTime) }}</span>
              <span>报名截止：{{ formatTime(item.registerEndTime) }}</span>
            </div>

            <div class="pt-4 border-t border-gray-100 flex justify-between items-center">
              <button type="button" class="text-admin-600 hover:underline text-sm font-medium">详情</button>
              <a v-if="item.competitionLink" :href="item.competitionLink" target="_blank" @click.stop class="text-gray-400 hover:text-admin-600 transition-colors">
                <i class="fa fa-external-link"></i>
              </a>
            </div>
          </div>
        </div>
      </div>

      <pagination
          v-show="total > 0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
          class="mt-8 flex justify-end"
      />
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { listCompetition, getCompetitionStats, getCompetitionDetail } from '@/api/admin/competition'
import { previewImageSrc } from '@/utils/media'

const router = useRouter()
const chartRef = ref(null)
let chartInstance = null
const loading = ref(true)
const competitionList = ref([])
const total = ref(0)

const statusTabs = [
  { label: '全部', value: undefined },
  { label: '进行中', value: 'in_progress' },
  { label: '未进行', value: 'not_started' },
  { label: '已结束', value: 'ended' }
]

const queryParams = reactive({
  pageNum: 1,
  pageSize: 9,
  competitionName: undefined,
  status: undefined
})

/** 跳转详情页逻辑 */
const getCompetitionId = (item) => {
  return item?.competitionId || item?.competition_id || item?.id
}

const handleDetail = async (item) => {
  const id = getCompetitionId(item)
  if (!id) {
    console.warn('competition id is missing on card item:', item)
    return
  }
  sessionStorage.setItem('adminCompetitionDetailSnapshot', JSON.stringify(item))
  try {
    const detail = await getCompetitionDetail(id)
    sessionStorage.setItem(`adminCompetitionDetailPayload:${id}`, JSON.stringify(detail))
  } catch (error) {
    console.error('prefetch competition detail failed:', error)
  }
  router.push({
    path: `/admin/competition-detail/${id}`,
    query: { competitionId: id }
  })
}

/** 初始化或更新统计图表 */
const initChart = (data) => {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      top: '5%',
      left: '3%',
      right: '8%',
      bottom: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      splitLine: { lineStyle: { type: 'dashed', color: '#f3f4f6' } },
      axisLabel: { show: false },
      axisLine: { show: false }
    },
    yAxis: {
      type: 'category',
      data: ['已结束', '未进行', '进行中'],
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { color: '#4b5563', fontSize: 13, fontWeight: 'medium' }
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        barWidth: '55%',
        data: [
          { value: data.ended || 0, itemStyle: { color: '#ef4444', borderRadius: [0, 4, 4, 0] } },
          { value: data.not_started || 0, itemStyle: { color: '#10b981', borderRadius: [0, 4, 4, 0] } },
          { value: data.in_progress || 0, itemStyle: { color: '#3b82f6', borderRadius: [0, 4, 4, 0] } }
        ],
        label: {
          show: true,
          position: 'right',
          color: '#374151',
          fontWeight: 'bold',
          formatter: '{c} 条'
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

/** 获取列表及统计数据 */
const getList = () => {
  loading.value = true

  // 1. 获取赛事列表分页数据
  listCompetition(queryParams).then(response => {
    competitionList.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })

  // 2. 获取实时统计数据并更新图表
  getCompetitionStats().then(response => {
    initChart(response.data)
  })
}

const handleStatusTabChange = (statusValue) => {
  queryParams.status = statusValue
  queryParams.pageNum = 1
  getList()
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const getStatusInfo = (item) => {
  if (!item.registerStartTime || !item.registerEndTime) return { label: '待定', class: 'bg-gray-100' }
  const now = new Date()
  const start = new Date(item.registerStartTime)
  const end = new Date(item.registerEndTime)
  if (now < start) return { label: '未进行', class: 'bg-green-100 text-green-700' }
  if (now >= start && now <= end) return { label: '进行中', class: 'bg-blue-100 text-blue-700' }
  return { label: '已结束', class: 'bg-red-100 text-red-700' }
}

const formatTime = (time) => time ? time.split(' ')[0] : '待定'

onMounted(() => {
  getList()
  window.addEventListener('resize', () => chartInstance?.resize())
})
</script>

<style scoped>
.admin-card {
  @apply bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden;
}
.admin-badge {
  @apply px-2 py-0.5 rounded text-xs font-medium;
}
.admin-input {
  @apply border border-gray-200 rounded-lg focus:ring-2 focus:ring-admin-500 focus:border-admin-500 outline-none transition-all;
}
.btn-admin-primary {
  @apply bg-admin-600 text-white px-4 py-2 rounded-lg hover:bg-admin-700 transition-colors shadow-sm font-medium text-sm;
}
</style>
