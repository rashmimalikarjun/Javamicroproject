<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>QITROM Dashboard</title></head>
<body>
<h2>QITROM Network Dashboard</h2>
<p>Signed in as <c:out value="${sessionScope.user.username}"/> [<c:out value="${sessionScope.user.role}"/>]</p>
<c:if test="${not empty error}"><p style="color:#a33">${error}</p></c:if>
<table border="1" cellpadding="4">
  <tr><th>Station</th><th>Cell</th><th>Tech</th><th>Capacity Mbps</th><th>Load Mbps</th><th>Util %</th><th>Power</th></tr>
  <c:forEach var="bs" items="${stations}">
    <tr>
      <td><c:out value="${bs.stationId}"/></td>
      <td><c:out value="${bs.cellName}"/></td>
      <td><c:out value="${bs.technology}"/></td>
      <td><c:out value="${bs.capacityMbps}"/></td>
      <td><c:out value="${bs.currentLoadMbps}"/></td>
      <td><c:out value="${bs.utilizationPct}"/></td>
      <td><c:out value="${bs.powerStatus}"/></td>
    </tr>
  </c:forEach>
</table>
<form method="post" action="${pageContext.request.contextPath}/logout">
  <button type="submit">Logout</button>
</form>
</body>
</html>
