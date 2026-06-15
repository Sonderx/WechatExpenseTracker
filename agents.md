# agents.md

## 项目概述
我们要一起开发一个微信小程序 —— **日常记账助手**，实现便捷的个人收支记录与可视化统计，界面和体验参考支付宝的月账单图表。项目采用前后端分离架构，后端使用 Java 生态，前端为微信小程序。

## 技术栈
- **前端**：微信小程序原生框架，使用 ECharts 微信小程序版（或 wx-charts）绘制柱状图、饼图。
- **后端**：Java 17 + Spring Boot 3，提供 RESTful API。
- **ORM**：MyBatis-Plus，简化数据库操作。
- **JWT 认证**：jjwt (io.jsonwebtoken) 库，用于用户认证。
- **工具库**：Hutool（通用工具）、Lombok（简化代码）。
- **数据库**：MySQL 8.0，持久化用户账目数据。
- **用户认证**：微信小程序登录（wx.login + 后端解码获取 openid，无需手机号）。
- **部署**（后续考虑）：Docker Compose / 云服务，当前先本地开发。
- **后端端口**：8080
- **代码规范**：所有代码带详细中文注释

## 功能需求

### 1. 核心记账
- **记录一笔收支**：
  - 金额（必填，正数为收入，负数为支出，或通过"收入/支出"开关选择）。
  - 分类（必选，如餐饮、交通、购物、娱乐、居家、医疗、教育、人情、工资、理财、其他，可自定义分类管理）。
  - 日期和时间（选择器，默认当前时间）。
  - 备注（可选文本）。
- **账目列表**：
  - 按时间倒序展示所有记录。
  - 每条记录显示金额（支出红色，收入绿色）、分类图标/名称、日期、备注摘要。
  - 支持左滑删除或长按编辑。
- **编辑/删除**：点击某一记录可修改金额、分类、日期、备注。

### 2. 分类管理
- 预置一套常用分类，每个分类有名称和图标（可选用 emoji 或本地图标）。
- 用户可增加、修改、删除自定义分类（若某分类已有账目，不允许删除，可隐藏）。
- 分类分为"支出"和"收入"两大类。

### 3. 统计图表（按月查看）
- **月度收支总览**：
  - 顶部显示当月总收入、总支出、结余。
  - 柱状图：近 6 个月或 12 个月每月支出 vs 收入双柱对比（支持月份左右滑动）。
  - 饼状图：当月支出分类占比，点击扇区可查看具体金额和百分比。
- **自定义时间范围统计**（v1.1+）：可自由选择起止日期查看收支趋势。

### 4. 预算与提醒（补充功能，可选 v1.0 实现）
- 设置月度总支出预算。
- 记账时提示预算剩余额度。
- 超过预算时给予醒目提示（如首页顶部条变红）。

### 5. 搜索与导出（后期扩展）
- 按备注/分类/金额范围搜索记录。
- 导出月度账单为图片或 CSV 文件。

### 6. 多账户与同步（未来）
- 支持现金、银行卡、微信/支付宝等账户选择。
- 多设备同步（由于已有后端，天然支持）。

## UI/UX 设计指引
- **主页**：顶部显示本月收支汇总卡片，下方为记录列表，支持下拉刷新、上拉加载更多。底部导航栏：首页、统计、我的。
- **统计页**：月收支柱状图 + 当月支出分类饼图（可切换月份），风格清爽现代，参考支付宝账单页。
- **记账按钮**：可在首页悬浮"+"按钮，点击弹出半屏表单或全屏页面，填写上述字段。
- **分类选择**：以图标网格展示，选中的高亮。
- **颜色规范**：主题色 #1AAD19（微信绿），支出用 #EE5A5A，收入用 #1AAD19，保持一致性。

## 数据库设计（参考）
### 表：`users`
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| openid | varchar(64) | 微信唯一标识 |
| created_at | datetime | |

### 表：`categories`
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 所属用户，0 表示系统预置 |
| name | varchar(20) | 分类名 |
| type | tinyint | 0支出 1收入 |
| icon | varchar(50) | 图标（emoji或代号） |
| is_system | boolean | 是否系统预置 |
| is_hidden | boolean | 用户是否隐藏 |
| sort_order | int | 排序 |

### 表：`records`
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 所属用户 |
| amount | decimal(12,2) | 金额（支出为负） |
| category_id | bigint | 分类 |
| record_time | datetime | 账目发生的日期时间 |
| remark | varchar(200) | 备注 |
| created_at | datetime | 记录创建时间 |

### 表：`budgets`
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 所属用户 |
| year_month | varchar(7) | 月份，格式：2026-06 |
| amount | decimal(12,2) | 预算金额 |
| created_at | datetime | 创建时间 |

## AI 协作流程
1. **初始化项目**：生成 Spring Boot 后端脚手架（Maven），包含依赖：spring-boot-starter-web, mybatis-plus, mysql-connector, hutool, lombok。生成微信小程序前端代码（使用原生框架，搭配 ECharts 组件）。
2. **搭建基础架构**：实现微信登录、用户表操作、统一返回格式、异常处理。
3. **开发接口**：
   - `POST /api/auth/login` 微信登录
   - `POST /api/records` 创建记录
   - `PUT /api/records/{id}` 更新记录
   - `DELETE /api/records/{id}` 删除记录
   - `GET /api/records?month=2026-06&page=1&size=20` 分页查记录
   - `GET /api/statistics/monthly?year=2026` 获取某年每月收支汇总
   - `GET /api/statistics/category?month=2026-06` 当月分类支出统计
   - `GET /api/statistics/overview?month=2026-06` 当月收支概览
   - `GET /api/categories` 获取用户分类列表
   - `POST/PUT/DELETE /api/categories` 管理分类
   - `GET /api/budget?month=2026-06` 获取月度预算
   - `POST /api/budget` 设置月度预算
4. **前端页面开发**：
   - 首页（记录列表 + 新建/编辑入口）
   - 统计页（ECharts 柱状图、饼图，支持月份切换）
   - 我的页（分类管理、预算设置、导出入口）
5. **测试与联调**：使用微信开发者工具配合后端接口联调，保证数据正确。

## 开发规范
- 后端代码放在 `backend/` 目录，使用标准分层（controller, service, mapper）。
- 前端代码放在 `miniprogram/` 目录，遵循小程序官方结构。
- 所有 API 使用 RESTful 风格，返回 JSON：`{ "code": 0, "data": ..., "msg": "success" }`。
- 金额统一用 `decimal`，单位：元。
- 时间使用 ISO 8601 字符串传输（如 "2026-06-14T19:30:00"）。
- 小程序端封装请求方法 `api.js`，统一携带 token（通过微信登录后获取后端 JWT 或自定义 token）。

## 当前任务
### Phase 1: 后端基础搭建 ✅ 已完成
- 创建 Maven 项目 `backend/`，配置 `pom.xml`（Spring Boot 3 + MyBatis-Plus + jjwt + Hutool）
- 编写 `application.yml` / `application-dev.yml` 配置
- 创建启动类、配置类（MyBatisPlusConfig, WebConfig）
- 创建通用类（Result, BusinessException, GlobalExceptionHandler）
- 创建实体类（User, Category, Record, Budget）
- 创建 Mapper 接口（含统计查询 SQL）
- 创建 Service 接口和实现
- 创建 DTO/VO 类
- 创建认证拦截器和 JWT 工具类
- 创建 Controller（Auth, Record, Category, Statistics, Budget）
- 编写数据库初始化脚本 `sql/init.sql`

### Phase 2: 后端核心接口 ✅ 已完成
- 登录接口返回 JWT Token ✅
- 记录 CRUD 接口正常 ✅
- 分类管理接口正常 ✅
- 预算管理接口正常 ✅

### Phase 3: 后端统计接口 ✅ 已完成
- 统计接口返回正确的月度汇总和分类统计数据 ✅

### Phase 4: 小程序基础框架 ✅ 已完成
- 创建小程序项目结构（miniprogram/）
- 配置 app.json（页面路由、tabBar、窗口样式）
- 创建全局样式 app.wxss（主题色、卡片样式、浮动按钮）
- 创建 utils/api.js（统一请求封装 + Token 管理）
- 创建 utils/auth.js（微信登录流程）
- 创建 utils/util.js（日期格式化等工具函数）

### Phase 5: 小程序核心页面 ✅ 已完成
- 首页（pages/index）：月度收支汇总卡片 + 记录列表 + 下拉刷新
- 记账页（pages/record）：收入/支出切换 + 分类网格 + 金额/日期/备注输入
- record-item 组件：列表项（左滑删除）
- category-grid 组件：分类选择网格

### Phase 6: 小程序统计 + 我的 ✅ 已完成
- 统计页（pages/statistics）：Canvas 柱状图 + 饼图 + 月份切换
- 我的页（pages/profile）：分类管理 + 预算设置入口
- 分类管理页（pages/category-manage）：新增/编辑/删除分类
- month-picker 组件：月份选择器