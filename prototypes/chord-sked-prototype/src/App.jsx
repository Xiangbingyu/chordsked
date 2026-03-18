import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Schedule from './pages/Schedule';
import Students from './pages/Students';
import StudentDetail from './pages/StudentDetail';
import Orders from './pages/Orders';
import Teaching from './pages/Teaching';
import Teachers from './pages/Teachers';
import Settings from './pages/Settings';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Dashboard />} />
          <Route path="schedule" element={<Schedule />} />
          <Route path="students" element={<Students />} />
          <Route path="students/:id" element={<StudentDetail />} />
          <Route path="teachers" element={<Teachers />} />
          <Route path="orders" element={<Orders />} />
          <Route path="teaching" element={<Teaching />} />
          <Route path="settings" element={<Settings />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
