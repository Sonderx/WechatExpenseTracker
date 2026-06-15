# 日常记账助手

一款基于微信小程序的个人记账应用，支持收支记录、分类管理、统计图表和预算设置。

## 功能特性

- **记账**：支持收入/支出记录，分类选择，日期时间，备注
- **统计**：月度收支柱状图，分类支出饼图，支持月份切换
- **分类管理**：系统预置 + 用户自定义分类
- **预算**：月度预算设置，超支提醒
- **用户信息**：微信头像、昵称展示

## 技术栈

| 模块 | 技术 |
|------|------|
| 前端 | 微信小程序原生框架 |
| 后端 | Java 17 + Spring Boot 3.2.5 |
| ORM | MyBatis-Plus 3.5.6 |
| 数据库 | MySQL 8.0 |
| 认证 | JWT (jjwt 0.12.5) |
| 工具 | Hutool 5.8.27 + Lombok |

## 项目结构

```
ExpenseTracker/
├── backend/               # 后端 Spring Boot 项目
│   ├── pom.xml
│   ├── sql/               # 数据库初始化脚本
│   └── src/main/java/     # Java 源码
├── miniprogram/           # 微信小程序前端
│   ├── pages/             # 页面
│   ├── components/        # 组件
│   └── utils/             # 工具模块
└── README.md              # 本文件
```

## 部署指南

### 环境要求

- JDK 17
- Maven 3.6+
- MySQL 8.0
- 微信开发者工具
- IntelliJ IDEA（推荐）

### 1. 克隆项目

```bash
git clone https://github.com/your-username/ExpenseTracker.git
cd ExpenseTracker
```

### 2. 配置数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE expense_tracker DEFAULT CHARSET utf8mb4;

# 导入初始化脚本
mysql -u root -p expense_tracker < backend/sql/init.sql
```

### 3. 配置后端

```bash
# 复制配置模板
cd backend/src/main/resources
cp application-dev.yml.example application-dev.yml

# 编辑 application-dev.yml，填入真实值：
# - spring.datasource.username: MySQL 用户名
# - spring.datasource.password: MySQL 密码
# - wx.mini.app-id: 微信小程序 AppID
# - wx.mini.app-secret: 微信小程序 AppSecret
# - jwt.secret: JWT 签名密钥（随意生成一个长字符串）
```

### 4. 启动后端

```bash
cd backend
mvn package -DskipTests
java -jar target/expense-tracker-1.0.0.jar
```

或在 IDEA 中打开 `backend/` 目录，运行 `ExpenseApplication.java`。

后端默认运行在 `http://localhost:8080`。

### 5. 配置小程序

```bash
cd miniprogram

# 复制配置模板
cp config.example.js config.js

# 编辑 config.js，设置后端地址：
# - 模拟器：http://localhost:8080
# - 真机调试：http://你电脑的局域网IP:8080
```

然后在微信开发者工具中打开 `miniprogram/` 目录，修改 `project.config.json` 中的 `appid` 为你的小程序 AppID：

```json
{
  "appid": "你的小程序AppID"
}
```

### 6. 运行小程序

1. 打开微信开发者工具
2. 导入 `miniprogram/` 目录
3. AppID 填入你的小程序 AppID
4. 勾选「不校验合法域名」（开发阶段）
5. 编译运行

### 7. 真机调试

1. 手机和电脑连接同一 Wi-Fi
2. `miniprogram/config.js` 中 `baseUrl` 改为电脑局域网 IP
3. 微信开发者工具 → 真机调试

## 配置说明

### 后端配置 (`application-dev.yml`)

| 配置项 | 说明 |
|--------|------|
| `spring.datasource.username` | MySQL 用户名 |
| `spring.datasource.password` | MySQL 密码 |
| `wx.mini.app-id` | 微信小程序 AppID |
| `wx.mini.app-secret` | 微信小程序 AppSecret |
| `wx.mini.mock-openid` | 是否 mock 登录（开发调试用） |
| `jwt.secret` | JWT 签名密钥 |

### 前端配置 (`miniprogram/config.js`)

| 配置项 | 说明 |
|--------|------|
| `baseUrl` | 后端 API 地址 |

## 注意事项

- `application-dev.yml` 和 `config.js` 包含敏感信息，已在 `.gitignore` 中忽略，不会提交到仓库
- 首次使用需要在微信公众平台注册小程序获取 AppID 和 AppSecret
- 开发阶段建议开启「不校验合法域名」，上线前需配置服务器域名

## License

MIT
