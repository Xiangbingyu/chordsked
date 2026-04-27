import AccountPageContent from './components/AccountPageContent'
import OrgPageShell from '../shared/OrgPageShell'

function AccountPage() {
  return (
    <OrgPageShell
      pageKey="org-account"
      title="账号管理"
      description="账号管理属于账号与授权部分。当前列表直接调用教务端账号接口，最终展示结果以当前登录账号的数据权限范围和后端鉴权结果为准。"
    >
      <AccountPageContent />
    </OrgPageShell>
  )
}

export default AccountPage
