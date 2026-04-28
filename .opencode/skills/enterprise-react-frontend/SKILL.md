---
name: enterprise-react-frontend
description: 前端开发规范 Skill。自动激活：当用户创建前端项目、开发 React/TypeScript 功能、编写组件、样式、API、使用 Ant Design、状态管理或任何前端相关任务时，始终优先使用此 skill。提供完整的前端开发规范、代码标准、项目结构、最佳实践参考。适配 React + Vite + Ant Design 5 + TypeScript + Zustand 技术栈。
license: MIT
compatibility: opencode
metadata:
  audience: developers
  workflow: frontend
  tech-stack: react, typescript, antd5, zustand, vite, javascript, css, scss
  auto-activate: true
  priority: high
---

## Frontend Development Skill

### 项目技术栈

- 框架: React 18.x
- UI库: Ant Design 5.x
- 状态管理: Zustand
- 语言: TypeScript 5.x
- 构建工具: Vite 5.x

---

## ROLE

你是资深前端工程师，负责确保所有生成的前端代码遵循企业级开发标准。

优先级：
1. 正确性
2. 可维护性
3. 性能
4. 一致性
5. 可访问性

---

## SKILL ACTIVATION

当任务描述包含以下任意关键词时，必须启用本 skill：

- React/TypeScript/前端/Frontend
- 组件/Component/页面/Page
- Ant Design/Antd
- 状态管理/Zustand/Store
- API/接口/服务
- Bug 修复/排查/重构
- 样式/CSS/SCSS

---

## DEVELOPMENT WORKFLOW

1. **先搜索** - 在项目中查找类似功能的实现
2. **再复用** - 优先使用已有的通用组件和 Hooks
3. **参考模式** - 按照项目现有的代码模式编写
4. **检查规范** - 确保符合 ESLint 和 TypeScript 规则

---

## DIRECTORY STRUCTURE

### 推荐目录结构 (Feature-based)

```
src/
├── assets/             # 静态资源
│   ├── images/        # 图片
│   └── icons/         # 图标
├── components/        # 通用组件 (可被多个feature复用)
│   ├── common/        # 基础组件
│   └── business/      # 业务组件
├── features/          # 功能模块 (按业务功能划分)
│   ├── feature-a/
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── services/
│   │   ├── types.ts
│   │   └── index.tsx
│   └── feature-b/
├── hooks/             # 全局自定义Hooks
├── layouts/           # 布局组件
├── pages/             # 页面入口 (路由对应)
├── routes/            # 路由配置
├── services/          # API服务 (全局)
├── stores/            # Zustand状态管理
│   ├── index.ts       # store入口
│   └── useXxxStore.ts # 状态定义
├── styles/            # 全局样式
├── types/             # 全局类型定义
├── utils/             # 工具函数
├── App.tsx            # 根组件
└── main.tsx           # 入口文件
```

### 页面开发模式

```
features/user/
├── components/      # 页面专用组件
│   ├── UserForm.tsx
│   └── UserTable.tsx
├── hooks/           # 页面专用Hooks
├── services/        # 页面API
├── types.ts         # 类型定义
├── UserList.tsx     # 列表页面
└── index.scss       # 样式
```

### 组件开发模式

```
components/Loading/
├── index.tsx        # 组件实现
├── index.scss      # 样式文件
└── type.ts         # 类型定义
```

---

## NAMING CONVENTIONS

### 文件命名规则

```bash
# 普通文件：小写+中划线
this-is-an-example.ts

# React组件/页面：大驼峰
ThisIsAnExample.tsx

# 组件目录
component-name/
├── index.tsx        # 组件实现
└── index.scss       # 样���文件

# 页面目录
PageName/
├── index.ts         # 页面入口
├── PageName.tsx     # 页面组件
├── http.ts          # 页面API
├── interface.ts     # 类型定义
└── drawer/          # 抽屉组件
```

---

## CODE STYLE

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

## REACT SPECIFICATION

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

### JSX 格式

```tsx
// good
<Foo
  longParam="bar"
  anotherParam="baz"
/>

{showButton && <Button />}

{condition ? <A /> : <B />}
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

## ANT DESIGN 5 SPECIFICATION

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

#### 全局Token

```tsx
<ConfigProvider
  theme={{
    token: {
      colorPrimary: '#1677ff',
      borderRadius: 6,
      fontSize: 14,
      colorBgContainer: '#ffffff',
    },
  }}
>
```

#### 组件Token

```tsx
<ConfigProvider
  theme={{
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

#### 亮色/暗色主题

```tsx
import { theme } from 'antd';

<ConfigProvider
  theme={{
    algorithm: theme.darkAlgorithm, // 暗色主题
  }}
>
```

### 5.x API 变��

- `visible` -> `open` (Modal, Drawer, Dropdown 等)
- `onVisibleChange` -> `onOpenChange`

```tsx
// Modal
<Modal open={visible} onOpenChange={setVisible} />

// Drawer
<Drawer open={open} onOpenChange={setOpen} />
```

---

## API SPECIFICATION

### axios 封装

```ts
// services/request.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
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
      } else {
        message.error(error.message || '请求失败');
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

### 环境变量

```ts
// .env.development
VITE_API_BASE_URL=/api
VITE_APP_TITLE=管理后台

// .env.production
VITE_API_BASE_URL=https://api.example.com
```

---

## STATE MANAGEMENT (ZUSTAND)

### 目录结构

```
stores/
├── index.ts       # store入口，导出所有store
├── useAppStore.ts # 应用级状态
└── useUserStore.ts # 用户状态
```

### 定义 Store

```ts
// stores/useAppStore.ts
import { create } from 'zustand';

interface AppState {
  collapsed: boolean;
  userInfo: Record<string, unknown>;
  setCollapsed: (collapsed: boolean) => void;
  setUserInfo: (userInfo: Record<string, unknown>) => void;
}

export const useAppStore = create<AppState>((set) => ({
  collapsed: false,
  userInfo: {},
  setCollapsed: (collapsed) => set({ collapsed }),
  setUserInfo: (userInfo) => set({ userInfo }),
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

### 异步操作

```ts
// stores/useUserStore.ts
import { create } from 'zustand';
import { fetchUserInfo } from '@/services/api';

interface UserState {
  userInfo: Record<string, unknown> | null;
  loading: boolean;
  fetchUser: () => Promise<void>;
}

export const useUserStore = create<UserState>((set) => ({
  userInfo: null,
  loading: false,
  fetchUser: async () => {
    set({ loading: true });
    try {
      const data = await fetchUserInfo();
      set({ userInfo: data, loading: false });
    } catch (error) {
      set({ loading: false });
    }
  },
}));
```

### 派生状态 (Selectors)

```tsx
// 基础选择器
const count = useStore((state) => state.count);

// 精细选择器 (避免不必要渲染)
const { name, age } = useStore((state) => ({
  name: state.name,
  age: state.age,
}));

// 使用 shallow 比较
import { shallow } from 'zustand/shallow';
const { name, age } = useStore(
  (state) => ({ name: state.name, age: state.age }),
  shallow
);
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
    {
      name: 'app-storage',
    }
  )
);
```

---

## COMMON COMPONENTS

项目在 `components/` 目录下封装了大量通用组件，避免重复造轮子。

### 表单组件

| 组件 | 说明 |
|------|------|
| `Form` | 通用表单组件，支持配置化生成表单 |
| `SearchForm` | 搜索表单组件 |

```tsx
import Form from 'components/form';
import SearchForm from 'components/searchForm';

const formList = [
  { label: '名称', key: 'name', type: 'input' },
  { label: '类型', key: 'type', type: 'select', options: [...] },
];
<Form formList={formList} onSubmit={handleSubmit} />
```

### 表格组件

| 组件 | 说明 |
|------|------|
| `Table` | 通用表格组件，支持列配置、分页、排序等 |
| `EditableTable` | 可编辑表格 |

### 弹窗组件

| 组件 | 说明 |
|------|------|
| `Modal` | 通用弹窗组件 |
| `Drawer` | 抽屉组件 |

### 按钮组件

| 组件 | 说明 |
|------|------|
| `DebounceButton` | 防抖按钮，防止重复点击 |

```tsx
import DebounceButton from 'components/debounceButton';

<DebounceButton type="primary" onClick={handleSubmit}>
  提交
</DebounceButton>
```

### 自定义Hooks

项目在 `hooks/` 目录下封装了常用 Hooks：

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
import useAutoHeight from 'hooks/useAutoHeight';

const MyComponent = () => {
  useWatermark();
  const height = useAutoHeight();
  return <div style={{ height }}>...</div>;
};
```

---

## CSS SPECIFICATION

### 命名规范

```css
/* 通用样式：小写+中划线 */
.page-container { }

/* CSS Module/组件样式：小驼峰 */
.pageContainer { }
```

### 文件位置

组件样式与组件同目录

```
components/
└── breadCrumb/
    ├── index.tsx
    └── index.scss
```

---

## QUALITY CHECKLIST

- [ ] ESLint无错误
- [ ] 表单验证完整
- [ ] 加载/空状态处理
- [ ] 无内存泄漏

---

## QUICK REFERENCE

### 代码复用优先级

1. **通用组件** - `components/` 下已有组件优先使用
2. **自定义Hooks** - `hooks/` 下的 Hooks
3. **工具函数** - `utils/` 下的工具方法
4. **现有页面** - 参考类似页面实现

### 快速命令

```bash
# 开发
npm run dev          # 启动开发服务器
npm run dev:test     # test环境
npm run dev:prod     # 生产环境

# 构建
npm run build        # 构建生产版本
npm run build:test  # 测试环境构建
npm run preview     # 预览构建结果

# 检查
npm run lint        # ESLint检查
npm run lint:fix    # ESLint修复
npm run typecheck   # TypeScript类型检查
```

### 任务类型与规范对应

| 任务类型 | 主要参考模块 |
|----------|----------|
| 开发新功能 | Code Style, React, Components, API, Antd5 |
| 代码重构 | Refactor, Guide, Code Style |
| Bug修复 | Guide, Code Style, React, Quality |
| 了解项目结构 | Tech Stack, Directory, Naming |
| 状态管理 | Zustand |
| 组件开发 | React, Components, CSS, Naming |
| API开发 | API, Commands |
| 代码审查 | Code Style, React, Quality, Convention |