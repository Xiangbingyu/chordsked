import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { LayoutDashboard, Calendar, Users, ShoppingBag, BookOpen, Settings, LogOut, GraduationCap, Bell } from 'lucide-react';
import { notifications } from '../lib/mockData';

const Layout = () => {
  const navItems = [
    { to: '/', icon: LayoutDashboard, label: '工作台' },
    { to: '/schedule', icon: Calendar, label: '排课管理' },
    { to: '/students', icon: Users, label: '学员管理' },
    { to: '/teachers', icon: GraduationCap, label: '教师管理' },
    { to: '/orders', icon: ShoppingBag, label: '团购核销' },
    { to: '/teaching', icon: BookOpen, label: '教学中心' },
    { to: '/settings', icon: Settings, label: '系统设置' },
  ];

  const unreadNotifications = notifications.length;

  return (
    <div className="flex h-screen bg-background font-sans text-text-main">
      {/* Sidebar */}
      <aside className="w-64 bg-white shadow-md flex flex-col z-10">
        <div className="p-6 flex items-center gap-3 border-b border-gray-100">
          <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center text-white font-bold shadow-lg shadow-primary/30">C</div>
          <span className="text-xl font-bold text-primary tracking-tight">ChordSked</span>
        </div>
        
        <nav className="flex-1 p-4 space-y-1">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 group ${
                  isActive 
                    ? 'bg-primary/10 text-primary font-medium shadow-sm' 
                    : 'text-text-secondary hover:bg-gray-50 hover:text-primary'
                }`
              }
            >
              <item.icon size={20} className="stroke-[1.5]" />
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="p-4 border-t border-gray-100">
          <button className="flex items-center gap-3 px-4 py-3 w-full text-text-secondary hover:text-error hover:bg-red-50 rounded-xl transition-colors">
            <LogOut size={20} />
            <span>退出登录</span>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-auto bg-background p-8">
        <header className="mb-8 flex justify-between items-center">
            <div>
                <h1 className="text-2xl font-bold text-text-main">欢迎回来, 教务老师</h1>
                <p className="text-text-secondary text-sm mt-1">今天又是充满音乐的一天 🎵</p>
            </div>
            <div className="flex items-center gap-4">
                <button className="p-2 rounded-full hover:bg-white bg-white/50 transition-colors relative group">
                    <span className="absolute top-2 right-2 w-2 h-2 bg-error rounded-full border border-white animate-pulse"></span>
                    <Bell size={20} className="text-text-secondary group-hover:text-primary" />
                    
                    {/* Notification Dropdown (Simple) */}
                    <div className="absolute right-0 top-full mt-2 w-80 bg-white rounded-xl shadow-xl border border-gray-100 p-4 hidden group-hover:block z-50">
                        <h4 className="font-bold mb-3 text-sm">通知中心 ({unreadNotifications})</h4>
                        <div className="space-y-3 max-h-64 overflow-auto custom-scrollbar">
                            {notifications.map(n => (
                                <div key={n.id} className="p-3 bg-gray-50 rounded-lg text-sm hover:bg-gray-100 transition-colors">
                                    <div className="flex justify-between items-center mb-1">
                                        <span className={`text-xs px-1.5 py-0.5 rounded ${
                                            n.type === 'warning' ? 'bg-orange-100 text-orange-600' : 
                                            n.type === 'info' ? 'bg-blue-100 text-blue-600' : 'bg-gray-200 text-gray-600'
                                        }`}>{n.title}</span>
                                        <span className="text-xs text-text-hint">{n.time}</span>
                                    </div>
                                    <p className="text-text-secondary text-xs">{n.content}</p>
                                </div>
                            ))}
                        </div>
                    </div>
                </button>
                <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-primary to-blue-400 flex items-center justify-center text-white font-medium shadow-lg shadow-primary/20 cursor-pointer">
                    JD
                </div>
            </div>
        </header>
        <div className="animate-fade-in-up">
            <Outlet />
        </div>
      </main>
    </div>
  );
};

export default Layout;
