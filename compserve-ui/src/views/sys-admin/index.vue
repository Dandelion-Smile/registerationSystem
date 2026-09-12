<template>
  <div class="sys-admin-container flex min-h-screen bg-gray-50 font-sans antialiased text-gray-800">
    <!-- 顶部导航栏 -->
    <header class="fixed top-0 left-0 right-0 bg-admin-600/90 text-white shadow-lg z-50 transparent-nav">
      <div class="container mx-auto px-4 py-3">
        <div class="flex items-center justify-between">
          <!-- 返回按钮 -->
          <div class="flex items-center">
             <!-- Placeholder for back button if needed -->
          </div>

          <!-- 居中标题 -->
          <div class="flex-1 text-center">
            <h1 class="text-xl md:text-2xl font-bold tracking-wide">
              <i class="fa fa-trophy mr-2 text-yellow-300"></i>
              "智启赛途" 高校竞赛AI智驭平台
            </h1>
          </div>

          <!-- 右侧占位元素 -->
          <div class="w-24"></div>
        </div>
      </div>
    </header>

    <!-- 主体布局 (添加 pt-16 以适应固定顶部栏) -->
    <div class="flex w-full pt-16">
      <!-- 左侧导航栏 -->
      <aside class="w-64 bg-white border-r border-gray-200 shadow-sm fixed h-full overflow-y-auto">
        <nav class="p-4 space-y-1">
          <p class="text-xs font-semibold text-gray-500 uppercase tracking-wider px-4 py-2">智能功能</p>

          <a href="#" @click.prevent="activeTab = 'management'" :class="['sidebar-item', activeTab === 'management' ? 'active' : '']">
            <i class="fa fa-filter w-5 text-center"></i>
            <span>智能赛事管理</span>
          </a>

          <a href="#" @click.prevent="activeTab = 'report'" :class="['sidebar-item', activeTab === 'report' ? 'active' : '']">
            <i class="fa fa-file-text-o w-5 text-center"></i>
            <span>赛事总览报告</span>
          </a>

          <a href="#" @click.prevent="activeTab = 'user'" :class="['sidebar-item', activeTab === 'user' ? 'active' : '']">
            <i class="fa fa-user-circle-o w-5 text-center"></i>
            <span>用户中心管理</span>
          </a>

          <a href="#" @click.prevent="activeTab = 'screening'" :class="['sidebar-item', activeTab === 'screening' ? 'active' : '']">
            <i class="fa fa-filter w-5 text-center"></i>
            <span>队伍晋级筛选</span>
          </a>
        </nav>
      </aside>

      <!-- 主要内容区 (添加 ml-64 以适应固定侧边栏) -->
      <main class="flex-1 p-6 ml-64 overflow-auto">
        <div class="max-w-7xl mx-auto space-y-8">
          <!-- 页面标题 -->
          <div class="flex items-center justify-between">
            <div>
              <h2 class="text-2xl font-bold text-gray-800">智能赛事管理</h2>
              <p class="text-gray-500 mt-1">全生命周期赛事管理，高效处理赛事事务</p>
            </div>
            <div class="flex gap-3">
               <!-- Actions -->
            </div>
          </div>

          <!-- 赛事创建功能区 (Toggleable) -->
          <section v-if="showPublishSection" class="card fade-in">
            <div class="p-6 border-b border-gray-100">
              <h3 class="text-xl font-semibold flex items-center gap-2">
                <i class="fa fa-file-text-o text-admin-500"></i>
                赛事创建功能区
              </h3>
            </div>
            <div class="p-6">
              <form class="grid grid-cols-1 md:grid-cols-2 gap-6" @submit.prevent>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">赛事名称 <span class="text-red-500">*</span></label>
                  <input type="text" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-admin-200 focus:border-admin-500 transition-colors" placeholder="请输入赛事名称">
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">举办时间 <span class="text-red-500">*</span></label>
                  <input type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-admin-200 focus:border-admin-500 transition-colors">
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">赛事类别 <span class="text-red-500">*</span></label>
                  <select class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-admin-200 focus:border-admin-500 transition-colors">
                    <option value="">请选择赛事类别</option>
                    <option>编程竞赛</option>
                    <option>设计竞赛</option>
                    <option>创新创业竞赛</option>
                    <option>学科竞赛</option>
                  </select>
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">参赛对象范围 <span class="text-red-500">*</span></label>
                  <input type="text" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-admin-200 focus:border-admin-500 transition-colors" placeholder="例如：全体本科生">
                </div>

                <div class="md:col-span-2">
                  <label class="block text-sm font-medium text-gray-700 mb-1">赛事简介</label>
                  <textarea rows="4" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-admin-200 focus:border-admin-500 transition-colors" placeholder="请输入赛事详细介绍..."></textarea>
                </div>

                <div class="md:col-span-2">
                  <h4 class="text-sm font-medium text-gray-700 mb-3">赛事流程节点设置</h4>
                  <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
                    <div>
                      <label class="block text-xs text-gray-500 mb-1">报名截止时间</label>
                      <input type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-admin-200 focus:border-admin-500 transition-colors">
                    </div>
                    <div>
                      <label class="block text-xs text-gray-500 mb-1">初赛时间</label>
                      <input type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-admin-200 focus:border-admin-500 transition-colors">
                    </div>
                    <div>
                      <label class="block text-xs text-gray-500 mb-1">决赛时间</label>
                      <input type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-admin-200 focus:border-admin-500 transition-colors">
                    </div>
                  </div>
                </div>

                <div class="md:col-span-2">
                  <label class="block text-sm font-medium text-gray-700 mb-1">上传赛事相关文件</label>
                  <div class="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-lg hover:border-admin-300 transition-colors">
                    <div class="space-y-1 text-center">
                      <i class="fa fa-cloud-upload text-gray-400 text-3xl mb-2"></i>
                      <div class="flex text-sm text-gray-600 justify-center">
                        <label class="relative cursor-pointer bg-white rounded-md font-medium text-admin-600 hover:text-admin-500 focus-within:outline-none">
                          <span>上传文件</span>
                          <input type="file" class="sr-only">
                        </label>
                        <p class="pl-1">或拖放文件</p>
                      </div>
                      <p class="text-xs text-gray-500">支持 PDF, Word, JPG 格式，最大 10MB</p>
                    </div>
                  </div>
                </div>

                <div class="md:col-span-2 flex justify-end gap-3 pt-2">
                  <button type="button" class="btn-admin-outline">保存草稿</button>
                  <button type="submit" class="btn-admin-primary">提交创建申请</button>
                </div>
              </form>
            </div>
          </section>

          <!-- 赛事审核列表 -->
          <section class="card fade-in" style="animation-delay: 0.1s;">
            <div class="p-6 border-b border-gray-100">
              <h3 class="text-xl font-semibold flex items-center gap-2">
                <i class="fa fa-check-square-o text-admin-500"></i>
                赛事审核列表
              </h3>
            </div>
            <div class="p-6">
              <div class="overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                  <thead class="bg-gray-50">
                    <tr>
                      <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">赛事名称</th>
                      <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">创建人</th>
                      <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">创建时间</th>
                      <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
                      <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
                    </tr>
                  </thead>
                  <tbody class="bg-white divide-y divide-gray-200">
                    <tr v-for="(item, index) in auditList" :key="index">
                      <td class="px-6 py-4 whitespace-nowrap">
                        <div class="text-sm font-medium text-gray-900">{{ item.name }}</div>
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap">
                        <div class="text-sm text-gray-500">{{ item.creator }}</div>
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap">
                        <div class="text-sm text-gray-500">{{ item.createTime }}</div>
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap">
                        <span class="badge badge-blue">{{ item.status }}</span>
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <div class="flex gap-2">
                          <button class="text-blue-600 hover:text-blue-900" @click="viewDetails(item)">查看</button>
                          <button class="text-green-600 hover:text-green-900" @click="approveItem(item)">通过</button>
                          <button class="text-red-600 hover:text-red-900" @click="rejectItem(item)">驳回</button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <!-- 分页 -->
              <div class="flex items-center justify-between pt-4">
                <div class="text-sm text-gray-500">显示 1 至 2 条，共 2 条</div>
                <div class="flex gap-1">
                  <button class="px-3 py-1 border border-gray-300 rounded-md bg-white text-gray-500 hover:bg-gray-50 disabled:opacity-50" disabled>
                    <i class="fa fa-chevron-left text-xs"></i>
                  </button>
                  <button class="px-3 py-1 border border-admin-500 bg-admin-50 text-admin-600 rounded-md">1</button>
                  <button class="px-3 py-1 border border-gray-300 rounded-md bg-white text-gray-500 hover:bg-gray-50 disabled:opacity-50" disabled>
                    <i class="fa fa-chevron-right text-xs"></i>
                  </button>
                </div>
              </div>
            </div>
          </section>

          <!-- 赛事运营模块 -->
          <section class="card fade-in" style="animation-delay: 0.2s;">
            <div class="p-6 border-b border-gray-100">
              <h3 class="text-xl font-semibold flex items-center gap-2">
                <i class="fa fa-cogs text-admin-500"></i>
                赛事运营模块
              </h3>
            </div>
            <div class="p-6">
              <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <!-- 发布补充通知 -->
                <div class="bg-white rounded-lg shadow-sm p-5 border border-gray-100 hover:shadow-md transition-shadow">
                  <div class="text-admin-500 text-3xl mb-3">
                    <i class="fa fa-bullhorn"></i>
                  </div>
                  <h4 class="text-lg font-medium mb-1">发布补充通知</h4>
                  <p class="text-gray-500 text-sm mb-4">向参赛队伍发布赛事补充说明、变更通知等信息</p>
                  <button class="btn-admin-primary w-full" @click="openModal('notice-modal')">进入</button>
                </div>

                <!-- 管理报名情况 -->
                <div class="bg-white rounded-lg shadow-sm p-5 border border-gray-100 hover:shadow-md transition-shadow">
                  <div class="text-admin-500 text-3xl mb-3">
                    <i class="fa fa-users"></i>
                  </div>
                  <h4 class="text-lg font-medium mb-1">管理报名情况</h4>
                  <p class="text-gray-500 text-sm mb-4">查看报名队伍列表、审核报名资格、管理参赛人员信息</p>
                  <button class="btn-admin-primary w-full" @click="openModal('registration-modal')">进入</button>
                </div>

                <!-- 安排赛事评委 -->
                <div class="bg-white rounded-lg shadow-sm p-5 border border-gray-100 hover:shadow-md transition-shadow">
                  <div class="text-admin-500 text-3xl mb-3">
                    <i class="fa fa-user-o"></i>
                  </div>
                  <h4 class="text-lg font-medium mb-1">安排赛事评委</h4>
                  <p class="text-gray-500 text-sm mb-4">邀请并管理赛事评委，分配评审任务和时间安排</p>
                  <button class="btn-admin-primary w-full" @click="openModal('judge-modal')">进入</button>
                </div>
              </div>
            </div>
          </section>

          <!-- 赛事归档区 -->
          <section class="card fade-in" style="animation-delay: 0.3s;">
             <div class="p-6 border-b border-gray-100">
               <h3 class="text-xl font-semibold flex items-center gap-2">
                 <i class="fa fa-archive text-admin-500"></i>
                 赛事归档区
               </h3>
             </div>
             <div class="p-6">
               <!-- Table Content -->
               <div class="overflow-x-auto">
                 <table class="min-w-full divide-y divide-gray-200">
                   <thead class="bg-gray-50">
                     <tr>
                       <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">赛事名称</th>
                       <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">结束时间</th>
                       <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">参赛人数</th>
                       <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
                     </tr>
                   </thead>
                   <tbody class="bg-white divide-y divide-gray-200">
                     <tr>
                       <td class="px-6 py-4 whitespace-nowrap"><div class="text-sm font-medium text-gray-900">2023年创新实践项目竞赛</div></td>
                       <td class="px-6 py-4 whitespace-nowrap"><div class="text-sm text-gray-500">2023-06-15</div></td>
                       <td class="px-6 py-4 whitespace-nowrap"><div class="text-sm text-gray-500">287人</div></td>
                       <td class="px-6 py-4 whitespace-nowrap text-sm font-medium"><button class="text-blue-600 hover:text-blue-900">查看详情</button></td>
                     </tr>
                     <tr>
                        <td class="px-6 py-4 whitespace-nowrap"><div class="text-sm font-medium text-gray-900">数据结构与算法设计竞赛</div></td>
                        <td class="px-6 py-4 whitespace-nowrap"><div class="text-sm text-gray-500">2023-05-22</div></td>
                        <td class="px-6 py-4 whitespace-nowrap"><div class="text-sm text-gray-500">156人</div></td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium"><button class="text-blue-600 hover:text-blue-900">查看详情</button></td>
                     </tr>
                   </tbody>
                 </table>
               </div>
             </div>
          </section>

        </div>
      </main>
    </div>

    <!-- Modals -->
    <div v-if="activeModal" id="modal-container" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4" @click.self="closeModal">
      <!-- 发布补充通知弹窗 -->
      <div v-if="activeModal === 'notice-modal'" class="modal bg-white rounded-xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-auto">
        <div class="p-6 border-b border-gray-100 flex justify-between items-center">
          <h3 class="text-xl font-semibold">审核发布补充通知</h3>
          <button class="text-gray-500 hover:text-gray-800" @click="closeModal">
            <i class="fa fa-times text-xl"></i>
          </button>
        </div>
        <div class="p-6">
          <form class="space-y-6" @submit.prevent="closeModal">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">通知标题</label>
              <input type="text" class="w-full px-3 py-2 border border-gray-300 rounded-lg" placeholder="请输入通知标题">
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">通知内容</label>
              <textarea rows="6" class="w-full px-3 py-2 border border-gray-300 rounded-lg" placeholder="请输入通知详细内容..."></textarea>
            </div>
            <div>
               <label class="block text-sm font-medium text-gray-700 mb-1">发布范围</label>
               <select class="w-full px-3 py-2 border border-gray-300 rounded-lg">
                   <option>所有参赛队伍</option>
                   <option>初赛队伍</option>
                   <option>决赛队伍</option>
               </select>
            </div>
            <div class="flex justify-end gap-3">
              <button type="button" class="btn-admin-outline" @click="closeModal">取消</button>
              <button type="submit" class="btn-admin-primary">发布通知</button>
            </div>
          </form>
        </div>
      </div>

      <!-- 管理报名情况弹窗 -->
      <div v-if="activeModal === 'registration-modal'" class="modal bg-white rounded-xl shadow-xl w-full max-w-4xl max-h-[90vh] overflow-auto">
        <div class="p-6 border-b border-gray-100 flex justify-between items-center">
          <h3 class="text-xl font-semibold">管理报名情况</h3>
          <button class="text-gray-500 hover:text-gray-800" @click="closeModal">
            <i class="fa fa-times text-xl"></i>
          </button>
        </div>
        <div class="p-6">
           <div class="flex justify-between items-center mb-6">
             <div><p class="text-sm text-gray-500">共收到 <span class="font-medium text-admin-600">28</span> 份报名申请</p></div>
             <div><input type="text" placeholder="搜索队伍名称" class="pl-3 pr-3 py-1 border border-gray-300 rounded-lg text-sm"></div>
           </div>
           <div class="overflow-x-auto">
             <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">队伍名称</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">队长</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">队员</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">报名状态</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr>
                    <td class="px-6 py-4 whitespace-nowrap"><div class="text-sm font-medium text-gray-900">编程达人队</div></td>
                    <td class="px-6 py-4 whitespace-nowrap"><div class="text-sm text-gray-500">张三（计算机学院）</div></td>
                    <td class="px-6 py-4 whitespace-nowrap"><div class="text-sm text-gray-500">李四、王五</div></td>
                    <td class="px-6 py-4 whitespace-nowrap"><span class="badge badge-blue">待审核</span></td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div class="flex gap-2">
                        <button class="text-green-600 hover:text-green-900">通过</button>
                        <button class="text-red-600 hover:text-red-900">驳回</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
             </table>
           </div>
           <div class="flex justify-end mt-6">
             <button class="btn-admin-outline" @click="closeModal">关闭</button>
           </div>
        </div>
      </div>

      <!-- 安排赛事评委弹窗 -->
      <div v-if="activeModal === 'judge-modal'" class="modal bg-white rounded-xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-auto">
        <div class="p-6 border-b border-gray-100 flex justify-between items-center">
            <h3 class="text-xl font-semibold">审核安排赛事评委</h3>
            <button class="text-gray-500 hover:text-gray-800" @click="closeModal">
                <i class="fa fa-times text-xl"></i>
            </button>
        </div>
        <div class="p-6">
            <div class="mb-6">
                <h4 class="text-lg font-medium mb-3">已添加评委</h4>
                <div class="space-y-3">
                    <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                        <div class="flex items-center gap-3">
                            <img src="https://ui-avatars.com/api/?background=random&name=User" alt="评委头像" class="w-10 h-10 rounded-full">
                            <div>
                                <p class="font-medium">王教授</p>
                                <p class="text-sm text-gray-500">计算机学院</p>
                            </div>
                        </div>
                        <button class="text-red-500 hover:text-red-700"><i class="fa fa-trash"></i></button>
                    </div>
                </div>
            </div>
            <div>
                <h4 class="text-lg font-medium mb-3">添加新评委</h4>
                <form class="space-y-6" @submit.prevent>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">评委姓名</label>
                        <input type="text" class="w-full px-3 py-2 border border-gray-300 rounded-lg" placeholder="请输入评委姓名">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">所属单位</label>
                        <input type="text" class="w-full px-3 py-2 border border-gray-300 rounded-lg" placeholder="请输入所属单位">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">专业领域</label>
                        <select class="w-full px-3 py-2 border border-gray-300 rounded-lg">
                            <option>程序设计</option>
                            <option>人工智能</option>
                        </select>
                    </div>
                    <div class="flex justify-end gap-3">
                        <button type="button" class="btn-admin-outline" @click="closeModal">取消</button>
                        <button type="submit" class="btn-admin-primary">添加评委</button>
                    </div>
                </form>
            </div>
        </div>
      </div>

      <!-- Confirm Modal -->
      <div v-if="activeModal === 'confirm-modal'" class="modal bg-white rounded-xl shadow-xl w-full max-w-md max-h-[90vh] overflow-auto">
         <div class="p-6 border-b border-gray-100 flex justify-between items-center">
           <h3 class="text-xl font-semibold">确认操作</h3>
           <button class="text-gray-500 hover:text-gray-800" @click="closeModal"><i class="fa fa-times text-xl"></i></button>
         </div>
         <div class="p-6">
           <p class="text-gray-700 mb-6">{{ confirmMessage }}</p>
           <div class="flex justify-end gap-3">
             <button type="button" class="btn-admin-outline" @click="closeModal">取消</button>
             <button type="button" class="btn-admin-primary" @click="confirmAction">确定</button>
           </div>
         </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeTab = ref('management')
const showPublishSection = ref(false) // Hidden by default in static html? Static HTML has 'hidden' class initially
const activeModal = ref(null)
const confirmMessage = ref('')
let confirmCallback = null

const auditList = ref([
  {
    name: '全国大学生程序设计大赛',
    creator: '张教授（计算机学院）',
    createTime: '2023-10-15 09:23',
    status: '待审核'
  },
  {
    name: '人工智能创新设计大赛',
    creator: '李老师（智能科学系）',
    createTime: '2023-10-14 15:47',
    status: '待审核'
  }
])

const openModal = (id) => {
  activeModal.value = id
}

const closeModal = () => {
  activeModal.value = null
}

const approveItem = (item) => {
  confirmMessage.value = `您确定要通过赛事 "${item.name}" 的审核吗？`
  confirmCallback = () => {
    // Perform action
    console.log('Approved', item)
    closeModal()
  }
  activeModal.value = 'confirm-modal'
}

const rejectItem = (item) => {
  confirmMessage.value = `您确定要驳回赛事 "${item.name}" 吗？`
  confirmCallback = () => {
    console.log('Rejected', item)
    closeModal()
  }
  activeModal.value = 'confirm-modal'
}

const confirmAction = () => {
  if (confirmCallback) confirmCallback()
}

const viewDetails = (item) => {
    console.log('View', item)
}
</script>

<style scoped lang="scss">
.sys-admin-container {
  /* Scope wrapper if needed, but scoped style handles it */
}

/* Utilities */
.btn-admin-primary {
  @apply bg-gradient-to-r from-admin-600 to-admin-500 hover:from-admin-700 hover:to-admin-600 text-white font-medium py-2 px-4 rounded-lg transition-all duration-300 shadow-md;
}

.btn-admin-outline {
  @apply border border-gray-300 text-gray-700 hover:bg-gray-50 font-medium py-2 px-4 rounded-lg transition-all duration-300;
}

.sidebar-item {
  @apply flex items-center space-x-3 px-4 py-3 text-gray-600 hover:bg-gray-50 hover:text-admin-600 rounded-lg transition-colors duration-200 block;
  
  &.active {
    @apply bg-admin-50 text-admin-600 font-medium border-l-4 border-admin-500;
  }
}

.card {
  @apply bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden;
}

.badge {
  @apply px-2 py-1 text-xs font-medium rounded-full;
  
  &.badge-blue {
    @apply bg-blue-50 text-blue-600;
  }
}

/* Animations */
.fade-in {
  animation: fadeIn 0.5s ease-out forwards;
  opacity: 0; /* Start hidden */
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Layout helpers */
.transparent-nav {
  backdrop-filter: blur(10px);
}

/* Modal specific */
.modal {
  @apply transform transition-all;
}
</style>
