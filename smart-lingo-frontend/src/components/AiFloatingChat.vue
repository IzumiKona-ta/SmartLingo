<template>
  <div class="fixed bottom-8 right-8 z-50 flex flex-col items-end">
    <!-- Chat Window -->
    <transition name="scale-fade">
      <div v-if="isOpen" class="mb-4 w-96 bg-white/80 backdrop-blur-xl border border-white/20 rounded-2xl shadow-2xl flex flex-col overflow-hidden" style="height: 500px;">
        <!-- Header -->
        <div class="bg-indigo-600/90 p-4 text-white flex justify-between items-center backdrop-blur-sm">
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center">🤖</div>
            <span class="font-bold">AI 助教</span>
          </div>
          <button @click="isOpen = false" class="text-white/80 hover:text-white">
            <Close class="w-5 h-5" />
          </button>
        </div>

        <!-- Messages -->
        <div class="flex-1 overflow-y-auto p-4 space-y-4 bg-transparent" ref="messagesContainer">
          <div v-for="(msg, index) in messages" :key="index" :class="['flex', msg.role === 'user' ? 'justify-end' : 'justify-start']">
            <div 
              :class="['max-w-[85%] rounded-2xl p-3 text-sm shadow-sm', 
                msg.role === 'user' ? 'bg-indigo-600 text-white rounded-br-none' : 'bg-white text-gray-800 rounded-bl-none']"
            >
              <div v-if="msg.image" class="mb-2">
                <img :src="msg.image" class="rounded-lg max-h-32 object-cover border border-white/20" />
              </div>
              <div v-html="renderMarkdown(msg.text)" class="prose prose-sm prose-invert"></div>
            </div>
          </div>
          <div v-if="isLoading" class="flex justify-start">
             <div class="bg-white rounded-2xl rounded-bl-none p-3 shadow-sm">
                <div class="flex space-x-1">
                  <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                  <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
                  <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.4s"></div>
                </div>
             </div>
          </div>
        </div>

        <!-- Input -->
        <div class="p-3 bg-white/50 border-t border-gray-100 backdrop-blur-sm">
          <div class="flex items-center space-x-2">
             <label class="cursor-pointer p-2 hover:bg-gray-200/50 rounded-full transition-colors text-gray-500">
               <input type="file" accept="image/*" class="hidden" @change="handleImageUpload" />
               <Picture class="w-5 h-5" />
             </label>
             <input 
               v-model="inputText" 
               @keyup.enter="sendMessage"
               type="text" 
               placeholder="请输入问题..." 
               class="flex-1 bg-white/80 border-none rounded-full px-4 py-2 text-sm focus:ring-2 focus:ring-indigo-500 outline-none shadow-sm"
             />
             <button 
               @click="sendMessage" 
               :disabled="!inputText && !selectedImage"
               class="p-2 bg-indigo-600 text-white rounded-full hover:bg-indigo-700 disabled:opacity-50 transition-colors shadow-lg"
             >
               <Position class="w-5 h-5" />
             </button>
          </div>
          <div v-if="selectedImage" class="mt-2 flex items-center space-x-2 bg-gray-100 rounded-lg p-1 px-2 w-fit">
            <span class="text-xs text-gray-500 truncate max-w-[150px]">{{ selectedImageName }}</span>
            <button @click="clearImage" class="text-gray-400 hover:text-red-500">
              <Close class="w-3 h-3" />
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- Trigger Button -->
    <button 
      @click="isOpen = !isOpen"
      class="w-14 h-14 bg-indigo-600 rounded-full shadow-2xl flex items-center justify-center text-white hover:bg-indigo-700 hover:scale-110 transition-all duration-300 group"
    >
      <ChatDotRound v-if="!isOpen" class="w-8 h-8 group-hover:rotate-12 transition-transform" />
      <Close v-else class="w-8 h-8" />
    </button>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ChatDotRound, Close, Picture, Position } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import axios from 'axios'

const isOpen = ref(false)
const inputText = ref('')
const messages = ref([
  { role: 'ai', text: '你好，我是你的 AI 助教！' }
])
const isLoading = ref(false)
const selectedImage = ref(null)
const selectedImageName = ref('')
const messagesContainer = ref(null)

const md = new MarkdownIt()

const renderMarkdown = (text) => {
  return md.render(text || '')
}

const handleImageUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      selectedImage.value = e.target.result // Base64
      selectedImageName.value = file.name
    }
    reader.readAsDataURL(file)
  }
}

const clearImage = () => {
  selectedImage.value = null
  selectedImageName.value = ''
}

const sendMessage = async () => {
  if ((!inputText.value.trim() && !selectedImage.value) || isLoading.value) return

  const userMsg = {
    role: 'user',
    text: inputText.value,
    image: selectedImage.value
  }
  messages.value.push(userMsg)
  
  const payload = {
    text: inputText.value,
    image: selectedImage.value ? selectedImage.value.split(',')[1] : null // Send base64 without prefix
  }

  inputText.value = ''
  clearImage()
  isLoading.value = true
  
  await nextTick()
  scrollToBottom()

  try {
    const res = await axios.post('/api/ai/chat', payload)
    // Handle Python API response structure.
    let aiText = ''
    if (res.data && res.data.reply) {
      aiText = res.data.reply
    } else if (typeof res.data === 'string') {
      aiText = res.data
    } else {
      aiText = JSON.stringify(res.data)
    }

    messages.value.push({
      role: 'ai',
      text: aiText
    })
  } catch (e) {
    messages.value.push({
      role: 'ai',
      text: '抱歉，我连接不到大脑了。请检查后端服务是否启动。'
    })
  } finally {
    isLoading.value = false
    await nextTick()
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}
</script>

<style scoped>
.scale-fade-enter-active,
.scale-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.scale-fade-enter-from,
.scale-fade-leave-to {
  opacity: 0;
  transform: scale(0.9) translateY(20px);
  transform-origin: bottom right;
}
</style>
