<template>
  <div 
    ref="rootRef"
    class="fixed z-50 flex flex-col items-end"
    :class="{ 'bottom-8 right-8': !hasMoved }"
    :style="containerStyle"
  >
    <!-- Chat Window -->
    <transition name="scale-fade">
      <div v-if="isOpen" class="mb-4 w-96 bg-white/80 backdrop-blur-xl border border-white/20 rounded-2xl shadow-2xl flex flex-col overflow-hidden" style="height: 500px;">
        <!-- Header -->
        <div 
          class="bg-indigo-600/90 p-4 text-white flex justify-between items-center backdrop-blur-sm cursor-move select-none"
          @mousedown="startDrag"
        >
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center">🤖</div>
            <span class="font-bold">AI 助教</span>
          </div>
          <button @click.stop="isOpen = false" class="text-white/80 hover:text-white" @mousedown.stop>
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
             <input 
               v-model="inputText" 
               @keyup.enter="sendMessage"
               type="text" 
               placeholder="请输入问题..." 
               class="flex-1 bg-white/80 border-none rounded-full px-4 py-2 text-sm focus:ring-2 focus:ring-indigo-500 outline-none shadow-sm"
             />
             <button 
               @click="sendMessage" 
               :disabled="!inputText"
               class="p-2 bg-indigo-600 text-white rounded-full hover:bg-indigo-700 disabled:opacity-50 transition-colors shadow-lg"
             >
               <Position class="w-5 h-5" />
             </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- Trigger Button -->
    <button 
      @mousedown="handleButtonMouseDown"
      @mouseup="handleButtonMouseUp"
      @touchstart="handleButtonMouseDown"
      @touchend="handleButtonMouseUp"
      class="w-14 h-14 bg-indigo-600 rounded-full shadow-2xl flex items-center justify-center text-white hover:bg-indigo-700 hover:scale-110 transition-all duration-300 group cursor-pointer select-none"
    >
      <ChatDotRound v-if="!isOpen" class="w-8 h-8 group-hover:rotate-12 transition-transform" />
      <Close v-else class="w-8 h-8" />
    </button>
  </div>
</template>

<script setup>
import { ref, nextTick, computed, onUnmounted } from 'vue'
import { ChatDotRound, Close, Position } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import axios from 'axios'

const isOpen = ref(false)
const inputText = ref('')
const messages = ref([
  { role: 'ai', text: '你好，我是你的 AI 助教！' }
])
const isLoading = ref(false)
const messagesContainer = ref(null)

// Dragging Logic
const rootRef = ref(null)
const hasMoved = ref(false)
// Use right/bottom for better expansion behavior
const position = ref({ right: 32, bottom: 32 }) 
const isDragging = ref(false)
const dragOffset = ref({ x: 0, y: 0 })
const isLongPress = ref(false)
const pressTimer = ref(null)

const containerStyle = computed(() => {
  if (!hasMoved.value) return {}
  return {
    right: `${position.value.right}px`,
    bottom: `${position.value.bottom}px`,
    top: 'auto',
    left: 'auto'
  }
})

// Header Drag (Immediate)
const startDrag = (e) => {
  if (e.button !== 0) return
  initDrag(e)
}

// Button Long Press Logic
const handleButtonMouseDown = (e) => {
  // Reset states
  isLongPress.value = false
  
  // Start 1s timer
  pressTimer.value = setTimeout(() => {
    isLongPress.value = true
    initDrag(e)
  }, 1000)
}

const handleButtonMouseUp = () => {
  if (pressTimer.value) {
    clearTimeout(pressTimer.value)
    pressTimer.value = null
  }
  
  if (isDragging.value) {
    stopDrag()
  } else if (!isLongPress.value) {
    // If not a long press (drag), treat as click
    isOpen.value = !isOpen.value
  }
  isLongPress.value = false
}

const initDrag = (e) => {
  const rect = rootRef.value.getBoundingClientRect()
  const clientX = e.touches ? e.touches[0].clientX : e.clientX
  const clientY = e.touches ? e.touches[0].clientY : e.clientY

  if (!hasMoved.value) {
    // Initial sync
    const windowWidth = window.innerWidth
    const windowHeight = window.innerHeight
    position.value = {
      right: windowWidth - rect.right,
      bottom: windowHeight - rect.bottom
    }
    hasMoved.value = true
  }

  // Calculate offset from the bottom-right corner of the element
  // right = windowWidth - (clientX + offsetFromRight)
  // offsetFromRight = (windowWidth - right) - clientX => rect.right - clientX
  // Actually simpler:
  // We want to maintain relative position of mouse within the element.
  // But since we position by right/bottom, let's track the offset from the anchor.
  
  dragOffset.value = {
    x: rect.right - clientX,
    y: rect.bottom - clientY
  }

  isDragging.value = true
  
  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('mouseup', stopDrag)
  window.addEventListener('touchmove', onMouseMove)
  window.addEventListener('touchend', stopDrag)
}

const onMouseMove = (e) => {
  if (!isDragging.value) return
  e.preventDefault() // Prevent scrolling on touch
  
  const clientX = e.touches ? e.touches[0].clientX : e.clientX
  const clientY = e.touches ? e.touches[0].clientY : e.clientY
  
  const windowWidth = window.innerWidth
  const windowHeight = window.innerHeight

  // Calculate new right/bottom
  // rect.right = clientX + dragOffset.x
  // right = windowWidth - rect.right
  let newRight = windowWidth - (clientX + dragOffset.value.x)
  let newBottom = windowHeight - (clientY + dragOffset.value.y)
  
  // Boundary checks (Keep fully on screen ideally)
  const width = rootRef.value.offsetWidth
  const height = rootRef.value.offsetHeight

  // Clamp right: min 0 (right edge), max windowWidth - width (left edge)
  newRight = Math.max(0, Math.min(newRight, windowWidth - width))
  // Clamp bottom: min 0 (bottom edge), max windowHeight - height (top edge)
  newBottom = Math.max(0, Math.min(newBottom, windowHeight - height))

  position.value = {
    right: newRight,
    bottom: newBottom
  }
}

const stopDrag = () => {
  isDragging.value = false
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('mouseup', stopDrag)
  window.removeEventListener('touchmove', onMouseMove)
  window.removeEventListener('touchend', stopDrag)
}

onUnmounted(() => {
  if (pressTimer.value) clearTimeout(pressTimer.value)
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('mouseup', stopDrag)
})

const md = new MarkdownIt()

const renderMarkdown = (text) => {
  return md.render(text || '')
}

const sendMessage = async () => {
  if (!inputText.value.trim() || isLoading.value) return

  const userText = inputText.value
  const userMsg = {
    role: 'user',
    text: userText
  }
  messages.value.push(userMsg)
  
  inputText.value = ''
  isLoading.value = true
  
  await nextTick()
  scrollToBottom()

  // 创建一个空的 AI 消息用于流式接收
  const aiMsg = {
    role: 'ai',
    text: ''
  }
  messages.value.push(aiMsg)

  try {
    const response = await fetch('/api/ai/chat', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ message: userText })
    })

    if (!response.ok) throw new Error(response.statusText)

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
        const { done, value } = await reader.read()
        if (done) break
        
        const chunk = decoder.decode(value, { stream: true })
        buffer += chunk
        
        // SSE 格式解析: 消息以 \n\n 分隔
        const lines = buffer.split('\n\n')
        // 保留最后一个可能不完整的片段
        buffer = lines.pop()
        
        for (const line of lines) {
            // 处理每一行 (Spring SseEmitter 可能将多行内容拆分为多个 data: 行)
            // 但通常每个 send() 对应一个块
            // 简单的解析逻辑: 提取 data: 后的内容
            const dataLines = line.split('\n')
            for (const dataLine of dataLines) {
                if (dataLine.startsWith('data:')) {
                    // 移除 "data:" 前缀
                    let content = dataLine.slice(5)
                    // 恢复换行符 (如果 SseEmitter 拆分了换行，这里可能需要根据上下文处理，但通常流式文本直接拼接即可)
                    // 注意：Spring SseEmitter 会把换行符转换成多行 data: 
                    // 例如 "A\nB" -> "data:A\ndata:B\n\n"
                    // 所以这里直接拼接 content 即可
                    aiMsg.text += content
                }
            }
        }
        scrollToBottom()
    }

  } catch (e) {
    console.error(e)
    if (!aiMsg.text) {
        aiMsg.text = '抱歉，我连接不到大脑了。请检查后端服务是否启动。'
    } else {
        aiMsg.text += '\n[连接中断]'
    }
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
