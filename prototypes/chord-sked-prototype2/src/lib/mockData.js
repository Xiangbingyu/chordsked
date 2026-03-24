export const adminKpis = [
  { label: '本周核销数', value: 128, trend: '较上周 +18%' },
  { label: '待审核申请', value: 5, trend: '需今日处理 3 单' },
  { label: '未预约提醒', value: 8, trend: '核销后3天未预约' },
  { label: '本周上课节数', value: 246, trend: '排课完成率 96%' }
]

export const conversionLeads = [
  {
    id: 'L-001',
    name: '林知夏',
    phone: '13800101122',
    platform: '抖音',
    coupon: 'DY-774129',
    status: '已核销待建档',
    instrument: '木吉他',
    campus: '北环国基路校区',
    timeline: ['已导入', '已核销'],
    linkSentAt: ''
  },
  {
    id: 'L-002',
    name: '赵一诺',
    phone: '15900100018',
    platform: '美团',
    coupon: 'MT-220312',
    status: '已建档待预约',
    instrument: '电吉他',
    campus: '西大剧院校区',
    timeline: ['已导入', '已核销', '已建档'],
    linkSentAt: '2026-03-23 10:22'
  },
  {
    id: 'L-003',
    name: '陈沐辰',
    phone: '18600101820',
    platform: '大众点评',
    coupon: 'DP-002883',
    status: '已预约待签到',
    instrument: '贝斯',
    campus: '北环国基路校区',
    timeline: ['已导入', '已核销', '已建档', '已约体验'],
    linkSentAt: '2026-03-22 19:11'
  },
  {
    id: 'L-004',
    name: '高景川',
    phone: '13700101219',
    platform: '抖音',
    coupon: 'DY-991011',
    status: '已导入待核销',
    instrument: '架子鼓',
    campus: '西大剧院校区',
    timeline: ['已导入'],
    linkSentAt: ''
  }
]

export const conversionOrders = [
  {
    id: 'O-001',
    orderNo: 'DY20260322001',
    platform: '抖音',
    studentName: '林知夏',
    phone: '13800101122',
    campus: '北环国基路校区',
    course: '团购体验课',
    purchasedAt: '2026-03-22 14:30',
    status: '待核销',
    validation: '通过'
  },
  {
    id: 'O-002',
    orderNo: 'MT20260321008',
    platform: '美团',
    studentName: '赵一诺',
    phone: '15900100018',
    campus: '西大剧院校区',
    course: '吉他体验课',
    purchasedAt: '2026-03-21 20:10',
    status: '已核销',
    validation: '通过'
  },
  {
    id: 'O-003',
    orderNo: 'DY20260322005',
    platform: '抖音',
    studentName: '陈沐辰',
    phone: '',
    campus: '北环国基路校区',
    course: '贝斯体验课',
    purchasedAt: '2026-03-22 11:05',
    status: '导入失败',
    validation: '手机号缺失'
  },
  {
    id: 'O-004',
    orderNo: 'DP20260319001',
    platform: '大众点评',
    studentName: '王乐',
    phone: '13600102291',
    campus: '',
    course: '架子鼓体验课',
    purchasedAt: '2026-03-19 16:44',
    status: '导入失败',
    validation: '校区缺失'
  },
  {
    id: 'O-005',
    orderNo: 'DY20260322001',
    platform: '抖音',
    studentName: '林知夏',
    phone: '13800101122',
    campus: '北环国基路校区',
    course: '团购体验课',
    purchasedAt: '2026-03-22 14:30',
    status: '导入失败',
    validation: '重复订单号'
  },
  {
    id: 'O-006',
    orderNo: 'MT20260320015',
    platform: '美团',
    studentName: '高景川',
    phone: '13700101219',
    campus: '西大剧院校区',
    course: '鼓组体验课',
    purchasedAt: '2026-03-20 18:15',
    status: '待核销',
    validation: '通过'
  }
]

export const teachers = [
  {
    id: 'T-01',
    name: '刘老师',
    employeeNo: 'EMP-1001',
    phone: '13888881111',
    entryDate: '2021-03-12',
    teachingType: '团课+一对一',
    teachingQualification: '8年教学经验，持高级教师资格证',
    expertiseTracks: ['木吉他', '电吉他'],
    level: 'S',
    campus: '北环国基路校区',
    status: '在职',
    subject: '木吉他 / 电吉他',
    capacity: '团课 6 人',
    available: '周二四六 10:00-18:00',
    boundStudents: ['张小满', '李予安']
  },
  {
    id: 'T-02',
    name: '陈老师',
    employeeNo: 'EMP-1002',
    phone: '13888882222',
    entryDate: '2022-06-08',
    teachingType: '团课',
    teachingQualification: '6年教学经验，擅长启蒙教学',
    expertiseTracks: ['木吉他'],
    level: 'A',
    campus: '西大剧院校区',
    status: '在职',
    subject: '木吉他',
    capacity: '团课 6 人',
    available: '周一三五 13:00-20:00',
    boundStudents: ['张小满']
  },
  {
    id: 'T-03',
    name: '王老师',
    employeeNo: 'EMP-1003',
    phone: '13888883333',
    entryDate: '2020-11-16',
    teachingType: '一对一+团课',
    teachingQualification: '10年教学经验，乐队演出导师',
    expertiseTracks: ['贝斯', '电吉他'],
    level: 'S',
    campus: '北环国基路校区',
    status: '在职',
    subject: '贝斯 / 乐队编排',
    capacity: '团课 4 人',
    available: '周一至周日 15:00-21:00',
    boundStudents: ['王星河', '李予安']
  },
  {
    id: 'T-04',
    name: '周老师',
    employeeNo: 'EMP-1004',
    phone: '13888884444',
    entryDate: '2023-02-01',
    teachingType: '团课',
    teachingQualification: '4年教学经验，节奏专项导师',
    expertiseTracks: ['电吉他'],
    level: 'B',
    campus: '西大剧院校区',
    status: '冻结',
    subject: '电吉他 / 节奏训练',
    capacity: '团课 5 人',
    available: '周末全天 / 工作日夜场',
    boundStudents: ['李予安']
  },
  {
    id: 'T-05',
    name: '赵老师',
    employeeNo: 'EMP-1005',
    phone: '13888885555',
    entryDate: '2019-09-09',
    teachingType: '一对一',
    teachingQualification: '12年教学经验，赛事辅导导师',
    expertiseTracks: ['贝斯', '木吉他'],
    level: 'A',
    campus: '北环国基路校区',
    status: '在职',
    subject: '一对一冲刺课',
    capacity: '一对一',
    available: '周二至周六 09:00-17:00',
    boundStudents: ['王星河']
  }
]

export const students = [
  {
    id: 'S-001',
    name: '张小满',
    phone: '139****4432',
    guitarCategory: '木吉他',
    age: 12,
    gender: '女',
    parentName: '张磊',
    hasBasic: '有',
    basicDescription: '会基础和弦转换',
    intendedSubject: '木吉他',
    learningPurpose: '校园演出弹唱',
    expectedClassTime: '周六 10:00',
    expectedClassLocation: '北环国基路校区',
    groupCoursePackage: '体验课2节包',
    channel: '抖音团购',
    hasInstrument: '有',
    signupHours: 24,
    formalTeacher: '陈老师',
    isReferral: '否',
    registrationFormSummary: '团购体验预约已登记，偏好周末白天课程',
    progressTags: ['低活跃提醒', '正式课'],
    packageName: '标准课24节包',
    remaining: 18,
    tags: ['低活跃提醒', '正式课', '北环校区'],
    goal: '两个月内完成《晴天》弹唱',
    lastFeedback: '节奏稳定，和弦转换仍需提速',
    classRecords: ['2026-03-10 标准课：扫弦练习', '2026-03-17 标准课：节拍器训练'],
    homeworkRecords: ['2026-03-18 作业：主歌弹唱视频 已点评'],
    signupTime: '2026-01-15',
    paymentAmount: 3280,
    benefits: '赠送入门课程资料包+月度测评'
  },
  {
    id: 'S-002',
    name: '李予安',
    phone: '158****1126',
    guitarCategory: '电吉他',
    age: 14,
    gender: '男',
    parentName: '李芳',
    hasBasic: '无',
    basicDescription: '',
    intendedSubject: '电吉他',
    learningPurpose: '组建校园乐队',
    expectedClassTime: '周三 19:00',
    expectedClassLocation: '西大剧院校区',
    groupCoursePackage: '体验课2节包',
    channel: '美团',
    hasInstrument: '无',
    signupHours: 12,
    formalTeacher: '王老师',
    isReferral: '是',
    registrationFormSummary: '体验预约登记完成，需学校放学后时段',
    progressTags: ['即将到期', '团课'],
    packageName: '冲刺课12节包',
    remaining: 6,
    tags: ['即将到期', '团课', '西大剧院校区'],
    goal: '建立扫弦节奏与右手控制',
    lastFeedback: '课堂表现积极，建议增加节拍器练习',
    classRecords: ['2026-03-12 团课：Power Chord 练习'],
    homeworkRecords: ['2026-03-16 作业：节奏型音频 已点评'],
    signupTime: '2025-12-08',
    paymentAmount: 4980,
    benefits: '乐队排练室体验券2次'
  },
  {
    id: 'S-003',
    name: '王星河',
    phone: '136****0901',
    guitarCategory: '贝斯',
    age: 15,
    gender: '男',
    parentName: '王强',
    hasBasic: '有',
    basicDescription: '有半年民谣吉他基础',
    intendedSubject: '贝斯',
    learningPurpose: '比赛备赛',
    expectedClassTime: '周日 16:00',
    expectedClassLocation: '北环国基路校区',
    groupCoursePackage: '体验课2节包',
    channel: '门店咨询',
    hasInstrument: '有',
    signupHours: 24,
    formalTeacher: '赵老师',
    isReferral: '否',
    registrationFormSummary: '正式课转化学员，已登记高阶课程目标',
    progressTags: ['课时不足', '一对一'],
    packageName: '标准课24节包',
    remaining: 2,
    tags: ['课时不足', '一对一', '北环校区'],
    goal: '备赛曲目《Canon Rock》',
    lastFeedback: '左手按弦准确度提升明显',
    classRecords: ['2026-03-09 一对一：速弹分解训练', '2026-03-18 一对一：舞台表现训练'],
    homeworkRecords: ['2026-03-19 作业：速度练习视频 未提交'],
    signupTime: '2025-10-22',
    paymentAmount: 3280,
    benefits: '赠送阶段测评与演出复盘'
  }
]

export const scheduleCards = [
  { id: 'C-101', title: '体验课：新学员合班', teacher: '刘老师', room: 'A101', time: '周二 14:00-15:00', conflict: false },
  { id: 'C-102', title: '标准课：扫弦进阶', teacher: '陈老师', room: 'B203', time: '周二 15:00-16:30', conflict: false },
  { id: 'C-103', title: '一对一：王星河', teacher: '赵老师', room: 'A203', time: '周二 16:00-17:00', conflict: true },
  { id: 'C-104', title: '团课：贝斯合奏', teacher: '王老师', room: 'B205', time: '周二 18:00-19:00', conflict: false }
]

export const adjustmentRequests = [
  {
    id: 'A-203',
    student: '李予安',
    type: '调课',
    originTime: '3/25 19:00 标准课',
    expectTime: '3/27 18:00 后',
    impact: '改期不退课时'
  },
  {
    id: 'A-204',
    student: '张小满',
    type: '取消',
    originTime: '3/26 10:00 一对一',
    expectTime: '',
    impact: '按规则退 1 课时'
  },
  {
    id: 'A-205',
    student: '王星河',
    type: '调课',
    originTime: '3/28 15:00 冲刺课',
    expectTime: '3/29 11:00',
    impact: '推荐同模板课程 2 个'
  }
]

export const teacherTodo = ['待签到课程 2 节', '待填写课后反馈 3 节', '待批改作业 6 份', '未提交作业提醒 4 人']

export const teacherTodayCourses = [
  {
    id: 'TC-1',
    title: '标准课：节奏训练营',
    time: '今日 15:00-16:00',
    room: '北环校区 A101',
    studentCount: 4,
    students: [
      { name: '张小满', checked: true },
      { name: '李予安', checked: true },
      { name: '王星河', checked: false },
      { name: '陈可心', checked: true }
    ]
  },
  { id: 'TC-2', title: '一对一：周末冲刺', time: '今日 18:30-19:30', room: '北环校区 B103', studentCount: 1, students: [] }
]

export const homeworkPool = [
  {
    id: 'H-01',
    student: '张小满',
    title: '《晴天》主歌弹唱',
    status: '待批改',
    media: '视频 01:26',
    deadline: '截止 3/26 21:00'
  },
  {
    id: 'H-02',
    student: '李予安',
    title: 'C和弦到G和弦切换',
    status: '已提交',
    media: '音频 00:47',
    deadline: '截止 3/25 20:00'
  },
  {
    id: 'H-03',
    student: '王星河',
    title: '16分音符节拍练习',
    status: '未提交',
    media: '无提交',
    deadline: '截止 3/24 22:00'
  }
]

export const studentRemaining = {
  remainingHours: 6,
  todayClass: {
    title: '标准课：扫弦进阶',
    time: '今日 19:00-20:00',
    teacher: '陈老师'
  }
}

export const studentBookings = [
  { id: 'B-1', type: '体验课', time: '3/27 周四 14:00', blockReason: '' },
  { id: 'B-2', type: '团课', time: '3/28 周五 19:00', blockReason: '该时段人数已满' },
  { id: 'B-3', type: '正式课', time: '3/30 周日 16:00', blockReason: '该老师当日不可授课' }
]

export const studentTimeline = [
  { id: 'G-1', date: '2026-03-20', title: '课后反馈：扫弦节奏稳定', summary: '老师建议：下节课加入切音技巧训练' },
  { id: 'G-2', date: '2026-03-15', title: '课后反馈：和弦转换更流畅', summary: '作业：录制A小调进行弹唱视频' },
  { id: 'G-3', date: '2026-03-10', title: '阶段总结：出勤良好', summary: '建议每周至少2次打卡练习' }
]

export const coursePackages = [
  {
    id: 'P-1',
    type: '团购体验',
    name: '体验课2节包',
    displayPrice: 99,
    salePrice: 49,
    stock: 320,
    validDays: 30,
    validUntil: '2026-05-31',
    status: '上架',
    soldCount: 128,
    canCancel: true
  },
  {
    id: 'P-2',
    type: '正式课程',
    name: '标准课24节包',
    displayPrice: 3680,
    salePrice: 3280,
    stock: 86,
    validDays: 365,
    validUntil: '2027-03-31',
    status: '上架',
    soldCount: 53,
    canCancel: true
  },
  {
    id: 'P-3',
    type: '一对一',
    name: '冲刺课12节包',
    displayPrice: 5680,
    salePrice: 4980,
    stock: 34,
    validDays: 180,
    validUntil: '2026-12-31',
    status: '下架',
    soldCount: 21,
    canCancel: false
  }
]

export const notifications = [
  { id: 'N-1', title: '未预约提醒', content: '林知夏核销后第3天未预约体验课', time: '10分钟前', read: false, category: '待办提醒' },
  { id: 'N-2', title: '审批待处理', content: '调课申请 A-203 等待教务确认', time: '35分钟前', read: false, category: '审批结果' },
  { id: 'N-3', title: '系统公告', content: '今晚 23:30 将进行数据备份，不影响演示数据', time: '2小时前', read: true, category: '系统公告' },
  { id: 'N-4', title: '触达反馈', content: '赵一诺预约链接已成功送达并点击', time: '3小时前', read: true, category: '触达结果' }
]
