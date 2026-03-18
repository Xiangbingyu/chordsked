import React from 'react';
import { Users, Calendar, ShoppingBag, BookOpen } from 'lucide-react';
import { students, orders, schedule, homework } from '../lib/mockData';

const StatCard = ({ title, value, icon: Icon, color, subtext }) => (
  <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex items-start justify-between hover:shadow-md transition-shadow">
    <div>
      <p className="text-text-secondary text-sm font-medium">{title}</p>
      <h3 className="text-3xl font-bold mt-2 text-text-main">{value}</h3>
      {subtext && <p className="text-xs text-text-hint mt-1">{subtext}</p>}
    </div>
    <div className={`p-3 rounded-xl shadow-lg shadow-gray-200 ${color}`}>
      <Icon size={24} className="text-white" />
    </div>
  </div>
);

const Dashboard = () => {
  const pendingOrders = orders.filter(o => o.status === '未核销').length;
  // Mock logic for today's classes count based on mock data logic
  const todayClasses = schedule.length; 
  const pendingHomework = homework.filter(h => h.status === '待批改').length;

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard title="在读学员" value={students.length} icon={Users} color="bg-primary" subtext="本月新增 +2" />
        <StatCard title="今日课程" value={todayClasses} icon={Calendar} color="bg-secondary" subtext="待开始 2 节" />
        <StatCard title="待核销订单" value={pendingOrders} icon={ShoppingBag} color="bg-orange-400" subtext="来自抖音/美团" />
        <StatCard title="待批改作业" value={pendingHomework} icon={BookOpen} color="bg-green-500" subtext="需及时反馈" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-lg font-bold">今日课程概览</h2>
            <button className="text-sm text-primary hover:underline">查看全部</button>
          </div>
          <div className="space-y-4">
            {schedule.slice(0, 3).map(cls => (
                <div key={cls.id} className="flex items-center p-4 rounded-xl bg-gray-50 border border-gray-100 hover:border-primary/30 transition-colors group cursor-pointer">
                    <div className="w-16 text-center">
                        <span className="block text-lg font-bold text-text-main group-hover:text-primary transition-colors">{cls.time}</span>
                    </div>
                    <div className="w-1 h-10 bg-gray-200 rounded-full mx-4 group-hover:bg-primary transition-colors"></div>
                    <div className="flex-1">
                        <h4 className="font-bold text-text-main">{cls.type}</h4>
                        <p className="text-sm text-text-secondary">
                            {cls.studentId ? '学员: ' + students.find(s => s.id === cls.studentId)?.name : '团课 (' + cls.students?.length + '人)'}
                        </p>
                    </div>
                    <span className="px-3 py-1 rounded-full text-xs font-medium bg-blue-50 text-blue-600 border border-blue-100">
                        {cls.status}
                    </span>
                </div>
            ))}
            {schedule.length === 0 && <p className="text-text-hint text-center py-8">今日暂无课程</p>}
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
           <h2 className="text-lg font-bold mb-6">快捷入口</h2>
           <div className="grid grid-cols-2 gap-4">
                <button className="p-4 rounded-xl bg-blue-50 text-primary hover:bg-blue-100 hover:shadow-md transition-all flex flex-col items-center gap-2">
                    <div className="w-10 h-10 rounded-full bg-white flex items-center justify-center text-primary shadow-sm">
                        <Calendar size={20} />
                    </div>
                    <span className="text-sm font-medium">快速排课</span>
                </button>
                <button className="p-4 rounded-xl bg-orange-50 text-secondary hover:bg-orange-100 hover:shadow-md transition-all flex flex-col items-center gap-2">
                    <div className="w-10 h-10 rounded-full bg-white flex items-center justify-center text-secondary shadow-sm">
                        <ShoppingBag size={20} />
                    </div>
                    <span className="text-sm font-medium">团购核销</span>
                </button>
                <button className="p-4 rounded-xl bg-green-50 text-green-600 hover:bg-green-100 hover:shadow-md transition-all flex flex-col items-center gap-2">
                     <div className="w-10 h-10 rounded-full bg-white flex items-center justify-center text-green-600 shadow-sm">
                        <Users size={20} />
                    </div>
                    <span className="text-sm font-medium">新增学员</span>
                </button>
                 <button className="p-4 rounded-xl bg-purple-50 text-purple-600 hover:bg-purple-100 hover:shadow-md transition-all flex flex-col items-center gap-2">
                     <div className="w-10 h-10 rounded-full bg-white flex items-center justify-center text-purple-600 shadow-sm">
                        <BookOpen size={20} />
                    </div>
                    <span className="text-sm font-medium">布置作业</span>
                </button>
           </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
