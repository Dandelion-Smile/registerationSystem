<template>
  <div class="guide-page">
    <StudentNavbar />

    <!-- 1. 列表页视图 -->
    <template v-if="viewMode === 'list'">
      <section class="hero-section">
        <!-- 装饰性元素 -->
        <div class="hero-decor decor-1"><i class="fas fa-trophy"></i></div>
        <div class="hero-decor decor-2"><i class="fas fa-code"></i></div>
        <div class="hero-decor decor-3"><i class="fas fa-cog"></i></div>
        <div class="hero-decor decor-4"><i class="fas fa-microchip"></i></div>
        
        <div class="hero-content animate-slide-up">
          <span class="hero-badge">HELP CENTER</span>
          <h1 class="hero-title">赛事指南 <span class="accent-text">中心</span></h1>
          <p class="hero-subtitle">聚合全站赛事规则，一站式助你从报名到获奖</p>
          
          <div class="hero-search-wrap">
            <div class="search-input-box">
              <i class="fas fa-search"></i>
              <input type="text" placeholder="搜索你感兴趣的竞赛指南..." v-model="searchQuery">
              <button class="search-btn">搜索</button>
            </div>
            <div class="hot-tags-cloud">
              <span class="hot-label">热门：</span>
              <div class="tags-list">
                <button 
                  v-for="tag in ['互联网+', '数学建模', '挑战杯']" 
                  :key="tag"
                  class="tag-pill" 
                  :class="{ active: searchQuery === tag }"
                  @click="searchQuery = searchQuery === tag ? '' : tag"
                >
                  {{ tag }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <main class="list-content-wrap">
        <div class="container">
          <div class="section-header">
            <div class="title-group">
              <h2 class="section-main-title">全部赛事指南</h2>
            </div>
          </div>

          <!-- 加载中状态 -->
          <div v-if="loading" class="loading-state">
            <div class="loading-spinner">
              <i class="fas fa-spinner fa-spin"></i>
            </div>
            <p>正在加载赛事指南...</p>
          </div>

          <!-- 数据展示 -->
          <div v-else-if="filteredCompetitions.length > 0" class="guide-cards-grid">
            <div
              v-for="(comp, index) in filteredCompetitions"
              :key="comp.id"
              class="premium-card animate-fade-in"
              :style="{ animationDelay: `${index * 0.1}s` }"
              @click="enterDetail(comp.id)"
            >
              <div class="card-body">
                <div class="card-badge">{{ comp.level }}</div>
                <h3 class="card-title">{{ comp.name }}</h3>
                <p class="card-desc">{{ comp.desc }}</p>

                <div class="card-footer">
                  <div class="card-meta">
                    <i class="far fa-clock"></i>
                    <span>{{ comp.time }}</span>
                  </div>
                  <div class="card-link">
                    <span>查看指南</span>
                    <i class="fas fa-arrow-right"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 无搜索结果提示 -->
          <div v-else class="empty-state animate-fade-in">
            <div class="empty-icon"><i class="fas fa-search-minus"></i></div>
            <h3>未找到相关指南</h3>
            <p>试试其他关键词，或者清除搜索内容</p>
            <button class="reset-search-btn" @click="searchQuery = ''">清除搜索内容</button>
          </div>
        </div>
      </main>
    </template>

    <!-- 2. 详情页视图 -->
    <template v-else-if="viewMode === 'detail'">
      <div class="detail-page-container">
        <div class="detail-header-nav">
          <div class="container nav-container-row">
            <div class="title-section">
              <div class="back-link-simple" @click="viewMode = 'list'">
                <i class="fas fa-chevron-left"></i>
                <span>返回列表</span>
              </div>
              <h1 class="page-main-title">{{ currentCompData.fullName }}</h1>
              <div class="competition-level">
                <span class="level-tag">{{ currentCompData.level }}</span>
              </div>
            </div>
            <div class="files-section">
              <div class="files-header">
                <h3 style="margin: 0; font-size: 16px; font-weight: 600; color: #1e293b;">官方文件</h3>
              </div>
              <div style="margin-top: 8px; display: flex; flex-direction: column; gap: 8px; max-height: 160px; overflow-y: auto;">
                <template v-if="currentCompData.officialFiles && currentCompData.officialFiles.length > 0">
                  <a 
                    v-for="(file, index) in currentCompData.officialFiles" 
                    :key="index" 
                    :href="file.fileUrl" 
                    target="_blank" 
                    style="text-decoration: none; display: flex; align-items: center; gap: 10px; padding: 8px 12px; border-radius: 6px; transition: all 0.2s ease; font-size: 13px; color: #1e293b;"
                    @mouseenter="style={background: '#f8fafc'}"
                    @mouseleave="style={background: 'transparent'}"
                  >
                    <div :style="{ width: '24px', height: '24px', display: 'flex', alignItems: 'center', justifyContent: 'center', background: getFileColor(file.fileType), borderRadius: '4px', color: 'white', fontSize: '12px', flexShrink: 0 }">
                      <i :class="getFileIcon(file.fileType)"></i>
                    </div>
                    <div style="flex: 1; min-width: 0;">
                      <p style="margin: 0; font-weight: 500; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">{{ file.fileName }}</p>
                    </div>
                    <i class="fas fa-download" style="font-size: 12px; color: #6366f1; flex-shrink: 0;"></i>
                  </a>
                </template>
                <div v-else style="text-align: center; padding: 12px; color: #94a3b8; font-size: 12px;">
                  <i class="fas fa-file-alt" style="font-size: 16px; margin-bottom: 4px;"></i>
                  <p style="margin: 0;">暂无官方文件</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-main-wrap">
          <div class="container detail-flex-layout">
            <!-- 侧边导航 -->
            <aside class="detail-sidebar">
              <div class="sidebar-sticky">
                <nav class="side-menu">
                  <div 
                    v-for="menu in sideMenus" 
                    :key="menu.id"
                    class="side-menu-item"
                    :class="{ active: activeSection === menu.id }"
                    @click="menu.isLink ? goToDiscussion(menu.route) : scrollToSection(menu.id)"
                  >
                    <div class="item-icon-wrap"><i :class="menu.icon"></i></div>
                    <span>{{ menu.title }}</span>
                    <i v-if="menu.isLink" class="fas fa-external-link-alt" style="margin-left: auto; font-size: 12px; color: #94a3b8;"></i>
                  </div>
                </nav>
              </div>
            </aside>

            <!-- 主内容区 -->
            <article class="detail-content-area">
              <div class="premium-content-body">
                <!-- 1. 赛事介绍 -->
                <div id="intro" class="detail-section animate-fade-in">
                  <div class="section-header">
                    <h2 class="content-main-title">赛事介绍</h2>
                    <div class="title-underline"></div>
                  </div>
                  <div class="info-grid">
                    <div class="info-card">
                      <label>赛事全称</label>
                      <p>{{ currentCompData.fullName }}</p>
                    </div>
                    <div class="info-card">
                      <label>主办单位</label>
                      <p>{{ currentCompData.organizer }}</p>
                    </div>
                    <div class="info-card">
                      <label>赛事级别</label>
                      <p class="tag-p">{{ currentCompData.level }}</p>
                    </div>
                    <div class="info-card">
                      <label>参赛对象</label>
                      <p>{{ currentCompData.target }}</p>
                    </div>
                  </div>
                  <div class="purpose-box">
                    <label><i class="fas fa-bullseye"></i> 赛事目的</label>
                    <p>{{ currentCompData.purpose }}</p>
                  </div>
                </div>

                <!-- 2. 报名流程 -->
                <div id="process" class="detail-section animate-fade-in">
                  <div class="section-header">
                    <h2 class="content-main-title">报名流程</h2>
                    <div class="title-underline"></div>
                  </div>
                  <div class="modern-steps">
                    <div v-for="(step, index) in currentCompData.process" :key="index" class="modern-step-item">
                      <div class="step-circle">{{ index + 1 }}</div>
                      <p class="step-desc">{{ step }}</p>
                    </div>
                  </div>
                </div>

                <!-- 3. 时间安排 -->
                <div id="schedule" class="detail-section animate-fade-in">
                  <div class="section-header">
                    <h2 class="content-main-title">时间安排</h2>
                    <div class="title-underline"></div>
                  </div>
                  <div class="timeline-grid">
                    <div v-for="item in currentCompData.schedule" :key="item.label" class="timeline-card">
                      <div class="timeline-icon"><i class="far fa-clock"></i></div>
                      <div class="timeline-info">
                        <label>{{ item.label }}</label>
                        <p>{{ item.time }}</p>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 4. 参赛要求 -->
                <div id="require" class="detail-section animate-fade-in">
                  <div class="section-header">
                    <h2 class="content-main-title">参赛要求</h2>
                    <div class="title-underline"></div>
                  </div>
                  <div class="require-grid">
                    <div v-for="(req, key) in [{icon:'fas fa-users', label:'组队人数', val:currentCompData.requirements.teamSize}, {icon:'fas fa-user-graduate', label:'指导老师', val:currentCompData.requirements.teacher}, {icon:'fas fa-file-alt', label:'作品形式', val:currentCompData.requirements.workForm}, {icon:'fas fa-file-upload', label:'提交要求', val:currentCompData.requirements.fileType}]" :key="key" class="require-item">
                      <i :class="req.icon"></i>
                      <div class="req-text">
                        <label>{{ req.label }}</label>
                        <p>{{ req.val }}</p>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 5. 评分标准 -->
                <div id="standard" class="detail-section animate-fade-in">
                  <div class="section-header">
                    <h2 class="content-main-title">评分标准</h2>
                    <div class="title-underline"></div>
                  </div>
                  <el-table :data="currentCompData.standards" class="premium-table">
                    <el-table-column prop="item" label="评分维度" width="160">
                      <template #default="scope">
                        <span class="table-item-name">{{ scope.row.item }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="desc" label="详细考察要点" />
                  </el-table>
                </div>

                <!-- 6. 常见问题 FAQ -->
                <div id="faq" class="detail-section animate-fade-in">
                  <div class="section-header">
                    <h2 class="content-main-title">常见问题 FAQ</h2>
                    <div class="title-underline"></div>
                  </div>
                  <div class="faq-list-wrap">
                    <div 
                      v-for="(faq, index) in currentCompData.faqs" 
                      :key="index" 
                      class="faq-card"
                      :class="{ expanded: activeFaq.includes(index.toString()) }"
                    >
                      <div class="faq-q" @click="toggleFaq(index)">
                        <span class="q-icon"><i class="fas fa-question"></i></span>
                        <h4>{{ faq.q }}</h4>
                        <span class="faq-arrow"><i class="fas fa-chevron-down"></i></span>
                      </div>
                      <div class="faq-a-wrapper">
                        <div class="faq-a">
                          <span class="a-icon"><i class="fas fa-check"></i></span>
                          <p>{{ faq.a }}</p>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- 未找到答案入口 -->
                  <div class="faq-footer-contact" @click="viewMode = 'ai'">
                    <div class="contact-text">
                      <i class="fas fa-comment-dots"></i>
                      <span>未找到答案？联系我们的 <strong>AI 助学官</strong> 进一步咨询</span>
                    </div>
                    <i class="fas fa-arrow-right"></i>
                  </div>
                </div>

                <!-- 讨论区入口卡片 -->
                <div class="discussion-entry-card" @click="goToDiscussion('/discussion')">
                  <div class="entry-icon">
                    <i class="fas fa-comments"></i>
                  </div>
                  <div class="entry-content">
                    <h3 class="entry-title">进入讨论区</h3>
                    <p class="entry-desc">与其他同学交流经验、组队招募、问题求助</p>
                  </div>
                  <div class="entry-arrow">
                    <i class="fas fa-arrow-right"></i>
                  </div>
                </div>
              </div>
            </article>
          </div>
        </div>
      </div>
    </template>

    <!-- 3. 综合赛事智能体视图 -->
    <template v-else-if="viewMode === 'ai'">
      <div class="ai-view-container">
        <div class="ai-view-header">
          <div class="container flex-between">
            <div class="ai-header-left">
              <div class="back-circle" @click="viewMode = 'list'"><i class="fas fa-arrow-left"></i></div>
              <div class="ai-header-info">
                <h2>综合赛事智能助手</h2>
                <p><span class="online-dot"></span> 智启赛途 AI 已就绪</p>
              </div>
            </div>
            <div class="ai-header-tags">
              <span>#规则咨询</span>
              <span>#材料优化</span>
              <span>#答辩辅导</span>
            </div>
          </div>
        </div>

        <main class="ai-frame-content">
          <div class="container">
            <div class="iframe-wrapper">
              <iframe 
                src="https://udify.app/chatbot/744pg5jCXq0FcFfs" 
                style="width: 100%; height: 780px; border: none;" 
                frameborder="0" 
                allow="microphone"> 
              </iframe>
            </div>
          </div>
        </main>
      </div>
    </template>

    <!-- 底部栏 (仅在详情页) -->
    <footer v-if="viewMode === 'detail'" class="premium-footer">
      <div class="container">
        <div class="footer-grid">
          <div class="footer-contact">
            <div class="contact-pill"><i class="fas fa-phone-alt"></i> {{ currentCompData.contact.phone }}</div>
            <div class="contact-pill"><i class="fas fa-user-tie"></i> {{ currentCompData.contact.teacher }}</div>
          </div>
          <div class="footer-org">
            <p>主办单位：{{ currentCompData.organizer }}</p>
          </div>
        </div>
      </div>
    </footer>

    <!-- AI 悬浮球 -->
    <div class="ai-float-bubble" v-if="viewMode !== 'ai'" @click="viewMode = 'ai'">
      <div class="ai-bubble-tip">有问题？问问 AI 助学官</div>
      <div class="ai-bubble-inner">
        <i class="fas fa-robot"></i>
      </div>
    </div>
  </div>
</template>

<script setup name="CompetitionGuide">
import StudentNavbar from "@/components/StudentNavbar.vue";
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'
import { listStudentCompetitions, getCompetitionDetail } from '@/api/competition'
import { proxyFileUrl } from '@/utils/media'

const router = useRouter()
const userStore = useUserStore()
const viewMode = ref('list')
const activeSection = ref('intro')
const selectedCompId = ref(1)
const searchQuery = ref('')
const activeFaq = ref([])
const competitionOptions = ref([])
const loading = ref(false)

function toggleFaq(index) {
  const idx = index.toString()
  const pos = activeFaq.value.indexOf(idx)
  if (pos > -1) {
    activeFaq.value.splice(pos, 1)
  } else {
    activeFaq.value.push(idx)
  }
}

// 用于控制滚动时是否更新 activeSection
let isManualScrolling = false

const sideMenus = [
  { id: 'intro', title: '赛事介绍', icon: 'fas fa-info-circle' },
  { id: 'process', title: '报名流程', icon: 'fas fa-tasks' },
  { id: 'schedule', title: '时间安排', icon: 'fas fa-calendar-alt' },
  { id: 'require', title: '参赛要求', icon: 'fas fa-clipboard-check' },
  { id: 'standard', title: '评分标准', icon: 'fas fa-star' },
  { id: 'faq', title: '常见问题 FAQ', icon: 'fas fa-question-circle' },
  { id: 'discussion', title: '讨论区', icon: 'fas fa-comments', isLink: true, route: '/discussion' }
]

function scrollToSection(id) {
  isManualScrolling = true
  activeSection.value = id
  const el = document.getElementById(id)
  if (el) {
    const headerOffset = 140 // 顶部导航栏 + 详情页二级导航的高度
    const elementPosition = el.getBoundingClientRect().top
    const offsetPosition = elementPosition + window.pageYOffset - headerOffset

    window.scrollTo({
      top: offsetPosition,
      behavior: 'smooth'
    })

    // 滚动结束后恢复自动监听
    setTimeout(() => {
      isManualScrolling = false
    }, 1000)
  }
}

function goToDiscussion(route) {
  router.push(route)
}

// 滚动监听逻辑
let observer = null
const initObserver = () => {
  const options = {
    root: null,
    rootMargin: '-140px 0px -60% 0px',
    threshold: 0
  }

  observer = new IntersectionObserver((entries) => {
    if (isManualScrolling) return
    
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        activeSection.value = entry.target.id
      }
    })
  }, options)

  sideMenus.forEach(menu => {
    const el = document.getElementById(menu.id)
    if (el) observer.observe(el)
  })
}

onMounted(async () => {
  console.log('组件初始化，开始加载竞赛列表...')
  console.log('初始化时searchQuery:', searchQuery.value)
  console.log('初始化时competitionOptions:', competitionOptions.value)
  console.log('初始化时competitionOptions长度:', competitionOptions.value.length)
  await loadCompetitionList()
  console.log('加载完成后competitionOptions:', competitionOptions.value)
  console.log('加载完成后competitionOptions长度:', competitionOptions.value.length)
  console.log('加载完成后filteredCompetitions:', filteredCompetitions.value)
  console.log('加载完成后filteredCompetitions长度:', filteredCompetitions.value.length)
  if (viewMode.value === 'detail') {
    initObserver()
  }
})

onUnmounted(() => {
  if (observer) observer.disconnect()
})

// 监听视图切换，重新初始化观察者
import { watch } from 'vue'
watch(viewMode, (newVal) => {
  if (newVal === 'detail') {
    setTimeout(initObserver, 100)
  } else {
    if (observer) observer.disconnect()
  }
})

const filteredCompetitions = computed(() => {
  if (!searchQuery.value) return competitionOptions.value
  return competitionOptions.value.filter(c => c.name.toLowerCase().includes(searchQuery.value.toLowerCase()))
})

const createDefaultGuideData = () => ({
  fullName: '',
  organizer: '赛事组委会',
  purpose: '以当年官方通知为准',
  target: '在校学生',
  level: '未设置',
  process: [],
  schedule: [],
  requirements: {
    teamSize: '待维护',
    teacher: '待维护',
    workForm: '待维护',
    fileType: '待维护'
  },
  standards: [],
  contact: { phone: '待维护', teacher: '待维护' },
  faqs: []
})

const currentCompData = ref(createDefaultGuideData())
const currentMenuTitle = computed(() => sideMenus.find(m => m.id === activeSection.value)?.title)

// 根据文件类型获取图标
const getFileIcon = (fileType) => {
  if (!fileType) return 'fas fa-file';
  const type = fileType.toLowerCase();
  if (type.includes('pdf')) return 'fas fa-file-pdf';
  if (type.includes('word') || type.includes('doc')) return 'fas fa-file-word';
  if (type.includes('excel') || type.includes('xls') || type.includes('sheet')) return 'fas fa-file-excel';
  if (type.includes('powerpoint') || type.includes('ppt') || type.includes('presentation')) return 'fas fa-file-powerpoint';
  if (type.includes('zip') || type.includes('rar') || type.includes('7z') || type.includes('archive')) return 'fas fa-file-archive';
  if (type.includes('image') || type.includes('jpg') || type.includes('png') || type.includes('jpeg')) return 'fas fa-file-image';
  if (type.includes('video') || type.includes('mp4') || type.includes('avi')) return 'fas fa-file-video';
  if (type.includes('audio') || type.includes('mp3') || type.includes('wav')) return 'fas fa-file-audio';
  if (type.includes('text') || type.includes('txt')) return 'fas fa-file-alt';
  if (type.includes('code') || type.includes('json') || type.includes('js') || type.includes('java')) return 'fas fa-file-code';
  return 'fas fa-file';
};

// 根据文件类型获取背景颜色
const getFileColor = (fileType) => {
  if (!fileType) return 'linear-gradient(135deg, #94a3b8 0%, #64748b 100%)';
  const type = fileType.toLowerCase();
  if (type.includes('pdf')) return 'linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%)';
  if (type.includes('word') || type.includes('doc')) return 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)';
  if (type.includes('excel') || type.includes('xls') || type.includes('sheet')) return 'linear-gradient(135deg, #22c55e 0%, #16a34a 100%)';
  if (type.includes('powerpoint') || type.includes('ppt') || type.includes('presentation')) return 'linear-gradient(135deg, #f97316 0%, #ea580c 100%)';
  if (type.includes('zip') || type.includes('rar') || type.includes('7z') || type.includes('archive')) return 'linear-gradient(135deg, #a855f7 0%, #8b5cf6 100%)';
  if (type.includes('image') || type.includes('jpg') || type.includes('png')) return 'linear-gradient(135deg, #ec4899 0%, #db2777 100%)';
  return 'linear-gradient(135deg, #94a3b8 0%, #64748b 100%)';
};

const guideTemplates = {
  defaultNational: {
    level: '国家级（以官方发布为准）',
    organizer: '赛事组委会',
    purpose: '以赛促学、以赛促创，提升学生实践与创新能力。',
    target: '高校在校学生（以当年通知为准）',
    process: ['官网报名', '校赛/初赛', '区域赛/省赛', '全国赛/总决赛', '获奖公示'],
    schedule: [
      { label: '报名阶段', time: '按官网通知' },
      { label: '初赛阶段', time: '按官网通知' },
      { label: '决赛阶段', time: '按官网通知' },
      { label: '结果公布', time: '按官网通知' }
    ],
    requirements: {
      teamSize: '按赛项要求组队',
      teacher: '可设指导老师',
      workForm: '方案/代码/论文/演示材料',
      fileType: 'PDF、PPT、源代码、视频（按官网要求）'
    },
    standards: [
      { item: '技术能力', desc: '考察基础能力、工程实现与问题求解能力。' },
      { item: '创新程度', desc: '考察创新点、应用价值与方案完整性。' },
      { item: '规范性', desc: '考察提交材料完整性与答辩展示质量。' },
      { item: '综合表现', desc: '考察团队协作、现场发挥与成果落地潜力。' }
    ],
    faqs: [
      { q: '如何了解比赛的最新信息？', a: '关注赛事官网、官方微信公众号或学校通知，获取最新比赛信息。' },
      { q: '报名需要哪些材料？', a: '通常需要个人身份证明、学生证、参赛作品或项目方案等材料，具体以官网要求为准。' },
      { q: '比赛是否收取报名费用？', a: '不同赛事收费标准不同，有些免费，有些需要缴纳报名费。具体费用请查看赛事通知。' },
      { q: '比赛的奖项设置有哪些？', a: '一般包括特等奖、一等奖、二等奖、三等奖等，部分赛事还设有专项奖和优秀组织奖。' },
      { q: '获奖后有什么好处？', a: '获奖可以获得荣誉证书、奖金，部分赛事的获奖作品还可以推荐参加更高级别的比赛或进行成果转化。' },
      { q: '如何提高比赛获奖几率？', a: '充分了解比赛规则和评分标准，提前准备，注重作品的创新性和实用性，加强团队协作。' }
    ]
  },
  internetPlus: {
    level: '国家级（教育部等多部委主办）',
    organizer: '教育部等中央部委与地方政府联合主办',
    purpose: '聚焦新工科、新医科、新农科、新文科与产业需求，推动高校创新成果转化与高质量创业就业。',
    target: '普通高等学校在校生及毕业5年内毕业生（按当年赛道方案执行）',
    process: [
      '团队在全国大学生创业服务网（cy.ncss.cn）完成账号注册与项目报名',
      '按赛道要求提交商业计划书、路演PPT、视频与证明材料',
      '参加校级初赛，入围后由学校推荐进入省级复赛',
      '通过省赛后参加全国总决赛项目网评、路演答辩与现场评审',
      '根据成绩评定奖项并进入成果对接与落地转化环节'
    ],
    schedule: [
      { label: '报名阶段', time: '4月-7月（以当年通知为准）' },
      { label: '校级初赛', time: '5月-6月' },
      { label: '省级复赛', time: '7月-9月' },
      { label: '全国总决赛', time: '10月左右' }
    ],
    requirements: {
      teamSize: '一般不超过15人，项目仅可选择1个赛道申报',
      teacher: '可配指导教师，建议由创新创业导师或专业教师担任',
      workForm: '商业计划书、路演展示材料、项目视频、佐证附件',
      fileType: 'PDF、PPT/PPTX、MP4（按系统要求上传）'
    },
    standards: [
      { item: '创新维度', desc: '考察技术创新、模式创新、场景创新与差异化竞争力。' },
      { item: '商业与社会价值', desc: '考察市场需求匹配度、可持续性、就业带动与社会贡献。' },
      { item: '团队能力', desc: '考察核心成员结构、执行能力、项目推进与资源整合能力。' },
      { item: '落地可行性', desc: '考察技术可实现性、阶段成果、风险控制与转化潜力。' }
    ],
    faqs: [
      { q: '互联网+大赛有哪些赛道？', a: '通常包括高教主赛道、“青年红色筑梦之旅”赛道、职教赛道、萌芽赛道等，具体以当年通知为准。' },
      { q: '如何选择适合的赛道？', a: '根据项目类型、团队构成和项目阶段选择相应赛道，例如科技创新项目适合高教主赛道，乡村振兴项目适合“青年红色筑梦之旅”赛道。' },
      { q: '商业计划书需要包含哪些内容？', a: '通常包括项目背景、产品/服务介绍、市场分析、商业模式、运营计划、财务规划、团队介绍等。' },
      { q: '如何提高项目的竞争力？', a: '突出项目的创新性、技术壁垒、市场潜力和社会价值，注重商业模式的可行性和团队的执行能力。' },
      { q: '比赛对项目的成熟度有要求吗？', a: '不同赛道对项目成熟度要求不同，既有初创项目也有成熟项目，关键是项目的创新性和发展潜力。' },
      { q: '获奖项目有哪些支持政策？', a: '获奖项目可获得资金支持、孵化服务、投资对接、成果转化等多方面的支持，具体政策因地区而异。' }
    ]
  },
  lanqiaoAlgo: {
    level: '国家级（工业和信息化部人才交流中心组织）',
    organizer: '蓝桥杯全国软件和信息技术专业人才大赛组委会',
    purpose: '推动软件与信息技术人才培养，提升程序设计与工程实践能力。',
    target: '全日制在籍研究生、本科生、高职高专学生',
    process: ['官网报名', '省赛（选拔赛）', '晋级总决赛', '总决赛评奖', '证书公示'],
    schedule: [
      { label: '报名', time: '每年秋季至次年春季（按官网）' },
      { label: '省赛', time: '每年春季（按官网）' },
      { label: '总决赛', time: '每年春末夏初（按官网）' },
      { label: '结果发布', time: '赛后官网公示' }
    ],
    requirements: {
      teamSize: '个人赛为主，部分赛项按章程执行',
      teacher: '每位参赛者需关联指导教师（按当届要求）',
      workForm: '在线编程答题或赛项作品提交',
      fileType: '代码与文档按蓝桥杯平台要求'
    },
    standards: [
      { item: '算法与编码', desc: '考察编程基本功、算法设计与实现效率。' },
      { item: '工程实践', desc: '考察对软件开发流程与工程能力的掌握。' },
      { item: '稳定性', desc: '考察程序正确性、边界处理与鲁棒性。' },
      { item: '竞赛成绩', desc: '按赛题得分与排名评定奖项。' }
    ],
    faqs: [
      { q: '蓝桥杯算法大赛有哪些组别？', a: '通常包括研究生组、大学A组、大学B组、大学C组（高职高专）等，具体以当年通知为准。' },
      { q: '比赛使用哪些编程语言？', a: '支持C/C++、Java、Python等多种编程语言，具体以当年赛项要求为准。' },
      { q: '如何备战蓝桥杯算法大赛？', a: '建议系统学习算法与数据结构，多做真题和模拟题，注重编程实践和调试能力的培养。' },
      { q: '省赛和总决赛的赛制有什么不同？', a: '省赛通常为线上或线下的编程答题，总决赛难度更高，可能包含更多综合性题目。' },
      { q: '比赛的评分标准是什么？', a: '主要根据题目完成情况和代码质量评分，部分题目可能有部分分。' },
      { q: '获奖后有什么好处？', a: '获奖可以获得荣誉证书，部分企业在招聘时会参考蓝桥杯成绩，优秀选手还有机会获得实习或就业推荐。' }
    ]
  },
  lanqiaoSecurity: {
    level: '国家级（工业和信息化部人才交流中心组织）',
    organizer: '蓝桥杯大赛组委会',
    purpose: '培养网络安全技术能力与攻防实战能力。',
    target: '高校在籍学生（按赛道要求）',
    process: ['官网报名', '赛道训练/答疑', '省赛', '全国总决赛', '成绩公示'],
    schedule: [
      { label: '报名', time: '按蓝桥杯官网通知' },
      { label: '省赛', time: '按赛道安排' },
      { label: '总决赛', time: '按赛道安排' },
      { label: '公示', time: '赛后统一发布' }
    ],
    requirements: {
      teamSize: '按赛道规则（个人/团队）',
      teacher: '建议配置指导教师',
      workForm: '漏洞分析、攻防实操、题目解答',
      fileType: '线上答题记录及赛项要求材料'
    },
    standards: [
      { item: '安全基础', desc: '考察密码学、系统安全、网络安全基础知识。' },
      { item: '攻防能力', desc: '考察漏洞利用、防护思路与应急处理能力。' },
      { item: '实战表现', desc: '考察规定时限内完成任务的准确性与效率。' },
      { item: '规范合规', desc: '考察比赛纪律与提交材料规范性。' }
    ],
    faqs: [
      { q: '蓝桥杯网络安全大赛有哪些赛项？', a: '通常包括CTF夺旗赛、网络安全攻防赛、安全运维赛等，具体以当年通知为准。' },
      { q: '需要具备哪些网络安全知识？', a: '建议掌握网络协议、操作系统安全、Web安全、密码学、漏洞利用与防护等基础知识。' },
      { q: '如何备战网络安全大赛？', a: '建议多做CTF题目，参与线上靶场练习，学习安全工具的使用，关注安全漏洞的最新动态。' },
      { q: '比赛中需要注意哪些事项？', a: '遵守比赛规则，不得攻击比赛平台，注意时间管理，合理分配任务。' },
      { q: '获奖后有什么好处？', a: '获奖可以获得荣誉证书，提升个人安全技能，增加就业竞争力，部分优秀选手还可获得企业实习机会。' },
      { q: '非计算机专业学生可以参加吗？', a: '可以，只要对网络安全感兴趣并具备一定的技术基础，都可以报名参加。' }
    ]
  },
  mathModeling: {
    level: '国家级（中国工业与应用数学学会主办）',
    organizer: '中国工业与应用数学学会主办',
    purpose: '培养学生运用数学方法和计算机技术解决实际问题的综合能力。',
    target: '全国普通高校本科生及高职高专学生（按组别参赛）',
    process: [
      '学校完成组队与报名，参赛队赛前熟悉论文规范与提交流程',
      '竞赛开始时统一发布赛题，队伍在规定时段内选题并建模求解',
      '队伍独立完成论文撰写、结果分析与支撑材料整理',
      '按要求提交论文与相关文件，进入省赛和国赛评阅',
      '经评阅与复核后公布获奖名单'
    ],
    schedule: [
      { label: '赛题发布', time: '每年9月上旬周四18:00' },
      { label: '竞赛作答', time: '连续72小时（周日20:00截止）' },
      { label: '论文提交', time: '按国赛系统与省赛通知执行' },
      { label: '评阅公布', time: '秋季学期陆续公布' }
    ],
    requirements: {
      teamSize: '每队3名学生，独立完成，不得与队外讨论赛题',
      teacher: '可配置指导教师，竞赛期间原则上不得进行赛题指导',
      workForm: '数学建模论文为主，可附程序和支撑材料',
      fileType: '论文PDF及规定格式附件，按竞赛系统要求提交'
    },
    standards: [
      { item: '模型科学性', desc: '建模假设合理、方法选择恰当、推导过程严谨。' },
      { item: '结果有效性', desc: '结果可靠，具备验证与对比分析，结论可信。' },
      { item: '应用解释力', desc: '能解释实际问题并提出可执行建议。' },
      { item: '论文规范性', desc: '结构完整、表达清晰、格式合规、引用规范。' }
    ],
    faqs: [
      { q: '数学建模大赛的赛题类型有哪些？', a: '通常包括连续型、离散型和综合型赛题，涵盖工程、管理、环境、社会等多个领域。' },
      { q: '如何组队参加数学建模大赛？', a: '每队由3名学生组成，建议队员之间优势互补，分别擅长数学建模、编程和论文写作。' },
      { q: '竞赛期间可以使用哪些资源？', a: '可以使用各类图书资料、计算机和软件，但不得与队外人员讨论赛题，不得使用互联网查阅与赛题相关的资料。' },
      { q: '如何撰写高质量的数学建模论文？', a: '论文应包括问题重述、模型假设、模型建立、模型求解、结果分析、模型检验与改进等部分，结构清晰，逻辑严谨。' },
      { q: '常用的数学建模软件有哪些？', a: '常用软件包括MATLAB、Python、LINGO、SPSS等，可根据具体问题选择合适的软件。' },
      { q: '获奖后有什么好处？', a: '获奖可以获得荣誉证书，提升数学建模能力和团队协作能力，对考研、就业有一定帮助。' }
    ]
  },
  ncccu: {
    level: '国家级（全国高等学校计算机教育研究会主办）',
    organizer: '全国高等学校计算机教育研究会',
    purpose: '提升大学生计算机应用能力与综合实践创新能力。',
    target: '全日制高校专科、本科、研究生',
    process: ['官网报名', '区域赛（初赛）', '晋级国赛', '国赛评审', '获奖发布'],
    schedule: [
      { label: '报名', time: '每年下半年（按官网）' },
      { label: '区域赛', time: '按赛区通知' },
      { label: '国赛', time: '区域赛后组织' },
      { label: '证书发布', time: '官网统一公示' }
    ],
    requirements: {
      teamSize: '个人赛与团队赛并行（团队通常1-3人）',
      teacher: '可设1名指导教师',
      workForm: '机试答题或作品提交',
      fileType: '答卷、作品与附件按官网规范上传'
    },
    standards: [
      { item: '知识与技能', desc: '考察Office、编程、AI/大数据等专项能力。' },
      { item: '应用能力', desc: '考察将计算机技术用于实际问题的能力。' },
      { item: '创新表现', desc: '考察方案创意、实现质量与展示效果。' },
      { item: '竞赛成绩', desc: '按成绩分布与排名评定奖项。' }
    ],
    faqs: [
      { q: '高校计算机能力挑战赛有哪些赛项？', a: '通常包括Office高级应用、程序设计、人工智能与大数据、计算机网络等多个赛项，具体以当年通知为准。' },
      { q: '如何选择适合的赛项？', a: '根据个人兴趣和专业背景选择赛项，例如Office高级应用适合所有专业学生，程序设计适合计算机相关专业学生。' },
      { q: '比赛的形式是什么样的？', a: '通常采用线上或线下机试的形式，部分赛项可能需要提交作品或项目。' },
      { q: '如何备考高校计算机能力挑战赛？', a: '建议根据赛项要求系统学习相关知识，多做练习题，熟悉考试环境和题型。' },
      { q: '比赛的评分标准是什么？', a: '主要根据答题正确率、完成时间、作品质量等因素评分。' },
      { q: '获奖后有什么好处？', a: '获奖可以获得荣誉证书，提升计算机应用能力，对就业和考研有一定帮助。' }
    ]
  },
  gplt: {
    level: '国家级（中国高校计算机大赛竞赛版块）',
    organizer: '中国高校计算机大赛-团体程序设计天梯赛组委会',
    purpose: '考察程序设计基础与数据结构算法能力，强化团队协作。',
    target: '高校本科与专科在校生',
    process: ['组队报名', '模拟赛', '全国赛/决赛', '成绩评定', '奖项公布'],
    schedule: [
      { label: '报名与赛前会', time: '每年冬春季（按通知）' },
      { label: '模拟赛', time: '赛前2-3周' },
      { label: '正式比赛', time: '每年春季' },
      { label: '获奖公示', time: '比赛后官网公布' }
    ],
    requirements: {
      teamSize: '每队最多10人，个人独立答题、团队计分',
      teacher: '每队至少1名教练（校内正式教师）',
      workForm: '在线编程题（基础/进阶/登顶梯级）',
      fileType: '系统在线提交代码'
    },
    standards: [
      { item: '程序设计能力', desc: '考察算法思维、编码效率与正确性。' },
      { item: '分层突破能力', desc: '考察基础、进阶、登顶梯级的综合发挥。' },
      { item: '团队贡献', desc: '考察团队整体得分与协作水平。' },
      { item: '竞赛纪律', desc: '考察比赛规范执行与诚信参赛。' }
    ],
    faqs: [
      { q: '天梯赛的比赛形式是什么样的？', a: '天梯赛采用团队赛形式，每队由最多10名学生组成，个人独立答题，团队累计得分。' },
      { q: '比赛的题目难度如何？', a: '比赛题目分为基础题、进阶题和登顶题三个梯级，难度逐渐增加，适合不同水平的选手。' },
      { q: '如何组队参加天梯赛？', a: '由学校组织组队，每队至少1名教练，队员可以是不同年级、不同专业的学生。' },
      { q: '如何提高团队的成绩？', a: '建议队员之间合理分工，基础好的队员可以挑战难题，基础较弱的队员确保完成基础题，提高团队的整体得分。' },
      { q: '比赛使用哪些编程语言？', a: '支持C/C++、Java、Python等多种编程语言，具体以当年通知为准。' },
      { q: '获奖后有什么好处？', a: '获奖可以获得荣誉证书，提升团队协作能力和编程水平，对学校的计算机教育评价也有积极影响。' }
    ]
  },
  icpc: {
    level: '国际级（ICPC基金会主办）',
    organizer: 'ICPC Foundation',
    purpose: '提升高水平算法与工程实现能力，锻炼高压下团队协作解题能力。',
    target: '高校在校学生（按ICPC参赛资格规则）',
    process: ['校内选拔', '区域赛报名', '区域赛比赛', '晋级世界总决赛', '全球排名'],
    schedule: [
      { label: '区域赛季', time: '每年9-12月' },
      { label: '晋级确认', time: '区域赛后' },
      { label: '世界总决赛', time: '次年春季至夏季' },
      { label: '赛季总结', time: '官网发布' }
    ],
    requirements: {
      teamSize: '3人1机',
      teacher: '由学校教练团队组织与带队',
      workForm: '5小时现场编程解题',
      fileType: '在线评测系统提交代码'
    },
    standards: [
      { item: '解题数量', desc: '优先按通过题目数量排名。' },
      { item: '总罚时', desc: '同题数下按总用时与罚时判定名次。' },
      { item: '算法深度', desc: '考察复杂问题建模与实现能力。' },
      { item: '团队协同', desc: '考察三人协作分工与现场决策能力。' }
    ],
    faqs: [
      { q: 'ICPC的比赛形式是什么样的？', a: 'ICPC采用3人1机的团队赛形式，比赛时间为5小时，参赛队伍需要解决8-12道算法题。' },
      { q: '如何组队参加ICPC？', a: '由学校组织选拔，每队3名学生，通常由学校的ACM/ICPC教练负责训练和指导。' },
      { q: '比赛的题目难度如何？', a: 'ICPC题目难度较高，涵盖算法、数据结构、数学等多个领域，需要队员具备扎实的编程基础和算法知识。' },
      { q: '如何提高ICPC比赛的成绩？', a: '建议系统学习算法与数据结构，多参加训练赛和模拟赛，注重团队协作和分工配合。' },
      { q: '区域赛和世界总决赛的区别是什么？', a: '区域赛是选拔赛，成绩优异的队伍可以晋级世界总决赛，世界总决赛汇聚了全球顶尖的编程团队。' },
      { q: '获奖后有什么好处？', a: 'ICPC获奖是计算机领域的重要荣誉，对申请国内外名校、就业都有很大帮助，优秀选手还可能获得企业的特别关注。' }
    ]
  },
  iscc: {
    level: '国家级（ISCC组委会组织）',
    organizer: '中国兵工学会、北京理工大学及ISCC组委会',
    purpose: '提升信息安全意识与网络攻防实战能力，发现安全人才。',
    target: '高校在校学生（含本科、专科、研究生等分赛项）',
    process: ['线上报名', '线上挑战赛', '选拔晋级', '线下对抗赛/专项赛', '结果公示'],
    schedule: [
      { label: '报名', time: '每年春夏季（按官网）' },
      { label: '线上赛', time: '按赛项通知' },
      { label: '线下赛', time: '按晋级通知' },
      { label: '颁奖公示', time: '官网统一发布' }
    ],
    requirements: {
      teamSize: '按赛项设置（个人赛/团队赛）',
      teacher: '团队赛建议配置指导教师',
      workForm: 'CTF破阵夺旗、攻防对抗、专项赛作品',
      fileType: '线上提交flag/报告/作品材料'
    },
    standards: [
      { item: '安全技术能力', desc: '考察Web、逆向、PWN、MISC等安全能力。' },
      { item: '攻防对抗能力', desc: '考察真实环境下攻防博弈与防守能力。' },
      { item: '创新融合能力', desc: '考察安全技术与行业场景融合创新。' },
      { item: '规范性', desc: '考察原创性、规则遵循与材料质量。' }
    ],
    faqs: [
      { q: 'ISCC的比赛形式是什么样的？', a: 'ISCC通常包括线上CTF夺旗赛、线下攻防对抗赛和专项赛等形式，具体以当年通知为准。' },
      { q: '需要具备哪些信息安全知识？', a: '建议掌握网络安全基础、Web安全、逆向工程、密码学、漏洞利用与防护等知识。' },
      { q: '如何备战ISCC？', a: '建议多参加线上CTF比赛，学习安全工具的使用，关注安全漏洞的最新动态，积累实战经验。' },
      { q: '比赛中需要注意哪些事项？', a: '遵守比赛规则，不得攻击比赛平台和其他参赛队伍，注意时间管理，合理分配任务。' },
      { q: 'ISCC与其他安全竞赛有什么不同？', a: 'ISCC注重实战能力的考察，比赛形式多样，涵盖了信息安全的多个领域，是发现和培养安全人才的重要平台。' },
      { q: '获奖后有什么好处？', a: '获奖可以获得荣誉证书，提升个人安全技能，增加就业竞争力，部分优秀选手还可获得企业实习或就业机会。' }
    ]
  },
  raicom: {
    level: '国家级（睿抗机器人开发者大赛）',
    organizer: 'RAICOM国际公开赛组委会',
    purpose: '促进机器人、人工智能与数字技术创新应用，提升工程实践能力。',
    target: '全日制在籍高校学生（本科/研究生）',
    process: ['官网报名', '省赛/区域选拔', '晋级国赛', '总决赛答辩或实操', '结果发布'],
    schedule: [
      { label: '报名阶段', time: '每年春季至夏季（按官网）' },
      { label: '省赛/区域赛', time: '5-7月左右' },
      { label: '总决赛', time: '7-11月（按赛道）' },
      { label: '公示颁奖', time: '赛后公布' }
    ],
    requirements: {
      teamSize: '团队赛常见不超过6人，个人赛按赛项规则',
      teacher: '指导老师通常不超过2人',
      workForm: '工程竞技、信息技术创新、创客与数字创意作品',
      fileType: '报名表、技术文档、演示材料按官网要求提交'
    },
    standards: [
      { item: '工程实现', desc: '考察系统集成、调试能力与现场稳定性。' },
      { item: '创新性', desc: '考察技术路线、创意设计与应用价值。' },
      { item: '实战表现', desc: '考察任务完成度、效率与容错能力。' },
      { item: '成果展示', desc: '考察报告表达、答辩质量与可推广性。' }
    ],
    faqs: [
      { q: '睿抗机器人开发者大赛有哪些赛项？', a: '通常包括机器人挑战赛、人工智能赛、创意赛等多个赛项，具体以当年通知为准。' },
      { q: '需要具备哪些技术知识？', a: '建议掌握机器人技术、人工智能、电子电路、编程等相关知识，具体要求因赛项而异。' },
      { q: '如何组队参加睿抗机器人开发者大赛？', a: '由学校组织或学生自由组队，建议队员之间优势互补，涵盖机械、电子、软件等不同专业背景。' },
      { q: '比赛的评审标准是什么？', a: '主要考察机器人的功能实现、稳定性、创新性、技术难度以及团队的答辩表现等。' },
      { q: '如何准备比赛？', a: '建议提前了解赛题要求，组建团队，制定详细的开发计划，注重系统的稳定性和创新性。' },
      { q: '获奖后有什么好处？', a: '获奖可以获得荣誉证书、奖金，提升工程实践能力和团队协作能力，对就业和考研有一定帮助。' }
    ]
  },
  globalAiAlgo: {
    level: '国家级（纳入高校竞赛榜单）',
    organizer: '江苏省人工智能学会、全球校园人工智能算法精英大赛组委会',
    purpose: '搭建全球高校算法竞技平台，推动人工智能前沿技术实践。',
    target: '全球在校研究生、本科生、高职高专学生',
    process: ['官网报名', '赛道参赛', '省赛/区域赛', '全国总决赛', '奖项公示'],
    schedule: [
      { label: '赛事周期', time: '每年6-12月（原则）' },
      { label: '省赛阶段', time: '每年10月左右' },
      { label: '国赛阶段', time: '每年12月前后' },
      { label: '证书发布', time: '赛后统一公示' }
    ],
    requirements: {
      teamSize: '按赛道规则组队（常见1-5人）',
      teacher: '可设置指导教师',
      workForm: '算法挑战、创新应用、产业命题等赛道作品',
      fileType: '代码、说明文档、结果文件按赛题要求提交'
    },
    standards: [
      { item: '算法效果', desc: '考察模型性能、泛化能力与指标得分。' },
      { item: '工程能力', desc: '考察训练效率、复现实验与部署可行性。' },
      { item: '创新应用', desc: '考察算法与实际场景结合深度。' },
      { item: '规范性', desc: '考察作品完整度与提交材料合规性。' }
    ],
    faqs: [
      { q: '全球校园人工智能算法精英大赛有哪些赛道？', a: '通常包括算法挑战赛、创新应用赛、产业命题赛等多个赛道，具体以当年通知为准。' },
      { q: '需要具备哪些人工智能知识？', a: '建议掌握机器学习、深度学习、数据处理、模型训练与评估等相关知识，具体要求因赛道而异。' },
      { q: '如何组队参加比赛？', a: '由学校组织或学生自由组队，建议队员之间优势互补，涵盖算法设计、编程实现、数据分析等不同技能。' },
      { q: '比赛的评审标准是什么？', a: '主要考察算法的性能指标、创新性、工程实现质量、与实际场景的结合程度等。' },
      { q: '如何准备比赛？', a: '建议提前了解赛题要求，学习相关算法和技术，进行充分的模型训练和优化，注重代码的规范性和可复现性。' },
      { q: '获奖后有什么好处？', a: '获奖可以获得荣誉证书、奖金，提升人工智能技术能力，增加就业竞争力，部分优秀选手还可获得企业实习或就业机会。' }
    ]
  },
  iflytekAi: {
    level: '国家级（AI应用与算法竞赛平台）',
    organizer: '科大讯飞发起，中国信息协会联合主办',
    purpose: '推动AI算法与应用创新，促进产学研协同与成果转化。',
    target: '高校学生与开发者团队（按赛题规则）',
    process: ['平台注册报名', '选择赛题', '提交方案与代码/应用', '复赛答辩', '决赛评审'],
    schedule: [
      { label: '赛题发布', time: '按年度赛程滚动发布' },
      { label: '初赛提交', time: '按题目周期进行' },
      { label: '复赛决赛', time: '按题目通知组织' },
      { label: '颁奖公示', time: '官网统一发布' }
    ],
    requirements: {
      teamSize: '个人或团队均可（按赛题限制）',
      teacher: '高校赛可配指导教师',
      workForm: '算法模型、应用系统、创新作品',
      fileType: '代码、技术报告、演示视频与路演材料'
    },
    standards: [
      { item: '模型能力', desc: '考察算法性能、鲁棒性与可解释性。' },
      { item: '应用价值', desc: '考察业务场景适配与实际落地价值。' },
      { item: '创新与体验', desc: '考察方案创新度与用户体验质量。' },
      { item: '综合答辩', desc: '考察技术表达、方案完整性与团队协作。' }
    ],
    faqs: [
      { q: '科大讯飞AI大赛有哪些赛题类型？', a: '通常包括语音识别、自然语言处理、计算机视觉、机器学习等多个领域的赛题，具体以当年发布的赛题为准。' },
      { q: '需要具备哪些AI技术知识？', a: '建议掌握机器学习、深度学习、数据处理、模型训练与评估等相关知识，具体要求因赛题而异。' },
      { q: '如何参加科大讯飞AI大赛？', a: '在科大讯飞AI开发者平台注册账号，选择感兴趣的赛题，按照要求提交解决方案。' },
      { q: '比赛的评审标准是什么？', a: '主要考察算法的性能指标、创新性、工程实现质量、与实际场景的结合程度等。' },
      { q: '如何准备比赛？', a: '建议提前了解赛题要求，学习相关算法和技术，进行充分的模型训练和优化，注重代码的规范性和可复现性。' },
      { q: '获奖后有什么好处？', a: '获奖可以获得荣誉证书、奖金，提升AI技术能力，增加就业竞争力，部分优秀选手还可获得科大讯飞的实习或就业机会。' }
    ]
  },
  huaweiIct: {
    level: '国际级（华为面向全球高校年度赛事）',
    organizer: '华为ICT大赛组委会',
    purpose: '提升ICT理论与实践能力，培养网络、云、计算与AI方向人才。',
    target: '全球高校学生（按赛道规则）',
    process: ['校内/区域报名', '国家赛', '区域赛', '全球总决赛', '颁奖与人才对接'],
    schedule: [
      { label: '报名启动', time: '每赛季由官网发布' },
      { label: '国家赛', time: '按赛季安排' },
      { label: '区域赛', time: '按赛季安排' },
      { label: '全球总决赛', time: '次年春季左右' }
    ],
    requirements: {
      teamSize: '常见3名学生+1名指导老师',
      teacher: '通常每队配1名指导老师',
      workForm: '实践赛实验任务、创新赛项目展示',
      fileType: '实验结果、作品说明、答辩材料按规则提交'
    },
    standards: [
      { item: '理论基础', desc: '考察ICT核心知识掌握情况。' },
      { item: '上机实践', desc: '考察网络、云、计算、AI等实操能力。' },
      { item: '创新能力', desc: '考察创新方案设计与技术实现水平。' },
      { item: '团队协作', desc: '考察分工协同、沟通与现场执行能力。' }
    ],
    faqs: [
      { q: '华为ICT大赛有哪些赛项？', a: '通常包括实践赛和创新赛两个赛项，实践赛考察ICT核心知识和实操能力，创新赛考察技术创新和应用能力。' },
      { q: '需要具备哪些ICT知识？', a: '建议掌握网络技术、云计算、人工智能、大数据等相关知识，具体要求因赛项而异。' },
      { q: '如何组队参加华为ICT大赛？', a: '由学校组织组队，每队通常由3名学生和1名指导老师组成，队员可以是不同年级、不同专业的学生。' },
      { q: '比赛的评审标准是什么？', a: '实践赛主要考察理论知识和实操能力，创新赛主要考察项目的创新性、技术实现质量和应用价值。' },
      { q: '如何准备比赛？', a: '建议系统学习ICT相关知识，参加华为提供的培训和认证，进行充分的实践练习和项目准备。' },
      { q: '获奖后有什么好处？', a: '获奖可以获得荣誉证书、奖金，提升ICT技术能力，增加就业竞争力，部分优秀选手还可获得华为的实习或就业机会。' }
    ]
  },
  softwareCup: {
    level: '国家级（中国软件杯）',
    organizer: '中国软件杯大学生软件设计大赛组委会',
    purpose: '促进产教融合，提升大学生软件设计与工程开发能力。',
    target: '全日制普通高校在籍学生（含本科、研究生、高职）',
    process: ['官网报名', '赛题辅导与开发', '初赛提交', '决赛答辩', '获奖公布'],
    schedule: [
      { label: '报名', time: '每年春季（按官网）' },
      { label: '辅导开发', time: '4-7月左右' },
      { label: '初赛评审', time: '按赛题通知' },
      { label: '全国总决赛', time: '8月中旬左右' }
    ],
    requirements: {
      teamSize: '按赛题组队，通常团队参赛',
      teacher: '同一指导老师可指导多支队伍',
      workForm: '企业命题软件作品、演示视频与技术文档',
      fileType: '源代码、可执行程序、文档、视频按官网提交'
    },
    standards: [
      { item: '功能完整度', desc: '考察需求覆盖度与功能实现质量。' },
      { item: '工程质量', desc: '考察代码规范、架构设计与可维护性。' },
      { item: '创新实用性', desc: '考察场景价值与用户体验。' },
      { item: '答辩表现', desc: '考察演示效果、技术阐述与问题回应。' }
    ],
    faqs: [
      { q: '软件杯的赛题来源是什么？', a: '软件杯的赛题主要来源于企业的实际需求，由企业出题，学生根据赛题要求开发软件作品。' },
      { q: '如何组队参加软件杯？', a: '由学校组织或学生自由组队，建议队员之间优势互补，涵盖需求分析、设计、编码、测试等不同技能。' },
      { q: '比赛的评审标准是什么？', a: '主要考察软件的功能完整度、工程质量、创新性、实用性以及团队的答辩表现等。' },
      { q: '如何准备比赛？', a: '建议提前了解赛题要求，组建团队，制定详细的开发计划，注重代码的规范性和软件的用户体验。' },
      { q: '比赛对软件的技术栈有要求吗？', a: '通常不限制技术栈，学生可以根据赛题要求和自身技术优势选择合适的技术栈。' },
      { q: '获奖后有什么好处？', a: '获奖可以获得荣誉证书、奖金，提升软件设计与开发能力，增加就业竞争力，部分优秀作品还可能被企业采纳或转化。' }
    ]
  },
  fwwb: {
    level: '国家级（服务外包领域权威赛事）',
    organizer: '中国大学生服务外包创新创业大赛组委会',
    purpose: '强化服务外包与创新创业能力，促进高校人才与产业需求衔接。',
    target: '高校在校生及毕业5年内大学生（按当届规则）',
    process: ['官网报名', 'A/B/C类赛道参赛', '区域赛', '全国总决赛', '成果对接'],
    schedule: [
      { label: '报名周期', time: '通常上一年末至当年春季' },
      { label: '区域赛', time: '春夏季' },
      { label: '总决赛', time: '暑期' },
      { label: '结果发布', time: '决赛后官网公布' }
    ],
    requirements: {
      teamSize: '按赛道要求组队，鼓励跨专业互补',
      teacher: '可设置指导教师',
      workForm: '企业命题方案、创业实践项目与路演材料',
      fileType: '方案书PDF、PPT、路演视频等'
    },
    standards: [
      { item: '需求匹配度', desc: '考察方案对企业/产业真实需求的解决能力。' },
      { item: '商业可行性', desc: '考察项目商业模式、实施路径与成长性。' },
      { item: '技术与交付', desc: '考察技术实现质量与交付完整度。' },
      { item: '展示答辩', desc: '考察路演表达与现场答辩质量。' }
    ],
    faqs: [
      { q: '服务外包大赛有哪些赛道？', a: '通常包括A类（企业命题）、B类（创业实践）、C类（创新服务）等多个赛道，具体以当年通知为准。' },
      { q: '如何组队参加服务外包大赛？', a: '由学校组织或学生自由组队，建议队员之间优势互补，涵盖技术、商务、设计等不同专业背景。' },
      { q: '比赛的评审标准是什么？', a: '主要考察方案对企业需求的匹配度、商业可行性、技术实现质量以及团队的答辩表现等。' },
      { q: '如何准备比赛？', a: '建议提前了解赛题要求，组建团队，制定详细的项目计划，注重方案的创新性和可行性。' },
      { q: '比赛对项目的成熟度有要求吗？', a: '不同赛道对项目成熟度要求不同，既有创意阶段的项目，也有成熟的创业项目。' },
      { q: '获奖后有什么好处？', a: '获奖可以获得荣誉证书、奖金，提升服务外包与创新创业能力，增加就业竞争力，部分优秀项目还可能获得企业投资或合作机会。' }
    ]
  },
  smartCar: {
    level: '国家级（中国自动化学会主办）',
    organizer: '全国大学生智能汽车竞赛组委会',
    purpose: '培养自动控制、感知、嵌入式与系统集成综合能力。',
    target: '全国高校在校大学生',
    process: ['校内组队', '分赛区比赛', '晋级全国总决赛', '提交技术报告', '成绩公布'],
    schedule: [
      { label: '报名与备赛', time: '按当届竞赛通知' },
      { label: '分赛区赛', time: '每年7-8月左右' },
      { label: '全国总决赛', time: '分赛区后组织' },
      { label: '结果公示', time: '赛后官网公布' }
    ],
    requirements: {
      teamSize: '按车模/赛项规则组队',
      teacher: '建议配置指导教师',
      workForm: '现场竞速/创意赛表现与技术报告',
      fileType: '技术报告、调试记录等按规则提交'
    },
    standards: [
      { item: '竞赛成绩', desc: '以现场完成赛道任务时间与稳定性计分。' },
      { item: '系统设计', desc: '考察硬件选型、控制策略与软硬件协同。' },
      { item: '创新能力', desc: '考察创意赛与高阶赛项的技术创新点。' },
      { item: '工程规范', desc: '考察技术报告质量与复现实验能力。' }
    ],
    faqs: [
      { q: '智能汽车竞赛有哪些赛项？', a: '通常包括竞速赛、创意赛等多个赛项，竞速赛又分为电磁组、光电组、摄像头组等不同组别，具体以当年通知为准。' },
      { q: '需要具备哪些技术知识？', a: '建议掌握自动控制、嵌入式系统、传感器技术、图像处理等相关知识，具体要求因赛项而异。' },
      { q: '如何组队参加智能汽车竞赛？', a: '由学校组织组队，建议队员之间优势互补，涵盖机械、电子、软件等不同专业背景。' },
      { q: '比赛的评审标准是什么？', a: '主要考察智能汽车的竞速成绩、系统设计的合理性、创新性以及技术报告的质量等。' },
      { q: '如何准备比赛？', a: '建议提前了解赛题要求，组建团队，购买或制作车模，进行充分的调试和训练，注重系统的稳定性和创新性。' },
      { q: '获奖后有什么好处？', a: '获奖可以获得荣誉证书、奖金，提升自动控制与系统集成能力，增加就业竞争力，对考研也有一定帮助。' }
    ]
  },
  challengeCup: {
    level: '国家级（团中央等部委联合主办）',
    organizer: '共青团中央、中国科协、教育部、中国社会科学院、全国学联等联合主办',
    purpose: '引导学生崇尚科学、追求真知，提升学术研究与科技创新实践能力。',
    target: '高校在校学生，按个人作品与集体作品分类申报',
    process: [
      '围绕自然科学、哲学社会科学、科技发明制作三大类准备作品',
      '参加校级遴选并完成作品资格审查与校内答辩',
      '入围作品参加省级竞赛评审与展示交流',
      '省赛推荐优秀项目进入全国终审决赛',
      '通过文本评审、现场问辩和展示后确定奖项'
    ],
    schedule: [
      { label: '校赛启动', time: '每届竞赛周期内由学校发布通知' },
      { label: '省赛评审', time: '校赛后由各省赛区组织' },
      { label: '全国终审', time: '两年一届，具体时间以组委会公告为准' },
      { label: '成果展示', time: '终审期间同步开展展示交流活动' }
    ],
    requirements: {
      teamSize: '个人作品需本人承担60%以上研究工作；作者超过3人申报集体作品',
      teacher: '需按通知提交指导意见与推荐材料',
      workForm: '学术论文、调查报告、科技发明制作及相关证明材料',
      fileType: '申报书、论文/报告、附件证明（按赛区系统格式）'
    },
    standards: [
      { item: '学术与技术水平', desc: '关注理论深度、技术先进性与研究方法规范性。' },
      { item: '创新性', desc: '关注原创点、关键问题突破和跨学科融合能力。' },
      { item: '实践与应用价值', desc: '关注社会效益、推广前景和现实问题解决能力。' },
      { item: '现场表现', desc: '关注答辩表达、证据支撑与团队协作展示。' }
    ],
    faqs: [
      { q: '挑战杯有哪些类别？', a: '通常包括自然科学类学术论文、哲学社会科学类社会调查报告和学术论文、科技发明制作类作品三大类。' },
      { q: '如何参加挑战杯？', a: '由学校组织选拔，学生个人或团队提交作品，经过校级、省级评审后，优秀作品进入全国总决赛。' },
      { q: '比赛的评审标准是什么？', a: '主要考察作品的学术与技术水平、创新性、实践与应用价值以及团队的现场答辩表现等。' },
      { q: '如何准备挑战杯？', a: '建议提前确定研究方向，组建团队，进行充分的调研和实验，撰写高质量的学术论文或研究报告。' },
      { q: '挑战杯的周期是多久？', a: '挑战杯每两年举办一届，分为校级、省级和全国级三个阶段。' },
      { q: '获奖后有什么好处？', a: '获奖可以获得荣誉证书、奖金，提升学术研究能力和团队协作能力，对考研、就业有很大帮助，部分优秀作品还可能获得进一步的支持和推广。' }
    ]
  }
}

const formatDate = (value) => {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 10)
}

const resolveGuideTemplate = (competitionName = '') => {
  const text = String(competitionName).toLowerCase()
  if (text.includes('互联网') || text.includes('中国国际大学生创新大赛')) return guideTemplates.internetPlus
  if (text.includes('数学建模')) return guideTemplates.mathModeling
  if (text.includes('蓝桥杯') && text.includes('网络安全')) return guideTemplates.lanqiaoSecurity
  if (text.includes('蓝桥杯')) return guideTemplates.lanqiaoAlgo
  if (text.includes('高校计算机能力挑战赛')) return guideTemplates.ncccu
  if (text.includes('天梯赛')) return guideTemplates.gplt
  if (text.includes('acm-icpc') || text.includes('icpc')) return guideTemplates.icpc
  if (text.includes('信息安全与对抗') || text.includes('iscc')) return guideTemplates.iscc
  if (text.includes('睿抗')) return guideTemplates.raicom
  if (text.includes('全球校园人工智能算法精英')) return guideTemplates.globalAiAlgo
  if (text.includes('科大讯飞') || text.includes('iflytek')) return guideTemplates.iflytekAi
  if (text.includes('华为ict')) return guideTemplates.huaweiIct
  if (text.includes('软件杯')) return guideTemplates.softwareCup
  if (text.includes('服务外包')) return guideTemplates.fwwb
  if (text.includes('智能汽车')) return guideTemplates.smartCar
  if (text.includes('挑战杯') || text.includes('课外学术')) return guideTemplates.challengeCup
  return guideTemplates.defaultNational
}

const buildGuideData = (detail = {}) => {
  const start = formatDate(detail.registerStartTime)
  const end = formatDate(detail.registerEndTime)
  const template = resolveGuideTemplate(detail.competitionName)
  const defaults = createDefaultGuideData()

  const schedule = template?.schedule?.length
    ? template.schedule
    : [
        { label: '报名开始时间', time: start || '待维护' },
        { label: '报名截止时间', time: end || '待维护' }
      ]

  const process = template?.process?.length
    ? template.process
    : ['阅读赛事说明', '完成队伍信息填写', '提交报名并等待审核']

  const standards = template?.standards?.length
    ? template.standards
    : [
        { item: '完整性', desc: '请根据赛事要求准备完整申报材料。' },
        { item: '规范性', desc: '请保证提交内容格式规范、信息真实。' }
      ]

  return {
    fullName: detail.competitionName || '未命名竞赛',
    organizer: template?.organizer || defaults.organizer,
    purpose: template?.purpose || detail.description || defaults.purpose,
    target: template?.target || defaults.target,
    level: template?.level || '国家级',
    process,
    schedule,
    requirements: template?.requirements || defaults.requirements,
    standards,
    contact: defaults.contact,
    faqs: template?.faqs || [
      { q: '如何报名？', a: '在报名时间内进入赛事页面点击报名即可。' },
      { q: '规则在哪里看？', a: detail.description || '请查看赛事说明。' },
      { q: '比赛的截止时间是什么时候？', a: '具体截止时间以赛事官网通知为准，请及时关注赛事页面的时间安排。' },
      { q: '是否需要组队参加？', a: '根据不同赛事要求，有些比赛需要组队，有些可以个人参加。请查看具体赛事的参赛要求。' },
      { q: '比赛的评分标准是什么？', a: '不同赛事的评分标准不同，通常包括创新性、技术实现、实用性、展示效果等方面。' },
      { q: '如何准备比赛材料？', a: '根据赛事要求准备相关材料，包括项目方案、代码、演示文稿等，确保材料完整、规范。' },
      { q: '比赛获奖有什么奖励？', a: '通常包括证书、奖金、奖杯等，部分赛事还会提供实习、就业机会或项目孵化支持。' },
      { q: '比赛是否需要缴纳费用？', a: '不同赛事收费标准不同，有些免费，有些需要缴纳报名费或参赛费。' },
      { q: '如何联系赛事组委会？', a: '通常可以通过赛事官网提供的联系方式，如邮箱、电话等与组委会联系。' },
      { q: '比赛结果什么时候公布？', a: '比赛结果通常在比赛结束后一段时间内公布，具体时间以赛事官网通知为准。' }
    ],
    officialFiles: detail.officialFiles || []
  }
}

// 设置请求超时时间
const REQUEST_TIMEOUT = 100000 // 10秒

// 超时处理函数
const timeoutPromise = (ms, promise) => {
  return new Promise((resolve, reject) => {
    const timeoutId = setTimeout(() => {
      reject(new Error('请求超时，请检查网络连接'))
    }, ms)
    promise.then(
      (res) => {
        clearTimeout(timeoutId)
        resolve(res)
      },
      (err) => {
        clearTimeout(timeoutId)
        reject(err)
      }
    )
  })
}

const loadCompetitionList = async () => {
  loading.value = true
  try {
    console.log('开始加载竞赛列表...')
    console.log('当前searchQuery:', searchQuery.value)
    const res = await timeoutPromise(REQUEST_TIMEOUT, listStudentCompetitions(1, 100, {}))
    console.log('API响应:', res)
    const list = res?.data?.list || []
    console.log('竞赛列表数据:', list)
    if (list.length === 0) {
      console.log('API返回空数据，使用静态数据作为后备...')
      competitionOptions.value = [
        {
          id: 1,
          name: '互联网+创新创业大赛',
          time: '2024-05-01',
          level: '国家级',
          desc: '面向全国高校学生的创新创业大赛',
          template: guideTemplates.internetPlus
        },
        {
          id: 2,
          name: '数学建模竞赛',
          time: '2024-06-15',
          level: '国家级',
          desc: '培养学生数学建模能力的竞赛',
          template: guideTemplates.mathModeling
        },
        {
          id: 3,
          name: '挑战杯',
          time: '2024-07-20',
          level: '国家级',
          desc: '全国大学生课外学术科技作品竞赛',
          template: guideTemplates.challengeCup
        }
      ]
    } else {
      competitionOptions.value = list.map(item => ({
        template: resolveGuideTemplate(item.competitionName),
        id: item.competitionId,
        name: item.competitionName,
        time: formatDate(item.registerStartTime),
        level: resolveGuideTemplate(item.competitionName)?.level || '国家级',
        desc: item.description || '暂无竞赛说明'
      }))
    }
    console.log('转换后的竞赛选项:', competitionOptions.value)
    console.log('转换后的竞赛选项长度:', competitionOptions.value.length)
  } catch (error) {
    console.error('加载竞赛列表失败:', error)
    ElMessage({
      message: error.message || '加载竞赛列表失败，请稍后重试',
      type: 'error'
    })
    // 使用静态数据作为后备
    console.log('使用静态数据作为后备...')
    competitionOptions.value = [
      {
        id: 1,
        name: '互联网+创新创业大赛',
        time: '2024-05-01',
        level: '国家级',
        desc: '面向全国高校学生的创新创业大赛',
        template: guideTemplates.internetPlus
      },
      {
        id: 2,
        name: '数学建模竞赛',
        time: '2024-06-15',
        level: '国家级',
        desc: '培养学生数学建模能力的竞赛',
        template: guideTemplates.mathModeling
      },
      {
        id: 3,
        name: '挑战杯',
        time: '2024-07-20',
        level: '国家级',
        desc: '全国大学生课外学术科技作品竞赛',
        template: guideTemplates.challengeCup
      }
    ]
    console.log('静态数据设置完成:', competitionOptions.value)
    console.log('静态数据长度:', competitionOptions.value.length)
  } finally {
    loading.value = false
    console.log('加载完成，最终竞赛选项:', competitionOptions.value)
    console.log('最终竞赛选项长度:', competitionOptions.value.length)
    console.log('最终筛选后的竞赛:', filteredCompetitions.value)
    console.log('最终筛选后的竞赛长度:', filteredCompetitions.value.length)
  }
}

const loadGuideDetail = async (competitionId) => {
  try {
    const res = await timeoutPromise(REQUEST_TIMEOUT, getCompetitionDetail(competitionId))
    const compData = res?.data || {}
    
    currentCompData.value = buildGuideData(compData)
  } catch (error) {
    console.error('加载竞赛详情失败:', error)
    ElMessage({
      message: error.message || '加载竞赛详情失败，请稍后重试',
      type: 'error'
    })
    // 使用模板数据作为后备
    const template = resolveGuideTemplate(competitionOptions.value.find(item => item.id === competitionId)?.name || '')
    currentCompData.value = buildGuideData({ competitionName: template?.name || '竞赛详情' })
  }
}

function getCompIcon(id) {
  const comp = competitionOptions.value.find(item => item.id === id)
  const text = `${comp?.name || ''}${comp?.level || ''}`.toLowerCase()
  if (text.includes('数学') || text.includes('建模')) return 'fas fa-calculator'
  if (text.includes('互联网') || text.includes('创新')) return 'fas fa-rocket'
  if (text.includes('挑战') || text.includes('科技')) return 'fas fa-lightbulb'
  return 'fas fa-trophy'
}

async function enterDetail(id) {
  selectedCompId.value = id
  await loadGuideDetail(id)
  activeSection.value = 'intro'
  viewMode.value = 'detail'
  window.scrollTo({ top: 0, behavior: 'smooth' })
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
</script>

<style scoped>
/* ==================== 基础样式与变量 ==================== */
:root {
  --primary-color: #6366f1;
  --primary-gradient: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  --bg-soft-blue: #f0f9ff;
  --text-main: #1e293b;
  --text-muted: #64748b;
  --card-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.05);
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
}

.flex-between { display: flex; justify-content: space-between; align-items: center; }

.animate-fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ==================== 加载状态 ==================== */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #64748b;
}

.loading-spinner {
  font-size: 48px;
  color: #6366f1;
  margin-bottom: 16px;
}

.loading-state p {
  font-size: 16px;
  color: #64748b;
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

/* ==================== Hero 区域 ==================== */
.hero-section {
  position: relative;
  padding: 80px 0 20px;
  background-color: #ffffff;
  /* 采用现代 Mesh Gradient (网格渐变) 设计，营造通透高级感 */
  background-image: 
    radial-gradient(at 0% 0%, rgba(99, 102, 241, 0.08) 0px, transparent 50%),
    radial-gradient(at 100% 0%, rgba(168, 85, 247, 0.08) 0px, transparent 50%),
    radial-gradient(at 100% 100%, rgba(99, 102, 241, 0.08) 0px, transparent 50%),
    radial-gradient(at 0% 100%, rgba(168, 85, 247, 0.08) 0px, transparent 50%);
  color: #1e293b;
  text-align: center;
  overflow: hidden;
}

/* 装饰性图标 */
.hero-decor {
  position: absolute;
  font-size: 48px;
  color: rgba(99, 102, 241, 0.05);
  pointer-events: none;
  z-index: 1;
  animation: float 6s ease-in-out infinite;
}

.decor-1 { top: 15%; left: 10%; animation-delay: 0s; }
.decor-2 { top: 60%; left: 15%; animation-delay: 1s; font-size: 36px; }
.decor-3 { top: 20%; right: 12%; animation-delay: 2s; font-size: 42px; }
.decor-4 { bottom: 15%; right: 15%; animation-delay: 3s; }

.hero-section::before {
  content: '';
  position: absolute;
  top: -20%;
  left: -10%;
  width: 60%;
  height: 100%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.1) 0%, transparent 70%);
  z-index: 0;
  filter: blur(80px);
}

.hero-section::after {
  content: '';
  position: absolute;
  bottom: -20%;
  right: -10%;
  width: 60%;
  height: 100%;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.1) 0%, transparent 70%);
  z-index: 0;
  filter: blur(80px);
}

.hero-content {
  position: relative;
  z-index: 2;
  max-width: 900px;
  margin: 0 auto;
}

.hero-badge {
  display: inline-block;
  padding: 8px 20px;
  background: rgba(99, 102, 241, 0.08);
  border: 1px solid rgba(99, 102, 241, 0.15);
  border-radius: 100px;
  color: #6366f1;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 1.5px;
  margin-bottom: 24px;
  text-transform: uppercase;
  backdrop-filter: blur(8px);
}

.hero-title {
  font-size: 52px;
  font-weight: 900;
  margin-bottom: 20px;
  letter-spacing: -1.5px;
  color: #0f172a;
  line-height: 1.1;
}

.accent-text {
  background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);
  -webkit-background-clip: text;
  color: transparent;
  position: relative;
  display: inline-block;
  filter: drop-shadow(0 0 20px rgba(99, 102, 241, 0.4));
}

.accent-text::after {
  content: '';
  position: absolute;
  left: 5%;
  right: 5%;
  bottom: 8px;
  height: 10px;
  background: rgba(99, 102, 241, 0.12);
  border-radius: 5px;
  z-index: -1;
}

.hero-subtitle {
  font-size: 18px;
  color: #64748b;
  margin-bottom: 40px;
  font-weight: 500;
  letter-spacing: -0.2px;
}

.hero-search-wrap {
  max-width: 720px;
  margin: 0 auto;
}

.search-input-box {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 20px;
  padding: 10px 10px 10px 24px;
  box-shadow: 
    0 4px 6px -1px rgba(0, 0, 0, 0.05), 
    0 20px 40px -8px rgba(99, 102, 241, 0.12);
  border: 1px solid #e2e8f0;
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.search-input-box:focus-within {
  border-color: #6366f1;
  box-shadow: 
    0 10px 15px -3px rgba(99, 102, 241, 0.1),
    0 30px 60px -12px rgba(99, 102, 241, 0.2);
  transform: translateY(-4px);
}

.search-input-box i {
  color: #94a3b8;
  font-size: 18px;
}

.search-input-box input {
  flex: 1;
  border: none;
  outline: none;
  padding: 12px 16px;
  font-size: 17px;
  color: #1e293b;
  font-weight: 500;
}

.search-btn {
  background: #6366f1;
  color: white;
  border: none;
  padding: 12px 36px;
  border-radius: 14px;
  font-weight: 700;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.search-btn:hover {
  background: #4f46e5;
  transform: scale(1.02);
  box-shadow: inset 0 2px 4px rgba(255, 255, 255, 0.2);
}

.search-btn:active {
  transform: scale(0.98);
}

/* 标签云样式 */
.hot-tags-cloud {
  margin-top: 28px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
}

.hot-label {
  font-size: 14px;
  color: #94a3b8;
  font-weight: 600;
}

.tags-list {
  display: flex;
  gap: 10px;
}

.tag-pill {
  padding: 6px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 100px;
  font-size: 13px;
  color: #64748b;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.tag-pill:hover {
  border-color: #6366f1;
  color: #6366f1;
  background: rgba(99, 102, 241, 0.05);
  transform: translateY(-2px);
}

.tag-pill.active {
  background: #6366f1;
  border-color: #6366f1;
  color: white;
  box-shadow: 0 8px 16px -4px rgba(99, 102, 241, 0.3);
}

/* ==================== 列表内容 ==================== */
.list-content-wrap {
  padding: 20px 0 100px;
  background: #f8fafc;
}

.empty-state {
  padding: 80px 0;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  color: #cbd5e1;
  margin-bottom: 24px;
}

.empty-state h3 {
  font-size: 24px;
  color: #1e293b;
  margin-bottom: 12px;
}

.empty-state p {
  color: #64748b;
  margin-bottom: 32px;
}

.reset-search-btn {
  background: white;
  border: 1px solid #e2e8f0;
  padding: 12px 32px;
  border-radius: 12px;
  color: #6366f1;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
}

.reset-search-btn:hover {
  background: #f1f5f9;
  border-color: #6366f1;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.section-main-title {
  font-size: 24px;
  font-weight: 800;
  color: #1e293b;
  position: relative;
  padding-left: 18px;
}

.section-main-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: #6366f1;
  border-radius: 4px;
}

.ai-button {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #6366f1;
  color: white;
  padding: 10px 24px;
  border-radius: 100px;
  font-weight: 700;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 10px 20px rgba(99, 102, 241, 0.2);
}

.ai-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 30px rgba(99, 102, 241, 0.3);
  background: #4f46e5;
}

.guide-cards-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.premium-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  border: 1px solid #f1f5f9;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}

.premium-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
  border-color: #e2e8f0;
}

.card-body {
  padding: 28px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-badge {
  display: inline-block;
  padding: 4px 12px;
  background: #eef2ff;
  color: #6366f1;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
  margin-bottom: 20px;
  width: fit-content;
}

.card-title {
  font-size: 18px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 50px;
}

.card-desc {
  color: #64748b;
  font-size: 13.5px;
  line-height: 1.6;
  margin-bottom: 24px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 44px;
}

.card-footer {
  margin-top: auto;
  padding-top: 24px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #94a3b8;
  font-size: 14px;
  font-weight: 500;
}

.card-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6366f1;
  font-weight: 700;
  font-size: 14px;
  transition: all 0.2s;
}

.premium-card:hover .card-link {
  transform: translateX(4px);
  color: #4f46e5;
}

/* ==================== 详情页 ==================== */
.detail-page-container {
  background: #f8fafc;
  min-height: 100vh;
}

.detail-header-nav {
  background: rgba(240, 249, 255, 0.6); /* 淡蓝色背景 */
  backdrop-filter: blur(10px);
  padding: 24px 0 32px;
  position: static;
  border-bottom: 1px solid rgba(226, 232, 240, 0.5);
}

.nav-container-col {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.nav-container-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 40px;
}

.title-section {
  flex: 1;
  min-width: 0;
}

.files-section {
  width: 320px;
  flex-shrink: 0;
  background: transparent;
  border: none;
  border-radius: 8px;
  padding: 12px;
  box-shadow: none;
}

.files-header {
  border-bottom: none;
  padding-bottom: 8px;
  margin-bottom: 8px;
}

/* 响应式调整 */
@media (max-width: 1024px) {
  .nav-container-row {
    flex-direction: column;
    gap: 24px;
  }
  
  .files-section {
    width: 100%;
  }
  
  .detail-flex-layout {
    flex-direction: column;
  }
  
  .detail-sidebar {
    width: 100%;
  }
  
  .sidebar-sticky {
    position: static;
  }
}

.back-link-simple {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #6366f1; /* 主题紫色/蓝色 */
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  align-self: flex-start;
}

.back-link-simple i {
  font-size: 12px;
  transition: transform 0.3s;
}

.back-link-simple:hover {
  color: #4f46e5;
  text-decoration: underline;
  text-underline-offset: 4px;
}

.back-link-simple:hover i {
  transform: translateX(-3px);
}

.page-main-title {
  font-size: 32px;
  font-weight: 800;
  color: #0f172a; /* 深色文字 */
  line-height: 1.2;
  margin: 0 0 12px 0;
  letter-spacing: -0.5px;
}

.competition-level {
  margin-top: 8px;
}

.level-tag {
  display: inline-block;
  padding: 6px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 100px;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.detail-main-wrap {
  padding: 40px 0 80px;
}

.detail-flex-layout {
  display: flex;
  gap: 32px;
}

.detail-sidebar {
  width: 260px;
  flex-shrink: 0;
}

.sidebar-sticky {
  position: sticky;
  top: 130px;
}

.comp-info-brief {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
  padding: 0 4px;
}

.brief-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  box-shadow: 0 10px 20px rgba(99, 102, 241, 0.2);
}

.brief-text {
  text-align: left;
}

.brief-text h4 {
  font-size: 20px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 4px;
  letter-spacing: -0.5px;
}

.brief-text p {
  font-size: 13px;
  color: #94a3b8;
  font-weight: 500;
}

.side-menu {
  background: white;
  border-radius: 24px;
  padding: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
  border: 1px solid #f1f5f9;
}

.side-menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #64748b;
  font-weight: 700;
  font-size: 15px;
  margin-bottom: 4px;
}

.side-menu-item:last-child {
  margin-bottom: 0;
}

.side-menu-item:hover {
  background: #f8fafc;
  color: #1e293b;
}

.side-menu-item.active {
  background: #f5f3ff;
  color: #6366f1;
}

.item-icon-wrap {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.detail-content-area {
  flex: 1;
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
  border: 1px solid #f1f5f9;
}

.detail-section {
  padding: 40px 0;
  border-bottom: 1px solid #f1f5f9;
}

.detail-section:last-child {
  border-bottom: none;
}

.detail-section:first-child {
  padding-top: 0;
}

.section-header {
  margin-bottom: 32px;
}

.content-main-title {
  font-size: 28px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 0;
  letter-spacing: -0.5px;
  position: relative;
  padding-left: 18px;
  display: flex;
  align-items: center;
}

.content-main-title::before {
  content: '';
  position: absolute;
  left: 0;
  width: 4px;
  height: 24px;
  background: #6366f1;
  border-radius: 2px;
}

.title-underline {
  display: none;
}

/* AI 悬浮球 */
.ai-float-bubble {
  position: fixed;
  right: 40px;
  bottom: 40px;
  z-index: 2000;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.ai-bubble-inner {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 26px;
  box-shadow: 0 16px 32px rgba(99, 102, 241, 0.4);
  position: relative;
}

.ai-bubble-inner::after {
  content: '';
  position: absolute;
  top: -4px;
  right: -4px;
  width: 16px;
  height: 16px;
  background: #10b981;
  border: 3px solid white;
  border-radius: 50%;
}

.ai-float-bubble:hover {
  transform: scale(1.1) translateY(-8px);
}

.ai-float-bubble:hover .ai-bubble-inner {
  box-shadow: 0 30px 60px rgba(99, 102, 241, 0.6);
}

.ai-bubble-tip {
  position: absolute;
  right: 90px;
  top: 50%;
  transform: translateY(-50%);
  background: white;
  padding: 12px 24px;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  white-space: nowrap;
  font-weight: 700;
  color: #1e293b;
  opacity: 0;
  pointer-events: none;
  transition: all 0.3s;
}

.ai-float-bubble:hover .ai-bubble-tip {
  opacity: 1;
  right: 100px;
}

/* 内容组件样式 */
.info-grid { 
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 32px; 
}
.info-card { 
  display: flex; 
  align-items: center;
  padding: 16px 20px; 
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  border-radius: 12px;
  transition: all 0.2s ease;
}
.info-card:hover {
  background: white;
  border-color: #6366f1;
}
.info-card label { 
  width: 100px;
  font-size: 13px; 
  font-weight: 700; 
  color: #94a3b8; 
  margin-bottom: 0; 
  text-transform: none;
  letter-spacing: 0;
  flex-shrink: 0;
}
.info-card p { 
  font-size: 15px; 
  font-weight: 600; 
  color: #1e293b; 
  margin: 0;
  line-height: 1.4;
}
.tag-p { 
  display: inline-flex;
  padding: 2px 10px;
  background: #6366f1;
  color: white !important;
  border-radius: 6px;
  font-size: 12px !important;
  width: fit-content;
  box-shadow: none;
}

.purpose-box { 
  padding: 20px 24px; 
  background: #f1f5f9;
  border-radius: 16px;
  border-left: 4px solid #6366f1;
}
.purpose-box label { 
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700; 
  color: #1e293b; 
  margin-bottom: 10px; 
}
.purpose-box label i {
  color: #6366f1;
  font-size: 16px;
}
.purpose-box p { 
  font-size: 14px; 
  line-height: 1.6; 
  color: #475569; 
  margin: 0;
}

.modern-steps { 
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px 0;
}

.modern-step-item { 
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 24px;
  background: #f8fafc;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.modern-step-item:hover {
  background: #f1f5f9;
}

.step-circle {
  width: 32px; 
  height: 32px;
  background: #6366f1; 
  border-radius: 50%;
  display: flex; 
  align-items: center; 
  justify-content: center;
  font-weight: 700; 
  color: white; 
  font-size: 14px;
  flex-shrink: 0;
}

.step-desc { 
  color: #1e293b; 
  font-size: 16px; 
  font-weight: 600;
  margin: 0;
}

.timeline-grid { display: flex; flex-direction: column; gap: 8px; }
.timeline-card {
  display: flex; 
  align-items: center; 
  gap: 16px; 
  padding: 12px 16px; 
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid transparent;
  transition: all 0.2s ease;
  margin-bottom: 0;
}
.timeline-card:hover {
  background: white;
  border-color: #6366f1;
  transform: none;
  box-shadow: none;
}
.timeline-icon {
  width: 32px; 
  height: 32px; 
  background: white; 
  border-radius: 8px;
  display: flex; 
  align-items: center; 
  justify-content: center;
  color: #6366f1; 
  font-size: 14px;
  flex-shrink: 0;
  box-shadow: none;
}
.timeline-info { display: flex; align-items: center; flex: 1; gap: 16px; }
.timeline-info label { 
  width: 100px; 
  font-size: 13px; 
  color: #94a3b8; 
  font-weight: 700; 
  flex-shrink: 0;
  text-transform: none;
}
.timeline-info p { 
  font-size: 14px; 
  font-weight: 700; 
  color: #1e293b; 
  margin: 0; 
}

.require-grid { display: flex; flex-direction: column; gap: 8px; }
.require-item {
  display: flex; 
  align-items: center; 
  gap: 16px; 
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 10px;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.require-item:hover {
  background: white;
  border-color: #6366f1;
  transform: none;
  box-shadow: none;
}
.require-item i { font-size: 16px; color: #6366f1; width: 20px; text-align: center; }
.req-text { display: flex; align-items: center; flex: 1; gap: 16px; }
.req-text label { 
  width: 100px; 
  font-size: 13px; 
  color: #94a3b8; 
  font-weight: 700; 
  flex-shrink: 0;
  text-transform: none;
}
.req-text p { font-size: 14px; font-weight: 700; color: #1e293b; margin: 0; }

.premium-table { border-radius: 12px; overflow: hidden; border: 1px solid #f1f5f9; }
.table-item-name { font-weight: 700; color: #6366f1; }

.faq-list-wrap { 
  display: flex; 
  flex-direction: column; 
  gap: 6px; 
}

.faq-card { 
  background: #f8fafc;
  border-radius: 8px;
  border: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  position: relative;
}

.faq-card::after {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #6366f1;
  opacity: 0;
  transition: opacity 0.3s;
}

.faq-card.expanded::after {
  opacity: 1;
}

.faq-q { 
  display: flex; 
  gap: 12px; 
  align-items: flex-start; /* 改为顶对齐，通过 padding 控制视觉居中 */
  padding: 14px 24px;
  min-height: 48px;
  cursor: pointer;
  user-select: none;
  box-sizing: border-box;
}

.faq-card.expanded {
  background: #f8fafc;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.q-icon { 
  color: #6366f1; 
  font-size: 14px; 
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 20px;
  height: 20px; /* 给图标一个固定大小，便于对齐 */
  margin-top: 1px; /* 微调图标对齐第一行文字 */
}

.faq-q h4 { 
  font-size: 15px; 
  font-weight: 700; 
  color: #1e293b; 
  margin: 0; 
  flex: 1;
  line-height: 20px; /* 与图标高度保持一致 */
}

.faq-arrow {
  color: #94a3b8;
  font-size: 12px;
  transition: transform 0.3s;
  display: flex;
  align-items: center;
  height: 20px; /* 与图标/行高对齐 */
}

.faq-card.expanded .faq-arrow {
  transform: rotate(180deg);
}

.faq-a-wrapper {
  display: grid;
  grid-template-rows: 0fr;
  transition: grid-template-rows 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.faq-card.expanded .faq-a-wrapper {
  grid-template-rows: 1fr;
}

.faq-a { 
  min-height: 0;
  display: flex; 
  gap: 12px; 
  padding: 0 24px 24px 24px;
  opacity: 0;
  transition: opacity 0.2s;
}

.faq-card.expanded .faq-a {
  opacity: 1;
}

.a-icon { 
  color: #10b981; 
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 4px;
}

.faq-a p { 
  color: #64748b; 
  line-height: 1.7; 
  margin: 0; 
  font-size: 14px; 
  flex: 1;
}

.faq-footer-contact {
  margin-top: 32px;
  background: #f8fafc;
  border: 1px dashed #e2e8f0;
  border-radius: 16px;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
}

.faq-footer-contact:hover {
  background: #f5f3ff;
  border-color: #6366f1;
  transform: translateY(-2px);
}

.contact-text {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #64748b;
  font-size: 14px;
}

.contact-text i {
  font-size: 20px;
  color: #6366f1;
}

.contact-text strong {
  color: #6366f1;
}

.faq-footer-contact > i {
  color: #94a3b8;
  font-size: 14px;
  transition: transform 0.3s;
}

.faq-footer-contact:hover > i {
  transform: translateX(4px);
  color: #6366f1;
}

/* ==================== AI 视图 ==================== */
.ai-view-container { background: #f5f3ff; min-height: 100vh; }
.ai-view-header { background: white; padding: 20px 0; border-bottom: 1px solid #ddd6fe; }
.ai-header-left { display: flex; align-items: center; gap: 20px; }
.back-circle {
  width: 44px; height: 44px; border-radius: 50%; border: 1px solid #e2e8f0;
  display: flex; align-items: center; justify-content: center; cursor: pointer;
  transition: all 0.2s;
}
.back-circle:hover { background: #f8fafc; color: var(--primary-color); border-color: var(--primary-color); }
.ai-header-info h2 { font-size: 22px; font-weight: 800; color: #1e293b; }
.online-dot { display: inline-block; width: 8px; height: 8px; background: #10b981; border-radius: 50%; margin-right: 6px; }
.ai-header-info p { font-size: 13px; color: #64748b; }
.ai-header-tags { display: flex; gap: 10px; }
.ai-header-tags span { padding: 6px 14px; background: #f3f4f6; border-radius: 100px; font-size: 12px; font-weight: 600; color: #64748b; }

.ai-frame-content { padding: 40px 0; }
.iframe-wrapper { background: white; border-radius: 24px; overflow: hidden; box-shadow: 0 20px 60px rgba(0,0,0,0.1); }

/* ==================== 底部 ==================== */
.premium-footer { background: #0f172a; padding: 40px 0; color: white; }
.footer-grid { display: flex; justify-content: space-between; align-items: center; }
.footer-contact { display: flex; gap: 24px; }
.contact-pill {
  padding: 10px 20px; background: rgba(255,255,255,0.05); border-radius: 100px;
  font-size: 14px; font-weight: 500;
}
.contact-pill i { color: #818cf8; margin-right: 8px; }
.footer-org p { font-size: 14px; color: #94a3b8; }

/* ==================== 讨论区入口卡片样式 ==================== */
.discussion-entry-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.discussion-entry-card:hover {
  border-color: #6366f1;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.15);
  transform: translateY(-2px);
}

.entry-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.entry-icon i {
  font-size: 24px;
  color: white;
}

.entry-content {
  flex: 1;
}

.entry-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 6px 0;
}

.entry-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.entry-arrow {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.discussion-entry-card:hover .entry-arrow {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
}

.entry-arrow i {
  font-size: 14px;
  color: #64748b;
  transition: all 0.3s ease;
}

.discussion-entry-card:hover .entry-arrow i {
  color: white;
}
</style>
