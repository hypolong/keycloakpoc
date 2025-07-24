// src/ProtectedRoute.jsx
import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';

export default function ProtectedRoute({ children }) {
  const [isAuthenticated, setIsAuthenticated] = useState(null); // null = 未知

  useEffect(() => {
    // 检查登录状态（示例：向 BFF 发请求）
    fetch('http://localhost:3010/mecom', {
      method: "GET",
      credentials: 'include'
    })
      .then(res => setIsAuthenticated(res.ok))
      .catch(() => setIsAuthenticated(false));
  }, []);

  if (isAuthenticated === null) return <p>正在加载...需要登录</p>;
  if (!isAuthenticated) return <Navigate to="/" />;

  return children;
}
