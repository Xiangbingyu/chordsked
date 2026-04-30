import CampusPageContent from './components/CampusPageContent'
import OrgPageShell from '../shared/OrgPageShell'

function CampusPage() {
  return (
    <OrgPageShell
      pageKey="org-campus"
      title="组织管理"
      description="统一维护校区、部门、小组等组织节点。校区作为根节点维护，部门和小组通过父子关系构建完整组织结构。"
    >
      <CampusPageContent />
    </OrgPageShell>
  )
}

export default CampusPage
