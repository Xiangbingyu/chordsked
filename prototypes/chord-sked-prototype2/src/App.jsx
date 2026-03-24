import { useMemo, useState } from 'react'
import { BrowserRouter, Navigate, NavLink, Route, Routes, useLocation, useNavigate, useParams } from 'react-router-dom'
import {
  Bell,
  BookOpen,
  CalendarDays,
  ChevronDown,
  ChevronRight,
  CheckCircle2,
  ClipboardList,
  GraduationCap,
  Home,
  MessageSquare,
  Package,
  Repeat2,
  School,
  Search,
  Shield,
  ShoppingBag,
  UserSquare2,
  Users
} from 'lucide-react'
import {
  adminKpis,
  conversionOrders,
  conversionLeads,
  teachers,
  students,
  scheduleCards,
  adjustmentRequests,
  teacherTodo,
  teacherTodayCourses,
  homeworkPool,
  studentRemaining,
  studentBookings,
  studentTimeline,
  coursePackages,
  notifications
} from './lib/mockData'
import './App.css'

const roleMeta = {
  admin: { title: '教务端', subtitle: '排课 / 转化 / 审批 / 权限', icon: School },
  teacher: { title: '教师端', subtitle: '签到 / 反馈 / 作业 / 课表', icon: GraduationCap },
  student: { title: '学员端', subtitle: '课表 / 学习 / 我的', icon: UserSquare2 }
}

const navMap = {
  student: [
    { key: 'schedule', label: '课表', icon: CalendarDays },
    { key: 'learning', label: '学习', icon: BookOpen },
    { key: 'profile', label: '我的', icon: UserSquare2 }
  ]
}

const adminSitemap = [
  { key: 'workbench', label: '工作台', icon: Home, directPage: 'workbench-overview', children: [] },
  { key: 'messages', label: '消息中心', icon: MessageSquare, children: [{ key: 'messages-list', label: '消息列表' }] },
  { key: 'conversion', label: '团购转化', icon: ShoppingBag, children: [{ key: 'conversion-import', label: '订单导入/列表' }, { key: 'conversion-clue', label: '核销登记/线索详情' }, { key: 'conversion-qr', label: '预约二维码页' }] },
  { key: 'schedule', label: '排课与课表', icon: CalendarDays, children: [{ key: 'schedule-calendar', label: '排课日历' }, { key: 'schedule-template', label: '课程模板管理' }, { key: 'schedule-room', label: '教室资源管理' }] },
  { key: 'adjustments', label: '调课/取消/补课', icon: Repeat2, children: [{ key: 'adjustments-list', label: '申请列表' }, { key: 'adjustments-log', label: '审核日志' }] },
  { key: 'students', label: '学员管理', icon: Users, children: [{ key: 'students-list', label: '学员列表' }] },
  { key: 'teachers', label: '教师管理', icon: GraduationCap, children: [{ key: 'teachers-list', label: '教师列表/档案' }] },
  { key: 'packages', label: '课包管理', icon: Package, children: [{ key: 'packages-list', label: '课包列表' }] },
  { key: 'notice', label: '公告/通知', icon: MessageSquare, children: [{ key: 'notice-rules', label: '消息提醒规则' }, { key: 'notice-templates', label: '公告模板管理' }, { key: 'notice-touch', label: '触达记录' }] },
  { key: 'org', label: '组织与权限', icon: Shield, children: [{ key: 'org-campus', label: '校区管理' }, { key: 'org-tree', label: '组织树管理' }, { key: 'org-account', label: '账号管理' }, { key: 'org-rbac', label: '角色与数据权限' }, { key: 'org-permtree', label: '权限树映射' }] },
  { key: 'analytics', label: '数据看板（二期）', icon: School, children: [{ key: 'analytics-overview', label: '校区经营/质量/续费（预留）' }] }
]

const adminPageKeys = adminSitemap.flatMap((group) => (
  group.directPage ? [group.directPage] : group.children.map((child) => child.key)
))

const teacherSitemap = [
  {
    key: 't-workbench',
    label: '工作台',
    icon: Home,
    directPage: 'teacher-workbench-overview',
    children: []
  },
  { key: 't-messages', label: '消息中心', icon: MessageSquare, children: [{ key: 'teacher-messages-list', label: '消息列表' }] },
  { key: 't-timetable', label: '我的课表', icon: CalendarDays, children: [{ key: 'teacher-timetable', label: '日/周视图' }] },
  { key: 't-attendance', label: '上课签到', icon: CheckCircle2, children: [{ key: 'teacher-attendance', label: '待签到列表' }] },
  { key: 't-feedback', label: '课后反馈', icon: ClipboardList, children: [{ key: 'teacher-feedback', label: '待反馈队列' }] },
  { key: 't-homework', label: '作业管理', icon: BookOpen, children: [{ key: 'teacher-homework', label: '作业列表' }] },
  { key: 't-students', label: '学员管理', icon: Users, children: [{ key: 'teacher-students', label: '已分配学员' }] },
  { key: 't-availability', label: '可授课时间与承载量', icon: School, children: [{ key: 'teacher-availability-time', label: '可用时间配置' }, { key: 'teacher-availability-pref', label: '授课偏好' }] }
]

const teacherPageKeys = teacherSitemap.flatMap((group) => (
  group.directPage ? [group.directPage] : group.children.map((child) => child.key)
))

const getDefaultPage = (role) => {
  if (role === 'admin') return 'workbench-overview'
  if (role === 'teacher') return 'teacher-workbench-overview'
  return navMap[role][0].key
}

function Shell() {
  const { role, page } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const currentRole = roleMeta[role] ? role : 'admin'
  const navItems = currentRole === 'student' ? navMap.student : []
  const currentPage = currentRole === 'admin'
    ? (adminPageKeys.includes(page) ? page : getDefaultPage('admin'))
    : currentRole === 'teacher'
        ? (teacherPageKeys.includes(page) ? page : getDefaultPage('teacher'))
        : (navItems.some((item) => item.key === page) ? page : getDefaultPage(currentRole))
  const [campus, setCampus] = useState('北环国基路校区')
  const [notificationOpen, setNotificationOpen] = useState(false)
  const [expandedAdminTabs, setExpandedAdminTabs] = useState(
    adminSitemap.reduce((acc, group, index) => ({ ...acc, [group.key]: index === 0 }), {})
  )
  const [expandedTeacherTabs, setExpandedTeacherTabs] = useState(
    teacherSitemap.reduce((acc, group, index) => ({ ...acc, [group.key]: index === 0 }), {})
  )
  const unreadCount = notifications.filter((item) => !item.read).length
  const RoleIcon = roleMeta[currentRole].icon
  const messageFilter = useMemo(() => new URLSearchParams(location.search).get('filter') || '全部', [location.search])

  const roleSwitcher = useMemo(() => Object.keys(roleMeta), [])

  return (
    <div className="min-h-screen demo-bg text-[#2b2b2b]">
      <div className="mx-auto flex min-h-screen max-w-[1600px] gap-5 p-5">
        <aside className="w-[260px] rounded-2xl bg-[#23242e] p-5 text-[#f1f1f2] shadow-2xl shadow-black/20">
          <div className="mb-6 rounded-xl bg-white/10 p-4">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#ff9b54] text-[#1f1f1f]">
                <RoleIcon size={18} />
              </div>
              <div>
                <div className="text-base font-semibold">ChordSked Demo</div>
                <div className="text-xs text-white/70">{roleMeta[currentRole].title}</div>
              </div>
            </div>
          </div>

          <div className="mb-6 grid grid-cols-3 gap-2">
            {roleSwitcher.map((item) => (
              <button
                key={item}
                onClick={() => navigate(`/${item}/${getDefaultPage(item)}`)}
                className={`rounded-lg px-2 py-2 text-xs font-medium transition ${
                  currentRole === item ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-white/10 text-white/80 hover:bg-white/20'
                }`}
              >
                {roleMeta[item].title.replace('端', '')}
              </button>
            ))}
          </div>

          {currentRole === 'student' && (
            <nav className="space-y-1.5">
              {navItems.map((item) => (
                <NavLink
                  key={item.key}
                  to={`/${currentRole}/${item.key}`}
                  className={({ isActive }) =>
                    `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm transition ${
                      isActive ? 'bg-[#ff9b54] text-[#1f1f1f] font-semibold' : 'text-white/85 hover:bg-white/10'
                    }`
                  }
                >
                  <item.icon size={17} />
                  {item.label}
                </NavLink>
              ))}
            </nav>
          )}
          {currentRole === 'teacher' && (
            <nav className="space-y-1.5">
              {teacherSitemap.map((group) => {
                if (group.directPage) {
                  return (
                    <div key={group.key} className="rounded-xl bg-white/5 p-1">
                      <NavLink
                        to={`/teacher/${group.directPage}`}
                        className={({ isActive }) =>
                          `flex w-full items-center gap-2.5 rounded-lg px-2.5 py-2 text-left text-sm ${
                            isActive ? 'bg-[#ff9b54] text-[#1f1f1f] font-semibold' : 'text-white/85 hover:bg-white/10'
                          }`
                        }
                      >
                        <group.icon size={16} />
                        {group.label}
                      </NavLink>
                    </div>
                  )
                }
                const isCurrentGroup = group.children.some((child) => child.key === currentPage)
                const isExpanded = expandedTeacherTabs[group.key] || isCurrentGroup
                return (
                  <div key={group.key} className="rounded-xl bg-white/5 p-1">
                    <button
                      onClick={() => setExpandedTeacherTabs((prev) => ({ ...prev, [group.key]: !prev[group.key] }))}
                      className={`flex w-full items-center justify-between rounded-lg px-2.5 py-2 text-left text-sm ${
                        isCurrentGroup ? 'bg-[#ff9b54] text-[#1f1f1f] font-semibold' : 'text-white/85 hover:bg-white/10'
                      }`}
                    >
                      <span className="flex items-center gap-2.5">
                        <group.icon size={16} />
                        {group.label}
                      </span>
                      {isExpanded ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
                    </button>
                    {isExpanded && (
                      <div className="mt-1 space-y-1 px-1 pb-1">
                        {group.children.map((child) => (
                          <NavLink
                            key={child.key}
                            to={`/teacher/${child.key}`}
                            className={({ isActive }) =>
                              `block rounded-lg px-8 py-1.5 text-xs transition ${
                                isActive ? 'bg-white text-[#2a2a2f] font-medium' : 'text-white/80 hover:bg-white/10'
                              }`
                            }
                          >
                            {child.label}
                          </NavLink>
                        ))}
                      </div>
                    )}
                  </div>
                )
              })}
            </nav>
          )}
          {currentRole === 'admin' && (
            <>
              <div className="mb-3 rounded-xl bg-white/10 p-3">
                <div className="mb-2 text-xs text-white/70">工作台校区选择</div>
                <select
                  value={campus}
                  onChange={(event) => setCampus(event.target.value)}
                  className="w-full rounded-lg border border-white/20 bg-[#2c2d38] px-2.5 py-2 text-xs text-white outline-none"
                >
                  <option className="text-black">北环国基路校区</option>
                  <option className="text-black">西大剧院校区</option>
                </select>
              </div>
              <nav className="space-y-1.5">
                {adminSitemap.map((group) => {
                if (group.directPage) {
                  return (
                    <div key={group.key} className="rounded-xl bg-white/5 p-1">
                      <NavLink
                        to={`/admin/${group.directPage}`}
                        className={({ isActive }) =>
                          `flex w-full items-center gap-2.5 rounded-lg px-2.5 py-2 text-left text-sm ${
                            isActive ? 'bg-[#ff9b54] text-[#1f1f1f] font-semibold' : 'text-white/85 hover:bg-white/10'
                          }`
                        }
                      >
                        <group.icon size={16} />
                        {group.label}
                      </NavLink>
                    </div>
                  )
                }

                const isCurrentGroup = group.children.some((child) => child.key === currentPage)
                const isExpanded = expandedAdminTabs[group.key] || isCurrentGroup
                return (
                  <div key={group.key} className="rounded-xl bg-white/5 p-1">
                    <button
                      onClick={() => setExpandedAdminTabs((prev) => ({ ...prev, [group.key]: !prev[group.key] }))}
                      className={`flex w-full items-center justify-between rounded-lg px-2.5 py-2 text-left text-sm ${
                        isCurrentGroup ? 'bg-[#ff9b54] text-[#1f1f1f] font-semibold' : 'text-white/85 hover:bg-white/10'
                      }`}
                    >
                      <span className="flex items-center gap-2.5">
                        <group.icon size={16} />
                        {group.label}
                      </span>
                      {isExpanded ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
                    </button>
                    {isExpanded && (
                      <div className="mt-1 space-y-1 px-1 pb-1">
                        {group.children.map((child) => (
                          <NavLink
                            key={child.key}
                            to={`/admin/${child.key}`}
                            className={({ isActive }) =>
                              `block rounded-lg px-8 py-1.5 text-xs transition ${
                                isActive ? 'bg-white text-[#2a2a2f] font-medium' : 'text-white/80 hover:bg-white/10'
                              }`
                            }
                          >
                            {child.label}
                          </NavLink>
                        ))}
                      </div>
                    )}
                  </div>
                )
                })}
              </nav>
            </>
          )}
        </aside>

        <div className="flex-1">
          <header className="mb-4 flex items-center justify-between rounded-2xl bg-white px-6 py-4 shadow-sm">
            <div>
              <h1 className="text-2xl font-semibold text-[#2a2a2f]">{roleMeta[currentRole].title}</h1>
              <p className="text-sm text-[#7f7f88]">{roleMeta[currentRole].subtitle}</p>
            </div>
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-2 rounded-xl border border-[#ece7df] bg-[#fffaf2] px-3 py-2 text-sm">
                <Search size={14} />
                <input
                  className="w-44 bg-transparent text-sm outline-none"
                  placeholder="搜索学员/老师/订单"
                />
              </div>
              {currentRole !== 'admin' && (
                <select
                  value={campus}
                  onChange={(event) => setCampus(event.target.value)}
                  className="rounded-xl border border-[#ece7df] bg-white px-3 py-2 text-sm outline-none"
                >
                  <option>北环国基路校区</option>
                  <option>西大剧院校区</option>
                </select>
              )}
              <button
                onClick={() => setNotificationOpen((open) => !open)}
                className="relative rounded-xl bg-[#fff3e8] p-2.5 text-[#8f6746]"
              >
                <Bell size={16} />
                {unreadCount > 0 && (
                  <span className="absolute right-1 top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-[#ff6f3d] px-1 text-[10px] text-white">
                    {unreadCount}
                  </span>
                )}
              </button>
              {notificationOpen && (
                <div className="absolute right-11 top-20 z-20 w-[360px] rounded-2xl border border-[#f0ebe3] bg-white p-4 shadow-xl">
                  <div className="mb-3 flex items-center justify-between">
                    <div className="text-sm font-semibold">通知提醒</div>
                    <button
                      onClick={() => {
                        navigate('/admin/messages-list?filter=未读')
                        setNotificationOpen(false)
                      }}
                      className="text-xs text-[#bc7844]"
                    >
                      进入消息中心（未读）
                    </button>
                  </div>
                  <div className="mb-3 flex gap-2">
                    {['待办提醒', '审批结果', '系统公告'].map((item) => (
                      <button
                        key={item}
                        onClick={() => {
                          navigate(`/admin/messages-list?filter=${item}`)
                          setNotificationOpen(false)
                        }}
                        className="rounded-full bg-[#fff5eb] px-3 py-1 text-xs text-[#8b5d34]"
                      >
                        {item}
                      </button>
                    ))}
                  </div>
                  <div className="space-y-2">
                    {notifications.slice(0, 3).map((item) => (
                      <button
                        key={item.id}
                        onClick={() => {
                          navigate(`/admin/messages-list?filter=${item.category}`)
                          setNotificationOpen(false)
                        }}
                        className="w-full rounded-xl bg-[#faf8f4] p-3 text-left"
                      >
                        <div className="flex justify-between text-xs">
                          <span className="font-medium">{item.title}</span>
                          <span className="text-[#8f8376]">{item.time}</span>
                        </div>
                        <div className="mt-1 text-xs text-[#8f8376]">{item.content}</div>
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </header>

          <main className="rounded-2xl bg-white p-6 shadow-sm">
            {currentRole === 'admin' && (
              <AdminView
                page={currentPage}
                messageFilter={messageFilter}
                onNavigate={navigate}
              />
            )}
            {currentRole === 'teacher' && <TeacherView page={currentPage} />}
            {currentRole === 'student' && <StudentView page={currentPage} />}
          </main>
        </div>
      </div>
    </div>
  )
}

function QrPreview({ value }) {
  const dots = Array.from({ length: 21 * 21 }, (_, idx) => {
    const row = Math.floor(idx / 21)
    const col = idx % 21
    const seed = value.split('').reduce((sum, ch) => sum + ch.charCodeAt(0), 0)
    const finder = (row < 7 && col < 7) || (row < 7 && col > 13) || (row > 13 && col < 7)
    if (finder) {
      const inFrame = row === 0 || row === 6 || col === 0 || col === 6
      const inCore = row >= 2 && row <= 4 && col >= 2 && col <= 4
      return inFrame || inCore
    }
    return ((row * 17 + col * 13 + seed) % 3) === 0
  })

  return (
    <div className="rounded-2xl border border-[#e8dfd3] bg-white p-3 inline-flex flex-col items-center gap-2">
      <div
        className="grid gap-[2px] bg-white p-1"
        style={{ gridTemplateColumns: 'repeat(21, minmax(0, 1fr))' }}
      >
        {dots.map((dark, idx) => (
          <div key={idx} className={`h-[6px] w-[6px] ${dark ? 'bg-black' : 'bg-white'}`} />
        ))}
      </div>
      <div className="text-[11px] text-[#8f8376]">扫码预约体验课</div>
    </div>
  )
}

function PaginatedTable({ columns, rows, rowKey = 'id', pageSize = 5, emptyText = '暂无数据' }) {
  const [page, setPage] = useState(1)
  const totalPages = Math.max(1, Math.ceil(rows.length / pageSize))
  const safePage = Math.min(page, totalPages)
  const start = (safePage - 1) * pageSize
  const pageRows = rows.slice(start, start + pageSize)

  return (
    <div className="rounded-2xl border border-[#f0ebe3] overflow-hidden">
      <table className="w-full text-sm">
        <thead className="bg-[#faf7f1] text-[#6f655b]">
          <tr>
            {columns.map((col) => (
              <th key={col.key} className="px-3 py-2 text-left font-medium">{col.title}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {pageRows.map((row) => (
            <tr key={row[rowKey]} className="border-t border-[#f1ece4]">
              {columns.map((col) => (
                <td key={col.key} className="px-3 py-2 text-[#3a352f]">
                  {col.render ? col.render(row) : row[col.key]}
                </td>
              ))}
            </tr>
          ))}
          {pageRows.length === 0 && (
            <tr>
              <td colSpan={columns.length} className="px-3 py-8 text-center text-[#9b9187]">{emptyText}</td>
            </tr>
          )}
        </tbody>
      </table>
      <div className="flex items-center justify-end gap-2 border-t border-[#f1ece4] bg-white px-3 py-2 text-xs">
        <button onClick={() => setPage((p) => Math.max(1, p - 1))} className="rounded border border-[#e8dfd3] px-2 py-1">上一页</button>
        <span>{safePage}/{totalPages}</span>
        <button onClick={() => setPage((p) => Math.min(totalPages, p + 1))} className="rounded border border-[#e8dfd3] px-2 py-1">下一页</button>
      </div>
    </div>
  )
}

function AdminView({ page, messageFilter, onNavigate }) {
  const [leadRows, setLeadRows] = useState(conversionLeads)
  const [orderRows, setOrderRows] = useState(conversionOrders)
  const [adjustmentRows, setAdjustmentRows] = useState(adjustmentRequests)
  const [studentRows, setStudentRows] = useState(students)
  const [teacherRows, setTeacherRows] = useState(teachers)
  const [packageRows, setPackageRows] = useState(coursePackages)
  const [activeLeadId, setActiveLeadId] = useState(conversionLeads[0].id)
  const [activeRequestId, setActiveRequestId] = useState(adjustmentRequests[0].id)
  const [activeStudentId, setActiveStudentId] = useState(students[0].id)
  const [activeTeacherId, setActiveTeacherId] = useState(teachers[0].id)
  const [searchLeadKeyword, setSearchLeadKeyword] = useState('')
  const [flowTip, setFlowTip] = useState('')
  const [selectedOrderIds, setSelectedOrderIds] = useState([])
  const [messageDrawerOpen, setMessageDrawerOpen] = useState(false)
  const [activeMessageId, setActiveMessageId] = useState(notifications[0].id)
  const [leadDrawerOpen, setLeadDrawerOpen] = useState(false)
  const [showBookingQr, setShowBookingQr] = useState(false)
  const [showArchiveForm, setShowArchiveForm] = useState(false)
  const [studentDrawerOpen, setStudentDrawerOpen] = useState(false)
  const [adjustmentDrawerOpen, setAdjustmentDrawerOpen] = useState(false)
  const [adjustmentRebindModalOpen, setAdjustmentRebindModalOpen] = useState(false)
  const [newStudentDrawerOpen, setNewStudentDrawerOpen] = useState(false)
  const [teacherDrawerOpen, setTeacherDrawerOpen] = useState(false)
  const [newTeacherDrawerOpen, setNewTeacherDrawerOpen] = useState(false)
  const [packageDrawerOpen, setPackageDrawerOpen] = useState(false)
  const [newPackageDrawerOpen, setNewPackageDrawerOpen] = useState(false)
  const [newStudentForm, setNewStudentForm] = useState({
    name: '',
    gender: '男',
    phone: '',
    guitarCategory: '木吉他',
    signupTime: '2026-03-24',
    paymentAmount: '0',
    benefits: ''
  })
  const [newTeacherForm, setNewTeacherForm] = useState({
    name: '',
    employeeNo: '',
    phone: '',
    entryDate: '2026-03-24',
    teachingType: '团课',
    teachingQualification: '',
    expertiseTrack: '木吉他',
    level: 'B',
    campus: '北环国基路校区'
  })
  const [newPackageForm, setNewPackageForm] = useState({
    type: '正式课程',
    name: '',
    displayPrice: '0',
    salePrice: '0',
    stock: '0',
    validDays: '365',
    validUntil: '2027-03-31'
  })
  const [scheduleRoomRows, setScheduleRoomRows] = useState([
    { id: 'ROOM-001', campus: '北环国基路校区', name: 'A101', type: '标准教室', capacity: 6, status: '启用' },
    { id: 'ROOM-002', campus: '北环国基路校区', name: 'A203', type: '一对一教室', capacity: 2, status: '启用' },
    { id: 'ROOM-003', campus: '西大剧院校区', name: 'B203', type: '标准教室', capacity: 8, status: '启用' },
    { id: 'ROOM-004', campus: '西大剧院校区', name: 'B205', type: '合奏教室', capacity: 10, status: '维修中' }
  ])
  const [scheduleRoomCampus, setScheduleRoomCampus] = useState('全部校区')
  const [scheduleRoomModalOpen, setScheduleRoomModalOpen] = useState(false)
  const [scheduleRoomEditingId, setScheduleRoomEditingId] = useState('')
  const [scheduleRoomForm, setScheduleRoomForm] = useState({
    campus: '北环国基路校区',
    name: '',
    type: '标准教室',
    capacity: '6',
    status: '启用'
  })
  const [scheduleTemplateRows, setScheduleTemplateRows] = useState([
    { id: 'TM-001', name: '团购体验课模板', courseType: '团购体验课', subject: '木吉他', consumeHours: 1, capacityMin: 3, capacityMax: 5, teachingType: '团课', ageGroup: '未成年', needStudentBinding: false, status: '启用' },
    { id: 'TM-002', name: '标准课模板', courseType: '标准课', subject: '木吉他', consumeHours: 1, capacityMin: 3, capacityMax: 5, teachingType: '团课', ageGroup: '成人', needStudentBinding: false, status: '启用' },
    { id: 'TM-003', name: '一对一模板', courseType: '一对一', subject: '贝斯', consumeHours: 1, capacityMin: 1, capacityMax: 1, teachingType: '一对一', ageGroup: '不限', needStudentBinding: true, status: '启用' }
  ])
  const [scheduleTemplateModalOpen, setScheduleTemplateModalOpen] = useState(false)
  const [scheduleTemplateEditingId, setScheduleTemplateEditingId] = useState('')
  const [scheduleTemplateForm, setScheduleTemplateForm] = useState({
    name: '',
    courseType: '标准课',
    subject: '木吉他',
    consumeHours: '1',
    capacityMin: '3',
    capacityMax: '5',
    teachingType: '团课',
    ageGroup: '成人',
    needStudentBinding: false,
    status: '启用'
  })
  const [scheduleCalendarView, setScheduleCalendarView] = useState('week')
  const [scheduleSelectedDate, setScheduleSelectedDate] = useState('2026-03-24')
  const [scheduleArrangeModalOpen, setScheduleArrangeModalOpen] = useState(false)
  const [schedulePlanForm, setSchedulePlanForm] = useState({
    campus: '北环国基路校区',
    ageGroup: '成人',
    templateId: 'TM-002',
    timeSlot: '',
    teacherId: '',
    roomId: '',
    studentName: ''
  })
  const [scheduleCandidateRows, setScheduleCandidateRows] = useState([])
  const [schedulePlanRows, setSchedulePlanRows] = useState([
    { id: 'SCH-001', date: '2026-03-24', timeSlot: '14:00-15:00', templateName: '团购体验课模板', templateType: '团购体验课', teacherId: 'T-01', teacher: '刘老师', roomId: 'ROOM-001', room: 'A101', campus: '北环国基路校区', ageGroup: '未成年', reservedCount: 4, capacityMax: 5, studentName: '' },
    { id: 'SCH-002', date: '2026-03-24', timeSlot: '15:00-16:00', templateName: '标准课模板', templateType: '标准课', teacherId: 'T-02', teacher: '陈老师', roomId: 'ROOM-003', room: 'B203', campus: '西大剧院校区', ageGroup: '成人', reservedCount: 3, capacityMax: 5, studentName: '' },
    { id: 'SCH-003', date: '2026-03-25', timeSlot: '18:00-19:00', templateName: '一对一模板', templateType: '一对一', teacherId: 'T-05', teacher: '赵老师', roomId: 'ROOM-002', room: 'A203', campus: '北环国基路校区', ageGroup: '成人', reservedCount: 1, capacityMax: 1, studentName: '王星河' }
  ])
  const [noticeRuleRows, setNoticeRuleRows] = useState([
    { id: 'R-001', category: '上课提醒', remindAhead: '提前1天、提前6小时', channels: ['站内消息', '小程序订阅消息', '微信模板消息', '短信'], enabled: true },
    { id: 'R-002', category: '作业提醒', remindAhead: '截止前24小时、截止后2小时', channels: ['站内消息', '小程序订阅消息'], enabled: true },
    { id: 'R-003', category: '核销未预约提醒', remindAhead: '核销后第1/3/7天', channels: ['微信模板消息', '短信'], enabled: true },
    { id: 'R-004', category: '课时不足/到期提醒', remindAhead: '剩余≤2课时即时、到期前7天', channels: ['站内消息', '微信模板消息'], enabled: true },
    { id: 'R-005', category: '调课审核结果提醒', remindAhead: '审批完成后即时', channels: ['站内消息', '短信'], enabled: true }
  ])
  const [noticeTemplateRows, setNoticeTemplateRows] = useState([
    { id: 'T-001', name: '上课提醒模板', scene: '上课提醒', channels: ['站内消息', '短信'], content: '您有一节课程将在{time}开始，请提前到校。', updatedAt: '2026-03-24 10:15' },
    { id: 'T-002', name: '作业提醒模板', scene: '作业提醒', channels: ['站内消息', '小程序订阅消息'], content: '作业{homework}将在{deadline}截止，请及时提交。', updatedAt: '2026-03-23 18:22' },
    { id: 'T-003', name: '核销未预约提醒模板', scene: '核销未预约提醒', channels: ['微信模板消息', '短信'], content: '您的体验课资格已激活，点击链接选择上课时间。', updatedAt: '2026-03-21 09:40' }
  ])
  const [touchRecordRows, setTouchRecordRows] = useState([
    { id: 'D-001', scene: '上课提醒', target: '张小满', channel: '短信', triggerAt: '2026-03-24 08:00', result: '发送成功' },
    { id: 'D-002', scene: '作业提醒', target: '李予安', channel: '小程序订阅消息', triggerAt: '2026-03-23 20:00', result: '已触达' },
    { id: 'D-003', scene: '核销未预约提醒', target: '林知夏', channel: '微信模板消息', triggerAt: '2026-03-22 10:00', result: '用户点击' }
  ])
  const [ruleModalOpen, setRuleModalOpen] = useState(false)
  const [templateModalOpen, setTemplateModalOpen] = useState(false)
  const [activeRuleId, setActiveRuleId] = useState('R-001')
  const [editingTemplateId, setEditingTemplateId] = useState('')
  const [ruleForm, setRuleForm] = useState({ category: '', remindAhead: '', channels: ['站内消息'], enabled: true })
  const [templateForm, setTemplateForm] = useState({ name: '', scene: '上课提醒', channels: ['站内消息'], content: '' })
  const [orgCampusRows, setOrgCampusRows] = useState([
    { id: 'CAMPUS-1', code: 'BHGJ', name: '北环国基路校区', principal: '李校长', phone: '0371-66112233', address: '金水区北环路88号', roomCount: 8, status: '启用', createdAt: '2024-08-01' },
    { id: 'CAMPUS-2', code: 'XDJY', name: '西大剧院校区', principal: '周校长', phone: '0371-66778899', address: '中原区建设路66号', roomCount: 5, status: '启用', createdAt: '2025-01-12' }
  ])
  const [orgCampusModalOpen, setOrgCampusModalOpen] = useState(false)
  const [orgCampusEditingId, setOrgCampusEditingId] = useState('')
  const [orgCampusForm, setOrgCampusForm] = useState({
    code: '',
    name: '',
    principal: '',
    phone: '',
    address: '',
    roomCount: '0',
    status: '启用'
  })
  const [orgAccountRows, setOrgAccountRows] = useState([
    { id: 'ACC-1', username: 'admin_li', name: '李教务', phone: '13800110001', status: '启用', roleIds: ['ROLE-ADMIN'], campusId: 'CAMPUS-1' },
    { id: 'ACC-2', username: 'op_zhou', name: '周教务', phone: '13800110002', status: '启用', roleIds: ['ROLE-OP'], campusId: 'CAMPUS-2' }
  ])
  const [orgAccountModalOpen, setOrgAccountModalOpen] = useState(false)
  const [orgAccountEditingId, setOrgAccountEditingId] = useState('')
  const [orgAccountForm, setOrgAccountForm] = useState({
    username: '',
    name: '',
    phone: '',
    status: '启用',
    roleIds: ['ROLE-OP'],
    campusId: 'CAMPUS-1'
  })
  const [orgRoleRows, setOrgRoleRows] = useState([
    { id: 'ROLE-ADMIN', name: '校区负责人', dataScope: 'CAMPUS', permissionKeys: ['tab.org-campus', 'tab.org-tree', 'tab.org-rbac', 'tab.org-account', 'op.org-campus.create', 'op.org-campus.edit', 'op.org-tree.edit', 'op.org-tree.delete', 'op.org-tree.bind', 'op.account.create', 'op.account.edit', 'op.account.disable', 'op.role.create', 'op.role.edit'], updatedAt: '2026-03-24 09:30' },
    { id: 'ROLE-OP', name: '教务', dataScope: 'ASSIGNED', permissionKeys: ['tab.org-campus', 'tab.org-tree', 'tab.org-account', 'op.org-tree.bind', 'op.account.edit'], updatedAt: '2026-03-23 18:00' },
    { id: 'ROLE-TEACHER', name: '教师', dataScope: 'SELF', permissionKeys: ['tab.teacher-dashboard'], updatedAt: '2026-03-20 11:40' }
  ])
  const [orgRoleModalOpen, setOrgRoleModalOpen] = useState(false)
  const [orgRoleEditingId, setOrgRoleEditingId] = useState('')
  const [orgRoleForm, setOrgRoleForm] = useState({
    name: '',
    dataScope: 'ASSIGNED',
    permissionKeys: []
  })
  const [orgPermissionSubTab, setOrgPermissionSubTab] = useState('role-manage')
  const [orgTreeNodes, setOrgTreeNodes] = useState([
    {
      id: 'NODE-C1',
      name: '北环国基路校区',
      type: 'CAMPUS',
      manager: '李校长',
      status: '启用',
      accountIds: ['ACC-1'],
      children: [
        {
          id: 'NODE-D1',
          name: '教务部',
          type: 'DEPT',
          manager: '王教务',
          status: '启用',
          accountIds: ['ACC-1'],
          children: [
            { id: 'NODE-G1', name: '吉他教学组A', type: 'GROUP', manager: '刘老师', status: '启用', accountIds: [], children: [] }
          ]
        }
      ]
    },
    {
      id: 'NODE-C2',
      name: '西大剧院校区',
      type: 'CAMPUS',
      manager: '周校长',
      status: '启用',
      accountIds: ['ACC-2'],
      children: [
        {
          id: 'NODE-D2',
          name: '教学运营部',
          type: 'DEPT',
          manager: '陈主管',
          status: '启用',
          accountIds: ['ACC-2'],
          children: [
            { id: 'NODE-G2', name: '贝斯进阶组', type: 'GROUP', manager: '赵老师', status: '启用', accountIds: [], children: [] }
          ]
        }
      ]
    }
  ])
  const [orgExpandedNodeIds, setOrgExpandedNodeIds] = useState(['NODE-C1', 'NODE-D1', 'NODE-C2', 'NODE-D2'])
  const [orgNodeModalOpen, setOrgNodeModalOpen] = useState(false)
  const [orgNodeEditingId, setOrgNodeEditingId] = useState('')
  const [orgNodeForm, setOrgNodeForm] = useState({ name: '', manager: '', status: '启用' })
  const [orgBindModalOpen, setOrgBindModalOpen] = useState(false)
  const [orgBindNodeId, setOrgBindNodeId] = useState('')
  const [orgBindAccountIds, setOrgBindAccountIds] = useState([])
  const [archiveForm, setArchiveForm] = useState({
    name: '',
    age: '',
    gender: '男',
    phone: '',
    parentName: '',
    hasBasic: '无',
    basicDescription: '',
    intendedSubject: '木吉他',
    learningPurpose: '',
    expectedClassTime: '',
    expectedClassLocation: '',
    groupCoursePackage: '',
    channel: '',
    hasInstrument: '有',
    signupHours: '',
    formalTeacher: '',
    isReferral: '否'
  })
  const activeLead = leadRows.find((item) => item.id === activeLeadId) || leadRows[0]
  const activeRequest = adjustmentRows.find((item) => item.id === activeRequestId) || adjustmentRows[0]
  const activeStudent = studentRows.find((item) => item.id === activeStudentId)
  const activeTeacher = teacherRows.find((item) => item.id === activeTeacherId) || teacherRows[0]
  const [activePackageId, setActivePackageId] = useState(coursePackages[0].id)
  const activePackage = packageRows.find((item) => item.id === activePackageId) || packageRows[0]
  const activeMessage = notifications.find((item) => item.id === activeMessageId) || notifications[0]
  const [orgPreviewAccountId, setOrgPreviewAccountId] = useState('ACC-1')
  const [orgPermExpandedIds, setOrgPermExpandedIds] = useState(['perm-root', 'perm-l1-org', 'perm-l2-org-account'])
  const permissionCatalog = [
    {
      id: 'perm-root',
      key: 'perm-root',
      label: '系统权限树',
      children: [
        {
          id: 'perm-l1-org',
          key: 'tab.l1.org',
          label: '一级Tab：组织与权限',
          children: [
            {
              id: 'perm-l2-org-campus',
              key: 'tab.org-campus',
              label: '二级Tab：校区管理',
              children: [
                { id: 'perm-op-campus-create', key: 'op.org-campus.create', label: '按钮：创建校区', children: [] },
                { id: 'perm-op-campus-edit', key: 'op.org-campus.edit', label: '按钮：修改校区', children: [] }
              ]
            },
            {
              id: 'perm-l2-org-tree',
              key: 'tab.org-tree',
              label: '二级Tab：组织树管理',
              children: [
                { id: 'perm-op-tree-edit', key: 'op.org-tree.edit', label: '按钮：修改节点', children: [] },
                { id: 'perm-op-tree-delete', key: 'op.org-tree.delete', label: '按钮：删除节点', children: [] },
                { id: 'perm-op-tree-bind', key: 'op.org-tree.bind', label: '按钮：挂载绑定教务账号', children: [] }
              ]
            },
            {
              id: 'perm-l2-org-account',
              key: 'tab.org-account',
              label: '二级Tab：账号管理',
              children: [
                { id: 'perm-op-account-create', key: 'op.account.create', label: '按钮：创建账号', children: [] },
                { id: 'perm-op-account-edit', key: 'op.account.edit', label: '按钮：修改账号', children: [] },
                { id: 'perm-op-account-disable', key: 'op.account.disable', label: '按钮：禁用账号', children: [] }
              ]
            },
            {
              id: 'perm-l2-org-rbac',
              key: 'tab.org-rbac',
              label: '二级Tab：角色与数据权限',
              children: [
                { id: 'perm-op-role-create', key: 'op.role.create', label: '按钮：创建角色', children: [] },
                { id: 'perm-op-role-edit', key: 'op.role.edit', label: '按钮：修改角色', children: [] }
              ]
            }
          ]
        }
      ]
    }
  ]

  const shortcuts = [
    { label: '快速排课', to: '/admin/schedule-calendar', icon: CalendarDays, card: 'bg-blue-50 text-[#4f46e5] hover:bg-blue-100' },
    { label: '团购核销', to: '/admin/conversion-import', icon: ShoppingBag, card: 'bg-orange-50 text-[#f97316] hover:bg-orange-100' },
    { label: '新增学员', to: '/admin/students-list', icon: Users, card: 'bg-green-50 text-[#16a34a] hover:bg-green-100' },
    { label: '发布公告', to: '/admin/notice-templates', icon: BookOpen, card: 'bg-purple-50 text-[#9333ea] hover:bg-purple-100' }
  ]

  const todoItems = [
    { id: 'T-01', content: '核销未预约', value: '8人', action: '发送提醒' },
    { id: 'T-02', content: '待审核调课/取消', value: '5单', action: '去审批' },
    { id: 'T-03', content: '即将到期学员', value: '11人', action: '查看名单' },
    { id: 'T-04', content: '异常/低活跃', value: '一期占位', action: '规则预留' }
  ]

  const messageFilterOptions = ['全部', '未读', '待办提醒', '审批结果', '系统公告', '触达结果']
  const filteredNotifications = notifications.filter((item) => {
    if (messageFilter === '全部') return true
    if (messageFilter === '未读') return !item.read
    return item.category === messageFilter
  })

  const importSummary = {
    total: orderRows.length,
    missingPhone: orderRows.filter((item) => item.validation === '手机号缺失').length,
    duplicateOrder: orderRows.filter((item) => item.validation === '重复订单号').length,
    missingCampus: orderRows.filter((item) => item.validation === '校区缺失').length
  }

  const toggleOrderSelection = (orderId) => {
    setSelectedOrderIds((prev) =>
      prev.includes(orderId) ? prev.filter((id) => id !== orderId) : [...prev, orderId]
    )
  }

  const batchMark = (validation, statusText = '导入失败') => {
    if (selectedOrderIds.length === 0) return
    setOrderRows((prev) =>
      prev.map((row) =>
        selectedOrderIds.includes(row.id)
          ? { ...row, validation, status: statusText }
          : row
      )
    )
    setFlowTip(`已处理 ${selectedOrderIds.length} 条订单：${validation}`)
    setSelectedOrderIds([])
  }

  const runImport = () => {
    const newOrder = {
      id: `O-${String(orderRows.length + 1).padStart(3, '0')}`,
      orderNo: `DY${Date.now().toString().slice(-9)}`,
      platform: '抖音',
      studentName: '新导入学员',
      phone: '13500107890',
      campus: '北环国基路校区',
      course: '团购体验课',
      purchasedAt: '2026-03-24 14:20',
      status: '待核销',
      validation: '通过'
    }
    setOrderRows((prev) => [newOrder, ...prev])
    setFlowTip('导入完成：新增1条待核销订单，已进入列表')
  }

  const leadRowsFiltered = leadRows.filter((item) =>
    [item.name, item.phone, item.coupon].join(' ').includes(searchLeadKeyword)
  )

  const writeOffLead = () => {
    if (!activeLead) return
    setLeadRows((prev) =>
      prev.map((item) =>
        item.id === activeLead.id && item.status === '已导入待核销'
          ? { ...item, status: '已核销待建档', timeline: [...item.timeline, '已核销'] }
          : item
      )
    )
    setFlowTip(`已完成核销登记：${activeLead.name}`)
    setLeadDrawerOpen(true)
  }

  const openArchiveForm = () => {
    if (!activeLead) return
    setArchiveForm({
      name: activeLead.name || '',
      age: '',
      gender: '男',
      phone: activeLead.phone || '',
      parentName: '',
      hasBasic: '无',
      basicDescription: '',
      intendedSubject: activeLead.instrument || '木吉他',
      learningPurpose: '',
      expectedClassTime: '',
      expectedClassLocation: activeLead.campus || '',
      groupCoursePackage: '体验课2节包',
      channel: activeLead.platform || '',
      hasInstrument: '有',
      signupHours: '24',
      formalTeacher: '',
      isReferral: '否'
    })
    setShowArchiveForm(true)
  }

  const archiveLead = () => {
    if (!activeLead) return
    if (!archiveForm.name || !archiveForm.phone || !archiveForm.intendedSubject) {
      setFlowTip('请先完整填写建档信息（姓名/手机号/意向科目）')
      return
    }
    setLeadRows((prev) =>
      prev.map((item) =>
        item.id === activeLead.id && item.status.includes('待建档')
          ? { ...item, status: '已建档待预约', timeline: [...item.timeline, '已建档'] }
          : item
      )
    )
    const newStudent = {
      id: `S-${String(studentRows.length + 1).padStart(3, '0')}`,
      name: archiveForm.name,
      age: archiveForm.age,
      gender: archiveForm.gender,
      phone: archiveForm.phone,
      parentName: archiveForm.parentName,
      hasBasic: archiveForm.hasBasic,
      basicDescription: archiveForm.basicDescription,
      intendedSubject: archiveForm.intendedSubject,
      goal: archiveForm.learningPurpose || '待教务补充',
      progressTags: ['新建档', '待首课'],
      packageName: archiveForm.groupCoursePackage || '体验课2节包',
      remaining: Number(archiveForm.signupHours || 0),
      classRecords: ['暂无上课记录'],
      homeworkRecords: ['暂无作业记录'],
      signupTime: '2026-03-24',
      paymentAmount: 0,
      benefits: '建档后赠送预约提醒服务',
      guitarCategory: archiveForm.intendedSubject,
      expectedClassTime: archiveForm.expectedClassTime,
      expectedClassLocation: archiveForm.expectedClassLocation,
      channel: archiveForm.channel,
      hasInstrument: archiveForm.hasInstrument,
      formalTeacher: archiveForm.formalTeacher || '待分配',
      isReferral: archiveForm.isReferral,
      registrationFormSummary: `家长：${archiveForm.parentName || '未填'}；课程：${archiveForm.groupCoursePackage || '未填'}；目的：${archiveForm.learningPurpose || '未填'}`,
      tags: ['新学员', archiveForm.intendedSubject]
    }
    setStudentRows((prev) => [newStudent, ...prev])
    setActiveStudentId(newStudent.id)
    setFlowTip(`已为 ${activeLead.name} 完成建档并创建账号`)
    setShowArchiveForm(false)
    setLeadDrawerOpen(true)
  }

  const sendBookingLink = () => {
    if (!activeLead) return
    const now = '2026-03-24 15:08'
    setLeadRows((prev) =>
      prev.map((item) =>
        item.id === activeLead.id
          ? { ...item, linkSentAt: now }
          : item
      )
    )
    setFlowTip(`预约链接已发送：${activeLead.name}（微信小程序+短信）`)
    setShowBookingQr(true)
    setLeadDrawerOpen(true)
  }

  const createStudent = () => {
    if (!newStudentForm.name || !newStudentForm.phone) {
      setFlowTip('请填写新建学生的姓名和手机号')
      return
    }
    const student = {
      id: `S-${String(studentRows.length + 1).padStart(3, '0')}`,
      name: newStudentForm.name,
      age: '',
      gender: newStudentForm.gender,
      phone: newStudentForm.phone,
      parentName: '',
      hasBasic: '待补充',
      basicDescription: '',
      intendedSubject: newStudentForm.guitarCategory,
      goal: '待补充学习目标',
      progressTags: ['新建'],
      packageName: '待选择',
      remaining: 0,
      classRecords: ['暂无上课记录'],
      homeworkRecords: ['暂无作业记录'],
      signupTime: newStudentForm.signupTime,
      paymentAmount: Number(newStudentForm.paymentAmount || 0),
      benefits: newStudentForm.benefits || '待补充',
      guitarCategory: newStudentForm.guitarCategory,
      expectedClassTime: '',
      expectedClassLocation: '',
      channel: '门店',
      hasInstrument: '待补充',
      formalTeacher: '待分配',
      isReferral: '否',
      registrationFormSummary: '待完成预约登记表',
      tags: ['新建']
    }
    setStudentRows((prev) => [student, ...prev])
    setNewStudentDrawerOpen(false)
    setNewStudentForm({
      name: '',
      gender: '男',
      phone: '',
      guitarCategory: '木吉他',
      signupTime: '2026-03-24',
      paymentAmount: '0',
      benefits: ''
    })
    setFlowTip(`已新建学生：${student.name}`)
  }

  const createTeacher = () => {
    if (!newTeacherForm.name || !newTeacherForm.employeeNo || !newTeacherForm.phone) {
      setFlowTip('请填写教师姓名、工号和联系方式')
      return
    }
    const teacher = {
      id: `T-${String(teacherRows.length + 1).padStart(2, '0')}`,
      name: newTeacherForm.name,
      employeeNo: newTeacherForm.employeeNo,
      phone: newTeacherForm.phone,
      entryDate: newTeacherForm.entryDate,
      teachingType: newTeacherForm.teachingType,
      teachingQualification: newTeacherForm.teachingQualification || '待补充',
      expertiseTracks: [newTeacherForm.expertiseTrack],
      level: newTeacherForm.level,
      campus: newTeacherForm.campus,
      status: '在职',
      subject: newTeacherForm.expertiseTrack,
      capacity: '团课 5 人',
      available: '待排班',
      boundStudents: []
    }
    setTeacherRows((prev) => [teacher, ...prev])
    setActiveTeacherId(teacher.id)
    setNewTeacherDrawerOpen(false)
    setNewTeacherForm({
      name: '',
      employeeNo: '',
      phone: '',
      entryDate: '2026-03-24',
      teachingType: '团课',
      teachingQualification: '',
      expertiseTrack: '木吉他',
      level: 'B',
      campus: '北环国基路校区'
    })
    setFlowTip(`已新建教师：${teacher.name}`)
  }

  const toggleFreezeTeacher = (teacherId) => {
    setTeacherRows((prev) =>
      prev.map((item) =>
        item.id === teacherId
          ? { ...item, status: item.status === '冻结' ? '在职' : '冻结' }
          : item
      )
    )
    const target = teacherRows.find((item) => item.id === teacherId)
    if (target) {
      setFlowTip(`${target.name} 已${target.status === '冻结' ? '恢复在职' : '冻结'}`)
    }
  }

  const deleteTeacher = (teacherId) => {
    const target = teacherRows.find((item) => item.id === teacherId)
    if (!target) return
    setTeacherRows((prev) => prev.filter((item) => item.id !== teacherId))
    if (activeTeacherId === teacherId && teacherRows.length > 1) {
      const nextTeacher = teacherRows.find((item) => item.id !== teacherId)
      if (nextTeacher) setActiveTeacherId(nextTeacher.id)
    }
    setFlowTip(`已删除教师：${target.name}`)
    setTeacherDrawerOpen(false)
  }

  const createPackage = () => {
    if (!newPackageForm.name) {
      setFlowTip('请填写课包名称')
      return
    }
    const pack = {
      id: `P-${packageRows.length + 1}`,
      type: newPackageForm.type,
      name: newPackageForm.name,
      displayPrice: Number(newPackageForm.displayPrice || 0),
      salePrice: Number(newPackageForm.salePrice || 0),
      stock: Number(newPackageForm.stock || 0),
      validDays: Number(newPackageForm.validDays || 0),
      validUntil: newPackageForm.validUntil,
      status: '上架',
      soldCount: 0,
      canCancel: true
    }
    setPackageRows((prev) => [pack, ...prev])
    setActivePackageId(pack.id)
    setNewPackageDrawerOpen(false)
    setNewPackageForm({
      type: '正式课程',
      name: '',
      displayPrice: '0',
      salePrice: '0',
      stock: '0',
      validDays: '365',
      validUntil: '2027-03-31'
    })
    setFlowTip(`已新建课包：${pack.name}`)
  }

  const togglePackageSale = (packageId) => {
    setPackageRows((prev) =>
      prev.map((item) =>
        item.id === packageId
          ? { ...item, status: item.status === '上架' ? '下架' : '上架' }
          : item
      )
    )
    const target = packageRows.find((item) => item.id === packageId)
    if (target) setFlowTip(`${target.name} 已${target.status === '上架' ? '下架停售' : '重新上架'}`)
  }

  const cancelPackage = (packageId) => {
    const target = packageRows.find((item) => item.id === packageId)
    if (!target) return
    setPackageRows((prev) =>
      prev.map((item) =>
        item.id === packageId ? { ...item, status: '已取消', canCancel: false } : item
      )
    )
    setFlowTip(`已取消课包：${target.name}`)
    setPackageDrawerOpen(false)
  }

  const openRuleModal = (rule) => {
    setActiveRuleId(rule.id)
    setRuleForm({
      category: rule.category,
      remindAhead: rule.remindAhead,
      channels: rule.channels,
      enabled: rule.enabled
    })
    setRuleModalOpen(true)
  }

  const saveRule = () => {
    setNoticeRuleRows((prev) =>
      prev.map((item) =>
        item.id === activeRuleId ? { ...item, ...ruleForm } : item
      )
    )
    setRuleModalOpen(false)
    setFlowTip(`已更新提醒规则：${ruleForm.category}`)
  }

  const openTemplateModal = (template) => {
    if (!template) {
      setEditingTemplateId('')
      setTemplateForm({ name: '', scene: '上课提醒', channels: ['站内消息'], content: '' })
      setTemplateModalOpen(true)
      return
    }
    setEditingTemplateId(template.id)
    setTemplateForm({
      name: template.name,
      scene: template.scene,
      channels: template.channels,
      content: template.content
    })
    setTemplateModalOpen(true)
  }

  const saveTemplate = () => {
    if (!templateForm.name || !templateForm.content) {
      setFlowTip('请填写模板名称和模板内容')
      return
    }
    if (editingTemplateId) {
      setNoticeTemplateRows((prev) =>
        prev.map((item) =>
          item.id === editingTemplateId
            ? { ...item, ...templateForm, updatedAt: '2026-03-24 16:40' }
            : item
        )
      )
      setFlowTip(`已更新模板：${templateForm.name}`)
    } else {
      const newTemplate = {
        id: `T-${String(noticeTemplateRows.length + 1).padStart(3, '0')}`,
        ...templateForm,
        updatedAt: '2026-03-24 16:40'
      }
      setNoticeTemplateRows((prev) => [newTemplate, ...prev])
      setFlowTip(`已新增模板：${templateForm.name}`)
    }
    setTemplateModalOpen(false)
  }

  const deleteTemplate = (templateId) => {
    const target = noticeTemplateRows.find((item) => item.id === templateId)
    if (!target) return
    setNoticeTemplateRows((prev) => prev.filter((item) => item.id !== templateId))
    setFlowTip(`已删除模板：${target.name}`)
  }

  const openCampusModal = (campus) => {
    if (!campus) {
      setOrgCampusEditingId('')
      setOrgCampusForm({ code: '', name: '', principal: '', phone: '', address: '', roomCount: '0', status: '启用' })
      setOrgCampusModalOpen(true)
      return
    }
    setOrgCampusEditingId(campus.id)
    setOrgCampusForm({
      code: campus.code,
      name: campus.name,
      principal: campus.principal,
      phone: campus.phone,
      address: campus.address,
      roomCount: String(campus.roomCount),
      status: campus.status
    })
    setOrgCampusModalOpen(true)
  }

  const saveCampus = () => {
    if (!orgCampusForm.name || !orgCampusForm.code) {
      setFlowTip('请填写校区编码与校区名称')
      return
    }
    if (orgCampusEditingId) {
      setOrgCampusRows((prev) =>
        prev.map((item) =>
          item.id === orgCampusEditingId
            ? { ...item, ...orgCampusForm, roomCount: Number(orgCampusForm.roomCount || 0) }
            : item
        )
      )
      setFlowTip(`已更新校区：${orgCampusForm.name}`)
    } else {
      const newCampus = {
        id: `CAMPUS-${orgCampusRows.length + 1}`,
        ...orgCampusForm,
        roomCount: Number(orgCampusForm.roomCount || 0),
        createdAt: '2026-03-24'
      }
      setOrgCampusRows((prev) => [newCampus, ...prev])
      setFlowTip(`已创建校区：${orgCampusForm.name}`)
    }
    setOrgCampusModalOpen(false)
  }

  const openAccountModal = (account) => {
    if (!account) {
      setOrgAccountEditingId('')
      setOrgAccountForm({ username: '', name: '', phone: '', status: '启用', roleIds: ['ROLE-OP'], campusId: orgCampusRows[0]?.id || 'CAMPUS-1' })
      setOrgAccountModalOpen(true)
      return
    }
    setOrgAccountEditingId(account.id)
    setOrgAccountForm({
      username: account.username,
      name: account.name,
      phone: account.phone,
      status: account.status,
      roleIds: account.roleIds,
      campusId: account.campusId
    })
    setOrgAccountModalOpen(true)
  }

  const saveAccount = () => {
    if (!orgAccountForm.username || !orgAccountForm.name) {
      setFlowTip('请填写账号用户名与姓名')
      return
    }
    if (orgAccountForm.roleIds.length === 0) {
      setFlowTip('请至少绑定一个角色')
      return
    }
    if (orgAccountEditingId) {
      setOrgAccountRows((prev) => prev.map((item) => (item.id === orgAccountEditingId ? { ...item, ...orgAccountForm } : item)))
      setFlowTip(`已更新账号：${orgAccountForm.name}`)
    } else {
      const newAccount = { id: `ACC-${orgAccountRows.length + 1}`, ...orgAccountForm }
      setOrgAccountRows((prev) => [newAccount, ...prev])
      setFlowTip(`已创建账号：${orgAccountForm.name}`)
    }
    setOrgAccountModalOpen(false)
  }

  const toggleAccountStatus = (accountId) => {
    setOrgAccountRows((prev) =>
      prev.map((item) => (item.id === accountId ? { ...item, status: item.status === '启用' ? '禁用' : '启用' } : item))
    )
  }

  const openRoleModal = (role) => {
    if (!role) {
      setOrgRoleEditingId('')
      setOrgRoleForm({ name: '', dataScope: 'ASSIGNED', permissionKeys: [] })
      setOrgRoleModalOpen(true)
      return
    }
    setOrgRoleEditingId(role.id)
    setOrgRoleForm({ name: role.name, dataScope: role.dataScope, permissionKeys: role.permissionKeys })
    setOrgRoleModalOpen(true)
  }

  const saveRole = () => {
    if (!orgRoleForm.name) {
      setFlowTip('请填写角色名称')
      return
    }
    if (orgRoleEditingId) {
      setOrgRoleRows((prev) =>
        prev.map((item) =>
          item.id === orgRoleEditingId ? { ...item, ...orgRoleForm, updatedAt: '2026-03-24 17:10' } : item
        )
      )
      setFlowTip(`已更新角色：${orgRoleForm.name}`)
    } else {
      const newRole = { id: `ROLE-${Date.now().toString().slice(-4)}`, ...orgRoleForm, updatedAt: '2026-03-24 17:10' }
      setOrgRoleRows((prev) => [newRole, ...prev])
      setFlowTip(`已创建角色：${orgRoleForm.name}`)
    }
    setOrgRoleModalOpen(false)
  }

  const updateTreeNodes = (nodes, targetId, updater) =>
    nodes.map((node) => {
      if (node.id === targetId) return updater(node)
      if (node.children?.length) return { ...node, children: updateTreeNodes(node.children, targetId, updater) }
      return node
    })

  const deleteTreeNode = (nodes, targetId) =>
    nodes
      .filter((node) => node.id !== targetId)
      .map((node) => (node.children?.length ? { ...node, children: deleteTreeNode(node.children, targetId) } : node))

  const toggleNodeExpand = (nodeId) => {
    setOrgExpandedNodeIds((prev) => (prev.includes(nodeId) ? prev.filter((id) => id !== nodeId) : [...prev, nodeId]))
  }

  const openNodeModal = (node) => {
    setOrgNodeEditingId(node.id)
    setOrgNodeForm({ name: node.name, manager: node.manager, status: node.status })
    setOrgNodeModalOpen(true)
  }

  const saveNode = () => {
    setOrgTreeNodes((prev) =>
      updateTreeNodes(prev, orgNodeEditingId, (node) => ({ ...node, ...orgNodeForm }))
    )
    setOrgNodeModalOpen(false)
    setFlowTip(`已更新组织节点：${orgNodeForm.name}`)
  }

  const removeNode = (nodeId) => {
    setOrgTreeNodes((prev) => deleteTreeNode(prev, nodeId))
    setFlowTip('已删除组织节点')
  }

  const openBindAccountModal = (node) => {
    setOrgBindNodeId(node.id)
    setOrgBindAccountIds(node.accountIds || [])
    setOrgBindModalOpen(true)
  }

  const saveBindAccounts = () => {
    if (!orgBindNodeId) return
    setOrgTreeNodes((prev) =>
      updateTreeNodes(prev, orgBindNodeId, (node) => ({
        ...node,
        accountIds: orgBindAccountIds
      }))
    )
    setOrgBindModalOpen(false)
    setFlowTip('已完成组织节点挂载绑定教务账号')
  }

  const [selectedRebindCourseId, setSelectedRebindCourseId] = useState('')
  const [adjustmentAuditLogRows, setAdjustmentAuditLogRows] = useState([
    { id: 'AL-001', time: '2026-03-24 09:20', student: '李予安', type: '调课', action: '确认重绑', result: '通过', detail: '3/25 19:00 标准课 → 周二 15:00-16:30 / 标准课：扫弦进阶' },
    { id: 'AL-002', time: '2026-03-24 10:05', student: '张小满', type: '取消', action: '申请审批', result: '通过', detail: '按规则退 1 课时' },
    { id: 'AL-003', time: '2026-03-24 11:30', student: '王星河', type: '调课', action: '申请审批', result: '驳回', detail: '时间冲突，建议改约周末' }
  ])

  const adjustmentCourseCandidates = scheduleCards.map((item) => ({
    id: item.id,
    title: item.title,
    time: item.time,
    teacher: item.teacher,
    room: item.room
  }))
  const selectedRebindCourse = adjustmentCourseCandidates.find((item) => item.id === selectedRebindCourseId)

  const saveAdjustmentDecision = (decision) => {
    if (!activeRequest) return
    if (decision === 'rebind') {
      if (activeRequest.type !== '调课') {
        setFlowTip('当前申请不是调课类型，无需重绑课程')
        return
      }
      const selected = adjustmentCourseCandidates.find((item) => item.id === selectedRebindCourseId)
      if (!selected) {
        setFlowTip('请先选择可预约课程进行重绑')
        return
      }
      setAdjustmentRows((prev) =>
        prev.map((item) =>
          item.id === activeRequest.id
            ? {
                ...item,
                expectTime: selected.time,
                impact: `已重绑：${selected.title} / ${selected.teacher}`,
                status: '已重绑待确认'
              }
            : item
        )
      )
      setAdjustmentAuditLogRows((prev) => [
        {
          id: `AL-${String(prev.length + 1).padStart(3, '0')}`,
          time: new Date().toLocaleString('zh-CN', { hour12: false }),
          student: activeRequest.student,
          type: activeRequest.type,
          action: '确认重绑',
          result: '通过',
          detail: `${activeRequest.originTime} → ${selected.time} / ${selected.title}`
        },
        ...prev
      ])
      setFlowTip(`已为 ${activeRequest.student} 重新绑定可预约课程`)
      setAdjustmentDrawerOpen(false)
      setAdjustmentRebindModalOpen(false)
      return
    }

    if (decision === 'approve') {
      setAdjustmentRows((prev) =>
        prev.map((item) => (item.id === activeRequest.id ? { ...item, status: '已同意待生效' } : item))
      )
      setAdjustmentAuditLogRows((prev) => [
        {
          id: `AL-${String(prev.length + 1).padStart(3, '0')}`,
          time: new Date().toLocaleString('zh-CN', { hour12: false }),
          student: activeRequest.student,
          type: activeRequest.type,
          action: '申请审批',
          result: '通过',
          detail: `${activeRequest.originTime} / ${activeRequest.impact}`
        },
        ...prev
      ])
      setFlowTip(`已同意申请：${activeRequest.student}`)
      setAdjustmentDrawerOpen(false)
      return
    }

    setAdjustmentRows((prev) =>
      prev.map((item) => (item.id === activeRequest.id ? { ...item, status: '已拒绝' } : item))
    )
    setAdjustmentAuditLogRows((prev) => [
      {
        id: `AL-${String(prev.length + 1).padStart(3, '0')}`,
        time: new Date().toLocaleString('zh-CN', { hour12: false }),
        student: activeRequest.student,
        type: activeRequest.type,
        action: '申请审批',
        result: '驳回',
        detail: `${activeRequest.originTime} / ${activeRequest.impact}`
      },
      ...prev
    ])
    setFlowTip(`已拒绝申请：${activeRequest.student}`)
    setAdjustmentDrawerOpen(false)
  }

  const openAdjustmentDrawer = (request) => {
    setActiveRequestId(request.id)
    setAdjustmentDrawerOpen(true)
  }

  const openAdjustmentRebind = (request) => {
    setActiveRequestId(request.id)
    setSelectedRebindCourseId('')
    setAdjustmentRebindModalOpen(true)
  }

  const scheduleCampusOptions = orgCampusRows.map((item) => item.name)
  const scheduleWeekDays = ['2026-03-23', '2026-03-24', '2026-03-25', '2026-03-26', '2026-03-27', '2026-03-28', '2026-03-29']
  const scheduleMonthDays = Array.from({ length: 31 }, (_, idx) => `2026-03-${String(idx + 1).padStart(2, '0')}`)
  const scheduleCalendarDays = scheduleCalendarView === 'week' ? scheduleWeekDays : scheduleMonthDays
  const scheduleTimeSlots = ['10:00-11:00', '14:00-15:00', '15:00-16:00', '16:00-17:00', '18:00-19:00', '19:00-20:00']
  const teacherAvailabilityMap = {
    'T-01': ['14:00-15:00', '15:00-16:00', '18:00-19:00', '19:00-20:00'],
    'T-02': ['10:00-11:00', '14:00-15:00', '15:00-16:00', '16:00-17:00'],
    'T-03': ['15:00-16:00', '16:00-17:00', '18:00-19:00'],
    'T-04': ['14:00-15:00', '15:00-16:00'],
    'T-05': ['10:00-11:00', '16:00-17:00', '18:00-19:00', '19:00-20:00']
  }

  const openScheduleRoomModal = (room) => {
    if (!room) {
      setScheduleRoomEditingId('')
      setScheduleRoomForm({
        campus: scheduleCampusOptions[0] || '北环国基路校区',
        name: '',
        type: '标准教室',
        capacity: '6',
        status: '启用'
      })
      setScheduleRoomModalOpen(true)
      return
    }
    setScheduleRoomEditingId(room.id)
    setScheduleRoomForm({
      campus: room.campus,
      name: room.name,
      type: room.type,
      capacity: String(room.capacity),
      status: room.status
    })
    setScheduleRoomModalOpen(true)
  }

  const saveScheduleRoom = () => {
    if (!scheduleRoomForm.name || !scheduleRoomForm.campus) {
      setFlowTip('请填写教室名称和所属校区')
      return
    }
    if (scheduleRoomEditingId) {
      setScheduleRoomRows((prev) =>
        prev.map((item) =>
          item.id === scheduleRoomEditingId
            ? { ...item, ...scheduleRoomForm, capacity: Number(scheduleRoomForm.capacity || 0) }
            : item
        )
      )
      setFlowTip(`已更新教室：${scheduleRoomForm.name}`)
    } else {
      const newRoom = {
        id: `ROOM-${String(scheduleRoomRows.length + 1).padStart(3, '0')}`,
        campus: scheduleRoomForm.campus,
        name: scheduleRoomForm.name,
        type: scheduleRoomForm.type,
        capacity: Number(scheduleRoomForm.capacity || 0),
        status: scheduleRoomForm.status
      }
      setScheduleRoomRows((prev) => [newRoom, ...prev])
      setFlowTip(`已创建教室：${newRoom.name}`)
    }
    setScheduleRoomModalOpen(false)
  }

  const openScheduleTemplateModal = (template) => {
    if (!template) {
      setScheduleTemplateEditingId('')
      setScheduleTemplateForm({
        name: '',
        courseType: '标准课',
        subject: '木吉他',
        consumeHours: '1',
        capacityMin: '3',
        capacityMax: '5',
        teachingType: '团课',
        ageGroup: '成人',
        needStudentBinding: false,
        status: '启用'
      })
      setScheduleTemplateModalOpen(true)
      return
    }
    setScheduleTemplateEditingId(template.id)
    setScheduleTemplateForm({
      name: template.name,
      courseType: template.courseType,
      subject: template.subject,
      consumeHours: String(template.consumeHours),
      capacityMin: String(template.capacityMin),
      capacityMax: String(template.capacityMax),
      teachingType: template.teachingType,
      ageGroup: template.ageGroup,
      needStudentBinding: template.needStudentBinding,
      status: template.status
    })
    setScheduleTemplateModalOpen(true)
  }

  const saveScheduleTemplate = () => {
    if (!scheduleTemplateForm.name) {
      setFlowTip('请填写模板名称')
      return
    }
    const nextTemplate = {
      ...scheduleTemplateForm,
      consumeHours: Number(scheduleTemplateForm.consumeHours || 0),
      capacityMin: Number(scheduleTemplateForm.capacityMin || 1),
      capacityMax: Number(scheduleTemplateForm.capacityMax || 1)
    }
    if (scheduleTemplateEditingId) {
      setScheduleTemplateRows((prev) =>
        prev.map((item) => (item.id === scheduleTemplateEditingId ? { ...item, ...nextTemplate } : item))
      )
      setFlowTip(`已更新课程模板：${nextTemplate.name}`)
    } else {
      const newTemplate = {
        id: `TM-${String(scheduleTemplateRows.length + 1).padStart(3, '0')}`,
        ...nextTemplate
      }
      setScheduleTemplateRows((prev) => [newTemplate, ...prev])
      setFlowTip(`已创建课程模板：${newTemplate.name}`)
    }
    setScheduleTemplateModalOpen(false)
  }

  const openScheduleArrangeModal = (date) => {
    setScheduleSelectedDate(date)
    setScheduleCandidateRows([])
    setSchedulePlanForm((prev) => ({
      ...prev,
      campus: scheduleCampusOptions[0] || prev.campus,
      timeSlot: '',
      teacherId: '',
      roomId: '',
      studentName: ''
    }))
    setScheduleArrangeModalOpen(true)
  }

  const searchScheduleCandidates = () => {
    const selectedTemplate = scheduleTemplateRows.find((item) => item.id === schedulePlanForm.templateId)
    if (!selectedTemplate) {
      setFlowTip('请先选择课程模板')
      return
    }
    const rows = scheduleTimeSlots.reduce((acc, slot) => {
      const teacherCandidates = teacherRows.filter((teacher) => {
        const sameCampus = teacher.campus === schedulePlanForm.campus
        const available = (teacherAvailabilityMap[teacher.id] || []).includes(slot)
        const subjectMatched = (teacher.expertiseTracks || []).includes(selectedTemplate.subject)
        const typeMatched = teacher.teachingType.includes(selectedTemplate.teachingType)
        const ageMatched = selectedTemplate.ageGroup === '不限' || selectedTemplate.ageGroup === schedulePlanForm.ageGroup
        if (!sameCampus || !available || !subjectMatched || !typeMatched || !ageMatched) return false
        const sameSlotRows = schedulePlanRows.filter((item) => item.date === scheduleSelectedDate && item.timeSlot === slot && item.teacherId === teacher.id)
        const occupiedCount = sameSlotRows.reduce((sum, item) => sum + (item.reservedCount || 0), 0)
        if (selectedTemplate.needStudentBinding) return occupiedCount === 0
        return occupiedCount < 5
      })
      if (teacherCandidates.length === 0) return acc
      const teacher = teacherCandidates[0]
      const roomCandidates = scheduleRoomRows.filter((room) => {
        if (room.campus !== schedulePlanForm.campus || room.status !== '启用') return false
        return !schedulePlanRows.some((item) => item.date === scheduleSelectedDate && item.timeSlot === slot && item.roomId === room.id)
      })
      if (roomCandidates.length === 0) return acc
      const room = roomCandidates[0]
      return [
        ...acc,
        {
          id: `${scheduleSelectedDate}-${slot}-${teacher.id}-${room.id}`,
          timeSlot: slot,
          teacherId: teacher.id,
          teacherName: teacher.name,
          roomId: room.id,
          roomName: room.name,
          matchDesc: `教师可授课+${selectedTemplate.subject}擅长+${schedulePlanForm.ageGroup}人群+教室空置`
        }
      ]
    }, [])
    setScheduleCandidateRows(rows)
    if (rows.length === 0) {
      setFlowTip('未搜索到可排课时间段，请调整校区/人群/模板后重试')
    } else {
      setFlowTip(`已搜索到 ${rows.length} 个可排课时间段`)
    }
  }

  const applyScheduleCandidate = (candidate) => {
    setSchedulePlanForm((prev) => ({
      ...prev,
      timeSlot: candidate.timeSlot,
      teacherId: candidate.teacherId,
      roomId: candidate.roomId
    }))
    setFlowTip(`已选择排课候选：${candidate.timeSlot} / ${candidate.teacherName} / ${candidate.roomName}`)
  }

  const submitSchedulePlan = () => {
    const selectedTemplate = scheduleTemplateRows.find((item) => item.id === schedulePlanForm.templateId)
    const selectedTeacher = teacherRows.find((item) => item.id === schedulePlanForm.teacherId)
    const selectedRoom = scheduleRoomRows.find((item) => item.id === schedulePlanForm.roomId)
    if (!selectedTemplate || !selectedTeacher || !selectedRoom || !schedulePlanForm.timeSlot) {
      setFlowTip('请先完成候选选择并绑定模板、老师、教室')
      return
    }
    if (selectedTemplate.needStudentBinding && !schedulePlanForm.studentName) {
      setFlowTip('一对一课程必须绑定学员')
      return
    }
    const newPlan = {
      id: `SCH-${String(schedulePlanRows.length + 1).padStart(3, '0')}`,
      date: scheduleSelectedDate,
      timeSlot: schedulePlanForm.timeSlot,
      templateName: selectedTemplate.name,
      templateType: selectedTemplate.courseType,
      teacherId: selectedTeacher.id,
      teacher: selectedTeacher.name,
      roomId: selectedRoom.id,
      room: selectedRoom.name,
      campus: schedulePlanForm.campus,
      ageGroup: schedulePlanForm.ageGroup,
      reservedCount: selectedTemplate.needStudentBinding ? 1 : selectedTemplate.capacityMin,
      capacityMax: selectedTemplate.capacityMax,
      studentName: selectedTemplate.needStudentBinding ? schedulePlanForm.studentName : ''
    }
    setSchedulePlanRows((prev) => [newPlan, ...prev])
    setScheduleArrangeModalOpen(false)
    setScheduleCandidateRows([])
    setFlowTip(`已完成排课：${newPlan.date} ${newPlan.timeSlot} ${newPlan.templateName}`)
  }

  if (page === 'workbench-overview') {
    const todayCourses = scheduleCards.slice(0, 4).map((item, index) => ({
      id: item.id,
      no: index + 1,
      time: item.time,
      title: item.title,
      teacher: item.teacher,
      room: item.room,
      status: index < 2 ? '待上课' : '准备中'
    }))
    return (
      <section className="space-y-5">
        <h2 className="text-sm font-semibold">工作台</h2>
        <div className="grid grid-cols-4 gap-4">
          {adminKpis.map((item) => (
            <article key={item.label} className="rounded-2xl border border-[#f0ebe3] bg-[#fffcf8] p-4">
              <div className="text-xs text-[#8b8177]">{item.label}</div>
              <div className="mt-2 text-3xl font-semibold text-[#2e2a25]">{item.value}</div>
              <div className="mt-2 text-xs text-[#bc7844]">{item.trend}</div>
            </article>
          ))}
        </div>
        <div className="grid grid-cols-[1.5fr_1fr] gap-4">
          <div>
            <div className="mb-2 text-sm font-semibold">待办</div>
            <PaginatedTable
              columns={[
                { key: 'content', title: '事项' },
                { key: 'value', title: '数量/状态' },
                { key: 'action', title: '下一步' }
              ]}
              rows={todoItems}
              pageSize={4}
            />
          </div>
          <div>
            <div className="mb-2 text-sm font-semibold">快捷入口</div>
            <div className="grid grid-cols-2 gap-3 rounded-2xl border border-[#f0ebe3] p-3">
              {shortcuts.map((item) => (
                <button
                  key={item.label}
                  onClick={() => onNavigate(item.to)}
                  className={`rounded-2xl border border-white/60 p-3 text-left transition ${item.card}`}
                >
                  <div className="mb-3 flex h-9 w-9 items-center justify-center rounded-xl bg-white">
                    <item.icon size={18} />
                  </div>
                  <div className="text-sm font-semibold">{item.label}</div>
                </button>
              ))}
            </div>
          </div>
        </div>
        <div>
          <div className="mb-2 text-sm font-semibold">今日待上课程</div>
          <PaginatedTable
            columns={[
              { key: 'no', title: '序号' },
              { key: 'time', title: '上课时间' },
              { key: 'title', title: '课程名称' },
              { key: 'teacher', title: '授课老师' },
              { key: 'room', title: '教室' },
              {
                key: 'status',
                title: '状态',
                render: (row) => (
                  <span className={`rounded-full px-2 py-0.5 text-xs ${row.status === '待上课' ? 'bg-[#fff4ea] text-[#b55e22]' : 'bg-[#faf8f4] text-[#8f8376]'}`}>
                    {row.status}
                  </span>
                )
              }
            ]}
            rows={todayCourses}
            pageSize={5}
          />
        </div>
      </section>
    )
  }

  if (page === 'messages-list') {
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">消息中心 / 消息列表</h2>
        <div className="mb-1 flex flex-wrap gap-2">
          {messageFilterOptions.map((item) => (
            <button
              key={item}
              onClick={() => onNavigate(`/admin/messages-list?filter=${item}`)}
              className={`rounded-full px-3 py-1 text-xs ${
                item === messageFilter ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#8f8376]'
              }`}
            >
              {item}
            </button>
          ))}
        </div>
        <PaginatedTable
          columns={[
            { key: 'title', title: '标题' },
            { key: 'category', title: '分类' },
            { key: 'content', title: '内容摘要' },
            { key: 'time', title: '时间' },
            {
              key: 'action',
              title: '操作',
              render: (row) => (
                <button
                  className="text-[#bc7844]"
                  onClick={() => {
                    setActiveMessageId(row.id)
                    setMessageDrawerOpen(true)
                  }}
                >
                  查看详情
                </button>
              )
            }
          ]}
          rows={filteredNotifications}
          pageSize={5}
        />
        {messageDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setMessageDrawerOpen(false)} />
            <div className="h-full w-[420px] bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">消息详情</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setMessageDrawerOpen(false)}>关闭</button>
              </div>
              <div className="space-y-3 text-sm">
                <div className="rounded-xl bg-[#faf8f4] p-3">
                  <div className="font-semibold">{activeMessage.title}</div>
                  <div className="mt-1 text-xs text-[#8f8376]">{activeMessage.category} · {activeMessage.time}</div>
                </div>
                <div className="rounded-xl bg-[#faf8f4] p-3">{activeMessage.content}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">关联对象：调课申请 A-203</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">跳转入口：调课/取消/补课</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">时间线：创建 → 触达 → 待处理</div>
              </div>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'conversion-import') {
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">团购转化 / 订单导入与列表</h2>
        <div className="grid grid-cols-[1.2fr_1fr] gap-4">
          <article className="rounded-2xl border border-[#f0ebe3] p-4">
            <div className="mb-2 text-sm font-semibold">导入区</div>
            <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#7d7267]">
              字段映射预览：订单编号→orderNo，联系方式→phone，购买课程→course，购买时间→purchasedAt，校区→campus
            </div>
            <div className="mt-3 flex gap-2">
              <button onClick={runImport} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">模拟导入订单</button>
              <button onClick={() => batchMark('标记无效')} className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">批量标记无效</button>
              <button onClick={() => batchMark('重复订单号')} className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">批量标记重复</button>
            </div>
            {flowTip && <div className="mt-3 rounded-xl bg-[#fff5eb] p-2 text-xs text-[#8b5d34]">{flowTip}</div>}
          </article>
          <article className="rounded-2xl border border-[#f0ebe3] p-4">
            <div className="mb-2 text-sm font-semibold">导入校验结果</div>
            <div className="space-y-2 text-sm">
              <div className="rounded-xl bg-[#faf8f4] p-3">导入总条数：{importSummary.total}</div>
              <div className="rounded-xl bg-[#faf8f4] p-3">手机号缺失：{importSummary.missingPhone}</div>
              <div className="rounded-xl bg-[#faf8f4] p-3">重复订单号：{importSummary.duplicateOrder}</div>
              <div className="rounded-xl bg-[#faf8f4] p-3">校区缺失：{importSummary.missingCampus}</div>
            </div>
          </article>
        </div>
        <PaginatedTable
          columns={[
            { key: 'check', title: '选择', render: (row) => <input type="checkbox" checked={selectedOrderIds.includes(row.id)} onChange={() => toggleOrderSelection(row.id)} /> },
            { key: 'orderNo', title: '订单编号' },
            { key: 'platform', title: '平台' },
            { key: 'studentName', title: '学员姓名' },
            { key: 'phone', title: '联系方式', render: (row) => row.phone || '—' },
            { key: 'campus', title: '校区', render: (row) => row.campus || '—' },
            { key: 'course', title: '购买课程' },
            { key: 'purchasedAt', title: '购买时间' },
            { key: 'status', title: '核销状态' },
            { key: 'validation', title: '校验结果' }
          ]}
          rows={orderRows}
          pageSize={6}
        />
      </section>
    )
  }

  if (page === 'conversion-clue') {
    return (
      <section className="space-y-3">
        <article className="space-y-3">
          <h2 className="text-sm font-semibold">团购转化 / 核销登记与线索详情</h2>
          <div className="flex items-center gap-2 rounded-xl border border-[#ece7df] bg-[#fffaf2] px-3 py-2 text-sm">
            <Search size={14} />
            <input
              value={searchLeadKeyword}
              onChange={(event) => setSearchLeadKeyword(event.target.value)}
              className="w-full bg-transparent text-sm outline-none"
              placeholder="券码/手机号/姓名"
            />
          </div>
          <PaginatedTable
            columns={[
              { key: 'name', title: '姓名' },
              { key: 'phone', title: '手机号' },
              { key: 'coupon', title: '券码' },
              { key: 'status', title: '状态' },
              {
                key: 'action',
                title: '操作',
                render: (row) => (
                  <button
                    className="text-[#bc7844]"
                    onClick={() => {
                      setActiveLeadId(row.id)
                      setShowBookingQr(false)
                      setLeadDrawerOpen(true)
                    }}
                  >
                    查看线索
                  </button>
                )
              }
            ]}
            rows={leadRowsFiltered}
            pageSize={5}
          />
        </article>
        {leadDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setLeadDrawerOpen(false)} />
            <div className="h-full w-[460px] bg-white p-5 shadow-2xl overflow-auto">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">线索详情与动作</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setLeadDrawerOpen(false)}>关闭</button>
              </div>
              <div className="space-y-2 text-sm">
                <div>姓名：{activeLead?.name}</div>
                <div>手机号：{activeLead?.phone}</div>
                <div>平台/券码：{activeLead?.platform} / {activeLead?.coupon}</div>
                <div>状态：{activeLead?.status}</div>
                <div>转化节点：{activeLead?.timeline.join(' → ')}</div>
                <div>最近发送预约链接：{activeLead?.linkSentAt || '未发送'}</div>
              </div>
              <div className="mt-4 grid grid-cols-1 gap-2">
                <button onClick={writeOffLead} className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">核销登记</button>
                <button onClick={openArchiveForm} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">一键建档</button>
                <button onClick={sendBookingLink} className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">发送预约链接</button>
                <button onClick={() => onNavigate('/admin/conversion-qr')} className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">打开二维码静态页</button>
              </div>
              {showArchiveForm && (
                <div className="mt-4 rounded-2xl border border-[#f0ebe3] p-3">
                  <div className="mb-2 text-sm font-semibold">建档信息录入</div>
                  <div className="grid grid-cols-2 gap-2">
                    <input value={archiveForm.name} onChange={(e) => setArchiveForm((p) => ({ ...p, name: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-1.5 text-xs outline-none" placeholder="姓名" />
                    <input value={archiveForm.age} onChange={(e) => setArchiveForm((p) => ({ ...p, age: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-1.5 text-xs outline-none" placeholder="年龄" />
                    <select value={archiveForm.gender} onChange={(e) => setArchiveForm((p) => ({ ...p, gender: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-1.5 text-xs outline-none"><option>男</option><option>女</option></select>
                    <input value={archiveForm.phone} onChange={(e) => setArchiveForm((p) => ({ ...p, phone: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-1.5 text-xs outline-none" placeholder="手机号" />
                    <input value={archiveForm.parentName} onChange={(e) => setArchiveForm((p) => ({ ...p, parentName: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-1.5 text-xs outline-none" placeholder="家长姓名" />
                    <select value={archiveForm.hasBasic} onChange={(e) => setArchiveForm((p) => ({ ...p, hasBasic: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-1.5 text-xs outline-none"><option>有</option><option>无</option></select>
                    <input value={archiveForm.basicDescription} onChange={(e) => setArchiveForm((p) => ({ ...p, basicDescription: e.target.value }))} className="col-span-2 rounded-lg border border-[#e9e2d8] px-2 py-1.5 text-xs outline-none" placeholder="有基础情况说明" />
                    <select value={archiveForm.intendedSubject} onChange={(e) => setArchiveForm((p) => ({ ...p, intendedSubject: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-1.5 text-xs outline-none"><option>木吉他</option><option>电吉他</option><option>贝斯</option></select>
                    <input value={archiveForm.learningPurpose} onChange={(e) => setArchiveForm((p) => ({ ...p, learningPurpose: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-1.5 text-xs outline-none" placeholder="学习目的" />
                    <input value={archiveForm.expectedClassTime} onChange={(e) => setArchiveForm((p) => ({ ...p, expectedClassTime: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-1.5 text-xs outline-none" placeholder="预期上课时间" />
                    <input value={archiveForm.expectedClassLocation} onChange={(e) => setArchiveForm((p) => ({ ...p, expectedClassLocation: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-1.5 text-xs outline-none" placeholder="预期上课地点" />
                    <input value={archiveForm.groupCoursePackage} onChange={(e) => setArchiveForm((p) => ({ ...p, groupCoursePackage: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-1.5 text-xs outline-none" placeholder="团购课时包" />
                    <input value={archiveForm.channel} onChange={(e) => setArchiveForm((p) => ({ ...p, channel: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-1.5 text-xs outline-none" placeholder="了解渠道" />
                    <select value={archiveForm.hasInstrument} onChange={(e) => setArchiveForm((p) => ({ ...p, hasInstrument: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-1.5 text-xs outline-none"><option>有</option><option>无</option></select>
                    <input value={archiveForm.signupHours} onChange={(e) => setArchiveForm((p) => ({ ...p, signupHours: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-1.5 text-xs outline-none" placeholder="报名课时" />
                    <input value={archiveForm.formalTeacher} onChange={(e) => setArchiveForm((p) => ({ ...p, formalTeacher: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-1.5 text-xs outline-none" placeholder="正式课负责老师" />
                    <select value={archiveForm.isReferral} onChange={(e) => setArchiveForm((p) => ({ ...p, isReferral: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-1.5 text-xs outline-none"><option>是</option><option>否</option></select>
                  </div>
                  <button onClick={archiveLead} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存建档信息</button>
                </div>
              )}
              {flowTip && <div className="mt-3 rounded-xl bg-[#fff5eb] p-2 text-xs text-[#8b5d34]">{flowTip}</div>}
              {showBookingQr && (
                <div className="mt-4 rounded-2xl border border-[#f0ebe3] p-3">
                  <div className="mb-2 text-sm font-semibold">预约链接二维码</div>
                  <div className="flex items-center gap-3">
                    <QrPreview value={`${activeLead?.name}-${activeLead?.phone}-${activeLead?.coupon}`} />
                    <div className="text-xs text-[#8f8376]">
                      学员可直接扫码预约体验课，教务也可截图后通过微信发送。
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'conversion-qr') {
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">团购转化 / 预约二维码静态页</h2>
        <div className="rounded-2xl border border-[#f0ebe3] p-6">
          <div className="mb-4 text-sm text-[#8f8376]">用于教务现场展示：可扫码预约，也可直接截图发送给学员。</div>
          <div className="flex items-center gap-6">
            <QrPreview value="chordsked-booking-static-page-001" />
            <div className="space-y-2 text-sm text-[#6f665d]">
              <div>预约入口：ChordSked 学员端体验课预约</div>
              <div>有效期：24小时内可使用</div>
              <div>建议：教务点击“发送预约链接”后同步截图发送家长微信</div>
            </div>
          </div>
        </div>
      </section>
    )
  }

  if (page === 'schedule-calendar') {
    const scheduleRowsOfDate = schedulePlanRows.filter((item) => item.date === scheduleSelectedDate)
    return (
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold">排课与课表 / 排课日历</h2>
          <div className="flex gap-2 text-xs">
            <button onClick={() => setScheduleCalendarView('week')} className={`rounded-lg px-3 py-1.5 ${scheduleCalendarView === 'week' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>周视图</button>
            <button onClick={() => setScheduleCalendarView('month')} className={`rounded-lg px-3 py-1.5 ${scheduleCalendarView === 'month' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>月视图</button>
          </div>
        </div>
        <div className={`grid gap-3 ${scheduleCalendarView === 'week' ? 'grid-cols-7' : 'grid-cols-7'}`}>
          {scheduleCalendarDays.map((date) => {
            const dayRows = schedulePlanRows.filter((item) => item.date === date)
            return (
              <button key={date} onClick={() => openScheduleArrangeModal(date)} className={`rounded-2xl border p-3 text-left ${scheduleSelectedDate === date ? 'border-[#ff9b54] bg-[#fff8f1]' : 'border-[#f0ebe3] hover:bg-[#faf8f4]'}`}>
                <div className="text-xs text-[#8f8376]">{date}</div>
                <div className="mt-1 text-xs text-[#6f665d]">已排 {dayRows.length} 节</div>
                <div className="mt-2 space-y-1">
                  {dayRows.slice(0, 2).map((row) => (
                    <div key={row.id} className="rounded bg-white px-2 py-1 text-[11px] text-[#6a6259]">
                      {row.timeSlot} {row.templateType}
                    </div>
                  ))}
                  {dayRows.length > 2 && <div className="text-[11px] text-[#8f8376]">+{dayRows.length - 2} 节</div>}
                </div>
              </button>
            )
          })}
        </div>
        <article className="rounded-2xl border border-[#f0ebe3] p-4">
          <div className="mb-2 text-sm font-semibold">当日排课明细（{scheduleSelectedDate}）</div>
          <PaginatedTable
            columns={[
              { key: 'timeSlot', title: '时间段' },
              { key: 'templateType', title: '课程模板' },
              { key: 'teacher', title: '教师' },
              { key: 'room', title: '教室' },
              { key: 'ageGroup', title: '人群' },
              { key: 'capacity', title: '承载量', render: (row) => `${row.reservedCount}/${row.capacityMax}` }
            ]}
            rows={scheduleRowsOfDate}
            pageSize={6}
          />
        </article>
        {scheduleArrangeModalOpen && (
          <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setScheduleArrangeModalOpen(false)} />
            <div className="relative w-full max-w-[1080px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">排课操作（{scheduleSelectedDate}）</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setScheduleArrangeModalOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-4 gap-3">
                <div className="space-y-1">
                  <div className="text-xs text-[#7b7064]">校区</div>
                  <select value={schedulePlanForm.campus} onChange={(e) => setSchedulePlanForm((prev) => ({ ...prev, campus: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none">
                    {scheduleCampusOptions.map((item) => <option key={item}>{item}</option>)}
                  </select>
                </div>
                <div className="space-y-1">
                  <div className="text-xs text-[#7b7064]">人群</div>
                  <select value={schedulePlanForm.ageGroup} onChange={(e) => setSchedulePlanForm((prev) => ({ ...prev, ageGroup: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none">
                    <option>成人</option>
                    <option>未成年</option>
                    <option>儿童</option>
                  </select>
                </div>
                <div className="space-y-1">
                  <div className="text-xs text-[#7b7064]">课程模板</div>
                  <select value={schedulePlanForm.templateId} onChange={(e) => setSchedulePlanForm((prev) => ({ ...prev, templateId: e.target.value, studentName: '' }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none">
                    {scheduleTemplateRows.filter((item) => item.status === '启用').map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
                  </select>
                </div>
                <div className="flex items-end">
                  <button onClick={searchScheduleCandidates} className="w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">搜索可排课时间段</button>
                </div>
              </div>
              <div className="mt-3 rounded-xl bg-[#faf8f4] p-3 text-xs text-[#6f665d]">
                系统按教师可授课时间/承载量/擅长科目/授课类型、校区教室空置、人群匹配综合搜索候选时间段。
              </div>
              <div className="mt-3">
                <PaginatedTable
                  columns={[
                    { key: 'timeSlot', title: '可排课时间段' },
                    { key: 'teacherName', title: '可绑定教师' },
                    { key: 'roomName', title: '可绑定教室' },
                    { key: 'matchDesc', title: '匹配说明' },
                    { key: 'action', title: '操作', render: (row) => <button onClick={() => applyScheduleCandidate(row)} className="text-[#bc7844]">选择</button> }
                  ]}
                  rows={scheduleCandidateRows}
                  pageSize={5}
                  emptyText="请先点击“搜索可排课时间段”"
                />
              </div>
              <div className="mt-3 grid grid-cols-4 gap-3">
                <div className="rounded-xl bg-[#faf8f4] p-3 text-xs">时间段：{schedulePlanForm.timeSlot || '未选择'}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 text-xs">教师：{teacherRows.find((item) => item.id === schedulePlanForm.teacherId)?.name || '未选择'}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 text-xs">教室：{scheduleRoomRows.find((item) => item.id === schedulePlanForm.roomId)?.name || '未选择'}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 text-xs">模板：{scheduleTemplateRows.find((item) => item.id === schedulePlanForm.templateId)?.name || '未选择'}</div>
              </div>
              {scheduleTemplateRows.find((item) => item.id === schedulePlanForm.templateId)?.needStudentBinding && (
                <div className="mt-3 space-y-1">
                  <div className="text-xs text-[#7b7064]">一对一绑定学员</div>
                  <input value={schedulePlanForm.studentName} onChange={(e) => setSchedulePlanForm((prev) => ({ ...prev, studentName: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" placeholder="请输入学员姓名" />
                </div>
              )}
              <div className="mt-4 flex justify-end gap-2">
                <button onClick={() => setScheduleArrangeModalOpen(false)} className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">取消</button>
                <button onClick={submitSchedulePlan} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">确认排课</button>
              </div>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'schedule-template') {
    return (
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold">排课与课表 / 课程模板管理</h2>
          <button onClick={() => openScheduleTemplateModal()} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">创建模板</button>
        </div>
        <PaginatedTable
          columns={[
            { key: 'name', title: '模板名称' },
            { key: 'courseType', title: '课程类型' },
            { key: 'subject', title: '科目' },
            { key: 'consumeHours', title: '消耗课时' },
            { key: 'capacity', title: '承载量', render: (row) => `${row.capacityMin}-${row.capacityMax}人` },
            { key: 'teachingType', title: '授课类型' },
            { key: 'ageGroup', title: '人群' },
            { key: 'binding', title: '一对一规则', render: (row) => (row.needStudentBinding ? '需绑定学员+教师' : '仅绑定教师') },
            { key: 'action', title: '操作', render: (row) => <button onClick={() => openScheduleTemplateModal(row)} className="text-[#bc7844]">修改</button> }
          ]}
          rows={scheduleTemplateRows}
          pageSize={5}
        />
        {scheduleTemplateModalOpen && (
          <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setScheduleTemplateModalOpen(false)} />
            <div className="relative w-full max-w-[760px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">{scheduleTemplateEditingId ? '修改课程模板' : '创建课程模板'}</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setScheduleTemplateModalOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">模板名称</div><input value={scheduleTemplateForm.name} onChange={(e) => setScheduleTemplateForm((prev) => ({ ...prev, name: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">课程类型</div><select value={scheduleTemplateForm.courseType} onChange={(e) => setScheduleTemplateForm((prev) => ({ ...prev, courseType: e.target.value, teachingType: e.target.value === '一对一' ? '一对一' : '团课', capacityMin: e.target.value === '一对一' ? '1' : prev.capacityMin, capacityMax: e.target.value === '一对一' ? '1' : prev.capacityMax, needStudentBinding: e.target.value === '一对一' }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>团购体验课</option><option>标准课</option><option>一对一</option></select></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">科目</div><select value={scheduleTemplateForm.subject} onChange={(e) => setScheduleTemplateForm((prev) => ({ ...prev, subject: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>木吉他</option><option>电吉他</option><option>贝斯</option></select></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">消耗课时</div><input value={scheduleTemplateForm.consumeHours} onChange={(e) => setScheduleTemplateForm((prev) => ({ ...prev, consumeHours: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">最小承载量</div><input value={scheduleTemplateForm.capacityMin} onChange={(e) => setScheduleTemplateForm((prev) => ({ ...prev, capacityMin: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">最大承载量</div><input value={scheduleTemplateForm.capacityMax} onChange={(e) => setScheduleTemplateForm((prev) => ({ ...prev, capacityMax: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">授课类型</div><select value={scheduleTemplateForm.teachingType} onChange={(e) => setScheduleTemplateForm((prev) => ({ ...prev, teachingType: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>团课</option><option>一对一</option></select></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">适用人群</div><select value={scheduleTemplateForm.ageGroup} onChange={(e) => setScheduleTemplateForm((prev) => ({ ...prev, ageGroup: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>成人</option><option>未成年</option><option>儿童</option><option>不限</option></select></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">状态</div><select value={scheduleTemplateForm.status} onChange={(e) => setScheduleTemplateForm((prev) => ({ ...prev, status: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>启用</option><option>停用</option></select></div>
                <label className="mt-6 flex items-center gap-2 text-xs text-[#6f665d]"><input type="checkbox" checked={scheduleTemplateForm.needStudentBinding} onChange={(e) => setScheduleTemplateForm((prev) => ({ ...prev, needStudentBinding: e.target.checked }))} />一对一排课需绑定学员和教师</label>
              </div>
              <button onClick={saveScheduleTemplate} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存模板</button>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'schedule-room') {
    const filteredRoomRows = scheduleRoomCampus === '全部校区'
      ? scheduleRoomRows
      : scheduleRoomRows.filter((item) => item.campus === scheduleRoomCampus)
    return (
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold">排课与课表 / 教室资源管理</h2>
          <div className="flex items-center gap-2">
            <select value={scheduleRoomCampus} onChange={(e) => setScheduleRoomCampus(e.target.value)} className="rounded-lg border border-[#e8dfd3] px-2 py-2 text-xs outline-none">
              <option>全部校区</option>
              {scheduleCampusOptions.map((item) => <option key={item}>{item}</option>)}
            </select>
            <button onClick={() => openScheduleRoomModal()} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">创建教室</button>
          </div>
        </div>
        <PaginatedTable
          columns={[
            { key: 'campus', title: '所属校区' },
            { key: 'name', title: '教室名称' },
            { key: 'type', title: '教室类型' },
            { key: 'capacity', title: '容纳人数' },
            { key: 'status', title: '状态' },
            { key: 'action', title: '操作', render: (row) => <button onClick={() => openScheduleRoomModal(row)} className="text-[#bc7844]">修改信息</button> }
          ]}
          rows={filteredRoomRows}
          pageSize={6}
        />
        {scheduleRoomModalOpen && (
          <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setScheduleRoomModalOpen(false)} />
            <div className="relative w-full max-w-[680px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">{scheduleRoomEditingId ? '修改教室信息' : '创建教室'}</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setScheduleRoomModalOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">所属校区</div><select value={scheduleRoomForm.campus} onChange={(e) => setScheduleRoomForm((prev) => ({ ...prev, campus: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none">{scheduleCampusOptions.map((item) => <option key={item}>{item}</option>)}</select></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">教室名称</div><input value={scheduleRoomForm.name} onChange={(e) => setScheduleRoomForm((prev) => ({ ...prev, name: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">教室类型</div><select value={scheduleRoomForm.type} onChange={(e) => setScheduleRoomForm((prev) => ({ ...prev, type: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>标准教室</option><option>一对一教室</option><option>合奏教室</option></select></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">容纳人数</div><input value={scheduleRoomForm.capacity} onChange={(e) => setScheduleRoomForm((prev) => ({ ...prev, capacity: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">状态</div><select value={scheduleRoomForm.status} onChange={(e) => setScheduleRoomForm((prev) => ({ ...prev, status: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>启用</option><option>维修中</option><option>停用</option></select></div>
              </div>
              <button onClick={saveScheduleRoom} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存教室</button>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'adjustments-list') {
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">调课/取消/补课 / 申请列表</h2>
        <PaginatedTable
          columns={[
            { key: 'student', title: '学员' },
            { key: 'type', title: '申请类型' },
            { key: 'originTime', title: '原课程时间' },
            { key: 'expectTime', title: '期望时间', render: (row) => row.expectTime || '—' },
            { key: 'impact', title: '课时影响' },
            { key: 'status', title: '状态', render: (row) => row.status || '待审核' },
            {
              key: 'action',
              title: '操作',
              render: (row) => (
                <div className="flex gap-3">
                  <button onClick={() => openAdjustmentDrawer(row)} className="text-[#bc7844]">申请详情</button>
                  {row.type === '调课' && <button onClick={() => openAdjustmentRebind(row)} className="text-[#bc7844]">确认重绑</button>}
                </div>
              )
            }
          ]}
          rows={adjustmentRows}
        />
        {adjustmentDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setAdjustmentDrawerOpen(false)} />
            <div className="h-full w-[500px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">申请详情</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setAdjustmentDrawerOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div className="rounded-xl bg-[#faf8f4] p-3">学员：{activeRequest.student}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">申请类型：{activeRequest.type}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">原课程：{activeRequest.originTime}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">期望时间：{activeRequest.expectTime || '无'}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">课时影响：{activeRequest.impact}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">当前状态：{activeRequest.status || '待审核'}</div>
              </div>
              {activeRequest.type === '调课' && (
                <div className="mt-3 rounded-xl border border-[#f0ebe3] bg-[#fffcf8] p-3 text-xs text-[#7b7064]">
                  调课申请需先为学员重新绑定可预约课程，再完成审批确认。
                </div>
              )}
              <div className="mt-4 grid grid-cols-3 gap-2">
                {activeRequest.type === '调课' ? (
                  <button onClick={() => openAdjustmentRebind(activeRequest)} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">确认重绑</button>
                ) : (
                  <button onClick={() => saveAdjustmentDecision('approve')} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">同意申请</button>
                )}
                <button onClick={() => saveAdjustmentDecision('reject')} className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">拒绝申请</button>
                <button onClick={() => setAdjustmentDrawerOpen(false)} className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">稍后处理</button>
              </div>
            </div>
          </div>
        )}
        {adjustmentRebindModalOpen && (
          <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setAdjustmentRebindModalOpen(false)} />
            <div className="relative w-full max-w-[980px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">可预约课程池</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setAdjustmentRebindModalOpen(false)}>关闭</button>
              </div>
              <div className="mb-3 grid grid-cols-2 gap-2 text-sm">
                <div className="rounded-xl bg-[#faf8f4] p-3">当前学员：{activeRequest.student}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">原课程：{activeRequest.originTime}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">期望时间：{activeRequest.expectTime || '无'}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">已选课程：{selectedRebindCourse ? `${selectedRebindCourse.title} / ${selectedRebindCourse.time}` : '未选择'}</div>
              </div>
              <PaginatedTable
                columns={[
                  { key: 'title', title: '课程' },
                  { key: 'time', title: '可预约时间' },
                  { key: 'teacher', title: '授课老师' },
                  { key: 'room', title: '教室' },
                  {
                    key: 'action',
                    title: '操作',
                    render: (row) => (
                      <button onClick={() => setSelectedRebindCourseId(row.id)} className={`rounded px-2 py-1 text-xs ${selectedRebindCourseId === row.id ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>
                        {selectedRebindCourseId === row.id ? '已选择' : '选择课程'}
                      </button>
                    )
                  }
                ]}
                rows={adjustmentCourseCandidates}
                pageSize={4}
              />
              <div className="mt-3 flex justify-end gap-2">
                <button onClick={() => setAdjustmentRebindModalOpen(false)} className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">取消</button>
                <button onClick={() => saveAdjustmentDecision('rebind')} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">确认重绑</button>
              </div>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'adjustments-log') {
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">调课/取消/补课 / 审核日志</h2>
        <PaginatedTable
          columns={[
            { key: 'time', title: '审核时间' },
            { key: 'student', title: '学员' },
            { key: 'type', title: '申请类型' },
            { key: 'action', title: '审核动作' },
            {
              key: 'result',
              title: '结果',
              render: (row) => (
                <span className={`rounded-full px-2 py-0.5 text-xs ${row.result === '通过' ? 'bg-[#effaf1] text-[#2f8a47]' : 'bg-[#fff1ef] text-[#c9413a]'}`}>
                  {row.result}
                </span>
              )
            },
            { key: 'detail', title: '审核说明' }
          ]}
          rows={adjustmentAuditLogRows}
          pageSize={6}
        />
      </section>
    )
  }

  if (page === 'students-list') {
    return (
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold">学员管理 / 学员列表</h2>
          <button onClick={() => setNewStudentDrawerOpen(true)} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">新建学生</button>
        </div>
        <PaginatedTable
          columns={[
            { key: 'name', title: '学员姓名' },
            { key: 'guitarCategory', title: '吉他门类', render: (row) => row.guitarCategory || row.intendedSubject || '木吉他' },
            { key: 'phone', title: '手机号' },
            { key: 'progressTags', title: '进度标签', render: (row) => (row.progressTags || row.tags || []).join(' / ') },
            { key: 'remaining', title: '剩余课时' },
            { key: 'signupTime', title: '报名时间', render: (row) => row.signupTime || '—' },
            { key: 'paymentAmount', title: '缴费金额', render: (row) => `¥${row.paymentAmount ?? 0}` },
            {
              key: 'action',
              title: '操作',
              render: (row) => (
                <button
                  onClick={() => {
                    setActiveStudentId(row.id)
                    setStudentDrawerOpen(true)
                  }}
                  className="text-[#bc7844]"
                >
                  查看详情
                </button>
              )
            }
          ]}
          rows={studentRows}
        />
        {studentDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setStudentDrawerOpen(false)} />
            <div className="h-full w-[520px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">学员详情</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setStudentDrawerOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div className="rounded-xl bg-[#faf8f4] p-3">姓名：{activeStudent?.name}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">手机号：{activeStudent?.phone}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">吉他门类：{activeStudent?.guitarCategory || activeStudent?.intendedSubject || '木吉他'}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">课包/课时余额：{activeStudent?.packageName || '待分配'} / {activeStudent?.remaining ?? 0}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">团购/体验课程预约登记表：{activeStudent?.registrationFormSummary || '待补充登记表信息'}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">学习目标/进度标签：{activeStudent?.goal || '待补充'} / {(activeStudent?.progressTags || activeStudent?.tags || []).join('、')}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">报名时间：{activeStudent?.signupTime || '—'}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">缴费金额：¥{activeStudent?.paymentAmount ?? 0}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">报名权益：{activeStudent?.benefits || '待补充'}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">上课记录归档：{(activeStudent?.classRecords || ['暂无']).join('；')}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">作业记录归档：{(activeStudent?.homeworkRecords || ['暂无']).join('；')}</div>
              </div>
            </div>
          </div>
        )}
        {newStudentDrawerOpen && (
          <div className="fixed inset-0 z-30 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setNewStudentDrawerOpen(false)} />
            <div className="relative w-full max-w-[640px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">新建学生</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setNewStudentDrawerOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <div className="text-xs text-[#7b7064]">姓名</div>
                  <input value={newStudentForm.name} onChange={(e) => setNewStudentForm((p) => ({ ...p, name: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" />
                </div>
                <div className="space-y-1">
                  <div className="text-xs text-[#7b7064]">性别</div>
                  <select value={newStudentForm.gender} onChange={(e) => setNewStudentForm((p) => ({ ...p, gender: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>男</option><option>女</option></select>
                </div>
                <div className="space-y-1">
                  <div className="text-xs text-[#7b7064]">手机号</div>
                  <input value={newStudentForm.phone} onChange={(e) => setNewStudentForm((p) => ({ ...p, phone: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" />
                </div>
                <div className="space-y-1">
                  <div className="text-xs text-[#7b7064]">吉他门类</div>
                  <select value={newStudentForm.guitarCategory} onChange={(e) => setNewStudentForm((p) => ({ ...p, guitarCategory: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>木吉他</option><option>电吉他</option><option>贝斯</option></select>
                </div>
                <div className="space-y-1">
                  <div className="text-xs text-[#7b7064]">报名时间</div>
                  <input value={newStudentForm.signupTime} onChange={(e) => setNewStudentForm((p) => ({ ...p, signupTime: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" />
                </div>
                <div className="space-y-1">
                  <div className="text-xs text-[#7b7064]">缴费金额</div>
                  <input value={newStudentForm.paymentAmount} onChange={(e) => setNewStudentForm((p) => ({ ...p, paymentAmount: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" />
                </div>
                <div className="col-span-2 space-y-1">
                  <div className="text-xs text-[#7b7064]">报名权益</div>
                  <input value={newStudentForm.benefits} onChange={(e) => setNewStudentForm((p) => ({ ...p, benefits: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" />
                </div>
              </div>
              <button onClick={createStudent} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存学生</button>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teachers-list') {
    return (
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold">教师管理 / 教师列表</h2>
          <button onClick={() => setNewTeacherDrawerOpen(true)} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">新建教师</button>
        </div>
        <PaginatedTable
          columns={[
            { key: 'name', title: '教师姓名' },
            { key: 'employeeNo', title: '工号' },
            { key: 'phone', title: '联系方式' },
            { key: 'teachingType', title: '授课类型' },
            { key: 'level', title: '教师等级' },
            { key: 'campus', title: '所属校区' },
            {
              key: 'status',
              title: '状态',
              render: (row) => (
                <span className={`rounded-full px-2 py-0.5 text-xs ${row.status === '冻结' ? 'bg-[#ffe8e8] text-[#a64545]' : 'bg-[#fff4ea] text-[#b55e22]'}`}>
                  {row.status}
                </span>
              )
            },
            {
              key: 'action',
              title: '操作',
              render: (row) => (
                <div className="flex gap-2">
                  <button onClick={() => { setActiveTeacherId(row.id); setTeacherDrawerOpen(true) }} className="text-[#bc7844]">详情</button>
                  <button onClick={() => toggleFreezeTeacher(row.id)} className="text-[#8f8376]">{row.status === '冻结' ? '解冻' : '冻结'}</button>
                  <button onClick={() => deleteTeacher(row.id)} className="text-[#a64545]">删除</button>
                </div>
              )
            }
          ]}
          rows={teacherRows}
        />
        {teacherDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setTeacherDrawerOpen(false)} />
            <div className="h-full w-[520px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">教师详情</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setTeacherDrawerOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div className="rounded-xl bg-[#faf8f4] p-3">姓名：{activeTeacher?.name}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">工号：{activeTeacher?.employeeNo}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">联系方式：{activeTeacher?.phone}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">入职时间：{activeTeacher?.entryDate}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">授课类型：{activeTeacher?.teachingType}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">教师等级：{activeTeacher?.level}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">教学资历：{activeTeacher?.teachingQualification}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">擅长曲目：{(activeTeacher?.expertiseTracks || []).join(' / ')}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">所属校区：{activeTeacher?.campus}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">状态：{activeTeacher?.status}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">已绑定学员：{(activeTeacher?.boundStudents || []).length ? activeTeacher.boundStudents.join('、') : '暂无绑定学员'}</div>
              </div>
              <div className="mt-3 grid grid-cols-3 gap-2">
                <button onClick={() => toggleFreezeTeacher(activeTeacher?.id)} className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">{activeTeacher?.status === '冻结' ? '解冻教师' : '冻结教师'}</button>
                <button onClick={() => deleteTeacher(activeTeacher?.id)} className="rounded-lg border border-[#f2c5c5] px-3 py-2 text-sm text-[#a64545]">删除教师</button>
                <button onClick={() => setTeacherDrawerOpen(false)} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">完成</button>
              </div>
            </div>
          </div>
        )}
        {newTeacherDrawerOpen && (
          <div className="fixed inset-0 z-30 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setNewTeacherDrawerOpen(false)} />
            <div className="relative w-full max-w-[680px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">新建教师</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setNewTeacherDrawerOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">姓名</div><input value={newTeacherForm.name} onChange={(e) => setNewTeacherForm((p) => ({ ...p, name: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">工号</div><input value={newTeacherForm.employeeNo} onChange={(e) => setNewTeacherForm((p) => ({ ...p, employeeNo: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">联系方式</div><input value={newTeacherForm.phone} onChange={(e) => setNewTeacherForm((p) => ({ ...p, phone: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">入职时间</div><input value={newTeacherForm.entryDate} onChange={(e) => setNewTeacherForm((p) => ({ ...p, entryDate: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">授课类型</div><select value={newTeacherForm.teachingType} onChange={(e) => setNewTeacherForm((p) => ({ ...p, teachingType: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>团课</option><option>一对一</option><option>团课+一对一</option></select></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">教师等级</div><select value={newTeacherForm.level} onChange={(e) => setNewTeacherForm((p) => ({ ...p, level: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>S</option><option>A</option><option>B</option><option>C</option></select></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">擅长曲目</div><select value={newTeacherForm.expertiseTrack} onChange={(e) => setNewTeacherForm((p) => ({ ...p, expertiseTrack: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>木吉他</option><option>电吉他</option><option>贝斯</option></select></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">所属校区</div><select value={newTeacherForm.campus} onChange={(e) => setNewTeacherForm((p) => ({ ...p, campus: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>北环国基路校区</option><option>西大剧院校区</option></select></div>
                <div className="col-span-2 space-y-1"><div className="text-xs text-[#7b7064]">教学资历</div><input value={newTeacherForm.teachingQualification} onChange={(e) => setNewTeacherForm((p) => ({ ...p, teachingQualification: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
              </div>
              <button onClick={createTeacher} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存教师</button>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'packages-list') {
    return (
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold">课包管理 / 课包列表</h2>
          <button onClick={() => setNewPackageDrawerOpen(true)} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">新建课包</button>
        </div>
        <PaginatedTable
          columns={[
            { key: 'type', title: '类型' },
            { key: 'name', title: '课包名称' },
            { key: 'displayPrice', title: '展示价', render: (row) => `¥${row.displayPrice}` },
            { key: 'salePrice', title: '售卖价', render: (row) => `¥${row.salePrice}` },
            { key: 'stock', title: '库存' },
            { key: 'validDays', title: '有效期(天)' },
            { key: 'validUntil', title: '有效截止' },
            {
              key: 'status',
              title: '状态',
              render: (row) => (
                <span className={`rounded-full px-2 py-0.5 text-xs ${row.status === '上架' ? 'bg-[#fff4ea] text-[#b55e22]' : 'bg-[#faf8f4] text-[#8f8376]'}`}>
                  {row.status}
                </span>
              )
            },
            {
              key: 'action',
              title: '操作',
              render: (row) => (
                <div className="flex gap-2">
                  <button onClick={() => { setActivePackageId(row.id); setPackageDrawerOpen(true) }} className="text-[#bc7844]">详情</button>
                  <button onClick={() => togglePackageSale(row.id)} className="text-[#8f8376]">{row.status === '上架' ? '停售' : '售卖'}</button>
                  <button onClick={() => cancelPackage(row.id)} className="text-[#a64545]">取消课包</button>
                </div>
              )
            }
          ]}
          rows={packageRows}
        />
        {packageDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setPackageDrawerOpen(false)} />
            <div className="h-full w-[480px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">课包详情</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setPackageDrawerOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div className="rounded-xl bg-[#faf8f4] p-3">课包名称：{activePackage?.name}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">课包类型：{activePackage?.type}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">展示价：¥{activePackage?.displayPrice}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">售卖价：¥{activePackage?.salePrice}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">库存：{activePackage?.stock}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">已售：{activePackage?.soldCount}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">有效期：{activePackage?.validDays} 天</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">有效截止：{activePackage?.validUntil}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">状态：{activePackage?.status}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">可取消：{activePackage?.canCancel ? '是' : '否'}</div>
              </div>
              <div className="mt-3 grid grid-cols-3 gap-2">
                <button onClick={() => togglePackageSale(activePackage?.id)} className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">{activePackage?.status === '上架' ? '停售' : '恢复售卖'}</button>
                <button onClick={() => cancelPackage(activePackage?.id)} className="rounded-lg border border-[#f2c5c5] px-3 py-2 text-sm text-[#a64545]">取消课包</button>
                <button onClick={() => setPackageDrawerOpen(false)} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">完成</button>
              </div>
            </div>
          </div>
        )}
        {newPackageDrawerOpen && (
          <div className="fixed inset-0 z-30 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setNewPackageDrawerOpen(false)} />
            <div className="relative w-full max-w-[640px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">新建课包</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setNewPackageDrawerOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">课包类型</div><select value={newPackageForm.type} onChange={(e) => setNewPackageForm((p) => ({ ...p, type: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>团购体验</option><option>正式课程</option><option>一对一</option></select></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">课包名称</div><input value={newPackageForm.name} onChange={(e) => setNewPackageForm((p) => ({ ...p, name: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">展示价</div><input value={newPackageForm.displayPrice} onChange={(e) => setNewPackageForm((p) => ({ ...p, displayPrice: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">售卖价</div><input value={newPackageForm.salePrice} onChange={(e) => setNewPackageForm((p) => ({ ...p, salePrice: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">库存</div><input value={newPackageForm.stock} onChange={(e) => setNewPackageForm((p) => ({ ...p, stock: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">有效期(天)</div><input value={newPackageForm.validDays} onChange={(e) => setNewPackageForm((p) => ({ ...p, validDays: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="col-span-2 space-y-1"><div className="text-xs text-[#7b7064]">有效截止日期</div><input value={newPackageForm.validUntil} onChange={(e) => setNewPackageForm((p) => ({ ...p, validUntil: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
              </div>
              <button onClick={createPackage} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存课包</button>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'notice-rules' || page === 'notice-templates' || page === 'notice-touch') {
    const firstTabs = [
      { key: 'message', label: '消息提醒', pages: ['notice-rules', 'notice-touch'] },
      { key: 'template', label: '公告模板', pages: ['notice-templates'] }
    ]
    const secondTabs = page === 'notice-templates'
      ? [{ key: 'notice-templates', label: '模板管理' }]
      : [{ key: 'notice-rules', label: '提醒规则' }, { key: 'notice-touch', label: '触达记录' }]

    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">公告/通知</h2>
        <div className="rounded-2xl border border-[#f0ebe3] bg-[#fffaf2] p-3">
          <div className="mb-2 flex gap-2">
            {firstTabs.map((tab) => (
              <button
                key={tab.key}
                onClick={() => onNavigate(`/admin/${tab.pages[0]}`)}
                className={`rounded-full px-3 py-1 text-xs ${
                  tab.pages.includes(page) ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-white text-[#8f8376]'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
          <div className="flex gap-2">
            {secondTabs.map((tab) => (
              <button
                key={tab.key}
                onClick={() => onNavigate(`/admin/${tab.key}`)}
                className={`rounded-full px-3 py-1 text-xs ${
                  tab.key === page ? 'bg-[#2a2a2f] text-white' : 'bg-white text-[#8f8376]'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        {page === 'notice-rules' && (
          <>
            <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#7d7267]">
              已覆盖提醒种类：上课提醒（提前1天/6小时）、作业提醒（截止前24h/后2h）、核销未预约（1/3/7天）、课时不足/到期提醒、调课审核结果即时提醒；提醒方式支持站内消息、短信、小程序订阅消息、微信模板消息。
            </div>
            <PaginatedTable
              columns={[
                { key: 'category', title: '提醒种类' },
                { key: 'remindAhead', title: '提前多久提醒' },
                { key: 'channels', title: '提醒方式', render: (row) => row.channels.join(' / ') },
                { key: 'enabled', title: '启用', render: (row) => (row.enabled ? '是' : '否') },
                {
                  key: 'action',
                  title: '操作',
                  render: (row) => (
                    <button className="text-[#bc7844]" onClick={() => openRuleModal(row)}>设置</button>
                  )
                }
              ]}
              rows={noticeRuleRows}
              pageSize={6}
            />
            {ruleModalOpen && (
              <div className="fixed inset-0 z-30 flex items-center justify-center bg-black/30 p-4">
                <button className="absolute inset-0" onClick={() => setRuleModalOpen(false)} />
                <div className="relative w-full max-w-[620px] rounded-2xl bg-white p-5 shadow-2xl">
                  <div className="mb-3 flex items-center justify-between">
                    <h3 className="text-base font-semibold">提醒规则设置</h3>
                    <button className="text-sm text-[#8f8376]" onClick={() => setRuleModalOpen(false)}>关闭</button>
                  </div>
                  <div className="grid grid-cols-2 gap-3">
                    <div className="space-y-1"><div className="text-xs text-[#7b7064]">提醒种类</div><input value={ruleForm.category} onChange={(e) => setRuleForm((p) => ({ ...p, category: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                    <div className="space-y-1"><div className="text-xs text-[#7b7064]">是否启用</div><select value={ruleForm.enabled ? '是' : '否'} onChange={(e) => setRuleForm((p) => ({ ...p, enabled: e.target.value === '是' }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>是</option><option>否</option></select></div>
                    <div className="col-span-2 space-y-1"><div className="text-xs text-[#7b7064]">提前多久提醒</div><input value={ruleForm.remindAhead} onChange={(e) => setRuleForm((p) => ({ ...p, remindAhead: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                    <div className="col-span-2 space-y-1">
                      <div className="text-xs text-[#7b7064]">提醒方式</div>
                      <div className="grid grid-cols-2 gap-2 rounded-lg border border-[#e9e2d8] p-2 text-xs">
                        {['站内消息', '短信', '小程序订阅消息', '微信模板消息'].map((item) => (
                          <label key={item} className="flex items-center gap-2">
                            <input
                              type="checkbox"
                              checked={ruleForm.channels.includes(item)}
                              onChange={(e) => {
                                setRuleForm((p) => ({
                                  ...p,
                                  channels: e.target.checked ? [...p.channels, item] : p.channels.filter((c) => c !== item)
                                }))
                              }}
                            />
                            {item}
                          </label>
                        ))}
                      </div>
                    </div>
                  </div>
                  <button onClick={saveRule} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存规则</button>
                </div>
              </div>
            )}
          </>
        )}

        {page === 'notice-templates' && (
          <>
            <div className="flex justify-end">
              <button onClick={() => openTemplateModal()} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">新增模板</button>
            </div>
            <PaginatedTable
              columns={[
                { key: 'name', title: '模板名称' },
                { key: 'scene', title: '模板场景' },
                { key: 'channels', title: '触达方式', render: (row) => row.channels.join(' / ') },
                { key: 'content', title: '模板内容' },
                { key: 'updatedAt', title: '更新时间' },
                {
                  key: 'action',
                  title: '操作',
                  render: (row) => (
                    <div className="flex gap-2">
                      <button className="text-[#bc7844]" onClick={() => openTemplateModal(row)}>修改</button>
                      <button className="text-[#a64545]" onClick={() => deleteTemplate(row.id)}>删除</button>
                    </div>
                  )
                }
              ]}
              rows={noticeTemplateRows}
              pageSize={6}
            />
            {templateModalOpen && (
              <div className="fixed inset-0 z-30 flex items-center justify-center bg-black/30 p-4">
                <button className="absolute inset-0" onClick={() => setTemplateModalOpen(false)} />
                <div className="relative w-full max-w-[700px] rounded-2xl bg-white p-5 shadow-2xl">
                  <div className="mb-3 flex items-center justify-between">
                    <h3 className="text-base font-semibold">{editingTemplateId ? '修改模板' : '新增模板'}</h3>
                    <button className="text-sm text-[#8f8376]" onClick={() => setTemplateModalOpen(false)}>关闭</button>
                  </div>
                  <div className="grid grid-cols-2 gap-3">
                    <div className="space-y-1"><div className="text-xs text-[#7b7064]">模板名称</div><input value={templateForm.name} onChange={(e) => setTemplateForm((p) => ({ ...p, name: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                    <div className="space-y-1"><div className="text-xs text-[#7b7064]">模板场景</div><input value={templateForm.scene} onChange={(e) => setTemplateForm((p) => ({ ...p, scene: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                    <div className="col-span-2 space-y-1">
                      <div className="text-xs text-[#7b7064]">触达方式</div>
                      <div className="grid grid-cols-2 gap-2 rounded-lg border border-[#e9e2d8] p-2 text-xs">
                        {['站内消息', '短信', '小程序订阅消息', '微信模板消息'].map((item) => (
                          <label key={item} className="flex items-center gap-2">
                            <input
                              type="checkbox"
                              checked={templateForm.channels.includes(item)}
                              onChange={(e) => {
                                setTemplateForm((p) => ({
                                  ...p,
                                  channels: e.target.checked ? [...p.channels, item] : p.channels.filter((c) => c !== item)
                                }))
                              }}
                            />
                            {item}
                          </label>
                        ))}
                      </div>
                    </div>
                    <div className="col-span-2 space-y-1"><div className="text-xs text-[#7b7064]">模板内容</div><textarea value={templateForm.content} onChange={(e) => setTemplateForm((p) => ({ ...p, content: e.target.value }))} className="h-28 w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                  </div>
                  <button onClick={saveTemplate} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存模板</button>
                </div>
              </div>
            )}
          </>
        )}

        {page === 'notice-touch' && (
          <PaginatedTable
            columns={[
              { key: 'scene', title: '提醒场景' },
              { key: 'target', title: '触达对象' },
              { key: 'channel', title: '触达方式' },
              { key: 'triggerAt', title: '触发时间' },
              { key: 'result', title: '触达结果' }
            ]}
            rows={touchRecordRows}
            pageSize={8}
          />
        )}
      </section>
    )
  }

  if (page === 'org-campus' || page === 'org-tree' || page === 'org-account' || page === 'org-rbac' || page === 'org-permtree') {
    const orgFirstTabs = [
      { key: 'org-manage', label: '组织管理', pages: ['org-campus', 'org-tree'] },
      { key: 'auth-manage', label: '账号与授权', pages: ['org-account', 'org-rbac', 'org-permtree'] }
    ]
    const orgSecondTabs = orgFirstTabs[0].pages.includes(page)
      ? [{ key: 'org-campus', label: '校区管理' }, { key: 'org-tree', label: '组织树管理' }]
      : [{ key: 'org-account', label: '账号管理' }, { key: 'org-rbac', label: '角色与数据权限' }, { key: 'org-permtree', label: '权限树映射' }]

    const collectPermissionLabels = (nodes, acc = {}) => {
      nodes.forEach((node) => {
        acc[node.key] = node.label
        if (node.children?.length) collectPermissionLabels(node.children, acc)
      })
      return acc
    }
    const permissionLabelMap = collectPermissionLabels(permissionCatalog)
    const previewAccount = orgAccountRows.find((item) => item.id === orgPreviewAccountId) || orgAccountRows[0]
    const previewRolePermissions = orgRoleRows
      .filter((role) => previewAccount?.roleIds.includes(role.id))
      .flatMap((role) => role.permissionKeys)
    const previewPermissionList = [...new Set(previewRolePermissions)].map((key) => permissionLabelMap[key] || key)
    const getAccountName = (accountId) => orgAccountRows.find((item) => item.id === accountId)?.name || accountId

    const togglePermExpand = (nodeId) => {
      setOrgPermExpandedIds((prev) => (prev.includes(nodeId) ? prev.filter((id) => id !== nodeId) : [...prev, nodeId]))
    }

    const renderPermissionTreeView = (node, depth = 0) => {
      const hasChildren = node.children && node.children.length > 0
      const expanded = orgPermExpandedIds.includes(node.id)
      return (
        <div key={node.id} className="space-y-2">
          <div className="rounded-lg border border-[#ece7df] bg-white p-2" style={{ marginLeft: `${depth * 16}px` }}>
            <div className="flex items-center gap-2">
              <button onClick={() => hasChildren && togglePermExpand(node.id)} className="h-5 w-5 rounded border border-[#e8dfd3] text-xs">
                {hasChildren ? (expanded ? '−' : '+') : '•'}
              </button>
              <span className="text-xs font-medium">{node.label}</span>
              <span className="rounded-full bg-[#faf8f4] px-2 py-0.5 text-[10px] text-[#7d7267]">{node.key}</span>
            </div>
          </div>
          {hasChildren && expanded && <div className="space-y-2">{node.children.map((child) => renderPermissionTreeView(child, depth + 1))}</div>}
        </div>
      )
    }

    const renderPermissionSelectNode = (node, depth = 0) => {
      const hasChildren = node.children && node.children.length > 0
      const expanded = orgPermExpandedIds.includes(node.id)
      return (
        <div key={node.id} className="space-y-1">
          <label className="flex items-center gap-2 text-xs" style={{ paddingLeft: `${depth * 14}px` }}>
            <button type="button" onClick={() => hasChildren && togglePermExpand(node.id)} className="h-4 w-4 rounded border border-[#e8dfd3] text-[10px]">
              {hasChildren ? (expanded ? '−' : '+') : '•'}
            </button>
            <input
              type="checkbox"
              checked={orgRoleForm.permissionKeys.includes(node.key)}
              onChange={(e) =>
                setOrgRoleForm((p) => ({
                  ...p,
                  permissionKeys: e.target.checked
                    ? [...p.permissionKeys, node.key]
                    : p.permissionKeys.filter((item) => item !== node.key)
                }))
              }
            />
            {node.label}
          </label>
          {hasChildren && expanded && <div className="space-y-1">{node.children.map((child) => renderPermissionSelectNode(child, depth + 1))}</div>}
        </div>
      )
    }

    const renderTreeNode = (node, depth = 0) => {
      const isExpanded = orgExpandedNodeIds.includes(node.id)
      const hasChildren = node.children && node.children.length > 0
      return (
        <div key={node.id} className="space-y-2">
          <div className="rounded-xl border border-[#f0ebe3] bg-[#fffdf9] p-3" style={{ marginLeft: `${depth * 16}px` }}>
            <div className="flex items-start justify-between">
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => hasChildren && toggleNodeExpand(node.id)}
                    className="h-6 w-6 rounded-md border border-[#e9e2d8] text-xs"
                  >
                    {hasChildren ? (isExpanded ? '−' : '+') : '•'}
                  </button>
                  <span className="text-sm font-semibold">{node.name}</span>
                  <span className="rounded-full bg-[#faf8f4] px-2 py-0.5 text-xs">{node.type}</span>
                  <span className="rounded-full bg-[#fff4ea] px-2 py-0.5 text-xs text-[#b55e22]">{node.status}</span>
                </div>
                <div className="text-xs text-[#7d7267]">负责人：{node.manager}</div>
                <div className="flex flex-wrap gap-1">
                  {node.accountIds.map((accountId) => (
                    <span key={accountId} className="rounded-full bg-[#eef6ff] px-2 py-0.5 text-xs text-[#2f5d8b]">
                      {getAccountName(accountId)}
                    </span>
                  ))}
                  {node.accountIds.length === 0 && <span className="text-xs text-[#9b9187]">未绑定教务账号</span>}
                </div>
              </div>
              <div className="flex gap-2 text-xs">
                <button className="text-[#bc7844]" onClick={() => openNodeModal(node)}>修改节点</button>
                {node.type !== 'CAMPUS' && <button className="text-[#a64545]" onClick={() => removeNode(node.id)}>删除节点</button>}
              </div>
            </div>
                <div className="mt-2 flex items-center gap-2">
                  <button onClick={() => openBindAccountModal(node)} className="rounded-lg border border-[#e8dfd3] px-2 py-1 text-xs">挂载绑定教务账号</button>
                </div>
          </div>
          {hasChildren && isExpanded && (
            <div className="ml-3 border-l-2 border-dashed border-[#eadfce] pl-3 space-y-2">
              {node.children.map((child) => renderTreeNode(child, depth + 1))}
            </div>
          )}
        </div>
      )
    }

    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">组织与权限</h2>
        <div className="rounded-2xl border border-[#f0ebe3] bg-[#fffaf2] p-3">
          <div className="mb-2 flex gap-2">
            {orgFirstTabs.map((tab) => (
              <button
                key={tab.key}
                onClick={() => onNavigate(`/admin/${tab.pages[0]}`)}
                className={`rounded-full px-3 py-1 text-xs ${
                  tab.pages.includes(page) ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-white text-[#8f8376]'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
          <div className="flex flex-wrap gap-2">
            {orgSecondTabs.map((tab) => (
              <button
                key={tab.key}
                onClick={() => onNavigate(`/admin/${tab.key}`)}
                className={`rounded-full px-3 py-1 text-xs ${
                  tab.key === page ? 'bg-[#2a2a2f] text-white' : 'bg-white text-[#8f8376]'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        {page === 'org-campus' && (
          <>
            <div className="flex items-center justify-between">
              <div className="text-sm text-[#7d7267]">校区是组织树顶层节点（CAMPUS），支持创建、修改和状态维护。</div>
              <button onClick={() => openCampusModal()} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">创建校区</button>
            </div>
            <PaginatedTable
              columns={[
                { key: 'code', title: '校区编码' },
                { key: 'name', title: '校区名称' },
                { key: 'principal', title: '负责人' },
                { key: 'phone', title: '联系方式' },
                { key: 'roomCount', title: '教室数' },
                { key: 'status', title: '状态' },
                { key: 'action', title: '操作', render: (row) => <button className="text-[#bc7844]" onClick={() => openCampusModal(row)}>修改</button> }
              ]}
              rows={orgCampusRows}
              pageSize={8}
            />
          </>
        )}

        {page === 'org-tree' && (
          <>
            <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#7d7267]">
              组织树层级：CAMPUS（顶层）→ DEPT（部门）→ GROUP（分组）。节点支持展开、修改、删除，并可绑定教务账号。
            </div>
            <div className="space-y-2">{orgTreeNodes.map((node) => renderTreeNode(node))}</div>
          </>
        )}

        {page === 'org-account' && (
          <>
            <div className="flex items-center justify-between">
              <div className="text-sm text-[#7d7267]">创建教务账号是所有组织权限操作的起点，可绑定角色并控制可见Tab与按钮。</div>
              <button onClick={() => openAccountModal()} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">创建账号</button>
            </div>
            <PaginatedTable
              columns={[
                { key: 'username', title: '账号' },
                { key: 'name', title: '姓名' },
                { key: 'phone', title: '联系方式' },
                { key: 'campusId', title: '所属校区', render: (row) => orgCampusRows.find((item) => item.id === row.campusId)?.name || row.campusId },
                { key: 'roleIds', title: '绑定角色', render: (row) => row.roleIds.map((roleId) => orgRoleRows.find((role) => role.id === roleId)?.name || roleId).join(' / ') },
                { key: 'status', title: '状态' },
                {
                  key: 'action',
                  title: '操作',
                  render: (row) => (
                    <div className="flex gap-2">
                      <button className="text-[#bc7844]" onClick={() => openAccountModal(row)}>修改</button>
                      <button className="text-[#a64545]" onClick={() => toggleAccountStatus(row.id)}>{row.status === '启用' ? '禁用' : '启用'}</button>
                    </div>
                  )
                }
              ]}
              rows={orgAccountRows}
              pageSize={8}
            />
            <div className="rounded-xl border border-[#f0ebe3] p-3">
              <div className="mb-2 text-sm font-semibold">账号可见性预览</div>
              <div className="mb-2 flex items-center gap-2">
                <span className="text-xs text-[#7b7064]">预览账号</span>
                <select value={orgPreviewAccountId} onChange={(e) => setOrgPreviewAccountId(e.target.value)} className="rounded-lg border border-[#e9e2d8] px-2 py-1 text-xs">
                  {orgAccountRows.map((account) => (
                    <option key={account.id} value={account.id}>{account.name}</option>
                  ))}
                </select>
              </div>
              <div className="grid grid-cols-2 gap-2 text-xs">
                {previewPermissionList.map((item) => (
                  <div key={item} className="rounded-lg bg-[#faf8f4] p-2">{item}</div>
                ))}
              </div>
            </div>
          </>
        )}

        {page === 'org-rbac' && (
          <>
            <div className="flex items-center justify-between">
              <div className="text-sm text-[#7d7267]">角色支持创建和修改，绑定权限树节点后，账号自动继承对应一级/二级Tab及操作按钮。</div>
              <button onClick={() => openRoleModal()} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">创建角色</button>
            </div>
            <PaginatedTable
              columns={[
                { key: 'name', title: '角色名称' },
                { key: 'dataScope', title: '数据权限范围' },
                { key: 'permissionKeys', title: '绑定节点数', render: (row) => row.permissionKeys.length },
                { key: 'updatedAt', title: '更新时间' },
                { key: 'action', title: '操作', render: (row) => <button className="text-[#bc7844]" onClick={() => openRoleModal(row)}>修改</button> }
              ]}
              rows={orgRoleRows}
              pageSize={8}
            />
          </>
        )}

        {page === 'org-permtree' && (
          <>
            <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#7d7267]">
              权限树与导航匹配规则：一级节点对应一级Tab，二级节点对应二级Tab，叶子节点对应二级Tab下操作按钮。
            </div>
            <div className="space-y-2">{permissionCatalog.map((node) => renderPermissionTreeView(node))}</div>
          </>
        )}

        {orgCampusModalOpen && (
          <div className="fixed inset-0 z-30 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setOrgCampusModalOpen(false)} />
            <div className="relative w-full max-w-[720px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">{orgCampusEditingId ? '修改校区' : '创建校区'}</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setOrgCampusModalOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">校区编码</div><input value={orgCampusForm.code} onChange={(e) => setOrgCampusForm((p) => ({ ...p, code: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">校区名称</div><input value={orgCampusForm.name} onChange={(e) => setOrgCampusForm((p) => ({ ...p, name: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">负责人</div><input value={orgCampusForm.principal} onChange={(e) => setOrgCampusForm((p) => ({ ...p, principal: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">联系方式</div><input value={orgCampusForm.phone} onChange={(e) => setOrgCampusForm((p) => ({ ...p, phone: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">教室数</div><input value={orgCampusForm.roomCount} onChange={(e) => setOrgCampusForm((p) => ({ ...p, roomCount: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">状态</div><select value={orgCampusForm.status} onChange={(e) => setOrgCampusForm((p) => ({ ...p, status: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>启用</option><option>禁用</option></select></div>
                <div className="col-span-2 space-y-1"><div className="text-xs text-[#7b7064]">地址</div><input value={orgCampusForm.address} onChange={(e) => setOrgCampusForm((p) => ({ ...p, address: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
              </div>
              <button onClick={saveCampus} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存校区</button>
            </div>
          </div>
        )}

        {orgAccountModalOpen && (
          <div className="fixed inset-0 z-30 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setOrgAccountModalOpen(false)} />
            <div className="relative w-full max-w-[720px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">{orgAccountEditingId ? '修改账号' : '创建账号'}</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setOrgAccountModalOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">账号用户名</div><input value={orgAccountForm.username} onChange={(e) => setOrgAccountForm((p) => ({ ...p, username: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">姓名</div><input value={orgAccountForm.name} onChange={(e) => setOrgAccountForm((p) => ({ ...p, name: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">联系方式</div><input value={orgAccountForm.phone} onChange={(e) => setOrgAccountForm((p) => ({ ...p, phone: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">账号状态</div><select value={orgAccountForm.status} onChange={(e) => setOrgAccountForm((p) => ({ ...p, status: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>启用</option><option>禁用</option></select></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">绑定校区</div><select value={orgAccountForm.campusId} onChange={(e) => setOrgAccountForm((p) => ({ ...p, campusId: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none">{orgCampusRows.map((campus) => <option key={campus.id} value={campus.id}>{campus.name}</option>)}</select></div>
                <div className="space-y-1">
                  <div className="text-xs text-[#7b7064]">绑定角色（可多选）</div>
                  <div className="rounded-lg border border-[#e9e2d8] p-2">
                    <div className="grid grid-cols-2 gap-2 text-xs">
                      {orgRoleRows.map((role) => (
                        <label key={role.id} className="flex items-center gap-2">
                          <input
                            type="checkbox"
                            checked={orgAccountForm.roleIds.includes(role.id)}
                            onChange={(e) =>
                              setOrgAccountForm((p) => ({
                                ...p,
                                roleIds: e.target.checked
                                  ? [...p.roleIds, role.id]
                                  : p.roleIds.filter((id) => id !== role.id)
                              }))
                            }
                          />
                          {role.name}
                        </label>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
              <button onClick={saveAccount} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存账号</button>
            </div>
          </div>
        )}

        {orgRoleModalOpen && (
          <div className="fixed inset-0 z-30 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setOrgRoleModalOpen(false)} />
            <div className="relative w-full max-w-[780px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">{orgRoleEditingId ? '修改角色' : '创建角色'}</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setOrgRoleModalOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">角色名称</div><input value={orgRoleForm.name} onChange={(e) => setOrgRoleForm((p) => ({ ...p, name: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">数据权限范围</div><select value={orgRoleForm.dataScope} onChange={(e) => setOrgRoleForm((p) => ({ ...p, dataScope: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>ALL</option><option>CAMPUS</option><option>ASSIGNED</option><option>SELF</option></select></div>
                <div className="col-span-2 space-y-2">
                  <div className="text-xs text-[#7b7064]">权限树节点绑定</div>
                  <div className="rounded-lg border border-[#e9e2d8] p-2 space-y-1">
                    {permissionCatalog.map((node) => renderPermissionSelectNode(node))}
                  </div>
                </div>
              </div>
              <button onClick={saveRole} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存角色</button>
            </div>
          </div>
        )}

        {orgBindModalOpen && (
          <div className="fixed inset-0 z-30 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setOrgBindModalOpen(false)} />
            <div className="relative w-full max-w-[620px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">挂载绑定教务账号</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setOrgBindModalOpen(false)}>关闭</button>
              </div>
              <div className="rounded-lg border border-[#e9e2d8] p-3">
                <div className="mb-2 text-xs text-[#7b7064]">可多选绑定，体现该组织节点下教务的直属关系。</div>
                <div className="grid grid-cols-2 gap-2 text-xs">
                  {orgAccountRows.map((account) => (
                    <label key={account.id} className="flex items-center gap-2">
                      <input
                        type="checkbox"
                        checked={orgBindAccountIds.includes(account.id)}
                        onChange={(e) =>
                          setOrgBindAccountIds((prev) =>
                            e.target.checked ? [...prev, account.id] : prev.filter((id) => id !== account.id)
                          )
                        }
                      />
                      {account.name}（{account.username}）
                    </label>
                  ))}
                </div>
              </div>
              <button onClick={saveBindAccounts} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存绑定关系</button>
            </div>
          </div>
        )}

        {orgNodeModalOpen && (
          <div className="fixed inset-0 z-30 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setOrgNodeModalOpen(false)} />
            <div className="relative w-full max-w-[560px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">修改组织节点</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setOrgNodeModalOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">节点名称</div><input value={orgNodeForm.name} onChange={(e) => setOrgNodeForm((p) => ({ ...p, name: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">负责人</div><input value={orgNodeForm.manager} onChange={(e) => setOrgNodeForm((p) => ({ ...p, manager: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="col-span-2 space-y-1"><div className="text-xs text-[#7b7064]">节点状态</div><select value={orgNodeForm.status} onChange={(e) => setOrgNodeForm((p) => ({ ...p, status: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>启用</option><option>禁用</option></select></div>
              </div>
              <button onClick={saveNode} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存节点</button>
            </div>
          </div>
        )}
      </section>
    )
  }

  return (
    <section className="space-y-4">
      <h2 className="text-sm font-semibold">数据看板（二期）预留</h2>
      <PaginatedTable
        columns={[
          { key: 'module', title: '模块' },
          { key: 'desc', title: '说明' }
        ]}
        rows={[
          { id: 'A1', module: '校区经营', desc: '营收、约课转化、课包销售' },
          { id: 'A2', module: '教学质量', desc: '作业点评覆盖率、反馈完成率' },
          { id: 'A3', module: '学员活跃与续费', desc: '低活跃、到期、续费预测' }
        ]}
      />
    </section>
  )
}

function TeacherView({ page }) {
  const [teacherTip, setTeacherTip] = useState('')
  const [teacherMessageFilter, setTeacherMessageFilter] = useState('未读')
  const [teacherMessageDrawerOpen, setTeacherMessageDrawerOpen] = useState(false)
  const [activeTeacherMessageId, setActiveTeacherMessageId] = useState(notifications[0].id)
  const [teacherTimetableView, setTeacherTimetableView] = useState('周视图')
  const [teacherTimetableCampus, setTeacherTimetableCampus] = useState('全部校区')
  const [teacherTimetableType, setTeacherTimetableType] = useState('全部类型')
  const [teacherTimetableDate, setTeacherTimetableDate] = useState('2026-03-24')
  const [teacherCourseDrawerOpen, setTeacherCourseDrawerOpen] = useState(false)
  const [activeCourseId, setActiveCourseId] = useState(teacherTodayCourses[0].id)
  const [attendanceTab, setAttendanceTab] = useState('今日')
  const [attendanceRows, setAttendanceRows] = useState(
    teacherTodayCourses.map((course, idx) => ({
      id: course.id,
      title: course.title,
      time: course.time,
      room: course.room,
      period: idx === 0 ? '今日' : '本周',
      status: '待签到',
      students: (course.students || []).map((student) => ({
        name: student.name,
        checked: !!student.checked,
        leave: false,
        deductHours: student.checked ? 1 : 0
      }))
    }))
  )
  const [activeAttendanceId, setActiveAttendanceId] = useState(teacherTodayCourses[0].id)
  const [attendanceDrawerOpen, setAttendanceDrawerOpen] = useState(false)
  const [feedbackQueueRows, setFeedbackQueueRows] = useState(
    teacherTodayCourses.map((course, idx) => ({
      id: course.id,
      title: course.title,
      time: course.time,
      status: idx === 0 ? '待反馈' : '已反馈'
    }))
  )
  const [activeFeedbackId, setActiveFeedbackId] = useState(teacherTodayCourses[0].id)
  const [feedbackDrawerOpen, setFeedbackDrawerOpen] = useState(false)
  const [feedbackForm, setFeedbackForm] = useState({
    summary: '',
    review: '',
    studentComment: '',
    nextAdvice: '',
    homeworkTitle: '',
    homeworkType: '视频',
    homeworkDeadline: '2026-03-26 20:00'
  })
  const [homeworkRows, setHomeworkRows] = useState(
    homeworkPool.map((item, idx) => ({
      ...item,
      className: idx % 2 === 0 ? '标准课A班' : '体验班',
      course: idx % 2 === 0 ? '标准课' : '体验课',
      reminderSent: false
    }))
  )
  const [activeHomeworkId, setActiveHomeworkId] = useState(homeworkPool[0].id)
  const [homeworkDrawerOpen, setHomeworkDrawerOpen] = useState(false)
  const [homeworkCourseKeyword, setHomeworkCourseKeyword] = useState('')
  const [homeworkAssetView, setHomeworkAssetView] = useState('')
  const [homeworkComment, setHomeworkComment] = useState('')
  const [teacherStudentDrawerOpen, setTeacherStudentDrawerOpen] = useState(false)
  const [activeTeacherStudentId, setActiveTeacherStudentId] = useState(students[0].id)
  const [teacherAvailabilityRows, setTeacherAvailabilityRows] = useState([
    { id: 'AT-001', mode: '周重复', day: '周一', time: '13:00-18:00', campus: '北环国基路校区', status: '启用' },
    { id: 'AT-002', mode: '周重复', day: '周三', time: '14:00-20:00', campus: '北环国基路校区', status: '启用' },
    { id: 'AT-003', mode: '临时调整', day: '2026-03-28', time: '09:00-12:00', campus: '西大剧院校区', status: '启用' }
  ])
  const [availabilityModalOpen, setAvailabilityModalOpen] = useState(false)
  const [availabilityForm, setAvailabilityForm] = useState({
    mode: '周重复',
    day: '周二',
    time: '',
    campus: '北环国基路校区',
    status: '启用'
  })
  const [availabilityPref, setAvailabilityPref] = useState({
    teachingTypes: ['团课'],
    subjects: ['木吉他'],
    capacity: '5'
  })

  const teacherKpiRows = [
    { id: 'TK-1', label: '今日新增学员', value: 2, trend: '较昨日 +1' },
    { id: 'TK-2', label: '本周核销数', value: 17, trend: '较上周 +3' },
    { id: 'TK-3', label: '本周上课节数', value: 26, trend: '完成率 93%' },
    { id: 'TK-4', label: '本周营收', value: '¥12,800', trend: '数据演示占位' }
  ]
  const teacherRemindRows = [
    { id: 'TR-1', type: '到期学员', count: '3人', next: '跟进续费' },
    { id: 'TR-2', type: '核销未预约', count: '2人', next: '提醒预约' },
    { id: 'TR-3', type: '作业未提交', count: '5人', next: '批量提醒' }
  ]
  const teacherMessageFilterOptions = ['未读', '全部', '课程提醒', '作业提醒', '系统公告', '审批结果']
  const teacherMessages = notifications.filter((item) => {
    if (teacherMessageFilter === '全部') return true
    if (teacherMessageFilter === '未读') return !item.read
    return item.category === teacherMessageFilter
  })

  const timetableRows = teacherTodayCourses
    .map((course, idx) => ({
      id: course.id,
      date: idx === 0 ? '2026-03-24' : '2026-03-26',
      time: course.time,
      title: course.title,
      type: idx === 0 ? '标准课' : '一对一',
      campus: course.room.includes('北环') ? '北环国基路校区' : '西大剧院校区',
      room: course.room
    }))
    .filter((item) => teacherTimetableCampus === '全部校区' || item.campus === teacherTimetableCampus)
    .filter((item) => teacherTimetableType === '全部类型' || item.type === teacherTimetableType)

  const activeCourse = teacherTodayCourses.find((item) => item.id === activeCourseId) || teacherTodayCourses[0]
  const filteredAttendanceRows = attendanceRows.filter((item) => item.period === attendanceTab)
  const activeAttendance = attendanceRows.find((item) => item.id === activeAttendanceId) || attendanceRows[0]
  const activeFeedback = feedbackQueueRows.find((item) => item.id === activeFeedbackId) || feedbackQueueRows[0]
  const activeHomework = homeworkRows.find((item) => item.id === activeHomeworkId) || homeworkRows[0]
  const homeworkCourseGroups = homeworkRows.reduce((acc, item) => {
    const found = acc.find((group) => group.course === item.course)
    if (found) {
      found.submissions.push(item)
      return acc
    }
    return [...acc, { id: `HG-${acc.length + 1}`, course: item.course, submissions: [item] }]
  }, [])
  const filteredHomeworkCourseGroups = homeworkCourseGroups.filter((group) =>
    group.course.includes(homeworkCourseKeyword.trim())
  )
  const getHomeworkAssetType = (row) => {
    if ((row.media || '').includes('音频')) return 'audio'
    if ((row.media || '').includes('视频')) return 'video'
    if ((row.media || '').includes('图文') || (row.media || '').includes('图片')) return 'image'
    return 'text'
  }
  const assignedStudents = students.slice(0, 6).map((item, idx) => ({
    ...item,
    level: ['A', 'B', 'A', 'C', 'B', 'A'][idx] || 'B',
    stageReport: idx % 2 === 0 ? '已完成阶段测评' : '待完成阶段测评',
    transferNode: idx % 2 === 0 ? '团购已转正课' : '团购核销后跟进中'
  }))
  const activeTeacherStudent = assignedStudents.find((item) => item.id === activeTeacherStudentId) || assignedStudents[0]

  const toggleAttendanceStudent = (name) => {
    setAttendanceRows((prev) =>
      prev.map((course) =>
        course.id === activeAttendance.id
          ? {
              ...course,
              students: course.students.map((student) =>
                student.name === name
                  ? { ...student, checked: !student.checked, leave: student.checked ? true : false, deductHours: student.checked ? 0 : 1 }
                  : student
              )
            }
          : course
      )
    )
  }

  const setAttendanceDeduct = (name, value) => {
    setAttendanceRows((prev) =>
      prev.map((course) =>
        course.id === activeAttendance.id
          ? {
              ...course,
              students: course.students.map((student) =>
                student.name === name ? { ...student, deductHours: Number(value || 0) } : student
              )
            }
          : course
      )
    )
  }

  const submitAttendance = () => {
    setAttendanceRows((prev) =>
      prev.map((course) =>
        course.id === activeAttendance.id
          ? { ...course, status: '已完成扣减' }
          : course
      )
    )
    setTeacherTip(`已完成签到并生成上课记录：${activeAttendance.title}`)
    setAttendanceDrawerOpen(false)
  }

  const submitFeedback = () => {
    if (!feedbackForm.summary || !feedbackForm.review || !feedbackForm.homeworkTitle) {
      setTeacherTip('请完整填写教学内容、课后回顾和作业标题')
      return
    }
    setFeedbackQueueRows((prev) =>
      prev.map((item) => (item.id === activeFeedback.id ? { ...item, status: '已反馈' } : item))
    )
    setHomeworkRows((prev) => [
      {
        id: `H-${String(prev.length + 1).padStart(2, '0')}`,
        student: '全班',
        title: feedbackForm.homeworkTitle,
        status: '待提交',
        media: `${feedbackForm.homeworkType}作业`,
        deadline: `截止 ${feedbackForm.homeworkDeadline}`,
        className: '课堂发布',
        course: activeFeedback.title,
        reminderSent: false
      },
      ...prev
    ])
    setTeacherTip(`课后反馈已同步成长记录并发布作业：${feedbackForm.homeworkTitle}`)
    setFeedbackDrawerOpen(false)
  }

  const submitHomeworkComment = () => {
    if (!homeworkComment) {
      setTeacherTip('请先输入批改点评内容')
      return
    }
    setHomeworkRows((prev) =>
      prev.map((item) => (item.id === activeHomework.id ? { ...item, status: '已批改' } : item))
    )
    setHomeworkComment('')
    setTeacherTip(`已完成作业批改：${activeHomework.title}`)
    setHomeworkDrawerOpen(false)
  }

  const saveAvailability = () => {
    if (!availabilityForm.day || !availabilityForm.time) {
      setTeacherTip('请填写可用时间配置')
      return
    }
    const newRow = { id: `AT-${String(teacherAvailabilityRows.length + 1).padStart(3, '0')}`, ...availabilityForm }
    setTeacherAvailabilityRows((prev) => [newRow, ...prev])
    setAvailabilityModalOpen(false)
    setTeacherTip(`已保存可授课时间：${newRow.day} ${newRow.time}`)
  }

  const saveAvailabilityPref = () => {
    setTeacherTip('已保存授课偏好与承载量配置')
  }

  if (page === 'teacher-workbench-overview') {
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">工作台</h2>
        <div className="grid grid-cols-4 gap-4">
          {teacherKpiRows.map((item) => (
            <article key={item.id} className="rounded-2xl border border-[#f0ebe3] bg-[#fffcf8] p-4">
              <div className="text-xs text-[#8b8177]">{item.label}</div>
              <div className="mt-2 text-2xl font-semibold text-[#2e2a25]">{item.value}</div>
              <div className="mt-2 text-xs text-[#bc7844]">{item.trend}</div>
            </article>
          ))}
        </div>
        <div className="grid grid-cols-[1.3fr_1fr] gap-4">
          <article className="rounded-2xl border border-[#f0ebe3] p-4">
            <div className="mb-2 text-sm font-semibold">今日课程</div>
            <PaginatedTable
              columns={[
                { key: 'time', title: '上课时间' },
                { key: 'title', title: '课程名称' },
                { key: 'room', title: '教室' },
                { key: 'studentCount', title: '人数' },
                { key: 'action', title: '操作', render: (row) => <button onClick={() => { setActiveCourseId(row.id); setTeacherCourseDrawerOpen(true) }} className="text-[#bc7844]">课程详情</button> }
              ]}
              rows={teacherTodayCourses}
              pageSize={4}
            />
          </article>
          <article className="rounded-2xl border border-[#f0ebe3] p-4">
            <div className="mb-2 text-sm font-semibold">待办</div>
            <PaginatedTable
              columns={[
                { key: 'todo', title: '待办事项' },
                { key: 'count', title: '数量' },
                { key: 'next', title: '处理动作' }
              ]}
              rows={[
                { id: 'TT-1', todo: '待签到课程', count: '2节', next: '进入上课签到' },
                { id: 'TT-2', todo: '待写课后反馈', count: '1节', next: '进入课后反馈' },
                { id: 'TT-3', todo: '待批改作业', count: '3份', next: '进入作业管理' },
                { id: 'TT-4', todo: '未提交提醒', count: '5人', next: '批量提醒' }
              ]}
              pageSize={4}
            />
          </article>
        </div>
        <article className="rounded-2xl border border-[#f0ebe3] p-4">
          <div className="mb-2 text-sm font-semibold">提醒</div>
          <PaginatedTable
            columns={[
              { key: 'type', title: '提醒类型' },
              { key: 'count', title: '数量' },
              { key: 'next', title: '建议操作' }
            ]}
            rows={teacherRemindRows}
            pageSize={6}
          />
        </article>
        {teacherCourseDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setTeacherCourseDrawerOpen(false)} />
            <div className="h-full w-[500px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">课程详情</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setTeacherCourseDrawerOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div className="rounded-xl bg-[#faf8f4] p-3">课程：{activeCourse.title}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">时间：{activeCourse.time}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">教室：{activeCourse.room}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">签到入口：上课签到</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">课后反馈入口：课后反馈 / 反馈编辑</div>
              </div>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teacher-messages-list') {
    const activeMessage = notifications.find((item) => item.id === activeTeacherMessageId) || notifications[0]
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">消息中心 / 消息列表</h2>
        <div className="flex flex-wrap gap-2">
          {teacherMessageFilterOptions.map((item) => (
            <button key={item} onClick={() => setTeacherMessageFilter(item)} className={`rounded-full px-3 py-1 text-xs ${teacherMessageFilter === item ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#8f8376]'}`}>{item}</button>
          ))}
        </div>
        <PaginatedTable
          columns={[
            { key: 'title', title: '消息标题' },
            { key: 'category', title: '类型' },
            { key: 'time', title: '时间' },
            { key: 'status', title: '状态', render: (row) => (row.read ? '已读' : '未读') },
            { key: 'action', title: '操作', render: (row) => <button onClick={() => { setActiveTeacherMessageId(row.id); setTeacherMessageDrawerOpen(true) }} className="text-[#bc7844]">查看详情</button> }
          ]}
          rows={teacherMessages}
          pageSize={6}
        />
        {teacherMessageDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setTeacherMessageDrawerOpen(false)} />
            <div className="h-full w-[500px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">{activeMessage.title}</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setTeacherMessageDrawerOpen(false)}>关闭</button>
              </div>
              <div className="space-y-2 text-sm">
                <div className="rounded-xl bg-[#faf8f4] p-3">消息类型：{activeMessage.category}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">发送时间：{activeMessage.time}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">内容：{activeMessage.content}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">关联入口：{activeMessage.category === '课程提醒' ? '我的课表' : '工作台'}</div>
              </div>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teacher-timetable') {
    const weekDays = ['2026-03-24', '2026-03-25', '2026-03-26', '2026-03-27', '2026-03-28', '2026-03-29', '2026-03-30']
    const monthDays = Array.from({ length: 31 }, (_, idx) => `2026-03-${String(idx + 1).padStart(2, '0')}`)
    const calendarDays = teacherTimetableView === '日视图'
      ? [teacherTimetableDate]
      : teacherTimetableView === '周视图'
        ? weekDays
        : monthDays
    const dayCourseRows = timetableRows.filter((item) => item.date === teacherTimetableDate)
    return (
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold">我的课表 / 日周视图</h2>
          <div className="flex items-center gap-2">
            <button onClick={() => setTeacherTimetableView('日视图')} className={`rounded-lg px-2.5 py-1.5 text-xs ${teacherTimetableView === '日视图' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>日视图</button>
            <button onClick={() => setTeacherTimetableView('周视图')} className={`rounded-lg px-2.5 py-1.5 text-xs ${teacherTimetableView === '周视图' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>周视图</button>
            <button onClick={() => setTeacherTimetableView('月视图')} className={`rounded-lg px-2.5 py-1.5 text-xs ${teacherTimetableView === '月视图' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>月视图</button>
            <select value={teacherTimetableCampus} onChange={(e) => setTeacherTimetableCampus(e.target.value)} className="rounded-lg border border-[#e8dfd3] px-2 py-1.5 text-xs outline-none"><option>全部校区</option><option>北环国基路校区</option><option>西大剧院校区</option></select>
            <select value={teacherTimetableType} onChange={(e) => setTeacherTimetableType(e.target.value)} className="rounded-lg border border-[#e8dfd3] px-2 py-1.5 text-xs outline-none"><option>全部类型</option><option>标准课</option><option>一对一</option></select>
          </div>
        </div>
        <div className="grid grid-cols-7 gap-3">
          {calendarDays.map((date) => {
            const items = timetableRows.filter((row) => row.date === date)
            return (
              <button key={date} onClick={() => setTeacherTimetableDate(date)} className={`rounded-2xl border p-3 text-left ${teacherTimetableDate === date ? 'border-[#ff9b54] bg-[#fff8f1]' : 'border-[#f0ebe3] hover:bg-[#faf8f4]'}`}>
                <div className="text-xs text-[#8f8376]">{date}</div>
                <div className="mt-1 text-xs text-[#6f665d]">{items.length} 节课</div>
                <div className="mt-2 space-y-1">
                  {items.slice(0, 2).map((item) => (
                    <div key={item.id} className="rounded bg-white px-2 py-1 text-[11px] text-[#6a6259]">{item.time}</div>
                  ))}
                  {items.length > 2 && <div className="text-[11px] text-[#8f8376]">+{items.length - 2} 节</div>}
                </div>
              </button>
            )
          })}
        </div>
        <article className="rounded-2xl border border-[#f0ebe3] p-4">
          <div className="mb-2 text-sm font-semibold">当日课程（{teacherTimetableDate}）</div>
          <div className="space-y-2">
            {dayCourseRows.length === 0 && (
              <div className="rounded-xl bg-[#faf8f4] p-3 text-sm text-[#8f8376]">当日暂无课程</div>
            )}
            {dayCourseRows.map((row) => (
              <div key={row.id} className="rounded-xl border border-[#f0ebe3] p-3">
                <div className="flex items-center justify-between">
                  <div>
                    <div className="text-sm font-medium">{row.time} · {row.title}</div>
                    <div className="text-xs text-[#8f8376]">{row.type} / {row.campus} / {row.room}</div>
                  </div>
                  <button onClick={() => { setActiveCourseId(row.id); setTeacherCourseDrawerOpen(true) }} className="text-[#bc7844] text-sm">课程详情</button>
                </div>
              </div>
            ))}
          </div>
        </article>
        {teacherCourseDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setTeacherCourseDrawerOpen(false)} />
            <div className="h-full w-[500px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">课程详情</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setTeacherCourseDrawerOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div className="rounded-xl bg-[#faf8f4] p-3">课程：{activeCourse.title}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">时间：{activeCourse.time}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">教室：{activeCourse.room}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">签到入口：上课签到</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">课后反馈入口：课后反馈 / 反馈编辑</div>
              </div>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teacher-attendance') {
    return (
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold">上课签到 / 待签到列表</h2>
          <div className="flex gap-2 text-xs">
            <button onClick={() => setAttendanceTab('今日')} className={`rounded-lg px-2.5 py-1.5 ${attendanceTab === '今日' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>今日</button>
            <button onClick={() => setAttendanceTab('本周')} className={`rounded-lg px-2.5 py-1.5 ${attendanceTab === '本周' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>本周</button>
          </div>
        </div>
        <PaginatedTable
          columns={[
            { key: 'title', title: '课程' },
            { key: 'time', title: '时间' },
            { key: 'room', title: '教室' },
            { key: 'status', title: '状态' },
            {
              key: 'action',
              title: '操作',
              render: (row) => (
                <button
                  onClick={() => {
                    setActiveAttendanceId(row.id)
                    setAttendanceDrawerOpen(true)
                  }}
                  className="text-[#bc7844]"
                >
                  签到详情
                </button>
              )
            }
          ]}
          rows={filteredAttendanceRows}
          pageSize={6}
        />
        {attendanceDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setAttendanceDrawerOpen(false)} />
            <div className="h-full w-[520px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">签到详情</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setAttendanceDrawerOpen(false)}>关闭</button>
              </div>
              <div className="mb-2 rounded-xl bg-[#faf8f4] p-3 text-sm">{activeAttendance.title} · {activeAttendance.time}</div>
              <div className="space-y-2">
                {activeAttendance.students.map((student) => (
                  <div key={student.name} className="rounded-xl border border-[#f0ebe3] p-3">
                    <div className="flex items-center justify-between text-sm">
                      <div>{student.name}</div>
                      <button onClick={() => toggleAttendanceStudent(student.name)} className={`rounded px-2 py-1 text-xs ${student.checked ? 'bg-[#effaf1] text-[#2f8a47]' : 'bg-[#fff1ef] text-[#c9413a]'}`}>{student.checked ? '已签到' : '请假'}</button>
                    </div>
                    <div className="mt-2 flex items-center justify-between text-xs text-[#7b7064]">
                      <span>特殊扣减课时</span>
                      <input value={student.deductHours} onChange={(e) => setAttendanceDeduct(student.name, e.target.value)} className="w-16 rounded-md border border-[#e9e2d8] px-2 py-1 text-center outline-none" />
                    </div>
                  </div>
                ))}
              </div>
              <button onClick={submitAttendance} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">完成签到并生成上课记录</button>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teacher-feedback') {
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">课后反馈 / 待反馈列表</h2>
        <PaginatedTable
          columns={[
            { key: 'title', title: '课程' },
            { key: 'time', title: '时间' },
            { key: 'status', title: '状态' },
            {
              key: 'action',
              title: '操作',
              render: (row) => (
                <button
                  onClick={() => {
                    setActiveFeedbackId(row.id)
                    setFeedbackDrawerOpen(true)
                  }}
                  className="text-[#bc7844]"
                >
                  反馈详情
                </button>
              )
            }
          ]}
          rows={feedbackQueueRows}
          pageSize={6}
        />
        {feedbackDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setFeedbackDrawerOpen(false)} />
            <div className="h-full w-[560px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">反馈详情</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setFeedbackDrawerOpen(false)}>关闭</button>
              </div>
              <div className="mb-2 rounded-xl bg-[#faf8f4] p-3 text-sm">{activeFeedback.title} · {activeFeedback.time}</div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">教学内容</div><textarea value={feedbackForm.summary} onChange={(e) => setFeedbackForm((prev) => ({ ...prev, summary: e.target.value }))} className="h-24 w-full rounded-xl border border-[#e9e2d8] px-3 py-2 text-sm outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">课后回顾</div><textarea value={feedbackForm.review} onChange={(e) => setFeedbackForm((prev) => ({ ...prev, review: e.target.value }))} className="h-24 w-full rounded-xl border border-[#e9e2d8] px-3 py-2 text-sm outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">逐学员课堂表现与评语</div><textarea value={feedbackForm.studentComment} onChange={(e) => setFeedbackForm((prev) => ({ ...prev, studentComment: e.target.value }))} className="h-24 w-full rounded-xl border border-[#e9e2d8] px-3 py-2 text-sm outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">下节课建议</div><textarea value={feedbackForm.nextAdvice} onChange={(e) => setFeedbackForm((prev) => ({ ...prev, nextAdvice: e.target.value }))} className="h-24 w-full rounded-xl border border-[#e9e2d8] px-3 py-2 text-sm outline-none" /></div>
              </div>
              <div className="mt-3 rounded-2xl border border-[#f0ebe3] p-3">
                <div className="mb-2 text-sm font-semibold">发布作业（同页完成）</div>
                <div className="grid grid-cols-3 gap-2">
                  <input value={feedbackForm.homeworkTitle} onChange={(e) => setFeedbackForm((prev) => ({ ...prev, homeworkTitle: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" placeholder="作业标题" />
                  <select value={feedbackForm.homeworkType} onChange={(e) => setFeedbackForm((prev) => ({ ...prev, homeworkType: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>文字</option><option>图片</option><option>音频</option><option>视频</option></select>
                  <input value={feedbackForm.homeworkDeadline} onChange={(e) => setFeedbackForm((prev) => ({ ...prev, homeworkDeadline: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" />
                </div>
              </div>
              <button onClick={submitFeedback} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存反馈并同步成长记录、发布作业</button>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teacher-homework') {
    const currentAssetType = homeworkAssetView || getHomeworkAssetType(activeHomework)
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">作业管理 / 作业列表（课程总-学员分）</h2>
        <div className="rounded-2xl border border-[#f0ebe3] p-3">
          <div className="text-xs text-[#7b7064]">按课程搜索</div>
          <input value={homeworkCourseKeyword} onChange={(e) => setHomeworkCourseKeyword(e.target.value)} className="mt-1 w-full rounded-lg border border-[#e9e2d8] px-3 py-2 text-sm outline-none" placeholder="请输入课程名称，例如：标准课 / 体验课" />
        </div>
        <div className="space-y-3">
          {filteredHomeworkCourseGroups.map((group) => (
            <article key={group.id} className="rounded-2xl border border-[#f0ebe3] p-4">
              <div className="mb-2 flex items-center justify-between">
                <div>
                  <div className="text-sm font-semibold">{group.course}</div>
                  <div className="text-xs text-[#8f8376]">作业提交 {group.submissions.length} 份</div>
                </div>
                <div className="text-xs text-[#8f8376]">
                  未提交 {group.submissions.filter((item) => item.status === '未提交').length} 份
                </div>
              </div>
              <PaginatedTable
                columns={[
                  { key: 'student', title: '学员提交' },
                  { key: 'title', title: '作业标题' },
                  { key: 'className', title: '班级' },
                  { key: 'deadline', title: '截止时间' },
                  { key: 'status', title: '状态' },
                  {
                    key: 'action',
                    title: '操作',
                    render: (row) => (
                      <button
                        onClick={() => {
                          setActiveHomeworkId(row.id)
                          setHomeworkDrawerOpen(true)
                          setHomeworkAssetView(getHomeworkAssetType(row))
                        }}
                        className="text-[#bc7844]"
                      >
                        去批改
                      </button>
                    )
                  }
                ]}
                rows={group.submissions}
                pageSize={5}
              />
            </article>
          ))}
        </div>
        {homeworkDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setHomeworkDrawerOpen(false)} />
            <div className="h-full w-[560px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">批改页</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setHomeworkDrawerOpen(false)}>关闭</button>
              </div>
              <div className="rounded-xl bg-[#faf8f4] p-3 text-sm">
                <div className="font-medium">{activeHomework.title}</div>
                <div className="mt-1 text-xs text-[#8f8376]">{activeHomework.student} · {activeHomework.media} · {activeHomework.deadline}</div>
              </div>
              <div className="mt-3 rounded-2xl border border-[#f0ebe3] p-3">
                <div className="mb-2 text-sm font-semibold">学员作业内容</div>
                <div className="mb-2 flex gap-2">
                  <button onClick={() => setHomeworkAssetView('audio')} className={`rounded px-2 py-1 text-xs ${currentAssetType === 'audio' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>打开音频</button>
                  <button onClick={() => setHomeworkAssetView('video')} className={`rounded px-2 py-1 text-xs ${currentAssetType === 'video' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>打开视频</button>
                  <button onClick={() => setHomeworkAssetView('image')} className={`rounded px-2 py-1 text-xs ${currentAssetType === 'image' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>打开图文</button>
                  <button onClick={() => setHomeworkAssetView('text')} className={`rounded px-2 py-1 text-xs ${currentAssetType === 'text' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>打开文字</button>
                </div>
                {currentAssetType === 'audio' && (
                  <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#6f665d]">音频作业已打开（原型预览）：可播放学员练习音频并进行点评。</div>
                )}
                {currentAssetType === 'video' && (
                  <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#6f665d]">视频作业已打开（原型预览）：可查看学员演奏视频并进行点评。</div>
                )}
                {currentAssetType === 'image' && (
                  <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#6f665d]">图文作业已打开（原型预览）：可查看谱例图片与文字说明。</div>
                )}
                {currentAssetType === 'text' && (
                  <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#6f665d]">文字作业已打开（原型预览）：可查看学员练习记录和自评内容。</div>
                )}
              </div>
              <textarea value={homeworkComment} onChange={(e) => setHomeworkComment(e.target.value)} className="mt-3 h-32 w-full rounded-xl border border-[#e9e2d8] px-3 py-2 text-sm outline-none" placeholder="请输入文字/语音/视频点评摘要" />
              <div className="mt-2 grid grid-cols-3 gap-2">
                <button className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">上传语音点评</button>
                <button className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">上传视频点评</button>
                <button className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">上传图文点评</button>
              </div>
              <button onClick={submitHomeworkComment} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">提交点评</button>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teacher-students') {
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">学员管理 / 已分配学员</h2>
        <PaginatedTable
          columns={[
            { key: 'name', title: '学员' },
            { key: 'remaining', title: '剩余课时' },
            { key: 'level', title: '等级标注' },
            { key: 'stageReport', title: '阶段测评' },
            { key: 'transferNode', title: '团购转化节点' },
            { key: 'action', title: '操作', render: (row) => <button onClick={() => { setActiveTeacherStudentId(row.id); setTeacherStudentDrawerOpen(true) }} className="text-[#bc7844]">学员详情</button> }
          ]}
          rows={assignedStudents}
          pageSize={6}
        />
        {teacherStudentDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setTeacherStudentDrawerOpen(false)} />
            <div className="h-full w-[520px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">学员详情</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setTeacherStudentDrawerOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div className="rounded-xl bg-[#faf8f4] p-3">学员：{activeTeacherStudent.name}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">剩余课时：{activeTeacherStudent.remaining}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">等级：{activeTeacherStudent.level}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">阶段测评：{activeTeacherStudent.stageReport}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">作业详情：{(activeTeacherStudent.homeworkRecords || ['暂无']).join('；')}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">团购转化节点：{activeTeacherStudent.transferNode}</div>
              </div>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teacher-availability-time') {
    return (
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold">可授课时间与承载量 / 可用时间配置</h2>
          <button onClick={() => setAvailabilityModalOpen(true)} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">新增时间配置</button>
        </div>
        <PaginatedTable
          columns={[
            { key: 'mode', title: '类型' },
            { key: 'day', title: '日期/星期' },
            { key: 'time', title: '时间段' },
            { key: 'campus', title: '校区' },
            { key: 'status', title: '状态' }
          ]}
          rows={teacherAvailabilityRows}
          pageSize={6}
        />
        {availabilityModalOpen && (
          <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setAvailabilityModalOpen(false)} />
            <div className="relative w-full max-w-[620px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">新增可用时间配置</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setAvailabilityModalOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">配置类型</div><select value={availabilityForm.mode} onChange={(e) => setAvailabilityForm((prev) => ({ ...prev, mode: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>周重复</option><option>临时调整</option></select></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">日期/星期</div><input value={availabilityForm.day} onChange={(e) => setAvailabilityForm((prev) => ({ ...prev, day: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">时间段</div><input value={availabilityForm.time} onChange={(e) => setAvailabilityForm((prev) => ({ ...prev, time: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">校区</div><select value={availabilityForm.campus} onChange={(e) => setAvailabilityForm((prev) => ({ ...prev, campus: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>北环国基路校区</option><option>西大剧院校区</option></select></div>
              </div>
              <button onClick={saveAvailability} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存配置</button>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teacher-availability-pref') {
    const toggleType = (type) => {
      setAvailabilityPref((prev) => ({
        ...prev,
        teachingTypes: prev.teachingTypes.includes(type)
          ? prev.teachingTypes.filter((item) => item !== type)
          : [...prev.teachingTypes, type]
      }))
    }
    const toggleSubject = (subject) => {
      setAvailabilityPref((prev) => ({
        ...prev,
        subjects: prev.subjects.includes(subject)
          ? prev.subjects.filter((item) => item !== subject)
          : [...prev.subjects, subject]
      }))
    }
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">可授课时间与承载量 / 授课偏好</h2>
        <div className="grid grid-cols-2 gap-4">
          <article className="rounded-2xl border border-[#f0ebe3] p-4">
            <div className="mb-2 text-sm font-semibold">授课类型偏好</div>
            <div className="flex flex-wrap gap-2">
              {['团课', '一对一'].map((type) => (
                <button key={type} onClick={() => toggleType(type)} className={`rounded-full px-3 py-1 text-xs ${availabilityPref.teachingTypes.includes(type) ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#8f8376]'}`}>{type}</button>
              ))}
            </div>
            <div className="mt-3 text-sm text-[#6f665d]">当前：{availabilityPref.teachingTypes.join(' / ')}</div>
          </article>
          <article className="rounded-2xl border border-[#f0ebe3] p-4">
            <div className="mb-2 text-sm font-semibold">擅长科目</div>
            <div className="flex flex-wrap gap-2">
              {['木吉他', '电吉他', '贝斯'].map((subject) => (
                <button key={subject} onClick={() => toggleSubject(subject)} className={`rounded-full px-3 py-1 text-xs ${availabilityPref.subjects.includes(subject) ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#8f8376]'}`}>{subject}</button>
              ))}
            </div>
            <div className="mt-3 text-sm text-[#6f665d]">当前：{availabilityPref.subjects.join(' / ')}</div>
          </article>
        </div>
        <article className="rounded-2xl border border-[#f0ebe3] p-4">
          <div className="mb-2 text-sm font-semibold">承载量配置（同一时间段）</div>
          <div className="flex items-center gap-3">
            <span className="text-sm text-[#6f665d]">最大承载量</span>
            <input value={availabilityPref.capacity} onChange={(e) => setAvailabilityPref((prev) => ({ ...prev, capacity: e.target.value }))} className="w-20 rounded-lg border border-[#e9e2d8] px-2 py-1 text-sm outline-none" />
            <span className="text-sm text-[#6f665d]">人</span>
            <button onClick={saveAvailabilityPref} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存偏好</button>
          </div>
        </article>
      </section>
    )
  }

  return (
    <section className="space-y-4">
      <h2 className="text-sm font-semibold">教师端页面预留</h2>
      <div className="rounded-xl bg-[#faf8f4] p-3 text-sm">当前页面正在补充中</div>
    </section>
  )
}

function StudentView({ page }) {
  const [activeBookingId, setActiveBookingId] = useState(studentBookings[0].id)
  const [activeHomeworkId, setActiveHomeworkId] = useState(homeworkPool[0].id)
  const [learningTab, setLearningTab] = useState('约课')
  const [homeworkTab, setHomeworkTab] = useState('待完成')
  const [studentTip, setStudentTip] = useState('')
  const [scheduleDate, setScheduleDate] = useState('2026-03-24')
  const [scheduleView, setScheduleView] = useState('周')
  const [bookingDate, setBookingDate] = useState('2026-03-24')
  const [profileDetailTab, setProfileDetailTab] = useState('home')
  const activeBooking = studentBookings.find((item) => item.id === activeBookingId)
  const activeHomework = homeworkPool.find((item) => item.id === activeHomeworkId)
  const homeworkRows = homeworkPool.map((item, idx) => ({
    ...item,
    statusTag: idx === 0 ? '待完成' : idx === 1 ? '已提交' : '已批改',
    review: idx === 2 ? '老师点评：右手节奏稳定，注意换和弦连贯性。' : '暂未点评'
  }))
  const filteredHomeworkRows = homeworkRows.filter((item) => item.statusTag === homeworkTab)
  const calendarRows = [
    { date: '2026-03-24', short: '03-24', classes: ['标准课'], notes: '今日 1 节' },
    { date: '2026-03-25', short: '03-25', classes: [], notes: '无课' },
    { date: '2026-03-26', short: '03-26', classes: ['团课'], notes: '1 节' },
    { date: '2026-03-27', short: '03-27', classes: [], notes: '无课' },
    { date: '2026-03-28', short: '03-28', classes: ['体验课'], notes: '1 节' },
    { date: '2026-03-29', short: '03-29', classes: [], notes: '无课' },
    { date: '2026-03-30', short: '03-30', classes: ['正式课'], notes: '1 节' }
  ]
  const monthCalendarRows = Array.from({ length: 31 }, (_, idx) => {
    const date = `2026-03-${String(idx + 1).padStart(2, '0')}`
    const hasClass = ['2026-03-24', '2026-03-26', '2026-03-28', '2026-03-30'].includes(date)
    return {
      date,
      short: `03-${String(idx + 1).padStart(2, '0')}`,
      notes: hasClass ? '1 节' : '无课'
    }
  })
  const scheduleCalendarRows = scheduleView === '周' ? calendarRows : monthCalendarRows
  const bookingCalendarRows = [
    { date: '2026-03-24', short: '03-24', summary: '可约 2 节', ids: ['B-1', 'B-2'] },
    { date: '2026-03-25', short: '03-25', summary: '可约 1 节', ids: ['B-3'] },
    { date: '2026-03-26', short: '03-26', summary: '可约 2 节', ids: ['B-4', 'B-5'] },
    { date: '2026-03-27', short: '03-27', summary: '暂满', ids: [] },
    { date: '2026-03-28', short: '03-28', summary: '可约 1 节', ids: ['B-6'] },
    { date: '2026-03-29', short: '03-29', summary: '可约 1 节', ids: ['B-7'] },
    { date: '2026-03-30', short: '03-30', summary: '可约 2 节', ids: ['B-8', 'B-9'] }
  ]
  const bookingRowsByDate = bookingCalendarRows.find((item) => item.date === bookingDate)?.ids || []
  const bookingList = bookingRowsByDate.map((id, idx) => ({
    id,
    type: idx % 3 === 0 ? '正式课（可选老师）' : idx % 3 === 1 ? '团课（系统推荐）' : '体验课',
    time: idx % 3 === 0 ? '14:00-15:00' : idx % 3 === 1 ? '16:00-17:00' : '19:00-20:00',
    teacher: idx % 3 === 0 ? '刘老师' : '系统推荐',
    blockReason: ''
  }))
  const scheduleClassRows = [
    { id: 'SC-1', date: '2026-03-24', title: '标准课：和弦转换', time: '18:30-19:30', teacher: '赵老师', room: 'A101' },
    { id: 'SC-2', date: '2026-03-26', title: '团课：节奏练习', time: '19:00-20:00', teacher: '陈老师', room: 'B203' },
    { id: 'SC-3', date: '2026-03-28', title: '体验课：入门试听', time: '15:00-16:00', teacher: '刘老师', room: 'A203' }
  ]
  const scheduleList = scheduleClassRows.filter((item) => item.date === scheduleDate)
  const packageRows = [
    { id: 'PK-1', name: '正式课48课时包', remain: '21', status: '已购买', validUntil: '2026-12-31' },
    { id: 'PK-2', name: '进阶课24课时包', remain: '0', status: '可购买', validUntil: '—' }
  ]
  const remindRows = [
    { id: 'RM-1', scene: '上课提醒', rule: '课前1天 + 6小时', channel: '小程序通知', status: '开启' },
    { id: 'RM-2', scene: '核销未预约提醒', rule: '核销后第1天', channel: '小程序通知', status: '开启' },
    { id: 'RM-3', scene: '作业未提交提醒', rule: '截止前12小时', channel: '小程序通知', status: '开启' },
    { id: 'RM-4', scene: '剩余课时提醒', rule: '低于10课时', channel: '小程序通知', status: '开启' }
  ]
  const activeBookingCard = bookingList.find((item) => item.id === activeBookingId) || bookingList[0] || activeBooking

  const mobileCard = 'mx-auto w-full max-w-[390px] rounded-[24px] border border-[#f0ebe3] bg-[#fffdf9] p-4 shadow-sm'

  if (page === 'schedule') {
    return (
      <section className="space-y-3">
        <article className={mobileCard}>
          <div className="flex items-center justify-between">
            <div className="text-base font-semibold">课表</div>
            <div className="flex gap-1">
              <button onClick={() => setScheduleView('周')} className={`rounded-full px-2.5 py-1 text-xs ${scheduleView === '周' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#8f8376]'}`}>周</button>
              <button onClick={() => setScheduleView('月')} className={`rounded-full px-2.5 py-1 text-xs ${scheduleView === '月' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#8f8376]'}`}>月</button>
            </div>
          </div>
          <div className="mt-3 rounded-2xl bg-[#fff3e8] p-3 text-sm">
            <div className="font-medium">{studentRemaining.todayClass.title}</div>
            <div className="mt-1 text-xs text-[#8f8376]">{studentRemaining.todayClass.time} · {studentRemaining.todayClass.teacher}</div>
            <div className="mt-1 text-xs text-[#8f8376]">地点：北环国基路校区 A101 ｜ 签到规则：课前10分钟可签到</div>
          </div>
          <div className="mt-3 text-xs text-[#8f8376]">手机端{scheduleView}课表（日历可左右滑动）</div>
          <div className="mt-2 overflow-x-auto pb-1">
            <div className={`grid gap-1 ${scheduleView === '周' ? 'min-w-[560px] grid-cols-7' : 'min-w-[860px] grid-cols-7'}`}>
              {scheduleCalendarRows.map((day) => (
                <button key={day.date} onClick={() => setScheduleDate(day.date)} className={`rounded-xl border px-1 py-2 text-center text-[11px] ${scheduleDate === day.date ? 'border-[#ff9b54] bg-[#fff4ea]' : 'border-[#f0ebe3] bg-[#faf8f4]'}`}>
                  <div>{day.short}</div>
                  <div className="mt-1 text-[#8f8376]">{day.notes}</div>
                </button>
              ))}
            </div>
          </div>
          <div className="mt-3 rounded-xl bg-[#faf8f4] p-3">
            <div className="text-xs text-[#8f8376]">当日课程（{scheduleDate}）</div>
            <div className="mt-2 space-y-2">
              {scheduleList.length === 0 && <div className="text-xs text-[#8f8376]">当日暂无课程</div>}
              {scheduleList.map((item) => (
                <div key={item.id} className="rounded-xl bg-white p-2 text-xs">
                  <div className="font-medium text-[#2e2a25]">{item.title}</div>
                  <div className="mt-1 text-[#8f8376]">{item.time} · {item.teacher} · {item.room}</div>
                </div>
              ))}
            </div>
          </div>
        </article>
      </section>
    )
  }

  if (page === 'learning') {
    return (
      <section className="space-y-3">
        <article className={mobileCard}>
          <div className="text-base font-semibold">学习</div>
          <div className="mt-3 flex gap-2">
            {['约课', '作业', '成长'].map((item) => (
              <button key={item} onClick={() => setLearningTab(item)} className={`rounded-full px-3 py-1.5 text-xs ${learningTab === item ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#8f8376]'}`}>{item}</button>
            ))}
          </div>
          {learningTab === '约课' && (
            <div className="mt-3 space-y-2">
              <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#8f8376]">日历上展示简要可约信息，点击日期后显示可预约课程。</div>
              <div className="overflow-x-auto pb-1">
                <div className="grid min-w-[560px] grid-cols-7 gap-1">
                  {bookingCalendarRows.map((day) => (
                    <button key={day.date} onClick={() => setBookingDate(day.date)} className={`rounded-xl border px-1 py-2 text-center text-[11px] ${bookingDate === day.date ? 'border-[#ff9b54] bg-[#fff4ea]' : 'border-[#f0ebe3] bg-[#faf8f4]'}`}>
                      <div>{day.short}</div>
                      <div className="mt-1 text-[#8f8376]">{day.summary}</div>
                    </button>
                  ))}
                </div>
              </div>
              <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#8f8376]">已选日期：{bookingDate}</div>
              {bookingList.map((item) => (
                <button
                  key={item.id}
                  onClick={() => setActiveBookingId(item.id)}
                  className={`w-full rounded-2xl border p-3 text-left ${
                    activeBookingId === item.id ? 'border-[#ff9b54] bg-[#fff4ea]' : 'border-[#f0ebe3] bg-[#faf8f4]'
                  }`}
                >
                  <div className="text-sm font-medium">{item.type}</div>
                  <div className="text-xs text-[#8f8376]">{item.time} · {item.teacher}</div>
                </button>
              ))}
              {bookingList.length === 0 && (
                <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#8f8376]">当日暂无可预约课程</div>
              )}
              <button onClick={() => setStudentTip(`已预约：${activeBookingCard?.type || '当日课程'} / ${bookingDate}`)} className="w-full rounded-xl bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">确认预约</button>
            </div>
          )}
          {learningTab === '作业' && (
            <div className="mt-3 space-y-2">
              <div className="flex gap-2 overflow-x-auto pb-1">
                {['待完成', '已提交', '已批改'].map((item) => (
                  <button key={item} onClick={() => setHomeworkTab(item)} className={`rounded-full px-3 py-1 text-xs whitespace-nowrap ${homeworkTab === item ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#8f8376]'}`}>{item}</button>
                ))}
              </div>
              {filteredHomeworkRows.map((item) => (
                <button
                  key={item.id}
                  onClick={() => setActiveHomeworkId(item.id)}
                  className={`w-full rounded-xl border p-3 text-left ${
                    activeHomeworkId === item.id ? 'border-[#ff9b54] bg-[#fff4ea]' : 'border-[#f0ebe3] bg-[#faf8f4]'
                  }`}
                >
                  <div className="text-sm font-medium">{item.title}</div>
                  <div className="text-xs text-[#8f8376]">{item.deadline}</div>
                  <div className="mt-1 text-xs text-[#6f665d]">{item.review}</div>
                </button>
              ))}
              <textarea className="h-20 w-full rounded-xl border border-[#e9e2d8] px-3 py-2 text-sm outline-none" placeholder="提交作业说明" />
              <div className="grid grid-cols-3 gap-2 text-xs">
                <button className="rounded-lg border border-[#e9e2d8] px-2 py-2">上传图片</button>
                <button className="rounded-lg border border-[#e9e2d8] px-2 py-2">上传音频</button>
                <button className="rounded-lg border border-[#e9e2d8] px-2 py-2">上传视频</button>
              </div>
              <button onClick={() => setStudentTip(`已提交作业：${activeHomework.title}`)} className="w-full rounded-xl bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">提交作业</button>
            </div>
          )}
          {learningTab === '成长' && (
            <div className="mt-3 space-y-2">
              <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#8f8376]">成长记录：课后反馈时间线 + 阶段测评（一期先展示反馈）</div>
              {studentTimeline.map((item) => (
                <div key={item.id} className="rounded-xl bg-[#faf8f4] p-3">
                  <div className="text-xs text-[#8f8376]">{item.date}</div>
                  <div className="mt-1 text-sm font-medium">{item.title}</div>
                  <div className="mt-1 text-xs text-[#8f8376]">{item.summary}</div>
                </div>
              ))}
            </div>
          )}
          {studentTip && (
            <div className="mt-3 rounded-xl bg-[#fff4ea] p-3 text-xs text-[#8f5d34]">{studentTip}</div>
          )}
        </article>
      </section>
    )
  }

  return (
    <section className="space-y-3">
      <article className={mobileCard}>
        <div className="text-base font-semibold">我的</div>
        <div className="mt-3 rounded-xl bg-[#fff4ea] p-3 text-sm">
          剩余课时：<span className="text-lg font-semibold">{studentRemaining.remainingHours}</span>
        </div>
        {profileDetailTab === 'home' && (
          <div className="mt-3 space-y-2">
            <button onClick={() => setProfileDetailTab('packages')} className="w-full rounded-xl border border-[#f0ebe3] bg-[#faf8f4] p-3 text-left text-sm">我的课包</button>
            <button onClick={() => setProfileDetailTab('alerts')} className="w-full rounded-xl border border-[#f0ebe3] bg-[#faf8f4] p-3 text-left text-sm">我的提醒</button>
            <button onClick={() => setProfileDetailTab('info')} className="w-full rounded-xl border border-[#f0ebe3] bg-[#faf8f4] p-3 text-left text-sm">个人信息</button>
          </div>
        )}
        {profileDetailTab === 'packages' && (
          <div className="mt-3">
            <button onClick={() => setProfileDetailTab('home')} className="mb-2 rounded-lg border border-[#e8dfd3] px-2 py-1 text-xs">返回</button>
            <div className="overflow-x-auto">
              <table className="min-w-[520px] text-xs">
                <thead>
                  <tr className="bg-[#faf8f4] text-[#8f8376]">
                    <th className="px-2 py-2 text-left">课包</th>
                    <th className="px-2 py-2 text-left">剩余课时</th>
                    <th className="px-2 py-2 text-left">状态</th>
                    <th className="px-2 py-2 text-left">有效期</th>
                  </tr>
                </thead>
                <tbody>
                  {packageRows.map((item) => (
                    <tr key={item.id} className="border-b border-[#f0ebe3]">
                      <td className="px-2 py-2">{item.name}</td>
                      <td className="px-2 py-2">{item.remain}</td>
                      <td className="px-2 py-2">{item.status}</td>
                      <td className="px-2 py-2">{item.validUntil}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
        {profileDetailTab === 'alerts' && (
          <div className="mt-3">
            <button onClick={() => setProfileDetailTab('home')} className="mb-2 rounded-lg border border-[#e8dfd3] px-2 py-1 text-xs">返回</button>
            <div className="overflow-x-auto">
              <table className="min-w-[520px] text-xs">
                <thead>
                  <tr className="bg-[#faf8f4] text-[#8f8376]">
                    <th className="px-2 py-2 text-left">提醒项</th>
                    <th className="px-2 py-2 text-left">规则</th>
                    <th className="px-2 py-2 text-left">渠道</th>
                    <th className="px-2 py-2 text-left">状态</th>
                  </tr>
                </thead>
                <tbody>
                  {remindRows.map((item) => (
                    <tr key={item.id} className="border-b border-[#f0ebe3]">
                      <td className="px-2 py-2">{item.scene}</td>
                      <td className="px-2 py-2">{item.rule}</td>
                      <td className="px-2 py-2">{item.channel}</td>
                      <td className="px-2 py-2">{item.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
        {profileDetailTab === 'info' && (
          <div className="mt-3">
            <button onClick={() => setProfileDetailTab('home')} className="mb-2 rounded-lg border border-[#e8dfd3] px-2 py-1 text-xs">返回</button>
            <div className="space-y-2 text-sm">
              <div className="rounded-xl bg-[#faf8f4] p-3">姓名：李予安</div>
              <div className="rounded-xl bg-[#faf8f4] p-3">手机号：138****1024</div>
              <div className="rounded-xl bg-[#faf8f4] p-3">我的申请：调课申请 #A203 已通过</div>
            </div>
          </div>
        )}
      </article>
    </section>
  )
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/admin/workbench-overview" replace />} />
        <Route path="/:role/:page" element={<Shell />} />
        <Route path="*" element={<Navigate to="/admin/workbench-overview" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
