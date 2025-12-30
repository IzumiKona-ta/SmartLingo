# SmartLingo 智能词汇学习平台系统详解文档

## 1. 项目概述 (Project Overview)

SmartLingo 是一个基于现代 Web 技术栈构建的全栈智能英语学习平台。本项目旨在解决传统背单词软件枯燥、缺乏反馈、资源分散等痛点，通过科学的记忆算法（如艾宾浩斯遗忘曲线的基础实现）、可视化的数据反馈以及沉浸式的学习体验，帮助用户高效掌握英语词汇。

系统集成了 **单词记忆**、**真题训练**、**生词管理**、**数据看板** 四大核心模块，并采用了前后端分离的架构设计，确保了系统的高性能与可扩展性。

---

## 2. 技术架构与技术栈 (Architecture & Tech Stack)

### 2.1 前端架构 (Frontend)
前端采用 **Vue 3 Composition API** 模式开发，利用 Vite 进行极速构建。
- **核心框架**: Vue 3.x
- **状态管理**: 响应式 Ref/Reactive (轻量级状态管理)
- **路由管理**: Vue Router 4.x (History 模式)
- **UI 样式**:
  - **Tailwind CSS**: 用于构建响应式布局、原子化样式（如 Flex/Grid 布局、间距、颜色）。
  - **Element Plus**: 用于复杂的交互组件（如 Message 消息提示、MessageBox 弹窗）。
  - **Animate.css**: 用于页面转场和元素入场动画。
- **网络请求**: Axios (封装了拦截器，统一处理错误)。
- **特效**: CSS 3D Transforms (用于单词卡片翻转)。

### 2.2 后端架构 (Backend)
后端基于 **Spring Boot** 生态构建，提供 RESTful API 服务。
- **核心框架**: Spring Boot 2.7.x
- **持久层**: MyBatis-Plus 3.5.x (大幅简化 CRUD 操作)。
- **数据库**: MySQL 8.0 (存储用户数据、词库、日志)。
- **工具库**: Lombok (简化 Java Bean), Hutool (通用工具类)。
- **API 文档**: 遵循 RESTful 规范。

---

## 3. 功能实现深度解析 (Deep Dive into Features)

### 3.1 看板模块 (Dashboard) - `Dashboard.vue`

看板是用户进入系统的首页，承载了数据概览、状态管理和核心功能入口。

#### 3.1.1 学习数据统计
- **功能描述**：展示用户今日及累计的学习单词数、学习时长。
- **实现原理**：
  - 前端调用 `/api/user/stats` 接口。
  - **后端逻辑 (`UserController.getUserStats`)**：
    - 获取 `currentBook` (当前词书类型，如 CET4)。
    - 查询 `study_logs` 表，条件：`user_id` + `activity_type='VOCAB'` + `book_type=当前书`。
    - **今日数据**：追加条件 `created_at >= 今日0点`。
    - **累计数据**：无时间限制。
    - **学习时长**：目前采用估算逻辑（`单词数 * 1分钟`），后续可扩展为精确计时。

#### 3.1.2 每日签到系统
- **功能描述**：用户每日点击签到，获取积分并维持连续打卡天数 (Streak)。
- **UI 交互**：
  - 按钮状态根据 `isSignedIn` 变量动态变化（蓝色可点 -> 灰色禁用）。
  - 签到成功后触发烟花特效或消息提示。
- **后端逻辑 (`UserController.signIn`)**：
  - **重复检查**：查询今日是否已有 `activity_type='SIGN_IN'` 的记录，若有则返回 "Already signed in"。
  - **连续签到计算**：
    - 查询**昨天**是否有签到记录。
    - 若有：`streak_days = streak_days + 1`。
    - 若无：`streak_days` 重置为 1。
  - **记录写入**：向 `study_logs` 插入一条签到记录。
  - **状态更新**：更新 `users` 表的 `streak_days` 字段。

#### 3.1.3 词书切换与进度管理
- **功能描述**：用户可以在四级、六级、考研词汇之间自由切换，且**进度互不干扰**。
- **实现细节**：
  - **独立进度条**：
    - 进度计算公式：`totalLearned (当前书) / totalWords (当前书) * 100%`。
    - `totalWords` 是硬编码的常量（CET4=4500, CET6=5500, KAOYAN=6000），或者通过 `selectCount` 查询词库表获得。
  - **切换逻辑**：
    - 用户在弹窗中选择新词书。
    - 调用 `/api/user/book` 接口更新 `users` 表的 `current_book` 字段。
    - 前端接收成功响应后，立即重新调用 `fetchUserData` 刷新看板数据（此时后端会根据新的 `current_book` 返回对应的数据）。

#### 3.1.4 词书重置功能
- **功能描述**：允许用户清空当前词书的所有学习记录，重新开始。
- **安全机制**：
  - 前端使用 `ElMessageBox.confirm` 进行二次确认，防止误触。
  - 按钮样式使用红色警告色。
- **后端逻辑 (`UserController.resetBook`)**：
  - 接收 `userId` 和 `book` 参数。
  - 执行 `DELETE FROM study_logs WHERE user_id=? AND book_type=? AND activity_type='VOCAB'`。
  - **注意**：仅删除背词记录，不删除签到记录或生词本记录（除非生词本也绑定了书本类型，目前设计生词本是跨书本的）。

#### 3.1.5 日历热力图 (Calendar Heatmap)
- **功能描述**：展示过去 7 天的学习活跃度。
- **实现原理**：
  - 后端生成一个包含过去 7 天日期的列表。
  - 遍历每一天，查询 `study_logs` 是否有记录（Count > 0）。
  - 返回数据结构：`[{ date: '12', week: 'Mon', checked: true }, ...]`。
  - 前端根据 `checked` 状态渲染不同的颜色圆点。

---

### 3.2 单词特训模块 (Vocabulary Study) - `VocabStudy.vue`

这是系统的核心业务模块，采用了类似 "百词斩" 或 "墨墨背单词" 的卡片式学习流。

#### 3.2.1 动态单词获取 (Fetching Strategy)
- **核心痛点**：传统应用需要预生成 "UserWord" 表，数据量大且维护麻烦。
- **SmartLingo 解决方案**：**动态游标 (Dynamic Cursor)**。
- **后端逻辑 (`StudyController` / `WordController`)**：
  1. **查询已学 ID**：`SELECT word_id FROM study_logs WHERE user_id=? AND book_type=?`。
  2. **排除已学**：`SELECT * FROM cet4_word WHERE id NOT IN (已学IDs) LIMIT 20`。
  3. **优势**：无需维护用户的 "未学列表"，每次请求都实时计算，天然支持 "重置进度"（只需清空日志）。
  4. **边界处理**：如果所有单词都学完了，返回空数组，前端显示 "恭喜完成"。

#### 3.2.2 沉浸式卡片交互 (Card Interaction)
- **3D 翻转技术**：
  - 使用 CSS `perspective` 属性创建 3D 空间。
  - 卡片分为 `.front` (问题面) 和 `.back` (答案面)。
  - `.back` 默认 `rotateY(180deg)`。
  - 点击翻转时，容器整体旋转 180 度。
  - `backface-visibility: hidden` 确保背对屏幕的一面不可见，防止内容透视干扰。
- **键盘快捷键**：
  - 监听 `window.keydown` 事件。
  - `Space`：翻转卡片。
  - `1`：忘记/不认识 (Unknown)。
  - `2`：模糊 (Fuzzy)。
  - `3`：掌握 (Mastered)。
  - `S`：加入/移除生词本 (Star)。

#### 3.2.3 学习流程控制 (Study Flow Control)
- **"认识" (Mastered) 逻辑**：
  - **乐观更新 (Optimistic Update)**：前端先把 `todayLearned` +1，播放卡片飞出动画。
  - **后台同步**：异步发送 `checkin` 请求到后端。
  - **Check-in 接口**：记录 `userId`, `wordId`, `bookType` 到数据库。这是判定 "已学" 的唯一标准。
- **"不认识" (Unknown) 逻辑 - 强化记忆**：
  - **不发送请求**：此时不告诉后端 "我学过了"。
  - **队列追加**：`words.value.push(currentWord)`。
  - **效果**：该单词被移动到当前学习组的**末尾**。用户在学完这组词的最后，会再次遇到这个词，直到点击 "认识" 为止。
  - 这种机制实现了 **"组内即时复习"**，确保过关的单词至少是短期记忆记住的。

#### 3.2.4 学习完成结算
- **状态判断**：当 `currentIndex >= words.length` 时，触发完成状态。
- **结算页**：
  - 展示本次学习的单词列表。
  - 提供 "再来一组" 按钮（重置 index，重新 fetch）和 "返回看板" 按钮。

---

### 3.3 真题资源模块 (Exam Resources) - `ExamResources.vue`

提供历年真题的下载和预览功能。

#### 3.3.1 文件名智能解析 (Smart Parsing)
- **需求**：后台直接扔出一堆文件名（如 `CET420231.pdf`），前端需要美观展示。
- **正则引擎**：
  - 核心正则：`/^([a-zA-Z]+\d*?)((?:19|20)\d{2})(\d?)$/`
  - **Group 1 (Type)**: 匹配 `CET4`, `CET6`, `GEE`。使用非贪婪匹配 `*?` 防止吃掉年份数字。
  - **Group 2 (Year)**: 匹配 `2023`, `1998` 等年份。
  - **Group 3 (Session)**: 匹配可选的 `1` (上半年) 或 `2` (下半年)。
- **容错处理**：如果正则不匹配，回退到简单的 `includes` 判断类型，并尝试提取 4 位数字作为年份。

#### 3.3.2 动态卡片样式
- **视觉区分**：
  - **四级**: 靛蓝渐变 (`from-indigo-500 to-blue-600`) - 沉稳专业。
  - **六级**: 紫罗兰渐变 (`from-violet-500 to-purple-600`) - 进阶挑战。
  - **考研**: 玫瑰红渐变 (`from-rose-500 to-red-600`) - 紧迫热烈。
- **实现**：`getCardStyle` 函数根据解析出的 `category` 返回对应的 Tailwind CSS 类名。

#### 3.3.3 资源预览与下载
- **预览**：
  - 使用 `iframe` 嵌入 PDF 文件 URL。
  - 弹窗模式 (`fixed inset-0`) 覆盖全屏，提供沉浸式阅读。
- **下载**：
  - 标准 `<a href="..." download>` 标签实现文件下载。

---

### 3.4 生词本模块 (Notebook)

跨越所有词书的全局生词管理系统。

#### 3.4.1 数据结构
- **独立表设计 (`notebook` / `user_words`)**：
  - 字段：`id`, `user_id`, `word` (字符串), `created_at`。
  - **设计决策**：存储单词拼写而不是 `word_id`。
  - **原因**：`word_id` 在 `cet4_word` 和 `cet6_word` 表中可能重复或不一致（例如 apple 在四级表 ID=5，在六级表 ID=10）。通过存储拼写，实现了 "一旦标记，处处可见" 的效果。

#### 3.4.2 交互集成
- **背词时标记**：
  - 在 `VocabStudy` 页面，每张卡片右上角有星星按钮。
  - **初始化**：组件加载时，先拉取当前用户所有生词列表 (`Set<String>`)。
  - **状态绑定**：计算属性 `isStarred` 检查当前单词是否在 Set 中。
  - **操作**：
    - 点击星星 -> 调用 `/api/notebook/add` -> 前端 Set 添加。
    - 取消星星 -> 调用 `/api/notebook/delete` -> 前端 Set 移除。

---

### 3.5 用户体系与认证 (User System)

目前处于 MVP (最小可行性产品) 阶段，实现了基础的身份标识。

#### 3.5.1 简易会话管理
- **Local Storage**：用户登录后，用户信息（如 ID, Username）存储在浏览器 `localStorage` 中。
- **自动登录**：页面加载时优先读取 `localStorage`，若无则默认为 ID=1 的测试用户。

#### 3.5.2 测试用户生成器
- **功能**：为了演示图表效果，系统提供了 "创建测试用户" 接口。
- **实现 (`UserController.createTestUser`)**：
  - 指定用户 ID (如 999)。
  - **清空历史**：删除该用户所有日志。
  - **伪造数据**：循环生成过去 5 天的 `study_logs` 记录。
  - 每天生成随机数量 (100-150) 的日志，时间戳分散在当天的不同时刻。
  - 这使得 "学习曲线" 图表能立即展示出波动效果，便于演示和调试。

---

### 3.6 AI 助教模块 (AI Tutor) - `AiFloatingChat.vue`

智能悬浮球组件，提供实时的 AI 问答服务。

#### 3.6.1 悬浮交互逻辑 (Floating Interaction)
- **即时拖拽 (Immediate Drag)**：
  - 为了提升用户体验，放弃了传统的 "长按拖拽" 模式，改为 **"即时拖拽 + 阈值判定"**。
  - **算法**：
    - 监听 `mousedown` / `touchstart` 事件记录起始坐标。
    - 监听 `mousemove` 计算位移 `delta`。
    - 阈值判定：当位移 `> 5px` 时，判定为拖拽行为，进入 `initDrag` 模式；否则视为点击行为，触发 `toggleOpen`。
  - **边界限制**：实时计算 `getBoundingClientRect`，防止悬浮球被拖出屏幕可视区域。

#### 3.6.2 伪流式传输与安全分块 (Simulated Streaming & Safe Chunking)
- **技术挑战**：
  - 远程 Python AI 服务暂不支持标准的 `stream: true` 流式响应。
  - 直接通过 SSE (Server-Sent Events) 转发多行文本（如例句列表）时，换行符会被浏览器误判为 SSE 事件分隔符，导致数据截断或丢失。
- **解决方案**：**"后端分块 + 前端重组"** 策略。
  - **后端 (`AiController`)**：
    1. **降级获取**：使用 `postForObject` 一次性获取 AI 的完整回复。
    2. **安全封装**：将回复文本切割为小块（Chunk Size = 5字符）。
    3. **JSON 包装**：每个块封装为 `{"chunk": "..."}` 格式发送。**核心作用**：JSON 格式化会自动转义换行符（`\n` -> `\\n`），确保通过 SSE 传输时的协议安全性。
    4. **视觉模拟**：在发送块之间人为增加 `20ms` 延时，在前端还原出丝滑的 "打字机" 效果，优化用户体验。
  - **前端 (`AiFloatingChat`)**：
    1. 使用 `fetch` API + `TextDecoder` 读取 ReadableStream。
    2. **流式解析器**：
       - 监听 `data:` 开头的行。
       - 尝试 `JSON.parse` 提取 `chunk` 字段。
       - 实时拼接到消息气泡中，实现逐字显示。

---

### 3.7 系统设置模块 (Settings) - `Settings.vue`

提供用户个性化配置及资料管理功能。

#### 3.7.1 个人资料管理
- **功能描述**：允许用户修改昵称、邮箱以及上传个性化头像。
- **头像上传安全机制**：
  - 前端：使用隐藏的 `<input type="file">` 触发文件选择，通过 `FormData` 封装文件。
  - **后端优化 (`UserController.uploadAvatar`)**：
    - 采用 `try-with-resources` 语法糖自动管理文件流，确保 `InputStream` 在复制完成后被强制关闭，防止文件句柄泄漏导致无法删除旧文件或占用系统资源。
    - 文件名处理：使用 `System.currentTimeMillis()` 加随机数生成唯一文件名，防止重名覆盖。
  - **资源映射**：通过 `WebConfig` 将 URL 路径 `/avatars/**` 映射到本地文件系统，实现静态资源访问。

#### 3.7.2 深色模式 (Dark Mode)
- **实现原理**：
  - **Tailwind 配置**：在 `tailwind.config.cjs` 中开启 `darkMode: 'class'` 模式，允许通过父级类名手动控制。
  - **样式策略**：组件内部全面适配 `dark:` 前缀样式（如 `bg-white dark:bg-gray-800`），确保文字、背景、边框在深色模式下具有足够的对比度。
  - **切换逻辑**：监听开关状态，动态在 `html` 根标签上添加或移除 `dark` 类名，配合 CSS 变量实现毫秒级的主题切换。

## 4. 数据库设计详解 (Database Schema)

### 4.1 Users 表 (用户表)
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | BIGINT | 主键 |
| `username` | VARCHAR | 用户名 |
| `password` | VARCHAR | 密码 (加密存储) |
| `current_book` | VARCHAR | 当前选择的词书 (CET4/CET6/KAOYAN) |
| `streak_days` | INT | 连续打卡天数 |
| `points` | INT | 积分 |

### 4.2 Study Logs 表 (学习日志表)
核心表，记录每一次学习行为。
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | BIGINT | 主键 |
| `user_id` | BIGINT | 用户外键 |
| `word_id` | BIGINT | 单词外键 (对应词库表) |
| `book_type` | VARCHAR | 词书类型 (用于隔离进度) |
| `activity_type` | VARCHAR | 类型: 'VOCAB'(背词), 'SIGN_IN'(签到) |
| `created_at` | DATETIME | 记录时间 |

### 4.3 Notebook 表 (生词本表)
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | BIGINT | 主键 |
| `user_id` | BIGINT | 用户外键 |
| `word` | VARCHAR | 单词拼写 |
| `translate` | VARCHAR | 缓存的中文释义 |
| `created_at` | DATETIME | 收藏时间 |

### 4.4 词库表 (cet4_word / cet6_word 等)
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | BIGINT | 主键 |
| `word` | VARCHAR | 单词拼写 |
| `phonetic` | VARCHAR | 音标 |
| `translate` | TEXT | 中文释义 |
| `example_en` | TEXT | 英文例句 |
| `example_cn` | TEXT | 例句翻译 |

---

## 5. API 接口清单 (API Reference)

### User 相关
- `GET /api/user/stats`: 获取用户统计面板数据。
- `POST /api/user/signin`: 执行每日签到。
- `POST /api/user/book`: 切换当前词书。
- `POST /api/user/reset-book`: 重置当前词书进度。
- `POST /api/user/create-test-user`: 生成测试数据。

### Study 相关
- `POST /api/study/checkin`: 提交单词学习记录。
  - Payload: `{ userId: 1, wordId: 100, type: 'VOCAB', book: 'CET4' }`
- `GET /api/words/CURRENT`: 获取当前用户下一组待学单词。

### Notebook 相关
- `GET /api/notebook`: 获取用户生词列表。
- `POST /api/notebook/add`: 添加生词。
- `DELETE /api/notebook/{id}`: 移除生词。

### Resources 相关
- `GET /api/resources`: 获取真题文件列表。

---

## 6. 部署与运行 (Setup)

1. **数据库准备**:
   - 创建 MySQL 数据库 `smart_lingo`。
   - 运行 SQL 脚本初始化表结构和词库数据。
   - 务必运行 `fix_schema_and_add_user.sql` 确保存储过程和字段完整。

2. **后端启动**:
   - 修改 `application.yml` 中的数据库账号密码。
   - 运行 `SmartLingoApplication.java`。
   - 服务默认运行在 `localhost:8080`。

3. **前端启动**:
   - 进入 `smart-lingo-frontend` 目录。
   - `npm install` 安装依赖。
   - `npm run dev` 启动开发服务器。
   - 访问 `http://localhost:5173`。

---

> 文档最后更新时间: 2025-12-30
> 维护者: SmartLingo Team
