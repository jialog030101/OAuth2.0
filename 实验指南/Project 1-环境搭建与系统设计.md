---
title: Project 1 - 环境搭建与系统设计
date: 2025-01-08
categories:
  - Java
tags:
  - OAuth
---

# 实验目标

本节主要完成以下任务：

1. ✅ 搭建完整的 Java 开发环境
2. ✅ 配置 MySQL 数据库并初始化数据
3. ✅ 配置 Redis 缓存服务
4. ✅ 运行项目并验证环境正确性
5. ✅ 理解 OAuth 2.0 基本概念和系统整体设计

---

# 详细任务

## 一、环境搭建

### 1. 安装 JDK 17

#### 步骤

1. **下载 JDK**
   - 官方版本：[Oracle JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
   - 开源版本：[OpenJDK 17](https://adoptium.net/)（推荐）

2. **安装 JDK**

   **Windows**：
   ```powershell
   # 双击运行 .exe 文件，按提示完成安装
   # 默认安装路径：C:\Program Files\Java\jdk-17
   ```

   **macOS**：
   ```bash
   # 使用 Homebrew 安装（推荐）
   brew install openjdk@17
   
   # 或下载 .dmg 文件手动安装
   ```

   **Linux (Ubuntu/Debian)**：
   ```bash
   sudo apt update
   sudo apt install openjdk-17-jdk
   ```

   **Linux (CentOS/RHEL)**：
   ```bash
   sudo yum install java-17-openjdk-devel
   ```

3. **配置环境变量**

   **Windows**：
   ```powershell
   # 1. 新建系统变量
   变量名：JAVA_HOME
   变量值：C:\Program Files\Java\jdk-17
   
   # 2. 编辑 Path 变量，添加：
   %JAVA_HOME%\bin
   ```

   **macOS/Linux**：
   ```bash
   # 编辑 ~/.bashrc 或 ~/.zshrc
   echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
   echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
   source ~/.bashrc
   
   # macOS (Homebrew) 可能需要：
   echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
   ```

4. **验证安装**
   ```bash
   java -version
   # 输出示例：
   # openjdk version "17.0.12" 2024-07-16
   # OpenJDK Runtime Environment (build 17.0.12+7)
   # OpenJDK 64-Bit Server VM (build 17.0.12+7, mixed mode)
   ```

---

### 2. 安装 Maven 3.8+

#### 步骤

1. **下载 Maven**
   - 访问：[Maven 官网下载页](https://maven.apache.org/download.cgi)
   - 下载 Binary zip 包（如 `apache-maven-3.9.6-bin.zip`）

2. **解压并配置**

   **Windows**：
   ```powershell
   # 解压到：C:\Program Files\apache-maven-3.9.6
   
   # 新建系统变量
   MAVEN_HOME = C:\Program Files\apache-maven-3.9.6
   
   # 编辑 Path，添加：
   %MAVEN_HOME%\bin
   ```

   **macOS/Linux**：
   ```bash
   # 解压
   tar -xzf apache-maven-3.9.6-bin.tar.gz
   sudo mv apache-maven-3.9.6 /opt/maven
   
   # 配置环境变量
   echo 'export MAVEN_HOME=/opt/maven' >> ~/.bashrc
   echo 'export PATH=$MAVEN_HOME/bin:$PATH' >> ~/.bashrc
   source ~/.bashrc
   ```

3. **配置 Maven 镜像（加速依赖下载）**

   编辑 `$MAVEN_HOME/conf/settings.xml`，在 `<mirrors>` 标签中添加：

   ```xml
   <mirror>
     <id>aliyun</id>
     <mirrorOf>central</mirrorOf>
     <name>Aliyun Maven Mirror</name>
     <url>https://maven.aliyun.com/repository/public</url>
   </mirror>
   ```

4. **验证安装**
   ```bash
   mvn -v
   # 输出示例：
   # Apache Maven 3.9.6 (bc0240f3c744dd6b6ec2920b3cd08dcc295161ae)
   # Maven home: /opt/maven
   # Java version: 17.0.12, vendor: Oracle Corporation
   ```

---

### 3. 安装 MySQL 8.0

#### 步骤

1. **下载并安装**

   **Windows**：
   - 访问：[MySQL Community Downloads](https://dev.mysql.com/downloads/mysql/)
   - 下载 MySQL Installer
   - 运行安装向导，选择 "Developer Default"
   - **重要**：设置 root 密码并牢记（建议使用 `root123456` 便于测试）
   - 端口保持默认 `3306`

   **macOS**：
   ```bash
   brew install mysql@8.0
   brew services start mysql@8.0
   
   # 设置 root 密码
   mysql_secure_installation
   ```

   **Linux (Ubuntu/Debian)**：
   ```bash
   sudo apt update
   sudo apt install mysql-server
   sudo systemctl start mysql
   sudo systemctl enable mysql
   
   # 设置 root 密码
   sudo mysql_secure_installation
   ```

2. **创建数据库**

   ```bash
   # 登录 MySQL
   mysql -u root -p
   ```

   ```sql
   -- 创建数据库
   CREATE DATABASE smart_sso DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   
   -- 创建用户（可选，用于应用连接）
   CREATE USER 'sso_user'@'localhost' IDENTIFIED BY 'sso_password';
   GRANT ALL PRIVILEGES ON smart_sso.* TO 'sso_user'@'localhost';
   FLUSH PRIVILEGES;
   
   EXIT;
   ```

3. **初始化数据表**

   在项目根目录找到 `sql/init.sql` 文件，执行：

   ```bash
   mysql -u root -p smart_sso < sql/init.sql
   ```

   该脚本会创建以下表：
   - `user` - 用户表
   - `app` - 客户端应用表
   - 其他必要的配置表

4. **验证**
   ```sql
   USE smart_sso;
   SHOW TABLES;
   
   -- 应显示：user, app 等表
   ```

---

### 4. 安装 Redis

#### 步骤

**Windows**：
```powershell
# 方法 1：使用 WSL
wsl --install
wsl
sudo apt install redis-server
redis-server

# 方法 2：下载 Windows 版本
# 访问：https://github.com/microsoftarchive/redis/releases
# 下载 Redis-x64-*.msi 并安装
# 默认端口：6379
```

**macOS**：
```bash
brew install redis
brew services start redis

# 测试连接
redis-cli ping
# 输出：PONG
```

**Linux**：
```bash
# Ubuntu/Debian
sudo apt install redis-server
sudo systemctl start redis
sudo systemctl enable redis

# CentOS/RHEL
sudo yum install redis
sudo systemctl start redis
sudo systemctl enable redis
```

**配置 Redis（可选）**：

编辑 `/etc/redis/redis.conf`（Linux）或 `/usr/local/etc/redis.conf`（macOS）：

```conf
# 允许远程连接（仅开发环境）
bind 0.0.0.0

# 设置密码（推荐）
requirepass yourpassword
```

重启 Redis：
```bash
sudo systemctl restart redis
```

---

### 5. 配置 IntelliJ IDEA

#### 步骤

1. **下载并安装 IDEA**
   - Community 版（免费）：[下载链接](https://www.jetbrains.com/idea/download/)
   - Ultimate 版（付费，学生可免费）：[学生授权](https://www.jetbrains.com/community/education/)

2. **配置 JDK**
   ```
   File → Project Structure → SDKs → + → Add JDK
   选择 JDK 17 安装目录
   ```

3. **配置 Maven**
   ```
   Settings → Build, Execution, Deployment → Build Tools → Maven
   Maven home path: 选择 Maven 安装目录
   User settings file: 勾选 Override，选择你的 settings.xml
   ```

4. **安装推荐插件**
   ```
   Settings → Plugins → Marketplace
   ```

   推荐安装：
   - ✅ Lombok（必装）
   - ✅ MyBatisX（MyBatis 增强）
   - ✅ Database Navigator（数据库管理）
   - ✅ Redis（Redis 客户端）
   - ✅ Rainbow Brackets（彩虹括号）
   - ✅ GitToolBox（Git 增强）

5. **配置数据库连接**
   ```
   View → Tool Windows → Database
   + → Data Source → MySQL
   
   Host: localhost
   Port: 3306
   Database: smart_sso
   User: root
   Password: 你的密码
   
   点击 "Test Connection" 验证
   ```

6. **配置 Redis 连接（需安装 Redis 插件）**
   ```
   Tools → Redis → Configure
   
   Host: localhost
   Port: 6379
   Password: (如果有)
   ```

---

### 6. 获取项目代码并验证环境

#### 步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/jialog030101/OAuth2.0.git
   cd OAuth2.0
   git checkout project1
   ```

2. **使用 IDEA 打开项目**
   ```
   File → Open → 选择项目目录
   等待 Maven 下载依赖（首次较慢，请耐心等待）
   ```

3. **配置 application.yml**

   编辑 `smart-sso-server/src/main/resources/application.yml`：

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/smart_sso?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
       username: root
       password: 你的MySQL密码
       driver-class-name: com.mysql.cj.jdbc.Driver
     
     redis:
       host: localhost
       port: 6379
       password: # 如果有密码则填写
   ```

4. **运行项目**

   找到主类 `SmartSsoServerApplication.java`，右键 → Run

   控制台输出类似：
   ```
   Started SmartSsoServerApplication in 5.234 seconds
   ```

5. **测试端点**

   在浏览器访问：

   - **测试 1**：http://localhost:8080/api/hello
     ```
     预期输出：Hello, Smart SSO Server is running!
     ```

   - **测试 2**：http://localhost:8080/api/env
     ```json
     {
       "mysql": "connected",
       "redis": "connected",
       "timestamp": "2025-01-08T10:30:00"
     }
     ```

   如果两个测试都通过，恭喜你，环境搭建成功！

---

## 二、OAuth 2.0 概念学习

在开始编码前，请务必理解以下核心概念：

### 1. OAuth 2.0 基本概念

**核心角色**：
- **Resource Owner（资源所有者）**：用户
- **Client（客户端）**：第三方应用
- **Authorization Server（授权服务器）**：本项目要实现的 SSO 服务器
- **Resource Server（资源服务器）**：受保护的 API 服务器

**核心令牌**：
- **Authorization Code（授权码）**：临时凭证，生命周期短（如 5 分钟）
- **Access Token（访问令牌）**：访问 API 的凭证，生命周期短（如 15 分钟）
- **Refresh Token（刷新令牌）**：刷新访问令牌的凭证，生命周期长（如 30 天）

### 2. 授权码模式流程图

```
+----------+                                      +---------------+
|          |---(A) 授权请求 ---------------------->|               |
|          |      (client_id, redirect_uri,       |  Authorization|
|  User    |       code_challenge)                |     Server    |
|  Agent   |                                      |   (SSO Server)|
|          |<--(B) 授权码 -------------------------|               |
|          |      (code)                          |               |
+----------+                                      +---------------+
     |                                                     ^
     |                                                     |
     v                                                     |
+----------+                                               |
|          |--(C) 授权码 + 验证器 ---------------------->|
|  Client  |      (code, code_verifier,
|   App    |       client_id, client_secret)              |
|          |                                               |
|          |<--(D) 访问令牌 + 刷新令牌 ------------------|
+----------+      (access_token, refresh_token)
```

### 3. 必读材料

请按顺序阅读以下材料（约需 2-3 小时）：

1. ✅ [理解OAuth 2.0 - 阮一峰](https://www.ruanyifeng.com/blog/2014/05/oauth_2_0.html)（入门）
2. ✅ [OAuth 2.0 详解 - 廖雪峰](https://www.liaoxuefeng.com/wiki/1252599548343744/1266612291584864)（进阶）
3. ✅ [RFC 6749 - 第4.1节](https://datatracker.ietf.org/doc/html/rfc6749#section-4.1)（授权码模式标准）
4. ✅ [PKCE 详解](https://oauth.net/2/pkce/)（安全增强）

### 4. 系统整体设计

本项目的模块划分：

```
smart-sso-starter-server/
├── controller/          # 控制器层
│   ├── AuthorizeController.java    # 授权端点
│   ├── TokenController.java        # 令牌端点
│   └── LogoutController.java       # 登出端点
├── service/             # 服务层
│   ├── UserService.java            # 用户验证
│   ├── AppService.java             # 客户端验证
│   ├── TicketGrantingTicketManager # TGT 管理
│   ├── CodeManager                 # 授权码管理
│   └── TokenManager                # 令牌管理
├── entity/              # 实体类
│   ├── User.java
│   ├── App.java
│   └── TokenContent.java
└── config/              # 配置类
    └── SecurityConfig.java
```

**关键流程**：
1. 用户在客户端应用点击"登录"
2. 客户端重定向用户到 `/oauth/authorize`
3. 用户输入账号密码，SSO 服务器验证
4. 验证成功后颁发授权码，重定向回客户端
5. 客户端后端拿授权码到 `/oauth/token` 换取令牌
6. 客户端使用访问令牌调用 API

---

## 三、预习下周内容

下周我们将实现**用户和客户端的验证逻辑**，请提前了解：

- BCrypt 密码加密算法
- Spring Security 的 PasswordEncoder
- 缓存在账户锁定中的应用

---

## 常见问题 (FAQ)

### Q1：Maven 下载依赖很慢怎么办？
A1：配置阿里云镜像（见上文 Maven 配置）。如果已配置仍然慢，可能是网络问题，建议使用代理或切换网络。

### Q2：MySQL 连接报错 "Public Key Retrieval is not allowed"
A2：在 JDBC URL 中添加 `allowPublicKeyRetrieval=true`：
```
jdbc:mysql://localhost:3306/smart_sso?allowPublicKeyRetrieval=true&useSSL=false
```

### Q3：Redis 连接失败
A3：检查 Redis 是否启动：
```bash
redis-cli ping
# 应返回 PONG
```

如果返回错误，重启 Redis：
```bash
# macOS/Linux
sudo systemctl restart redis

# Windows
net stop Redis
net start Redis
```

### Q4：IDEA 无法识别 Lombok 注解
A4：
1. 安装 Lombok 插件：Settings → Plugins → Lombok
2. 启用注解处理：Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing

### Q5：项目启动后访问 localhost:8080 显示 404
A5：检查：
1. 控制台是否有错误日志
2. application.yml 配置是否正确
3. 数据库和 Redis 连接是否正常

---

## 总结

完成本实验后，你应该：

- ✅ 拥有完整的 Java 开发环境
- ✅ 理解 OAuth 2.0 的核心概念
- ✅ 了解本项目的整体架构
- ✅ 能够成功运行项目并通过环境测试

如有任何问题，请在 GitHub Issues 中提问，并附上详细的错误日志和环境信息。

下周见！👋
