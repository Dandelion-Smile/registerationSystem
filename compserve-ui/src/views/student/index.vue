<template>
  <div class="home-container">
    <StudentNavbar />
      <div v-if="showProfileReminder" class="profile-reminder-banner">
        <span class="profile-reminder-text">
          <span class="profile-reminder-icon">!</span>
          <span>请先完善个人信息</span>
        </span>
        <button type="button" class="profile-reminder-dismiss" @click="dismissProfileReminder">不再提示</button>
      </div>

    <!-- 轮播图 -->
    <div class="slider" @mouseenter="pauseSlider" @mouseleave="resumeSlider">
      <div class="slider-track" :style="{ transform: `translateX(-${currentSlide * 100}%)` }">
        <div 
          v-for="(competition, index) in latestCompetitions" 
          :key="competition.id"
          class="slide"
          :style="{ backgroundImage: `url(${getCompetitionImage(competition)})`, backgroundSize: 'cover', backgroundPosition: 'center' }"
        >
          <div class="slide-content" :class="`slide-${index + 1}`" @click="goToCompetition(competition)">
            <div class="slide-overlay">
              <span class="slide-badge">{{ competition.type }}</span>
              <h1>{{ competition.title }}</h1>
              <p class="subtitle">{{ competition.subtitle }}</p>
              <p class="description">{{ competition.description }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 轮播指示器 -->
      <div class="slider-indicators">
        <button 
          v-for="(competition, index) in latestCompetitions" 
          :key="competition.id"
          :class="['indicator', { 'active': currentSlide === index }]"
          @click="goToSlide(index)"
        ></button>
      </div>

      <!-- 导航箭头 -->
      <button class="slider-nav prev" @click="prevSlide">
        <i class="fa fa-chevron-left"></i>
      </button>
      <button class="slider-nav next" @click="nextSlide">
        <i class="fa fa-chevron-right"></i>
      </button>
    </div>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <!-- 核心功能区 -->
      <div class="core-features">
        <h2 class="section-title">核心功能</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fa fa-file-alt"></i>
            </div>
            <h3>文档智能审核和美化</h3>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fa fa-microphone"></i>
            </div>
            <h3>模拟答辩</h3>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fa fa-users"></i>
            </div>
            <h3>智能组队</h3>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fa fa-bullhorn"></i>
            </div>
            <h3>智能赛事推送</h3>
          </div>
        </div>
      </div>

      <!-- 通知公告和近期赛事 -->
      <div class="info-section">
        <div class="info-grid">
          <!-- 通知公告 -->
          <div class="info-card">
            <div class="info-header">
              <h3 class="info-title">通知公告</h3>
              <a href="#" class="more-link">更多 ></a>
            </div>
            <div class="notice-content">
              <div class="notice-item">
                <div class="notice-info">
                  <h4 class="notice-title">2026年挑战杯报名即将结束</h4>
                  <p class="notice-desc">挑战杯报名将于3月31日截止，请尚未报名的同学尽快完成报名</p>
                </div>
              </div>
              <div class="notice-item">
                <div class="notice-info">
                  <h4 class="notice-title">2025年度竞赛证书开始发放</h4>
                  <p class="notice-desc">2025年度各类竞赛证书已开始发放，请获奖同学前往学院办公室领取</p>
                </div>
              </div>

              <div class="notice-item">
                <div class="notice-info">
                  <h4 class="notice-title">竞赛培训讲座安排</h4>
                  <p class="notice-desc">本周六将举办竞赛培训讲座，邀请专家分享竞赛经验和技巧</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 近期赛事 -->
          <div class="info-card">
            <div class="info-header">
              <h3 class="info-title">近期赛事</h3>
              <a href="/competition" class="more-link">更多 ></a>
            </div>
            <div class="competition-content">
              <div class="competition-list">
                <div class="competition-item" @click="goToCompetitionDetail(53)">
                  <div class="competition-info">
                    <h4 class="competition-title">2026年挑战杯</h4>
                    <p class="competition-desc">全国大学生课外学术科技作品竞赛</p>
                    <div class="competition-meta">
                      <span class="competition-date">2026-10-31截止</span>
                    </div>
                  </div>
                </div>
                <div class="competition-item" @click="goToCompetitionDetail(60)">
                  <div class="competition-info">
                    <h4 class="competition-title">中国国际大学生创新竞赛</h4>
                    <p class="competition-desc">激发创新潜能，培养创业精神</p>
                    <div class="competition-meta">
                      <span class="competition-date">2026-04-10截止</span>
                    </div>
                  </div>
                </div>
                <div class="competition-item" @click="goToCompetitionDetail(15)">
                  <div class="competition-info">
                    <h4 class="competition-title">“西门子杯”中国智能制造挑战杯</h4>
                    <p class="competition-desc">培养学生工程应用与创新研发能力</p>
                    <div class="competition-meta">
                      <span class="competition-date">2026-05-31截止</span>
                    </div>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>
      </div>

      <!-- 猜你感兴趣的赛事 -->
      <div class="interest-section">
        <h3 class="section-title">猜你感兴趣的赛事</h3>
        <div class="interest-grid">
          <div class="interest-card">
            <div class="interest-card-content">
              <div class="interest-icon">
                <i class="fa fa-microchip"></i>
              </div>
              <h4 class="interest-title">2026年全国大学生电子设计竞赛</h4>
              <p class="interest-desc">培养学生的创新能力和实践能力</p>
              <div class="interest-meta">
                <span class="interest-date">2026-05-31截止</span>
              </div>
              <a href="/competition/detail/11" class="interest-link">查看详情</a>
            </div>
          </div>
          <div class="interest-card">
            <div class="interest-card-content">
              <div class="interest-icon">
                <i class="fa fa-line-chart"></i>
              </div>
              <h4 class="interest-title">2026年中国机器人及人工智能大赛</h4>
              <p class="interest-desc">培养学生的机器人设计和人工智能应用能力</p>
              <div class="interest-meta">
                <span class="interest-date">2026-06-30截止</span>
              </div>
              <a href="/competition/detail/13" class="interest-link">查看详情</a>
            </div>
          </div>
          <div class="interest-card">
            <div class="interest-card-content">
              <div class="interest-icon">
                <i class="fa fa-rocket"></i>
              </div>
              <h4 class="interest-title">全国大学生统计建模大赛</h4>
              <p class="interest-desc">培养学生的统计分析和数据挖掘能力</p>
              <div class="interest-meta">
                <span class="interest-date">2026-04-30截止</span>
              </div>
              <a href="/competition/detail/17" class="interest-link">查看详情</a>
            </div>
          </div>
        </div>
      </div>

      <!-- 智能客服 -->
      <div class="service-section">
        <div class="service-btn" @click="openFastGPT">
          <i class="fa fa-comments"></i>
          <span>智能客服</span>
        </div>
      </div>
    </main>

    <!--页脚-->
    <footer class="page-footer">
      <div class="footer-container">
        <div class="footer-section">
          <h4><i class="fas fa-trophy"></i> 智启赛途</h4>
          <p>专注为高校学生提供全方位的竞赛管理与培训服务，助力学生在各类编程竞赛中取得优异成绩。</p>
          </div>
        <div class="footer-section">
          <h4>快速链接</h4>
          <div class="footer-links">
            <router-link to="/student" class="nav-item">首页</router-link>
            <router-link to="/competition" class="nav-item">赛事</router-link>
           <!--
            <a href="#">经验圈</a>
            <a href="#">关于我们</a>
            -->
          </div>
        </div>
       
        <!--
        <div class="footer-section">
          <h4>快捷链接</h4>
          <div class="footer-links">
            <router-link to="/student" class="nav-item">首页</router-link>
            <router-link to="/competition" class="nav-item">赛事</router-link>
            <a href="#">经验圈</a>
            <a href="#">模拟考试</a>
            <a href="#">刷题训练</a>
          </div>
        </div>
    -->
        <div class="footer-section">
          <h4>关于我们</h4>
          <div class="social-links">
            <a href="#"><i class="fab fa-weixin"></i></a>
            <a href="#"><i class="fab fa-weibo"></i></a>
            <a href="#"><i class="fab fa-qq"></i></a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
import StudentNavbar from "@/components/StudentNavbar.vue";
import useUserStore from '@/store/modules/user'
import { ElMessageBox } from 'element-plus'
import { getUserProfile } from "@/api/system/user";
import {
  isStudentRegistrationProfileComplete,
  STUDENT_PROFILE_REMINDER_STORAGE_KEY,
} from "@/utils/studentProfile";
export default {
  name: 'HomePage',
  components: { StudentNavbar },
  data() {
    return {
      userStore: useUserStore(),
      currentSlide: 0,
      slideInterval: null,
      mobileMenuOpen: false,
      showRegistrationModal: false,
      modalTitle: '',
      selectedTeams: [],
      showProfileReminder: false,
      latestCompetitions: [
        {
          id: 53,
          title: '挑战杯',
          subtitle: '全国大学生课外学术科技作品竞赛',
          description: '培养大学生创新精神和实践能力的重要平台，展示青年学子的科研成果',
          type: '权威赛事',
          category: 'ai'
        },
        {
          id: 60,
          title: '中国国际大学生创新竞赛',
          subtitle: '激发创新潜能，培养创业精神',
          description: '全球大学生创新与创业的盛会，汇聚世界各地的优秀青年人才',
          type: '国际赛事',
          category: 'programming'
        },
        {
          id: 15,
          title: '西门子杯',
          subtitle: '中国智能制造挑战赛',
          description: '培养大学生智能制造领域的创新能力和实践能力，推动智能制造技术的发展',
          type: '学科赛事',
          category: 'data'
        },
        {
          id: 4,
          title: '材料智能审核',
          subtitle: '系统核心功能',
          description: '利用人工智能技术对竞赛材料进行智能审核和评估，提高评审效率和准确性',
          type: '系统功能',
          category: 'ai'
        }
      ]
    }
  },
  mounted() {
    this.startSlider()
    this.loadProfileReminder()
  },
  beforeUnmount() {
    this.stopSlider()
  },
  methods: {
    async loadProfileReminder() {
      try {
        const response = await getUserProfile()
        const profile = response?.data || {}
        const dismissed = sessionStorage.getItem(STUDENT_PROFILE_REMINDER_STORAGE_KEY) === '1'
        this.showProfileReminder =
          !isStudentRegistrationProfileComplete(profile) && !dismissed
      } catch (error) {
        this.showProfileReminder = false
      }
    },
    dismissProfileReminder() {
      sessionStorage.setItem(STUDENT_PROFILE_REMINDER_STORAGE_KEY, '1')
      this.showProfileReminder = false
    },
    handleCommand(command) {
      if (command === 'logout') {
        ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.userStore.logOut().then(() => {
            location.href = '/login'
          })
        }).catch(() => {})
      }
    },
    startSlider() {
      this.slideInterval = setInterval(this.nextSlide, 7000)
    },
    stopSlider() {
      if (this.slideInterval) {
        clearInterval(this.slideInterval)
      }
    },
    pauseSlider() {
      this.stopSlider()
    },
    resumeSlider() {
      this.startSlider()
    },
    nextSlide() {
      this.currentSlide = (this.currentSlide + 1) % this.latestCompetitions.length
    },
    prevSlide() {
      this.currentSlide = (this.currentSlide - 1 + this.latestCompetitions.length) % this.latestCompetitions.length
    },
    goToSlide(index) {
      this.currentSlide = index
    },
    toggleMobileMenu() {
      this.mobileMenuOpen = !this.mobileMenuOpen
    },
    goToIntelligent() {
      window.location.href = 'intelligent.html'
    },
    goToCompetition(competition) {
      if (competition.id !== 4) { // 排除材料智能审核（它不是比赛）
        // 打印调试信息
        console.log('点击轮播图，竞赛ID:', competition.id);
        console.log('跳转路径:', `/competition/detail/${competition.id}`);
        // 直接跳转到竞赛详情页面
        this.$router.push(`/competition/detail/${competition.id}`)
      }
    },
    goToCompetitionDetail(competitionId) {
      // 打印调试信息
      console.log('点击近期赛事，竞赛ID:', competitionId);
      console.log('跳转路径:', `/competition/detail/${competitionId}`);
      // 直接跳转到竞赛详情页面
      this.$router.push(`/competition/detail/${competitionId}`)
    },
    openFastGPT() {
      // 打开FastGPT智能体链接
      window.open('https://cloud.fastgpt.cn/chat/share?shareId=rQ15G5X96zmNEBggLffDlN2P', '_blank');
    },
    getCompetitionImage(competition) {
      // 根据竞赛标题返回对应的图片
      const competitionImages = {
        '挑战杯': 'https://picsum.photos/id/1/1920/1080', // 挑战杯 - 科技学术风格
        '中国国际大学生创新竞赛': 'https://picsum.photos/id/20/1920/1080', // 中国国际大学生创新竞赛 - 创新国际风格
        '西门子杯': 'https://picsum.photos/id/24/1920/1080', // 西门子杯 - 智能制造风格
        '材料智能审核': 'https://picsum.photos/id/96/1920/1080' // 材料智能审核 - 文档审核风格
      };
      
      // 根据标题返回对应图片
      return competitionImages[competition.title] || 'https://picsum.photos/id/50/1920/1080';
    }
  }
}
</script>

<style scoped>
/* 基础样式 */
/* 猜你感兴趣的赛事样式 */
.interest-section {
  margin-bottom: 3rem;
}

.interest-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.interest-card {
  background: white;
  border-radius: 0.75rem;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  transition: all 0.3s ease;
  cursor: pointer;
}

.interest-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.interest-card-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
  text-align: center;
}

.interest-icon {
  width: 3.5rem;
  height: 3.5rem;
  border-radius: 50%;
  background: #e0e7ff;
  color: #6366f1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  margin-bottom: 0.5rem;
  transition: all 0.3s ease;
}

.interest-card:hover .interest-icon {
  background: #6366f1;
  color: white;
  transform: scale(1.1);
}

.interest-title {
  font-size: 1rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  line-height: 1.3;
}

.interest-desc {
  font-size: 0.875rem;
  color: #4b5563;
  margin: 0;
  line-height: 1.4;
}

.interest-meta {
  margin: 0.5rem 0;
}

.interest-date {
  font-size: 0.75rem;
  color: #9ca3af;
  background: #e5e7eb;
  padding: 0.25rem 0.75rem;
  border-radius: 0.25rem;
  display: inline-block;
}

.interest-link {
  color: #8b5cf6;
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.3s ease;
  margin-top: 0.5rem;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.interest-link:hover {
  color: #7c3aed;
  transform: translateX(2px);
}

.interest-link::after {
  content: '>';
  font-size: 0.75rem;
}
/* 基础样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', 'system-ui', sans-serif;
  background-color: #f9fafb;
  line-height: 1.6;
}

.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.profile-reminder-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  width: 100%;
  margin: 0 0 -2.6rem;
  padding: 0.38rem 1.6rem;
  background: #fff4de;
  color: #d18d2b;
  border-top: 1px solid rgba(209, 141, 43, 0.18);
  border-bottom: 1px solid rgba(209, 141, 43, 0.18);
  border-radius: 0;
  box-shadow: 0 8px 20px rgba(157, 107, 36, 0.08);
  position: relative;
  z-index: 6;
  min-height: 2.45rem;
}

.profile-reminder-text {
  display: inline-flex;
  align-items: center;
  gap: 0.7rem;
  font-size: 1rem;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: inherit;
}

.profile-reminder-icon {
  width: 1.45rem;
  height: 1.45rem;
  border-radius: 50%;
  background: #eba02d;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.95rem;
  font-weight: 700;
  flex-shrink: 0;
}

.profile-reminder-dismiss {
  border: 0;
  background: rgba(209, 141, 43, 0.12);
  color: #9a6422;
  padding: 0.32rem 0.8rem;
  border-radius: 999px;
  font-size: 0.84rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
  flex-shrink: 0;
}

.profile-reminder-dismiss:hover {
  background: rgba(209, 141, 43, 0.18);
  transform: translateY(-1px);
}

/* ==================== 顶部导航 ==================== */
.header-nav {
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
  cursor: pointer;
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

.dropdown-header {
  color: #6366f1;
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 8px;
  padding: 4px 8px;
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

.nav-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-box {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 6px 16px;
  gap: 8px;
  transition: all 0.3s;
}

.search-box:focus-within {
  background: rgba(255, 255, 255, 0.3);
}

.search-box i {
  color: white;
  font-size: 14px;
}

.search-box input {
  background: transparent;
  border: none;
  outline: none;
  color: white;
  font-size: 14px;
  width: 200px;
}

.search-box input::placeholder {
  color: rgba(255, 255, 255, 0.7);
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

/* 移动端菜单 */
.mobile-menu {
  display: none;
  background: white;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  padding: 0.75rem 1rem;
  flex-direction: column;
  gap: 0.75rem;
}

.mobile-menu.active {
  display: flex;
}

.mobile-nav-item {
  color: #6b7280;
  text-decoration: none;
  padding: 0.5rem 0;
  transition: color 0.3s;
}

.mobile-nav-item:hover,
.mobile-nav-item.active {
  color: #7c3aed;
}

.mobile-smart-features h4 {
  color: #7c3aed;
  font-weight: 500;
  margin-bottom: 0.5rem;
}

.mobile-smart-features ul {
  list-style: none;
  padding-left: 1rem;
}

.mobile-smart-features li {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  color: #6b7280;
  font-size: 0.875rem;
}

.mobile-search {
  position: relative;
  margin: 0.5rem 0;
}

.mobile-search i {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
}

.mobile-search input {
  width: 100%;
  padding: 0.5rem 0.5rem 0.5rem 2.5rem;
  border: 1px solid #d1d5db;
  border-radius: 9999px;
  outline: none;
}

.mobile-login-btn {
  border: 1px solid #7c3aed;
  color: #7c3aed;
  background: none;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  font-weight: 500;
  margin-top: 0.5rem;
}

/* 轮播图样式 */
.slider {
  height: 28rem;
  position: relative;
  overflow: hidden;
  witdth:100%;
}

.slider-track {
  display: flex;
  height: 100%;
  transition: transform 0.8s ease;
}

.slide {
  flex: 0 0 100%;
  position: relative;
  height: 100%;
}

.slide-content {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  color: white;
  z-index: 2;
}

.slide-overlay {
  background: rgba(0, 0, 0, 0.3);
  padding: 3rem 2rem;
  border-radius: 1rem;
  max-width: 800px;
  width: 90%;
  text-align: center;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transform: translateY(-10px);
  transition: all 0.5s ease;
}

.slide-content:hover .slide-overlay {
  transform: translateY(0);
  background: rgba(0, 0, 0, 0.4);
}

.slide-overlay h1 {
  font-size: 2.5rem;
  font-weight: 800;
  margin-bottom: 1rem;
  color: white;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.8);
  line-height: 1.2;
  transition: all 0.3s ease;
}

.slide-content:hover .slide-overlay h1 {
  transform: scale(1.02);
}

.slide-overlay .subtitle {
  font-size: 1.3rem;
  margin-bottom: 1rem;
  color: white;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.6);
  font-weight: 500;
  transition: all 0.3s ease;
}

.slide-overlay .description {
  font-size: 1rem;
  line-height: 1.6;
  color: white;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.6);
  margin-bottom: 1.5rem;
  font-weight: 400;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  transition: all 0.3s ease;
}

.slide-badge {
  display: inline-block;
  padding: 0.5rem 1.5rem;
  border-radius: 50px;
  font-size: 0.9rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  transition: all 0.3s ease;
}

.slide-content:hover .slide-badge {
  transform: scale(1.05);
}

.slide-1 .slide-badge {
  background: rgba(139, 92, 246, 0.1);
  color: #8b5cf6;
}

.slide-2 .slide-badge {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.description {
  font-size: 1.125rem;
  margin-bottom: 2rem;
  max-width: 32rem;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.cta-button {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
  color: white;
  border: none;
  padding: 0.75rem 2rem;
  border-radius: 0.5rem;
  font-weight: 500;
  font-size: 1.125rem;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.cta-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.slider-indicators {
  position: absolute;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 0.5rem;
}

.indicator {
  width: 0.75rem;
  height: 0.75rem;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  border: none;
  cursor: pointer;
  transition: all 0.3s;
}

.indicator.active {
  width: 1.5rem;
  border-radius: 0.5rem;
  background: white;
}

.slider-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255, 255, 255, 0.3);
  color: white;
  border: none;
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.slider-nav:hover {
  background: rgba(255, 255, 255, 0.5);
}

.prev {
  left: 1rem;
}

.next {
  right: 1rem;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 3rem 1rem;
  width: 100%;
  position: relative;
}

/* 核心功能区 */
.core-features {
  margin-bottom: 3rem;
}

.section-title {
  text-align: center;
  font-size: 1.5rem;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 2rem;
  background: #f3f4f6;
  padding: 0.5rem;
  display: inline-block;
  margin-left: auto;
  margin-right: auto;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.5rem;
}

.feature-card {
  background: white;
  border-radius: 0.75rem;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  padding: 2rem 1.5rem;
  text-align: center;
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  width: 4rem;
  height: 4rem;
  border-radius: 50%;
  background: #e0e7ff;
  color: #4f46e5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
  font-size: 1.5rem;
}

.feature-card h3 {
  font-size: 1rem;
  font-weight: 600;
  color: #1f2937;
}

/* 通知公告和近期赛事 */
.info-section {
  margin-bottom: 3rem;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 2rem;
}

.info-card {
  background: white;
  border-radius: 0.75rem;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.info-title {
  font-size: 1.25rem;
  font-weight: bold;
  color: #1f2937;
  background: #f3f4f6;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  display: inline-block;
}

.more-link {
  color: #8b5cf6;
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.more-link:hover {
  color: #7c3aed;
  transform: translateX(2px);
}

.notice-content {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.notice-item {
  background: #f9fafb;
  padding: 1.25rem;
  border-radius: 0.75rem;
  border-left: 4px solid #8b5cf6;
  transition: all 0.3s ease;
  cursor: pointer;
}

.notice-item:hover {
  background: #f3f4f6;
  transform: translateX(4px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.notice-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.notice-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  line-height: 1.4;
}

.notice-desc {
  font-size: 0.8rem;
  color: #4b5563;
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notice-time {
  font-size: 0.75rem;
  color: #9ca3af;
  margin-top: 0.25rem;
  align-self: flex-start;
  background: #e5e7eb;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
}

.competition-content {
  position: relative;
  min-height: 150px;
}

.competition-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.competition-item {
  background: #f9fafb;
  padding: 1rem;
  border-radius: 0.5rem;
  border-left: 3px solid #8b5cf6;
  transition: all 0.3s ease;
  cursor: pointer;
}

.competition-item:hover {
  background-color: rgba(139, 92, 246, 0.05);
  transform: translateX(5px);
}

.competition-item:hover {
  background: #f3f4f6;
  transform: translateX(3px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.competition-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.competition-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  line-height: 1.3;
}

.competition-desc {
  font-size: 0.8rem;
  color: #4b5563;
  margin: 0;
  line-height: 1.3;
}

.competition-meta {
  margin-top: 0.25rem;
}

.competition-date {
  font-size: 0.75rem;
  color: #9ca3af;
  background: #e5e7eb;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  display: inline-block;
}

.more-btn {
  background: #8b5cf6;
  color: white;
  border: none;
  padding: 0.625rem 1.25rem;
  border-radius: 0.375rem;
  font-weight: 500;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: block;
  margin: 0 auto;
  box-shadow: 0 2px 4px rgba(139, 92, 246, 0.3);
}

.more-btn:hover {
  background: #7c3aed;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px rgba(139, 92, 246, 0.4);
}

/* 猜你感兴趣的赛事 */
.interest-section {
  margin-bottom: 3rem;
  position: relative;
}

.interest-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
  perspective: 1000px;
}

.interest-card {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  padding: 2rem;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transform-style: preserve-3d;
}

.interest-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #8b5cf6, #6366f1);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.interest-card:hover::before {
  transform: scaleX(1);
}

.interest-card:hover {
  transform: translateY(-8px) rotateX(5deg);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.interest-card-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
  text-align: center;
  position: relative;
  z-index: 1;
  height: 100%;
  justify-content: center;
}

.interest-icon {
  width: 4rem;
  height: 4rem;
  border-radius: 50%;
  background: linear-gradient(135deg, #e0e7ff, #c7d2fe);
  color: #6366f1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(99, 102, 241, 0.3);
}

.interest-card:hover .interest-icon {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  transform: scale(1.15) rotate(5deg);
  box-shadow: 0 8px 15px rgba(99, 102, 241, 0.4);
}

.interest-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
  line-height: 1.3;
  transition: color 0.3s ease;
}

.interest-card:hover .interest-title {
  color: #8b5cf6;
}

.interest-desc {
  font-size: 0.9rem;
  color: #4b5563;
  margin: 0;
  line-height: 1.4;
  transition: color 0.3s ease;
}

.interest-card:hover .interest-desc {
  color: #374151;
}

.interest-meta {
  margin: 0.5rem 0;
}

.interest-date {
  font-size: 0.8rem;
  color: #6b7280;
  background: #f3f4f6;
  padding: 0.375rem 0.75rem;
  border-radius: 0.375rem;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  transition: all 0.3s ease;
}

.interest-card:hover .interest-date {
  background: #e5e7eb;
  transform: scale(1.05);
}

.interest-link {
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  color: white;
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 600;
  padding: 0.625rem 1.25rem;
  border-radius: 0.375rem;
  transition: all 0.3s ease;
  margin-top: 0.5rem;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 4px 6px rgba(139, 92, 246, 0.3);
}

.interest-link:hover {
  background: linear-gradient(135deg, #7c3aed, #4f46e5);
  transform: translateY(-2px);
  box-shadow: 0 8px 15px rgba(139, 92, 246, 0.4);
}

.interest-link i {
  transition: transform 0.3s ease;
}

.interest-link:hover i {
  transform: translateX(3px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .interest-grid {
    grid-template-columns: 1fr;
  }
  
  .interest-card {
    padding: 1.5rem;
  }
}

/* 智能客服 */
.service-section {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  z-index: 1000;
}

.service-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
  transition: all 0.3s ease;
  font-size: 0.75rem;
}

.service-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(139, 92, 246, 0.6);
}

.service-btn i {
  font-size: 1.25rem;
  margin-bottom: 0.25rem;
}

/* 页脚样式 */
/* ==================== 页脚 ==================== */
.page-footer {
  background: #1f2937;
  color: white;
  padding: 40px 24px 24px;
  margin-top: 60px;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
}

.footer-section h4 {
  font-size: 16px;
  margin: 0 0 16px 0;
  font-weight: 600;
}

.footer-section p {
  font-size: 14px;
  color: #9ca3af;
  line-height: 1.6;
}

.footer-links {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.footer-links a {
  color: #9ca3af;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: white;
}

.social-links {
  display: flex;
  gap: 12px;
}

.social-links a {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  text-decoration: none;
  transition: all 0.3s;
}

.social-links a:hover {
  background: #8b5cf6;
  transform: translateY(-2px);
}

/* ==================== 响应式 ==================== */
@media (max-width: 900px) {
  .comp-grid {
    grid-template-columns: 1fr;
  }
  
  .content-wrapper {
    grid-template-columns: 1fr;
  }
  
  .footer-container {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .interest-grid {
    grid-template-columns: 1fr;
  }
  
  .nav-container {
    flex-wrap: wrap;
    height: auto;
    padding: 12px 16px;
  }
  
  .main-nav {
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .search-box input {
    width: 150px;
  }
  
  .filter-container {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-item {
    width: 100%;
  }
  
  .footer-container {
    grid-template-columns: 1fr;
  }
  
  .header-container {
    padding: 0.5rem 1rem;
  }
  
  .logo {
    font-size: 1.25rem;
  }
  
  .slider {
    height: 16rem;
  }

  .profile-reminder-banner {
    width: 100%;
    margin: 0 0 -2.2rem;
    padding: 0.32rem 0.9rem;
  }

  .profile-reminder-text {
    font-size: 0.92rem;
  }

  .profile-reminder-dismiss {
    padding: 0.28rem 0.68rem;
    font-size: 0.78rem;
  }
  
  .slide h1 {
    font-size: 1.5rem;
  }
  
  .subtitle {
    font-size: 1rem;
  }
  
  .description {
    font-size: 0.875rem;
  }
  
  .cta-button {
    padding: 0.5rem 1.5rem;
    font-size: 1rem;
  }
  
  .main-content {
    padding: 1.5rem 1rem;
  }
  
  .feature-card {
    padding: 1.5rem 1rem;
  }
  
  .info-card {
    padding: 1rem;
  }
  
  .interest-card {
    height: 120px;
  }
}

@media (max-width: 480px) {
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .feature-icon {
    width: 3rem;
    height: 3rem;
    font-size: 1.25rem;
  }
  
  .service-btn {
    width: 50px;
    height: 50px;
    font-size: 0.65rem;
  }
  
  .service-btn i {
    font-size: 1rem;
  }
}

/* 导航链接下划线动画 */
.nav-item::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: #8b5cf6;
  transition: width 0.3s ease;
}

.nav-item:hover::after {
  width: 100%;
}

/* 卡片悬停效果增强 */
.card {
  position: relative;
  overflow: hidden;
}

.card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(139, 92, 246, 0.04), transparent);
  transition: left 0.6s ease;
}

.card:hover::before {
  left: 100%;
}

/* 按钮悬停效果 */
.cta-button {
  position: relative;
  overflow: hidden;
}

.cta-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s ease;
}

.cta-button:hover::before {
  left: 100%;
}

/* 滚动条样式 */
.modal-body::-webkit-scrollbar {
  width: 6px;
}

.modal-body::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.modal-body::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.modal-body::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 加载动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card {
  animation: fadeIn 0.6s ease-out;
}

.registration-item {
  animation: fadeIn 0.6s ease-out;
}

.registration-item:nth-child(1) { animation-delay: 0.1s; }
.registration-item:nth-child(2) { animation-delay: 0.2s; }
.registration-item:nth-child(3) { animation-delay: 0.3s; }

/* 确保图片覆盖整个轮播图区域 */
.slide::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: -1;
}

.btn-white {
  background-color: white;
  color: #333; /* 文字颜色 */
  border: 1px solid #ddd;
  padding: 8px 16px;
  border-radius: 4px;
  text-decoration: none;
  display: inline-block;
  transition: all 0.3s;
}

.btn-white:hover {
  background-color: #f5f5f5;
  border-color: #ccc;
}

/* 轮播图内容层级调整 */
.slide-content {
  z-index: 2;
}

/* 高对比度模式支持 */
@media (prefers-contrast: high) {
  .card {
    border: 1px solid #e5e7eb;
  }
  
  .registration-item {
    border-left-width: 3px;
  }
  
  .nav-item.active {
    border-bottom-width: 3px;
  }
}

/* 减少动画支持 */
@media (prefers-reduced-motion: reduce) {
  .card,
  .registration-item,
  .slider-track,
  .modal {
    transition: none;
    animation: none;
  }
  
  .nav-item::after,
  .card::before,
  .cta-button::before {
    display: none;
  }
}
</style>
