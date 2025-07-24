const express = require('express');
const session = require('express-session');
const { Issuer } = require('openid-client');
require('dotenv').config();

const app = express();
const port = 3010;

app.use(session({
  secret: '123456',
  resave: false,
  saveUninitialized: true,
  cookie: { secure: false }
}));

let client; // Keycloak client

// 初始化 OpenID Client 并注册路由
(async () => {
  try {
    const keycloakIssuer = await Issuer.discover(`${process.env.KEYCLOAK_BASE_URL}/.well-known/openid-configuration`);
    client = new keycloakIssuer.Client({
      client_id: process.env.CLIENT_ID,
      client_secret: process.env.CLIENT_SECRET,
      redirect_uris: [process.env.REDIRECT_URI],
      response_types: ['code'],
    });

    // 🔐 登录跳转
    app.get('/login', (req, res) => {
      const url = client.authorizationUrl({
        scope: 'openid profile email',
        state: 'random_state',
      });
      res.redirect(url);
      console.log(url);
    });

    // 🔐 登录回调
    app.get('/callback', async (req, res) => {
      const params = client.callbackParams(req);
      const tokenSet = await client.callback(process.env.REDIRECT_URI, params);
      const userinfo = await client.userinfo(tokenSet.access_token);

      req.session.user = userinfo;
      req.session.tokenSet = tokenSet;
      res.redirect('/');
    });

    app.get('/me', (req, res) => {
      if (req.session.user) {
        res.json(req.session.user);
      } else {
        res.status(401).json({ error: 'unauthenticated' });
      }
    });

    app.get('/logout', (req, res) => {
      req.session.destroy(() => {
        res.redirect(`${process.env.KEYCLOAK_BASE_URL}/protocol/openid-connect/logout?post_logout_redirect_uri=http://localhost:3000/test`);
      });
    });

    app.listen(port, () => {
      console.log(`BFF listening at http://localhost:${port}`);
    });

  } catch (err) {
    console.error('Failed to initialize OIDC client:', err);
    process.exit(1);
  }
})();
