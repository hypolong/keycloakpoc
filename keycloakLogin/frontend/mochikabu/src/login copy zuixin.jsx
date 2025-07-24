// src/Login.jsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

import './App.css';
import { Row, Col,Layout, Menu } from "antd";
import Column from 'antd/es/table/Column';

const { Header, Sider, Content, Footer } = Layout;

export default function Login() {
  const [username, setUsername] = useState('for_bff');
  const [password, setPassword] = useState('Hypolong2025');
  const [error,setError] = useState(null);
  const [error_token,setErrorToken] = useState(null);
  const [error_token_exchange,setErrorTokenEx] = useState(null);
  const [error_token_exchange_user,setErrorTokenExUser] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setErrorToken(null);
    setErrorTokenEx(null);
    setErrorTokenExUser(null);
    try {
      const res = await fetch('http://localhost:3010/api/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({"username": username, "password":password }),
        credentials: 'include', 
      });
      if (res.ok) {
        console.log("验证成功");
        // 登录成功，重定向或显示登录成功
        window.location.href = '/main';
      } else {
        const data = await res.json();
        setError(data.message || '登录失败');
      }
    } catch (err) {
      setError('网络错误');
    }
  };
  const redirecctURL=()=>{
    window.location.href = '/test'
  };

  const userVerify = async (e) =>{
    // const res = await fetch('http://localhost:3010/login', {
    //     method: 'POST',
    //     headers: {'Content-Type': 'application/json'},
    //     body: JSON.stringify({"username": username, "password":password }),
    //     // credentials: 'include', 
    //   });  
  axios.post("http://localhost:3010/login",{"username": username, "password":password },{
          headers: {
              'Content-Type': 'application/json;charset=UTF-8'
          },
          withCredentials: true //这里一定要设置，否则后面的验证“是否登录”都会出错
        }).then((response) =>{
          console.log(response.data.user);
          if(response.data.message=="ok"){
            window.location.href = '/main';
          }else{
            setError('用户验证失败');
          }
        }); 
  };
  
  const addUsers = async (e) =>{
    console.log(username);
    const res = await fetch('http://localhost:8080/creatUsersIdP', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({"name": username, "pwd":password }),
        // credentials: 'include', 
      }).then(response => {
    // 处理响应
    return response.text(); // 或者 response.json() 如果你期望的是JSON格式
    })
    .then(data => {
        // 处理返回的字符串或JSON数据
        // console.log(data); // 这里就是从响应体中获取的字符串或JSON数据
        if (data.toString()=="succeed") {
          // 登录成功，重定向或显示登录成功
          // window.location.href = '/main';
          setError('新增成功');
        } else {
          
          setError('新增失败');
        }
    })
  };

  const getToken = async (e) =>{
    console.log(username);
    const res = await fetch('http://localhost:3010/bfftoken', {
        method: 'GET',
        headers: {'Content-Type': 'application/json'},
      }).then(response => {
    // 处理响应
    return response.json(); // 或者 response.json() 如果你期望的是JSON格式
    })
    .then(data => {
        // 处理返回的字符串或JSON数据
        console.log(data); // 这里就是从响应体中获取的字符串或JSON数据
        if (data.message.toString().includes("succeed")) {
          // 登录成功，重定向或显示登录成功
          // window.location.href = '/main';
          setErrorToken(data.token);
        } else {
          setErrorToken('获取失败');
        }
    })
  };

  //bff直接交换token方式
  const exchangeToken = async (e) =>{
    console.log(username);
    const res = await fetch('http://localhost:3010/tokenexchange', {
        method: 'GET',
        headers: {'Content-Type': 'application/json'},
      }).then(response => {
    // 处理响应
    return response.json(); // 或者 response.json() 如果你期望的是JSON格式
    })
    .then(data => {
        // 处理返回的字符串或JSON数据
        // console.log(data); // 这里就是从响应体中获取的字符串或JSON数据
        if (data.message.toString().includes("succeed")) {
          // 登录成功，重定向或显示登录成功
          // window.location.href = '/main';
          setErrorTokenEx(data.tokenB.toString());
        } else {
          
          setErrorTokenEx('获取失败');
        }
    })
  };

  // //之前从后台获取token的方式************************************************
  // const getToken = async (e) =>{
  //   console.log(username);
  //   const res = await fetch('http://localhost:8080/accessTokenGet', {
  //       method: 'POST',
  //       headers: {'Content-Type': 'application/json'},
  //       body: JSON.stringify({"name": username, "pwd":password }),
  //       // credentials: 'include', 
  //     }).then(response => {
  //   // 处理响应
  //   return response.text(); // 或者 response.json() 如果你期望的是JSON格式
  //   })
  //   .then(data => {
  //       // 处理返回的字符串或JSON数据
  //       // console.log(data); // 这里就是从响应体中获取的字符串或JSON数据
  //       if (data.toString().includes("succeed")) {
  //         // 登录成功，重定向或显示登录成功
  //         // window.location.href = '/main';
  //         setErrorToken(data.replace("succeed",""));
  //       } else {
          
  //         setErrorToken('获取失败');
  //       }
  //   })
  // };

  // //采用后端交换token的方式
  // const exchangeToken = async (e) =>{
  //   console.log(username);
  //   const res = await fetch('http://localhost:8080/exchangeToken', {
  //       method: 'POST',
  //       headers: {'Content-Type': 'application/json'},
  //       body: JSON.stringify({"name": username, "pwd":password }),
  //       // credentials: 'include', 
  //     }).then(response => {
  //   // 处理响应
  //   return response.text(); // 或者 response.json() 如果你期望的是JSON格式
  //   })
  //   .then(data => {
  //       // 处理返回的字符串或JSON数据
  //       // console.log(data); // 这里就是从响应体中获取的字符串或JSON数据
  //       if (data.toString().includes("succeed")) {
  //         // 登录成功，重定向或显示登录成功
  //         // window.location.href = '/main';
  //         setErrorTokenEx(data.replace("succeed",""));
  //       } else {
          
  //         setErrorTokenEx('获取失败');
  //       }
  //   })
  // };

  const exchangeTokenUser = async (e) =>{
    console.log(username);
    const res = await fetch('http://localhost:8080/exchangeTokenUser', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({"name": username, "pwd":password }),
        // credentials: 'include', 
      }).then(response => {
    // 处理响应
    return response.text(); // 或者 response.json() 如果你期望的是JSON格式
    })
    .then(data => {
        // 处理返回的字符串或JSON数据
        // console.log(data); // 这里就是从响应体中获取的字符串或JSON数据
         if (data.toString()=="succeed") {
          // 登录成功，重定向或显示登录成功
          // window.location.href = '/main';
          setErrorTokenExUser('获取成功');
        } else {
          
          setErrorTokenExUser('获取失败');
        }
    })
  };

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Layout style={{ padding: "16px" }}>
        <Header style={{ color: "#fff", fontSize: "20px" ,textAlign: "center" }}>登　录</Header>
        <Content
          style={{
            padding: 0,
            margin: 0,
            background: "#fff",
            minHeight: 280,
            
          }}
        >
            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={4}>
                <label>ユーザー名：</label>
              </Col>
              <Col span={20}>
                <input
                  type="text" placeholder="用户名"
                  value={username} onChange={e => setUsername(e.target.value)}
                  required
                />
              </Col>
            </Row>
            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={4}>
                <label>パスワード：</label>
              </Col>
              <Col span={20}>
                <input
                  type="password" placeholder="密码"
                  value={password} onChange={e => setPassword(e.target.value)}
                  required
                />
              </Col>
            </Row>
            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={10}>
                <button onClick={handleSubmit}>BFFへのログイン</button>
                {error && <p style={{color:'red'}}>{error}</p>}
              </Col>
              <Col span={20}>
                
              </Col>
            </Row>
            <Row>
              <Col span={24}>
                <button onClick={redirecctURL}>他のテスト画面</button>
              </Col>
            </Row>
            <Row>
              <Col span={24}></Col>
            </Row>
            <Row>
              <Col span={24}>
                <button onClick={userVerify}>keycloakでのログイン</button>
              </Col>
            </Row>
            <Row>
              <Col span={24}></Col>
            </Row>
            <Row>
              <Col span={24}>
                <button onClick={addUsers}>keycloakへユーザー追加</button>
                {error && <p style={{color:'red'}}>{error}</p>}
              </Col>
            </Row>
            <Row>
              <Col span={24}></Col>
            </Row>
            <Row>
              <Col span={24}>
                <button onClick={getToken}>keycloak アクセストークンを貰う</button>
                {error_token && <p style={{color:'blue'}}>{error_token}</p>}
              </Col>
              <Col span={24}></Col>
              <Col span={24}>
                <button onClick={exchangeToken}>keycloak Token Exchange</button>
                {error_token_exchange && <p style={{color:'green'}}>{error_token_exchange}</p>}
              </Col>
              <Col span={24}></Col>
              <Col span={24}>
                <button onClick={exchangeTokenUser}>keycloak Token Exchange for user</button>
                {error_token_exchange_user && <p style={{color:'blue'}}>{error_token_exchange_user}</p>}
              </Col>
            </Row>
            <Row>
              <Col span={24}></Col>
            </Row>
            
        </Content>

        <Footer style={{ textAlign: "center" }}>
          ©2025 Created by hypolong
        </Footer>
      </Layout>
    </Layout>
    
  );
}
