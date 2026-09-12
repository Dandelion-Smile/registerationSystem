<template>
  <div class="discussion-page">
    <StudentNavbar />

    <!-- 页面头部横幅 - 优化设计 -->
    <section class="page-banner">
      <div class="banner-background">
        <div class="banner-pattern"></div>
        <div class="banner-gradient"></div>
      </div>
      <div class="banner-content">
        <div class="banner-breadcrumb">
          <router-link to="/competition-guide" class="breadcrumb-link">
            <i class="fas fa-home"></i>
            赛事指南
          </router-link>
          <span class="breadcrumb-separator">
            <i class="fas fa-chevron-right"></i>
          </span>
          <span class="breadcrumb-current">讨论区</span>
        </div>
        
        <div class="banner-main">
          <div class="banner-icon-wrapper">
            <div class="banner-icon">
              <i class="fas fa-comments"></i>
            </div>
            <div class="banner-icon-glow"></div>
          </div>
          <div class="banner-text">
            <h1 class="banner-title">讨论区</h1>
            <p class="banner-subtitle">与其他同学交流经验、组队招募、问题求助</p>
          </div>
        </div>
        
        <div class="banner-stats">
          <div class="banner-stat">
            <div class="stat-icon-wrapper">
              <i class="fas fa-comment-dots"></i>
            </div>
            <div class="stat-info">
              <span class="stat-number">{{ discussionList.length }}</span>
              <span class="stat-text">话题</span>
            </div>
          </div>
          <div class="banner-stat">
            <div class="stat-icon-wrapper">
              <i class="fas fa-users"></i>
            </div>
            <div class="stat-info">
              <span class="stat-number">{{ discussionStats.participants }}</span>
              <span class="stat-text">参与者</span>
            </div>
          </div>
          <div class="banner-stat">
            <div class="stat-icon-wrapper highlight">
              <i class="fas fa-chart-line"></i>
            </div>
            <div class="stat-info">
              <span class="stat-number">{{ discussionStats.todayPosts }}</span>
              <span class="stat-text">今日新增</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 主内容区 -->
    <main class="main-content">
      <div class="container">
        <!-- 工具栏 -->
        <div class="toolbar">
          <div class="toolbar-left">
            <!-- 搜索框 -->
            <div class="search-box">
              <i class="fas fa-search"></i>
              <input 
                v-model="searchKeyword" 
                type="text" 
                placeholder="搜索话题..."
                @keyup.enter="handleSearch"
              >
              <button v-if="searchKeyword" class="search-clear" @click="searchKeyword = ''">
                <i class="fas fa-times"></i>
              </button>
            </div>

            <!-- 竞赛选择器 -->
            <el-select 
              v-model="selectedCompetition" 
              placeholder="全部竞赛" 
              class="competition-select"
              clearable
              @change="loadDiscussions"
            >
              <el-option 
                v-for="comp in competitionList" 
                :key="comp.id" 
                :label="comp.name" 
                :value="comp.id" 
              />
            </el-select>

            <!-- 分类标签 -->
            <div class="tag-filter">
              <button 
                class="filter-btn" 
                :class="{ active: activeTag === '' }" 
                @click="activeTag = ''"
              >
                全部
              </button>
              <button 
                v-for="tag in tagList" 
                :key="tag.value" 
                class="filter-btn" 
                :class="{ active: activeTag === tag.value, [tag.class]: true }"
                @click="activeTag = tag.value"
              >
                {{ tag.label }}
              </button>
            </div>
          </div>

          <div class="toolbar-right">
            <!-- 排序选项 -->
            <div class="sort-dropdown">
              <button class="sort-btn" @click="showSortMenu = !showSortMenu">
                <i class="fas fa-sort-amount-down"></i>
                <span>{{ sortOptions.find(s => s.value === sortBy)?.label }}</span>
                <i class="fas fa-chevron-down" :class="{ rotated: showSortMenu }"></i>
              </button>
              <div v-show="showSortMenu" class="sort-menu">
                <div 
                  v-for="option in sortOptions" 
                  :key="option.value"
                  class="sort-item"
                  :class="{ active: sortBy === option.value }"
                  @click="sortBy = option.value; showSortMenu = false"
                >
                  <i :class="option.icon"></i>
                  {{ option.label }}
                </div>
              </div>
            </div>

            <button class="btn-create-topic" @click="openTopicDialog">
              <i class="fas fa-plus"></i>
              发布话题
            </button>
          </div>
        </div>

        <!-- 筛选结果统计 -->
        <div v-if="searchKeyword || selectedCompetition || activeTag" class="filter-summary">
          <span class="filter-count">共 {{ filteredDiscussions.length }} 个结果</span>
          <div class="active-filters">
            <span v-if="searchKeyword" class="filter-tag">
              搜索: {{ searchKeyword }}
              <i class="fas fa-times" @click="searchKeyword = ''"></i>
            </span>
            <span v-if="selectedCompetition" class="filter-tag">
              {{ competitionList.find(c => c.id === selectedCompetition)?.name }}
              <i class="fas fa-times" @click="selectedCompetition = ''"></i>
            </span>
            <span v-if="activeTag" class="filter-tag">
              {{ activeTag }}
              <i class="fas fa-times" @click="activeTag = ''"></i>
            </span>
            <button class="clear-all" @click="clearAllFilters">清除全部</button>
          </div>
        </div>

        <!-- 内容布局 -->
        <div class="content-layout">
          <!-- 左侧话题列表 -->
          <div class="content-main">
            <!-- 话题列表 -->
            <div class="discussion-list">
              <div 
                v-for="topic in filteredDiscussions" 
                :key="topic.id" 
                class="topic-card"
                @click="viewTopicDetail(topic)"
              >
                <div class="topic-avatar">
                  <img :src="topic.authorAvatar || '/default-avatar.png'" :alt="topic.authorName">
                </div>

                <div class="topic-content">
                  <div class="topic-header">
                    <h4 class="topic-title">{{ topic.title }}</h4>
                    <span class="topic-tag" :class="topic.tagType">{{ topic.tag }}</span>
                  </div>
                  <p class="topic-desc">{{ topic.content }}</p>
                  <div class="topic-meta">
                    <div class="meta-left">
                      <span class="author-name">{{ topic.authorName }}</span>
                      <span class="meta-dot">·</span>
                      <span class="competition-badge" v-if="topic.competitionName">
                        <i class="fas fa-trophy"></i>
                        {{ topic.competitionName }}
                      </span>
                      <span class="meta-dot" v-if="topic.competitionName">·</span>
                      <span class="post-time">{{ formatTime(topic.createTime) }}</span>
                    </div>
                    <div class="meta-right">
                      <span class="meta-item vote-item" @click.stop="likeTopic(topic)">
                        <i class="fas fa-thumbs-up" :class="{ liked: topic.isLiked }"></i>
                        {{ topic.likeCount }}
                      </span>
                      <span class="meta-item">
                        <i class="fas fa-comment-alt"></i>
                        {{ topic.replyCount }} 回复
                      </span>
                      <span class="meta-item">
                        <i class="fas fa-eye"></i>
                        {{ topic.viewCount }} 浏览
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="filteredDiscussions.length === 0" class="discussion-empty">
              <div class="empty-icon">
                <i class="fas fa-comments"></i>
              </div>
              <p class="empty-text">暂无讨论话题</p>
              <p class="empty-subtext">成为第一个发起讨论的人吧！</p>
              <button class="btn-create-topic empty-btn" @click="openTopicDialog">
                <i class="fas fa-plus"></i>
                发布话题
              </button>
            </div>

            <!-- 加载更多 -->
            <div v-if="filteredDiscussions.length > 0 && hasMoreTopics" class="load-more">
              <button class="btn-load-more" @click="loadMoreTopics" :loading="loadingMore">
                {{ loadingMore ? '加载中...' : '加载更多' }}
              </button>
            </div>
          </div>

          <!-- 右侧侧边栏 -->
          <aside class="content-sidebar">
            <!-- 热门话题 -->
            <div class="sidebar-card">
              <div class="card-header">
                <i class="fas fa-fire"></i>
                <h3>热门话题</h3>
              </div>
              <div class="hot-topics">
                <div 
                  v-for="(topic, index) in hotTopics" 
                  :key="topic.id" 
                  class="hot-topic-item"
                  @click="viewTopicDetail(topic)"
                >
                  <span class="hot-rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
                  <div class="hot-topic-info">
                    <p class="hot-topic-title">{{ topic.title }}</p>
                    <span class="hot-topic-heat">
                      <i class="fas fa-fire"></i>
                      {{ topic.heat }} 热度
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 推荐竞赛 -->
            <div class="sidebar-card">
              <div class="card-header">
                <i class="fas fa-trophy"></i>
                <h3>推荐竞赛</h3>
              </div>
              <div class="competition-list">
                <div 
                  v-for="comp in recommendedCompetitions" 
                  :key="comp.id" 
                  class="competition-item"
                  @click="selectedCompetition = comp.id"
                >
                  <div class="comp-icon" :style="{ background: comp.color }">
                    <i :class="comp.icon"></i>
                  </div>
                  <div class="comp-info">
                    <p class="comp-name">{{ comp.name }}</p>
                    <span class="comp-count">{{ comp.topicCount }} 个话题</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 社区公告 -->
            <div class="sidebar-card">
              <div class="card-header">
                <i class="fas fa-bullhorn"></i>
                <h3>社区公告</h3>
              </div>
              <div class="notice-list">
                <div class="notice-item">
                  <span class="notice-dot"></span>
                  <p class="notice-text">请遵守社区规范，文明交流</p>
                </div>
                <div class="notice-item">
                  <span class="notice-dot"></span>
                  <p class="notice-text">禁止发布广告和违规内容</p>
                </div>
                <div class="notice-item">
                  <span class="notice-dot"></span>
                  <p class="notice-text">遇到问题请联系管理员</p>
                </div>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </main>

    <!-- 发布话题对话框 -->
    <el-dialog
      v-model="topicDialogVisible"
      title="发布话题"
      width="600px"
      class="topic-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="currentTopic" label-position="top">
        <el-form-item label="关联竞赛" required>
          <el-select v-model="currentTopic.competitionId" placeholder="请选择关联的竞赛" style="width: 100%">
            <el-option 
              v-for="comp in competitionList" 
              :key="comp.id" 
              :label="comp.name" 
              :value="comp.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="话题标题" required>
          <el-input
            v-model="currentTopic.title"
            placeholder="请输入话题标题，简洁明了地描述你的问题或分享"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="话题分类" required>
          <el-select v-model="currentTopic.tag" placeholder="请选择话题分类" style="width: 100%">
            <el-option label="报名咨询" value="报名咨询" />
            <el-option label="经验分享" value="经验分享" />
            <el-option label="组队招募" value="组队招募" />
            <el-option label="问题求助" value="问题求助" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="话题内容" required>
          <el-input
            v-model="currentTopic.content"
            type="textarea"
            :rows="6"
            placeholder="详细描述你的问题、经验或招募需求，让其他同学更好地理解和帮助你..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="topicDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTopic">发布</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StudentNavbar from '@/components/StudentNavbar.vue'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

// 竞赛列表
const competitionList = ref([
  { id: 1, name: '互联网+大学生创新创业大赛' },
  { id: 2, name: '蓝桥杯全国软件和信息技术专业人才大赛' },
  { id: 3, name: '全国大学生数学建模竞赛' },
  { id: 4, name: '挑战杯全国大学生课外学术科技作品竞赛' }
])

// 选中的竞赛
const selectedCompetition = ref('')

// 分类标签
const tagList = ref([
  { label: '报名咨询', value: '报名咨询', class: 'tag-primary' },
  { label: '经验分享', value: '经验分享', class: 'tag-success' },
  { label: '组队招募', value: '组队招募', class: 'tag-warning' },
  { label: '问题求助', value: '问题求助', class: 'tag-danger' },
  { label: '其他', value: '其他', class: 'tag-info' }
])

// 当前选中的标签
const activeTag = ref('')

// 讨论区数据
const discussionList = ref([
  {
    id: 1,
    title: '关于报名流程的疑问，求解答！',
    content: '请问报名时需要准备哪些材料？团队报名和个人报名有什么区别吗？希望有经验的同学能帮忙解答一下。',
    authorName: '张同学',
    authorAvatar: '',
    createTime: '2026-03-20 14:30',
    likeCount: 12,
    replyCount: 5,
    viewCount: 89,
    tag: '报名咨询',
    tagType: 'tag-primary',
    isLiked: false,
    competitionId: 1,
    competitionName: '互联网+大学生创新创业大赛',
    heat: 156
  },
  {
    id: 2,
    title: '分享一些备赛经验和资料',
    content: '整理了一些往年的优秀作品和备赛资料，希望能帮到大家。需要的朋友可以留言~',
    authorName: '李学长',
    authorAvatar: '',
    createTime: '2026-03-19 10:15',
    likeCount: 28,
    replyCount: 15,
    viewCount: 256,
    tag: '经验分享',
    tagType: 'tag-success',
    isLiked: true,
    competitionId: 2,
    competitionName: '蓝桥杯全国软件和信息技术专业人才大赛',
    heat: 328
  },
  {
    id: 3,
    title: '组队招募：寻找前端开发队友',
    content: '我们团队目前已有3人，还需要一名熟悉Vue/React的前端开发同学，有兴趣的可以联系我！',
    authorName: '王同学',
    authorAvatar: '',
    createTime: '2026-03-18 16:45',
    likeCount: 8,
    replyCount: 12,
    viewCount: 134,
    tag: '组队招募',
    tagType: 'tag-warning',
    isLiked: false,
    competitionId: 3,
    competitionName: '全国大学生数学建模竞赛',
    heat: 89
  },
  {
    id: 4,
    title: '比赛作品提交格式要求',
    content: '请问作品提交时有什么格式要求吗？需要包含哪些内容？',
    authorName: '刘同学',
    authorAvatar: '',
    createTime: '2026-03-17 09:20',
    likeCount: 5,
    replyCount: 3,
    viewCount: 67,
    tag: '问题求助',
    tagType: 'tag-danger',
    isLiked: false,
    competitionId: 4,
    competitionName: '挑战杯全国大学生课外学术科技作品竞赛',
    heat: 45
  },
  {
    id: 5,
    title: '请问初赛和决赛的评分标准有什么不同？',
    content: '想了解一下初赛和决赛在评分标准上有什么差异，需要重点准备哪些方面？',
    authorName: '陈同学',
    authorAvatar: '',
    createTime: '2026-03-16 14:00',
    likeCount: 15,
    replyCount: 8,
    viewCount: 178,
    tag: '报名咨询',
    tagType: 'tag-primary',
    isLiked: false,
    competitionId: 1,
    competitionName: '互联网+大学生创新创业大赛',
    heat: 198
  }
])

// 热门话题
const hotTopics = computed(() => {
  return [...discussionList.value]
    .sort((a, b) => b.heat - a.heat)
    .slice(0, 5)
})

// 推荐竞赛
const recommendedCompetitions = ref([
  { id: 1, name: '互联网+大学生创新创业大赛', topicCount: 128, icon: 'fas fa-rocket', color: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)' },
  { id: 2, name: '蓝桥杯', topicCount: 96, icon: 'fas fa-code', color: 'linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%)' },
  { id: 3, name: '数学建模', topicCount: 64, icon: 'fas fa-calculator', color: 'linear-gradient(135deg, #f59e0b 0%, #f97316 100%)' },
  { id: 4, name: '挑战杯', topicCount: 52, icon: 'fas fa-trophy', color: 'linear-gradient(135deg, #ef4444 0%, #ec4899 100%)' }
])

// 统计数据
const discussionStats = ref({
  participants: 156,
  todayPosts: 12
})

// 搜索关键词
const searchKeyword = ref('')

// 排序相关
const sortBy = ref('latest')
const showSortMenu = ref(false)
const sortOptions = ref([
  { label: '最新发布', value: 'latest', icon: 'fas fa-clock' },
  { label: '最多回复', value: 'replies', icon: 'fas fa-comment-alt' },
  { label: '最多浏览', value: 'views', icon: 'fas fa-eye' },
  { label: '最多点赞', value: 'likes', icon: 'fas fa-thumbs-up' },
  { label: '热度最高', value: 'heat', icon: 'fas fa-fire' }
])

// 处理搜索
const handleSearch = () => {
  // 搜索功能已通过计算属性实现
}

// 清除所有筛选
const clearAllFilters = () => {
  searchKeyword.value = ''
  selectedCompetition.value = ''
  activeTag.value = ''
}

// 过滤后的话题列表
const filteredDiscussions = computed(() => {
  let result = discussionList.value
  
  // 搜索过滤
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(t => 
      t.title.toLowerCase().includes(keyword) || 
      t.content.toLowerCase().includes(keyword) ||
      t.authorName.toLowerCase().includes(keyword)
    )
  }
  
  // 竞赛过滤
  if (selectedCompetition.value) {
    result = result.filter(t => t.competitionId === selectedCompetition.value)
  }
  
  // 标签过滤
  if (activeTag.value) {
    result = result.filter(t => t.tag === activeTag.value)
  }
  
  // 排序
  switch (sortBy.value) {
    case 'latest':
      result = [...result].sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      break
    case 'replies':
      result = [...result].sort((a, b) => b.replyCount - a.replyCount)
      break
    case 'views':
      result = [...result].sort((a, b) => b.viewCount - a.viewCount)
      break
    case 'likes':
      result = [...result].sort((a, b) => b.likeCount - a.likeCount)
      break
    case 'heat':
      result = [...result].sort((a, b) => b.heat - a.heat)
      break
  }
  
  return result
})

const hasMoreTopics = ref(true)
const loadingMore = ref(false)
const topicDialogVisible = ref(false)
const currentTopic = ref({
  title: '',
  content: '',
  tag: '报名咨询',
  competitionId: null
})

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const now = new Date()
  const postTime = new Date(time)
  const diff = now - postTime
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return time
}

// 加载讨论列表
const loadDiscussions = () => {
  // TODO: 从后端API加载讨论数据
}

// 打开发布话题对话框
const openTopicDialog = () => {
  topicDialogVisible.value = true
  currentTopic.value = { title: '', content: '', tag: '报名咨询', competitionId: selectedCompetition.value || null }
}

// 发布话题
const submitTopic = () => {
  if (!currentTopic.value.competitionId) {
    ElMessage.warning('请选择关联的竞赛')
    return
  }
  if (!currentTopic.value.title.trim()) {
    ElMessage.warning('请输入话题标题')
    return
  }
  if (!currentTopic.value.content.trim()) {
    ElMessage.warning('请输入话题内容')
    return
  }
  
  const comp = competitionList.value.find(c => c.id === currentTopic.value.competitionId)
  
  const newTopic = {
    id: Date.now(),
    title: currentTopic.value.title,
    content: currentTopic.value.content,
    authorName: userStore.name || '匿名用户',
    authorAvatar: userStore.avatar || '',
    createTime: new Date().toLocaleString(),
    likeCount: 0,
    replyCount: 0,
    viewCount: 0,
    tag: currentTopic.value.tag,
    tagType: getTagType(currentTopic.value.tag),
    isLiked: false,
    competitionId: currentTopic.value.competitionId,
    competitionName: comp ? comp.name : '',
    heat: 0
  }
  
  discussionList.value.unshift(newTopic)
  topicDialogVisible.value = false
  ElMessage.success('话题发布成功！')
}

// 获取标签类型
const getTagType = (tag) => {
  const tagMap = {
    '报名咨询': 'tag-primary',
    '经验分享': 'tag-success',
    '组队招募': 'tag-warning',
    '问题求助': 'tag-danger',
    '其他': 'tag-info'
  }
  return tagMap[tag] || 'tag-info'
}

// 点赞话题
const likeTopic = (topic) => {
  topic.isLiked = !topic.isLiked
  topic.likeCount += topic.isLiked ? 1 : -1
  ElMessage.success(topic.isLiked ? '点赞成功' : '已取消点赞')
}

// 查看话题详情
const viewTopicDetail = (topic) => {
  topic.viewCount++
  ElMessage.info('该功能正在开发中，敬请期待！')
}

// 加载更多话题
const loadMoreTopics = () => {
  loadingMore.value = true
  setTimeout(() => {
    loadingMore.value = false
    hasMoreTopics.value = false
  }, 1000)
}

// 处理用户菜单命令
</script>

<style scoped>
/* ==================== 基础样式 ==================== */
.discussion-page {
  min-height: 100vh;
  background: #f8fafc;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
}

/* ==================== 顶部导航 ==================== */
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

.logo-section i {
  font-size: 24px;
  color: white;
}

.logo-text {
  font-size: 18px;
  color: white;
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

.dropdown-trigger {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
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
  display: block;
  padding: 10px 16px;
  font-size: 14px;
  color: #374151;
  text-decoration: none;
  border-radius: 6px;
  transition: all 0.2s;
}

.dropdown-menu a:hover {
  background: #f3f4f6;
  color: #6366f1;
}

.nav-actions {
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  cursor: pointer;
  padding: 4px;
  border-radius: 50%;
  transition: all 0.2s;
}

.avatar-wrapper:hover {
  background: rgba(255, 255, 255, 0.15);
}

.user-avatar-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

/* ==================== 页面横幅 - 优化设计 ==================== */
.page-banner {
  position: relative;
  padding: 40px 0 48px;
  overflow: hidden;
  background: #0f172a;
}

.banner-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.banner-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(99, 102, 241, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(139, 92, 246, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(168, 85, 247, 0.1) 0%, transparent 60%);
}

.banner-gradient {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(99, 102, 241, 0.9) 0%, 
    rgba(139, 92, 246, 0.85) 35%, 
    rgba(168, 85, 247, 0.8) 70%,
    rgba(236, 72, 153, 0.7) 100%);
  backdrop-filter: blur(10px);
}

.banner-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  position: relative;
  z-index: 1;
}

/* 面包屑导航 */
.banner-breadcrumb {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.breadcrumb-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  text-decoration: none;
  transition: all 0.2s;
  padding: 6px 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.breadcrumb-link:hover {
  color: white;
  background: rgba(255, 255, 255, 0.2);
}

.breadcrumb-separator {
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
}

.breadcrumb-current {
  color: white;
  font-size: 14px;
  font-weight: 600;
}

/* 主标题区域 */
.banner-main {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 32px;
}

.banner-icon-wrapper {
  position: relative;
}

.banner-icon {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0.1) 100%);
  backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  z-index: 1;
}

.banner-icon i {
  font-size: 32px;
  color: white;
}

.banner-icon-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(255,255,255,0.3) 0%, transparent 70%);
  border-radius: 50%;
  z-index: 0;
}

.banner-text {
  flex: 1;
}

.banner-title {
  font-size: 42px;
  font-weight: 800;
  color: white;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.2);
}

.banner-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
  font-weight: 400;
}

/* 统计数据 */
.banner-stats {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.banner-stat {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  transition: all 0.3s;
}

.banner-stat:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}

.stat-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon-wrapper i {
  font-size: 20px;
  color: white;
}

.stat-icon-wrapper.highlight {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-number {
  font-size: 24px;
  font-weight: 800;
  color: white;
  line-height: 1;
}

.stat-text {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}

/* ==================== 主内容区 ==================== */
.main-content {
  padding: 32px 0 60px;
}

/* ==================== 工具栏 ==================== */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 搜索框 */
.search-box {
  position: relative;
  width: 280px;
}

.search-box > i {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  font-size: 16px;
}

.search-box input {
  width: 100%;
  height: 44px;
  padding: 0 40px 0 44px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  font-size: 14px;
  color: #1e293b;
  background: white;
  transition: all 0.2s;
}

.search-box input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.search-box input::placeholder {
  color: #94a3b8;
}

.search-clear {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  border: none;
  background: #e2e8f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  color: #64748b;
  font-size: 10px;
}

.search-clear:hover {
  background: #cbd5e1;
  color: #475569;
}

/* 排序下拉菜单 */
.sort-dropdown {
  position: relative;
}

.sort-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.sort-btn:hover {
  border-color: #6366f1;
  color: #6366f1;
}

.sort-btn i.rotated {
  transform: rotate(180deg);
}

.sort-btn > i:last-child {
  transition: transform 0.2s;
  font-size: 12px;
}

.sort-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.12);
  padding: 8px;
  min-width: 160px;
  z-index: 100;
  border: 1px solid #f1f5f9;
}

.sort-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.sort-item:hover {
  background: #f8fafc;
  color: #6366f1;
}

.sort-item.active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
  color: #6366f1;
  font-weight: 600;
}

.sort-item i {
  font-size: 14px;
  width: 16px;
}

.competition-select {
  width: 200px;
}

:deep(.competition-select .el-input__inner) {
  border-radius: 10px;
  border-color: #e2e8f0;
  height: 44px;
}

.tag-filter {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 10px 20px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 100px;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn:hover {
  border-color: #6366f1;
  color: #6366f1;
}

.filter-btn.active {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-color: transparent;
  color: white;
}

.filter-btn.active.tag-success {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
}

/* 筛选摘要 */
.filter-summary {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 14px 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
  flex-wrap: wrap;
}

.filter-count {
  font-size: 14px;
  font-weight: 600;
  color: #6366f1;
}

.active-filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-tag {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 100px;
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.filter-tag i {
  cursor: pointer;
  font-size: 11px;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.filter-tag i:hover {
  background: #fee2e2;
  color: #ef4444;
}

.clear-all {
  padding: 6px 14px;
  background: transparent;
  border: 1px dashed #cbd5e1;
  border-radius: 100px;
  font-size: 13px;
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-all:hover {
  border-color: #6366f1;
  color: #6366f1;
  background: rgba(99, 102, 241, 0.05);
}

.filter-btn.active.tag-warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.filter-btn.active.tag-danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.filter-btn.active.tag-info {
  background: linear-gradient(135deg, #64748b 0%, #475569 100%);
}

.btn-create-topic {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  white-space: nowrap;
}

.btn-create-topic:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4);
}

/* ==================== 内容布局 ==================== */
.content-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 28px;
}

/* ==================== 话题列表 - 优化设计 ==================== */
.discussion-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.topic-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.topic-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #6366f1 0%, #8b5cf6 100%);
  transform: scaleX(0);
  transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.topic-card:hover {
  border-color: #6366f1;
  box-shadow: 0 8px 30px rgba(99, 102, 241, 0.12);
  transform: translateY(-3px);
}

.topic-card:hover::before {
  transform: scaleX(1);
}

.topic-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: 3px solid #f8fafc;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.2);
  flex-shrink: 0;
  transition: all 0.3s;
}

.topic-card:hover .topic-avatar {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.topic-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.vote-item {
  cursor: pointer;
  transition: all 0.2s;
}

.vote-item:hover {
  color: #6366f1;
}

.vote-item i.liked {
  color: #6366f1;
}

.topic-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topic-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.topic-title {
  font-size: 17px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}

.topic-card:hover .topic-title {
  color: #6366f1;
}

.topic-tag {
  padding: 5px 14px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
  transition: all 0.2s;
}

.tag-primary {
  background: rgba(99, 102, 241, 0.1);
  color: #6366f1;
}

.tag-success {
  background: rgba(34, 197, 94, 0.1);
  color: #22c55e;
}

.tag-warning {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.tag-danger {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.tag-info {
  background: rgba(100, 116, 139, 0.1);
  color: #64748b;
}

.topic-desc {
  font-size: 14px;
  color: #64748b;
  line-height: 1.7;
  margin: 0 0 16px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  flex: 1;
}

.topic-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
  margin-top: auto;
}

.meta-left {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #94a3b8;
}

.author-name {
  font-weight: 600;
  color: #6366f1;
  cursor: pointer;
  transition: all 0.2s;
}

.author-name:hover {
  color: #4f46e5;
  text-decoration: underline;
}

.meta-dot {
  color: #cbd5e1;
  font-size: 10px;
}

.competition-badge {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 4px 12px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08) 0%, rgba(139, 92, 246, 0.08) 100%);
  border-radius: 100px;
  font-size: 12px;
  color: #6366f1;
  font-weight: 500;
  transition: all 0.2s;
}

.competition-badge:hover {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15) 0%, rgba(139, 92, 246, 0.15) 100%);
}

.competition-badge i {
  font-size: 10px;
}

.meta-right {
  display: flex;
  align-items: center;
  gap: 18px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #94a3b8;
  transition: all 0.2s;
  cursor: pointer;
}

.meta-item:hover {
  color: #6366f1;
}

.meta-item i {
  font-size: 14px;
  transition: transform 0.2s;
}

.meta-item:hover i {
  transform: scale(1.1);
}

/* ==================== 侧边栏 - 优化设计 ==================== */
.content-sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.sidebar-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
}

.sidebar-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.card-header i {
  font-size: 20px;
  color: #6366f1;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
  border-radius: 10px;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

/* 热门话题 */
.hot-topics {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hot-topic-item {
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  padding: 12px;
  border-radius: 12px;
  transition: all 0.25s;
}

.hot-topic-item:hover {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  transform: translateX(4px);
}

.hot-rank {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 800;
  color: #64748b;
  flex-shrink: 0;
  transition: all 0.2s;
}

.hot-topic-item:hover .hot-rank {
  transform: scale(1.1);
}

.hot-rank.top {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.hot-topic-info {
  flex: 1;
  min-width: 0;
}

.hot-topic-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}

.hot-topic-item:hover .hot-topic-title {
  color: #6366f1;
}

.hot-topic-heat {
  font-size: 12px;
  color: #f59e0b;
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: 500;
}

.hot-topic-heat i {
  font-size: 11px;
}

/* 推荐竞赛 */
.competition-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.competition-item {
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  padding: 12px;
  border-radius: 12px;
  transition: all 0.25s;
}

.competition-item:hover {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  transform: translateX(4px);
}

.comp-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.competition-item:hover .comp-icon {
  transform: scale(1.1) rotate(5deg);
}

.comp-icon i {
  font-size: 20px;
  color: white;
}

.comp-info {
  flex: 1;
  min-width: 0;
}

.comp-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}

.competition-item:hover .comp-name {
  color: #6366f1;
}

.comp-count {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
}

/* 社区公告 */
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px;
  border-radius: 10px;
  transition: all 0.2s;
  cursor: pointer;
}

.notice-item:hover {
  background: #f8fafc;
}

.notice-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  margin-top: 6px;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(99, 102, 241, 0.3);
}

.notice-text {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
  margin: 0;
  transition: color 0.2s;
}

.notice-item:hover .notice-text {
  color: #475569;
}

/* ==================== 空状态 ==================== */
.discussion-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  background: white;
  border-radius: 16px;
  border: 1px solid #f1f5f9;
}

.empty-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.empty-icon i {
  font-size: 32px;
  color: #94a3b8;
}

.empty-text {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.empty-subtext {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 24px 0;
}

.empty-btn {
  margin-top: 8px;
}

/* ==================== 加载更多 ==================== */
.load-more {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

.btn-load-more {
  padding: 12px 32px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-load-more:hover {
  border-color: #6366f1;
  color: #6366f1;
  background: rgba(99, 102, 241, 0.05);
}

/* ==================== 对话框样式 ==================== */
:deep(.topic-dialog .el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}

:deep(.topic-dialog .el-dialog__title) {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

:deep(.topic-dialog .el-dialog__body) {
  padding: 24px;
}

:deep(.topic-dialog .el-form-item__label) {
  font-weight: 600;
  color: #374151;
}

:deep(.topic-dialog .el-input__inner),
:deep(.topic-dialog .el-textarea__inner) {
  border-radius: 8px;
  border-color: #e2e8f0;
}

:deep(.topic-dialog .el-input__inner:focus),
:deep(.topic-dialog .el-textarea__inner:focus) {
  border-color: #6366f1;
}

:deep(.topic-dialog .el-select .el-input__inner) {
  border-radius: 8px;
}

:deep(.topic-dialog .dialog-footer) {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.topic-dialog .el-button) {
  border-radius: 8px;
  padding: 10px 24px;
}

:deep(.topic-dialog .el-button--primary) {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
}

/* ==================== 响应式 ==================== */
@media (max-width: 1024px) {
  .content-layout {
    grid-template-columns: 1fr;
  }
  
  .content-sidebar {
    display: none;
  }
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .toolbar-left {
    flex-direction: column;
    align-items: stretch;
  }
  
  .competition-select {
    width: 100%;
  }
  
  .tag-filter {
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 8px;
  }
  
  .banner-stats {
    gap: 24px;
  }
  
  .banner-title {
    font-size: 28px;
  }
}
</style>
