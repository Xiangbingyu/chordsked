import OrgPageShell from '../shared/OrgPageShell'

function OrgTreePage() {
  return (
    <OrgPageShell
      pageKey="org-tree"
      title="组织树管理"
      description="组织树管理属于组织管理部分。当前先保留页面入口和切换关系，后续在这里接入组织树节点维护、层级展示和账号挂载能力。"
    >
      <div style={{ fontSize: 14, color: '#6f6256', lineHeight: 1.8 }}>
        组织树管理主内容预留区
      </div>
    </OrgPageShell>
  )
}

export default OrgTreePage
