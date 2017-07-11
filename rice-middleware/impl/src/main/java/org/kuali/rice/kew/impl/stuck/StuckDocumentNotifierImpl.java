package org.kuali.rice.kew.impl.stuck;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.config.property.RuntimeConfig;
import org.kuali.rice.core.api.mail.MailMessage;
import org.kuali.rice.core.api.mail.Mailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StuckDocumentNotifierImpl implements StuckDocumentNotifier {

    private static final Logger LOG = LoggerFactory.getLogger(StuckDocumentNotifierImpl.class);

    private static final String EMAIL_TEMPLATE =
            "{0} stuck documents have been identified within the workflow system with the following document IDs:\n\n{1}";

    private RuntimeConfig from;
    private RuntimeConfig to;
    private RuntimeConfig subject;

    private Mailer mailer;

    @Override
    public void notify(List<String> documentIds) {
        if (!documentIds.isEmpty()) {
            send(MessageFormat.format(EMAIL_TEMPLATE, documentIds.size(), String.join("\n", documentIds)));
        }

    }

    private void send(String messageBody) {
        if (checkCanSend()) {
            MailMessage message = new MailMessage();
            message.setFromAddress(from.getValue());
            message.setToAddresses(Collections.singleton(to.getValue()));
            message.setSubject(subject.getValue());
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

}
