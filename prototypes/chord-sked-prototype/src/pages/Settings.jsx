import React, { useState } from 'react';
import { Settings as SettingsIcon, Layout, Box, Users, Bell } from 'lucide-react';

const Settings = () => {
  const [activeTab, setActiveTab] = useState('general');

  const tabs = [
    { id: 'general', label: '通用设置', icon: SettingsIcon },
    { id: 'courses', label: '课程设置', icon: Box },
    { id: 'classrooms', label: '教室管理', icon: Layout },
    { id: 'roles', label: '角色权限', icon: Users },
    { id: 'notifications', label: '通知配置', icon: Bell },
  ];

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 flex h-[calc(100vh-8rem)] animate-in fade-in duration-500 overflow-hidden">
        {/* Sidebar */}
        <div className="w-64 border-r border-gray-100 bg-gray-50/50 p-4">
            <h2 className="text-xl font-bold mb-6 px-4">系统设置</h2>
            <nav className="space-y-1">
                {tabs.map(tab => (
                    <button
                        key={tab.id}
                        onClick={() => setActiveTab(tab.id)}
                        className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all text-sm font-medium ${
                            activeTab === tab.id 
                                ? 'bg-white text-primary shadow-sm ring-1 ring-gray-100' 
                                : 'text-text-secondary hover:bg-gray-100 hover:text-text-main'
                        }`}
                    >
                        <tab.icon size={18} />
                        {tab.label}
                    </button>
                ))}
            </nav>
        </div>

        {/* Content */}
        <div className="flex-1 p-8 overflow-auto custom-scrollbar">
            {activeTab === 'general' && (
                <div className="max-w-2xl space-y-6">
                    <h3 className="text-lg font-bold mb-4">机构基础信息</h3>
                    <div className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-text-secondary mb-1">机构名称</label>
                            <input type="text" defaultValue="ChordSked 音乐教育中心" className="w-full p-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-primary/20 outline-none" />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-text-secondary mb-1">联系电话</label>
                            <input type="text" defaultValue="010-88888888" className="w-full p-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-primary/20 outline-none" />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-text-secondary mb-1">机构地址</label>
                            <input type="text" defaultValue="北京市朝阳区音乐产业园 A座 101" className="w-full p-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-primary/20 outline-none" />
                        </div>
                        <button className="bg-primary text-white px-6 py-2 rounded-xl hover:bg-blue-900 transition-colors">保存更改</button>
                    </div>
                </div>
            )}

            {activeTab === 'courses' && (
                <div className="space-y-6">
                    <div className="flex justify-between items-center mb-4">
                        <h3 className="text-lg font-bold">课程类型管理</h3>
                        <button className="text-primary text-sm font-medium hover:underline">+ 新增课程类型</button>
                    </div>
                    <div className="grid gap-4">
                        {['钢琴一对一', '吉他团课', '架子鼓基础班', '声乐启蒙'].map((course, i) => (
                            <div key={i} className="flex justify-between items-center p-4 border border-gray-100 rounded-xl hover:shadow-sm bg-white">
                                <span className="font-medium">{course}</span>
                                <div className="flex gap-4 text-sm">
                                    <button className="text-text-secondary hover:text-primary">编辑</button>
                                    <button className="text-text-secondary hover:text-error">删除</button>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}
             
             {/* Placeholder for other tabs */}
             {['classrooms', 'roles', 'notifications'].includes(activeTab) && (
                 <div className="flex flex-col items-center justify-center h-full text-text-hint">
                     <SettingsIcon size={48} className="mb-4 opacity-20" />
                     <p>该模块正在开发中...</p>
                 </div>
             )}
        </div>
    </div>
  );
};

export default Settings;
