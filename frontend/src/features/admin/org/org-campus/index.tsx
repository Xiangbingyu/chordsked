import CampusPageContent from './components/CampusPageContent'
import OrgPageShell from '../shared/OrgPageShell'

function CampusPage() {
  return (
    <OrgPageShell
      pageKey="org-campus"
      title="校区管理"
      description="校区管理属于组织管理部分。这里可直接查看校区列表、创建校区并维护校区基础信息与状态。"
    >
      <CampusPageContent />
    </OrgPageShell>
  )
}

export default CampusPage
