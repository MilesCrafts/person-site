# LEKANG JOURNAL 总进度

> 文档所有者：总控线程（原则上仅由总控线程维护）  
> 最近更新时间：2026-08-11（Asia/Shanghai）

## 当前里程碑

V2-5：上线准备。V2-4 文章优先公开站已完成 Mock 与真实 API 浏览器回归；Profile、Now Watching、首页 Feature 和影集扩展延期。

## 总体完成度

约 **82%**。架构、前端目录迁移、公开 API/DTO、管理前后端文章编辑闭环、本机 Flyway V1-V4、PostgreSQL trigram 搜索优化，以及 V2-4 公开前端的 Mock/真实 API 浏览器验收已完成；V2-5 HTTPS、备份、部署和线上验收尚未完成。该百分比是项目级粗略估算，不代表生产就绪度。

## 模块状态

| 模块 | 状态 | 摘要 | 详情 |
|---|---|---|---|
| 前端 | V2-4 已完成 | 个人站改版、GSAP 交互、生产构建及真实 API 三档浏览器验收已完成 | [`frontend-status.md`](frontend-status.md) |
| 后端 | V2-0 已完成 | 公开 API、管理安全/编辑闭环、Flyway V1-V4 和 PostgreSQL `pg_trgm` ranked search 已在本机验证 | [`backend-status.md`](backend-status.md) |
| 部署 | 方案阶段 | 未检查或修改线上环境，`deploy/` 暂无部署实现 | [`deployment-status.md`](deployment-status.md) |

## 已完成事项

- 只读盘点当前目录、目标文档和 Git 状态。
- 确认现有前端代码、图片、构建产物和托管配置均保留原位。
- 保留并引用 `docs/backend-technical-design.md`，未覆盖。
- 建立项目级协作规则、架构、路线图和统一模块状态模板。
- 建立 `frontend/`、`backend/` 和 `deploy/` 空目录。
- 记录多任务、文件所有权、Worktree、API、Flyway、安全和验证规则。
- 经用户确认，将 Vue/Vite 源码与构建配置整体迁入 `frontend/`。
- 更新根目录 README 和架构/状态文档中的命令与路径。
- 保留根目录迁移前的 `dist/`、`node_modules/` 和现有托管绑定，未部署。
- 迁移前后生产构建均通过，29 个构建文件的路径、大小与 SHA-256 完全一致。
- `frontend/` 执行 `npm ci` 成功，审计结果为 0 个已知漏洞。
- 建立 Java 21 / Spring Boot 3.5.4 模块化单体后端和 Maven Wrapper。
- 建立 PostgreSQL/Flyway 内容模型、约束、索引及 12 篇现有文章的稳定 slug 初始数据。
- 实现 `/api/v1` 下的 home、article、taxonomy、archive、search、album 和 profile 公开读取 API。
- 实现稳定分页、DTO 投影、RFC 7807 Problem Details、requestId、请求日志、Caffeine、Actuator 和 OpenAPI。
- 后端使用 Java 21 完成编译，`mvn test` 和 `mvn verify` 通过，并生成可执行 JAR。
- 使用本机 PostgreSQL 18 的独立 `lekang_journal` schema 成功执行 Flyway V1/V2，并导入 12 篇初始文章；未修改既有 `ai_chat` schema。
- 修复 `ArticleQueryService` 多构造器导致的 Spring 启动失败，重新执行 Maven `verify` 并生成 JAR。
- 本地后端 readiness 和 `/api/v1/taxonomies` 返回 200。
- 用户确认当前主线为“修复文章查询 API → 前端接入真实数据 → 开发 `/admin` 编辑后台”，并确认 `/admin` 集成到当前 Vue 应用。
- 前端已建立 `frontend/src/api/`、公开 DTO、显式开发 Mock、Vite `/api` proxy 和公共页面 API 调用基础。
- 建立 [`v2-technical-design.md`](v2-technical-design.md)，将公共前端、管理前端、管理后端和上线门槛统一为 V2 全栈版本。
- 完成 PostgreSQL 搜索优化：启用 `pg_trgm`，为标题/摘要/正文建立 GIN trigram 索引，标题优先相关度排序，并完成代表性 `EXPLAIN ANALYZE`。
- 完成公共页面 1280px、768px、375px 浏览器验收，6 个主路由共 18 个组合无横向溢出或非预期浏览器错误。
- 完成 `/admin` 登录、Session/CSRF、草稿、编辑、Markdown 预览、发布、撤回、归档、公开可见性和退出的真实浏览器闭环。
- 修复平板文章引用块横向溢出，补充 favicon，并收口退出后的 Session 探测。
- 隔离验收数据已清理，库中恢复为 12 篇初始文章，临时文章、管理员和审计记录均为 0。
- 2026-08-10 完成 2.0 文章优先公开站前端实现：白色现代视觉、基础文章首页/列表/详情/搜索/归档、兼容 About 与稳定公开 URL；个人介绍、近期状态、独立兴趣模块、影集扩展和复杂开场未进入首页。
- 2026-08-10 安装 GSAP 3.15.0 并实现“文章卡片叠 / Article Deck”：最多 5 篇真实文章、点击/拖动/触控/键盘切换、减少动态效果和静态降级；GSAP 为动态独立 chunk。
- 2026-08-10 `npm run build` 通过；显式 Mock 模式下 6 个路由 × 3 个视口共 18 个组合均完成加载、无浏览器错误或横向溢出，Article Deck 从 `01 / 05` 正常切换至 `02 / 05`。
- 2026-08-11 使用隔离 PostgreSQL、Java 21 Spring Boot 和关闭 Mock 的 Vite 完成 V2-4 真实联调：8 个公开路由 × 3 个视口共 24 个页面、30 次 `/api/v1` 响应，0 浏览器错误、0 横向溢出；管理匿名守卫正确跳转登录页。
- 2026-08-11 后端 `mvn verify` 通过并生成新 JAR；真实 PostgreSQL 管理写入闭环单独通过并自动清理临时数据，readiness 和 Vite 代理搜索返回 200。

## 正在进行

- V2-5：本地服务与构建已就绪，等待部署授权和生产前安全验收。
- 静态托管工作目录和未来发布方式评估。

## 下一步

1. V2-5：完成 HTTPS、备份、静态托管、Nginx、回滚和上线冒烟。

## 阻塞项

- 用户已决定暂时不创建 Git，因此当前没有分支、提交或 Worktree；这不阻塞本地目录迁移，但阻塞基于 Git 的并行协作。
- `.openai/hosting.json` 仍位于根目录；未确认子目录工作目录或产物发布方式前不得部署新版本。
- 安装 GSAP 后 npm 自动审计摘要报告 1 个 moderate、1 个 high 依赖风险；具体 advisory 尚未获得在线审计授权，未执行自动修复。
- Docker daemon 未运行，3 条 Testcontainers PostgreSQL 集成测试被明确跳过；本机 schema 已完成迁移，可用于针对性回归，但不能替代可重复的容器测试。
- 正式图片访问路径尚未确认，公开 API 当前使用规划中的 `/images/journal/**` URL。
- 未获得线上服务器只读核查或变更授权；本轮不触碰线上环境。
- HTTPS 尚未确认完成，管理端相关阶段不能上线。

## 待用户决定事项

- 将来何时创建 Git 并启用 Worktree。
- 静态托管对子目录构建工作目录的支持，或是否直接发布 `frontend/dist/`。
- HTTPS 启用时间与证书方案。
- `/admin` 第一版管理员初始化方式、Session 有效期和密码轮换流程。
- 正式管理员初始化、密码轮换和 Session 有效期。
- 图片对象存储、CDN、生命周期和备份方案。
- staging 环境和服务器资源预算；本机 PostgreSQL 当前为 18。

## 2026-08-11 首页文案配置交付

- 完成首页首屏文案的管理闭环：管理员在 `/admin/homepage` 编辑，后端以 Flyway V7 持久化，公开首页通过稳定 DTO 读取并保留旧版本降级文案。
- 前端生产构建通过；后端 `mvn test`、`mvn verify` 通过并生成可执行 JAR，27 条测试通过、4 条 PostgreSQL/Testcontainers 集成测试因 Docker 不可用跳过。未迁移本机或生产数据库，未部署线上环境。
- 首页文案配置继续扩展至右侧轮播纸片（小标题、三行正文、底部英文），由 Flyway V8 增量维护；不开放跳转 URL 配置，现有 `/capabilities` 导航语义保持不变。
- 2026-08-12：前端构建与后端 27 条测试通过；后端跳过可执行重打包的 `verify` 通过。普通 `verify` 仅在最终 Spring Boot JAR 重打包时因本地运行进程锁文件失败，未停止服务；Docker 不可用，V8 空库迁移未运行，未修改本机或线上数据库。
# 2026-08-12 留言与管理收件箱

- 已完成：首页私人留言表单、管理员收件箱与未读通知、已读/归档状态、分页和可降级 GSAP 动效。
- 已完成：公开提交校验/蜜罐/单实例限流，管理 Session/角色/CSRF 边界，Flyway V9 与接口测试。
- 验证：前端 `npm run build` 通过；后端 `mvn test` 为 34 个测试、0 失败、0 错误、4 个环境性跳过。
- 未执行：真实 PostgreSQL/Flyway 集成验证、浏览器登录态视觉冒烟、可执行 JAR 重打包、部署与线上操作。
- 运行态发现：2026-08-12 当前本机后端对留言、管理 Session 及未知 API 路径均超过 5～8 秒无响应；前端已增加 12 秒超时恢复，但该进程仍需在获得授权后重启并复核数据库迁移 V9。
