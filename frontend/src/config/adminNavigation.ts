export type AdminNavigationLinkItem = {
  key: string
  label: string
  path: string
  href: string
}

export type AdminNavigationGroupChildItem = {
  key: string
  label: string
  path: string
  href: string
}

export type AdminNavigationGroupCategoryItem = {
  key: string
  label: string
  children: AdminNavigationGroupChildItem[]
}

export type AdminNavigationGroupItem = {
  key: string
  label: string
  children: AdminNavigationGroupChildItem[]
  categories: AdminNavigationGroupCategoryItem[]
}

export const adminNavigation = {
  title: 'ChordSked Demo',
  subtitle: '教务端',
  headerTitle: '教务端',
  headerSubtitle: '排课 / 转化 / 审批 / 权限',
  links: [
    { key: 'workbench', label: '工作台', path: 'workbench', href: '/admin/workbench' },
  ] satisfies AdminNavigationLinkItem[],
  groups: [
    {
      key: 'org',
      label: '组织与权限',
      children: [
        { key: 'org-campus', label: '组织管理', path: 'org-campus', href: '/admin/org-campus' },
        { key: 'org-tree', label: '组织树管理', path: 'org-tree', href: '/admin/org-tree' },
        { key: 'org-account', label: '账号管理', path: 'org-account', href: '/admin/org-account' },
        {
          key: 'org-rbac',
          label: '角色与数据权限',
          path: 'org-rbac',
          href: '/admin/org-rbac',
        },
        {
          key: 'org-permtree',
          label: '权限树映射',
          path: 'org-permtree',
          href: '/admin/org-permtree',
        },
      ] satisfies AdminNavigationGroupChildItem[],
      categories: [
        {
          key: 'org-manage',
          label: '组织管理',
          children: [
              { key: 'org-campus', label: '组织管理', path: 'org-campus', href: '/admin/org-campus' },
            { key: 'org-tree', label: '组织树管理', path: 'org-tree', href: '/admin/org-tree' },
          ],
        },
        {
          key: 'auth-manage',
          label: '账号与授权',
          children: [
            { key: 'org-account', label: '账号管理', path: 'org-account', href: '/admin/org-account' },
            {
              key: 'org-rbac',
              label: '角色与数据权限',
              path: 'org-rbac',
              href: '/admin/org-rbac',
            },
            {
              key: 'org-permtree',
              label: '权限树映射',
              path: 'org-permtree',
              href: '/admin/org-permtree',
            },
          ],
        },
      ] satisfies AdminNavigationGroupCategoryItem[],
    },
  ] satisfies AdminNavigationGroupItem[],
}
