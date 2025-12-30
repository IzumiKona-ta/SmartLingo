<template>
  <div class="max-w-4xl mx-auto space-y-6">
    <h2 class="text-2xl font-bold text-gray-800">系统设置</h2>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <div class="p-6 border-b border-gray-100">
        <h3 class="text-lg font-semibold text-gray-800">个人资料</h3>
        <p class="text-gray-500 text-sm mt-1">管理您的个人信息和账户安全</p>
      </div>
      <div class="p-6 space-y-6">
        <div class="flex items-center space-x-6">
          <div class="w-20 h-20 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 text-2xl font-bold">
            U
          </div>
          <div>
            <el-button type="primary" size="small">更换头像</el-button>
            <el-button size="small">删除</el-button>
          </div>
        </div>
        
        <el-form label-position="top" class="max-w-md">
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="每日学习目标 (分钟)">
            <el-slider v-model="form.dailyGoal" :min="10" :max="120" :step="10" show-input />
          </el-form-item>
          <el-form-item label="每组单词数量">
             <div class="flex items-center space-x-4">
                <el-input-number v-model="form.wordsPerGroup" :min="5" :max="50" :step="5" />
                <span class="text-sm text-gray-500">建议: 10-20个/组</span>
             </div>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <div class="p-6 border-b border-gray-100">
        <h3 class="text-lg font-semibold text-gray-800">偏好设置</h3>
      </div>
      <div class="p-6">
        <div class="flex items-center justify-between py-3 border-b border-gray-50">
          <div>
            <div class="font-medium text-gray-700">深色模式</div>
            <div class="text-sm text-gray-500">开启护眼深色主题</div>
          </div>
          <el-switch v-model="form.darkMode" />
        </div>
        <div class="flex items-center justify-between py-3 border-b border-gray-50">
          <div>
            <div class="font-medium text-gray-700">消息通知</div>
            <div class="text-sm text-gray-500">接收学习提醒和进度报告</div>
          </div>
          <el-switch v-model="form.notifications" />
        </div>
        <div class="flex items-center justify-between py-3">
          <div>
            <div class="font-medium text-gray-700">音效</div>
            <div class="text-sm text-gray-500">播放单词发音和交互音效</div>
          </div>
          <el-switch v-model="form.sound" />
        </div>
      </div>
    </div>
    
    <div class="flex justify-end space-x-4">
      <el-button>取消</el-button>
      <el-button type="primary" @click="saveSettings">保存更改</el-button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const fileInput = ref(null)

const form = reactive({
  nickname: '',
  email: '',
  avatar: '',
  dailyGoal: 30,
  wordsPerGroup: parseInt(localStorage.getItem('wordsPerGroup')) || 20,
  darkMode: false,
  notifications: true,
  sound: true
})

const getAvatarUrl = (path) => {
    if (!path) return ''
    if (path.startsWith('http')) return path
    // Assuming backend is on same host/port or proxied, otherwise need base URL
    // In dev usually /api is proxied but static files might need full URL or proxy
    // If path starts with /avatars, it should work if mapped in backend
    return `http://localhost:8080${path}` // Adjust port if needed, hardcoding for now or use env
}

const fetchProfile = async () => {
    try {
        const userStr = localStorage.getItem('user')
        const userId = userStr ? JSON.parse(userStr).id : 1
        
        const res = await axios.get(`/api/user/stats?userId=${userId}`)
        if (res.data.user) {
            form.nickname = res.data.user.nickname || res.data.user.username
            form.email = res.data.user.email || ''
            form.avatar = res.data.user.avatar
        }
    } catch (e) {
        console.error("Failed to fetch profile", e)
    }
}

const handleFileChange = async (e) => {
    const file = e.target.files[0]
    if (!file) return

    const formData = new FormData()
    const userStr = localStorage.getItem('user')
    const userId = userStr ? JSON.parse(userStr).id : 1
    
    formData.append('file', file)
    formData.append('userId', userId)

    try {
        const res = await axios.post('/api/user/upload-avatar', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        })
        if (res.data.success) {
            form.avatar = res.data.avatar
            ElMessage.success('头像上传成功')
            // Update local user info just in case
            const user = JSON.parse(localStorage.getItem('user') || '{}')
            user.avatar = res.data.avatar
            localStorage.setItem('user', JSON.stringify(user))
        } else {
            ElMessage.error(res.data.message || '上传失败')
        }
    } catch (e) {
        ElMessage.error('上传出错')
    }
}

const saveSettings = async () => {
  localStorage.setItem('wordsPerGroup', form.wordsPerGroup)
  
  // Save profile to backend
  try {
      const userStr = localStorage.getItem('user')
      const userId = userStr ? JSON.parse(userStr).id : 1
      
      await axios.post('/api/user/update-profile', {
          userId,
          nickname: form.nickname,
          email: form.email
      })
      
      ElMessage.success('设置已保存')
      
      // Update local storage
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      user.nickname = form.nickname
      localStorage.setItem('user', JSON.stringify(user))
      window.dispatchEvent(new Event('user-updated'))
      
  } catch (e) {
      ElMessage.error('保存失败')
  }
}

watch(() => form.darkMode, (val) => {
    if (val) {
        document.documentElement.classList.add('dark')
    } else {
        document.documentElement.classList.remove('dark')
    }
})

onMounted(() => {
    fetchProfile()
    // Check system preference or saved preference for dark mode
    // (Optional implementation detail)
})
</script>
