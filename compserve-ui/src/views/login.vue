<template>
  <div class="login-container">
    <!-- 粒子背景容器 -->
    <div id="particles-js"></div>

    <!-- 内容包装器 -->
    <div class="content-wrapper">
      <!-- 导航栏 -->
      <nav class="nav-bar">
        <div class="nav-content">
          <div class="logo-container">
            <i class="fas fa-trophy logo-icon"></i>
            <span class="logo-text">智启赛途</span>
          </div>
        </div>
      </nav>

      <!-- 主要内容 -->
      <main class="main-content">
        <div class="container">
          <div class="split-layout">
            <!-- 左侧插图/描述 -->
            <div class="left-panel">
              <h1 class="main-title">加入智启赛途</h1>
              <p class="subtitle">一站式竞赛学习平台，提供学·练·测·评全流程服务，助力你在编程竞赛中脱颖而出。</p>
              <div class="features">
                <div class="feature-item">
                  <i class="fas fa-check-circle feature-icon"></i>
                  <span class="feature-text">海量竞赛题库</span>
                </div>
                <div class="feature-item">
                  <i class="fas fa-check-circle feature-icon"></i>
                  <span class="feature-text">实时评测系统</span>
                </div>
                <div class="feature-item">
                  <i class="fas fa-check-circle feature-icon"></i>
                  <span class="feature-text">专业学习路径</span>
                </div>
              </div>
            </div>

            <!-- 右侧登录注册区域 -->
            <div class="right-panel">
              <div class="auth-box">
                <!-- 标签页切换 -->
                <div class="tabs">
                  <button 
                    class="tab-btn" 
                    :class="{ active: activeTab === 'login' }" 
                    @click="activeTab = 'login'"
                  >登录</button>
                  <button 
                    class="tab-btn" 
                    :class="{ active: activeTab === 'register' }" 
                    @click="activeTab = 'register'"
                  >注册</button>
                </div>

                <!-- 登录表单 -->
                <div v-show="activeTab === 'login'" class="form-container">
                  <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form" autocomplete="off">
                    <!-- 隐藏的伪输入框，用于误导浏览器自动填充 -->
                    <input type="text" name="fake_login_user" style="position:fixed;bottom:-9999px;">
                    <input type="password" name="fake_login_pwd" style="position:fixed;bottom:-9999px;">

                    <el-form-item prop="username">
                      <label class="input-label">账号</label>
                      <el-input
                        v-model="loginForm.username"
                        type="text"
                        size="large"
                        autocomplete="off"
                        name="login_user_field"
                        placeholder="请输入您的账号"
                      >
                        <template #prefix><i class="fas fa-user input-icon"></i></template>
                      </el-input>
                    </el-form-item>
                    <el-form-item prop="password">
                      <label class="input-label">密码</label>
                      <el-input
                        v-model="loginForm.password"
                        type="password"
                        size="large"
                        autocomplete="new-password"
                        name="login_pwd_field"
                        placeholder="请输入您的密码"
                        @keyup.enter="handleLogin"
                      >
                        <template #prefix><i class="fas fa-lock input-icon"></i></template>
                      </el-input>
                    </el-form-item>
                    <el-form-item prop="code" v-if="captchaEnabled">
                      <label class="input-label">验证码</label>
                      <div class="captcha-container">
                        <el-input
                          v-model="loginForm.code"
                          size="large"
                          autocomplete="off"
                          placeholder="验证码"
                          style="width: 63%"
                          @keyup.enter="handleLogin"
                        >
                          <template #prefix><i class="fas fa-shield-alt input-icon"></i></template>
                        </el-input>
                        <div class="login-code">
                          <img :src="codeUrl" @click="getCode" class="login-code-img"/>
                        </div>
                      </div>
                    </el-form-item>
                    <div class="form-actions">
                      <el-checkbox v-model="loginForm.rememberMe" @change="handleRememberChange">记住我</el-checkbox>
                      <a href="#" class="forgot-password">忘记密码?</a>
                    </div>
                    <el-button
                      :loading="loading"
                      size="large"
                      type="primary"
                      class="submit-btn"
                      @click.prevent="handleLogin"
                    >
                      <span v-if="!loading">登 录</span>
                      <span v-else>登 录 中...</span>
                    </el-button>
                  </el-form>
                </div>

                <!-- 注册表单 -->
                <div v-show="activeTab === 'register'" class="form-container">
                  <el-form ref="registerRef" :model="registerForm" :rules="registerRules" class="register-form" autocomplete="off">
                    <!-- 隐藏的伪输入框，用于误导浏览器自动填充 -->
                    <input type="text" name="fake_username" style="position:fixed;bottom:-9999px;">
                    <input type="password" name="fake_password" style="position:fixed;bottom:-9999px;">
                    
                    <el-form-item prop="username">
                      <label class="input-label">用户名</label>
                      <el-input 
                        v-model="registerForm.username" 
                        type="text" 
                        size="large" 
                        autocomplete="off" 
                        name="reg_user_name_12"
                        inputmode="numeric"
                        pattern="\\d*"
                        placeholder="请输入12位数字账号"
                        @update:modelValue="onRegisterUsernameInput"
                      >
                        <template #prefix><i class="fas fa-user input-icon"></i></template>
                      </el-input>
                    </el-form-item>
                    <el-form-item prop="password">
                      <label class="input-label">密码</label>
                      <el-input
                        v-model="registerForm.password"
                        type="password"
                        size="large" 
                        autocomplete="new-password"
                        name="reg_pwd_field"
                        placeholder="设置您的密码"
                      >
                        <template #prefix><i class="fas fa-lock input-icon"></i></template>
                      </el-input>
                    </el-form-item>
                    <el-form-item prop="confirmPassword">
                      <label class="input-label">确认密码</label>
                      <el-input
                        v-model="registerForm.confirmPassword"
                        type="password"
                        size="large" 
                        autocomplete="new-password"
                        name="reg_confirm_pwd_field"
                        placeholder="再次输入密码"
                        @keyup.enter="handleRegister"
                      >
                        <template #prefix><i class="fas fa-lock input-icon"></i></template>
                      </el-input>
                    </el-form-item>
                    <el-form-item prop="code" v-if="captchaEnabled">
                      <label class="input-label">验证码</label>
                      <div class="captcha-container">
                        <el-input
                          size="large" 
                          v-model="registerForm.code"
                          auto-complete="off"
                          placeholder="验证码"
                          style="width: 63%"
                          @keyup.enter="handleRegister"
                        >
                          <template #prefix><i class="fas fa-shield-alt input-icon"></i></template>
                        </el-input>
                        <div class="register-code">
                          <img :src="codeUrl" @click="getCode" class="register-code-img"/>
                        </div>
                      </div>
                    </el-form-item>
                    <el-form-item prop="agree">
                      <div class="form-actions full-width">
                        <el-checkbox v-model="registerForm.agree">
                          我已阅读并同意 <a href="#" class="link-text">服务条款</a> 和 <a href="#" class="link-text">隐私政策</a>
                        </el-checkbox>
                      </div>
                    </el-form-item>
                    <el-button
                      :loading="loading"
                      size="large" 
                      type="primary"
                      class="submit-btn"
                      @click.prevent="handleRegister"
                    >
                      <span v-if="!loading">注 册</span>
                      <span v-else>注 册 中...</span>
                    </el-button>
                  </el-form>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>

      <!-- 页脚 -->
      <footer class="footer">
        <div class="footer-content">
          <div class="logo-container">
            <i class="fas fa-trophy footer-icon"></i>
            <span class="footer-text">智启赛途</span>
          </div>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg, register } from "@/api/login"
import useUserStore from '@/store/modules/user'
import { ElMessage, ElMessageBox } from "element-plus";

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

// 状态控制
const activeTab = ref('login')
const loading = ref(false)
const captchaEnabled = ref(true)
const codeUrl = ref("")
const redirect = ref(undefined)
const rememberKey = "compserve-login-remember"

// 登录表单
const loginForm = ref({
  username: "",
  password: "",
  rememberMe: false,
  code: "",
  uuid: ""
})

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

// 注册表单
const registerForm = ref({
  username: "",
  password: "",
  confirmPassword: "",
  code: "",
  uuid: "",
  agree: false
})

const equalToPassword = (rule, value, callback) => {
  if (registerForm.value.password !== value) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, trigger: "blur", message: "请输入12位数字账号" },
    { len: 12, message: "账号必须为12位数字", trigger: ["blur", "change"] },
    { validator: (rule, value, callback) => {
      const v = String(value || '').trim()
      if (!/^\d{12}$/.test(v)) callback(new Error("账号必须为12位数字"))
      else callback()
    }, trigger: ["blur", "change"] }
  ],
  password: [
    { required: true, trigger: "blur", message: "请输入您的密码" },
    { min: 5, max: 20, message: "用户密码长度必须介于 5 和 20 之间", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, trigger: "blur", message: "请再次输入您的密码" },
    { required: true, validator: equalToPassword, trigger: "blur" }
  ],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }],
  agree: [{ 
    validator: (rule, value, callback) => {
      if (!value) {
        callback(new Error("请同意服务条款和隐私政策"))
      } else {
        callback()
      }
    }, 
    trigger: "change" 
  }]
}

watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

function onRegisterUsernameInput(val) {
  const digits = String(val ?? "").replace(/\D/g, "")
  if (digits !== registerForm.value.username) {
    registerForm.value.username = digits
  }
}

// 登录逻辑
function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      userStore.login(loginForm.value).then(() => {
        saveLoginRemember()
        // 获取用户信息以决定跳转
        userStore.getInfo().then((res) => {
          const roles = res.roles || []
          console.log('[Login] User roles:', roles)
          let path = "/"
          
          if (roles.includes("student")) {
            path = "/student"
          } else if (roles.includes("teacher")) {
            path = "/teacher"
          } else if (roles.includes("sys_admin")|| roles.includes("comp_admin")) {
             path = "/sys-admin"
          } else {
             // admin 或其他角色，默认去 redirect 或 /
             path = (redirect.value && redirect.value !== '/') ? redirect.value : "/"
          }
          console.log('[Login] Calculated redirect path:', path)
          
          const query = route.query
          const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
            if (cur !== "redirect") {
              acc[cur] = query[cur]
            }
            return acc
          }, {})
          router.push({ path: path, query: otherQueryParams })
        })
      }).catch(() => {
        loading.value = false
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

// 注册逻辑
function handleRegister() {
  if (!registerForm.value.agree) {
    ElMessage.warning("请先勾选同意服务条款和隐私政策")
    proxy.$refs.registerRef.validateField("agree")
    return
  }
  proxy.$refs.registerRef.validate(valid => {
    if (valid) {
      loading.value = true;
      register(registerForm.value).then(res => {
        loading.value = false;
        const username = registerForm.value.username;
        ElMessageBox.alert("<font color='red'>恭喜你，您的账号 " + username + " 注册成功！</font>", "系统提示", {
          dangerouslyUseHTMLString: true,
          type: "success",
        }).then(() => {
          activeTab.value = 'login'
          loginForm.value.username = username
          loginForm.value.password = registerForm.value.password
        }).catch(() => {});
      }).catch(() => {
        loading.value = false;
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  });
}

function saveLoginRemember() {
  if (loginForm.value.rememberMe) {
    localStorage.setItem(rememberKey, JSON.stringify({
      username: loginForm.value.username,
      password: loginForm.value.password,
      rememberMe: true
    }))
    return
  }
  clearLoginRemember()
}

function clearLoginRemember() {
  localStorage.removeItem(rememberKey)
}

function handleRememberChange(checked) {
  if (!checked) {
    clearLoginRemember()
  }
}

function loadLoginRemember() {
  try {
    const saved = JSON.parse(localStorage.getItem(rememberKey) || "{}")
    if (saved.rememberMe) {
      loginForm.value.username = saved.username || ""
      loginForm.value.password = saved.password || ""
      loginForm.value.rememberMe = true
    }
  } catch (e) {
    clearLoginRemember()
  }
}

// 获取验证码
function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
      registerForm.value.uuid = res.uuid
    }
  })
}

onMounted(() => {
  getCode()
  loadLoginRemember()

  const initParticles = () => {
    if (window.particlesJS) {
      window.particlesJS('particles-js', {
        "particles": {
          "number": {
            "value": 80,
            "density": {
              "enable": true,
              "value_area": 800
            }
          },
          "color": {
            "value": "#a78bfa"
          },
          "shape": {
            "type": "circle"
          },
          "opacity": {
            "value": 0.5,
            "random": true,
            "anim": {
              "enable": true,
              "speed": 1,
              "opacity_min": 0.1
            }
          },
          "size": {
            "value": 3,
            "random": true,
            "anim": {
              "enable": true,
              "speed": 2
            }
          },
          "line_linked": {
            "enable": true,
            "distance": 150,
            "color": "#c4b5fd",
            "opacity": 0.4,
            "width": 1
          },
          "move": {
            "enable": true,
            "speed": 2,
            "direction": "none",
            "random": true,
            "out_mode": "out",
            "attract": {
              "enable": true,
              "rotateX": 600,
              "rotateY": 1200
            }
          }
        },
        "interactivity": {
          "detect_on": "canvas",
          "events": {
            "onhover": {
              "enable": true,
              "mode": "grab"
            },
            "onclick": {
              "enable": true,
              "mode": "push"
            }
          },
          "modes": {
            "grab": {
              "distance": 140,
              "line_linked": {
                "opacity": 1
              }
            },
            "push": {
              "particles_nb": 4
            }
          }
        },
        "retina_detect": true
      });
    }
  }

  if (!window.particlesJS) {
    const script = document.createElement('script')
    script.src = '/particles.js'
    script.onload = initParticles
    document.body.appendChild(script)
  } else {
    initParticles()
  }
})
</script>

<style lang='scss' scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');

.login-container {
  font-family: 'Poppins', sans-serif;
  background-color: #f8fafc;
  position: relative;
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

#particles-js {
  position: fixed;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 0;
  pointer-events: none; /* 确保不阻挡底层交互 */
}

.content-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  height: 100vh;
}

.nav-bar {
  background: transparent;
  width: 100%;
  z-index: 10;
  padding: 0.5rem 0;
}

.nav-content {
  max-width: 80rem;
  margin: 0 auto;
  padding: 0 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 3rem;
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo-icon {
  color: #4f46e5;
  font-size: 1.5rem;
  margin-right: 0.5rem;
  filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.1));
}

.logo-text {
  color: #4f46e5;
  font-weight: 700;
  font-size: 1.25rem;
  filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.1));
}

.main-content {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.5rem 1rem;
  min-height: 0;
}

.container {
  width: 100%;
  max-width: 72rem;
  margin: 0 auto;
}

.split-layout {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3rem;
}

@media (min-width: 768px) {
  .split-layout {
    flex-direction: row;
  }
}

.left-panel {
  width: 100%;
  text-align: center;
}

@media (min-width: 768px) {
  .left-panel {
    width: 50%;
    text-align: left;
  }
}

.main-title {
  font-size: 2.25rem;
  font-weight: 700;
  color: #1f2937;
  margin-top: 1.5rem;
}

.subtitle {
  font-size: 1.125rem;
  color: #4b5563;
  margin-top: 1rem;
}

.features {
  margin-top: 2rem;
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  justify-content: center;
}

@media (min-width: 768px) {
  .features {
    justify-content: flex-start;
  }
}

.feature-item {
  display: flex;
  align-items: center;
}

.feature-icon {
  color: #6366f1;
  margin-right: 0.5rem;
}

.feature-text {
  color: #374151;
}

.right-panel {
  width: 100%;
  max-width: 28rem;
}

@media (min-width: 768px) {
  .right-panel {
    width: 50%;
  }
}

.auth-box {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.15);
  border-radius: 0.75rem;
  padding: 2rem;
  background-color: white;
}

.tabs {
  display: flex;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 1.5rem;
}

.tab-btn {
  transition: all 0.3s ease;
  position: relative;
  padding: 0.5rem 1rem;
  font-weight: 500;
  color: #6b7280;
  background: none;
  border: none;
  cursor: pointer;
  outline: none;
}

.tab-btn.active {
  color: #4f46e5;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 3px;
  background: #4f46e5;
  border-radius: 3px 3px 0 0;
}

.form-container {
  margin-top: 1rem;
}

.input-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.25rem;
}

.input-icon {
  margin-left: 5px;
  color: #6b7280;
}

.captcha-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.login-code-img, .register-code-img {
  height: 38px;
  cursor: pointer;
  margin-left: 10px;
}

.form-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.forgot-password, .link-text {
  font-size: 0.875rem;
  color: #4f46e5;
  text-decoration: none;
}

.forgot-password:hover, .link-text:hover {
  color: #4338ca;
}

.submit-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.footer {
  background-color: #f9fafb;
  padding: 0.75rem 0;
}

.full-width {
  width: 100%;
}

.footer-content {
  max-width: 80rem;
  margin: 0 auto;
  padding: 0 1rem;
}

.footer-icon {
  color: #4f46e5;
  font-size: 1.25rem;
  margin-right: 0.5rem;
}

.footer-text {
  color: #4f46e5;
  font-weight: 700;
}

/* Element Plus Override */
:deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.8);
  box-shadow: 0 0 0 1px #d1d5db inset;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover), :deep(.el-input__wrapper.is-focus) {
  background-color: white;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.2) inset !important;
}

:deep(.el-form-item) {
  margin-bottom: 1.2rem;
}
</style>
