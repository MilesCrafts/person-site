# LEKANG JOURNAL 后端状态

> 文档所有者：后端线程  
> 最近更新时间：2026-08-11（Asia/Shanghai）

## 模块目标

交付 V2 的稳定公开读取 API、单管理员 Session、安全管理 API 和文章编辑发布闭环；继续使用 Java 21、Spring Boot 3.5、PostgreSQL 和 Flyway 模块化单体。

## 当前分支或工作区

- 工作区：`D:\lekang`
- 当前分支：不可用；当前目录不是可识别的 Git 工作区。
- 模块目录：`D:\lekang\backend`
- 构建 JDK：`D:\jdk-21`（Java 21.0.2）
- 本轮 Maven 本地依赖缓存：环境默认使用 `D:\aa`（项目目录另有 `.m2` 缓存目录）。

## 已完成

- 初始化 Spring Boot 3.5.4 / Java 21 / Maven 工程及标准 Maven Wrapper 配置。
- 建立按业务模块组织的 `article`、`taxonomy`、`album`、`profile`、`homepage`、`search`、`archive`、`media` 和 `common` 包结构。
- 配置 PostgreSQL、Flyway、JPA `ddl-auto=validate`、UTC、连接池、local/prod/test Profile。
- 添加 `V1__create_content_schema.sql`，创建 Category、Topic、Article、Media、Album、Profile 和首页 Feature 数据结构、索引、检查约束、唯一约束及组合外键。
- 添加 `V2__seed_initial_content.sql`，导入现有前端 12 篇文章的稳定 slug、分类、Topic、标题、摘要、发布时间和初始正文，并初始化 Profile、首页 Feature 和影集。
- 实现公开读取 API：
  - `GET /api/v1/home`
  - `GET /api/v1/articles`
  - `GET /api/v1/articles/{slug}`
  - `GET /api/v1/articles/{slug}/navigation`
  - `GET /api/v1/taxonomies`
  - `GET /api/v1/archives`
  - `GET /api/v1/search`
  - `GET /api/v1/albums`
  - `GET /api/v1/albums/{slug}`
  - `GET /api/v1/albums/{slug}/photos`
  - `GET /api/v1/profile`
- 列表 API 使用稳定分页、最大 `size=50` 和后端排序白名单；文章列表使用 DTO 投影，不加载完整正文。
- 添加 RFC 7807 Problem Details、统一异常处理、requestId 生成/透传和请求耗时日志。
- 添加 Spring Security 只读公开边界；管理路径不对匿名用户开放，未启用默认管理账号。
- 配置 Caffeine、Actuator health/readiness、local OpenAPI/Swagger，并在 prod 关闭 OpenAPI/Swagger。
- 添加 Controller 测试和基于 Testcontainers PostgreSQL 的 Flyway/公开 API 集成测试。
- 生成可执行 JAR：`backend/target/lekang-journal-api-0.1.0-SNAPSHOT.jar`。
- 本机 PostgreSQL 18 的独立 `lekang_journal` schema 已成功执行 Flyway V1/V2 并导入初始内容。
- 修复 `ArticleQueryService` 多构造器未标注注入构造器导致的 Spring 启动失败。
- 本地启动后 readiness 与 `/api/v1/taxonomies` 已返回 200。
- 完成 V2-0：文章公开查询不再向 PostgreSQL 传递无类型的空参数，列表、筛选和搜索已在本机 PostgreSQL 18 返回 200。
- 新增 Flyway `V3__add_admin_security.sql`，创建 `admin_user` 与 `admin_audit_log`；迁移不包含账号、密码或密码哈希。
- 完成 V2-2 管理安全基线：
  - `POST/GET/DELETE /api/v1/admin/session`
  - 数据库管理员身份、BCrypt、服务端 Session、CSRF Cookie/Header、管理员权限
  - 登录失败限流、401/403 Problem Details、管理响应 `Cache-Control: no-store`
  - 通过 `JOURNAL_ADMIN_USERNAME` 和 `JOURNAL_ADMIN_PASSWORD` 执行可选的一次性管理员引导
- 完成 V2-3 文章编辑后端闭环：
  - 管理文章分页、创建草稿、详情与更新
  - Markdown 预览、CommonMark 渲染和 Jsoup 白名单清洗
  - 发布、撤回、归档独立动作接口
  - JPA `@Version` 与请求 `version` 乐观锁，旧版本返回 409
  - 发布后 slug 稳定、阅读时间计算、事务提交后公共缓存清理和管理操作审计
- 本机 PostgreSQL 已应用 Flyway V3；真实数据库闭环测试已覆盖登录、创建、编辑、发布、公开可见、409、撤回后 404 和归档，固定测试数据已清理。
- 2026-08-03 使用隔离 PostgreSQL 空库与临时管理员完成前端真实浏览器闭环；Flyway V1～V4、Session/CSRF、文章生命周期和公共可见性均通过，验收文章、管理员及审计数据已清理。
- 最终 Spring Boot JAR 已重新生成并在 `127.0.0.1:8081` 启动。
- 新增 Flyway `V4__add_trigram_search.sql`：在独立 `lekang_journal` schema 启用 `pg_trgm`，并为标题、摘要和 Markdown 正文建立 `lower(...)` GIN trigram 索引；未修改既有 `ai_chat` schema。
- `/api/v1/search` 已切换为 PostgreSQL 原生 ranked projection：标题相似度权重最高，并叠加标题/摘要命中加分，发布时间和 ID 作为稳定的次级排序；搜索结果仍只包含已发布且已到发布时间的文章。
- 针对 PostgreSQL `currentSchema=lekang_journal`，查询中的 `similarity` 和迁移中的 `gin_trgm_ops` 均显式使用 `public` schema，避免扩展函数/操作类因 search path 不含 `public` 而不可见。
- 本地 CORS 白名单补充当前 Vite 地址 `http://localhost:4174` 与 `http://127.0.0.1:4174`；正式前端仍通过同域相对 `/api/v1` 访问，不启用生产宽泛 CORS。
- 后端 README 已同步当前真实能力：管理 Session/CSRF、文章写入、乐观锁、Markdown 清洗和审计均已实现；生产 `/admin` 仍受 HTTPS、Secure Cookie、备份和回滚门槛约束。
- 2026-08-11 完成管理员工作台增强接口：新增 `GET /api/v1/admin/overview`，一次返回草稿、已发布、归档计数与按上海日期稳定轮换的每日文案；文案池可通过 `journal.admin.daily-notes` / `JOURNAL_DAILY_NOTES` 配置。
- 管理文章列表新增标题/slug 搜索与排序白名单：`q` 最长 100 字符，`sort` 支持 `updatedAt`、`createdAt`、`title`，`direction` 支持 `asc`/`desc`；分页上限和稳定 ID 次级排序保持不变。
- 2026-08-11 完成文章封面媒体接口：`GET/POST /api/v1/admin/media` 提供已就绪素材分页和受 Session/CSRF 保护的 multipart 上传，`GET /api/v1/media/{id}/content` 提供公开图片读取；上传限制 JPEG/PNG、8MB、8000px 单边和 4000 万像素，并重新编码清除 EXIF。Flyway V5 扩展 `media_asset` 以支持 `LOCAL_UPLOAD`，文件写入 `JOURNAL_MEDIA_ROOT`，数据库不保存二进制。

## 正在进行

- 2026-08-11：首页单篇 Feature 已向后兼容扩展为最多 5 篇有序精选。新增 Flyway V6、`GET/PUT /api/v1/admin/homepage/features`、发布状态/重复/数量校验、审计与事务提交后首页缓存清理；公共 `/api/v1/home` 新增 `features` 并保留旧 `feature` 字段。

- V2-1～V2-3 已完成本地前后端浏览器验收；后端公开/管理 DTO 和 `/api/v1` 路径保持冻结。
- V2-4 文章优先公开前端已完成真实 API 回归，作品、能力、文章、详情、搜索、归档、关于和管理匿名守卫均通过本地联调；Profile、Now Watching 和首页 Feature 管理 API 延期。
- 当前隔离本地数据库已存在 1 个管理员记录，用户名为 `admin`；密码仅保存为 BCrypt 哈希，当前文档和运行环境均不持有明文。若无法登录，应通过运行时引导或受控重置流程更换密码，不得在文档中记录密码。
- Docker/Testcontainers 的可重复空库测试仍等待 Docker daemon；本轮已用本机 PostgreSQL 补充真实写链路验证。

## 下一步

1. 保持文章、分类、搜索和归档公开 API 稳定，只有契约回归或缺陷才进入后端修改。
2. 确定正式环境管理员初始化和密码轮换流程；临时验收管理员已删除。
3. V2-5：执行 HTTPS、备份、Nginx、Cookie 和生产回滚验收；未满足 HTTPS 门槛前不开放线上管理功能。

## 阻塞项

- Docker CLI 已安装但 Docker daemon 未运行，因此 Testcontainers 测试被明确跳过。
- 本机 PostgreSQL 18 的独立 `lekang_journal` schema 已执行 Flyway；Flyway 当前日志提示 PostgreSQL 18 高于已验证支持的 PostgreSQL 17，需要关注兼容性。
- 生产迁移角色需要具备安装 `pg_trgm` 扩展的权限，或由 DBA 预先在目标数据库安装扩展；当前本机使用 `postgres` 完成验证，未修改生产数据库。
- 当前目录不是 Git 工作区，无法建立推荐 Worktree 或提供 Git diff。
- 正式图片访问路径和 HTTPS 尚未确认；生产管理功能受 HTTPS 门槛阻塞。
- 隔离本地数据库已有 `admin` 管理员记录，但明文密码未知；正式环境管理员仍须通过运行时变量或受控流程初始化，并满足 HTTPS 门槛。

## 最近验证结果

- 2026-08-11：首页精选实现后，使用本机 `D:\jdk-21` 定向执行 `AdminHomepageFeatureControllerTest`，3 条测试全部通过；新增管理精选匿名守卫、成功替换与最多 5 篇校验。Java 21 全量 `mvn verify` 被同一工作区并行新增的 `AdminHomepageCopyControllerTest.blankCopyIsRejected` 既有断言失败阻断（预期自定义 validation type，实际为 `about:blank`），精选相关测试无失败。Docker/Testcontainers 仍因 daemon 不可用跳过，V6 空库迁移断言已加入公共集成测试。
- 隔离本地 PostgreSQL `127.0.0.1:55432` 已由 Flyway 成功从 V5 迁移到 V6（同次启动也应用了并行新增的 V7）；V6 历史记录为成功，既有单篇精选保留为 `sort_order=0`。临时 8082 启动进程已随验证超时结束，未影响现有 8081/4174 服务。
- 停止已确认监听 8081 的旧 PID 10104 后，Java 21 `mvn -DskipTests package` 成功完成 Spring Boot repackage；新 JAR 由 PID 23760 在 `127.0.0.1:8081` 运行。readiness 为 200，直接及 Vite 代理的 `/api/v1/home` 均返回 1 篇 `features`，旧 `feature.slug` 为 `messi-world-cup`，匿名精选管理接口返回 401。前端 4174 与 PostgreSQL 55432 保持运行。

- `mvn test`：2026-08-03 使用 Java 21 通过；11 条测试 0 失败，4 条跳过（本机 PostgreSQL 测试 1 条、Testcontainers 测试 3 条，均因 Docker daemon/测试开关不可用）。
- `mvn verify`：2026-08-03 使用 Java 21 通过；同样为 11 条测试、0 失败、4 条跳过，并生成可执行 JAR。
- `mvn package -DskipTests`：2026-08-03 使用 Java 21 通过，可执行 JAR 已生成。
- Maven 依赖缓存可使用 `D:\aa`，但当前受限执行环境对该目录的元数据写入返回拒绝访问；未把该权限问题误报为项目构建失败。
- 显式本机 PostgreSQL 管理闭环测试：1 条通过，覆盖登录、CSRF 管理写入、草稿、编辑、HTML 清洗、发布、公开读取、旧版本 409、撤回后 404 和归档；测试数据已自动清理。
- Maven Wrapper 生成：标准脚本和 3.9.9 配置已生成；本轮 Wrapper 下载 Maven 发行包超过 120 秒窗口，未取得 Wrapper 命令完成结论。
- PostgreSQL/Flyway：本机 schema 成功从 V2 迁移到 V3，Hibernate `ddl-auto=validate` 启动通过。
- PostgreSQL/Flyway：本机 schema 已成功从 V3 迁移到 V4；`pg_trgm` 扩展和 3 个 trigram GIN 索引均已存在，Hibernate `ddl-auto=validate` 启动通过。
- 本地运行：readiness 200；`/api/v1/search?q=梅西&page=0&size=12` 返回 200 且命中 1 篇；未登录 Session 状态正常；匿名管理列表 401。
- 代表性 `EXPLAIN (ANALYZE, BUFFERS)`（`messi`）执行时间 1.435 ms、命中 58 个 shared buffers；12 篇文章的小表规模下 PostgreSQL 选择 Seq Scan 是预期的成本优化，索引已在迁移中建立，数据量增长后需继续复查计划。
- 单字段索引检查（仅为验证索引可用性临时关闭 Seq Scan）出现 `Bitmap Index Scan on idx_article_title_trgm`；该设置未写入应用或生产配置。
- 线上健康检查和冒烟测试：未执行；未部署或操作线上环境。
- 2026-08-11：Java 21 `mvn test` 通过，11 条测试 0 失败、4 条因 Docker daemon/显式本机测试开关跳过；随后针对 `127.0.0.1:55432` 隔离 PostgreSQL 单独启用 `LocalPostgresAdminIntegrationTest`，1 条完整管理闭环通过并自动清理临时管理员、文章和审计数据。
- 2026-08-11：Java 21 `mvn verify` 通过，11 条测试 0 失败、4 条跳过，生成新的可执行 JAR；JAR 在 `127.0.0.1:8081` 启动，Flyway 验证 V1～V4、readiness、公开 API 矩阵和经 Vite 代理的搜索均返回 200。
- 2026-08-11：按用户要求恢复本地运行环境；隔离 PostgreSQL 在 `127.0.0.1:55432` 接受连接，Spring Boot 进程 PID 25684 监听 `127.0.0.1:8081`，readiness、直接管理 Session、经 Vite 代理的管理 Session 与首页 API 均返回 200。未传管理员引导变量、未创建或修改账号。
- 2026-08-11：管理员增强后 Java 21 `mvn test` 与 `mvn verify` 均通过，14 条测试 0 失败、4 条按环境跳过；新增概览 401/成功响应测试及列表搜索排序参数测试。重新生成可执行 JAR并在 `127.0.0.1:8081` 启动，readiness 为 200，新概览接口匿名访问为 401。未写入文章、管理员或生产数据。
- 2026-08-11：媒体上传接口完成后使用 Java 21 执行 `mvn verify` 通过，共 20 条测试、0 失败、0 错误，4 条因 Docker/Testcontainers 环境跳过；隔离 PostgreSQL `127.0.0.1:55432` 已应用 Flyway V5。新版 JAR 使用 `D:\jdk-21` 启动，进程 PID 10104 监听 `127.0.0.1:8081`；readiness 返回 200，匿名管理素材列表返回 401，不存在的公开媒体内容返回 404，经 Vite `4174` 代理访问管理素材列表同样返回 401。未创建或重置管理员，未执行真实登录上传，未操作线上环境。

## 涉及文件

- `backend/pom.xml`
- `backend/mvnw`、`backend/mvnw.cmd`、`backend/.mvn/wrapper/maven-wrapper.properties`
- `backend/src/main/**`
- `backend/src/test/**`
- 媒体模块：`media/api`、`media/application`、`media/infrastructure`、Flyway V5 与媒体 Controller/Service 测试。
- `backend/README.md`
- `docs/backend-status.md`
- `docs/v2-technical-design.md`
- V2-0：Article Repository、Article Query Service 和公开 API 集成测试。
- V2-2：`admin` 身份/Session/审计模块、Security 配置、错误处理、应用配置和 Flyway V3。
- V2-3：管理文章 DTO、Controller、Service、Markdown 渲染、Article Entity/Repository、taxonomy/media Repository 和测试。

## 需要其他模块配合的事项

- 总控线程：维护 V2 工作包、接口门禁和上线范围。
- 前端线程：V2-1～V2-3 已完成本地浏览器验收；继续按既定 Session/CSRF 和 401/403/409 契约实施 V2-4。
- 部署线程：提供本地/生产 `/api` 代理、隔离测试/生产 PostgreSQL、Java 21、HTTPS 与 `127.0.0.1:8081` 运行约束；未经授权不得部署。

## 2026-08-11 首页文案配置

- 新增 Flyway V7 `homepage_copy` 单记录表，保存眉题、三段主标题、说明文字、更新时间和乐观锁版本；默认数据与现有首页文案一致。
- `GET /api/v1/home` 新增 `heroCopy`；新增受管理员身份和 CSRF 保护的 `GET/PUT /api/v1/admin/homepage/copy`，写入后记录审计并清理首页缓存，版本冲突返回 409。
- Java 21 `mvn test` 与 `mvn verify` 通过：27 条测试、0 失败、4 条因 Docker/Testcontainers 环境不可用跳过，并生成可执行 JAR。V7 空库迁移集成测试因此未实际运行，未对本机或线上 PostgreSQL 执行迁移。
- Flyway V8 为 `homepage_copy` 增加纸片小标题、三行正文和底部英文，并以现有首页内容作为非空默认值；公开和管理 DTO、校验、审计及缓存失效沿用原配置闭环。
- 2026-08-12：V8 扩展后 Java 21 `mvn test` 通过，27 条测试、0 失败、4 条因 Docker/Testcontainers 不可用跳过；`mvn -Dspring-boot.repackage.skip=true verify` 通过。普通 `mvn verify` 的测试与普通 JAR 阶段通过，但本地运行中的后端进程锁定目标 JAR，Spring Boot 可执行 JAR 重打包失败；未停止现有服务，待服务停止后需重新执行普通 `mvn verify`。V8 尚未在 PostgreSQL 空库实际迁移。
# 2026-08-12 留言与管理收件箱

- 新增 Flyway V9 `guest_message` 表，状态为 `UNREAD / READ / ARCHIVED`，包含乐观锁、状态时间约束和稳定分页索引；数据库不保存来源 IP。
- 新增匿名 `POST /api/v1/messages`，包含长度校验、隐藏蜜罐和单实例 30 分钟 3 条的内存限流。
- 新增管理留言列表、未读计数、标记已读和归档接口；管理写操作要求管理员 Session、CSRF 与 `version`。
- Java 21 下执行 `mvn test` 成功：34 个测试、0 失败、0 错误、4 个依赖本地 PostgreSQL/Testcontainers 的集成测试因 Docker 不可用跳过；新增的 7 个留言接口测试全部通过。
- 本次未运行 Flyway 到真实数据库，也未停止当前后端进程或重新部署。
