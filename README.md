# 若依商城管理系统

基于 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue)（Spring Boot 3）+ [RuoYi-Vue3](https://gitcode.com/yangzongzhuan/RuoYi-Vue3) 搭建的**商城后台管理系统**。

## 功能模块

### 管理后台（B 端）

| 模块 | 说明 |
|------|------|
| 商品分类 | 分类增删改查、排序、启用停用 |
| 商品管理 | 商品信息、价格库存、上下架 |
| 订单管理 | 订单列表、详情、发货/完成/取消 |

### C 端商城（用户购物）

| 功能 | 说明 |
|------|------|
| 商城首页 | 分类筛选、商品列表（无需登录可浏览） |
| 商品详情 | 加入购物车、立即购买 |
| 购物车 | 勾选、改数量、结算 |
| 下单支付 | 填写收货信息提交订单，模拟支付 |
| 我的订单 | 查看订单、支付、取消待付款订单 |

## 从 0 到上线（服务器 / 备案 / 域名 / 真微信登录）

零基础可按文档逐步操作（请用 UTF-8 打开）：

- **[docs/deploy-from-zero.md](docs/deploy-from-zero.md)**（推荐，英文文件名不易乱码）
- [docs/从零到上线指南.md](docs/从零到上线指南.md)（与上为同一份内容）
- **[docs/git-deploy.md](docs/git-deploy.md)** — **GitHub 推送 + 服务器克隆/更新**（含 **2核4G 省内存** 编译与更新）

**已购腾讯云轻量 Docker、先用公网 IP 测试**：直接看文档 **阶段 3A**，配套 **[deploy/](deploy/)**（`docker compose` 一键起 MySQL + Redis + 后端 + Nginx）。  
**SSH 密钥登录**：`scp`/`ssh` 须加 `-i 你的.pem`，见 `deploy/README.md` 与文档 **Q8**。

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 5.7+ / 8.0+
- Node.js 18+（前端）

## 快速启动

### 1. 数据库

执行**一份完整脚本**即可（已含建库语句）：

```bash
mysql -u root -p < sql/ry_mall_complete.sql
```

或在 MySQL 客户端中：

```sql
source sql/ry_mall_complete.sql;
```

（也可分步执行 `ry_20260417.sql` → `mall.sql` → `mall_menu.sql` → `mall_cart.sql`）

若需**微信/支付宝登录**，额外执行：

```bash
mysql -u root -p ry-vue < sql/mall_social.sql
```

### 2. 后端

修改 `ruoyi-admin/src/main/resources/application-druid.yml` 中的数据库账号密码，然后：

```bash
cd ruoyi-admin
mvn spring-boot:run
```

默认端口：`8080`

### 3. 前端

```bash
cd ruoyi-ui
npm install
npm run dev
```

默认端口：`80`，代理到后端 `http://localhost:8080`

### 4. 登录

- 地址：http://localhost
- 账号：`admin` / `admin123`（若依默认）

登录后左侧菜单可见 **商城管理** → 商品分类 / 商品管理 / 订单管理。

### 5. C 端商城

浏览器访问：**http://localhost/shop/home**

- 未登录可浏览商品；加购、下单需先登录（**C 端建议用普通用户** `ry` / `admin123`，不要用 `admin` 逛商城）
- C 端登录后仅显示昵称与「退出」，**无管理后台入口**；非管理员账号无法进入后台管理页
- 登录页有「进入商城」入口，并支持 **微信 / 支付宝登录**（见下方说明）
- 流程：选商品 → 购物车 → 结算 → 模拟支付 → **我的**（订单/资料）
- 移动端底部 Tab：**首页 / 购物车 / 我的**（类淘宝：订单筛选、编辑昵称手机、确认收货）
- **响应式布局**：同一地址自动适配——手机（宽度小于 769px）为 H5 底部 Tab + 双列商品；PC 浏览器为顶栏导航 + 左侧分类 + 四列商品。可用浏览器开发者工具切换设备预览

## 项目结构

```
mall/
├── ruoyi-admin/      # 启动入口、Controller
├── ruoyi-mall/       # 商城业务（domain/mapper/service）
├── ruoyi-ui/         # Vue3 前端
└── sql/
    ├── ry_20260417.sql   # 若依基础
    ├── mall.sql          # 商城表
    └── mall_menu.sql     # 商城菜单
```

## API 说明（C 端）

| 接口前缀 | 说明 |
|----------|------|
| `GET /app/mall/category/list` | 分类列表（匿名） |
| `GET /app/mall/product/list` | 商品列表（匿名） |
| `GET /app/mall/product/{id}` | 商品详情（匿名） |
| `/app/mall/cart/**` | 购物车（需登录） |
| `/app/mall/order/**` | 下单/支付/订单（需登录） |

## 微信 / 支付宝登录

1. 执行 `sql/mall_social.sql` 创建绑定表 `sys_social_bind`
2. 重启后端；登录页会显示「微信登录」「支付宝登录」
3. **本地开发**（默认）：`application.yml` 中 `social.mock-enabled: true` 且未填 AppId 时，点击按钮走**模拟登录**，自动创建并绑定商城用户（角色与普通用户相同）
4. **正式微信登录**：在 [微信开放平台](https://open.weixin.qq.com/) 创建网站应用，配置：
   - `social.wechat.app-id` / `app-secret`
   - `social.wechat.redirect-uri`：后端回调，如 `http://你的域名:8080/social/callback/wechat`
   - `social.frontend-base`：前端访问地址，如 `http://localhost` 或局域网 IP（须与浏览器访问一致）
5. **正式支付宝**：需在开放平台配置 `social.alipay.app-id` 与回调地址；完整换 token 需接入支付宝 SDK（当前未配置 AppId 时同样可用模拟登录）

## 后续扩展建议

- 微信/支付宝真实支付
- SKU 多规格、优惠券、会员积分
- uni-app 小程序独立打包

## 编码（防止乱码）

本仓库已强制 **UTF-8**（见 `.vscode/settings.json`、`.editorconfig`、`.gitattributes`）。请用 Cursor/VS Code 打开本文件夹后**重新加载窗口**一次。

| 措施 | 说明 |
|------|------|
| 编辑器 | `files.encoding: utf8`，关闭自动猜测编码 |
| AI 规则 | `.cursor/rules/utf8-encoding.mdc` 约束后续修改 |
| 一键检查/修复 | 见下方命令 |

```bash
# 检查是否还有 GBK/乱码文件（CI 可用）
python scripts/ensure_utf8_workspace.py --check

# 自动把 GBK 文本转为 UTF-8
python scripts/ensure_utf8_workspace.py

# C 端商城页面被改乱时，可再执行
python scripts/fix_shop_utf8_all.py
```

上线文档请打开 **`docs/deploy-from-zero.md`**（与 `docs/从零到上线指南.md` 内容相同，英文路径更不易乱码）。

## 参考

- 若依文档：https://doc.ruoyi.vip/
