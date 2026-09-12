<template>
  <div class="admin-page-container space-y-6">
    <section class="admin-card dashboard-hero fade-in">
      <div class="p-6 md:p-7 flex flex-col lg:flex-row lg:items-center justify-between gap-5">
        <div>
          <h2 class="admin-page-title">智能赛事管理</h2>
          <p class="admin-page-subtitle">覆盖赛事审核、运营执行与归档追踪的一体化看板</p>
          <div class="flex flex-wrap gap-2 mt-4">
            <span class="hero-tag"><i class="fa fa-bolt mr-1.5"></i>流程提效</span>
            <span class="hero-tag"><i class="fa fa-eye mr-1.5"></i>进度可视</span>
            <span class="hero-tag"><i class="fa fa-shield mr-1.5"></i>质量把控</span>
          </div>
        </div>
        <div class="flex flex-wrap gap-3">
          <button class="btn-admin-outline flex items-center gap-2">
            <i class="fa fa-refresh"></i>
            刷新看板
          </button>
          <button class="btn-admin-primary flex items-center gap-2" @click="openModal('notice-modal')">
            <i class="fa fa-bullhorn"></i>
            发布通知
          </button>
        </div>
      </div>
    </section>

    <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-5">
      <div v-for="item in overviewStats" :key="item.label" class="admin-card stat-card" :class="item.borderClass">
        <div class="p-6 flex items-center justify-between gap-4">
          <div>
            <p class="text-sm font-medium text-gray-500">{{ item.label }}</p>
            <p class="mt-2 text-3xl font-bold text-gray-800">{{ item.value }}</p>
            <p class="mt-1 text-sm font-medium" :class="item.trendClass">{{ item.trend }}</p>
          </div>
          <div class="stat-icon" :class="item.iconClass">
            <i :class="item.icon"></i>
          </div>
        </div>
      </div>
    </div>

    <section class="admin-card fade-in" style="animation-delay: 0.1s;">
      <div class="admin-card-header flex items-center justify-between gap-3">
        <h3 class="text-lg font-bold text-gray-800 flex items-center gap-2">
          <div class="p-1.5 bg-admin-100 rounded-lg text-admin-700">
            <i class="fa fa-check-square-o"></i>
          </div>
          赛事审核列表
        </h3>
        <button class="btn-admin-ghost text-xs">查看全部</button>
      </div>
      <div class="admin-card-body p-0">
        <div v-if="loading" class="p-8">
          <div class="space-y-4">
            <div v-for="i in 3" :key="i" class="flex items-center gap-4 animate-pulse">
              <div class="flex-1 space-y-2">
                <div class="h-4 bg-gray-200 rounded w-1/4"></div>
              </div>
              <div class="h-4 bg-gray-200 rounded w-1/6"></div>
              <div class="h-4 bg-gray-200 rounded w-1/6"></div>
              <div class="h-6 bg-gray-200 rounded w-16"></div>
              <div class="h-6 bg-gray-200 rounded w-24"></div>
            </div>
          </div>
        </div>

        <div v-else-if="auditList.length === 0" class="p-12 text-center">
          <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300">
            <i class="fa fa-clipboard text-2xl"></i>
          </div>
          <p class="text-gray-500">暂无待审核赛事</p>
        </div>

        <div v-else class="admin-table-container border-0 shadow-none rounded-none">
          <table class="admin-table">
            <thead>
              <tr>
                <th scope="col">赛事名称</th>
                <th scope="col">创建人</th>
                <th scope="col">创建时间</th>
                <th scope="col">状态</th>
                <th scope="col">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in auditList" :key="index" class="table-row-hover">
                <td><div class="text-sm font-semibold text-gray-900">{{ item.name }}</div></td>
                <td><div class="text-sm text-gray-500">{{ item.creator }}</div></td>
                <td><div class="text-sm text-gray-500">{{ item.createTime }}</div></td>
                <td><span class="admin-badge admin-badge-info">{{ item.status }}</span></td>
                <td>
                  <div class="flex gap-2">
                    <button class="table-action-btn table-action-btn-view" @click="viewDetails(item)">查看</button>
                    <button class="table-action-btn table-action-btn-success" @click="approveItem(item)">通过</button>
                    <button class="table-action-btn table-action-btn-danger" @click="rejectItem(item)">驳回</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="!loading && auditList.length > 0" class="flex items-center justify-between px-6 py-4 border-t border-gray-100 bg-gray-50/50 rounded-b-xl">
          <div class="text-sm text-gray-500">显示 1 至 {{ auditList.length }} 条，共 {{ auditList.length }} 条</div>
          <div class="flex gap-1">
            <button class="w-8 h-8 flex items-center justify-center border border-gray-200 rounded-md bg-white text-gray-400 hover:bg-gray-50 disabled:opacity-50 transition-colors" disabled>
              <i class="fa fa-chevron-left text-xs"></i>
            </button>
            <button class="w-8 h-8 flex items-center justify-center border border-admin-500 bg-admin-50 text-admin-700 font-semibold rounded-md shadow-sm">1</button>
            <button class="w-8 h-8 flex items-center justify-center border border-gray-200 rounded-md bg-white text-gray-400 hover:bg-gray-50 disabled:opacity-50 transition-colors" disabled>
              <i class="fa fa-chevron-right text-xs"></i>
            </button>
          </div>
        </div>
      </div>
    </section>

    <section class="admin-card fade-in" style="animation-delay: 0.2s;">
      <div class="admin-card-header">
        <h3 class="text-lg font-bold text-gray-800 flex items-center gap-2">
          <div class="p-1.5 bg-admin-100 rounded-lg text-admin-700">
            <i class="fa fa-cogs"></i>
          </div>
          赛事运营模块
        </h3>
      </div>
      <div class="admin-card-body">
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-5">
          <div class="module-card group" @click="openModal('notice-modal')">
            <div class="module-icon"><i class="fa fa-bullhorn"></i></div>
            <h4 class="text-lg font-bold text-gray-900 mb-2">发布补充通知</h4>
            <p class="text-gray-500 text-sm mb-6 line-clamp-2">向参赛队伍发布赛事补充说明、变更通知等信息</p>
            <button class="btn-admin-secondary w-full">进入模块</button>
          </div>

          <div class="module-card group" @click="openModal('registration-modal')">
            <div class="module-icon"><i class="fa fa-users"></i></div>
            <h4 class="text-lg font-bold text-gray-900 mb-2">管理报名情况</h4>
            <p class="text-gray-500 text-sm mb-6 line-clamp-2">查看报名队伍列表、审核报名资格、管理参赛人员信息</p>
            <button class="btn-admin-secondary w-full">进入模块</button>
          </div>

          <div class="module-card group" @click="openModal('judge-modal')">
            <div class="module-icon"><i class="fa fa-user-o"></i></div>
            <h4 class="text-lg font-bold text-gray-900 mb-2">安排赛事评委</h4>
            <p class="text-gray-500 text-sm mb-6 line-clamp-2">邀请并管理赛事评委，分配评审任务和时间安排</p>
            <button class="btn-admin-secondary w-full">进入模块</button>
          </div>
        </div>
      </div>
    </section>

    <section class="admin-card fade-in" style="animation-delay: 0.3s;">
      <div class="admin-card-header">
        <h3 class="text-lg font-bold text-gray-800 flex items-center gap-2">
          <div class="p-1.5 bg-gray-100 rounded-lg text-gray-600">
            <i class="fa fa-archive"></i>
          </div>
          赛事归档区
        </h3>
      </div>
      <div class="admin-card-body p-0">
        <div class="admin-table-container border-0 shadow-none rounded-none">
          <table class="admin-table">
            <thead>
              <tr>
                <th scope="col">赛事名称</th>
                <th scope="col">结束时间</th>
                <th scope="col">参赛人数</th>
                <th scope="col">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr class="table-row-hover">
                <td><div class="text-sm font-semibold text-gray-900">2023年创新实践项目竞赛</div></td>
                <td><div class="text-sm text-gray-500">2023-06-15</div></td>
                <td><div class="text-sm text-gray-500">287人</div></td>
                <td><button class="table-action-btn table-action-btn-view">查看详情</button></td>
              </tr>
              <tr class="table-row-hover">
                <td><div class="text-sm font-semibold text-gray-900">数据结构与算法设计竞赛</div></td>
                <td><div class="text-sm text-gray-500">2023-05-22</div></td>
                <td><div class="text-sm text-gray-500">156人</div></td>
                <td><button class="table-action-btn table-action-btn-view">查看详情</button></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>

    <transition name="fade">
      <div v-if="activeModal" id="modal-container" class="fixed inset-0 bg-gray-900/60 z-50 flex items-center justify-center p-4 backdrop-blur-sm" @click.self="closeModal">
        <div v-if="activeModal === 'notice-modal'" class="modal-card w-full max-w-2xl">
          <div class="modal-header">
            <h3 class="text-lg font-bold text-gray-800">审核发布补充通知</h3>
            <button class="modal-close-btn" @click="closeModal">
              <i class="fa fa-times text-lg"></i>
            </button>
          </div>
          <div class="p-6 overflow-y-auto">
            <form class="space-y-6" @submit.prevent="closeModal">
              <div>
                <label class="admin-label">通知标题</label>
                <input type="text" class="admin-input" placeholder="请输入通知标题">
              </div>
              <div>
                <label class="admin-label">通知内容</label>
                <textarea rows="6" class="admin-input" placeholder="请输入通知详细内容..."></textarea>
              </div>
              <div>
                <label class="admin-label">发布范围</label>
                <select class="admin-select">
                  <option>所有参赛队伍</option>
                  <option>初赛队伍</option>
                  <option>决赛队伍</option>
                </select>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn-admin-outline" @click="closeModal">取消</button>
            <button type="submit" class="btn-admin-primary" @click="closeModal">发布通知</button>
          </div>
        </div>

        <div v-if="activeModal === 'registration-modal'" class="modal-card w-full max-w-4xl">
          <div class="modal-header">
            <h3 class="text-lg font-bold text-gray-800">管理报名情况</h3>
            <button class="modal-close-btn" @click="closeModal">
              <i class="fa fa-times text-lg"></i>
            </button>
          </div>
          <div class="p-6 overflow-y-auto">
            <div class="flex flex-col md:flex-row justify-between md:items-center gap-3 mb-6">
              <div><p class="text-sm text-gray-500">共收到 <span class="font-bold text-admin-700 text-lg">28</span> 份报名申请</p></div>
              <div class="relative w-full md:w-64">
                <input type="text" placeholder="搜索队伍名称..." class="admin-input pl-10">
                <i class="fa fa-search absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"></i>
              </div>
            </div>
            <div class="admin-table-container">
              <table class="admin-table">
                <thead>
                  <tr>
                    <th scope="col">队伍名称</th>
                    <th scope="col">队长</th>
                    <th scope="col">提交时间</th>
                    <th scope="col">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr class="table-row-hover">
                    <td><div class="text-sm font-semibold text-gray-900">极客小队</div></td>
                    <td><div class="text-sm text-gray-500">王五</div></td>
                    <td><div class="text-sm text-gray-500">2023-04-12 10:30</div></td>
                    <td>
                      <div class="flex gap-3">
                        <button class="table-action-btn table-action-btn-view">审核</button>
                        <button class="table-action-btn table-action-btn-danger">移除</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn-admin-outline" @click="closeModal">关闭</button>
          </div>
        </div>

        <div v-if="activeModal === 'judge-modal'" class="modal-card w-full max-w-3xl">
          <div class="modal-header">
            <h3 class="text-lg font-bold text-gray-800">安排赛事评委</h3>
            <button class="modal-close-btn" @click="closeModal">
              <i class="fa fa-times text-lg"></i>
            </button>
          </div>
          <div class="p-6 overflow-y-auto space-y-4">
            <div v-for="judge in judgeList" :key="judge.name" class="flex items-center justify-between p-4 rounded-xl border border-gray-100 bg-gray-50/60">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-primary-100 text-primary-700 font-semibold flex items-center justify-center">
                  {{ judge.name.charAt(0) }}
                </div>
                <div>
                  <p class="font-semibold text-gray-900">{{ judge.name }}</p>
                  <p class="text-sm text-gray-500">{{ judge.org }}</p>
                </div>
              </div>
              <button class="btn-admin-outline text-xs py-1.5 px-3">分配任务</button>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn-admin-outline" @click="closeModal">关闭</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const activeModal = ref(null)
const loading = ref(true)

const auditList = ref([])
const overviewStats = [
  {
    label: '待审核赛事',
    value: '12',
    trend: '较昨日 +3',
    trendClass: 'text-green-600',
    icon: 'fa fa-clock-o text-xl',
    iconClass: 'bg-orange-50 text-orange-600',
    borderClass: 'border-l-orange-500'
  },
  {
    label: '本周发布通知',
    value: '26',
    trend: '覆盖 31 支队伍',
    trendClass: 'text-gray-500',
    icon: 'fa fa-bullhorn text-xl',
    iconClass: 'bg-primary-50 text-primary-600',
    borderClass: 'border-l-primary-500'
  },
  {
    label: '已报名队伍',
    value: '284',
    trend: '周环比 +8.5%',
    trendClass: 'text-green-600',
    icon: 'fa fa-users text-xl',
    iconClass: 'bg-green-50 text-green-600',
    borderClass: 'border-l-green-500'
  },
  {
    label: '归档赛事',
    value: '48',
    trend: '累计历史数据',
    trendClass: 'text-gray-500',
    icon: 'fa fa-archive text-xl',
    iconClass: 'bg-gray-100 text-gray-600',
    borderClass: 'border-l-gray-400'
  }
]
const judgeList = [
  { name: '王教授', org: '计算机学院' },
  { name: '刘老师', org: '人工智能研究院' },
  { name: '周老师', org: '软件工程学院' }
]

onMounted(() => {
  setTimeout(() => {
    auditList.value = [
      { name: 'AI算法挑战赛', creator: '李老师', createTime: '2023-08-01', status: '待审核' },
      { name: '校园摄影大赛', creator: '张老师', createTime: '2023-07-28', status: '待审核' }
    ]
    loading.value = false
  }, 800)
})

const openModal = (modalName) => {
  activeModal.value = modalName
}

const closeModal = () => {
  activeModal.value = null
}

const viewDetails = (item) => {
  console.log('View details', item)
}

const approveItem = (item) => {
  console.log('Approve item', item)
}

const rejectItem = (item) => {
  console.log('Reject item', item)
}
</script>

<style scoped lang="scss">
.admin-page-container {
  @apply space-y-6;
}

.admin-page-title {
  @apply text-2xl md:text-3xl font-bold text-gray-900 tracking-tight;
}

.admin-page-subtitle {
  @apply text-gray-600 mt-2;
}

.admin-card {
  @apply bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden;
}

.admin-card-header {
  @apply px-6 py-5 border-b border-gray-100;
}

.admin-card-body {
  @apply p-6;
}

.dashboard-hero {
  @apply bg-gradient-to-br from-primary-50 via-white to-admin-50/60 border-primary-100;
}

.hero-tag {
  @apply inline-flex items-center px-2.5 py-1 rounded-lg bg-white/90 border border-primary-100 text-xs text-primary-700 font-medium;
}

.btn-admin-primary {
  @apply bg-gradient-to-r from-primary-700 to-primary-600 hover:from-primary-800 hover:to-primary-700 text-white font-semibold py-2.5 px-4 rounded-xl transition-all duration-300 shadow-md;
}

.btn-admin-outline {
  @apply border border-gray-200 text-gray-700 hover:bg-gray-50 font-semibold py-2.5 px-4 rounded-xl transition-all duration-300;
}

.btn-admin-secondary {
  @apply border border-primary-100 text-primary-700 bg-primary-50/70 hover:bg-primary-100 font-semibold py-2.5 px-4 rounded-xl transition-all duration-300;
}

.btn-admin-ghost {
  @apply text-admin-700 hover:bg-admin-50 px-3 py-1.5 rounded-lg font-semibold transition-colors;
}

.admin-input {
  @apply h-10 border border-gray-200 rounded-xl px-3 text-sm text-gray-700 placeholder:text-gray-400 focus:outline-none focus:border-primary-400 focus:ring-4 focus:ring-primary-100 transition-all bg-white w-full;
}

.admin-select {
  @apply h-10 border border-gray-200 rounded-xl px-3 text-sm text-gray-700 bg-white focus:outline-none focus:border-primary-400 focus:ring-4 focus:ring-primary-100 transition-all w-full;
}

.admin-label {
  @apply block text-sm font-semibold text-gray-700 mb-1.5;
}

.admin-table-container {
  @apply overflow-x-auto;
}

.admin-table {
  @apply min-w-full;
}

.admin-table thead tr {
  @apply bg-gray-50;
}

.admin-table th {
  @apply px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider;
}

.admin-table td {
  @apply px-6 py-4 border-t border-gray-100;
}

.admin-badge {
  @apply inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold;
}

.admin-badge-info {
  @apply bg-admin-100 text-admin-800;
}

.stat-card {
  @apply border-l-4 hover:-translate-y-1 hover:shadow-md transition-all duration-300;
}

.stat-icon {
  @apply w-12 h-12 rounded-xl flex items-center justify-center;
}

.module-card {
  @apply bg-gray-50 rounded-xl p-6 border border-gray-100 hover:bg-white hover:shadow-lg hover:border-primary-100 transition-all duration-300 cursor-pointer;
}

.module-icon {
  @apply w-12 h-12 bg-white rounded-lg shadow-sm flex items-center justify-center text-primary-600 text-xl mb-4 transition-colors duration-300;
}

.module-card:hover .module-icon {
  @apply bg-primary-600 text-white;
}

.table-row-hover {
  @apply transition-colors;
}

.table-row-hover:hover td {
  @apply bg-primary-50/30;
}

.table-action-btn {
  @apply text-xs font-semibold px-2.5 py-1 rounded-md transition-colors;
}

.table-action-btn-view {
  @apply text-primary-700 hover:bg-primary-50;
}

.table-action-btn-success {
  @apply text-green-700 hover:bg-green-50;
}

.table-action-btn-danger {
  @apply text-red-700 hover:bg-red-50;
}

.modal-card {
  @apply bg-white rounded-2xl shadow-2xl max-h-[90vh] overflow-hidden flex flex-col slide-up-enter-active;
}

.modal-header {
  @apply px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50;
}

.modal-footer {
  @apply px-6 py-4 border-t border-gray-100 bg-gray-50 flex justify-end gap-3;
}

.modal-close-btn {
  @apply text-gray-400 hover:text-gray-700 transition-colors w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-200;
}

.fade-in {
  animation: fadeIn 0.5s ease-in-out backwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.slide-up-enter-active {
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>
