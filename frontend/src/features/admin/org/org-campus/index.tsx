import OrgPageShell from '../shared/OrgPageShell'

function CampusPage() {
  return (
    <OrgPageShell
      pageKey="org-campus"
      title="校区管理"
      description="校区管理属于组织管理部分。当前先保留页面入口和切换关系，后续在这里接入校区列表、创建校区和状态维护能力。"
    >
      <div style={{ fontSize: 14, color: '#6f6256', lineHeight: 1.8 }}>
        校区管理主内容预留区
      </div>
    </OrgPageShell>
  )
}

export default CampusPage
