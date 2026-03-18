export const students = [
  { 
    id: 1, 
    name: '张小明', 
    age: 8, 
    parent: '张伟', 
    phone: '13800138001', 
    balance: 12, 
    level: '初级', 
    joinedDate: '2025-01-15',
    instrument: '钢琴',
    records: [
      { id: 101, date: '2025-03-01', type: '上课', content: '学习了《小星星》第一段', teacher: '刘老师', feedback: '指法需要加强' },
      { id: 102, date: '2025-02-22', type: '上课', content: '复习C大调音阶', teacher: '刘老师', feedback: '节奏感很好' },
      { id: 103, date: '2025-02-15', type: '作业', content: '提交了音阶练习视频', teacher: '刘老师', feedback: '通过' }
    ]
  },
  { 
    id: 2, 
    name: '李子涵', 
    age: 10, 
    parent: '李芳', 
    phone: '13900139002', 
    balance: 5, 
    level: '中级', 
    joinedDate: '2024-11-20',
    instrument: '吉他',
    records: []
  },
  { 
    id: 3, 
    name: '王浩宇', 
    age: 7, 
    parent: '王强', 
    phone: '13700137003', 
    balance: 20, 
    level: '初级', 
    joinedDate: '2025-02-10',
    instrument: '架子鼓',
    records: []
  },
  { 
    id: 4, 
    name: '陈欣怡', 
    age: 9, 
    parent: '陈丽', 
    phone: '13600136004', 
    balance: 0, 
    level: '体验', 
    joinedDate: '2025-03-05',
    instrument: '钢琴',
    records: []
  },
];

export const teachers = [
  { 
    id: 1, 
    name: '刘老师', 
    instrument: '钢琴', 
    type: '正式', 
    capacity: 1, 
    color: 'bg-blue-100 border-blue-300',
    phone: '13811112222',
    workingHours: '周一至周五 10:00-18:00, 周六 09:00-12:00',
    bio: '中央音乐学院毕业，10年教龄，擅长少儿钢琴启蒙。'
  },
  { 
    id: 2, 
    name: '陈老师', 
    instrument: '吉他', 
    type: '团课', 
    capacity: 5, 
    color: 'bg-green-100 border-green-300',
    phone: '13933334444',
    workingHours: '周二至周日 13:00-21:00',
    bio: '独立音乐人，教学风格幽默风趣。'
  },
  { 
    id: 3, 
    name: '赵老师', 
    instrument: '架子鼓', 
    type: '正式', 
    capacity: 1, 
    color: 'bg-orange-100 border-orange-300',
    phone: '13755556666',
    workingHours: '周一至周五 14:00-20:00',
    bio: '曾任知名乐队鼓手，注重基本功训练。'
  },
];

export const orders = [
  { id: 'ORD-20250301-001', platform: '抖音', studentName: '赵雷', phone: '15800158001', course: '吉他体验课', price: 9.9, status: '未核销', code: '887766' },
  { id: 'ORD-20250301-002', platform: '美团', studentName: '孙艺珍', phone: '15900159002', course: '钢琴试听', price: 19.9, status: '已核销', code: '112233' },
  { id: 'ORD-20250302-003', platform: '抖音', studentName: '周杰', phone: '13500135003', course: '架子鼓体验', price: 9.9, status: '未核销', code: '554433' },
];

// Initial schedule data for the current week
const today = new Date();
const getDayOffset = (offset) => {
  const date = new Date(today);
  date.setDate(today.getDate() + offset);
  return date.toISOString().split('T')[0];
}

export const schedule = [
  { id: 1, teacherId: 1, date: getDayOffset(0), time: '10:00', type: '一对一', studentId: 1, status: '已排课' },
  { id: 2, teacherId: 2, date: getDayOffset(0), time: '14:00', type: '团课', studentId: null, students: [2, 3], status: '已排课' },
  { id: 3, teacherId: 1, date: getDayOffset(1), time: '16:00', type: '一对一', studentId: 2, status: '已排课' },
  { id: 4, teacherId: 3, date: getDayOffset(2), time: '19:00', type: '一对一', studentId: 1, status: '已排课' },
];

export const homework = [
  { id: 1, studentId: 1, teacherId: 1, title: '《小星星》变奏曲练习', type: '视频', status: '待批改', submittedAt: '2025-03-05 20:30' },
  { id: 2, studentId: 2, teacherId: 2, title: 'C大调音阶爬格子', type: '音频', status: '已批改', submittedAt: '2025-03-04 18:15', feedback: '节奏稳了很多，注意手指按弦力度。' },
  { id: 3, studentId: 3, teacherId: 2, title: '扫弦节奏型A', type: '视频', status: '未提交', submittedAt: null },
];

export const notifications = [
    { id: 1, title: '上课提醒', content: '张小明明天的钢琴课将于 10:00 开始，请提醒家长准时送达。', time: '10分钟前', type: 'warning' },
    { id: 2, title: '作业未交', content: '王浩宇尚未提交本周架子鼓作业，请跟进。', time: '1小时前', type: 'info' },
    { id: 3, title: '系统更新', content: '系统将于今晚 02:00 进行例行维护。', time: '2小时前', type: 'system' },
];
