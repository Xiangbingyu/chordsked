import OrgPageShell from '../shared/OrgPageShell'

function PermissionTreePage() {
  return (
    <OrgPageShell
      pageKey="org-permtree"
      title="权限树映射"
      description="权限树映射属于账号与授权部分。当前先保留页面入口和切换关系，后续在这里接入权限树展示、节点映射和角色授权联动能力。"
    >
      <div style={{ fontSize: 14, color: '#6f6256', lineHeight: 1.8 }}>
        权限树映射主内容预留区
      </div>
    </OrgPageShell>
  )
}

export default PermissionTreePage
