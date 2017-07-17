<%@ include file="/kr/WEB-INF/jsp/tldHeader.jsp" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Stuck Documents Autofix Report</title>
    <style type="text/css">
        td, th
        {
            padding:0 15px 0 15px;
        }
    </style>
</head>
<body>
<h1>Stuck Documents Autofix Report</h1>
<p>Display up to the most recent 1000 stuck document incidents and autofix attempts.</p>
<table>
    <thead>
        <tr>
            <th>Document ID</th>
            <th>Start Date</th>
            <th>End Date</th>
            <th>Status</th>
            <th>Autofix Attempts</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="incidentHistory" items="${history}">
        <tr>
            <td><c:out value="${incidentHistory.documentId}"/></td>
            <td><c:out value="${incidentHistory.startDate}"/></td>
            <td><c:out value="${incidentHistory.endDate}"/></td>
            <td><c:out value="${incidentHistory.status}"/></td>
            <td><c:out value="${incidentHistory.fixAttempts}"/></td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>