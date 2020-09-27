
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>商城模块2</title>
</head>


<body>

    <h1>欢迎来到购物商城2</h1>
    <%=request.getRemoteUser()%>
    <%--  在cas文件中配置了如果退出可以访问外部网端 这里访问的是百度网页--%>
    <a href="http://192.168.188.146:9100/cas/logout?service=http://www.baidu.com"> 点击退出</a>

</body>
</html>
