<template>
  <div class="student-page">
    <StudentNavbar />
    <div class="content-container">
      <div class="content-box">
        <div class="section-header">
          <div class="filter-row">
            <!-- 左侧分类 -->
            <div class="filter-left">
              <!-- 资源类型分类 -->
              <div class="category-section">
                <div class="category-title">资源类型：</div>
                <div class="category-tags">
                  <span
                    v-for="type in resourceTypes"
                    :key="type.value"
                    class="category-tag"
                    :class="{ active: selectedResourceType === type.value }"
                    @click="selectResourceType(type.value)"
                  >
                    {{ type.label }}
                  </span>
                </div>
              </div>

              <!-- 主题分类 -->
              <div class="category-section">
                <div class="category-title">主题分类：</div>
                <div class="category-tags">
                  <span
                    v-for="category in categories"
                    :key="category.value"
                    class="category-tag"
                    :class="{ active: selectedCategory === category.value }"
                    @click="selectCategory(category.value)"
                  >
                    {{ category.label }}
                  </span>
                </div>
              </div>
            </div>

            <!-- 右侧搜索和重置 -->
            <div class="filter-right">
              <div class="search-actions">
                <div class="search-container">
                  <div class="search-box">
                    <i class="fas fa-search"></i>
                    <input
                      type="text"
                      placeholder="搜索学习资源..."
                      v-model="searchKeyword"
                      @keyup.enter="applyFilters"
                    />
                    <button class="btn-search" @click="applyFilters">
                      搜索
                    </button>
                  </div>
                </div>
                <button class="btn-reset" @click="resetFilters">
                  <i class="fas fa-redo"></i> 重置
                </button>
              </div>
            </div>
          </div>

          <!-- 排序选项 -->
          <div class="sort-section">
            <div class="sort-title">排序方式：</div>
            <div class="sort-options">
              <span
                v-for="option in sortOptions"
                :key="option.value"
                class="sort-option"
                :class="{ active: selectedSort === option.value }"
                @click="selectSort(option.value)"
              >
                {{ option.label }}
              </span>
            </div>
          </div>
        </div>
        <div
          v-if="loading && learningResources.length === 0"
          class="loading-container"
        >
          <el-icon :size="40" class="is-loading"><Loading /></el-icon>
          <p>加载中...</p>
        </div>
        <div v-else class="learning-resources-grid">
          <div
            v-for="resource in learningResources"
            :key="resource.id"
            class="learning-card"
            @click="navigateToLink(resource)"
          >
            <div class="card-image-container">
              <el-image
                :src="previewSrc(resource.imageUrl)"
                :alt="resource.title"
                class="card-image"
                loading="lazy"
                fit="cover"
              >
                <template #placeholder>
                  <div class="card-image-placeholder"></div>
                </template>
              </el-image>
            </div>
            <div class="card-content">
              <h3 class="card-title">
                <span class="title-icon"><i class="fas fa-book-open"></i></span>
                {{ resource.title }}
              </h3>
              <!-- 将简介移到学习人数上面 -->
              <p class="card-description">{{ resource.description }}</p>
              <!-- 添加学习人数显示 -->
              <div class="card-learners" v-if="resource.learners !== undefined">
                <i class="fas fa-users"></i> {{ resource.learners }}人学过
              </div>
              <div class="card-meta">
                <div class="meta-item" v-if="resource.author !== '未知'">
                  <i class="fas fa-user-circle"></i>
                  <span>{{ resource.author }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="loadingMore" class="loading-more">
          <el-icon :size="20" class="is-loading"><Loading /></el-icon>
          <span>加载更多...</span>
        </div>
        <div v-if="!hasMore && learningResources.length > 0" class="no-more">
          <p>没有更多资源了</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="LearningResources">
import StudentNavbar from "@/components/StudentNavbar.vue";
import {
  ElMessageBox,
  ElMessage,
  ElImage,
  ElIcon,
  ElSelect,
  ElOption,
} from "element-plus";
import { Loading } from "@element-plus/icons-vue";
import useUserStore from "@/store/modules/user";
import { ref, onMounted, onUnmounted, watch } from "vue";
import axios from "axios";
import { listStudentCompetitions } from "@/api/competition";
import request from "@/utils/request";
import { isExternal } from "@/utils/validate";

const userStore = useUserStore();
const loading = ref(false);
const loadingMore = ref(false);
const learningResources = ref([]);
const pageNum = ref(1);
const pageSize = ref(12);
const hasMore = ref(true);
let totalCompetitions = ref([]);
let processedCompetitions = ref([]);

const baseUrl = import.meta.env.VITE_APP_BASE_API;

// 统一生成图片预览地址（私有桶通过后端代理签名）
const previewSrc = (url) => {
  if (!url) return "";
  // 直接放行 data:/blob: 等内联或本地对象 URL
  if (url.startsWith("data:") || url.startsWith("blob:")) return url;
  // 其余一律通过后端代理（包含外链私有资源）
  return `${baseUrl}/common/preview/url?url=${encodeURIComponent(url)}`;
};

// 搜索和筛选
const searchKeyword = ref("");
const selectedCategory = ref("");
const selectedResourceType = ref("");
const selectedSort = ref("comprehensive");

// 资源类型分类
const resourceTypes = [
  { label: "全部", value: "" },
  { label: "学习资料", value: "material" },
  { label: "学习视频", value: "video" },
];

// 主题分类选项
const categories = [
  { label: "全部", value: "" },
  { label: "算法设计", value: "algorithm" },
  { label: "软件开发", value: "software" },
  { label: "创新创业", value: "innovation" },
  { label: "机器人", value: "robot" },
  { label: "设计", value: "design" },
  { label: "通用基础", value: "general" },
];

// 排序选项
const sortOptions = [
  { label: "综合", value: "comprehensive" },
  { label: "最新", value: "latest" },
  { label: "最热", value: "hottest" },
];

const navigateToLink = async (resource) => {
  if (resource.link) {
    try {
      await request({
        url: `/student/competition/${resource.id}/learning`,
        method: "post",
        headers: {
          isToken: false,
        },
      });

      // 更新本地学习人数
      resource.learners = (resource.learners || 0) + 1;

      // 打开资源链接
      window.open(resource.link, "_blank", "noopener,noreferrer");
    } catch (error) {
      console.error("记录学习次数失败:", error);
      // 即使API调用失败，也允许用户打开链接
      window.open(resource.link, "_blank", "noopener,noreferrer");
    }
  } else {
    ElMessage.warning("该学习资源暂无链接");
  }
};

// 处理用户命令
const handleCommand = (command) => {
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
};

// 页面加载时的初始化逻辑
onMounted(() => {
  fetchLearningResources();
  window.addEventListener("scroll", handleScroll);
});

// 监听搜索关键词变化（防抖）
let searchTimer = null;
watch(searchKeyword, () => {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    applyFilters();
  }, 300); // 减少防抖时间，提升响应速度
});

// 选择资源类型
const selectResourceType = (type) => {
  selectedResourceType.value = type;
  applyFilters();
};

// 选择分类
const selectCategory = (category) => {
  selectedCategory.value = category;
  applyFilters();
};

// 选择排序
const selectSort = (sort) => {
  selectedSort.value = sort;
  applyFilters();
};

// 应用筛选
const applyFilters = () => {
  fetchLearningResources();
};

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = "";
  selectedCategory.value = "";
  selectedResourceType.value = "";
  selectedSort.value = "comprehensive";
  fetchLearningResources();
  ElMessage.success("筛选条件已重置");
};

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
});

// 滚动加载更多
const handleScroll = () => {
  if (loadingMore.value || loading.value || !hasMore.value) return;

  const scrollTop =
    document.documentElement.scrollTop || document.body.scrollTop;
  const scrollHeight =
    document.documentElement.scrollHeight || document.body.scrollHeight;
  const clientHeight =
    document.documentElement.clientHeight || window.innerHeight;

  if (scrollTop + clientHeight >= scrollHeight - 100) {
    loadMore();
  }
};

// 加载更多数据
const loadMore = async () => {
  if (!hasMore.value || loadingMore.value) return;

  loadingMore.value = true;
  try {
    const nextPage = pageNum.value + 1;

    // 构建筛选参数
    const filters = {
      keyword: searchKeyword.value,
      theme: selectedCategory.value,
      sort: selectedSort.value,
    };

    // 调用API加载更多数据
    const response = await listStudentCompetitions(
      nextPage,
      pageSize.value,
      filters,
    );
    const newCompetitions = response.data.list || [];

    if (newCompetitions.length > 0) {
      // 对新数据进行排序和筛选
      let filteredCompetitions = newCompetitions;

      // 筛选资源
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase();
        filteredCompetitions = filteredCompetitions.filter((comp) => {
          const title = (
            comp.learningTitle ||
            comp.competitionName ||
            ""
          ).toLowerCase();
          const description = (
            comp.learningDescription ||
            comp.description ||
            ""
          ).toLowerCase();
          const author = (comp.organizer || "").toLowerCase();
          return (
            title.includes(keyword) ||
            description.includes(keyword) ||
            author.includes(keyword)
          );
        });
      }

      // 按主题筛选
      if (selectedCategory.value) {
        filteredCompetitions = filteredCompetitions.filter((comp) => {
          const theme = detectCompetitionTheme(comp);
          return theme === selectedCategory.value;
        });
      }

      // 按资源类型筛选
      if (selectedResourceType.value) {
        // 暂时不做特殊处理，保持所有资源可见
        // 后续可以根据实际需求添加具体的筛选逻辑
        // 例如：根据资源链接类型判断是视频还是资料
      }

      // 对竞赛进行排序
      let sortedCompetitions = filteredCompetitions;

      if (selectedSort.value === "latest") {
        sortedCompetitions = filteredCompetitions.sort((a, b) => {
          return (b.competitionId || 0) - (a.competitionId || 0);
        });
      } else if (selectedSort.value === "hottest") {
        // 按最热排序（基于学习人数）
        sortedCompetitions = filteredCompetitions.sort((a, b) => {
          const learnersA = a.learningCount || 0;
          const learnersB = b.learningCount || 0;
          return learnersB - learnersA;
        });
      } else {
        sortedCompetitions = filteredCompetitions.sort((a, b) => {
          const hasResourceA = !!(
            a.learningLink ||
            a.learningImage ||
            a.learningTitle ||
            a.learningDescription
          );
          const hasResourceB = !!(
            b.learningLink ||
            b.learningImage ||
            b.learningTitle ||
            b.learningDescription
          );

          if (hasResourceA && !hasResourceB) return -1;
          if (!hasResourceA && hasResourceB) return 1;
          return 0;
        });
      }

      // 添加到总数据中（去重）
      const existingIds = new Set(
        totalCompetitions.value.map((comp) => comp.competitionId),
      );
      const uniqueCompetitions = sortedCompetitions.filter(
        (comp) => !existingIds.has(comp.competitionId),
      );
      totalCompetitions.value = [
        ...totalCompetitions.value,
        ...uniqueCompetitions,
      ];

      // 加载新页数据
      await loadPageResources(nextPage);

      // 检查是否还有更多数据
      hasMore.value = response.data.hasMore || false;
    } else {
      hasMore.value = false;
    }
  } catch (error) {
    console.error("Failed to load more resources:", error);
    ElMessage.error("加载更多资源失败");
  } finally {
    loadingMore.value = false;
  }
};

// 获取学习资源数据
const fetchLearningResources = async () => {
  try {
    loading.value = true;
    learningResources.value = [];
    pageNum.value = 1;
    hasMore.value = true;
    processedCompetitions.value = [];

    // 构建筛选参数
    const filters = {
      keyword: searchKeyword.value,
      theme: selectedCategory.value,
      sort: selectedSort.value,
    };

    // 使用更小的初始数据量，加快加载速度
    const response = await listStudentCompetitions(1, 24, filters);
    let competitions = response.data.list || [];

    // 检查是否有更多数据
    hasMore.value = response.data.hasMore || false;

    // 对竞赛进行排序和筛选
    if (competitions.length > 0) {
      // 去重
      competitions = [
        ...new Map(
          competitions.map((comp) => [comp.competitionId, comp]),
        ).values(),
      ];
      // 筛选资源
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase();
        competitions = competitions.filter((comp) => {
          const title = (
            comp.learningTitle ||
            comp.competitionName ||
            ""
          ).toLowerCase();
          const description = (
            comp.learningDescription ||
            comp.description ||
            ""
          ).toLowerCase();
          const author = (comp.organizer || "").toLowerCase();
          return (
            title.includes(keyword) ||
            description.includes(keyword) ||
            author.includes(keyword)
          );
        });
      }

      // 按主题筛选
      if (selectedCategory.value) {
        competitions = competitions.filter((comp) => {
          const theme = detectCompetitionTheme(comp);
          return theme === selectedCategory.value;
        });
      }

      // 按资源类型筛选
      if (selectedResourceType.value) {
        // 暂时不做特殊处理，保持所有资源可见
        // 后续可以根据实际需求添加具体的筛选逻辑
        // 例如：根据资源链接类型判断是视频还是资料
      }

      // 对竞赛进行排序
      let sortedCompetitions = competitions;

      if (selectedSort.value === "latest") {
        // 按最新排序（假设competitionId越大越新）
        sortedCompetitions = competitions.sort((a, b) => {
          return (b.competitionId || 0) - (a.competitionId || 0);
        });
      } else if (selectedSort.value === "hottest") {
        // 按最热排序（基于学习人数）
        sortedCompetitions = competitions.sort((a, b) => {
          const learnersA = a.learningCount || 0;
          const learnersB = b.learningCount || 0;
          return learnersB - learnersA;
        });
      } else {
        // 综合排序：有学习资源的排在前面，保持原有顺序
        sortedCompetitions = competitions.sort((a, b) => {
          const hasResourceA = !!(
            a.learningLink ||
            a.learningImage ||
            a.learningTitle ||
            a.learningDescription
          );
          const hasResourceB = !!(
            b.learningLink ||
            b.learningImage ||
            b.learningTitle ||
            b.learningDescription
          );

          if (hasResourceA && !hasResourceB) return -1;
          if (!hasResourceA && hasResourceB) return 1;
          return 0;
        });
      }

      totalCompetitions.value = sortedCompetitions;
    } else {
      totalCompetitions.value = [];
    }

    // 加载第一页数据
    await loadPageResources(1);

    ElMessage.success("学习资源加载成功");
  } catch (error) {
    console.error("Failed to fetch learning resources:", error);
    ElMessage.error("学习资源加载失败");
    // 加载失败时显示示例资源
    learningResources.value = generateExampleResources();
    hasMore.value = false;
  } finally {
    loading.value = false;
  }
};

// 加载指定页的资源
const loadPageResources = async (page) => {
  const startIndex = (page - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;

  if (startIndex >= totalCompetitions.value.length) {
    hasMore.value = false;
    return;
  }

  const pageCompetitions = totalCompetitions.value.slice(startIndex, endIndex);

  // 为当前页的竞赛生成学习资源卡片
  const resources = pageCompetitions.map((comp) => {
    // 处理简介
    let description = comp.learningDescription || comp.description;
    if (!description || description === "写简介") {
      description = "暂无简介";
    }

    // 检测竞赛主题
    const theme = detectCompetitionTheme(comp);

    return {
      id: comp.competitionId,
      title: comp.learningTitle || comp.competitionName,
      description: description,
      imageUrl: comp.learningImage || getThemePlaceholderImage(theme),
      link: comp.learningLink,
      author: comp.organizer || "未知",
      theme: theme,
      learners: comp.learningCount || 0, // 直接使用后端返回的 learningCount
    };
  });

  // 只在第一页时替换数据，否则追加
  if (page === 1) {
    learningResources.value = resources;
  } else {
    learningResources.value = [...learningResources.value, ...resources];
  }

  processedCompetitions.value = [
    ...processedCompetitions.value,
    ...pageCompetitions,
  ];
  pageNum.value = page;

  if (endIndex >= totalCompetitions.value.length) {
    hasMore.value = false;
  }
};

// 检测竞赛主题
const detectCompetitionTheme = (competition) => {
  // 先检查是否已有主题标记
  if (competition.theme) {
    return competition.theme;
  }

  const { competitionName, description } = competition;
  const text = `${competitionName || ""} ${description || ""}`.toLowerCase();

  if (
    text.includes("算法") ||
    text.includes("程序设计") ||
    text.includes("编程竞赛")
  ) {
    return "algorithm";
  }

  if (text.includes("软件") || text.includes("开发") || text.includes("编程")) {
    return "software";
  }

  if (text.includes("创新") || text.includes("创业") || text.includes("商业")) {
    return "innovation";
  }

  if (
    text.includes("机器人") ||
    text.includes("robot") ||
    text.includes("自动化")
  ) {
    return "robot";
  }

  if (
    text.includes("设计") ||
    text.includes("ui") ||
    text.includes("ux") ||
    text.includes("创意")
  ) {
    return "design";
  }

  return "general";
};

// 获取主题占位图
const getThemePlaceholderImage = (theme) => {
  // 使用更简单的内联SVG作为占位图，减少大小
  const placeholders = {
    algorithm: `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='150' height='100' viewBox='0 0 150 100'%3E%3Crect width='150' height='100' fill='%23667eea'/%3E%3C/svg%3E`,
    software: `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='150' height='100' viewBox='0 0 150 100'%3E%3Crect width='150' height='100' fill='%23764ba2'/%3E%3C/svg%3E`,
    innovation: `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='150' height='100' viewBox='0 0 150 100'%3E%3Crect width='150' height='100' fill='%23f093fb'/%3E%3C/svg%3E`,
    robot: `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='150' height='100' viewBox='0 0 150 100'%3E%3Crect width='150' height='100' fill='%234facfe'/%3E%3C/svg%3E`,
    design: `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='150' height='100' viewBox='0 0 150 100'%3E%3Crect width='150' height='100' fill='%2343e97b'/%3E%3C/svg%3E`,
    general: `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='150' height='100' viewBox='0 0 150 100'%3E%3Crect width='150' height='100' fill='%23fa709a'/%3E%3C/svg%3E`,
  };

  return placeholders[theme] || placeholders.general;
};

// 获取加载中图片
const getLoadingImage = (theme) => {
  // 返回与主题相关的加载中占位图
  const colors = {
    algorithm: "#667eea",
    software: "#764ba2",
    innovation: "#f093fb",
    robot: "#4facfe",
    design: "#43e97b",
    general: "#fa709a",
  };

  const color = colors[theme] || colors.general;

  return `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='200' viewBox='0 0 300 200'%3E%3Crect width='300' height='200' fill='%23f0f0f0'/%3E%3C/svg%3E`;
};

// 生成示例学习资源
const generateExampleResources = () => {
  return [
    {
      id: "example-1",
      title: "抄到国奖",
      description: "互联网+，挑战杯，三创赛，大创获奖案例直接用",
      imageUrl: getThemePlaceholderImage("innovation"),
      link: "https://example.com/resource1",
      author: "中国国际大学生创新大赛",
      theme: "innovation",
    },
    {
      id: "example-2",
      title: "只用6小时入门智能机器人创意大赛",
      description: "中国高校智能机器人创意大赛入门教程",
      imageUrl: getThemePlaceholderImage("robot"),
      link: "https://example.com/resource2",
      author: "中国高校智能机器人创意大赛组委会",
      theme: "robot",
    },
    {
      id: "example-3",
      title: "睿抗机器人开发者大赛",
      description: "RAICOM睿抗机器人开发者大赛培训入门教程",
      imageUrl: getThemePlaceholderImage("robot"),
      link: "https://example.com/resource3",
      author: "睿抗机器人开发者大赛组委会",
      theme: "robot",
    },
  ];
};
</script>

<style scoped>
:global(html, body) {
  margin: 0;
  padding: 0;
}

.student-page {
  min-height: 100vh;
  background-color: #ffffff;
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

.content-container {
  max-width: 1400px;
  margin: 32px auto;
  padding: 0 24px;
}

.content-box {
  background: #ffffff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e5e7eb;
}

.section-header {
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f3f4f6;
}

.section-header {
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f3f4f6;
}

.filter-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
}

.filter-left {
  flex: 1;
  min-width: 500px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-right {
  min-width: 300px;
}

.category-section {
  margin-bottom: 0;
}

.category-section .category-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 4px;
}

.category-title,
.sort-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.category-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-tag {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 16px;
  font-size: 13px;
  color: #4b5563;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.category-tag:hover {
  border-color: #8b5cf6;
  color: #8b5cf6;
}

.category-tag.active {
  background: #8b5cf6;
  border-color: #8b5cf6;
  color: white;
}

.search-actions {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
  min-width: 300px;
}

.search-container {
  min-width: 200px;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}

.search-box i {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  font-size: 14px;
  z-index: 1;
}

.search-box input {
  flex: 1;
  padding: 10px 100px 10px 36px;
  border: 1px solid #d1d5db;
  border-radius: 8px 0 0 8px;
  font-size: 14px;
  transition: all 0.3s ease;
  border-right: none;
  min-width: 150px;
}

.search-box input:focus {
  outline: none;
  border-color: #8b5cf6;
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1);
}

.btn-search {
  padding: 10px 20px;
  background: #8b5cf6;
  border: 1px solid #8b5cf6;
  border-radius: 0 8px 8px 0;
  font-size: 14px;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.btn-search:hover {
  background: #7c3aed;
  border-color: #7c3aed;
}

.btn-reset {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: #f9fafb;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  color: #4b5563;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.btn-reset:hover {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.sort-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 0;
}

.sort-options {
  display: flex;
  gap: 16px;
}

.sort-option {
  font-size: 14px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.3s ease;
  padding-bottom: 4px;
  border-bottom: 2px solid transparent;
}

.sort-option:hover {
  color: #8b5cf6;
}

.sort-option.active {
  color: #8b5cf6;
  border-bottom-color: #8b5cf6;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .filter-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-left,
  .filter-right {
    width: 100%;
    min-width: 0;
  }
}

@media (max-width: 768px) {
  .filter-left {
    flex-direction: column;
    gap: 12px;
  }

  .category-section.inline {
    width: 100%;
    min-width: 0;
  }

  .search-actions {
    width: 100%;
  }

  .search-container {
    flex: 1;
    min-width: 0;
  }

  .search-box input {
    min-width: 0;
  }
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.loading-container p {
  margin-top: 16px;
  color: #6b7280;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
  color: #6b7280;
  gap: 8px;
}

.no-more {
  text-align: center;
  padding: 20px 0;
  color: #9ca3af;
}

.learning-resources-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 28px;
}

.learning-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: all 0.2s ease;
  cursor: pointer;
  border: 1px solid #e5e7eb;
}

.learning-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #d1d5db;
}

.card-image-container {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background-color: #f9fafb;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.learning-card:hover .card-image {
  transform: scale(1.03);
}

.title-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: rgba(102, 126, 234, 0.9);
  color: white;
  border-radius: 50%;
  font-size: 12px;
  margin-right: 8px;
  vertical-align: middle;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.title-icon i {
  font-size: 10px;
}

.card-content {
  padding: 20px;
}

.card-title {
  margin: 0 0 12px 0;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-description {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #9ca3af;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-item i {
  font-size: 14px;
  color: #8b5cf6;
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
  border-radius: 10px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  padding: 12px;
  min-width: 200px;
  display: none;
  z-index: 1000;
  border: 1px solid #e5e7eb;
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
  padding: 10px 12px;
  color: #4b5563;
  text-decoration: none;
  border-radius: 6px;
  font-size: 13px;
  transition: all 0.2s;
}

.dropdown-menu a:hover {
  background: #f9fafb;
  color: #6366f1;
}

.card-learners {
  color: #666;
  font-size: 12px;
  margin: 4px 0 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-learners i {
  color: #667eea;
  font-size: 12px;
}
</style>
