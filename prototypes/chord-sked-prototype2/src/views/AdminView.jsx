import { useState } from 'react'
import { BookOpen, CalendarDays, Search, ShoppingBag, Users } from 'lucide-react'
import {
  adminKpis,
  conversionOrders,
  conversionLeads,
  teachers,
  students,
  scheduleCards,
  adjustmentRequests,
  coursePackages,
  homeworkPool,
  notifications
} from '../lib/mockData'
import { PaginatedTable, QrPreview } from '../components/shared/uiBlocks'

export default function AdminView({ page, messageFilter, onNavigate, campus }) {
  const [leadRows, setLeadRows] = useState(conversionLeads)
  const [orderRows, setOrderRows] = useState(conversionOrders)
  const [adjustmentRows, setAdjustmentRows] = useState(adjustmentRequests)
  const [studentRows, setStudentRows] = useState(students)
  const [teacherRows, setTeacherRows] = useState(teachers)
  const [packageRows, setPackageRows] = useState(coursePackages)
  const [homeworkRows, setHomeworkRows] = useState([
    ...homeworkPool.map((item, idx) => ({
      id: `AH-${item.id}`,
      campus: idx % 2 === 0 ? '北环国基路校区' : '西大剧院校区',
      student: item.student,
      course: idx % 2 === 0 ? '标准课：节奏训练营' : '团购体验课：基础和弦',
      title: item.title,
      deadline: item.deadline?.replace('截止 ', '') || '2026-03-26 21:00',
      status: item.status,
      submittedAt: item.status === '已提交' ? '2026-03-24 20:12' : item.status === '待批改' ? '2026-03-24 18:40' : '—',
      teacher: idx % 2 === 0 ? '陈老师' : '刘老师'
    })),
    { id: 'AH-004', campus: '北环国基路校区', student: '王星河', course: '一对一：周末冲刺', title: '速度练习打卡', deadline: '2026-03-26 22:00', status: '未提交', submittedAt: '—', teacher: '赵老师' },
    { id: 'AH-005', campus: '西大剧院校区', student: '陈可心', course: '标准课：扫弦进阶', title: '扫弦节奏视频', deadline: '2026-03-25 21:00', status: '待提交', submittedAt: '—', teacher: '陈老师' }
  ])
  const [homeworkStatusFilter, setHomeworkStatusFilter] = useState('全部')
  const [homeworkDrawerOpen, setHomeworkDrawerOpen] = useState(false)
  const [activeHomeworkId, setActiveHomeworkId] = useState('')
  const [activeLeadId, setActiveLeadId] = useState(conversionLeads[0].id)
  const [activeRequestId, setActiveRequestId] = useState(adjustmentRequests[0].id)
  const [activeStudentId, setActiveStudentId] = useState(students[0].id)
  const [activeTeacherId, setActiveTeacherId] = useState(teachers[0].id)
  const [searchLeadKeyword, setSearchLeadKeyword] = useState('')
  const [flowTip, setFlowTip] = useState('')
  const [selectedOrderIds, setSelectedOrderIds] = useState([])
  const [orderInvalidModalOpen, setOrderInvalidModalOpen] = useState(false)
  const [orderInvalidTargetId, setOrderInvalidTargetId] = useState('')
  const [orderInvalidReason, setOrderInvalidReason] = useState('')
  const [messageDrawerOpen, setMessageDrawerOpen] = useState(false)
  const [activeMessageId, setActiveMessageId] = useState(notifications[0].id)
  const [leadDrawerOpen, setLeadDrawerOpen] = useState(false)
  const [showBookingQr, setShowBookingQr] = useState(false)
  const [showArchiveForm, setShowArchiveForm] = useState(false)
  const [studentDrawerOpen, setStudentDrawerOpen] = useState(false)
  const [studentArchiveDrawerOpen, setStudentArchiveDrawerOpen] = useState(false)
  const [studentArchiveDrawerType, setStudentArchiveDrawerType] = useState('hours')
  const [adjustmentDrawerOpen, setAdjustmentDrawerOpen] = useState(false)
  const [adjustmentRebindModalOpen, setAdjustmentRebindModalOpen] = useState(false)
  const [newStudentDrawerOpen, setNewStudentDrawerOpen] = useState(false)
  const [studentPackageModalOpen, setStudentPackageModalOpen] = useState(false)
  const [teacherDrawerOpen, setTeacherDrawerOpen] = useState(false)
  const [newTeacherDrawerOpen, setNewTeacherDrawerOpen] = useState(false)
  const [packageDrawerOpen, setPackageDrawerOpen] = useState(false)
  const [newPackageDrawerOpen, setNewPackageDrawerOpen] = useState(false)
  const [studentHourConsumeRows] = useState([
    { id: 'HC-001', studentId: 'S-001', date: '2026-03-10 15:10', action: '上课扣减', hours: 1, course: '标准课：扫弦练习', operator: '系统' },
    { id: 'HC-002', studentId: 'S-001', date: '2026-03-17 15:12', action: '上课扣减', hours: 1, course: '标准课：节拍器训练', operator: '系统' },
    { id: 'HC-003', studentId: 'S-001', date: '2026-03-20 20:30', action: '补扣', hours: 1, course: '补扣：补签消耗', operator: '教务' },
    { id: 'HC-004', studentId: 'S-002', date: '2026-03-08 19:05', action: '上课扣减', hours: 1, course: '团购体验课：基础和弦', operator: '系统' },
    { id: 'HC-005', studentId: 'S-002', date: '2026-03-15 19:06', action: '退回', hours: 1, course: '撤销签到退回', operator: '教师' },
    { id: 'HC-006', studentId: 'S-003', date: '2026-03-25 18:55', action: '上课扣减', hours: 1, course: '一对一：周末冲刺', operator: '系统' }
  ])
  const [studentPackagePurchaseRows] = useState([
    { id: 'PB-001', studentId: 'S-001', date: '2026-01-15 12:10', packageName: '标准课24节包', hours: 24, amount: 3280, channel: '门店', status: '已支付' },
    { id: 'PB-002', studentId: 'S-002', date: '2026-03-22 14:30', packageName: '体验课2节包', hours: 2, amount: 49, channel: '抖音团购', status: '已支付' },
    { id: 'PB-003', studentId: 'S-003', date: '2026-02-03 20:40', packageName: '冲刺课12节包', hours: 12, amount: 4980, channel: '门店', status: '已支付' }
  ])
  const [studentClassRecordRows] = useState([
    { id: 'CR-001', studentId: 'S-001', date: '2026-03-10', course: '标准课：扫弦练习', teacher: '陈老师', room: '北环校区 A101', sign: '已签到', deductHours: 1 },
    { id: 'CR-002', studentId: 'S-001', date: '2026-03-17', course: '标准课：节拍器训练', teacher: '陈老师', room: '北环校区 A101', sign: '已签到', deductHours: 1 },
    { id: 'CR-003', studentId: 'S-002', date: '2026-03-08', course: '团购体验课：基础和弦', teacher: '刘老师', room: '西大剧院校区 B203', sign: '已签到', deductHours: 1 },
    { id: 'CR-004', studentId: 'S-003', date: '2026-03-25', course: '一对一：周末冲刺', teacher: '赵老师', room: '北环校区 B103', sign: '未签到', deductHours: 0 }
  ])
  const [studentFeedbackRows] = useState([
    { id: 'FB-001', studentId: 'S-001', date: '2026-03-10 16:20', course: '标准课：扫弦练习', teacher: '陈老师', summary: '扫弦节奏稳定，需提升换和弦速度', rating: 'B' },
    { id: 'FB-002', studentId: 'S-001', date: '2026-03-17 16:10', course: '标准课：节拍器训练', teacher: '陈老师', summary: '拍点更准，建议增加慢速练习', rating: 'A-' },
    { id: 'FB-003', studentId: 'S-002', date: '2026-03-08 20:10', course: '团购体验课：基础和弦', teacher: '刘老师', summary: '基础和弦能按稳，建议购买正课包继续学习', rating: 'B+' }
  ])
  const [studentHomeworkRecordRows] = useState([
    { id: 'HW-001', studentId: 'S-001', date: '2026-03-18', course: '标准课：扫弦练习', title: '主歌弹唱视频', status: '已提交', submittedAt: '2026-03-18 21:10', teacher: '陈老师' },
    { id: 'HW-002', studentId: 'S-001', date: '2026-03-24', course: '标准课：节拍器训练', title: '节拍器练习音频', status: '待提交', submittedAt: '—', teacher: '陈老师' },
    { id: 'HW-003', studentId: 'S-002', date: '2026-03-08', course: '团购体验课：基础和弦', title: '和弦转换练习视频', status: '未提交', submittedAt: '—', teacher: '刘老师' },
    { id: 'HW-004', studentId: 'S-003', date: '2026-03-25', course: '一对一：周末冲刺', title: '速度练习打卡', status: '已提交', submittedAt: '2026-03-25 22:05', teacher: '赵老师' }
  ])
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
    bindMode: 'free',
    templateIds: [],
    displayPrice: '0',
    salePrice: '0',
    stock: '0',
    validDays: '365',
    validUntil: '2027-03-31'
  })
  const [studentPackageForm, setStudentPackageForm] = useState({
    packageId: coursePackages[0]?.id || 'P-1',
    grantHours: '24',
    attrs: ''
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
    ,
    { id: 'SCH-004', date: '2026-03-26', timeSlot: '10:00-11:00', templateName: '标准课模板', templateType: '标准课', teacherId: 'T-02', teacher: '陈老师', roomId: 'ROOM-001', room: 'A101', campus: '北环国基路校区', ageGroup: '成人', reservedCount: 1, capacityMax: 5, studentName: '', bookedStudents: ['李予安'] },
    { id: 'SCH-005', date: '2026-03-26', timeSlot: '19:00-20:00', templateName: '团购体验课模板', templateType: '团购体验课', teacherId: 'T-01', teacher: '刘老师', roomId: 'ROOM-003', room: 'B203', campus: '西大剧院校区', ageGroup: '未成年', reservedCount: 0, capacityMax: 5, studentName: '', bookedStudents: [] },
    { id: 'SCH-006', date: '2026-03-27', timeSlot: '14:00-15:00', templateName: '标准课模板', templateType: '标准课', teacherId: 'T-02', teacher: '陈老师', roomId: 'ROOM-001', room: 'A101', campus: '北环国基路校区', ageGroup: '成人', reservedCount: 4, capacityMax: 5, studentName: '', bookedStudents: ['张小满', '王星河', '陈可心', '林知夏'] }
  ])
  const [scheduleCourseRows, setScheduleCourseRows] = useState([
    { id: 'SCH-001', date: '2026-03-24', timeSlot: '14:00-15:00', templateType: '团购体验课', teacher: '刘老师', student: '李予安', teacherSigned: false, studentSigned: false, cancelled: false },
    { id: 'SCH-002', date: '2026-03-24', timeSlot: '15:00-16:00', templateType: '标准课', teacher: '陈老师', student: '张小满', teacherSigned: true, studentSigned: true, cancelled: false },
    { id: 'SCH-003', date: '2026-03-25', timeSlot: '18:00-19:00', templateType: '一对一', teacher: '赵老师', student: '王星河', teacherSigned: true, studentSigned: false, cancelled: true }
  ])
  const [scheduleListTab, setScheduleListTab] = useState('scheduled')
  const [scheduleActionModalOpen, setScheduleActionModalOpen] = useState(false)
  const [scheduleActionType, setScheduleActionType] = useState('teacher-sign')
  const [activeScheduleCourseId, setActiveScheduleCourseId] = useState('')
  const [scheduleAssistStudentName, setScheduleAssistStudentName] = useState('')
  const [scheduleAssistDeductHours, setScheduleAssistDeductHours] = useState('1')
  const [bookingModalOpen, setBookingModalOpen] = useState(false)
  const [bookingStep, setBookingStep] = useState(1)
  const [bookingStudentKeyword, setBookingStudentKeyword] = useState('')
  const [bookingSelectedStudentId, setBookingSelectedStudentId] = useState('')
  const [bookingSelectedSlotId, setBookingSelectedSlotId] = useState('')
  const [bookingDeductHours, setBookingDeductHours] = useState('1')
  const [packageTemplateKeyword, setPackageTemplateKeyword] = useState('')
  const [noticeRuleRows, setNoticeRuleRows] = useState([
    { id: 'R-001', category: '上课提醒', remindAhead: '提前1天、提前6小时', effectiveTime: '2026-03-24 00:00', channels: ['站内消息', '小程序订阅消息', '微信模板消息', '短信'], enabled: true },
    { id: 'R-002', category: '作业提醒', remindAhead: '截止前24小时、截止后2小时', effectiveTime: '2026-03-24 00:00', channels: ['站内消息', '小程序订阅消息'], enabled: true },
    { id: 'R-003', category: '核销未预约提醒', remindAhead: '核销后第1/3/7天', effectiveTime: '2026-03-24 00:00', channels: ['微信模板消息', '短信'], enabled: true },
    { id: 'R-004', category: '课时不足/到期提醒', remindAhead: '剩余≤2课时即时、到期前7天', effectiveTime: '2026-03-24 00:00', channels: ['站内消息', '微信模板消息'], enabled: true },
    { id: 'R-005', category: '调课审核结果提醒', remindAhead: '审批完成后即时', effectiveTime: '2026-03-24 00:00', channels: ['站内消息', '短信'], enabled: true }
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
  const [ruleForm, setRuleForm] = useState({ category: '', remindAhead: '', effectiveTime: '2026-03-24 00:00', channels: ['站内消息'], enabled: true })
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
  const activeStudentHourRows = studentHourConsumeRows.filter((item) => item.studentId === activeStudentId)
  const activeStudentPurchaseRows = studentPackagePurchaseRows.filter((item) => item.studentId === activeStudentId)
  const activeStudentClassRows = studentClassRecordRows.filter((item) => item.studentId === activeStudentId)
  const activeStudentFeedbackRows = studentFeedbackRows.filter((item) => item.studentId === activeStudentId)
  const activeStudentHomeworkRows = studentHomeworkRecordRows.filter((item) => item.studentId === activeStudentId)
  const [activePackageId, setActivePackageId] = useState(coursePackages[0].id)
  const activePackage = packageRows.find((item) => item.id === activePackageId) || packageRows[0]
  const activeMessage = notifications.find((item) => item.id === activeMessageId) || notifications[0]
  const activeScheduleCourse = scheduleCourseRows.find((item) => item.id === activeScheduleCourseId) || scheduleCourseRows[0]
  const scheduleAssistStudent = studentRows.find((item) => item.name === scheduleAssistStudentName)
  const bookingSelectedStudent = studentRows.find((item) => item.id === bookingSelectedStudentId)
  const bookingSelectedSlot = schedulePlanRows.find((item) => item.id === bookingSelectedSlotId)
  const resolveTemplateNames = (templateIds = []) => {
    if (!templateIds || templateIds.length === 0) return []
    return templateIds.map((id) => scheduleTemplateRows.find((item) => item.id === id)?.name || id)
  }
  const packageTemplateCandidates = scheduleTemplateRows.filter((item) =>
    item.status === '启用' && item.name.includes(packageTemplateKeyword.trim())
  )
  const scheduleBookableRows = (campus ? schedulePlanRows.filter((item) => item.campus === campus) : schedulePlanRows).filter((item) => item.reservedCount < item.capacityMax)
  const filteredBookingStudents = studentRows.filter((item) => (
    bookingStudentKeyword.trim()
      ? `${item.name}${item.phone}${item.packageName || ''}`.includes(bookingStudentKeyword.trim())
      : true
  ))
  const filteredAdminHomeworkRows = campus
    ? homeworkRows.filter((item) => item.campus === campus)
    : homeworkRows
  const filteredHomeworkRows = filteredAdminHomeworkRows.filter((item) => (
    homeworkStatusFilter === '全部' ? true : item.status === homeworkStatusFilter
  ))
  const activeHomework = homeworkRows.find((item) => item.id === activeHomeworkId) || homeworkRows[0]
  const openStudentArchiveDrawer = (type) => {
    setStudentArchiveDrawerType(type)
    setStudentArchiveDrawerOpen(true)
  }
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
    { label: '发布公告', to: '/admin/notice-rules', icon: BookOpen, card: 'bg-purple-50 text-[#9333ea] hover:bg-purple-100' }
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

  const openOrderInvalidModal = (orderId) => {
    setOrderInvalidTargetId(orderId)
    setOrderInvalidReason('')
    setOrderInvalidModalOpen(true)
  }

  const submitOrderInvalid = () => {
    if (!orderInvalidTargetId || !orderInvalidReason.trim()) {
      setFlowTip('请先填写无效原因')
      return
    }
    setOrderRows((prev) =>
      prev.map((row) =>
        row.id === orderInvalidTargetId
          ? { ...row, validation: `标记无效：${orderInvalidReason}`, status: '无效' }
          : row
      )
    )
    setOrderInvalidModalOpen(false)
    setFlowTip('已标记订单无效并记录原因')
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
    if (!newPackageForm.name.trim()) {
      setFlowTip('请填写课包名称')
      return
    }
    const enabledTemplateIds = scheduleTemplateRows.filter((item) => item.status === '启用').map((item) => item.id)
    const normalizedTemplateIds = [...new Set(newPackageForm.templateIds)].filter((id) => enabledTemplateIds.includes(id))
    const templateIds = newPackageForm.bindMode === 'template' && normalizedTemplateIds.length === 0 && enabledTemplateIds.length > 0
      ? [enabledTemplateIds[0]]
      : normalizedTemplateIds
    if (newPackageForm.bindMode === 'template' && templateIds.length === 0) {
      setFlowTip('当前没有可用模板，请先在课程模板管理中启用模板')
      return
    }
    const pack = {
      id: `P-${packageRows.length + 1}`,
      type: newPackageForm.type,
      name: newPackageForm.name.trim(),
      bindMode: newPackageForm.bindMode,
      templateIds,
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
      bindMode: 'free',
      templateIds: [],
      displayPrice: '0',
      salePrice: '0',
      stock: '0',
      validDays: '365',
      validUntil: '2027-03-31'
    })
    setPackageTemplateKeyword('')
    setFlowTip(`已新建课包：${pack.name}${newPackageForm.bindMode === 'template' && normalizedTemplateIds.length === 0 ? '（已自动绑定首个可用模板）' : ''}`)
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

  const openStudentPackageModal = (student) => {
    setActiveStudentId(student.id)
    setStudentPackageForm({
      packageId: packageRows[0]?.id || 'P-1',
      grantHours: '24',
      attrs: ''
    })
    setStudentPackageModalOpen(true)
  }

  const grantStudentPackage = () => {
    const selectedPackage = packageRows.find((item) => item.id === studentPackageForm.packageId)
    if (!selectedPackage) {
      setFlowTip('请选择课包后再赋予')
      return
    }
    setStudentRows((prev) =>
      prev.map((item) =>
        item.id === activeStudentId
          ? {
              ...item,
              packageName: selectedPackage.name,
              remaining: Number(studentPackageForm.grantHours || 0),
              benefits: studentPackageForm.attrs || item.benefits
            }
          : item
      )
    )
    setStudentPackageModalOpen(false)
    setFlowTip(`已为学员赋予课包：${selectedPackage.name}`)
  }

  const openRuleModal = (rule) => {
    setActiveRuleId(rule.id)
    setRuleForm({
      category: rule.category,
      remindAhead: rule.remindAhead,
      effectiveTime: rule.effectiveTime || '2026-03-24 00:00',
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
    setScheduleCourseRows((prev) => [
      {
        id: newPlan.id,
        date: newPlan.date,
        timeSlot: newPlan.timeSlot,
        templateType: newPlan.templateType,
        teacher: newPlan.teacher,
        student: newPlan.studentName || '团课学员',
        teacherSigned: false,
        studentSigned: false,
        cancelled: false
      },
      ...prev
    ])
    setScheduleArrangeModalOpen(false)
    setScheduleCandidateRows([])
    setFlowTip(`已完成排课：${newPlan.date} ${newPlan.timeSlot} ${newPlan.templateName}`)
  }

  const openScheduleActionModal = (rowId, actionType) => {
    const target = scheduleCourseRows.find((item) => item.id === rowId)
    if (!target) return
    const defaultStudent = studentRows.find((item) => item.name === target.student)?.name || studentRows[0]?.name || ''
    setActiveScheduleCourseId(rowId)
    setScheduleActionType(actionType)
    setScheduleAssistStudentName(defaultStudent)
    setScheduleAssistDeductHours('1')
    setScheduleActionModalOpen(true)
  }

  const confirmScheduleAction = () => {
    if (!activeScheduleCourseId) return
    if (scheduleActionType === 'student-sign' && !scheduleAssistStudentName) {
      setFlowTip('请先选择学员')
      return
    }
    setScheduleCourseRows((prev) =>
      prev.map((item) => {
        if (item.id !== activeScheduleCourseId) return item
        if (scheduleActionType === 'teacher-sign') {
          return { ...item, teacherSigned: !item.teacherSigned }
        }
        if (scheduleActionType === 'student-sign') {
          return {
            ...item,
            studentSigned: true,
            student: scheduleAssistStudentName
          }
        }
        return {
          ...item,
          teacherSigned: false,
          studentSigned: false,
          cancelled: false
        }
      })
    )
    if (scheduleActionType === 'student-sign') {
      const deductHours = Number(scheduleAssistDeductHours || 0)
      setStudentRows((prev) =>
        prev.map((item) =>
          item.name === scheduleAssistStudentName
            ? { ...item, remaining: Math.max(0, (item.remaining ?? 0) - deductHours) }
            : item
        )
      )
    }
    if (scheduleActionType === 'teacher-sign') {
      setFlowTip('已完成教师签到状态更新')
    } else if (scheduleActionType === 'student-sign') {
      setFlowTip(`已帮助学员签到并扣减 ${scheduleAssistDeductHours || 0} 课时`)
    } else {
      setFlowTip('已取消该课程签到状态')
    }
    setScheduleActionModalOpen(false)
  }

  const openBookingModal = (slotId = '') => {
    setBookingSelectedSlotId(slotId)
    setBookingSelectedStudentId('')
    setBookingStudentKeyword('')
    setBookingDeductHours('1')
    setBookingStep(1)
    setBookingModalOpen(true)
  }

  const chooseBookingStudent = (studentId) => {
    setBookingSelectedStudentId(studentId)
    setBookingStep(2)
  }

  const chooseBookingSlot = (slotId) => {
    setBookingSelectedSlotId(slotId)
    setBookingStep(3)
  }

  const submitBooking = () => {
    if (!bookingSelectedStudentId) {
      setFlowTip('请先选择学员')
      return
    }
    if (!bookingSelectedSlotId) {
      setFlowTip('请先选择课程')
      return
    }
    const deductHours = Number(bookingDeductHours || 0)
    if (Number.isNaN(deductHours) || deductHours <= 0) {
      setFlowTip('请填写有效扣减课时')
      return
    }
    const student = studentRows.find((item) => item.id === bookingSelectedStudentId)
    const slot = schedulePlanRows.find((item) => item.id === bookingSelectedSlotId)
    if (!student || !slot) return
    if (slot.reservedCount >= slot.capacityMax) {
      setFlowTip('该课程已满员，无法预约')
      return
    }
    const existedBookedStudents = Array.isArray(slot.bookedStudents) ? slot.bookedStudents : slot.studentName ? [slot.studentName] : []
    const isNewReservation = !existedBookedStudents.includes(student.name)
    setSchedulePlanRows((prev) =>
      prev.map((item) => {
        if (item.id !== bookingSelectedSlotId) return item
        const bookedStudents = Array.isArray(item.bookedStudents) ? item.bookedStudents : item.studentName ? [item.studentName] : []
        const nextBooked = bookedStudents.includes(student.name) ? bookedStudents : [...bookedStudents, student.name]
        return {
          ...item,
          bookedStudents: nextBooked,
          reservedCount: bookedStudents.includes(student.name) ? item.reservedCount : Math.min(item.capacityMax, item.reservedCount + 1),
          studentName: item.capacityMax === 1 ? student.name : item.studentName
        }
      })
    )
    setScheduleCourseRows((prev) => {
      const existing = prev.find((item) => item.id === bookingSelectedSlotId)
      const targetPlan = schedulePlanRows.find((item) => item.id === bookingSelectedSlotId) || slot
      const fallbackStudent = targetPlan.studentName || student.name
      const bookedStudents = Array.isArray(targetPlan.bookedStudents) ? targetPlan.bookedStudents : targetPlan.studentName ? [targetPlan.studentName] : []
      const nextBooked = bookedStudents.includes(student.name) ? bookedStudents : [...bookedStudents, student.name]
      const nextStudentText = nextBooked.length > 0 ? nextBooked.join('、') : fallbackStudent
      if (existing) {
        return prev.map((item) => (item.id === bookingSelectedSlotId ? { ...item, student: nextStudentText } : item))
      }
      return [
        {
          id: targetPlan.id,
          date: targetPlan.date,
          timeSlot: targetPlan.timeSlot,
          templateType: targetPlan.templateType,
          teacher: targetPlan.teacher,
          student: nextStudentText,
          teacherSigned: false,
          studentSigned: false,
          cancelled: false
        },
        ...prev
      ]
    })
    setStudentRows((prev) =>
      prev.map((item) =>
        item.id === bookingSelectedStudentId
          ? { ...item, remaining: isNewReservation ? Math.max(0, (item.remaining ?? 0) - deductHours) : (item.remaining ?? 0) }
          : item
      )
    )
    setBookingModalOpen(false)
    setFlowTip(`已为学员完成代预约：${student.name}（扣减${deductHours}课时）`)
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
            { key: 'orderNo', title: '订单编号' },
            { key: 'platform', title: '平台' },
            { key: 'studentName', title: '学员姓名' },
            { key: 'phone', title: '联系方式', render: (row) => row.phone || '—' },
            { key: 'campus', title: '校区', render: (row) => row.campus || '—' },
            { key: 'course', title: '购买课程' },
            { key: 'purchasedAt', title: '购买时间' },
            { key: 'status', title: '核销状态' },
            { key: 'validation', title: '校验结果' },
            { key: 'action', title: '操作', render: (row) => <button onClick={() => openOrderInvalidModal(row.id)} className="text-[#a64545]">标记无效</button> }
          ]}
          rows={orderRows}
          pageSize={6}
        />
        {orderInvalidModalOpen && (
          <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setOrderInvalidModalOpen(false)} />
            <div className="relative w-full max-w-[520px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">标记无效</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setOrderInvalidModalOpen(false)}>关闭</button>
              </div>
              <div className="space-y-1">
                <div className="text-xs text-[#7b7064]">无效原因</div>
                <textarea value={orderInvalidReason} onChange={(e) => setOrderInvalidReason(e.target.value)} className="h-24 w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" placeholder="请输入无效原因，例如退款、手机号错误等" />
              </div>
              <button onClick={submitOrderInvalid} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">确认标记无效</button>
            </div>
          </div>
        )}
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
        <h2 className="text-sm font-semibold">教学管理 / 排课日历</h2>
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

  if (page === 'schedule-list') {
    const scheduleListTabs = [
      { key: 'scheduled', label: '已排课' },
      { key: 'bookable', label: '可预约课程池' }
    ]
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">教学管理 / 课程列表（排课）</h2>
        <div className="rounded-2xl border border-[#f0ebe3] bg-[#fffaf2] p-3">
          <div className="flex flex-wrap items-center gap-2">
            {scheduleListTabs.map((item) => (
              <button
                key={item.key}
                onClick={() => setScheduleListTab(item.key)}
                className={`rounded-full px-3 py-1 text-xs ${
                  scheduleListTab === item.key ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-white text-[#8f8376]'
                }`}
              >
                {item.label}
              </button>
            ))}
            <span className="ml-auto text-xs text-[#8f8376]">校区：{campus || '全部'}</span>
          </div>
        </div>
        {scheduleListTab === 'scheduled' && (
          <PaginatedTable
            columns={[
              { key: 'date', title: '日期' },
              { key: 'timeSlot', title: '时间段' },
              { key: 'templateType', title: '课程类型' },
              { key: 'student', title: '预约学员' },
              { key: 'teacherSigned', title: '教师签到', render: (row) => (row.teacherSigned ? '已签到' : '未签到') },
              { key: 'studentSigned', title: '学员签到', render: (row) => (row.studentSigned ? '已签到' : '未签到') },
              { key: 'cancelled', title: '是否取消', render: (row) => (row.cancelled ? '是' : '否') },
              {
                key: 'action',
                title: '操作',
                render: (row) => (
                  <div className="flex gap-2">
                    <button onClick={() => openBookingModal(row.id)} className="text-[#bc7844] text-xs">代预约</button>
                    <button onClick={() => openScheduleActionModal(row.id, 'teacher-sign')} className="text-[#bc7844] text-xs">帮助老师签到</button>
                    <button onClick={() => openScheduleActionModal(row.id, 'student-sign')} className="text-[#bc7844] text-xs">帮助学生签到</button>
                    <button onClick={() => openScheduleActionModal(row.id, 'cancel-sign')} className="text-[#bc7844] text-xs">取消签到状态</button>
                  </div>
                )
              }
            ]}
            rows={campus ? scheduleCourseRows.filter((item) => schedulePlanRows.find((p) => p.id === item.id)?.campus === campus) : scheduleCourseRows}
            pageSize={8}
          />
        )}
        {scheduleListTab === 'bookable' && (
          <PaginatedTable
            columns={[
              { key: 'date', title: '日期' },
              { key: 'timeSlot', title: '时间段' },
              { key: 'templateName', title: '课程模板' },
              { key: 'teacher', title: '教师' },
              { key: 'room', title: '教室' },
              { key: 'capacity', title: '承载量', render: (row) => `${row.reservedCount}/${row.capacityMax}` },
              { key: 'bookedStudents', title: '已预约学员', render: (row) => (Array.isArray(row.bookedStudents) ? row.bookedStudents.join('、') : row.studentName || '—') },
              { key: 'action', title: '操作', render: (row) => <button onClick={() => openBookingModal(row.id)} className="text-[#bc7844]">代预约</button> }
            ]}
            rows={scheduleBookableRows}
            pageSize={8}
            emptyText="当前校区暂无可预约课程"
          />
        )}
        {scheduleActionModalOpen && (
          <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setScheduleActionModalOpen(false)} />
            <div className="relative w-full max-w-[760px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">
                  {scheduleActionType === 'teacher-sign' ? '帮助老师签到' : scheduleActionType === 'student-sign' ? '帮助学生签到' : '取消签到状态'}
                </h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setScheduleActionModalOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div className="rounded-xl bg-[#faf8f4] p-3">课程日期：{activeScheduleCourse?.date}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">时间段：{activeScheduleCourse?.timeSlot}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">课程类型：{activeScheduleCourse?.templateType}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">授课教师：{activeScheduleCourse?.teacher}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">教师签到：{activeScheduleCourse?.teacherSigned ? '已签到' : '未签到'}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">学员签到：{activeScheduleCourse?.studentSigned ? '已签到' : '未签到'}</div>
              </div>
              {scheduleActionType === 'student-sign' && (
                <div className="mt-3 grid grid-cols-2 gap-3">
                  <div className="space-y-1">
                    <div className="text-xs text-[#7b7064]">选择学员</div>
                    <select value={scheduleAssistStudentName} onChange={(e) => setScheduleAssistStudentName(e.target.value)} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none">
                      {studentRows.map((item) => <option key={item.id}>{item.name}</option>)}
                    </select>
                  </div>
                  <div className="space-y-1">
                    <div className="text-xs text-[#7b7064]">扣减课时</div>
                    <input value={scheduleAssistDeductHours} onChange={(e) => setScheduleAssistDeductHours(e.target.value)} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" />
                  </div>
                  <div className="col-span-2 rounded-xl bg-[#fffcf8] p-3 text-xs text-[#6f665d]">
                    学员详情：{scheduleAssistStudent?.name || '—'} / 课包：{scheduleAssistStudent?.packageName || '待分配'} / 剩余课时：{scheduleAssistStudent?.remaining ?? 0} / 标签：{(scheduleAssistStudent?.progressTags || []).join('、') || '—'}
                  </div>
                </div>
              )}
              {scheduleActionType === 'cancel-sign' && (
                <div className="mt-3 rounded-xl border border-[#f3e5d5] bg-[#fffaf2] p-3 text-xs text-[#7b7064]">
                  将取消该课程当前签到结果，教师与学员签到状态会同步重置。
                </div>
              )}
              <button onClick={confirmScheduleAction} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">确认提交</button>
            </div>
          </div>
        )}
        {bookingModalOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setBookingModalOpen(false)} />
            <div className="relative w-full max-w-[980px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">教务代预约课程</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setBookingModalOpen(false)}>关闭</button>
              </div>
              <div className="mb-3 rounded-xl border border-[#f0ebe3] bg-[#fffaf2] p-3 text-xs text-[#6f665d]">
                步骤 {bookingStep}/3：{bookingStep === 1 ? '选学员' : bookingStep === 2 ? '选课程' : '确认预约'}
              </div>
              {bookingStep === 1 && (
                <div className="space-y-3">
                  <input value={bookingStudentKeyword} onChange={(e) => setBookingStudentKeyword(e.target.value)} className="w-full rounded-lg border border-[#e9e2d8] px-3 py-2 text-sm outline-none" placeholder="搜索学员姓名/手机号/课包" />
                  <PaginatedTable
                    columns={[
                      { key: 'name', title: '学员' },
                      { key: 'phone', title: '手机号' },
                      { key: 'packageName', title: '课包' },
                      { key: 'remaining', title: '剩余课时' },
                      { key: 'action', title: '操作', render: (row) => <button onClick={() => chooseBookingStudent(row.id)} className="text-[#bc7844]">选择</button> }
                    ]}
                    rows={filteredBookingStudents}
                    pageSize={6}
                  />
                </div>
              )}
              {bookingStep === 2 && (
                <div className="space-y-3">
                  <div className="rounded-xl bg-[#faf8f4] p-3 text-sm">
                    已选学员：{bookingSelectedStudent?.name || '—'} / 课包：{bookingSelectedStudent?.packageName || '待分配'} / 剩余课时：{bookingSelectedStudent?.remaining ?? 0}
                  </div>
                  <PaginatedTable
                    columns={[
                      { key: 'date', title: '日期' },
                      { key: 'timeSlot', title: '时间段' },
                      { key: 'templateName', title: '课程模板' },
                      { key: 'teacher', title: '教师' },
                      { key: 'room', title: '教室' },
                      { key: 'capacity', title: '承载量', render: (row) => `${row.reservedCount}/${row.capacityMax}` },
                      { key: 'action', title: '操作', render: (row) => <button onClick={() => chooseBookingSlot(row.id)} className="text-[#bc7844]">选择</button> }
                    ]}
                    rows={scheduleBookableRows}
                    pageSize={6}
                    emptyText="当前校区暂无可预约课程"
                  />
                  <div className="flex justify-end gap-2">
                    <button onClick={() => setBookingStep(1)} className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">上一步</button>
                  </div>
                </div>
              )}
              {bookingStep === 3 && (
                <div className="space-y-3">
                  <div className="grid grid-cols-2 gap-2 text-sm">
                    <div className="rounded-xl bg-[#faf8f4] p-3">学员：{bookingSelectedStudent?.name || '—'}</div>
                    <div className="rounded-xl bg-[#faf8f4] p-3">剩余课时：{bookingSelectedStudent?.remaining ?? 0}</div>
                    <div className="rounded-xl bg-[#faf8f4] p-3">课程日期：{bookingSelectedSlot?.date || '—'}</div>
                    <div className="rounded-xl bg-[#faf8f4] p-3">时间段：{bookingSelectedSlot?.timeSlot || '—'}</div>
                    <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">课程：{bookingSelectedSlot?.templateName || '—'} / {bookingSelectedSlot?.teacher || '—'} / {bookingSelectedSlot?.room || '—'}</div>
                  </div>
                  <div className="grid grid-cols-2 gap-3">
                    <div className="space-y-1">
                      <div className="text-xs text-[#7b7064]">扣减课时</div>
                      <input value={bookingDeductHours} onChange={(e) => setBookingDeductHours(e.target.value)} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" />
                    </div>
                    <div className="space-y-1">
                      <div className="text-xs text-[#7b7064]">占位规则</div>
                      <div className="rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs text-[#6f665d]">确认后占用该课程名额，并同步到课程预约名单</div>
                    </div>
                  </div>
                  <div className="flex justify-end gap-2">
                    <button onClick={() => setBookingStep(2)} className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">上一步</button>
                    <button onClick={submitBooking} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">确认代预约</button>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'homework-list') {
    const filterTabs = ['全部', '待提交', '未提交', '已提交', '待批改']
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">教学管理 / 作业列表</h2>
        <div className="rounded-2xl border border-[#f0ebe3] bg-[#fffaf2] p-3">
          <div className="flex flex-wrap items-center gap-2">
            {filterTabs.map((item) => (
              <button
                key={item}
                onClick={() => setHomeworkStatusFilter(item)}
                className={`rounded-full px-3 py-1 text-xs ${
                  homeworkStatusFilter === item ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-white text-[#8f8376]'
                }`}
              >
                {item}
              </button>
            ))}
            <span className="ml-auto text-xs text-[#8f8376]">校区：{campus || '全部'}</span>
          </div>
        </div>
        <PaginatedTable
          columns={[
            { key: 'student', title: '学员' },
            { key: 'course', title: '课程' },
            { key: 'title', title: '作业' },
            { key: 'deadline', title: '截止时间' },
            { key: 'status', title: '状态' },
            { key: 'submittedAt', title: '提交时间' },
            { key: 'teacher', title: '授课老师' },
            {
              key: 'action',
              title: '操作',
              render: (row) => (
                <div className="flex gap-2">
                  <button onClick={() => { setActiveHomeworkId(row.id); setHomeworkDrawerOpen(true) }} className="text-[#bc7844]">详情</button>
                  {row.status === '未提交' || row.status === '待提交'
                    ? <button onClick={() => setFlowTip(`已向学员发送催交通知：${row.student}`)} className="text-[#8f8376]">催交</button>
                    : <button onClick={() => setFlowTip(`已转交授课老师处理：${row.teacher}`)} className="text-[#8f8376]">转老师</button>}
                </div>
              )
            }
          ]}
          rows={filteredHomeworkRows}
          pageSize={8}
        />
        {homeworkDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setHomeworkDrawerOpen(false)} />
            <div className="h-full w-[520px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">作业详情</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setHomeworkDrawerOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div className="rounded-xl bg-[#faf8f4] p-3">校区：{activeHomework?.campus}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">授课老师：{activeHomework?.teacher}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">学员：{activeHomework?.student}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">状态：{activeHomework?.status}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">课程：{activeHomework?.course}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">作业：{activeHomework?.title}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">截止：{activeHomework?.deadline}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">提交：{activeHomework?.submittedAt}</div>
              </div>
              <div className="mt-3 grid grid-cols-3 gap-2">
                <button onClick={() => setFlowTip(`已发送作业提醒：${activeHomework?.student}`)} className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">发送提醒</button>
                <button onClick={() => setFlowTip(`已通知授课老师：${activeHomework?.teacher}`)} className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">通知老师</button>
                <button onClick={() => setHomeworkDrawerOpen(false)} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">完成</button>
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
          <h2 className="text-sm font-semibold">教学管理 / 课程模板管理</h2>
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
          <h2 className="text-sm font-semibold">教学管理 / 教室资源管理</h2>
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
                <div className="flex gap-2">
                  <button
                    onClick={() => {
                      setActiveStudentId(row.id)
                      setStudentDrawerOpen(true)
                    }}
                    className="text-[#bc7844]"
                  >
                    查看详情
                  </button>
                  <button onClick={() => openStudentPackageModal(row)} className="text-[#bc7844]">赋予课包</button>
                </div>
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
              <div className="mb-3 grid grid-cols-2 gap-2 text-sm">
                <button onClick={() => openStudentArchiveDrawer('hours')} className="rounded-xl border border-[#f0ebe3] bg-[#fffaf2] p-3 text-left">
                  <div className="text-xs text-[#8f8376]">课时消费</div>
                  <div className="mt-1 text-sm font-semibold">{activeStudentHourRows.length} 条</div>
                  <div className="mt-1 text-xs text-[#bc7844]">查看明细</div>
                </button>
                <button onClick={() => openStudentArchiveDrawer('packages')} className="rounded-xl border border-[#f0ebe3] bg-[#fffaf2] p-3 text-left">
                  <div className="text-xs text-[#8f8376]">课包购买</div>
                  <div className="mt-1 text-sm font-semibold">{activeStudentPurchaseRows.length} 笔</div>
                  <div className="mt-1 text-xs text-[#bc7844]">查看明细</div>
                </button>
                <button onClick={() => openStudentArchiveDrawer('classes')} className="rounded-xl border border-[#f0ebe3] bg-[#fffaf2] p-3 text-left">
                  <div className="text-xs text-[#8f8376]">上课记录</div>
                  <div className="mt-1 text-sm font-semibold">{activeStudentClassRows.length} 节</div>
                  <div className="mt-1 text-xs text-[#bc7844]">查看明细</div>
                </button>
                <button onClick={() => openStudentArchiveDrawer('feedback')} className="rounded-xl border border-[#f0ebe3] bg-[#fffaf2] p-3 text-left">
                  <div className="text-xs text-[#8f8376]">课程反馈</div>
                  <div className="mt-1 text-sm font-semibold">{activeStudentFeedbackRows.length} 条</div>
                  <div className="mt-1 text-xs text-[#bc7844]">查看明细</div>
                </button>
                <button onClick={() => openStudentArchiveDrawer('homework')} className="col-span-2 rounded-xl border border-[#f0ebe3] bg-[#fffaf2] p-3 text-left">
                  <div className="text-xs text-[#8f8376]">作业记录</div>
                  <div className="mt-1 text-sm font-semibold">{activeStudentHomeworkRows.length} 条</div>
                  <div className="mt-1 text-xs text-[#bc7844]">查看明细</div>
                </button>
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
              </div>
            </div>
          </div>
        )}
        {studentArchiveDrawerOpen && (
          <div className="fixed inset-0 z-40 flex justify-end bg-black/30">
            <button className="flex-1" onClick={() => setStudentArchiveDrawerOpen(false)} />
            <div className="h-full w-[720px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">
                  {studentArchiveDrawerType === 'hours'
                    ? '课时消费明细'
                    : studentArchiveDrawerType === 'packages'
                      ? '课包购买记录'
                      : studentArchiveDrawerType === 'classes'
                        ? '上课记录'
                        : studentArchiveDrawerType === 'feedback'
                          ? '课程反馈记录'
                          : '作业记录'}
                </h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setStudentArchiveDrawerOpen(false)}>关闭</button>
              </div>
              <div className="mb-3 rounded-xl bg-[#faf8f4] p-3 text-sm">
                学员：{activeStudent?.name || '—'} / 当前课包：{activeStudent?.packageName || '待分配'} / 剩余课时：{activeStudent?.remaining ?? 0}
              </div>
              {studentArchiveDrawerType === 'hours' && (
                <PaginatedTable
                  columns={[
                    { key: 'date', title: '时间' },
                    { key: 'action', title: '动作' },
                    { key: 'hours', title: '课时', render: (row) => `${row.hours}` },
                    { key: 'course', title: '关联课程/说明' },
                    { key: 'operator', title: '操作人' }
                  ]}
                  rows={activeStudentHourRows}
                  pageSize={8}
                />
              )}
              {studentArchiveDrawerType === 'packages' && (
                <PaginatedTable
                  columns={[
                    { key: 'date', title: '购买时间' },
                    { key: 'packageName', title: '课包' },
                    { key: 'hours', title: '课时' },
                    { key: 'amount', title: '金额', render: (row) => `¥${row.amount}` },
                    { key: 'channel', title: '渠道' },
                    { key: 'status', title: '状态' }
                  ]}
                  rows={activeStudentPurchaseRows}
                  pageSize={8}
                />
              )}
              {studentArchiveDrawerType === 'classes' && (
                <PaginatedTable
                  columns={[
                    { key: 'date', title: '日期' },
                    { key: 'course', title: '课程' },
                    { key: 'teacher', title: '老师' },
                    { key: 'room', title: '教室' },
                    { key: 'sign', title: '签到' },
                    { key: 'deductHours', title: '扣减课时' }
                  ]}
                  rows={activeStudentClassRows}
                  pageSize={8}
                />
              )}
              {studentArchiveDrawerType === 'feedback' && (
                <PaginatedTable
                  columns={[
                    { key: 'date', title: '反馈时间' },
                    { key: 'course', title: '课程' },
                    { key: 'teacher', title: '老师' },
                    { key: 'rating', title: '评级' },
                    { key: 'summary', title: '摘要' }
                  ]}
                  rows={activeStudentFeedbackRows}
                  pageSize={8}
                />
              )}
              {studentArchiveDrawerType === 'homework' && (
                <PaginatedTable
                  columns={[
                    { key: 'date', title: '日期' },
                    { key: 'course', title: '课程' },
                    { key: 'title', title: '作业' },
                    { key: 'status', title: '状态' },
                    { key: 'submittedAt', title: '提交时间' },
                    { key: 'teacher', title: '老师' }
                  ]}
                  rows={activeStudentHomeworkRows}
                  pageSize={8}
                />
              )}
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
        {studentPackageModalOpen && (
          <div className="fixed inset-0 z-30 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setStudentPackageModalOpen(false)} />
            <div className="relative w-full max-w-[520px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">赋予课包课时与属性</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setStudentPackageModalOpen(false)}>关闭</button>
              </div>
              <div className="space-y-3">
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">选择课包</div><select value={studentPackageForm.packageId} onChange={(e) => setStudentPackageForm((p) => ({ ...p, packageId: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none">{packageRows.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">赋予课时</div><input value={studentPackageForm.grantHours} onChange={(e) => setStudentPackageForm((p) => ({ ...p, grantHours: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">属性说明</div><input value={studentPackageForm.attrs} onChange={(e) => setStudentPackageForm((p) => ({ ...p, attrs: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" placeholder="例如：仅周末可约 / 赠送阶段测评" /></div>
              </div>
              <button onClick={grantStudentPackage} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">确认赋予</button>
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
            { key: 'bindMode', title: '模板绑定', render: (row) => (row.bindMode === 'template' ? '绑定模板' : '不绑定模板') },
            { key: 'templateIds', title: '已绑模板', render: (row) => resolveTemplateNames(row.templateIds).join('、') || '不限模板' },
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
                <div className="rounded-xl bg-[#faf8f4] p-3">模板绑定模式：{activePackage?.bindMode === 'template' ? '绑定模板' : '不绑定模板'}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">绑定模板：{resolveTemplateNames(activePackage?.templateIds).join('、') || '不限模板'}</div>
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
          <div
            className="fixed inset-0 z-30 flex items-center justify-center bg-black/30 p-4"
            onMouseDown={() => setNewPackageDrawerOpen(false)}
          >
            <div
              className="w-full max-w-[640px] rounded-2xl bg-white p-5 shadow-2xl"
              onMouseDown={(e) => e.stopPropagation()}
            >
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
                <div className="col-span-2 space-y-2">
                  <div className="text-xs text-[#7b7064]">模板绑定模式</div>
                  <div className="flex flex-wrap gap-2 text-xs">
                    <button onClick={() => setNewPackageForm((p) => ({ ...p, bindMode: 'free', templateIds: [] }))} className={`rounded-lg px-3 py-2 ${newPackageForm.bindMode === 'free' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e9e2d8]'}`}>不绑定模板（可约任意课程）</button>
                    <button onClick={() => setNewPackageForm((p) => ({ ...p, bindMode: 'template' }))} className={`rounded-lg px-3 py-2 ${newPackageForm.bindMode === 'template' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e9e2d8]'}`}>绑定模板（仅可约固定模板）</button>
                  </div>
                </div>
                {newPackageForm.bindMode === 'template' && (
                  <div className="col-span-2 space-y-2">
                    <div className="text-xs text-[#7b7064]">绑定课程模板（可多选）</div>
                    <input value={packageTemplateKeyword} onChange={(e) => setPackageTemplateKeyword(e.target.value)} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" placeholder="输入模板名搜索" />
                    <div className="flex items-center gap-2 text-xs">
                      <button onClick={() => setNewPackageForm((p) => ({ ...p, templateIds: [...new Set([...p.templateIds, ...packageTemplateCandidates.map((item) => item.id)])] }))} className="rounded border border-[#e8dfd3] px-2 py-1">全选当前筛选</button>
                      <button onClick={() => setNewPackageForm((p) => ({ ...p, templateIds: [] }))} className="rounded border border-[#e8dfd3] px-2 py-1">清空选择</button>
                      <span className="text-[#8f8376]">已选 {newPackageForm.templateIds.length} 个</span>
                    </div>
                    <div className="max-h-48 overflow-auto rounded-lg border border-[#e9e2d8] p-2 text-xs">
                      <div className="mb-2 flex flex-wrap gap-1">
                        {resolveTemplateNames(newPackageForm.templateIds).map((name) => (
                          <span key={name} className="rounded-full bg-[#faf8f4] px-2 py-0.5">{name}</span>
                        ))}
                      </div>
                      <div className="grid grid-cols-2 gap-2">
                        {packageTemplateCandidates.map((item) => (
                        <label key={item.id} className="flex items-center gap-2">
                          <input
                            type="checkbox"
                            checked={newPackageForm.templateIds.includes(item.id)}
                            onChange={(e) =>
                              setNewPackageForm((p) => ({
                                ...p,
                                templateIds: e.target.checked
                                  ? [...p.templateIds, item.id]
                                  : p.templateIds.filter((id) => id !== item.id)
                              }))
                            }
                          />
                          {item.name}
                        </label>
                      ))}
                      </div>
                    </div>
                  </div>
                )}
              </div>
              <button onClick={createPackage} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存课包</button>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'notice-rules' || page === 'notice-touch') {
    const firstTabs = [
      { key: 'message', label: '消息提醒', pages: ['notice-rules', 'notice-touch'] }
    ]
    const secondTabs = [{ key: 'notice-rules', label: '提醒规则' }, { key: 'notice-touch', label: '触达记录' }]

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
                { key: 'effectiveTime', title: '生效时间' },
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
                    <div className="col-span-2 space-y-1"><div className="text-xs text-[#7b7064]">生效时间</div><input value={ruleForm.effectiveTime} onChange={(e) => setRuleForm((p) => ({ ...p, effectiveTime: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
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
