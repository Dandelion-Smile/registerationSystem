<template>
  <header class="student-navbar">
    <div class="student-navbar__container">
      <div class="student-navbar__logo">
        <i class="fas fa-trophy"></i>
        <span class="student-navbar__logo-text">智启赛途</span>
      </div>

      <nav class="student-navbar__main">
        <router-link
          to="/student"
          class="student-navbar__item"
          :class="{ active: route.path === '/student' }"
        >
          首页
        </router-link>
        <router-link
          to="/competition"
          class="student-navbar__item"
          :class="{ active: route.path.startsWith('/competition') }"
        >
          赛事
        </router-link>
        <router-link
          to="/learning-resources"
          class="student-navbar__item"
          :class="{ active: route.path === '/learning-resources' }"
        >
          学习资源
        </router-link>

        <div
          class="student-navbar__item student-navbar__dropdown"
          :class="{
            active:
              route.path.includes('/mock-exam') ||
              route.path.includes('/practice'),
          }"
        >
          <span class="student-navbar__dropdown-trigger">
            备考中心
            <i class="fa fa-angle-down"></i>
          </span>
          <div class="student-navbar__dropdown-menu">
            <router-link to="/mock-exam">模拟考试</router-link>
            <router-link to="/practice">刷题训练</router-link>
          </div>
        </div>

        <div
          class="student-navbar__item student-navbar__dropdown"
          :class="{ active: route.path.includes('/ai-assistant') }"
        >
          <span class="student-navbar__dropdown-trigger">
            AI助手
            <i class="fa fa-angle-down"></i>
          </span>
          <div class="student-navbar__dropdown-menu">
            <router-link to="/ai-assistant/mock-defense">模拟答辩</router-link>
            <router-link to="/ai-assistant/material-optimization">材料优化</router-link>
          </div>
        </div>

        <router-link
          to="/team-matching"
          class="student-navbar__item"
          :class="{ active: route.path === '/team-matching' }"
        >
          智能组队
        </router-link>
        <router-link
          to="/ability-profile"
          class="student-navbar__item"
          :class="{ active: route.path === '/ability-profile' }"
        >
          能力画像
        </router-link>
        <router-link
          to="/competition-guide"
          class="student-navbar__item"
          :class="{
            active:
              route.path === '/competition-guide' ||
              route.path === '/discussion',
          }"
        >
          赛事指南
        </router-link>
        <router-link
          to="/student-registered-competitions"
          class="student-navbar__item"
          :class="{ active: route.path === '/student-registered-competitions' }"
        >
          已报名竞赛
        </router-link>
      </nav>

      <div class="student-navbar__actions">
        <el-dropdown
          class="student-navbar__avatar"
          trigger="hover"
          @command="handleCommand"
        >
          <div class="student-navbar__avatar-wrapper">
            <img
              v-if="avatarSrc && !avatarLoadError"
              :src="avatarSrc"
              class="student-navbar__avatar-image"
              @error="avatarLoadError = true"
            />
            <i v-else class="fas fa-user student-navbar__avatar-icon"></i>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <router-link to="/student-center/profile">
                <el-dropdown-item>个人中心</el-dropdown-item>
              </router-link>
              <el-dropdown-item divided command="logout">
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ElMessageBox } from "element-plus";
import { computed, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { getRegistrationPage } from "@/api/competition";
import {
  readRegisteredCompetitionsCache,
  shouldReuseRegisteredCompetitionsCache,
  sortRegisteredCompetitions,
  writeRegisteredCompetitionsCache,
} from "@/utils/registeredCompetition";
import useUserStore from "@/store/modules/user";

const route = useRoute();
const userStore = useUserStore();
const avatarLoadError = ref(false);
const hasPrefetchedRegisteredCompetitions = ref(false);

const avatarSrc = computed(() => userStore.avatar || "");

watch(avatarSrc, () => {
  avatarLoadError.value = false;
});

async function prefetchRegisteredCompetitions() {
  if (hasPrefetchedRegisteredCompetitions.value) {
    return;
  }

  const cache = readRegisteredCompetitionsCache();
  if (shouldReuseRegisteredCompetitionsCache(cache)) {
    hasPrefetchedRegisteredCompetitions.value = true;
    return;
  }

  hasPrefetchedRegisteredCompetitions.value = true;
  try {
    const response = await getRegistrationPage(1, 10);
    const rows = response?.data?.rows;
    const list = sortRegisteredCompetitions(Array.isArray(rows) ? rows : []);
    writeRegisteredCompetitionsCache(list);
  } catch (error) {
    hasPrefetchedRegisteredCompetitions.value = false;
  }
}

onMounted(() => {
  if (route.path === "/student-registered-competitions") {
    return;
  }
  prefetchRegisteredCompetitions();
});

const handleCommand = (command) => {
  if (command !== "logout") {
    return;
  }

  ElMessageBox.confirm("确定注销并退出系统吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => userStore.logOut())
    .then(() => {
      location.href = "/login";
    })
    .catch(() => {});
};
</script>

<style scoped>
.student-navbar {
  background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.student-navbar__container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 22px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  height: 78px;
  gap: 18px;
}

.student-navbar__logo {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 0 0 auto;
}

.student-navbar__logo i {
  font-size: 24px;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.student-navbar__logo-text {
  font-size: 19px;
  font-weight: 700;
  line-height: 1;
  color: #fff;
  letter-spacing: 0.2px;
  white-space: nowrap;
}

.student-navbar__main {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 18px;
  flex: 1 1 auto;
  min-width: 0;
  margin-left: 18px;
  white-space: nowrap;
}

.student-navbar__item {
  position: relative;
  padding: 10px 6px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  font-weight: 500;
  text-decoration: none;
  border-radius: 6px;
  transition: all 0.3s ease;
  cursor: pointer;
  white-space: nowrap;
  line-height: 1;
}

.student-navbar__item:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.15);
}

.student-navbar__item.active {
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
}

.student-navbar__item.active::after {
  content: "";
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: #fff;
  border-radius: 2px;
}

.student-navbar__dropdown {
  position: relative;
}

.student-navbar__dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 4px;
}

.student-navbar__dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  min-width: 150px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.3s ease;
  z-index: 100;
}

.student-navbar__dropdown:hover .student-navbar__dropdown-menu {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

:deep(.student-navbar__dropdown-menu a) {
  display: block;
  padding: 10px 14px;
  color: #333;
  font-size: 13px;
  text-decoration: none;
  transition: background 0.2s ease;
}

:deep(.student-navbar__dropdown-menu a:hover) {
  background: #f5f3ff;
}

:deep(.student-navbar__dropdown-menu a:first-child) {
  border-radius: 8px 8px 0 0;
}

:deep(.student-navbar__dropdown-menu a:last-child) {
  border-radius: 0 0 8px 8px;
}

.student-navbar__actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 0 0 auto;
  margin-left: auto;
}

.student-navbar__avatar {
  cursor: pointer;
}

.student-navbar__avatar-wrapper {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.5);
  transition: border-color 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.student-navbar__avatar-wrapper:hover {
  border-color: #fff;
}

.student-navbar__avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.student-navbar__avatar-icon {
  color: #fff;
  font-size: 21px;
}

@media (max-width: 1280px) {
  .student-navbar__container {
    padding: 0 18px;
    gap: 14px;
  }

  .student-navbar__main {
    gap: 12px;
    margin-left: 12px;
  }

  .student-navbar__item {
    padding: 9px 4px;
    font-size: 14px;
  }
}

@media (max-width: 900px) {
  .student-navbar__main {
    display: none;
  }

  .student-navbar__container {
    padding: 0 16px;
  }
}
</style>
