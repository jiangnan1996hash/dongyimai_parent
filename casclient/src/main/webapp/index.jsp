<%--
  Created by IntelliJ IDEA.
  User: Thinkpad
  Date: 2020/9/17
  Time: 12:49
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>商城模块1</title>
</head>

<body>

    <h1>欢迎来到购物商城1</h1>
    <%=request.getRemoteUser()%>
    <%--  在cas文件中配置了如果退出可以访问外部网端 这里访问的是百度网页--%>
    <a href="http://192.168.188.146:9100/cas/logout?service=http://www.baidu.com"> 点击退出</a>

</body>
</html>
