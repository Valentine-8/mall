# 商城 Docker 部署（腾讯云轻量）

与 `docs/deploy-from-zero.md` 中 **「阶段 3A：公网 IP 测试」** 配套使用。

## 目录结构

```
deploy/
├── docker-compose.yml
├── .env.example          → 复制为 .env
├── config/               → 从 .example 复制并改密码、公网 IP
├── app/                  → 放 ruoyi-admin.jar
├── html/                 → 放 ruoyi-ui 打包后的 dist 内容
├── nginx/default.conf
└── ssh-config.example    → 可选，Windows SSH 别名配置
```

## 服务器防火墙

在轻量控制台：**服务器 → 实例 → 防火墙**，放行 **22 / 80 / 443**。不要对公网开放 3306、6379、8080。

## SSH 密钥登录（购机时若选了密钥）

本机 `scp`/`ssh` **必须** 指定私钥，否则会误提示输入 password：

```powershell
# 将路径换成你下载的 .pem，例如 ruoyimall.pem
$PEM = "C:\Users\lemon\Downloads\ruoyimall.pem"
$IP  = "82.156.68.87"
```

首次连接出现 `Are you sure you want to continue connecting` 时输入 **`yes`**。

## 一键命令摘要

### 本机（Windows）打包

```powershell
cd D:\Users\lemon\mall
mvn clean package -DskipTests -pl ruoyi-admin -am

cd ruoyi-ui
npm install
npm run build:prod
```

### 上传到服务器（分三步，均需 `-i $PEM`）

```powershell
$PEM = "C:\Users\lemon\Downloads\ruoyimall.pem"
$IP  = "82.156.68.87"

# ① deploy + sql
scp -i $PEM -r D:\Users\lemon\mall\deploy D:\Users\lemon\mall\sql ubuntu@${IP}:~/mall/

# ② jar（先完成 mvn package）
scp -i $PEM D:\Users\lemon\mall\ruoyi-admin\target\ruoyi-admin.jar ubuntu@${IP}:~/mall/deploy/app/

# ③ 前端 dist（先完成 npm run build:prod）
scp -i $PEM -r D:\Users\lemon\mall\ruoyi-ui\dist\* ubuntu@${IP}:~/mall/deploy/html/
```

### 登录服务器

```powershell
ssh -i C:\Users\lemon\Downloads\ruoyimall.pem ubuntu@82.156.68.87
```

### 服务器上启动

```bash
cd ~/mall/deploy
cp .env.example .env
cp config/application-druid.yml.example config/application-druid.yml
cp config/application.yml.example config/application.yml

nano .env                      # MYSQL_ROOT_PASSWORD、PUBLIC_HOST
nano config/application-druid.yml   # password 与 .env 一致
nano config/application.yml    # social.frontend-base 改为 http://你的公网IP

docker compose up -d
docker compose ps
docker compose logs -f mall-api
```

浏览器访问：`http://你的公网IP/`（例如 `http://82.156.68.87/`）

## 验证

```bash
curl -s http://127.0.0.1/prod-api/captchaImage | head -c 200
docker compose exec mysql mysql -uroot -p"你的MySQL密码" -e "USE \`ry-vue\`; SHOW TABLES LIKE 'mall_%';"
```

## 备案通过后

1. 域名 A 记录 → 公网 IP  
2. 申请 SSL，Nginx 增加 443（见上线指南阶段 9）  
3. 修改 `config/application.yml` 中 `social.frontend-base` 与 `redirect-uri` 为 `https://你的域名`  
4. `social.mock-enabled: false` 并填写微信 AppId（真登录）

## 同机再跑小程序（SwapMini）

需改端口、共用 MySQL/Redis，见上线指南 **附录：与 SwapMini 共存**。

---

## 用 Git 在服务器上更新（推荐长期做法）

可以。流程是：**本机 push 到 Git 仓库 → 服务器 `git pull` → 重新打包/重启**，不必每次 `scp`。

### 不要提交到 Git 的内容

| 路径 | 原因 |
|------|------|
| `deploy/.env` | MySQL 密码 |
| `deploy/config/application-druid.yml` | 数据库密码 |
| `deploy/config/application.yml` | 含 IP/密钥等 |
| `deploy/app/*.jar` | 构建产物 |
| `deploy/html/*` | 前端打包产物 |
| `ruoyi-ui/node_modules/`、`dist/` | 已在 .gitignore |

`deploy/.env.example`、`deploy/config/*.example` 可以提交，作模板。

### 服务器首次克隆（示例）

```bash
cd ~
git clone https://你的仓库地址/mall.git
cd mall/deploy
cp .env.example .env
cp config/application-druid.yml.example config/application-druid.yml
cp config/application.yml.example config/application.yml
# nano 改密码、公网 IP（仅服务器上改，不要提交 Git）
```

首次仍要本机或服务器上完成一次 `mvn package` 与 `npm run build:prod`，把 jar 放进 `deploy/app/`、dist 放进 `deploy/html/`，再 `sudo docker compose up -d`。

### 服务器需要具备

在 **2核4G** 上若要在服务器本地打包，需安装（Docker 镜像默认可能没有）：

- **JDK 17**、**Maven**（打 jar）
- **Node 18+**（`ruoyi-ui` 里 `npm run build:prod`）

**省内存（必读）：** 编译前 `docker compose stop`，设置 `MAVEN_OPTS` / `NODE_OPTIONS`，编完再 `up -d`；或本机打包后只 `scp` 产物。详见 **[docs/git-deploy.md 第二节](../docs/git-deploy.md#二2核4g-省内存指南服务器已装-mavennode)**。

若不想在服务器装 Maven/Node，可以：**本机打包** → 只把 `ruoyi-admin.jar` 和 `dist` 同步到服务器（`scp`），代码更新仍用 `git pull` 拉源码。

### 执行 SQL 脚本

**服务器上**（MySQL 容器已启动）：

```bash
chmod +x deploy/run-sql.sh
./deploy/run-sql.sh sql/mall_social.sql
./deploy/run-sql.sh --extra    # mall_social + mall_order_status_refund
./deploy/run-sql.sh --list
```

**本机 Windows**（经 SSH，不必开 3306）：

```powershell
deploy\run-sql.bat sql\mall_social.sql
deploy\run-sql.bat --extra
```

首次使用请编辑 `deploy\run-sql.bat` 里的私钥路径、公网 IP、`REMOTE_DIR`。

### 日常更新脚本（在服务器 `~/mall` 执行）

```bash
chmod +x deploy/update-from-git.sh
./deploy/update-from-git.sh
```

脚本会：`stop` 容器 → `git pull` → `mvn`/`npm`（默认限制内存）→ 复制 jar/dist → `docker compose up -d`。

### 注意

- 私钥 `ruoyimall.pem` **不要**进仓库；继续用 `ssh -i` 登录服务器。
- 只改 SQL 且要**重新初始化库**时，不要用 `git pull` 代替数据迁移，需自行备份后处理 MySQL volume。
- 小程序 `miniprogram` 可单独一个仓库，同机部署时再拉第二个项目。
