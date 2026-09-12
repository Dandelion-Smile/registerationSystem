import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isHttp, isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'
import { getXfcToken } from '@/utils/xfcAuth'
import { getUserProfile } from '@/api/system/user'
import {
  isStudentRegistrationProfileComplete,
  STUDENT_PROFILE_INCOMPLETE_MESSAGE
} from '@/utils/studentProfile'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register', '/auth-redirect', '/bind', '/404', '/401']
const isXfcRoute = (path) => typeof path === 'string' && (path === '/xfc' || path.startsWith('/xfc/'))
const XFC_PAGE_TITLE = '河南工业大学第三届“讯飞杯”AI+创新应用大赛报名平台'

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

const isCompetitionRegisterRoute = (path) => {
  return typeof path === 'string' && path.startsWith('/competition/register/')
}

async function blockIncompleteStudentRegisterRoute(to, next) {
  if (!isCompetitionRegisterRoute(to.path)) {
    return false
  }
  try {
    const response = await getUserProfile()
    if (isStudentRegistrationProfileComplete(response?.data || {})) {
      return false
    }
    ElMessage.warning(STUDENT_PROFILE_INCOMPLETE_MESSAGE)
    next(false)
    NProgress.done()
    return true
  } catch (error) {
    ElMessage.error('获取个人信息失败，请稍后重试')
    next(false)
    NProgress.done()
    return true
  }
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  console.log('[Permission] To:', to.path, 'From:', from.path)

  // 讯飞杯使用独立 Token，不进入旧平台的用户角色和路由流程。
  if (isXfcRoute(to.path)) {
    document.title = XFC_PAGE_TITLE
    if (to.path === '/xfc/login' || to.path === '/xfc') {
      next()
    } else if (getXfcToken()) {
      next()
    } else {
      next({ path: '/xfc/login', query: { redirect: to.fullPath } })
    }
    return
  }

  if (getToken()) {
    const userStore = useUserStore()
    const permissionStore = usePermissionStore()

    to.meta.title && useSettingsStore().setTitle(to.meta.title)

    if (to.path === '/login' || to.path === '/register') {
      if (userStore.roles.length === 0) {
        userStore.getInfo().then(() => {
          redirectBasedOnRole(userStore.roles, next)
          NProgress.done()
        })
      } else {
        redirectBasedOnRole(userStore.roles, next)
        NProgress.done()
      }
      return
    }

    blockIncompleteStudentRegisterRoute(to, next).then(blocked => {
      if (blocked) {
        return
      }

      if (userStore.roles.length === 0) {
        isRelogin.show = true
        userStore.getInfo().then(() => {
          isRelogin.show = false
          if (shouldForceLoginForIndex(to, userStore.roles)) {
            userStore.logOut().then(() => {
              next(`/login?redirect=${to.fullPath}`)
              NProgress.done()
            })
            return
          }
          permissionStore.generateRoutes().then(accessRoutes => {
            accessRoutes.forEach(route => {
              if (!isHttp(route.path)) {
                router.addRoute(route)
              }
            })
            if (checkRoleAccess(to, userStore.roles, next)) {
              next({ ...to, replace: true })
            }
          })
        }).catch(err => {
          userStore.logOut().then(() => {
            ElMessage.error(err)
            next({ path: '/' })
          })
        })
        return
      }

      if (shouldForceLoginForIndex(to, userStore.roles)) {
        userStore.logOut().then(() => {
          next(`/login?redirect=${to.fullPath}`)
          NProgress.done()
        })
        return
      }

      if (permissionStore.sidebarRouters.length === 0) {
        permissionStore.generateRoutes().then(accessRoutes => {
          accessRoutes.forEach(route => {
            if (!isHttp(route.path)) {
              router.addRoute(route)
            }
          })
          if (checkRoleAccess(to, userStore.roles, next)) {
            next({ ...to, replace: true })
          }
        })
      } else if (checkRoleAccess(to, userStore.roles, next)) {
        next()
      }
    })
    return
  }

  if (isWhiteList(to.path)) {
    next()
  } else {
    next(`/login?redirect=${to.fullPath}`)
    NProgress.done()
  }
})

function redirectBasedOnRole(roles, next) {
  console.log('[Permission] Redirecting based on roles:', roles)
  if (roles.includes('student')) next('/student')
  else if (roles.includes('teacher')) next('/teacher')
  else if (roles.includes('sys_admin') || roles.includes('admin') || roles.includes('comp_admin')) next('/sys-admin')
  else next('/index')
}

function shouldForceLoginForIndex(to, roles) {
  return to.path === '/index' && !isAdminOnlyAccount(roles)
}

function isAdminOnlyAccount(roles) {
  return roles.includes('admin') && !roles.includes('sys_admin')
}

function checkRoleAccess(to, roles, next) {
  if (to.meta && to.meta.roles && to.meta.roles.length > 0) {
    const hasRole = roles.some(role => to.meta.roles.includes(role))
    if (!hasRole) {
      redirectBasedOnRole(roles, next)
      NProgress.done()
      return false
    }
  }
  return true
}

router.afterEach(() => {
  NProgress.done()
})
