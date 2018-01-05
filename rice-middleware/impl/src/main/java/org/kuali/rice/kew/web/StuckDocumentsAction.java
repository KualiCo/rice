/**
 * Copyright 2005-2018 The Kuali Foundation
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

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.rice.core.api.config.property.RuntimeConfig;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.api.util.RiceConstants;
import org.kuali.rice.kew.doctype.service.DocumentTypeService;
import org.kuali.rice.kew.impl.stuck.StuckDocument;
import org.kuali.rice.kew.impl.stuck.StuckDocumentFixAttempt;
import org.kuali.rice.kew.impl.stuck.StuckDocumentIncident;
import org.kuali.rice.kew.impl.stuck.StuckDocumentNotificationJob;
import org.kuali.rice.kew.impl.stuck.StuckDocumentService;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kim.api.role.RoleService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kns.web.struts.action.KualiAction;
import org.kuali.rice.krad.exception.AuthorizationException;
import org.kuali.rice.krad.util.GlobalVariables;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class StuckDocumentsAction extends KualiAction {

    private static final String NOTIFICATION_ENABLED = "stuckDocumentsNotificationEnabledParam";
    private static final String NOTIFICATION_CRON_EXPRESSION = "stuckDocumentsNotificationCronExpressionParam";
    private static final String NOTIFICATION_FROM = "stuckDocumentsNotificationFromParam";
    private static final String NOTIFICATION_TO = "stuckDocumentsNotificationToParam";
    private static final String NOTIFICATION_SUBJECT = "stuckDocumentsNotificationSubjectParam";

    private static final String AUTOFIX_ENABLED = "stuckDocumentsAutofixEnabledParam";
    private static final String AUTOFIX_CRON_EXPRESSION = "stuckDocumentsAutofixCronExpressionParam";
    private static final String AUTOFIX_QUIET_PERIOD = "stuckDocumentsAutofixQuietPeriodParam";
    private static final String AUTOFIX_MAX_ATTEMPTS = "stuckDocumentsAutofixMaxAttemptsParam";
    private static final String AUTOFIX_NOTIFICATION_ENABLED = "stuckDocumentsAutofixNotificationEnabledParam";
    private static final String AUTOFIX_NOTIFICATION_SUBJECT = "stuckDocumentsAutofixNotificationSubjectParam";

    private static final int MAX_INCIDENTS = 1000;

    /**
     * To avoid having to go through the pain of setting up a KIM permission for "Use Screen" for this utility screen,
     * we'll hardcode this screen to the "KR-SYS Technical Administrator" role. Without doing this, the screen is open
     * to all users until that permission is setup which could be considered a security issue.
     */
    protected void checkAuthorization(ActionForm form, String methodToCall) throws AuthorizationException
    {
        boolean authorized = false;
        String principalId = GlobalVariables.getUserSession().getPrincipalId();
        RoleService roleService = KimApiServiceLocator.getRoleService();
        String roleId = roleService.getRoleIdByNamespaceCodeAndName("KR-SYS", "Technical Administrator");
        if (roleId != null) {
            authorized = roleService.principalHasRole(principalId, Collections.singletonList(roleId),
                    new HashMap<String, String>(), true);
        }

        if (!authorized) {
            throw new AuthorizationException(GlobalVariables.getUserSession().getPerson().getPrincipalName(),
                    methodToCall,
                    this.getClass().getSimpleName());
        }
    }

    @Override
    protected ActionForward defaultDispatch(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        StuckDocumentsForm form = (StuckDocumentsForm) actionForm;

        form.setNotificationEnabled(getNotificationEnabled().getValue());
        form.setNotificationCronExpression(getNotificationCronExpression().getValue());
        form.setNotificationFrom(getNotificationFrom().getValue());
        form.setNotificationTo(getNotificationTo().getValue());
        form.setNotificationSubject(getNotificationSubject().getValue());

        form.setAutofixEnabled(getAutofixEnabled().getValue());
        form.setAutofixCronExpression(getAutofixCronExpression().getValue());
        form.setAutofixQuietPeriod(getAutofixQuietPeriod().getValue());
        form.setAutofixMaxAttempts(getAutofixMaxAttempts().getValue());
        form.setAutofixNotificationEnabled(getAutofixNotificationEnabled().getValue());
        form.setAutofixNotificationSubject(getAutofixNotificationSubject().getValue());

        return super.defaultDispatch(mapping, form, request, response);
    }

    public ActionForward updateConfig(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        StuckDocumentsForm form = (StuckDocumentsForm)actionForm;

        getNotificationEnabled().setValue(form.getNotificationEnabled());
        getNotificationCronExpression().setValue(form.getNotificationCronExpression());
        getNotificationFrom().setValue(form.getNotificationFrom());
        getNotificationTo().setValue(form.getNotificationTo());
        getNotificationSubject().setValue(form.getNotificationSubject());

        getAutofixEnabled().setValue(form.getAutofixEnabled());
        getAutofixCronExpression().setValue(form.getAutofixCronExpression());
        getAutofixQuietPeriod().setValue(form.getAutofixQuietPeriod());
        getAutofixMaxAttempts().setValue(form.getAutofixMaxAttempts());
        getAutofixNotificationEnabled().setValue(form.getAutofixNotificationEnabled());
        getAutofixNotificationSubject().setValue(form.getAutofixNotificationSubject());

        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward runStuckNotificationNow(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // make sure we update any config first
        updateConfig(mapping, actionForm, request, response);
        // a little hacky, we are depending on that fact that this job doesn't use the JobExecutionContext
        new StuckDocumentNotificationJob().execute(null);
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward report(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<StuckDocument> stuckDocuments = getStuckDocumentService().findAllStuckDocuments();
        request.setAttribute("stuckDocuments", stuckDocuments);
        return mapping.findForward("report");
    }

    public ActionForward autofixReport(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        StuckDocumentService stuckDocumentService = getStuckDocumentService();
        StuckDocumentsForm form = (StuckDocumentsForm)actionForm;
        StuckDocumentsForm.Status selectedStatus = form.getSelectedStatus();
        List<StuckDocumentIncident> incidents;
        if (selectedStatus == null || selectedStatus.getValue().equals("All")) {
            incidents = stuckDocumentService.findAllIncidents(MAX_INCIDENTS);
        } else {
            incidents = stuckDocumentService.findIncidentsByStatus(MAX_INCIDENTS, StuckDocumentIncident.Status.valueOf(selectedStatus.getValue()));
        }
        List<IncidentHistory> history = incidents.stream().map(incident -> {
            List<StuckDocumentFixAttempt> attempts = stuckDocumentService.findAllFixAttempts(incident.getStuckDocumentIncidentId());
            String documentTypeLabel = getDocumentTypeService().findByDocumentId(incident.getDocumentId()).getLabel();
            return new IncidentHistory(incident, attempts, documentTypeLabel);
        }).collect(Collectors.toList());
        request.setAttribute("history", history);
        return mapping.findForward("autofixReport");
    }

    private RuntimeConfig getNotificationEnabled() {
        return GlobalResourceLoader.getService(NOTIFICATION_ENABLED);
    }

    private RuntimeConfig getNotificationCronExpression() {
        return GlobalResourceLoader.getService(NOTIFICATION_CRON_EXPRESSION);
    }

    private RuntimeConfig getNotificationFrom() {
        return GlobalResourceLoader.getService(NOTIFICATION_FROM);
    }

    private RuntimeConfig getNotificationTo() {
        return GlobalResourceLoader.getService(NOTIFICATION_TO);
    }

    private RuntimeConfig getNotificationSubject() {
        return GlobalResourceLoader.getService(NOTIFICATION_SUBJECT);
    }

    private RuntimeConfig getAutofixEnabled() {
        return GlobalResourceLoader.getService(AUTOFIX_ENABLED);
    }

    private RuntimeConfig getAutofixCronExpression() {
        return GlobalResourceLoader.getService(AUTOFIX_CRON_EXPRESSION);
    }

    private RuntimeConfig getAutofixQuietPeriod() {
        return GlobalResourceLoader.getService(AUTOFIX_QUIET_PERIOD);
    }

    private RuntimeConfig getAutofixMaxAttempts() {
        return GlobalResourceLoader.getService(AUTOFIX_MAX_ATTEMPTS);
    }

    private RuntimeConfig getAutofixNotificationEnabled() {
        return GlobalResourceLoader.getService(AUTOFIX_NOTIFICATION_ENABLED);
    }

    private RuntimeConfig getAutofixNotificationSubject() {
        return GlobalResourceLoader.getService(AUTOFIX_NOTIFICATION_SUBJECT);
    }

    private StuckDocumentService getStuckDocumentService() {
        return KEWServiceLocator.getStuckDocumentService();
    }

    private DocumentTypeService getDocumentTypeService() {
        return KEWServiceLocator.getDocumentTypeService();
    }

    public static class IncidentHistory {

        private final StuckDocumentIncident incident;
        private final List<StuckDocumentFixAttempt> attempts;
        private final String documentTypeLabel;

        IncidentHistory(StuckDocumentIncident incident, List<StuckDocumentFixAttempt> attempts, String documentTypeLabel) {
            this.incident = incident;
            this.attempts = attempts;
            this.documentTypeLabel = documentTypeLabel;
        }

        public String getDocumentId() {
            return incident.getDocumentId();
        }

        public String getStartDate() {
            return incident.getStartDate().toString();
        }

        public String getEndDate() {
            if (incident.getEndDate() == null) {
                return "";
            }
            return incident.getEndDate().toString();
        }

        public String getStatus() {
            return incident.getStatus().name();
        }

        public String getFixAttempts() {
            return attempts.stream().
                    map(attempt -> attempt.getTimestamp().toString()).
                    collect(Collectors.joining(", "));
        }

        public String getDocumentTypeLabel() {
            return documentTypeLabel;
        }
    }

}
