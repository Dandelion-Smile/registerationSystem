<template>
  <div class="competition-detail-page">
    <StudentNavbar />

    <section class="page-hero">
      <div class="hero-container">
        <div class="hero-main">
          <div class="hero-title">
            <i class="fas fa-trophy"></i>
            <h1>{{ currentCompetition.competitionName || "竞赛详情" }}</h1>
            <el-tag
              v-if="currentCompetition.inRegisterPeriod"
              size="small"
              type="success"
              effect="dark"
              class="hero-badge"
            >
              {{ currentCompetition.inRegisterPeriod ? "报名中" : "非报名期" }}
            </el-tag>
          </div>
          <p v-if="currentCompetition.description" class="hero-desc">
            {{ currentCompetition.description }}
          </p>
          <div class="hero-meta">
            <el-tag
              effect="plain"
              type="info"
              v-if="currentCompetition.competitionType"
              class="hero-chip"
            >
              {{ currentCompetition.competitionType }}
            </el-tag>
            <el-tag
              effect="plain"
              type="info"
              class="hero-chip"
              v-if="
                currentCompetition.registerStartTime &&
                currentCompetition.registerEndTime
              "
            >
              报名时间：{{ formatDate(currentCompetition.registerStartTime) }} -
              {{ formatDate(currentCompetition.registerEndTime) }}
            </el-tag>
          </div>
        </div>
        <div class="hero-actions">
          <el-button
            v-if="!registerStatus.registered"
            type="primary"
            @click="goToRegister"
            >立即报名</el-button
          >
          <el-button
            v-else
            type="success"
            class="premium-enter-btn"
            @click="goToRegister('upload')"
          >
            <i class="fas fa-cloud-upload-alt"></i>
            进入报名
          </el-button>
        </div>
      </div>
    </section>

    <!-- 竞赛详情内容 -->
    <div class="detail-content">
      <div class="detail-header"></div>

      <!-- 宣传图片展示区域 -->
      <div
        v-if="currentCompetition.posterImage || currentCompetition.bannerImage"
        class="external-content"
      >
        <div class="poster-container">
          <img
            :src="previewImageSrc(currentCompetition.posterImage || currentCompetition.bannerImage)"
            class="poster-image"
            @click="openExternalLink"
            alt="竞赛宣传图"
          />
        </div>
      </div>

      <div
        v-else-if="currentCompetition.competitionLink"
        class="external-content"
      >
        <div class="external-header">
          <h3>参考资料</h3>
          <div class="header-actions">
            <el-tooltip
              content="部分网站出于安全考虑可能无法在框架中显示"
              placement="top"
            >
              <el-button
                type="primary"
                size="small"
                @click="togglePreviewMode"
                :style="{ '--el-color-primary': '#7c4dff' }"
              >
                <i class="fas fa-exchange-alt"></i>
                {{ useIframe ? "切换到卡片模式" : "尝试预览模式" }}
              </el-button>
            </el-tooltip>
          </div>
        </div>

        <!-- iframe预览模式 -->
        <div v-if="useIframe" class="iframe-container">
          <div class="iframe-header">
            <div class="iframe-info">
              <i class="fas fa-link"></i>
              <span>{{
                extractDomain(currentCompetition.competitionLink)
              }}</span>
            </div>
            <div class="iframe-actions">
              <el-button type="text" size="small" @click="openExternalLink">
                <i class="fas fa-external-link-alt"></i> 在新窗口打开
              </el-button>
              <el-button type="text" size="small" @click="copyLink">
                <i class="fas fa-copy"></i> 复制链接
              </el-button>
            </div>
          </div>
          <div class="iframe-wrapper">
            <div v-if="iframeLoading" class="iframe-loading">
              <el-skeleton :rows="8" animated />
            </div>
            <iframe
              v-else-if="!iframeError"
              ref="iframeRef"
              :src="currentCompetition.competitionLink"
              frameborder="0"
              @error="handleIframeError"
              @load="handleIframeLoad"
            ></iframe>
            <div v-else class="iframe-error">
              <i class="fas fa-exclamation-triangle"></i>
              <div>
                <div class="error-title">无法加载外部页面</div>
                <div class="error-desc">
                  该网站出于安全考虑，不允许在其他网站中嵌入显示
                </div>
                <div class="error-actions">
                  <el-button
                    type="primary"
                    size="small"
                    @click="openExternalLink"
                    :style="{ '--el-color-primary': '#7c4dff' }"
                  >
                    <i class="fas fa-external-link-alt"></i> 在新窗口查看
                  </el-button>
                  <el-button size="small" @click="togglePreviewMode">
                    <i class="fas fa-th-large"></i> 显示卡片模式
                  </el-button>
                  <el-button type="text" size="small" @click="copyLink">
                    <i class="fas fa-copy"></i> 复制链接
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 链接预览卡片模式 -->
        <div v-else class="link-preview-card">
          <div class="link-preview-header">
            <div class="link-icon-wrapper">
              <i class="fas fa-link link-icon"></i>
            </div>
            <div class="link-preview-info">
              <div class="link-preview-title">
                {{ extractDomain(currentCompetition.competitionLink) }}
              </div>
              <div class="link-preview-url">
                {{ currentCompetition.competitionLink }}
              </div>
            </div>
          </div>
          <div class="link-actions">
            <el-button
              type="primary"
              size="small"
              @click="openExternalLink"
              :style="{ '--el-color-primary': '#7c4dff' }"
            >
              <i class="fas fa-external-link-alt"></i> 访问链接
            </el-button>
            <el-button
              type="text"
              size="small"
              @click="copyLink"
              :style="{ '--el-color-primary': '#7c4dff' }"
            >
              <i class="fas fa-copy"></i> 复制
            </el-button>
          </div>
        </div>
      </div>

      <!-- 自定义内容区域 -->
      <div class="custom-content">
        <h3>竞赛详情</h3>
        <div class="detail-info">
          <!-- 竞赛基本信息 -->
          <div class="info-item">
            <label>竞赛类型：</label>
            <span>{{ currentCompetition.competitionType || "未设置" }}</span>
          </div>

          <div class="info-item">
            <label>报名时间：</label>
            <span>
              {{ formatDate(currentCompetition.registerStartTime) }} -
              {{ formatDate(currentCompetition.registerEndTime) }}
            </span>
          </div>

          <div class="info-item">
            <label>竞赛描述：</label>
            <div class="competition-description">
              {{ currentCompetition.description || "暂无描述" }}
            </div>
          </div>
          <!-- ...其他详情信息 -->
        </div>

        <div class="info-item">
          <label>通知公告：</label>
          <div class="competition-notice">
            {{ currentCompetition.announcement || "暂无通知公告" }}
          </div>
        </div>

        <!-- 已报名提示与队伍信息 -->
        <div v-if="registerStatus.registered" class="team-info">
          <h4>我的队伍</h4>
          <div class="team-name">{{ registerStatus.teamName }}</div>
          <div v-if="registerStatus.reviewStatusText" class="team-review-status">
            当前状态：{{ registerStatus.reviewStatusText }}
          </div>
          <div class="team-members">
            <h5>队员列表</h5>
            <ul>
              <li
                v-for="(member, index) in registerStatus.teamMembers"
                :key="index"
              >
                {{ member.name }} ({{ member.major }})
                <span v-if="member.isLeader" class="leader-tag">队长</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="CompetitionDetail">
import StudentNavbar from "@/components/StudentNavbar.vue";
import { ref, onMounted, watch, onUnmounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { getCompetitionStatus, getCompetitionDetail } from "@/api/competition";
import { getUserProfile } from "@/api/system/user";
import { formatDate } from "@/utils";
import { previewImageSrc } from "@/utils/media";
import {
  isStudentRegistrationProfileComplete,
  STUDENT_PROFILE_INCOMPLETE_MESSAGE,
} from "@/utils/studentProfile";
import useUserStore from "@/store/modules/user";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// 从环境变量获取基础URL
const baseUrl = import.meta.env.VITE_APP_BASE_API;

// 状态定义
const currentCompetition = ref({
  // 初始值
});
const registerStatus = ref({
  registered: false,
  teamName: "",
  teamMembers: [],
  registerId: null,
  reviewStatusText: "",
});
const loading = ref(false);
const useIframe = ref(false); // 默认为卡片模式，避免直接显示错误
const iframeError = ref(false); // iframe加载错误状态
const iframeLoading = ref(false); // iframe加载中状态
const iframeRef = ref(null); // iframe DOM引用
let iframeTimeout = null; // 超时计时器

// 获取竞赛详情 - 修复：将本地函数重命名，避免与导入的API函数冲突
const fetchCompetitionDetail = async (competitionId) => {
  loading.value = true;
  try {
    // 调用真实API获取竞赛详情
    const response = await getCompetitionDetail(competitionId);
    if (response && response.data) {
      currentCompetition.value = response.data;
      // 重置iframe相关状态
      iframeError.value = false;
      iframeLoading.value = false;
    } else {
      ElMessage.error("获取竞赛详情失败");
    }
  } catch (error) {
    console.error("获取竞赛详情出错:", error);
    ElMessage.error("获取竞赛详情失败");
  } finally {
    loading.value = false;
  }
};

// 获取报名状态
const getRegisterStatus = (competitionId) => {
  getCompetitionStatus(competitionId)
    .then((res) => {
      const data = res?.data || {};
      registerStatus.value.registered = !!data.registered;
      registerStatus.value.teamName = data.teamName || "";
      registerStatus.value.registerId = data.registerId || null;
      registerStatus.value.reviewStatusText = data.reviewStatusText || "";
    })
    .catch(() => {
      registerStatus.value.registered = false;
    });
};

// 提取域名的方法
const extractDomain = (url) => {
  try {
    const domain = new URL(url).hostname;
    return domain.replace("www.", "");
  } catch {
    return "外部链接";
  }
};

// 直接打开外部链接的方法
const openExternalLink = () => {
  window.open(currentCompetition.value.competitionLink, "_blank");
};

// 统一使用 @/utils/media.previewImageSrc

// 复制链接方法
const copyLink = () => {
  navigator.clipboard
    .writeText(currentCompetition.value.competitionLink)
    .then(() => {
      ElMessage.success("链接已复制到剪贴板");
    })
    .catch(() => {
      ElMessage.error("复制失败，请手动复制");
    });
};

// iframe错误处理
const handleIframeError = () => {
  clearTimeout(iframeTimeout);
  iframeError.value = true;
  iframeLoading.value = false;
  console.log("iframe加载失败，可能是由于安全策略限制");
};

// iframe加载成功处理
const handleIframeLoad = () => {
  clearTimeout(iframeTimeout);
  iframeError.value = false;
  iframeLoading.value = false;
};

// 切换预览模式
const togglePreviewMode = () => {
  useIframe.value = !useIframe.value;

  // 如果切换到iframe模式，重置状态并开始加载
  if (useIframe.value) {
    iframeError.value = false;
    iframeLoading.value = true;

    // 设置超时检测
    iframeTimeout = setTimeout(() => {
      // 检查iframe是否真的加载成功
      try {
        const iframe = iframeRef.value;
        if (iframe && iframe.contentDocument && iframe.contentDocument.body) {
          // 检查是否是错误页面
          const bodyHTML = iframe.contentDocument.body.innerHTML.toLowerCase();
          if (
            bodyHTML.includes("拒绝连接") ||
            bodyHTML.includes("connection refused")
          ) {
            handleIframeError();
          }
        } else {
          handleIframeError();
        }
      } catch (error) {
        handleIframeError();
      }
    }, 5000); // 5秒超时
  }
};

// 页面初始化
onMounted(() => {
  const competitionId = route.params.id;
  if (competitionId) {
    fetchCompetitionDetail(competitionId);
    getRegisterStatus(competitionId);
  }
});

// 监听路由参数变化
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      fetchCompetitionDetail(newId);
      getRegisterStatus(newId);
    }
  },
);

// 组件销毁时清理
onUnmounted(() => {
  if (iframeTimeout) {
    clearTimeout(iframeTimeout);
  }
});

// 返回列表
const goBack = () => {
  router.push("/competition");
};

async function ensureProfileCompleteBeforeRegister() {
  try {
    const response = await getUserProfile();
    if (isStudentRegistrationProfileComplete(response?.data || {})) {
      return true;
    }
    ElMessage.warning(STUDENT_PROFILE_INCOMPLETE_MESSAGE);
    return false;
  } catch (error) {
    ElMessage.error("获取个人信息失败，请稍后重试");
    return false;
  }
}

// 跳转到报名页面
const goToRegister = async (target = "") => {
  if (!currentCompetition.value?.inRegisterPeriod) {
    ElMessage.warning("该赛事暂不能报名！");
    return;
  }
  const canRegister = await ensureProfileCompleteBeforeRegister();
  if (!canRegister) {
    return;
  }
  router.push({
    path: `/competition/register/${currentCompetition.value.competitionId}`,
    query: {
      name: currentCompetition.value.competitionName,
      desc: currentCompetition.value.description || "",
      type: currentCompetition.value.competitionType || "",
      start: currentCompetition.value.registerStartTime || "",
      end: currentCompetition.value.registerEndTime || "",
      ...(target ? { target } : {}),
    },
  });
};

// 处理用户操作命令
const handleCommand = (command) => {
  if (command === "logout") {
    ElMessageBox.confirm("确定注销并退出系统吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    })
      .then(() => {
        userStore.logOut().then(() => {
          location.href = "/index";
        });
      })
      .catch(() => {});
  }
};
</script>

<style scoped>
/* 顶部导航栏样式 */
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
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: white;
  border-radius: 2px;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-container {
  cursor: pointer;
}

.avatar-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
}

.user-avatar-circle {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.page-hero {
  padding: 0;
  margin-bottom: 32px;
  color: #7c4dff;
  background: linear-gradient(180deg, #ffffff 0%, #f8f6ff 100%);
  width: 100%;
  border-top: 1px solid rgba(124, 77, 255, 0.2);
  border-bottom: none;
  border-radius: 0 0 24px 24px;
  position: sticky;
  top: 64px;
  z-index: 90;
  box-shadow:
    0 10px 30px rgba(124, 77, 255, 0.12),
    0 4px 12px rgba(124, 77, 255, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  overflow: hidden;
}

.hero-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 40px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

/* 调整按钮样式以适应白色背景 */
.hero-actions .el-button--primary {
  background-color: #7c4dff;
  color: #ffffff;
  border-color: #7c4dff;
  padding: 18px 36px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(124, 77, 255, 0.2);
}

.hero-actions .el-button--primary:hover {
  background-color: #6a3dd8;
  border-color: #6a3dd8;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(124, 77, 255, 0.3);
}

.hero-actions .premium-enter-btn {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%) !important;
  border: none !important;
  color: #ffffff !important;
  padding: 18px 36px !important;
  font-size: 18px !important;
  font-weight: 700 !important;
  border-radius: 14px !important;
  box-shadow: 0 10px 24px rgba(16, 185, 129, 0.28) !important;
}

.hero-actions .premium-enter-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 28px rgba(16, 185, 129, 0.34) !important;
}

.hero-actions .premium-enter-btn i {
  margin-right: 8px;
}

/* 竞赛类型和时间标签样式 */
.hero-meta .el-tag {
  border-color: rgba(124, 77, 255, 0.3);
  color: #7c4dff; /* 文字颜色为紫色 */
  background: rgba(124, 77, 255, 0.08);
  margin-right: 8px;
  font-weight: 500;
}

/* 报名状态徽章样式 */
.hero-badge {
  background-color: rgba(82, 196, 26, 0.15);
  color: #7c4dff; /* 文字颜色改为紫色 */
  border: none;
  font-weight: 600;
}

/* 竞赛描述文字样式 */
.hero-desc {
  color: #7c4dff; /* 文字颜色改为紫色 */
  margin: 12px 0 16px 0;
  opacity: 0.92;
  font-size: 14px;
  line-height: 1.6;
}

/* 图标颜色 */
.hero-title .fa-trophy {
  color: #7c4dff; /* 奖杯图标为紫色 */
  font-size: 18px;
}

/* 其他样式 */
.hero-main {
  flex: 1;
  padding-top: 10px;
}

.team-review-status {
  margin: 10px 0 14px;
  display: inline-flex;
  align-items: center;
  padding: 6px 14px;
  border-radius: 999px;
  background: #eef2ff;
  color: #4f46e5;
  font-size: 14px;
  font-weight: 600;
}

.hero-title {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: flex-start;
}

.hero-title h1 {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  color: #7c4dff; /* 确保标题文字为紫色 */
}

.hero-chip + .hero-chip {
  margin-left: 8px;
}

/* 竞赛详情内容样式 */
.detail-content {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto 40px;
}

.detail-header {
  margin-bottom: 30px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e8e8e8;
}

.detail-status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
}

.badge-open {
  background-color: #e6f7ff;
  color: #1890ff;
}

.badge-closed {
  background-color: #f6ffed;
  color: #52c41a;
}

/* 外部内容样式 */
.external-content {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f8f8f8;
  border-radius: 12px;
}

.external-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.external-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 8px;
}

/* iframe预览模式样式 */
.iframe-container {
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.iframe-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #f5f5f5;
  border-bottom: 1px solid #e0e0e0;
}

.iframe-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.iframe-actions {
  display: flex;
  gap: 8px;
}

.iframe-wrapper {
  height: 600px;
  position: relative;
}

.iframe-wrapper iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.iframe-loading {
  padding: 20px;
  height: 100%;
}

.iframe-error {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  height: 100%;
  background-color: #fafafa;
  color: #999;
  padding: 0 24px;
  text-align: center;
  flex-direction: column;
}

.iframe-error i {
  font-size: 64px;
  color: #ff9800;
  margin-bottom: 16px;
}

.error-title {
  font-size: 20px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.error-desc {
  margin-bottom: 24px;
  color: #666;
  line-height: 1.5;
  max-width: 500px;
}

.error-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

/* 链接预览卡片样式 */
.link-preview-card {
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  gap: 20px;
}

.link-preview-card:hover {
  box-shadow: 0 6px 24px rgba(124, 77, 255, 0.12);
  transform: translateY(-2px);
}

.link-preview-header {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.link-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #7c4dff 0%, #a784ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.link-icon {
  font-size: 24px;
  color: #fff;
}

.link-preview-info {
  flex: 1;
  min-width: 0;
}

.link-preview-title {
  font-weight: 600;
  font-size: 16px;
  color: #333;
  margin-bottom: 4px;
}

.link-preview-url {
  font-size: 14px;
  color: #666;
  word-break: break-all;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.link-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.link-actions .el-button {
  border-radius: 8px;
  font-weight: 500;
}

.link-actions .el-button--primary {
  background-color: #7c4dff;
  border-color: #7c4dff;
  padding: 6px 16px;
}

.link-actions .el-button--primary:hover {
  background-color: #6a3dd8;
  border-color: #6a3dd8;
}

/* 宣传图片样式 */
.poster-container {
  text-align: center;
  margin-bottom: 20px;
}

.poster-image {
  max-width: 100%;
  max-height: 900px;
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.3s ease;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.poster-image:hover {
  transform: scale(1.02);
  box-shadow: 0 6px 24px rgba(124, 77, 255, 0.15);
}

.poster-actions {
  margin-top: 16px;
}

/* 自定义内容样式 */
.custom-content {
  margin-bottom: 20px;
}

.custom-content h3 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  color: #333;
}

.detail-info {
  margin-bottom: 20px;
}

/* 修改对应的CSS样式 */
.info-item {
  margin-bottom: 15px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  /* 确保内容从同一行开始 */
  flex-wrap: nowrap;
}

.info-item label {
  display: inline-block;
  width: 100px;
  font-weight: 500;
  color: #555;
  flex-shrink: 0;
  /* 确保标签与内容顶部对齐 */
  margin-top: 0;
}

.competition-description {
  /* 移除导致换行的margin-left */
  margin-left: 0;
  line-height: 1.6;
  color: #444;
  /* 让描述内容占据剩余空间 */
  flex: 1;
}

/* 队伍信息样式 */
.team-info {
  margin: 20px 0;
  padding: 20px;
  background-color: #f8f8f8;
  border-radius: 12px;
}

.team-info h4 {
  margin-top: 0;
  margin-bottom: 12px;
  font-size: 16px;
  color: #333;
}

.team-name {
  font-weight: 500;
  margin: 10px 0;
}

.team-members {
  margin-top: 15px;
}

.team-members h5 {
  margin-bottom: 10px;
  font-size: 14px;
  color: #666;
}

.team-members ul {
  list-style: none;
  padding: 0;
  margin: 10px 0;
}

.team-members li {
  margin-bottom: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.leader-tag {
  background-color: #7c4dff;
  color: #fff;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  margin-left: 8px;
  font-weight: 500;
}

/* 操作按钮样式 */
.action-buttons {
  margin-top: 30px;
}

.action-buttons .el-button {
  border-radius: 8px;
  padding: 10px 24px;
  font-size: 16px;
  font-weight: 500;
}

.action-buttons .el-button--primary {
  background-color: #7c4dff;
  border-color: #7c4dff;
}

.action-buttons .el-button--primary:hover {
  background-color: #6a3dd8;
  border-color: #6a3dd8;
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
</style>
