<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-50 to-purple-50 dark:from-gray-900 dark:to-gray-800 transition-colors duration-200">
    <div class="bg-white dark:bg-gray-800 p-8 rounded-2xl shadow-xl w-full max-w-md border border-transparent dark:border-gray-700 transition-colors duration-200">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-gray-800 dark:text-white transition-colors duration-200">创建账号</h1>
        <p class="text-gray-500 dark:text-gray-400 mt-2 transition-colors duration-200">加入 SmartLingo 开始学习</p>
      </div>
      
      <form @submit.prevent="handleRegister" class="space-y-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2 transition-colors duration-200">用户名</label>
          <input 
            v-model="username"
            type="text" 
            required
            class="w-full px-4 py-3 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all"
            placeholder="设置用户名"
          />
        </div>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2 transition-colors duration-200">密码</label>
          <input 
            v-model="password"
            type="password" 
            required
            class="w-full px-4 py-3 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all"
            placeholder="设置密码"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2 transition-colors duration-200">确认密码</label>
          <input 
            v-model="confirmPassword"
            type="password" 
            required
            class="w-full px-4 py-3 rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all"
            placeholder="再次输入密码"
          />
        </div>

        <div v-if="errorMsg" class="text-red-500 text-sm text-center">
          {{ errorMsg }}
        </div>
        
        <button 
          type="submit" 
          :disabled="isLoading"
          class="w-full py-3 bg-indigo-600 text-white rounded-lg font-bold hover:bg-indigo-700 dark:hover:bg-indigo-500 transition-colors disabled:opacity-50"
        >
          {{ isLoading ? '注册中...' : '注册' }}
        </button>
      </form>
      
      <div class="mt-6 text-center text-sm text-gray-500 dark:text-gray-400 transition-colors duration-200">
        已有账号？ 
        <router-link to="/login" class="text-indigo-600 dark:text-indigo-400 font-semibold hover:text-indigo-700 dark:hover:text-indigo-300 transition-colors duration-200">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const isLoading = ref(false)
const errorMsg = ref('')

const handleRegister = async () => {
  if (password.value !== confirmPassword.value) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }

  isLoading.value = true
  errorMsg.value = ''
  
  try {
    const res = await axios.post('/api/auth/register', {
      username: username.value,
      password: password.value
    })
    
    if (res.data.success) {
      // Auto login or redirect to login
      router.push('/login')
    } else {
      errorMsg.value = res.data.message || '注册失败'
    }
  } catch (e) {
    console.error(e)
    errorMsg.value = '连接服务器失败'
  } finally {
    isLoading.value = false
  }
}
</script>
