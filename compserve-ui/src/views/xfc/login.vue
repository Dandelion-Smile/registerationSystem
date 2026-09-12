<template>
  <main class="xfc-login">
    <img class="art-layer" :src="loginArt" alt="" draggable="false">
    <section class="login-card" aria-labelledby="login-title">
      <header>
        <h1 id="login-title">{{ isRegistering ? '学生注册' : '学生登录' }}</h1>
        <p v-if="!isRegistering">还没有账号？<button type="button" class="header-link" @click="showRegister">立即注册</button></p>
        <p v-else>已有账号？<button type="button" class="header-link" @click="showLogin">返回登录</button></p>
      </header>
      <el-form v-if="!isRegistering" ref="formRef" :model="form" :rules="rules" class="login-form" @submit.prevent="submit">
        <el-form-item prop="username">
          <el-input
            v-model.trim="form.username"
            autocomplete="username"
            placeholder="请输入学号"
            size="large"
            @keyup.enter="submit"
          >
            <template #prefix><i class="fa-regular fa-user"></i></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            autocomplete="current-password"
            placeholder="请输入密码"
            show-password
            size="large"
            type="password"
            @keyup.enter="submit"
          >
            <template #prefix><i class="fa-solid fa-lock"></i></template>
          </el-input>
        </el-form-item>
        <div class="form-options">
          <el-checkbox v-model="form.remember" @change="handleRememberChange">记住我</el-checkbox>
          <button type="button" class="text-link" @click="onForgotPassword">忘记密码?</button>
        </div>
        <el-button class="login-button" native-type="submit" :loading="submitting" type="primary">
          <span class="login-button-label">登 录</span>
          <span class="login-button-arrow" aria-hidden="true"><i class="fa-solid fa-arrow-right"></i></span>
        </el-button>
      </el-form>
      <el-form v-else ref="registerFormRef" :model="registerForm" :rules="registerRules" class="login-form register-form" @submit.prevent="submitRegister">
        <el-form-item prop="username">
          <el-input v-model.trim="registerForm.username" autocomplete="username" placeholder="请输入12位学号" size="large">
            <template #prefix><i class="fa-regular fa-user"></i></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" autocomplete="new-password" placeholder="请设置密码" show-password size="large" type="password">
            <template #prefix><i class="fa-solid fa-lock"></i></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" autocomplete="new-password" placeholder="请再次输入密码" show-password size="large" type="password" @keyup.enter="submitRegister">
            <template #prefix><i class="fa-solid fa-lock"></i></template>
          </el-input>
        </el-form-item>
        <el-form-item v-if="captchaEnabled" prop="code" class="captcha-item">
          <el-input v-model.trim="registerForm.code" autocomplete="off" placeholder="请输入验证码" size="large" @keyup.enter="submitRegister">
            <template #prefix><i class="fa-solid fa-shield-halved"></i></template>
          </el-input>
          <button type="button" class="captcha-button" title="点击刷新验证码" @click="getCode">
            <img v-if="codeUrl" :src="codeUrl" alt="验证码">
            <span v-else>加载中</span>
          </button>
        </el-form-item>
        <el-form-item prop="agree" class="agreement-item">
          <el-checkbox v-model="registerForm.agree">我已阅读并同意服务条款和隐私政策</el-checkbox>
        </el-form-item>
        <el-button class="login-button" native-type="submit" :loading="registering" type="primary">
          <span class="login-button-label">{{ registering ? '注册中' : '注 册' }}</span>
          <span class="login-button-arrow" aria-hidden="true"><i class="fa-solid fa-arrow-right"></i></span>
        </el-button>
      </el-form>
    </section>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { xfcLogin } from '@/api/xfc/auth'
import { getCodeImg, register } from '@/api/login'
import { setXfcToken } from '@/utils/xfcAuth'
import loginArt from '@/assets/images/xfc-login-art.jpg'

const REMEMBER_KEY = 'xfc-login-remember'
const router = useRouter()
const formRef = ref()
const registerFormRef = ref()
const submitting = ref(false)
const registering = ref(false)
const isRegistering = ref(false)
const captchaEnabled = ref(false)
const codeUrl = ref('')
const form = reactive({ username: '', password: '', remember: false })
const registerForm = reactive({ username: '', password: '', confirmPassword: '', code: '', uuid: '', agree: false })
const rules = {
  username: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const registerRules = {
  username: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { pattern: /^\d{12}$/, message: '学号必须为12位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 5, max: 20, message: '密码长度必须为5到20个字符', trigger: 'blur' },
    { pattern: /^[^<>"'|\\]+$/, message: '密码不能包含非法字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: (_rule, value, callback) => value === registerForm.password ? callback() : callback(new Error('两次输入的密码不一致')), trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  agree: [{ validator: (_rule, value, callback) => value ? callback() : callback(new Error('请先同意服务条款和隐私政策')), trigger: 'change' }]
}

onMounted(() => {
  loadRemembered()
})

function loadRemembered() {
  try {
    const saved = JSON.parse(localStorage.getItem(REMEMBER_KEY) || 'null')
    if (saved?.username) {
      form.username = saved.username
      form.remember = true
    }
  } catch {
    localStorage.removeItem(REMEMBER_KEY)
  }
}

function persistRemember() {
  if (form.remember && form.username) {
    localStorage.setItem(REMEMBER_KEY, JSON.stringify({ username: form.username }))
    return
  }
  localStorage.removeItem(REMEMBER_KEY)
}

function handleRememberChange(checked) {
  if (!checked) localStorage.removeItem(REMEMBER_KEY)
}

function onForgotPassword() {
  ElMessage.info('请联系竞赛组织方核验并重置账号')
}

function showRegister() {
  isRegistering.value = true
  getCode()
}

function showLogin() {
  isRegistering.value = false
  registerFormRef.value?.clearValidate()
}

async function getCode() {
  try {
    const response = await getCodeImg()
    captchaEnabled.value = response.captchaEnabled !== false
    if (captchaEnabled.value) {
      codeUrl.value = `data:image/gif;base64,${response.img}`
      registerForm.uuid = response.uuid
      registerForm.code = ''
    }
  } catch {
    ElMessage.error('验证码加载失败，请重试')
  }
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    persistRemember()
    const response = await xfcLogin(form.username, form.password)
    setXfcToken(response.token)
    await router.replace('/xfc/registration')
  } catch (error) {
    ElMessage.error(error?.message || '账号或密码错误，请重新输入')
  } finally {
    submitting.value = false
  }
}

async function submitRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return
  registering.value = true
  try {
    await register({
      username: registerForm.username,
      password: registerForm.password,
      code: registerForm.code,
      uuid: registerForm.uuid
    })
    const response = await xfcLogin(registerForm.username, registerForm.password)
    setXfcToken(response.token)
    ElMessage.success('注册成功，欢迎参加讯飞杯')
    await router.replace('/xfc/registration')
  } catch (error) {
    ElMessage.error(error?.message || '注册失败，请稍后重试')
    if (captchaEnabled.value) getCode()
  } finally {
    registering.value = false
  }
}
</script>

<style scoped>
.xfc-login {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  background: #b7dff8;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
}

.art-layer {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center center;
  pointer-events: none;
  user-select: none;
}

.login-card {
  position: absolute;
  z-index: 1;
  top: 50%;
  right: 11%;
  left: auto;
  width: min(30vw, 480px);
  min-width: 420px;
  transform: translateY(-50%);
  padding: 42px 44px 40px;
  border: 1px solid rgba(255, 255, 255, 0.95);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24px 56px rgba(80, 120, 200, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(18px);
}

.login-card header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
}

.login-card h1 {
  margin: 0;
  color: #16306e;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.login-card h1::after {
  content: "";
  display: block;
  width: 52px;
  height: 4px;
  margin-top: 10px;
  border-radius: 4px;
  background: linear-gradient(90deg, #c85bff, #6b6bff);
}

.login-card header p {
  margin: 8px 0 0;
  color: #8b95b0;
  font-size: 13px;
  white-space: nowrap;
}

.header-link {
  padding: 0;
  border: 0;
  background: transparent;
  color: #3d6dff;
  font: inherit;
  text-decoration: none;
  cursor: pointer;
}

.header-link:hover {
  text-decoration: underline;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.captcha-item :deep(.el-form-item__content) {
  display: flex;
  gap: 10px;
  flex-wrap: nowrap;
}

.captcha-button {
  flex: 0 0 112px;
  height: 52px;
  overflow: hidden;
  padding: 0;
  border: 1px solid #e4eaf6;
  border-radius: 14px;
  background: #fff;
  color: #8b95b0;
  cursor: pointer;
}

.captcha-button img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: fill;
}

.agreement-item {
  margin-top: 2px;
}

.agreement-item :deep(.el-checkbox__label) {
  color: #6b7694;
  font-size: 12px;
}

.login-form :deep(.el-input__wrapper) {
  min-height: 52px;
  padding: 4px 16px;
  border-radius: 14px;
  background: #fff !important;
  box-shadow: none;
  border: 1px solid #e4eaf6;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: none;
  border-color: #8aa0ff;
}

.login-form :deep(.el-input__inner) {
  font-size: 15px;
  background: transparent !important;
  box-shadow: none !important;
}

.login-form :deep(input:-webkit-autofill),
.login-form :deep(input:-webkit-autofill:hover),
.login-form :deep(input:-webkit-autofill:focus),
.login-form :deep(input:-webkit-autofill:active) {
  -webkit-text-fill-color: #16306e;
  caret-color: #16306e;
  transition: background-color 99999s ease-out;
  -webkit-box-shadow: 0 0 0 1000px #fff inset !important;
  box-shadow: 0 0 0 1000px #fff inset !important;
  background-color: #fff !important;
  background-image: none !important;
}

.login-form :deep(.el-input__prefix) {
  color: #9aa6c4;
  font-size: 16px;
}

.login-form :deep(.el-input__suffix) {
  color: #9aa6c4;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 2px 0 22px;
  color: #6b7694;
  font-size: 13px;
}

.form-options :deep(.el-checkbox__label) {
  color: #6b7694;
  font-size: 13px;
  padding-left: 6px;
}

.form-options :deep(.el-checkbox__inner) {
  border-radius: 4px;
}

.text-link {
  padding: 0;
  border: 0;
  background: none;
  color: #3d6dff;
  font-size: 13px;
  cursor: pointer;
}

.text-link:hover {
  text-decoration: underline;
}

.login-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  width: 100%;
  height: 52px;
  border: 0;
  border-radius: 26px;
  background: linear-gradient(90deg, #3fe4ea 0%, #7ea4ff 42%, #e8a6dc 78%, #f3b4d0 100%);
  box-shadow: 0 10px 20px rgba(111, 140, 244, 0.28);
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.42em;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 26px rgba(111, 140, 244, 0.35);
}

.login-button-label {
  margin-left: 0.42em;
}

.login-button-arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.94);
  color: #5b6bdb;
  font-size: 12px;
  letter-spacing: 0;
}

.login-button :deep(.el-icon) {
  display: none;
}

@media (max-width: 1100px) {
  .login-card {
    right: 8%;
    width: min(36vw, 440px);
    min-width: 380px;
    padding: 36px 34px 32px;
  }
}

@media (max-width: 800px) {
  .art-layer {
    object-fit: cover;
    object-position: left 8%;
  }

  .login-card {
    top: auto;
    right: auto;
    bottom: 24px;
    left: 50%;
    width: min(90vw, 480px);
    min-width: 0;
    transform: translateX(-50%);
    padding: 32px 24px 24px;
    background: rgba(255, 255, 255, 0.96);
  }
}

@media (max-width: 520px) {
  .login-card {
    bottom: 14px;
    padding: 26px 18px 20px;
  }

  .login-card header {
    align-items: flex-start;
    gap: 8px;
    flex-direction: column;
  }

  .login-card h1 {
    font-size: 24px;
  }

  .login-button {
    height: 48px;
    font-size: 16px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .login-button {
    transition: none;
  }
}
</style>
