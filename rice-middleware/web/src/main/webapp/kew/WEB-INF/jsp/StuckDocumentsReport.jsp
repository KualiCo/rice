<%--

    Copyright 2005-2018 The Kuali Foundation

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
    <title>Stuck Documents Report</title>
    <style type="text/css">
        td, th
        {
            padding:0 15px 0 15px;
        }
    </style>
</head>
<body>
<h1>Currently Stuck Documents</h1>
<c:choose>
    <c:when test="${fn:length(stuckDocuments) == 0}">
        <p>There are currently no stuck documents</p>
    </c:when>
    <c:otherwise>
    <table>
        <thead>
        <tr>
            <th>Document ID</th>
            <th>Document Type</th>
            <th>Create Date</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="stuckDocument" items="${stuckDocuments}">
          <tr>
              <td><c:out value="${stuckDocument.documentId}"/></td>
              <td><c:out value="${stuckDocument.documentTypeLabel}"/></td>
              <td><c:out value="${stuckDocument.formattedCreateDate}"/></td>
          </tr>
        </c:forEach>
    </c:otherwise>
</c:choose>
</body>
</html>