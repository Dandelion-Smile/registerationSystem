<template>
  <div class="student-page">
    <!-- 粒子效果背景 -->
    <div id="particles-js" class="particles-bg"></div>
    <StudentNavbar />

    <div class="content-box">
      <div class="page-header">
        <h1>文档优化</h1>
        <p class="page-subtitle">上传并优化你的竞赛文档，支持Word和PPT格式</p>
      </div>

      <!-- 优化数据统计 -->
      <div class="stats-section">
        <div class="stat-card">
          <div class="stat-icon blue">
            <i class="fa fa-file-text-o"></i>
          </div>
          <div class="stat-value">{{ stats.optimizationCount }}</div>
          <div class="stat-label">优化次数</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon purple">
            <i class="fa fa-check-circle-o"></i>
          </div>
          <div class="stat-value">{{ stats.successRate }}%</div>
          <div class="stat-label">优化成功</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon orange">
            <i class="fa fa-clock-o"></i>
          </div>
          <div class="stat-value">{{ Number(stats.averageTime).toFixed(3) }}s</div>
          <div class="stat-label">平均优化时间</div>
        </div>
      </div>

      <!-- 上传区域 -->
      <div class="upload-section">
        <!-- Word文档上传 -->
        <div class="upload-card word-card">
          <div class="upload-icon-container blue">
            <i class="fa fa-file-word-o"></i>
          </div>
          <h3>Word文档</h3>
          <p class="upload-desc">
            优化Word文档的结构、格式和内容，提升文档的专业性
          </p>
          <div class="file-types">
            <el-tag size="small" class="file-tag">.doc</el-tag>
            <el-tag size="small" class="file-tag">.docx</el-tag>
          </div>
          <el-upload
            class="upload-btn"
            action=""
            :auto-upload="false"
            :on-change="handleWordFileChange"
            accept=".doc,.docx"
            :limit="1"
            :file-list="wordFileList"
          >
            <el-button type="primary" size="large" class="upload-button"
              >上传文档</el-button
            >
          </el-upload>
          <div v-if="selectedWordFile" class="file-preview">
            <el-button
              type="info"
              size="small"
              @click="previewWordFile"
              class="preview-button"
            >
              <i class="fa fa-eye"></i> 预览
            </el-button>
            <el-button
              type="success"
              :loading="optimLoading"
              @click="startWordOptimize"
              :disabled="!selectedWordFile || optimLoading"
              class="optimize-button"
            >
              开始优化
            </el-button>
          </div>
        </div>

        <!-- PPT文档上传 -->
        <div class="upload-card ppt-card">
          <div class="upload-icon-container orange">
            <i class="fa fa-file-powerpoint-o"></i>
          </div>
          <h3>PPT演示文档</h3>
          <p class="upload-desc">
            优化PPT的布局、逻辑和视觉效果，提升演示的吸引力
          </p>
          <div class="file-types">
            <el-tag size="small" class="file-tag">.ppt</el-tag>
            <el-tag size="small" class="file-tag">.pptx</el-tag>
          </div>
          <el-upload
            class="upload-btn"
            action=""
            :auto-upload="false"
            :on-change="handlePPTFileChange"
            accept=".ppt,.pptx"
            :limit="1"
            :file-list="pptFileList"
          >
            <el-button type="warning" size="large" class="upload-button"
              >上传PPT</el-button
            >
          </el-upload>
          <div v-if="selectedPPTFile" class="file-preview">
            <el-button
              type="info"
              size="small"
              @click="previewPPTFile"
              class="preview-button"
            >
              <i class="fa fa-eye"></i> 预览
            </el-button>
            <el-button
              type="success"
              :loading="optimLoading"
              @click="startPptOptimize"
              :disabled="!selectedPPTFile || optimLoading"
              class="optimize-button"
            >
              PPT优化
            </el-button>
          </div>
        </div>
      </div>

      <!-- 推荐模板 -->
      <div class="templates-section">
        <h2>推荐模板</h2>
        <div class="templates-grid">
          <div class="template-card">
            <div class="template-icon">
              <i class="fa fa-trophy"></i>
            </div>
            <h4>竞赛方案模板</h4>
            <p class="template-desc">适合各类竞赛的标准文档模板</p>
            <el-button type="primary" size="small" class="template-download">
              下载模板
            </el-button>
          </div>
          <div class="template-card">
            <div class="template-icon">
              <i class="fa fa-bullseye"></i>
            </div>
            <h4>项目展示模板</h4>
            <p class="template-desc">专业的项目展示PPT模板</p>
            <el-button type="primary" size="small" class="template-download">
              下载模板
            </el-button>
          </div>
          <div class="template-card">
            <div class="template-icon">
              <i class="fa fa-bar-chart"></i>
            </div>
            <h4>数据分析模板</h4>
            <p class="template-desc">数据可视化分析报告模板</p>
            <el-button type="primary" size="small" class="template-download">
              下载模板
            </el-button>
          </div>
        </div>
      </div>

      <!-- 最近优化记录 -->
      <div class="records-section">
        <h2>最近优化记录</h2>
        <div class="records-table">
          <div class="table-header">
            <div class="table-cell">文件名</div>
            <div class="table-cell">类型</div>
            <div class="table-cell">上传时间</div>
            <div class="table-cell">状态</div>
            <div class="table-cell">操作</div>
          </div>
          <div
            class="table-row"
            v-for="(record, index) in optimizationHistory"
            :key="index"
          >
            <div class="table-cell">{{ record.fileName }}</div>
            <div class="table-cell">{{ record.type }}</div>
            <div class="table-cell">{{ record.uploadTime }}</div>
            <div class="table-cell">
              <el-tag
                :type="
                  record.status === '成功'
                    ? 'success'
                    : record.status === '部分成功'
                      ? 'warning'
                      : 'danger'
                "
                size="small"
                >{{ record.status }}</el-tag
              >
            </div>
            <div class="table-cell">
              <el-button
                type="text"
                size="small"
                @click="reoptimizeFile(record)"
              >
                重新优化
              </el-button>
            </div>
          </div>
          <div v-if="optimizationHistory.length === 0" class="empty-records">
            暂无优化记录
          </div>
        </div>
      </div>

      <!-- 结果展示 -->
      <div
        v-if="
          optimResult.docUrl ||
          optimResult.docWithCommentsUrl ||
          optimResult.pptUrl
        "
        class="result-box"
      >
        <h4>✅ 优化完成</h4>
        <div class="result-links">
          <a
            v-if="optimResult.docWithCommentsUrl"
            :href="optimResult.docWithCommentsUrl"
            target="_blank"
            class="download-link word-link"
          >
            <i class="fa fa-download"></i> 下载带有意见的Word
          </a>
          <a
            v-if="optimResult.docUrl"
            :href="optimResult.docUrl"
            target="_blank"
            class="download-link word-link"
          >
            <i class="fa fa-download"></i> 下载修改后的Word
          </a>
          <a
            v-if="optimResult.pptUrl"
            :href="optimResult.pptUrl"
            target="_blank"
            class="download-link ppt-link"
          >
            <i class="fa fa-download"></i> 下载优化后的PPT
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="MaterialOptimization">
import StudentNavbar from "@/components/StudentNavbar.vue";
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import useUserStore from "@/store/modules/user";
import axios from "axios";
import { updateOptimizationCount, getOptimizationCount, getOptimizationStats, updateOptimizationRecord, getOptimizationHistory, addOptimizationHistory } from "@/api/system/user";

// 重命名函数
const addHistoryRecord = addOptimizationHistory;

// 初始化粒子效果
onMounted(() => {
  // 加载优化统计数据
  loadOptimizationStats();
  // 加载优化历史记录
  loadOptimizationHistory();
  // 动态加载particles.js库
  if (typeof particlesJS === "undefined") {
    const script = document.createElement("script");
    script.src = "https://cdn.jsdelivr.net/particles.js/2.0.0/particles.min.js";
    script.onload = initParticles;
    document.head.appendChild(script);
  } else {
    initParticles();
  }
});

// 加载优化历史记录
function loadOptimizationHistory() {
  getOptimizationHistory()
    .then(res => {
      if (res.data && res.data.length > 0) {
        // 转换日期格式并设置到optimizationHistory
        optimizationHistory.value = res.data.map(item => ({
          id: item.id,
          fileName: item.fileName,
          type: item.fileType,
          uploadTime: new Date(item.uploadTime).toLocaleString(),
          status: item.status,
          optimizationLevel: item.optimizationLevel,
          optimizationSpeed: item.optimizationSpeed
        }));
      }
    })
    .catch(() => {
      ElMessage.error("获取优化历史记录失败");
    });
}

// 加载优化统计数据
function loadOptimizationStats() {
  getOptimizationStats()
    .then(res => {
      stats.optimizationCount = res.data.optimizationCount || 0;
      stats.successRate = res.data.successRate || 0;
      stats.averageTime = res.data.averageTime || 0;
    })
    .catch(() => {
      ElMessage.error("获取优化统计数据失败");
    });
}

function initParticles() {
  if (typeof particlesJS === "function") {
    particlesJS("particles-js", {
      particles: {
        number: {
          value: 100,
          density: {
            enable: true,
            value_area: 800,
          },
        },
        color: {
          value: "#8b5cf6",
        },
        shape: {
          type: "circle",
          stroke: {
            width: 0,
            color: "#000000",
          },
          polygon: {
            nb_sides: 5,
          },
        },
        opacity: {
          value: 0.6,
          random: true,
          anim: {
            enable: true,
            speed: 1,
            opacity_min: 0.1,
            sync: false,
          },
        },
        size: {
          value: 4,
          random: true,
          anim: {
            enable: true,
            speed: 20,
            size_min: 0.5,
            sync: false,
          },
        },
        line_linked: {
          enable: true,
          distance: 120,
          color: "#8b5cf6",
          opacity: 0.3,
          width: 1,
        },
        move: {
          enable: true,
          speed: 3,
          direction: "none",
          random: true,
          straight: false,
          out_mode: "out",
          bounce: false,
          attract: {
            enable: false,
            rotateX: 600,
            rotateY: 1200,
          },
        },
      },
      interactivity: {
        detect_on: "canvas",
        events: {
          onhover: {
            enable: true,
            mode: "grab",
          },
          onclick: {
            enable: true,
            mode: "push",
          },
          resize: true,
        },
        modes: {
          grab: {
            distance: 120,
            line_linked: {
              opacity: 0.8,
            },
          },
          bubble: {
            distance: 300,
            size: 8,
            duration: 2,
            opacity: 0.8,
            speed: 3,
          },
          repulse: {
            distance: 200,
            duration: 0.4,
          },
          push: {
            particles_nb: 4,
          },
          remove: {
            particles_nb: 2,
          },
        },
      },
      retina_detect: true,
    });
  }
}

const userStore = useUserStore();

// Coze 配置（已填好你的信息）
const COZE_CONFIG = {
  API_TOKEN:
    "pat_77kSVaFVMnMS6kLqetBDpr2aEzk14gqzvtcIhPtg0AOj4N27pOHjlUJaStqFOSL8",
  WORKFLOW_ID: "7617000212753760310",
  UPLOAD_URL: "https://api.coze.cn/v1/files/upload",
  WORKFLOW_URL: "https://api.coze.cn/v1/workflow/run",
};

// 响应式数据
const showWordUpload = ref(false);
const showPptUpload = ref(false);
const activeCard = ref("");
const optimLoading = ref(false);
const selectedWordFile = ref(null);
const selectedPPTFile = ref(null);
const wordFileList = ref([]);
const pptFileList = ref([]);
const optimResult = reactive({
  docUrl: "",
  docWithCommentsUrl: "",
  pptUrl: "",
});

// 新增功能：优化设置
const optimizationLevel = ref("standard"); // 基础优化、标准优化、深度优化
const optimizationSpeed = ref("standard"); // 快速、标准、精细

// 新增功能：优化历史
const optimizationHistory = ref([]);

// 统计数据
const stats = reactive({
  optimizationCount: 0,
  successRate: 0,
  averageTime: 0,
});

// 卡片切换
function handleWordOptimize() {
  activeCard.value = "word";
  selectedWordFile.value = null;
  wordFileList.value = [];
}
function handlePptOptimize() {
  activeCard.value = "ppt";
  selectedPPTFile.value = null;
  pptFileList.value = [];
}

// 文件选择
function handleWordFileChange(uploadFile) {
  selectedWordFile.value = uploadFile.raw;
  wordFileList.value = [uploadFile];
  ElMessage.success(`已选择：${uploadFile.name}`);
  console.log("Word文件上传成功:", uploadFile.name);
  console.log("selectedWordFile:", selectedWordFile.value);
}

function handlePPTFileChange(uploadFile) {
  selectedPPTFile.value = uploadFile.raw;
  pptFileList.value = [uploadFile];
  ElMessage.success(`已选择：${uploadFile.name}`);
  console.log("PPT文件上传成功:", uploadFile.name);
  console.log("selectedPPTFile:", selectedPPTFile.value);
}

// 新增功能：预览文件
function previewWordFile() {
  if (selectedWordFile.value) {
    const fileURL = URL.createObjectURL(selectedWordFile.value);
    window.open(fileURL, "_blank");
  }
}

function previewPPTFile() {
  if (selectedPPTFile.value) {
    const fileURL = URL.createObjectURL(selectedPPTFile.value);
    window.open(fileURL, "_blank");
  }
}

// 新增功能：添加到优化历史
function addToOptimizationHistory(fileName, fileType, status, duration = 0) {
  const historyItem = {
    id: Date.now() + Math.random(),
    fileName,
    type: fileType,
    uploadTime: new Date().toLocaleString(),
    status,
    optimizationLevel: optimizationLevel.value,
    optimizationSpeed: optimizationSpeed.value,
  };
  optimizationHistory.value.unshift(historyItem);

  // 保存历史记录到后端
  addHistoryRecord({
    fileName,
    fileType,
    status,
    optimizationLevel: optimizationLevel.value,
    optimizationSpeed: optimizationSpeed.value
  }).catch(() => {
    ElMessage.error("保存优化历史记录失败");
  });

  // 更新优化记录到后端
  const success = status === "成功" || status === "部分成功";
  updateOptimizationRecord(success, duration)
    .then(() => {
      // 重新加载优化统计数据
      loadOptimizationStats();
    })
    .catch(() => {
      ElMessage.error("更新优化记录失败");
    });

  // 保持历史记录不超过20条
  if (optimizationHistory.value.length > 20) {
    optimizationHistory.value = optimizationHistory.value.slice(0, 20);
  }
}

// 新增功能：重新优化
function reoptimizeFile(record) {
  ElMessage.info(`准备重新优化：${record.fileName}`);
  // 这里可以添加重新优化的逻辑
  // 例如：根据记录重新设置文件类型和优化参数
  if (record.type === "Word文档") {
    activeCard.value = "word";
  } else if (record.type === "PPT文档") {
    activeCard.value = "ppt";
  }
  optimizationLevel.value = record.optimizationLevel;
  optimizationSpeed.value = record.optimizationSpeed;
}

// ------------------------------
// 核心函数：上传文件到Coze获取file_id
// ------------------------------
async function uploadFileToCoze(file) {
  try {
    const formData = new FormData();
    formData.append("file", file);

    const res = await axios.post(COZE_CONFIG.UPLOAD_URL, formData, {
      headers: {
        Authorization: `Bearer ${COZE_CONFIG.API_TOKEN}`,
        "Content-Type": "multipart/form-data",
      },
    });

    if (res.data.code === 0 && res.data.data?.id) {
      return res.data.data.id; // 返回file_id
    } else {
      throw new Error(`文件上传失败：${res.data.msg || "未知错误"}`);
    }
  } catch (e) {
    console.error("文件上传到Coze失败：", e);
    throw e; // 抛出错误让上层处理
  }
}

// ------------------------------
// Word 优化（上传文件+调用工作流）
// ------------------------------
async function startWordOptimize() {
  // 前置校验
  if (!selectedWordFile.value) {
    return ElMessage.warning("请先上传Word文件");
  }
  if (!selectedWordFile.value.name.endsWith(".docx")) {
    return ElMessage.warning("仅支持.docx格式的Word文件");
  }

  optimLoading.value = true;
  const startTime = Date.now();
  try {
    // 1. 上传文件到Coze，获取file_id
    ElMessage.info("正在上传文件到Coze...");
    const fileId = await uploadFileToCoze(selectedWordFile.value);

    // 2. 调用Coze工作流
    ElMessage.info("正在调用优化工作流...");
    const res = await axios({
      method: "post",
      url: COZE_CONFIG.WORKFLOW_URL,
      headers: {
        Authorization: `Bearer ${COZE_CONFIG.API_TOKEN}`,
        "Content-Type": "application/json",
      },
      data: {
        workflow_id: COZE_CONFIG.WORKFLOW_ID,
        parameters: {
          doc: { file_id: fileId },
        },
        response_format: "json",
      },
    });

    // 3. 处理响应结果
    console.log("Word优化响应：", res.data);
    const duration = (Date.now() - startTime) / 1000;
    if (res.data.code === 0 && res.data.data) {
      const resultData =
        typeof res.data.data === "string"
          ? JSON.parse(res.data.data)
          : res.data.data;
      optimResult.docUrl = resultData.doc_url_final || "";
      optimResult.docWithCommentsUrl = resultData.doc_url_comment || "";
      if (optimResult.docUrl || optimResult.docWithCommentsUrl) {
        ElMessage.success("Word 优化完成！");
        // 记录到优化历史
        addToOptimizationHistory(
          selectedWordFile.value.name,
          "Word文档",
          "成功",
          duration
        );
      } else {
        ElMessage.warning("Word优化成功，但未返回下载链接");
        addToOptimizationHistory(
          selectedWordFile.value.name,
          "Word文档",
          "部分成功",
          duration
        );
      }
    } else {
      ElMessage.error(`Word优化失败：${res.data.msg || "未知错误"}`);
      addToOptimizationHistory(selectedWordFile.value.name, "Word文档", "失败", duration);
    }
  } catch (e) {
    // 详细错误提示
    console.error("Word优化报错详情：", e);
    let errorMsg = "Word优化失败";
    if (e.response) {
      errorMsg += `：${e.response.status} - ${JSON.stringify(e.response.data)}`;
    } else if (e.message) {
      errorMsg += `：${e.message}`;
    }
    ElMessage.error(errorMsg);
    const duration = (Date.now() - startTime) / 1000;
    addToOptimizationHistory(selectedWordFile.value.name, "Word文档", "失败", duration);
  } finally {
    optimLoading.value = false;
  }
}

// ------------------------------
// PPT 优化（上传文件+调用工作流）
// ------------------------------
async function startPptOptimize() {
  // 前置校验
  if (!selectedPPTFile.value) {
    return ElMessage.warning("请先上传PPT文件");
  }
  if (!selectedPPTFile.value.name.endsWith(".pptx")) {
    return ElMessage.warning("仅支持.pptx格式的PPT文件");
  }

  optimLoading.value = true;
  const startTime = Date.now();
  try {
    // 1. 上传文件到Coze，获取file_id
    ElMessage.info("正在上传文件到Coze...");
    const fileId = await uploadFileToCoze(selectedPPTFile.value);

    // 2. 调用Coze工作流
    ElMessage.info("正在调用优化工作流...");
    const res = await axios({
      method: "post",
      url: COZE_CONFIG.WORKFLOW_URL,
      headers: {
        Authorization: `Bearer ${COZE_CONFIG.API_TOKEN}`,
        "Content-Type": "application/json",
      },
      data: {
        workflow_id: COZE_CONFIG.WORKFLOW_ID,
        parameters: {
          ppt: { file_id: fileId },
        },
        response_format: "json",
      },
    });

    // 3. 处理响应结果
    console.log("PPT优化响应：", res.data);
    const duration = (Date.now() - startTime) / 1000;
    if (res.data.code === 0 && res.data.data) {
      const resultData =
        typeof res.data.data === "string"
          ? JSON.parse(res.data.data)
          : res.data.data;
      optimResult.pptUrl = resultData.ppt_file_url || "";
      if (optimResult.pptUrl) {
        ElMessage.success("PPT 优化完成！");
        // 记录到优化历史
        addToOptimizationHistory(selectedPPTFile.value.name, "PPT文档", "成功", duration);
      } else {
        ElMessage.warning("PPT优化成功，但未返回下载链接");
        addToOptimizationHistory(
          selectedPPTFile.value.name,
          "PPT文档",
          "部分成功",
          duration
        );
      }
    } else {
      ElMessage.error(`PPT优化失败：${res.data.msg || "未知错误"}`);
      addToOptimizationHistory(selectedPPTFile.value.name, "PPT文档", "失败", duration);
    }
  } catch (e) {
    // 详细错误提示
    console.error("PPT优化报错详情：", e);
    let errorMsg = "PPT优化失败";
    if (e.response) {
      errorMsg += `：${e.response.status} - ${JSON.stringify(e.response.data)}`;
    } else if (e.message) {
      errorMsg += `：${e.message}`;
    }
    ElMessage.error(errorMsg);
    const duration = (Date.now() - startTime) / 1000;
    addToOptimizationHistory(selectedPPTFile.value.name, "PPT文档", "失败", duration);
  } finally {
    optimLoading.value = false;
  }
}

// 退出登录逻辑
function handleCommand(command) {
  if (command === "logout") {
    ElMessageBox.confirm("确定退出登录？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    })
      .then(() => {
        userStore.logOut().then(() => (location.href = "/login"));
      })
      .catch(() => {});
  }
}
</script>

<style scoped>
/* 样式部分完全保留你的原有代码，无修改 */
.student-page {
  min-height: 100vh;
  background-color: white;
  position: relative;
}

.particles-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
}
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
.content-box {
  max-width: 1000px;
  margin: 24px auto;
  padding: 40px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  min-height: calc(100vh - 112px);
  border-radius: 24px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 4px 16px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.content-box::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent,
    rgba(99, 102, 241, 0.05),
    transparent
  );
  transform: rotate(45deg);
  animation: float 20s linear infinite;
  z-index: 0;
}

@keyframes float {
  0% {
    transform: translateX(-100%) rotate(45deg);
  }
  100% {
    transform: translateX(100%) rotate(45deg);
  }
}

.page-header {
  text-align: center;
  margin-bottom: 60px;
  padding-top: 40px;
  position: relative;
  z-index: 1;
}
.page-header h1 {
  color: #1e293b;
  font-size: 48px;
  font-weight: 700;
  margin: 0;
  font-family: "Microsoft YaHei", sans-serif;
  letter-spacing: 2px;
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: glow 3s ease-in-out infinite alternate;
}

@keyframes glow {
  from {
    filter: drop-shadow(0 0 20px rgba(99, 102, 241, 0.3));
  }
  to {
    filter: drop-shadow(0 0 30px rgba(99, 102, 241, 0.6));
  }
}
.card-container {
  display: flex;
  gap: 100px;
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 40px;
}
.optim-card {
  width: 420px;
  height: 420px;
  background: white;
  border-radius: 24px;
  padding: 60px 32px;
  text-align: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow:
    0 8px 24px rgba(0, 0, 0, 0.12),
    0 4px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px solid #e2e8f0;
  position: relative;
  overflow: hidden;
  background: linear-gradient(145deg, #ffffff, #f8f9fa);
  transform-style: preserve-3d;
  perspective: 1000px;
}

.optim-card::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transform: rotate(45deg);
  animation: shine 6s linear infinite;
  opacity: 0;
  transition: opacity 0.3s;
}
.optim-card:hover::before {
  opacity: 1;
}
@keyframes shine {
  0% {
    transform: translateX(-100%) rotate(45deg);
  }
  100% {
    transform: translateX(100%) rotate(45deg);
  }
}
.optim-card:hover {
  transform: translateY(-12px) scale(1.02);
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.15);
  border-color: transparent;
}
.word-card {
  background: white;
  border: 3px solid #2b579a;
  box-shadow: 0 8px 24px rgba(43, 87, 154, 0.15);
}
.word-card:hover {
  background: #f8faff;
  box-shadow: 0 12px 32px rgba(43, 87, 154, 0.25);
}
.ppt-card {
  background: white;
  border: 3px solid #d24726;
  box-shadow: 0 8px 24px rgba(210, 71, 38, 0.15);
}
.ppt-card:hover {
  background: #fff8f0;
  box-shadow: 0 12px 32px rgba(210, 71, 38, 0.25);
}
.card-icon {
  width: 120px;
  height: 80px;
  margin: 0 auto 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition:
    transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275),
    box-shadow 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  user-select: none;
  pointer-events: none;
  position: relative;
  z-index: 1;
}
.card-icon:hover {
  transform: scale(1.1);
}
.word-icon {
  background: #2b579a;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(43, 87, 154, 0.3);
}
.ppt-icon {
  background: #d24726;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(210, 71, 38, 0.3);
}
.word-logo {
  color: white;
  font-size: 32px;
  font-weight: bold;
  font-family: "Microsoft YaHei", sans-serif;
  padding: 16px 24px;
}
.ppt-logo {
  color: white;
  font-size: 32px;
  font-weight: bold;
  font-family: "Microsoft YaHei", sans-serif;
  padding: 16px 24px;
}
.optim-card h3 {
  font-size: 38px;
  font-weight: 700;
  color: #333;
  margin: 0 0 36px;
  font-family: "Microsoft YaHei", sans-serif;
  text-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
  transition:
    transform 0.3s,
    color 0.3s;
  position: relative;
  z-index: 1;
}
.optim-card:hover h3 {
  transform: translateY(-4px);
  color: #2b579a;
}
.ppt-card:hover h3 {
  color: #d24726;
}
.upload-area {
  margin-top: 36px;
  padding-top: 36px;
  border-top: 2px solid rgba(0, 0, 0, 0.1);
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  transition: all 0.3s;
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.5s ease forwards;
}
@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.upload-area .el-button {
  padding: 14px 28px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow:
    0 6px 16px rgba(0, 0, 0, 0.12),
    0 3px 8px rgba(0, 0, 0, 0.08);
  border: none;
  position: relative;
  overflow: hidden;
  transform-style: preserve-3d;
  perspective: 1000px;
}
.upload-area .el-button:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow:
    0 8px 24px rgba(0, 0, 0, 0.18),
    0 4px 12px rgba(0, 0, 0, 0.12);
}
.upload-area .el-button:active {
  transform: translateY(-2px) scale(0.98);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.upload-area .el-button--primary {
  background: linear-gradient(135deg, #2b579a 0%, #1e40af 100%);
}
.upload-area .el-button--primary:hover {
  background: linear-gradient(135deg, #1e40af 0%, #1e3a8a 100%);
}
.upload-area .el-button--success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}
.upload-area .el-button--success:hover {
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
}
.result-box {
  margin-top: 80px;
  padding: 60px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 24px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.12);
  border: 2px solid #e2e8f0;
  text-align: center;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  animation: slideInUp 0.8s ease;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.result-box::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent,
    rgba(99, 102, 241, 0.1),
    transparent
  );
  transform: rotate(45deg);
  animation: shine 8s linear infinite;
  opacity: 0;
  transition: opacity 0.3s;
}

.result-box:hover::before {
  opacity: 1;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(60px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.result-box h4 {
  margin: 0 0 40px;
  color: #1e293b;
  font-size: 32px;
  font-weight: 700;
  text-align: center;
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  font-family: "Microsoft YaHei", sans-serif;
  background: linear-gradient(135deg, #10b981, #059669);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.result-links {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
  justify-content: center;
  position: relative;
  z-index: 1;
}

.download-link {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 20px 40px;
  color: white;
  text-decoration: none;
  border-radius: 16px;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  font-weight: 600;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  font-size: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  position: relative;
  overflow: hidden;
}

.download-link::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transition: left 0.6s ease;
}

.download-link:hover::before {
  left: 100%;
}

.download-link:hover {
  transform: translateY(-6px) scale(1.05);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3);
}

.word-link {
  background: linear-gradient(135deg, #2b579a 0%, #1e40af 100%);
}

.word-link:hover {
  background: linear-gradient(135deg, #1e40af 0%, #1e3a8a 100%);
}

.ppt-link {
  background: linear-gradient(135deg, #d24726 0%, #b45309 100%);
}

.ppt-link:hover {
  background: linear-gradient(135deg, #b45309 0%, #92400e 100%);
}
.dropdown {
  position: relative;
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
.dropdown-menu::before {
  content: "";
  position: absolute;
  top: -8px;
  left: 0;
  width: 100%;
  height: 8px;
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

/* 页面标题 */
.page-subtitle {
  font-size: 18px;
  color: #64748b;
  margin-top: 16px;
  text-align: center;
  font-weight: 500;
  line-height: 1.6;
  position: relative;
  z-index: 1;
}

/* 优化数据统计 */
.stats-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin: 40px 0;
  position: relative;
  z-index: 1;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  padding: 32px;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  text-align: center;
  transition:
    transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275),
    box-shadow 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  border: 1px solid #e2e8f0;
  backdrop-filter: blur(10px);
}

.stat-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  border-color: transparent;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  font-size: 24px;
  color: white;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1) rotate(5deg);
}

.stat-icon.blue {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
}

.stat-icon.purple {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.stat-icon.orange {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
  font-family: "Microsoft YaHei", sans-serif;
}

.stat-label {
  font-size: 16px;
  color: #64748b;
  font-weight: 500;
}

/* 上传区域 */
.upload-section {
  display: flex;
  flex-direction: column;
  gap: 32px;
  margin: 40px 0;
  position: relative;
  z-index: 1;
}

.upload-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 32px;
}

.upload-card {
  background: rgba(255, 255, 255, 0.95);
  padding: 48px;
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  text-align: center;
  border: 2px solid #e2e8f0;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.upload-card::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transform: rotate(45deg);
  animation: shine 6s linear infinite;
  opacity: 0;
  transition: opacity 0.3s;
}

.upload-card:hover::before {
  opacity: 1;
}

.upload-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15);
  border-color: transparent;
}

.upload-card.word-card {
  border-color: #3b82f6;
  background: linear-gradient(135deg, #ffffff 0%, #eff6ff 100%);
}

.upload-card.ppt-card {
  border-color: #f97316;
  background: linear-gradient(135deg, #ffffff 0%, #fff7ed 100%);
}

.upload-icon-container {
  width: 100px;
  height: 100px;
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 32px;
  font-size: 40px;
  color: white;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  z-index: 1;
}

.upload-card:hover .upload-icon-container {
  transform: scale(1.1) rotate(5deg);
}

.upload-icon-container.blue {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.upload-icon-container.orange {
  background: linear-gradient(135deg, #f97316, #ea580c);
}

.upload-card h3 {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 20px;
  font-family: "Microsoft YaHei", sans-serif;
  position: relative;
  z-index: 1;
}

.upload-desc {
  font-size: 16px;
  color: #64748b;
  margin-bottom: 32px;
  line-height: 1.6;
  position: relative;
  z-index: 1;
}

.file-types {
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}

.file-tag {
  margin: 0 8px;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid #e2e8f0;
}

.upload-btn {
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.upload-button {
  width: 100%;
  padding: 16px 0;
  font-size: 18px;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  border: none;
  position: relative;
  overflow: hidden;
}

.upload-button:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.file-preview {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 24px;
  position: relative;
  z-index: 1;
}

.preview-button {
  flex: 1;
  padding: 12px 0;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.optimize-button {
  flex: 2;
  padding: 12px 0;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.3s ease;
}

/* 公共优化按钮区域 */
.common-optimize-section {
  margin-top: 16px;
  padding: 32px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  backdrop-filter: blur(10px);
  text-align: center;
}

.optimize-buttons {
  display: flex;
  gap: 24px;
  justify-content: center;
  flex-wrap: wrap;
}

.common-optimize-button {
  padding: 16px 32px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  border: none;
  position: relative;
  overflow: hidden;
  min-width: 200px;
}

.common-optimize-button:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.common-optimize-button.word-optimize {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.common-optimize-button.ppt-optimize {
  background: linear-gradient(135deg, #f97316, #ea580c);
}

/* 推荐模板 */
.templates-section {
  margin: 60px 0;
  position: relative;
  z-index: 1;
}

.templates-section h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 32px;
  text-align: center;
  font-family: "Microsoft YaHei", sans-serif;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.templates-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.template-card {
  background: rgba(255, 255, 255, 0.95);
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  text-align: center;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  border: 1px solid #e2e8f0;
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.template-card::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent,
    rgba(99, 102, 241, 0.1),
    transparent
  );
  transform: rotate(45deg);
  animation: shine 6s linear infinite;
  opacity: 0;
  transition: opacity 0.3s;
}

.template-card:hover::before {
  opacity: 1;
}

.template-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  border-color: transparent;
}

.template-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  font-size: 32px;
  color: white;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease;
  position: relative;
  z-index: 1;
}

.template-card:hover .template-icon {
  transform: scale(1.1) rotate(5deg);
}

.template-card h4 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 12px;
  font-family: "Microsoft YaHei", sans-serif;
  position: relative;
  z-index: 1;
}

.template-desc {
  font-size: 16px;
  color: #64748b;
  margin-bottom: 24px;
  line-height: 1.6;
  position: relative;
  z-index: 1;
}

.template-download {
  width: 100%;
  padding: 12px 0;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: none;
  position: relative;
  z-index: 1;
}

.template-download:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

/* 最近优化记录 */
.records-section {
  margin: 60px 0;
  position: relative;
  z-index: 1;
}

.records-section h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 32px;
  text-align: center;
  font-family: "Microsoft YaHei", sans-serif;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.records-table {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  border: 1px solid #e2e8f0;
  backdrop-filter: blur(10px);
}

.table-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1.5fr 1fr 1fr;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 20px;
  font-weight: 600;
  color: #64748b;
  font-size: 16px;
  border-bottom: 2px solid #e2e8f0;
}

.table-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1.5fr 1fr 1fr;
  padding: 20px;
  border-bottom: 1px solid #f1f5f9;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.table-row:hover {
  background: #f8fafc;
  transform: translateX(8px);
}

.table-row:last-child {
  border-bottom: none;
}

.table-cell {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #475569;
  word-break: break-all;
  justify-content: center;
}

.table-cell:first-child {
  justify-content: flex-start;
  font-weight: 500;
}

.empty-records {
  padding: 60px;
  text-align: center;
  color: #94a3b8;
  font-size: 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .upload-cards {
    grid-template-columns: 1fr;
  }

  .templates-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .table-header,
  .table-row {
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }

  .table-header .table-cell:nth-child(n + 3),
  .table-row .table-cell:nth-child(n + 3) {
    display: none;
  }

  .optimize-buttons {
    flex-direction: column;
    align-items: center;
  }

  .common-optimize-button {
    width: 100%;
    max-width: 300px;
  }
}

@media (max-width: 768px) {
  .content-box {
    padding: 24px;
    margin: 16px;
  }

  .stats-section {
    grid-template-columns: 1fr;
  }

  .templates-grid {
    grid-template-columns: 1fr;
  }

  .upload-card {
    padding: 32px 24px;
  }

  .page-header h1 {
    font-size: 36px;
  }

  .page-subtitle {
    font-size: 16px;
  }

  .result-box {
    padding: 40px 24px;
  }

  .result-links {
    flex-direction: column;
    align-items: center;
  }

  .download-link {
    width: 100%;
    max-width: 300px;
    justify-content: center;
  }
}
</style>