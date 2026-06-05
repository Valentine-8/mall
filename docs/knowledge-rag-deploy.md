# 轻量 RAG 知识库 — 服务器部署指南

本文说明在 **2核4G 腾讯云 Lighthouse**（或其它已有 mall Docker 栈）上启用「MySQL 切块 + DashScope Embedding」轻量 RAG，**无需 Weaviate / 向量数据库**。

## 架构概览

```
用户提问 → 智能客服 API → 检索 Top-K 知识块（MySQL）
                              ↓
                    Embedding API（与 LLM 共用 Key/地址）
                              ↓
                    拼入 System Prompt → LLM 回复
```

| 组件 | 说明 |
|------|------|
| `mall_knowledge_doc` / `mall_knowledge_chunk` | 文档元数据 + 文本块 + embedding JSON |
| DashScope 兼容模式 | 同一 `ai_api_url` / `ai_api_key` 调用 `/embeddings` 与 `/chat/completions` |
| 检索 | 有向量则余弦相似度；无向量则关键词匹配 |

## 一、前置条件

1. 已部署 mall 后端 + MySQL + Redis + Nginx（见 [git-deploy.md](./git-deploy.md)）。
2. 若尚未部署客服，先执行 `sql/mall_chat.sql`。
3. 已开通 [阿里云 DashScope](https://dashscope.aliyun.com/)（或其它 OpenAI 兼容 Embedding 服务）。

## 二、数据库

在服务器 MySQL 中执行（容器内示例）：

```bash
docker exec -i mall-mysql mysql -uroot -p你的密码 ry-vue < sql/mall_knowledge.sql
```

或本机：

```bash
mysql -h 127.0.0.1 -P 3306 -u root -p ry-vue < sql/mall_knowledge.sql
```

**说明：**

- 脚本会 `ALTER TABLE mall_chat_config` 增加 RAG 字段；若列已存在会报错，可忽略对应行。
- 会创建 `mall_knowledge_*` 表及菜单 **3006 知识库**。

执行后 **重新登录后台** 或刷新权限，才能在侧栏看到「知识库」。

## 三、AI / Embedding 配置（后台）

路径：**商城管理 → 客服设置 → AI 配置**

| 字段 | 推荐值（DashScope） |
|------|---------------------|
| API 地址 | `https://dashscope.aliyuncs.com/compatible-mode/v1` |
| API Key | DashScope API Key |
| 对话模型 | `qwen-plus` 或 `qwen-turbo` |
| 启用知识库 | 开 |
| Embedding 模型 | `text-embedding-v3` |
| 检索 Top K | `3`（文档少时可 5） |
| 切块大小 / 重叠 | `500` / `80` |

上传文档：**商城管理 → 知识库**，支持 `txt` / `md` / `docx` / `pdf`，单文件 ≤10MB，单文档最多 200 块。

## 四、发布后端与前端

与常规发版相同（在项目根目录）：

```bash
# 后端
mvn clean package -DskipTests

# 前端
cd ruoyi-ui && npm run build:prod
```

上传到服务器（示例 IP `82.156.68.87`，按你的实际路径调整）：

```bash
scp ruoyi-admin/target/ruoyi-admin.jar root@82.156.68.87:/opt/mall/
scp -r ruoyi-ui/dist/* root@82.156.68.87:/opt/mall/nginx/html/
```

重启 API 与 Nginx：

```bash
ssh root@82.156.68.87
cd /opt/mall/deploy
docker compose restart mall-api nginx
```

## 五、服务器无需额外容器

轻量 RAG **不需要**：

- Weaviate / Milvus / Elasticsearch
- 新增 docker-compose 服务
- 单独 Embedding 微服务

仅需保证现有 `mall-api` 能访问外网调用 DashScope（HTTPS 443）。

## 六、磁盘与内存建议（2C4G）

| 项目 | 建议 |
|------|------|
| 知识库体量 | 文档总量 &lt; 50 篇、切块 &lt; 2000 较稳妥 |
| 上传目录 | 默认 `{ruoyi.profile}/knowledge`，Docker 需挂载 profile 卷（与现有上传一致） |
| JVM | 保持 `mall-api` 内存上限约 768MB；切块过多时检索会略增 CPU |
| MySQL | embedding 存 MEDIUMTEXT；2000 块约数十 MB，4G 机器可接受 |

若知识库继续增大，可考虑：提高切块大小减少块数、或后续升级为独立向量库（非本次范围）。

## 七、验证清单

1. 后台 **知识库** 菜单可见，上传一篇 `.md` 或 `.txt`，状态为 **已索引**，块数 &gt; 0。
2. **客服设置** 中 API 测试：C 端打开商城浮窗客服，问文档中的专属问题，应答应引用知识库内容。
3. 关闭「启用知识库」后，应答不再注入知识片段（仅系统提示词 + 模型能力）。
4. 故意填错 API Key：上传文档状态应为 **失败**，客服仍可用规则/LLM（无 RAG）。

## 八、常见问题

**Q: ALTER 报 Duplicate column？**  
A: 列已加过，跳过该 ALTER 即可。

**Q: 上传成功但索引失败？**  
A: 检查 API Key、Embedding 模型名；查看文档行的错误信息；服务器需能访问 Embedding 接口。

**Q: PDF 乱码或为空？**  
A: 扫描版 PDF 无文本层，需 OCR 或改为 docx/txt。

**Q: 和客服 SQL 执行顺序？**  
A: `mall.sql` → `mall_chat.sql` → `mall_knowledge.sql`。

## 九、相关文件

| 文件 | 说明 |
|------|------|
| `sql/mall_knowledge.sql` | RAG 表结构 + 菜单 |
| `sql/mall_chat.sql` | 客服表（含 RAG 字段的新装定义） |
| `ruoyi-ui/src/views/mall/knowledge/index.vue` | 知识库管理页 |
| `ruoyi-mall/.../MallKnowledge*.java` | 上传、切块、检索逻辑 |

完整部署流程仍见 [git-deploy.md](./git-deploy.md) 与 [从零到上线指南.md](./从零到上线指南.md)。
