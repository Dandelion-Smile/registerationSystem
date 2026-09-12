<template>
  <div class="student-page">
    <StudentNavbar />

    <main class="main-content">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="page-title-wrap">
          <i class="fas fa-file-alt page-icon"></i>
          <div>
            <h1 class="page-title">模拟考试系统</h1>
            <p class="page-desc">提供全面的在线考试服务，包括模拟考试、正式考试、错题分析等功能，助力你的学习与成长。</p>
          </div>
        </div>
      </div>

      <!-- 次级导航 -->
      <div class="sub-tabs">
        <button
          :class="['sub-tab', { active: examTab === 'hall' }]"
          @click="examTab = 'hall'"
        >
          考试大厅
        </button>
        <button
          :class="['sub-tab', { active: examTab === 'mine' }]"
          @click="examTab = 'mine'"
        >
          我的考试
        </button>
      </div>

      <template v-if="examTab === 'hall'">
        <!-- 推荐考试 -->
        <section class="section">
          <h2 class="section-title">推荐考试</h2>
          <div class="exam-cards recommended">
            <div
              v-for="exam in recommendedExams"
              :key="exam.id"
              class="exam-card"
            >
              <h3 class="exam-card-title">{{ exam.title }}</h3>
              <p class="exam-card-desc">{{ exam.desc }}</p>
              <div class="exam-card-meta">
                <span><i class="far fa-clock"></i> {{ exam.duration }}分钟</span>
                <span><i class="fas fa-star"></i> {{ exam.score }}分</span>
              </div>
              <el-button type="primary" class="btn-join" @click="joinExam(exam)">
                参加考试
              </el-button>
            </div>
          </div>
        </section>

        <!-- 考试分类 -->
        <section class="section">
          <h2 class="section-title">考试分类</h2>
          <div class="category-grid">
            <div
              v-for="cat in categories"
              :key="cat.id"
              class="category-card"
              @click="filterByCategory(cat)"
            >
              <div class="category-icon" :class="cat.iconClass">
                <i :class="cat.icon"></i>
              </div>
              <div class="category-name">{{ cat.name }}</div>
              <div class="category-count">{{ cat.count }}门考试</div>
            </div>
          </div>
        </section>

        <!-- 全部考试 -->
        <section class="section">
          <div class="section-head">
            <h2 class="section-title">全部考试</h2>
            <span class="exam-total">共{{ allExams.length }}门考试</span>
          </div>
          <div class="exam-list">
            <div
              v-for="exam in allExams"
              :key="exam.id"
              class="exam-list-item"
            >
              <div class="exam-list-main">
                <h3 class="exam-list-title">{{ exam.title }}</h3>
                <p class="exam-list-desc">{{ exam.desc }}</p>
                <div class="exam-list-meta">
                  <span><i class="far fa-clock"></i> {{ exam.duration }}分钟</span>
                  <span><i class="fas fa-star"></i> {{ exam.score }}分</span>
                  <span><i class="fas fa-user"></i> {{ exam.attendCount }}人已考</span>
                </div>
              </div>
              <el-button type="primary" class="btn-join" @click="joinExam(exam)">
                参加考试
              </el-button>
            </div>
          </div>
        </section>
      </template>

      <template v-else>
        <!-- 我的考试 -->
        <section class="section">
          <h2 class="section-title">我的考试记录</h2>
          <div class="exam-list" v-if="myExams.length">
            <div
              v-for="item in myExams"
              :key="item.id"
              class="exam-list-item"
            >
              <div class="exam-list-main">
                <h3 class="exam-list-title">{{ item.title }}</h3>
                <p class="exam-list-desc">{{ item.desc }}</p>
                <div class="exam-list-meta">
                  <span><i class="far fa-clock"></i> {{ item.duration }}分钟</span>
                  <span><i class="fas fa-star"></i> 得分 {{ item.myScore }}/{{ item.score }}</span>
                  <span><i class="far fa-calendar"></i> {{ item.finishTime }}</span>
                </div>
              </div>
              <el-button type="primary" plain class="btn-join" @click="viewReport(item)">
                查看报告
              </el-button>
            </div>
          </div>
          <el-empty v-else description="暂无考试记录" />
        </section>
      </template>
    </main>
  </div>
</template>

<script setup>
import StudentNavbar from "@/components/StudentNavbar.vue";
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMockExamHall, getMyMockExams } from '@/api/mockExam'

const userStore = useUserStore()
const router = useRouter()
const searchKeyword = ref('')
const examTab = ref('hall')

// 分类图标映射，保持与原有静态样式一致
const categoryIconMap = {
  programming: { icon: 'fas fa-code', iconClass: 'icon-code' },
  database: { icon: 'fas fa-database', iconClass: 'icon-db' },
  cs_basic: { icon: 'fas fa-desktop', iconClass: 'icon-pc' },
  ai: { icon: 'fas fa-brain', iconClass: 'icon-ai' }
}

// 数据源
const recommendedExams = ref([])
const categories = ref([])
const allExams = ref([])
const myExams = ref([])

// 加载状态
const loadingHall = ref(false)
const loadingMine = ref(false)

async function loadHallData() {
  loadingHall.value = true
  try {
    const res = await getMockExamHall({
      keyword: searchKeyword.value.trim() || undefined
    })
    const data = res?.data || {}
    recommendedExams.value = data.recommended || []

    const rawCategories = data.categories || []
    categories.value = rawCategories.map((cat, index) => {
      const mapping = categoryIconMap[cat.code] || {}
      return {
        id: cat.id || cat.code || index,
        icon: mapping.icon || 'fas fa-book',
        iconClass: mapping.iconClass || 'icon-code',
        ...cat
      }
    })

    // exams 或 list 二选一，兼容后端命名
    allExams.value = data.exams || data.list || []
  } catch (e) {
    ElMessage.error('加载考试大厅数据失败')
  } finally {
    loadingHall.value = false
  }
}

async function loadMyExams() {
  loadingMine.value = true
  try {
    const res = await getMyMockExams()
    const data = res?.data || {}
    myExams.value = data.list || data.records || data || []
  } catch (e) {
    ElMessage.error('加载我的考试数据失败')
  } finally {
    loadingMine.value = false
  }
}

function joinExam(exam) {
  if (!exam || !exam.id) return
  router.push({
    path: `/mock-exam/take/${exam.id}`,
    query: {
      title: exam.title || '',
      duration: exam.duration || '',
      score: exam.score || ''
    }
  })
}

function viewReport(item) {
  if (!item || !item.paperId) {
    ElMessage.warning('缺少试卷信息，暂时无法查看报告')
    return
  }
  router.push({
    path: `/mock-exam/take/${item.paperId}`,
    query: {
      recordId: item.id
    }
  })
}

function filterByCategory(cat) {
  // 简单做一个前端筛选示例，真实场景可调用后端接口按分类过滤
  if (!cat || !cat.code) {
    loadHallData()
    return
  }
  const code = cat.code
  allExams.value = allExams.value.filter(exam => exam.categoryCode === code || exam.category === cat.name)
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
  loadHallData()
  loadMyExams()
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

.page-title-wrap {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.page-icon {
  font-size: 32px;
  color: #8b5cf6;
  margin-top: 4px;
}

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

.sub-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 24px;
  border-bottom: 1px solid #e5e7eb;
}

.sub-tab {
  padding: 12px 24px;
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

.section { margin-bottom: 32px; }

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.exam-total { font-size: 14px; color: #6b7280; }

.recommended {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.exam-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
}

.exam-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.exam-card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.exam-card-desc {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
  margin: 0 0 12px 0;
  flex: 1;
}

.exam-card-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 12px;
}

.exam-card-meta span { display: flex; align-items: center; gap: 4px; }

.btn-join {
  background: #8b5cf6 !important;
  border-color: #8b5cf6 !important;
  align-self: flex-end;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.category-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: all 0.3s;
}

.category-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.category-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.icon-code { background: #ede9fe; color: #7c3aed; }
.icon-db { background: #dbeafe; color: #2563eb; }
.icon-pc { background: #dcfce7; color: #16a34a; }
.icon-ai { background: #fce7f3; color: #db2777; }

.category-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.category-count { font-size: 13px; color: #6b7280; }

.exam-list { display: flex; flex-direction: column; gap: 12px; }

.exam-list-item {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.exam-list-main { flex: 1; min-width: 0; }

.exam-list-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 6px 0;
}

.exam-list-desc {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 10px 0;
  line-height: 1.5;
}

.exam-list-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #6b7280;
}

.exam-list-meta span { display: flex; align-items: center; gap: 4px; }

@media (max-width: 900px) {
  .recommended { grid-template-columns: 1fr; }
  .category-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 600px) {
  .category-grid { grid-template-columns: 1fr; }
  .exam-list-item { flex-direction: column; align-items: flex-start; }
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
