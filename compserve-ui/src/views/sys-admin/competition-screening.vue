<template>
  <div class="space-y-8">
    <!-- 页面标题和面包屑导航 -->
    <div class="flex flex-col md:flex-row md:items-center justify-between">
      <div>
        <nav class="flex mb-2" aria-label="Breadcrumb">
          <ol class="inline-flex items-center space-x-1 md:space-x-2">
            <li class="inline-flex items-center">
              <router-link to="/sys-admin/competition-list" class="inline-flex items-center text-sm font-medium text-gray-500 hover:text-admin-600 transition-colors">
                <i class="fa fa-home mr-2"></i>
                智能赛事管理
              </router-link>
            </li>
            <li aria-current="page">
              <div class="flex items-center">
                <i class="fa fa-angle-right text-gray-400 mx-2"></i>
                <span class="text-sm font-medium text-admin-600">队伍晋级筛选</span>
              </div>
            </li>
          </ol>
        </nav>
        <h2 class="text-2xl font-bold text-gray-800">队伍晋级筛选</h2>
        <p class="text-gray-500 mt-1">管理赛事参赛队伍，进行晋级筛选操作</p>
      </div>
      <div class="mt-4 md:mt-0 flex gap-3">
        <button v-if="selectedCompetition" class="btn-admin-primary flex items-center gap-2" @click="saveResults">
          <i class="fa fa-save"></i>
          <span>保存筛选结果</span>
        </button>
      </div>
    </div>

    <!-- 赛事选择卡片 -->
    <section v-if="!selectedCompetition" class="admin-card fade-in">
      <div class="admin-card-header">
        <h3 class="text-xl font-semibold flex items-center gap-2 text-gray-800">
          <div class="p-1.5 bg-admin-100 rounded-lg text-admin-600">
            <i class="fa fa-search"></i>
          </div>
          选择赛事
        </h3>
      </div>
      <div class="p-8">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-200 hover:shadow-lg hover:border-admin-200 hover:-translate-y-1 transition-all duration-300 cursor-pointer group text-center" @click="selectCompetition('programming')">
            <div class="w-16 h-16 bg-admin-50 rounded-2xl flex items-center justify-center text-admin-600 text-3xl mb-4 mx-auto group-hover:scale-110 transition-transform duration-300">
              <i class="fa fa-code"></i>
            </div>
            <h4 class="text-lg font-bold text-gray-900 mb-2">全国大学生程序设计大赛</h4>
            <p class="text-gray-500 text-sm mb-6">参赛队伍: 32支</p>
            <button class="btn-admin-outline w-full group-hover:bg-admin-600 group-hover:text-white group-hover:border-admin-600">
              选择此赛事
            </button>
          </div>

          <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-200 hover:shadow-lg hover:border-green-200 hover:-translate-y-1 transition-all duration-300 cursor-pointer group text-center" @click="selectCompetition('ai')">
            <div class="w-16 h-16 bg-green-50 rounded-2xl flex items-center justify-center text-green-600 text-3xl mb-4 mx-auto group-hover:scale-110 transition-transform duration-300">
              <i class="fa fa-lightbulb-o"></i>
            </div>
            <h4 class="text-lg font-bold text-gray-900 mb-2">人工智能创新设计大赛</h4>
            <p class="text-gray-500 text-sm mb-6">参赛队伍: 24支</p>
            <button class="btn-admin-outline w-full hover:bg-green-600 hover:text-white hover:border-green-600">
              选择此赛事
            </button>
          </div>

          <div class="bg-white rounded-xl shadow-sm p-6 border border-gray-200 hover:shadow-lg hover:border-purple-200 hover:-translate-y-1 transition-all duration-300 cursor-pointer group text-center" @click="selectCompetition('innovation')">
             <div class="w-16 h-16 bg-purple-50 rounded-2xl flex items-center justify-center text-purple-600 text-3xl mb-4 mx-auto group-hover:scale-110 transition-transform duration-300">
              <i class="fa fa-line-chart"></i>
            </div>
            <h4 class="text-lg font-bold text-gray-900 mb-2">创新创业大赛</h4>
            <p class="text-gray-500 text-sm mb-6">参赛队伍: 18支</p>
            <button class="btn-admin-outline w-full hover:bg-purple-600 hover:text-white hover:border-purple-600">
              选择此赛事
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 队伍筛选主区域 -->
    <section v-else class="admin-card fade-in">
      <div class="admin-card-header flex justify-between items-center">
        <h3 class="text-xl font-semibold flex items-center gap-2 text-gray-800">
          <div class="p-1.5 bg-admin-100 rounded-lg text-admin-600">
            <i class="fa fa-filter"></i>
          </div>
          <span>{{ currentCompetitionName }} - 队伍筛选</span>
        </h3>
        <div class="flex items-center gap-4">
          <button class="text-sm text-gray-500 hover:text-admin-600 flex items-center gap-1 transition-colors" @click="selectedCompetition = null">
            <i class="fa fa-arrow-left"></i> 重新选择
          </button>
          <div class="h-6 w-px bg-gray-200"></div>
          <div class="flex items-center gap-2">
            <span class="text-sm text-gray-500">当前筛选阶段:</span>
            <select class="admin-select py-1 text-sm w-auto">
              <option>初赛晋级</option>
              <option>复赛晋级</option>
              <option>决赛晋级</option>
            </select>
          </div>
        </div>
      </div>

      <!-- 筛选选项卡 -->
      <div class="border-b border-gray-100 px-6">
        <nav class="flex gap-6">
          <button 
            @click="activeTab = 'all'" 
            class="pb-4 pt-2 text-sm font-medium relative transition-colors duration-300"
            :class="activeTab === 'all' ? 'text-admin-600' : 'text-gray-500 hover:text-gray-700'"
          >
            全部队伍 ({{ teamList.length }})
            <span v-if="activeTab === 'all'" class="absolute bottom-0 left-0 w-full h-0.5 bg-admin-600 rounded-t-full"></span>
          </button>
          <button 
            @click="activeTab = 'selected'" 
            class="pb-4 pt-2 text-sm font-medium relative transition-colors duration-300"
            :class="activeTab === 'selected' ? 'text-admin-600' : 'text-gray-500 hover:text-gray-700'"
          >
            晋级队伍 ({{ selectedCount }})
            <span v-if="activeTab === 'selected'" class="absolute bottom-0 left-0 w-full h-0.5 bg-admin-600 rounded-t-full"></span>
          </button>
          <button 
            @click="activeTab = 'eliminated'" 
            class="pb-4 pt-2 text-sm font-medium relative transition-colors duration-300"
            :class="activeTab === 'eliminated' ? 'text-admin-600' : 'text-gray-500 hover:text-gray-700'"
          >
            淘汰队伍 ({{ eliminatedCount }})
            <span v-if="activeTab === 'eliminated'" class="absolute bottom-0 left-0 w-full h-0.5 bg-admin-600 rounded-t-full"></span>
          </button>
        </nav>
      </div>

      <div class="p-6">
        <!-- 搜索和筛选工具 -->
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
          <div class="relative flex-1 max-w-lg">
            <input type="text" class="admin-input pl-10" placeholder="搜索队伍名称、队长或学校...">
            <i class="fa fa-search absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"></i>
          </div>
          <div class="flex gap-2">
            <button class="btn-admin-outline text-sm py-2 px-3 flex items-center gap-2">
              <i class="fa fa-filter"></i>
              <span>高级筛选</span>
            </button>
            <button class="btn-admin-outline text-sm py-2 px-3 flex items-center gap-2">
              <i class="fa fa-sort-amount-asc"></i>
              <span>成绩排序</span>
            </button>
          </div>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="space-y-4">
          <div v-for="i in 3" :key="i" class="bg-white p-5 rounded-xl border border-gray-100 animate-pulse">
            <div class="flex justify-between items-start">
              <div class="w-2/3">
                <div class="h-6 bg-gray-200 rounded w-1/3 mb-4"></div>
                <div class="h-4 bg-gray-200 rounded w-2/3 mb-2"></div>
                <div class="h-4 bg-gray-200 rounded w-1/2"></div>
              </div>
              <div class="w-24 h-8 bg-gray-200 rounded"></div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else-if="filteredList.length === 0" class="text-center py-12">
          <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300">
            <i class="fa fa-users text-3xl"></i>
          </div>
          <p class="text-gray-500">暂无相关队伍数据</p>
        </div>

        <!-- 队伍列表 -->
        <div v-else class="space-y-4">
          <div v-for="team in filteredList" :key="team.id" 
               class="bg-white p-5 rounded-xl shadow-sm border border-gray-100 hover:shadow-md transition-all duration-300 relative overflow-hidden group"
               :class="{'border-l-4 border-l-green-500 bg-green-50/10': team.status === 'selected', 'border-l-4 border-l-red-500 bg-red-50/10': team.status === 'eliminated'}"
          >
            <div class="flex flex-col md:flex-row md:items-start justify-between gap-4">
              <div class="flex-1">
                <div class="flex items-center gap-3 mb-2">
                  <h4 class="font-bold text-lg text-gray-800">{{ team.name }}</h4>
                  <span class="admin-badge admin-badge-info">初赛成绩: {{ team.score }}</span>
                  <span v-if="team.status === 'selected'" class="admin-badge admin-badge-success">已晋级</span>
                  <span v-if="team.status === 'eliminated'" class="admin-badge admin-badge-danger">已淘汰</span>
                </div>
                
                <div class="grid grid-cols-1 md:grid-cols-3 gap-y-2 gap-x-6 text-sm text-gray-600 mb-3">
                  <div class="flex items-center gap-2">
                    <i class="fa fa-user-circle text-admin-400"></i>
                    <span>队长: {{ team.captain }} ({{ team.department }})</span>
                  </div>
                  <div class="flex items-center gap-2">
                    <i class="fa fa-users text-admin-400"></i>
                    <span>队员: {{ team.members.join(', ') }}</span>
                  </div>
                  <div class="flex items-center gap-2">
                    <i class="fa fa-university text-admin-400"></i>
                    <span>{{ team.university }}</span>
                  </div>
                </div>

                <div class="flex gap-2">
                   <span v-for="tag in team.tags" :key="tag" class="px-2 py-0.5 bg-gray-100 text-gray-600 text-xs rounded-md">{{ tag }}</span>
                </div>
              </div>

              <div class="flex items-center gap-3 self-start">
                <button 
                  @click="updateStatus(team, 'selected')"
                  class="btn-admin-base text-xs py-1.5 px-3 rounded-lg border transition-colors flex items-center gap-1"
                  :class="team.status === 'selected' ? 'bg-green-600 text-white border-green-600' : 'bg-white text-green-600 border-green-200 hover:bg-green-50'"
                >
                  <i class="fa fa-check"></i> 晋级
                </button>
                <button 
                  @click="updateStatus(team, 'eliminated')"
                  class="btn-admin-base text-xs py-1.5 px-3 rounded-lg border transition-colors flex items-center gap-1"
                  :class="team.status === 'eliminated' ? 'bg-red-600 text-white border-red-600' : 'bg-white text-red-600 border-red-200 hover:bg-red-50'"
                >
                  <i class="fa fa-times"></i> 淘汰
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const selectedCompetition = ref(null)
const activeTab = ref('all')
const loading = ref(false)
const teamList = ref([])

const currentCompetitionName = computed(() => {
  const map = {
    programming: '全国大学生程序设计大赛',
    ai: '人工智能创新设计大赛',
    innovation: '创新创业大赛'
  }
  return map[selectedCompetition.value] || ''
})

const selectedCount = computed(() => teamList.value.filter(t => t.status === 'selected').length)
const eliminatedCount = computed(() => teamList.value.filter(t => t.status === 'eliminated').length)

const filteredList = computed(() => {
  if (activeTab.value === 'all') return teamList.value
  return teamList.value.filter(t => t.status === activeTab.value)
})

const selectCompetition = (type) => {
  loading.value = true
  selectedCompetition.value = type
  
  // Simulate data fetch
  setTimeout(() => {
    teamList.value = [
      {
        id: 1,
        name: '编程达人队',
        score: 92.5,
        captain: '张三',
        department: '计算机学院',
        members: ['李四', '王五'],
        university: '北京大学',
        tags: ['算法设计', 'C++'],
        status: 'pending'
      },
      {
        id: 2,
        name: '创新先锋队',
        score: 88.3,
        captain: '赵六',
        department: '软件学院',
        members: ['钱七', '孙八'],
        university: '清华大学',
        tags: ['人工智能', 'Python'],
        status: 'pending'
      },
      {
        id: 3,
        name: '极速代码队',
        score: 95.0,
        captain: '周九',
        department: '信息工程学院',
        members: ['吴十', '郑十一'],
        university: '浙江大学',
        tags: ['Java', '后端开发'],
        status: 'selected'
      }
    ]
    loading.value = false
  }, 600)
}

const updateStatus = (team, status) => {
  team.status = team.status === status ? 'pending' : status
}

const saveResults = () => {
  // Save logic
  console.log('Results saved')
}
</script>

<style scoped>
.fade-in {
  animation: fadeIn 0.4s ease-out backwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
