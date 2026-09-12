import { createWebHistory, createRouter } from "vue-router";
/* Layout */
import Layout from "@/layout";
import { ElMessage } from "element-plus";
import { getUserProfile } from "@/api/system/user";
import {
  isStudentRegistrationProfileComplete,
  STUDENT_PROFILE_INCOMPLETE_MESSAGE,
} from "@/utils/studentProfile";

async function guardStudentRegistrationProfile(to) {
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

/**
 * Note: 路由配置项
 *
 * hidden: true                     // 当设置 true 的时候该路由不会再侧边栏出现 如401，login等页面，或者如一些编辑页面/edit/1
 * alwaysShow: true                 // 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
 * // 只有一个时，会将那个子路由当做根路由显示在侧边栏--如引导页面
 * // 若你想不管路由下面的 children 声明的个数都显示你的根路由
 * // 你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，一直显示根路由
 * redirect: noRedirect             // 当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'               // 设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * query: '{"id": 1, "name": "ry"}' // 访问路由的默认传递参数
 * roles: ['admin', 'common']       // 访问路由的角色权限
 * permissions: ['a:a:a', 'b:b:b']  // 访问路由的菜单权限
 * meta : {
 noCache: true                   // 如果设置为true，则不会被 <keep-alive> 缓存(默认 false)
 title: 'title'                  // 设置该路由在侧边栏和面包屑中展示的名字
 icon: 'svg-name'                // 设置该路由的图标，对应路径src/assets/icons/svg
 breadcrumb: false               // 如果设置为false，则不会在breadcrumb屑中显示
 activeMenu: '/system/user'      // 当路由设置了该属性，则会高亮相对应的侧边栏。
 }
 */

// 公共路由
export const constantRoutes = [
  {
    path: "/redirect",
    component: Layout,
    hidden: true,
    children: [
      {
        path: "/redirect/:path(.*)",
        component: () => import("@/views/redirect/index.vue"),
      },
    ],
  },
  {
    path: "/login",
    component: () => import("@/views/login"),
    hidden: true,
  },
  {
    path: "/xfc/login",
    component: () => import("@/views/xfc/login.vue"),
    hidden: true,
    meta: { title: "讯飞杯报名登录" },
  },
  {
    path: "/xfc",
    component: () => import("@/views/xfc/competition-introduction.vue"),
    hidden: true,
    meta: { title: "讯飞杯 AI+创新应用大赛" },
  },
  {
    path: "/xfc/registration",
    component: () => import("@/views/xfc/registration-home.vue"),
    hidden: true,
    meta: { title: "讯飞杯报名" },
  },
  {
    path: "/register",
    component: () => import("@/views/register"),
    hidden: true,
  },
  {
    path: "/:pathMatch(.*)*",
    component: () => import("@/views/error/404"),
    hidden: true,
  },
  {
    path: "/401",
    component: () => import("@/views/error/401"),
    hidden: true,
  },
  {
    path: "",
    component: Layout,

    // 讯飞杯为本项目唯一对外入口；旧竞赛平台登录仍保留在 /login。
    redirect: "/xfc",
    hidden: true,

    children: [
      {
        path: "/index",
        component: () => import("@/views/index"),
        name: "Index",
        meta: { title: "首页", icon: "dashboard", affix: true },
      },
    ],
  },
  {
    path: "/user",
    component: Layout,
    hidden: true,
    redirect: "noredirect",
    children: [
      {
        path: "profile/:activeTab?",
        component: () => import("@/views/system/user/profile/index"),
        name: "Profile",
        meta: { title: "个人中心", icon: "user" },
      },
    ],
  },

  {
    path: "/student",
    component: () => import("@/views/student/index"),
    hidden: true,
    meta: { roles: ["student"] },
  },
  {
    path: "/competition",
    component: () => import("@/views/student/competition"),
    name: "StudentCompetition",

    hidden: true,
    meta: { roles: ["student"] },
  },
  {
    path: "/learning-resources",
    component: () => import("@/views/student/learning-resources/index.vue"),
    hidden: true,
    meta: { roles: ["student"] },
  },
  {
    path: "/ai-assistant/mock-defense",
    component: () => import("@/views/student/ai-assistant/mock-defense.vue"),
    hidden: true,
    meta: { roles: ["student"], title: "模拟答辩" },
  },
  {
    path: "/ai-assistant/material-optimization",
    component: () => import("@/views/student/ai-assistant/material-optimization.vue"),
    hidden: true,
    meta: { roles: ["student"], title: "材料优化" },
  },
  {
    path: "/competition-guide",
    component: () => import("@/views/student/competition-guide/index.vue"),
    hidden: true,
    meta: { roles: ["student"] },
  },
  {
    path: "/team-matching",
    component: () => import("@/views/student/team-matching/index.vue"),
    hidden: true,
    meta: { roles: ["student"], title: "智能组队" },
  },
  {
    path: "/ability-profile",
    component: () => import("@/views/student/ability-profile/index.vue"),
    hidden: true,
    meta: { roles: ["student"], title: "能力画像" },
  },
  {
    path: "/discussion",
    component: () => import("@/views/student/discussion.vue"),
    hidden: true,
    meta: { roles: ["student"] },
  },
  {
    path: "/competition/register/:id",
    component: () => import("@/views/student/competition-register.vue"),
    hidden: true,
    meta: { roles: ["student"] },
    beforeEnter: guardStudentRegistrationProfile,
  },
  {path: '/mock-exam',
    component: () => import('@/views/student/mock-exam'),
    hidden: true,
    meta: { roles: ['student'], title: '模拟考试' }
  },
  {
    path: '/mock-exam/take/:id',
    component: () => import('@/views/student/mock-exam-take.vue'),
    hidden: true,
    meta: { roles: ['student'], title: '模拟考试' }
  },
  {
    path: '/practice',
    component: () => import('@/views/student/practice'),
    hidden: true,
    meta: { roles: ['student'], title: '刷题训练' }
  },
  {
    path: "/student-center/profile",
    component: () => import("@/views/student/profile/index.vue"),
    hidden: true,
    meta: { roles: ["student", "sys_admin", "sys admin", "admin"] },
  },
  {
    path: "/student-registered-competitions",
    component: () => import("@/views/student/registered-competitions.vue"),
    hidden: true,
    meta: { roles: ["student"] },
  },
  {
    path: "/student-work-detail/:registerId",
    component: () => import("@/views/student/work-detail.vue"),
    hidden: true,
    meta: { roles: ["student"] },
  },
  {
    path: "/teacher",
    component: () => import("@/views/teacher/index"),
    hidden: true,
    meta: { roles: ["teacher"] },
  },
  {
    path: "/teacher/review/:competitionId/:participationId",
    component: () => import("@/views/teacher/review/CompetitionReview.vue"),
    hidden: true,
    meta: { roles: ["teacher"] },
  },
  {
    path: "/teacher/review/:competitionId/detail",
    component: () => import("@/views/teacher/review/CompetitionDetail.vue"),
    hidden: true,
    meta: { roles: ["teacher"] },
  },
  {
    path: "/teacher/profile",
    component: () => import("@/views/student/profile/index.vue"),
    hidden: true,
    meta: { roles: ["teacher"] },
  },

  {
    path: "/sys-admin",
    component: () => import("@/views/sys-admin/layout"),
    hidden: true,
    meta: { roles: ["sys_admin", "admin", "comp_admin"] },
    redirect: "/sys-admin/competition-list",
    children: [
      {
        path: "competition-management", // 新增的赛事管理路由
        component: () => import("@/views/sys-admin/competition-management"),
        name: "SysAdminCompetitionManagement",
        meta: {
          title: "发布赛事",
          roles: ["sys_admin", "admin", "comp_admin"],
        },
      },
      {
        path: "competition-list",
        component: () => import("@/views/sys-admin/competition-list"),
        name: "SysAdminCompetitionList",
        meta: {
          title: "智能赛事管理",
          roles: ["sys_admin", "admin", "comp_admin"],
        },
      },
      {
        path: "user-management",
        component: () => import("@/views/sys-admin/user-management"),
        name: "SysAdminUserManagement",
        meta: {
          title: "用户中心",
          roles: ["sys_admin", "admin", "comp_admin"],
        },
      },
      {
        path: "teacher-management",
        component: () => import("@/views/sys-admin/teacher-management"),
        name: "SysAdminTeacherManagement",
        meta: {
          title: "教师管理",
          roles: ["sys_admin", "admin", "comp_admin"],
        },
      },
      {
        path: "student-management",
        component: () => import("@/views/sys-admin/student-management"),
        name: "SysAdminStudentManagement",
        meta: {
          title: "学生管理",
          roles: ["sys_admin", "admin", "comp_admin"],
        },
      },
      {
        path: "competition-screening",
        component: () => import("@/views/sys-admin/competition-screening"),
        name: "SysAdminCompetitionScreening",
        meta: {
          title: "队伍筛选",
          roles: ["sys_admin", "admin", "comp_admin"],
        },
      },
      {
        path: "competition-management",
        component: () => import("@/views/sys-admin/competition-management"),
        name: "SysAdminCompetitionManagement",
        meta: {
          title: "发布赛事",
          roles: ["sys_admin", "admin", "comp_admin"],
        },
      },
      {
        path: "teacher-empowerment",
        component: () => import("@/views/sys-admin/teacher-empowerment"),
        name: "SysAdminTeacherEmpowerment",
        meta: {
          title: "教师赋权",
          roles: ["sys_admin", "admin", "comp_admin"],
        },
      },
      {
        path: "score-management",
        component: () => import("@/views/sys-admin/score-management"),
        name: "SysAdminScoreManagement",
        meta: {
          title: "评分管理",
          roles: ["sys_admin", "admin", "comp_admin"],
        },
      },
      {
        path: "index",
        component: () => import("@/views/sys-admin/index"),
        name: "SysAdminIndex",
        meta: { title: "首页", roles: ["sys_admin", "admin", "comp_admin"] },
      },
      {
        path: "profile",
        component: () => import("@/views/admin/competition/profile.vue"),
        name: "AdminProfile",
        meta: { title: "个人中心", roles: ["sys_admin", "admin", "comp_admin"] }
      },
    ],
  },

  // 重点修改：管理员端详情页路由配置
  {
    path: "/admin/competition-detail/:id",
    // 逻辑补全：指向你刚在 admin/competition 目录下新建的详情文件
    component: () => import("@/views/admin/competition/detail"),
    hidden: true,
    meta: {
      title: "赛事详情",
      // 显性权限补全：确保管理员能够进入
      roles: ["sys_admin", "admin", "comp_admin"],
      // 逻辑补全：确保进入详情页后，侧边栏的“赛事总览”高亮不消失
      activeMenu: "/sys-admin/competition-list"
    },
  },

  // 新增：队伍作品详情页路由配置
  {
    path: "/admin/competition-team/:id/:registerId/index",
    component: () => import("@/views/admin/competition/teamDetail"),
    name: "TeamDetail",
    hidden: true,
    meta: {
      title: "队伍作品详情",
      roles: ["sys_admin", "admin", "comp_admin"],
      activeMenu: "/sys-admin/competition-list"
    },
  },
  // 队伍详情页面
  {
    path: "/admin/competition/team-detail/:teamId",
    component: () => import("@/views/admin/competition/teamDetail"),
    hidden: true,
    meta: {
      title: "队伍详情",
      roles: ["sys_admin", "admin", "comp_admin"],
      activeMenu: "/sys-admin/competition-list"
    },
  },

  // 学生端竞赛详情路由
  {
    path: "/competition/detail/:id",
    component: () => import("@/views/student/competition-detail.vue"),
    hidden: true,
    meta: { roles: ["student"] },
  },
];

// 动态路由，基于用户权限动态去加载
export const dynamicRoutes = [
  {
    path: "/system/user-auth",
    component: Layout,
    hidden: true,
    permissions: ["system:user:edit"],
    children: [
      {
        path: "role/:userId(\\d+)",
        component: () => import("@/views/system/user/authRole"),
        name: "AuthRole",
        meta: { title: "分配角色", activeMenu: "/system/user" },
      },
    ],
  },
  {
    path: "/system/role-auth",
    component: Layout,
    hidden: true,
    permissions: ["system:role:edit"],
    children: [
      {
        path: "user/:roleId(\\d+)",
        component: () => import("@/views/system/role/authUser"),
        name: "AuthUser",
        meta: { title: "分配用户", activeMenu: "/system/role" },
      },
    ],
  },
  {
    path: "/system/dict-data",
    component: Layout,
    hidden: true,
    permissions: ["system:dict:list"],
    children: [
      {
        path: "index/:dictId(\\d+)",
        component: () => import("@/views/system/dict/data"),
        name: "Data",
        meta: { title: "字典数据", activeMenu: "/system/dict" },
      },
    ],
  },
  {
    path: "/monitor/job-log",
    component: Layout,
    hidden: true,
    permissions: ["monitor:job:list"],
    children: [
      {
        path: "index/:jobId(\\d+)",
        component: () => import("@/views/monitor/job/log"),
        name: "JobLog",
        meta: { title: "调度日志", activeMenu: "/monitor/job" },
      },
    ],
  },
  {
    path: "/tool/gen-edit",
    component: Layout,
    hidden: true,
    permissions: ["tool:gen:edit"],
    children: [
      {
        path: "index/:tableId(\\d+)",
        component: () => import("@/views/tool/gen/editTable"),
        name: "GenEdit",
        meta: { title: "修改生成配置", activeMenu: "/tool/gen" },
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    }
    return { top: 0 };
  },
});

export default router;
