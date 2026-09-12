<template>
  <div class="p-8 bg-gray-50 min-h-screen animate-in">
    <div class="mb-8">
      <h2 class="text-3xl font-black text-gray-900 tracking-tight">个人中心</h2>
      <p class="text-gray-400 text-sm mt-1 font-medium">管理您的账号信息、安全设置及系统偏好</p>
    </div>

    <div class="grid grid-cols-12 gap-8">
      <div class="col-span-12 lg:col-span-4">
        <div class="bg-white rounded-[2.5rem] shadow-xl shadow-blue-100/50 border border-gray-100 overflow-hidden transition-all duration-500 hover:shadow-2xl hover:shadow-blue-200/50">
          <div class="h-32 bg-gradient-to-r from-blue-600 to-indigo-600"></div>
          <div class="px-8 pb-8">
            <div class="relative -mt-16 mb-6 flex justify-center">
              <div class="p-2 bg-white rounded-[2rem] shadow-lg group">
                <img :src="user.avatar || 'https://placehold.co/200x200?text=Admin'"
                     class="w-32 h-32 rounded-[1.5rem] object-cover border-4 border-gray-50 transition-transform duration-500 group-hover:scale-105" />
                <button class="absolute bottom-2 right-1/3 w-10 h-10 bg-blue-600 text-white rounded-full border-4 border-white hover:scale-110 active:scale-90 transition-all shadow-md">
                  <i class="fa fa-camera text-xs"></i>
                </button>
              </div>
            </div>

            <div class="text-center mb-8">
              <h3 class="text-2xl font-black text-gray-900">{{ user.nickName }}</h3>
              <p class="text-blue-600 font-bold text-sm uppercase tracking-widest mt-1">{{ user.roleGroup }}</p>
            </div>

            <div class="space-y-4 border-t border-gray-50 pt-6">
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-400 font-bold">账号名</span>
                <span class="text-gray-700 font-medium">{{ user.userName }}</span>
              </div>
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-400 font-bold">所属部门</span>
                <span class="text-gray-700 font-medium">{{ user.deptName }}</span>
              </div>
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-400 font-bold">注册日期</span>
                <span class="text-gray-700 font-medium">{{ user.createTime }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-span-12 lg:col-span-8">
        <div class="bg-white rounded-[2.5rem] shadow-sm border border-gray-100 min-h-[600px] flex flex-col overflow-hidden">

          <div class="flex items-center gap-2 px-8 py-6 border-b border-gray-50 bg-gray-50/30">
            <button v-for="tab in tabs" :key="tab.key"
                    @click="activeTab = tab.key"
                    :class="[
                      'relative px-8 py-3 text-sm font-bold transition-all duration-500 rounded-full flex items-center gap-2 overflow-hidden group',
                      activeTab === tab.key
                        ? 'bg-white text-blue-600 shadow-[0_10px_25px_-5px_rgba(59,130,246,0.2)] scale-105'
                        : 'text-gray-400 hover:bg-white/80 hover:text-gray-600 hover:shadow-sm'
                    ]">

              <span :class="[
                'w-1.5 h-1.5 rounded-full transition-all duration-500',
                activeTab === tab.key ? 'bg-blue-600 scale-125' : 'bg-gray-300 group-hover:bg-gray-400'
              ]"></span>

              {{ tab.label }}

              <div v-if="activeTab === tab.key"
                   class="absolute inset-0 border-2 border-blue-500/10 rounded-full animate-ping pointer-events-none">
              </div>
            </button>
          </div>

          <div class="p-10 flex-grow">
            <div v-if="activeTab === 'profile'" class="space-y-6 animate-in fade-in duration-500">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div class="space-y-2">
                  <label class="text-xs font-black text-gray-400 uppercase ml-2">用户昵称</label>
                  <input v-model="user.nickName" type="text" class="w-full px-6 py-4 bg-gray-50 border-none rounded-2xl focus:ring-2 focus:ring-blue-500 transition-all outline-none" />
                </div>
                <div class="space-y-2">
                  <label class="text-xs font-black text-gray-400 uppercase ml-2">手机号码</label>
                  <input v-model="user.phonenumber" type="text" class="w-full px-6 py-4 bg-gray-50 border-none rounded-2xl focus:ring-2 focus:ring-blue-500 transition-all outline-none" />
                </div>
              </div>
              <div class="space-y-2">
                <label class="text-xs font-black text-gray-400 uppercase ml-2">电子邮箱</label>
                <input v-model="user.email" type="email" class="w-full px-6 py-4 bg-gray-50 border-none rounded-2xl focus:ring-2 focus:ring-blue-500 transition-all outline-none" />
              </div>
              <div class="pt-6">
                <button @click="handleUpdate" class="group px-10 py-4 bg-blue-600 text-white rounded-2xl font-bold shadow-lg shadow-blue-100 hover:bg-blue-700 hover:scale-105 active:scale-95 transition-all flex items-center gap-2">
                  <span>保存修改</span>
                  <i class="fa fa-check-circle opacity-0 group-hover:opacity-100 transition-opacity"></i>
                </button>
              </div>
            </div>

            <div v-if="activeTab === 'security'" class="space-y-6 animate-in slide-in-from-right-4 duration-500">
              <div class="p-6 bg-orange-50 rounded-[2rem] border border-orange-100 flex items-center justify-between group hover:shadow-md transition-all">
                <div class="flex items-center gap-4">
                  <div class="w-12 h-12 bg-white rounded-2xl flex items-center justify-center text-orange-500 shadow-sm group-hover:scale-110 transition-transform">
                    <i class="fa fa-lock text-xl"></i>
                  </div>
                  <div>
                    <h4 class="font-bold text-gray-900">登录密码</h4>
                    <p class="text-xs text-gray-500">定期修改密码可以提高账号安全性</p>
                  </div>
                </div>
                <button class="px-6 py-2 bg-white border border-orange-200 text-orange-600 font-bold rounded-xl hover:bg-orange-600 hover:text-white transition-all shadow-sm">修改密码</button>
              </div>

              <div class="p-6 bg-blue-50 rounded-[2rem] border border-blue-100 flex items-center justify-between group hover:shadow-md transition-all">
                <div class="flex items-center gap-4">
                  <div class="w-12 h-12 bg-white rounded-2xl flex items-center justify-center text-blue-500 shadow-sm group-hover:scale-110 transition-transform">
                    <i class="fa fa-shield text-xl"></i>
                  </div>
                  <div>
                    <h4 class="font-bold text-gray-900">双重身份认证</h4>
                    <p class="text-xs text-gray-500">增加额外的安全保护层</p>
                  </div>
                </div>
                <div class="w-14 h-7 bg-blue-200 rounded-full relative cursor-pointer hover:bg-blue-300 transition-colors">
                  <div class="absolute left-1 top-1 w-5 h-5 bg-white rounded-full shadow-md"></div>
                </div>
              </div>
            </div>

            <div v-if="activeTab === 'logs'" class="space-y-4 animate-in slide-in-from-bottom-4 duration-500">
              <div v-for="i in 3" :key="i" class="flex gap-4 p-5 hover:bg-gray-50 rounded-3xl transition-all items-center border border-transparent hover:border-gray-100 group">
                <div class="w-2.5 h-2.5 rounded-full bg-blue-500 group-hover:scale-150 transition-transform"></div>
                <div class="flex-grow">
                  <p class="text-sm font-bold text-gray-800">修改了赛事 [2026全国创赛] 的报名截止时间</p>
                  <p class="text-xs text-gray-400">2026-03-12 17:41:00 · IP: 127.0.0.1</p>
                </div>
                <i class="fa fa-chevron-right text-gray-200 group-hover:text-blue-500 transition-colors"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const activeTab = ref('profile')
const tabs = [
  { label: '基本资料', key: 'profile' },
  { label: '安全设置', key: 'security' },
  { label: '活动日志', key: 'logs' }
]

const user = reactive({
  userName: 'admin',
  nickName: '系统管理员',
  roleGroup: '超级管理员',
  deptName: '技术研发部',
  phonenumber: '13800138000',
  email: 'admin@iflytek.com',
  avatar: '',
  createTime: '2025-10-01'
})

const handleUpdate = () => {
  // 这里可以加入 Loading 动画逻辑
  alert('✨ 资料更新成功，已同步至云端')
}
</script>

<style scoped>
/* 自定义进入动画 */
.animate-in {
  animation: pageFadeIn 0.6s ease-out;
}

@keyframes pageFadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 选项卡内容切换动画 */
.fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.98); }
  to { opacity: 1; transform: scale(1); }
}

/* 针对 iOS 风格开关的微调 */
.cursor-pointer div {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
</style>