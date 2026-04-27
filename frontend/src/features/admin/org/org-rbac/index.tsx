import RolePermissionPageContent from './components/RolePermissionPageContent'
import OrgPageShell from '../shared/OrgPageShell'

function RolePermissionPage() {
  return (
    <OrgPageShell
      pageKey="org-rbac"
      title="角色与数据权限"
      description="角色与数据权限属于账号与授权部分。当前页面展示角色列表、角色状态、权限绑定规模和关联用户规模，后续在此接入角色权限点编辑能力。"
    >
      <RolePermissionPageContent />
    </OrgPageShell>
  )
}

export default RolePermissionPage
