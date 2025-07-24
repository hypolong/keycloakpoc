import { useEffect, useState } from 'react';
import axios from 'axios';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCoffee,faRectangleTimes,faRegistered } from '@fortawesome/free-solid-svg-icons';
import logo from './logo.svg';
import './App.css';

export default function Login() {
const [user, setUser] = useState(null);

useEffect(() => {
  axios.get('http://localhost:3010/me', { withCredentials: true })
    .then(res => setUser(res.data))
    .catch(() => setUser(null));
}, []);

const handleLogin = () => {
  window.location.href = 'http://localhost:3010/login';
};
const handleLoginCompany = () => {
  window.location.href = 'http://localhost:3010/logincompany';
};

const handleLogout = () => {
  window.location.href = 'http://localhost:3010/logout';
};

  return (
     <div className="App">
      <header className="App-header">
      <img src={logo} className="App-logo" alt="logo" />
      <h1>OIDC POC</h1>
      <h2>FrontEnd(React) + BFF(node.js) + BackEnd(Springboot) + OIDC(Keycloak) </h2>
      
      {user ? (
        <div>
          <p>Welcome, {user.name || user.preferred_username}</p>
          <button onClick={handleLogout}>Logout</button>
        </div>
      ) : (
        <button onClick={handleLogin} style={{ height:'50px',backgroundColor:'000000', cursor: 'pointer' }}><FontAwesomeIcon icon={faRegistered} /> 会員ログイン</button>
      )}
      <div>
        <p>OR</p>
      </div>
       {user ? (
        <div>
          <p>Welcome, {user.name || user.preferred_username}</p>
          <button onClick={handleLogout}>Logout</button>
        </div>
      ) : (
        <button onClick={handleLoginCompany} style={{ height:'50px',backgroundColor:'000000', cursor: 'pointer' }}><FontAwesomeIcon icon={faRegistered} /> 企業ログイン</button>
      )}
      </header>
    </div>
    
  );
}