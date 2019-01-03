<%--

    Copyright 2005-2019 The Kuali Foundation

    Licensed under the Educational Community License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.opensource.org/licenses/ecl2.php

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
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
<p>Displays up to the most recent 1000 stuck document incidents and autofix attempts.</p>
<form action="StuckDocuments.do" method="POST">
    <select name="statusFilter" onchange="this.form.submit()">
        <c:forEach var="status" items="${KualiForm.statuses}">
            <option value="${status.value}" ${status.selected ? 'selected="selected"' : ''}>${status.value}</option>
        </c:forEach>
    </select>
    <input type="hidden" name="methodToCall" value="autofixReport"/>
    <br><br>
    <table>
        <thead>
            <tr>
                <th>Document ID</th>
                <th>Document Type</th>
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
                <td><c:out value="${incidentHistory.documentTypeLabel}"/></td>
                <td><c:out value="${incidentHistory.startDate}"/></td>
                <td><c:out value="${incidentHistory.endDate}"/></td>
                <td><c:out value="${incidentHistory.status}"/></td>
                <td><c:out value="${incidentHistory.fixAttempts}"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <kul:csrf/>
</form>
</body>
</html>