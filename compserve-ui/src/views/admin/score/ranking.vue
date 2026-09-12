<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>竞赛排名</span>
        </div>
      </template>

      <!-- 查询条件 -->
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="100px">
        <el-form-item label="选择竞赛" prop="competitionId">
          <el-select
            v-model="queryParams.competitionId"
            placeholder="请选择竞赛"
            filterable
            style="width: 300px"
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
        <el-form-item label="显示前N名">
          <el-input-number
            v-model="queryParams.topN"
            :min="0"
            :max="100"
            placeholder="0表示显示全部"
            style="width: 150px"
          />
          <span style="margin-left: 10px; color: #909399;">（0表示显示全部）</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button type="success" icon="Check" @click="handleConfirm" :disabled="!queryParams.competitionId || rankingList.length === 0">确认排名</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="rankingList" border style="margin-top: 20px;">
        <el-table-column label="排名" align="center" width="80">
          <template #default="scope">
            <span style="font-size: 18px; font-weight: bold; color: #409EFF;">{{ scope.row.rank }}</span>
          </template>
        </el-table-column>
        <el-table-column label="队伍名称" align="center" prop="teamName" :show-overflow-tooltip="true" min-width="200" />
        <el-table-column label="平均分" align="center" prop="avgScore" width="120">
          <template #default="scope">
            <span style="font-size: 16px; font-weight: bold; color: #67C23A;">{{ scope.row.avgScore }}</span>
          </template>
        </el-table-column>
        <el-table-column label="评分数量" align="center" prop="scoreCount" width="120" />
        <el-table-column label="详细评分" align="center" width="200">
          <template #default="scope">
            <el-button link type="primary" @click="viewScores(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="rankingList.length === 0 && !loading" style="text-align: center; padding: 40px; color: #909399;">
        暂无排名数据，请先选择竞赛并查询
      </div>
    </el-card>

    <!-- 评分详情弹窗 -->
    <el-dialog v-model="scoreDetailVisible" title="评分详情" width="800px">
      <el-table :data="currentScores" border>
        <el-table-column label="评审人" align="center" prop="reviewerName" width="150" />
        <el-table-column label="分数" align="center" prop="score" width="120">
          <template #default="scope">
            <span style="font-weight: bold;">{{ scope.row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column label="评语" align="center" prop="comment" :show-overflow-tooltip="true" />
        <el-table-column label="评分时间" align="center" prop="scoreTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.scoreTime) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="ScoreRanking">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCompetitionRanking, confirmRanking } from '@/api/admin/score'
import { listCompetition } from '@/api/admin/competition'
import { parseTime } from '@/utils/ruoyi'

const loading = ref(false)
const rankingList = ref([])
const competitionList = ref([])
const scoreDetailVisible = ref(false)
const currentScores = ref([])

const queryParams = reactive({
  competitionId: null,
  topN: 0
})

// 查询排名
function handleQuery() {
  if (!queryParams.competitionId) {
    ElMessage.warning('请先选择竞赛')
    return
  }
  
  loading.value = true
  getCompetitionRanking(queryParams.competitionId, queryParams.topN > 0 ? queryParams.topN : null).then(res => {
    rankingList.value = res.data || []
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

// 查看评分详情
function viewScores(row) {
  currentScores.value = row.scores || []
  scoreDetailVisible.value = true
}

// 确认排名
function handleConfirm() {
  if (!queryParams.competitionId) {
    ElMessage.warning('请先选择竞赛')
    return
  }
  
  ElMessageBox.confirm('确认要保存当前排名结果吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    confirmRanking({
      competitionId: queryParams.competitionId,
      rankingList: rankingList.value
    }).then(() => {
      ElMessage.success('排名确认成功')
    })
  })
}

onMounted(() => {
  loadCompetitions()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
</style>
