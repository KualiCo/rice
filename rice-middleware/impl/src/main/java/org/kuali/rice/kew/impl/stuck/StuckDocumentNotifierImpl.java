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
package org.kuali.rice.kew.impl.stuck;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.config.property.RuntimeConfig;
import org.kuali.rice.core.api.config.property.RuntimeConfigSet;
import org.kuali.rice.core.api.mail.MailMessage;
import org.kuali.rice.core.api.mail.Mailer;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.krad.util.KRADConstants;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StuckDocumentNotifierImpl implements StuckDocumentNotifier, InitializingBean {

    private static final Logger LOG = LogManager.getLogger(StuckDocumentNotifierImpl.class);

    private static final String NOTIFICATION_SUBJECT_TEMPLATE_NAME = "notificationSubject";
    private static final String NOTIFICATION_EMAIL_TEMPLATE_NAME = "notificationEmail";
    private static final String AUTOFIX_SUBJECT_TEMPLATE_NAME = "autofixSubject";
    private static final String AUTOFIX_EMAIL_TEMPLATE_NAME = "autofixEmail";

    private static final String NOTIFICATION_EMAIL_TEMPLATE =
            "${numStuckDocuments} stuck documents have been identified within the workflow system:\n\n" +
                    "Document ID, Document Type, Create Date\n" +
                    "---------------------------------------\n" +
                    "<#list stuckDocuments as stuckDocument>${stuckDocument.documentId}, ${stuckDocument.documentTypeLabel}, ${stuckDocument.createDate}\n</#list>";
    private static final String AUTOFIX_EMAIL_TEMPLATE =
            "Failed to autofix document ${documentId}, ${documentTypeLabel}.\n\nIncident details:\n\tStarted: ${startDate}\n\tEnded: ${endDate}\n\n" +
                    "Attempts occurred at the following times: <#list autofixAttempts as autofixAttempt>\n\t${autofixAttempt.timestamp}</#list>";
    private static final String FAILURE_EMAIL_SUBJECT_TEMPLATE = "Failed to autofix stuck document with ID {0}";

    private RuntimeConfig from;
    private RuntimeConfig to;
    private RuntimeConfig subject;

    private RuntimeConfig autofixSubject;

    private Configuration freemarkerConfig;
    private StringTemplateLoader templateLoader;

    private Mailer mailer;

    public void afterPropertiesSet() {
        this.freemarkerConfig = new Configuration(Configuration.VERSION_2_3_21);
        this.templateLoader = new StringTemplateLoader();
        this.freemarkerConfig.setTemplateLoader(templateLoader);
        updateTemplates();
        new RuntimeConfigSet(subject, autofixSubject).listen(runtimeConfigSet -> updateTemplates());
    }

    private void updateTemplates() {
        this.templateLoader.putTemplate(NOTIFICATION_SUBJECT_TEMPLATE_NAME, subject.getValue());
        this.templateLoader.putTemplate(NOTIFICATION_EMAIL_TEMPLATE_NAME, NOTIFICATION_EMAIL_TEMPLATE);
        this.templateLoader.putTemplate(AUTOFIX_SUBJECT_TEMPLATE_NAME, autofixSubject.getValue());
        this.templateLoader.putTemplate(AUTOFIX_EMAIL_TEMPLATE_NAME, AUTOFIX_EMAIL_TEMPLATE);
        this.freemarkerConfig.clearTemplateCache();
    }

    @Override
    public void notify(List<StuckDocument> stuckDocuments) {
        if (!stuckDocuments.isEmpty()) {
            Map<String, Object> dataModel = buildNotificationTemplateDataModel(stuckDocuments);
            String subject = processTemplate(NOTIFICATION_SUBJECT_TEMPLATE_NAME, dataModel);
            String body = processTemplate(NOTIFICATION_EMAIL_TEMPLATE_NAME, dataModel);
            send(subject, body);
        }
    }

    /**
     * Supported values include:
     *
     * - numStuckDocuments
     * - stuckDocuments (List of StuckDocument)
     * - environment
     * - applicationUrl
     */
    private Map<String, Object> buildNotificationTemplateDataModel(List<StuckDocument> stuckDocuments) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("numStuckDocuments", stuckDocuments.size());
        dataModel.put("stuckDocuments", stuckDocuments);
        addGlobalDataModel(dataModel);
        return dataModel;
    }

    @Override
    public void notifyIncidentFailure(StuckDocumentIncident incident, List<StuckDocumentFixAttempt> attempts) {
        Map<String, Object> dataModel = buildIncidentFailureTemplateDataModel(incident, attempts);
        String subject = processTemplate(AUTOFIX_SUBJECT_TEMPLATE_NAME, dataModel);
        String body = processTemplate(AUTOFIX_EMAIL_TEMPLATE_NAME, dataModel);
        send(subject, body);
    }

    /**
     * Supported values include:
     *
     * - documentId
     * - documentTypeLabel
     * - startDate
     * - endDate
     * - numberOfAutofixAttempts
     * - attempts (List of StuckDocumentFixAttempt)
     * - environment
     * - applicationUrl
     */
    private Map<String, Object> buildIncidentFailureTemplateDataModel(StuckDocumentIncident incident, List<StuckDocumentFixAttempt> attempts) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("documentId", incident.getDocumentId());
        dataModel.put("documentTypeLabel", resolveDocumentTypeLabel(incident.getDocumentId()));
        dataModel.put("startDate", incident.getStartDate());
        dataModel.put("endDate", incident.getEndDate());
        dataModel.put("numberOfAutofixAttempts", attempts.size());
        dataModel.put("autofixAttempts", attempts);
        addGlobalDataModel(dataModel);
        return dataModel;
    }

    private void addGlobalDataModel(Map<String, Object> dataModel) {
        dataModel.put("environment", ConfigContext.getCurrentContextConfig().getEnvironment());
        dataModel.put("applicationUrl", ConfigContext.getCurrentContextConfig().getProperty(KRADConstants.APPLICATION_URL_KEY));
    }

    private String processTemplate(String templateName, Object dataModel) {
        try {
            StringWriter writer = new StringWriter();
            Template template = freemarkerConfig.getTemplate(templateName);
            template.process(dataModel, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new IllegalStateException("Failed to execute template " + templateName, e);
        }
    }

    private String resolveDocumentTypeLabel(String documentId) {
        return KEWServiceLocator.getDocumentTypeService().findByDocumentId(documentId).getLabel();
    }

    private void send(String messageBody) {
        send(subject.getValue(), messageBody);
    }

    private void send(String subject, String messageBody) {
        if (checkCanSend()) {
            MailMessage message = new MailMessage();
            message.setFromAddress(from.getValue());
            message.setToAddresses(Collections.singleton(to.getValue()));
            message.setSubject(subject);
            message.setMessage(messageBody);
            try {
                mailer.sendEmail(message);
            } catch (Exception e) {
                // we don't want some email configuration issue to mess up our stuck document processing, just log the error
                LOG.error("Failed to send stuck document notification email with the body:\n" + messageBody, e);
            }
        }
    }

    private boolean checkCanSend() {
        boolean canSend = true;
        if (StringUtils.isBlank(from.getValue())) {
            LOG.error("Cannot send stuck documentation notification because no 'from' address is configured.");
            canSend = false;
        }
        if (StringUtils.isBlank(to.getValue())) {
            LOG.error("Cannot send stuck documentation notification because no 'to' address is configured.");
            canSend = false;
        }
        return canSend;
    }

    @Required
    public void setFrom(RuntimeConfig from) {
        this.from = from;
    }

    @Required
    public void setTo(RuntimeConfig to) {
        this.to = to;
    }

    @Required
    public void setSubject(RuntimeConfig subject) {
        this.subject = subject;
    }

    @Required
    public void setMailer(Mailer mailer) {
        this.mailer = mailer;
    }

    @Required
    public void setAutofixSubject(RuntimeConfig autofixSubject) {
        this.autofixSubject = autofixSubject;
    }
}
