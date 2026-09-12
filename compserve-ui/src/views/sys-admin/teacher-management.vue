<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">教师账号管理</h2>
        <p class="text-gray-500 mt-1">统一管理系统教师账号，支持教师详细信息的维护</p>
      </div>
      <div class="flex gap-3">
        <button class="btn-admin-primary flex items-center gap-2" @click="handleAdd">
          <i class="fa fa-plus"></i>
          <span>新增教师</span>
        </button>
        <button class="btn-admin-outline flex items-center gap-2" @click="$refs.uploadInput.click()">
          <i class="fa fa-upload"></i>
          <span>Excel导入</span>
        </button>
        <button class="btn-admin-outline flex items-center gap-2" @click="downloadTemplate">
          <i class="fa fa-download"></i>
          <span>下载模板</span>
        </button>
        <input ref="uploadInput" type="file" accept=".xlsx,.xls" class="hidden" @change="handleExcelChange">
      </div>
    </div>

  <!-- 导入中遮罩 -->
  <div v-if="importing" class="fixed inset-0 bg-black/50 flex items-center justify-center z-[9999]">
    <div class="bg-white rounded-xl p-8 flex flex-col items-center gap-4 shadow-2xl">
      <div class="w-12 h-12 border-4 border-admin-600 border-t-transparent rounded-full animate-spin"></div>
      <p class="text-lg font-medium text-gray-800">正在导入中...</p>
      <p class="text-sm text-gray-500">请稍候，系统正在处理您的导入请求</p>
    </div>
  </div>

  <!-- 导入失败弹窗 -->
  <el-dialog v-model="importFailVisible" title="导入失败详情" width="800px" custom-class="admin-dialog" :close-on-click-modal="false">
    <div class="max-h-[400px] overflow-y-auto">
      <div class="space-y-2">
        <div v-for="(reason, index) in importFailReasons" :key="index" class="text-sm text-red-600 bg-red-50 px-4 py-2 rounded-lg">
          {{ reason }}
        </div>
      </div>
    </div>
    <template #footer>
      <div class="flex justify-end gap-3">
        <button class="btn-admin-primary px-6 py-2" @click="importFailVisible = false">关闭</button>
      </div>
    </template>
  </el-dialog>

  <!-- Excel导入预览 -->
    <el-dialog v-model="importPreviewVisible" title="Excel导入预览" width="900px" custom-class="admin-dialog">
      <div class="max-h-[400px] overflow-y-auto border rounded-lg">
        <table class="admin-table">
          <thead class="sticky top-0 z-10">
            <tr>
              <th class="bg-gray-50">账号</th>
              <th class="bg-gray-50">昵称</th>
              <th class="bg-gray-50">手机号码</th>
              <th class="bg-gray-50">邮箱</th>
              <th class="bg-gray-50">密码</th>
              <th class="bg-gray-50">状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, i) in importPreview" :key="i">
              <td class="py-2 px-4 border-t">{{ item.userName }}</td>
              <td class="py-2 px-4 border-t">{{ item.nickName }}</td>
              <td class="py-2 px-4 border-t">{{ item.phonenumber }}</td>
              <td class="py-2 px-4 border-t">{{ item.email }}</td>
              <td class="py-2 px-4 border-t text-gray-500">{{ item.password }}</td>
              <td class="py-2 px-4 border-t">{{ item.status === '0' ? '正常' : '停用' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button class="btn-admin-outline px-4 py-2" @click="importPreviewVisible = false">取消</button>
          <button class="btn-admin-primary px-6 py-2" @click="importExcel" :loading="importing">确认导入</button>
        </div>
      </template>
    </el-dialog>

    <!-- 教师列表 -->
    <section class="admin-card fade-in">
      <div class="admin-card-header flex flex-col lg:flex-row lg:items-center justify-between gap-4">
        <h3 class="text-xl font-semibold flex items-center gap-2 text-gray-800">
          <div class="p-1.5 bg-admin-100 rounded-lg text-admin-700">
            <i class="fa fa-list-ul"></i>
          </div>
          教师账号列表
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

        <div v-else class="admin-table-container">
          <table class="admin-table">
            <thead>
              <tr>
                <th scope="col" class="w-12 text-center"><input type="checkbox" @change="toggleAll"></th>
                <th scope="col">教师信息</th>
                <th scope="col">账号</th>
                <th scope="col">手机号码</th>
                <th scope="col">状态</th>
                <th scope="col">创建时间</th>
                <th scope="col" class="text-right">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in teacherList" :key="item.userId" class="table-row-hover">
                <td class="text-center"><input type="checkbox" :value="item.userId" v-model="ids"></td>
                <td>
                  <div class="flex items-center gap-3">
                    <div class="w-8 h-8 rounded-full bg-admin-100 text-admin-600 flex items-center justify-center font-bold text-xs">
                      {{ (item.nickName || item.userName).charAt(0) }}
                    </div>
                    <span class="text-sm font-bold text-gray-900">{{ item.nickName }}</span>
                  </div>
                </td>
                <td class="text-sm text-gray-600">{{ item.userName }}</td>
                <td class="text-sm text-gray-600">{{ item.phonenumber || '-' }}</td>
                <td>
                  <el-switch
                    v-model="item.status"
                    active-value="0"
                    inactive-value="1"
                    @change="value => handleStatusChange(item, value)"
                  />
                </td>
                <td class="text-xs text-gray-500">{{ parseTime(item.createTime, '{y}-{m}-{d}') }}</td>
                <td class="text-right">
                  <div class="flex justify-end gap-1">
                    <button class="table-icon-btn text-blue-600 hover:bg-blue-50" @click="handleUpdate(item)" title="修改"><i class="fa fa-edit"></i></button>
                    <button class="table-icon-btn text-amber-600 hover:bg-amber-50" @click="handleResetPwd(item)" title="重置密码"><i class="fa fa-key"></i></button>
                    <button class="table-icon-btn text-red-600 hover:bg-red-50" @click="handleDelete(item)" title="删除"><i class="fa fa-trash"></i></button>
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

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" title="预览批量创建结果" width="700px" custom-class="admin-dialog">
      <div class="max-h-[400px] overflow-y-auto border rounded-lg">
        <table class="admin-table">
          <thead class="sticky top-0 z-10">
            <tr><th class="bg-gray-50">账号</th><th class="bg-gray-50">初始密码</th><th class="bg-gray-50">昵称</th></tr>
          </thead>
          <tbody>
            <tr v-for="(acc, i) in previewAccounts" :key="i">
              <td class="py-2 px-4 border-t">{{ acc.account }}</td>
              <td class="py-2 px-4 border-t text-gray-500">{{ acc.password }}</td>
              <td class="py-2 px-4 border-t">{{ acc.nickname }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button class="btn-admin-outline px-4 py-2" @click="previewVisible = false">取消</button>
          <button class="btn-admin-primary px-6 py-2" @click="confirmBatchCreate">确认创建</button>
        </div>
      </template>
    </el-dialog>

    <!-- 修改教师弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="800px" custom-class="admin-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="grid grid-cols-2 gap-x-8 gap-y-4">
          <!-- 左侧列 -->
          <div class="space-y-4">
            <el-form-item label="姓名" prop="nickName">
              <input v-model="form.nickName" class="admin-input w-full bg-gray-50" placeholder="请输入姓名">
            </el-form-item>
            <el-form-item label="性别" prop="sex">
              <div class="flex gap-4">
                <label class="flex items-center gap-2 cursor-pointer bg-gray-50 px-4 py-2 rounded-lg border border-gray-200 w-full justify-center">
                  <input type="radio" v-model="form.sex" value="0" class="text-admin-600 focus:ring-admin-500">
                  <span class="text-sm font-medium">男</span>
                </label>
                <label class="flex items-center gap-2 cursor-pointer bg-gray-50 px-4 py-2 rounded-lg border border-gray-200 w-full justify-center">
                  <input type="radio" v-model="form.sex" value="1" class="text-admin-600 focus:ring-admin-500">
                  <span class="text-sm font-medium">女</span>
                </label>
              </div>
            </el-form-item>
            <el-form-item label="联系电话" prop="phonenumber">
              <div class="relative">
                <input v-model="form.phonenumber" class="admin-input w-full pl-9 bg-gray-50" placeholder="请输入联系电话">
              </div>
            </el-form-item>
            <el-form-item label="电子邮箱" prop="email">
              <div class="relative">
                <i class="fa fa-envelope-o absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"></i>
                <input v-model="form.email" class="admin-input w-full pl-9 bg-gray-50" placeholder="请输入电子邮箱">
              </div>
            </el-form-item>
            <!-- 隐藏的用户名和密码，如果是新增则需要 -->
            <el-form-item label="用户账号" prop="userName" v-if="!form.userId">
              <input v-model="form.userName" class="admin-input w-full bg-gray-50" placeholder="请输入系统登录账号">
            </el-form-item>
            <el-form-item label="密码" prop="password" v-if="!form.userId">
              <input v-model="form.password" type="password" class="admin-input w-full bg-gray-50" placeholder="请输入登录密码">
            </el-form-item>
          </div>

          <!-- 右侧列 -->
          <div class="space-y-4">
            <el-form-item label="单位" prop="workUnit">
              <input v-model="form.workUnit" class="admin-input w-full bg-gray-50" placeholder="请输入工作单位">
            </el-form-item>
            <el-form-item label="职称" prop="title">
              <input v-model="form.title" class="admin-input w-full bg-gray-50" placeholder="请输入职称">
            </el-form-item>
            <el-form-item label="职务" prop="position">
              <input v-model="form.position" class="admin-input w-full bg-gray-50" placeholder="请输入职务">
            </el-form-item>
            <el-form-item label="政治面貌" prop="politicalStatus">
              <el-select v-model="form.politicalStatus" placeholder="请选择" class="w-full admin-el-select bg-gray-50">
                <el-option label="中共党员" value="中共党员"></el-option>
                <el-option label="中共预备党员" value="中共预备党员"></el-option>
                <el-option label="共青团员" value="共青团员"></el-option>
                <el-option label="群众" value="群众"></el-option>
                <el-option label="其他党派" value="其他党派"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="出生年月" prop="birthDate">
              <el-date-picker 
                v-model="form.birthDate" 
                type="month" 
                placeholder="选择年月" 
                class="w-full admin-el-select"
                value-format="YYYY-MM"
              />
            </el-form-item>
          </div>
        </div>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3 border-t border-gray-100 pt-4 mt-2">
          <button class="btn-admin-outline px-6 py-2.5" @click="dialogVisible = false">取消</button>
          <button class="btn-admin-primary px-8 py-2.5 shadow-md" @click="submitForm">确定保存</button>
        </div>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog title="重置教师密码" v-model="resetPwdVisible" width="400px" custom-class="admin-dialog">
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
import { ref, reactive, onMounted } from 'vue'
import { listTeachers, insertTeacher, updateTeacher, deleteTeachers, resetTeacherPassword, batchCreateTeachers, getTeacherInfo, changeTeacherStatus, importTeachers } from '@/api/admin/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'
import * as XLSX from 'xlsx'

const loading = ref(false)
const teacherList = ref([])
const total = ref(0)
const ids = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const resetPwdVisible = ref(false)
const previewVisible = ref(false)
const previewAccounts = ref([])
const previewLoading = ref(false)
const creating = ref(false)
const excelFile = ref(null)
const importPreviewVisible = ref(false)
const importPreview = ref([])
const importing = ref(false)
const importFailVisible = ref(false)
const importFailReasons = ref([])

const queryParams = reactive({ pageNum: 1, pageSize: 10, userName: null, phonenumber: null, status: null })
const batchForm = reactive({ accountPrefix: 'teacher_', count: 10, defaultPassword: '', nicknamePrefix: '教师' })
const batchFormRef = ref(null)
const batchRules = { accountPrefix: [{ required: true, message: '账号前缀必填' }], count: [{ required: true, message: '数量必填' }] }

const form = reactive({ 
  userId: null, 
  userName: '', 
  nickName: '', 
  sex: '0',
  phonenumber: '', 
  email: '', 
  password: '', 
  status: '0',
  workUnit: '',
  title: '',
  position: '',
  politicalStatus: '',
  birthDate: ''
})
const formRef = ref(null)
const phonePattern = /^1[3-9]\d{9}$/
const rules = {
  userName: [{ required: true, message: '用户名必填', trigger: 'blur' }],
  nickName: [{ required: true, message: '昵称必填', trigger: 'blur' }],
  phonenumber: [
    { required: true, message: '联系电话必填', trigger: 'blur' },
    { pattern: phonePattern, message: '请输入正确的11位手机号', trigger: ['blur', 'change'] }
  ],
  email: [
    { type: 'email', message: '请输入正确的电子邮箱', trigger: ['blur', 'change'] }
  ],
  password: [{ required: true, message: '密码必填', trigger: 'blur' }]
}

const resetPwdForm = reactive({ userId: null, newPassword: '', confirmPassword: '' })
const resetPwdFormRef = ref(null)
const resetPwdRules = {
  newPassword: [{ required: true, message: '密码必填' }, { min: 6, message: '至少6位' }],
  confirmPassword: [{ validator: (r, v, cb) => v !== resetPwdForm.newPassword ? cb(new Error('不一致')) : cb(), trigger: 'blur' }]
}

function normalizeStatus(status) {
  return String(status) === '1' ? '1' : '0'
}

function getList() {
  loading.value = true
  listTeachers(queryParams).then(res => {
    teacherList.value = (res.rows || []).map(item => ({
      ...item,
      status: normalizeStatus(item.status)
    }))
    total.value = res.total
    loading.value = false
  }).finally(() => loading.value = false)
}

function handleQuery() { queryParams.pageNum = 1; getList(); }
function resetQuery() { queryParams.userName = null; queryParams.phonenumber = null; queryParams.status = null; handleQuery(); }
function toggleAll(e) { ids.value = e.target.checked ? teacherList.value.map(i => i.userId) : [] }

function handleAdd() { dialogTitle.value = '新增教师'; resetForm(); dialogVisible.value = true; }
function handleUpdate(row) {
  getTeacherInfo(row.userId).then(res => {
    Object.assign(form, res.data)
    dialogTitle.value = '修改教师'
    dialogVisible.value = true
  })
}

function submitForm() {
  formRef.value.validate(valid => {
    if (!valid) return
    const api = form.userId ? updateTeacher : insertTeacher
    const payload = {
      userId: form.userId,
      userName: form.userName,
      nickName: form.nickName,
      sex: form.sex,
      phonenumber: form.phonenumber,
      email: form.email,
      password: form.password,
      status: form.status,
      workUnit: form.workUnit,
      title: form.title,
      position: form.position,
      politicalStatus: form.politicalStatus,
      birthDate: form.birthDate
    }
    api(payload).then(() => {
      ElMessage.success('操作成功')
      dialogVisible.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  const uids = row.userId ? [row.userId] : ids.value
  ElMessageBox.confirm('确认删除选中的教师账号？', '警告', { type: 'warning' }).then(() => {
    deleteTeachers(uids).then(() => { getList(); ElMessage.success('删除成功'); })
  })
}

function handleStatusChange(row, value) {
  const nextStatus = normalizeStatus(value)
  const previousStatus = nextStatus === '0' ? '1' : '0'
  row.status = nextStatus
  changeTeacherStatus(row.userId, nextStatus).then(() => {
    ElMessage.success(nextStatus === '0' ? '已设为正常' : '已设为停用')
    getList()
  }).catch(() => {
    row.status = previousStatus
    ElMessage.error('状态更新失败')
  })
}

function handleResetPwd(row) {
  resetPwdForm.userId = row.userId
  resetPwdForm.newPassword = ''
  resetPwdForm.confirmPassword = ''
  resetPwdVisible.value = true
}

function submitResetPwd() {
  resetPwdFormRef.value.validate(valid => {
    if (!valid) return
    resetTeacherPassword(resetPwdForm.userId, resetPwdForm.newPassword).then(() => {
      ElMessage.success('密码重置成功')
      resetPwdVisible.value = false
    })
  })
}

function previewBatchCreate() {
  const accs = []
  for (let i = 1; i <= batchForm.count; i++) {
    const acc = batchForm.accountPrefix + String(i).padStart(3, '0')
    accs.push({ account: acc, password: batchForm.defaultPassword || (acc + '123456'), nickname: batchForm.nicknamePrefix + String(i).padStart(3, '0') })
  }
  previewAccounts.value = accs
  previewVisible.value = true
}

function confirmBatchCreate() {
  creating.value = true
  batchCreateTeachers(batchForm).then(res => {
    ElMessage.success(`成功创建 ${res.data.successCount} 个账号`)
    previewVisible.value = false
    getList()
  }).finally(() => creating.value = false)
}

function resetForm() { 
  Object.assign(form, { 
    userId: null, 
    userName: '', 
    nickName: '', 
    sex: '0',
    phonenumber: '', 
    email: '', 
    password: '', 
    status: '0',
    workUnit: '',
    title: '',
    position: '',
    politicalStatus: '',
    birthDate: ''
  }) 
}

// 处理Excel文件变化
function handleExcelChange(e) {
  try {
    const file = e.target.files[0]
    if (!file) return
    
    console.log('选择的文件:', file)
    
    excelFile.value = file
    // 解析Excel文件
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const data = new Uint8Array(e.target.result)
        const workbook = XLSX.read(data, { type: 'array' })
        const firstSheetName = workbook.SheetNames[0]
        const worksheet = workbook.Sheets[firstSheetName]
        const jsonData = XLSX.utils.sheet_to_json(worksheet)
        
        console.log('解析的数据:', jsonData)
        
        // 处理导入预览数据
        const previewData = jsonData.map((item, index) => {
          // 处理性别字段，将文本转换为数字
          let sex = '0'; // 默认男
          if (item['性别（男/女）'] === '女') {
            sex = '1';
          } else if (item['性别（男/女）'] === '男') {
            sex = '0';
          }
          
          return {
            rowIndex: index + 2, // 行号（Excel从1开始，跳过表头）
            userName: item['用户账号'] || '',
            nickName: item['姓名'] || '',
            sex: sex,
            phonenumber: item['联系电话'] || '',
            email: item['电子邮箱'] || '',
            password: item['密码'] || (item['用户账号'] + '123456'),
            status: '0', // 默认正常状态
            workUnit: item['单位'] || '',
            title: item['职称'] || '',
            position: item['职务'] || '',
            politicalStatus: item['政治面貌（中共党员/中共预备党员/群众/其他党派）'] || '',
            birthDate: item['出生年月'] || ''
          }
        })
        
        importPreview.value = previewData
        
        console.log('预览数据:', importPreview.value)
        
        // 检查是否有重复数据
        const hasDuplicates = previewData.some(item => item.isDuplicate)
        if (hasDuplicates) {
          ElMessage.warning('检测到重复数据（姓名和联系电话都相同），已在预览中标记，请确认是否继续导入')
        }
        
        importPreviewVisible.value = true
        console.log('显示预览对话框')
      } catch (error) {
        console.error('解析Excel文件失败:', error)
        ElMessage.error('解析Excel文件失败，请检查文件格式')
      }
    }
    reader.onerror = (error) => {
      console.error('读取文件失败:', error)
      ElMessage.error('读取文件失败，请检查文件')
    }
    reader.readAsArrayBuffer(file)
    
    // 重置input，允许重复选择同一文件
    setTimeout(() => {
      e.target.value = ''
      console.log('重置input值')
    }, 100)
  } catch (error) {
    console.error('处理Excel文件失败:', error)
    ElMessage.error('处理Excel文件失败，请检查文件')
  }
}

// 导入Excel
function importExcel() {
  if (!excelFile.value) {
    ElMessage.warning('请选择Excel文件')
    return
  }
  
  importing.value = true
  
  // 构建FormData
  const formData = new FormData()
  formData.append('file', excelFile.value)
  
  importTeachers(formData).then(response => {
    console.log('导入响应:', response)
    // 检查response结构，确保正确获取successCount和failCount
    let successCount = 0
    let failCount = 0
    let failReasons = []
    
    // 处理不同的响应结构
    if (response && typeof response === 'object') {
      if (response.successCount !== undefined) {
        // 直接从response中获取
        successCount = response.successCount
        failCount = response.failCount || 0
        failReasons = response.failReasons || []
      } else if (response.data && typeof response.data === 'object' && response.data.successCount !== undefined) {
        // 从response.data中获取
        successCount = response.data.successCount
        failCount = response.data.failCount || 0
        failReasons = response.data.failReasons || []
      }
    }
    
    importing.value = false
    
    if (failCount > 0 && failReasons.length > 0) {
      // 显示失败详情弹窗
      importFailReasons.value = failReasons
      importFailVisible.value = true
    }
    
    ElMessage.success(`成功导入${successCount}个教师账号，失败${failCount}个`)
    importPreviewVisible.value = false
    getList()
    // 重置
    excelFile.value = null
    importPreview.value = []
  }).catch(error => {
    console.error('导入失败:', error)
    console.error('错误详情:', error.response)
    importing.value = false
    ElMessage.error('导入失败，请检查网络连接或文件格式')
  })
}

// 下载模板
function downloadTemplate() {
  console.log('开始下载模板')
  try {
    // 创建模板数据
    const templateData = [
      {
        '姓名': '教师001',
        '性别（男/女）': '男',
        '联系电话': '13800138001',
        '电子邮箱': 'teacher001@example.com',
        '用户账号': 'teacher_001',
        '密码': 'teacher_001123456',
        '单位': '示例大学',
        '职称': '教授',
        '职务': '系主任',
        '政治面貌（中共党员/中共预备党员/群众/其他党派）': '中共党员',
        '出生年月': '1980-01'
      },
      {
        '姓名': '教师002',
        '性别（男/女）': '女',
        '联系电话': '13800138002',
        '电子邮箱': 'teacher002@example.com',
        '用户账号': 'teacher_002',
        '密码': 'teacher_002123456',
        '单位': '示例学院',
        '职称': '副教授',
        '职务': '教师',
        '政治面貌（中共党员/中共预备党员/群众/其他党派）': '群众',
        '出生年月': '1985-06'
      }
    ]
    
    console.log('模板数据创建成功', templateData)
    
    // 创建工作簿
    const workbook = XLSX.utils.book_new()
    const worksheet = XLSX.utils.json_to_sheet(templateData)
    XLSX.utils.book_append_sheet(workbook, worksheet, '教师账号模板')
    
    console.log('工作簿创建成功')
    
    // 下载文件
    XLSX.writeFile(workbook, '教师账号导入模板.xlsx')
    console.log('文件下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    ElMessage.error('下载模板失败，请检查网络连接')
  }
}

onMounted(getList)
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
.fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

:deep(.admin-el-number .el-input__wrapper) { @apply rounded-lg border-gray-200 shadow-none ring-0 !important; }
:deep(.admin-el-select .el-input__wrapper) { @apply rounded-lg border-gray-200 shadow-none ring-0 !important; height: 40px; }
</style>
