import OrgPageShell from '../shared/OrgPageShell'

function RolePermissionPage() {
  return (
    <OrgPageShell
      pageKey="org-rbac"
      title="角色与数据权限"
      description="角色与数据权限属于账号与授权部分。当前先保留页面入口和切换关系，后续在这里接入角色配置、数据范围设置和权限关联能力。"
    >
      <div style={{ fontSize: 14, color: '#6f6256', lineHeight: 1.8 }}>
        角色与数据权限主内容预留区
      </div>
    </OrgPageShell>
  )
}

export default RolePermissionPage
