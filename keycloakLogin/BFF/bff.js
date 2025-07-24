const axios = require('axios');
const qs = require('qs');
const cors = require('cors');
const express = require('express');
const session = require('express-session');
const { Issuer, generators } = require('openid-client');  // ✅ 正确写法
const app = express();
const port = 3010;

// 中间件：允许处理 JSON 请求
app.use(express.json());

//设置session
app.use(session({
  name: 'my.sid',
  secret: 'secret123',
  resave: false,
  saveUninitialized: true,
  cookie: { 
    secure: false,        // ✅ 本地开发必须为 false
    httpOnly: true,
    sameSite: "lax"       // ✅ 默认即可，若跨站可设为 "none"
   },// 生产环境需开启 secure: true
}));

//允许跨域,一定必须再session设置之后
app.use(cors({
  origin: 'http://localhost:3000', // 前端地址
  httpOnly: false,
  credentials: true                // ✅ 允许携带 cookie
}));

// Keycloak 配置
const KEYCLOAK_BASE_URL = "http://localhost:8090";
const REALM = "myrealm";
const CLIENT_ID = "mochikabu_bff";
const CLIENT_SECRET = "jIM90uY8z4bGCOhJIeXbYl5hD6kDKEyV"; // 如果 client 是 confidential 类型
const CLIENT_B_ID = 'kaiin_bff';

//获取这个BFF的token
app.get("/bfftoken", async (req, res) => {
  try {
    const tokenUrl = `${KEYCLOAK_BASE_URL}/realms/${REALM}/protocol/openid-connect/token`;

    const data = qs.stringify({
      grant_type: 'client_credentials',
      client_id: CLIENT_ID,
      client_secret: CLIENT_SECRET,
    });

    const response = await axios.post(tokenUrl, data, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });
    // console.log(response.data.access_token);
    const payload = response.data.access_token.split('.')[1]; // 第二段是 payload
    const decoded = Buffer.from(payload, 'base64').toString('utf8');
    console.log(JSON.parse(decoded));
    res.json({ message: "succeed", token:decoded});
  } catch (err) {
    console.error(err.response?.data || err.message);
    res.status(401).json({ message: "ng" });
  }
});

//获取kaiin BFF的token
app.get("/tokenexchange", async (req, res) => {
  try {
      const tokenUrl = `${KEYCLOAK_BASE_URL}/realms/${REALM}/protocol/openid-connect/token`;

      const dataA = qs.stringify({
        grant_type: 'password',
        client_id: CLIENT_ID,
        client_secret: CLIENT_SECRET,
        username:'test_user1',
        password:'Deloitte2017',
        // scope:'openid audience-kaiin'
      });

      const responseA = await axios.post(tokenUrl, dataA, {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
      });

    console.log("token_before:");
    const payloadA = responseA.data.access_token.split('.')[1]; // 第二段是 payload
    const decodedA = Buffer.from(payloadA, 'base64').toString('utf8');
    console.log(JSON.parse(decodedA));
    
      //获取kaiinbff的token
      const dataB = qs.stringify({
        grant_type: 'urn:ietf:params:oauth:grant-type:token-exchange',
        client_id: CLIENT_ID,
        client_secret: CLIENT_SECRET,
        subject_token: responseA.data.access_token,
        subject_token_type: 'urn:ietf:params:oauth:token-type:access_token',
        requested_token_type: 'urn:ietf:params:oauth:token-type:access_token',
        audience: CLIENT_B_ID,  // 这是 token exchange 的关键参数
        scope:'audience-kaiin'
      });
    console.log("After exchange:");
    const responseB = await axios.post(tokenUrl, dataB, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    });
    
    const payloadB = responseB.data.access_token.split('.')[1]; // 第二段是 payload
    const decodedB = Buffer.from(payloadB, 'base64').toString('utf8');
    console.log(JSON.parse(decodedB));
    res.json({ message: "succeed", tokenA:decodedA,tokenB:decodedB});
  } catch (err) {
    console.error(err.response?.data || err.message);
    res.status(401).json({ message: "ng" });
  }
});

// 登录接口,BFF想keycloak无界面认证
app.post("/login", async (req, res) => {
  console.log("1请求来了");
  const { username, password } = req.body;
  console.log(username);
  try {
    const params = new URLSearchParams();
    params.append("grant_type", "password");
    params.append("client_id", CLIENT_ID);
    params.append("client_secret", CLIENT_SECRET); // 若为 public client，可省略
    params.append("username", username);
    params.append("password", password);

    const tokenResponse = await axios.post(
      `${KEYCLOAK_BASE_URL}/realms/${REALM}/protocol/openid-connect/token`,
      params,
      { headers: { "Content-Type": "application/x-www-form-urlencoded" } }
    );

    const tokenData = tokenResponse.data;

    // 在 session 中保存用户认证信息
    req.session.user = {
      username,
      access_token: tokenData.access_token,
      refresh_token: tokenData.refresh_token,
    };
    res.json({ message: "ok", user: req.session.user });
  } catch (err) {
    console.error(err.response?.data || err.message);
    res.status(401).json({ message: "ng" });
  }
});
// 登录接口，BFF仅仅生成重定向URL返回给前端
app.post("/loginURL", async (req, res) => {
  console.log("1请求来了");
  const { username, password } = req.body;
  console.log(username);
  try {
    const url = client.authorizationUrl({
      scope: 'openid profile email',
      state: 'random_state', // 可改为更安全的随机生成
    });
  res.redirect(url);
    res.json({ message: "ok", user: req.session.user });
  } catch (err) {
    console.error(err.response?.data || err.message);
    res.status(401).json({ message: "ng" });
  }
});

// 获取当前 session 用户
app.get("/me", (req, res) => {
  // console.log("cookie:", req.headers.cookie);
  // console.log("session:", req.session);
  console.log("sessionID:", req.sessionID);
  console.log("req.session.user:", req.session.user);
  if (req.session.user) {
    console.log("2返回true");
    res.json({ login: true, user: req.session.user });
  } else {
    console.log("2返回false");
    // res.json({ login: true, user: req.session.user });
    res.status(401).json({ login: false });
  }
});

// 退出登录
app.post("/logout", (req, res) => {
  console.log("logout");
  req.session.destroy(() => {
    res.clearCookie('my.sid', { path: '/' });
    res.json({ message: "已退出登录" });
  });
});

// 登录回调(暂时无用)
app.get('/callback', async (req, res, next) => {
  console.log("进入重定向");
  const params = client.callbackParams(req);

  try {
    const tokenSet = await client.callback('http://localhost:3010/callback', params, {
      code_verifier: req.session.code_verifier,
    });

    req.session.tokenSet = tokenSet;
    req.session.userinfo = await client.userinfo(tokenSet.access_token);

    res.redirect('/test');
  } catch (err) {
    next(err);
  }
});

// 受保护页面
app.get('/test', (req, res) => {
  if (!req.session.userinfo) return res.redirect('/login');

  res.send(`<h1>欢迎，${req.session.userinfo.name || req.session.userinfo.preferred_username}</h1>`);
});

// 登出
app.get('/logout', (req, res) => {
  req.session.destroy(() => {
    res.redirect('/');
  });
});

// BFF 接口：整合两个后端服务
app.get('/api/token', async (req, res) => {
  
  const userName = req.query.name;
  const userPWD = req.query.pwd;

  if (!userName) {
    return res.status(400).json({ error: 'Missing user ID' });
  }

  let combinedData={url: '',uid:''};
  try {
      axios.post('http://localhost:8080/getURL',{"url":"http://localhost:3001"},{
                headers: {
                    'Content-Type': 'application/json;charset=UTF-8'
                }
        }).then((response) =>{       
          combinedData.url=response.data;
          axios.post('http://localhost:8080/getKey',{
              "id": "",
              "name": userName,
              "pwd": userPWD
          },{
          headers: {
              'Content-Type': 'application/json;charset=UTF-8'
          }
          }).then((response) =>{
            combinedData.uid=response.data;
            res.json(combinedData);
          });

        })
  } catch (error) {
    console.error('Error fetching user data:', error.message);
    res.status(500).json({ error: 'Failed to fetch user data' });
  }
});

//中转请求
app.get('/api/user-info', async (req, res) => {
  try {
    // console.log(req.query.token);
    axios.get('http://localhost:8080/getUserInfo?token=' + req.query.token).then((response) =>{ 
      res.json(response.data);
    });
  } catch (error) {
  console.error('Error fetching user data:', error.message);
  res.status(500).json({ error: 'Failed to fetch user data' });
  }
});

//登录请求，暂时登录到后端，后续重定向到OIDC
app.post('/api/login', async (req, res) => {
  try {
    axios.post('http://localhost:8080/userVerify',
      {
          "name": req.body.username,
          "pwd": req.body.password
      },{
        headers: {
            'Content-Type': 'application/json;charset=UTF-8'
        }
      }

    ).then((response) =>{ 
      if(response.data==1){
        res.json({"result":response.data});
        //暂时设置session name
        req.session.token=1;
        console.log(req.session.token);
      }else{
        res.status(500).json({ error: 'Failed to fetch user data' });
      }
      // console.log(response.data);
    });
  } catch (error) {
  console.error('Error fetching user data:', error.message);
  res.status(500).json({ error: 'Failed to fetch user data' });
  }
});

// 启动服务器
app.listen(port, () => {
  console.log(`BFF server is running at http://localhost:${port}`);
});
