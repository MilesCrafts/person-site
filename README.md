# LEKANG JOURNAL

[![Deploy Vue to GitHub Pages](https://github.com/MilesCrafts/person-site/actions/workflows/deploy.yml/badge.svg)](https://github.com/MilesCrafts/person-site/actions/workflows/deploy.yml)

[在线预览](https://MilesCrafts.github.io/person-site/) · [正式站点](http://journal.lekang.site)

> 一个以文章发布、浏览、搜索和阅读为核心的现代个人内容网站。

LEKANG JOURNAL 2.0 使用 Vue 3 与 Spring Boot 构建。公开前端采用现代、克制的白色阅读界面，目前聚焦文章首页、文章列表、文章详情、搜索和归档，并使用 GSAP 提供可降级的 Article Deck 特色交互。

## 功能特性

- 文章首页、列表、详情、搜索与归档
- 适配桌面端与移动端的响应式布局
- 基于 GSAP 的渐进增强交互
- 完整的 loading、empty 与 error 状态
- 集中的 `/api/v1` API 客户端与开发代理

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vite、TypeScript、Vue Router、SCSS、GSAP |
| 后端 | Java 21、Spring Boot 3.5、Spring Data JPA、Maven |
| 数据与迁移 | PostgreSQL、Flyway |
| 部署 | GitHub Actions、GitHub Pages、Nginx |

## 项目结构

```text
person-site/
├── .github/workflows/  # GitHub Actions
├── frontend/           # Vue 前端应用
├── backend/            # Spring Boot 后端应用
├── deploy/             # 部署配置与说明
├── docs/               # 架构、路线图与状态文档
├── AGENTS.md            # 项目协作规则
└── README.md
```

## 在线预览与部署

GitHub Pages：<https://MilesCrafts.github.io/person-site/>

仓库使用 GitHub Actions 构建并部署 `frontend/dist/`。首次部署前，请在仓库的 **Settings → Pages** 中将 **Source** 设置为 **GitHub Actions**。

- 推送到 `main` 分支会自动触发部署，也可以在 Actions 页面手动运行。
- 项目部署在 `/person-site/` 子路径。
- 工作流会生成 `404.html`，支持 Vue Router history 模式下的页面刷新与直达。

> GitHub Pages 只托管静态前端，不运行 Spring Boot。依赖动态数据的页面仍需要可用的 `/api/v1` 后端服务。

## 本地运行

```bash
cd frontend
npm ci
npm run dev
```

生产构建：

```bash
cd frontend
npm run build
npm run preview
```

公开内容通过同域 `/api/v1` 读取；本地 Mock 只允许在开发模式显式启用。

2.0 范围与实施顺序见 [`docs/v2-technical-design.md`](docs/v2-technical-design.md) 和 [`docs/roadmap.md`](docs/roadmap.md)；后端基线见 [`docs/backend-technical-design.md`](docs/backend-technical-design.md)。
