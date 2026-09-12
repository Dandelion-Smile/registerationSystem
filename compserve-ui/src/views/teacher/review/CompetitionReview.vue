<template>
  <div class="review-page">
    <!-- 顶部栏 -->
    <header class="review-header">
      <div class="header-left">
        <el-button link @click="$router.push(`/teacher/review/${competitionId}/detail`)">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <span class="team-name">{{ teamName }}</span>
        <span class="competition-name">{{ competitionName }}</span>
      </div>
      <div class="header-right">
        <div class="progress-bar">
          <span>当前批阅进度：{{ reviewedCount }} / {{ totalCount }}</span>
          <el-progress 
            :percentage="progressPercentage" 
            :format="p => p === 100 ? '完成' : `${p}%`"
            class="progress-line"
          />
        </div>
        <el-button type="primary" link @click="downloadCurrentFile">
          <el-icon><Download /></el-icon> 下载当前文件
        </el-button>
      </div>
    </header>

    <div class="review-container">
      <!-- 左侧附件列表 -->
      <aside class="file-sidebar">
        <div class="sidebar-title">附件列表</div>
        <div class="file-list">
          <template v-for="(file, index) in attachments" :key="index">
            <!-- File Item -->
            <div 
              class="file-item"
              :class="{ active: currentFile === file }"
              @click="selectFile(file)"
            >
              <el-icon class="file-icon"><Document /></el-icon>
              <span class="file-name" :title="file.name">{{ file.name }}</span>
              <!-- Toggle Arrow for ZIP -->
              <el-icon 
                v-if="file.type === 'zip'" 
                class="zip-arrow"
                :class="{ 'is-expanded': file.isExpanded }"
                @click.stop="toggleZip(file)"
              >
                <ArrowRight />
              </el-icon>
            </div>
            
            <!-- ZIP Content (Tree) inside the list -->
            <div 
              v-if="file.type === 'zip' && file.isExpanded" 
              class="zip-content-inline"
            >
               <el-tree
                v-loading="file.loadingZip"
                :data="file.zipTreeData || []"
                :props="{ label: 'path', children: 'children' }"
                @node-click="handleZipNodeClick"
                class="zip-tree-inline"
              >
                <template #default="{ node, data }">
                  <span class="custom-tree-node">
                    <el-icon v-if="data.isDirectory"><Folder /></el-icon>
                    <el-icon v-else><Document /></el-icon>
                    <span :title="node.label">{{ node.label }}</span>
                  </span>
                </template>
              </el-tree>
            </div>
          </template>
        </div>
      </aside>

      <!-- 中间预览区 -->
      <main class="preview-area">
        <div v-if="loadingPreview" class="loading-state">
          <el-icon class="is-loading"><Loading /></el-icon>
          <p>文件加载中...</p>
        </div>
        
        <div v-else-if="!previewUrl && previewType !== 'onlyoffice'" class="empty-state">
          <el-empty description="请选择左侧文件进行预览" />
        </div>

        <template v-else>
          <!-- OnlyOffice -->
          <div v-if="previewType === 'onlyoffice'" class="onlyoffice-container">
              <div id="onlyoffice-placeholder"></div>
          </div>

          <!-- Image -->
          <div v-else-if="previewType === 'image'" class="image-preview">
            <el-image :src="previewUrl" fit="contain" :preview-src-list="[previewUrl]" />
          </div>

          <!-- Video -->
          <video 
            v-else-if="previewType === 'video'" 
            :src="previewUrl" 
            controls 
            class="video-preview"
          ></video>

          <!-- OnlyOffice -->
          <!-- Text/Code -->
          <pre v-else-if="previewType === 'text'" class="text-preview">{{ textContent }}</pre>

          <!-- ZIP Info -->
          <div v-else-if="previewType === 'zip-info'" class="empty-state">
             <el-empty description="请选择压缩包内文件预览，部分格式仅支持下载。" />
          </div>

          <div v-else-if="previewType === 'download-only'" class="empty-state">
            <el-empty description="该文件暂不支持在线预览，请下载查看。">
              <el-button type="primary" @click="downloadCurrentFile">下载查看</el-button>
            </el-empty>
          </div>

          <!-- Fallback -->
          <div v-else class="empty-state">
            <el-empty description="该文件格式不支持预览，请下载查看" />
          </div>
        </template>
      </main>

      <!-- 右侧评分区 -->
      <aside class="grading-sidebar">
        <div class="grading-panel">
          <div class="panel-header">教师批阅区</div>
          
          <div class="score-section">
            <div class="section-label">作业成绩：</div>
            <div class="score-input-wrapper">
              <el-input-number 
                v-model="scoreForm.score" 
                :min="0" 
                :max="100" 
                :precision="1"
                size="large" 
                class="score-input"
              />
              <span class="score-suffix">分</span>
            </div>
            <el-slider v-model="scoreForm.score" :max="100" :step="0.5" class="score-slider" />
          </div>

          <div class="comment-section">
            <div class="section-label">
              <span>教师评语：</span>
              <span class="word-count">{{ (scoreForm.comment || '').length }}/500</span>
            </div>
            <el-input
              v-model="scoreForm.comment"
              type="textarea"
              :rows="8"
              placeholder="请输入评语..."
              maxlength="500"
              show-word-limit
            />
            <div class="quick-comments">
              <el-tag 
                v-for="tag in quickComments" 
                :key="tag" 
                class="comment-tag" 
                @click="addQuickComment(tag)"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>

          <div class="action-buttons">
            <el-button @click="handlePrev">上一个</el-button>
            <el-button type="primary" @click="handleSave">保存</el-button>
            <el-button @click="handleNext">下一个</el-button>
          </div>

          <div class="status-info">
            <p>批阅状态：
              <el-tag :type="hasReviewed ? 'success' : 'warning'">
                {{ hasReviewed ? '已批阅' : '未批阅' }}
              </el-tag>
            </p>
            <p v-if="lastUpdateTime">最后保存：{{ lastUpdateTime }}</p>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick, onBeforeUnmount, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, Download, Document, Folder, Loading, ArrowRight 
} from '@element-plus/icons-vue'
import { 
  getReviewTeamDetail, 
  getTeamScores, 
  submitScore, 
  updateScore,
  getReviewTeams
} from '@/api/teacher'
import request from '@/utils/request'

import { getToken } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const competitionId = route.params.competitionId

// Data
const teamName = ref('')
const competitionName = ref('')
const reviewedCount = ref(0)
const totalCount = ref(0)
const attachments = ref([])
const currentFile = ref(null)
const previewUrl = ref('')
const previewType = ref('') // onlyoffice, image, video, zip, text
const loadingPreview = ref(false)
const zipTreeData = ref([])
const textContent = ref('')
const currentZipUrl = ref('') // 记录当前正在浏览的压缩包根URL
const participationId = ref(null) // 存储从后端获取的 registerId/participationId

const resetPreviewState = () => {
  attachments.value = []
  currentFile.value = null
  previewUrl.value = ''
  previewType.value = ''
  loadingPreview.value = false
  zipTreeData.value = []
  textContent.value = ''
  currentZipUrl.value = ''
}

const collapseAllZip = () => {
  attachments.value.forEach(f => {
    if (f && f.type === 'zip' && f.isExpanded) {
      f.isExpanded = false
    }
  })
}

const scoreForm = ref({
  score: null,
  comment: ''
})
const originalScoreId = ref(null)
const hasReviewed = ref(false)
const lastUpdateTime = ref('')

const quickComments = ['做得不错', '资料详实', '逻辑清晰', '还需要改进', '缺少关键数据']

// Computed
const progressPercentage = computed(() => {
  if (totalCount.value === 0) return 0
  return Math.round((reviewedCount.value / totalCount.value) * 100)
})

// Methods
const init = async () => {
  console.log('[TeacherCompetitionReview] init:start', {
    routeCompetitionId: route.params.competitionId,
    routeParticipationId: route.params.participationId,
    fullPath: route.fullPath
  })
  resetPreviewState()
  await loadTeamInfo()
  await loadScores()
  await loadProgress()
}

const loadTeamInfo = async () => {
  try {
    const currentParticipationId = route.params.participationId
    console.log('[TeacherCompetitionReview] loadTeamInfo:request', {
      competitionId,
      currentParticipationId,
      requestUrl: `/teacher/review/${competitionId}/participations/${currentParticipationId}`
    })
    const res = await getReviewTeamDetail(competitionId, currentParticipationId)
    const data = res.data || {}
    console.log('[TeacherCompetitionReview] loadTeamInfo:response', {
      registerId: data.registerId,
      teamId: data.teamId,
      teamName: data.teamName,
      competitionName: data.competitionName,
      submitTime: data.submitTime,
      fileCount: Array.isArray(data.files) ? data.files.length : 0
    })
    teamName.value = data.teamName
    competitionName.value = data.competitionName // Assuming API returns this
    // 后端返回的 registerId 即为 participationId
    participationId.value = data.registerId 

    // Parse attachments
    attachments.value = []
    
    // 优先使用后端返回的详细文件列表（包含原始文件名）
    if (data.files && data.files.length > 0) {
      data.files.forEach(file => {
        attachments.value.push({ 
          name: file.name, 
          url: file.url, 
          type: getFileType(file.url) // 也可以直接使用后端返回的 type
        })
      })
    } else {
      // 兼容旧逻辑
      if (data.pptPath) {
        attachments.value.push({ name: getFileName(data.pptPath), url: data.pptPath, type: getFileType(data.pptPath) })
      }
      if (data.pdfPath) {
          // Handle multiple files separated by |
          const paths = data.pdfPath.split('|')
          paths.forEach(p => {
              if(p) attachments.value.push({ name: getFileName(p), url: p, type: getFileType(p) })
          })
      }
    }
    
    // Select first file
    if (attachments.value.length > 0) {
      selectFile(attachments.value[0])
    }
  } catch (error) {
    console.error('[TeacherCompetitionReview] loadTeamInfo:error', {
      competitionId,
      currentParticipationId: route.params.participationId,
      error
    })
    ElMessage.error('加载队伍信息失败')
  }
}

const loadScores = async () => {
  try {
    const currentParticipationId = route.params.participationId
    console.log('[TeacherCompetitionReview] loadScores:request', {
      competitionId,
      currentParticipationId,
      requestUrl: `/teacher/review/${competitionId}/participations/${currentParticipationId}/scores`
    })
    const res = await getTeamScores(competitionId, currentParticipationId)
    const scores = res.data || []
    console.log('[TeacherCompetitionReview] loadScores:response', {
      currentParticipationId,
      scoreCount: scores.length,
      firstScore: scores[0] || null
    })
    if (scores.length > 0) {
      const latest = scores[0]
      scoreForm.value.score = latest.score
      scoreForm.value.comment = latest.comment
      originalScoreId.value = latest.scoreId
      hasReviewed.value = true
      lastUpdateTime.value = latest.scoreTime
    } else {
      scoreForm.value.score = null
      scoreForm.value.comment = ''
      originalScoreId.value = null
      hasReviewed.value = false
    }
  } catch (error) {
    console.error('[TeacherCompetitionReview] loadScores:error', {
      competitionId,
      currentParticipationId: route.params.participationId,
      error
    })
  }
}

const teamsList = ref([]) // Store the list of teams for navigation

const loadProgress = async () => {
    try {
      const res = await getReviewTeams(competitionId)
      teamsList.value = res.data || []
      // 按注册时间排序，与后端保持一致
      teamsList.value.sort((a, b) => {
          const t1 = new Date(a.registerTime).getTime()
          const t2 = new Date(b.registerTime).getTime()
          return t1 - t2
      })
      totalCount.value = teamsList.value.length
      reviewedCount.value = teamsList.value.filter(t => t.reviewed).length
      console.log('[TeacherCompetitionReview] loadProgress:teams', teamsList.value.map(team => ({
        registerId: team.registerId,
        participationId: team.participationId,
        teamId: team.teamId,
        teamName: team.teamName,
        reviewed: team.reviewed,
        submitTime: team.submitTime,
        registerTime: team.registerTime
      })))
    } catch (e) {
      console.error('[TeacherCompetitionReview] loadProgress:error', {
        competitionId,
        error: e
      })
    }
}

const getFileName = (url) => {
  if (!url) return ''
  // Handle signed url if necessary, but usually clean url is stored or passed
  const cleanUrl = url.split('?')[0]
  return cleanUrl.substring(cleanUrl.lastIndexOf('/') + 1)
}

const getFileType = (url) => {
  const ext = getFileName(url).split('.').pop().toLowerCase()
  if (['doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'pdf'].includes(ext)) return 'office'
  if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext)) return 'image'
  if (['mp4', 'webm'].includes(ext)) return 'video'
  if (['txt', 'md', 'json', 'xml', 'log', 'csv'].includes(ext)) return 'text'
  if (['zip', 'rar', '7z'].includes(ext)) return 'zip'
  return 'unknown'
}

const docEditor = shallowRef(null)

const loadOnlyOfficeScript = () => {
  return new Promise((resolve, reject) => {
    if (window.DocsAPI) {
      resolve()
      return
    }
    const script = document.createElement('script')
    // 使用环境变量配置的 OnlyOffice API 地址
    script.src = import.meta.env.VITE_APP_ONLYOFFICE_API
    script.onload = () => resolve()
    script.onerror = (e) => reject(e)
    document.head.appendChild(script)
  })
}

onBeforeUnmount(() => {
    if (docEditor.value) {
        docEditor.value.destroyEditor()
        docEditor.value = null
    }
})

const toggleZip = async (file) => {
    // Toggle expand state
    file.isExpanded = !file.isExpanded
    
    // If expanding and data not loaded, load it
    if (file.isExpanded && !file.zipTreeData) {
        file.loadingZip = true
        try {
            const res = await request({
                url: '/common/preview/zip/tree',
                method: 'get',
                params: { url: file.url }
            })
            // Build tree structure from flat list
            const flatList = res.data || []
            file.zipTreeData = buildTree(flatList)
        } catch (e) {
            ElMessage.error('加载压缩包内容失败')
            file.isExpanded = false // Collapse on error
        } finally {
            file.loadingZip = false
        }
    }
}

const selectFile = async (file) => {
  currentFile.value = file
  loadingPreview.value = true
  previewUrl.value = ''
  textContent.value = ''
  
  if (docEditor.value) {
      docEditor.value.destroyEditor()
      docEditor.value = null
  }
  
  try {
    const type = file.type
    
    // 如果点击的是 ZIP 文件本身（不是里面的子文件），我们不再直接预览 ZIP（也没法预览）
    // 而是自动展开它的目录树（如果没展开的话）
    if (type === 'zip' && !file.isInnerFile) {
        currentZipUrl.value = file.url
        // 如果没展开，就展开；如果已展开，不做操作或者保持展开
        if (!file.isExpanded) {
            await toggleZip(file)
        }
        loadingPreview.value = false
        // 显示一个提示或者保持右侧为空/默认状态
        previewType.value = 'zip-info' // 可以做一个简单的 ZIP 信息展示页
        return
    }
    
    // 非 ZIP 顶层文件时，清空当前 ZIP 根URL（避免后续错误复用）
    if (type !== 'zip' && !file.isInnerFile) {
        currentZipUrl.value = ''
        collapseAllZip()
    }

    const apiUrl = import.meta.env.VITE_APP_BASE_API
    
    if (type === 'image' || type === 'video') {
        // Direct proxy
        previewType.value = type
        previewUrl.value = file.url.startsWith('http') || file.url.startsWith('/')
          ? file.url
          : `${apiUrl}/common/preview/url?url=${encodeURIComponent(file.url)}`
    } else if (type === 'office') {
         previewType.value = 'onlyoffice'
         previewUrl.value = file.url // 设置 previewUrl 以绕过模板中的非空检查
         
         await loadOnlyOfficeScript()
         
         console.log('Requesting OnlyOffice config for:', file.name)
         const res = await request({
             url: '/common/onlyoffice/config',
             method: 'get',
             params: { url: file.url, name: file.name }
         })
         console.log('OnlyOffice config received:', res.data)
          
          // 关键修正：先关闭 loading 状态，让 Vue 渲染 DOM
          loadingPreview.value = false
          await nextTick()
          
         const config = res.data
          if (config) {
              const editorOptions = {
                  documentType: config.documentType,
                  document: config.document,
                  editorConfig: config.editorConfig,
                  token: config.token
              }
              console.log('OnlyOffice debug info:', {
                  documentUrl: editorOptions?.document?.url,
                  documentKey: editorOptions?.document?.key,
                  tokenLength: (editorOptions?.token || '').length,
                  docsApiLoaded: !!window.DocsAPI
              })
              console.log('Initializing DocEditor with config:', editorOptions)
              if (window.DocsAPI) {
                  // Double check element
                  const placeholder = document.getElementById("onlyoffice-placeholder")
                 if (placeholder) {
                     console.log('Found placeholder element:', placeholder)
                     // Clear previous content if any (though logic should handle it)
                     placeholder.innerHTML = ""
                     try {
                        docEditor.value = new window.DocsAPI.DocEditor("onlyoffice-placeholder", editorOptions)
                        console.log('DocEditor initialized successfully', docEditor.value)
                     } catch (err) {
                        console.error('DocEditor constructor threw error:', err)
                        ElMessage.error('OnlyOffice 初始化异常: ' + err.message)
                     }
                 } else {
                     console.error('Placeholder element NOT found in DOM!')
                     ElMessage.error('无法找到预览容器')
                 }
             } else {
                 console.error('DocsAPI not found on window object')
                 ElMessage.error('OnlyOffice 脚本加载失败')
             }
         } else {
             ElMessage.error('获取 OnlyOffice 配置失败')
         }
     } else if (type === 'text') {
        previewType.value = 'text'
        const res = await request({
            url: file.url,
            method: 'get',
            responseType: 'text'
        })
        textContent.value = typeof res === 'string' ? res : (res?.data || '')
     } else if (type === 'zip') {
        previewType.value = 'zip'
        // Load ZIP tree
        const res = await request({
            url: '/common/preview/zip/tree',
            method: 'get',
            params: { url: file.url }
        })
        // Build tree structure from flat list
        const flatList = res.data || []
        zipTreeData.value = buildTree(flatList)
    } else if (type === 'download') {
        previewType.value = 'download-only'
    } else {
        previewType.value = 'unknown'
    }
  } catch (e) {
      console.error(e)
      ElMessage.error('预览失败')
  } finally {
      loadingPreview.value = false
  }
}

const buildTree = (list) => {
    // Simple implementation assuming paths like "folder/file.txt"
    // This needs a proper tree builder
    // For now, just listing them flat for simplicity if complex
    // Or assume backend returns flat and we show flat
    // Let's just return flat mapped to tree format for now
    return list.map(item => ({
        path: item.path,
        isDirectory: item.isDirectory,
        label: item.path
    }))
}

const handleZipNodeClick = (data) => {
    if (!data.isDirectory) {
        const zipUrl = currentZipUrl.value || currentFile.value.url
        const innerPath = data.path
        request({
          url: '/common/preview/zip/preview',
          method: 'get',
          params: { url: zipUrl, path: innerPath }
        }).then((res) => {
          const payload = res.data || {}
          const tempFile = {
            name: payload.name || innerPath.split('/').pop(),
            url: payload.previewUrl,
            downloadUrl: payload.downloadUrl || payload.previewUrl,
            type: payload.type || 'download',
            isInnerFile: true
          }
          if (!payload.canPreview) {
            currentFile.value = tempFile
            previewType.value = 'download-only'
            previewUrl.value = ''
            loadingPreview.value = false
            ElMessage.info('该压缩包内文件暂不支持在线预览，请下载查看')
            return
          }
          selectFile(tempFile)
        }).catch(() => {
          previewType.value = 'download-only'
          previewUrl.value = ''
          loadingPreview.value = false
          ElMessage.warning('该压缩包内文件暂不支持在线预览，请下载查看')
        })
    }
}

const downloadCurrentFile = () => {
    if (currentFile.value) {
        let target = currentFile.value.downloadUrl || currentFile.value.url
        if (target && !target.startsWith('http') && !target.startsWith('/common/preview/')) {
          target = `/common/preview/url?url=${encodeURIComponent(target)}&download=1&name=${encodeURIComponent(currentFile.value.name || '')}`
        } else if (target && target.startsWith('/common/preview/temp?id=')) {
          target = target.includes('download=1') ? target : `${target}&download=1`
        }
        window.open(target, '_blank')
    }
}

const handleSave = async () => {
    if (scoreForm.value.score === null) {
    ElMessage.warning('请输入分数')
    return
  }
  
  if (!participationId.value) {
    ElMessage.error('无法获取参赛ID，请刷新重试')
    return
  }

  try {
    const payload = {
      participationId: participationId.value,
      score: scoreForm.value.score,
      comment: scoreForm.value.comment
    }
    
    if (originalScoreId.value) {
      await updateScore(originalScoreId.value, payload)
    } else {
      await submitScore(payload)
    }
    ElMessage.success('保存成功')
    await loadScores()
    await loadProgress()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const addQuickComment = (tag) => {
    scoreForm.value.comment = (scoreForm.value.comment || '') + tag + '，'
}

const handlePrev = async () => {
  if (teamsList.value.length === 0) {
      await loadProgress()
  }
  const currentParticipationId = Number(route.params.participationId)
  const currentIndex = teamsList.value.findIndex(t => t.registerId === currentParticipationId)
  
  if (currentIndex > 0) {
    const prevTeam = teamsList.value[currentIndex - 1]
    await router.push(`/teacher/review/${competitionId}/${prevTeam.registerId}`)
    // init() 会在 watch 中自动触发
  } else {
    ElMessage.info('已经是第一个队伍了')
  }
}

const handleNext = async () => {
  if (teamsList.value.length === 0) {
      await loadProgress()
  }
  const currentParticipationId = Number(route.params.participationId)
  const currentIndex = teamsList.value.findIndex(t => t.registerId === currentParticipationId)
  
  if (currentIndex !== -1 && currentIndex < teamsList.value.length - 1) {
    const nextTeam = teamsList.value[currentIndex + 1]
    await router.push(`/teacher/review/${competitionId}/${nextTeam.registerId}`)
    // init() 会在 watch 中自动触发
  } else {
    ElMessage.info('已经是最后一个队伍了')
  }
}

// 监听路由参数变化，重新加载数据
watch(
  () => route.params.participationId,
  (newId) => {
    if (newId) {
      console.log('[TeacherCompetitionReview] route.params.participationId:changed', {
        newId,
        fullPath: route.fullPath
      })
      resetPreviewState()
      init()
    }
  }
)

onMounted(() => {
  init()
})
</script>

<style scoped>
.onlyoffice-container {
    width: 100%;
    height: 100%;
    background: #fff;
    display: flex;
    flex-direction: column;
}
#onlyoffice-placeholder {
    width: 100%;
    height: 100%;
    flex: 1;
}
/* Force iframe to take full height */
#onlyoffice-placeholder iframe {
    width: 100% !important;
    height: 100% !important;
}
.review-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f7fa;
}

.review-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.team-name {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.competition-name {
  font-size: 14px;
  color: #909399;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.progress-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 300px;
}

.progress-line {
  flex: 1;
}

.review-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧边栏 */
.file-sidebar {
  width: 250px;
  background: #fff;
  border-right: 1px solid #dcdfe6;
  display: flex;
  flex-direction: column;
}

.sidebar-title {
  padding: 12px 16px;
  font-weight: bold;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.file-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.2s;
  margin-bottom: 4px;
  position: relative;
}

.file-item:hover {
  background: #f5f7fa;
}

.file-item.active {
  background: #ecf5ff;
  color: #409eff;
}

.file-icon {
  margin-right: 8px;
  font-size: 18px;
  flex-shrink: 0;
}

.file-name {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 20px; /* Space for arrow */
}

.zip-arrow {
    position: absolute;
    right: 10px;
    transition: transform 0.3s;
    font-size: 14px;
    color: #909399;
}

.zip-arrow.is-expanded {
    transform: rotate(90deg);
}

.zip-content-inline {
    padding-left: 20px;
    margin-bottom: 10px;
}

.zip-tree-inline {
    background: transparent;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Hide original zip container styles */
.zip-tree-container {
    display: none;
}

/* 中间预览区 */
.preview-area {
  flex: 1;
  background: #eef0f5;
  padding: 0;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  background: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.image-preview, .video-preview {
  max-width: 100%;
  max-height: 100%;
}

.text-preview {
  background: #fff;
  padding: 20px;
  width: 100%;
  height: 100%;
  overflow: auto;
}

/* 右侧评分区 */
.grading-sidebar {
  width: 320px;
  background: #fff;
  border-left: 1px solid #dcdfe6;
  display: flex;
  flex-direction: column;
}

.grading-panel {
  padding: 20px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.panel-header {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.score-section {
  margin-bottom: 24px;
}

.section-label {
  font-weight: 500;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
}

.score-input-wrapper {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.score-input {
  flex: 1;
}

.score-suffix {
  margin-left: 10px;
  font-size: 16px;
  font-weight: bold;
}

.comment-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
}

.word-count {
  font-size: 12px;
  color: #909399;
}

.quick-comments {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.comment-tag {
  cursor: pointer;
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.status-info {
  font-size: 12px;
  color: #909399;
  text-align: center;
}
</style>
