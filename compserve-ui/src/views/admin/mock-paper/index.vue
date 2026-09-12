<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>模拟试卷管理</span>
        </div>
      </template>

      <!-- 查询条件 -->
      <el-form
        :model="queryParams"
        ref="queryRef"
        :inline="true"
        v-show="showSearch"
        label-width="80px"
      >
        <el-form-item label="试卷名称" prop="title">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入试卷名称关键字"
            clearable
            style="width: 260px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="分类" prop="categoryName">
          <el-input
            v-model="queryParams.categoryName"
            placeholder="请输入分类名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
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
          <el-button
            type="success"
            plain
            icon="Edit"
            :disabled="single"
            @click="handleUpdate"
          >
            修改
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
          >
            删除
          </el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </el-row>

      <el-table
        v-loading="loading"
        :data="paperList"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="ID" prop="paperId" width="80" align="center" />
        <el-table-column
          label="试卷名称"
          prop="title"
          min-width="220"
          :show-overflow-tooltip="true"
        />
        <el-table-column
          label="分类"
          prop="categoryName"
          width="150"
          align="center"
        />
        <el-table-column label="总分" prop="totalScore" width="90" align="center" />
        <el-table-column label="时长(分钟)" prop="duration" width="110" align="center" />
        <el-table-column label="难度" prop="level" width="100" align="center">
          <template #default="scope">
            <el-tag
              v-if="scope.row.level === 'easy'"
              type="success"
              size="small"
            >简单</el-tag>
            <el-tag
              v-else-if="scope.row.level === 'medium'"
              type="warning"
              size="small"
            >中等</el-tag>
            <el-tag
              v-else-if="scope.row.level === 'hard'"
              type="danger"
              size="small"
            >困难</el-tag>
            <span v-else>{{ scope.row.level || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="启用" width="90" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.enabled === 1" type="success" size="small">启用</el-tag>
            <el-tag v-else type="info" size="small">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180" align="center">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          align="center"
          width="260"
          class-name="small-padding fixed-width"
        >
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">
              修改
            </el-button>
            <el-button
              link
              type="warning"
              icon="Setting"
              @click="handleAssignQuestions(scope.row)"
            >
              分配题库
            </el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 新增/编辑试卷弹窗 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="720px"
      append-to-body
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="试卷名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入试卷名称" />
        </el-form-item>
        <el-form-item label="试卷描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入试卷描述"
          />
        </el-form-item>
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input v-model="form.categoryCode" placeholder="请输入分类编码（如：base/db）" />
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称（如：计算机基础）" />
        </el-form-item>
        <el-form-item label="考试时长" prop="duration">
          <el-input-number
            v-model="form.duration"
            :min="10"
            :max="300"
          />
          <span style="margin-left: 8px;">分钟</span>
        </el-form-item>
        <el-form-item label="总分" prop="totalScore">
          <el-input-number
            v-model="form.totalScore"
            :min="1"
            :max="1000"
          />
        </el-form-item>
        <el-form-item label="难度" prop="level">
          <el-select v-model="form.level" placeholder="请选择难度" style="width: 200px">
            <el-option label="简单" value="easy" />
            <el-option label="中等" value="medium" />
            <el-option label="困难" value="hard" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用" prop="enabled">
          <el-switch
            v-model="form.enabled"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配题库弹窗 -->
    <el-dialog
      :title="`分配题库 - ${currentPaper?.title || ''}`"
      v-model="assignDialogVisible"
      width="900px"
      append-to-body
    >
      <div v-if="currentPaper">
        <div class="assign-paper-info">
          <span>试卷名称：{{ currentPaper.title }}</span>
          <span>分类：{{ currentPaper.categoryName || '未设置' }}</span>
          <span>总分：{{ currentPaper.totalScore }}</span>
          <span>时长：{{ currentPaper.duration }} 分钟</span>
        </div>
        <el-alert
          type="info"
          show-icon
          :closable="false"
          title="勾选需要加入本试卷的题目，可以单独调整每道题在本试卷中的分值；保存时会覆盖原有配置。"
          class="mb8"
        />
      </div>
      <el-table
        v-loading="assignLoading"
        :data="assignQuestionTable"
        height="420px"
        border
        ref="assignTableRef"
        @selection-change="handleAssignSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="题目ID" prop="questionId" width="90" align="center" />
        <el-table-column
          label="题干"
          prop="content"
          min-width="260"
          :show-overflow-tooltip="true"
        />
        <el-table-column label="题型" prop="type" width="90" align="center">
          <template #default="scope">
            <span v-if="scope.row.type === 'judge'">判断题</span>
            <span v-else-if="scope.row.type === 'single'">单选题</span>
            <span v-else-if="scope.row.type === 'multiple'">多选题</span>
            <span v-else>{{ scope.row.type }}</span>
          </template>
        </el-table-column>
        <el-table-column label="题库分值" prop="defaultScore" width="90" align="center" />
        <el-table-column label="试卷分值" width="120" align="center">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.examScore"
              :min="1"
              :max="100"
              size="small"
            />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MockPaperAdmin">
import { ref, reactive, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'
import { listMockPaper, getMockPaper, addMockPaper, updateMockPaper, delMockPaper, getPaperQuestions, savePaperQuestions } from '@/api/admin/mockExamPaper'
import { listMockQuestion } from '@/api/admin/mockExamQuestion'

const loading = ref(false)
const paperList = ref([])
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const selectedPaper = ref(null) // 当前列表中选中的第一条试卷

// 分配题库相关
const assignDialogVisible = ref(false)
const assignLoading = ref(false)
const currentPaper = ref(null)
const assignQuestionTable = ref([]) // 带 examScore、defaultScore 的题目列表
const selectedAssignIds = ref([]) // 已勾选的 questionId 列表
const assignTableRef = ref(null) // 分配弹窗内表格引用，用于反显勾选

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: null,
  categoryName: null
})

const form = ref({})
const rules = {
  title: [{ required: true, message: '试卷名称不能为空', trigger: 'blur' }],
  duration: [{ required: true, message: '考试时长不能为空', trigger: 'change' }],
  totalScore: [{ required: true, message: '总分不能为空', trigger: 'change' }]
}

const queryRef = ref(null)
const formRef = ref(null)

// 查询列表
function getList() {
  loading.value = true
  listMockPaper(queryParams).then(res => {
    if (Array.isArray(res.rows)) {
      paperList.value = (res.rows || []).map(item => ({
        // 兼容后端返回 id 或 paperId 两种写法
        ...item,
        paperId: item.paperId != null ? item.paperId : item.id
      }))
      total.value = res.total || 0
    } else {
      const data = res.data || []
      paperList.value = data.map(item => ({
        ...item,
        paperId: item.paperId != null ? item.paperId : item.id
      }))
      total.value = paperList.value.length
    }
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
  queryRef.value?.resetFields()
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.paperId || item.id)
  selectedPaper.value = selection.length ? selection[0] : null
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

// 新增
function handleAdd() {
  reset()
  dialogTitle.value = '新增试卷'
  dialogVisible.value = true
}

// 修改
function handleUpdate(row) {
  reset()
  const id = row?.paperId || ids.value[0]
  if (!id) {
    ElMessage.warning('请选择要修改的试卷')
    return
  }
  getMockPaper(id).then(res => {
    form.value = res.data || {}
    dialogTitle.value = '修改试卷'
    dialogVisible.value = true
  })
}

// 提交表单
function submitForm() {
  formRef.value.validate(valid => {
    if (!valid) return
    const isUpdate = !!form.value.paperId
    const api = isUpdate ? updateMockPaper : addMockPaper
    api(form.value).then(() => {
      ElMessage.success(isUpdate ? '修改成功' : '新增成功')
      dialogVisible.value = false
      getList()
    })
  })
}

// 删除
function handleDelete(row) {
  const delIds = row?.paperId || ids.value
  if (!delIds || (Array.isArray(delIds) && !delIds.length)) {
    ElMessage.warning('请选择要删除的试卷')
    return
  }
  ElMessageBox.confirm(`是否确认删除试卷编号为 "${delIds}" 的数据项？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return delMockPaper(delIds)
  }).then(() => {
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

// 表单重置
function reset() {
  form.value = {
    paperId: null,
    title: '',
    description: '',
    categoryCode: '',
    categoryName: '',
    duration: 60,
    totalScore: 100,
    level: 'easy',
    enabled: 1
  }
  formRef.value?.resetFields()
}

getList()

// 打开分配题库弹窗
function handleAssignQuestions(row) {
  const paper = row || selectedPaper.value
  if (!paper) {
    ElMessage.warning('请先选择一条试卷')
    return
  }
  const paperId = paper.paperId || paper.id
  if (!paperId) {
    ElMessage.error('试卷ID缺失，无法分配题库')
    return
  }
  currentPaper.value = { ...paper, paperId }
  assignDialogVisible.value = true
  loadAssignData(paperId)
}

// 加载分配弹窗数据：题库列表 + 已配置题目
async function loadAssignData(paperId) {
  if (!paperId) {
    return
  }
  assignLoading.value = true
  try {
    // 1. 查询所有启用题目（可按需增加筛选条件）
    const questionRes = await listMockQuestion({ enabled: 1, pageNum: 1, pageSize: 1000 })
    const questions = Array.isArray(questionRes.rows) ? questionRes.rows : (questionRes.data || [])

    // 2. 查询当前试卷已配置题目关联
    const paperQRes = await getPaperQuestions(paperId)
    const paperQuestions = paperQRes.data || []
    const assocMap = {}
    paperQuestions.forEach(item => {
      if (item.questionId != null) {
        assocMap[item.questionId] = item
      }
    })

    // 3. 组装表格数据
    const tableData = questions.map(q => {
      const assoc = assocMap[q.questionId]
      return {
        questionId: q.questionId,
        content: q.content,
        type: q.type,
        defaultScore: q.score,
        examScore: assoc && assoc.score != null ? assoc.score : (q.score || 5)
      }
    })
    assignQuestionTable.value = tableData

    // 4. 默认选中已有的关联题目
    selectedAssignIds.value = paperQuestions.map(item => item.questionId)

    // 5. 等待表格渲染完成后，根据 selectedAssignIds 反显勾选状态
    await nextTick()
    const table = assignTableRef.value
    if (table && table.clearSelection && table.toggleRowSelection) {
      table.clearSelection()
      assignQuestionTable.value.forEach(row => {
        if (selectedAssignIds.value.includes(row.questionId)) {
          table.toggleRowSelection(row, true)
        }
      })
    }
  } finally {
    assignLoading.value = false
  }
}

// 记录当前勾选的题目
function handleAssignSelectionChange(selection) {
  selectedAssignIds.value = selection.map(item => item.questionId)
}

// 提交分配结果
async function submitAssign() {
  if (!currentPaper.value) {
    ElMessage.error('当前试卷信息缺失')
    return
  }
  const paperId = currentPaper.value.paperId
  if (!selectedAssignIds.value.length) {
    // 允许清空全部题目
    await savePaperQuestions(paperId, [])
    ElMessage.success('已清空试卷题目配置')
    assignDialogVisible.value = false
    return
  }
  // 组装提交数据：questionId、score、sortOrder
  const payload = []
  let order = 1
  assignQuestionTable.value.forEach(row => {
    if (selectedAssignIds.value.includes(row.questionId)) {
      payload.push({
        questionId: row.questionId,
        score: row.examScore,
        sortOrder: order++
      })
    }
  })
  await savePaperQuestions(paperId, payload)
  ElMessage.success('题库分配已保存')
  assignDialogVisible.value = false
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.mb8 {
  margin-bottom: 8px;
}
</style>

