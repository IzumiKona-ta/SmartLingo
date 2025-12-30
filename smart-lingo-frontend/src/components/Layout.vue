<template>
  <div class="flex h-screen bg-gray-50 font-sans">
    <!-- Mobile Sidebar Overlay -->
    <div 
      v-if="isMobileMenuOpen" 
      class="fixed inset-0 z-20 bg-black bg-opacity-50 transition-opacity md:hidden"
      @click="isMobileMenuOpen = false"
    ></div>

    <!-- Sidebar -->
    <aside 
      :class="[
        'fixed inset-y-0 left-0 z-30 w-64 bg-white shadow-xl transform transition-transform duration-300 ease-in-out md:translate-x-0 md:static md:inset-0 flex flex-col',
        isMobileMenuOpen ? 'translate-x-0' : '-translate-x-full'
      ]"
    >
      <!-- Logo -->
      <div class="h-16 flex items-center justify-center border-b border-gray-100 bg-white">
        <div class="flex items-center space-x-2">
          <div class="w-8 h-8 bg-indigo-600 rounded-lg flex items-center justify-center">
            <span class="text-white font-bold text-lg">S</span>
          </div>
          <h1 class="text-xl font-bold text-gray-800 tracking-wide">SmartLingo</h1>
        </div>
      </div>

      <!-- Nav Items -->
      <nav class="flex-1 overflow-y-auto py-6">
        <ul class="space-y-1 px-3">
          <li v-for="item in navItems" :key="item.path">
            <router-link 
              :to="item.path"
              custom
              v-slot="{ href, navigate, isActive, isExactActive }"
            >
              <a 
                :href="href" 
                @click="navigate; isMobileMenuOpen = false"
                class="flex items-center px-4 py-3 rounded-xl transition-all duration-200 group relative overflow-hidden cursor-pointer"
                :class="[
                  (item.path === '/' ? isExactActive : isActive) 
                    ? 'bg-indigo-50 text-indigo-600 shadow-sm font-semibold' 
                    : 'text-gray-600 hover:bg-gray-50 hover:text-indigo-600'
                ]"
              >
                <component 
                  :is="item.icon" 
                  class="w-5 h-5 mr-3 transition-transform group-hover:scale-110" 
                />
                <span class="font-medium">{{ item.name }}</span>
                
                <!-- Active Indicator Bar -->
                <div 
                  class="absolute right-0 top-1/2 transform -translate-y-1/2 w-1 h-8 bg-indigo-600 rounded-l-full transition-opacity duration-200"
                  :class="[(item.path === '/' ? isExactActive : isActive) ? 'opacity-100' : 'opacity-0']"
                ></div>
              </a>
            </router-link>
          </li>
        </ul>
      </nav>

      <!-- User Profile -->
      <div class="p-4 border-t border-gray-100 bg-gray-50">
        <div class="flex items-center space-x-3 p-2 rounded-lg hover:bg-white transition-colors cursor-pointer group relative">
          <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center overflow-hidden text-indigo-600 font-bold shadow-sm border border-indigo-200">
            <img v-if="avatar" :src="getAvatarUrl(avatar)" class="w-full h-full object-cover" alt="User" />
            <span v-else>{{ (nickname || username)[0]?.toUpperCase() || 'U' }}</span>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-semibold text-gray-800 truncate">{{ nickname || username }}</p>
            <div class="flex items-center mt-0.5">
              <div class="w-2 h-2 rounded-full bg-green-500 mr-1.5"></div>
              <p class="text-xs text-gray-500">在线</p>
            </div>
          </div>
          
          <!-- Logout Button -->
          <button @click="logout" class="absolute right-2 p-1 text-gray-400 hover:text-red-500 opacity-0 group-hover:opacity-100 transition-opacity" title="退出登录">
             <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
               <path fill-rule="evenodd" d="M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414 1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414 1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293 1.293z" clip-rule="evenodd" />
             </svg>
          </button>
        </div>
      </div>
    </aside>

    <!-- Main Content Wrapper -->
    <div class="flex-1 flex flex-col overflow-hidden min-w-0">
      <!-- Mobile Header -->
      <header class="md:hidden bg-white shadow-sm h-16 flex items-center justify-between px-4 z-10">
        <button 
          @click="isMobileMenuOpen = true"
          class="text-gray-600 hover:text-indigo-600 focus:outline-none"
        >
          <el-icon :size="24"><Menu /></el-icon>
        </button>
        <span class="font-bold text-gray-800">SmartLingo</span>
        <div class="w-8"></div> <!-- Spacer for centering -->
      </header>

      <!-- Main Page Content -->
      <main class="flex-1 overflow-x-hidden overflow-y-auto bg-gray-50 p-4 md:p-8 relative scroll-smooth">
         <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
      </main>

      <!-- AI Chat -->
      <AiFloatingChat />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeFilled, Reading, DataAnalysis, Setting, Menu, Document } from '@element-plus/icons-vue'
import AiFloatingChat from './AiFloatingChat.vue'

const route = useRoute()
const router = useRouter()
const isMobileMenuOpen = ref(false)
const username = ref('User')
const nickname = ref('')
const avatar = ref('')

const getAvatarUrl = (path) => {
    if (!path) return ''
    if (path.startsWith('http')) return path
    return `http://localhost:8080${path}`
}

const navItems = [
  { name: '学习看板', path: '/', icon: HomeFilled },
  { name: '单词特训', path: '/study', icon: Reading },
  { name: '真题资源', path: '/resources', icon: Document },
  { name: '数据统计', path: '/stats', icon: DataAnalysis },
  { name: '设置', path: '/settings', icon: Setting },
]

const updateUserInfo = () => {
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    try {
      const user = JSON.parse(storedUser)
      username.value = user.username || 'User'
      nickname.value = user.nickname || ''
      avatar.value = user.avatar || ''
    } catch (e) {
      console.error('Failed to parse user data', e)
    }
  }
}

onMounted(() => {
  updateUserInfo()
  window.addEventListener('user-updated', updateUserInfo)
})

onUnmounted(() => {
  window.removeEventListener('user-updated', updateUserInfo)
})

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(10px);
}

/* Custom Scrollbar for Sidebar */
nav::-webkit-scrollbar {
  width: 4px;
}
nav::-webkit-scrollbar-track {
  background: transparent;
}
nav::-webkit-scrollbar-thumb {
  background-color: rgba(156, 163, 175, 0.5);
  border-radius: 20px;
}
</style>
