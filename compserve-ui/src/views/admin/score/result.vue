<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>评分结果</span>
        </div>
      </template>

      <!-- 查询条件 -->
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
        <el-form-item label="竞赛名称" prop="competitionId">
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
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>

        <el-table v-loading="loading" :data="scoreList">
          <el-table-column label="序号" type="index" width="60" align="center" />
          <el-table-column label="竞赛名称" align="center" prop="competitionName" :show-overflow-tooltip="true" min-width="150" />
          <el-table-column label="队伍名称" align="center" prop="teamName" :show-overflow-tooltip="true" min-width="150" />
          <el-table-column label="评审人" align="center" prop="reviewerName" width="120" />
          <el-table-column label="分数" align="center" prop="score" width="100">
            <template #default="scope">
              <span style="font-weight: bold; color: #409EFF;">{{ scope.row.score }}</span>
            </template>
          </el-table-column>
          <el-table-column label="评语" align="center" prop="comment" :show-overflow-tooltip="true" min-width="200" />
          <el-table-column label="评分时间" align="center" prop="scoreTime" width="180">
            <template #default="scope">
              <span>{{ parseTime(scope.row.scoreTime) }}</span>
            </template>
          </el-table-column>
        </el-table>
    </el-card>
  </div>
</template>

<script setup name="ScoreResult">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listScoreResults } from '@/api/admin/score'
import { listCompetition } from '@/api/admin/competition'
import { parseTime } from '@/utils/ruoyi'

const loading = ref(false)
const scoreList = ref([])
const showSearch = ref(true)
const competitionList = ref([])

const queryParams = reactive({
  competitionId: null
})

// 查询列表
function getList() {
  loading.value = true
  listScoreResults(queryParams.competitionId).then(res => {
    scoreList.value = res.data || []
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// 加载竞赛列表
function loadCompetitions() {
  listCompetition({ pageNum: 1, pageSize: 1000 }).then(res => {
    competitionList.value = res.rows || []
  })
}

// 搜索
function handleQuery() {
  getList()
}

// 重置
function resetQuery() {
  queryParams.competitionId = null
  getList()
}

onMounted(() => {
  loadCompetitions()
  getList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
</style>
