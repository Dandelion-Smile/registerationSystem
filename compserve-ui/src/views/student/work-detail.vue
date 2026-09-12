<template>
  <div class="work-detail-page">
    <StudentNavbar />

    <main class="work-detail-page__main">
      <div class="page-toolbar artifact-header">
        <button type="button" class="back-button" @click="goBack">
          <i class="fas fa-arrow-left"></i>
          <span>返回</span>
        </button>
        <h1 class="page-title">竞赛作品详情</h1>
        <button type="button" class="toolbar-action" @click="downloadAllMaterials">
          <i class="fas fa-download"></i>
          <span>下载作品资料</span>
        </button>
      </div>

      <section class="detail-top-grid">
        <div class="left-column">
          <article class="surface-card work-panel">
            <div class="card-head">
              <span class="card-icon"><i class="fas fa-file-alt"></i></span>
              <h2>作品信息</h2>
            </div>

            <div class="artifact-layout">
              <div class="artifact-cover">
                <div class="artifact-cover__icon">
                  <i class="fas fa-file-signature"></i>
                </div>
              </div>

              <div class="artifact-body">
                <h3 class="artifact-title">{{ detail.workName || "未填写作品名称" }}</h3>
                <p class="artifact-summary">{{ detail.workDescription || "暂无作品说明" }}</p>

                <div class="artifact-meta-cards">
                  <div class="meta-card">
                    <span class="meta-label">报名时间</span>
                    <strong>{{ formatDate(detail.registerTime) || "—" }}</strong>
                  </div>
                  <div class="meta-card">
                    <span class="meta-label">作品状态</span>
                    <strong class="status-text" :class="heroStatusClass">
                      {{ detail.recordStatusText || detail.reviewStatusText || "未评审" }}
                    </strong>
                  </div>
                  <div class="meta-card">
                    <span class="meta-label">作品编号</span>
                    <strong>{{ workCode }}</strong>
                  </div>
                </div>
              </div>
            </div>
          </article>

          <article class="surface-card content-card">
            <div class="card-head">
              <span class="card-icon"><i class="fas fa-lightbulb"></i></span>
              <h2>作品内容</h2>
            </div>

            <div class="content-card__rows">
              <div class="content-item">
                <span class="content-item__label">作品摘要</span>
                <p class="content-item__value">{{ detail.workDescription || "暂无作品说明" }}</p>
              </div>

              <div class="content-item">
                <span class="content-item__label">关键技术</span>
                <div class="keyword-pills">
                  <span v-for="keyword in keywordList" :key="keyword" class="keyword-pill">
                    {{ keyword }}
                  </span>
                  <span v-if="keywordList.length === 0" class="empty-inline">暂无关键技术标签</span>
                </div>
              </div>

              <div class="content-item">
                <span class="content-item__label">作品附件</span>
                <div class="attachment-stack">
                  <button
                    v-if="detail.pptPath"
                    type="button"
                    class="attachment-card"
                    @click="openFile(detail.pptPath)"
                  >
                    <i class="fas fa-file-powerpoint attachment-type"></i>
                    <div class="attachment-meta">
                      <strong>{{ detail.pptName || "PPT材料" }}</strong>
                      <span>点击查看或下载</span>
                    </div>
                    <i class="fas fa-download attachment-action"></i>
                  </button>

                  <button
                    v-for="file in documentFiles"
                    :key="file.url"
                    type="button"
                    class="attachment-card"
                    @click="openFile(file.url)"
                  >
                    <i :class="[file.icon, 'attachment-type']"></i>
                    <div class="attachment-meta">
                      <strong>{{ file.name }}</strong>
                      <span>点击查看或下载</span>
                    </div>
                    <i class="fas fa-download attachment-action"></i>
                  </button>

                  <span v-if="!detail.pptPath && documentFiles.length === 0" class="empty-inline">暂无作品附件</span>
                </div>
              </div>
            </div>
          </article>
        </div>

        <div class="right-column">
          <article class="surface-card side-info-card team-panel">
            <div class="card-head card-head--between">
              <div class="card-head__title">
                <span class="card-icon"><i class="fas fa-users"></i></span>
                <h2>队伍信息</h2>
              </div>
              <button type="button" class="subtle-action" @click="openTeamDetailDialog">
                <span>查看队伍详情</span>
                <i class="fas fa-angle-right"></i>
              </button>
            </div>

            <div class="team-detail-grid">
              <span class="detail-key">队伍名称</span>
              <span class="detail-value">{{ detail.teamName || "—" }}</span>

              <span class="detail-key">队长</span>
              <span class="detail-value leader-inline-name">{{ leaderMemberLabel }}</span>

              <span class="detail-key">队员</span>
              <div class="team-member-pills">
                <span
                  v-for="member in simplifiedMembers"
                  :key="`${member.studentNo}-${member.role || ''}`"
                  class="team-member-chip"
                >
                  {{ member.name || "未填写" }}
                </span>
                <span v-if="simplifiedMembers.length === 0" class="empty-inline">暂无队员信息</span>
              </div>

              <span class="detail-key">所属院校</span>
              <span class="detail-value">{{ teamSchool }}</span>

              <span class="detail-key">联系电话</span>
              <span class="detail-value">{{ teamPhone }}</span>
            </div>
          </article>

          <article class="surface-card side-info-card mentor-panel">
            <div class="card-head card-head--between">
              <div class="card-head__title">
                <span class="card-icon"><i class="fas fa-user-tie"></i></span>
                <h2>指导老师</h2>
              </div>
              <button type="button" class="subtle-action" @click="openMentorDetailDialog">
                <span>查看详情</span>
                <i class="fas fa-angle-right"></i>
              </button>
            </div>

            <div class="mentor-detail-grid">
              <span class="detail-key">指导老师</span>
              <span class="detail-value">{{ primaryTeacher.name || "—" }}</span>

              <span class="detail-key">所在单位</span>
              <span class="detail-value">{{ primaryTeacher.unit || primaryTeacher.workUnit || "—" }}</span>

              <span class="detail-key">联系电话</span>
              <span class="detail-value">{{ primaryTeacher.phone || "—" }}</span>
            </div>
          </article>
        </div>
      </section>

      <section class="surface-card review-panel review-primary">
        <div class="card-head">
          <span class="card-icon"><i class="fas fa-clipboard-check"></i></span>
          <h2>评审情况</h2>
        </div>

        <div class="review-summary-bar review-status-strip">
          <div class="summary-chip">
            <span>评审状态</span>
            <strong :class="heroStatusClass">{{ detail.recordStatusText || detail.reviewStatusText || "未评审" }}</strong>
          </div>
          <div class="summary-chip">
            <span>已分配评审老师</span>
            <strong>{{ detail.reviewerCount || 0 }} 位</strong>
          </div>
          <div class="summary-chip">
            <span>已完成评审</span>
            <strong>{{ detail.reviewedCount || 0 }} 位</strong>
          </div>
          <div class="summary-chip">
            <span>平均得分</span>
            <strong>{{ detail.finalScore ?? "--" }} 分</strong>
          </div>
        </div>

        <div class="review-table review-score-table">
          <div class="review-table__head">
            <span>评审老师</span>
            <span>职称/单位</span>
            <span>评审状态</span>
            <span>评分</span>
            <span>评分时间</span>
            <span>评审意见</span>
          </div>

          <div
            v-for="reviewer in normalizedReviewers"
            :key="`${reviewer.assignmentId}-${reviewer.reviewerId}`"
            class="review-table__row"
          >
            <span class="reviewer-name">{{ reviewer.reviewerName }}</span>
            <span class="reviewer-unit">{{ reviewer.titleOrUnit }}</span>
            <span class="review-badge" :class="{ reviewed: reviewer.reviewed, pending: !reviewer.reviewed }">
              {{ reviewer.reviewStatusText }}
            </span>
            <span class="review-score">{{ reviewer.score ?? "--" }}</span>
            <span>{{ formatDate(reviewer.scoreTime) || "--" }}</span>
            <span class="review-opinion">{{ formatReviewOpinion(reviewer.comment) }}</span>
          </div>

          <div v-if="normalizedReviewers.length === 0" class="review-table__empty">
            暂未分配评审老师
          </div>
        </div>
      </section>

      <el-dialog
        v-model="teamDetailDialogVisible"
        title="队伍信息详情"
        width="760px"
        align-center
        class="detail-dialog"
      >
        <div class="dialog-section">
          <div class="dialog-summary">
            <div class="dialog-summary__item">
              <span class="dialog-label">队伍名称</span>
              <strong>{{ detail.teamName || "—" }}</strong>
            </div>
            <div class="dialog-summary__item">
              <span class="dialog-label">所属院校</span>
              <strong>{{ teamSchool }}</strong>
            </div>
            <div class="dialog-summary__item">
              <span class="dialog-label">联系电话</span>
              <strong>{{ teamPhone }}</strong>
            </div>
          </div>

          <div class="member-dialog-list">
            <article
              v-for="member in detail.teamMembers || []"
              :key="`${member.studentNo}-${member.role || ''}`"
              class="member-detail-card"
            >
              <div class="member-detail-card__top">
                <strong>{{ member.name || "未填写" }}</strong>
                <span class="member-role" :class="{ active: member.isCurrentUser }">
                  {{ member.role === "leader" ? "队长" : "队员" }}
                </span>
              </div>
              <div class="member-detail-grid">
                <span>学号：{{ member.studentNo || "—" }}</span>
                <span>专业：{{ member.major || "—" }}</span>
                <span>学院：{{ member.college || "—" }}</span>
                <span>电话：{{ member.phone || "—" }}</span>
                <span>邮箱：{{ member.email || "—" }}</span>
              </div>
            </article>
          </div>
        </div>
      </el-dialog>

      <el-dialog
        v-model="mentorDetailDialogVisible"
        title="指导老师详情"
        width="720px"
        align-center
        class="detail-dialog"
      >
        <div class="dialog-section">
          <article
            v-for="teacher in detail.teacherList || []"
            :key="`${teacher.teacherId || teacher.name}-${teacher.phone || ''}`"
            class="mentor-detail-card"
          >
            <strong>{{ teacher.name || "未填写" }}</strong>
            <div class="mentor-detail-grid-card">
              <span>职称：{{ teacher.title || "—" }}</span>
              <span>职位：{{ teacher.position || "—" }}</span>
              <span>单位：{{ teacher.unit || teacher.workUnit || "—" }}</span>
              <span>电话：{{ teacher.phone || "—" }}</span>
              <span>邮箱：{{ teacher.email || "—" }}</span>
              <span>政治面貌：{{ teacher.politicalStatus || "—" }}</span>
            </div>
          </article>
          <div v-if="!detail.teacherList || detail.teacherList.length === 0" class="empty-inline">暂无指导老师信息</div>
        </div>
      </el-dialog>
    </main>
  </div>
</template>

<script setup name="StudentWorkDetail">
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import StudentNavbar from "@/components/StudentNavbar.vue";
import { getStudentWorkDetail } from "@/api/competition";
import { formatDate as formatDateText } from "@/utils/index";
import { formatReviewOpinion } from "@/utils/reviewOpinion";

const route = useRoute();
const router = useRouter();

const teamDetailDialogVisible = ref(false);
const mentorDetailDialogVisible = ref(false);

const detail = reactive({
  competitionName: "",
  workName: "",
  workDescription: "",
  teamName: "",
  teamId: "",
  registerTime: "",
  submitTime: "",
  submissionStatus: "",
  recordStatusText: "",
  reviewStatusText: "",
  finalScore: null,
  reviewedCount: 0,
  reviewerCount: 0,
  pptPath: "",
  pptName: "",
  pdfNames: "",
  pdfPath: "",
  teacherList: [],
  teamMembers: [],
  reviewers: [],
});

const heroStatusClass = computed(() => {
  const text = String(detail.recordStatusText || detail.reviewStatusText || "");
  if (text.includes("已评审")) return "is-reviewed";
  if (text.includes("评审中")) return "is-reviewing";
  return "is-pending";
});

const keywordList = computed(() => {
  const text = String(detail.workDescription || "");
  const presets = ["深度学习", "图像识别", "卷积神经网络", "数据增强", "移动端应用"];
  return presets.filter((item) => text.includes(item));
});

const workCode = computed(() => {
  const registerId = detail.displayRegisterId || route.params.registerId || "";
  return registerId ? `P${String(registerId).padStart(10, "0")}` : "—";
});

const documentFiles = computed(() => {
  const rawPaths = String(detail.pdfPath || "").split("|").filter(Boolean);
  const rawNames = String(detail.pdfNames || "").split("|");
  return rawPaths.map((url, index) => {
    const name = rawNames[index] || url.split("/").pop() || `材料 ${index + 1}`;
    const lower = name.toLowerCase();
    return {
      url,
      name,
      icon: lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z")
        ? "fas fa-file-archive"
        : "fas fa-file-pdf",
    };
  });
});

const normalizedReviewers = computed(() =>
  (detail.reviewers || []).map((reviewer) => ({
    ...reviewer,
    titleOrUnit: reviewer.titleOrUnit || reviewer.unit || reviewer.workUnit || "—",
  })),
);

const primaryTeacher = computed(() => detail.teacherList?.[0] || {});

const leaderMember = computed(() =>
  (detail.teamMembers || []).find((member) => member.role === "leader") || {},
);

const leaderMemberLabel = computed(() => {
  const leader = leaderMember.value;
  if (!leader.name) return "—";
  return leader.isCurrentUser ? `${leader.name}（我）` : leader.name;
});

const simplifiedMembers = computed(() =>
  (detail.teamMembers || []).filter((member) => member.role !== "leader"),
);

const teamSchool = computed(() => {
  const firstWithCollege = (detail.teamMembers || []).find((member) => member.college);
  return firstWithCollege?.college || "—";
});

const teamPhone = computed(() => leaderMember.value?.phone || "—");

function formatDate(value) {
  return value ? formatDateText(value) : "";
}

function openFile(url) {
  if (!url) return;
  window.open(url, "_blank", "noopener");
}

function downloadAllMaterials() {
  if (detail.pptPath) openFile(detail.pptPath);
  documentFiles.value.forEach((file) => openFile(file.url));
}

function goBack() {
  router.back();
}

function openTeamDetailDialog() {
  teamDetailDialogVisible.value = true;
}

function openMentorDetailDialog() {
  mentorDetailDialogVisible.value = true;
}

async function loadDetail() {
  const registerId = route.params.registerId;
  const response = await getStudentWorkDetail(registerId);
  Object.assign(detail, response?.data || {});
}

onMounted(() => {
  loadDetail();
});
</script>

<style scoped>
.work-detail-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(129, 140, 248, 0.12), transparent 26%),
    linear-gradient(180deg, #f7f8ff 0%, #f8fbff 55%, #f2f5ff 100%);
}

.work-detail-page__main {
  max-width: 1480px;
  margin: 0 auto;
  padding: 28px 32px 50px;
}

.page-toolbar {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 16px;
  margin-bottom: 22px;
}

.page-title {
  margin: 0;
  color: #111827;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.back-button,
.toolbar-action,
.subtle-action {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 0 16px;
  border-radius: 12px;
  border: 1px solid #dcdffb;
  background: #ffffff;
  color: #5b5bd6;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(99, 102, 241, 0.05);
}

.surface-card {
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid #e7e9fb;
  border-radius: 22px;
  box-shadow: 0 10px 24px rgba(99, 102, 241, 0.05);
}

.detail-top-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(0, 0.92fr);
  gap: 20px;
  margin-bottom: 20px;
}

.left-column,
.right-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.work-panel,
.content-card,
.side-info-card,
.review-panel {
  padding: 20px 22px 18px;
}

.card-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.card-head--between {
  justify-content: space-between;
}

.card-head__title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-icon {
  width: 34px;
  height: 34px;
  border-radius: 11px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eef1ff, #e2dfff);
  color: #5b5bd6;
  font-size: 16px;
}

.card-head h2 {
  margin: 0;
  color: #3d43b8;
  font-size: 18px;
  font-weight: 800;
}

.artifact-layout {
  display: grid;
  grid-template-columns: 148px 1fr;
  gap: 20px;
  align-items: center;
}

.artifact-cover__icon {
  width: 138px;
  height: 132px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0ecff, #e7e1ff);
  color: #6d4aff;
  font-size: 54px;
}

.artifact-title {
  margin: 0;
  color: #18212f;
  font-size: 18px;
  font-weight: 800;
}

.artifact-summary {
  margin: 8px 0 14px;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.8;
}

.artifact-meta-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.meta-card,
.summary-chip {
  min-height: 68px;
  padding: 13px 15px;
  border-radius: 12px;
  background: linear-gradient(180deg, #fbfbff 0%, #f4f6ff 100%);
  border: 1px solid #f0f1fd;
}

.meta-label,
.summary-chip span {
  display: block;
  color: #6b7280;
  font-size: 12px;
  font-weight: 700;
}

.meta-card strong,
.summary-chip strong {
  display: block;
  margin-top: 10px;
  color: #1f2937;
  font-size: 15px;
  font-weight: 800;
}

.status-text.is-pending,
.summary-chip strong.is-pending,
.review-badge.pending {
  color: #5476ff;
}

.status-text.is-reviewing,
.summary-chip strong.is-reviewing {
  color: #5476ff;
}

.status-text.is-reviewed,
.summary-chip strong.is-reviewed,
.review-badge.reviewed {
  color: #56c596;
}

.content-card__rows {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.content-item {
  display: grid;
  grid-template-columns: 92px 1fr;
  gap: 16px;
  align-items: start;
}

.content-item__label {
  color: #6b7280;
  font-size: 14px;
  font-weight: 700;
  padding-top: 2px;
}

.content-item__value {
  margin: 0;
  color: #5f6877;
  font-size: 14px;
  line-height: 1.95;
}

.keyword-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-pill {
  height: 28px;
  padding: 0 12px;
  border-radius: 12px;
  background: #f3f4ff;
  color: #6d6fc7;
  font-size: 13px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}

.attachment-stack {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 336px;
}

.attachment-card {
  display: grid;
  grid-template-columns: 24px 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 11px 13px;
  border-radius: 12px;
  border: 1px solid #ececff;
  background: #ffffff;
  color: #5b5bd6;
  cursor: pointer;
}

.attachment-type {
  font-size: 20px;
}

.attachment-meta strong {
  display: block;
  color: #374151;
  font-size: 14px;
  font-weight: 700;
}

.attachment-meta span {
  display: block;
  margin-top: 3px;
  color: #9aa3b2;
  font-size: 12px;
}

.side-info-card {
  min-height: 240px;
}

.team-detail-grid,
.mentor-detail-grid {
  display: grid;
  grid-template-columns: 100px 1fr;
  row-gap: 16px;
  column-gap: 16px;
  padding: 4px 2px 0;
}

.detail-key {
  color: #6b7280;
  font-size: 14px;
  font-weight: 700;
}

.detail-value {
  color: #1f2937;
  font-size: 14px;
  font-weight: 700;
}

.leader-inline-name {
  color: #495264;
}

.team-member-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.team-member-chip {
  height: 30px;
  padding: 0 12px;
  border-radius: 12px;
  background: #f1efff;
  color: #6b67d8;
  font-size: 13px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}

.review-primary {
  padding-bottom: 22px;
}

.review-summary-bar {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.review-score-table {
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #ececff;
  background: #ffffff;
}

.review-table__head,
.review-table__row {
  display: grid;
  grid-template-columns: 1fr 1.2fr 0.9fr 0.7fr 1fr 1.6fr;
  gap: 12px;
  align-items: center;
  padding: 14px 16px;
}

.review-table__head {
  background: #fbfbff;
  color: #6b7280;
  font-size: 12px;
  font-weight: 800;
}

.review-table__row {
  border-top: 1px solid #f1f2fd;
  color: #4b5563;
  font-size: 13px;
}

.reviewer-name {
  color: #1f2937;
  font-weight: 700;
}

.reviewer-unit,
.review-opinion {
  line-height: 1.7;
}

.review-score {
  color: #374151;
  font-size: 14px;
  font-weight: 700;
}

.review-badge {
  height: 26px;
  padding: 0 11px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #eef2ff;
  font-size: 12px;
  font-weight: 800;
}

.review-badge.reviewed {
  background: #eafaf3;
}

.review-table__empty,
.empty-inline {
  color: #98a2b3;
  font-size: 13px;
}

.detail-dialog :deep(.el-dialog) {
  border-radius: 18px;
}

.dialog-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dialog-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.dialog-summary__item,
.member-detail-card,
.mentor-detail-card {
  border: 1px solid #ececff;
  background: #fdfdff;
  border-radius: 14px;
  padding: 14px 16px;
}

.dialog-label {
  display: block;
  color: #6b7280;
  font-size: 12px;
  font-weight: 700;
}

.dialog-summary__item strong {
  display: block;
  margin-top: 8px;
  color: #1f2937;
  font-size: 14px;
  font-weight: 700;
}

.member-dialog-list,
.mentor-dialog-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.member-detail-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.member-detail-card__top strong,
.mentor-detail-card strong {
  color: #1f2937;
  font-size: 15px;
  font-weight: 800;
}

.member-detail-grid,
.mentor-detail-grid-card {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 18px;
  color: #5f6877;
  font-size: 13px;
  line-height: 1.7;
  margin-top: 10px;
}

@media (max-width: 1180px) {
  .detail-top-grid,
  .artifact-meta-cards,
  .review-summary-bar,
  .dialog-summary {
    grid-template-columns: 1fr;
  }

  .review-table__head,
  .review-table__row,
  .artifact-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .work-detail-page__main {
    padding: 18px 14px 36px;
  }

  .page-toolbar,
  .team-detail-grid,
  .mentor-detail-grid,
  .content-item,
  .member-detail-grid,
  .mentor-detail-grid-card {
    grid-template-columns: 1fr;
  }

  .page-title {
    font-size: 18px;
  }

  .work-panel,
  .content-card,
  .side-info-card,
  .review-panel {
    padding: 16px;
  }
}
</style>
