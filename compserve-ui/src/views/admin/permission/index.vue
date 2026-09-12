<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>教师赋权管理</span>
        </div>
      </template>

      <!-- 赋权表单 -->
      <el-card shadow="never" class="grant-card">
        <template #header>
          <span>为教师分配竞赛评审权限</span>
        </template>
        <el-form :model="grantForm" :rules="grantRules" ref="grantFormRef" label-width="120px">
          <el-form-item label="选择竞赛" prop="competitionId">
            <el-select
              v-model="grantForm.competitionId"
              placeholder="请选择竞赛"
              filterable
              style="width: 100%"
              @change="handleCompetitionChange"
            >
              <el-option
                v-for="comp in competitionList"
                :key="comp.competitionId"
                :label="`${comp.competitionName} (${comp.competitionType})`"
                :value="comp.competitionId"
              >
                <div>
                  <div>{{ comp.competitionName }}</div>
                  <div style="font-size: 12px; color: #909399;">
                    {{ comp.competitionType }} | 
                    {{ parseTime(comp.registerStartTime, '{y}-{m}-{d}') }} ~ 
                    {{ parseTime(comp.registerEndTime, '{y}-{m}-{d}') }}
                  </div>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="选择教师" prop="teacherIds">
            <el-select
              v-model="grantForm.teacherIds"
              placeholder="请选择教师（可多选）"
              multiple
              filterable
              style="width: 100%"
              @change="handleTeacherChange"
            >
              <el-option
                v-for="teacher in teacherList"
                :key="teacher.userId"
                :label="`${teacher.nickName || teacher.userName} (${teacher.userName})`"
                :value="teacher.userId"
              >
                <div>
                  <div>{{ teacher.nickName || teacher.userName }}</div>
                  <div style="font-size: 12px; color: #909399;">
                    {{ teacher.userName }}
                    <span v-if="teacher.phonenumber"> | {{ teacher.phonenumber }}</span>
                    <span v-if="isTeacherDisabled(teacher.userId)" style="color: #f56c6c;">
                      （该教师是该竞赛的指导老师，可赋权但不能评审自己指导的队伍）
                    </span>
                  </div>
                </div>
              </el-option>
            </el-select>
            <div class="form-tip">支持多选，已选择 {{ grantForm.teacherIds?.length || 0 }} 位教师</div>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleGrant" :loading="granting">确认赋权</el-button>
            <el-button @click="resetGrantForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 已赋权列表 -->
      <div class="table-container" style="margin-top: 20px;">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="竞赛名称" prop="competitionName">
            <el-select
              v-model="queryParams.competitionId"
              placeholder="请选择竞赛"
              clearable
              filterable
              style="width: 240px"
              @change="handleQuery"
            >
              <el-option
                v-for="comp in competitionList"
                :key="comp.competitionId"
                :label="comp.competitionName"
                :value="comp.competitionId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="教师名称" prop="teacherName">
            <el-input
              v-model="queryParams.teacherName"
              placeholder="请输入教师名称"
              clearable
              style="width: 240px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>

        <el-table v-loading="loading" :data="permissionList">
          <el-table-column label="序号" type="index" width="60" align="center" />
          <el-table-column label="竞赛名称" align="center" prop="competitionName" :show-overflow-tooltip="true" />
          <el-table-column label="竞赛类型" align="center" prop="competitionType" width="120" />
          <el-table-column label="教师姓名" align="center" prop="teacherName" :show-overflow-tooltip="true" />
          <el-table-column label="教师账号" align="center" prop="teacherUserName" :show-overflow-tooltip="true" />
          <el-table-column label="手机号码" align="center" prop="teacherPhone" width="120" />
          <el-table-column label="邮箱" align="center" prop="teacherEmail" :show-overflow-tooltip="true" />
          <el-table-column label="操作" align="center" width="120" fixed="right">
            <template #default="scope">
              <el-button
                type="danger"
                link
                icon="Delete"
                @click="handleRevoke(scope.row)"
              >撤销</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup name="Permission">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCompetitions, listTeachers, grantPermission, revokePermission, listPermissions } from '@/api/admin/permission'
import { parseTime } from '@/utils/ruoyi'

// 表单数据
const grantForm = ref({
  competitionId: null,
  teacherIds: []
})

const grantRules = {
  competitionId: [
    { required: true, message: '请选择竞赛', trigger: 'change' }
  ],
  teacherIds: [
    { required: true, message: '请选择至少一位教师', trigger: 'change' }
  ]
}

const grantFormRef = ref(null)
const granting = ref(false)

// 竞赛列表
const competitionList = ref([])
// 教师列表
const teacherList = ref([])
// 已赋权列表
const permissionList = ref([])

// 查询参数
const queryParams = reactive({
  competitionId: null,
  teacherName: ''
})

const loading = ref(false)
const showSearch = ref(true)

// 已禁用的教师ID列表（指导老师）
const disabledTeacherIds = ref([])

// 加载竞赛列表
function loadCompetitions() {
  listCompetitions().then(res => {
    competitionList.value = res.data || []
  }).catch(() => {
    ElMessage.error('加载竞赛列表失败')
  })
}

// 加载教师列表
function loadTeachers() {
  listTeachers().then(res => {
    teacherList.value = res.data || []
  }).catch(() => {
    ElMessage.error('加载教师列表失败')
  })
}

// 竞赛选择变化
function handleCompetitionChange(competitionId) {
  // 清空已选择的教师
  grantForm.value.teacherIds = []
  disabledTeacherIds.value = []
}

// 教师选择变化
function handleTeacherChange(teacherIds) {
  // 可以在这里添加额外的验证逻辑
}

// 判断教师是否被禁用
function isTeacherDisabled(teacherId) {
  return disabledTeacherIds.value.includes(teacherId)
}

// 确认赋权
function handleGrant() {
  grantFormRef.value.validate(valid => {
    if (!valid) {
      return
    }

    ElMessageBox.confirm(
      `确认要为 ${grantForm.value.teacherIds.length} 位教师分配竞赛评审权限吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      granting.value = true
      grantPermission({
        competitionId: grantForm.value.competitionId,
        teacherIds: grantForm.value.teacherIds
      }).then(res => {
        ElMessage.success(`成功为 ${res.data} 位教师赋权`)
        resetGrantForm()
        getList()
      }).catch(() => {
        ElMessage.error('赋权失败')
      }).finally(() => {
        granting.value = false
      })
    }).catch(() => {
      // 用户取消
    })
  })
}

// 重置赋权表单
function resetGrantForm() {
  grantForm.value = {
    competitionId: null,
    teacherIds: []
  }
  disabledTeacherIds.value = []
  grantFormRef.value?.resetFields()
}

// 查询已赋权列表
function getList() {
  loading.value = true
  listPermissions({
    competitionId: queryParams.competitionId || undefined,
    teacherId: undefined // 暂时不支持按教师ID筛选，可以通过教师名称前端筛选
  }).then(res => {
    let list = res.data || []
    
    // 前端筛选教师名称
    if (queryParams.teacherName) {
      const keyword = queryParams.teacherName.toLowerCase()
      list = list.filter(item => {
        const teacherName = (item.teacherName || '').toLowerCase()
        const teacherUserName = (item.teacherUserName || '').toLowerCase()
        return teacherName.includes(keyword) || teacherUserName.includes(keyword)
      })
    }
    
    permissionList.value = list
  }).catch(() => {
    ElMessage.error('加载赋权列表失败')
  }).finally(() => {
    loading.value = false
  })
}

// 搜索
function handleQuery() {
  getList()
}

// 重置查询
function resetQuery() {
  queryParams.competitionId = null
  queryParams.teacherName = ''
  getList()
}

// 撤销权限
function handleRevoke(row) {
  ElMessageBox.confirm(
    `确认要撤销教师"${row.teacherName}"对竞赛"${row.competitionName}"的评审权限吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    revokePermission(row.competitionId, row.userId).then(() => {
      ElMessage.success('撤销成功')
      getList()
    }).catch(() => {
      ElMessage.error('撤销失败')
    })
  }).catch(() => {
    // 用户取消
  })
}

onMounted(() => {
  loadCompetitions()
  loadTeachers()
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

.grant-card {
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
</style>
