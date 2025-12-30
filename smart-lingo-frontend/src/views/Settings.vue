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
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'

const form = reactive({
  nickname: '演示用户',
  email: 'user@example.com',
  dailyGoal: 30,
  wordsPerGroup: parseInt(localStorage.getItem('wordsPerGroup')) || 20,
  darkMode: false,
  notifications: true,
  sound: true
})

const saveSettings = () => {
  localStorage.setItem('wordsPerGroup', form.wordsPerGroup)
  ElMessage.success('设置已保存')
}
</script>
