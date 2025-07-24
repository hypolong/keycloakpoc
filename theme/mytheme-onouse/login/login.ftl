<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>ログイン</title>
  <style>
    * {
      box-sizing: border-box;
    }

    body {
      margin: 0;
      font-family: "Segoe UI", "Helvetica Neue", sans-serif;
      background: url('${url.resourcesPath}/img/bg.jpg') no-repeat center center fixed;
      background-size: cover;
    }

    .overlay {
      background: rgba(0, 0, 0, 0.5);
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      display: flex;
      justify-content: center;
      align-items: center;
    }

    .login-card {
      background: white;
      padding: 2.5rem;
      border-radius: 12px;
      width: 400px;
      max-width: 90%;
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
      position: relative;
    }

    .login-logo {
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 1.5rem;
    }

    .login-logo img {
      height: 50px;
      margin-right: 12px;
    }

    .login-logo h2 {
      font-size: 1.8rem;
      margin: 0;
      color: #333;
    }

    .tabs {
      display: flex;
      justify-content: space-around;
      margin-bottom: 1.2rem;
      border-bottom: 1px solid #ccc;
    }

    .tab {
      padding: 0.6rem 1rem;
      cursor: pointer;
      font-weight: bold;
      color: #666;
      border-bottom: 3px solid transparent;
      transition: 0.3s;
    }

    .tab.active {
      color: #007bff;
      border-color: #007bff;
    }

    .login-form input {
      width: 100%;
      margin-bottom: 1rem;
      padding: 0.7rem;
      border: 1px solid #ccc;
      border-radius: 6px;
      font-size: 1rem;
    }

    .login-form button {
      width: 100%;
      padding: 0.8rem;
      background-color: #007bff;
      color: white;
      font-size: 1rem;
      font-weight: bold;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      transition: 0.3s;
    }

    .login-form button:hover {
      background-color: #0056b3;
    }

    .hidden {
      display: none;
    }
  </style>
</head>
<body>

<div class="overlay">
  <div class="login-card">
    <div class="login-logo">
      <img src="${url.resourcesPath}/img/logo.png" alt="logo">
      <h3>新持株システム</h3>
    </div>

    <div class="tabs">
      <div class="tab active" onclick="switchTab('default')">メールアドレスログイン</div>
      <div class="tab" onclick="switchTab('company')">会員コードログイン</div>
    </div>

    <form id="kc-form-login" class="login-form" action="${url.loginAction}" method="post">
      <input type="hidden" id="loginType" name="loginType" value="default"/>

      <div id="default-tab">
        <input type="text" id="username" name="username" placeholder="メールアドレス" autofocus autocomplete="off"/>
        <input type="password" id="password" name="password" placeholder="パスワード" autocomplete="off"/>
      </div>

      <div id="company-tab" class="hidden">
        <input type="text" name="companyCode" placeholder="企業コード" autocomplete="off"/>
        <input type="text" name="memberCode" placeholder="会員コード" autocomplete="off"/>
        <input type="password" name="password2" placeholder="パスワード" autocomplete="off"/>
      </div>
 <#if message?has_content && message.type == "error">
  <div class="error-message" style="color:red; margin-bottom: 1rem;">
    <p>${kcSanitize(message.summary)?no_esc}</p>
  </div>
</#if>
      <button type="submit">ログイン</button>

   
    </form>
  </div>
</div>

<script>
  function switchTab(type) {
    document.getElementById("loginType").value = type;
    document.getElementById("default-tab").classList.toggle("hidden", type !== 'default');
    document.getElementById("company-tab").classList.toggle("hidden", type !== 'company');

    const tabs = document.querySelectorAll('.tab');
    tabs.forEach(tab => tab.classList.remove('active'));
    tabs[type === 'default' ? 0 : 1].classList.add('active');
  }
</script>

</body>
</html>
