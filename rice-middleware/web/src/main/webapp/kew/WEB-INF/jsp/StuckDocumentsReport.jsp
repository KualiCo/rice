<%@ include file="/kr/WEB-INF/jsp/tldHeader.jsp" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Stuck Documents Report</title>
</head>
<body>
<h1>Currently Stuck Documents</h1>
<ul>
<c:forEach var="stuckDocumentId" items="${stuckDocumentIds}">
    <li><c:out value="${stuckDocumentId}"/></li>
</c:forEach>
</ul>
</body>
</html>