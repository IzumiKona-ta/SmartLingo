<template>
  <div class="space-y-8 animate__animated animate__fadeIn">
    
    <!-- 顶部：正在学习卡片 -->
    <div class="bg-white rounded-3xl p-6 shadow-sm border border-gray-100 relative overflow-hidden">
      <div class="absolute top-0 right-0 w-64 h-64 bg-gradient-to-br from-indigo-50 to-transparent rounded-bl-full -mr-16 -mt-16 pointer-events-none"></div>
      
      <div class="flex justify-between items-start relative z-10">
        <div>
          <h2 class="text-xl font-bold text-gray-800 mb-1">正在学习</h2>
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
               <h3 class="text-lg font-bold text-gray-800">{{ getBookLabel(currentBook) }}</h3>
               <div class="flex items-center text-sm text-gray-500 space-x-4">
                  <span>已学习 <strong class="text-indigo-600">{{ todayLearned }}</strong> 词</span>
                  <span>总词数 <strong class="text-gray-800">{{ totalWords }}</strong></span>
               </div>
               <div class="w-48 h-2 bg-gray-100 rounded-full mt-2 overflow-hidden">
                 <div class="h-full bg-indigo-500 rounded-full" style="width: 2%"></div>
               </div>
               <div class="mt-2">
                 <button @click="showBookSelector = true" class="text-xs px-3 py-1 bg-orange-50 text-orange-600 rounded-full font-medium hover:bg-orange-100 transition-colors">
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
      
      <!-- 生词本入口 -->
      <router-link to="/notebook" class="mt-6 flex items-center p-3 bg-gray-50 rounded-xl hover:bg-gray-100 cursor-pointer transition-colors w-full md:w-auto md:inline-flex">
         <div class="w-10 h-10 rounded-lg bg-orange-100 flex items-center justify-center text-orange-600 mr-3">
           <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
             <path d="M5 4a2 2 0 012-2h6a2 2 0 012 2v14l-5-2.5L5 18V4z" />
           </svg>
         </div>
         <div class="text-left">
            <p class="text-sm font-bold text-gray-800">生词本</p>
            <p class="text-xs text-gray-500">复习收藏的难点词汇</p>
         </div>
      </router-link>
    </div>

    <!-- 我的数据 -->
    <div>
      <h2 class="text-xl font-bold text-gray-800 mb-4">我的数据</h2>
      <div class="bg-white rounded-3xl p-6 shadow-sm border border-gray-100">
        
        <!-- 概览 -->
        <div class="mb-6">
           <div class="flex justify-between items-center mb-4">
             <h3 class="font-bold text-gray-700">概览</h3>
             <button class="text-gray-400 hover:text-gray-600"><svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg></button>
           </div>
           <div class="grid grid-cols-2 gap-8">
              <div>
                <div class="text-xs text-gray-500 mb-1 flex items-center"><span class="w-2 h-2 rounded-full bg-yellow-400 mr-2"></span>今日学习&复习</div>
                <div class="text-2xl font-bold text-gray-800">{{ todayLearned }} <span class="text-sm font-normal text-gray-400">词</span></div>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1 flex items-center"><span class="w-2 h-2 rounded-full bg-red-400 mr-2"></span>累计学习</div>
                <div class="text-2xl font-bold text-gray-800">{{ totalLearned }} <span class="text-sm font-normal text-gray-400">词</span></div>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1 flex items-center"><span class="w-2 h-2 rounded-full bg-yellow-400 mr-2"></span>今日总时长</div>
                <div class="text-2xl font-bold text-gray-800">{{ todayDuration }} <span class="text-sm font-normal text-gray-400">分钟</span></div>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1 flex items-center"><span class="w-2 h-2 rounded-full bg-red-400 mr-2"></span>累计时长</div>
                <div class="text-2xl font-bold text-gray-800">{{ totalDuration }} <span class="text-sm font-normal text-gray-400">分钟</span></div>
              </div>
           </div>
        </div>

        <div class="h-px bg-gray-100 my-6"></div>

        <!-- 日历 -->
        <div>
           <div class="flex justify-between items-center mb-4">
             <h3 class="font-bold text-gray-700">日历</h3>
             <span class="text-xs text-gray-500">连续签到 <strong class="text-indigo-600">{{ streakDays }}</strong> 天</span>
           </div>
           <!-- 简单日历展示 -->
           <div class="flex justify-between text-center">
              <div v-for="(day, index) in calendarDays" :key="index" class="flex flex-col items-center">
                 <span class="text-xs text-gray-400 mb-2 uppercase">{{ day.week }}</span>
                 <div 
                   class="w-8 h-8 flex items-center justify-center rounded-full text-sm font-medium transition-all"
                   :class="day.isToday ? 'bg-indigo-600 text-white shadow-md' : (day.checked ? 'bg-indigo-50 text-indigo-600' : 'text-gray-600')"
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
       <div class="bg-white rounded-2xl w-full max-w-md p-6 animate__animated animate__fadeInUp">
          <h3 class="text-xl font-bold text-gray-800 mb-6 text-center">选择词书</h3>
          <div class="space-y-3">
             <button 
               v-for="book in books" 
               :key="book.value"
               @click="selectBook(book.value)"
               class="w-full flex items-center p-4 rounded-xl border-2 transition-all"
               :class="currentBook === book.value ? 'border-indigo-600 bg-indigo-50 text-indigo-700' : 'border-gray-100 hover:border-indigo-100 hover:bg-gray-50'"
             >
                <div class="w-10 h-12 bg-gray-200 rounded mr-4 flex items-center justify-center text-xs font-bold text-gray-500">
                   {{ book.short }}
                </div>
                <div class="text-left flex-1">
                   <div class="font-bold">{{ book.label }}</div>
                   <div class="text-xs text-gray-500 mt-1">大纲词汇 · 乱序版</div>
                </div>
                <div v-if="currentBook === book.value" class="text-indigo-600">
                   <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                </div>
             </button>
          </div>
          <button @click="showBookSelector = false" class="mt-6 w-full py-3 bg-gray-100 text-gray-600 rounded-xl font-bold hover:bg-gray-200 transition-colors">取消</button>
       </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

// State
const currentBook = ref('CET4')
const showBookSelector = ref(false)
const todayLearned = ref(0)
const totalLearned = ref(112)
const todayDuration = ref(0)
const totalDuration = ref(0)
const streakDays = ref(3)
const totalWords = ref(4500)

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
    }
  } catch (e) {
    console.error("Failed to fetch user stats", e)
  }
}

const selectBook = async (book) => {
  try {
    const userStr = localStorage.getItem('user')
    const userId = userStr ? JSON.parse(userStr).id : 1
    
    await axios.post('/api/user/book', { userId: userId, book })
    currentBook.value = book
    showBookSelector.value = false
    // Refresh stats if needed
    totalWords.value = book === 'CET4' ? 4500 : (book === 'CET6' ? 5500 : 6000)
  } catch (e) {
    console.error("Failed to update book", e)
  }
}

onMounted(() => {
  fetchUserData()
})
</script>
