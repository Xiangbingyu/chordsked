---
name: enterprise-react-frontend
description: 前端开发规范 Skill。自动激活：当用户创建前端项目、开发 React/TypeScript 功能、编写组件、样式、API、使用 Ant Design、状态管理或任何前端相关任务时，始终优先使用此 skill。
TRIGGER when: code file ends with .tsx, .ts, .jsx, .js, .scss, .css and involves React, TypeScript, Ant Design, Zustand, Vite, or user asks for frontend development, component creation, UI implementation
SKIP: file ends with .java, .py, .go, .rs, or user explicitly requests backend or other non-frontend development
---

# Enterprise React Frontend Skill

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | React 18.x |
| UI库 | Ant Design 5.x |
| 状态管理 | Zustand |
| 路由 | React Router v6 |
| 构建 | Vite 5.x |
| 语言 | TypeScript 5.x |
| 懒加载 | React.lazy + Suspense |
| 包管理器 | pnpm / npm |
| Node版本 | 18.x+ / 20.x |

主要特性：

- React 18: 并发渲染、Suspense、useTransition
- Ant Design 5: CSS-in-JS、Design Token、组件重写
- Zustand: 轻量级状态管理、简洁API
- Vite: 快速HMR、按需构建

---

## 目录结构 (Feature-based)

```
src/
├── assets/             # 静态资源
├── components/         # 通用组件 (可被多个feature复用)
│   ├── common/         # 基础组件
│   └── business/       # 业务组件
├── features/           # 功能模块 (按业务功能划分)
│   └── feature-a/
│       ├── components/
│       ├── hooks/
│       ├── services/
│       ├── types.ts
│       └── index.tsx
├── hooks/              # 全局自定义Hooks
├── layouts/            # 布局组件
├── pages/              # 页面入口 (路由对应)
├── routes/             # 路由配置
├── services/           # API服务 (全局)
├── stores/             # Zustand状态管理
├── styles/             # 全局样式
├── types/              # 全局类型定义
├── utils/              # 工具函数
├── App.tsx             # 根组件
└── main.tsx            # 入口文件
```

### 页面开发模式

```
features/user/
├── components/         # 页面专用组件
│   ├── UserForm.tsx
│   └── UserTable.tsx
├── hooks/              # 页面专用Hooks
├── services/           # 页面API
├── types.ts            # 类型定义
├── UserList.tsx        # 列表页面
└── index.scss          # 样式
```

### 组件开发模式

```
components/Loading/
├── index.tsx           # 组件实现
├── index.scss          # 样式文件
└── type.ts             # 类型定义
```

---

## 开发流程

1. **先搜索** - 在项目中查找类似功能的实现
2. **再复用** - 优先使用已有的通用组件和 Hooks
3. **参考模式** - 按照项目现有的代码模式编写
4. **检查规范** - 确保符合 ESLint 和 TypeScript 规则

### 代码复用优先级

1. **通用组件** - `components/` 下已有组件优先使用
2. **自定义Hooks** - `hooks/` 下的 Hooks
3. **工具函数** - `utils/` 下的工具方法
4. **现有页面** - 参考类似页面实现

---

## 文件命名规范

```bash
# 普通文件：小写+中划线
this-is-an-example.ts

# React组件/页面：大驼峰
ThisIsAnExample.tsx

# 组件目录
component-name/
├── index.tsx           # 组件实现
└── index.scss          # 样式文件

# 页面目录
PageName/
├── index.ts            # 页面入口
├── PageName.tsx        # 页面组件
├── http.ts             # 页面API
├── interface.ts        # 类型定义
└── drawer/             # 抽屉组件
```

---

## 代码规范

### 格式规范

- 缩进：2空格
- 引号：JS单引号，JSX双引号
- 分号：行尾添加
- 行尾逗号

### 变量声明

```js
// good
const items = getItems();
const goSportsTeam = true;
let dragonball;
```

### 函数规范

- 匿名函数使用箭头函数
- 箭头函数参数加括号
- 异步函数使用 async/await

```js
// good
const fetchData = async () => {
  const res = await request.get('/api/data');
  return res;
};

[1, 2, 3].map((x) => x * 2);
```

### 模块导入

```js
// good
import { useState, useEffect } from 'react';
import { Button, Table } from 'antd';
import isEmpty from 'lodash/isEmpty';
import $http from './http';
```

---

## React/TSX规范

### 组件规范

优先使用函数组件 + Hooks

```tsx
import { useState, useEffect, useMemo, useCallback } from 'react';
import './index.scss';

interface Props {
  title: string;
  options?: Record<string, unknown>;
}

const MyComponent = ({ title, options = {} }: Props) => {
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchData();
    return () => {
      // cleanup
    };
  }, []);

  const value = useMemo(() => {
    return computeValue();
  }, [dependencies]);

  const handleClick = useCallback(() => {
    // ...
  }, [dependencies]);

  return <div>{title}</div>;
};

export default MyComponent;
```

### React 18 新特性

#### Suspense + 懒加载

```tsx
import { lazy, Suspense } from 'react';

const LazyComponent = lazy(() => import('./LazyComponent'));

const App = () => (
  <Suspense fallback={<Spin />}>
    <LazyComponent />
  </Suspense>
);
```

#### useTransition

```tsx
import { useTransition, useState } from 'react';

const [isPending, startTransition] = useTransition();
const [filter, setFilter] = useState('');

const handleChange = (value: string) => {
  startTransition(() => {
    setFilter(value);
  });
};
```

### 自定义Hooks

使用 useXxx 命名

```ts
// hooks/useWatermark.ts
const useWatermark = (text: string) => {
  // implementation
  return { setWatermark, removeWatermark };
};
export default useWatermark;
```

### Props 规范

```tsx
// good
<Foo
  userName="hello"
  hidden
/>

{items.map((item) => (
  <Item key={item.id} {...item} />
))}
```

---

## Ant Design 5.x 使用规范

### 快速开始

```tsx
import { Button, ConfigProvider } from 'antd';

const App = () => (
  <ConfigProvider
    theme={{
      token: {
        colorPrimary: '#1677ff',
        borderRadius: 6,
      },
    }}
  >
    <Button type="primary">Primary</Button>
  </ConfigProvider>
);
```

### Design Token

```tsx
<ConfigProvider
  theme={{
    token: {
      colorPrimary: '#1677ff',
      borderRadius: 6,
      fontSize: 14,
    },
    components: {
      Button: {
        primaryShadow: '0 2px 4px rgba(0,0,0,0.1)',
      },
      Table: {
        headerBg: '#f6f7f8',
      },
    },
  }}
>
```

### 主题切换

```tsx
import { theme } from 'antd';

<ConfigProvider
  theme={{
    algorithm: theme.darkAlgorithm, // 暗色主题
  }}
>
```

### 5.x 变化

```tsx
// visible -> open (Modal, Drawer, Dropdown 等)
<Modal open={visible} onOpenChange={setVisible} />
<Drawer open={open} onOpenChange={setOpen} />
```

---

## 通用组件封装

### 表单组件

| 组件 | 说明 |
|------|------|
| `Form` | 通用表单组件，支持配置化生成表单 |
| `SearchForm` | 搜索表单组件 |

```tsx
import Form from 'components/form';

const formList = [
  { label: '名称', key: 'name', type: 'input' },
  { label: '类型', key: 'type', type: 'select', options: [...] },
];
<Form formList={formList} onSubmit={handleSubmit} />
```

### 表格组件

| 组件 | 说明 |
|------|------|
| `Table` | 通用表格组件 |
| `EditableTable` | 可编辑表格 |

### 弹窗组件

| 组件 | 说明 |
|------|------|
| `Modal` | 通用弹窗组件 |
| `Drawer` | 抽屉组件 |

### 自定义Hooks

| Hook | 说明 |
|------|------|
| `useWatermark` | 水印功能 |
| `useNetwork` | 网络状态监控 |
| `useCopy` | 复制功能 |
| `useAutoHeight` | 自动高度 |
| `useInterval` | 定时器 |
| `useDebounce` | 防抖 |
| `useThrottle` | 节流 |

```tsx
import useWatermark from 'hooks/useWatermark';

const MyComponent = () => {
  useWatermark();
  const height = useAutoHeight();
  return <div style={{ height }}>...</div>;
};
```

---

## API请求

### axios 封装

```ts
// services/request.ts
import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { message } from 'antd';

const createRequest = () => {
  const instance: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 10000,
  });

  instance.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });

  instance.interceptors.response.use(
    (response: AxiosResponse) => response.data,
    (error) => {
      const status = error.response?.status;
      if (status === 401) {
        message.error('登录已过期，请重新登录');
      }
      return Promise.reject(error);
    }
  );

  return instance;
};

export const request = createRequest();
```

### 页面API封装

```ts
// features/user/services/api.ts
import { request } from '@/services/request';

export interface UserInfo {
  id: string;
  name: string;
}

export const getUserInfo = (params: { id: string }) => 
  request.get<UserInfo>('/api/user/info', { params });

export const updateUserInfo = (data: Partial<UserInfo>) => 
  request.post('/api/user/update', data);
```

---

## 状态管理 (Zustand)

### 定义 Store

```ts
// stores/useAppStore.ts
import { create } from 'zustand';

interface AppState {
  collapsed: boolean;
  setCollapsed: (collapsed: boolean) => void;
}

export const useAppStore = create<AppState>((set) => ({
  collapsed: false,
  setCollapsed: (collapsed) => set({ collapsed }),
}));
```

### 组件中使用

```tsx
import { useAppStore } from '@/stores/useAppStore';

const MyComponent = () => {
  const { collapsed, setCollapsed } = useAppStore();
  return (
    <button onClick={() => setCollapsed(!collapsed)}>
      {collapsed ? '展开' : '收起'}
    </button>
  );
};
```

### 持久化

```ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useStore = create(
  persist(
    (set) => ({
      theme: 'light',
      setTheme: (theme: string) => set({ theme }),
    }),
    { name: 'app-storage' }
  )
);
```

---

## Vite 配置

### 基础配置

```ts
// vite.config.ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { resolve } from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:3000',
        changeOrigin: true,
      },
    },
  },
});
```

### 环境变量

```ts
// .env.development
VITE_API_BASE_URL=/api

// .env.production
VITE_API_BASE_URL=https://api.example.com

// 使用
import.meta.env.VITE_API_BASE_URL
```

---

## 命令参考

### 开发环境

```bash
npm run dev              # 启动开发服务器
npm run dev:test         # test环境开发
npm run dev:prod         # 生产环境开发
```

### 构建

```bash
npm run build            # 构建生产版本
npm run build:test       # 测试环境构建
npm run preview          # 预览构建结果
```

### 代码检查

```bash
npm run lint             # ESLint检查
npm run lint:fix         # ESLint自动修复
npm run typecheck        # TypeScript类型检查
```

---

## 项目约定

### 交互规范

- 异步操作添加 loading 状态
- 用户交互添加防抖
- 字符串输入需 trim
- 页面使用懒加载

### 错误处理

- 可预知错误：提示原因和解决方案
- 不可预知错误：收集错误信息

### 分辨率

- PC端：1200px
- 移动端：750px

---

## 重构指南

### 核心原则

1. **小步快跑** - 每次只修改一个文件或一个模块
2. **保持功能不变** - 重构不改变现有功能
3. **及时测试** - 每一步修改后都要验证
4. **提交记录** - 每次重构单独提交

### 重构步骤

```
1. 识别目标    → 确定要重构的代码和原因
2. 制定计划    → 拆分为多个小步骤
3. 逐步实施    → 每次只改一处
4. 验证功能    → 确保功能正常
5. 提交代码    → 记录重构过程
```

### 禁止的重构方式

```bash
# 错误示例
- 批量重命名多个文件
- 一次性迁移整个模块
- 同时修改多个不相关的组件

# 正确示例
- 第一步：只修改组件A的命名
- 第二步：只修改组件A的引用
- 第三步：验证组件A功能正常
- 第四步：提交代码
```

---

## 质量检查清单

- [ ] ESLint无错误
- [ ] 表单验证完整
- [ ] 加载/空状态处理
- [ ] 无内存泄漏
- [ ] 组件使用 open 替代 visible
- [ ] 使用 TypeScript 类型完整
