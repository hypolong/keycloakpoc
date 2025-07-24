import logo from './logo.svg';
import './App.css';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import NewComponent from './getdata'; 
import Login from './login'; // 注意路径是否正确
import Mainpage from './mainpage';
import { Navigate } from "react-router-dom";
import ProtectedRoute from './ProtectedRoute';

import Mycom from './mycom';
import Mycomcompany from './mycomcompany';

import RegistrationForm from './RegistrationForm';

// import useAuth from "./ProtectedRoute";

// const ProtectedRoute = ({ children }) => {
//   const { loading, authenticated } = useAuth();

//   if (loading) return null;

//   if (!authenticated) return <Navigate to="/login" />;

//   return children;
// };

function App() {
  // const [text, setText] = useState('初始文本');
 
  // const handleClick = () => {
  //   setText('文本已改变');
  // };
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/test" element={
            <ProtectedRoute>
              <NewComponent />
            </ProtectedRoute>
          } />
        <Route path="/main" element={
          <ProtectedRoute>
              <Mycom />
          </ProtectedRoute>
          } />

          <Route path="/maincom" element={
          <ProtectedRoute>
              <Mycomcompany />
          </ProtectedRoute>
          } />

          <Route path="/userreg" element={
          <ProtectedRoute>
              <RegistrationForm />
          </ProtectedRoute>
          } />
      </Routes>
    </Router>
    
  );
}

export default App;

