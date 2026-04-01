import { useMemo, useState } from 'react'
import { NavLink, useLocation, useNavigate, useParams } from 'react-router-dom'
import { Bell, ChevronDown, ChevronRight } from 'lucide-react'
import { notifications } from '../lib/mockData'
import { roleMeta, navMap, adminSitemap, adminPageKeys, teacherSitemap, teacherPageKeys, getDefaultPage } from '../config/appConfig'
import AdminView from './AdminView'
import TeacherView from './TeacherView'
import StudentView from './StudentView'

export default function Shell() {
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
  const pageFilter = useMemo(() => new URLSearchParams(location.search).get('filter') || '', [location.search])
  const pageTab = useMemo(() => new URLSearchParams(location.search).get('tab') || '', [location.search])
  const pageRange = useMemo(() => new URLSearchParams(location.search).get('range') || '', [location.search])
  const pageScope = useMemo(() => new URLSearchParams(location.search).get('scope') || '', [location.search])

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
                  <option className="text-black">所有</option>
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
                pageFilter={pageFilter}
                pageTab={pageTab}
                pageRange={pageRange}
                pageScope={pageScope}
                onNavigate={navigate}
                campus={campus}
              />
            )}
            {currentRole === 'teacher' && <TeacherView page={currentPage} onNavigate={navigate} />}
            {currentRole === 'student' && <StudentView page={currentPage} />}
          </main>
        </div>
      </div>
    </div>
  )
}
