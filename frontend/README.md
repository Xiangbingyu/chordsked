# ChordSked Frontend

React + Vite + TypeScript + Ant Design + Zustand 前端示例工程。

## 环境模式

- development：本地开发，默认代理后端 `http://localhost:8080`
- test：测试联调，默认代理后端 `http://localhost:8081`
- production：生产构建模式

对应配置文件：

- `.env.development`
- `.env.test`
- `.env.production`

关键变量：

- `VITE_APP_ENV` 当前环境标识
- `VITE_PORT` 前端本地端口
- `VITE_API_BASE_URL` Axios 基础路径
- `VITE_API_PROXY_TARGET` Vite 代理目标

## 启动与构建

```bash
npm run dev
npm run dev:test
npm run dev:prod

npm run build
npm run build:dev
npm run build:test

npm run preview
npm run preview:test
```
