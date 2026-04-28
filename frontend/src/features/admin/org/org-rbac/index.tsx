import RolePermissionPageContent from './components/RolePermissionPageContent'
import OrgPageShell from '../shared/OrgPageShell'

function RolePermissionPage() {
  return (
    <OrgPageShell
      pageKey="org-rbac"
      title="角色与数据权限"
      description="角色与数据权限属于账号与授权部分。当前页面已支持角色列表展示、创建角色、详情加载与修改提交；删除角色能力正在页面操作链路中接入。"
    >
      <RolePermissionPageContent />
    </OrgPageShell>
  )
}

export default RolePermissionPage
