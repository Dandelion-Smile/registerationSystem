<template>
  <div class="user-management-page space-y-6">
    <section class="admin-card user-hero fade-in">
      <div class="p-6 md:p-7 flex flex-col lg:flex-row lg:items-center justify-between gap-5">
        <div>
          <h2 class="text-2xl md:text-3xl font-bold text-gray-900 tracking-tight">用户中心管理</h2>
          <p class="text-gray-600 mt-2">统一管理系统用户、角色与状态，提升用户运营效率</p>
          <div class="flex flex-wrap gap-2 mt-4">
            <span class="hero-tag">
              <i class="fa fa-shield mr-1.5"></i>
              权限分级
            </span>
            <span class="hero-tag">
              <i class="fa fa-line-chart mr-1.5"></i>
              活跃监控
            </span>
            <span class="hero-tag">
              <i class="fa fa-bell-o mr-1.5"></i>
              风险预警
            </span>
          </div>
        </div>
        <div class="flex flex-wrap gap-3">
          <button class="btn-admin-outline flex items-center gap-2">
            <i class="fa fa-download"></i>
            导出用户
          </button>
          <button class="btn-admin-primary flex items-center gap-2">
            <i class="fa fa-plus"></i>
            添加新用户
          </button>
        </div>
      </div>
    </section>

    <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-5">
      <div class="admin-card stat-card border-l-indigo-500">
        <div class="p-6 flex items-center justify-between gap-4">
          <div>
            <p class="text-sm font-medium text-gray-500">总用户数</p>
            <p class="mt-2 text-3xl font-bold text-gray-800">1,248</p>
            <p class="mt-1 text-sm text-green-600 flex items-center font-medium">
              <i class="fa fa-arrow-up mr-1"></i>
              12.5% 月增长
            </p>
          </div>
          <div class="stat-icon bg-indigo-50 text-indigo-600">
            <i class="fa fa-users text-xl"></i>
          </div>
        </div>
      </div>

      <div class="admin-card stat-card border-l-orange-500">
        <div class="p-6 flex items-center justify-between gap-4">
          <div>
            <p class="text-sm font-medium text-gray-500">活跃用户</p>
            <p class="mt-2 text-3xl font-bold text-gray-800">842</p>
            <p class="mt-1 text-sm text-green-600 flex items-center font-medium">
              <i class="fa fa-arrow-up mr-1"></i>
              8.3% 月增长
            </p>
          </div>
          <div class="stat-icon bg-orange-50 text-orange-600">
            <i class="fa fa-line-chart text-xl"></i>
          </div>
        </div>
      </div>

      <div class="admin-card stat-card border-l-green-500">
        <div class="p-6 flex items-center justify-between gap-4">
          <div>
            <p class="text-sm font-medium text-gray-500">管理员</p>
            <p class="mt-2 text-3xl font-bold text-gray-800">28</p>
            <p class="mt-1 text-sm text-gray-500">占总用户 2.2%</p>
          </div>
          <div class="stat-icon bg-green-50 text-green-600">
            <i class="fa fa-shield text-xl"></i>
          </div>
        </div>
      </div>

      <div class="admin-card stat-card border-l-purple-500">
        <div class="p-6 flex items-center justify-between gap-4">
          <div>
            <p class="text-sm font-medium text-gray-500">今日新增</p>
            <p class="mt-2 text-3xl font-bold text-gray-800">15</p>
            <p class="mt-1 text-sm text-red-500 flex items-center font-medium">
              <i class="fa fa-arrow-down mr-1"></i>
              2.1% 较昨日
            </p>
          </div>
          <div class="stat-icon bg-purple-50 text-purple-600">
            <i class="fa fa-user-plus text-xl"></i>
          </div>
        </div>
      </div>
    </div>

    <section class="admin-card fade-in">
      <div class="admin-card-header flex flex-col xl:flex-row xl:items-center justify-between gap-4">
        <h3 class="text-xl font-semibold flex items-center gap-2 text-gray-800">
          <div class="p-1.5 bg-admin-100 rounded-lg text-admin-700">
            <i class="fa fa-users"></i>
          </div>
          用户列表
        </h3>
        <div class="flex flex-col lg:flex-row items-center gap-3 w-full xl:w-auto">
          <div class="relative w-full lg:w-72">
            <input type="text" placeholder="搜索用户姓名或邮箱..." class="admin-input pl-10 w-full">
            
          </div>
          <select class="admin-select w-full lg:w-auto">
            <option>全部角色</option>
            <option>普通用户</option>
            <option>管理员</option>
          </select>
          <select class="admin-select w-full lg:w-auto">
            <option>全部状态</option>
            <option>正常</option>
            <option>禁用</option>
          </select>
        </div>
      </div>

      <div class="admin-card-body p-0">
        <div v-if="loading" class="p-8">
          <div class="space-y-4">
            <div v-for="i in 5" :key="i" class="flex items-center gap-4 animate-pulse">
              <div class="w-10 h-10 bg-gray-200 rounded-full"></div>
              <div class="flex-1 space-y-2">
                <div class="h-4 bg-gray-200 rounded w-1/4"></div>
                <div class="h-3 bg-gray-200 rounded w-1/3"></div>
              </div>
              <div class="w-20 h-6 bg-gray-200 rounded"></div>
            </div>
          </div>
        </div>

        <div v-else-if="userList.length === 0" class="p-12 text-center">
          <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300">
            <i class="fa fa-user-times text-3xl"></i>
          </div>
          <p class="text-gray-500">未找到相关用户</p>
        </div>

        <div v-else class="admin-table-container border-0 shadow-none rounded-none">
          <table class="admin-table">
            <thead>
              <tr>
                <th scope="col">用户</th>
                <th scope="col">角色</th>
                <th scope="col">最后登录</th>
                <th scope="col">活跃度</th>
                <th scope="col">状态</th>
                <th scope="col" class="text-right">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in userList" :key="user.id" class="table-row-hover">
                <td>
                  <div class="flex items-center">
                    <div class="w-10 h-10 rounded-full flex items-center justify-center text-white font-bold text-sm shadow-sm" :class="user.avatarColor">
                      {{ user.name.charAt(0) }}
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-bold text-gray-900">{{ user.name }}</div>
                      <div class="text-xs text-gray-500">{{ user.email }}</div>
                    </div>
                  </div>
                </td>
                <td>
                  <span class="admin-badge" :class="user.role === '管理员' ? 'admin-badge-primary' : 'admin-badge-gray'">{{ user.role }}</span>
                </td>
                <td>
                  <div class="text-sm text-gray-500">{{ user.lastLogin }}</div>
                </td>
                <td>
                  <div class="flex items-center">
                    <div class="w-16 bg-gray-100 rounded-full h-1.5 overflow-hidden">
                      <div class="h-1.5 rounded-full" :class="getActivityColor(user.activity)" :style="{ width: user.activity + '%' }"></div>
                    </div>
                    <span class="ml-2 text-xs text-gray-500">{{ getActivityLabel(user.activity) }}</span>
                  </div>
                </td>
                <td>
                  <span class="admin-badge" :class="user.status === '正常' ? 'admin-badge-success' : 'admin-badge-danger'">{{ user.status }}</span>
                </td>
                <td class="text-right">
                  <div class="flex justify-end gap-2">
                    <button class="table-icon-btn table-icon-btn-view" title="查看">
                      <i class="fa fa-eye"></i>
                    </button>
                    <button class="table-icon-btn table-icon-btn-edit" title="编辑">
                      <i class="fa fa-edit"></i>
                    </button>
                    <button class="table-icon-btn table-icon-btn-ban" title="禁用">
                      <i class="fa fa-ban"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="!loading && userList.length > 0" class="flex items-center justify-between px-6 py-4 border-t border-gray-100 bg-gray-50/50 rounded-b-xl">
        <div class="text-sm text-gray-500">显示 1 至 {{ userList.length }} 条，共 {{ userList.length }} 条</div>
        <div class="flex gap-2">
          <button class="w-8 h-8 flex items-center justify-center border border-gray-200 rounded-lg bg-white text-gray-400 hover:bg-gray-50 disabled:opacity-50 transition-colors" disabled>
            <i class="fa fa-chevron-left text-xs"></i>
          </button>
          <button class="w-8 h-8 flex items-center justify-center border border-admin-500 bg-admin-50 text-admin-600 font-medium rounded-lg shadow-sm">1</button>
          <button class="w-8 h-8 flex items-center justify-center border border-gray-200 rounded-lg bg-white text-gray-400 hover:bg-gray-50 disabled:opacity-50 transition-colors" disabled>
            <i class="fa fa-chevron-right text-xs"></i>
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const loading = ref(true)
const userList = ref([])

onMounted(() => {
  setTimeout(() => {
    userList.value = [
      {
        id: 1,
        name: '张三',
        email: 'zhangsan@example.com',
        role: '普通用户',
        lastLogin: '2023-11-15 14:23',
        activity: 75,
        status: '正常',
        avatarColor: 'bg-blue-500'
      },
      {
        id: 2,
        name: '李四',
        email: 'lisi@example.com',
        role: '管理员',
        lastLogin: '2023-11-15 09:47',
        activity: 45,
        status: '正常',
        avatarColor: 'bg-purple-500'
      },
       {
        id: 3,
        name: '王五',
        email: 'wangwu@example.com',
        role: '普通用户',
        lastLogin: '2023-11-14 18:20',
        activity: 90,
        status: '正常',
        avatarColor: 'bg-green-500'
      }
    ]
    loading.value = false
  }, 800)
})

const getActivityColor = (val) => {
  if (val >= 80) return 'bg-green-500'
  if (val >= 50) return 'bg-blue-500'
  return 'bg-yellow-500'
}

const getActivityLabel = (val) => {
  if (val >= 80) return '高'
  if (val >= 50) return '中'
  return '一般'
}
</script>

<style scoped lang="scss">
.admin-card {
  @apply bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden;
}

.admin-card-header {
  @apply px-6 py-5 border-b border-gray-100;
}

.admin-card-body {
  @apply p-6;
}

.btn-admin-primary {
  @apply bg-gradient-to-r from-primary-700 to-primary-600 hover:from-primary-800 hover:to-primary-700 text-white font-semibold py-2.5 px-4 rounded-xl transition-all duration-300 shadow-md;
}

.btn-admin-outline {
  @apply border border-gray-200 text-gray-700 hover:bg-gray-50 font-semibold py-2.5 px-4 rounded-xl transition-all duration-300;
}

.admin-input {
  @apply h-10 border border-gray-200 rounded-xl px-3 text-sm text-gray-700 placeholder:text-gray-400 focus:outline-none focus:border-primary-400 focus:ring-4 focus:ring-primary-100 transition-all bg-white;
}

.admin-select {
  @apply h-10 border border-gray-200 rounded-xl px-3 text-sm text-gray-700 bg-white focus:outline-none focus:border-primary-400 focus:ring-4 focus:ring-primary-100 transition-all;
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

.admin-badge-primary {
  @apply bg-primary-50 text-primary-700;
}

.admin-badge-success {
  @apply bg-green-50 text-green-700;
}

.admin-badge-danger {
  @apply bg-red-50 text-red-700;
}

.admin-badge-gray {
  @apply bg-gray-100 text-gray-600;
}

.user-hero {
  @apply bg-gradient-to-br from-primary-50 via-white to-admin-50/60 border-primary-100;
}

.hero-tag {
  @apply inline-flex items-center px-2.5 py-1 rounded-lg bg-white/90 border border-primary-100 text-xs text-primary-700 font-medium;
}

.stat-card {
  @apply border-l-4 hover:-translate-y-1 hover:shadow-md transition-all duration-300;
}

.stat-icon {
  @apply w-12 h-12 rounded-xl flex items-center justify-center;
}

.table-row-hover {
  @apply transition-colors;
}

.table-row-hover:hover td {
  @apply bg-primary-50/30;
}

.table-icon-btn {
  @apply w-8 h-8 flex items-center justify-center rounded-lg transition-all duration-200;
}

.table-icon-btn-view {
  @apply text-primary-600 hover:bg-primary-50;
}

.table-icon-btn-edit {
  @apply text-amber-600 hover:bg-amber-50;
}

.table-icon-btn-ban {
  @apply text-red-600 hover:bg-red-50;
}

.fade-in {
  animation: fadeIn 0.4s ease-out backwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
