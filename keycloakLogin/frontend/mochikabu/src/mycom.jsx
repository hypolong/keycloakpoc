import React , { useEffect,useState } from "react";
import { Layout, Menu,Typography } from "antd";

import {
  UserOutlined,
  LaptopOutlined,
  NotificationOutlined,
} from "@ant-design/icons";
import useAuth from "./ProtectedRoute";
import { Spin } from "antd";
import axios from 'axios';
import Cookies from 'js-cookie';

const { Header, Sider, Content, Footer } = Layout;
const { Text } = Typography;
const items = [
  {
    key: '1',
    icon: <UserOutlined />,
    label: 'Topページ',
  },
  {
    key: '2',
    icon: <LaptopOutlined />,
    label: '株一覧',
  },
  {
    key: '3',
    icon: <NotificationOutlined />,
    label: '設定',
  },
  ,
  {
    key: '4',
    icon: <NotificationOutlined />,
    label: 'ログアウト',
  },
  {
    key: '5',
    icon: <NotificationOutlined />,
    label: '会員ログイン(OTP)',
  },
  ,
  {
    key: '6',
    icon: <NotificationOutlined />,
    label: '会員ログイン(OIDC)',
  },
];

const handleClick = (e) => {
    if(e.key==1){
        window.location.href = 'http://localhost:3000/';
    }
    if(e.key==4){
        window.location.href = 'http://localhost:3010/logout';
    }
    if(e.key==5){
        //获取token并重定向到会员系统
        window.location.href = 'http://localhost:3010/loginotp';
    }
    if(e.key==6){
        //获取token并重定向到会员系统
        window.location.href = 'http://localhost:3010/loginimpersonate';
    }
  };



const HomePage = () => {
  const [userInfo, setUserInfo] = useState(null);

  useEffect(() => {
    const userInfoStr = Cookies.get('user_info');
    if (userInfoStr) {
      try {
        const parsed = JSON.parse(userInfoStr);
        setUserInfo(parsed);
      } catch (e) {
        console.error('ユーザ情報取得してません:', e);
      }
    }
  }, []);
  

  return (
    <Layout style={{ minHeight: "100vh" }}>
      {/* 顶部导航栏 */}
      <Header style={{ color: "#fff", fontSize: "20px" }}>
        shin mochikabu system
      </Header>

      {/* 主体区域 */}
      <Layout>
        {/* 左侧导航栏 */}
        <Sider width={200} theme="dark">
          <Menu
            mode="inline"
            defaultSelectedKeys={["1"]}
            style={{ height: "100%", borderRight: 0 }}
            items={items}
            onClick={handleClick}
          />
        </Sider>

        {/* 内容区域 */}
        <Layout style={{ padding: "16px" }}>
          <Content
            style={{
              padding: 24,
              margin: 0,
              background: "#fff",
              minHeight: 280,
              fontSize:48
            }}
          >
           これは会員TOP画面です～

          <div style={{ textAlign: 'left', paddingLeft: 16 }}>
          {userInfo ? (
            <Text strong>こんにちは，{userInfo.name || userInfo.username} さん</Text>
          ) : (
            <Text type="secondary">ログインユーザー未取得</Text>
          )}
        </div>
          </Content>

          <Footer style={{ textAlign: "center" }}>
            ©2025 Created by HYPOLONG
          </Footer>
        </Layout>
      </Layout>
    </Layout>
  );
};

export default HomePage;
