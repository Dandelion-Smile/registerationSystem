<template>
  <div class="student-page">
    <StudentNavbar />
    
    <!-- 主内容区域 -->
    <div class="content-container">
      <div class="content-box">
        <!-- 页面标题 -->
        <div class="page-header">
          <div class="header-decoration"></div>
          <h1 class="page-title">学生能力画像</h1>
          <p class="page-subtitle">基于竞赛表现的综合能力评估与分析</p>
          <div class="header-stats">
            <div class="stat-item">
              <span class="stat-value">{{ studentInfo.competitionCount }}</span>
              <span class="stat-label">参与竞赛</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value">{{ studentInfo.improvement }}%</span>
              <span class="stat-label">能力提升</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value">{{ totalScore }}</span>
              <span class="stat-label">综合评分</span>
            </div>
          </div>
        </div>

        <!-- 学生基本信息卡片 -->
        <section class="section-card profile-card">
          <div class="student-info-header">
            <div class="avatar-wrapper-lg">
              <div class="avatar-lg">{{ studentInfo.name.charAt(0) }}</div>
              <div class="avatar-glow"></div>
            </div>
            <div class="student-info">
              <h3 class="student-name">{{ studentInfo.name }}</h3>
              <div class="student-meta">
                <span>{{ studentInfo.department }} | {{ studentInfo.grade }}级</span>
                <el-tag type="success" size="small" class="org-tag">{{ studentInfo.organization }}</el-tag>
              </div>
              <div class="student-badge">
                <el-tag size="small" class="badge-expert">竞赛达人</el-tag>
                <el-tag size="small" class="badge-active">活跃成员</el-tag>
              </div>
            </div>
            <div class="student-update">
              <div class="update-card">
                <div class="update-icon">
                  <i class="fa fa-calendar"></i>
                </div>
                <div class="update-info">
                  <div class="update-label">最近更新</div>
                  <div class="update-date">{{ studentInfo.lastUpdate }}</div>
                </div>
              </div>
              <div class="improvement-badge">
                <i class="fa fa-arrow-up"></i>
                <span>较上月提升 {{ studentInfo.improvement }}%</span>
              </div>
            </div>
          </div>
        </section>

        <!-- 能力雷达图和评分 -->
        <div class="grid-layout">
          <!-- 雷达图 -->
          <section class="section-card radar-section">
            <div class="section-header">
              <div class="section-icon">
                <i class="fas fa-star"></i>
              </div>
              <h2 class="section-title">综合能力评估</h2>
              <div class="section-badge">
                <span class="badge-text">AI智能分析</span>
              </div>
            </div>
            <div class="radar-container">
              <div class="chart-wrapper">
                <canvas ref="radarChartRef"></canvas>
              </div>
              <p class="chart-desc">该雷达图展示了您在五个关键维度的能力评估情况，基于您参与的{{ studentInfo.competitionCount }}场竞赛数据分析得出。</p>
            </div>
          </section>

          <!-- 能力维度评分 -->
          <section class="section-card score-card">
            <div class="section-header">
              <div class="section-icon">
                <i class="fas fa-bar-chart"></i>
              </div>
              <h2 class="section-title">能力维度评分</h2>
            </div>
            <div class="progress-list">
              <div v-for="(item, index) in abilityScores" :key="index" class="progress-item">
                <div class="progress-header">
                  <div class="progress-icon" :style="{ background: getScoreColor(item.score) }">
                    <i :class="getScoreIcon(item.score)"></i>
                  </div>
                  <span class="progress-label">{{ item.name }}</span>
                  <span class="progress-value">{{ item.score }}<span class="value-unit">/100</span></span>
                </div>
                <div class="progress-bar">
                  <div class="progress-track">
                    <div class="progress-fill" :style="{ width: item.score + '%', background: getScoreGradient(item.score) }"></div>
                    <div class="progress-glow" :style="{ width: item.score + '%' }"></div>
                  </div>
                </div>
                <div class="progress-level">{{ getScoreLevel(item.score) }}</div>
              </div>
            </div>
          </section>
        </div>

        <!-- 能力维度详情和成长轨迹 -->
        <div class="grid-layout">
          <!-- 能力维度详情 -->
          <section class="section-card dimension-card">
            <div class="section-header">
              <div class="section-icon">
                <i class="fas fa-search-plus"></i>
              </div>
              <h2 class="section-title">能力维度详情</h2>
            </div>
            <div class="dimension-cards">
              <div 
                v-for="(dimension, index) in dimensions" 
                :key="index"
                class="dimension-card-item"
                :class="{ active: activeDimension === dimension.key }"
                @click="selectDimension(dimension.key)"
              >
                <div class="dimension-icon-wrap">
                  <div class="dimension-icon" :style="{ background: dimension.bgColor }">
                    <i :class="dimension.icon" :style="{ color: dimension.iconColor }"></i>
                  </div>
                  <div v-if="activeDimension === dimension.key" class="dimension-active-dot"></div>
                </div>
                <div class="dimension-info">
                  <h4 class="dimension-name">{{ dimension.name }}</h4>
                  <p class="dimension-data">{{ dimension.dataCount }}项竞赛数据</p>
                </div>
                <div class="dimension-score">
                  <span :style="{ color: dimension.iconColor }">{{ dimension.score }}</span>
                </div>
              </div>
            </div>
            <div class="dimension-detail">
              <div class="detail-header">
                <h4 class="detail-title">
                  <i :class="currentDimension.icon" :style="{ color: currentDimension.iconColor }"></i>
                  {{ currentDimension.name }}
                </h4>
                <el-tag :type="getTagType(currentDimension.score)" size="small" class="detail-tag">{{ currentDimension.score }}/100</el-tag>
              </div>
              <div class="detail-content">
                <div class="detail-grid">
                  <div class="detail-item">
                    <div class="detail-item-header">
                      <h5>{{ currentDimension.subTitle1 }}</h5>
                      <div class="detail-item-icon" :style="{ background: currentDimension.bgColor }">
                        <i class="fa fa-file-text" :style="{ color: currentDimension.iconColor }"></i>
                      </div>
                    </div>
                    <div class="detail-list">
                      <div v-for="(item, idx) in currentDimension.data1" :key="idx" class="detail-row">
                        <span class="detail-row-label">{{ item.name }}</span>
                        <span class="detail-row-value" :style="{ color: currentDimension.iconColor }">{{ item.value }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="detail-item">
                    <div class="detail-item-header">
                      <h5>{{ currentDimension.subTitle2 }}</h5>
                      <div class="detail-item-icon" :style="{ background: currentDimension.bgColor }">
                        <i class="fa fa-trophy" :style="{ color: currentDimension.iconColor }"></i>
                      </div>
                    </div>
                    <div class="detail-list">
                      <div v-for="(item, idx) in currentDimension.data2" :key="idx" class="detail-row">
                        <span class="detail-row-label">{{ item.name }}</span>
                        <span class="detail-row-value" :style="{ color: currentDimension.iconColor }">{{ item.value }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="comparison-section">
                  <div class="comparison-header">
                    <h5>同专业对比</h5>
                    <span class="comparison-rank">排名前 {{ getRankPercent(currentDimension.score) }}%</span>
                  </div>
                  <div class="comparison-bar-wrapper">
                    <div class="comparison-bar">
                      <div class="comparison-fill-average" :style="{ width: currentDimension.average + '%' }"></div>
                      <div class="comparison-fill" :style="{ width: currentDimension.score + '%' }"></div>
                      <div class="comparison-fill-top" :style="{ width: currentDimension.top10 + '%' }"></div>
                    </div>
                    <div class="comparison-markers">
                      <span class="marker average-marker" :style="{ left: currentDimension.average + '%' }">平均</span>
                      <span class="marker top-marker" :style="{ left: currentDimension.top10 + '%' }">Top10%</span>
                    </div>
                  </div>
                  <div class="comparison-info">
                    <span>专业平均: <strong>{{ currentDimension.average }}</strong></span>
                    <span>专业前10%: <strong>{{ currentDimension.top10 }}</strong></span>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- 成长轨迹 -->
          <section class="section-card growth-card">
            <div class="section-header">
              <div class="section-icon">
                <i class="fas fa-line-chart"></i>
              </div>
              <h2 class="section-title">成长轨迹</h2>
              <div class="section-period">
                <span>近一年</span>
              </div>
            </div>
            <div class="growth-container">
              <div class="chart-wrapper">
                <canvas ref="growthChartRef"></canvas>
              </div>
              <div class="growth-tip">
                <div class="tip-icon">
                  <i class="fa fa-lightbulb-o"></i>
                </div>
                <div class="tip-content">
                  <p>您的<strong>创新思维能力</strong>和<strong>团队协作能力</strong>在过去一年中提升显著。</p>
                  <p class="tip-suggestion">建议继续保持创新思维训练，可尝试参与更多跨学科竞赛。</p>
                </div>
              </div>
            </div>
          </section>
        </div>

        <!-- 能力洞察建议 -->
        <section class="section-card insight-card">
          <div class="section-header">
            <div class="section-icon">
              <i class="fas fa-lightbulb"></i>
            </div>
            <h2 class="section-title">能力洞察与建议</h2>
          </div>
          <div class="insight-content">
            <div class="insight-grid">
              <div class="insight-item">
                <div class="insight-icon strong">
                  <i class="fa fa-thumbs-up"></i>
                </div>
                <div class="insight-info">
                  <h4>优势能力</h4>
                  <p>创新思维能力突出，建议重点培养，可参与创新性竞赛项目</p>
                </div>
              </div>
              <div class="insight-item">
                <div class="insight-icon improve">
                  <i class="fa fa-arrow-up"></i>
                </div>
                <div class="insight-info">
                  <h4>待提升能力</h4>
                  <p>实践操作能力有提升空间，建议多参与实践类项目和实验课程</p>
                </div>
              </div>
              <div class="insight-item">
                <div class="insight-icon balance">
                  <i class="fa fa-balance-scale"></i>
                </div>
                <div class="insight-info">
                  <h4>综合评价</h4>
                  <p>各维度发展均衡，团队协作能力优秀，适合担任团队负责人</p>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import StudentNavbar from "@/components/StudentNavbar.vue";
import { ref, onMounted, computed } from 'vue';
import useUserStore from '@/store/modules/user';

const userStore = useUserStore();

// Mock数据
const studentInfo = ref({
  name: '张三',
  department: '计算机科学系',
  grade: '2023',
  organization: '竞赛先锋组织',
  lastUpdate: '2025-7-15',
  improvement: 8,
  competitionCount: 5
});

const abilityScores = ref([
  { name: '专业知识水平', score: 82 },
  { name: '实践操作能力', score: 76 },
  { name: '团队协作能力', score: 88 },
  { name: '创新思维能力', score: 91 },
  { name: '问题解决能力', score: 79 }
]);

const totalScore = computed(() => {
  const sum = abilityScores.value.reduce((acc, item) => acc + item.score, 0);
  return Math.round(sum / abilityScores.value.length);
});

const activeDimension = ref('knowledge');

const dimensions = ref([
  {
    key: 'knowledge',
    name: '专业知识水平',
    icon: 'fa fa-book',
    bgColor: '#dbeafe',
    iconColor: '#3b82f6',
    dataCount: 5,
    score: 82,
    subTitle1: '相关课程成绩',
    data1: [
      { name: '数据结构', value: '92' },
      { name: '算法设计与分析', value: '88' },
      { name: '数据库系统', value: '85' }
    ],
    subTitle2: '竞赛表现',
    data2: [
      { name: 'ACM程序设计大赛', value: '省二等奖' },
      { name: '数据科学竞赛', value: '全国三等奖' }
    ],
    average: 76,
    top10: 89
  },
  {
    key: 'practice',
    name: '实践操作能力',
    icon: 'fa fa-flask',
    bgColor: '#d1fae5',
    iconColor: '#10b981',
    dataCount: 3,
    score: 76,
    subTitle1: '实验课程表现',
    data1: [
      { name: '程序设计实验', value: '85' },
      { name: '数据库实验', value: '88' }
    ],
    subTitle2: '竞赛实践表现',
    data2: [
      { name: '黑客马拉松', value: '24小时完成' },
      { name: '创新项目实践', value: '优秀' }
    ],
    average: 72,
    top10: 84
  },
  {
    key: 'teamwork',
    name: '团队协作能力',
    icon: 'fa fa-users',
    bgColor: '#ede9fe',
    iconColor: '#8b5cf6',
    dataCount: 4,
    score: 88,
    subTitle1: '团队角色',
    data1: [
      { name: '团队队长', value: '3次' },
      { name: '核心成员', value: '5次' }
    ],
    subTitle2: '团队评价',
    data2: [
      { name: '沟通能力', value: '优秀' },
      { name: '责任心', value: '优秀' }
    ],
    average: 75,
    top10: 86
  }
]);

const currentDimension = computed(() => {
  return dimensions.value.find(d => d.key === activeDimension.value) || dimensions.value[0];
});

const selectDimension = (key) => {
  activeDimension.value = key;
};

const getTagType = (score) => {
  if (score >= 90) return 'success';
  if (score >= 80) return 'primary';
  if (score >= 70) return 'warning';
  return 'danger';
};

const getScoreColor = (score) => {
  if (score >= 90) return 'linear-gradient(135deg, #10b981 0%, #059669 100%)';
  if (score >= 80) return 'linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%)';
  if (score >= 70) return 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)';
  return 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)';
};

const getScoreIcon = (score) => {
  if (score >= 90) return 'fa fa-trophy';
  if (score >= 80) return 'fa fa-star';
  if (score >= 70) return 'fa fa-check';
  return 'fa fa-arrow-up';
};

const getScoreGradient = (score) => {
  if (score >= 90) return 'linear-gradient(90deg, #10b981 0%, #34d399 100%)';
  if (score >= 80) return 'linear-gradient(90deg, #8b5cf6 0%, #a78bfa 100%)';
  if (score >= 70) return 'linear-gradient(90deg, #f59e0b 0%, #fbbf24 100%)';
  return 'linear-gradient(90deg, #ef4444 0%, #f87171 100%)';
};

const getScoreLevel = (score) => {
  if (score >= 90) return '优秀';
  if (score >= 80) return '良好';
  if (score >= 70) return '中等';
  return '待提升';
};

const getRankPercent = (score) => {
  return Math.round((100 - score) / 1.5);
};

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout();
  }
};

// 图表初始化
let radarChart = null;
let growthChart = null;
const radarChartRef = ref(null);
const growthChartRef = ref(null);

onMounted(() => {
  // 加载Chart.js
  const script = document.createElement('script');
  script.src = 'https://cdn.jsdelivr.net/npm/chart.js';
  script.onload = () => {
    initRadarChart();
    initGrowthChart();
  };
  document.head.appendChild(script);
});

const initRadarChart = () => {
  if (!radarChartRef.value) return;
  
  const ctx = radarChartRef.value.getContext('2d');
  radarChart = new window.Chart(ctx, {
    type: 'radar',
    data: {
      labels: ['专业知识', '实践操作', '团队协作', '创新思维', '问题解决'],
      datasets: [{
        label: '您的评分',
        data: [82, 76, 88, 91, 79],
        backgroundColor: 'rgba(139, 92, 246, 0.2)',
        borderColor: 'rgba(139, 92, 246, 1)',
        borderWidth: 2,
        pointBackgroundColor: '#fff',
        pointBorderColor: 'rgba(139, 92, 246, 1)',
        pointBorderWidth: 2,
        pointRadius: 4,
        pointHoverRadius: 6
      }, {
        label: '专业平均',
        data: [76, 72, 75, 78, 74],
        backgroundColor: 'rgba(99, 102, 241, 0.1)',
        borderColor: 'rgba(99, 102, 241, 0.4)',
        borderWidth: 1.5,
        borderDash: [5, 5],
        pointRadius: 0
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        r: {
          angleLines: {
            display: true,
            color: 'rgba(0, 0, 0, 0.08)'
          },
          grid: {
            color: 'rgba(0, 0, 0, 0.08)'
          },
          suggestedMin: 50,
          suggestedMax: 100,
          ticks: {
            stepSize: 20,
            backdropColor: 'transparent',
            font: { size: 11 },
            color: '#6b7280'
          },
          pointLabels: {
            font: { size: 12, weight: 500 },
            color: '#374151'
          }
        }
      },
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            boxWidth: 12,
            padding: 16,
            font: { size: 11 },
            usePointStyle: true,
            pointStyle: 'circle',
            color: '#374151'
          }
        },
        tooltip: {
          backgroundColor: 'rgba(139, 92, 246, 0.95)',
          titleFont: { size: 13, weight: 600 },
          bodyFont: { size: 12 },
          padding: 12,
          cornerRadius: 8,
          usePointStyle: true
        }
      }
    }
  });
};

const initGrowthChart = () => {
  if (!growthChartRef.value) return;
  
  const ctx = growthChartRef.value.getContext('2d');
  growthChart = new window.Chart(ctx, {
    type: 'line',
    data: {
      labels: ['2025-01', '2025-03', '2025-05', '2025-07', '2025-09', '2025-11'],
      datasets: [
        {
          label: '专业知识',
          data: [70, 72, 75, 78, 80, 82],
          borderColor: '#6366F1',
          backgroundColor: 'rgba(99, 102, 241, 0.08)',
          borderWidth: 2.5,
          pointBackgroundColor: '#fff',
          pointBorderColor: '#6366F1',
          pointBorderWidth: 2,
          pointRadius: 5,
          pointHoverRadius: 7,
          tension: 0.4,
          fill: true
        },
        {
          label: '实践操作',
          data: [65, 68, 70, 72, 74, 76],
          borderColor: '#10B981',
          backgroundColor: 'rgba(16, 185, 129, 0.08)',
          borderWidth: 2.5,
          pointBackgroundColor: '#fff',
          pointBorderColor: '#10B981',
          pointBorderWidth: 2,
          pointRadius: 5,
          pointHoverRadius: 7,
          tension: 0.4,
          fill: true
        },
        {
          label: '团队协作',
          data: [75, 78, 82, 85, 86, 88],
          borderColor: '#8B5CF6',
          backgroundColor: 'rgba(139, 92, 246, 0.08)',
          borderWidth: 2.5,
          pointBackgroundColor: '#fff',
          pointBorderColor: '#8B5CF6',
          pointBorderWidth: 2,
          pointRadius: 5,
          pointHoverRadius: 7,
          tension: 0.4,
          fill: true
        },
        {
          label: '创新思维',
          data: [80, 82, 85, 88, 90, 91],
          borderColor: '#EC4899',
          backgroundColor: 'rgba(236, 72, 153, 0.08)',
          borderWidth: 2.5,
          pointBackgroundColor: '#fff',
          pointBorderColor: '#EC4899',
          pointBorderWidth: 2,
          pointRadius: 5,
          pointHoverRadius: 7,
          tension: 0.4,
          fill: true
        },
        {
          label: '问题解决',
          data: [70, 72, 74, 76, 77, 79],
          borderColor: '#F59E0B',
          backgroundColor: 'rgba(245, 158, 11, 0.08)',
          borderWidth: 2.5,
          pointBackgroundColor: '#fff',
          pointBorderColor: '#F59E0B',
          pointBorderWidth: 2,
          pointRadius: 5,
          pointHoverRadius: 7,
          tension: 0.4,
          fill: true
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: false,
          min: 60,
          max: 100,
          grid: {
            color: 'rgba(0, 0, 0, 0.06)',
            drawBorder: false,
            lineWidth: 1
          },
          ticks: {
            font: { size: 11 },
            stepSize: 10,
            color: '#6b7280',
            backdropColor: 'transparent'
          }
        },
        x: {
          grid: {
            display: false,
            drawBorder: false
          },
          ticks: {
            font: { size: 11 },
            color: '#6b7280'
          }
        }
      },
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            boxWidth: 14,
            padding: 16,
            font: { size: 11 },
            usePointStyle: true,
            pointStyle: 'circle',
            color: '#374151'
          }
        },
        tooltip: {
          backgroundColor: 'rgba(139, 92, 246, 0.95)',
          titleFont: { size: 13, weight: 600 },
          bodyFont: { size: 12 },
          padding: 12,
          cornerRadius: 8,
          usePointStyle: true
        }
      },
      interaction: {
        intersect: false,
        mode: 'index'
      },
      elements: {
        line: {
          borderWidth: 2.5
        }
      }
    }
  });
};
</script>

<style scoped>
.student-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f4f8 50%, #ede9fe 100%);
  position: relative;
  overflow-x: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: fixed;
  top: -200px;
  right: -200px;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.15) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
}

.bg-decoration-2 {
  position: fixed;
  bottom: -150px;
  left: -150px;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.12) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
}

/* 顶部导航栏 */
.header-nav {
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  box-shadow: 0 4px 20px rgba(139, 92, 246, 0.3);
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1000;
}

.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-section i {
  font-size: 26px;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.logo-text {
  font-size: 22px;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item {
  position: relative;
  padding: 0 20px;
  height: 64px;
  display: flex;
  align-items: center;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  border-radius: 8px;
}

.nav-item:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.12);
  transform: translateY(-1px);
}

.nav-item.active {
  color: #fff;
  background: rgba(255, 255, 255, 0.18);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 32px;
  height: 3px;
  background: linear-gradient(90deg, #fff 0%, rgba(255, 255, 255, 0.6) 100%);
  border-radius: 2px;
}

.dropdown {
  position: relative;
}

.dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 4px;
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  min-width: 160px;
  opacity: 0;
  visibility: hidden;
  transform: translateX(-50%) translateY(-10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1000;
  overflow: hidden;
}

.dropdown:hover .dropdown-menu {
  opacity: 1;
  visibility: visible;
  transform: translateX(-50%) translateY(0);
}

.dropdown-menu a {
  display: block;
  padding: 12px 16px;
  color: #374151;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s ease;
}

.dropdown-menu a:hover {
  background: #f5f3ff;
  padding-left: 20px;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-container {
  cursor: pointer;
}

.avatar-wrapper {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.avatar-wrapper:hover {
  border-color: #fff;
  transform: scale(1.05);
}

.user-avatar-circle {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 主内容区域 */
.content-container {
  padding: 32px 24px;
  position: relative;
  z-index: 1;
}

.content-box {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
  position: relative;
}

.header-decoration {
  position: absolute;
  top: -8px;
  left: 0;
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #8b5cf6 0%, #6366f1 100%);
  border-radius: 2px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 12px 0;
  letter-spacing: -0.5px;
}

.page-subtitle {
  font-size: 15px;
  color: #6b7280;
  margin: 0 0 24px 0;
}

.header-stats {
  display: flex;
  align-items: center;
  gap: 32px;
  background: #fff;
  padding: 20px 24px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #8b5cf6;
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #e5e7eb;
}

/* 卡片样式 */
.section-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.section-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #faf5ff 0%, #f0f2ff 100%);
}

.section-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
}

.section-title {
  font-size: 17px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.section-badge {
  margin-left: auto;
}

.badge-text {
  padding: 4px 12px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e;
  font-size: 12px;
  font-weight: 500;
  border-radius: 20px;
}

.section-period {
  margin-left: auto;
  padding: 4px 12px;
  background: #f3f4f6;
  color: #6b7280;
  font-size: 12px;
  border-radius: 8px;
}

/* 学生信息卡片 */
.profile-card {
  background: linear-gradient(135deg, #fff 0%, #faf5ff 100%);
}

.student-info-header {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 28px 32px;
}

.avatar-wrapper-lg {
  position: relative;
}

.avatar-lg {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32px;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.4);
  position: relative;
  z-index: 1;
}

.avatar-glow {
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.3) 0%, transparent 70%);
  border-radius: 50%;
  z-index: 0;
  animation: pulse 3s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

.student-info {
  flex: 1;
}

.student-name {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 10px 0;
}

.student-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 12px;
}

.org-tag {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #065f46 !important;
  border: none;
}

.student-badge {
  display: flex;
  gap: 8px;
}

.badge-expert {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e !important;
  border: none;
}

.badge-active {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1d4ed8 !important;
  border: none;
}

.student-update {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-end;
}

.update-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #f9fafb;
  border-radius: 12px;
}

.update-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4f46e5;
}

.update-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.update-label {
  font-size: 12px;
  color: #9ca3af;
}

.update-date {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.improvement-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #065f46;
  font-size: 13px;
  font-weight: 500;
  border-radius: 20px;
}

.improvement-badge i {
  font-size: 14px;
}

/* 网格布局 */
.grid-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

@media (max-width: 1024px) {
  .grid-layout {
    grid-template-columns: 1fr;
  }
}

/* 雷达图区域 */
.radar-section {
  grid-column: span 1;
}

.radar-container {
  padding: 24px;
  height: 320px;
  position: relative;
}

.chart-wrapper {
  width: 100%;
  height: 260px;
}

.radar-container canvas {
  width: 100% !important;
  height: 260px !important;
}

.chart-desc {
  font-size: 13px;
  color: #6b7280;
  margin: 12px 0 0 0;
  text-align: center;
  padding: 0 40px;
  line-height: 1.6;
}

/* 评分卡片 */
.score-card {
  display: flex;
  flex-direction: column;
}

.progress-list {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  flex: 1;
}

.progress-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.progress-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-icon {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 13px;
}

.progress-label {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.progress-value {
  font-size: 16px;
  font-weight: 700;
  color: #8b5cf6;
}

.value-unit {
  font-size: 12px;
  font-weight: 400;
  color: #9ca3af;
}

.progress-bar {
  width: 100%;
}

.progress-track {
  position: relative;
  height: 10px;
  background: #f3f4f6;
  border-radius: 6px;
  overflow: visible;
}

.progress-fill {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  border-radius: 6px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.progress-glow {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: linear-gradient(90deg, rgba(139, 92, 246, 0.3) 0%, transparent 100%);
  border-radius: 6px;
  filter: blur(8px);
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.progress-level {
  font-size: 12px;
  color: #9ca3af;
  text-align: right;
}

/* 能力维度卡片 */
.dimension-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
}

@media (max-width: 768px) {
  .dimension-cards {
    grid-template-columns: 1fr;
  }
}

.dimension-card-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 14px;
  border: 2px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.dimension-card-item:hover {
  border-color: #8b5cf6;
  background: #faf5ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(139, 92, 246, 0.15);
}

.dimension-card-item.active {
  border-color: #8b5cf6;
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 100%);
  box-shadow: 0 4px 20px rgba(139, 92, 246, 0.2);
}

.dimension-icon-wrap {
  position: relative;
}

.dimension-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.dimension-active-dot {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 14px;
  height: 14px;
  background: #8b5cf6;
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.4);
}

.dimension-info {
  flex: 1;
}

.dimension-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.dimension-data {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}

.dimension-score {
  font-size: 20px;
  font-weight: 700;
}

/* 维度详情 */
.dimension-detail {
  padding: 24px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-tag {
  font-weight: 600;
}

.detail-content {
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
  border-radius: 14px;
  padding: 20px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

.detail-item {
  background: #fff;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.detail-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.detail-item h5 {
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  margin: 0;
}

.detail-item-icon {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row-label {
  font-size: 13px;
  color: #374151;
}

.detail-row-value {
  font-size: 13px;
  font-weight: 600;
}

/* 对比区域 */
.comparison-section {
  border-top: 1px solid #e5e7eb;
  padding-top: 20px;
}

.comparison-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.comparison-header h5 {
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  margin: 0;
}

.comparison-rank {
  font-size: 12px;
  font-weight: 600;
  color: #8b5cf6;
  padding: 4px 10px;
  background: #f5f3ff;
  border-radius: 12px;
}

.comparison-bar-wrapper {
  position: relative;
  margin-bottom: 8px;
}

.comparison-bar {
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  position: relative;
  overflow: visible;
}

.comparison-fill-average {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: #d1d5db;
  border-radius: 4px;
}

.comparison-fill {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: linear-gradient(90deg, #8b5cf6 0%, #6366f1 100%);
  border-radius: 4px;
}

.comparison-fill-top {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: transparent;
  border-right: 2px dashed #8b5cf6;
}

.comparison-markers {
  position: absolute;
  top: 12px;
  left: 0;
  right: 0;
}

.marker {
  position: absolute;
  font-size: 11px;
  color: #9ca3af;
  transform: translateX(-50%);
}

.comparison-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #6b7280;
}

.comparison-info strong {
  color: #374151;
}

/* 成长轨迹 */
.growth-card {
  display: flex;
  flex-direction: column;
}

.growth-container {
  padding: 20px 24px;
  flex: 1;
}

.growth-container canvas {
  width: 100% !important;
  height: 240px !important;
}

.growth-tip {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 12px;
  margin-top: 16px;
  border-left: 4px solid #f59e0b;
}

.tip-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f59e0b;
  font-size: 18px;
  flex-shrink: 0;
}

.tip-content {
  flex: 1;
}

.tip-content p {
  font-size: 13px;
  color: #92400e;
  margin: 0 0 6px 0;
  line-height: 1.5;
}

.tip-content p:last-child {
  margin-bottom: 0;
}

.tip-content strong {
  color: #78350f;
}

.tip-suggestion {
  font-style: italic;
  opacity: 0.8;
}

/* 能力洞察卡片 */
.insight-card {
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
}

.insight-card .section-header {
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.insight-card .section-title {
  color: #fff;
}

.insight-card .section-icon {
  background: rgba(255, 255, 255, 0.2);
}

.insight-content {
  padding: 24px;
}

.insight-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

@media (max-width: 900px) {
  .insight-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 600px) {
  .insight-grid {
    grid-template-columns: 1fr;
  }
}

.insight-item {
  display: flex;
  gap: 14px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 14px;
  transition: all 0.3s ease;
}

.insight-item:hover {
  background: rgba(255, 255, 255, 0.18);
  transform: translateY(-2px);
}

.insight-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.insight-icon.strong {
  background: rgba(34, 197, 94, 0.3);
  color: #86efac;
}

.insight-icon.improve {
  background: rgba(251, 191, 36, 0.3);
  color: #fde047;
}

.insight-icon.balance {
  background: rgba(59, 130, 246, 0.3);
  color: #93c5fd;
}

.insight-info {
  flex: 1;
}

.insight-info h4 {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 6px 0;
}

.insight-info p {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  line-height: 1.5;
}

/* 响应式 */
@media (max-width: 768px) {
  .nav-container {
    padding: 0 16px;
  }
  
  .main-nav {
    display: none;
  }
  
  .content-container {
    padding: 20px 16px;
  }
  
  .page-title {
    font-size: 26px;
  }
  
  .header-stats {
    flex-wrap: wrap;
    gap: 20px;
    padding: 16px;
  }
  
  .stat-divider {
    display: none;
  }
  
  .student-info-header {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
  
  .student-update {
    align-items: center;
  }
  
  .student-meta {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .dimension-cards {
    padding: 16px;
  }
  
  .insight-grid {
    grid-template-columns: 1fr;
  }
}
</style>
