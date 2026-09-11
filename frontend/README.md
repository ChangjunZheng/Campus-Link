# Campus-Link Frontend

Campus-Link（重庆工程学院计算机专业学生交流论坛）Web 前端。
技术栈：Vue 3 + Vite + TypeScript + Pinia + Vue Router + Element Plus（UI 稿为设计评审门遗留行动项，先行用组件库拼页面，见行动清单 P1）。

## 本地启动

前置：Node 18+（本机 Node 24 已验证）。

```bash
npm install
npm run dev        # http://localhost:5173，/api 代理到 http://localhost:8080
```

需要先启动 backend（见 backend/README.md），注册链路依赖学籍核验 + 验证码接口。

## Sprint 1 已实现

- 应用骨架：顶栏导航（6 版块入口 / 搜索占位 / 通知角标占位 / 用户菜单）
- **注册 / 登录页**：学籍核验（学号 + 姓名）→ 邮箱验证码 → 注册；验证码登录
- 版块页与首页占位（内容链路 Sprint 2）
- 统一 API 客户端（`{code,message,data,traceId}` 响应 + JWT 头注入）

## 目录约定

```
src/
├── api/         # 接口封装（client.ts 统一响应处理）
├── components/  # 通用组件（Placeholder 等）
├── router/      # 路由与登录守卫
├── stores/      # Pinia（auth）
└── views/       # 页面（PRD 5.1 页面清单）
```

## 常用命令

```bash
npm run dev      # 开发
npm run build    # 构建产物到 dist/
npm run preview  # 预览构建产物
```
