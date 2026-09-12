<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>模拟题库管理</span>
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
        <el-form-item label="题干" prop="content">
          <el-input
            v-model="queryParams.content"
            placeholder="请输入题目内容关键字"
            clearable
            style="width: 260px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="题型" prop="type">
          <el-select
            v-model="queryParams.type"
            placeholder="请选择题型"
            clearable
            style="width: 160px"
          >
            <el-option label="判断题" value="judge" />
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multiple" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input
            v-model="queryParams.category"
            placeholder="请输入分类（如：数据库/算法）"
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
        :data="questionList"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="ID" prop="questionId" width="80" align="center" />
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
        <el-table-column label="分类" prop="category" width="120" align="center" />
        <el-table-column label="难度" prop="difficulty" width="90" align="center">
          <template #default="scope">
            <el-tag
              v-if="scope.row.difficulty === 'easy'"
              type="success"
              size="small"
            >简单</el-tag>
            <el-tag
              v-else-if="scope.row.difficulty === 'medium'"
              type="warning"
              size="small"
            >中等</el-tag>
            <el-tag
              v-else-if="scope.row.difficulty === 'hard'"
              type="danger"
              size="small"
            >困难</el-tag>
            <span v-else>{{ scope.row.difficulty || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="分值" prop="score" width="80" align="center" />
        <el-table-column label="是否启用" prop="enabled" width="90" align="center">
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
          width="200"
          class-name="small-padding fixed-width"
        >
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">
              修改
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

    <!-- 新增/编辑题目弹窗 -->
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
        <el-form-item label="题型" prop="type">
          <el-select v-model="form.type" placeholder="请选择题型" style="width: 200px">
            <el-option label="判断题" value="judge" />
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multiple" />
          </el-select>
        </el-form-item>
        <el-form-item label="题干" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="3"
            placeholder="请输入题目内容"
          />
        </el-form-item>
        <el-form-item label="选项A" v-if="form.type !== 'judge'">
          <el-input v-model="form.optionA" placeholder="请输入选项A" />
        </el-form-item>
        <el-form-item label="选项B" v-if="form.type !== 'judge'">
          <el-input v-model="form.optionB" placeholder="请输入选项B" />
        </el-form-item>
        <el-form-item label="选项C" v-if="form.type !== 'judge'">
          <el-input v-model="form.optionC" placeholder="请输入选项C" />
        </el-form-item>
        <el-form-item label="选项D" v-if="form.type !== 'judge'">
          <el-input v-model="form.optionD" placeholder="请输入选项D" />
        </el-form-item>
        <el-form-item label="正确答案" prop="correctAnswer">
          <el-input
            v-if="form.type === 'judge'"
            v-model="form.correctAnswer"
            placeholder="判断题请输入 T 或 F"
            style="width: 220px"
          />
          <el-input
            v-else-if="form.type === 'single'"
            v-model="form.correctAnswer"
            placeholder="单选题请输入 A / B / C / D"
            style="width: 220px"
          />
          <el-input
            v-else
            v-model="form.correctAnswer"
            placeholder="多选题请输入如 A,B,C（英文逗号分隔）"
          />
        </el-form-item>
        <el-form-item label="分值" prop="score">
          <el-input-number
            v-model="form.score"
            :min="1"
            :max="100"
          />
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="form.difficulty" placeholder="请选择难度" style="width: 200px">
            <el-option label="简单" value="easy" />
            <el-option label="中等" value="medium" />
            <el-option label="困难" value="hard" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="请输入题目分类，如：数据库、操作系统" />
        </el-form-item>
        <el-form-item label="是否启用" prop="enabled">
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
  </div>
</template>

<script setup name="MockQuestionAdmin">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'
import { listMockQuestion, getMockQuestion, addMockQuestion, updateMockQuestion, delMockQuestion } from '@/api/admin/mockExamQuestion'

const loading = ref(false)
const questionList = ref([])
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  content: null,
  type: null,
  category: null
})

const form = ref({})
const rules = {
  type: [{ required: true, message: '题型不能为空', trigger: 'change' }],
  content: [{ required: true, message: '题干不能为空', trigger: 'blur' }],
  correctAnswer: [{ required: true, message: '正确答案不能为空', trigger: 'blur' }],
  score: [{ required: true, message: '分值不能为空', trigger: 'change' }]
}

const queryRef = ref(null)
const formRef = ref(null)

// 查询列表
function getList() {
  loading.value = true
  listMockQuestion(queryParams).then(res => {
    // 兼容 ruoyi 风格（rows/total）或直接 data
    if (Array.isArray(res.rows)) {
      questionList.value = res.rows
      total.value = res.total || 0
    } else {
      questionList.value = res.data || []
      total.value = questionList.value.length
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
  ids.value = selection.map(item => item.questionId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

// 新增
function handleAdd() {
  reset()
  dialogTitle.value = '新增题目'
  dialogVisible.value = true
}

// 修改
function handleUpdate(row) {
  reset()
  const id = row?.questionId || ids.value[0]
  if (!id) {
    ElMessage.warning('请选择要修改的题目')
    return
  }
  getMockQuestion(id).then(res => {
    form.value = res.data || {}
    dialogTitle.value = '修改题目'
    dialogVisible.value = true
  })
}

// 提交表单
function submitForm() {
  formRef.value.validate(valid => {
    if (!valid) return
    const isUpdate = !!form.value.questionId
    const api = isUpdate ? updateMockQuestion : addMockQuestion
    api(form.value).then(() => {
      ElMessage.success(isUpdate ? '修改成功' : '新增成功')
      dialogVisible.value = false
      getList()
    })
  })
}

// 删除
function handleDelete(row) {
  const delIds = row?.questionId || ids.value
  if (!delIds || (Array.isArray(delIds) && !delIds.length)) {
    ElMessage.warning('请选择要删除的题目')
    return
  }
  ElMessageBox.confirm(`是否确认删除题目编号为 "${delIds}" 的数据项？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return delMockQuestion(delIds)
  }).then(() => {
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

// 表单重置
function reset() {
  form.value = {
    questionId: null,
    type: 'single',
    content: '',
    optionA: '',
    optionB: '',
    optionC: '',
    optionD: '',
    correctAnswer: '',
    score: 5,
    category: '',
    difficulty: 'easy',
    enabled: 1
  }
  formRef.value?.resetFields()
}

getList()
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.mb8 {
  margin-bottom: 8px;
}
</style>

