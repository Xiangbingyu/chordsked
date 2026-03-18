import React, { useState } from 'react';
import { Upload, CheckCircle, Clock, Search } from 'lucide-react';
import { orders as initialOrders } from '../lib/mockData';

const Orders = () => {
  const [orders, setOrders] = useState(initialOrders);

  const handleWriteOff = (id) => {
    setOrders(orders.map(o => o.id === id ? { ...o, status: '已核销' } : o));
    alert('核销成功！已自动创建意向学员档案。');
  };

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 flex flex-col h-[calc(100vh-8rem)] animate-in fade-in duration-500">
        <div className="p-6 border-b border-gray-100 flex justify-between items-center">
            <div>
                <h2 className="text-xl font-bold">团购核销管理</h2>
                <p className="text-sm text-text-secondary mt-1">支持抖音/美团订单导入与核销</p>
            </div>
            <div className="flex gap-4">
                 <div className="relative group">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-text-hint group-focus-within:text-primary transition-colors" size={18} />
                    <input 
                        type="text" 
                        placeholder="输入券码/手机号核销" 
                        className="pl-10 pr-4 py-2 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-secondary/20 w-64 outline-none transition-all placeholder:text-text-hint"
                    />
                </div>
                <button className="flex items-center gap-2 px-4 py-2 bg-green-50 text-green-700 rounded-xl hover:bg-green-100 transition-colors border border-green-200 shadow-sm">
                    <Upload size={18} />
                    <span>导入Excel订单</span>
                </button>
            </div>
        </div>

        <div className="flex-1 overflow-auto custom-scrollbar">
            <table className="w-full text-left">
                <thead className="bg-gray-50/80 backdrop-blur-sm text-text-secondary text-sm sticky top-0 z-10">
                    <tr>
                        <th className="px-6 py-4 font-medium">订单编号</th>
                        <th className="px-6 py-4 font-medium">平台</th>
                        <th className="px-6 py-4 font-medium">学员信息</th>
                        <th className="px-6 py-4 font-medium">购买课程</th>
                        <th className="px-6 py-4 font-medium">实付金额</th>
                        <th className="px-6 py-4 font-medium">券码</th>
                        <th className="px-6 py-4 font-medium">状态</th>
                        <th className="px-6 py-4 font-medium text-right">操作</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                    {orders.map(order => (
                        <tr key={order.id} className="group hover:bg-orange-50/20 transition-colors">
                            <td className="px-6 py-4 text-sm font-mono text-text-secondary">{order.id}</td>
                            <td className="px-6 py-4">
                                <span className={`px-2 py-1 rounded text-xs font-bold ${
                                    order.platform === '抖音' ? 'bg-black text-white' : 'bg-yellow-400 text-black'
                                }`}>
                                    {order.platform}
                                </span>
                            </td>
                            <td className="px-6 py-4">
                                <div className="font-bold text-text-main">{order.studentName}</div>
                                <div className="text-xs text-text-hint">{order.phone}</div>
                            </td>
                            <td className="px-6 py-4 text-text-main">{order.course}</td>
                            <td className="px-6 py-4 font-medium text-orange-600">¥{order.price}</td>
                            <td className="px-6 py-4 font-mono text-lg tracking-wider">{order.code}</td>
                            <td className="px-6 py-4">
                                {order.status === '已核销' ? (
                                    <span className="flex items-center gap-1 text-success text-sm font-medium bg-green-50 px-2 py-1 rounded-full w-fit">
                                        <CheckCircle size={14} /> 已核销
                                    </span>
                                ) : (
                                    <span className="flex items-center gap-1 text-warning text-sm font-medium bg-yellow-50 px-2 py-1 rounded-full w-fit">
                                        <Clock size={14} /> 待核销
                                    </span>
                                )}
                            </td>
                            <td className="px-6 py-4 text-right">
                                {order.status === '未核销' && (
                                    <button 
                                        onClick={() => handleWriteOff(order.id)}
                                        className="bg-secondary text-white px-3 py-1.5 rounded-lg text-sm font-medium hover:bg-orange-600 shadow-sm transition-colors active:scale-95"
                                    >
                                        核销
                                    </button>
                                )}
                                {order.status === '已核销' && (
                                     <button className="text-primary text-sm hover:underline font-medium">预约体验</button>
                                )}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    </div>
  );
};

export default Orders;
