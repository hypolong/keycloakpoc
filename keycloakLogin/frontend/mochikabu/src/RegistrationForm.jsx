import React from 'react';
import { Form, Input, Button, Checkbox } from 'antd';
import axios from 'axios';

const RegistrationForm = () => {
  const onFinish = (values) => {
    console.log('Received values of form: ', values.enterprise_code);
    try {
        // axios.post('http://localhost:3010/adduser',
        //   {
        //     "enterprise_code":values.enterprise_code,
        //     "name": values.name,
        //     "pwd": values.pwd,
        //     "phone": values.phone,
        //     "email": values.email,
        //   },{
        //           headers: {
        //               'Content-Type': 'application/json;charset=UTF-8'
        //           }
        //   }).then((response) =>{       
        //     console.log('response data: ', response.data);
        //   })
         const  idSet= Math.floor(Math.random() * (200000 - 1 + 1)) ;
          fetch('http://localhost:3010/adduser', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
         
          body: JSON.stringify({
            "company_code":values.enterprise_code,
            "username": values.username,
            "password": values.pwd,
            "phone": values.phone,
            "email": values.email,
            "firstname": values.name,
            "lastname": " ",
            "firstname": values.name,
            "kaiin_code":values.username + idSet,
            "id": idSet,
            "auth_email": values.email,
          }) // 发送JSON格式的数据
        })
        .then(response => response.text())
        .then(data => {
          console.log(data);
          alert("regist succed,turn to login ?");
          //succed, login to user for test ************
          window.location.href = 'http://localhost:3010/login';

        }) // 打印服务器的响应
        .catch(error => console.error('Error:', error));
    } catch (error) {
      console.log('Error fetching user data:', error.message);
    }
  };
 
  return (
    <Form
      name="registration"
      onFinish={onFinish}
      initialValues={{ remember: true }}
      autoComplete="off"
    >
      <Form.Item
        label="企業コード："
        name="enterprise_code"
        rules={[{ required: true, message: 'Please input your company code!' }]}
        labelCol={{ span: 4 }} // 设置 label 宽度为 4/24 网格
        wrapperCol={{ span: 20 }} // 设置控件宽度为 20/24 网格
      >
        <Input  style={{width:200}}/>
      </Form.Item>

      <Form.Item
        label="氏名："
        name="name"
        rules={[{ required: true, message: 'Please input your name!' }]}
        labelCol={{ span: 4 }} // 设置 label 宽度为 4/24 网格
        wrapperCol={{ span: 20 }} // 设置控件宽度为 20/24 网格
      >
        <Input  style={{width:200}}/>
      </Form.Item>

      <Form.Item
        label="ユーザID："
        name="username"
        rules={[{ required: true, message: 'Please input your userID!' }]}
        labelCol={{ span: 4 }} // 设置 label 宽度为 4/24 网格
        wrapperCol={{ span: 20 }} // 设置控件宽度为 20/24 网格
      >
        <Input  style={{width:200}}/>
      </Form.Item>

      <Form.Item
        label="メールアドレス："
        name="email"
        rules={[{ required: true, message: 'Please input your username!' }]}
        labelCol={{ span: 4 }} // 设置 label 宽度为 4/24 网格
        wrapperCol={{ span: 20 }} // 设置控件宽度为 20/24 网格
      >
        <Input  style={{width:200}}/>
      </Form.Item>

      <Form.Item
        label="携帯："
        name="phone"
        rules={[{ required: true, message: 'Please input your phone number!' }]}
        labelCol={{ span: 4 }} // 设置 label 宽度为 4/24 网格
        wrapperCol={{ span: 20 }} // 设置控件宽度为 20/24 网格
      >
        <Input  style={{width:200}}/>
      </Form.Item>

      <Form.Item
        label="パスワード："
        name="pwd"
        rules={[{ required: true, message: 'Please input your password!' }]}
        labelCol={{ span: 4 }} // 设置 label 宽度为 4/24 网格
        wrapperCol={{ span: 20 }} // 设置控件宽度为 20/24 网格
      >
        <Input.Password style={{width:200}}/>
      </Form.Item>
      
      <Form.Item
        label="パスワード再入力："
        name="confirmPassword"
        dependencies={['pwd']}
        hasFeedback
        rules={[
          { required: true, message: 'Please confirm your password!' },
          ({ getFieldValue }) => ({
            validator(_, value) {
              if (!value || getFieldValue('pwd') === value) {
                return Promise.resolve();
              }
              return Promise.reject(new Error('The two passwords that you entered do not match!'));
            },
          }),
        ]}
        labelCol={{ span: 4 }} // 设置 label 宽度为 4/24 网格
        wrapperCol={{ span: 20 }} // 设置控件宽度为 20/24 网格
      >
        <Input.Password style={{width:200}}/>
      </Form.Item>
      
      <Form.Item name="remember" valuePropName="checked"
        wrapperCol={{ offset: 4, span: 20 }}
      >
        <Checkbox>Remember me</Checkbox>
      </Form.Item>
      
      <Form.Item
        wrapperCol={{ offset: 4, span: 20 }}
      >
       
        <Button type="primary" htmlType="submit">Register</Button>
      </Form.Item>
    </Form>
  );
};
 
export default RegistrationForm;