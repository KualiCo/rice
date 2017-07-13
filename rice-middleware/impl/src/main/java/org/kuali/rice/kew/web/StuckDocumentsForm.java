/**
 * Copyright 2005-2017 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kew.web;

import org.kuali.rice.kns.web.struts.form.KualiForm;

public class StuckDocumentsForm extends KualiForm {

    private String notificationEnabled;
    private String notificationCronExpression;
    private String notificationFrom;
    private String notificationTo;
    private String notificationSubject;

    private String autofixEnabled;
    private String autofixCronExpression;
    private String autofixQuietPeriod;
    private String autofixMaxAttempts;

    public String getNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(String notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public String getNotificationCronExpression() {
        return notificationCronExpression;
    }

    public void setNotificationCronExpression(String notificationCronExpression) {
        this.notificationCronExpression = notificationCronExpression;
    }

    public String getNotificationFrom() {
        return notificationFrom;
    }

    public void setNotificationFrom(String notificationFrom) {
        this.notificationFrom = notificationFrom;
    }

    public String getNotificationTo() {
        return notificationTo;
    }

    public void setNotificationTo(String notificationTo) {
        this.notificationTo = notificationTo;
    }

    public String getNotificationSubject() {
        return notificationSubject;
    }

    public void setNotificationSubject(String notificationSubject) {
        this.notificationSubject = notificationSubject;
    }

    public String getAutofixEnabled() {
        return autofixEnabled;
    }

    public void setAutofixEnabled(String autofixEnabled) {
        this.autofixEnabled = autofixEnabled;
    }

    public String getAutofixCronExpression() {
        return autofixCronExpression;
    }

    public void setAutofixCronExpression(String autofixCronExpression) {
        this.autofixCronExpression = autofixCronExpression;
    }

    public String getAutofixQuietPeriod() {
        return autofixQuietPeriod;
    }

    public void setAutofixQuietPeriod(String autofixQuietPeriod) {
        this.autofixQuietPeriod = autofixQuietPeriod;
    }

    public String getAutofixMaxAttempts() {
        return autofixMaxAttempts;
    }

    public void setAutofixMaxAttempts(String autofixMaxAttempts) {
        this.autofixMaxAttempts = autofixMaxAttempts;
    }
}
