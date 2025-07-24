<h2>请选择账号</h2>
<form method="post">
    <#list users as u>
        <label>
            <input type="radio" name="selectedUserId" value="${u.id}"/>
            企业：${u.companyCode}，会员：${u.memberCode}
        </label><br/>
    </#list>
    <input type="password" name="password" placeholder="密码"/><br/>
    <button type="submit">继续</button>
</form>