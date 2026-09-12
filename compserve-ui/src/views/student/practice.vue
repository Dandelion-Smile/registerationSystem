<template>
  <div class="student-page">
    <StudentNavbar />

    <main class="main-content">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">刷题训练</h1>
        <p class="page-desc">针对性练习，快速提升竞赛能力</p>
      </div>

      <!-- 最新文章 / 训练题库 区块 -->
      <section class="section-card article-section">
        <div class="section-card-head">
          <h2 class="section-title">最新文章</h2>
        </div>
        <div class="sub-tabs">
          <button
            :class="['sub-tab', { active: practiceTab === 'bank' }]"
            @click="switchPracticeTab('bank')"
          >
            训练题库
          </button>
          <button
            :class="['sub-tab', { active: practiceTab === 'record' }]"
            @click="switchPracticeTab('record')"
          >
            训练记录
          </button>
        </div>
        <div class="filter-row">
          <el-input
            v-model="searchInput"
            placeholder="输入关键字搜索题目"
            clearable
            class="filter-input"
            @change="loadRecommend"
          />
          <el-select
            v-model="categorySelect"
            placeholder="请选择归属"
            clearable
            class="filter-select"
            @change="loadRecommend"
          >
            <el-option label="编程开发" value="dev" />
            <el-option label="数据库" value="db" />
            <el-option label="计算机基础" value="base" />
            <el-option label="人工智能" value="ai" />
          </el-select>
        </div>
      </section>

      <!-- 数据统计卡片 -->
      <div class="stat-cards">
        <div class="stat-card">
          <div class="stat-label">题库分类:{{ overview.categoryName }}</div>
          <div class="stat-value">{{ overview.categoryCount }}个分类</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">错题记录:{{ overview.categoryName }}</div>
          <div class="stat-value">{{ overview.wrongCount }}道</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">训练记录:{{ overview.categoryName }}</div>
          <div class="stat-value">{{ overview.recordCount }}条</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">试题数量</div>
          <div class="stat-value">{{ overview.questionCount }}道</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">试题覆盖</div>
          <div class="stat-value">{{ overview.coverageRate }}%</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">最近训练</div>
          <div class="stat-value">{{ overview.recentDuration }}</div>
        </div>
      </div>

      <!-- 正确率趋势 -->
      <section class="section-card chart-section">
        <h2 class="section-title">正确率趋势</h2>
        <div ref="chartRef" class="chart-container"></div>
      </section>

      <!-- 推荐题目（仅在训练题库 Tab 下显示） -->
      <section
        v-if="practiceTab === 'bank'"
        class="section-card recommend-section"
      >
        <h2 class="section-title">推荐题目</h2>
        <div class="question-list" v-if="recommendedQuestions.length">
          <div
            v-for="q in recommendedQuestions"
            :key="q.id"
            class="question-card"
          >
            <div class="question-main">
              <div class="question-header">
                <span :class="['difficulty-tag', q.difficulty]">{{ q.difficultyLabel }}</span>
                <span class="question-title">{{ q.title }}</span>
              </div>
              <p class="question-desc">{{ q.desc }}</p>
              <div class="question-meta">
                <span><i class="far fa-clock"></i> 建议用时: {{ q.minutes }}分钟</span>
                <span><i class="fas fa-user"></i> 通过率: {{ q.passRate }}%</span>
              </div>
            </div>
            <div class="question-side">
              <span class="category-tag">{{ q.category }}</span>
              <el-button type="primary" class="btn-start" @click="startAnswer(q)">开始答题</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无匹配的训练题目" />
      </section>

      <!-- 训练记录列表（当前页内的简单记录） -->
      <section
        v-if="practiceTab === 'record'"
        class="section-card"
      >
        <h2 class="section-title">本页训练记录</h2>
        <div v-if="practiceHistory.length" class="question-list">
          <div
            v-for="(rec, idx) in practiceHistory"
            :key="idx"
            class="question-card"
          >
            <div class="question-main">
              <div class="question-header">
                <span
                  class="difficulty-tag"
                  :class="rec.correct ? 'easy' : 'medium'"
                >
                  {{ rec.correct ? '正确' : '错误' }}
                </span>
                <span class="question-title">{{ rec.title }}</span>
              </div>
              <p class="question-desc">
                提交时间：{{ rec.time }}
              </p>
              <div class="question-meta">
                <span>得分：{{ rec.score }} / {{ rec.totalScore }}</span>
                <span>你的答案：{{ rec.userAnswer || '未作答' }}</span>
                <span>正确答案：{{ rec.correctAnswer }}</span>
              </div>
            </div>
            <div class="question-side">
              <span class="category-tag">{{ rec.category }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="本页暂时没有训练记录" />
      </section>

      <!-- 开始答题弹窗 -->
      <el-dialog
        v-model="answerDialogVisible"
        :title="currentQuestion?.title || '开始答题'"
        width="480px"
      >
        <div v-if="currentQuestion">
          <p class="dialog-desc">{{ currentQuestion.desc }}</p>
          <div class="dialog-meta">
            <span><i class="far fa-clock"></i> 建议用时: {{ currentQuestion.minutes }}分钟</span>
            <span><i class="fas fa-user"></i> 通过率: {{ currentQuestion.passRate }}%</span>
          </div>
          <div class="dialog-question">
            <div class="dialog-label">你的答案：</div>
            <!-- 判断题：对/错 -->
            <el-radio-group
              v-if="currentQuestion.type === 'judge'"
              v-model="currentAnswer"
            >
              <el-radio label="对">对</el-radio>
              <el-radio label="错">错</el-radio>
            </el-radio-group>
            <!-- 有选项的题目（单选、多选等）：直接渲染选项列表 -->
            <el-radio-group
              v-else-if="(currentQuestion.options || []).length"
              v-model="currentAnswer"
            >
              <el-radio
                v-for="opt in currentQuestion.options || []"
                :key="opt"
                :label="opt"
              >
                {{ opt }}
              </el-radio>
            </el-radio-group>
            <!-- 其它类型：预留，先简单用输入框 -->
            <el-input
              v-else
              v-model="currentAnswer"
              placeholder="请输入你的答案"
            />
          </div>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="answerDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitAnswer">提交</el-button>
          </span>
        </template>
      </el-dialog>
    </main>
  </div>
</template>

<script setup>
import StudentNavbar from "@/components/StudentNavbar.vue";
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import useUserStore from '@/store/modules/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPracticeOverview, getPracticeRecommend, submitPracticeAnswer, getPracticeRecords } from '@/api/practice'

const userStore = useUserStore()
const searchKeyword = ref('')
const searchInput = ref('')
const categorySelect = ref('')
const practiceTab = ref('bank')
const chartRef = ref(null)
let chartInstance = null

// 概览统计数据
const overview = ref({
  categoryName: '',
  categoryCount: 0,
  wrongCount: 0,
  recordCount: 0,
  questionCount: 0,
  coverageRate: 0,
  recentDuration: ''
})

// 趋势数据与推荐题目
const trendData = ref([])
const recommendedQuestions = ref([])

// 当前答题弹窗状态与训练记录
const answerDialogVisible = ref(false)
const currentQuestion = ref(null)
const currentAnswer = ref('')
const practiceHistory = ref([]) // 当前用户的训练记录（从后端加载）

function startAnswer(q) {
  if (!q) return
  currentQuestion.value = q
  currentAnswer.value = ''
  answerDialogVisible.value = true
}

function renderChart() {
  if (!chartRef.value || !trendData.value.length) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c}%'
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendData.value.map(d => d.date),
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { color: '#6b7280' }
    },
    yAxis: {
      type: 'value',
      min: 20,
      max: 100,
      interval: 10,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f3f4f6' } },
      axisLabel: {
        color: '#6b7280',
        formatter: '{value}%'
      }
    },
    series: [
      {
        type: 'line',
        data: trendData.value.map(d => d.rate),
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { color: '#8b5cf6', width: 2 },
        itemStyle: { color: '#8b5cf6' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(139, 92, 246, 0.3)' },
            { offset: 1, color: 'rgba(139, 92, 246, 0.05)' }
          ])
        }
      }
    ]
  }
  chartInstance.setOption(option)
}

function handleResize() {
  chartInstance?.resize()
}

async function loadOverview() {
  try {
    const res = await getPracticeOverview()
    const data = res?.data || {}
    overview.value = {
      categoryName: data.categoryName || '计算机',
      categoryCount: data.categoryCount || 0,
      wrongCount: data.wrongCount || 0,
      recordCount: data.recordCount || 0,
      questionCount: data.questionCount || 0,
      coverageRate: data.coverageRate || 0,
      recentDuration: data.recentDuration || ''
    }
    trendData.value = data.trend || []
    renderChart()
  } catch (e) {
    ElMessage.error('加载训练概览数据失败')
  }
}

async function loadRecommend() {
  try {
    const res = await getPracticeRecommend({
      keyword: searchInput.value.trim() || undefined,
      categoryKey: categorySelect.value || undefined
    })
    recommendedQuestions.value = res?.data || []
  } catch (e) {
    ElMessage.error('加载推荐题目失败')
  }
}

// 加载当前用户的刷题训练记录列表
async function loadPracticeRecords() {
  try {
    const res = await getPracticeRecords()
    const list = res?.data || []
    // 后端暂未返回题目标题，这里先占位为 “练习题目（ID：xxx）”
    practiceHistory.value = list.map(item => ({
      time: item.time,
      title: item.title || `练习题目（ID：${item.questionId}）`,
      category: item.category,
      correct: !!item.correct,
      score: item.score ?? 0,
      totalScore: item.totalScore ?? 0,
      userAnswer: item.userAnswer,
      correctAnswer: item.correctAnswer
    }))
  } catch (e) {
    ElMessage.error('加载训练记录失败')
  }
}

// Tab 切换时触发相应数据加载
function switchPracticeTab(tab) {
  practiceTab.value = tab
  if (tab === 'bank') {
    loadRecommend()
  } else if (tab === 'record') {
    loadPracticeRecords()
  }
}

function submitAnswer() {
  if (!currentAnswer.value) {
    ElMessage.warning('请先选择你的答案')
    return
  }
  const payload = {
    questionId: currentQuestion.value.id,
    answer: currentAnswer.value
  }
  submitPracticeAnswer(payload)
    .then(res => {
      const data = res?.data || {}
      const correct = !!data.correct
      const score = data.score ?? 0
      const total = data.totalScore ?? 0
      if (correct) {
        ElMessage.success(`回答正确！得分 ${score} / ${total}`)
      } else {
        ElMessage.warning(`回答错误，本题得分 ${score} / ${total}`)
      }
      // 记录到本地训练记录列表
      practiceHistory.value.unshift({
        time: new Date().toLocaleString(),
        title: currentQuestion.value.title,
        category: currentQuestion.value.category,
        correct,
        score,
        totalScore: total,
        userAnswer: data.userAnswer,
        correctAnswer: data.correctAnswer
      })
      answerDialogVisible.value = false
    })
    .catch(() => {
      ElMessage.error('提交失败，请稍后重试')
    })
}

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logOut().then(() => {
        location.href = '/login'
      })
    }).catch(() => {})
  }
}

onMounted(() => {
  loadOverview()
  loadRecommend()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.student-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Helvetica Neue", Arial, sans-serif;
}

.header-nav {
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  height: 64px;
  gap: 40px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
  font-size: 20px;
  font-weight: 600;
}

.logo-text { font-size: 18px; }

.main-nav {
  display: flex;
  align-items: center;
  gap: 32px;
  flex: 1;
}

.nav-item {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 0;
  position: relative;
  transition: all 0.3s;
}

.nav-item:hover { color: white; }

.nav-item.active {
  color: white;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: white;
  border-radius: 2px;
}

.nav-actions { display: flex; align-items: center; gap: 16px; }

.search-box {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 6px 16px;
  gap: 8px;
}

.search-box i { color: white; font-size: 14px; }

.search-box input {
  background: transparent;
  border: none;
  outline: none;
  color: white;
  font-size: 14px;
  width: 200px;
}

.search-box input::placeholder { color: rgba(255, 255, 255, 0.7); }

.avatar-container { display: flex; align-items: center; }

.avatar-wrapper { display: flex; align-items: center; cursor: pointer; }

.user-avatar-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 0 0 2px rgba(255,255,255,0.3);
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-header { margin-bottom: 24px; }

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.page-desc {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
  margin: 0;
}

.section-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  margin-bottom: 24px;
}

.section-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.btn-write {
  background: #8b5cf6 !important;
  border-color: #8b5cf6 !important;
}

.btn-write i { margin-right: 6px; }

.sub-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.sub-tab {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  background: none;
  border: none;
  cursor: pointer;
  position: relative;
  margin-bottom: -1px;
}

.sub-tab:hover { color: #8b5cf6; }

.sub-tab.active {
  color: #8b5cf6;
}

.sub-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #8b5cf6;
}

.filter-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-input { width: 280px; }

.filter-select { width: 180px; }

:deep(.filter-input .el-input__wrapper),
:deep(.filter-select .el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e5e7eb inset;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  transition: all 0.3s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.chart-section .section-title { margin-bottom: 16px; }

.chart-container {
  width: 100%;
  height: 320px;
}

.recommend-section .section-title { margin-bottom: 20px; }

.question-list { display: flex; flex-direction: column; gap: 16px; }

.question-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 20px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  transition: all 0.3s;
}

.question-card:hover {
  border-color: #d1d5db;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.question-main { flex: 1; min-width: 0; }

.question-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.difficulty-tag { font-size: 13px; font-weight: 500; flex-shrink: 0; }

.difficulty-tag.easy { color: #2563eb; }

.difficulty-tag.medium { color: #ea580c; }

.question-title { font-size: 16px; font-weight: 600; color: #1f2937; }

.question-desc {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
  margin: 0 0 12px 0;
}

.question-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #6b7280;
}

.question-meta span { display: flex; align-items: center; gap: 6px; }

.question-meta i { color: #9ca3af; font-size: 12px; }

.question-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  flex-shrink: 0;
}

.category-tag {
  font-size: 13px;
  color: #6b7280;
  background: #f3f4f6;
  padding: 4px 12px;
  border-radius: 6px;
}

.btn-start { background: #8b5cf6 !important; border-color: #8b5cf6 !important; }

@media (max-width: 1100px) {
  .stat-cards { grid-template-columns: repeat(3, 1fr); }
}

@media (max-width: 768px) {
  .question-card { flex-direction: column; align-items: stretch; }
  .question-side { flex-direction: row; align-items: center; justify-content: space-between; }
}

@media (max-width: 600px) {
  .stat-cards { grid-template-columns: repeat(2, 1fr); }
  .section-card-head { flex-direction: column; align-items: flex-start; gap: 12px; }
  .filter-row { flex-direction: column; }
  .filter-input, .filter-select { width: 100%; }
}

.dropdown {
  position: relative;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  margin-top: 8px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 12px;
  min-width: 200px;
  display: none;
  z-index: 1000;
}

.dropdown-menu::before {
  content: '';
  position: absolute;
  top: -8px;
  left: 0;
  width: 100%;
  height: 8px;
}

.dropdown:hover .dropdown-menu {
  display: block;
}

.dropdown-menu a {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  color: #4b5563;
  text-decoration: none;
  border-radius: 4px;
  font-size: 13px;
  transition: all 0.2s;
}

.dropdown-menu a:hover {
  background: #f3f4f6;
  color: #6366f1;
}
</style>
