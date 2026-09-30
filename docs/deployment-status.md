# LEKANG JOURNAL 部署状态

> 文档所有者：部署线程  
> 最近更新时间：2026-08-03（Asia/Shanghai）

## 模块目标

为 V2 公共前端、`/admin` 和 Spring Boot 管理 API 提供 HTTPS、同域 `/api`、备份、监控和可回滚发布机制。

## 当前分支或工作区

- 工作区：`D:\lekang`
- 当前分支：不可用；当前目录不是可识别的 Git 工作区。
- 推荐未来分支：`codex/deployment`
- 模块目录：`D:\lekang\deploy`

## 已完成

- 已形成目标部署拓扑和 Nginx 代理方案文档。
- 已建立空的 `deploy/` 目录作为可审查部署资产的所有权边界。
- 已明确未经授权不修改服务器、宝塔、Nginx、数据库、域名或 SSL。

## 正在进行

- 无线上操作；按 `docs/v2-technical-design.md` 跟踪 V2-1 公开数据、V2-2 管理安全和 V2-5 上线门槛。

## 下一步

1. 经授权只读确认现有 Nginx、宝塔项目、Java、PostgreSQL、端口、防火墙、证书和备份状态。
2. 根据实际配置形成变更清单、语法检查、冒烟测试和回滚步骤。
3. 确认静态托管是否支持 `frontend/` 子目录构建。
4. V2-1：确认本地 Vite `/api` proxy 和生产 Nginx `/api` 同域代理行为一致。
5. V2-2：评审 Session Cookie、CSRF、管理端 no-store 和 `/admin` SPA fallback。
6. V2-5：完成 HTTPS、数据库备份、Flyway 兼容性、readiness、公开 API、关键页面和回滚检查。

## 阻塞项

- 当前目录不是 Git 工作区，无法建立推荐 Worktree。
- 尚无线上环境核查结果或线上变更授权。
- HTTPS 状态、PostgreSQL 版本、服务器资源和备份状态未知。
- 本地 readiness、公开文章查询和搜索均已通过；公共页面与管理端已完成隔离环境浏览器验收。该结果不是线上部署验收。
- HTTPS 未完成前不得在生产开放 `/admin` 登录、编辑、发布或上传。

## 最近验证结果

- 2026-07-29：本地只读确认 `.openai/hosting.json` 存在。
- 2026-07-29：本地后端 `127.0.0.1:8081` readiness 与 taxonomy 验证通过；这不是线上验证。
- 2026-08-03：本地 Vite 同域 `/api` proxy、18 组公共页面视口组合和管理端完整文章生命周期通过；验收数据已清理。
- 2026-07-29：未连接、检查或修改线上服务器。
- Nginx、数据库、域名、SSL 和线上页面均未做本轮变更。

## 涉及文件

- `deploy/`
- `.openai/hosting.json`（现有托管绑定，只读保留）
- `docs/deployment-status.md`
- `docs/backend-technical-design.md`（部署实施参考）
- `docs/v2-technical-design.md`（V2 上线门槛）

## 需要其他模块配合的事项

- 总控线程：授权边界、变更窗口、HTTPS 决策和上线验收标准。
- 前端线程：Vite `/api` proxy、构建命令、输出目录、公开路由与 `/admin` SPA fallback。
- 后端线程：JAR、环境变量清单、健康检查、Flyway 行为、Session/CSRF/Cookie 和回滚兼容性。
