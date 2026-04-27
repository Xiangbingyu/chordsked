import { Alert } from 'antd'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { listInternalUserCampusOptions } from '../../../../../services/campusService'
import {
  createInternalUser,
  getInternalUserDetail,
  listInternalUsers,
  resetInternalUserPassword,
  updateInternalUser,
  updateInternalUserStatus,
} from '../../../../../services/internalUserService'
import { listRoles } from '../../../../../services/roleService'
import type { InternalUserCampusOptionVO } from '../../../../../types/campus'
import type {
  InternalUserQueryResultVO,
  InternalUserStatus,
  UserDataScopeType,
} from '../../../../../types/internalUser'
import type { RoleQueryResultVO } from '../../../../../types/role'
import AccountCreateModal from './AccountCreateModal'
import AccountEditModal from './AccountEditModal'
import AccountOverview from './AccountOverview'
import AccountResetPasswordModal from './AccountResetPasswordModal'
import AccountTableCard from './AccountTableCard'
import AccountVisibilityPreview from './AccountVisibilityPreview'
import {
  DATA_SCOPE_OPTIONS,
  PAGE_SIZE,
  PHONE_PATTERN,
  SYSTEM_ADMIN_ROLE_CODE,
  TABLE_LOADING_OVERLAY_DELAY_MS,
  USERNAME_PATTERN,
  type AccountCreateFormState,
  type AccountEditFormState,
  getAccountOperationBlockReason,
  isForbiddenError,
  resolveApiErrorMessage,
} from '../utils/accountPageShared'

function AccountPageContent() {
  const [loading, setLoading] = useState(true)
  const [showTableLoadingOverlay, setShowTableLoadingOverlay] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [permissionDenied, setPermissionDenied] = useState(false)
  const [page, setPage] = useState(1)
  const [total, setTotal] = useState(0)
  const [rows, setRows] = useState<InternalUserQueryResultVO[]>([])
  const [roles, setRoles] = useState<RoleQueryResultVO[]>([])
  const [actionLoadingKey, setActionLoadingKey] = useState<string | null>(null)
  const [actionMessage, setActionMessage] = useState('')
  const [actionErrorMessage, setActionErrorMessage] = useState('')
  const [createModalOpen, setCreateModalOpen] = useState(false)
  const [createLoading, setCreateLoading] = useState(false)
  const [createSubmitting, setCreateSubmitting] = useState(false)
  const [createErrorMessage, setCreateErrorMessage] = useState('')
  const [createForm, setCreateForm] = useState<AccountCreateFormState | null>(null)
  const [editModalOpen, setEditModalOpen] = useState(false)
  const [editLoading, setEditLoading] = useState(false)
  const [editSubmitting, setEditSubmitting] = useState(false)
  const [editErrorMessage, setEditErrorMessage] = useState('')
  const [editTargetUsername, setEditTargetUsername] = useState('')
  const [editForm, setEditForm] = useState<AccountEditFormState | null>(null)
  const [visibilityPreviewRefreshKey, setVisibilityPreviewRefreshKey] = useState(0)
  const [campusOptions, setCampusOptions] = useState<InternalUserCampusOptionVO[]>([])
  const [resetPasswordModalOpen, setResetPasswordModalOpen] = useState(false)
  const [resetPasswordReason, setResetPasswordReason] = useState('管理员手动重置')
  const [resetTarget, setResetTarget] = useState<InternalUserQueryResultVO | null>(null)
  const [resetErrorMessage, setResetErrorMessage] = useState('')

  const loadAccounts = useCallback(async () => {
    setLoading(true)
    setErrorMessage('')
    setPermissionDenied(false)
    try {
      const [internalUserResult, roleResult] = await Promise.allSettled([
        listInternalUsers({ page, pageSize: PAGE_SIZE }),
        listRoles({ page: 1, pageSize: 100 }),
      ])

      if (internalUserResult.status !== 'fulfilled') {
        throw internalUserResult.reason
      }

      if (internalUserResult.value.code !== 0) {
        throw new Error(internalUserResult.value.message || '加载教务账号列表失败')
      }

      const items = internalUserResult.value.data.items || []
      setRows(items)
      setTotal(internalUserResult.value.data.total || 0)

      if (roleResult.status === 'fulfilled' && roleResult.value.code === 0) {
        setRoles(roleResult.value.data.items || [])
      } else {
        setRoles([])
      }
    } catch (error) {
      setRows([])
      setTotal(0)
      setRoles([])
      setPermissionDenied(isForbiddenError(error))
      setErrorMessage(
        resolveApiErrorMessage(error, '加载教务账号列表失败', 'list'),
      )
    } finally {
      setLoading(false)
    }
  }, [page])

  useEffect(() => {
    void loadAccounts()
  }, [loadAccounts])

  useEffect(() => {
    if (!loading) {
      setShowTableLoadingOverlay(false)
      return
    }

    const timer = window.setTimeout(() => {
      setShowTableLoadingOverlay(true)
    }, TABLE_LOADING_OVERLAY_DELAY_MS)

    return () => {
      window.clearTimeout(timer)
    }
  }, [loading])

  const availablePrimaryCampuses = useMemo(() => {
    if (!editForm) {
      return []
    }

    return campusOptions.filter((option) => editForm.campusIds.includes(option.id))
  }, [campusOptions, editForm])

  const availableCreatePrimaryCampuses = useMemo(() => {
    if (!createForm) {
      return []
    }

    return campusOptions.filter((option) => createForm.campusIds.includes(option.id))
  }, [campusOptions, createForm])

  const campusSelectOptions = useMemo(
    () => campusOptions.map((option) => ({ label: option.name, value: option.id })),
    [campusOptions],
  )

  const roleSelectOptions = useMemo(
    () =>
      roles
        .filter((role) => role.code !== SYSTEM_ADMIN_ROLE_CODE)
        .map((role) => ({ label: role.name, value: role.id })),
    [roles],
  )

  const dataScopeSelectOptions = useMemo(
    () => DATA_SCOPE_OPTIONS.map((option) => ({ label: option.label, value: option.value })),
    [],
  )

  const updateEditForm = useCallback(
    (updater: (current: AccountEditFormState) => AccountEditFormState) => {
      setEditErrorMessage('')
      setEditForm((current) => (current ? updater(current) : current))
    },
    [],
  )

  const updateCreateForm = useCallback(
    (updater: (current: AccountCreateFormState) => AccountCreateFormState) => {
      setCreateErrorMessage('')
      setCreateForm((current) => (current ? updater(current) : current))
    },
    [],
  )

  const closeCreateModal = useCallback(() => {
    if (createLoading || createSubmitting) {
      return
    }

    setCreateModalOpen(false)
    setCreateErrorMessage('')
    setCreateForm(null)
  }, [createLoading, createSubmitting])

  const closeEditModal = useCallback(() => {
    if (editLoading || editSubmitting) {
      return
    }

    setEditModalOpen(false)
    setEditErrorMessage('')
    setEditTargetUsername('')
    setEditForm(null)
  }, [editLoading, editSubmitting])

  const closeResetPasswordModal = useCallback(
    (forceClose = false) => {
      if (!forceClose && actionLoadingKey === `${resetTarget?.id}:reset-password`) {
        return
      }

      setResetPasswordModalOpen(false)
      setResetPasswordReason('管理员手动重置')
      setResetTarget(null)
      setResetErrorMessage('')
    },
    [actionLoadingKey, resetTarget],
  )

  const handleOpenCreateModal = async () => {
    setCreateModalOpen(true)
    setCreateLoading(true)
    setCreateSubmitting(false)
    setCreateErrorMessage('')
    setCreateForm(null)
    setActionMessage('')
    setActionErrorMessage('')

    try {
      const [campusResult, roleResult] = await Promise.all([
        listInternalUserCampusOptions(),
        roles.length > 0 ? Promise.resolve(null) : listRoles({ page: 1, pageSize: 100 }),
      ])
      if (campusResult.code !== 0) {
        throw new Error(campusResult.message || '加载校区选项失败')
      }
      if (roleResult && roleResult.code !== 0) {
        throw new Error(roleResult.message || '加载角色选项失败')
      }

      const nextRoles = roleResult?.data.items || roles
      const assignableRoles = nextRoles.filter((role) => role.code !== SYSTEM_ADMIN_ROLE_CODE)
      if (assignableRoles.length === 0) {
        throw new Error('当前没有可选角色，无法创建账号')
      }

      setCampusOptions(campusResult.data || [])
      if (roleResult) {
        setRoles(roleResult.data.items || [])
      }
      setCreateForm({
        username: '',
        name: '',
        avatar: '',
        phone: '',
        campusIds: [],
        primaryCampusId: null,
        roleIds: [],
        dataScopeType: null,
      })
    } catch (error) {
      setCreateErrorMessage(
        resolveApiErrorMessage(error, '加载创建账号信息失败', 'createLoad'),
      )
    } finally {
      setCreateLoading(false)
    }
  }

  const handleUpdateUser = async (row: InternalUserQueryResultVO) => {
    const blockedReason = getAccountOperationBlockReason(row)
    if (blockedReason) {
      setActionErrorMessage(blockedReason)
      setActionMessage('')
      return
    }

    const loadingKey = `${row.id}:update`
    setActionLoadingKey(loadingKey)
    setActionMessage('')
    setActionErrorMessage('')
    setEditModalOpen(true)
    setEditLoading(true)
    setEditSubmitting(false)
    setEditErrorMessage('')
    setEditTargetUsername(row.username)
    setEditForm(null)

    try {
      const [detailResult, campusResult, roleResult] = await Promise.all([
        getInternalUserDetail(row.id),
        listInternalUserCampusOptions(),
        roles.length > 0 ? Promise.resolve(null) : listRoles({ page: 1, pageSize: 100 }),
      ])
      if (detailResult.code !== 0) {
        throw new Error(detailResult.message || `加载账号 ${row.username} 详情失败`)
      }
      if (campusResult.code !== 0) {
        throw new Error(campusResult.message || '加载校区选项失败')
      }
      if (roleResult && roleResult.code !== 0) {
        throw new Error(roleResult.message || '加载角色选项失败')
      }

      const detail = detailResult.data
      if (
        detail.primaryCampusId === null ||
        detail.dataScopeType === null ||
        detail.campusIds.length === 0
      ) {
        throw new Error('当前账号缺少校区或数据范围配置，无法发起修改')
      }

      const nextRoles = roleResult?.data.items || roles
      const assignableRoles = nextRoles.filter((role) => role.code !== SYSTEM_ADMIN_ROLE_CODE)
      if (assignableRoles.length === 0) {
        throw new Error('当前没有可选角色，无法发起修改')
      }

      setCampusOptions(campusResult.data || [])
      if (roleResult) {
        setRoles(roleResult.data.items || [])
      }
      setEditForm({
        userId: row.id,
        username: row.username,
        name: detail.name,
        avatar: detail.avatar || '',
        phone: detail.phone,
        campusIds: detail.campusIds,
        primaryCampusId: detail.primaryCampusId,
        roleIds: detail.roleIds,
        dataScopeType: detail.dataScopeType,
      })
    } catch (error) {
      setEditErrorMessage(
        resolveApiErrorMessage(
          error,
          `加载账号 ${row.username} 编辑信息失败`,
          'editLoad',
        ),
      )
    } finally {
      setEditLoading(false)
      setActionLoadingKey(null)
    }
  }

  const handleCreateCampusChange = useCallback((nextCampusIds: number[]) => {
    setCreateErrorMessage('')
    setCreateForm((current) => {
      if (!current) {
        return current
      }

      const primaryCampusId = current.primaryCampusId
      return {
        ...current,
        campusIds: nextCampusIds,
        primaryCampusId:
          primaryCampusId !== null && nextCampusIds.includes(primaryCampusId)
            ? primaryCampusId
            : null,
      }
    })
  }, [])

  const handleCreateUsernameChange = useCallback((value: string) => {
    updateCreateForm((current) => ({
      ...current,
      username: value,
    }))
  }, [updateCreateForm])

  const handleCreateNameChange = useCallback((value: string) => {
    updateCreateForm((current) => ({
      ...current,
      name: value,
    }))
  }, [updateCreateForm])

  const handleCreatePhoneChange = useCallback((value: string) => {
    updateCreateForm((current) => ({
      ...current,
      phone: value,
    }))
  }, [updateCreateForm])

  const handleCreateAvatarChange = useCallback((value: string) => {
    updateCreateForm((current) => ({
      ...current,
      avatar: value,
    }))
  }, [updateCreateForm])

  const handleCreatePrimaryCampusChange = useCallback((value: number | null) => {
    updateCreateForm((current) => ({
      ...current,
      primaryCampusId: value,
    }))
  }, [updateCreateForm])

  const handleCreateDataScopeTypeChange = useCallback((value: UserDataScopeType | null) => {
    updateCreateForm((current) => ({
      ...current,
      dataScopeType: value,
    }))
  }, [updateCreateForm])

  const handleCreateRoleIdsChange = useCallback((values: number[]) => {
    updateCreateForm((current) => ({
      ...current,
      roleIds: values,
    }))
  }, [updateCreateForm])

  const handleEditCampusChange = (nextCampusIds: number[]) => {
    setEditErrorMessage('')
    setEditForm((current) => {
      if (!current) {
        return current
      }

      const primaryCampusId = current.primaryCampusId
      return {
        ...current,
        campusIds: nextCampusIds,
        primaryCampusId:
          primaryCampusId !== null && nextCampusIds.includes(primaryCampusId)
            ? primaryCampusId
            : null,
      }
    })
  }

  const handleEditNameChange = useCallback((value: string) => {
    updateEditForm((current) => ({
      ...current,
      name: value,
    }))
  }, [updateEditForm])

  const handleEditPhoneChange = useCallback((value: string) => {
    updateEditForm((current) => ({
      ...current,
      phone: value,
    }))
  }, [updateEditForm])

  const handleEditAvatarChange = useCallback((value: string) => {
    updateEditForm((current) => ({
      ...current,
      avatar: value,
    }))
  }, [updateEditForm])

  const handleEditPrimaryCampusChange = useCallback((value: number | null) => {
    updateEditForm((current) => ({
      ...current,
      primaryCampusId: value,
    }))
  }, [updateEditForm])

  const handleEditDataScopeTypeChange = useCallback((value: UserDataScopeType | null) => {
    updateEditForm((current) => ({
      ...current,
      dataScopeType: value,
    }))
  }, [updateEditForm])

  const handleEditRoleIdsChange = useCallback((values: number[]) => {
    updateEditForm((current) => ({
      ...current,
      roleIds: values,
    }))
  }, [updateEditForm])

  const handleOpenResetPasswordModal = (row: InternalUserQueryResultVO) => {
    const blockedReason = getAccountOperationBlockReason(row)
    if (blockedReason) {
      setActionErrorMessage(blockedReason)
      setActionMessage('')
      return
    }

    setResetTarget(row)
    setResetPasswordReason('管理员手动重置')
    setResetErrorMessage('')
    setActionMessage('')
    setActionErrorMessage('')
    setResetPasswordModalOpen(true)
  }

  const handleSubmitCreate = async () => {
    if (!createForm) {
      return
    }

    const username = createForm.username.trim()
    const name = createForm.name.trim()
    const phone = createForm.phone.trim()
    const avatar = createForm.avatar.trim()

    if (!username) {
      setCreateErrorMessage('账号不能为空')
      return
    }
    if (username.length > 50) {
      setCreateErrorMessage('账号长度不能超过 50 个字符')
      return
    }
    if (!USERNAME_PATTERN.test(username)) {
      setCreateErrorMessage('账号仅支持字母、数字和下划线')
      return
    }
    if (!name) {
      setCreateErrorMessage('账户姓名不能为空')
      return
    }
    if (name.length > 50) {
      setCreateErrorMessage('账户姓名长度不能超过 50 个字符')
      return
    }
    if (!phone) {
      setCreateErrorMessage('联系方式不能为空')
      return
    }
    if (!PHONE_PATTERN.test(phone)) {
      setCreateErrorMessage('联系方式格式不正确，请输入 11 位手机号')
      return
    }
    if (avatar.length > 500) {
      setCreateErrorMessage('头像 URL 长度不能超过 500 个字符')
      return
    }
    if (createForm.campusIds.length === 0) {
      setCreateErrorMessage('请至少选择一个可分配校区')
      return
    }
    if (createForm.primaryCampusId === null) {
      setCreateErrorMessage('请选择主校区')
      return
    }
    if (!createForm.campusIds.includes(createForm.primaryCampusId)) {
      setCreateErrorMessage('主校区必须包含在可分配校区中')
      return
    }
    if (createForm.roleIds.length === 0) {
      setCreateErrorMessage('请至少选择一个角色')
      return
    }
    if (
      createForm.roleIds.some((roleId) =>
        roles.some((role) => role.id === roleId && role.code === SYSTEM_ADMIN_ROLE_CODE),
      )
    ) {
      setCreateErrorMessage('系统管理员角色为固定系统角色，不能分配给普通账号')
      return
    }
    if (createForm.dataScopeType === null) {
      setCreateErrorMessage('请选择数据范围')
      return
    }

    setActionLoadingKey('create')
    setCreateSubmitting(true)
    setCreateErrorMessage('')
    setActionMessage('')
    setActionErrorMessage('')
    try {
      const createResult = await createInternalUser({
        username,
        phone,
        name,
        avatar: avatar || undefined,
        roleIds: createForm.roleIds,
        campusIds: createForm.campusIds,
        primaryCampusId: createForm.primaryCampusId,
        dataScopeType: createForm.dataScopeType,
      })
      if (createResult.code !== 0) {
        throw new Error(createResult.message || `创建账号 ${username} 失败`)
      }

      setCreateModalOpen(false)
      setCreateForm(null)
      await loadAccounts()
      setActionMessage(`账号 ${username} 创建成功`)
    } catch (error) {
      setCreateErrorMessage(
        resolveApiErrorMessage(error, `创建账号 ${username} 失败`, 'create'),
      )
    } finally {
      setCreateSubmitting(false)
      setActionLoadingKey(null)
    }
  }

  const handleSubmitEdit = async () => {
    if (!editForm) {
      return
    }

    const name = editForm.name.trim()
    const phone = editForm.phone.trim()
    const avatar = editForm.avatar.trim()

    if (!name) {
      setEditErrorMessage('账户姓名不能为空')
      return
    }
    if (!phone) {
      setEditErrorMessage('联系方式不能为空')
      return
    }
    if (!PHONE_PATTERN.test(phone)) {
      setEditErrorMessage('联系方式格式不正确，请输入 11 位手机号')
      return
    }
    if (editForm.campusIds.length === 0) {
      setEditErrorMessage('请至少选择一个可分配校区')
      return
    }
    if (editForm.primaryCampusId === null) {
      setEditErrorMessage('请选择主校区')
      return
    }
    if (!editForm.campusIds.includes(editForm.primaryCampusId)) {
      setEditErrorMessage('主校区必须包含在可分配校区中')
      return
    }
    if (editForm.roleIds.length === 0) {
      setEditErrorMessage('请至少选择一个角色')
      return
    }
    if (
      editForm.roleIds.some((roleId) =>
        roles.some((role) => role.id === roleId && role.code === SYSTEM_ADMIN_ROLE_CODE),
      )
    ) {
      setEditErrorMessage('系统管理员角色为固定系统角色，不能分配给普通账号')
      return
    }
    if (editForm.dataScopeType === null) {
      setEditErrorMessage('请选择数据范围')
      return
    }

    const loadingKey = `${editForm.userId}:update`
    setActionLoadingKey(loadingKey)
    setEditSubmitting(true)
    setEditErrorMessage('')
    setActionMessage('')
    setActionErrorMessage('')
    try {
      const updateResult = await updateInternalUser(editForm.userId, {
        phone,
        name,
        avatar: avatar || undefined,
        roleIds: editForm.roleIds,
        campusIds: editForm.campusIds,
        primaryCampusId: editForm.primaryCampusId,
        dataScopeType: editForm.dataScopeType,
      })
      if (updateResult.code !== 0) {
        throw new Error(updateResult.message || `修改账号 ${editForm.username} 失败`)
      }

      setEditModalOpen(false)
      setEditForm(null)
      setEditTargetUsername('')
      await loadAccounts()
      setVisibilityPreviewRefreshKey((current) => current + 1)
      setActionMessage(`账号 ${editForm.username} 修改成功`)
    } catch (error) {
      setEditErrorMessage(
        resolveApiErrorMessage(
          error,
          `修改账号 ${editForm.username} 失败`,
          'update',
        ),
      )
    } finally {
      setEditSubmitting(false)
      setActionLoadingKey(null)
    }
  }

  const handleToggleUserStatus = async (row: InternalUserQueryResultVO) => {
    const blockedReason = getAccountOperationBlockReason(row)
    if (blockedReason) {
      setActionErrorMessage(blockedReason)
      setActionMessage('')
      return
    }

    if (row.status === 2 || row.status === null) {
      return
    }

    const nextStatus: InternalUserStatus = row.status === 0 ? 1 : 0
    const actionLabel = nextStatus === 1 ? '启用' : '禁用'
    const loadingKey = `${row.id}:status-toggle`
    setActionLoadingKey(loadingKey)
    setActionMessage('')
    setActionErrorMessage('')
    try {
      const result = await updateInternalUserStatus(row.id, { status: nextStatus })
      if (result.code !== 0) {
        throw new Error(result.message || `${actionLabel}账号 ${row.username} 失败`)
      }
      await loadAccounts()
      setActionMessage(`账号 ${row.username} 已${actionLabel}`)
    } catch (error) {
      setActionErrorMessage(
        resolveApiErrorMessage(
          error,
          `${actionLabel}账号 ${row.username} 失败`,
          'status',
        ),
      )
    } finally {
      setActionLoadingKey(null)
    }
  }

  const handleSubmitResetPassword = async () => {
    if (!resetTarget) {
      return
    }

    const loadingKey = `${resetTarget.id}:reset-password`
    setActionLoadingKey(loadingKey)
    setActionMessage('')
    setActionErrorMessage('')
    setResetErrorMessage('')
    try {
      const result = await resetInternalUserPassword(resetTarget.id, {
        reason: resetPasswordReason.trim() || undefined,
      })
      if (result.code !== 0) {
        throw new Error(result.message || `重置账号 ${resetTarget.username} 密码失败`)
      }
      closeResetPasswordModal(true)
      await loadAccounts()
      setActionMessage(`账号 ${resetTarget.username} 密码已重置`)
    } catch (error) {
      setResetErrorMessage(
        resolveApiErrorMessage(
          error,
          `重置账号 ${resetTarget.username} 密码失败`,
          'reset',
        ),
      )
    } finally {
      setActionLoadingKey(null)
    }
  }

  const showTableLoadingRow = loading && rows.length === 0 && !errorMessage
  const showTableOverlay = showTableLoadingOverlay && rows.length > 0

  return (
    <section style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <AccountOverview
        visibleCount={rows.length}
        total={total}
        createDisabled={loading || createLoading || createSubmitting || actionLoadingKey !== null}
        createLoading={createLoading}
        onOpenCreateModal={() => {
          void handleOpenCreateModal()
        }}
      />
      {actionErrorMessage ? (
        <Alert
          message={actionErrorMessage}
          type="error"
          showIcon
          closable
          onClose={() => setActionErrorMessage('')}
        />
      ) : null}
      {actionMessage ? (
        <Alert
          message={actionMessage}
          type="success"
          showIcon
          closable
          onClose={() => setActionMessage('')}
        />
      ) : null}
      <AccountTableCard
        rows={rows}
        total={total}
        page={page}
        pageSize={PAGE_SIZE}
        loading={loading}
        errorMessage={errorMessage}
        permissionDenied={permissionDenied}
        actionLoadingKey={actionLoadingKey}
        showTableLoadingRow={showTableLoadingRow}
        showTableOverlay={showTableOverlay}
        onPageChange={setPage}
        onUpdateUser={handleUpdateUser}
        onToggleUserStatus={handleToggleUserStatus}
        onOpenResetPasswordModal={handleOpenResetPasswordModal}
      />
      <AccountVisibilityPreview
        rows={rows}
        permissionDenied={permissionDenied}
        refreshKey={visibilityPreviewRefreshKey}
      />
      <AccountCreateModal
        open={createModalOpen}
        createLoading={createLoading}
        createSubmitting={createSubmitting}
        createErrorMessage={createErrorMessage}
        createForm={createForm}
        availablePrimaryCampuses={availableCreatePrimaryCampuses}
        campusSelectOptions={campusSelectOptions}
        roleSelectOptions={roleSelectOptions}
        dataScopeSelectOptions={dataScopeSelectOptions}
        onCancel={closeCreateModal}
        onSubmit={handleSubmitCreate}
        onUsernameChange={handleCreateUsernameChange}
        onCampusIdsChange={handleCreateCampusChange}
        onNameChange={handleCreateNameChange}
        onPhoneChange={handleCreatePhoneChange}
        onAvatarChange={handleCreateAvatarChange}
        onPrimaryCampusChange={handleCreatePrimaryCampusChange}
        onDataScopeTypeChange={handleCreateDataScopeTypeChange}
        onRoleIdsChange={handleCreateRoleIdsChange}
      />
      <AccountEditModal
        open={editModalOpen}
        editLoading={editLoading}
        editSubmitting={editSubmitting}
        editErrorMessage={editErrorMessage}
        editTargetUsername={editTargetUsername}
        editForm={editForm}
        availablePrimaryCampuses={availablePrimaryCampuses}
        campusSelectOptions={campusSelectOptions}
        roleSelectOptions={roleSelectOptions}
        dataScopeSelectOptions={dataScopeSelectOptions}
        onCancel={closeEditModal}
        onSubmit={handleSubmitEdit}
        onCampusIdsChange={handleEditCampusChange}
        onNameChange={handleEditNameChange}
        onPhoneChange={handleEditPhoneChange}
        onAvatarChange={handleEditAvatarChange}
        onPrimaryCampusChange={handleEditPrimaryCampusChange}
        onDataScopeTypeChange={handleEditDataScopeTypeChange}
        onRoleIdsChange={handleEditRoleIdsChange}
      />
      <AccountResetPasswordModal
        open={resetPasswordModalOpen}
        resetTarget={resetTarget}
        resetPasswordReason={resetPasswordReason}
        resetErrorMessage={resetErrorMessage}
        resetting={actionLoadingKey === `${resetTarget?.id}:reset-password`}
        onCancel={() => closeResetPasswordModal()}
        onSubmit={handleSubmitResetPassword}
        onReasonChange={(value) => {
          setResetErrorMessage('')
          setResetPasswordReason(value)
        }}
      />
    </section>
  )
}

export default AccountPageContent
