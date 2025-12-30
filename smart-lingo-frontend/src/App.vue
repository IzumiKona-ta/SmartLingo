<script setup>
import { RouterView } from 'vue-router'
import { onMounted } from 'vue'

onMounted(() => {
  // Check system preference or saved preference for dark mode
  if (localStorage.getItem('darkMode') === 'true' || 
     (!localStorage.getItem('darkMode') && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
      document.documentElement.classList.add('dark')
  } else {
      document.documentElement.classList.remove('dark')
  }
})
</script>

<template>
  <RouterView />
</template>

<style>
/* Global styles if needed */
</style>
