import { Navigate, Route, Routes } from 'react-router-dom'
import { adminNavigation } from '../config/adminNavigation'
import AccountPage from '../features/admin/org/org-account'
import CampusPage from '../features/admin/org/org-campus'
import OrgTreePage from '../features/admin/org/org-tree'
import PermissionTreePage from '../features/admin/org/org-permtree'
import RolePermissionPage from '../features/admin/org/org-rbac'
import WorkbenchPage from '../features/admin/workbench/pages/WorkbenchPage'
import AdminLayout from '../layouts/AdminLayout'
import LoginPage from '../pages/LoginPage'

function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<Navigate to="/admin/workbench" replace />} />
        <Route path="workbench" element={<WorkbenchPage />} />
        <Route path="org-campus" element={<CampusPage />} />
        <Route path="org-tree" element={<OrgTreePage />} />
        <Route path="org-account" element={<AccountPage />} />
        <Route path="org-rbac" element={<RolePermissionPage />} />
        <Route path="org-permtree" element={<PermissionTreePage />} />
      </Route>
      <Route path="/dashboard" element={<Navigate to="/admin/workbench" replace />} />
      {adminNavigation.groups.flatMap((group) =>
        group.children.map((item) => (
          <Route
            key={`legacy-${item.key}`}
            path={`/dashboard/${item.path}`}
            element={<Navigate to={item.href} replace />}
          />
        )),
      )}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default AppRouter

