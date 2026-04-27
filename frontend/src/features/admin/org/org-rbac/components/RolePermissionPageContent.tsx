import { Alert } from 'antd'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { getPermissionTree } from '../../../../../services/permissionService'
import {
  createRole,
  getRoleDetail,
  listRoles,
  updateRole,
} from '../../../../../services/roleService'
import type { InternalPermissionTreeQueryResultVO } from '../../../../../types/permission'
import type { RoleCreateRequest, RoleQueryResultVO, RoleUpdateRequest } from '../../../../../types/role'
import RolePermissionEditorModal, {
  type RolePermissionEditorFormState,
} from './RolePermissionEditorModal'
import RolePermissionOverview from './RolePermissionOverview'
import RolePermissionTableCard from './RolePermissionTableCard'
import {
  PAGE_SIZE,
  resolveApiErrorMessage,
  summarizeRoleStats,
} from '../utils/rolePermissionShared'

function RolePermissionPageContent() {
  const [loading, setLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState('')
  const [page, setPage] = useState(1)
  const [total, setTotal] = useState(0)
  const [rows, setRows] = useState<RoleQueryResultVO[]>([])
  const [requestVersion, setRequestVersion] = useState(0)

  const [permissionTree, setPermissionTree] = useState<InternalPermissionTreeQueryResultVO[]>([])
  const [createOpen, setCreateOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<RoleQueryResultVO | null>(null)
  const [modalLoading, setModalLoading] = useState(false)
  const [modalSubmitting, setModalSubmitting] = useState(false)
  const [modalErrorMessage, setModalErrorMessage] = useState('')
  const [editorForm, setEditorForm] = useState<RolePermissionEditorFormState | null>(null)

  const refreshList = useCallback(() => {
    setRequestVersion((value) => value + 1)
  }, [])

  const ensurePermissionTree = useCallback(async () => {
    if (permissionTree.length > 0) {
      return permissionTree
    }

    const result = await getPermissionTree()
    if (result.code !== 0) {
      throw new Error(result.message || '加载权限树失败')
    }

    const tree = result.data || []
    setPermissionTree(tree)
    return tree
  }, [permissionTree])

  const openCreateModal = useCallback(async () => {
    setCreateOpen(true)
    setEditTarget(null)
    setModalErrorMessage('')
    setModalLoading(true)

    try {
      await ensurePermissionTree()
      setEditorForm({
        code: generateRoleCode(),
        name: '',
        description: '',
        status: 1,
        permissionIds: [],
      })
    } catch (error) {
      setEditorForm(null)
      setModalErrorMessage(resolveApiErrorMessage(error, '加载角色创建信息失败'))
    } finally {
      setModalLoading(false)
    }
  }, [ensurePermissionTree])

  const openEditModal = useCallback(
    async (row: RoleQueryResultVO) => {
      setCreateOpen(false)
      setEditTarget(row)
      setModalErrorMessage('')
      setModalLoading(true)
      setEditorForm(null)

      try {
        await ensurePermissionTree()
        const result = await getRoleDetail(row.id)
        if (result.code !== 0) {
          throw new Error(result.message || '加载角色详情失败')
        }

        const detail = result.data
        setEditorForm({
          code: detail.code || '',
          name: detail.name || '',
          description: detail.description || '',
          status: detail.status ?? 1,
          permissionIds: detail.permissionIds || [],
        })
      } catch (error) {
        setModalErrorMessage(resolveApiErrorMessage(error, '加载角色详情失败'))
      } finally {
        setModalLoading(false)
      }
    },
    [ensurePermissionTree],
  )

  const closeModal = useCallback(() => {
    setCreateOpen(false)
    setEditTarget(null)
    setModalErrorMessage('')
    setModalLoading(false)
    setEditorForm(null)
  }, [])

  const updateEditorForm = useCallback((patch: Partial<RolePermissionEditorFormState>) => {
    setEditorForm((current) => (current ? { ...current, ...patch } : current))
  }, [])

  const validateEditorForm = useCallback((form: RolePermissionEditorFormState) => {
    const code = form.code.trim()
    if (!code) {
      return '请输入角色编码'
    }
    const name = form.name.trim()
    if (!name) {
      return '请输入角色名称'
    }
    if (form.permissionIds.length === 0) {
      return '请至少选择一个权限节点'
    }
    return ''
  }, [])

  const handleSubmit = useCallback(async () => {
    if (!editorForm) {
      return
    }

    const validationMessage = validateEditorForm(editorForm)
    if (validationMessage) {
      setModalErrorMessage(validationMessage)
      return
    }

    setModalSubmitting(true)
    setModalErrorMessage('')

    try {
      if (editTarget) {
        const payload: RoleUpdateRequest = {
          code: editorForm.code.trim(),
          name: editorForm.name.trim(),
          description: editorForm.description.trim() || undefined,
          status: editorForm.status,
          permissionIds: editorForm.permissionIds,
        }
        const result = await updateRole(editTarget.id, payload)
        if (result.code !== 0) {
          throw new Error(result.message || '保存角色失败')
        }
      } else {
        const payload: RoleCreateRequest = {
          code: editorForm.code,
          name: editorForm.name.trim(),
          description: editorForm.description.trim() || undefined,
          status: 1,
          permissionIds: editorForm.permissionIds,
        }
        const result = await createRole(payload)
        if (result.code !== 0) {
          throw new Error(result.message || '创建角色失败')
        }
      }

      closeModal()
      refreshList()
    } catch (error) {
      setModalErrorMessage(resolveApiErrorMessage(error, editTarget ? '保存角色失败' : '创建角色失败'))
    } finally {
      setModalSubmitting(false)
    }
  }, [closeModal, editTarget, editorForm, refreshList, validateEditorForm])

  useEffect(() => {
    let isMounted = true

    const loadRoles = async () => {
      setLoading(true)
      setErrorMessage('')

      try {
        const result = await listRoles({ page, pageSize: PAGE_SIZE })
        if (!isMounted) {
          return
        }

        if (result.code !== 0) {
          throw new Error(result.message || '加载角色列表失败')
        }

        setRows(result.data.items || [])
        setTotal(result.data.total || 0)
      } catch (error) {
        if (!isMounted) {
          return
        }

        setRows([])
        setTotal(0)
        setErrorMessage(resolveApiErrorMessage(error, '加载角色列表失败'))
      } finally {
        if (isMounted) {
          setLoading(false)
        }
      }
    }

    void loadRoles()

    return () => {
      isMounted = false
    }
  }, [page, requestVersion])

  const summary = useMemo(() => summarizeRoleStats(rows), [rows])
  const modalOpen = createOpen || editTarget !== null

  const modalTree = useMemo(
    () => (editorForm && permissionTree.length > 0 ? permissionTree : []),
    [editorForm, permissionTree],
  )

  return (
    <section style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      {modalErrorMessage && !modalOpen ? (
        <Alert
          message={modalErrorMessage}
          type="error"
          showIcon
          closable
          onClose={() => setModalErrorMessage('')}
        />
      ) : null}
      <RolePermissionOverview
        visibleCount={rows.length}
        total={total}
        enabledCount={summary.enabledCount}
        permissionCount={summary.permissionCount}
        userCount={summary.userCount}
        onCreate={() => {
          void openCreateModal()
        }}
      />
      <RolePermissionTableCard
        rows={rows}
        total={total}
        page={page}
        pageSize={PAGE_SIZE}
        loading={loading}
        errorMessage={errorMessage}
        onPageChange={setPage}
        onEdit={(row) => {
          void openEditModal(row)
        }}
      />
      <RolePermissionEditorModal
        open={modalOpen}
        mode={editTarget ? 'edit' : 'create'}
        loading={modalLoading}
        submitting={modalSubmitting}
        errorMessage={modalErrorMessage}
        form={editorForm}
        tree={modalTree}
        onCancel={closeModal}
        onSubmit={handleSubmit}
        onCodeChange={(value) => updateEditorForm({ code: value })}
        onNameChange={(value) => updateEditorForm({ name: value })}
        onDescriptionChange={(value) => updateEditorForm({ description: value })}
        onPermissionIdsChange={(values) => updateEditorForm({ permissionIds: values })}
      />
    </section>
  )
}

export default RolePermissionPageContent

function generateRoleCode() {
  const randomSuffix = Math.random().toString(36).slice(2, 8).toUpperCase()
  const timestampSuffix = Date.now().toString(36).slice(-4).toUpperCase()
  return `ROLE_${timestampSuffix}${randomSuffix}`
}
