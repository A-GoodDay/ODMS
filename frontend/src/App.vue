<script setup>
import { ref, computed } from 'vue'
import UserSearch from './components/UserSearch.vue'
import AdminPanel from './components/AdminPanel.vue'
import AdminLogin from './components/AdminLogin.vue'

// 用户角色状态管理
const currentUserRole = ref('user') // 'user' 或 'admin'
const isAdminLoggedIn = ref(false)
const loggedInAdmin = ref('')

// 计算当前显示的组件
const currentView = computed(() => {
  if (currentUserRole.value === 'user') {
    return 'user-search'
  } else {
    if (isAdminLoggedIn.value) {
      return 'admin-panel'
    } else {
      return 'admin-login'
    }
  }
})

// 切换用户角色
function switchRole(role) {
  currentUserRole.value = role
}

// 处理管理员登录
function handleAdminLogin(adminInfo) {
  isAdminLoggedIn.value = true
  // 确保正确存储管理员姓名
  loggedInAdmin.value = typeof adminInfo === 'string' ? adminInfo : (adminInfo?.name || adminInfo?.username || '管理员')
}

// 处理管理员登出
function handleAdminLogout() {
  isAdminLoggedIn.value = false
  loggedInAdmin.value = ''
  currentUserRole.value = 'user'
}

// 取消登录
function handleCancelLogin() {
  currentUserRole.value = 'user'
}
</script>

<template>
  <div id="app">
    <header>
      <h1>公文检索系统</h1>
      <nav>
        <button 
          @click="switchRole('user')" 
          :class="{ active: currentUserRole === 'user' }"
        >
          用户检索
        </button>
        <button 
          @click="switchRole('admin')" 
          :class="{ active: currentUserRole === 'admin' }"
        >
          管理员入口
        </button>
        
        <!-- 管理员登录状态显示 -->
        <div v-if="isAdminLoggedIn" class="admin-status">
          <span>欢迎，{{ loggedInAdmin }}！</span>
          <button @click="handleAdminLogout" class="logout-button">退出登录</button>
        </div>
      </nav>
    </header>

    <main>
      <div v-if="currentView === 'user-search'">
        <UserSearch />
      </div>
      <div v-else-if="currentView === 'admin-login'">
        <AdminLogin @login="handleAdminLogin" @cancel="handleCancelLogin" />
      </div>
      <div v-else>
        <AdminPanel />
      </div>
    </main>
  </div>
</template>

<style scoped>
#app {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

header {
  text-align: center;
  margin-bottom: 30px;
}

header h1 {
  color: #333;
}

nav {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

nav button {
  padding: 10px 20px;
  margin: 0 10px;
  border: none;
  background-color: #f0f0f0;
  cursor: pointer;
  border-radius: 4px;
}

nav button.active {
  background-color: #007bff;
  color: white;
}

.admin-status {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-left: auto;
  background-color: #e9f5ff;
  padding: 8px 15px;
  border-radius: 20px;
}

.logout-button {
  padding: 5px 10px;
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.logout-button:hover {
  background-color: #c82333;
}

main {
  min-height: 500px;
}
</style>