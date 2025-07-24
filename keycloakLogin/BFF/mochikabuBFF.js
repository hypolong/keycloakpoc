const express = require('express');

const session = require('express-session');
const { Issuer } = require('openid-client');
const crypto = require('crypto');
const axios = require('axios');
const qs = require('qs');

require('dotenv').config();

const app = express();
const port = 3010;

app.use(express.json()); // 支持JSON格式的body解析
app.use(express.urlencoded({ extended: true })); // 支持URL编码的body解析

const cors = require('cors');
app.use(cors({
  origin: 'http://localhost:3000',
  credentials: true
}));

app.use(session({
  secret: 'your-session-secret',
  resave: false,
  saveUninitialized: true,
  cookie: { secure: false }, // 本地调试用 false，生产改为 true + HTTPS
}));

let client; // OpenID client 实例-会员
let client_company; // OpenID client 实例-会员

// 初始化 client 并注册路由
(async () => {
  try {
    const issuer = await Issuer.discover(`${process.env.KEYCLOAK_BASE_URL}/.well-known/openid-configuration`);
    client = new issuer.Client({
      client_id: process.env.CLIENT_ID,
      client_secret: process.env.CLIENT_SECRET,
      redirect_uris: [process.env.REDIRECT_URI],
      response_types: ['code'],
    });

    // 🔐 登录发起-------------------会员
    app.get('/login', (req, res) => {
      const state = crypto.randomBytes(16).toString('hex');
      const nonce = crypto.randomBytes(16).toString('hex');

      req.session.state = state;
      req.session.nonce = nonce;

      const url = client.authorizationUrl({
        scope: 'openid profile email',
        state,
        nonce,
        prompt: 'login' 
      });

      res.redirect(url);
      // console.log(url);
    });

    // 🔐 登录回调
    app.get('/callback', async (req, res) => {
      const params = client.callbackParams(req);
      const expectedState = req.session.state;
      const expectedNonce = req.session.nonce;

      if (!expectedState || !expectedNonce) {
        return res.status(400).send('Missing state or nonce in session');
      }

      try {
        const tokenSet = await client.callback(process.env.REDIRECT_URI, params, {
          state: expectedState,
          nonce: expectedNonce,
        });

        const userinfo = await client.userinfo(tokenSet.access_token);

        // 登录成功，保存 session
        req.session.user = userinfo;
        req.session.tokenSet = tokenSet;

        // 安全地选择要暴露的信息（不要整个 userinfo）
        const { email, name, preferred_username } = userinfo;

        // 设置 cookie，有效期 1 小时，HttpOnly 可配置
        res.cookie('user_info', JSON.stringify({
          email,
          name,
          username: preferred_username
        }), {
          httpOnly: false,      // 可被前端 JS 读取
          secure: true,         // 生产环境启用 HTTPS 时使用
          maxAge: 3600 * 1000,  // 1 小时
          sameSite: 'Lax'       // 根据跨域策略配置
        });

        console.log("userinfo:");
        console.log(userinfo);
        console.log("tokenSet:");
        console.log(tokenSet);
        res.redirect('/');
      } catch (err) {
        console.error('OIDC callback error:', err);
        res.status(500).send('Authentication failed');
      }
    });

    app.get('/', (req, res) => {
      res.redirect('http://localhost:3000/main');
    });

    // 🔎 获取当前登录用户
    app.get('/me', (req, res) => {
      console.log("用户：");
      // console.log(req.session.user);
      if (req.session.user) {
        res.json(req.session.user);
      } else {
        console.log("错误");
        res.status(401).json({ error: 'unauthenticated' });
      }
    });

    // 🚪 登出
    app.get('/logout', (req, res) => {
    const idToken = req.session.tokenSet?.id_token;

      if (!idToken) {
        return res.status(400).send('Missing ID token for logout');
      }

      const logoutUrl = `${process.env.KEYCLOAK_BASE_URL}/protocol/openid-connect/logout` +
                        `?id_token_hint=${encodeURIComponent(idToken)}` +
                        `&post_logout_redirect_uri=http://localhost:3000`;

      req.session.destroy(() => {
        res.redirect(logoutUrl);
      });
    });

    // 🚪 登出
    app.get('/logoutcom', (req, res) => {
    const idToken = req.session.tokenSet?.id_token;

      if (!idToken) {
        return res.status(400).send('Missing ID token for logout');
      }

      const logoutUrl = `${process.env.KEYCLOAK_BASE_URL_COM}/protocol/openid-connect/logout` +
                        `?id_token_hint=${encodeURIComponent(idToken)}` +
                        `&post_logout_redirect_uri=http://localhost:3000`;

      req.session.destroy(() => {
        res.redirect(logoutUrl);
      });
    });

    //*******************************企业相关****************************************** */
    const issuer_company = await Issuer.discover(`${process.env.KEYCLOAK_BASE_URL_COM}/.well-known/openid-configuration`);
    client_company = new issuer_company.Client({
      client_id: process.env.CLIENT_ID_COM,
      client_secret: process.env.CLIENT_SECRET_COM,
      redirect_uris: [process.env.REDIRECT_URI_COM],
      response_types: ['code'],
    });
    // 🔐 登录发起-------------------企业
    app.get('/logincompany', (req, res) => {
      const state = crypto.randomBytes(8).toString('hex');
      const nonce = crypto.randomBytes(8).toString('hex');

      req.session.state = state;
      req.session.nonce = nonce;

      const url = client_company.authorizationUrl({
        scope: 'openid profile email',
        state,
        nonce,
        prompt: 'login' 
      });

      res.redirect(url);
      // console.log(url);
    });
    // 🔐 登录回调----------------------企业
    app.get('/callbackcom', async (req, res) => {
      const params = client_company.callbackParams(req);
      const expectedState = req.session.state;
      const expectedNonce = req.session.nonce;

      if (!expectedState || !expectedNonce) {
        return res.status(400).send('Missing state or nonce in session');
      }

      try {
        const tokenSet = await client_company.callback(process.env.REDIRECT_URI_COM, params, {
          state: expectedState,
          nonce: expectedNonce,
        });

        const userinfo = await client_company.userinfo(tokenSet.access_token);

        // 登录成功，保存 session
        req.session.user = userinfo;
        req.session.tokenSet = tokenSet;

        // 安全地选择要暴露的信息（不要整个 userinfo）
        const { email, name, preferred_username } = userinfo;

        // 设置 cookie，有效期 1 小时，HttpOnly 可配置
        res.cookie('user_info', JSON.stringify({
          email,
          name,
          username: preferred_username
        }), {
          httpOnly: false,      // 可被前端 JS 读取
          secure: true,         // 生产环境启用 HTTPS 时使用
          maxAge: 3600 * 1000,  // 1 小时
          sameSite: 'Lax'       // 根据跨域策略配置
        });

        res.redirect('/maincom');
      } catch (err) {
        console.error('OIDC callback error:', err);
        res.status(500).send('Authentication failed');
      }
    });

    app.get('/maincom', (req, res) => {
      res.redirect('http://localhost:3000/maincom');
    });

    // 🔎 获取当前登录用户
    app.get('/mecom', (req, res) => {
      console.log("用户2：");
      // console.log(req.session.user);
      if (req.session.user) {
        res.json(req.session.user);
      } else {
        console.log("错误发生");
        res.status(401).json({ error: 'unauthenticated' });
      }
    });

    // 🔎 获取当前登录用户
    app.post('/adduser', (req, res) => {
      // console.log(req.body);
      //调用后端接口，更新数据库
      try {
          axios.post('http://localhost:8080/createuser',req.body,{
                    headers: {
                        'Content-Type': 'application/json;charset=UTF-8'
                    }
            }).then((response) =>{       
              console.log(response.data);
              //add user to keycloak for temp************************
              res.status(200).send('ok');
            })
      } catch (error) {
        console.error('Error fetching user data:', error.message);
        res.status(500).json({ error: 'Failed to fetch user data' });
      }
    });

    //OTP方式
    app.get('/loginotp', async(req, res) => {
      
      try {
                const token_get='123456';
        //重新向到会员系统并登录
        res.redirect('http://192.168.0.8:3000/impersonate?code='+token_get);
        // console.log(url);

      } catch (err) {
        console.error('❌ Token exchange failed:', err.response?.data || err.message);
      }
      
    });

    // 🔐 代理登录到“会员系统”
    app.get('/loginimpersonate', async(req, res) => {
      //明天在这里增加调用keycloak API获取exchange token的代码即可**************************代理登录存在问题，未解决
      //报错：Requested audience not available: kaiin_bff
      
      const tokenEndpoint = 'http://localhost:8090/realms/myrealm/protocol/openid-connect/token';

      // //获取 admin client 的 access token
      // const dataAdmin = {
      //   grant_type: 'client_credentials',
      //   client_id: 'mochikabu_bff',
      //   client_secret: 'vRFoayXay8LWgwfSze0tO92tlGsRPWxb',
      // };
      // let adminToken='';
      // try {
      //   const response = await axios.post(tokenEndpoint, qs.stringify(dataAdmin), {
      //     headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      //   });
      //   adminToken=response.data.access_token;
      // } catch (err) {
      //   console.error('❌ Admin Token get failed:', err.response?.data || err.message);
      // }
      // console.log("adminToken");
      // console.log(adminToken);
      // console.log("req.session.tokenSet.access_token");
      // console.log(req.session.tokenSet.access_token);
      //再拿着这个admin client的token，获取会员系统用户Y的token
      const dataY = {
        grant_type: 'urn:ietf:params:oauth:grant-type:token-exchange',
        client_id: 'mochikabu_bff',
        client_secret: 'vRFoayXay8LWgwfSze0tO92tlGsRPWxb',
        subject_token: req.session.tokenSet.access_token,  //用户X的accesstoken
        subject_token_type: 'urn:ietf:params:oauth:token-type:access_token',
        // requested_subject: '5206be4e-f8b4-4fee-81e5-d9e763d24f55',  // 💡 用户Y的 Keycloak user ID
        audience: 'kaiin_bff',                     // 💡 获取的 access_token 可用于访问 client B
      };

      try {
        const response = await axios.post(tokenEndpoint, qs.stringify(dataY), {
          headers: { 'Content-Type': 'application/x-www-form-urlencoded',
            // 'Authorization': 'Bearer ${adminToken}' 
           }
        });

        // 你可以用这个 token 无界面登录 system B，或调用 B 的 API
        const accessToken = response.data.access_token;
        console.log('✅ Got token for user Y:', accessToken);

        const token_get='123456';
        //重新向到会员系统并登录
        res.redirect('http://192.168.0.8:3000/impersonate?code='+token_get);
        // console.log(url);

      } catch (err) {
        console.error('❌ Token exchange failed:', err.response?.data || err.message);
      }
      
    });

    app.listen(port, () => {
      console.log(`✅ BFF listening at http://localhost:${port}`);
    });

  } catch (err) {
    console.error('❌ Failed to initialize OpenID client:', err);
    process.exit(1);
  }
})();
