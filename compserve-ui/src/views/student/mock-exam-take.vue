<template>
  <div class="student-page">
    <StudentNavbar />

    <main class="main-content">
      <div v-if="loading" class="loading-wrapper">
        <el-skeleton :rows="6" animated />
      </div>

      <template v-else>
        <div v-if="exam" class="exam-container">
          <!-- 考试信息 -->
          <section class="exam-header">
            <div class="exam-title-row">
              <h1 class="exam-title">{{ exam.title }}</h1>
              <span class="exam-tag">模拟考试</span>
            </div>
            <p class="exam-desc">{{ exam.desc }}</p>
            <div class="exam-meta">
              <div class="meta-item">
                <i class="far fa-clock"></i>
                <span>考试时长：{{ exam.duration }} 分钟</span>
              </div>
              <div class="meta-item">
                <i class="fas fa-star"></i>
                <span>试卷总分：{{ exam.score }} 分</span>
              </div>
              <div class="meta-item">
                <i class="fas fa-list-ol"></i>
                <span>题目数量：{{ exam.questionCount || questions.length }} 题</span>
              </div>
              <div class="meta-item highlight" v-if="!isSubmitted && remainingTime > 0">
                <i class="far fa-hourglass"></i>
                <span>剩余时间：{{ formattedTime }}</span>
              </div>
              <div class="meta-item result" v-if="isSubmitted && resultInfo">
                <i class="fas fa-check-circle"></i>
                <span>本次得分：{{ resultInfo.score }} / {{ resultInfo.totalScore || exam.score }} 分</span>
              </div>
            </div>
          </section>

          <!-- 题目列表 -->
          <section class="question-section">
            <h2 class="section-title">答题区域</h2>
            <div v-if="questions.length" class="question-list">
              <div
                v-for="(q, index) in questions"
                :key="q.id || index"
                class="question-card"
              >
                <div class="question-header">
                  <span class="question-index">第 {{ index + 1 }} 题</span>
                  <span class="question-type">
                    {{ q.type === 'judge' ? '判断题' : '单选题' }}
                  </span>
                  <span class="question-score">{{ q.score || 0 }} 分</span>
                  <span
                    v-if="isSubmitted && getDetail(q.id)"
                    class="question-result"
                    :class="{ correct: getDetail(q.id).correct === 1, wrong: getDetail(q.id).correct === 0 }"
                  >
                    {{ getDetail(q.id).correct === 1 ? '回答正确' : '回答错误' }}
                  </span>
                </div>
                <div class="question-content">
                  {{ q.content }}
                </div>
                <div class="question-options" v-if="q.type !== 'judge'">
                  <el-radio-group
                    v-model="answers[q.id]"
                    class="option-group"
                    :disabled="isSubmitted"
                  >
                    <el-radio
                      v-for="opt in q._options || []"
                      :key="opt.value"
                      :label="opt.value"
                    >
                      {{ opt.label }}
                    </el-radio>
                  </el-radio-group>
                </div>
                <div class="question-options" v-else>
                  <el-radio-group
                    v-model="answers[q.id]"
                    class="option-group"
                    :disabled="isSubmitted"
                  >
                    <el-radio label="对">对</el-radio>
                    <el-radio label="错">错</el-radio>
                  </el-radio-group>
                </div>

                <!-- 判卷结果展示 -->
                <div
                  v-if="getDetail(q.id)"
                  class="question-review"
                >
                  <div class="review-row">
                    <span class="review-label">你的答案：</span>
                    <span class="review-value">
                      {{ formatUserAnswer(q, getDetail(q.id).userAnswer) || '未作答' }}
                    </span>
                  </div>
                <div class="review-row">
                  <span class="review-label">正确答案：</span>
                  <span class="review-value correct-text">
                    {{
                      formatCorrectAnswer(q, getDetail(q.id))
                        || (getDetail(q.id) && getDetail(q.id).correctAnswer)
                        || q.correctAnswer
                        || '—'
                    }}
                  </span>
                </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂未配置题目" />

            <div class="exam-actions">
              <el-button @click="goBack">返回考试大厅</el-button>
              <el-button v-if="!isSubmitted" type="primary" @click="submitExam">提交试卷</el-button>
            </div>
          </section>
        </div>
        <el-empty v-else description="未找到对应的考试信息" />
      </template>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMockExamDetail, submitMockExam, getMockExamRecordDetail } from '@/api/mockExam'
import StudentNavbar from '@/components/StudentNavbar.vue'

const route = useRoute()
const router = useRouter()

const exam = ref(null)
const questions = ref([])
const answers = ref({})
const loading = ref(false)
const isSubmitted = ref(false)
const resultInfo = ref(null)

const remainingTime = ref(0) // 单位：秒
let timer = null

const formattedTime = computed(() => {
  const total = remainingTime.value
  const m = Math.floor(total / 60)
  const s = total % 60
  const mm = m.toString().padStart(2, '0')
  const ss = s.toString().padStart(2, '0')
  return `${mm}:${ss}`
})

function startTimer(durationMinutes) {
  if (!durationMinutes || durationMinutes <= 0) return
  remainingTime.value = durationMinutes * 60
  timer = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    } else {
      clearInterval(timer)
      timer = null
      ElMessageBox.alert('考试时间已到，系统将自动提交当前作答。', '提示', {
        confirmButtonText: '确定'
      }).then(() => {
        submitExam()
      }).catch(() => {})
    }
  }, 1000)
}

async function loadExam() {
  const id = route.params.id
  if (!id) {
    ElMessage.error('考试ID缺失')
    return
  }
  loading.value = true
  try {
    const res = await getMockExamDetail(id)
    const data = res?.data || {}
    exam.value = data
    const rawQuestions = data.questions || []

    // 预处理题目：将选项拆分为 { label, value }，value 只保留选项字母 A/B/C/D，便于提交判分
    questions.value = rawQuestions.map(q => {
      const base = { ...q }
      if (q.type !== 'judge') {
        const opts = q.options || []
        base._options = opts.map(opt => {
          // 期望后端传过来形如 "A. xxx"
          if (typeof opt === 'string' && opt.length >= 2 && opt[1] === '.') {
            const key = opt[0]
            return { label: opt, value: key }
          }
          return { label: opt, value: opt }
        })
      }
      return base
    })

    // 初始化答案对象
    const initialAnswers = {}
    questions.value.forEach(q => {
      if (q.type === 'multiple') {
        initialAnswers[q.id] = []
      } else {
        initialAnswers[q.id] = ''
      }
    })
    answers.value = initialAnswers

    // 启动倒计时
    startTimer(data.duration || 0)
  } catch (e) {
    ElMessage.error('加载考试信息失败')
  } finally {
    loading.value = false
  }
}

// 根据记录ID加载一次历史考试（查看报告）
async function loadRecord() {
  const recordId = route.query.recordId
  if (!recordId) {
    await loadExam()
    return
  }
  loading.value = true
  try {
    const res = await getMockExamRecordDetail(recordId)
    const data = res?.data || {}

    // 试卷信息
    const examData = data.exam || {}
    exam.value = examData

    // 题目
    const rawQuestions = data.questions || []
    questions.value = rawQuestions.map(q => {
      const base = { ...q }
      if (q.type !== 'judge') {
        const opts = q.options || []
        base._options = opts.map(opt => {
          if (typeof opt === 'string' && opt.length >= 2 && opt[1] === '.') {
            const key = opt[0]
            return { label: opt, value: key }
          }
          return { label: opt, value: opt }
        })
      }
      return base
    })

    // 判分结果
    resultInfo.value = {
      recordId: data.recordId,
      paperId: data.paperId,
      score: data.score,
      totalScore: data.totalScore,
      paperTitle: data.paperTitle,
      submitTime: data.submitTime,
      details: data.details || []
    }
    isSubmitted.value = true

    // 根据明细还原当时选择的答案，用于高亮选项
    const initialAnswers = {}
    const detailMap = {}
    ;(data.details || []).forEach(d => {
      detailMap[d.questionId] = d
    })
    questions.value.forEach(q => {
      const detail = detailMap[q.id]
      if (!detail) {
        if (q.type === 'multiple') {
          initialAnswers[q.id] = []
        } else {
          initialAnswers[q.id] = ''
        }
        return
      }
      const ua = detail.userAnswer || ''
      if (q.type === 'judge') {
        if (ua.toUpperCase() === 'T') initialAnswers[q.id] = '对'
        else if (ua.toUpperCase() === 'F') initialAnswers[q.id] = '错'
        else initialAnswers[q.id] = ''
      } else if (q.type === 'multiple') {
        initialAnswers[q.id] = ua ? ua.split(',') : []
      } else {
        // 单选题：直接用字母 A/B/C/D
        initialAnswers[q.id] = ua
      }
    })
    answers.value = initialAnswers
  } catch (e) {
    ElMessage.error('加载考试报告失败')
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/mock-exam')
}

async function submitExam() {
  if (!questions.value.length) {
    ElMessage.warning('当前试卷未配置题目，无法提交')
    return
  }

  // 统计已作答题目数量：逐题检查当前答案
  const answeredCount = questions.value.filter(q => {
    const val = answers.value[q.id]
    if (q.type === 'multiple') {
      if (Array.isArray(val)) {
        return val.length > 0
      }
      // 当前多选题仍用单选控件时，val 可能是字符串，也视为已作答
      return val !== '' && val != null
    }
    return val !== '' && val != null
  }).length

  if (answeredCount === 0) {
    ElMessage.warning('请先作答至少一题再提交')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定提交试卷吗？已作答 ${answeredCount} / ${questions.value.length} 题。`,
      '提交确认',
      { confirmButtonText: '提交', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  // 组装提交给后端的答案：judge -> T/F，single -> A/B/C/D，multiple -> A,B,C
  const payload = {
    answers: questions.value.map(q => {
      const val = answers.value[q.id]
      let answer = ''
      if (q.type === 'judge') {
        if (val === '对') answer = 'T'
        else if (val === '错') answer = 'F'
        else answer = ''
      } else if (q.type === 'multiple') {
        if (Array.isArray(val)) {
          answer = val.join(',')
        }
      } else {
        // 单选题
        answer = val || ''
      }
      return {
        questionId: q.id,
        answer
      }
    })
  }

  try {
    const res = await submitMockExam(exam.value.id, payload)
    const data = res?.data || {}
    // 停止计时
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    isSubmitted.value = true
    resultInfo.value = data

    const score = data.score ?? 0
    const total = data.totalScore ?? exam.value.score ?? 0
    ElMessage.success(`提交成功，本次得分：${score} / ${total} 分`)
  } catch (e) {
    ElMessage.error('提交试卷失败，请稍后重试')
  }
}

function getDetail(questionId) {
  if (!resultInfo.value || !Array.isArray(resultInfo.value.details)) return null
  const qid = Number(questionId)
  const detail = resultInfo.value.details.find(d => Number(d.questionId) === qid) || null
  // 调试日志：确认每题能否匹配到明细
  // eslint-disable-next-line no-console
  console.log('[MockExamTake] getDetail', { questionId, qid, detail })
  return detail
}

function formatUserAnswer(q, val) {
  if (!val) return ''
  if (q.type === 'judge') {
    if (val.toUpperCase() === 'T') return '对'
    if (val.toUpperCase() === 'F') return '错'
    return val
  }
  // 单选、多选：直接显示字母，多个用逗号
  return val
}

function formatCorrectAnswer(q, detail) {
  const raw =
    (detail && detail.correctAnswer) ||
    q.correctAnswer ||
    ''
  if (!raw) return ''
  if (q.type === 'judge') {
    if (raw.toUpperCase() === 'T') return '对'
    if (raw.toUpperCase() === 'F') return '错'
    return raw
  }
  // 其它题型：先直接返回字母（A / A,B），确保能看到正确答案
  return raw
}

onMounted(() => {
  // 如果带有 recordId，则进入“查看报告”模式；否则为正常考试模式
  if (route.query.recordId) {
    loadRecord()
  } else {
    loadExam()
  }
})

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
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

.logo-text {
  font-size: 18px;
}

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

.nav-item:hover,
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

.nav-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-container {
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-avatar-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid #ffffff;
  box-shadow: 0 0 0 2px rgba(255,255,255,0.3);
}

.main-content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px 16px 40px;
}

.loading-wrapper {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.exam-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.exam-header {
  background: #fff;
  border-radius: 12px;
  padding: 24px 24px 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.exam-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.exam-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.exam-tag {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(139, 92, 246, 0.1);
  color: #7c3aed;
}

.exam-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 12px;
}

.exam-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 24px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #4b5563;
}

.meta-item i {
  color: #8b5cf6;
}

.meta-item.highlight {
  color: #b91c1c;
}

.meta-item.highlight i {
  color: #dc2626;
}

.question-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.question-card {
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  padding: 16px 16px 14px;
  transition: all 0.2s;
}

.question-card:hover {
  border-color: #c4b5fd;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.question-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
}

.question-index {
  font-weight: 600;
  color: #4f46e5;
}

.question-type {
  padding: 2px 8px;
  border-radius: 999px;
  background: #eef2ff;
  color: #4f46e5;
}

.question-score {
  margin-left: auto;
  color: #6b7280;
}

.question-content {
  font-size: 14px;
  color: #111827;
  margin-bottom: 10px;
}

.question-options {
  margin-top: 8px;
}

.option-group :deep(.el-radio) {
  display: block;
  margin: 10px 0;
  color: #111827;
  font-size: 14px;
  padding: 6px 10px;
  border-radius: 8px;
  background-color: #f9fafb;
}

.option-group :deep(.el-radio__label) {
  color: #111827;
  font-size: 14px;
}

.option-group :deep(.el-radio.is-checked) {
  background-color: #eef2ff;
}

.option-group :deep(.el-radio__inner) {
  border-color: #8b5cf6;
}

.option-group :deep(.el-radio.is-checked .el-radio__inner) {
  background-color: #8b5cf6;
  border-color: #8b5cf6;
}

.exam-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 640px) {
  .main-content {
    padding: 16px 12px 32px;
  }

  .exam-header,
  .question-section {
    padding: 16px 12px;
  }

  .exam-meta {
    flex-direction: column;
    gap: 8px;
  }
}

.dropdown {
  position: relative;
}

.dropdown::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  height: 20px;
  background: transparent;
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

