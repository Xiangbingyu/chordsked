import { isAxiosError } from 'axios'
import { useEffect, useMemo, useState } from 'react'
import { getPermissionTree } from '../../../../../services/permissionService'
import type { ApiResponse } from '../../../../../types/common'
import type { InternalPermissionTreeQueryResultVO } from '../../../../../types/permission'
import PermissionTreeOverview from './PermissionTreeOverview'
import PermissionTreePanel from './PermissionTreePanel'
import {
  buildPermissionTreeData,
  collectExpandedKeys,
  summarizePermissionTree,
} from '../utils/permissionTree'

function resolveApiErrorMessage(error: unknown, fallback: string) {
  if (isAxiosError<ApiResponse<null>>(error)) {
    if (error.response?.status === 403) {
      return error.response?.data?.message || '当前账号没有查看权限树的权限'
    }

    return error.response?.data?.message || fallback
  }

  return error instanceof Error ? error.message : fallback
}

function PermissionTreePageContent() {
  const [loading, setLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState('')
  const [permissionTree, setPermissionTree] = useState<InternalPermissionTreeQueryResultVO[]>([])
  const [expandedKeys, setExpandedKeys] = useState<string[]>([])

  useEffect(() => {
    let isMounted = true

    const loadPermissionTree = async () => {
      setLoading(true)
      setErrorMessage('')

      try {
        const result = await getPermissionTree()
        if (!isMounted) {
          return
        }

        if (result.code !== 0) {
          throw new Error(result.message || '加载权限树失败')
        }

        setPermissionTree(result.data || [])
        setExpandedKeys(collectExpandedKeys(result.data || []))
      } catch (error) {
        if (!isMounted) {
          return
        }

        setPermissionTree([])
        setExpandedKeys([])
        setErrorMessage(resolveApiErrorMessage(error, '加载权限树失败'))
      } finally {
        if (isMounted) {
          setLoading(false)
        }
      }
    }

    void loadPermissionTree()

    return () => {
      isMounted = false
    }
  }, [])

  const summary = useMemo(() => summarizePermissionTree(permissionTree), [permissionTree])
  const treeData = useMemo(() => buildPermissionTreeData(permissionTree), [permissionTree])

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <PermissionTreeOverview summary={summary} />
      <PermissionTreePanel
        loading={loading}
        errorMessage={errorMessage}
        treeData={treeData}
        expandedKeys={expandedKeys}
        onExpand={setExpandedKeys}
      />
    </div>
  )
}

export default PermissionTreePageContent
