<#import "template.ftl" as layout>
<style>
  body {
    margin: 0;
    font-family: "Segoe UI", sans-serif;
    background: url('${url.resourcesPath}/img/bg.jpg') no-repeat center center fixed;
    background-size: cover;
  }

  .overlay {
    background-color: rgba(0, 0, 0, 0.6);
    position: fixed;
    top: 0; left: 0; right: 0; bottom: 0;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .select-card {
    background: white;
    padding: 2.5rem 2rem;
    border-radius: 12px;
    width: 450px;
    box-shadow: 0 0 25px rgba(0,0,0,0.4);
    text-align: center;
  }

  .select-card h2 {
    margin-bottom: 1.5rem;
    color: #333;
  }

  .user-option {
    display: flex;
    align-items: center;
    margin-bottom: 1rem;
    border: 1px solid #ccc;
    padding: 0.8rem;
    border-radius: 6px;
    background: #f9f9f9;
    cursor: pointer;
    transition: background-color 0.3s;
  }

  .user-option:hover {
    background: #e6f2ff;
  }

  .user-option input[type="radio"] {
    margin-right: 1rem;
  }

  .submit-btn {
    margin-top: 1.5rem;
    width: 100%;
    padding: 0.7rem;
    background: #007bff;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 1rem;
    font-weight: bold;
    cursor: pointer;
    transition: background-color 0.3s;
  }

  .submit-btn:hover {
    background-color: #0056b3;
  }
</style>
<h2>企業を１つ選択してからログイン</h2>
<div class="overlay">
  <div class="select-card">
    <h2>企業を選択してください</h2>
    <form method="post" action="${url.loginAction}">
        <#list candidates as user>
          <label class="user-option">
            <input type="radio" name="selectedUserId" value="${user.id}" required />
            ${user.companyCode}（${user.email}）
          </label>
        </#list>
        <button type="submit" class="submit-btn">ログイン</button>
    </form>
  </div>
</div>