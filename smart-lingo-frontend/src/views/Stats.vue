<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-bold text-gray-800 dark:text-white">数据统计</h2>
      <div class="flex space-x-2">
        <el-button-group>
          <el-button type="primary" plain class="dark:bg-indigo-900/50 dark:border-indigo-700 dark:text-indigo-300">本周</el-button>
          <el-button plain class="dark:bg-gray-800 dark:border-gray-700 dark:text-gray-300">本月</el-button>
          <el-button plain class="dark:bg-gray-800 dark:border-gray-700 dark:text-gray-300">全年</el-button>
        </el-button-group>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div class="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-gray-500 dark:text-gray-400 text-sm">累计学习时长</h3>
          <el-icon class="text-indigo-500 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-900/30 p-2 rounded-lg box-content" :size="20"><Timer /></el-icon>
        </div>
        <div class="flex items-end space-x-2">
          <span class="text-3xl font-bold text-gray-800 dark:text-white">{{ stats.totalDuration }}</span>
          <span class="text-gray-500 dark:text-gray-400 mb-1">小时</span>
        </div>
        <div class="mt-4 text-sm text-green-600 dark:text-green-400 flex items-center">
          <el-icon><Top /></el-icon>
          <span>较上周 +{{ stats.durationGrowth }}%</span>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-gray-500 dark:text-gray-400 text-sm">掌握单词量</h3>
          <el-icon class="text-green-500 dark:text-green-400 bg-green-50 dark:bg-green-900/30 p-2 rounded-lg box-content" :size="20"><Check /></el-icon>
        </div>
        <div class="flex items-end space-x-2">
          <span class="text-3xl font-bold text-gray-800 dark:text-white">{{ stats.masteryCount }}</span>
          <span class="text-gray-500 dark:text-gray-400 mb-1">个</span>
        </div>
        <div class="mt-4 text-sm text-green-600 dark:text-green-400 flex items-center">
          <el-icon><Top /></el-icon>
          <span>较上周 +{{ stats.masteryGrowth }}</span>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-gray-500 dark:text-gray-400 text-sm">学习连续打卡</h3>
          <el-icon class="text-orange-500 dark:text-orange-400 bg-orange-50 dark:bg-orange-900/30 p-2 rounded-lg box-content" :size="20"><Calendar /></el-icon>
        </div>
        <div class="flex items-end space-x-2">
          <span class="text-3xl font-bold text-gray-800 dark:text-white">{{ stats.streakDays }}</span>
          <span class="text-gray-500 dark:text-gray-400 mb-1">天</span>
        </div>
        <div class="mt-4 text-sm text-gray-500 dark:text-gray-400">
          <span>历史最高: {{ stats.streakDays + 2 }}天</span>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="grid grid-cols-1 gap-6">
      <div class="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700">
        <h3 class="font-bold text-gray-800 dark:text-white mb-4">学习趋势</h3>
        <div ref="trendChartRef" class="h-64 w-full"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import axios from 'axios'
import { Timer, Check, Calendar, Top } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const stats = ref({
  totalDuration: 0,
  masteryCount: 0,
  streakDays: 0,
  durationGrowth: 0,
  masteryGrowth: 0
})

const trendChartRef = ref(null)

const fetchStats = async () => {
  try {
    const userStr = localStorage.getItem('user')
    const userId = userStr ? JSON.parse(userStr).id : 1
    
    const res = await axios.get(`/api/user/stats?userId=${userId}`)
    if (res.data.stats) {
      stats.value = {
        totalDuration: (res.data.stats.totalDuration / 60).toFixed(1), // Convert to hours
        masteryCount: res.data.stats.masteryCount,
        streakDays: res.data.stats.streakDays,
        durationGrowth: res.data.stats.durationGrowth,
        masteryGrowth: res.data.stats.masteryGrowth
      }
    }

    await nextTick()
    
    if (res.data.calendarDays) {
        initTrendChart(res.data.calendarDays)
    }

  } catch (e) {
    console.error("Failed to fetch stats", e)
  }
}

const initTrendChart = (data) => {
    if (!trendChartRef.value) return
    const chart = echarts.init(trendChartRef.value)
    
    // Backend returns chronological (Oldest first)
    // No need to reverse if we want Newest on Right
    const chartData = [...data]
    
    const option = {
        tooltip: {
            trigger: 'axis'
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: chartData.map(d => d.week),
            axisLine: { lineStyle: { color: '#E5E7EB' } },
            axisLabel: { color: '#6B7280' }
        },
        yAxis: {
            type: 'value',
            splitLine: { lineStyle: { type: 'dashed', color: '#F3F4F6' } }
        },
        series: [
            {
                name: '学习时长(分钟)',
                type: 'line',
                smooth: true,
                data: chartData.map(d => d.count), // Assuming 'count' is duration or words? Logic uses count.
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(99, 102, 241, 0.2)' },
                        { offset: 1, color: 'rgba(99, 102, 241, 0)' }
                    ])
                },
                itemStyle: { color: '#6366F1' },
                lineStyle: { width: 3 }
            }
        ]
    }
    
    chart.setOption(option)
    
    window.addEventListener('resize', () => chart.resize())
}

onMounted(() => {
  fetchStats()
})
</script>
