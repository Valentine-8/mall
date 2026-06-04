# Git 部署与更新手册（GitHub → 腾讯云服务器）

> 仓库地址：[https://github.com/Valentine-8/mall](https://github.com/Valentine-8/mall)  
> 适用：已购腾讯云轻量、Docker 部署、`deploy/` 目录编排。  
> 每条命令下方有注释说明用途。

---

## 一、准备清单

| 项 | 说明 |
|----|------|
| 本机 | Windows，已装 Git、JDK 17、Maven、Node 18+ |
| 服务器 | 公网 IP、SSH 私钥 `ruoyimall.pem`、用户 `ubuntu` |
| 仓库 | `https://github.com/Valentine-8/mall.git` |
| 勿提交 | `deploy/.env`、`deploy/config/*.yml`（含密码）、`application-druid.yml` |
| 规格建议 | **2核4G** 可跑商城；**编译时**需按 [第二节](#二2核4g-省内存指南服务器已装-mavennode) 操作 |

---

## 二、2核4G 省内存指南（服务器已装 Maven/Node）

腾讯云轻量 **2核4G** 同时跑 **Docker（MySQL+Redis+Java）** 和 **Maven/Node 编译** 时，内存会顶满。下面做法按优先级排列。

### 2.1 原则（记住三条）

| 原则 | 说明 |
|------|------|
| **编译时停 Docker** | `mvn`、`npm run build` 最吃内存；编译前 `docker compose stop`，编完再 `up -d` |
| **限制编译内存** | Maven 512MB、Node 512MB 左右，避免占光 4G |
| **能本机编就别在服务器编** | 本机 `mvn`/`npm` 完成后，只上传 `jar` + `dist`，服务器只 `git pull` + 重启容器 |

你已安装 Maven、Node：**可以继续用**，只要编译前停容器并限制内存。

### 2.2 编译前：先看内存 + 停容器

```bash
# 查看内存（关注 available 一行，建议 > 1.2G 再编译）
free -h

# 进入编排目录
cd ~/mall/deploy

# 停止所有商城容器（释放 MySQL/Redis/Java/Nginx 占用的内存）
# 不删数据，volume 里数据库仍在
sudo docker compose stop

# 再确认内存是否回升
free -h
```

### 2.3 编译时：限制 Maven / Node 占用

```bash
# 进入项目根目录
cd ~/mall

# 限制 Maven 最大堆内存约 512MB（当前终端会话有效）
export MAVEN_OPTS="-Xmx512m"

# 限制 Node 打包最大内存约 512MB（当前终端会话有效）
export NODE_OPTIONS="--max-old-space-size=512"

# 使用仓库自带脚本（内部会 git pull、mvn、npm、重启容器）
# 脚本会在编译前尝试 stop、编译后 up -d，见 deploy/update-from-git.sh
chmod +x deploy/update-from-git.sh
./deploy/update-from-git.sh
```

若不用脚本、手动执行时，也在 **同一终端** 里先 `export` 再 `mvn` / `npm`。

### 2.4 编译后：启动 Docker

```bash
# 进入编排目录
cd ~/mall/deploy

# 启动容器（若 update-from-git.sh 已执行过，可跳过）
sudo docker compose up -d

# 查看是否都在运行
sudo docker compose ps
```

### 2.5 更省内存：本机编译，服务器只部署产物（推荐 4G 机）

服务器 **不必** 每次 `mvn`/`npm`，已装 Maven/Node 可保留备用。

**本机 Windows：**

```powershell
cd D:\Users\lemon\mall
mvn clean package -DskipTests -pl ruoyi-admin -am
cd ruoyi-ui
npm run build:prod
```

**上传到服务器（密钥路径按你的改）：**

```powershell
scp -i C:\Users\lemon\Downloads\ruoyimall.pem D:\Users\lemon\mall\ruoyi-admin\target\ruoyi-admin.jar ubuntu@82.156.68.87:~/mall/deploy/app/
scp -i C:\Users\lemon\Downloads\ruoyimall.pem -r D:\Users\lemon\mall\ruoyi-ui\dist\* ubuntu@82.156.68.87:~/mall/deploy/html/
```

**服务器只拉配置/代码并重启（不编译）：**

```bash
cd ~/mall
git pull origin main
cd deploy
sudo docker compose restart mall-api nginx
```

### 2.6 按需更新（少占内存）

| 只改了什么 | 服务器做什么 | 是否停 Docker |
|------------|--------------|---------------|
| 仅前端 Vue | `git pull` → 本机或服务器 `npm run build:prod` → 覆盖 `deploy/html/` → `restart nginx` | 服务器编前端时建议 **停** |
| 仅后端 Java | `git pull` → 本机或服务器 `mvn package` → 覆盖 `deploy/app/*.jar` → `restart mall-api` | 服务器编后端时建议 **停** |
| 只改 SQL/文档 | `git pull` 即可，**不用** mvn/npm | 不必停 |
| 日常小版本 | 优先 **本机编译 + scp**，服务器只重启 | 不必停 |

### 2.7 监控与告警（可选）

```bash
# 实时看内存
free -h

# 看哪个容器吃内存（Docker 运行期间）
cd ~/mall/deploy
sudo docker stats

# 若编译时进程被 Kill，日志里出现 Killed，就是内存不够
# 处理：确保 compose stop 后再编，或改本机编译
```

### 2.8 以后若要同机跑小程序

SwapMini（第二个 Java + RabbitMQ）在 2核4G 上 **偏紧**，建议升级 **4核8G**，或小程序单独一台轻量。

---

## 三、本机首次：把代码推到 GitHub

在 **PowerShell** 中执行（路径按你电脑修改）。

### 3.1 进入项目目录

```powershell
# 切换到 mall 项目根目录
cd D:\Users\lemon\mall
```

### 3.2 配置 Git 用户信息（仅首次需要）

```powershell
# 设置提交时显示的名字（改成你的昵称）
git config user.name "Valentine-8"

# 设置提交时显示的邮箱（建议与 GitHub 账号邮箱一致）
git config user.email "your_email@example.com"
```

### 3.3 添加 GitHub 远程仓库

```powershell
# 查看当前远程地址（若仍是若依官方 Gitee，可保留不动）
git remote -v

# 新增远程名 github，指向你的空仓库（只执行一次）
git remote add github https://github.com/Valentine-8/mall.git

# 若提示 remote github already exists，可改用下面命令改地址：
# git remote set-url github https://github.com/Valentine-8/mall.git
```

### 3.4 本地数据库配置（不提交密码）

```powershell
# 若还没有 application-druid.yml，从示例复制
copy ruoyi-admin\src\main\resources\application-druid.yml.example ruoyi-admin\src\main\resources\application-druid.yml

# 用记事本或 Cursor 打开，把 your_mysql_password 改成你本机 MySQL 真实密码
notepad ruoyi-admin\src\main\resources\application-druid.yml
```

> `application-druid.yml` 已在 `.gitignore` 中，**不会**被 push 到 GitHub。

### 3.5 提交并推送

```powershell
# 把所有要纳入版本管理的文件加入暂存区（遵守 .gitignore）
git add .

# 从 Git 索引移除敏感文件（若曾经被跟踪过）
git rm --cached ruoyi-admin/src/main/resources/application-druid.yml 2>$null

# 查看即将提交的文件列表（确认没有 .env、jar、dist）
git status

# 创建一次提交（说明可自拟）
git commit -m "feat: 若依商城 C 端、Docker 部署、我的中心与 Git 部署文档"

# 推送到 GitHub 的 main 分支
git push -u github main

# 若推送失败（index-pack failed），可用「无历史」分支再推（见本文第七节）
# git checkout --orphan main
# git add -A
# git commit -m "feat: initial mall release"
# git push -u github main
```

若 GitHub 要求登录：

- 推荐 [Personal Access Token](https://github.com/settings/tokens) 作为密码  
- 或配置 SSH：`git@github.com:Valentine-8/mall.git`

推送成功后，打开 https://github.com/Valentine-8/mall 应能看到代码。

---

## 四、服务器首次：从 Git 克隆并部署

SSH 登录服务器（把 IP 换成你的）：

```powershell
# 本机用密钥连接服务器
ssh -i C:\Users\lemon\Downloads\ruoyimall.pem ubuntu@82.156.68.87
```

登录后，在 **服务器** 上执行：

### 4.1 安装 Git 与构建工具（仅首次）

```bash
# 更新软件包索引
sudo apt update

# 安装 Git（拉取代码）
sudo apt install -y git

# 安装 JDK 17（编译后端）
sudo apt install -y openjdk-17-jdk

# 安装 Maven（打包 ruoyi-admin.jar）
sudo apt install -y maven

# 安装 Node.js 18（打包前端，Ubuntu 22 可用 NodeSource 或 apt）
sudo apt install -y nodejs npm

# 验证版本
java -version
mvn -version
node -v
npm -v
```

> 若 `node -v` 低于 18，请按 Node 官网换源安装 18+，否则 `npm run build:prod` 可能失败。

### 4.2 克隆仓库

```bash
# 进入用户主目录
cd ~

# 从 GitHub 克隆（HTTPS；私有库需配置 token 或改用 SSH 地址）
git clone https://github.com/Valentine-8/mall.git

# 进入项目根目录
cd ~/mall
```

### 4.3 配置部署环境（仅首次）

```bash
# 进入 Docker 编排目录
cd ~/mall/deploy

# 复制环境变量模板
cp .env.example .env

# 复制后端配置模板
cp config/application-druid.yml.example config/application-druid.yml
cp config/application.yml.example config/application.yml

# 编辑 .env：设置 MySQL 密码、公网 IP
nano .env
# 修改 MYSQL_ROOT_PASSWORD=你的强密码
# 修改 PUBLIC_HOST=82.156.68.87

# 编辑数据库连接（password 与 .env 中 MYSQL_ROOT_PASSWORD 一致）
nano config/application-druid.yml

# 编辑商城对外地址（IP 测试阶段用 http://公网IP）
nano config/application.yml
```

### 4.4 执行首次构建并启动 Docker

> **2核4G 必读：** 先阅读 [第二节 省内存指南](#二2核4g-省内存指南服务器已装-mavennode)。首次编译建议先 `sudo docker compose stop`（若容器已存在），或尚未 `up -d` 则直接编译。

```bash
# 回到项目根目录
cd ~/mall

# 限制编译内存（建议每次编译前执行）
export MAVEN_OPTS="-Xmx512m"
export NODE_OPTIONS="--max-old-space-size=512"

# 给更新脚本执行权限（以后更新用）
chmod +x deploy/update-from-git.sh

# 若 Docker 已在跑，先停容器腾出内存
cd deploy && sudo docker compose stop && cd ~/mall

# 运行一键脚本：mvn/npm 打包，结束后自动 up -d
./deploy/update-from-git.sh
```

若脚本报错 `permission denied` 访问 Docker：

```bash
# 将当前用户加入 docker 组（执行后需重新 SSH 登录）
sudo usermod -aG docker ubuntu

# 或脚本里 docker 命令前加 sudo
```

### 4.5 验证

```bash
# 查看容器是否都在运行（State 为 Up）
cd ~/mall/deploy
sudo docker compose ps

# 看后端日志是否有报错
sudo docker compose logs -f mall-api
# 无错误后按 Ctrl+C 退出
```

浏览器访问：

- `http://你的公网IP/` — 管理后台  
- `http://你的公网IP/shop/home` — C 端商城  
- `http://你的公网IP/shop/mine` — 我的  

### 4.6 数据库补充脚本（若需要）

首次 `docker compose up` 会自动执行 `sql/ry_mall_complete.sql`。若还要社交登录、退款状态字典：

```bash
# 在项目根目录执行（自动读 deploy/.env、走 Docker 里的 MySQL）
cd ~/mall
chmod +x deploy/run-sql.sh
./deploy/run-sql.sh --extra
# 或单独执行某一个：
# ./deploy/run-sql.sh sql/mall_social.sql
```

本机 Windows 也可用 `deploy\run-sql.bat sql\mall_social.sql`（经 SSH，无需 DBeaver 开 3306）。

---

## 五、日常更新：本机改代码 → 推 Git → 服务器拉取

### 5.1 本机：提交并推送

```powershell
# 进入项目
cd D:\Users\lemon\mall

# 查看改了哪些文件
git status

# 加入暂存区
git add .

# 提交（说明写清楚本次改了什么）
git commit -m "fix: 描述本次修改"

# 推到 GitHub main 分支
git push github HEAD:main
```

### 5.2 服务器：拉取并重新部署

```powershell
# SSH 登录服务器
ssh -i C:\Users\lemon\Downloads\ruoyimall.pem ubuntu@82.156.68.87
```

```bash
# 进入项目目录
cd ~/mall

# 拉取 GitHub 最新代码（与首次 clone 的远程一致时可用下面两种之一）

# 方式 A：若已设置 origin 指向 GitHub
git pull origin main

# 方式 B：若只 clone 过、远程名是 origin 且跟踪 main
git pull

# 若首次 clone 后添加了 github 远程：
# git remote add origin https://github.com/Valentine-8/mall.git
# git pull origin main

# 2核4G：编译前停 Docker、限制内存（见第二节）
export MAVEN_OPTS="-Xmx512m"
export NODE_OPTIONS="--max-old-space-size=512"
cd deploy && sudo docker compose stop && cd ~/mall

# 执行更新脚本（编译前 stop、编译后 up -d）
./deploy/update-from-git.sh
```

更省内存：改成本机编译 + `scp` jar/dist，服务器只 `git pull` 和 `docker compose restart`（见第二节 2.5）。

`update-from-git.sh` 内部等价于：

```bash
docker compose stop                 # 编译前释放内存（脚本自动）
git pull                            # 拉最新代码
mvn clean package ...               # 打 jar（受 MAVEN_OPTS 限制）
cp ... ruoyi-admin.jar deploy/app/
npm ci && npm run build:prod        # 打前端（受 NODE_OPTIONS 限制）
cp dist/* deploy/html/
docker compose up -d                # 编完后启动（脚本自动）
```

### 5.3 仅改了前端时（可选，更快）

```bash
cd ~/mall
git pull
cd ruoyi-ui
npm ci
npm run build:prod
rm -rf ../deploy/html/*
cp -r dist/* ../deploy/html/
cd ../deploy
sudo docker compose restart nginx
```

### 5.4 仅改了后端时（可选）

```bash
cd ~/mall
git pull
mvn clean package -DskipTests -pl ruoyi-admin -am
cp ruoyi-admin/target/ruoyi-admin.jar deploy/app/
cd deploy
sudo docker compose restart mall-api
```

---

## 六、常见问题

| 现象 | 处理 |
|------|------|
| 编译时卡住 / `Killed` | 先 `sudo docker compose stop`；`export MAVEN_OPTS="-Xmx512m"`、`NODE_OPTIONS="--max-old-space-size=512"`；或改本机编译（第二节 2.5） |
| `git push` 要密码 | 用 GitHub Token，或 SSH 地址 `git@github.com:Valentine-8/mall.git` |
| `git pull` 冲突 | 服务器上 `git stash` 后 `git pull`，再处理；**不要**在服务器改代码，应在本机改完 push |
| `mvn` 内存不足 | 见第二节；或本机打包 jar 只上传 `deploy/app/` |
| `npm run build` 失败 | 确认 Node ≥ 18；编译前停 Docker；或本机 build 后 scp `dist` |
| 容器启动失败 | `sudo docker compose logs mall-api` 查 MySQL 密码是否与 config 一致 |
| 改了 `.env` 被覆盖 | `.env` 在 `.gitignore`，`git pull` **不会**覆盖；仅当误提交到 Git 才会冲突 |

---

## 七、安全提醒

1. **永远不要**把 `deploy/.env`、`deploy/config/application-druid.yml`、真实 `application-druid.yml` 提交到 Git。  
2. SSH 私钥 `ruoyimall.pem` 放在本机即可，**不要**上传仓库。  
3. 生产环境 MySQL、Redis **不要**对公网开放端口，仅 Docker 内网访问。

---

## 八、推送失败 `index-pack failed` 时

若 `git push` 报错 `did not receive expected object`，多为旧远程历史过大。可改用**无历史**首次推送：

```powershell
cd D:\Users\lemon\mall
git checkout --orphan main
git add -A
git status
git commit -m "feat: initial mall release"
git push -u github main
```

成功后 GitHub 上只有一条干净提交，不影响日常使用 `git pull` / `git push`。

---

## 九、相关文档

- [从零到上线指南](./deploy-from-zero.md) — 买服务器、备案、HTTPS  
- [deploy/README.md](../deploy/README.md) — Docker 目录说明与命令速查
