import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import './App.css'
import Shell from './views/Shell'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path='/' element={<Navigate to='/admin/workbench-overview' replace />} />
        <Route path='/:role/:page' element={<Shell />} />
        <Route path='*' element={<Navigate to='/admin/workbench-overview' replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
