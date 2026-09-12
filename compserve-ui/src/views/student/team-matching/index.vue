<template>
  <div class="student-page">
    <StudentNavbar />

    <!-- 主内容区域 -->
    <div class="content-container">
      <div class="content-box">
        <!-- 页面标题 -->
        <div class="page-header">
          <h1 class="page-title">智能组队匹配</h1>
          <p class="page-subtitle">基于AI算法，为您推荐最合适的队友</p>
        </div>

        <!-- 学生信息完善卡片 -->
        <section class="section-card">
          <div class="section-header">
            <div class="section-icon">
              <i class="fas fa-user-circle"></i>
            </div>
            <h2 class="section-title">完善个人信息</h2>
            <span class="section-tip">完善信息可获得更精准的匹配推荐</span>
          </div>
          
          <div class="info-form">
            <div class="form-row">
              <div class="form-item">
                <label class="form-label">姓名</label>
                <el-input v-model="studentInfo.name" placeholder="请输入姓名" clearable />
              </div>
              <div class="form-item">
                <label class="form-label">学校</label>
                <el-input v-model="studentInfo.school" placeholder="请输入学校" clearable />
              </div>
              <div class="form-item">
                <label class="form-label">专业</label>
                <el-input v-model="studentInfo.major" placeholder="请输入专业" clearable />
              </div>
              <div class="form-item">
                <label class="form-label">擅长技能</label>
                <el-input v-model="studentInfo.skills" placeholder="技能1, 技能2, 技能3" clearable />
              </div>
            </div>
            
            <div class="form-item full-width">
              <label class="form-label">参赛经历</label>
              <el-input 
                v-model="studentInfo.experience" 
                type="textarea" 
                :rows="3"
                placeholder="请描述您的参赛经历和获奖情况"
              />
            </div>
            
            <div class="form-actions">
              <el-button type="primary" @click="saveStudentInfo" :icon="Check">
                保存信息
              </el-button>
            </div>
          </div>
        </section>

        <!-- 已加入队伍 -->
        <section class="section-card">
          <div class="section-header">
            <div class="section-icon">
              <i class="fas fa-users"></i>
            </div>
            <h2 class="section-title">已加入队伍</h2>
            <span class="section-count">共 {{ joinedTeams.length }} 支队伍</span>
          </div>
          
          <div v-if="joinedTeams.length === 0" class="empty-state">
            <div class="empty-icon">
              <i class="fas fa-users"></i>
            </div>
            <p class="empty-text">暂无加入的队伍</p>
            <el-button type="primary" plain @click="showCreateTeamModal = true">
              <i class="fas fa-plus mr-1"></i> 创建队伍
            </el-button>
          </div>
          
          <div v-else class="teams-grid">
            <div 
              v-for="team in joinedTeams" 
              :key="team.id"
              class="team-card"
              @click="viewTeamDetail(team)"
            >
              <div class="team-header">
                <h3 class="team-name">{{ team.name }}</h3>
                <el-tag :type="getTeamStatusType(team.status)" size="small">{{ team.status }}</el-tag>
              </div>
              <div class="team-body">
                <p class="team-info">
                  <i class="fas fa-trophy"></i> {{ team.targetCompetition }}
                </p>
                <p class="team-info">
                  <i class="fas fa-users"></i> {{ team.memberCount }}人
                </p>
                <p class="team-info">
                  <i class="fas fa-clock"></i> {{ team.createTime }}
                </p>
              </div>
            </div>
          </div>
        </section>

        <!-- 组队需求发布 -->
        <section class="section-card">
          <div class="section-header">
            <div class="section-icon">
              <i class="fas fa-bullhorn"></i>
            </div>
            <h2 class="section-title">发布组队需求</h2>
          </div>
          
          <div class="info-form">
            <div class="form-row">
              <div class="form-item">
                <label class="form-label">期望队友专业</label>
                <el-input v-model="teamRequirement.major" placeholder="如：计算机科学、软件工程" clearable />
              </div>
              <div class="form-item">
                <label class="form-label">技能要求</label>
                <el-input v-model="teamRequirement.skills" placeholder="如：Java开发、UI设计" clearable />
              </div>
              <div class="form-item">
                <label class="form-label">参赛目标</label>
                <el-select v-model="teamRequirement.target" placeholder="请选择参赛目标" style="width: 100%">
                  <el-option label="挑战杯" value="挑战杯" />
                  <el-option label="中国国际大学生创新竞赛" value="中国国际大学生创新竞赛" />
                  <el-option label="电子设计竞赛" value="电子设计竞赛" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-item">
                <label class="form-label">截止时间</label>
                <el-date-picker 
                  v-model="teamRequirement.deadline" 
                  type="date" 
                  placeholder="选择截止时间"
                  style="width: 100%"
                />
              </div>
              <div class="form-item">
                <label class="form-label">队伍人数</label>
                <el-input-number 
                  v-model="teamRequirement.maxMembers" 
                  :min="2" 
                  :max="10"
                  style="width: 100%"
                />
              </div>
            </div>
            
            <div class="form-item full-width">
              <label class="form-label">补充说明</label>
              <el-input 
                v-model="teamRequirement.description" 
                type="textarea" 
                :rows="2"
                placeholder="其他要求或说明..."
              />
            </div>
            
            <div class="form-actions">
              <el-button type="primary" @click="publishRequirement" :icon="Promotion">
                发布需求
              </el-button>
            </div>
          </div>
        </section>

        <!-- 智能匹配推荐 -->
        <section class="section-card">
          <div class="section-header">
            <div class="section-icon">
              <i class="fas fa-magic"></i>
            </div>
            <h2 class="section-title">智能匹配推荐</h2>
            <el-button type="primary" link @click="refreshRecommendations" :icon="Refresh">
              刷新推荐
            </el-button>
          </div>
          
          <div v-if="recommendations.length === 0" class="empty-state">
            <div class="empty-icon">
              <i class="fas fa-search"></i>
            </div>
            <p class="empty-text">暂无匹配的队友推荐</p>
            <p class="empty-tip">完善个人信息后可获得更精准的推荐</p>
          </div>
          
          <div v-else class="recommendations-grid">
            <div 
              v-for="recommendation in recommendations" 
              :key="recommendation.id"
              class="recommendation-card"
            >
              <div class="recommendation-header">
                <div class="student-avatar">
                  <i class="fas fa-user"></i>
                </div>
                <div class="student-info">
                  <h3 class="student-name">{{ recommendation.name }}</h3>
                  <p class="student-major">{{ recommendation.major }}</p>
                </div>
                <div class="match-score">
                  <span class="score-number">{{ recommendation.matchScore }}</span>
                  <span class="score-label">分</span>
                </div>
              </div>
              
              <div class="recommendation-body">
                <div class="skills-container">
                  <el-tag 
                    v-for="skill in recommendation.skills" 
                    :key="skill"
                    size="small"
                    type="info"
                    effect="plain"
                  >
                    {{ skill }}
                  </el-tag>
                </div>
                
                <p class="student-introduction">{{ recommendation.introduction }}</p>
                
                <div class="recommendation-footer">
                  <span class="target-competition">
                    <i class="fas fa-trophy"></i> {{ recommendation.targetCompetition }}
                  </span>
                  <el-button type="primary" size="small" @click="sendTeamRequest(recommendation)">
                    <i class="fas fa-user-plus mr-1"></i> 发送申请
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- 组队申请处理 -->
        <section class="section-card">
          <div class="section-header">
            <div class="section-icon">
              <i class="fas fa-inbox"></i>
            </div>
            <h2 class="section-title">组队申请</h2>
            <span class="section-count">{{ pendingApplications.length }} 条待处理</span>
          </div>
          
          <div v-if="pendingApplications.length === 0" class="empty-state">
            <div class="empty-icon">
              <i class="fas fa-inbox"></i>
            </div>
            <p class="empty-text">暂无组队申请</p>
          </div>
          
          <div v-else class="applications-list">
            <div 
              v-for="application in pendingApplications" 
              :key="application.id"
              class="application-item"
            >
              <div class="application-header">
                <div class="applicant-avatar">
                  <i class="fas fa-user"></i>
                </div>
                <div class="applicant-info">
                  <h3 class="applicant-name">{{ application.studentName }}</h3>
                  <p class="applicant-details">{{ application.major }} | {{ application.skills }}</p>
                </div>
                <div class="application-actions">
                  <el-button type="success" size="small" @click="handleApplication(application, 'accept')">
                    <i class="fas fa-check mr-1"></i> 同意
                  </el-button>
                  <el-button type="info" size="small" @click="handleApplication(application, 'reject')">
                    <i class="fas fa-times mr-1"></i> 拒绝
                  </el-button>
                </div>
              </div>
              <div class="application-reason">
                <span class="reason-label">申请理由：</span>
                <span class="reason-text">{{ application.reason }}</span>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>

    <!-- 队伍详情弹窗 -->
    <el-dialog v-model="showTeamDetailModal" title="队伍详情" width="500px" :close-on-click-modal="false">
      <div v-if="selectedTeam">
        <div class="team-detail-header">
          <h3 class="team-detail-name">{{ selectedTeam.name }}</h3>
          <el-tag :type="getTeamStatusType(selectedTeam.status)" size="small">{{ selectedTeam.status }}</el-tag>
        </div>
        <div class="team-detail-info">
          <p class="detail-item">
            <i class="fas fa-trophy"></i> 目标赛事：{{ selectedTeam.targetCompetition }}
          </p>
          <p class="detail-item">
            <i class="fas fa-users"></i> 成员人数：{{ selectedTeam.memberCount }}人
          </p>
          <p class="detail-item">
            <i class="fas fa-clock"></i> 组队时间：{{ selectedTeam.createTime }}
          </p>
        </div>
        
        <div class="team-detail-members">
          <h4 class="members-title">队伍成员</h4>
          <div class="members-list">
            <div 
              v-for="(member, index) in selectedTeam.members" 
              :key="index"
              class="member-item"
            >
              <div class="member-avatar">
                <i class="fas fa-user"></i>
              </div>
              <div class="member-info">
                <p class="member-name">{{ member.name }}</p>
                <p class="member-major">{{ member.major }}</p>
              </div>
              <el-tag v-if="member.isLeader" type="primary" size="small">队长</el-tag>
            </div>
          </div>
        </div>
        
        <div class="team-detail-description">
          <h4 class="description-title">队伍简介</h4>
          <p class="description-text">{{ selectedTeam.description }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="closeTeamDetailModal">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 创建队伍弹窗 -->
    <el-dialog v-model="showCreateTeamModal" title="创建队伍" width="500px" :close-on-click-modal="false">
      <el-form :model="newTeam" label-width="100px">
        <el-form-item label="队伍名称">
          <el-input v-model="newTeam.name" placeholder="请输入队伍名称" />
        </el-form-item>
        <el-form-item label="目标赛事">
          <el-select v-model="newTeam.targetCompetition" placeholder="请选择目标赛事" style="width: 100%">
            <el-option label="挑战杯" value="挑战杯" />
            <el-option label="中国国际大学生创新竞赛" value="中国国际大学生创新竞赛" />
            <el-option label="电子设计竞赛" value="电子设计竞赛" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="队伍人数">
          <el-input-number v-model="newTeam.maxMembers" :min="2" :max="10" />
        </el-form-item>
        <el-form-item label="队伍简介">
          <el-input v-model="newTeam.description" type="textarea" :rows="3" placeholder="请描述队伍的目标和愿景..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeCreateTeamModal">取消</el-button>
        <el-button type="primary" @click="createTeam">创建队伍</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TeamMatching">
import StudentNavbar from "@/components/StudentNavbar.vue";
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Promotion, Refresh } from '@element-plus/icons-vue'
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()

// 学生信息
const studentInfo = reactive({
  name: '',
  school: '',
  major: '',
  skills: '',
  experience: ''
})

// 组队需求
const teamRequirement = reactive({
  major: '',
  skills: '',
  target: '',
  deadline: '',
  maxMembers: 4,
  description: ''
})

// 新队伍信息
const newTeam = reactive({
  name: '',
  targetCompetition: '',
  description: '',
  maxMembers: 4
})

// 弹窗状态
const showTeamDetailModal = ref(false)
const showCreateTeamModal = ref(false)
const selectedTeam = ref(null)

// 已加入的队伍
const joinedTeams = ref([
  {
    id: 1,
    name: '创新先锋队',
    targetCompetition: '挑战杯',
    memberCount: 4,
    createTime: '2026-03-15',
    status: '已报名',
    description: '致力于科技创新，追求卓越',
    members: [
      { name: '张三', major: '计算机科学', isLeader: true },
      { name: '李四', major: '软件工程', isLeader: false },
      { name: '王五', major: '电子信息', isLeader: false },
      { name: '赵六', major: '数据科学', isLeader: false }
    ]
  },
  {
    id: 2,
    name: 'AI梦之队',
    targetCompetition: '中国国际大学生创新竞赛',
    memberCount: 3,
    createTime: '2026-04-01',
    status: '招募中',
    description: '专注人工智能领域创新',
    members: [
      { name: '张三', major: '计算机科学', isLeader: true },
      { name: '钱七', major: '人工智能', isLeader: false },
      { name: '孙八', major: '自动化', isLeader: false }
    ]
  }
])

// 智能匹配推荐
const recommendations = ref([
  {
    id: 1,
    name: '李明',
    major: '软件工程',
    skills: ['Java', 'Spring Boot', 'MySQL'],
    introduction: '有丰富的后端开发经验，曾参与多个企业级项目开发，擅长系统架构设计。',
    targetCompetition: '挑战杯',
    matchScore: 85
  },
  {
    id: 2,
    name: '小红',
    major: '视觉传达设计',
    skills: ['UI设计', 'Figma', 'Photoshop'],
    introduction: '专注用户体验设计，设计作品曾获多项设计奖项，擅长品牌视觉设计。',
    targetCompetition: '挑战杯',
    matchScore: 78
  },
  {
    id: 3,
    name: '王强',
    major: '数据科学',
    skills: ['Python', '机器学习', '数据分析'],
    introduction: '数据挖掘与机器学习方向，发表多篇论文，擅长数据分析和模型构建。',
    targetCompetition: '中国国际大学生创新竞赛',
    matchScore: 82
  },
  {
    id: 4,
    name: '陈静',
    major: '电子信息工程',
    skills: ['硬件开发', '嵌入式系统', 'C语言'],
    introduction: '硬件开发经验丰富，参与多个智能硬件项目，擅长嵌入式系统开发。',
    targetCompetition: '电子设计竞赛',
    matchScore: 75
  },
  {
    id: 5,
    name: '刘洋',
    major: '计算机科学',
    skills: ['前端开发', 'Vue.js', 'React'],
    introduction: '全栈开发工程师，前后端技术扎实，有丰富的项目经验。',
    targetCompetition: '挑战杯',
    matchScore: 80
  },
  {
    id: 6,
    name: '赵敏',
    major: '市场营销',
    skills: ['市场分析', '文案策划', '活动组织'],
    introduction: '擅长市场调研和活动策划，曾成功组织多次大型活动。',
    targetCompetition: '挑战杯',
    matchScore: 73
  }
])

// 待处理的组队申请
const pendingApplications = ref([
  {
    id: 1,
    studentName: '周杰',
    major: '计算机科学',
    skills: 'Python, 机器学习',
    reason: '我对AI领域非常感兴趣，希望能加入贵队一起参加比赛，共同进步。'
  },
  {
    id: 2,
    studentName: '吴芳',
    major: '软件工程',
    skills: '前端开发, Vue.js',
    reason: '看到你们的招募信息，我觉得我的技能可以为团队做出贡献。'
  }
])

// 获取队伍状态类型
const getTeamStatusType = (status) => {
  const statusMap = {
    '已报名': 'success',
    '招募中': 'warning',
    '已结束': 'info'
  }
  return statusMap[status] || 'info'
}

// 保存学生信息
const saveStudentInfo = () => {
  ElMessage.success('个人信息保存成功！')
}

// 查看队伍详情
const viewTeamDetail = (team) => {
  selectedTeam.value = team
  showTeamDetailModal.value = true
}

// 关闭队伍详情弹窗
const closeTeamDetailModal = () => {
  showTeamDetailModal.value = false
  selectedTeam.value = null
}

// 关闭创建队伍弹窗
const closeCreateTeamModal = () => {
  showCreateTeamModal.value = false
  newTeam.name = ''
  newTeam.targetCompetition = ''
  newTeam.description = ''
  newTeam.maxMembers = 4
}

// 创建队伍
const createTeam = () => {
  if (!newTeam.name || !newTeam.targetCompetition) {
    ElMessage.warning('请填写队伍名称和目标赛事')
    return
  }
  
  const team = {
    id: Date.now(),
    name: newTeam.name,
    targetCompetition: newTeam.targetCompetition,
    description: newTeam.description,
    memberCount: 1,
    createTime: new Date().toISOString().split('T')[0],
    status: '招募中',
    members: [{ name: studentInfo.name || '我', major: studentInfo.major || '未填写', isLeader: true }]
  }
  
  joinedTeams.value.push(team)
  closeCreateTeamModal()
  ElMessage.success('队伍创建成功！')
}

// 发布组队需求
const publishRequirement = () => {
  if (!teamRequirement.major || !teamRequirement.target) {
    ElMessage.warning('请填写期望专业和参赛目标')
    return
  }
  ElMessage.success('组队需求发布成功！')
  teamRequirement.major = ''
  teamRequirement.skills = ''
  teamRequirement.target = ''
  teamRequirement.deadline = ''
  teamRequirement.maxMembers = 4
  teamRequirement.description = ''
}

// 刷新推荐
const refreshRecommendations = () => {
  ElMessage.info('正在刷新推荐列表...')
}

// 发送组队申请
const sendTeamRequest = (student) => {
  ElMessage.success(`已向 ${student.name} 发送组队申请！`)
}

// 处理组队申请
const handleApplication = (application, action) => {
  const index = pendingApplications.value.findIndex(item => item.id === application.id)
  if (index > -1) {
    pendingApplications.value.splice(index, 1)
    if (action === 'accept') {
      ElMessage.success(`已同意 ${application.studentName} 的申请`)
    } else {
      ElMessage.info(`已拒绝 ${application.studentName} 的申请`)
    }
  }
}

// 处理用户命令
const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessage.confirm('确定注销并退出系统吗？', '提示', {
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
  // 初始化数据
})
</script>

<style scoped>
/* 基础样式 */
.student-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

/* 顶部导航栏 */
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
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.3);
}

/* 内容容器 */
.content-container {
  flex: 1;
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  width: 100%;
}

.content-box {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 24px;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 2px solid #f0f0f0;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* 区块卡片 */
.section-card {
  background: #f9fafb;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

.section-card:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.section-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  flex: 1;
}

.section-tip {
  font-size: 12px;
  color: #9ca3af;
}

.section-count {
  font-size: 12px;
  color: #6b7280;
  background: #e5e7eb;
  padding: 4px 12px;
  border-radius: 12px;
}

/* 表单样式 */
.info-form {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item.full-width {
  grid-column: 1 / -1;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 48px 24px;
}

.empty-icon {
  width: 80px;
  height: 80px;
  background: #f3f4f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  color: #9ca3af;
  font-size: 32px;
}

.empty-text {
  font-size: 16px;
  color: #6b7280;
  margin: 0 0 8px 0;
}

.empty-tip {
  font-size: 14px;
  color: #9ca3af;
  margin: 0;
}

/* 队伍网格 */
.teams-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.team-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e5e7eb;
}

.team-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  border-color: #8b5cf6;
}

.team-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.team-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.team-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.team-info {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.team-info i {
  color: #8b5cf6;
}

/* 推荐网格 */
.recommendations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.recommendation-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s;
}

.recommendation-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  border-color: #8b5cf6;
}

.recommendation-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.student-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e0e7ff, #c7d2fe);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6366f1;
  font-size: 20px;
}

.student-info {
  flex: 1;
}

.student-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.student-major {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

.match-score {
  text-align: center;
}

.score-number {
  font-size: 24px;
  font-weight: 700;
  color: #8b5cf6;
}

.score-label {
  font-size: 12px;
  color: #6b7280;
}

.recommendation-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skills-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.student-introduction {
  font-size: 13px;
  color: #4b5563;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.recommendation-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.target-competition {
  font-size: 12px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 4px;
}

.target-competition i {
  color: #8b5cf6;
}

/* 申请列表 */
.applications-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.application-item {
  background: white;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e5e7eb;
}

.application-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.applicant-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e0e7ff, #c7d2fe);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6366f1;
  font-size: 20px;
}

.applicant-info {
  flex: 1;
}

.applicant-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.applicant-details {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

.application-actions {
  display: flex;
  gap: 8px;
}

.application-reason {
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px;
  font-size: 13px;
  color: #4b5563;
  line-height: 1.6;
}

.reason-label {
  font-weight: 500;
  color: #374151;
}

.reason-text {
  color: #6b7280;
}

/* 弹窗样式 */
.team-detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.team-detail-name {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.team-detail-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

.detail-item {
  font-size: 14px;
  color: #4b5563;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-item i {
  color: #8b5cf6;
}

.team-detail-members {
  margin-bottom: 20px;
}

.members-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.members-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.member-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e0e7ff, #c7d2fe);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6366f1;
  font-size: 16px;
}

.member-info {
  flex: 1;
}

.member-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.member-major {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}

.team-detail-description {
  background: #f9fafb;
  border-radius: 8px;
  padding: 16px;
}

.description-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.description-text {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.6;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .form-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .nav-container {
    padding: 0 16px;
    gap: 16px;
  }
  
  .main-nav {
    gap: 16px;
  }
  
  .content-container {
    padding: 16px;
  }
  
  .content-box {
    padding: 16px;
  }
  
  .section-card {
    padding: 16px;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .teams-grid,
  .recommendations-grid {
    grid-template-columns: 1fr;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .section-header {
    flex-wrap: wrap;
  }
}
</style>