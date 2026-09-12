<template>
  <div
    class="sys-admin-container flex min-h-screen bg-gray-50 font-sans antialiased text-gray-800"
  >
    <header
      class="fixed top-0 left-0 right-0 bg-primary-700 text-white shadow-lg z-50"
    >
      <div class="container mx-auto px-4 py-3">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <button
              @click="toggleSidebar"
              class="lg:hidden text-white hover:text-gray-200 focus:outline-none"
            >
              <i class="fa fa-bars text-xl"></i>
            </button>
            <div class="flex items-center gap-3">
              <div class="bg-primary-800 p-2 rounded-lg">
                <i class="fa fa-trophy text-yellow-300 text-xl"></i>
              </div>
              <h1 class="text-xl font-bold tracking-wide hidden sm:block">
                "智启赛途"
                <span class="text-primary-100 text-sm font-normal ml-1"
                  >高校竞赛AI智驭平台</span
                >
              </h1>
            </div>
          </div>

          <div class="flex items-center justify-end">
            <div class="user-dropdown-container relative">
              <button
                class="flex items-center gap-3 focus:outline-none group p-1.5 rounded-lg bg-primary-600 hover:bg-primary-500 transform hover:scale-105 transition-all duration-300 shadow-md"
                @click="toggleDropdown"
              >
                <div class="text-right hidden md:block">
                  <div class="text-sm font-medium text-white">系统管理员</div>
                  <div class="text-xs text-primary-100">admin</div>
                </div>
                <div
                  class="user-avatar bg-white text-primary-600 shadow-sm ring-2 ring-white/20 flex items-center justify-center font-bold"
                  style="width: 36px; height: 36px; border-radius: 50%"
                >
                  管
                </div>
                <i
                  class="fa fa-chevron-down text-xs text-white transition-transform duration-200"
                  :class="{ 'rotate-180': dropdownOpen }"
                ></i>
              </button>

              <transition name="fade">
                <div
                  v-if="dropdownOpen"
                  class="user-dropdown absolute right-0 top-full mt-3 w-56 bg-white rounded-xl shadow-xl py-2 z-50 border border-gray-100 ring-1 ring-black/5 transform origin-top-right"
                >
                  <div
                    class="px-5 py-4 border-b border-gray-50 mb-1 bg-gray-50"
                  >
                    <p class="text-sm font-bold text-gray-900">系统管理员</p>
                    <p class="text-xs text-gray-500 truncate mt-0.5">
                      admin@example.com
                    </p>
                  </div>
                  <div class="py-1">
                    <router-link
                      to="/sys-admin/profile"
                      @click.stop
                      class="flex items-center px-5 py-3 text-sm text-gray-700 hover:bg-primary-50 hover:text-primary-600 transition-colors group"
                    >
                      <div
                        class="w-8 h-8 rounded-full bg-gray-100 text-gray-500 flex items-center justify-center mr-3 group-hover:bg-primary-100 group-hover:text-primary-600 transition-colors"
                      >
                        <i class="fa fa-user-circle-o"></i>
                      </div>
                      <span class="font-medium">个人中心</span>
                    </router-link>
                  </div>
                  <div class="border-t border-gray-50 py-1">
                    <a
                      href="#"
                      @click.prevent="handleLogout"
                      class="flex items-center px-5 py-3 text-sm text-gray-700 hover:bg-red-50 hover:text-red-600 transition-colors group"
                    >
                      <div
                        class="w-8 h-8 rounded-full bg-gray-100 text-gray-500 flex items-center justify-center mr-3 group-hover:bg-red-100 group-hover:text-red-600 transition-colors"
                      >
                        <i class="fa fa-sign-out"></i>
                      </div>
                      <span class="font-medium">退出登录</span>
                    </a>
                  </div>
                </div>
              </transition>
            </div>
          </div>
        </div>
      </div>
    </header>
    //测试

    <div class="flex w-full pt-16 relative">
      <div
        v-if="sidebarOpen"
        class="fixed inset-0 bg-black/50 z-30 lg:hidden"
        @click="sidebarOpen = false"
      ></div>

      <aside
        class="bg-white border-r border-gray-200 shadow-lg fixed h-full overflow-y-auto z-40 transition-transform duration-300 ease-in-out w-64 lg:translate-x-0"
        :class="sidebarOpen ? 'translate-x-0' : '-translate-x-full'"
      >
        <div class="p-6">
          <p
            class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-4"
          >
            智能功能中心
          </p>
          <nav class="space-y-1.5">
            <router-link
              to="/sys-admin/competition-list"
              custom
              v-slot="{ navigate, isActive }"
            >
              <a
                href="#"
                @click="navigate"
                :class="['sidebar-item', 'group', isActive ? 'active' : '']"
              >
                <i
                  class="fa fa-list-alt w-5 text-center transition-transform group-hover:scale-110"
                ></i>
                <span>智能赛事管理</span>
                <div v-if="isActive" class="active-indicator"></div>
              </a>
            </router-link>

            <router-link
              to="/sys-admin/user-management"
              custom
              v-slot="{ navigate, isActive }"
            >
              <a
                href="#"
                @click="navigate"
                :class="['sidebar-item', 'group', isActive ? 'active' : '']"
              >
                <i
                  class="fa fa-users w-5 text-center transition-transform group-hover:scale-110"
                ></i>
                <span>用户中心</span>
                <div v-if="isActive" class="active-indicator"></div>
              </a>
            </router-link>

            <router-link
              to="/sys-admin/teacher-management"
              custom
              v-slot="{ navigate, isActive }"
            >
              <a
                href="#"
                @click="navigate"
                :class="['sidebar-item', 'group', isActive ? 'active' : '']"
              >
                <i
                  class="fa fa-university w-5 text-center transition-transform group-hover:scale-110"
                ></i>
                <span>教师管理</span>
                <div v-if="isActive" class="active-indicator"></div>
              </a>
            </router-link>

            <router-link
              to="/sys-admin/student-management"
              custom
              v-slot="{ navigate, isActive }"
            >
              <a
                href="#"
                @click="navigate"
                :class="['sidebar-item', 'group', isActive ? 'active' : '']"
              >
                <i
                  class="fa fa-graduation-cap w-5 text-center transition-transform group-hover:scale-110"
                ></i>
                <span>学生管理</span>
                <div v-if="isActive" class="active-indicator"></div>
              </a>
            </router-link>

            <router-link
              to="/sys-admin/competition-screening"
              custom
              v-slot="{ navigate, isActive }"
            >
              <a
                href="#"
                @click="navigate"
                :class="['sidebar-item', 'group', isActive ? 'active' : '']"
              >
                <i
                  class="fa fa-filter w-5 text-center transition-transform group-hover:scale-110"
                ></i>
                <span>队伍晋级筛选</span>
                <div v-if="isActive" class="active-indicator"></div>
              </a>
            </router-link>
            <router-link
              to="/sys-admin/competition-management"
              custom
              v-slot="{ navigate, isActive }"
            >
              <a
                href="#"
                @click="navigate"
                :class="['sidebar-item', 'group', isActive ? 'active' : '']"
              >
                <i
                  class="fa fa-trophy w-5 text-center transition-transform group-hover:scale-110"
                ></i>
                <span>发布赛事</span>
                <div v-if="isActive" class="active-indicator"></div>
              </a>
            </router-link>

            <router-link
              to="/sys-admin/teacher-empowerment"
              custom
              v-slot="{ navigate, isActive }"
            >
              <a
                href="#"
                @click="navigate"
                :class="['sidebar-item', 'group', isActive ? 'active' : '']"
              >
                <i
                  class="fa fa-shield w-5 text-center transition-transform group-hover:scale-110"
                ></i>
                <span>教师赋权</span>
                <div v-if="isActive" class="active-indicator"></div>
              </a>
            </router-link>

            <router-link
              to="/sys-admin/score-management"
              custom
              v-slot="{ navigate, isActive }"
            >
              <a
                href="#"
                @click="navigate"
                :class="['sidebar-item', 'group', isActive ? 'active' : '']"
              >
                <i
                  class="fa fa-star w-5 text-center transition-transform group-hover:scale-110"
                ></i>
                <span>评分管理</span>
                <div v-if="isActive" class="active-indicator"></div>
              </a>
            </router-link>
          </nav>
        </div>

        <div class="absolute bottom-0 w-full p-6 text-center">
          <p class="text-xs text-gray-400">© 2023 智启赛途 V1.0</p>
        </div>
      </aside>

      <main
        class="flex-1 p-6 lg:ml-64 w-full transition-all duration-300 min-h-[calc(100vh-4rem)] bg-gray-50"
      >
        <div class="max-w-7xl mx-auto">
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import useUserStore from "@/store/modules/user";
import "@/assets/styles/admin.scss";

const router = useRouter();
const userStore = useUserStore();

const dropdownOpen = ref(false);
const sidebarOpen = ref(false);

const toggleDropdown = () => {
  dropdownOpen.value = !dropdownOpen.value;
};

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value;
};

const handleLogout = async () => {
  try {
    await userStore.logOut();
    location.href = "/index";
  } catch (error) {
    console.error("Logout failed:", error);
    location.href = "/index";
  }
};

const closeDropdown = (e) => {
  if (!e.target.closest(".user-dropdown-container")) {
    dropdownOpen.value = false;
  }
};

const handleResize = () => {
  if (window.innerWidth >= 1024) {
    sidebarOpen.value = false;
  }
};

onMounted(() => {
  document.addEventListener("click", closeDropdown);
  window.addEventListener("resize", handleResize);
});

onUnmounted(() => {
  document.removeEventListener("click", closeDropdown);
  window.removeEventListener("resize", handleResize);
});
</script>

<style scoped lang="scss">
.sidebar-item {
  @apply relative flex items-center space-x-3 px-4 py-3.5 text-gray-500 rounded-xl transition-all duration-200 font-medium overflow-hidden;

  &:hover {
    @apply bg-gray-100 text-admin-600;
  }

  &.active {
    @apply bg-gray-100 text-admin-600 shadow-sm border border-admin-100;

    .active-indicator {
      @apply absolute left-0 top-1/2 -translate-y-1/2 w-1 h-8 bg-admin-500 rounded-r-full;
    }
  }
}

.user-avatar {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-weight: bold;
}

/* 路由过渡动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
