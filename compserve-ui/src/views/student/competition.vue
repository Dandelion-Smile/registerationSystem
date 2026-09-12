<template>
  <div class="student-page">
    <StudentNavbar />

    <!-- 筛选区域 -->
    <div class="filter-section">
      <div class="filter-card">
        <div class="filter-container">
          <div class="filter-item">
            <label>竞赛类型：</label>
            <el-select
              v-model="filterType"
              placeholder="全部"
              size="default"
              clearable
              class="filter-select"
              @change="applyFilters"
            >
              <el-option label="全部" value=""></el-option>
              <el-option
                v-for="type in competitionTypeOptions"
                :key="type"
                :label="type"
                :value="type"
              ></el-option>
            </el-select>
          </div>

          <div class="filter-item">
            <label>报名状态：</label>
            <el-select
              v-model="filterStatus"
              placeholder="全部"
              size="default"
              clearable
              class="filter-select"
              @change="applyFilters"
            >
              <el-option label="全部" value=""></el-option>
              <el-option label="报名中" value="open"></el-option>
              <el-option label="非报名期" value="closed"></el-option>
            </el-select>
          </div>

          <div class="filter-actions">
            <button class="btn-filter" @click="resetFilters">
              <i class="fas fa-redo"></i> 重置筛选
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 主体内容区 -->
    <div class="main-content">
      <div class="content-wrapper">
        <!-- 左侧主区域 -->
        <div class="left-content">
          <!-- 竞赛统计 -->
          <div class="comp-header">
            <h2>共计 {{ competitions.length }} 个符合条件的竞赛</h2>
          </div>

          <!-- 热门竞赛 -->
          <div class="section-card">
            <div class="section-header">
              <i class="fas fa-fire"></i>
              <h3>热门竞赛</h3>
            </div>

            <div class="comp-grid" v-loading="loadingMore">
              <div
                v-for="comp in competitions"
                :key="comp.competitionId"
                class="comp-card"
                @click="handleCompetitionClick(comp)"
              >
                <div class="comp-header">
                  <div class="comp-icon">
                    <i class="fas fa-trophy"></i>
                  </div>
                  <span
                    class="comp-status-badge"
                    :class="
                      isInRegisterPeriod(comp) ? 'badge-open' : 'badge-closed'
                    "
                  >
                    {{ isInRegisterPeriod(comp) ? "报名中" : "非报名期" }}
                  </span>
                </div>

                <h4 class="comp-title">{{ comp.competitionName }}</h4>

                <div class="comp-tags">
                  <span class="tag tag-participate">{{
                    getCompType(comp)
                  }}</span>
                  <span class="tag tag-category" v-if="comp.competitionType">{{
                    comp.competitionType
                  }}</span>
                </div>

                <div class="comp-time">
                  <i class="far fa-calendar-alt"></i>
                  <span
                    >{{ formatDate(comp.registerStartTime) }} -
                    {{ formatDate(comp.registerEndTime) }}</span
                  >
                </div>

                <div class="comp-actions">
                  <a
                    class="link-detail"
                    @click.stop="handleCompetitionClick(comp)"
                  >
                    查看详情 <i class="fas fa-arrow-right"></i>
                  </a>
                  <button
                    v-if="!comp.userRegistered"
                    class="btn-register"
                    @click.stop="handleRegisterEntry(comp)"
                  >
                    <i class="fas fa-edit"></i> 立即报名
                  </button>
                  <button
                    v-else
                    class="btn-register"
                    @click.stop="handleRegisterEntry(comp, 'upload')"
                  >
                    <i class="fas fa-edit"></i> 修改报名信息
                  </button>
                </div>
              </div>
            </div>

            <!-- 加载更多按钮 -->
            <div v-if="hasMore" class="load-more-container">
              <button
                class="btn-load-more"
                :loading="loadingMore"
                @click="loadMore"
              >
                <i v-if="!loadingMore" class="fas fa-angle-down"></i>
                <i v-else class="fas fa-spinner fa-spin"></i>
                {{ loadingMore ? "加载中..." : "加载更多" }}
              </button>
            </div>

            <!-- 没有更多数据提示 -->
            <div v-else-if="competitions.length > 0" class="no-more-tip">
              <i class="fas fa-check-circle"></i> 已加载全部竞赛
            </div>
          </div>
        </div>

        <!-- 右侧边栏 -->
        <aside class="right-sidebar">
          <div class="sidebar-card">
            <!-- 近期报名 -->
            <div class="sidebar-section">
              <div class="sidebar-header">
                <i class="fas fa-user-graduate"></i>
                <h3>近期报名</h3>
              </div>
              <div class="recent-list">
                <div
                  v-for="(item, idx) in recentRegistrations"
                  :key="idx"
                  class="recent-item"
                >
                  <div class="recent-dot"></div>
                  <div class="recent-info">
                    <h5>{{ item.title }}</h5>
                    <p>{{ item.date }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- 分隔线 -->
            <div class="sidebar-divider"></div>

            <!-- 热门标签 -->
            <div class="sidebar-section">
              <div class="sidebar-header">
                <i class="fas fa-tags"></i>
                <h3>热门标签</h3>
              </div>
              <div class="hot-topics">
                <div class="topic-tags">
                  <span class="topic-tag">软件设计</span>
                  <span class="topic-tag">算法设计</span>
                  <span class="topic-tag">创新创业</span>
                  <span class="topic-tag">数学建模</span>
                  <span class="topic-tag">Java开发</span>
                  <span class="topic-tag">前端技术</span>
                  <span class="topic-tag">AI算法</span>
                  <span class="topic-tag">数据结构</span>
                </div>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </div>

    <!-- 竞赛详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="竞赛详情"
      width="700px"
      :close-on-click-modal="true"
    >
      <div v-if="currentCompetition" class="detail-content">
        <div class="detail-header">
          <h2>{{ currentCompetition.competitionName }}</h2>
          <span
            class="detail-status-badge"
            :class="
              currentCompetition.inRegisterPeriod
                ? 'badge-open'
                : 'badge-closed'
            "
          >
            {{ currentCompetition.inRegisterPeriod ? "报名中" : "非报名期" }}
          </span>
        </div>

        <div class="detail-info">
          <div class="info-item">
            <label>竞赛类型：</label>
            <span>{{ currentCompetition.competitionType || "未设置" }}</span>
          </div>
          <div class="info-item">
            <label>报名时间：</label>
            <span
              >{{ formatDate(currentCompetition.registerStartTime) }} -
              {{ formatDate(currentCompetition.registerEndTime) }}</span
            >
          </div>
          <div class="info-item">
            <label>竞赛描述：</label>
            <p class="description-text">
              {{ currentCompetition.description || "暂无描述" }}
            </p>
          </div>

          <!-- 已报名提示与队伍信息 -->
          <div v-if="registerStatus.registered" class="info-item">
            <label>我的报名：</label>
            <div class="my-register">
              <el-alert
                type="success"
                show-icon
                :closable="false"
                class="my-register-alert"
              >
                <template #title>
                  <span
                    >你已报名本竞赛，队伍：{{
                      registerStatus.teamName || "未命名队伍"
                    }}</span
                  >
                </template>
              </el-alert>

              <div
                v-if="
                  registerStatus.teamMembers &&
                  registerStatus.teamMembers.length
                "
                class="my-team-members"
              >
                <h4>队伍成员</h4>
                <ul>
                  <li v-for="(m, idx) in registerStatus.teamMembers" :key="idx">
                    <span class="member-name">{{
                      m.name || "未填写姓名"
                    }}</span>
                    <span v-if="m.major" class="member-major"
                      >（{{ m.major }}）</span
                    >
                    <span v-if="m.studentNo" class="member-no">
                      - {{ m.studentNo }}</span
                    >
                    <el-button
                      v-if="
                        registerStatus.isLeader &&
                        String(m.role || '').toLowerCase() !== 'leader'
                      "
                      type="danger"
                      size="small"
                      text
                      @click="removeMemberFromTeam(m.studentNo)"
                      style="margin-left: 8px"
                      >移除</el-button
                    >
                  </li>
                </ul>
              </div>

              <div v-if="registerStatus.isLeader" class="invite-section">
                <h4>添加队员</h4>
                <el-input
                  v-model="inviteStudentNo"
                  size="small"
                  placeholder="请输入学号"
                  style="max-width: 240px; margin-right: 8px"
                />
                <el-button type="primary" size="small" @click="sendInvite"
                  >发送邀请</el-button
                >
              </div>
            </div>
          </div>
        </div>

      </div>
    </el-dialog>

    <!-- 报名弹窗 -->
    <el-dialog
      v-model="registerDialogVisible"
      title="竞赛报名"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-if="currentCompetition" class="dialog-content">
        <div class="comp-info-banner">
          <h3>{{ currentCompetition.competitionName }}</h3>
          <p>{{ currentCompetition.description }}</p>
        </div>

        <el-form :model="registerForm" label-width="120px">
          <el-form-item label="报名方式" required>
            <el-radio-group
              v-model="registerForm.registerType"
              @change="handleRegisterTypeChange"
            >
              <el-radio label="create">创建新队伍</el-radio>
              <el-radio label="join">加入已有队伍</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 加入已有队伍 -->
          <template v-if="registerForm.registerType === 'join'">
            <el-form-item label="选择队伍" required>
              <el-select
                v-model="registerForm.selectedTeamId"
                placeholder="请选择要加入的队伍"
                filterable
                @focus="loadTeams"
                @change="handleTeamChange"
                style="width: 100%"
              >
                <el-option
                  v-for="team in availableTeams"
                  :key="team.teamId"
                  :label="`${team.teamName} (${team.memberCount}人)`"
                  :value="team.teamId"
                >
                  <div>
                    <div>{{ team.teamName }}</div>
                    <div style="font-size: 12px; color: #909399">
                      成员数：{{ team.memberCount }}
                    </div>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </template>

          <!-- 创建新队伍 -->
          <template v-if="registerForm.registerType === 'create'">
            <el-form-item label="队伍名称" required>
              <el-input
                v-model="registerForm.teamName"
                placeholder="请输入队伍名称"
              />
            </el-form-item>
          </template>

          <el-form-item label="队伍成员" required>
            <div v-if="registerForm.registerType === 'join'" class="join-tip">
              <el-alert type="info" :closable="false" show-icon>
                <template #title>
                  <span>加入已有队伍时，请填写您的个人信息</span>
                </template>
              </el-alert>
            </div>
            <div class="members-list">
              <div
                v-for="(member, index) in registerForm.members"
                :key="index"
                class="member-item"
              >
                <div class="member-header">
                  <span class="member-label">
                    {{
                      registerForm.registerType === "join" &&
                      index < registerForm.existingMemberCount
                        ? `队员 ${index + 1}`
                        : "您的信息"
                    }}
                  </span>
                  <el-button
                    v-if="
                      registerForm.members.length > 1 &&
                      registerForm.registerType === 'create'
                    "
                    type="danger"
                    size="small"
                    text
                    @click="removeMember(index)"
                    >删除</el-button
                  >
                </div>
                <el-row :gutter="10">
                  <el-col :span="12">
                    <el-input
                      v-model="member.name"
                      placeholder="姓名"
                      size="small"
                      :disabled="
                        registerForm.registerType === 'join' &&
                        index < registerForm.existingMemberCount
                      "
                    />
                  </el-col>
                  <el-col :span="12">
                    <el-input
                      v-model="member.studentNo"
                      placeholder="学号"
                      size="small"
                      :disabled="
                        registerForm.registerType === 'join' &&
                        index < registerForm.existingMemberCount
                      "
                    />
                  </el-col>
                </el-row>
                <el-row :gutter="10" style="margin-top: 8px">
                  <el-col :span="12">
                    <el-input
                      v-model="member.major"
                      placeholder="专业"
                      size="small"
                      :disabled="
                        registerForm.registerType === 'join' &&
                        index < registerForm.existingMemberCount
                      "
                    />
                  </el-col>
                  <el-col :span="12">
                    <el-input
                      v-model="member.teacherName"
                      placeholder="指导老师"
                      size="small"
                      :disabled="
                        registerForm.registerType === 'join' &&
                        index < registerForm.existingMemberCount
                      "
                    />
                  </el-col>
                </el-row>
              </div>
              <el-button
                v-if="registerForm.registerType === 'create'"
                type="primary"
                text
                @click="addMember"
                class="btn-add-member"
              >
                <i class="fas fa-plus"></i> 添加成员
              </el-button>
            </div>
          </el-form-item>
        </el-form>
        <!-- 在D:\wqq\competitionsystem\compserve\compserve-ui\src\views\student\competition.vue的详情弹窗中添加 -->
        <el-form-item label="竞赛链接">
          <el-link
            v-if="currentCompetition.competitionLink"
            :href="currentCompetition.competitionLink"
            target="_blank"
          >
            {{ currentCompetition.competitionLink }}
          </el-link>
          <span v-else>无</span>
        </el-form-item>
      </div>

      <template #footer>
        <el-button @click="registerDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="registerLoading"
          @click="submitRegister"
        >
          提交报名
        </el-button>
      </template>
    </el-dialog>

    <!-- 材料提交弹窗 -->
    <el-dialog
      v-model="materialDialogVisible"
      title="提交参赛材料"
      width="700px"
    >
      <el-form :model="materialForm" label-width="100px">
        <el-form-item label="报名ID">
          <el-input v-model="materialForm.registerId" disabled />
        </el-form-item>
        <el-form-item label="资料文件1">
          <FileUpload
            v-model="materialForm.pptPath"
            :limit="1"
            :file-type="[
              'ppt',
              'pptx',
              'doc',
              'docx',
              'xls',
              'xlsx',
              'pdf',
              'zip',
              'rar',
              '7z',
              'mp4',
              'avi',
              'mov',
              'wmv',
              'mkv',
              'flv',
            ]"
            :file-size="100"
            :drag="false"
          />
        </el-form-item>
        <el-form-item label="资料文件2">
          <FileUpload
            v-model="materialForm.pdfPath"
            :limit="1"
            :file-type="[
              'ppt',
              'pptx',
              'doc',
              'docx',
              'xls',
              'xlsx',
              'pdf',
              'zip',
              'rar',
              '7z',
              'mp4',
              'avi',
              'mov',
              'wmv',
              'mkv',
              'flv',
            ]"
            :file-size="100"
            :drag="false"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="materialDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="materialLoading"
          @click="submitMaterial"
        >
          提交材料
        </el-button>
      </template>
    </el-dialog>

    <!-- 页脚 -->
    <footer class="page-footer">
      <div class="footer-container">
        <div class="footer-section">
          <h4><i class="fas fa-trophy"></i> 智启赛途</h4>
          <p>
            专注为高校学生提供全方位的竞赛管理与培训服务，助力学生在各类编程竞赛中取得优异成绩。
          </p>
        </div>
        <div class="footer-section">
          <h4>快速链接</h4>
          <div class="footer-links">
            <router-link to="/student" class="nav-item">首页</router-link>
            <router-link to="/competition" class="nav-item">赛事</router-link>
            <!--
           <a href="#">经验圈</a>
            <a href="#">关于我们</a>
            -->
          </div>
        </div>
        <!--<div class="footer-section">
          <h4>快捷链接</h4>
          <div class="footer-links">
            <router-link to="/student" class="nav-item">首页</router-link>
            <router-link to="/competition" class="nav-item">赛事</router-link>
            <a href="#">经验圈</a>
            <a href="#">模拟考试</a>
            <a href="#">刷题训练</a>
          </div>
        </div>-->
        <div class="footer-section">
          <h4>关于我们</h4>
          <div class="social-links">
            <a href="#"><i class="fab fa-weixin"></i></a>
            <a href="#"><i class="fab fa-weibo"></i></a>
            <a href="#"><i class="fab fa-qq"></i></a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import StudentNavbar from "@/components/StudentNavbar.vue";
import { ref, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import useUserStore from "@/store/modules/user";
import { ElMessageBox, ElMessage } from "element-plus";
import {
  listStudentCompetitions,
  registerCompetition,
  submitMaterials,
  getCompetitionTeams,
  getCompetitionStatus,
  getUploadedMaterials,
  getRecentRegistrations,
  getCompetitionTypes,
  applyJoinTeam,
  inviteTeamMember,
  removeTeamMember,
} from "@/api/competition";
import { getUserProfile } from "@/api/system/user";
import FileUpload from "@/components/FileUpload";
import {
  isStudentRegistrationProfileComplete,
  STUDENT_PROFILE_INCOMPLETE_MESSAGE,
} from "@/utils/studentProfile";
import { isApprovedMemberStatus } from "@/utils/competitionStatus";

const userStore = useUserStore();
const route = useRoute();
const router = useRouter();

// 搜索与筛选
const searchKeyword = ref("");
const filterType = ref("");
const filterStatus = ref("open");
const competitionTypeOptions = ref([]);

// 竞赛列表
const competitions = ref([]); // 存储显示的竞赛数据
const pageNum = ref(1);
const pageSize = ref(10);
const hasMore = ref(true);
const loadingMore = ref(false);

// 详情弹窗
const detailDialogVisible = ref(false);
const registerStatus = ref({
  registered: false,
  teamName: "",
  teamMembers: [],
  registerId: null,
  teamId: null,
  isLeader: false,
});

// 报名弹窗
const registerDialogVisible = ref(false);
const currentCompetition = ref(null);
const registerForm = ref({
  registerType: "create", // 'create' 或 'join'
  teamName: "",
  selectedTeamId: null,
  existingMemberCount: 0,
  members: [{ name: "", major: "", studentNo: "", teacherName: "" }],
});
const registerLoading = ref(false);
const availableTeams = ref([]);
const loadingTeams = ref(false);

// 材料提交弹窗
const materialDialogVisible = ref(false);
const materialForm = ref({
  registerId: "",
  pptPath: "",
  pdfPath: "",
});
const materialLoading = ref(false);

// 智能功能下拉菜单
const intelligentFeatures = [
  { name: "学生组织管理", icon: "fas fa-users-cog" },
  { name: "智能组队匹配", icon: "fas fa-user-friends" },
  { name: "赛事进度追踪", icon: "fas fa-tasks" },
  { name: "学生能力画像", icon: "fas fa-chart-pie" },
  { name: "智能赛事推送", icon: "fas fa-bell" },
];

// 近期报名
const recentRegistrations = ref([]);
const inviteStudentNo = ref("");

// 构建筛选参数
function getFilters() {
  return {
    keyword: (searchKeyword.value || "").trim(),
    type: filterType.value,
    status: filterStatus.value,
  };
}

function applyKeywordFilter(list) {
  if (!Array.isArray(list)) {
    return [];
  }
  const keyword = (searchKeyword.value || "").trim().toLowerCase();
  if (!keyword) {
    return list;
  }
  return list.filter((item) =>
    String(item?.competitionName || "").toLowerCase().includes(keyword),
  );
}

function isInRegisterPeriod(comp) {
  if (!comp?.registerStartTime || !comp?.registerEndTime) {
    return false;
  }
  const now = Date.now();
  const start = new Date(comp.registerStartTime).getTime();
  const end = new Date(comp.registerEndTime).getTime();
  if (Number.isNaN(start) || Number.isNaN(end)) {
    return !!comp?.inRegisterPeriod;
  }
  return now >= start && now <= end;
}

function applyLocalStatusFilter(list) {
  if (!Array.isArray(list)) {
    return [];
  }
  if (!filterStatus.value) {
    return list;
  }
  return list.filter((item) =>
    filterStatus.value === "open"
      ? isInRegisterPeriod(item)
      : !isInRegisterPeriod(item),
  );
}

function loadCompetitionTypes() {
  return getCompetitionTypes()
    .then((res) => {
      const list = Array.isArray(res.data) ? res.data : [];
      competitionTypeOptions.value = Array.from(
        new Set(list.map((type) => String(type || "").trim()).filter(Boolean)),
      );
    })
    .catch(() => {
      competitionTypeOptions.value = [];
    });
}

// 获取竞赛列表（初始加载）
function loadCompetitions() {
  pageNum.value = 1;
  const filters = getFilters();
  // 增加 loading 状态防止白屏感
  loadingMore.value = true;
  return listStudentCompetitions(pageNum.value, pageSize.value, filters)
    .then((res) => {
      competitions.value = applyKeywordFilter(
        applyLocalStatusFilter(res.data.list || []),
      );
      hasMore.value = res.data.hasMore || false;
    })
    .catch(() => {
      ElMessage.error("加载竞赛列表失败");
    })
    .finally(() => {
      loadingMore.value = false;
    });
}

// 加载更多
function loadMore() {
  if (loadingMore.value || !hasMore.value) return;

  loadingMore.value = true;
  pageNum.value += 1;

  const filters = getFilters();
  listStudentCompetitions(pageNum.value, pageSize.value, filters)
    .then((res) => {
      const newList = applyKeywordFilter(
        applyLocalStatusFilter(res.data.list || []),
      );
      competitions.value = [...competitions.value, ...newList];
      hasMore.value = res.data.hasMore || false;
    })
    .catch(() => {
      ElMessage.error("加载更多失败");
      pageNum.value -= 1; // 失败时回退页码
    })
    .finally(() => {
      loadingMore.value = false;
    });
}

// 应用筛选（重新加载数据）
function applyFilters() {
  loadCompetitions();
}

function triggerSearch() {
  applyFilters();
}

// 重置筛选条件
function resetFilters() {
  searchKeyword.value = "";
  filterType.value = "";
  filterStatus.value = "";
  loadCompetitions();
  ElMessage.success("筛选条件已重置");
}

function showClosedRegistrationMessage() {
  ElMessage.warning("该赛事暂不能报名！");
}

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

function handleCompetitionClick(comp) {
  if (!isInRegisterPeriod(comp)) {
    showClosedRegistrationMessage();
    return;
  }
  router.push(`/competition/detail/${comp.competitionId}`);
}

async function handleRegisterEntry(comp, target = "") {
  if (!isInRegisterPeriod(comp)) {
    showClosedRegistrationMessage();
    return;
  }
  const canRegister = await ensureProfileCompleteBeforeRegister();
  if (!canRegister) {
    return;
  }
  router.push({
    path: `/competition/register/${comp.competitionId}`,
    query: {
      name: comp.competitionName,
      desc: comp.description || "",
      type: comp.competitionType || "",
      start: comp.registerStartTime || "",
      end: comp.registerEndTime || "",
      ...(target ? { target } : {}),
    },
  });
}

// 监听搜索关键词变化（防抖）
let searchTimer = null;
watch(searchKeyword, () => {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    applyFilters();
  }, 500);
});

// 判断竞赛类型（根据名称关键词）
function getCompType(comp) {
  const name = comp.competitionName || "";
  const desc = comp.description || "";
  const text = name + desc;

  if (text.includes("团队") || text.includes("组队") || text.includes("团体")) {
    return "团队赛";
  } else if (text.includes("个人")) {
    return "个人赛";
  } else {
    return "不限";
  }
}

// 打开报名弹窗
function openRegisterDialog(comp) {
  if (comp && !comp.inRegisterPeriod) {
    showClosedRegistrationMessage();
    return;
  }
  currentCompetition.value = comp;
  registerForm.value = {
    registerType: "create",
    teamName: "",
    selectedTeamId: null,
    members: [{ name: "", major: "", studentNo: "", teacherName: "" }],
  };
  availableTeams.value = [];
  registerDialogVisible.value = true;
}

// 添加成员
function addMember() {
  registerForm.value.members.push({
    name: "",
    major: "",
    studentNo: "",
    teacherName: "",
  });
}

// 移除成员
function removeMember(index) {
  if (registerForm.value.members.length === 1) {
    ElMessage.warning("至少保留一位成员");
    return;
  }
  registerForm.value.members.splice(index, 1);
}

// 提交报名
function submitRegister() {
  if (!currentCompetition.value) return;

  // 验证表单
  if (registerForm.value.registerType === "create") {
    if (!registerForm.value.teamName) {
      ElMessage.warning("请填写队伍名称");
      return;
    }
  } else {
    if (!registerForm.value.selectedTeamId) {
      ElMessage.warning("请选择要加入的队伍");
      return;
    }
  }

  if (registerForm.value.registerType === "join") {
    registerLoading.value = true;
    const me = registerForm.value.members[
      registerForm.value.members.length - 1
    ] || { name: "", major: "", studentNo: "", teacherName: "" };
    applyJoinTeam(registerForm.value.selectedTeamId, me)
      .then(() => {
        ElMessage.success("已提交申请，待队长同意");
        registerDialogVisible.value = false;
        loadCompetitions();
      })
      .catch((err) => {
        ElMessage.error(err.message || "申请失败");
      })
      .finally(() => {
        registerLoading.value = false;
      });
    return;
  }

  const hasEmptyMember = registerForm.value.members.some(
    (m) => !m.name || !m.studentNo || !m.major,
  );
  if (hasEmptyMember) {
    ElMessage.warning("请完整填写所有成员信息");
    return;
  }

  registerLoading.value = true;
  const payload = {
    competitionId: currentCompetition.value.competitionId,
    teamId: null,
    teamName: registerForm.value.teamName,
    teamMembersJson: JSON.stringify(registerForm.value.members),
  };

  registerCompetition(payload)
    .then(() => {
      const memberCount = Array.isArray(registerForm.value.members)
        ? registerForm.value.members.length
        : 0;
      if (memberCount > 1) {
        ElMessage.success("队伍创建成功，已向成员发送邀请，待对方同意");
      } else {
        ElMessage.success("队伍创建成功，已成功报名");
      }
      registerDialogVisible.value = false;
      loadCompetitions();
    })
    .catch((err) => {
      ElMessage.error(err.message || "报名失败");
    })
    .finally(() => {
      registerLoading.value = false;
    });
}

// 提交材料
function submitMaterial() {
  if (!materialForm.value.registerId) {
    ElMessage.warning("请填写报名ID");
    return;
  }

  materialLoading.value = true;
  submitMaterials(materialForm.value)
    .then(() => {
      ElMessage.success("提交成功");
      materialDialogVisible.value = false;
    })
    .catch(() => {
      ElMessage.error("提交失败");
    })
    .finally(() => {
      materialLoading.value = false;
    });
}

// 从列表打开材料上传弹窗
function openUploadDialog(comp) {
  if (!comp.registerId) {
    ElMessage.warning("未找到您的报名记录");
    return;
  }

  // 先初始化表单
  materialForm.value = {
    registerId: comp.registerId,
    pptPath: "",
    pdfPath: "",
  };

  // 查询已上传的材料信息
  getUploadedMaterials(comp.registerId)
    .then((res) => {
      const data = res.data || {};
      if (data.pptPath) {
        materialForm.value.pptPath = data.pptPath;
      }
      if (data.pdfPath) {
        materialForm.value.pdfPath = data.pdfPath;
      }
    })
    .catch(() => {
      // 查询失败不影响打开弹窗，只是不反显已上传的文件
    })
    .finally(() => {
      materialDialogVisible.value = true;
    });
}

// 查看竞赛详情
function viewCompDetail(comp) {
  currentCompetition.value = comp;
  registerStatus.value = {
    registered: false,
    teamName: "",
    teamMembers: [],
    registerId: null,
  };

  getCompetitionStatus(comp.competitionId)
    .then((res) => {
      const data = res.data || {};
      registerStatus.value.registered = !!data.registered;
      registerStatus.value.teamName = data.teamName || "";
      registerStatus.value.registerId = data.registerId || null;
      registerStatus.value.teamId = data.teamId || null;
      registerStatus.value.isLeader = !!data.isLeader;

      // 尝试解析 teamMembers JSON，为前端展示准备
      let members = [];
      try {
        members = data.teamMembers ? JSON.parse(data.teamMembers) : [];
        if (!Array.isArray(members)) members = [];
      } catch (e) {
        members = [];
      }
      registerStatus.value.teamMembers = members.filter((m) => {
        return isApprovedMemberStatus(m.status);
      });
    })
    .catch(() => {
      // 查询失败不影响详情展示，只是不显示已报名信息
      registerStatus.value.registered = false;
    })
    .finally(() => {
      detailDialogVisible.value = true;
    });
}

function sendInvite() {
  if (!registerStatus.value.teamId) {
    ElMessage.warning("未找到队伍信息");
    return;
  }
  const sno = (inviteStudentNo.value || "").trim();
  if (!sno) {
    ElMessage.warning("请填写学号");
    return;
  }
  inviteTeamMember(registerStatus.value.teamId, sno)
    .then(() => {
      ElMessage.success("已发送邀请");
      inviteStudentNo.value = "";
    })
    .catch((e) => {
      const msg = (e && e.msg) || (e && e.message) || "邀请失败";
      ElMessage.error(msg);
    });
}

function removeMemberFromTeam(studentNo) {
  if (!registerStatus.value.teamId) {
    ElMessage.warning("未找到队伍信息");
    return;
  }
  if (!studentNo) {
    ElMessage.warning("缺少队员学号");
    return;
  }
  ElMessageBox.confirm("确定移除该队员吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      return removeTeamMember(registerStatus.value.teamId, studentNo);
    })
    .then(() => {
      ElMessage.success("已移除队员");
      return getCompetitionStatus(currentCompetition.value.competitionId);
    })
    .then((res) => {
      const data = res.data || {};
      registerStatus.value.registered = !!data.registered;
      registerStatus.value.teamName = data.teamName || "";
      registerStatus.value.registerId = data.registerId || null;
      registerStatus.value.teamId = data.teamId || null;
      registerStatus.value.isLeader = !!data.isLeader;
      let members = [];
      try {
        members = data.teamMembers ? JSON.parse(data.teamMembers) : [];
        if (!Array.isArray(members)) members = [];
      } catch (e) {
        members = [];
      }
      registerStatus.value.teamMembers = members.filter((m) => {
        return isApprovedMemberStatus(m.status);
      });
    })
    .catch((e) => {
      if (!e) return;
      const msg = (e && e.msg) || (e && e.message) || "移除失败";
      ElMessage.error(msg);
    });
}

// 根据路由参数打开详情或应用筛选
function openFromQuery() {
  const gotoId = route.query.goto;
  const gotoName = route.query.gotoName;
  // 如果需要按级别筛选，先加载再打开
  loadCompetitions().then(() => {
    let comp = null;
    if (gotoId) {
      comp = competitions.value.find(
        (c) => String(c.competitionId) === String(gotoId),
      );
    } else if (gotoName) {
      comp = competitions.value.find((c) => c.competitionName === gotoName);
    }
    if (comp) viewCompDetail(comp);
  });
}

// 从详情页跳转到报名
function goToRegister() {
  detailDialogVisible.value = false;
  if (currentCompetition.value && currentCompetition.value.inRegisterPeriod) {
    handleRegisterEntry(currentCompetition.value);
    return;
  }
  showClosedRegistrationMessage();
}

// 加载可用队伍列表
function loadTeams() {
  if (!currentCompetition.value || loadingTeams.value) return;

  loadingTeams.value = true;
  getCompetitionTeams(currentCompetition.value.competitionId)
    .then((res) => {
      const list = Array.isArray(res.data) ? res.data : [];
      availableTeams.value = list.map((t) => {
        let members = [];
        try {
          members = t.teamMembers ? JSON.parse(t.teamMembers) : [];
          if (!Array.isArray(members)) members = [];
        } catch (e) {
          members = [];
        }
        const approved = members.filter((m) => {
          const s = String(m.status || "").toLowerCase();
          return s === "" || s === "approved";
        });
        return {
          ...t,
          memberCount: approved.length,
          teamMembers: JSON.stringify(approved),
        };
      });
    })
    .catch(() => {
      ElMessage.error("加载队伍列表失败");
      availableTeams.value = [];
    })
    .finally(() => {
      loadingTeams.value = false;
    });
}

// 处理报名方式变化
function handleRegisterTypeChange() {
  if (registerForm.value.registerType === "join") {
    registerForm.value.teamName = "";
    // 加入队伍时只保留一个成员输入框（用户自己），已有成员从队伍信息中带出
    registerForm.value.members = [
      { name: "", major: "", studentNo: "", teacherName: "" },
    ];
    registerForm.value.existingMemberCount = 0;
    loadTeams();
  } else {
    registerForm.value.selectedTeamId = null;
    // 创建队伍时至少保留一个成员
    if (registerForm.value.members.length === 0) {
      registerForm.value.members = [
        { name: "", major: "", studentNo: "", teacherName: "" },
      ];
    }
    registerForm.value.existingMemberCount = 0;
  }
}

// 选择已有队伍后，反显队伍成员信息，并在末尾追加当前用户填写
function handleTeamChange(teamId) {
  const team = availableTeams.value.find((t) => t.teamId === teamId);
  if (!team) return;

  let existing = [];
  try {
    existing = team.teamMembers ? JSON.parse(team.teamMembers) : [];
    if (!Array.isArray(existing)) existing = [];
  } catch (e) {
    existing = [];
  }

  const approved = existing.filter((m) => {
    const s = String(m.status || "").toLowerCase();
    return s === "" || s === "approved";
  });
  registerForm.value.existingMemberCount = approved.length;
  registerForm.value.members = [
    ...approved.map((m) => ({
      name: m.name || "",
      major: m.major || "",
      studentNo: m.studentNo || "",
      teacherName: m.teacherName || "",
    })),
    { name: "", major: "", studentNo: "", teacherName: "" },
  ];
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  return `${date.getMonth() + 1}月${date.getDate()}日`;
}

// 格式化相对时间（如"10分钟前"、"1小时前"、"2天前"等）
function formatRelativeTime(dateStr) {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date; // 毫秒差

  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  const months = Math.floor(days / 30);
  const years = Math.floor(days / 365);

  if (years > 0) {
    return `${years}年前`;
  } else if (months > 0) {
    return `${months}个月前`;
  } else if (days > 0) {
    return `${days}天前`;
  } else if (hours > 0) {
    return `${hours}小时前`;
  } else if (minutes > 0) {
    return `${minutes}分钟前`;
  } else {
    return "刚刚";
  }
}

// 加载近期报名数据
function loadRecentRegistrations() {
  getRecentRegistrations(10)
    .then((res) => {
      const data = res.data || [];
      recentRegistrations.value = data.map((item) => ({
        title: item.competitionName || "未知竞赛",
        date: formatRelativeTime(item.registerTime),
      }));
    })
    .catch(() => {
      ElMessage.error("加载近期报名失败");
    });
}

// 退出登录
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

onMounted(() => {
  loadCompetitionTypes();
  loadCompetitions();
  loadRecentRegistrations();
  openFromQuery();
});
</script>

<style scoped>
/* 全局样式 */
.student-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", "Helvetica Neue", Arial,
    sans-serif;
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

.dropdown {
  position: relative;
}

.dropdown::after {
  content: "";
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

.dropdown-header {
  color: #6366f1;
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 8px;
  padding: 4px 8px;
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

.search-box {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 6px 16px;
  gap: 8px;
  transition: all 0.3s;
}

.search-box:focus-within {
  background: rgba(255, 255, 255, 0.3);
}

.search-box i {
  color: white;
  font-size: 14px;
}

.search-box input {
  background: transparent;
  border: none;
  outline: none;
  color: white;
  font-size: 14px;
  width: 200px;
}

.search-box input::placeholder {
  color: rgba(255, 255, 255, 0.7);
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

/* ==================== 筛选区域 ==================== */
.filter-section {
  background: #f5f5f5;
  padding: 24px;
}

.filter-card {
  max-width: 1400px;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.filter-container {
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-size: 14px;
  color: #6b7280;
  white-space: nowrap;
  font-weight: 500;
}

/* Element Plus 下拉框样式 */
:deep(.filter-select) {
  width: 140px;
}

:deep(.filter-select .el-input) {
  font-size: 14px;
}

:deep(.filter-select .el-input__wrapper) {
  background-color: #fff !important;
  box-shadow: 0 0 0 1px #e5e7eb inset !important;
  transition: all 0.3s;
}

:deep(.filter-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #d1d5db inset !important;
}

:deep(.filter-select .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #8b5cf6 inset !important;
}

:deep(.filter-select .el-input__inner) {
  color: #303133 !important;
  font-size: 14px !important;
  height: auto !important;
}

:deep(.filter-select .el-select__placeholder) {
  color: #a8abb2 !important;
}

:deep(.filter-select .el-select__selected-item) {
  color: #303133 !important;
  font-size: 14px !important;
}

.filter-actions {
  display: flex;
  gap: 12px;
  margin-left: auto;
}

.btn-filter,
.btn-create {
  padding: 8px 20px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  border: none;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s;
}

.btn-filter {
  background: #6366f1;
  color: white;
}

.btn-filter:hover {
  background: #4f46e5;
}

.btn-create {
  background: #8b5cf6;
  color: white;
}

.btn-create:hover {
  background: #7c3aed;
}

/* ==================== 主体内容 ==================== */
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: start;
}

/* 左侧主区域 */
.left-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comp-header {
  min-height: 24px;
}

.comp-header h2 {
  font-size: 16px;
  color: #6b7280;
  font-weight: 500;
  margin: 0;
}

.section-card {
  background: transparent;
  border-radius: 0;
  padding: 0;
  box-shadow: none;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  padding: 0;
}

.section-header i {
  color: #f59e0b;
  font-size: 20px;
}

.section-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.comp-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.comp-card {
  border: none;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  transition: all 0.3s;
  background: #fafafa;
}

.comp-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.comp-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.comp-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.comp-icon i {
  color: white;
  font-size: 17px;
}

.comp-status-badge {
  padding: 5px 14px;
  border-radius: 16px;
  font-size: 11px;
  font-weight: 600;
}

.badge-open {
  background: #d1fae5;
  color: #059669;
}

.badge-closed {
  background: #fee2e2;
  color: #dc2626;
}

.comp-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  line-height: 1.5;
}

.comp-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.tag-participate {
  background: #e0e7ff;
  color: #4f46e5;
}

.tag-category {
  background: #fef3c7;
  color: #d97706;
}

.comp-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

.comp-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: auto;
}

.link-detail {
  font-size: 13px;
  color: #8b5cf6;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s;
  text-decoration: none;
}

.link-detail:hover {
  color: #7c3aed;
}

.link-detail i {
  font-size: 11px;
  transition: transform 0.3s;
}

.link-detail:hover i {
  transform: translateX(3px);
}

.btn-register {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.3s;
  font-weight: 500;
  background: #8b5cf6;
  color: white;
  border: 1.5px solid #8b5cf6;
}

.btn-register:hover:not(:disabled) {
  background: #7c3aed;
  border-color: #7c3aed;
}

.btn-register:disabled {
  background: #e5e7eb;
  border-color: #e5e7eb;
  color: #9ca3af;
  cursor: not-allowed;
}

/* 加载更多按钮 */
.load-more-container {
  display: flex;
  justify-content: center;
  padding: 30px 0;
  margin-top: 20px;
}

.btn-load-more {
  padding: 12px 40px;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.3);
}

.btn-load-more:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
}

.btn-load-more:active {
  transform: translateY(0);
}

.no-more-tip {
  text-align: center;
  padding: 30px 0;
  margin-top: 20px;
  color: #9ca3af;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.no-more-tip i {
  color: #10b981;
}

/* 右侧边栏 */
.right-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 44px; /* 对齐左侧"热门竞赛"卡片顶部 (24px header高度 + 20px gap) */
  background: transparent;
}

.sidebar-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.sidebar-section {
  margin-bottom: 0;
}

.sidebar-section:last-child {
  margin-bottom: 0;
}

.sidebar-divider {
  height: 1px;
  background: #e5e7eb;
  margin: 24px 0;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 0;
  border-bottom: none;
}

.sidebar-header i {
  color: #8b5cf6;
  font-size: 16px;
}

.sidebar-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recent-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  transition: all 0.3s;
  cursor: pointer;
}

.recent-item:hover {
  background: #f9fafb;
}

.recent-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  flex-shrink: 0;
  margin-top: 4px;
}

.recent-info h5 {
  font-size: 12px;
  font-weight: 500;
  color: #1f2937;
  margin: 0 0 2px 0;
  line-height: 1.4;
}

.recent-info p {
  font-size: 11px;
  color: #9ca3af;
  margin: 0;
}

.hot-topics {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.topic-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.topic-tag {
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 12px;
  background: #f3f4f6;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
  white-space: nowrap;
}

.topic-tag:hover {
  background: #e5e7eb;
}

/* ==================== 弹窗样式 ==================== */
/* Element Plus 弹窗样式覆盖 */
:deep(.el-dialog) {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

:deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f3f4f6;
  background: #fafafa;
  border-radius: 12px 12px 0 0;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

:deep(.el-dialog__body) {
  padding: 24px;
  background: #ffffff;
}

/* 材料提交弹窗中禁用输入框的背景色改为白色 */
:deep(.el-dialog__body .el-input.is-disabled .el-input__wrapper) {
  background-color: #ffffff !important;
  box-shadow: 0 0 0 1px #e5e7eb inset !important;
}

:deep(.el-dialog__body .el-input.is-disabled .el-input__inner) {
  color: #1f2937 !important;
}

/* 材料提交弹窗中文件名显示样式，防止溢出，宽度与输入框一致 */
:deep(.el-dialog__body .upload-file) {
  width: 100%;
}

:deep(.el-dialog__body .upload-file-list) {
  width: 100%;
}

:deep(.el-dialog__body .upload-file-list .el-upload-list__item) {
  width: 100%;
}

/* 确保文件名和删除按钮在同一行，不换行 */
:deep(.el-dialog__body .upload-file-list .ele-upload-list__item-content) {
  display: flex !important;
  flex-wrap: nowrap !important;
  justify-content: space-between !important;
  align-items: center !important;
}

/* 文件名部分可以换行，但整体不换行 */
:deep(
  .el-dialog__body .upload-file-list .ele-upload-list__item-content > .el-link
) {
  flex: 1;
  min-width: 0;
  word-break: break-all;
  overflow-wrap: break-word;
  margin-right: 10px;
}

/* 删除按钮不换行，始终在右侧 */
:deep(
  .el-dialog__body .upload-file-list .ele-upload-list__item-content-action
) {
  flex-shrink: 0;
  white-space: nowrap;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f3f4f6;
  background: #fafafa;
  border-radius: 0 0 12px 12px;
}

.detail-content {
  padding: 10px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f3f4f6;
}

.detail-header h2 {
  font-size: 22px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.detail-status-badge {
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 600;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 0;
}

.info-item label {
  font-weight: 500;
  color: #6b7280;
  min-width: 100px;
  flex-shrink: 0;
  font-size: 14px;
}

.info-item span {
  color: #1f2937;
  font-size: 14px;
}

.description-text {
  color: #4b5563;
  line-height: 1.8;
  margin: 0;
  white-space: pre-wrap;
  background: #f9fafb;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.detail-actions {
  display: flex;
  justify-content: center;
  padding-top: 24px;
  border-top: 2px solid #f3f4f6;
}

.my-register {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.my-register-alert {
  border-radius: 8px;
  background: #fafafa !important;
  border: 1px solid #e5e7eb !important;
}

.my-register-alert :deep(.el-alert__content) {
  color: #1f2937;
}

.my-register-alert :deep(.el-alert__icon) {
  color: #8b5cf6;
}

.my-team-members {
  background: #fafafa;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  margin-top: 8px;
}

.my-team-members h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.my-team-members ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.my-team-members li {
  font-size: 13px;
  color: #4b5563;
}

.my-team-members .member-name {
  font-weight: 500;
}

:deep(.el-dialog__footer) {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.dialog-content {
  padding: 10px 0;
}

.comp-info-banner {
  background: #fafafa;
  border: 1px solid #e5e7eb;
  border-left: 4px solid #8b5cf6;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.comp-info-banner h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.comp-info-banner p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  line-height: 1.6;
}

.join-tip {
  margin-bottom: 16px;
}

.join-tip :deep(.el-alert) {
  background: #fafafa !important;
  border: 1px solid #e5e7eb !important;
  border-radius: 8px;
}

.join-tip :deep(.el-alert__content) {
  color: #1f2937;
  font-size: 13px;
}

.join-tip :deep(.el-alert__icon) {
  color: #8b5cf6;
}

.members-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.member-item {
  padding: 16px;
  background: #fafafa; /* 与竞赛卡片背景保持一致 */
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

/* 队伍成员表单输入背景色（包含禁用状态） */
.member-item :deep(.el-input__wrapper) {
  background-color: #ffffff !important;
  box-shadow: 0 0 0 1px #e5e7eb inset !important;
}

.member-item :deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: #f3f4f6 !important;
  box-shadow: 0 0 0 1px #e5e7eb inset !important;
}

.member-item :deep(.el-input__inner) {
  color: #111827 !important;
}

.member-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.member-label {
  font-weight: 600;
  font-size: 14px;
  color: #374151;
}

.btn-add-member {
  margin-top: 8px;
}

/* ==================== 页脚 ==================== */
.page-footer {
  background: #1f2937;
  color: white;
  padding: 40px 24px 24px;
  margin-top: 60px;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
}

.footer-section h4 {
  font-size: 16px;
  margin: 0 0 16px 0;
  font-weight: 600;
}

.footer-section p {
  font-size: 14px;
  color: #9ca3af;
  line-height: 1.6;
}

.footer-links {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.footer-links a {
  color: #9ca3af;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: white;
}

.social-links {
  display: flex;
  gap: 12px;
}

.social-links a {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  text-decoration: none;
  transition: all 0.3s;
}

.social-links a:hover {
  background: #8b5cf6;
  transform: translateY(-2px);
}

/* ==================== 响应式 ==================== */
@media (max-width: 900px) {
  .comp-grid {
    grid-template-columns: 1fr;
  }

  .content-wrapper {
    grid-template-columns: 1fr;
  }

  .footer-container {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .nav-container {
    flex-wrap: wrap;
    height: auto;
    padding: 12px 16px;
  }

  .main-nav {
    flex-wrap: wrap;
    gap: 16px;
  }

  .search-box input {
    width: 150px;
  }

  .filter-container {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-item {
    width: 100%;
  }

  .footer-container {
    grid-template-columns: 1fr;
  }
}

/* 在D:\wqq\competitionsystem\compserve\compserve-ui\src\views\student\competition.vue的style部分添加 */
.comp-link {
  margin: 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6366f1;
  font-size: 14px;
}

.link-url {
  color: #6366f1;
  text-decoration: none;
  word-break: break-all;
}

.link-url:hover {
  text-decoration: underline;
}
</style>
