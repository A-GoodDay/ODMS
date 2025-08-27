<template>
  <div class="login-container">
    <div class="login-form">
      <h2>管理员登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名:</label>
          <input 
            id="username" 
            v-model="loginForm.username" 
            type="text" 
            required 
            placeholder="请输入用户名"
          />
        </div>
        
        <div class="form-group">
          <label for="password">密码:</label>
          <input 
            id="password" 
            v-model="loginForm.password" 
            type="password" 
            required 
            placeholder="请输入密码"
          />
        </div>
        
        <div class="form-actions">
          <button type="submit" class="login-button" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
          <button type="button" @click="cancelLogin" class="cancel-button" :disabled="loading">
            取消
          </button>
        </div>
        
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import apiService from '../api.js'

const emit = defineEmits(['login', 'cancel'])

const loginForm = reactive({
  username: '',
  password: ''
})

const errorMessage = ref('')
const loading = ref(false)

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    errorMessage.value = '请输入用户名和密码'
    return
  }
  
  loading.value = true
  errorMessage.value = ''
  
  try {
    const data = await apiService.login(loginForm.username, loginForm.password)
    // 登录成功，传递管理员信息（包括姓名）
    emit('login', data.admin?.name || data.admin?.username || loginForm.username)
  } catch (error) {
    // 登录失败
    errorMessage.value = error.message || '登录失败'
  } finally {
    loading.value = false
  }
}

const cancelLogin = () => {
  emit('cancel')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.login-form {
  width: 100%;
  max-width: 400px;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  background-color: white;
}

.login-form h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #555;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  box-sizing: border-box;
}

.form-group input:focus {
  border-color: #007bff;
  outline: none;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 30px;
}

.login-button,
.cancel-button {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}

.login-button {
  background-color: #007bff;
  color: white;
}

.login-button:hover {
  background-color: #0069d9;
}

.cancel-button {
  background-color: #6c757d;
  color: white;
}

.cancel-button:hover {
  background-color: #5a6268;
}

.error-message {
  margin-top: 15px;
  padding: 10px;
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
  border-radius: 4px;
  text-align: center;
}
</style>