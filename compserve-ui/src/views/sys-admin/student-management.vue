<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">学生账号管理</h2>
        <p class="text-gray-500 mt-1">管理系统学生用户，维护学生基本信息与账号状态</p>
      </div>
    </div>

    <!-- 统计概览卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
      <div class="admin-card stat-card border-l-blue-500">
        <div class="p-6 flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">总学生数</p>
            <p class="mt-2 text-3xl font-bold text-gray-800">{{ total }}</p>
          </div>
          <div class="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
            <i class="fa fa-graduation-cap text-xl"></i>
          </div>
        </div>
      </div>
      <div class="admin-card stat-card border-l-green-500">
        <div class="p-6 flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">正常状态</p>
            <p class="mt-2 text-3xl font-bold text-gray-800">{{ activeCount }}</p>
          </div>
          <div class="w-12 h-12 rounded-xl bg-green-50 text-green-600 flex items-center justify-center">
            <i class="fa fa-check-circle-o text-xl"></i>
          </div>
        </div>
      </div>
      <div class="admin-card stat-card border-l-red-500">
        <div class="p-6 flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">已禁用</p>
            <p class="mt-2 text-3xl font-bold text-gray-800">{{ total - activeCount }}</p>
          </div>
          <div class="w-12 h-12 rounded-xl bg-red-50 text-red-600 flex items-center justify-center">
            <i class="fa fa-user-times text-xl"></i>
          </div>
        </div>
      </div>
    </div>

    <section class="admin-card fade-in">
      <div class="admin-card-header flex flex-col lg:flex-row lg:items-center justify-between gap-4">
        <h3 class="text-xl font-semibold flex items-center gap-2 text-gray-800">
          <div class="p-1.5 bg-admin-100 rounded-lg text-admin-700">
            <i class="fa fa-list-ul"></i>
          </div>
          学生列表
        </h3>
        <div class="flex flex-col lg:flex-row items-center gap-3">
          <input v-model="queryParams.userName" type="text" placeholder="搜索用户名..." class="admin-input w-full lg:w-48" @keyup.enter="handleQuery">
          <input v-model="queryParams.phonenumber" type="text" placeholder="搜索手机号..." class="admin-input w-full lg:w-48" @keyup.enter="handleQuery">
          <el-select v-model="queryParams.status" placeholder="状态" clearable class="w-full lg:w-32 admin-el-select" @change="handleQuery">
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
          <button class="btn-admin-outline px-4 py-2" @click="handleQuery">搜索</button>
          <button class="btn-admin-outline px-4 py-2 bg-gray-50" @click="resetQuery">重置</button>
        </div>
      </div>

      <div class="admin-card-body p-0">
        <div v-if="loading" class="p-8">
          <div class="space-y-4 animate-pulse">
            <div v-for="i in 5" :key="i" class="h-12 bg-gray-100 rounded-lg w-full"></div>
          </div>
        </div>

        <div v-else-if="studentList.length === 0" class="p-12 text-center">
          <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300">
            <i class="fa fa-inbox text-3xl"></i>
          </div>
          <p class="text-gray-500">暂无学生数据</p>
        </div>

        <div v-else class="admin-table-container">
          <table class="admin-table">
            <thead>
              <tr>
                <th scope="col" class="w-12 text-center"><input type="checkbox" @change="toggleAll"></th>
                <th scope="col">学生姓名</th>
                <th scope="col">账号/学号</th>
                <th scope="col">手机号码</th>
                <th scope="col">电子邮箱</th>
                <th scope="col">状态</th>
                <th scope="col" class="text-right">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in studentList" :key="item.userId" class="table-row-hover">
                <td class="text-center"><input type="checkbox" :value="item.userId" v-model="ids"></td>
                <td>
                  <div class="flex items-center gap-3">
                    <div class="w-8 h-8 rounded-full bg-blue-50 text-blue-600 flex items-center justify-center font-bold text-xs shadow-sm">
                      {{ (item.nickName || item.userName).charAt(0) }}
                    </div>
                    <span class="text-sm font-bold text-gray-900">{{ item.nickName }}</span>
                  </div>
                </td>
                <td class="text-sm text-gray-600 font-mono">{{ item.userName }}</td>
                <td class="text-sm text-gray-600">{{ item.phonenumber || '-' }}</td>
                <td class="text-sm text-gray-600">{{ item.email || '-' }}</td>
                <td>
                  <span class="admin-badge" :class="item.status === '0' ? 'admin-badge-success' : 'admin-badge-danger'">
                    {{ item.status === '0' ? '正常' : '停用' }}
                  </span>
                </td>
                <td class="text-right">
                  <div class="flex justify-end gap-1">
                    <button class="table-icon-btn text-blue-600 hover:bg-blue-50" @click="handleUpdate(item)" title="修改"><i class="fa fa-edit"></i></button>
                    <button class="table-icon-btn text-amber-600 hover:bg-amber-50" @click="handleResetPwd(item)" title="重置密码"><i class="fa fa-key"></i></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="px-6 py-4 border-t border-gray-100 bg-gray-50/50">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </section>

    <!-- 修改学生弹窗 -->
    <el-dialog title="修改学生账号" v-model="dialogVisible" width="500px" custom-class="admin-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户账号" prop="userName">
          <input v-model="form.userName" class="admin-input w-full bg-gray-50 text-gray-400" disabled>
        </el-form-item>
        <el-form-item label="学生姓名" prop="nickName">
          <input v-model="form.nickName" class="admin-input w-full" placeholder="输入学生姓名">
        </el-form-item>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="手机号码" prop="phonenumber">
            <input v-model="form.phonenumber" class="admin-input w-full" placeholder="输入手机号">
          </el-form-item>
          <el-form-item label="电子邮箱" prop="email">
            <input v-model="form.email" class="admin-input w-full" placeholder="输入邮箱">
          </el-form-item>
        </div>
        <el-form-item label="账号状态">
          <div class="flex gap-4">
            <label class="flex items-center gap-2 cursor-pointer">
              <input type="radio" v-model="form.status" value="0" class="text-admin-600 focus:ring-admin-500">
              <span class="text-sm text-gray-700">正常</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer">
              <input type="radio" v-model="form.status" value="1" class="text-admin-600 focus:ring-admin-500">
              <span class="text-sm text-gray-700">停用</span>
            </label>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button class="btn-admin-outline px-4 py-2" @click="dialogVisible = false">取消</button>
          <button class="btn-admin-primary px-6 py-2" @click="submitForm">保存修改</button>
        </div>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog title="重置学生密码" v-model="resetPwdVisible" width="400px" custom-class="admin-dialog">
      <el-form ref="resetPwdFormRef" :model="resetPwdForm" :rules="resetPwdRules" label-position="top">
        <el-form-item label="新密码" prop="newPassword">
          <input v-model="resetPwdForm.newPassword" type="password" class="admin-input w-full" placeholder="输入新密码">
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <input v-model="resetPwdForm.confirmPassword" type="password" class="admin-input w-full" placeholder="再次确认新密码">
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button class="btn-admin-outline px-4 py-2" @click="resetPwdVisible = false">取消</button>
          <button class="btn-admin-primary px-6 py-2" @click="submitResetPwd">确认重置</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { listStudents, updateStudent, resetStudentPassword } from '@/api/admin/user'
import { ElMessage } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'

const loading = ref(false)
const studentList = ref([])
const total = ref(0)
const activeCount = computed(() => studentList.value.filter(i => i.status === '0').length)
const ids = ref([])
const dialogVisible = ref(false)
const resetPwdVisible = ref(false)
const currentUserId = ref(null)

const queryParams = reactive({ pageNum: 1, pageSize: 10, userName: null, phonenumber: null, status: null })
const form = ref({ userId: null, userName: '', nickName: '', phonenumber: '', email: '', status: '0' })
const rules = { nickName: [{ required: true, message: '学生姓名必填' }] }
const formRef = ref(null)

const resetPwdForm = ref({ newPassword: '', confirmPassword: '' })
const resetPwdRules = {
  newPassword: [{ required: true, message: '密码必填' }, { min: 6, message: '至少6位' }],
  confirmPassword: [{ validator: (r, v, cb) => v !== resetPwdForm.value.newPassword ? cb(new Error('不一致')) : cb(), trigger: 'blur' }]
}
const resetPwdFormRef = ref(null)

function getList() {
  loading.value = true
  listStudents(queryParams).then(res => {
    studentList.value = res.rows
    total.value = res.total
    loading.value = false
  }).finally(() => loading.value = false)
}

function handleQuery() { queryParams.pageNum = 1; getList(); }
function resetQuery() { queryParams.userName = null; queryParams.phonenumber = null; queryParams.status = null; handleQuery(); }
function toggleAll(e) { ids.value = e.target.checked ? studentList.value.map(i => i.userId) : [] }

function handleUpdate(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

function submitForm() {
  formRef.value.validate(valid => {
    if (!valid) return
    updateStudent(form.value).then(() => {
      ElMessage.success('修改成功')
      dialogVisible.value = false
      getList()
    })
  })
}

function handleResetPwd(row) {
  currentUserId.value = row.userId
  resetPwdForm.value = { newPassword: '', confirmPassword: '' }
  resetPwdVisible.value = true
}

function submitResetPwd() {
  resetPwdFormRef.value.validate(valid => {
    if (!valid) return
    resetStudentPassword(currentUserId.value, resetPwdForm.value.newPassword).then(() => {
      ElMessage.success('重置成功')
      resetPwdVisible.value = false
    })
  })
}

onMounted(getList)
</script>

<style scoped>
.admin-card { @apply bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden; }
.admin-card-header { @apply px-6 py-5 border-b border-gray-100; }
.admin-card-body { @apply p-6; }
.stat-card { @apply border-l-4 transition-transform hover:-translate-y-1; }
.btn-admin-primary { @apply bg-admin-600 hover:bg-admin-700 text-white font-medium rounded-lg transition-colors shadow-sm; }
.btn-admin-outline { @apply border border-gray-200 text-gray-700 hover:bg-gray-50 font-medium rounded-lg transition-colors px-4 py-2; }
.admin-input { @apply h-10 border border-gray-200 rounded-lg px-3 text-sm focus:outline-none focus:border-admin-500 focus:ring-2 focus:ring-admin-100; }
.admin-table { @apply min-w-full; }
.admin-table thead tr { @apply bg-gray-50; }
.admin-table th { @apply px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider; }
.admin-table td { @apply px-6 py-4 border-t border-gray-100; }
.table-row-hover { @apply transition-colors hover:bg-gray-50/50; }
.table-icon-btn { @apply w-8 h-8 flex items-center justify-center rounded-lg transition-colors; }
.admin-badge { @apply inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium; }
.admin-badge-success { @apply bg-green-50 text-green-700; }
.admin-badge-danger { @apply bg-red-50 text-red-700; }
.fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

:deep(.admin-el-select .el-input__wrapper) { @apply rounded-lg border-gray-200 shadow-none ring-0 !important; height: 40px; }
</style>
