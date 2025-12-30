<template>
  <div 
    ref="rootRef"
    class="fixed z-50 flex flex-col items-end"
    :class="{ 'bottom-8 right-8': !hasMoved }"
    :style="containerStyle"
  >
    <!-- Chat Window -->
    <transition name="scale-fade">
      <div v-if="isOpen" ref="chatWindowRef" class="mb-4 w-96 bg-white/80 backdrop-blur-xl border border-white/20 rounded-2xl shadow-2xl flex flex-col overflow-hidden" style="height: 500px;">
        <!-- Header -->
        <div 
          class="bg-indigo-600/90 p-4 text-white flex justify-between items-center backdrop-blur-sm cursor-pointer select-none"
          @mousedown="startDrag"
        >
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center">🤖</div>
            <span class="font-bold">AI 助教</span>
          </div>
          <button @click.stop="isOpen = false" class="text-white/80 hover:text-white cursor-pointer" @mousedown.stop>
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
import { ref, nextTick, computed, onUnmounted, watch } from 'vue'
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
const chatWindowRef = ref(null)

// Dragging Logic
const rootRef = ref(null)
const hasMoved = ref(false)
// Use right/bottom for better expansion behavior
const position = ref({ right: 32, bottom: 32 }) 
const isDragging = ref(false)
const dragOffset = ref({ x: 0, y: 0 })
const isLongPress = ref(false)
const pressTimer = ref(null)
const startPos = ref({ x: 0, y: 0 })

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

// Button Drag Logic (Immediate drag on move)
const handleButtonMouseDown = (e) => {
  if (e.button !== 0) return
  
  startPos.value = { x: e.clientX, y: e.clientY }
  isDragging.value = false // Not dragging yet
  
  window.addEventListener('mousemove', checkDragStart)
  window.addEventListener('mouseup', handleButtonMouseUp)
  window.addEventListener('touchmove', checkDragStart)
  window.addEventListener('touchend', handleButtonMouseUp)
}

const checkDragStart = (e) => {
    const clientX = e.touches ? e.touches[0].clientX : e.clientX
    const clientY = e.touches ? e.touches[0].clientY : e.clientY
    
    const dx = Math.abs(clientX - startPos.value.x)
    const dy = Math.abs(clientY - startPos.value.y)
    
    // Threshold to consider it a drag
    if (dx > 5 || dy > 5) {
        window.removeEventListener('mousemove', checkDragStart)
        window.removeEventListener('touchmove', checkDragStart)
        initDrag(e)
    }
}

const handleButtonMouseUp = (e) => {
  window.removeEventListener('mousemove', checkDragStart)
  window.removeEventListener('mouseup', handleButtonMouseUp)
  window.removeEventListener('touchmove', checkDragStart)
  window.removeEventListener('touchend', handleButtonMouseUp)
  
  if (isDragging.value) {
    stopDrag()
  } else {
    // It was a click
    isOpen.value = !isOpen.value
  }
}

// Watch isOpen to clamp position if off-screen
watch(isOpen, async (val) => {
  if (val) {
    await nextTick()
    if (!rootRef.value) return
    
    const rect = rootRef.value.getBoundingClientRect()
    const windowHeight = window.innerHeight
    const windowWidth = window.innerWidth
    
    // Check Top overflow (since we anchor bottom, it grows up)
    if (rect.top < 0) {
       // Too high, push down
       // If bottom is set, reduce it
       // rect.top = windowHeight - bottom - height
       // We want rect.top >= 0 => bottom <= windowHeight - height
       
       const height = rect.height
       let currentBottom = position.value.bottom
       if (!hasMoved.value) currentBottom = 32 // default bottom-8
       
       const maxBottom = windowHeight - height
       if (currentBottom > maxBottom) {
           position.value.bottom = Math.max(0, maxBottom)
           // If we modify position, we must set hasMoved to true to apply style
           hasMoved.value = true
       }
    }
    
    // Check Right overflow (shouldn't happen with right anchor, but check left)
    // rect.left = windowWidth - right - width
    // We want rect.left >= 0 => right <= windowWidth - width
    if (rect.left < 0) {
        const width = rect.width
        let currentRight = position.value.right
        if (!hasMoved.value) currentRight = 32
        
        const maxRight = windowWidth - width
        if (currentRight > maxRight) {
            position.value.right = Math.max(0, maxRight)
            hasMoved.value = true
        }
    }
  }
})

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
                    try {
                        // 尝试解析 JSON (后端可能为了安全传输换行符而封装了 JSON)
                        const json = JSON.parse(content)
                        if (json.chunk) {
                            aiMsg.text += json.chunk
                        } else if (json.content) {
                            aiMsg.text += json.content
                        } else {
                            // 纯文本或其他 JSON
                             aiMsg.text += content
                        }
                    } catch (e) {
                        // 不是 JSON，作为纯文本处理
                        aiMsg.text += content
                    }
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
