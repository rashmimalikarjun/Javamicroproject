<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>QITROM - Sign in</title></head>
<body>
<h2>QITROM Web - Sign in</h2>
<c:if test="${not empty error}"><p style="color:#a33">${error}</p></c:if>
<form method="post" action="${pageContext.request.contextPath}/login">
  <label>Username <input type="text" name="username" required></label><br>
  <label>Password <input type="password" name="password" required></label><br>
  <button type="submit">Sign In</button>
</form>
<p><small>Optimization shown here is a deterministic local simulation (NOT quantum / NOT QpiAI Opt).</small></p>
</body>
</html>
