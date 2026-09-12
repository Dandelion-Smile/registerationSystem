<template>
  <div class="team-detail-page">
    <nav class="team-detail-nav">
      <div class="team-detail-nav__brand">
        <div class="team-detail-nav__logo">
          <i class="fa fa-trophy"></i>
        </div>
        <div class="team-detail-nav__brand-text">
          <span class="team-detail-nav__title">“智启赛途”</span>
          <span class="team-detail-nav__subtitle">高校竞赛AI智驭平台</span>
        </div>
      </div>

      <el-dropdown trigger="click" placement="bottom-end" @command="handleUserCommand">
        <button class="user-menu-trigger">
          <div class="user-menu-meta">
            <p>系统管理员</p>
            <p>{{ userStore.name || "admin" }}</p>
          </div>
          <div class="user-menu-avatar">
            <i class="fa fa-user-circle"></i>
          </div>
          <i class="fa fa-chevron-down user-menu-arrow"></i>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <i class="fa fa-user-circle-o mr-2"></i>
              <span>个人中心</span>
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <i class="fa fa-sign-out mr-2"></i>
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </nav>

    <div class="team-detail-shell">
      <div class="team-detail-backbar">
        <button class="return-button" @click="router.back()">
          <i class="fa fa-arrow-left"></i>
          <span>返回</span>
        </button>
        <h1 class="page-heading">队伍作品详情</h1>
      </div>

      <div class="admin-detail-grid">
        <section class="summary-row">
          <article class="archive-hero">
            <div class="archive-hero__eyebrow">
              <span class="eyebrow-dot"></span>
              <span>Detailed Archives</span>
            </div>
            <h2 class="archive-hero__title">{{ team.teamName || "加载中..." }}</h2>

            <div class="archive-hero__chips">
              <div class="hero-chip hero-chip--purple">
                <i class="fa fa-user-circle"></i>
                <span>负责人: <strong>{{ leaderName }}</strong></span>
              </div>
              <div class="hero-chip hero-chip--green">
                <i class="fa fa-users"></i>
                <span>成员: <strong>{{ memberCount }} 人</strong></span>
              </div>
              <div class="hero-chip hero-chip--yellow">
                <i class="fa fa-book"></i>
                <span>作品: <strong>《{{ team.workName || "无" }}》</strong></span>
              </div>
              <div class="hero-chip hero-chip--orange">
                <i class="fa fa-star"></i>
                <span>综合平均分: <strong>{{ displayAvgScore }}</strong></span>
              </div>
            </div>

            <div class="archive-meta-grid">
              <div class="archive-meta-card">
                <span>报名时间</span>
                <strong>{{ formatTime(team.registerTime) }}</strong>
              </div>
              <div class="archive-meta-card">
                <span>作品状态</span>
                <strong :class="statusClass">{{ workStatusText }}</strong>
              </div>
              <div class="archive-meta-card">
                <span>作品编号</span>
                <strong>{{ workCode }}</strong>
              </div>
            </div>
          </article>

          <article class="detail-card team-info-card">
            <div class="detail-card__title detail-card__title--indigo">队伍信息</div>
            <div class="detail-card__rows">
              <div class="detail-row">
                <span>队伍名称</span>
                <strong>{{ team.teamName || "—" }}</strong>
              </div>
              <div class="detail-row">
                <span>队长</span>
                <strong>{{ leaderName }}</strong>
              </div>
              <div class="detail-row detail-row--members">
                <span>队员</span>
                <div class="member-tags">
                  <span
                    v-for="(member, index) in teamMembers"
                    :key="`team-member-${index}`"
                    class="member-tag"
                  >
                    {{ member.name || member }}
                  </span>
                </div>
              </div>
              <div class="detail-row">
                <span>所属院校</span>
                <strong>{{ schoolName }}</strong>
              </div>
              <div class="detail-row">
                <span>联系电话</span>
                <strong>{{ teamPhone }}</strong>
              </div>
            </div>
          </article>
        </section>

        <section class="detail-row-two">
          <article class="detail-card basic-info-card">
            <div class="detail-card__title detail-card__title--purple">基本信息</div>
            <div class="detail-card__rows">
              <div class="detail-row">
                <span>指导老师</span>
                <strong>{{ teacherName }}</strong>
              </div>
              <div class="detail-row">
                <span>负责人</span>
                <strong>{{ leaderName }}</strong>
              </div>
            </div>
          </article>

          <article class="detail-card work-info-card">
            <div class="detail-card__title detail-card__title--green">作品信息</div>
            <div class="work-block">
              <div class="work-field">
                <span>作品名称</span>
                <strong>{{ team.workName || "无" }}</strong>
              </div>
              <div class="work-field work-field--summary">
                <span>作品简介</span>
                <p>{{ team.workDescription || "暂无作品简介" }}</p>
              </div>
              <div class="work-field">
                <span>作品附件</span>
                <div class="attachment-list">
                  <button
                    v-for="file in team.attachments"
                    :key="file.name"
                    class="attachment-item"
                    type="button"
                    @click="openAttachment(file.url)"
                  >
                    <i class="fa fa-file-text-o"></i>
                    <div>
                      <strong>{{ file.name }}</strong>
                      <span>{{ file.size || "2.45 MB" }}</span>
                    </div>
                    <i class="fa fa-download"></i>
                  </button>
                  <div v-if="!team.attachments.length" class="empty-text">暂无作品附件</div>
                </div>
              </div>
            </div>
          </article>

          <article class="detail-card mentor-info-card">
            <div class="detail-card__title detail-card__title--blue">指导老师信息</div>
            <div class="detail-card__rows">
              <div class="detail-row">
                <span>指导老师</span>
                <strong>{{ teacherName }}</strong>
              </div>
              <div class="detail-row">
                <span>所在单位</span>
                <strong>{{ teacherUnit }}</strong>
              </div>
              <div class="detail-row">
                <span>联系电话</span>
                <strong>{{ teacherPhone }}</strong>
              </div>
            </div>
          </article>
        </section>

        <section class="detail-row-three">
          <article class="detail-card member-list-card">
            <div class="detail-card__title detail-card__title--yellow">成员列表</div>
            <div class="member-list">
              <div
                v-for="(member, index) in teamMembers"
                :key="`member-list-${index}`"
                class="member-list__item"
              >
                <strong>{{ member.name || member }}</strong>
                <i class="fa fa-check-circle"></i>
              </div>
              <div v-if="!teamMembers.length" class="empty-text">暂无成员信息</div>
            </div>
          </article>

          <article class="detail-card score-overview">
            <div class="score-overview__head">
              <div class="detail-card__title detail-card__title--violet">评分信息</div>
              <div class="score-overview__badge">Latest Score</div>
            </div>
            <div class="score-overview__grid">
              <div class="score-stat-card">
                <span>平均分数</span>
                <strong>{{ displayAvgScore }}</strong>
              </div>
              <div class="score-stat-card">
                <span>评审轮次</span>
                <strong>{{ reviewRoundCount }}</strong>
              </div>
            </div>
          </article>

          <article class="detail-card teacher-score-table">
            <div class="detail-card__title detail-card__title--orange">教师评分详情</div>
            <div class="teacher-score-table__wrap">
              <div class="teacher-score-table__head">
                <span>教师名称</span>
                <span>评分</span>
                <span>评审建议</span>
              </div>
              <div
                v-for="(score, index) in teamScores"
                :key="`teacher-score-${index}`"
                class="teacher-score-table__row"
              >
                <div class="teacher-cell">
                  <span class="teacher-avatar">{{ getTeacherInitial(score.reviewer_name) }}</span>
                  <strong>{{ score.reviewer_name || "未命名教师" }}</strong>
                </div>
                <div class="score-pill">{{ formatScore(score.score) }}</div>
                <div class="teacher-comment">{{ score.comment || "教师未留下详细评语" }}</div>
              </div>
              <div v-if="!teamScores.length" class="teacher-score-table__empty">目前暂无评审档案数据记录</div>
            </div>
          </article>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { useRoute, useRouter } from "vue-router";
import request from "@/utils/request";
import useUserStore from "@/store/modules/user";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const team = ref({
  teamName: "",
  teamMembers: "[]",
  workName: "",
  workDescription: "",
  avgScore: 0,
  registerId: "",
  registerTime: "",
  teacherName: "",
  teacherUnit: "",
  teacherPhone: "",
  schoolName: "",
  phone: "",
  workCode: "",
  statusText: "",
  attachments: [],
});

const teamScores = ref([]);

const parseMembers = (source) => {
  if (!source) return [];
  try {
    const parsed = typeof source === "string" ? JSON.parse(source) : source;
    return Array.isArray(parsed) ? parsed : [];
  } catch (error) {
    return [];
  }
};

const teamMembers = computed(() => parseMembers(team.value.teamMembers));

const memberCount = computed(() => teamMembers.value.length);

const leaderName = computed(() => {
  if (!teamMembers.value.length) return "未知";
  const leader =
    teamMembers.value.find((member) => member?.role === "leader") ||
    teamMembers.value[0];
  return leader?.name || leader || "未知";
});

const schoolName = computed(
  () => team.value.schoolName || team.value.collegeName || "某某大学"
);

const teacherName = computed(() => team.value.teacherName || "未指定");

const teacherUnit = computed(
  () => team.value.teacherUnit || "某某大学 计算机学院"
);

const teacherPhone = computed(() => team.value.teacherPhone || "188****6666");

const teamPhone = computed(() => team.value.phone || "188****8888");

const workCode = computed(() => {
  if (team.value.workCode) return team.value.workCode;
  if (team.value.registerId) {
    return `P${String(team.value.registerId).padStart(10, "0")}`;
  }
  return "P20220610001";
});

const displayAvgScore = computed(() => {
  const value = Number(team.value.avgScore || 0);
  return Number.isFinite(value) ? value.toFixed(2) : "0.00";
});

const reviewRoundCount = computed(() => teamScores.value.length);

const workStatusText = computed(() => {
  if (team.value.statusText) return team.value.statusText;
  const reviewedCount = teamScores.value.filter((item) => item.score !== null && item.score !== undefined).length;
  if (reviewedCount > 0 && reviewedCount === teamScores.value.length) return "已评审";
  if (reviewedCount > 0) return "评审中";
  if (teamScores.value.length > 0) return "未评审";
  return "已提交";
});

const statusClass = computed(() => {
  if (workStatusText.value === "已评审") return "status-reviewed";
  if (workStatusText.value === "评审中") return "status-reviewing";
  if (workStatusText.value === "未评审") return "status-pending";
  return "status-done";
});

const formatTime = (value) => {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  const pad = (num) => String(num).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
    date.getDate()
  )} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(
    date.getSeconds()
  )}`;
};

const formatScore = (value) => {
  if (value === null || value === undefined || value === "") return "--";
  const numeric = Number(value);
  return Number.isFinite(numeric) ? `${numeric} 分` : String(value);
};

const getTeacherInitial = (name) => {
  const value = String(name || "教").trim();
  return value ? value.charAt(0) : "教";
};

const normalizeAttachments = (detail) => {
  const files = [];
  if (detail?.pptPath) {
    files.push({
      name: detail.pptName || "作品说明书.ppt",
      size: "2.45 MB",
      url: detail.pptPath,
    });
  }
  if (detail?.pdfPath) {
    const names = String(detail.pdfNames || "").split("|");
    String(detail.pdfPath)
      .split("|")
      .filter(Boolean)
      .forEach((url, index) => {
        files.push({
          name: names[index] || url.split("/").pop() || `附件${index + 1}`,
          size: "2.45 MB",
          url,
        });
      });
  }
  return files;
};

const openAttachment = (url) => {
  if (!url) return;
  window.open(url, "_blank", "noopener");
};

const fetchDetail = async () => {
  const { registerId } = route.params;
  if (!registerId) return;

  try {
    const [teamRes, scoreRes, materialRes] = await Promise.all([
      request.get(`/admin/competition/team-detail/${registerId}`),
      request.get(`/admin/competition/team-scores/${registerId}`),
      request
        .get(`/student/competition/material/${registerId}`)
        .catch(() => ({ data: null })),
    ]);

    const detail = teamRes?.data || {};
    team.value = {
      ...detail,
      schoolName: detail.schoolName || detail.collegeName || "",
      teacherUnit: detail.teacherUnit || "",
      teacherPhone: detail.teacherPhone || "",
      phone: detail.phone || "",
      attachments: normalizeAttachments(materialRes?.data),
      workCode: detail?.registerId
        ? `P${String(detail.registerId).padStart(10, "0")}`
        : "",
      statusText: detail.statusText || detail.status_label || "",
    };
    teamScores.value = Array.isArray(scoreRes?.data) ? scoreRes.data : [];
  } catch (error) {
    console.error("加载队伍作品详情失败:", error);
  }
};

const handleUserCommand = (command) => {
  if (command === "profile") {
    router.push("/sys-admin/profile");
    return;
  }

  if (command === "logout") {
    ElMessageBox.confirm("确定注销并退出系统吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    })
      .then(() => userStore.logOut())
      .then(() => {
        location.href = "/index";
      })
      .catch(() => {});
  }
};

onMounted(() => {
  fetchDetail();
});
</script>

<style scoped>
.team-detail-page {
  min-height: 100vh;
  background: #f7f8fc;
}

.team-detail-nav {
  height: 64px;
  padding: 0 32px;
  background: #722ed1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4px 12px rgba(114, 46, 209, 0.12);
}

.team-detail-nav__brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.team-detail-nav__logo {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #facc15;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 1px 2px rgba(255, 255, 255, 0.25);
}

.team-detail-nav__brand-text {
  display: flex;
  flex-direction: column;
}

.team-detail-nav__title {
  color: #ffffff;
  font-size: 18px;
  font-weight: 900;
  letter-spacing: 0.08em;
  line-height: 1;
}

.team-detail-nav__subtitle {
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.62);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.team-detail-shell {
  max-width: 1560px;
  margin: 0 auto;
  padding: 22px 24px 20px;
}

.team-detail-backbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 18px;
}

.return-button {
  height: 34px;
  padding: 0 16px;
  border: 1px solid #d8def0;
  border-radius: 10px;
  background: #ffffff;
  color: #2f3b52;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 700;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.04);
  cursor: pointer;
}

.user-menu-trigger {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 6px 14px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.1);
  cursor: pointer;
}

.user-menu-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.user-menu-meta p:first-child {
  margin: 0;
  color: #ffffff;
  font-size: 11px;
  font-weight: 800;
  line-height: 1;
}

.user-menu-meta p:last-child {
  margin: 4px 0 0;
  color: rgba(255, 255, 255, 0.58);
  font-size: 10px;
  font-weight: 700;
  line-height: 1;
}

.user-menu-avatar {
  width: 30px;
  height: 30px;
  border-radius: 999px;
  background: #ffffff;
  color: #722ed1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(255, 255, 255, 0.18);
}

.user-menu-arrow {
  color: rgba(255, 255, 255, 0.82);
  font-size: 10px;
}

.page-heading {
  margin: 0;
  color: #101828;
  font-size: 21px;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.admin-detail-grid {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.summary-row {
  display: grid;
  grid-template-columns: minmax(0, 1.62fr) minmax(0, 0.94fr);
  gap: 18px;
}

.detail-row-two,
.detail-row-three {
  display: grid;
  grid-template-columns: minmax(0, 0.86fr) minmax(0, 1fr) minmax(0, 1fr);
  gap: 18px;
}

.archive-hero,
.detail-card {
  background: #ffffff;
  border: 1px solid #e8ebf5;
  border-radius: 14px;
  box-shadow: 0 6px 20px rgba(37, 46, 72, 0.04);
}

.archive-hero {
  padding: 20px 22px 18px;
}

.archive-hero__eyebrow {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #98a2b3;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.eyebrow-dot {
  width: 7px;
  height: 18px;
  border-radius: 4px;
  background: #7c3aed;
}

.archive-hero__title {
  margin: 10px 0 16px;
  color: #111827;
  font-size: 28px;
  line-height: 1.05;
  font-weight: 900;
  letter-spacing: -0.03em;
}

.archive-hero__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.hero-chip {
  min-height: 34px;
  padding: 7px 14px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  font-weight: 700;
  border: 1px solid transparent;
}

.hero-chip strong {
  font-size: 11px;
  font-weight: 800;
}

.hero-chip--purple {
  background: #f6efff;
  border-color: #efe2ff;
  color: #6f35db;
}

.hero-chip--green {
  background: #ebfaef;
  border-color: #d9f5e2;
  color: #1d9f55;
}

.hero-chip--yellow {
  background: #fff7df;
  border-color: #f7e8b1;
  color: #be7a0d;
}

.hero-chip--orange {
  background: #fff1e1;
  border-color: #ffe0bd;
  color: #eb7a1a;
}

.archive-meta-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  max-width: 760px;
}

.archive-meta-card,
.score-stat-card {
  min-height: 72px;
  padding: 13px 15px;
  background: #fbfcff;
  border: 1px solid #eef1f7;
  border-radius: 10px;
}

.archive-meta-card span,
.score-stat-card span {
  color: #98a2b3;
  font-size: 11px;
  font-weight: 700;
}

.archive-meta-card strong {
  display: block;
  margin-top: 9px;
  color: #243041;
  font-size: 12px;
  font-weight: 800;
  line-height: 1.55;
  word-break: break-word;
}

.status-done {
  color: #23a566 !important;
}

.status-reviewed {
  color: #16a34a !important;
}

.status-reviewing {
  color: #4f46e5 !important;
}

.status-pending {
  color: #c58419 !important;
}

.detail-card {
  padding: 18px 20px;
  min-width: 0;
}

.detail-card__title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
  color: #1d2433;
  font-size: 13px;
  font-weight: 800;
}

.detail-card__title::before {
  content: "";
  width: 5px;
  height: 22px;
  border-radius: 3px;
  background: #7c3aed;
  flex: 0 0 auto;
}

.detail-card__title--green::before {
  background: #22c55e;
}

.detail-card__title--yellow::before {
  background: #f6b500;
}

.detail-card__title--orange::before {
  background: #ff7a1a;
}

.detail-card__rows {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-row {
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr);
  gap: 14px;
  align-items: start;
}

.detail-row span {
  color: #97a1b5;
  font-size: 11px;
  font-weight: 700;
  line-height: 1.7;
}

.detail-row strong {
  color: #243041;
  font-size: 11px;
  font-weight: 800;
  line-height: 1.8;
  word-break: break-word;
}

.detail-row--members {
  align-items: start;
}

.member-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.member-tag {
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: #f2f4ff;
  color: #5d62d9;
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  font-weight: 700;
  max-width: 100%;
}

.work-block {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.work-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.work-field span {
  color: #97a1b5;
  font-size: 11px;
  font-weight: 700;
}

.work-field strong {
  color: #243041;
  font-size: 11px;
  font-weight: 800;
  line-height: 1.8;
}

.work-field--summary {
  padding: 14px 16px;
  border: 1px solid #eef1f7;
  border-radius: 10px;
  background: #fafbff;
}

.work-field--summary p {
  margin: 0;
  color: #667085;
  font-size: 11px;
  line-height: 1.8;
  word-break: break-word;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.attachment-item {
  width: 100%;
  padding: 12px 14px;
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr) 16px;
  gap: 10px;
  align-items: center;
  text-align: left;
  border: 1px solid #e7eaf5;
  border-radius: 10px;
  background: #ffffff;
  color: #5d5bd8;
  cursor: pointer;
}

.attachment-item strong {
  display: block;
  color: #243041;
  font-size: 11px;
  font-weight: 800;
  line-height: 1.5;
  word-break: break-word;
}

.attachment-item span {
  display: block;
  margin-top: 2px;
  color: #98a2b3;
  font-size: 10px;
  font-weight: 600;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.member-list__item {
  padding: 13px 15px;
  border: 1px solid #edf0f7;
  border-radius: 10px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.member-list__item strong {
  color: #243041;
  font-size: 11px;
  font-weight: 800;
  line-height: 1.5;
}

.member-list__item i {
  color: #4ade80;
  font-size: 16px;
}

.score-overview__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.score-overview__head .detail-card__title {
  margin-bottom: 0;
}

.score-overview__badge {
  padding: 5px 12px;
  border-radius: 999px;
  background: #f5ecff;
  color: #7c3aed;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.score-overview__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.score-stat-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  text-align: center;
}

.score-stat-card strong {
  display: block;
  margin-top: 16px;
  color: #1f2937;
  font-size: 22px;
  line-height: 1;
  font-weight: 900;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace;
}

.score-stat-card:first-child strong {
  color: #6f35db;
}

.teacher-score-table__wrap {
  border: 1px solid #edf0f7;
  border-radius: 10px;
  overflow: hidden;
}

.teacher-score-table__head,
.teacher-score-table__row {
  display: grid;
  grid-template-columns: minmax(0, 0.92fr) 92px minmax(0, 1.28fr);
  gap: 14px;
  align-items: center;
}

.teacher-score-table__head {
  padding: 13px 15px;
  background: #fbfcff;
}

.teacher-score-table__head span {
  color: #98a2b3;
  font-size: 11px;
  font-weight: 700;
}

.teacher-score-table__row {
  padding: 14px 15px;
  background: #ffffff;
  border-top: 1px solid #edf0f7;
}

.teacher-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.teacher-avatar {
  width: 28px;
  height: 28px;
  flex: 0 0 auto;
  border-radius: 999px;
  background: #f3f4f8;
  color: #7c3aed;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 800;
}

.teacher-cell strong {
  color: #243041;
  font-size: 11px;
  font-weight: 800;
  line-height: 1.6;
  word-break: break-word;
}

.score-pill {
  justify-self: start;
  min-width: 68px;
  padding: 6px 10px;
  border-radius: 999px;
  background: #fff7dd;
  color: #c1841a;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 800;
}

.teacher-comment {
  color: #667085;
  font-size: 11px;
  line-height: 1.8;
  word-break: break-word;
}

.teacher-score-table__empty,
.empty-text {
  padding: 26px 10px;
  text-align: center;
  color: #98a2b3;
  font-size: 11px;
  font-weight: 600;
}

@media (max-width: 1500px) {
  .summary-row {
    grid-template-columns: 1fr;
  }

  .detail-row-two,
  .detail-row-three {
    grid-template-columns: 1fr 1fr;
  }

  .mentor-info-card,
  .teacher-score-table {
    grid-column: 1 / -1;
  }
}

@media (max-width: 1024px) {
  .team-detail-nav {
    padding: 0 16px;
  }

  .detail-row-two,
  .detail-row-three,
  .score-overview__grid,
  .archive-meta-grid {
    grid-template-columns: 1fr;
  }

  .team-detail-backbar {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .team-detail-shell {
    padding: 18px 14px 20px;
  }
}

@media (max-width: 768px) {
  .user-menu-meta {
    display: none;
  }
}
</style>
