# Git 部署与更新手册（GitHub → 腾讯云服务器）

> 仓库：[https://github.com/Valentine-8/mall](https://github.com/Valentine-8/mall)  
> 适用：腾讯云轻量 **2核4G**、Docker 部署（`deploy/` 编排）。  
> **当前推荐**：本机编译，服务器只收产物并重启容器。

---

## 一、部署架构（当前配置）

```
本机 Windows                          GitHub                    服务器 ubuntu
─────────────────────────────────────────────────────────────────────────────
Git / JDK17 / Maven / Node18+    →    源码版本库          ←    Git（仅拉配置）
mvn package / npm build          →    （不含 jar/dist）        Docker Compose
scp jar + dist ──────────────────────────────────────────→    deploy/app/
                                                              deploy/html/
                                                              restart 容器
```

| 角色 | 安装什么 | 做什么 |
|------|----------|--------|
| **本机** | Git、JDK 17、Maven、Node 18+ | 改代码、提交、**打包**、**scp 上传** |
| **GitHub** | — | 存源码；**不含** jar、dist、密码 |
| **服务器** | **Git + Docker**（**不装** Maven/Node/JDK） | 跑容器；按需 `git pull`；**重启**加载新 jar/dist |

### 重要：只 push 不会更新网站

`ruoyi-admin.jar` 和 `ruoyi-ui/dist` 在 `.gitignore` 里，**不会**进 Git。  
日常发版必须：**本机 build → scp → 服务器 restart**。

---

## 二、准备清单

| 项 | 值 / 说明 |
|----|-----------|
| 本机项目路径 | `D:\Users\lemon\mall` |
| 本机工具 | Git、JDK 17、Maven、Node 18+ |
| GitHub 远程 | `github` → `https://github.com/Valentine-8/mall.git` |
| 服务器 IP | `82.156.68.87`（换成你的） |
| SSH 用户 | `ubuntu` |
| SSH 私钥 | `C:\Users\lemon\Downloads\ruoyimall.pem` |
| 服务器项目路径 | `~/mall` |
| 服务器规格 | 2核4G（跑 Docker 足够；编译在本机完成） |
| **勿提交 Git** | `deploy/.env`、`deploy/config/*.yml`、`application-druid.yml`、jar、dist |

---

## 三、本机首次：关联 GitHub 并推送

在 **PowerShell** 中执行。

### 3.1 进入项目

```powershell
cd D:\Users\lemon\mall
```

### 3.2 Git 用户信息（仅首次）

```powershell
git config user.name "Valentine-8"
git config user.email "your_email@example.com"
```

### 3.3 添加 GitHub 远程（仅首次）

```powershell
git remote -v
git remote add github https://github.com/Valentine-8/mall.git
# 若已存在：git remote set-url github https://github.com/Valentine-8/mall.git
```

### 3.4 本地数据库配置（不提交密码）

```powershell
copy ruoyi-admin\src\main\resources\application-druid.yml.example ruoyi-admin\src\main\resources\application-druid.yml
notepad ruoyi-admin\src\main\resources\application-druid.yml
```

### 3.5 提交并推送

```powershell
git add .
git status
git commit -m "fix: 描述本次修改"
git push github HEAD:main
```

登录 GitHub 失败时：用 [Personal Access Token](https://github.com/settings/tokens) 作密码，或改用 SSH 地址 `git@github.com:Valentine-8/mall.git`。

推送失败 `index-pack failed` 见 [附录 A](#附录-apush-失败-index-pack-failed)。

---

## 四、服务器首次：克隆、配置、启动

### 4.1 SSH 登录

```powershell
ssh -i C:\Users\lemon\Downloads\ruoyimall.pem ubuntu@82.156.68.87
```

### 4.2 安装 Git 与 Docker（仅首次）

```bash
sudo apt update
sudo apt install -y git

# Docker 若购机镜像已带可跳过；否则按腾讯云文档安装 Docker + Compose 插件
docker --version
docker compose version
```

> **不需要**在服务器安装 JDK、Maven、Node。编译全部在本机完成。

### 4.3 克隆仓库

```bash
cd ~
git clone https://github.com/Valentine-8/mall.git
cd ~/mall
```

### 4.4 配置部署环境（仅首次）

```bash
cd ~/mall/deploy
cp .env.example .env
cp config/application-druid.yml.example config/application-druid.yml
cp config/application.yml.example config/application.yml

nano .env
# MYSQL_ROOT_PASSWORD=你的强密码
# PUBLIC_HOST=82.156.68.87

nano config/application-druid.yml
# password 与 .env 中 MYSQL_ROOT_PASSWORD 一致

nano config/application.yml
# social.frontend-base 等改为 http://你的公网IP
```

### 4.5 本机打包并上传（首次也要在本机编）

**本机 PowerShell：**

```powershell
cd D:\Users\lemon\mall
mvn clean package -DskipTests -pl ruoyi-admin -am
cd ruoyi-ui
npm install
npm run build:prod
cd ..

$PEM = "C:\Users\lemon\Downloads\ruoyimall.pem"
$IP  = "82.156.68.87"

scp -i $PEM D:\Users\lemon\mall\ruoyi-admin\target\ruoyi-admin.jar ubuntu@${IP}:~/mall/deploy/app/
scp -i $PEM -r D:\Users\lemon\mall\ruoyi-ui\dist\* ubuntu@${IP}:~/mall/deploy/html/
```

### 4.6 启动 Docker

**服务器：**

```bash
cd ~/mall/deploy
sudo docker compose up -d
sudo docker compose ps
sudo docker compose logs -f mall-api
```

浏览器验证：

- `http://82.156.68.87/` — 管理后台
- `http://82.156.68.87/shop/home` — C 端商城

### 4.7 数据库补充脚本（可选）

首次 `docker compose up` 会自动执行 `sql/ry_mall_complete.sql`。若还要社交登录、退款字典：

**本机 Windows（推荐）：**

```powershell
deploy\run-sql.bat --extra
```

**或服务器：**

```bash
cd ~/mall
chmod +x deploy/run-sql.sh
./deploy/run-sql.sh --extra
```

---

## 五、日常更新（标准流程）

每次改完业务代码，按下面顺序操作。

### 5.1 本机：提交到 GitHub

```powershell
cd D:\Users\lemon\mall
git add .
git commit -m "fix: 描述本次修改"
git push github HEAD:main
```

> 到这一步，GitHub 有最新源码，**线上网站尚未变化**。

### 5.2 本机：打包

**只改了后端：**

```powershell
mvn clean package -DskipTests -pl ruoyi-admin -am
```

**只改了前端：**

```powershell
cd ruoyi-ui
npm run build:prod
cd ..
```

**前后端都改：** 两个命令都执行。

### 5.3 本机：上传到服务器

```powershell
$PEM = "C:\Users\lemon\Downloads\ruoyimall.pem"
$IP  = "82.156.68.87"

# 只改后端
scp -i $PEM D:\Users\lemon\mall\ruoyi-admin\target\ruoyi-admin.jar ubuntu@${IP}:~/mall/deploy/app/

# 只改前端
scp -i $PEM -r D:\Users\lemon\mall\ruoyi-ui\dist\* ubuntu@${IP}:~/mall/deploy/html/
```

### 5.4 服务器：重启容器

```powershell
ssh -i C:\Users\lemon\Downloads\ruoyimall.pem ubuntu@82.156.68.87
```

```bash
cd ~/mall/deploy

# 只改后端
sudo docker compose restart mall-api

# 只改前端
sudo docker compose restart nginx

# 前后端都改
sudo docker compose restart mall-api nginx

sudo docker compose ps
```

完成。浏览器 **Ctrl+F5** 强刷验证。

---

## 六、按需更新速查

| 改了什么 | 本机 | 服务器 |
|----------|------|--------|
| 仅 Java 后端 | `mvn package` → scp jar | `restart mall-api`（**不必** git pull） |
| 仅 Vue 前端 | `npm run build:prod` → scp dist | `restart nginx`（**不必** git pull） |
| Java + Vue | 都打包 → 都 scp | `restart mall-api nginx` |
| SQL 脚本 | push Git | `git pull` + 执行 SQL（见第七节） |
| `deploy/` 配置（compose、nginx 等） | push Git | `git pull`，必要时 `docker compose up -d` |
| 只改文档 | push Git | 可不做任何操作 |

**不要**在服务器上跑 `deploy/update-from-git.sh`（该脚本会在服务器编译，与当前配置不符）。  
附录 B 仅供仍保留 Maven/Node 的服务器参考。

---

## 七、执行 SQL 脚本

MySQL 在 Docker 内，**不必**对公网开放 3306。

### 7.1 本机 Windows（推荐）

编辑 `deploy/run-sql.bat` 顶部 PEM、IP、`REMOTE_DIR` 后：

```powershell
deploy\run-sql.bat sql\mall_social.sql
deploy\run-sql.bat --extra
deploy\run-sql.bat --list
```

### 7.2 服务器

```bash
cd ~/mall
git pull origin main
chmod +x deploy/run-sql.sh
./deploy/run-sql.sh sql/mall_social.sql
./deploy/run-sql.sh --extra
./deploy/run-sql.sh --list
```

---

## 八、常见问题

| 现象 | 处理 |
|------|------|
| push 后网站没变 | 正常。必须 **scp jar/dist + restart**，见第五节 |
| `git push` 连不上 GitHub | 确认翻墙/代理；终端有时比浏览器晚生效，重试 `git push github HEAD:main` |
| `git push` 要密码 | 用 GitHub Token，或 SSH 远程 `git@github.com:Valentine-8/mall.git` |
| `git pull` 冲突 | 服务器 `git stash` 后 `git pull`；**不要在服务器改代码** |
| scp 要 password | 加 `-i` 指定 `.pem` 私钥 |
| 页面空白 | 检查 `deploy/html/` 是否有 `index.html`（dist 是否传全） |
| 容器启动失败 | `sudo docker compose logs mall-api`，查 MySQL 密码是否与 config 一致 |
| `.env` 被覆盖 | `.env` 在 `.gitignore`，`git pull` **不会**覆盖 |
| 想释放服务器磁盘 | 删 `~/.m2`、`~/mall/ruoyi-ui/node_modules`（若曾装过构建工具） |

---

## 九、安全提醒

1. **永远不要**把 `deploy/.env`、`deploy/config/application*.yml`、真实 `application-druid.yml` 提交到 Git。
2. SSH 私钥 `ruoyimall.pem` 只放本机，**不要**进仓库。
3. 防火墙只放行 **22 / 80 / 443**，不要对公网开放 3306、6379、8080。

---

## 附录 A：push 失败 `index-pack failed`

```powershell
cd D:\Users\lemon\mall
git checkout --orphan main
git add -A
git status
git commit -m "feat: initial mall release"
git push -u github main
```

成功后 GitHub 只有一条干净提交，不影响后续 `git pull` / `git push`。

---

## 附录 B：服务器端编译（可选，非当前配置）

若服务器 **仍安装** JDK 17、Maven、Node 18+，可在 2核4G 上临时编译，但需：

1. 编译前 `sudo docker compose stop` 释放内存  
2. `export MAVEN_OPTS="-Xmx512m"`、`export NODE_OPTIONS="--max-old-space-size=512"`  
3. 编完后 `sudo docker compose up -d`

```bash
cd ~/mall
chmod +x deploy/update-from-git.sh
./deploy/update-from-git.sh
```

**当前环境已卸载 Maven/Node/JDK，请使用第五节本机编译流程。**  
若误装构建工具想清理：

```bash
sudo apt remove --purge -y maven nodejs npm openjdk-17-jdk openjdk-17-jre-headless
sudo apt autoremove -y
rm -rf ~/.m2 ~/mall/ruoyi-ui/node_modules ~/mall/ruoyi-admin/target
```

---

## 相关文档

- [从零到上线指南](./deploy-from-zero.md) — 买服务器、备案、HTTPS
- [deploy/README.md](../deploy/README.md) — Docker 目录与命令速查
