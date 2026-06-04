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

---

## 二、本机首次：把代码推到 GitHub

在 **PowerShell** 中执行（路径按你电脑修改）。

### 2.1 进入项目目录

```powershell
# 切换到 mall 项目根目录
cd D:\Users\lemon\mall
```

### 2.2 配置 Git 用户信息（仅首次需要）

```powershell
# 设置提交时显示的名字（改成你的昵称）
git config user.name "Valentine-8"

# 设置提交时显示的邮箱（建议与 GitHub 账号邮箱一致）
git config user.email "your_email@example.com"
```

### 2.3 添加 GitHub 远程仓库

```powershell
# 查看当前远程地址（若仍是若依官方 Gitee，可保留不动）
git remote -v

# 新增远程名 github，指向你的空仓库（只执行一次）
git remote add github https://github.com/Valentine-8/mall.git

# 若提示 remote github already exists，可改用下面命令改地址：
# git remote set-url github https://github.com/Valentine-8/mall.git
```

### 2.4 本地数据库配置（不提交密码）

```powershell
# 若还没有 application-druid.yml，从示例复制
copy ruoyi-admin\src\main\resources\application-druid.yml.example ruoyi-admin\src\main\resources\application-druid.yml

# 用记事本或 Cursor 打开，把 your_mysql_password 改成你本机 MySQL 真实密码
notepad ruoyi-admin\src\main\resources\application-druid.yml
```

> `application-druid.yml` 已在 `.gitignore` 中，**不会**被 push 到 GitHub。

### 2.5 提交并推送

```powershell
# 把所有要纳入版本管理的文件加入暂存区（遵守 .gitignore）
git add .

# 从 Git 索引移除敏感文件（若曾经被跟踪过）
git rm --cached ruoyi-admin/src/main/resources/application-druid.yml 2>$null

# 查看即将提交的文件列表（确认没有 .env、jar、dist）
git status

# 创建一次提交（说明可自拟）
git commit -m "feat: 若依商城 C 端、Docker 部署、我的中心与 Git 部署文档"

# 推送到 GitHub 的 main 分支（当前本地分支为 springboot3 时）
git push -u github HEAD:main
```

若 GitHub 要求登录：

- 推荐 [Personal Access Token](https://github.com/settings/tokens) 作为密码  
- 或配置 SSH：`git@github.com:Valentine-8/mall.git`

推送成功后，打开 https://github.com/Valentine-8/mall 应能看到代码。

---

## 三、服务器首次：从 Git 克隆并部署

SSH 登录服务器（把 IP 换成你的）：

```powershell
# 本机用密钥连接服务器
ssh -i C:\Users\lemon\Downloads\ruoyimall.pem ubuntu@82.156.68.87
```

登录后，在 **服务器** 上执行：

### 3.1 安装 Git 与构建工具（仅首次）

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

### 3.2 克隆仓库

```bash
# 进入用户主目录
cd ~

# 从 GitHub 克隆（HTTPS；私有库需配置 token 或改用 SSH 地址）
git clone https://github.com/Valentine-8/mall.git

# 进入项目根目录
cd ~/mall
```

### 3.3 配置部署环境（仅首次）

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

### 3.4 执行首次构建并启动 Docker

```bash
# 回到项目根目录
cd ~/mall

# 给更新脚本执行权限（以后更新用）
chmod +x deploy/update-from-git.sh

# 运行一键脚本：拉代码已在 clone 时完成，此处会 mvn/npm 打包并重启容器
./deploy/update-from-git.sh
```

若脚本报错 `permission denied` 访问 Docker：

```bash
# 将当前用户加入 docker 组（执行后需重新 SSH 登录）
sudo usermod -aG docker ubuntu

# 或脚本里 docker 命令前加 sudo
```

### 3.5 验证

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

### 3.6 数据库补充脚本（若需要）

首次 `docker compose up` 会自动执行 `sql/ry_mall_complete.sql`。若还要社交登录、退款状态字典：

```bash
# 进入 MySQL 容器执行（密码换成你的）
cd ~/mall/deploy
source .env
sudo docker compose exec mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" ry-vue < ../sql/mall_social.sql
sudo docker compose exec mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" ry-vue < ../sql/mall_order_status_refund.sql
```

---

## 四、日常更新：本机改代码 → 推 Git → 服务器拉取

### 4.1 本机：提交并推送

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

### 4.2 服务器：拉取并重新部署

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

# 执行更新脚本（编译前后端 + 重启 mall-api、nginx）
./deploy/update-from-git.sh
```

`update-from-git.sh` 内部等价于：

```bash
git pull                                    # 拉最新代码
mvn clean package -DskipTests -pl ruoyi-admin -am   # 打 jar
cp ruoyi-admin/target/ruoyi-admin.jar deploy/app/
cd ruoyi-ui && npm ci && npm run build:prod # 打前端
cp -r dist/* ../deploy/html/
cd ../deploy && sudo docker compose restart mall-api nginx
```

### 4.3 仅改了前端时（可选，更快）

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

### 4.4 仅改了后端时（可选）

```bash
cd ~/mall
git pull
mvn clean package -DskipTests -pl ruoyi-admin -am
cp ruoyi-admin/target/ruoyi-admin.jar deploy/app/
cd deploy
sudo docker compose restart mall-api
```

---

## 五、常见问题

| 现象 | 处理 |
|------|------|
| `git push` 要密码 | 用 GitHub Token，或 SSH 地址 `git@github.com:Valentine-8/mall.git` |
| `git pull` 冲突 | 服务器上 `git stash` 后 `git pull`，再处理；**不要**在服务器改代码，应在本机改完 push |
| `mvn` 内存不足 | `export MAVEN_OPTS="-Xmx512m"` 后重试，或本机打包好 jar 只上传 `deploy/app/` |
| `npm run build` 失败 | 确认 Node ≥ 18；或本机 build 后只 scp `dist` 到 `deploy/html/` |
| 容器启动失败 | `sudo docker compose logs mall-api` 查 MySQL 密码是否与 config 一致 |
| 改了 `.env` 被覆盖 | `.env` 在 `.gitignore`，`git pull` **不会**覆盖；仅当误提交到 Git 才会冲突 |

---

## 六、安全提醒

1. **永远不要**把 `deploy/.env`、`deploy/config/application-druid.yml`、真实 `application-druid.yml` 提交到 Git。  
2. SSH 私钥 `ruoyimall.pem` 放在本机即可，**不要**上传仓库。  
3. 生产环境 MySQL、Redis **不要**对公网开放端口，仅 Docker 内网访问。

---

## 七、相关文档

- [从零到上线指南](./deploy-from-zero.md) — 买服务器、备案、HTTPS  
- [deploy/README.md](../deploy/README.md) — Docker 目录说明与命令速查
