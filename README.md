# Smart Lingo - 智能英语学习平台 (Smart Lingo Platform)

> **版本**: 2.0.0
> **最后更新**: 2025-12-30
> **开发者**: Smart Lingo Team

Smart Lingo 是一个基于现代 Web 技术栈构建的全栈式英语学习应用。本项目不仅仅是一个简单的单词记忆工具，而是一个集成了**认知心理学记忆算法**、**沉浸式交互体验**、**数据可视化分析**以及**AI 辅助教学**的综合性平台。

本文档将从架构设计、技术选型、功能实现原理、代码细节等多个维度，对项目进行**原子级**的深度解析。

---

## 📚 目录 (Table of Contents)

1.  [项目背景与设计理念](#-项目背景与设计理念)
2.  [核心技术栈深度解析](#-核心技术栈深度解析)
3.  [项目结构与文件说明](#-项目结构与文件说明)
4.  [功能模块原子级实现原理](#-功能模块原子级实现原理)
    *   [4.1 单词特训 (Vocab Study) - 核心背词算法](#41-单词特训-vocab-study---核心背词算法)
    *   [4.2 全局黑夜模式 (Dark Mode) - 架构级主题切换](#42-全局黑夜模式-dark-mode---架构级主题切换)
    *   [4.3 智能 AI 助教 (AI Tutor) - 交互与流式模拟](#43-智能-ai-助教-ai-tutor---交互与流式模拟)
    *   [4.4 真题资源系统 (Resources) - 正则解析与文件流](#44-真题资源系统-resources---正则解析与文件流)
    *   [4.5 数据可视化仪表盘 (Dashboard)](#45-数据可视化仪表盘-dashboard)
    *   [4.6 用户系统与安全 (User & Security)](#46-用户系统与安全-user--security)
5.  [数据库设计 (Database Schema)](#-数据库设计-database-schema)
6.  [部署与运行指南](#-部署与运行指南)

---

## 🌟 项目背景与设计理念

在传统的英语学习应用中，用户常面临枯燥、缺乏反馈、难以坚持的痛点。Smart Lingo 的设计初衷是通过**高频的正向反馈**和**极致的 UI 交互**来解决这些问题。

*   **极简主义 (Minimalism)**: 界面去除一切干扰元素，让用户专注于“单词”本身。
*   **即时反馈 (Instant Feedback)**: 每一个操作（点击、拖拽、按键）都有流畅的动画响应。
*   **数据驱动 (Data Driven)**: 用图表量化用户的每一次努力，提升成就感。

---

## 🛠 核心技术栈深度解析

### 前端架构 (Frontend) - Vue 3 Ecosystem

*   **Vue 3 (Composition API)**:
    *   **选型理由**: 相比 Vue 2 的 Options API，Composition API 允许我们将逻辑关注点（如“背词逻辑”、“拖拽逻辑”）封装为独立的函数（Composables），极大地提高了代码的复用性和可维护性。
    *   **应用场景**: `VocabStudy.vue` 中的卡片翻转、评价逻辑都被组织在一起，而不是分散在 data/methods 中。
*   **Vite 5**:
    *   **选型理由**: 基于 ES Modules 的极速开发服务器，冷启动时间几乎为零，HMR（热模块替换）在修改样式时瞬间生效，极大提升开发效率。
*   **Tailwind CSS 3**:
    *   **选型理由**: 原子化 CSS 框架。不同于 Bootstrap 的预制组件，Tailwind 提供了底层的工具类（Utility-first），让我们能精确控制每一个像素（如 `w-64`, `rounded-3xl`）。
    *   **关键特性**: 其内置的 `dark:` 前缀是本项目实现黑夜模式的核心基石。
*   **Animate.css**:
    *   **应用**: 用于页面级别的路由切换动画（FadeIn, SlideUp），增强应用的“原生 App”质感。
*   **Axios**:
    *   **封装**: 我们创建了统一的请求实例，配置了响应拦截器。当后端返回 401 未授权时，前端会自动跳转登录页，实现无感知的安全防护。

### 后端架构 (Backend) - Spring Boot

*   **Spring Boot 2.7.x**:
    *   **核心**: 约定优于配置。内置 Tomcat 容器，简化了 Web 应用的部署。
*   **MyBatis-Plus**:
    *   **选型理由**: 在 MyBatis 基础上只做增强不做改变。它提供的 `BaseMapper` 接口让我们无需编写 XML 就能实现基础的 CRUD 操作，同时其强大的条件构造器（`QueryWrapper`）让复杂的 SQL 查询变得像写 Java 代码一样流畅。
*   **MySQL 8.0**:
    *   **存储**: 采用 InnoDB 引擎，支持事务处理，确保学习记录（StudyLog）的数据一致性。
*   **Lombok**:
    *   **作用**: 通过 `@Data`, `@Builder` 注解自动生成样板代码，让实体类（Entity）保持整洁。

---

## 📂 项目结构与文件说明

```
TrainingWeek/
├── smart-lingo-frontend/          # 前端单页应用
│   ├── public/                    # 静态资源 (favicon, robots.txt)
│   ├── src/
│   │   ├── assets/                # 项目内资源 (Logo, SVG)
│   │   ├── components/            # 公共 UI 组件
│   │   │   ├── AiFloatingChat.vue # 核心组件：可拖拽的 AI 悬浮球
│   │   │   └── Layout.vue         # 核心组件：应用的骨架 (Sidebar + Header)
│   │   ├── router/                # 路由定义 (Vue Router)
│   │   ├── views/                 # 页面级组件
│   │   │   ├── Dashboard.vue      # 仪表盘：聚合了进度、日历、签到功能
│   │   │   ├── VocabStudy.vue     # 核心功能：单词卡片学习界面
│   │   │   ├── Notebook.vue       # 生词本：复习与管理难词
│   │   │   ├── ExamResources.vue  # 资源页：真题列表与预览
│   │   │   ├── Stats.vue          # 统计页：ECharts 图表展示
│   │   │   └── Settings.vue       # 设置页：个人资料与偏好设置
│   │   ├── App.vue                # 根组件
│   │   ├── main.js                # 入口文件 (挂载 Vue, 引入全局样式)
│   │   └── style.css              # Tailwind 指令与全局 CSS 变量
│   ├── tailwind.config.cjs        # Tailwind 配置文件 (定义颜色、黑夜模式策略)
│   └── vite.config.js             # Vite 构建配置
│
├── smart-lingo-backend/           # 后端 RESTful API 服务
│   ├── src/main/java/com/smartlingo/
│   │   ├── config/                # 配置类 (WebMvc, Cors, Swagger)
│   │   ├── controller/            # 控制层 (处理 HTTP 请求)
│   │   ├── entity/                # 实体层 (对应数据库表)
│   │   ├── mapper/                # 持久层 (MyBatis 接口)
│   │   ├── service/               # 业务层 (核心逻辑)
│   │   └── SmartLingoApplication.java # 启动类
│   └── src/main/resources/
│       └── application.yml        # 应用配置文件 (DB 连接, 端口)
│
├── sql/                           # 数据库初始化脚本
│   ├── schema.sql                 # DDL: 建表语句
│   └── user_words.sql             # DML: 初始词库数据导入
└── README.md                      # 本文档
```

---

## 🧩 功能模块原子级实现原理

### 4.1 单词特训 (Vocab Study) - 核心背词算法

这是整个应用的心脏。我们重构了传统的“认识/不认识”二元对立，引入了**三级记忆深度**。

*   **交互逻辑**:
    1.  **卡片展示**: 初始状态下，卡片只显示单词（Word）和音标（Phonetic）。此时释义（Meaning）被隐藏，迫使用户进行主动回忆（Active Recall）。
    2.  **翻转机制**: 用户点击卡片空白处或按下 `Space` 键，`isFlipped` 状态变为 `true`。CSS 类 `.rotate-y-180` 生效，利用 `transform-style: preserve-3d` 实现逼真的 3D 翻转效果，展示背面的释义和例句。
    3.  **评价系统**:
        *   **🤯 不认识 (Unknown / Key 1)**: 用户完全遗忘。
            *   *算法*: 该单词会被立即推入当前学习队列的末尾 (`words.value.push(currentWord)`)，确保在当前 Session 结束前用户必须再次面对它，直到掌握为止。
        *   **🤔 记不太清 (Fuzzy / Key 2)**: 用户有模糊印象但不确定。
            *   *算法*: 记录为一次学习行为，但不标记为“已掌握”。该单词在下次学习 Session 中仍会高优先级出现。
        *   **💡 认识 (Mastered / Key 3)**: 用户能清晰回忆。
            *   *算法*: 调用后端 `/checkin` 接口，在 `user_word_relation` 表中记录状态，并在前端将 `masteredCount` 加一。
    4.  **飞出动画 (Fly-out Animation)**:
        *   评价后，卡片不会生硬消失。我们设置了 `isFlyingOut` 状态，触发 CSS 动画：`transform: translate(100px, -50px) rotate(15deg); opacity: 0;`。
        *   配合 `setTimeout(..., 500)`，在动画播放完毕后才真正切换数据索引 `currentIndex++`，保证视觉流畅性。

### 4.2 全局黑夜模式 (Dark Mode) - 架构级主题切换

我们不仅仅是把背景变黑，而是构建了一套完整的主题系统。

*   **实现原理**:
    *   **Tailwind 配置**: 在 `tailwind.config.cjs` 中设置 `darkMode: 'class'`。这意味着 Tailwind 不会跟随系统偏好，而是查找 HTML 根元素上是否有 `class="dark"`。
    *   **状态管理**: 在 `Settings.vue` 中，我们监听开关变量。
        ```javascript
        watch(isDark, (val) => {
           const html = document.documentElement;
           val ? html.classList.add('dark') : html.classList.remove('dark');
           localStorage.setItem('theme', val ? 'dark' : 'light');
        });
        ```
    *   **持久化与防闪烁**: 为了防止用户刷新页面时从白变黑的刺眼闪烁，我们在 `Layout.vue` 或 `main.js` 的最早期初始化代码中读取 `localStorage` 并立即应用类名。
*   **样式覆盖**:
    *   所有涉及颜色的组件都编写了双重样式。例如卡片背景：`bg-white dark:bg-gray-800`；文字颜色：`text-gray-900 dark:text-gray-100`。
    *   **细节**: 甚至连滚动条、输入框边框、阴影颜色都做了深色适配。

### 4.3 智能 AI 助教 (AI Tutor) - 交互与流式模拟

位于右下角的悬浮球，不仅是一个入口，更是一个充满细节的独立应用。

*   **拖拽算法 (Drag Physics)**:
    *   为了解决原生 HTML5 Drag API 的局限性，我们手写了基于 Mouse 事件的拖拽逻辑。
    *   `mousedown`: 记录鼠标初始位置 `startX`, `startY` 和元素初始位置 `rect.left`, `rect.top`。
    *   `mousemove`: 计算偏移量 `deltaX = currentX - startX`。实时更新 `el.style.left`。
    *   **边界检测**: 在计算新位置时，强制限制 `left` 在 `[0, window.innerWidth - width]` 之间，防止图标被拖出屏幕消失。
    *   **点击 vs 拖拽**: 通过记录 `mousedown` 和 `mouseup` 的时间差和位移差，区分用户的意图是“点击打开聊天”还是“拖拽移动位置”。
*   **流式响应模拟 (Pseudo-Streaming)**:
    *   后端虽然返回的是完整字符串，但为了给用户“AI 正在思考和打字”的真实感，前端实现了一个打字机效果。
    *   代码逻辑：收到回复后，不直接赋值，而是启动一个 `setInterval`，每隔 30ms 截取字符串的前 `i` 个字符赋值给显示变量，直到 `i` 等于字符串长度。

### 4.4 真题资源系统 (Resources) - 正则解析与文件流

*   **文件名解析引擎**:
    *   后端只存储文件名（如 `CET620232.pdf`）。前端负责将其翻译为人类可读格式。
    *   **正则**: `/^([a-zA-Z]+)(\d{4})(\d?)$/`
        *   Group 1 (`CET6`) -> 映射为 "六级真题"
        *   Group 2 (`2023`) -> 映射为 "2023年"
        *   Group 3 (`2`) -> 映射为 "下半年"
    *   **动态样式**: 根据解析出的考试类型，动态分配卡片背景色（四级蓝、六级紫、考研红）。
*   **安全文件预览**:
    *   不直接暴露文件系统路径。后端通过 `/api/resources/{filename}` 接口读取磁盘文件，设置 Content-Type 为 `application/pdf`，并将文件流写入 HttpServletResponse。
    *   前端使用 `iframe` 指向该 API 地址，实现浏览器原生的 PDF 预览。

### 4.5 数据可视化仪表盘 (Dashboard)

*   **进度计算**:
    *   前端获取 `totalLearned` (已学) 和 `totalWords` (总数)。
    *   计算百分比 `percent = (learned / total) * 100`。
    *   绑定到 CSS 变量或内联样式 `width: ${percent}%`，配合 `transition: width 1s ease` 实现进度条加载时的平滑增长动画。
*   **签到逻辑**:
    *   前端不只是简单的“打钩”。它会根据后端返回的 `calendarDays` 数组（包含日期、是否签到、是否是今天），渲染一个微型日历组件。
    *   已签到的日期会获得高亮样式 `bg-indigo-600`。

### 4.6 用户系统与安全 (User & Security)

*   **头像上传 (Avatar Upload)**:
    *   **前端**: 使用 `FormData` 对象封装文件，通过 `multipart/form-data` 格式发送 POST 请求。
    *   **后端**:
        *   接收 `MultipartFile`。
        *   生成唯一文件名（UUID + 时间戳）防止重名覆盖。
        *   **关键安全措施**: 使用 `try-with-resources` 块包裹文件流操作，确保即使发生异常，文件句柄也能被系统回收，防止服务器内存泄漏或文件锁定无法删除的问题。
*   **个人信息修改**:
    *   昵称修改实时同步到全局状态（LocalStorage），确保 Header 和 Sidebar 上的显示立即更新。

---

## 🗄 数据库设计 (Database Schema)

数据库设计遵循第三范式 (3NF)，主要包含以下核心表：

1.  **users (用户表)**:
    *   `id`: 主键
    *   `username`: 登录名
    *   `password`: 加密后的密码
    *   `nickname`: 显示昵称
    *   `avatar`: 头像路径
    *   `current_book`: 当前选择的词书 (CET4/CET6/KAOYAN)
    *   `streak_days`: 连续签到天数

2.  **cet4_words / cet6_words / graduate_words (词库表)**:
    *   `id`: 主键
    *   `word`: 单词拼写
    *   `phonetic`: 音标
    *   `translate`: 中文释义
    *   *设计思考*: 分表存储不同词库，提高查询效率，避免单表数据量过大。

3.  **study_logs (学习日志表)**:
    *   `id`: 主键
    *   `user_id`: 关联用户
    *   `word_id`: 关联单词
    *   `action`: 操作类型 (MASTERED/FORGOT/FUZZY)
    *   `created_at`: 记录时间
    *   *作用*: 用于生成统计图表和计算每日学习量。

4.  **user_notebook (生词本表)**:
    *   `id`: 主键
    *   `user_id`: 用户
    *   `word_id`: 单词
    *   *作用*: 存储用户收藏或标记为不认识的单词，支持多对多关系。

---

## 🚀 部署与运行指南

### 环境要求
*   **JDK**: 1.8 或 11+
*   **Node.js**: 16+
*   **MySQL**: 8.0+

### 步骤 1: 数据库初始化
1.  登录 MySQL。
2.  创建数据库: `CREATE DATABASE smart_lingo;`
3.  导入表结构: 运行 `sql/schema.sql`。
4.  导入基础数据: 运行 `sql/user_words.sql` (此文件包含数千条单词数据，请耐心等待)。

### 步骤 2: 后端启动
1.  使用 IDEA 打开 `smart-lingo-backend`。
2.  修改 `src/main/resources/application.yml`，配置你的 MySQL 用户名和密码。
3.  运行 `SmartLingoApplication` 主类。
4.  控制台看到 "Started SmartLingoApplication" 即表示启动成功 (端口 8080)。

### 步骤 3: 前端启动
1.  在终端进入 `smart-lingo-frontend` 目录。
2.  安装依赖: `npm install` (建议使用淘宝源)。
3.  启动开发服务: `npm run dev`。
4.  浏览器访问: `http://localhost:5173`。

---

## 📝 开发者致谢

Smart Lingo 的诞生离不开开源社区的贡献。特别感谢 Vue.js, Spring Boot, Tailwind CSS 等优秀项目的维护者。

*Enjoy Learning!* 🚀
