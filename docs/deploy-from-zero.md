# 商城项目：从 0 到上线完整指南

> 面向零基础：买服务器 → **先用公网 IP 测通** →（过几天）域名 → 备案 → HTTPS → 微信/支付宝真登录。  
> 建议**打印或边看边勾选**，全程以**一家云厂商**为准（阿里云 **或** 腾讯云，不要混用）。  
> **当前推荐部署**：腾讯云轻量 **Docker 镜像** + 本仓库 [`deploy/`](../deploy/)（详见 [阶段 3A](#6a-阶段-3a公网-ip-测试部署docker无需域名)）。

---

## 目录

1. [你要达成什么](#1-你要达成什么)
2. [整体路线图与时间](#2-整体路线图与时间)
3. [阶段 0：本地先跑通（你现在的电脑）](#3-阶段-0本地先跑通你现在的电脑)
4. [阶段 1：注册云账号并购买服务器](#4-阶段-1注册云账号并购买服务器)
5. [阶段 2：购买域名并完成实名](#5-阶段-2购买域名并完成实名)（可延后）
6. [阶段 3：ICP 备案（大陆服务器必做）](#6-阶段-3icp-备案大陆服务器必做)（可延后）
6A. [**阶段 3A：公网 IP 测试部署（Docker）**](#6a-阶段-3a公网-ip-测试部署docker无需域名) ← **推荐先做**
7. [阶段 4：初始化服务器](#7-阶段-4初始化服务器)
8. [阶段 5：安装 MySQL / Redis / JDK](#8-阶段-5安装-mysql--redis--jdk)（Docker 路线可跳过）
9. [阶段 6：导入数据库](#9-阶段-6导入数据库)（Docker 由 compose 自动导入）
10. [阶段 7：打包并部署后端](#10-阶段-7打包并部署后端)
11. [阶段 8：打包并部署前端](#11-阶段-8打包并部署前端)
12. [阶段 9：Nginx + HTTPS](#12-阶段-9nginx--https)（备案通过后）
13. [阶段 10：上线验证](#13-阶段-10上线验证)
14. [阶段 11：微信真登录](#14-阶段-11微信真登录)
15. [阶段 12：支付宝真登录（可选）](#15-阶段-12支付宝真登录可选)
16. [常见问题](#16-常见问题)
17. [附录：配置示例](#17-附录配置示例)

---

## 1. 你要达成什么

| 阶段 | 结果 |
|------|------|
| 本地 | 电脑访问 `http://localhost/shop/home` 能购物 |
| IP 测试 | 公网 `http://82.x.x.x/shop/home` 能访问（阶段 3A，无需域名） |
| 上线 | 公网 `https://shop.你的域名.com` 能访问商城 |
| 备案 | 获得 ICP 备案号，网站合法运行于大陆服务器 |
| 真登录 | 登录页微信扫码能真正登录（非模拟） |

**说明：**

- **没有大陆服务器 + 备案域名**，微信/支付宝**无法**做正式 OAuth 回调（`localhost` 不行）。
- 当前项目默认 `social.mock-enabled: true` 为**模拟登录**，上线后要改配置并申请开放平台应用。

---

## 2. 整体路线图与时间

```
第 1 天     本地跑通 + 买腾讯云轻量（Docker 镜像）+ 防火墙 22/80/443 + SSH 登录
第 1～2 天  阶段 3A：Docker 部署，浏览器用 http://公网IP/ 访问商城
第 2 天起   （可延后）买域名 + 实名 + 提交备案
第 3～20 天 等备案；期间继续用 IP 测功能
备案通过    域名解析 → HTTPS → 改 social 配置 → 微信真登录
```

**公网 IP 与域名：**

| 方式 | 地址栏示例 | 用途 |
|------|------------|------|
| 公网 IP | `http://82.156.68.87/` | 部署验证、自测（**你现在**） |
| 域名 + HTTPS | `https://shop.example.com` | 备案上线、微信正式登录 |

`192.168.x.x` 是家里局域网，不是云服务器公网 IP。

**费用粗算（人民币/年）：**

| 项目 | 大约 |
|------|------|
| 轻量服务器 2核2G～2核4G | 50～300 |
| 域名 `.cn` / `.com` | 10～80（首年） |
| 备案 | 0 |
| SSL 证书 | 0（云厂商免费） |

---

## 3. 阶段 0：本地先跑通（你现在的电脑）

在买服务器之前，确认项目在本机正常。

### 3.1 环境

- JDK **17**
- Maven **3.6+**
- MySQL **5.7 / 8.0**
- Redis（若依登录/缓存需要）
- Node.js **18+**

### 3.2 数据库

在项目根目录 `mall` 下，用 MySQL 客户端执行：

```sql
source D:/Users/lemon/mall/sql/ry_mall_complete.sql;
source D:/Users/lemon/mall/sql/mall_social.sql;
```

（路径按你实际存放位置修改。）

### 3.3 改数据库密码

编辑 `ruoyi-admin/src/main/resources/application-druid.yml`，修改 `username` / `password`。

> **安全提醒：** 不要把真实密码提交到 Git。

### 3.4 启动

```bash
# 后端
cd ruoyi-admin
mvn spring-boot:run -DskipTests

# 前端（新开终端）
cd ruoyi-ui
npm install
npm run dev
```

### 3.5 验证

| 地址 | 账号 | 用途 |
|------|------|------|
| http://localhost | admin / admin123 | 管理后台 |
| http://localhost/shop/home | ry / admin123 | C 端购物 |
| 登录页 | 微信/支付宝按钮 | 本地为**模拟登录** |

本地 OK 再进入买服务器环节。

---

## 4. 阶段 1：注册云账号并购买服务器

### 4.1 选一家（二选一）

| 厂商 | 新手入口 |
|------|----------|
| 阿里云 | https://www.aliyun.com/product/swas （轻量应用服务器） |
| 腾讯云 | https://cloud.tencent.com/product/lighthouse （轻量应用服务器） |

下文用 **「控制台」** 统称，两家界面类似。

### 4.2 购买参数（照抄即可）

| 项 | 选择 |
|----|------|
| 产品 | **轻量应用服务器 Lighthouse** |
| 地域 | **中国内地**（如 **北京**，要备案 + 微信正式环境） |
| 规格 | **2 核 4G**（约 65 元/月新客）；勿用 2 核 2G 跑 Docker |
| 镜像 | **Docker CE**（本文命令按此编写） |
| 带宽 | 4～5 Mbps |
| 时长 | **1 年** 或 ≥3 个月（备案以控制台为准） |
| 登录 | SSH 密钥 或 自动密码 |
| 不要选 | 香港/海外、AI 应用模板 |

### 4.3 记录这些信息（后面要用）

- [ ] 公网 IP：例如 `82.156.68.87`
- [ ] 登录用户：多为 `ubuntu`
- [ ] **SSH 私钥路径**（密钥登录时）：例如 `C:\Users\lemon\Downloads\ruoyimall.pem`（购机时下载，勿丢失、勿提交 Git）
- [ ] 若用密码登录：控制台 **重置密码** 后记下（密钥登录可跳过）
- [ ] **防火墙**：**服务器 → 点实例 →「防火墙」**（不是左侧「防火墙模板」），放行 **22、80、443**
- [ ] 勿对公网开放：3306、6379、8080

### 4.4 购机后自检（SSH 密钥登录示例）

**PowerShell 本机**（`-i` 后换成你的 `.pem` 路径）：

```powershell
ssh -i C:\Users\lemon\Downloads\ruoyimall.pem ubuntu@82.156.68.87
```

首次连接提示 `Are you sure you want to continue connecting` 时输入 **`yes`** 回车。  
**密钥登录不会出现 SSH 登录密码**；若 `scp`/`ssh` 仍索要密码，说明未加 `-i` 或私钥路径错误。

登录服务器后执行：

```bash
docker --version
docker compose version
free -h    # 2核4G 空载约 3.1Gi available
df -h
```

可选：在 `C:\Users\lemon\.ssh\config` 中配置别名，免每次写 `-i`（见阶段 3A.3）。

---

## 5. 阶段 2：购买域名并完成实名

### 5.1 购买

在同一云厂商控制台 → **域名注册** → 搜索可用域名 → 购买 1 年。

命名建议：短、好记，如 `lemonshop.cn`。

### 5.2 域名实名认证（必须）

控制台 → 域名列表 → **实名认证**：

- 个人：身份证
- 企业：营业执照

**实名主体必须与后续备案主体一致**（同一身份证或同一公司）。

### 5.3 规划两个子域名（推荐）

| 用途 | 示例 | 说明 |
|------|------|------|
| 前端（用户访问） | `shop.example.com` | 商城 + 管理后台页面 |
| 后端 API（可选独立） | `api.example.com` | 也可与前端同域，用 `/prod-api` 反代 |

新手可简化为：**只用一个域名** `www.example.com`，前后端都走 Nginx 反代（见阶段 9）。

---

## 6. 阶段 3：ICP 备案（大陆服务器必做）

### 6.1 为什么

大陆服务器上的网站，按法规需 **ICP 备案**，否则不能合法对外提供 Web 服务。

### 6.2 在哪里办

**在购买服务器的同一家云** → 控制台 → **备案 / ICP 备案** → **开始备案**。

- 阿里云帮助：https://help.aliyun.com/zh/icp-filing/
- 腾讯云帮助：https://cloud.tencent.com/document/product/243

### 6.3 操作步骤清单

- [ ] 1. 选择 **首次备案**
- [ ] 2. 填写 **备案主体**（个人或企业）
- [ ] 3. 填写 **网站信息**（网站名称、域名）
- [ ] 4. 关联你的 **服务器**（系统会生成/绑定备案服务码）
- [ ] 5. 上传证件、按要求 **人脸识别 / 幕布照**
- [ ] 6. 提交 → 云厂商 **初审**（约 1～2 工作日）
- [ ] 7. 手机收到 **工信部短信核验**，24 小时内完成
- [ ] 8. **管局审核**（约 7～20 工作日）→ 下发备案号

### 6.4 填写时注意

| 类型 | 注意 |
|------|------|
| **个人备案** | 一般要求非经营性；网站名称避免「公司」「商城有限公司」等；各地管局要求略有差异 |
| **正式卖货商城** | 长期经营建议 **个体工商户或公司** 主体备案，便于微信/支付宝企业应用 |
| **备案期间** | 按平台提示，通过前可能不宜将域名解析到对外网站 |

### 6.5 备案通过后

- [ ] 获得备案号，如：`京ICP备xxxxxxxx号`
- [ ] 网站首页底部展示备案号并链接 https://beian.miit.gov.cn/
- [ ] 30 日内完成 **公安联网备案**（控制台一般有入口）

**备案通过之前**，可用 **公网 IP** 做功能测试（见阶段 3A），但长期正式经营仍需域名 + 备案 + HTTPS。

> **阶段 2、3（域名、备案）可过几天再做**，不影响先用 IP 部署。

---

## 6A. 阶段 3A：公网 IP 测试部署（Docker，无需域名）

本阶段目标：浏览器打开 `http://你的公网IP/` 能登录后台并访问 C 端商城。  
配套文件：仓库 [`deploy/`](../deploy/)（`docker-compose.yml`、Nginx 配置、配置示例）。

### 6A.1 勾选清单

- [ ] 服务器 **运行中**，防火墙已放行 80/443/22
- [ ] SSH 能登录，`docker` / `docker compose` 有版本号
- [ ] 本机已能跑通阶段 0（`mvn package`、`npm run build:prod` 成功）

### 6A.2 在你电脑上打包

**PowerShell（路径按你本机修改）：**

```powershell
cd D:\Users\lemon\mall
mvn clean package -DskipTests -pl ruoyi-admin -am

cd ruoyi-ui
npm install
npm run build:prod
```

产物：

| 文件/目录 | 路径 |
|-----------|------|
| 后端 jar | `ruoyi-admin\target\ruoyi-admin.jar` |
| 前端静态 | `ruoyi-ui\dist\` 内全部文件 |

### 6A.3 上传到服务器

把下面命令里的 **`PUBLIC_IP`** 换成公网 IP，**`PEM`** 换成你的私钥路径（示例 `C:\Users\lemon\Downloads\ruoyimall.pem`）。

**上传顺序（建议分三步）：**

| 步骤 | 内容 | 目标目录 |
|------|------|----------|
| 1 | `deploy/` + `sql/` | `~/mall/` |
| 2 | `ruoyi-admin.jar` | `~/mall/deploy/app/` |
| 3 | `ruoyi-ui/dist/*` | `~/mall/deploy/html/` |

**SSH 密钥登录（推荐，每条命令都要带 `-i`）：**

```powershell
# ① deploy + sql（完成后可传 jar）
scp -i PEM -r D:\Users\lemon\mall\deploy D:\Users\lemon\mall\sql ubuntu@PUBLIC_IP:~/mall/

# ② 后端 jar（需先 mvn package 成功）
scp -i PEM D:\Users\lemon\mall\ruoyi-admin\target\ruoyi-admin.jar ubuntu@PUBLIC_IP:~/mall/deploy/app/

# ③ 前端静态（需先 npm run build:prod）
scp -i PEM -r D:\Users\lemon\mall\ruoyi-ui\dist\* ubuntu@PUBLIC_IP:~/mall/deploy/html/
```

**密码登录**（控制台重置过 `ubuntu` 密码时）：去掉 `-i PEM`，执行时输入密码（输入不显示字符，正常）。

**可选：SSH 配置别名**（`C:\Users\lemon\.ssh\config`）：

```text
Host lemon-mall
    HostName 82.156.68.87
    User ubuntu
    IdentityFile C:\Users\lemon\Downloads\ruoyimall.pem
```

之后可写：`scp -r D:\Users\lemon\mall\deploy lemon-mall:~/mall/deploy`（路径按需在服务器上保持 `~/mall/deploy`）。

也可用 WinSCP（协议 SFTP，选私钥文件）、OrcaTerm 上传，目录与上表一致即可。

### 6A.4 在服务器上配置并启动

**SSH 登录**（密钥）：

```powershell
ssh -i C:\Users\lemon\Downloads\ruoyimall.pem ubuntu@PUBLIC_IP
```

登录后执行（**把 MySQL 密码和公网 IP 改成你自己的**）：

```bash
cd ~/mall/deploy

cp .env.example .env
cp config/application-druid.yml.example config/application-druid.yml
cp config/application.yml.example config/application.yml

# 编辑 .env：MYSQL_ROOT_PASSWORD、PUBLIC_HOST=你的公网IP
nano .env
# 编辑 application-druid.yml：password 与 .env 中 MYSQL_ROOT_PASSWORD 一致
nano config/application-druid.yml
# 编辑 application.yml：social.frontend-base 与 redirect-uri 里的 IP
nano config/application.yml
```

`application.yml` 在 **IP 测试阶段** 建议保持：

```yaml
social:
  mock-enabled: true          # 模拟微信/支付宝登录
  frontend-base: http://你的公网IP
```

启动（首次拉镜像、导入 SQL 约 2～5 分钟）：

```bash
cd ~/mall/deploy
docker compose up -d
docker compose ps
docker compose logs -f mall-api
```

看到 Spring Boot 启动完成、无数据库连接错误即可 `Ctrl+C` 退出日志。

### 6A.5 浏览器验证

| 地址 | 账号 | 说明 |
|------|------|------|
| `http://公网IP/` | admin / admin123 | 管理后台 |
| `http://公网IP/shop/home` | ry / admin123 | C 端商城 |
| 登录页微信/支付宝 | — | IP 阶段为 **模拟登录** |

服务器上自检：

```bash
curl -s http://127.0.0.1/prod-api/captchaImage | head -c 120
docker compose exec mysql mysql -uroot -p"你的密码" -e "USE \`ry-vue\`; SHOW TABLES LIKE 'mall_%';"
```

### 6A.6 常用运维命令

```bash
cd ~/mall/deploy
docker compose restart mall-api    # 改配置后重启后端
docker compose logs -f mall-api    # 看后端日志
docker compose down                # 停止全部（数据在 volume 里保留）
docker compose up -d --build       # 再次启动
```

更新版本：本机重新 `mvn package` / `npm run build:prod`，再 `scp -i PEM ...` 覆盖 `app/ruoyi-admin.jar` 与 `html/`，然后 `docker compose restart mall-api nginx`。

### 6A.7 排错

| 现象 | 处理 |
|------|------|
| 首次连接问 `continue connecting` | 输入 **`yes`**，不是错误 |
| `scp`/`ssh` 仍要密码 | 购机为密钥登录时须加 **`-i 私钥.pem`**；或私钥丢失 → 控制台重置密码 |
| `Permission denied (publickey)` | 检查用户名是否为 `ubuntu`、公钥是否绑在本实例 |
| 浏览器打不开 IP | 查防火墙是否放行 80；`docker compose ps` 中 nginx 是否 Up |
| 502 / 接口失败 | `docker compose logs mall-api`；检查 MySQL 密码是否与 yml 一致 |
| 页面空白 | 确认 `deploy/html/` 内有 `index.html`（dist 内容是否上传完整） |
| 数据库未初始化 | 仅**首次**启动会执行 `sql/*.sql`；若 volume 已有脏数据：`docker compose down -v` 后重来（会清空库） |

更短命令摘要见 [`deploy/README.md`](../deploy/README.md)。

### 6A.8 用 Git 更新（可选，推荐长期使用）

可以：**本机 push 代码 → 服务器 `git pull` → 打包 → 重启容器**，不必每次 `scp` 传文件。

- **提交 Git**：源码、`deploy/docker-compose.yml`、`deploy/*.example`
- **不要提交**：`deploy/.env`、`deploy/config/application*.yml`（含密码）、`deploy/app/*.jar`、`deploy/html/*`（见 `deploy/.gitignore`）
- 服务器首次：`git clone` 后同样 `cp .env.example` 并在服务器上改密码
- 服务器需 **JDK17 + Maven + Node**，或在本机打包后只同步 jar/dist

详细脚本见 [`deploy/README.md`](../deploy/README.md) 末尾 **「用 Git 在服务器上更新」**。

---

## 7. 阶段 4：初始化服务器

> **若已完成阶段 3A（Docker compose），本章 7～9 可跳过。** 以下为不用 Docker、改用手动/宝塔时的备选。

### 7.1 登录服务器

Windows 可用：

- **PowerShell（SSH 密钥）**：`ssh -i C:\Users\lemon\Downloads\ruoyimall.pem ubuntu@你的公网IP`
- **PowerShell（密码）**：`ssh ubuntu@你的公网IP`
- 或控制台 **OrcaTerm / 登录**（网页终端，不依赖本机私钥）

确认能访问外网：`ping -c 2 baidu.com`

### 7.2 备选：安装宝塔面板（不用 Docker 时）

以 Ubuntu 为例（以宝塔官网最新安装命令为准）：

```bash
# 见 https://www.bt.cn/new/download.html 获取最新一键安装命令
wget -O install.sh https://download.bt.cn/install/install_panel.sh && sudo bash install.sh ed8484bec
```

安装后记下面板地址、用户名、密码。

在宝塔 **软件商店** 安装：

- [ ] Nginx
- [ ] MySQL 8.0
- [ ] Redis
- [ ] Java 项目管理器 或 手动安装 JDK 17

### 7.3 不用宝塔时（简要）

```bash
# Ubuntu
apt update && apt upgrade -y
apt install -y nginx mysql-server redis-server openjdk-17-jdk
```

---

## 8. 阶段 5：安装 MySQL / Redis / JDK

### 8.1 MySQL

在宝塔创建数据库：

| 项 | 建议值 |
|----|--------|
| 数据库名 | `ry-vue` |
| 用户名 | `ryvue`（或自定义） |
| 密码 | 强密码，记下来 |
| 访问 | 仅本机 `127.0.0.1` |

### 8.2 Redis

默认本机 `6379`，若设有密码，记下并在后端配置中填写。

### 8.3 JDK 17

```bash
java -version
# 应显示 17.x
```

---

## 9. 阶段 6：导入数据库

### 9.1 上传 SQL 文件

将本仓库 `sql/` 目录上传到服务器，或在本机用工具（Navicat、宝塔数据库导入）执行：

1. **必须先执行：** `ry_mall_complete.sql`（含若依基础 + 商城表 + 菜单等）
2. **若要做第三方登录：** `mall_social.sql`（表 `sys_social_bind`）

### 9.2 命令行示例

```bash
mysql -u ryvue -p ry-vue < /path/to/ry_mall_complete.sql
mysql -u ryvue -p ry-vue < /path/to/mall_social.sql
```

### 9.3 验证

```sql
USE `ry-vue`;
SHOW TABLES LIKE 'mall_%';
SHOW TABLES LIKE 'sys_social_bind';
```

应能看到 `mall_product`、`mall_order` 等表。

---

## 10. 阶段 7：打包并部署后端

### 10.1 在你开发电脑上打包

```bash
cd D:\Users\lemon\mall
mvn clean package -DskipTests -pl ruoyi-admin -am
```

成功后得到：

`ruoyi-admin/target/ruoyi-admin.jar`

### 10.2 生产配置文件

在服务器上准备 `application-prod.yml` 或通过环境变量覆盖，**至少**修改：

**`application-druid.yml`（数据库）：**

```yaml
spring:
  datasource:
    druid:
      master:
        url: jdbc:mysql://127.0.0.1:3306/ry-vue?useUnicode=true&characterEncoding=utf8&...
        username: ryvue
        password: 你的强密码
```

**`application.yml` 片段：**

```yaml
ruoyi:
  profile: /home/ruoyi/uploadPath   # Linux 路径，需提前 mkdir

server:
  port: 8080

spring:
  redis:
    host: 127.0.0.1
    port: 6379
    password:  # 若 Redis 有密码则填写

social:
  enabled: true
  mock-enabled: false              # 上线真登录时改为 false
  frontend-base: https://shop.你的域名.com
  wechat:
    enabled: true
    app-id: 上线后填写
    app-secret: 上线后填写
    redirect-uri: https://api.你的域名.com/social/callback/wechat
  alipay:
    enabled: true
    app-id: 上线后填写
    redirect-uri: https://api.你的域名.com/social/callback/alipay
```

可将 `application-druid.yml`、`application.yml` 放在 jar 同级目录的 `config/` 下，Spring Boot 会优先加载（便于不改 jar 调配置）。

### 10.3 上传并启动

```bash
mkdir -p /home/ruoyi/uploadPath
mkdir -p /home/ruoyi/app/config

# 上传 ruoyi-admin.jar 到 /home/ruoyi/app/

cd /home/ruoyi/app
nohup java -jar -Dfile.encoding=UTF-8 ruoyi-admin.jar --spring.profiles.active=prod > logs.out 2>&1 &
```

或用宝塔 **Java 项目** 添加 jar，端口 **8080**。

### 10.4 验证后端

```bash
curl http://127.0.0.1:8080/captchaImage
```

有 JSON 返回即表示后端已启动。

---

## 11. 阶段 8：打包并部署前端

### 11.1 在本机打包

确认 `ruoyi-ui/.env.production`：

```properties
VITE_APP_BASE_API = '/prod-api'
```

打包：

```bash
cd ruoyi-ui
npm install
npm run build:prod
```

生成目录：`ruoyi-ui/dist/`

### 11.2 上传到服务器

将 `dist` 内所有文件上传到例如：

`/www/wwwroot/shop.example.com/`

（路径与你在 Nginx 里配置的 root 一致。）

---

## 12. 阶段 9：Nginx + HTTPS

### 12.1 域名解析（备案通过后）

在域名控制台添加 **A 记录**：

| 主机记录 | 记录值 |
|----------|--------|
| `shop`（即 shop.example.com） | 服务器公网 IP |
| `api`（若拆分 API 域名） | 同上 |

### 12.2 Nginx 配置示例（单域名版）

站点 `shop.example.com`，同时托管前端 + 反代后端：

```nginx
server {
    listen 80;
    server_name shop.example.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name shop.example.com;

    # 证书路径由宝塔「SSL」或 certbot 自动生成
    ssl_certificate     /path/to/fullchain.pem;
    ssl_certificate_key /path/to/privkey.pem;

    root /www/wwwroot/shop.example.com;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /prod-api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 微信/支付宝 OAuth 回调走后端（也可只走 prod-api 反代，需与 redirect-uri 一致）
    location /social/ {
        proxy_pass http://127.0.0.1:8080/social/;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

若 `redirect-uri` 使用 `https://shop.example.com/social/callback/wechat`，则无需单独 `api` 子域名。

### 12.3 申请免费 SSL

宝塔：站点 → SSL → Let's Encrypt → 申请并开启 **强制 HTTPS**。

### 12.4 修改后端 social 地址

```yaml
social:
  frontend-base: https://shop.example.com
  wechat:
    redirect-uri: https://shop.example.com/social/callback/wechat
  alipay:
    redirect-uri: https://shop.example.com/social/callback/alipay
```

改完后重启 Java 进程。

---

## 13. 阶段 10：上线验证

按顺序勾选：

- [ ] `https://shop.example.com` 能打开登录页
- [ ] `admin` / `admin123` 能进管理后台，有「商城管理」菜单
- [ ] `https://shop.example.com/shop/home` C 端能浏览商品
- [ ] 用 `ry` / `admin123` C 端能加购、下单
- [ ] 浏览器 F12 网络请求无大量 502（若有，检查 Nginx `/prod-api` 反代）
- [ ] 首页底部已加 **ICP 备案号**

---

## 14. 阶段 11：微信真登录

### 14.1 前提

- [ ] 网站已 **HTTPS** 公网可访问
- [ ] 已执行 `mall_social.sql`
- [ ] `social.mock-enabled: false`
- [ ] 建议有 **企业/个体** 主体（微信开放平台网站应用审核常需）

### 14.2 微信开放平台

1. 注册 https://open.weixin.qq.com/ （需管理员微信扫码）
2. **开发者资质认证**（企业/个体，按平台指引）
3. 创建 **网站应用**（Web 登录，非小程序）
4. 配置 **授权回调域**：只填域名，如 `shop.example.com`（不要带 `https://` 和路径）
5. 提交审核，等待通过
6. 获得 **AppID**、**AppSecret**

### 14.3 修改项目配置

```yaml
social:
  mock-enabled: false
  frontend-base: https://shop.example.com
  wechat:
    app-id: wxXXXXXXXX
    app-secret: XXXXXXXX
    redirect-uri: https://shop.example.com/social/callback/wechat
```

重启后端。

### 14.4 测试流程

1. 打开 `https://shop.example.com/login`
2. 点击 **微信登录** → 应跳转微信扫码页（不再是直接模拟登录）
3. 扫码授权 → 跳回 `/social/callback` → 进入商城

失败时查：后端日志、回调域名是否与开放平台一致、是否 HTTPS。

---

## 15. 阶段 12：支付宝真登录（可选）

当前代码：**未配置 AppId 时走模拟**；正式环境需：

1. 登录 https://open.alipay.com/
2. 创建应用并配置 **授权回调地址**（与 `social.alipay.redirect-uri` 一致）
3. 开通 **用户信息授权** 等相关能力
4. 在 `SocialLoginService` 中接入 **支付宝 SDK** 用 `code` 换 token（需开发，可后续再做）

配置示例：

```yaml
social:
  alipay:
    app-id: 你的应用APPID
    redirect-uri: https://shop.example.com/social/callback/alipay
```

---

## 16. 常见问题

### Q1：备案要多久？

通常 **1～3 周**，各地不同。提交后注意短信核验。

### Q2：个人备案能开商城吗？

练手、展示可以；**正式商业运营、支付、企业微信应用** 建议 **个体户/公司** 主体。

### Q3：没有域名能否先部署？

**可以。** 按 [阶段 3A](#6a-阶段-3a公网-ip-测试部署docker无需域名) 用公网 IP 访问，例如 `http://82.156.68.87/shop/home`。  
地址栏显示的是 **公网 IP**（`82.x`、`43.x` 等），不是 `192.168.x.x`。  
**微信正式登录、小程序合法域名、长期正式上线** 仍需备案域名 + HTTPS。

### Q4：页面乱码怎么办？

在项目根目录执行：

```bash
python scripts/fix_all_encoding.py
```

重新打包部署前端。

### Q5：后端启动失败 / 找不到 SocialLoginService

在开发机先执行：

```bash
mvn install -pl ruoyi-admin -am -DskipTests
```

### Q6：502 Bad Gateway

- Java 是否在 8080 运行
- Nginx `proxy_pass` 是否指向 `127.0.0.1:8080`
- 防火墙是否放行本机访问

### Q7：微信回调 redirect_uri 参数错误

- 开放平台「授权回调域」与浏览器访问域名一致
- `application.yml` 里 `redirect-uri` 与微信后台登记的完全一致（含 https）

### Q8：`scp` 提示输入 password，但购机时选的是 SSH 密钥

- 密钥登录**没有** SSH 密码；必须在命令中加 **`-i 私钥文件路径`**
- 示例：`scp -i C:\Users\lemon\Downloads\ruoyimall.pem ... ubuntu@IP:~/mall/`
- 私钥购机时只下载一次，丢失需在腾讯云 **新建密钥** 绑定实例，或 **重置密码** 改用密码登录

---

## 17. 附录：配置示例

### 17.1 端口一览

| 服务 | 端口 | 对外 |
|------|------|------|
| Nginx | 80 / 443 | 是 |
| Spring Boot | 8080 | 仅本机（由 Nginx 反代） |
| MySQL | 3306 | 仅本机 |
| Redis | 6379 | 仅本机 |

### 17.2 默认账号（上线后务必修改密码）

| 账号 | 密码 | 说明 |
|------|------|------|
| admin | admin123 | 超级管理员，仅后台 |
| ry | admin123 | 普通用户，C 端购物 |

上线后：管理后台 → 用户管理 → 修改密码。

### 17.3 相关文件路径（本仓库）

| 文件 | 作用 |
|------|------|
| `deploy/docker-compose.yml` | 单机 Docker 编排（MySQL+Redis+API+Nginx） |
| `deploy/README.md` | 部署命令速查（含 `scp -i` 密钥示例） |
| `deploy/ssh-config.example` | Windows `~/.ssh/config` 别名示例 |
| `deploy/config/*.example` | 服务器生产配置模板（复制后改密码/IP） |
| `sql/ry_mall_complete.sql` | 全量建库 |
| `sql/mall_social.sql` | 第三方登录绑定表 |
| `ruoyi-admin/src/main/resources/application-druid.yml` | 本地开发数据库 |
| `ruoyi-ui/.env.production` | 生产 API 前缀 `/prod-api` |

### 17.5 与 SwapMini 小程序同机（可选）

若同一台 2核4G 还要跑 `D:\Users\lemon\miniprogram`：

| 项 | 建议 |
|----|------|
| MySQL | **一个实例**，库 `ry-vue` + `swapmini` |
| Redis | **一个实例**，不同 `database` 索引 |
| RabbitMQ | 仅小程序需要，约 300MB 内存 |
| 端口 | 商城 8080、小程序改 **8081**，均不映射公网，只经 Nginx |
| 升级 | 双 Java + Rabbit 吃紧时升 **4核8G** |

备案与域名就绪前，小程序正式版仍须 **https 合法域名**，不能长期用 IP。

### 17.4 官方文档链接

- 若依：https://doc.ruoyi.vip/
- 阿里云备案：https://help.aliyun.com/zh/icp-filing/
- 腾讯云备案：https://cloud.tencent.com/document/product/243
- 微信开放平台：https://developers.weixin.qq.com/doc/oplatform/Website_App/WeChat_Login/Wechat_Login.html

---

## 文档版本

- 适用项目：`mall`（RuoYi-Vue 3.9.2 + 商城模块）
- 更新说明：2026-06 增加腾讯云轻量 Docker、`deploy/` 编排、公网 IP 测试阶段 3A

如在某一阶段卡住，记下：**卡在第几阶段、截图/报错原文**，便于排查。
