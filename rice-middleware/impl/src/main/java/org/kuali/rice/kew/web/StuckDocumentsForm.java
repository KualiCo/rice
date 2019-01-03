/**
 * Copyright 2005-2019 The Kuali Foundation
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

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kew.impl.stuck.StuckDocumentIncident;
import org.kuali.rice.kns.web.struts.form.KualiForm;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

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
    private String autofixNotificationEnabled;
    private String autofixNotificationSubject;

    private List<Status> statuses;

    public StuckDocumentsForm() {
        this.statuses = new ArrayList<Status>();
        this.statuses.add(new Status("All", false));
        for (StuckDocumentIncident.Status status : StuckDocumentIncident.Status.values()) {
            this.statuses.add(new Status(status.name(), false));
        }
    }

    @Override
    public void populate(HttpServletRequest request) {
        super.populate(request);
        // determine if they set the status filter
        String statusFilter = request.getParameter("statusFilter");
        if (!StringUtils.isBlank(statusFilter)) {
            for (Status status : this.statuses) {
                if (status.getValue().equals(statusFilter)) {
                    status.setSelected(true);
                    break;
                }
            }
        }
    }

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

    public String getAutofixNotificationEnabled() {
        return autofixNotificationEnabled;
    }

    public void setAutofixNotificationEnabled(String autofixNotificationEnabled) {
        this.autofixNotificationEnabled = autofixNotificationEnabled;
    }

    public String getAutofixNotificationSubject() {
        return autofixNotificationSubject;
    }

    public void setAutofixNotificationSubject(String autofixNotificationSubject) {
        this.autofixNotificationSubject = autofixNotificationSubject;
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<Status> statuses) {
        this.statuses = statuses;
    }

    public Status getSelectedStatus() {
        for (Status status : statuses) {
            if (status.isSelected()) {
                return status;
            }
        }
        return null;
    }

    public static class Status {
        private String value;
        private boolean selected;
        public Status(String value, boolean selected) {
            this.value = value;
            this.selected = selected;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

}
