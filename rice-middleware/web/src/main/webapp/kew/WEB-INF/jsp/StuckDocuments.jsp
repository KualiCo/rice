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
        function runStuckNotificationNow() {
            document.forms[0].elements['methodToCall'].value = 'runStuckNotificationNow';
            document.forms[0].submit();
        }

    </script>
    <div class="headerarea" id="headerarea">
        <h1>Stuck Document Processing</h1>
    </div>
    <html-el:form action="StuckDocuments">
        <html-el:hidden property="methodToCall" value=""/>
        <kul:csrf />
        <div style="margin:2em">
            <p>
                On this page, you can manage the configuration for stuck document notification as well as controlling whether or
                not the system will attempt to automatically fix any stuck documents that are detected.
            </p>
            <p>
                For the cron expressions, you can use <a href="http://www.cronmaker.com/">Cron Maker</a> to help
                construct and interpret these.
            </p>
            <fieldset style="padding: 1em">
                <legend style="font-weight: bold; font-size: 150%">Notification</legend>
                <div style="padding: 0.5em">
                    <label for="notificationEnabled"><b>Enable:</b></label>
                    <html-el:text property="notificationEnabled" styleId="notificationEnabled"/>
                </div>
                <div style="padding: 0.5em">
                    <label for="notificationCronExpression"><b>Cron Expression:</b></label>
                    <html-el:text property="notificationCronExpression" styleId="notificationCronExpression"/>
                </div>
                <div style="padding: 0.5em">
                    <label for="notificationFrom"><b>From:</b></label>
                    <html-el:text property="notificationFrom" styleId="notificationFrom"/>
                </div>
                <div style="padding: 0.5em">
                    <label for="notificationTo"><b>To:</b></label>
                    <html-el:text property="notificationTo" styleId="notificationTo"/>
                </div>

                <div style="padding: 0.5em">
                    <label for="notificationSubject"><b>Subject:</b></label>
                    <html-el:text property="notificationSubject" styleId="notificationSubject" style="width:200px"/>
                </div>

            </fieldset>

            <fieldset style="padding: 1em; margin-top: 2em">
                <legend style="font-weight: bold; font-size: 150%">Autofix</legend>
                <div style="padding: 0.5em">
                    <label for="autofixEnabled"><b>Enable:</b></label>
                    <html-el:text property="autofixEnabled" styleId="autofixEnabled"/>
                </div>
                <div style="padding: 0.5em">
                    <label for="autofixCronExpression"><b>Cron Expression:</b></label>
                    <html-el:text property="autofixCronExpression" styleId="autofixCronExpression"/>
                </div>
                <div style="padding: 0.5em">
                    <label for="autofixQuietPeriod"><b>Quiet Period (sec):</b></label>
                    <html-el:text property="autofixQuietPeriod" styleId="autofixQuietPeriod"/>
                </div>
                <div style="padding: 0.5em">
                    <label for="autofixMaxAttempts"><b>Max Attempts:</b></label>
                    <html-el:text property="autofixMaxAttempts" styleId="autofixMaxAttempts"/>
                </div>
            </fieldset>

            <div style="margin-top: 2em; padding: 1em">
                <div>
                    <input type="button" style="padding:0.5em; background: #ccc; font-size: 150%" value="Update" onclick="updateConfig()"/>
                </div>
                <div style="margin-top: 1em">
                    <input type="button" style="padding:0.5em; background: #ccc; font-size: 150%" value="Run Notification Now" onclick="runStuckNotificationNow()"/>
                </div>
            </div>
        </div>
    </html-el:form>

</kul:page>
