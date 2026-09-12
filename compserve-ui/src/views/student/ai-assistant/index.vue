<template>
  <div class="student-page">
    <StudentNavbar />
    <div class="content-box">
      <h2>AI助手</h2>
    </div>
  </div>
</template>

<script setup name="AiAssistant">
import StudentNavbar from '@/components/StudentNavbar.vue'
</script>

<style scoped>
:global(html, body) {
  margin: 0;
  padding: 0;
}

.student-page {
  min-height: 100vh;
  background-color: #f5f7fa;
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
.logo-text { font-size: 18px; }
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
  content: '';
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
  box-shadow: 0 0 0 2px rgba(255,255,255,0.3);
}
.content-box {
  max-width: 1400px;
  margin: 24px auto;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  min-height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
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
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 12px;
  min-width: 200px;
  display: none;
  z-index: 1000;
}

.dropdown-menu::before {
  content: '';
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
</style>
