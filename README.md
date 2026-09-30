
# LEKANG JOURNAL

一个使用 Vue 3、Vite、TypeScript、Vue Router、SCSS 与 Spring Boot 构建的个人文章网站。2.0 公开前端已完成文章首页、文章列表、文章详情、搜索和归档的现代白色界面，并使用 GSAP 提供可降级的 Article Deck 特色交互。前端工程位于 `frontend/`，后端工程位于 `backend/`。

## 本地运行

```bash
cd frontend
npm install
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
>>>>>>> master
