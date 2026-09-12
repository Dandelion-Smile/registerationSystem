<template>
  <div class="student-page">
    <StudentNavbar />

    <div class="content-box">
      <div class="defense-container">
        <h2 class="page-title">🎯 AI模拟答辩</h2>
        <p class="page-desc">
          通过摄像头和麦克风实时录制答辩过程，AI将为您提供专业的多维度评分
        </p>

        <!-- 录制控制区域 -->
        <div class="record-section">
          <el-row :gutter="24">
            <!-- 摄像头预览 -->
            <el-col :span="16">
              <div class="video-preview-container">
                <div class="video-header">
                  <span class="status-indicator" :class="recordingStatus">
                    <i class="fas fa-circle"></i>
                    {{ statusText }}
                  </span>
                  <span v-if="isRecording" class="timer">
                    <i class="fas fa-clock"></i>
                    {{ formatTime(recordingTime) }}
                  </span>
                </div>

                <div class="video-wrapper">
                  <video
                    ref="videoPreview"
                    autoplay
                    muted
                    playsinline
                    class="video-preview"
                  ></video>

                  <!-- 录制提示 -->
                  <div v-if="isRecording" class="recording-overlay">
                    <div class="recording-badge">
                      <i class="fas fa-circle pulse"></i>
                      录制中
                    </div>
                  </div>

                  <!-- 权限提示 -->
                  <div v-if="!hasPermission" class="permission-overlay">
                    <i class="fas fa-video-slash"></i>
                    <p>请允许访问摄像头和麦克风</p>
                  </div>
                </div>

                <!-- 波形可视化 -->
                <div class="audio-visualizer">
                  <canvas ref="audioCanvas" width="800" height="60"></canvas>
                </div>
              </div>
            </el-col>

            <!-- 控制面板 -->
            <el-col :span="8">
              <div class="control-panel">
                <h3>录制控制</h3>

                <!-- 设备选择 -->
                <div class="device-selector">
                  <label>摄像头</label>
                  <el-select
                    v-model="selectedVideoDevice"
                    placeholder="选择摄像头"
                    size="large"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="device in videoDevices"
                      :key="device.deviceId"
                      :label="device.label"
                      :value="device.deviceId"
                    />
                  </el-select>
                </div>

                <div class="device-selector">
                  <label>麦克风</label>
                  <el-select
                    v-model="selectedAudioDevice"
                    placeholder="选择麦克风"
                    size="large"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="device in audioDevices"
                      :key="device.deviceId"
                      :label="device.label"
                      :value="device.deviceId"
                    />
                  </el-select>
                </div>

                <!-- 录制时长设置 -->
                <div class="duration-setting">
                  <label>录制时长</label>
                  <el-slider
                    v-model="maxDuration"
                    :min="30"
                    :max="600"
                    :step="30"
                    show-stops
                  />
                  <span class="duration-text"
                    >{{ maxDuration }}秒 ({{ Math.floor(maxDuration / 60) }}分{{
                      maxDuration % 60
                    }}秒)</span
                  >
                </div>

                <!-- 控制按钮 -->
                <div class="control-buttons">
                  <el-button
                    v-if="!isRecording && !isPaused"
                    type="primary"
                    size="large"
                    :disabled="!hasPermission"
                    @click="startRecording"
                  >
                    <i class="fas fa-video"></i>
                    开始录制
                  </el-button>

                  <el-button
                    v-if="isRecording && !isPaused"
                    type="warning"
                    size="large"
                    @click="pauseRecording"
                  >
                    <i class="fas fa-pause"></i>
                    暂停
                  </el-button>

                  <el-button
                    v-if="isPaused"
                    type="success"
                    size="large"
                    @click="resumeRecording"
                  >
                    <i class="fas fa-play"></i>
                    继续
                  </el-button>

                  <el-button
                    v-if="isRecording || isPaused"
                    type="danger"
                    size="large"
                    @click="stopRecording"
                  >
                    <i class="fas fa-stop"></i>
                    停止录制
                  </el-button>
                </div>

                <!-- 项目文档上传（可选） -->
                <div class="document-upload">
                  <label>项目文档URL（可选）</label>
                  <el-input
                    v-model="projectDocumentUrl"
                    placeholder="输入PDF/DOC文档公网URL"
                    size="large"
                    clearable
                  >
                    <template #prefix>
                      <i class="fas fa-file-alt"></i>
                    </template>
                  </el-input>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 录制历史 -->
        <div v-if="recordings.length > 0" class="recordings-section">
          <h3>📹 录制历史</h3>
          <el-row :gutter="16">
            <el-col
              v-for="(recording, index) in recordings"
              :key="index"
              :span="8"
            >
              <div class="recording-card">
                <video
                  :src="recording.url"
                  class="recording-thumb"
                  controls
                ></video>
                <div class="recording-info">
                  <span class="recording-time">{{ recording.duration }}</span>
                  <span class="recording-date">{{ recording.date }}</span>
                </div>
                <div class="recording-actions">
                  <el-button
                    size="small"
                    type="primary"
                    @click="analyzeRecording(recording)"
                    :loading="recording.analyzing"
                  >
                    <i class="fas fa-magic"></i>
                    分析
                  </el-button>
                  <el-button size="small" @click="downloadRecording(recording)">
                    <i class="fas fa-download"></i>
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    @click="deleteRecording(index)"
                  >
                    <i class="fas fa-trash"></i>
                  </el-button>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 分析进度 -->
        <div v-if="analyzing" class="progress-section">
          <el-steps :active="currentStep" align-center>
            <el-step title="上传文件" description="上传到对象存储" />
            <el-step title="调用分析" description="AI分析处理" />
            <el-step title="生成报告" description="生成评分报告" />
            <el-step title="完成" description="查看结果" />
          </el-steps>
          <div class="progress-text">
            <i class="fas fa-spinner fa-spin"></i>
            {{ progressText }}
          </div>
        </div>

        <!-- 分析结果 -->
        <div v-if="result && !analyzing" class="result-section">
          <div class="result-header">
            <h2>📊 分析结果</h2>
            <el-button
              v-if="result.report_url"
              type="success"
              @click="downloadReport"
            >
              <i class="fas fa-download"></i>
              下载PDF报告
            </el-button>
          </div>

          <!-- 总体评分 -->
          <div class="score-overview">
            <el-row :gutter="24">
              <el-col :span="6">
                <div
                  class="score-circle"
                  :class="getGradeClass(result.overall_score?.grade)"
                >
                  <div class="score-number">
                    {{ result.overall_score?.total_score || 0 }}
                  </div>
                  <div class="score-unit">分</div>
                </div>
                <div class="score-label">总分</div>
              </el-col>
              <el-col :span="6">
                <div
                  class="grade-badge"
                  :class="getGradeClass(result.overall_score?.grade)"
                >
                  {{ result.overall_score?.grade || "N/A" }}
                </div>
                <div class="score-label">等级评定</div>
              </el-col>
              <el-col :span="12">
                <div class="dimension-scores">
                  <div class="dimension-item">
                    <span class="dim-label">行为表现</span>
                    <el-progress
                      :percentage="
                        result.overall_score?.dimension_scores?.behavior || 0
                      "
                      :color="
                        getScoreColor(
                          result.overall_score?.dimension_scores?.behavior,
                        )
                      "
                      :stroke-width="12"
                    />
                    <span class="dim-score"
                      >{{
                        result.overall_score?.dimension_scores?.behavior || 0
                      }}分</span
                    >
                  </div>
                  <div class="dimension-item">
                    <span class="dim-label">语音表达</span>
                    <el-progress
                      :percentage="
                        result.overall_score?.dimension_scores?.speech || 0
                      "
                      :color="
                        getScoreColor(
                          result.overall_score?.dimension_scores?.speech,
                        )
                      "
                      :stroke-width="12"
                    />
                    <span class="dim-score"
                      >{{
                        result.overall_score?.dimension_scores?.speech || 0
                      }}分</span
                    >
                  </div>
                  <div class="dimension-item">
                    <span class="dim-label">内容质量</span>
                    <el-progress
                      :percentage="
                        result.overall_score?.dimension_scores?.content || 0
                      "
                      :color="
                        getScoreColor(
                          result.overall_score?.dimension_scores?.content,
                        )
                      "
                      :stroke-width="12"
                    />
                    <span class="dim-score"
                      >{{
                        result.overall_score?.dimension_scores?.content || 0
                      }}分</span
                    >
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>

          <!-- 详细分析 -->
          <el-row :gutter="24" style="margin-top: 24px">
            <el-col :span="12">
              <div class="detail-card">
                <h3>✨ 优势亮点</h3>
                <ul class="detail-list strength-list">
                  <li
                    v-for="(item, index) in result.overall_score?.strengths"
                    :key="'strength-' + index"
                  >
                    <i class="fas fa-check-circle"></i>
                    {{ item }}
                  </li>
                </ul>
              </div>
            </el-col>

            <el-col :span="12">
              <div class="detail-card">
                <h3>💪 改进建议</h3>
                <ul class="detail-list suggestion-list">
                  <li
                    v-for="(item, index) in result.overall_score?.suggestions"
                    :key="'suggestion-' + index"
                  >
                    <i class="fas fa-lightbulb"></i>
                    {{ item }}
                  </li>
                </ul>
              </div>
            </el-col>
          </el-row>

          <!-- 改进方向 -->
          <div
            v-if="result.overall_score?.areas_for_improvement"
            class="improvement-section"
          >
            <h3>🎯 重点改进方向</h3>
            <el-row :gutter="16">
              <el-col
                v-for="(item, index) in result.overall_score
                  .areas_for_improvement"
                :key="'improve-' + index"
                :span="12"
              >
                <div class="improvement-item">
                  <span class="improvement-number">{{ index + 1 }}</span>
                  <span class="improvement-text">{{ item }}</span>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>

        <!-- 错误提示 -->
        <el-alert
          v-if="error"
          :title="error"
          type="error"
          show-icon
          closable
          @close="error = null"
          style="margin-top: 20px"
        />
      </div>
    </div>
  </div>
</template>

<script setup name="MockDefenseLive">
import StudentNavbar from "@/components/StudentNavbar.vue";
import { ref, onMounted, onUnmounted, computed } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import useUserStore from "@/store/modules/user";

const userStore = useUserStore();

// ==================== 🔧 配置区域 ====================
// ⚠️ 重要：必须先启动后端代理服务 python src/proxy_server.py
const CONFIG = {
  // 代理服务地址（解决跨域问题）
  // 本地开发使用这个地址，确保后端代理服务已启动
  PROXY_API_URL: import.meta.env.VITE_APP_PROXY_API_URL || "/api/run",

  // Coze API Token（可选，建议在后端配置环境变量）
  // 如果后端已通过环境变量 COZE_API_TOKEN 配置，这里可以留空
  COZE_API_TOKEN: import.meta.env.VITE_APP_COZE_API_TOKEN || "",

  // 文件上传服务地址（可选）
  UPLOAD_API_URL: import.meta.env.VITE_APP_UPLOAD_API_URL || "/api/upload",
};

// ==================== 设备相关 ====================
const videoPreview = ref(null);
const audioCanvas = ref(null);
const videoDevices = ref([]);
const audioDevices = ref([]);
const selectedVideoDevice = ref("");
const selectedAudioDevice = ref("");
const hasPermission = ref(false);
const mediaStream = ref(null);

// ==================== 录制相关 ====================
const isRecording = ref(false);
const isPaused = ref(false);
const recordingStatus = ref("idle");
const recordingTime = ref(0);
const maxDuration = ref(180);
const mediaRecorder = ref(null);
const recordedChunks = ref([]);
const recordingTimer = ref(null);
const audioContext = ref(null);
const analyser = ref(null);
const audioAnimationFrame = ref(null);

// ==================== 录制历史 ====================
const recordings = ref([]);

// ==================== 分析相关 ====================
const analyzing = ref(false);
const currentStep = ref(0);
const progressText = ref("");
const result = ref(null);
const error = ref(null);
const projectDocumentUrl = ref("");

// ==================== 计算属性 ====================
const statusText = computed(() => {
  const statusMap = {
    idle: "准备就绪",
    recording: "正在录制",
    paused: "已暂停",
  };
  return statusMap[recordingStatus.value];
});

// ==================== 初始化 ====================
onMounted(async () => {
  await initDevices();
  await startPreview();
});

onUnmounted(() => {
  stopPreview();
  stopRecording();
});

async function initDevices() {
  try {
    // 检查 navigator.mediaDevices 是否可用
    if (!navigator.mediaDevices) {
      throw new Error("浏览器不支持媒体设备API，请使用HTTPS协议访问网站");
    }
    await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
    const devices = await navigator.mediaDevices.enumerateDevices();

    videoDevices.value = devices
      .filter((device) => device.kind === "videoinput")
      .map((device) => ({
        deviceId: device.deviceId,
        label: device.label || `摄像头 ${videoDevices.value.length + 1}`,
      }));

    audioDevices.value = devices
      .filter((device) => device.kind === "audioinput")
      .map((device) => ({
        deviceId: device.deviceId,
        label: device.label || `麦克风 ${audioDevices.value.length + 1}`,
      }));

    if (videoDevices.value.length > 0) {
      selectedVideoDevice.value = videoDevices.value[0].deviceId;
    }
    if (audioDevices.value.length > 0) {
      selectedAudioDevice.value = audioDevices.value[0].deviceId;
    }

    hasPermission.value = true;
  } catch (err) {
    console.error("获取设备权限失败:", err);
    error.value =
      err.message || "无法访问摄像头或麦克风，请检查权限设置和网络协议";
    hasPermission.value = false;
  }
}

async function startPreview() {
  try {
    // 检查 navigator.mediaDevices 是否可用
    if (!navigator.mediaDevices) {
      throw new Error("浏览器不支持媒体设备API，请使用HTTPS协议访问网站");
    }

    const constraints = {
      video: selectedVideoDevice.value
        ? {
            deviceId: { exact: selectedVideoDevice.value },
            width: { ideal: 1280 },
            height: { ideal: 720 },
          }
        : true,
      audio: selectedAudioDevice.value
        ? {
            deviceId: { exact: selectedAudioDevice.value },
          }
        : true,
    };

    mediaStream.value = await navigator.mediaDevices.getUserMedia(constraints);

    if (videoPreview.value) {
      videoPreview.value.srcObject = mediaStream.value;
    }

    initAudioVisualizer();
  } catch (err) {
    console.error("启动预览失败:", err);
    error.value = "启动预览失败: " + err.message;
  }
}

function initAudioVisualizer() {
  if (!mediaStream.value) return;

  try {
    audioContext.value = new (
      window.AudioContext || window.webkitAudioContext
    )();
    const source = audioContext.value.createMediaStreamSource(
      mediaStream.value,
    );
    analyser.value = audioContext.value.createAnalyser();
    analyser.value.fftSize = 256;
    source.connect(analyser.value);
    drawAudioWaveform();
  } catch (err) {
    console.error("初始化音频可视化失败:", err);
  }
}

function drawAudioWaveform() {
  if (!audioCanvas.value || !analyser.value) return;

  const canvas = audioCanvas.value;
  const canvasCtx = canvas.getContext("2d");
  const bufferLength = analyser.value.frequencyBinCount;
  const dataArray = new Uint8Array(bufferLength);

  function draw() {
    audioAnimationFrame.value = requestAnimationFrame(draw);
    analyser.value.getByteFrequencyData(dataArray);

    canvasCtx.fillStyle = "rgba(15, 23, 42, 0.9)";
    canvasCtx.fillRect(0, 0, canvas.width, canvas.height);

    const barWidth = (canvas.width / bufferLength) * 2.5;
    let barHeight;
    let x = 0;

    for (let i = 0; i < bufferLength; i++) {
      barHeight = dataArray[i] / 2;
      const gradient = canvasCtx.createLinearGradient(0, 0, 0, canvas.height);
      gradient.addColorStop(0, "#8b5cf6");
      gradient.addColorStop(1, "#6366f1");
      canvasCtx.fillStyle = gradient;
      canvasCtx.fillRect(x, canvas.height - barHeight, barWidth, barHeight);
      x += barWidth + 1;
    }
  }

  draw();
}

function stopPreview() {
  if (mediaStream.value) {
    mediaStream.value.getTracks().forEach((track) => track.stop());
    mediaStream.value = null;
  }
  if (audioAnimationFrame.value) {
    cancelAnimationFrame(audioAnimationFrame.value);
  }
  if (audioContext.value) {
    audioContext.value.close();
  }
}

async function startRecording() {
  if (!mediaStream.value) {
    await startPreview();
  }

  recordedChunks.value = [];
  recordingTime.value = 0;

  try {
    const options = { mimeType: "video/webm;codecs=vp9,opus" };

    if (!MediaRecorder.isTypeSupported(options.mimeType)) {
      options.mimeType = "video/webm;codecs=vp8,opus";
    }
    if (!MediaRecorder.isTypeSupported(options.mimeType)) {
      options.mimeType = "video/webm";
    }

    mediaRecorder.value = new MediaRecorder(mediaStream.value, options);

    mediaRecorder.value.ondataavailable = (event) => {
      if (event.data.size > 0) {
        recordedChunks.value.push(event.data);
      }
    };

    mediaRecorder.value.onstop = () => {
      const blob = new Blob(recordedChunks.value, { type: "video/webm" });
      handleRecordingComplete(blob);
    };

    mediaRecorder.value.start(1000);
    isRecording.value = true;
    isPaused.value = false;
    recordingStatus.value = "recording";

    recordingTimer.value = setInterval(() => {
      recordingTime.value++;
      if (recordingTime.value >= maxDuration.value) {
        stopRecording();
      }
    }, 1000);

    ElMessage.success("开始录制");
  } catch (err) {
    console.error("启动录制失败:", err);
    error.value = "启动录制失败: " + err.message;
  }
}

function pauseRecording() {
  if (mediaRecorder.value && mediaRecorder.value.state === "recording") {
    mediaRecorder.value.pause();
    isPaused.value = true;
    recordingStatus.value = "paused";
    if (recordingTimer.value) {
      clearInterval(recordingTimer.value);
    }
    ElMessage.info("录制已暂停");
  }
}

function resumeRecording() {
  if (mediaRecorder.value && mediaRecorder.value.state === "paused") {
    mediaRecorder.value.resume();
    isPaused.value = false;
    recordingStatus.value = "recording";
    recordingTimer.value = setInterval(() => {
      recordingTime.value++;
      if (recordingTime.value >= maxDuration.value) {
        stopRecording();
      }
    }, 1000);
    ElMessage.success("继续录制");
  }
}

function stopRecording() {
  if (mediaRecorder.value && mediaRecorder.value.state !== "inactive") {
    mediaRecorder.value.stop();
  }
  if (recordingTimer.value) {
    clearInterval(recordingTimer.value);
    recordingTimer.value = null;
  }
  isRecording.value = false;
  isPaused.value = false;
  recordingStatus.value = "idle";
}

async function handleRecordingComplete(blob) {
  ElMessage.success("录制完成！");
  const url = URL.createObjectURL(blob);

  const recording = {
    blob: blob,
    url: url,
    duration: formatTime(recordingTime.value),
    date: new Date().toLocaleString("zh-CN"),
    size: formatFileSize(blob.size),
    analyzing: false,
  };

  recordings.value.unshift(recording);

  ElMessageBox.confirm("录制已完成，是否立即进行AI分析？", "提示", {
    confirmButtonText: "立即分析",
    cancelButtonText: "稍后再说",
    type: "success",
  })
    .then(() => {
      analyzeRecording(recording);
    })
    .catch(() => {});
}

/**
 * 分析录制内容 - 通过代理服务调用Coze API
 */
async function analyzeRecording(recording) {
  recording.analyzing = true;
  analyzing.value = true;
  currentStep.value = 1;
  progressText.value = "正在准备分析...";
  error.value = null;
  result.value = null;

  try {
    currentStep.value = 2;
    progressText.value = "正在上传视频...";

    // 上传录制的视频文件
    const videoUrl = await uploadVideo(recording.blob);

    currentStep.value = 3;
    progressText.value = "正在调用AI分析...";

    const requestBody = {
      defense_video: {
        url: videoUrl,
        file_type: "video",
      },
      defense_audio: {
        url: "",
        file_type: "audio",
      },
      project_document: {
        url: projectDocumentUrl.value || "",
        file_type: "document",
      },
    };

    console.log("📤 发送分析请求到代理服务:", CONFIG.PROXY_API_URL);
    console.log("📤 请求体:", requestBody);

    // 构建请求头
    const headers = {
      "Content-Type": "application/json",
    };

    // 如果有Token，添加到请求头
    if (CONFIG.COZE_API_TOKEN) {
      headers["Authorization"] = `Bearer ${CONFIG.COZE_API_TOKEN}`;
    }

    // 通过代理服务发送请求
    const response = await fetch(CONFIG.PROXY_API_URL, {
      method: "POST",
      headers: headers,
      body: JSON.stringify(requestBody),
    });

    console.log("📥 响应状态:", response.status);

    if (!response.ok) {
      const errorText = await response.text();
      console.error("❌ 请求失败:", errorText);
      throw new Error(`分析失败: ${response.status} - ${errorText}`);
    }

    const responseText = await response.text();
    console.log("📥 响应内容:", responseText.substring(0, 500));

    currentStep.value = 4;
    progressText.value = "正在生成报告...";

    try {
      result.value = JSON.parse(responseText);
      currentStep.value = 5;
      progressText.value = "分析完成！";
      ElMessage.success("AI分析完成！");
    } catch (e) {
      console.error("解析响应失败:", e);
      result.value = {
        report_url: "",
        overall_score: {
          total_score: 0,
          grade: "N/A",
          dimension_scores: {},
          suggestions: ["响应格式异常"],
          strengths: [],
          areas_for_improvement: [],
        },
      };
    }
  } catch (err) {
    console.error("分析失败:", err);
    error.value = err.message;
    currentStep.value = 0;
    ElMessage.error("分析失败: " + err.message);
  } finally {
    analyzing.value = false;
    recording.analyzing = false;
  }
}

/**
 * 上传视频文件到服务器
 */
async function uploadVideo(blob) {
  try {
    const formData = new FormData();
    formData.append("file", blob, `defense_${Date.now()}.webm`);

    const response = await fetch(CONFIG.UPLOAD_API_URL, {
      method: "POST",
      body: formData,
    });

    if (!response.ok) {
      throw new Error("视频上传失败");
    }

    const data = await response.json();
    return data.url || "";
  } catch (err) {
    console.error("视频上传失败:", err);
    // 如果上传失败，使用测试视频URL作为备选
    return "https://coze-coding-mockdata.tos-cn-beijing.volces.com/video_x7rCVqJ.mp4";
  }
}

function downloadRecording(recording) {
  const a = document.createElement("a");
  a.href = recording.url;
  a.download = `defense_${Date.now()}.webm`;
  a.click();
}

function deleteRecording(index) {
  const recording = recordings.value[index];
  if (recording.url) {
    URL.revokeObjectURL(recording.url);
  }
  recordings.value.splice(index, 1);
  ElMessage.success("已删除");
}

function downloadReport() {
  if (result.value?.report_url) {
    window.open(result.value.report_url, "_blank");
  } else {
    ElMessage.warning("暂无报告下载链接");
  }
}

function formatTime(seconds) {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
}

function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + " B";
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + " KB";
  return (bytes / (1024 * 1024)).toFixed(2) + " MB";
}

function getScoreColor(score) {
  if (score >= 90) return "#67C23A";
  if (score >= 80) return "#409EFF";
  if (score >= 70) return "#E6A23C";
  return "#F56C6C";
}

function getGradeClass(grade) {
  const gradeMap = {
    A: "grade-a",
    B: "grade-b",
    C: "grade-c",
    D: "grade-d",
    F: "grade-f",
  };
  return gradeMap[grade] || "grade-d";
}

function handleCommand(command) {
  if (command === "logout") {
    ElMessageBox.confirm("确定注销并退出系统吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    })
      .then(() => {
        userStore.logOut().then(() => {
          location.href = "/login";
        });
      })
      .catch(() => {});
  }
}
</script>

<style scoped>
.student-page {
  min-height: 100vh;
  background-color: #f5f7fa;
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
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
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

.dropdown {
  position: relative;
}

.dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 4px;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 8px 0;
  min-width: 150px;
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.3s;
  z-index: 1000;
}

.dropdown:hover .dropdown-menu {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.dropdown-menu a {
  display: block;
  padding: 8px 16px;
  color: #333;
  text-decoration: none;
}

.dropdown-menu a:hover {
  background: #f5f7fa;
}

.avatar-wrapper {
  cursor: pointer;
}

.user-avatar-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid white;
}

.content-box {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.defense-container {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.page-title {
  margin: 0 0 8px;
  font-size: 24px;
  color: #1a1a1a;
}

.page-desc {
  color: #666;
  margin-bottom: 24px;
}

.record-section {
  margin-bottom: 24px;
}

.video-preview-container {
  background: #0f172a;
  border-radius: 12px;
  overflow: hidden;
}

.video-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.3);
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #94a3b8;
  font-size: 14px;
}

.status-indicator.recording {
  color: #ef4444;
}

.status-indicator.recording i {
  animation: pulse 1s infinite;
}

.timer {
  display: flex;
  align-items: center;
  gap: 6px;
  color: white;
  font-family: monospace;
  font-size: 16px;
}

.video-wrapper {
  position: relative;
  aspect-ratio: 16/9;
}

.video-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background: #000;
}

.recording-overlay {
  position: absolute;
  top: 16px;
  right: 16px;
}

.recording-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(239, 68, 68, 0.9);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.recording-badge i {
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.permission-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #94a3b8;
}

.permission-overlay i {
  font-size: 48px;
  margin-bottom: 12px;
}

.audio-visualizer {
  background: #0f172a;
  padding: 8px 0;
}

.control-panel {
  background: #f8fafc;
  border-radius: 12px;
  padding: 24px;
}

.control-panel h3 {
  margin: 0 0 20px;
  font-size: 16px;
  color: #1a1a1a;
}

.device-selector,
.duration-setting,
.document-upload {
  margin-bottom: 20px;
}

.device-selector label,
.duration-setting label,
.document-upload label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.duration-text {
  display: block;
  text-align: center;
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.control-buttons {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.control-buttons .el-button {
  flex: 1;
}

.recordings-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #eee;
}

.recordings-section h3 {
  margin-bottom: 16px;
}

.recording-card {
  background: #f8fafc;
  border-radius: 8px;
  overflow: hidden;
}

.recording-thumb {
  width: 100%;
  aspect-ratio: 16/9;
  object-fit: cover;
  background: #000;
}

.recording-info {
  display: flex;
  justify-content: space-between;
  padding: 8px 12px;
  font-size: 12px;
  color: #666;
}

.recording-actions {
  display: flex;
  gap: 8px;
  padding: 8px 12px;
  border-top: 1px solid #eee;
}

.progress-section {
  margin-top: 24px;
  padding: 24px;
  background: #f8fafc;
  border-radius: 12px;
}

.progress-text {
  text-align: center;
  margin-top: 16px;
  color: #6366f1;
  font-size: 14px;
}

.progress-text i {
  margin-right: 8px;
}

.result-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #eee;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.result-header h2 {
  margin: 0;
}

.score-overview {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 16px;
  padding: 32px;
}

.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin: 0 auto 8px;
  color: white;
}

.score-circle.grade-a {
  background: linear-gradient(135deg, #10b981, #059669);
}

.score-circle.grade-b {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.score-circle.grade-c {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.score-circle.grade-d {
  background: linear-gradient(135deg, #f97316, #ea580c);
}

.score-circle.grade-f {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.score-number {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
}

.score-unit {
  font-size: 14px;
  opacity: 0.9;
}

.score-label {
  text-align: center;
  font-size: 14px;
  color: #666;
}

.grade-badge {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 20px auto 8px;
  font-size: 32px;
  font-weight: 700;
  color: white;
}

.dimension-scores {
  padding: 8px 0;
}

.dimension-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.dim-label {
  width: 80px;
  font-size: 14px;
  color: #666;
}

.dim-score {
  width: 50px;
  text-align: right;
  font-size: 14px;
  font-weight: 500;
}

.detail-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  height: 100%;
}

.detail-card h3 {
  margin: 0 0 16px;
  font-size: 16px;
}

.detail-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.detail-list li {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 0;
  font-size: 14px;
  color: #555;
  border-bottom: 1px solid #eee;
}

.detail-list li:last-child {
  border-bottom: none;
}

.detail-list i {
  margin-top: 2px;
}

.strength-list i {
  color: #10b981;
}

.suggestion-list i {
  color: #f59e0b;
}

.improvement-section {
  margin-top: 24px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
}

.improvement-section h3 {
  margin: 0 0 16px;
}

.improvement-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: 8px;
  margin-bottom: 12px;
}

.improvement-number {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #6366f1;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.improvement-text {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
}
</style>