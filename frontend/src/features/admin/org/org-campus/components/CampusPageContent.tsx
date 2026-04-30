import { Alert, Button, Input, Select } from 'antd'
import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  createCampus,
  deleteCampus,
  getCampusDetail,
  listCampuses,
  updateCampus,
} from '../../../../../services/campusService'
import type {
  CampusCreateRequest,
  CampusQueryResultVO,
  CampusStatus,
  CampusUpdateRequest,
} from '../../../../../types/campus'
import CampusEditorModal from './CampusEditorModal'
import CampusOverview from './CampusOverview'
import CampusTableCard from './CampusTableCard'
import {
  CAMPUS_CODE_PATTERN,
  PAGE_SIZE,
  TABLE_LOADING_OVERLAY_DELAY_MS,
  createCampusEditorFormFromDetail,
  createEmptyCampusEditorForm,
  resolveApiErrorMessage,
  summarizeCampusStats,
  type CampusEditorFormState,
} from '../utils/campusPageShared'

type CampusFilters = {
  keyword: string
  status?: CampusStatus
}

function validateCampusForm(form: CampusEditorFormState) {
  const code = form.code.trim().toUpperCase()
  const name = form.name.trim()
  const address = form.address.trim()
  const phone = form.phone.trim()
  const remark = form.remark.trim()
  const sort = form.sort.trim()

  if (!code) {
    return { error: '校区编码不能为空' }
  }
  if (code.length > 20) {
    return { error: '校区编码长度不能超过 20 个字符' }
  }
  if (!CAMPUS_CODE_PATTERN.test(code)) {
    return { error: '校区编码仅支持大写字母、数字、下划线和中划线' }
  }
  if (!name) {
    return { error: '校区名称不能为空' }
  }
  if (name.length > 50) {
    return { error: '校区名称长度不能超过 50 个字符' }
  }
  if (address.length > 200) {
    return { error: '校区地址长度不能超过 200 个字符' }
  }
  if (phone.length > 30) {
    return { error: '联系方式长度不能超过 30 个字符' }
  }
  if (sort && !/^\d+$/.test(sort)) {
    return { error: '排序值必须为非负整数' }
  }
  if (remark.length > 200) {
    return { error: '备注长度不能超过 200 个字符' }
  }
  if (![0, 1].includes(form.status)) {
    return { error: '当前状态配置无效' }
  }

  const payload: CampusCreateRequest = {
    code,
    name,
    address: address || undefined,
    phone: phone || undefined,
    sort: sort ? Number(sort) : undefined,
    status: form.status,
    remark: remark || undefined,
  }

  return { payload }
}

function CampusPageContent() {
  const [loading, setLoading] = useState(true)
  const [showTableLoadingOverlay, setShowTableLoadingOverlay] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [permissionDenied, setPermissionDenied] = useState(false)
  const [page, setPage] = useState(1)
  const [total, setTotal] = useState(0)
  const [rows, setRows] = useState<CampusQueryResultVO[]>([])
  const [filters, setFilters] = useState<CampusFilters>({ keyword: '', status: undefined })
  const [appliedFilters, setAppliedFilters] = useState<CampusFilters>({ keyword: '', status: undefined })
  const [actionLoadingKey, setActionLoadingKey] = useState<string | null>(null)
  const [actionMessage, setActionMessage] = useState('')
  const [actionErrorMessage, setActionErrorMessage] = useState('')
  const [createModalOpen, setCreateModalOpen] = useState(false)
  const [createSubmitting, setCreateSubmitting] = useState(false)
  const [createErrorMessage, setCreateErrorMessage] = useState('')
  const [createForm, setCreateForm] = useState<CampusEditorFormState | null>(null)
  const [editModalOpen, setEditModalOpen] = useState(false)
  const [editLoading, setEditLoading] = useState(false)
  const [editSubmitting, setEditSubmitting] = useState(false)
  const [editErrorMessage, setEditErrorMessage] = useState('')
  const [editCampusId, setEditCampusId] = useState<number | null>(null)
  const [editForm, setEditForm] = useState<CampusEditorFormState | null>(null)

  const loadCampuses = useCallback(async () => {
    setLoading(true)
    setErrorMessage('')
    setPermissionDenied(false)
    try {
      const result = await listCampuses({
        page,
        pageSize: PAGE_SIZE,
        keyword: appliedFilters.keyword || undefined,
        status: appliedFilters.status,
      })
      if (result.code !== 0) {
        throw new Error(result.message || '加载校区列表失败')
      }

      setRows(result.data.items || [])
      setTotal(result.data.total || 0)
    } catch (error) {
      setRows([])
      setTotal(0)
      setPermissionDenied(
        error instanceof Error ? error.message.includes('权限') : false,
      )
      setErrorMessage(resolveApiErrorMessage(error, '加载校区列表失败'))
    } finally {
      setLoading(false)
    }
  }, [appliedFilters.keyword, appliedFilters.status, page])

  useEffect(() => {
    void loadCampuses()
  }, [loadCampuses])

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

  const stats = useMemo(() => summarizeCampusStats(rows), [rows])

  const statusFilterOptions = useMemo(
    () => [
      { label: '全部状态', value: 'all' },
      { label: '启用', value: 1 },
      { label: '禁用', value: 0 },
      { label: '已删除', value: 2 },
    ],
    [],
  )

  const statusEditorOptions = useMemo(
    () => [
      { label: '启用', value: 1 as CampusStatus },
      { label: '禁用', value: 0 as CampusStatus },
    ],
    [],
  )

  const updateCreateForm = useCallback(
    (updater: (current: CampusEditorFormState) => CampusEditorFormState) => {
      setCreateErrorMessage('')
      setCreateForm((current) => (current ? updater(current) : current))
    },
    [],
  )

  const updateEditForm = useCallback(
    (updater: (current: CampusEditorFormState) => CampusEditorFormState) => {
      setEditErrorMessage('')
      setEditForm((current) => (current ? updater(current) : current))
    },
    [],
  )

  const closeCreateModal = useCallback(() => {
    if (createSubmitting) {
      return
    }

    setCreateModalOpen(false)
    setCreateErrorMessage('')
    setCreateForm(null)
  }, [createSubmitting])

  const closeEditModal = useCallback(() => {
    if (editLoading || editSubmitting) {
      return
    }

    setEditModalOpen(false)
    setEditLoading(false)
    setEditErrorMessage('')
    setEditCampusId(null)
    setEditForm(null)
  }, [editLoading, editSubmitting])

  const reloadListWithCurrentPage = useCallback(async () => {
    await loadCampuses()
  }, [loadCampuses])

  const handleOpenCreateModal = () => {
    setCreateModalOpen(true)
    setCreateSubmitting(false)
    setCreateErrorMessage('')
    setCreateForm(createEmptyCampusEditorForm())
    setActionMessage('')
    setActionErrorMessage('')
  }

  const handleOpenEditModal = async (row: CampusQueryResultVO) => {
    setActionLoadingKey(`${row.id}:edit`)
    setActionMessage('')
    setActionErrorMessage('')
    setEditModalOpen(true)
    setEditLoading(true)
    setEditSubmitting(false)
    setEditErrorMessage('')
    setEditCampusId(row.id)
    setEditForm(null)

    try {
      const result = await getCampusDetail(row.id)
      if (result.code !== 0) {
        throw new Error(result.message || `加载校区 ${row.name} 详情失败`)
      }

      setEditForm(createCampusEditorFormFromDetail(result.data))
    } catch (error) {
      setEditErrorMessage(resolveApiErrorMessage(error, `加载校区 ${row.name} 详情失败`))
    } finally {
      setEditLoading(false)
      setActionLoadingKey(null)
    }
  }

  const handleSubmitCreate = async () => {
    if (!createForm) {
      return
    }

    const validation = validateCampusForm(createForm)
    if ('error' in validation) {
      setCreateErrorMessage(validation.error || '校区表单校验失败')
      return
    }

    setActionLoadingKey('create')
    setCreateSubmitting(true)
    setCreateErrorMessage('')
    setActionMessage('')
    setActionErrorMessage('')
    try {
      const result = await createCampus(validation.payload)
      if (result.code !== 0) {
        throw new Error(result.message || `创建校区 ${validation.payload.name} 失败`)
      }

      closeCreateModal()
      await reloadListWithCurrentPage()
      setActionMessage(`校区 ${validation.payload.name} 创建成功`)
    } catch (error) {
      setCreateErrorMessage(
        resolveApiErrorMessage(error, `创建校区 ${validation.payload.name} 失败`),
      )
    } finally {
      setCreateSubmitting(false)
      setActionLoadingKey(null)
    }
  }

  const handleSubmitEdit = async () => {
    if (!editForm || editCampusId === null) {
      return
    }

    const validation = validateCampusForm(editForm)
    if ('error' in validation) {
      setEditErrorMessage(validation.error || '校区表单校验失败')
      return
    }

    const payload: CampusUpdateRequest = validation.payload
    setActionLoadingKey(`${editCampusId}:submit`)
    setEditSubmitting(true)
    setEditErrorMessage('')
    setActionMessage('')
    setActionErrorMessage('')
    try {
      const result = await updateCampus(editCampusId, payload)
      if (result.code !== 0) {
        throw new Error(result.message || `修改校区 ${payload.name} 失败`)
      }

      closeEditModal()
      await reloadListWithCurrentPage()
      setActionMessage(`校区 ${payload.name} 修改成功`)
    } catch (error) {
      setEditErrorMessage(resolveApiErrorMessage(error, `修改校区 ${payload.name} 失败`))
    } finally {
      setEditSubmitting(false)
      setActionLoadingKey(null)
    }
  }

  const handleDeleteCampus = async (row: CampusQueryResultVO) => {
    setActionLoadingKey(`${row.id}:delete`)
    setActionMessage('')
    setActionErrorMessage('')
    try {
      const result = await deleteCampus(row.id)
      if (result.code !== 0) {
        throw new Error(result.message || `删除校区 ${row.name} 失败`)
      }

      if (rows.length === 1 && page > 1) {
        setPage((current) => current - 1)
      } else {
        await reloadListWithCurrentPage()
      }
      setActionMessage(`校区 ${row.name} 删除成功`)
    } catch (error) {
      setActionErrorMessage(resolveApiErrorMessage(error, `删除校区 ${row.name} 失败`))
    } finally {
      setActionLoadingKey(null)
    }
  }

  const showTableLoadingRow = loading && rows.length === 0 && !errorMessage
  const showTableOverlay = showTableLoadingOverlay && rows.length > 0

  return (
    <section style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <CampusOverview
        visibleCount={rows.length}
        total={total}
        enabledCount={stats.enabledCount}
        createDisabled={loading || createSubmitting || actionLoadingKey !== null}
        onOpenCreateModal={handleOpenCreateModal}
      />

      <section
        style={{
          borderRadius: 18,
          border: '1px solid #f0ebe3',
          backgroundColor: '#fffaf2',
          padding: 16,
          display: 'flex',
          flexDirection: 'column',
          gap: 12,
        }}
      >
        <div style={{ fontSize: 13, fontWeight: 600, color: '#2a2a2f' }}>筛选条件</div>
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
            gap: 12,
            alignItems: 'end',
          }}
        >
          <div>
            <div style={{ fontSize: 12, color: '#8f8376', marginBottom: 6 }}>关键字</div>
            <Input
              value={filters.keyword}
              placeholder="按校区编码或名称搜索"
              disabled={loading}
              onChange={(event) => {
                setFilters((current) => ({ ...current, keyword: event.target.value }))
              }}
              onPressEnter={() => {
                setPage(1)
                setAppliedFilters(filters)
              }}
            />
          </div>
          <div>
            <div style={{ fontSize: 12, color: '#8f8376', marginBottom: 6 }}>状态</div>
            <Select
              value={filters.status ?? 'all'}
              options={statusFilterOptions}
              disabled={loading}
              onChange={(value) => {
                setFilters((current) => ({
                  ...current,
                  status: value === 'all' ? undefined : (value as CampusStatus),
                }))
              }}
            />
          </div>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            <Button
              type="primary"
              disabled={loading}
              style={{
                backgroundColor: '#ff9b54',
                borderColor: '#ff9b54',
                color: '#1f1f1f',
              }}
              onClick={() => {
                setPage(1)
                setAppliedFilters(filters)
              }}
            >
              查询
            </Button>
            <Button
              disabled={loading}
              onClick={() => {
                const nextFilters = { keyword: '', status: undefined as CampusStatus | undefined }
                setFilters(nextFilters)
                setPage(1)
                setAppliedFilters(nextFilters)
              }}
            >
              重置
            </Button>
          </div>
        </div>
      </section>

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

      <CampusTableCard
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
        onOpenEditModal={handleOpenEditModal}
        onDelete={handleDeleteCampus}
      />

      <CampusEditorModal
        mode="create"
        open={createModalOpen}
        loading={false}
        submitting={createSubmitting}
        errorMessage={createErrorMessage}
        form={createForm}
        statusOptions={statusEditorOptions}
        onCancel={closeCreateModal}
        onSubmit={handleSubmitCreate}
        onCodeChange={(value) => {
          updateCreateForm((current) => ({ ...current, code: value.toUpperCase() }))
        }}
        onNameChange={(value) => {
          updateCreateForm((current) => ({ ...current, name: value }))
        }}
        onAddressChange={(value) => {
          updateCreateForm((current) => ({ ...current, address: value }))
        }}
        onPhoneChange={(value) => {
          updateCreateForm((current) => ({ ...current, phone: value }))
        }}
        onSortChange={(value) => {
          updateCreateForm((current) => ({ ...current, sort: value }))
        }}
        onStatusChange={(value) => {
          updateCreateForm((current) => ({ ...current, status: value }))
        }}
        onRemarkChange={(value) => {
          updateCreateForm((current) => ({ ...current, remark: value }))
        }}
      />

      <CampusEditorModal
        mode="edit"
        open={editModalOpen}
        loading={editLoading}
        submitting={editSubmitting}
        errorMessage={editErrorMessage}
        form={editForm}
        statusOptions={statusEditorOptions}
        onCancel={closeEditModal}
        onSubmit={handleSubmitEdit}
        onCodeChange={(value) => {
          updateEditForm((current) => ({ ...current, code: value.toUpperCase() }))
        }}
        onNameChange={(value) => {
          updateEditForm((current) => ({ ...current, name: value }))
        }}
        onAddressChange={(value) => {
          updateEditForm((current) => ({ ...current, address: value }))
        }}
        onPhoneChange={(value) => {
          updateEditForm((current) => ({ ...current, phone: value }))
        }}
        onSortChange={(value) => {
          updateEditForm((current) => ({ ...current, sort: value }))
        }}
        onStatusChange={(value) => {
          updateEditForm((current) => ({ ...current, status: value }))
        }}
        onRemarkChange={(value) => {
          updateEditForm((current) => ({ ...current, remark: value }))
        }}
      />
    </section>
  )
}

export default CampusPageContent
