<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>学生账号管理</span>
        </div>
      </template>

      <!-- 学生列表 -->
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
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate">修改</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="studentList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="用户编号" align="center" prop="userId" width="100" />
          <el-table-column label="用户名称" align="center" prop="userName" :show-overflow-tooltip="true" />
          <el-table-column label="用户昵称" align="center" prop="nickName" :show-overflow-tooltip="true" />
          <el-table-column label="手机号码" align="center" prop="phonenumber" width="120" />
          <el-table-column label="邮箱" align="center" prop="email" :show-overflow-tooltip="true" />
          <el-table-column label="状态" align="center" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
                {{ scope.row.status === '0' ? '正常' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center" prop="createTime" width="160">
            <template #default="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="250" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
              <el-button link type="primary" icon="Key" @click="handleResetPwd(scope.row)">重置密码</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </el-card>

    <!-- 修改学生弹窗 -->
    <el-dialog title="修改学生账号" v-model="dialogVisible" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名称" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入用户名称" disabled />
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
          <el-input v-model="resetPwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResetPwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="StudentManagement">
import { ref, reactive, onMounted } from 'vue'
import { listStudents, updateStudent, resetStudentPassword } from '@/api/admin/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'

const loading = ref(false)
const showSearch = ref(true)
const studentList = ref([])
const total = ref(0)
const single = ref(true)
const ids = ref([])
const dialogVisible = ref(false)
const resetPwdVisible = ref(false)
const currentUserId = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  userName: null,
  phonenumber: null,
  status: null
})

const form = ref({})
const rules = {
  nickName: [
    { required: true, message: '用户昵称不能为空', trigger: 'blur' }
  ],
  phonenumber: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const resetPwdForm = ref({
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
        if (value !== resetPwdForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const formRef = ref(null)
const resetPwdFormRef = ref(null)

// 查询列表
function getList() {
  loading.value = true
  listStudents(queryParams).then(res => {
    studentList.value = res.rows
    total.value = res.total
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
}

// 修改
function handleUpdate(row) {
  const userId = row.userId || ids.value[0]
  if (!userId) {
    ElMessage.warning('请选择要修改的学生')
    return
  }
  
  reset()
  // 从列表中获取学生信息
  const student = studentList.value.find(item => item.userId === userId)
  if (student) {
    form.value = {
      userId: student.userId,
      userName: student.userName,
      nickName: student.nickName,
      phonenumber: student.phonenumber,
      email: student.email,
      status: student.status
    }
    dialogVisible.value = true
  }
}

// 提交表单
function submitForm() {
  formRef.value.validate(valid => {
    if (valid) {
      updateStudent(form.value).then(() => {
        ElMessage.success('修改成功')
        dialogVisible.value = false
        getList()
      })
    }
  })
}

// 重置密码
function handleResetPwd(row) {
  const userId = row.userId || ids.value[0]
  if (!userId) {
    ElMessage.warning('请选择要重置密码的学生')
    return
  }
  
  currentUserId.value = userId
  resetPwdForm.value = {
    newPassword: '',
    confirmPassword: ''
  }
  resetPwdFormRef.value?.resetFields()
  resetPwdVisible.value = true
}

// 提交重置密码
function submitResetPwd() {
  resetPwdFormRef.value.validate(valid => {
    if (valid) {
      resetStudentPassword(currentUserId.value, resetPwdForm.value.newPassword).then(() => {
        ElMessage.success('密码重置成功')
        resetPwdVisible.value = false
      })
    }
  })
}

// 取消按钮
function cancel() {
  dialogVisible.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    userId: null,
    userName: null,
    nickName: null,
    phonenumber: null,
    email: null,
    status: '0'
  }
  formRef.value?.resetFields()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: 600;
}

.table-container {
  margin-top: 20px;
}
</style>
