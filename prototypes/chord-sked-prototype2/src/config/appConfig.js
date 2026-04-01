import {
  BookOpen,
  CalendarDays,
  CheckCircle2,
  ClipboardList,
  GraduationCap,
  Home,
  MessageSquare,
  Package,
  Repeat2,
  School,
  Shield,
  ShoppingBag,
  UserSquare2,
  Users
} from 'lucide-react'

export const roleMeta = {
  admin: { title: '教务端', subtitle: '排课 / 转化 / 审批 / 权限', icon: School },
  teacher: { title: '教师端', subtitle: '签到 / 反馈 / 作业 / 课表', icon: GraduationCap },
  student: { title: '学员端', subtitle: '首页 / 约课 / 学习 / 我的', icon: UserSquare2 }
}

export const navMap = {
  student: [
    { key: 'home', label: '首页', icon: Home },
    { key: 'booking', label: '约课', icon: CalendarDays },
    { key: 'learning', label: '学习', icon: BookOpen },
    { key: 'profile', label: '我的', icon: UserSquare2 }
  ]
}

export const adminSitemap = [
  { key: 'workbench', label: '工作台', icon: Home, directPage: 'workbench-overview', children: [] },
  { key: 'messages', label: '消息中心', icon: MessageSquare, children: [{ key: 'messages-list', label: '消息列表' }] },
  { key: 'conversion', label: '团购转化', icon: ShoppingBag, children: [{ key: 'conversion-import', label: '订单导入/列表' }, { key: 'conversion-clue', label: '核销登记/线索详情' }, { key: 'conversion-qr', label: '预约二维码页' }] },
  { key: 'teaching', label: '教学管理', icon: CalendarDays, children: [{ key: 'schedule-calendar', label: '排课日历' }, { key: 'homework-list', label: '作业列表' }, { key: 'schedule-template', label: '课程模板管理' }, { key: 'schedule-room', label: '教室资源管理' }] },
  { key: 'adjustments', label: '调课/取消/补课', icon: Repeat2, children: [{ key: 'adjustments-list', label: '申请列表' }, { key: 'adjustments-log', label: '审核日志' }] },
  { key: 'students', label: '学员管理', icon: Users, children: [{ key: 'students-list', label: '学员列表' }] },
  { key: 'teachers', label: '教师管理', icon: GraduationCap, children: [{ key: 'teachers-list', label: '教师列表/档案' }] },
  { key: 'packages', label: '课包管理', icon: Package, children: [{ key: 'packages-list', label: '课包列表' }] },
  { key: 'notice', label: '公告/通知', icon: MessageSquare, children: [{ key: 'notice-rules', label: '消息提醒规则' }, { key: 'notice-touch', label: '触达记录' }] },
  { key: 'org', label: '组织与权限', icon: Shield, children: [{ key: 'org-campus', label: '校区管理' }, { key: 'org-tree', label: '组织树管理' }, { key: 'org-account', label: '账号管理' }, { key: 'org-rbac', label: '角色与数据权限' }, { key: 'org-permtree', label: '权限树映射' }] },
  { key: 'analytics', label: '数据看板（二期）', icon: School, children: [{ key: 'analytics-overview', label: '校区经营/质量/续费（预留）' }] }
]

export const adminPageKeys = adminSitemap.flatMap((group) => (
  group.directPage ? [group.directPage] : group.children.map((child) => child.key)
))

export const teacherSitemap = [
  {
    key: 't-workbench',
    label: '工作台',
    icon: Home,
    directPage: 'teacher-workbench-overview',
    children: []
  },
  { key: 't-messages', label: '消息中心', icon: MessageSquare, children: [{ key: 'teacher-messages-list', label: '消息列表' }] },
  { key: 't-timetable', label: '我的课表', icon: CalendarDays, children: [{ key: 'teacher-timetable', label: '日/周视图' }] },
  { key: 't-attendance', label: '上课签到', icon: CheckCircle2, children: [{ key: 'teacher-attendance', label: '待签到列表' }, { key: 'teacher-attendance-signed', label: '已签到列表' }] },
  { key: 't-feedback', label: '课后反馈', icon: ClipboardList, children: [{ key: 'teacher-feedback', label: '待反馈队列' }] },
  { key: 't-homework', label: '作业管理', icon: BookOpen, children: [{ key: 'teacher-homework', label: '作业列表' }] },
  { key: 't-students', label: '学员管理', icon: Users, children: [{ key: 'teacher-students', label: '已分配学员' }] },
  { key: 't-availability', label: '可授课时间与承载量', icon: School, children: [{ key: 'teacher-availability-time', label: '可用时间配置' }, { key: 'teacher-availability-pref', label: '授课偏好' }] }
]

export const teacherPageKeys = teacherSitemap.flatMap((group) => (
  group.directPage ? [group.directPage] : group.children.map((child) => child.key)
))

export const getDefaultPage = (role) => {
  if (role === 'admin') return 'workbench-overview'
  if (role === 'teacher') return 'teacher-workbench-overview'
  return navMap[role][0].key
}
