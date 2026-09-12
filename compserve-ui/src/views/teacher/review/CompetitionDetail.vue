<template>
  <div class="review-detail-page">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="$router.push({ path: '/teacher', query: { menu: 'review' } })">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <span class="page-title">{{ competition?.competitionName || '竞赛详情' }}</span>
      </div>
    </div>

    <!-- Info & Stats -->
    <div class="info-section">
        <div class="info-group">
            <div class="info-item">
                <span class="info-label">作业发布</span>
                <span class="info-value">{{ formatDate(competition?.registerStartTime) }}</span>
            </div>
            <div class="info-divider"></div>
            <div class="info-item">
                <span class="info-label">作业提交截止时间</span>
                <span class="info-value highlight">{{ formatDate(competition?.registerEndTime) }}</span>
            </div>
        </div>
        
        <div class="stats-group">
            <div class="stat-box review-progress">
                <div class="stat-circle success">
                    <span class="stat-percent">{{ reviewRate }}%</span>
                </div>
                <div class="stat-desc">
                    <div class="stat-title">评审进度</div>
                    <div class="stat-detail">已批阅 {{ reviewedCount }} / 总数 {{ totalCount }}</div>
                </div>
            </div>
        </div>
    </div>

    <!-- Team List Table -->
    <div class="table-container">
        <div class="table-header">
            <div class="tabs">
                <div 
                    v-for="tab in tabs" 
                    :key="tab.value"
                    class="tab" 
                    :class="{ active: currentTab === tab.value }"
                    @click="currentTab = tab.value"
                >
                    {{ tab.label }}
                    <span class="tab-count" :class="{ active: currentTab === tab.value }">({{ tab.count }})</span>
                </div>
            </div>
            <div class="search-box">
                <el-input 
                    v-model="searchKeyword" 
                    placeholder="搜索队伍名称/学生姓名" 
                    :prefix-icon="Search" 
                    style="width: 250px"
                    clearable
                />
            </div>
        </div>
        
        <el-table 
            :data="filteredTeams" 
            style="width: 100%" 
            v-loading="loading"
            :header-cell-style="{ background: '#f9fafc', color: '#606266', fontWeight: '500' }"
        >
            <el-table-column prop="teamName" label="队伍名称" min-width="150">
                <template #default="{ row }">
                     <div class="student-info">
                        <span class="student-name">{{ row.teamName }}</span>
                     </div>
                </template>
            </el-table-column>
            <el-table-column prop="memberCount" label="队伍人数" width="80" align="left">
                <template #default="{ row }">
                    <div class="member-count-wrapper">
                        <span class="member-count">{{ row.memberCount }}</span>
                        <el-popover
                            v-if="row.teamMembers && getMemberList(row).length > 0"
                            placement="top"
                            trigger="click"
                            width="280"
                        >
                            <template #reference>
                                <el-button size="mini" type="text" class="member-btn">查看队员</el-button>
                            </template>
                            <div class="member-popover">
                                <div class="popover-title">队员列表</div>
                                <div class="member-items">
                                    <div 
                                        v-for="(member, idx) in getMemberList(row)" 
                                        :key="idx" 
                                        class="member-item"
                                    >
                                        <span class="member-name">{{ member.name || member.userName || member.studentNo }}</span>
                                        <span v-if="member.major" class="member-major">{{ member.major }}</span>
                                    </div>
                                </div>
                            </div>
                        </el-popover>
                    </div>
                </template>
            </el-table-column>
            
            <el-table-column prop="submitTime" label="提交时间" min-width="160">
                <template #default="{ row }">
                    <span v-if="row.submitTime" class="time-text">{{ formatDate(row.submitTime) }}</span>
                    <span v-else class="text-gray">未提交</span>
                </template>
            </el-table-column>
            
            <el-table-column prop="score" label="成绩" width="120" align="center" sortable>
                <template #default="{ row }">
                    <span v-if="row.score != null" class="score-text">{{ row.score }}</span>
                    <span v-else>-</span>
                </template>
            </el-table-column>
            
            <el-table-column prop="reviewed" label="状态" width="100" align="center">
                <template #default="{ row }">
                    <span :class="getStatusClass(row)">{{ getStatusText(row) }}</span>
                </template>
            </el-table-column>
            
            <el-table-column label="操作" width="120" fixed="right" align="center">
                <template #default="{ row }">
                    <div class="action-links">
                        <span class="action-link primary" @click="handleReview(row)">
                            {{ row.reviewed ? '查看' : '批阅' }}
                        </span>
                        <!-- 暂时隐藏删除，因为需要 scoreId
                        <span v-if="row.reviewed" class="action-link danger">删除</span>
                        -->
                    </div>
                </template>
            </el-table-column>
        </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Search } from '@element-plus/icons-vue'
import { getTeacherCompetitionDetail, getReviewTeams } from '@/api/teacher'

const route = useRoute()
const router = useRouter()
const competitionId = route.params.competitionId

const competition = ref(null)
const teams = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const currentTab = ref('all')

// 计算属性
const totalCount = computed(() => teams.value.length)
const submittedCount = computed(() => teams.value.filter(t => t.submitTime).length)
const reviewedCount = computed(() => teams.value.filter(t => t.reviewed).length)

const submitRate = computed(() => totalCount.value ? Math.round(submittedCount.value / totalCount.value * 100) : 0)
const reviewRate = computed(() => totalCount.value ? Math.round(reviewedCount.value / totalCount.value * 100) : 0)

const tabs = computed(() => [
    { label: '全部', value: 'all', count: totalCount.value },
    { label: '未批阅', value: 'unreviewed', count: totalCount.value - reviewedCount.value },
    { label: '已批阅', value: 'reviewed', count: reviewedCount.value }
])

const filteredTeams = computed(() => {
    let result = teams.value

    // Filter by tab
    if (currentTab.value === 'unreviewed') {
        result = result.filter(t => !t.reviewed)
    } else if (currentTab.value === 'reviewed') {
        result = result.filter(t => t.reviewed)
    }

    // Filter by keyword
    if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        result = result.filter(t => 
            t.teamName.toLowerCase().includes(keyword) || 
            (t.teamMembers && t.teamMembers.toLowerCase().includes(keyword))
        )
    }
    return result
})

const getStatusText = (row) => {
    if (!row.submitTime) return '未提交'
    if (row.reviewed) return '已批阅'
    return '待批阅'
}

const getStatusClass = (row) => {
    if (!row.submitTime) return 'status-text gray'
    if (row.reviewed) return 'status-text success'
    return 'status-text warning'
}

const getMemberList = (row) => {
    if (!row.teamMembers) return []
    try {
        const members = typeof row.teamMembers === 'string' 
            ? JSON.parse(row.teamMembers) 
            : row.teamMembers
        return Array.isArray(members) ? members : []
    } catch (e) {
        return []
    }
}

const formatDate = (str) => {
    if (!str) return '-'
    const date = new Date(str)
    return date.toLocaleString('zh-CN', { 
        year: 'numeric', 
        month: '2-digit', 
        day: '2-digit', 
        hour: '2-digit', 
        minute: '2-digit' 
    }).replace(/\//g, '-')
}

const loadData = async () => {
    loading.value = true
    try {
        console.log('[TeacherReviewDetail] loadData:start', {
            competitionId,
            autoStart: route.query.autoStart
        })
        const compRes = await getTeacherCompetitionDetail(competitionId)
        competition.value = compRes.data
        
        const teamRes = await getReviewTeams(competitionId)
        teams.value = sortTeams(teamRes.data || [])
        console.log('[TeacherReviewDetail] teams:loaded', teams.value.map(team => ({
            registerId: team.registerId,
            participationId: team.participationId,
            teamId: team.teamId,
            teamName: team.teamName,
            reviewed: team.reviewed,
            submitTime: team.submitTime,
            registerTime: team.registerTime
        })))
        if (route.query.autoStart === '1') {
            autoStartReview()
        }
    } catch (e) {
        console.error('[TeacherReviewDetail] loadData:error', e)
    } finally {
        loading.value = false
    }
}

const sortTeams = (list) => {
    return list.slice().sort((a, b) => {
        const timeA = a.registerTime ? new Date(a.registerTime).getTime() : Number.MAX_SAFE_INTEGER
        const timeB = b.registerTime ? new Date(b.registerTime).getTime() : Number.MAX_SAFE_INTEGER
        if (timeA !== timeB) {
            return timeA - timeB
        }
        return (a.registerId || 0) - (b.registerId || 0)
    })
}

const autoStartReview = () => {
    const target = teams.value.find(team => !team.reviewed) || teams.value[0]
    console.log('[TeacherReviewDetail] autoStartReview:selected', target ? {
        registerId: target.registerId,
        participationId: target.participationId,
        teamId: target.teamId,
        teamName: target.teamName,
        reviewed: target.reviewed,
        submitTime: target.submitTime,
        registerTime: target.registerTime
    } : null)
    if (!target?.registerId) {
        console.warn('[TeacherReviewDetail] autoStartReview:no-registerId', target)
        return
    }
    handleReview(target)
}

const handleReview = (team) => {
    console.log('[TeacherReviewDetail] handleReview:navigate', {
        competitionId,
        registerId: team?.registerId,
        participationId: team?.participationId,
        teamId: team?.teamId,
        teamName: team?.teamName,
        targetPath: `/teacher/review/${competitionId}/${team?.registerId}`
    })
    router.push(`/teacher/review/${competitionId}/${team.registerId}`)
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
.review-detail-page {
    padding: 20px;
    background: #f5f7fa;
    min-height: 100vh;
}
.page-header {
    margin-bottom: 20px;
    background: #fff;
    padding: 15px 20px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
.header-left {
    display: flex;
    align-items: center;
    gap: 10px;
}
.page-title {
    font-size: 18px;
    font-weight: bold;
    color: #303133;
}
.info-section {
    background: #fff;
    padding: 24px;
    border-radius: 8px;
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
.info-group {
    display: flex;
    align-items: center;
    gap: 40px;
}
.info-item {
    display: flex;
    flex-direction: column;
    gap: 8px;
}
.info-label {
    color: #909399;
    font-size: 14px;
}
.info-value {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
}
.info-value.highlight {
    color: #409eff;
}
.info-divider {
    width: 1px;
    height: 40px;
    background-color: #e4e7ed;
    margin: 0 20px;
}
.stats-group {
    display: flex;
    gap: 40px;
}
.stat-box {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px 32px;
    background: #f9fafc;
    border-radius: 8px;
    border: 1px solid #ebeef5;
    min-width: 280px;
}
.stat-box.review-progress {
    background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
    border: 1px solid #c2e7b0;
}
.stat-circle {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    font-size: 14px;
}
.stat-circle.pending {
    background: #fdf6ec;
    color: #e6a23c;
    border: 2px solid #faecd8;
}
.stat-circle.success {
    background: #f0f9eb;
    color: #67c23a;
    border: 2px solid #e1f3d8;
}
.stat-desc {
    display: flex;
    flex-direction: column;
    gap: 4px;
}
.stat-title {
    font-size: 16px;
    font-weight: bold;
    color: #303133;
}
.stat-detail {
    font-size: 12px;
    color: #909399;
}
.table-container {
    background: #fff;
    padding: 24px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
.table-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 24px;
    align-items: center;
}
.tabs {
    display: flex;
    gap: 32px;
    background: transparent;
    padding: 0;
    border-radius: 0;
}
.tab {
    cursor: pointer;
    padding: 8px 0;
    color: #606266;
    font-size: 14px;
    border-radius: 0;
    transition: all 0.3s;
    position: relative;
    display: flex;
    align-items: center;
    gap: 4px;
}
.tab:hover {
    color: #409eff;
    background: transparent;
}
.tab.active {
    color: #409eff;
    font-weight: 500;
    background: transparent;
    box-shadow: none;
}
.tab.active::after {
    content: '';
    position: absolute;
    bottom: -1px;
    left: 0;
    width: 100%;
    height: 2px;
    background-color: #409eff;
    border-radius: 2px;
}
.tab-count {
    color: #909399;
    font-size: 12px;
    font-weight: normal;
}
.tab-count.active {
    color: #409eff;
}
.student-info {
    display: flex;
    align-items: center;
}
.student-name {
    font-weight: 500;
    color: #303133;
}
.score-text {
    font-weight: bold;
    color: #303133;
    font-size: 14px;
}
.status-text {
    font-size: 13px;
    padding: 4px 12px;
    border-radius: 12px;
    display: inline-block;
}
.status-text.gray {
    color: #909399;
    background: #f4f4f5;
    border: 1px solid #e9e9eb;
}
.status-text.warning {
    color: #e6a23c;
    background: #fdf6ec;
    border: 1px solid #faecd8;
}
.status-text.success {
    color: #67c23a;
    background: #f0f9eb;
    border: 1px solid #e1f3d8;
}
.text-gray {
    color: #c0c4cc;
}
.time-text {
    color: #606266;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}
.action-links {
    display: flex;
    justify-content: center;
    gap: 12px;
}
.action-link {
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;
}
.action-link.primary {
    color: #409eff;
}
.action-link.primary:hover {
    color: #66b1ff;
    text-decoration: underline;
}
.action-link.danger {
    color: #f56c6c;
}
.action-link.danger:hover {
    color: #f78989;
    text-decoration: underline;
}
.member-count-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
}
.member-count {
    font-weight: 600;
    color: #303133;
    font-size: 18px;
}
.member-btn {
    font-size: 12px !important;
    color: #409eff !important;
    padding: 0 !important;
    height: auto !important;
    line-height: 1;
}
.member-btn:hover {
    color: #66b1ff !important;
    text-decoration: underline;
}
.member-popover {
    padding: 0;
}
.popover-title {
    padding: 12px 16px;
    font-weight: 600;
    color: #303133;
    border-bottom: 1px solid #ebeef5;
    background: #fafafa;
}
.member-items {
    padding: 8px;
    max-height: 300px;
    overflow-y: auto;
}
.member-item {
    padding: 10px 12px;
    margin-bottom: 4px;
    background: #f9fafc;
    border-radius: 6px;
    display: flex;
    flex-direction: column;
    gap: 4px;
}
.member-item:last-child {
    margin-bottom: 0;
}
.member-name {
    font-weight: 500;
    color: #303133;
    font-size: 14px;
}
.member-major {
    font-size: 12px;
    color: #909399;
}
</style>
