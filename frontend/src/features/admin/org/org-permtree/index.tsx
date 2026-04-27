import PermissionTreePageContent from './components/PermissionTreePageContent'
import OrgPageShell from '../shared/OrgPageShell'

function PermissionTreePage() {
  return (
    <OrgPageShell
      pageKey="org-permtree"
      title="权限树映射"
      description="权限树映射属于账号与授权部分。这里直接读取系统权限树，并以 Ant Design 风格展示节点层级、编码和基础元信息。"
    >
      <PermissionTreePageContent />
    </OrgPageShell>
  )
}

export default PermissionTreePage
