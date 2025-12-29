import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/Layout.vue'
import Dashboard from '@/views/Dashboard.vue'
import VocabStudy from '@/views/VocabStudy.vue'
import Stats from '@/views/Stats.vue'
import Settings from '@/views/Settings.vue'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import Notebook from '@/views/Notebook.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: Dashboard
      },
      {
        path: 'notebook',
        name: 'Notebook',
        component: Notebook
      },
      {
        path: 'study',
        name: 'VocabStudy',
        component: VocabStudy
      },
      {
        path: 'stats',
        name: 'Stats',
        component: Stats
      },
      {
        path: 'settings',
        name: 'Settings',
        component: Settings
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
