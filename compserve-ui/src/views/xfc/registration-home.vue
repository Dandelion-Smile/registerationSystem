<template>
  <main class="registration-page" v-loading="loading">
    <header class="topbar">
      <div class="event-identity"><div class="school-mark"><img src="@/assets/images/henu-seal.png" alt="河南工业大学校徽" /><span>河南工业大学<small>HENAN UNIVERSITY OF TECHNOLOGY</small></span></div><p>第三届 <strong>“讯飞杯”</strong> AI+创新应用大赛</p><h1>AI 赋能青春 · 创新点亮未来</h1></div>
      <div class="event-slogan">用 AI 连接无限可能<br><small>从校园走向更大的世界</small></div>
      <div class="top-actions"><div class="save-state" :class="saveState"><i class="fa-solid fa-floppy-disk"></i>{{ saveText }}</div><el-button class="logout-button" text @click="logout"><i class="fa-solid fa-arrow-right-from-bracket"></i> 退出登录</el-button></div>
    </header>

    <div class="layout">
      <aside class="progress-nav">
        <p>填写进度</p>
        <ol><li v-for="(step, index) in steps" :key="step.label" :class="{ active: activeStep === index }" @click="scrollToStep(index)"><span>{{ step.no }}</span>{{ step.label }}</li></ol>
        <div class="deadline"><i class="fa-regular fa-clock"></i><span>报名截止<br /><strong>2026.10.15 24:00</strong></span></div>
      </aside>

      <el-form class="registration-form" label-position="top">
        <section :ref="setSectionRef(0)" class="form-section">
          <div class="section-heading"><span>01</span><div><h2>报名与作品</h2><p>选择赛道，并用清晰的语言介绍团队作品。</p></div></div>
          <div class="two-columns">
            <el-form-item label="参赛赛道"><el-select v-model="form.trackCode" placeholder="请选择赛道"><el-option v-for="track in tracks" :key="track.value" :label="track.label" :value="track.value" /></el-select></el-form-item>
            <el-form-item label="队伍名称"><el-input v-model.trim="form.teamName" maxlength="100" show-word-limit placeholder="请输入队伍名称" /></el-form-item>
          </div>
          <el-form-item label="作品名称"><el-input v-model.trim="form.workTitle" maxlength="150" show-word-limit placeholder="请输入作品名称" /></el-form-item>
          <el-form-item label="作品简介"><el-input v-model="form.workSummary" type="textarea" :rows="5" maxlength="200" show-word-limit placeholder="说明要解决的问题、AI 技术方案与预期价值" /></el-form-item>
        </section>

        <section :ref="setSectionRef(1)" class="form-section">
          <div class="section-heading"><span>02</span><div><h2>团队成员</h2><p>按需要填写队长和队员信息，打印表中会按填写内容展示。</p></div></div>
          <article class="member-card captain-card"><div class="member-card-title"><span><b class="role-badge">队长</b> 队长信息</span><small>请手动填写</small></div><div class="member-fields"><el-input v-model.trim="captain.name" placeholder="姓名" /><el-input v-model.trim="captain.studentNo" placeholder="学号" /><el-input v-model.trim="captain.college" placeholder="学院" /><el-input v-model.trim="captain.major" placeholder="专业" /><el-input v-model.trim="captain.className" placeholder="班级" /><el-input v-model.trim="captain.phone" placeholder="联系电话" /></div></article>
          <div class="member-list">
            <article v-for="(member, index) in form.members" :key="index" class="member-card">
              <div class="member-card-title"><span>队员 {{ index + 1 }}</span><el-button link type="danger" @click="removeMember(index)">移除</el-button></div>
              <div class="member-fields"><el-input v-model.trim="member.name" placeholder="姓名" /><el-input v-model.trim="member.studentNo" placeholder="学号" /><el-input v-model.trim="member.college" placeholder="学院" /><el-input v-model.trim="member.major" placeholder="专业" /><el-input v-model.trim="member.className" placeholder="班级" /><el-input v-model.trim="member.phone" placeholder="联系电话" /></div>
            </article>
            <el-button class="add-button" plain :disabled="form.members.length >= MAX_MEMBERS" @click="addMember"><i class="fa-solid fa-plus"></i> 添加队员（最多 {{ MAX_MEMBERS }} 人）</el-button>
          </div>
        </section>

        <section :ref="setSectionRef(2)" class="form-section">
          <div class="section-heading"><span>03</span><div><h2>指导教师</h2><p>按需要填写指导教师信息，打印表中会按填写内容展示。</p></div></div>
          <div class="advisor-list">
            <article v-for="(advisor, index) in form.advisors" :key="index" class="advisor-card"><div class="member-card-title"><span>指导教师 {{ index + 1 }}</span><el-button link type="danger" @click="removeAdvisor(index)">移除</el-button></div><div class="advisor-fields"><el-input v-model.trim="advisor.name" placeholder="姓名" /><el-input v-model.trim="advisor.organization" placeholder="工作单位" /><el-input v-model.trim="advisor.title" placeholder="职称" /><el-input v-model.trim="advisor.phone" placeholder="联系电话" /><el-input v-model.trim="advisor.email" placeholder="电子邮箱" /></div></article>
            <el-button class="add-button" plain :disabled="form.advisors.length >= MAX_ADVISORS" @click="addAdvisor"><i class="fa-solid fa-plus"></i> 添加指导教师（最多 {{ MAX_ADVISORS }} 人）</el-button>
          </div>
        </section>

        <section :ref="setSectionRef(3)" class="form-section materials-section">
          <div class="section-heading"><span>04</span><div><h2>参赛资料</h2><p>上传后可随时替换同类文件；申报书将作为后续填写并转 PDF 的基础文件。</p></div></div>
          <div class="materials-table" role="table" aria-label="参赛资料">
            <div class="materials-head" role="row"><span>资料类别</span><span>文件名称</span><span>大小</span><span>上传时间</span><span>状态</span><span>操作</span></div>
            <div v-for="item in materialDefinitions" :key="item.type" class="materials-row" role="row">
              <div class="material-kind"><b>{{ item.label }}</b><small>{{ item.hint }}</small></div><span class="file-name" :title="materialFor(item.type)?.originalFilename || ''">{{ materialFor(item.type)?.originalFilename || '暂未上传' }}</span><span>{{ materialFor(item.type) ? formatFileSize(materialFor(item.type).fileSize) : '—' }}</span><span>{{ materialFor(item.type) ? formatUploadTime(materialFor(item.type).uploadedAt) : '—' }}</span><span><em :class="['material-status', materialFor(item.type) ? 'uploaded' : item.required ? 'required' : 'optional']">{{ materialFor(item.type) ? '已上传' : item.required ? '必交' : '选交' }}</em></span><el-upload :show-file-list="false" :auto-upload="false" :accept="item.accept" :disabled="uploadingType === item.type" @change="file => selectMaterial(file, item)"><el-button link type="primary" :loading="uploadingType === item.type">{{ materialFor(item.type) ? '替换' : '上传' }}</el-button></el-upload>
            </div>
          </div>
          <p class="material-note"><i class="fa-solid fa-circle-info"></i> 单个文件最大 100 MB。申报书仅支持 Word（.doc、.docx），PPT 演示资料仅支持 .ppt、.pptx；补充资料支持图片、文档、压缩包、视频和 PDF。</p>
        </section>

        <section :ref="setSectionRef(4)" class="form-section declaration">
          <div class="section-heading"><span>05</span><div><h2>队长声明</h2><p>请在打印前确认相关声明内容。</p></div></div>
          <el-checkbox v-model="form.statementTruth">我确认所填报名信息真实、准确，队员已知悉并同意参赛。</el-checkbox>
          <el-checkbox v-model="form.statementEligibility">我确认所有成员未参加本届赛事的其他有效报名队伍。</el-checkbox>
          <el-checkbox v-model="form.statementCopyright">我确认作品及相关材料不侵犯他人知识产权。</el-checkbox>
        </section>

        <div class="actions"><el-button :loading="saving" @click="saveNow">保存草稿</el-button><el-button type="primary" :loading="saving" @click="preparePdf">生成报名表 PDF</el-button></div>
      </el-form>
    </div>
  </main>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { downloadXfcPdf, generateXfcPdf, getXfcCurrentDraft, saveXfcDraft, uploadXfcMaterial } from '@/api/xfc/auth'
import { removeXfcToken } from '@/utils/xfcAuth'

const loading = ref(true)
const saving = ref(false)
const saveState = ref('idle')
const lastSavedAt = ref('')
const hasLegacyOverflow = ref(false)
const activeStep = ref(0)
const sectionElements = ref([])
const router = useRouter()
let scrollFrame
const steps = [{ no:'01', label:'报名与作品' }, { no:'02', label:'团队成员' }, { no:'03', label:'指导教师' }, { no:'04', label:'参赛资料' }, { no:'05', label:'提交声明' }]
const tracks = [{ value:'FOOD', label:'AI+粮食' }, { value:'INDUSTRY', label:'AI+工业' }, { value:'CITY', label:'AI+城市' }, { value:'EDUCATION', label:'AI+教育' }, { value:'MEDICAL', label:'AI+医疗' }]
const MAX_MEMBERS = 5
const MAX_ADVISORS = 2
const captain = reactive({ name:'', studentNo:'', college:'', major:'', className:'', phone:'' })
const form = reactive({ trackCode:'', teamName:'', workTitle:'', workSummary:'', members:[], advisors:[], statementTruth:false, statementEligibility:false, statementCopyright:false })
const materials = ref([])
const uploadingType = ref('')
const materialDefinitions = [{ type:'APPLICATION', label:'申报书', hint:'Word 文档', required:true, accept:'.doc,.docx' }, { type:'PRESENTATION', label:'PPT 演示资料', hint:'PowerPoint', required:true, accept:'.ppt,.pptx' }, { type:'SUPPLEMENT', label:'其他补充资料', hint:'可选附件', required:false, accept:'.doc,.docx,.xls,.xlsx,.ppt,.pptx,.pdf,.zip,.rar,.7z,.txt,.jpg,.jpeg,.png,.mp4,.mov' }]
const saveText = computed(() => saveState.value === 'saving' ? '正在保存…' : saveState.value === 'saved' ? `草稿已保存 ${lastSavedAt.value}` : saveState.value === 'failed' ? '草稿尚未保存' : '填写后请点击保存草稿')

function blankMember() { return { name:'', studentNo:'', college:'', major:'', className:'', phone:'' } }
function blankAdvisor() { return { name:'', organization:'', title:'', phone:'', email:'' } }
function applyDraft(data) {
  const registration = data.registration || {}
  form.trackCode = registration.trackCode || ''
  form.teamName = registration.teamName || ''
  form.workTitle = registration.workTitle || ''
  form.workSummary = registration.workSummary || ''
  form.statementTruth = Boolean(registration.statementTruth)
  form.statementEligibility = Boolean(registration.statementEligibility)
  form.statementCopyright = Boolean(registration.statementCopyright)
  materials.value = Array.isArray(data.materials) ? data.materials : []
  Object.assign(captain, data.captain || {})
  const savedMembers = data.members || []
  const savedAdvisors = data.advisors || []
  hasLegacyOverflow.value = savedMembers.length > MAX_MEMBERS || savedAdvisors.length > MAX_ADVISORS
  form.members.splice(0, form.members.length, ...savedMembers.slice(0, MAX_MEMBERS).map(({ name='', studentNo='', college='', major='', className='', phone='' }) => ({ name, studentNo, college, major, className, phone })))
  form.advisors.splice(0, form.advisors.length, ...savedAdvisors.slice(0, MAX_ADVISORS).map(({ name='', organization='', title='', phone='', email='' }) => ({ name, organization, title, phone, email })))
}
function materialFor(type) { return materials.value.find(item => item.materialType === type) }
function formatFileSize(size) { if (size < 1024 * 1024) return `${Math.max(1, Math.round(size / 1024))} KB`; return `${(size / 1024 / 1024).toFixed(size >= 10 * 1024 * 1024 ? 0 : 1)} MB` }
function formatUploadTime(value) { const date = new Date(value); return Number.isNaN(date.getTime()) ? String(value).replace('T', ' ').slice(0, 16) : date.toLocaleString('zh-CN', { year:'numeric', month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit', hour12:false }) }
async function selectMaterial(uploadFile, definition) {
  const file = uploadFile.raw; if (!file) return
  const extension = file.name.includes('.') ? file.name.split('.').pop().toLowerCase() : ''
  if (!definition.accept.split(',').map(item => item.slice(1)).includes(extension)) return ElMessage.error(`${definition.label}文件类型不符合要求`)
  if (file.size > 100 * 1024 * 1024) return ElMessage.error('单个文件不能超过 100 MB')
  uploadingType.value = definition.type
  try { const response = await uploadXfcMaterial(definition.type, file); const index = materials.value.findIndex(item => item.materialType === definition.type); if (index >= 0) materials.value.splice(index, 1, response.data); else materials.value.push(response.data); ElMessage.success(`${definition.label}已上传`) } catch { ElMessage.error(`${definition.label}上传失败，请稍后重试`) } finally { uploadingType.value = '' }
}
async function saveNow(showMessage = true) {
  if (saving.value) return
  saving.value = true; saveState.value = 'saving'
  try {
    const response = await saveXfcDraft({ ...form, captain })
    applyDraft(response.data)
    saveState.value = 'saved'; lastSavedAt.value = new Date().toLocaleTimeString('zh-CN', { hour:'2-digit', minute:'2-digit' })
    if (showMessage) ElMessage.success('草稿已保存')
  } catch (error) {
    saveState.value = 'failed'
    if (showMessage) ElMessage.error('草稿保存失败，请检查网络后重试')
  } finally { saving.value = false }
}
function addMember() {
  if (form.members.length >= MAX_MEMBERS) return ElMessage.warning(`队员最多添加 ${MAX_MEMBERS} 人`)
  form.members.push(blankMember())
}
function removeMember(index) { form.members.splice(index, 1) }
function addAdvisor() {
  if (form.advisors.length >= MAX_ADVISORS) return ElMessage.warning(`指导教师最多添加 ${MAX_ADVISORS} 人`)
  form.advisors.push(blankAdvisor())
}
function removeAdvisor(index) { form.advisors.splice(index, 1) }
function setSectionRef(index) { return (element) => { if (element) sectionElements.value[index] = element } }
function scrollToStep(index) { sectionElements.value[index]?.scrollIntoView({ behavior:'smooth', block:'start' }) }
function setupSectionObserver() {
  updateActiveStep()
  window.addEventListener('scroll', handleScroll, { passive: true })
}
function handleScroll() { window.cancelAnimationFrame(scrollFrame); scrollFrame = window.requestAnimationFrame(updateActiveStep) }
function updateActiveStep() {
  const readingLine = window.innerHeight * 0.32
  let closestIndex = 0
  let closestDistance = Number.POSITIVE_INFINITY
  sectionElements.value.forEach((element, index) => {
    const distance = Math.abs(element.getBoundingClientRect().top - readingLine)
    if (distance < closestDistance) { closestDistance = distance; closestIndex = index }
  })
  activeStep.value = closestIndex
}
function logout() { removeXfcToken(); router.replace('/xfc/login') }
async function preparePdf() {
  if (!materialFor('APPLICATION') || !materialFor('PRESENTATION')) return ElMessage.warning('请先上传必交的申报书和 PPT 演示资料')
  await saveNow(false)
  if (saveState.value !== 'saved') return
  try {
    const generated = await generateXfcPdf()
    const blob = await downloadXfcPdf(generated.data.id)
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = `讯飞杯报名表_${generated.data.version}.pdf`
    anchor.style.display = 'none'
    document.body.appendChild(anchor)
    anchor.click()
    anchor.remove()
    window.setTimeout(() => URL.revokeObjectURL(url), 1000)
    ElMessage.success(`报名表 PDF 已生成并开始下载（版本 ${generated.data.version}）`)
  } catch (error) {
    ElMessage.error('报名表 PDF 生成失败，请稍后重试')
  }
}
onMounted(async () => { try { const response = await getXfcCurrentDraft(); applyDraft(response.data); if (hasLegacyOverflow.value) ElMessage.warning(`已按最新规则仅保留前 ${MAX_MEMBERS} 名队员和前 ${MAX_ADVISORS} 名指导教师，请保存草稿以更新记录`) } catch { ElMessage.error('报名草稿加载失败，请重新登录后重试') } finally { loading.value = false; await nextTick(); setupSectionObserver() } })
onBeforeUnmount(() => { window.cancelAnimationFrame(scrollFrame); window.removeEventListener('scroll', handleScroll) })
</script>

<style scoped>
.registration-page{min-height:100vh;background:#f6f7f4;color:#17394c;font-family:"Microsoft YaHei",sans-serif}.topbar{display:flex;justify-content:space-between;align-items:end;padding:30px clamp(22px,5vw,80px);background:#0b405a;color:#fff}.topbar p{margin:0;color:#d7ab49;font-size:13px;font-weight:700;letter-spacing:.08em}.topbar h1{margin:7px 0 0;font:700 32px/1.2 STSong,serif}.top-actions{display:flex;align-items:center;gap:16px}.save-state{font-size:13px;color:#d2dfe3}.save-state i{margin-right:7px}.save-state.saved{color:#a9e0d1}.save-state.failed{color:#ffd08b}.logout-button{color:#d6e3e5}.logout-button:hover{color:#fff;background:rgba(255,255,255,.12)}.logout-button i{margin-right:6px}.layout{display:grid;grid-template-columns:210px minmax(0,880px);gap:36px;max-width:1180px;margin:0 auto;padding:38px 26px 72px}.progress-nav{position:sticky;top:22px;align-self:start;padding:20px 0;max-height:calc(100vh - 44px)}.progress-nav>p{color:#a17114;font-size:12px;font-weight:700;letter-spacing:.12em}.progress-nav ol{position:relative;padding:0;margin:20px 0;list-style:none}.progress-nav ol::before{content:"";position:absolute;top:6px;bottom:34px;left:8px;width:1px;background:#d9e3df}.progress-nav li{position:relative;z-index:1;padding:0 0 27px 34px;color:#9ba9aa;font-size:14px;cursor:pointer;transition:color .28s ease,transform .28s ease}.progress-nav li::after{content:"";position:absolute;left:3px;top:4px;width:11px;height:11px;border:2px solid #d2dddd;border-radius:50%;background:#f6f7f4;transition:all .28s ease}.progress-nav li:hover{color:#42666b;transform:translateX(2px)}.progress-nav li span{position:absolute;left:0;top:-4px;color:#bdc9c9;font:700 12px/1.8 Consolas,monospace;transition:color .28s ease}.progress-nav li.active{color:#075665;font-weight:800;transform:translateX(5px)}.progress-nav li.active::after{border-color:#d19a25;background:#e0a719;box-shadow:0 0 0 5px rgba(209,154,37,.16)}.progress-nav li.active span{color:#d69b18}.deadline{display:flex;gap:10px;margin-top:32px;padding:15px;border-top:1px solid #d9e0dc;border-bottom:1px solid #d9e0dc;color:#687a7c;font-size:12px;line-height:1.7}.deadline i{color:#c48b18;padding-top:4px}.deadline strong{color:#274a57}.registration-form{padding:42px;background:#fff;border:1px solid #e2e8e4;box-shadow:0 15px 45px rgba(33,69,74,.07)}.form-section{scroll-margin-top:24px;padding-bottom:36px;margin-bottom:36px;border-bottom:1px solid #e7ece8}.form-section:last-of-type{border-bottom:0;margin-bottom:0}.section-heading{display:flex;gap:16px;margin-bottom:26px}.section-heading>span{display:grid;place-items:center;flex:0 0 32px;height:32px;border-radius:50%;background:#edf4f1;color:#ba841b;font:700 12px Consolas,monospace}.section-heading h2{margin:0;color:#153b4b;font-size:20px}.section-heading p{margin:5px 0 0;color:#7a8a8b;font-size:13px}.two-columns{display:grid;grid-template-columns:1fr 1fr;gap:20px}.registration-form :deep(.el-form-item__label){font-weight:700;color:#40585d}.captain-card{border-color:#bdd7d2;background:#f5faf8}.role-badge{display:inline-block;margin-right:8px;padding:4px 8px;background:#0b5060;color:#fff;font-size:11px}.member-list,.advisor-list{display:grid;gap:13px}.member-card,.advisor-card{padding:18px;background:#fafcfb;border:1px solid #e0e9e5}.member-card-title{display:flex;justify-content:space-between;align-items:center;margin-bottom:13px;color:#34545b;font-size:14px;font-weight:700}.member-card-title small{color:#859596;font-size:12px;font-weight:400}.member-fields{display:grid;grid-template-columns:repeat(3,1fr);gap:10px}.advisor-fields{display:grid;grid-template-columns:repeat(3,1fr);gap:10px}.add-button{border-style:dashed;color:#2f6c76}.add-button i{margin-right:7px}.declaration{display:grid;gap:14px}.declaration .section-heading{margin-bottom:5px}.actions{display:flex;justify-content:flex-end;gap:12px;padding-top:8px}@media(max-width:850px){.layout{grid-template-columns:1fr}.progress-nav{display:none}.member-fields,.advisor-fields{grid-template-columns:repeat(2,1fr)}}@media(max-width:560px){.topbar{align-items:start;gap:14px;flex-direction:column}.top-actions{width:100%;justify-content:space-between}.layout{padding:20px 12px}.registration-form{padding:25px 18px}.two-columns,.member-fields,.advisor-fields{grid-template-columns:1fr}.actions{flex-direction:column}.actions .el-button{margin:0}}@media(prefers-reduced-motion:reduce){.progress-nav li,.progress-nav li::after{transition:none}}
/* Target composition: the background is the supplied artwork; all inputs keep their original v-model bindings. */
.registration-page {
  min-height: 100vh;
  padding: 25px 0 145px;
  background: #c7e7ff url("@/assets/images/xfc-register-background.png") center top / 100% auto no-repeat fixed;
  color: #123f7d;
}
.topbar { position: relative; box-sizing: border-box; width: min(1180px, calc(100% - 48px)); min-height: 224px; margin: 0 auto 6px; padding: 9px 18px; background: transparent; color: #0c3f89; align-items: flex-start; overflow: hidden; }
.event-identity { position: relative; z-index: 1; }.school-mark { display: flex; align-items: center; gap: 10px; font-size: 22px; font-weight: 800; letter-spacing: 3px; }.school-mark b { display: grid; place-items: center; width: 43px; height: 43px; border: 2px solid #1552a2; border-radius: 50%; font-size: 10px; letter-spacing: 0; }.school-mark small { display: block; margin-top: 3px; font-size: 7px; letter-spacing: 1px; }.topbar p { margin: 25px 0 0; color: #0b3f88; font-size: clamp(25px, 3vw, 39px); font-weight: 900; letter-spacing: 1px; text-shadow: 0 2px 10px rgba(255,255,255,.76); }.topbar p strong { color: #8738ef; }.topbar h1 { margin: 8px 0 0; color: #174a93; font: 500 20px/1.25 "Microsoft YaHei",sans-serif; letter-spacing: 7px; }.event-slogan { position: absolute; right: 22px; top: 29px; color: #155cd9; font-size: 17px; line-height: 1.6; text-align: center; transform: rotate(-8deg); font-weight: 700; }.event-slogan small { font-size: 14px; }.top-actions { position: absolute; z-index: 2; right: 15px; bottom: 20px; padding: 8px 11px; border-radius: 10px; background: rgba(255,255,255,.62); }.save-state { color: #466786; }.logout-button { color: #1556b3; }.logout-button:hover { color: #0e3d91; background: rgba(255,255,255,.7); }
.layout { width: min(1180px, calc(100% - 48px)); max-width: none; grid-template-columns: 214px minmax(0, 1fr); gap: 16px; padding: 0; align-items: start; }.progress-nav { box-sizing: border-box; min-height: 590px; max-height: none; padding: 24px 22px 20px; border: 1px solid rgba(255,255,255,.85); border-radius: 16px; background: linear-gradient(180deg,rgba(255,255,255,.92),rgba(247,248,255,.78)); box-shadow: 0 10px 26px rgba(64,107,184,.13); }.progress-nav>p { margin: 0 0 28px; color: #2072db; font-size: 16px; letter-spacing: 0; }.progress-nav ol { margin: 0; }.progress-nav ol::before { left: 10px; background: #dae2ef; }.progress-nav li { padding: 0 0 31px 35px; color: #9caac0; font-size: 14px; }.progress-nav li::after { left: 4px; width: 13px; height: 13px; border: 2px solid #d4dceb; background: #fff; }.progress-nav li span { display: grid; place-items: center; left: 0; top: 0; width: 21px; height: 21px; border-radius: 50%; background: #cdd6e7; color: #fff; font-size: 0; }.progress-nav li.active { color: #135fe3; transform: none; }.progress-nav li.active::after { border-color: #235be9; background: #235be9; box-shadow: 0 0 0 4px rgba(41,102,234,.13); }.progress-nav li.active span { background: #235be9; color: #fff; font-size: 12px; }.deadline { margin: 82px -22px 0; padding: 17px 20px; border-top: 1px solid #dce5f1; border-bottom: 0; color: #114c9e; }.deadline i { color: #225ee6; }.deadline strong { color: #114c9e; }
.registration-form { padding: 0; background: transparent; border: 0; box-shadow: none; }.form-section { box-sizing: border-box; margin: 0 0 12px; padding: 21px 24px 15px; border: 1px solid rgba(255,255,255,.87); border-radius: 17px; background: rgba(255,255,255,.91); box-shadow: 0 9px 24px rgba(43,111,179,.11); scroll-margin-top: 20px; }.form-section:last-of-type { margin-bottom: 12px; }.section-heading { gap: 14px; margin-bottom: 17px; }.section-heading>span { flex-basis: 43px; height: 43px; background: linear-gradient(135deg,#e2f3ff,#e7edff); color: #1859dc; font-size: 17px; }.section-heading h2 { color: #0d3d78; font-size: 20px; }.section-heading p { color: #8494ae; }.registration-form :deep(.el-form-item) { margin-bottom: 12px; }.registration-form :deep(.el-form-item__label) { color: #153f78; }.registration-form :deep(.el-input__wrapper),.registration-form :deep(.el-textarea__inner) { background: rgba(255,255,255,.82); box-shadow: 0 0 0 1px #dbe4f0 inset; }.registration-form :deep(.el-input__wrapper.is-focus),.registration-form :deep(.el-textarea__inner:focus) { box-shadow: 0 0 0 1px #4e85f1 inset; }.captain-card,.member-card,.advisor-card { padding:13px; border-color:#e1e9f3; border-radius:7px; background:#fbfdff; }.captain-card { background: #f7fbff; }.member-card-title { color: #1d578b; }.role-badge { background: #075d81; border-radius: 3px; }.add-button { width:100%; color:#125e91; border-color:#dce6ef; }.declaration { gap: 11px; }.declaration :deep(.el-checkbox) { color: #71829c; }.actions { margin-top: 0; padding: 16px 24px; border-radius: 16px; background: rgba(255,255,255,.88); box-shadow: 0 9px 24px rgba(43,111,179,.11); }.actions :deep(.el-button--primary) { border: 0; background: linear-gradient(100deg,#726df2,#40bdea); }
.school-mark img { display: block; width: 52px; height: 52px; object-fit: contain; border-radius: 50%; filter: drop-shadow(0 2px 4px rgba(14, 58, 129, .16)); }
@media(max-width:850px){.registration-page{padding-top:10px;background-size:auto 100%;}.topbar,.layout{width:calc(100% - 28px);}.topbar{min-height:166px;padding:4px 0;}.school-mark{font-size:16px;}.school-mark img{width:39px;height:39px}.topbar p{margin-top:22px;font-size:23px;}.topbar h1{font-size:13px;letter-spacing:3px;}.event-slogan{display:none;}.top-actions{right:0;bottom:2px;}.progress-nav{display:none;}.layout{grid-template-columns:1fr}.form-section{padding:18px 16px}.actions{padding:14px 16px}}
.materials-table{overflow:hidden;border:1px solid #dbe6f4;border-radius:10px;background:#fff}.materials-head,.materials-row{display:grid;grid-template-columns:1.25fr 2fr .75fr 1.5fr .7fr .55fr;align-items:center;gap:12px;padding:13px 16px}.materials-head{background:#f3f8ff;color:#315b8f;font-size:12px;font-weight:800}.materials-row{min-height:58px;border-top:1px solid #edf1f7;color:#6d7e97;font-size:13px}.material-kind{display:grid;gap:4px}.material-kind b{color:#174b8b;font-size:14px}.material-kind small{color:#94a4b9;font-size:11px}.file-name{overflow:hidden;color:#36587e;text-overflow:ellipsis;white-space:nowrap}.material-status{display:inline-block;padding:3px 7px;border-radius:10px;font-style:normal;font-size:11px;line-height:1}.material-status.uploaded{background:#e7f8ef;color:#26955e}.material-status.required{background:#fff0f1;color:#df6371}.material-status.optional{background:#f0f3f8;color:#8694a9}.material-note{margin:12px 2px 0;color:#8393ab;font-size:12px;line-height:1.7}.material-note i{margin-right:6px;color:#4d86e7}.materials-row :deep(.el-upload){justify-self:start}@media(max-width:850px){.materials-table{overflow-x:auto}.materials-head,.materials-row{min-width:700px}}@media(max-width:560px){.materials-section{padding-left:12px;padding-right:12px}.material-note{font-size:11px}}
</style>
