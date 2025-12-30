<template>
  <div class="space-y-8 animate__animated animate__fadeIn">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h2 class="text-2xl font-bold text-gray-800 dark:text-white">生词本</h2>
        <p class="text-gray-500 dark:text-gray-400 mt-1">复习你的难点词汇，温故而知新</p>
      </div>
      <div class="bg-white dark:bg-gray-800 px-4 py-2 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700">
        <span class="text-sm text-gray-500 dark:text-gray-400">共收藏</span>
        <strong class="text-xl text-indigo-600 dark:text-indigo-400 ml-2">{{ words.length }}</strong>
        <span class="text-sm text-gray-500 dark:text-gray-400 ml-1">个单词</span>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="words.length === 0 && !loading" class="bg-white dark:bg-gray-800 rounded-3xl p-12 text-center shadow-sm border border-gray-100 dark:border-gray-700">
      <div class="w-24 h-24 bg-gray-50 dark:bg-gray-700 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300 dark:text-gray-600">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
        </svg>
      </div>
      <h3 class="text-lg font-bold text-gray-800 dark:text-white mb-2">生词本是空的</h3>
      <p class="text-gray-500 dark:text-gray-400 mb-6">在学习过程中遇到不认识的单词，可以添加到这里重点复习</p>
      <router-link to="/study" class="inline-flex items-center px-6 py-3 bg-indigo-600 hover:bg-indigo-700 dark:bg-indigo-500 dark:hover:bg-indigo-600 text-white rounded-xl font-bold transition-colors">
        去背单词
      </router-link>
    </div>

    <!-- Word List -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="word in words" :key="word.id" class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-gray-700 hover:shadow-md transition-shadow group relative">
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-xl font-bold text-gray-800 dark:text-white">{{ word.word }}</h3>
          <button @click="removeWord(word.id)" class="text-gray-300 dark:text-gray-600 hover:text-red-500 dark:hover:text-red-400 transition-colors p-1" title="移除">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clip-rule="evenodd" />
            </svg>
          </button>
        </div>
        
        <div class="text-sm text-gray-500 dark:text-gray-400 font-mono mb-4">{{ word.phonetic }}</div>
        
        <div class="relative">
          <div :class="{'blur-sm select-none': !word.showMeaning, 'transition-all duration-300': true}" class="text-gray-700 dark:text-gray-300 min-h-[3rem]">
            {{ word.translate }}
          </div>
          <div v-if="!word.showMeaning" 
               @click="word.showMeaning = true"
               class="absolute inset-0 flex items-center justify-center cursor-pointer hover:bg-gray-50/50 dark:hover:bg-gray-700/50 rounded-lg group-inner">
            <span class="text-xs font-bold text-indigo-600 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-900/30 px-3 py-1 rounded-full group-inner-hover:bg-indigo-100 dark:group-inner-hover:bg-indigo-900/50 transition-colors">
              点击查看释义
            </span>
          </div>
        </div>
        
        <div class="mt-4 pt-4 border-t border-gray-50 dark:border-gray-700 text-xs text-gray-400 dark:text-gray-500 flex justify-between items-center">
          <span>加入时间: {{ formatDate(word.createdAt) }}</span>
          <button @click="speak(word.word)" class="text-gray-400 dark:text-gray-500 hover:text-indigo-600 dark:hover:text-indigo-400">
             <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
               <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
             </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const words = ref([])
const loading = ref(true)

const fetchWords = async () => {
  loading.value = true
  try {
    const userStr = localStorage.getItem('user')
    if (!userStr) return
    const user = JSON.parse(userStr)
    
    const res = await axios.get(`/api/notebook?userId=${user.id || 1}`) // Default to 1 if no id
    if (res.data.success) {
      words.value = res.data.words.map(w => ({ ...w, showMeaning: false }))
    }
  } catch (e) {
    console.error("Failed to fetch notebook", e)
  } finally {
    loading.value = false
  }
}

const removeWord = async (id) => {
  if (!confirm('确定要将这个单词移出生词本吗？')) return
  
  try {
    const res = await axios.delete(`/api/notebook/${id}`)
    if (res.data.success) {
      words.value = words.value.filter(w => w.id !== id)
    }
  } catch (e) {
    console.error("Failed to remove word", e)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const speak = (word) => {
  const utterance = new SpeechSynthesisUtterance(word)
  utterance.lang = 'en-US'
  window.speechSynthesis.speak(utterance)
}

onMounted(() => {
  fetchWords()
})
</script>