<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>教师账号管理</span>
        </div>
      </template>

      <!-- 批量创建教师账号 -->
      <el-card shadow="never" class="batch-create-card">
        <template #header>
          <span>批量创建教师账号</span>
        </template>
        <el-form :model="batchForm" :rules="batchRules" ref="batchFormRef" label-width="120px">
          <el-form-item label="账号前缀" prop="accountPrefix">
            <el-input 
              v-model="batchForm.accountPrefix" 
              placeholder="如：teacher_"
              style="width: 300px"
            />
            <div class="form-tip">系统将自动生成：teacher_001, teacher_002 ...</div>
          </el-form-item>
          
          <el-form-item label="创建数量" prop="count">
            <el-input-number 
              v-model="batchForm.count" 
              :min="1" 
              :max="100"
              style="width: 300px"
            />
            <div class="form-tip">建议一次不超过100个账号</div>
          </el-form-item>
          
          <el-form-item label="初始密码">
            <el-input 
              v-model="batchForm.defaultPassword" 
              type="password"
              placeholder="不填则使用默认规则：账号+123456"
              style="width: 300px"
            />
          </el-form-item>
          
          <el-form-item label="昵称前缀（可选）">
            <el-input 
              v-model="batchForm.nicknamePrefix" 
              placeholder="如：教师（将生成：教师001、教师002...）"
              style="width: 300px"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="previewBatchCreate" :loading="previewLoading">预览</el-button>
            <el-button type="success" @click="confirmBatchCreate" :loading="creating">确认创建</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 教师列表 -->
      <div class="table-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="用户名称" prop="userName">
            <el-input v-model="queryParams.userName" placeholder="请输入用户名称" clearable style="width: 240px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="手机号码" prop="phonenumber">
            <el-input v-model="queryParams.phonenumber" placeholder="请输入手机号码" clearable style="width: 240px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="用户状态" clearable style="width: 240px">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="teacherList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="用户编号" align="center" prop="userId" />
          <el-table-column label="用户名称" align="center" prop="userName" :show-overflow-tooltip="true" />
          <el-table-column label="用户昵称" align="center" prop="nickName" :show-overflow-tooltip="true" />
          <el-table-column label="手机号码" align="center" prop="phonenumber" width="120" />
          <el-table-column label="状态" align="center">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                active-value="0"
                inactive-value="1"
                @change="handleStatusChange(scope.row)"
              ></el-switch>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center" prop="createTime" width="160">
            <template #default="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
              <el-button link type="primary" icon="Key" @click="handleResetPwd(scope.row)">重置密码</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </el-card>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" title="预览批量创建结果" width="800px">
      <el-table :data="previewAccounts" border>
        <el-table-column prop="account" label="账号" width="200" />
        <el-table-column prop="password" label="初始密码" width="200" />
        <el-table-column prop="nickname" label="昵称" width="200" />
      </el-table>
      <template #footer>
        <el-button @click="previewVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchCreate">确认创建</el-button>
      </template>
    </el-dialog>

    <!-- 添加/修改教师弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名称" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入用户名称" />
        </el-form-item>
        <el-form-item label="用户昵称" prop="nickName">
          <el-input v-model="form.nickName" placeholder="请输入用户昵称" />
        </el-form-item>
        <el-form-item label="手机号码" prop="phonenumber">
          <el-input v-model="form.phonenumber" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.userId">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog title="重置密码" v-model="resetPwdVisible" width="400px" append-to-body>
      <el-form ref="resetPwdFormRef" :model="resetPwdForm" :rules="resetPwdRules" label-width="100px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetPwdForm.newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResetPwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TeacherManagement">
import { ref, reactive, onMounted } from 'vue'
import { listTeachers, insertTeacher, updateTeacher, deleteTeachers, resetTeacherPassword, batchCreateTeachers, getTeacherInfo, changeTeacherStatus } from '@/api/admin/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'

const loading = ref(false)
const showSearch = ref(true)
const teacherList = ref([])
const total = ref(0)
const single = ref(true)
const multiple = ref(true)
const ids = ref([])

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  userName: null,
  phonenumber: null,
  status: null
})

// 批量创建表单
const batchForm = reactive({
  accountPrefix: 'teacher_',
  count: 10,
  defaultPassword: '',
  nicknamePrefix: '教师'
})
const batchFormRef = ref(null)
const previewLoading = ref(false)
const creating = ref(false)
const previewVisible = ref(false)
const previewAccounts = ref([])

const batchRules = {
  accountPrefix: [
    { required: true, message: '账号前缀不能为空', trigger: 'blur' }
  ],
  count: [
    { required: true, message: '创建数量不能为空', trigger: 'blur' },
    { type: 'number', min: 1, max: 100, message: '创建数量必须在1-100之间', trigger: 'blur' }
  ]
}

// 添加/修改表单
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const phonePattern = /^1[3-9]\d{9}$/
const form = reactive({
  userId: null,
  userName: '',
  nickName: '',
  phonenumber: '',
  email: '',
  password: '',
  status: '0'
})

const rules = {
  userName: [
    { required: true, message: '用户名称不能为空', trigger: 'blur' }
  ],
  nickName: [
    { required: true, message: '用户昵称不能为空', trigger: 'blur' }
  ],
  phonenumber: [
    { required: true, message: '手机号码不能为空', trigger: 'blur' },
    { pattern: phonePattern, message: '请输入正确的11位手机号', trigger: ['blur', 'change'] }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 重置密码表单
const resetPwdVisible = ref(false)
const resetPwdFormRef = ref(null)
const resetPwdForm = reactive({
  userId: null,
  newPassword: '',
  confirmPassword: ''
})

const resetPwdRules = {
  newPassword: [
    { required: true, message: '新密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '确认密码不能为空', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetPwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 查询列表
function getList() {
  loading.value = true
  listTeachers(queryParams).then(response => {
    teacherList.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// 搜索
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

// 重置
function resetQuery() {
  queryParams.userName = null
  queryParams.phonenumber = null
  queryParams.status = null
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.userId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增教师'
  dialogVisible.value = true
  resetForm()
}

// 修改
function handleUpdate(row) {
  dialogTitle.value = '修改教师'
  dialogVisible.value = true
  const userId = row.userId || ids.value[0]
  getTeacherInfo(userId).then(response => {
    const user = response.data
    Object.assign(form, {
      userId: user.userId,
      userName: user.userName,
      nickName: user.nickName,
      phonenumber: user.phonenumber,
      email: user.email,
      status: user.status
    })
  })
}

// 删除
function handleDelete(row) {
  const userIds = row.userId ? [row.userId] : ids.value
  ElMessageBox.confirm('是否确认删除选中的教师账号？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return deleteTeachers(userIds)
  }).then(() => {
    getList()
    ElMessage.success('删除成功')
  })
}

// 状态修改
function handleStatusChange(row) {
  changeTeacherStatus(row.userId, row.status).then(() => {
    ElMessage.success('状态修改成功')
  }).catch(() => {
    // 如果失败，恢复原状态
    row.status = row.status === '0' ? '1' : '0'
  })
}

// 重置密码
function handleResetPwd(row) {
  resetPwdForm.userId = row.userId
  resetPwdForm.newPassword = ''
  resetPwdForm.confirmPassword = ''
  resetPwdVisible.value = true
}

// 提交重置密码
function submitResetPwd() {
  resetPwdFormRef.value.validate(valid => {
    if (valid) {
      resetTeacherPassword(resetPwdForm.userId, resetPwdForm.newPassword).then(() => {
        ElMessage.success('密码重置成功')
        resetPwdVisible.value = false
      })
    }
  })
}

// 提交表单
function submitForm() {
  formRef.value.validate(valid => {
    if (valid) {
      const payload = {
        userId: form.userId,
        userName: form.userName,
        nickName: form.nickName,
        phonenumber: form.phonenumber,
        email: form.email,
        password: form.password,
        status: form.status
      }
      if (form.userId) {
        updateTeacher(payload).then(() => {
          ElMessage.success('修改成功')
          dialogVisible.value = false
          getList()
        })
      } else {
        insertTeacher(payload).then(() => {
          ElMessage.success('新增成功')
          dialogVisible.value = false
          getList()
        })
      }
    }
  })
}

// 重置表单
function resetForm() {
  form.userId = null
  form.userName = ''
  form.nickName = ''
  form.phonenumber = ''
  form.email = ''
  form.password = ''
  form.status = '0'
}

// 预览批量创建
function previewBatchCreate() {
  batchFormRef.value.validate(valid => {
    if (valid) {
      previewLoading.value = true
      const accounts = []
      for (let i = 1; i <= batchForm.count; i++) {
        const account = batchForm.accountPrefix + String(i).padStart(3, '0')
        const nickname = (batchForm.nicknamePrefix || '教师') + String(i).padStart(3, '0')
        const password = batchForm.defaultPassword || (account + '123456')
        accounts.push({ account, password, nickname })
      }
      previewAccounts.value = accounts
      previewVisible.value = true
      previewLoading.value = false
    }
  })
}

// 确认批量创建
function confirmBatchCreate() {
  batchFormRef.value.validate(valid => {
    if (valid) {
      creating.value = true
      batchCreateTeachers({
        accountPrefix: batchForm.accountPrefix,
        count: batchForm.count,
        defaultPassword: batchForm.defaultPassword || null,
        nicknamePrefix: batchForm.nicknamePrefix || null
      }).then(response => {
        const data = response.data
        ElMessage.success(`成功创建${data.successCount}个教师账号，失败${data.failCount}个`)
        previewVisible.value = false
        creating.value = false
        getList()
        // 重置表单
        batchForm.accountPrefix = 'teacher_'
        batchForm.count = 10
        batchForm.defaultPassword = ''
        batchForm.nicknamePrefix = '教师'
      }).catch(() => {
        creating.value = false
      })
    }
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.batch-create-card {
  margin-bottom: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.table-container {
  margin-top: 20px;
}

.mb8 {
  margin-bottom: 8px;
}
</style>
