<template>
  <div class="space-y-8 animate__animated animate__fadeIn">
    
    <!-- 顶部：正在学习卡片 -->
    <div class="bg-white dark:bg-gray-800 rounded-3xl p-6 shadow-sm border border-gray-100 dark:border-gray-700 relative overflow-hidden transition-colors">
      <div class="absolute top-0 right-0 w-64 h-64 bg-gradient-to-br from-indigo-50 dark:from-indigo-900/20 to-transparent rounded-bl-full -mr-16 -mt-16 pointer-events-none"></div>
      
      <div class="flex justify-between items-start relative z-10">
        <div>
          <h2 class="text-xl font-bold text-gray-800 dark:text-white mb-1">正在学习</h2>
          <div class="flex items-center space-x-2 mt-4">
             <!-- 书籍封面模拟 -->
             <div class="w-24 h-32 bg-gradient-to-br from-orange-400 to-orange-500 rounded-lg shadow-md flex flex-col justify-center items-center text-white p-2">
                <span class="text-xs font-bold opacity-80 uppercase tracking-wider">{{ getBookName(currentBook) }}</span>
                <span class="text-2xl font-bold mt-1">大纲</span>
                <div class="w-full h-1 bg-white/30 rounded mt-2"></div>
                <div class="w-3/4 h-1 bg-white/30 rounded mt-1"></div>
             </div>
             
             <!-- 进度信息 -->
             <div class="ml-4 space-y-2">
               <h3 class="text-lg font-bold text-gray-800 dark:text-white">{{ getBookLabel(currentBook) }}</h3>
               <div class="flex items-center text-sm text-gray-500 dark:text-gray-400 space-x-4">
                 <span>已学习 <strong class="text-indigo-600 dark:text-indigo-400">{{ totalLearned }}</strong> 词</span>
                 <span>总词数 <strong class="text-gray-800 dark:text-gray-200">{{ totalWords }}</strong></span>
              </div>
              <div class="w-48 h-2 bg-gray-100 dark:bg-gray-700 rounded-full mt-2 overflow-hidden">
                <div class="h-full bg-indigo-500 rounded-full transition-all duration-500" :style="{ width: (totalWords > 0 ? (totalLearned / totalWords) * 100 : 0) + '%' }"></div>
              </div>
               <div class="mt-2">
                 <button @click="showBookSelector = true" class="text-xs px-3 py-1 bg-orange-50 dark:bg-orange-900/30 text-orange-600 dark:text-orange-400 rounded-full font-medium hover:bg-orange-100 dark:hover:bg-orange-900/50 transition-colors">
                   换本词书
                 </button>
               </div>
             </div>
          </div>
        </div>
        
        <!-- 配套真题 / 生词本 -->
        <div class="hidden">
           <div class="w-32 h-40 border-2 border-dashed border-gray-200 rounded-xl flex flex-col items-center justify-center text-gray-400 hover:border-indigo-300 hover:text-indigo-500 cursor-pointer transition-colors group">
              <span class="text-2xl mb-1 group-hover:scale-110 transition-transform">+</span>
              <span class="text-xs font-medium">配套真题词组</span>
           </div>
        </div>
      </div>
      
      <!-- 功能入口区域 -->
       <div class="mt-6 grid grid-cols-1 md:grid-cols-2 gap-4">
         <!-- 生词本入口 -->
         <router-link to="/notebook" class="flex items-center p-3 bg-gray-50 dark:bg-gray-700/50 rounded-xl hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer transition-colors">
            <div class="w-10 h-10 rounded-lg bg-orange-100 dark:bg-orange-900/50 flex items-center justify-center text-orange-600 dark:text-orange-400 mr-3">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path d="M5 4a2 2 0 012-2h6a2 2 0 012 2v14l-5-2.5L5 18V4z" />
              </svg>
            </div>
            <div class="text-left">
               <p class="text-sm font-bold text-gray-800 dark:text-white">生词本</p>
               <p class="text-xs text-gray-500 dark:text-gray-400">复习收藏的难点词汇</p>
            </div>
         </router-link>
       </div>
     </div>

    <!-- 我的数据 -->
    <div>
      <h2 class="text-xl font-bold text-gray-800 dark:text-white mb-4">我的数据</h2>
      <div class="bg-white dark:bg-gray-800 rounded-3xl p-6 shadow-sm border border-gray-100 dark:border-gray-700 transition-colors">
        
        <!-- 概览 -->
        <div class="mb-6">
           <div class="flex justify-between items-center mb-4">
             <h3 class="font-bold text-gray-700 dark:text-gray-200">概览</h3>
             <button class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"><svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg></button>
           </div>
           <div class="grid grid-cols-2 gap-8">
              <div>
                <div class="text-xs text-gray-500 dark:text-gray-400 mb-1 flex items-center"><span class="w-2 h-2 rounded-full bg-yellow-400 mr-2"></span>今日学习&复习</div>
                <div class="text-2xl font-bold text-gray-800 dark:text-white">{{ todayLearned }} <span class="text-sm font-normal text-gray-400">词</span></div>
              </div>
              <div>
                <div class="text-xs text-gray-500 dark:text-gray-400 mb-1 flex items-center"><span class="w-2 h-2 rounded-full bg-red-400 mr-2"></span>累计学习</div>
                <div class="text-2xl font-bold text-gray-800 dark:text-white">{{ totalLearned }} <span class="text-sm font-normal text-gray-400">词</span></div>
              </div>
              <div>
                <div class="text-xs text-gray-500 dark:text-gray-400 mb-1 flex items-center"><span class="w-2 h-2 rounded-full bg-yellow-400 mr-2"></span>今日总时长</div>
                <div class="text-2xl font-bold text-gray-800 dark:text-white">{{ todayDuration }} <span class="text-sm font-normal text-gray-400">分钟</span></div>
              </div>
              <div>
                <div class="text-xs text-gray-500 dark:text-gray-400 mb-1 flex items-center"><span class="w-2 h-2 rounded-full bg-red-400 mr-2"></span>累计时长</div>
                <div class="text-2xl font-bold text-gray-800 dark:text-white">{{ totalDuration }} <span class="text-sm font-normal text-gray-400">分钟</span></div>
              </div>
           </div>
        </div>

        <div class="h-px bg-gray-100 dark:bg-gray-700 my-6"></div>

        <!-- 日历 -->
        <div>
           <div class="flex justify-between items-center mb-4">
             <div class="flex items-center space-x-4">
               <h3 class="font-bold text-gray-700 dark:text-gray-200">日历</h3>
               <button 
                 @click="handleSignIn" 
                 :disabled="isSignedIn"
                 class="px-3 py-1 rounded-full text-xs font-bold transition-all"
                 :class="isSignedIn ? 'bg-gray-100 dark:bg-gray-700 text-gray-400 cursor-not-allowed' : 'bg-indigo-600 text-white hover:bg-indigo-700 shadow-md'"
               >
                 {{ isSignedIn ? '已签到' : '立即签到' }}
               </button>
             </div>
             <span class="text-xs text-gray-500 dark:text-gray-400">连续签到 <strong class="text-indigo-600 dark:text-indigo-400">{{ streakDays }}</strong> 天</span>
           </div>
           <!-- 简单日历展示 -->
           <div class="flex justify-between text-center">
              <div v-for="(day, index) in calendarDays" :key="index" class="flex flex-col items-center">
                 <span class="text-xs text-gray-400 mb-2 uppercase">{{ day.week }}</span>
                 <div 
                   class="w-8 h-8 flex items-center justify-center rounded-full text-sm font-medium transition-all"
                   :class="day.isToday ? 'bg-indigo-600 text-white shadow-md' : (day.checked ? 'bg-indigo-50 dark:bg-indigo-900/50 text-indigo-600 dark:text-indigo-400' : 'text-gray-600 dark:text-gray-500')"
                 >
                   {{ day.date }}
                 </div>
              </div>
           </div>
        </div>

      </div>
    </div>

    <!-- 词书选择弹窗 -->
    <div v-if="showBookSelector" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4" @click.self="showBookSelector = false">
       <div class="bg-white dark:bg-gray-800 rounded-2xl w-full max-w-md p-6 animate__animated animate__fadeInUp">
          <h3 class="text-xl font-bold text-gray-800 dark:text-white mb-6 text-center">选择词书</h3>
          <div class="space-y-3">
             <button 
               v-for="book in books" 
               :key="book.value"
               @click="selectBook(book.value)"
               class="w-full flex items-center p-4 rounded-xl border-2 transition-all"
               :class="currentBook === book.value ? 'border-indigo-600 bg-indigo-50 dark:bg-indigo-900/30 text-indigo-700 dark:text-indigo-400' : 'border-gray-100 dark:border-gray-700 hover:border-indigo-100 dark:hover:border-indigo-500/50 hover:bg-gray-50 dark:hover:bg-gray-700'"
             >
                <div class="w-10 h-12 bg-gray-200 dark:bg-gray-600 rounded mr-4 flex items-center justify-center text-xs font-bold text-gray-500 dark:text-gray-300">
                   {{ book.short }}
                </div>
                <div class="text-left flex-1">
                   <div class="font-bold text-gray-800 dark:text-white">{{ book.label }}</div>
                   <div class="text-xs text-gray-500 dark:text-gray-400 mt-1">大纲词汇 · 乱序版</div>
                </div>
                <div v-if="currentBook === book.value" class="text-indigo-600 dark:text-indigo-400">
                   <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                </div>
             </button>
          </div>
          
          <div class="mt-6 space-y-3">
            <button @click="handleResetBook" class="w-full py-3 border border-red-100 dark:border-red-900/30 text-red-500 rounded-xl font-bold hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
              重置当前词书进度
            </button>
            <button @click="showBookSelector = false" class="w-full py-3 bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 rounded-xl font-bold hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors">取消</button>
          </div>
       </div>
    </div>


  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// State
const currentBook = ref('CET4')
const showBookSelector = ref(false)
const todayLearned = ref(0)
const totalLearned = ref(112)
const todayDuration = ref(0)
const totalDuration = ref(0)
const streakDays = ref(3)
const totalWords = ref(4500)
const isSignedIn = ref(false)

const books = [
  { label: '四级词汇', value: 'CET4', short: 'CET4' },
  { label: '六级词汇', value: 'CET6', short: 'CET6' },
  { label: '考研词汇', value: 'KAOYAN', short: 'KY' }
]

// Calendar Data
const calendarDays = ref([])

// Methods
const getBookLabel = (val) => books.find(b => b.value === val)?.label || val
const getBookName = (val) => val === 'KAOYAN' ? '考研' : (val === 'CET4' ? '四级' : '六级')

const fetchUserData = async () => {
  try {
    const userStr = localStorage.getItem('user')
    const userId = userStr ? JSON.parse(userStr).id : 1
    
    const res = await axios.get(`/api/user/stats?userId=${userId}`)
    if (res.data.user) {
      currentBook.value = res.data.user.currentBook || 'CET4'
      streakDays.value = res.data.user.streakDays || 0
      // Total words in book
      totalWords.value = currentBook.value === 'CET4' ? 4500 : (currentBook.value === 'CET6' ? 5500 : 6000)
    }
    
    if (res.data.stats) {
      todayLearned.value = res.data.stats.todayLearned || 0
      totalLearned.value = res.data.stats.totalLearned || 0
      todayDuration.value = res.data.stats.todayDuration || 0
      totalDuration.value = res.data.stats.totalDuration || 0
    }

    if (res.data.calendarDays) {
      calendarDays.value = res.data.calendarDays
      // Check if today is signed in (has any activity)
      const today = calendarDays.value.find(d => d.isToday)
      isSignedIn.value = today ? today.checked : false
    }
  } catch (e) {
    console.error("Failed to fetch user data", e)
  }
}

const handleSignIn = async () => {
  try {
    const userStr = localStorage.getItem('user')
    const userId = userStr ? JSON.parse(userStr).id : 1
    
    const res = await axios.post('/api/user/signin', { userId })
    if (res.data.success) {
      streakDays.value = res.data.streakDays
      isSignedIn.value = true
      ElMessage.success('签到成功！')
      // Refresh to show checkmark
      fetchUserData()
    } else {
        if (res.data.message && res.data.message.includes("Already")) {
             isSignedIn.value = true
             ElMessage.info('今日已签到')
        } else {
             ElMessage.warning(res.data.message || '签到失败')
        }
    }
  } catch (e) {
    ElMessage.error('签到失败')
  }
}

const selectBook = async (book) => {
  try {
    const userStr = localStorage.getItem('user')
    const userId = userStr ? JSON.parse(userStr).id : 1
    
    await axios.post('/api/user/book', { userId, book })
    currentBook.value = book
    showBookSelector.value = false
    // Refetch to update words count
    fetchUserData()
  } catch (e) {
    console.error(e)
  }
}

const handleResetBook = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空当前词书的所有学习记录吗？此操作不可恢复。',
      '重置警告',
      {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const userStr = localStorage.getItem('user')
    const userId = userStr ? JSON.parse(userStr).id : 1
    
    const res = await axios.post('/api/user/reset-book', { userId, book: currentBook.value })
    if (res.data.success) {
       ElMessage.success('重置成功')
       fetchUserData()
       showBookSelector.value = false
    }
  } catch (e) {
    if (e !== 'cancel') {
        console.error(e)
        ElMessage.error('重置失败')
    }
  }
}

onMounted(() => {
  fetchUserData()
})
</script>
