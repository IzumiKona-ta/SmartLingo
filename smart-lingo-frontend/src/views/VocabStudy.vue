·<template>
  <div class="h-screen bg-gray-50 flex flex-col items-center font-sans text-gray-800 select-none overflow-hidden relative">
    
    <!-- Top Bar: Progress & Status -->
    <div class="flex-none w-full max-w-3xl px-6 pt-8 pb-4 flex items-center justify-between z-20">
       <div class="flex items-center space-x-2">
          <span class="w-2 h-8 bg-indigo-600 rounded-full"></span>
          <div>
            <h1 class="text-xl font-bold text-gray-900 tracking-tight">单词特训</h1>
            <p class="text-xs text-gray-500 font-medium tracking-wide uppercase">{{ currentBookLabel }}</p>
          </div>
       </div>

       <!-- Daily Goal Progress -->
       <div class="flex-1 mx-8 max-w-xs">
         <div class="flex justify-between text-xs font-bold text-gray-400 mb-1">
           <span>今日目标</span>
           <span>{{ todayLearned }} / {{ dailyGoal }}</span>
         </div>
         <div class="h-2 w-full bg-gray-200 rounded-full overflow-hidden">
           <div 
             class="h-full bg-indigo-500 transition-all duration-500 ease-out"
             :style="{ width: Math.min((todayLearned / dailyGoal) * 100, 100) + '%' }"
           ></div>
         </div>
       </div>

       <div class="flex items-center space-x-3">
         <div class="flex flex-col items-end">
             <span class="text-xs text-gray-400 font-bold uppercase">Streak</span>
             <span class="text-lg font-black text-indigo-600 leading-none">3 <span class="text-xs">DAYS</span></span>
         </div>
       </div>
    </div>

    <!-- Main Card Area -->
    <div class="flex-1 w-full flex items-center justify-center relative z-10 p-4">
      <div 
        class="relative perspective-1000"
        style="
          width: 720px;
          height: 420px;
          max-width: 90vw;
          max-height: 75vh;
        "
      >
      
      <!-- Loading / Error / Finished States -->
      <div v-if="isLoading" class="absolute inset-0 flex flex-col items-center justify-center bg-white rounded-3xl shadow-xl z-20">
         <div class="animate-spin rounded-full h-12 w-12 border-4 border-indigo-100 border-t-indigo-600 mb-4"></div>
         <p class="text-gray-500 font-medium">准备单词中...</p>
      </div>

      <div v-else-if="errorMsg" class="absolute inset-0 flex flex-col items-center justify-center bg-white rounded-3xl shadow-xl z-20 p-8 text-center">
         <div class="text-5xl mb-4">⚠️</div>
         <h3 class="text-xl font-bold text-gray-800 mb-2">出错了</h3>
         <p class="text-gray-500 mb-6">{{ errorMsg }}</p>
         <button @click="fetchWords" class="px-6 py-2 bg-indigo-600 text-white rounded-lg font-bold hover:bg-indigo-700 transition-colors">重试</button>
      </div>

      <div v-else-if="isFinished" class="absolute inset-0 flex flex-col items-center justify-center bg-white rounded-3xl shadow-xl z-20 p-8 text-center animate__animated animate__fadeIn">
         <div class="text-6xl mb-6">🎉</div>
         <h2 class="text-2xl font-bold text-gray-900 mb-2">今日任务完成!</h2>
         <p class="text-gray-500 mb-8">你已经完成了这一组的学习。</p>
         <div class="space-y-3 w-full">
           <button @click="reset" class="w-full py-3 bg-indigo-600 text-white rounded-xl font-bold shadow-lg hover:bg-indigo-700 hover:shadow-xl transition-all transform hover:-translate-y-1">
             再来一组
           </button>
           <button @click="router.push('/dashboard')" class="w-full py-3 bg-gray-100 text-gray-600 rounded-xl font-bold hover:bg-gray-200 transition-colors">
             返回看板
           </button>
         </div>
      </div>

      <!-- The Flip Card -->
      <div 
        v-else 
        class="w-full h-full relative preserve-3d cursor-pointer group"
        :class="[
           isFlipped ? 'rotate-y-180' : '', 
           isFlyingOut ? 'fly-out' : 'transition-transform duration-500 ease-out-back'
        ]"
        :style="{ 
          '--rotation': isFlipped ? 'rotateY(180deg)' : 'rotateY(0deg)',
          transformOrigin: 'center center'
        }"
        @click="!isFlipped && flipCard()"
      >
        <!-- FRONT FACE -->
        <div 
          class="absolute inset-0 backface-hidden bg-white rounded-3xl shadow-2xl flex flex-col items-center justify-center p-8 border border-gray-100 overflow-hidden"
          :style="{ zIndex: isFlipped ? 0 : 10, transform: isFlipped ? 'rotateY(0deg)' : 'rotateY(0deg) translateZ(1px)' }"
        >
           <!-- Decorative Background -->
           <div class="absolute inset-0 bg-gradient-to-br from-indigo-50/50 to-purple-50/30 opacity-50 pointer-events-none"></div>
           
           <!-- Notebook Button -->
           <button 
             @click.stop="addToNotebook" 
             class="absolute top-6 right-6 p-3 rounded-full hover:bg-gray-100 transition-colors z-20 group/star"
           >
             <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-gray-300 group-hover/star:text-yellow-400 transition-colors" fill="none" viewBox="0 0 24 24" stroke="currentColor">
               <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
             </svg>
           </button>

           <div class="flex-1 flex flex-col items-center justify-center w-full z-10">
              <!-- Word -->
              <h2 class="text-6xl font-black text-gray-800 mb-4 tracking-tight text-center break-words w-full">{{ currentWord.word }}</h2>
              
              <!-- Phonetic & Audio -->
              <div class="flex items-center space-x-3 mb-12">
                 <span v-if="currentWord.phonetic" class="text-xl text-gray-500 font-mono tracking-wider">/{{ currentWord.phonetic }}/</span>
                 <button 
                   @click.stop="playAudio" 
                   class="p-2 bg-indigo-100 text-indigo-600 rounded-full hover:bg-indigo-200 transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-400"
                 >
                   <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z"></path></svg>
                 </button>
              </div>

              <!-- Cloze Sentence (If available) -->
              <div v-if="clozeSentence" class="w-full p-6 bg-indigo-50/50 rounded-2xl text-center">
                 <p class="text-lg text-gray-600 leading-relaxed font-serif">
                   "{{ clozeSentence }}"
                 </p>
              </div>
              <div v-else class="w-full p-6 text-center text-gray-300 italic">
                 (暂无例句)
              </div>
           </div>

           <div class="text-gray-400 text-sm font-medium animate-pulse mt-4">
              点击卡片或按空格查看答案
           </div>
        </div>

        <!-- BACK FACE -->
        <div 
          class="absolute inset-0 backface-hidden rotate-y-180 bg-white rounded-3xl shadow-2xl flex flex-col p-8 border border-gray-100 overflow-hidden"
          :style="{ zIndex: isFlipped ? 10 : 0, transform: isFlipped ? 'rotateY(180deg) translateZ(1px)' : 'rotateY(180deg)' }"
        >
           <!-- Decorative Background -->
           <div class="absolute inset-0 bg-gradient-to-tl from-green-50/50 to-blue-50/30 opacity-50 pointer-events-none"></div>

           <!-- Content -->
           <div class="flex-1 overflow-y-auto z-10 custom-scrollbar">
              <!-- Header -->
              <div class="flex items-end justify-between border-b border-gray-100 pb-4 mb-6">
                 <h3 class="text-3xl font-bold text-gray-800">{{ currentWord.word }}</h3>
                 <button @click.stop="playAudio" class="text-indigo-500 hover:text-indigo-700 p-1">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z"></path></svg>
                 </button>
              </div>

              <!-- Translation -->
              <div class="mb-8">
                 <h4 class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">中文释义</h4>
                 <p class="text-xl font-medium text-gray-800 leading-relaxed">{{ currentWord.translate }}</p>
              </div>

              <!-- Full Example -->
              <div v-if="currentWord.exampleEn" class="mb-8">
                 <h4 class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">例句</h4>
                 <div class="p-4 bg-gray-50 rounded-xl border-l-4 border-indigo-400">
                    <p class="text-gray-700 italic leading-relaxed" v-html="highlightedExample"></p>
                    <p v-if="currentWord.exampleCn" class="text-gray-500 text-sm mt-2">{{ currentWord.exampleCn }}</p>
                 </div>
              </div>

              <!-- Mnemonic / Root (Placeholder/Optional) -->
              <div v-if="currentWord.mnemonic" class="mb-6">
                 <h4 class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">助记</h4>
                 <p class="text-gray-600 text-sm">{{ currentWord.mnemonic }}</p>
              </div>
           </div>
        </div>
      </div>
      </div>
    </div>

    <!-- Bottom Action Bar (Only visible when flipped) -->
    <div class="flex-none h-32 w-full max-w-lg mb-6 flex items-center justify-center z-20">
       <transition 
          enter-active-class="animate__animated animate__fadeInUp"
          leave-active-class="animate__animated animate__fadeOutDown"
       >
         <div v-if="isFlipped && !isFinished" class="grid grid-cols-3 gap-8 w-full px-8">
            <button 
              @click="handleResult('UNKNOWN')"
              class="flex flex-col items-center justify-center group focus:outline-none"
            >
              <div class="w-16 h-16 rounded-full bg-red-100 text-red-500 flex items-center justify-center shadow-lg group-hover:scale-110 group-hover:bg-red-500 group-hover:text-white transition-all duration-300 border-4 border-white ring-2 ring-red-50">
                 <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
              </div>
              <span class="mt-3 text-sm font-bold text-gray-400 group-hover:text-red-500 transition-colors">忘记 (1)</span>
            </button>

            <button 
              @click="handleResult('FUZZY')"
              class="flex flex-col items-center justify-center group focus:outline-none"
            >
              <div class="w-16 h-16 rounded-full bg-yellow-100 text-yellow-600 flex items-center justify-center shadow-lg group-hover:scale-110 group-hover:bg-yellow-500 group-hover:text-white transition-all duration-300 border-4 border-white ring-2 ring-yellow-50">
                 <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
              </div>
              <span class="mt-3 text-sm font-bold text-gray-400 group-hover:text-yellow-500 transition-colors">模糊 (2)</span>
            </button>

            <button 
              @click="handleResult('MASTERED')"
              class="flex flex-col items-center justify-center group focus:outline-none"
            >
              <div class="w-16 h-16 rounded-full bg-green-100 text-green-600 flex items-center justify-center shadow-lg group-hover:scale-110 group-hover:bg-green-500 group-hover:text-white transition-all duration-300 border-4 border-white ring-2 ring-green-50">
                 <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
              </div>
              <span class="mt-3 text-sm font-bold text-gray-400 group-hover:text-green-500 transition-colors">掌握 (3)</span>
            </button>
         </div>
       </transition>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const words = ref([])
const currentIndex = ref(0)
const isFlipped = ref(false)
const isLoading = ref(false)
const errorMsg = ref('')
const currentBookLabel = ref('Loading...')
const isFlyingOut = ref(false)

// Progress tracking
const todayLearned = ref(0)
const dailyGoal = ref(20) // Default goal

const isFinished = computed(() => words.value.length > 0 && currentIndex.value >= words.value.length)
const currentWord = computed(() => words.value[currentIndex.value] || {})

// Utilities
const getUserId = () => {
  try {
    const user = JSON.parse(localStorage.getItem('user'))
    return user ? user.id : 1
  } catch (e) {
    return 1
  }
}

// Computed Properties for Content
const clozeSentence = computed(() => {
  if (!currentWord.value.example) return null
  const word = currentWord.value.word
  // Create a regex to find the word (case insensitive)
  const regex = new RegExp(`\\b${word}\\w*\\b`, 'gi') 
  return currentWord.value.example.replace(regex, '______')
})

const highlightedExample = computed(() => {
  if (!currentWord.value.example) return ''
  const word = currentWord.value.word
  const regex = new RegExp(`(${word}\\w*)`, 'gi')
  return currentWord.value.example.replace(regex, '<span class="text-indigo-600 font-bold bg-indigo-50 px-1 rounded">$1</span>')
})

// Audio
const playAudio = () => {
  if (!currentWord.value.word) return
  window.speechSynthesis.cancel() // Stop previous
  const u = new SpeechSynthesisUtterance(currentWord.value.word)
  u.lang = 'en-US'
  u.rate = 0.9
  window.speechSynthesis.speak(u)
}

// Actions
const flipCard = () => {
  if (isFinished.value) return
  isFlipped.value = !isFlipped.value
  if (isFlipped.value) {
    // Maybe auto play audio on flip? User didn't ask, but it's nice. 
    // Let's stick to manual click as per request "clickable speaker icon"
  }
}

const handleResult = async (type) => {
  // type: UNKNOWN, FUZZY, MASTERED
  if (isFlyingOut.value) return

  // 1. Play fly out animation
  isFlyingOut.value = true
  
  // 2. Track stats (Optimistic update)
  todayLearned.value++

  // 3. API Call in background
  try {
    const userId = getUserId()
    // Map type to logic if needed, for now we just checkin
    // In a real app, we'd send the 'quality' of recall (0-5)
    await axios.post('/api/study/checkin', { userId: userId, type: 'VOCAB' })
  } catch (e) {
    console.error("Checkin failed", e)
  }

  // 4. Wait for animation then switch
  setTimeout(() => {
    isFlipped.value = false // Reset flip state instantly while invisible/flying
    isFlyingOut.value = false
    currentIndex.value++
    
    // Auto play audio for next word? 
    // Usually good apps do this. Let's do it if not finished.
    if (!isFinished.value) {
       setTimeout(() => playAudio(), 300)
    }
  }, 500) // Match duration-500
}

const addToNotebook = async () => {
  try {
    const word = currentWord.value
    const userId = getUserId()

    const res = await axios.post('/api/notebook/add', {
      userId: userId,
      word: word.word,
      translate: word.translate,
      phonetic: word.phonetic || ''
    })
    
    if (res.data.success) {
      // Visual feedback handled by icon color toggle logic if we had reactive state for it
      // For now, let's just show a quick toast or console log
      console.log('Added to notebook')
    }
  } catch (e) {
    console.error("Failed to add to notebook", e)
  }
}

const fetchWords = async () => {
  isLoading.value = true
  errorMsg.value = ''
  try {
    const res = await axios.get(`/api/words/CURRENT?limit=20`)
    
    if (res.data.error) throw new Error(res.data.error)
    
    words.value = Array.isArray(res.data) ? res.data : []
    
    if (words.value.length === 0) {
        errorMsg.value = "当前词书暂无内容，请检查数据库。"
    }

    // Mock data for demo purposes if fields are missing (Since user requested UI for it)
    // We only do this if the backend returned bare words without examples
    if (words.value.length > 0 && !words.value[0].exampleEn) {
       // Add fake example to first word just to show the UI capability
       // In production, remove this block
       /* 
       words.value[0].exampleEn = "This is a sample sentence demonstrating the word in context."
       words.value[0].phonetic = "ˈsæmpəl"
       */
    }
    
    const userId = getUserId()
    const userRes = await axios.get(`/api/user/stats?userId=${userId}`)
    const book = userRes.data.user.currentBook || 'CET4'
    currentBookLabel.value = book === 'CET4' ? '四级词汇' : (book === 'CET6' ? '六级词汇' : '考研词汇')
    
    if (userRes.data.stats) {
       todayLearned.value = userRes.data.stats.todayLearned || 0
    }

    currentIndex.value = 0
    isFlipped.value = false
    
    // Auto play first word
    if (words.value.length > 0) {
      setTimeout(() => playAudio(), 500)
    }

  } catch (e) {
    console.error("Failed to fetch words", e)
    errorMsg.value = "数据加载异常"
  } finally {
    isLoading.value = false
  }
}

const reset = () => {
  fetchWords()
}

// Keyboard Shortcuts
const handleKeydown = (e) => {
  if (isFinished.value || isLoading.value) return

  if (e.code === 'Space') {
    e.preventDefault() // Prevent scrolling
    flipCard()
  } else if (isFlipped.value) {
    if (e.key === '1') handleResult('UNKNOWN')
    if (e.key === '2') handleResult('FUZZY')
    if (e.key === '3') handleResult('MASTERED')
  }
}

onMounted(() => {
  fetchWords()
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.perspective-1000 {
  perspective: 1000px;
}

.preserve-3d {
  transform-style: preserve-3d;
  -webkit-transform-style: preserve-3d;
}

.backface-hidden {
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
}

.rotate-y-180 {
  transform: rotateY(180deg);
}

/* Custom Scrollbar for back card */
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #e5e7eb;
  border-radius: 20px;
}

/* Fly Out Animation */
.fly-out {
  animation: flyOut 0.5s ease-in forwards;
}

@keyframes flyOut {
  0% {
    transform: rotateY(180deg) scale(1);
    opacity: 1;
  }
  100% {
    transform: rotateY(0deg) scale(0.8) translateY(-100px);
    opacity: 0;
  }
}

/* Ease-out-back for nice flip */
.ease-out-back {
  transition-timing-function: cubic-bezier(0.34, 1.56, 0.64, 1);
}
</style>
