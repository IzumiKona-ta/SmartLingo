# SmartLingo 智能词汇学习平台

SmartLingo 是一个基于 **Vue 3** 和 **Spring Boot** 的全栈智能英语学习平台，旨在通过科学的方法帮助用户高效记忆单词、复习真题，并提供可视化的学习数据统计。

---

## 🚀 主要功能 (Features)

### 1. 📚 单词特训 (Vocabulary Study)
- **智能推荐算法**：根据艾宾浩斯遗忘曲线和用户历史学习数据，智能推荐需要复习的单词。
- **沉浸式学习卡片**：
  - **正面**：展示单词，支持点击发音。
  - **背面**：展示音标、中文释义、英文定义、例句（中英对照）、助记法等详细信息。
  - **翻转动画**：平滑的卡片翻转效果，提升交互体验。
- **生词本集成**：
  - 一键收藏生词（星标功能）。
  - 支持在学习过程中实时标记/取消标记。
- **学习进度追踪**：实时显示今日已学单词数和剩余目标。

### 2. 📝 生词本 (Notebook)
- **个性化词库**：集中展示用户标记的所有生词。
- **便捷管理**：
  - 支持对生词进行复习。
  - 掌握后可移除，不再出现在生词本中。
- **数据同步**：与后端数据库实时同步，保证数据安全。

### 3. 📄 真题资源 (Exam Resources)
- **历年真题库**：收录 CET-4（英语四级）、CET-6（英语六级）、GEE（考研英语）等历年真题 PDF。
- **智能解析与展示**：
  - 自动解析文件名（如 `CET420231`），以美观的卡片形式展示年份、类型（四级/六级/考研）和场次（上半年/下半年）。
  - 不同考试类型采用不同的主题色（四级橙色、六级靛青、考研红色），视觉清晰。
- **在线预览与下载**：
  - **页内预览**：无需跳转新标签页，直接在当前页面弹出全屏 PDF 阅读器，方便对照学习。
  - **资源下载**：支持将 PDF 文件下载到本地。
- **AI 伴学**：预览界面集成 AI 助教悬浮球，遇到不懂的题目可随时提问。

### 4. 📊 数据统计 (Statistics)
- **可视化看板**：
  - **学习时长**：今日及累计学习时长统计。
  - **单词掌握度**：已学单词量、掌握单词量趋势图。
- **打卡记录**：日历形式展示每日学习打卡情况，激励用户坚持学习。

### 5. 🤖 AI 助教 (AI Assistant)
- **全局悬浮球**：贯穿整个应用，随时待命。
- **智能问答**：基于大语言模型，解答词汇用法、长难句分析、真题解析等英语学习问题。

---

## 🛠️ 技术栈 (Tech Stack)

### 前端 (Frontend)
- **框架**：Vue 3 (Composition API)
- **构建工具**：Vite
- **路由管理**：Vue Router 4
- **UI 库**：Element Plus, Tailwind CSS (用于原子化样式)
- **HTTP 客户端**：Axios
- **图标库**：Element Plus Icons

### 后端 (Backend)
- **框架**：Spring Boot 2.7
- **ORM**：MyBatis-Plus
- **数据库**：MySQL 8.0
- **工具库**：Lombok, Hutool
- **资源管理**：Spring Resource Handler (静态资源映射)

---

## 📂 项目结构 (Project Structure)

```
TrainingWeek/
├── smart-lingo-backend/        # 后端项目根目录
│   ├── src/main/java/com/smartlingo/
│   │   ├── controller/         # 控制器 (WordController, ResourceController, etc.)
│   │   ├── entity/             # 实体类 (BaseWord, Cet4Word, UserWord, etc.)
│   │   ├── mapper/             # MyBatis Mapper 接口
│   │   ├── service/            # 业务逻辑层
│   │   └── config/             # 配置类 (WebConfig, CorsConfig)
│   └── src/main/resources/     # 配置文件 (application.yml)
│
├── smart-lingo-frontend/       # 前端项目根目录
│   ├── src/
│   │   ├── components/         # 公共组件 (Layout, AiFloatingChat)
│   │   ├── views/              # 页面组件 (VocabStudy, ExamResources, Dashboard, etc.)
│   │   ├── router/             # 路由配置
│   │   └── assets/             # 静态资源
│   └── vite.config.js          # Vite 配置 (代理设置等)
│
└── resource/                   # 外部资源文件夹 (存放真题 PDF)
```

---

## 🚀 快速开始 (Getting Started)

### 1. 环境准备
- **JDK**：17+
- **Node.js**：16+
- **MySQL**：8.0+
- **Maven**：3.6+

### 2. 数据库设置
1. 创建数据库 `smart_lingo`。
2. 导入提供的 SQL 脚本（如果有），或根据实体类自动生成表结构。
3. 确保 `application.yml` 中的数据库连接信息正确。

### 3. 启动后端
```bash
cd smart-lingo-backend
mvn spring-boot:run
```
后端服务将运行在 `http://localhost:8080`。

### 4. 启动前端
```bash
cd smart-lingo-frontend
npm install
npm run dev
```
前端服务将运行在 `http://localhost:5173`。

### 5. 资源文件配置
- 将真题 PDF 文件放入项目根目录下的 `resource` 文件夹中。
- 命名规范：`TYPE` + `YEAR` + `SESSION` + `.pdf`
  - 示例：`CET420231.pdf` (四级 2023年 上半年)
  - 示例：`GEE2020.pdf` (考研 2020年)

---

## 👨‍💻 作者
- Project by **TrainingWeek Team**

---

## 📝 待办事项 / 未来规划
- [ ] 增加听力训练模块。
- [ ] 引入更多游戏化学习元素。
- [ ] 优化 AI 助教的上下文理解能力。
