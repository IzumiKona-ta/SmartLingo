<template>
  <div class="min-h-screen bg-gray-50 font-sans text-gray-800">
    <div class="max-w-6xl mx-auto px-6 py-8">
      <!-- Header -->
      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-3xl font-bold text-gray-900">真题资源</h1>
          <p class="text-gray-500 mt-2">历年英语真题试卷下载与预览</p>
        </div>
        <button @click="router.push('/')" class="px-4 py-2 bg-white border border-gray-200 rounded-lg text-gray-600 hover:bg-gray-50 transition-colors">
          返回看板
        </button>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="flex justify-center py-12">
        <div class="animate-spin rounded-full h-8 w-8 border-4 border-indigo-100 border-t-indigo-600"></div>
      </div>

      <!-- Content -->
      <div v-else class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-4">
        <div 
          v-for="file in files" 
          :key="file.name" 
          class="aspect-[3/4] rounded-xl shadow-sm hover:shadow-xl transition-all duration-300 cursor-pointer group relative overflow-hidden"
          :class="getCardStyle(file.name)"
          @click="openPreview(file)"
        >
          <!-- Card Content -->
          <div class="absolute inset-0 p-4 flex flex-col justify-between text-white">
            <!-- Top Label -->
            <div class="flex justify-between items-start">
               <span class="text-sm font-medium opacity-90">{{ parseFile(file.name).category }}</span>
               <div class="opacity-0 group-hover:opacity-100 transition-opacity">
                 <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                   <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
                   <path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd" />
                 </svg>
               </div>
            </div>
            
            <!-- Center Title -->
            <div class="text-center">
              <h3 class="text-3xl font-bold tracking-tight mb-1">{{ parseFile(file.name).title }}</h3>
              <div class="h-1 w-8 bg-white/40 rounded-full mx-auto my-2"></div>
              <p class="text-xs font-medium opacity-80">{{ parseFile(file.name).subTitle }}</p>
            </div>

            <!-- Bottom Action -->
            <div class="text-center opacity-80 group-hover:opacity-100 transition-opacity">
               <span class="text-xs bg-white/20 px-2 py-1 rounded backdrop-blur-sm">点击预览</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="!loading && files.length === 0" class="text-center py-12">
        <p class="text-gray-400">暂无资源文件</p>
      </div>
    </div>

    <!-- PDF Preview Modal -->
    <div v-if="showPreview" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 md:p-8">
      <div class="bg-white w-full h-full max-w-6xl rounded-2xl shadow-2xl flex flex-col overflow-hidden animate__animated animate__fadeInUp">
        <!-- Modal Header -->
        <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-white">
           <div class="flex items-center space-x-3">
              <div class="p-2 bg-orange-100 text-orange-600 rounded-lg">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd" />
                </svg>
              </div>
              <div>
                <h3 class="font-bold text-gray-800">{{ currentFile?.name }}</h3>
                <p class="text-xs text-gray-500">在线预览模式</p>
              </div>
           </div>
           <div class="flex items-center space-x-3">
              <a :href="getFileUrl(currentFile?.url)" download class="px-4 py-2 bg-gray-100 text-gray-600 rounded-lg text-sm font-medium hover:bg-gray-200 transition-colors flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                </svg>
                下载文件
              </a>
              <button @click="showPreview = false" class="p-2 hover:bg-red-50 text-gray-400 hover:text-red-500 rounded-lg transition-colors">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
           </div>
        </div>
        <!-- PDF Viewer (Iframe) -->
        <div class="flex-1 bg-gray-100 relative">
           <iframe 
             v-if="currentFile"
             :src="getFileUrl(currentFile.url)" 
             class="w-full h-full border-none"
             title="PDF Preview"
           ></iframe>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const files = ref([])
const loading = ref(true)
const showPreview = ref(false)
const currentFile = ref(null)

const getFileUrl = (url) => {
  return `http://localhost:8080${url}`
}

const parseFile = (filename) => {
  // Expected format: TYPE + YEAR + SESSION + .pdf
  // Example: CET420231.pdf, GEE2019.pdf
  const name = filename.replace('.pdf', '')
  
  // Try to match standard format
  // Group 1: Type (CET4, CET6, GEE)
  // Group 2: Year (4 digits)
  // Group 3: Session (optional 1 digit)
  const match = name.match(/^([a-zA-Z]+)(\d{4})(\d?)$/)
  
  let category = '真题'
  let title = name
  let subTitle = ''

  if (match) {
    const type = match[1].toUpperCase()
    const year = match[2]
    const session = match[3]

    // Parse Type
    if (type === 'CET4') category = '四级'
    else if (type === 'CET6') category = '六级'
    else if (type === 'GEE') category = '考研'
    else category = type

    // Parse Title (Year)
    title = `${year}`

    // Parse Subtitle (Session)
    if (session === '1') subTitle = '上半年'
    else if (session === '2') subTitle = '下半年'
    else if (session) subTitle = session
  } else {
    // Fallback for underscore format if still present
    const parts = name.split('_')
    if (parts.length >= 2) {
       // ... (existing fallback logic if needed, or just skip)
    }
  }

  return { category, title, subTitle }
}

const getCardStyle = (filename) => {
  const { category } = parseFile(filename)
  if (category === '四级') return 'bg-gradient-to-br from-orange-400 to-orange-500'
  if (category === '六级') return 'bg-gradient-to-br from-indigo-400 to-indigo-500'
  if (category === '考研') return 'bg-gradient-to-br from-red-400 to-red-500'
  return 'bg-gradient-to-br from-gray-400 to-gray-500'
}

const openPreview = (file) => {
  currentFile.value = file
  showPreview.value = true
}

const fetchResources = async () => {
  try {
    const res = await axios.get('/api/resources')
    if (res.data.success) {
      files.value = res.data.files
    }
  } catch (e) {
    console.error("Failed to fetch resources", e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchResources()
})
</script>
