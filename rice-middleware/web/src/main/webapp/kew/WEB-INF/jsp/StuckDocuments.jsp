<%--

    Copyright 2005-2017 The Kuali Foundation

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

<kul:page headerTitle="Stuck Documents" lookup="true"
          transactionalDocument="false" showDocumentInfo="false"
          htmlFormAction="StuckDocuments" docTitle="Stuck Documents">
    <script>
        function updateConfig() {
            document.forms[0].elements['methodToCall'].value = 'updateConfig';
            document.forms[0].submit();
        }
    </script>
    <div class="headerarea" id="headerarea">
        <h1>Stuck Document Processing</h1>
    </div>
    <html-el:form action="StuckDocuments">
        <html-el:hidden property="methodToCall" value=""/>
        <kul:csrf />
        <div style="margin-left:20px">
            <h1>Stuck Document Notification</h1>
            <div>
                <label>Enable</label>
                <html-el:text property="notificationEnabled"/>
            </div>
            <div>
                <label>Cron Expression</label>
                <html-el:text property="notificationCronExpression"/>
            </div>
            <div>
                <label>From</label>
                <html-el:text property="notificationFrom"/>
            </div>
            <div>
                <label>To</label>
                <html-el:text property="notificationTo"/>
            </div>

            <div>
                <label>Subject</label>
                <html-el:text property="notificationSubject"/>
            </div>

            <h1>Stuck Document Autofix</h1>
            <div>
                <label>Enable</label>
                <html-el:text property="autofixEnabled"/>
            </div>
            <div>
                <label>Cron Expression</label>
                <html-el:text property="autofixCronExpression"/>
            </div>
            <div>
                <label>Quiet Period</label>
                <html-el:text property="autofixQuietPeriod"/>
            </div>
            <div>
                <label>Max Attempts</label>
                <html-el:text property="autofixMaxAttempts"/>
            </div>

            <input type="button" value="Update" onclick="updateConfig()"/>
        </div>
    </html-el:form>

</kul:page>
