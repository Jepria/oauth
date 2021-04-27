<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
  String query = request.getQueryString();
  String clientName = request.getParameter("client_name");
%>
<html>
<head>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
  <link type="text/css" rel="stylesheet" property='stylesheet' href="index.css"/>
  <title>Login</title>
</head>
<body>
<main>
  <form action="login?<%=query%>" method="post">
    <div class="container">
      <h5><%=clientName%>
      </h5>
      <label>
        <b>Username</b>
        <input type="text" placeholder="Enter Username" name="username" required></label>
      <label>
        <b>Password</b>
        <input type="password" placeholder="Enter Password" name="password" required>
      </label>
      <button type="submit">Login</button>
    </div>
  </form>
</main>
</body>
</html>
