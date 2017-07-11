package org.kuali.rice.kew.web;

import org.kuali.rice.kns.web.struts.form.KualiForm;

/**
 * Created by ewestfal on 7/7/17.
 */
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
