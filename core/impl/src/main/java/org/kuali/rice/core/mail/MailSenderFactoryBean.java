/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.rice.core.mail;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.kuali.rice.core.config.ConfigContext;
import org.kuali.rice.kew.mail.Mailer;
import org.kuali.rice.ksb.util.KSBConstants;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * A factory bean which reads mail-related properties from the Configuration system and
 * generates a Spring Java Mail Sender instance for use by services that send e-mail.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
@SuppressWarnings("unchecked")
public class MailSenderFactoryBean extends AbstractFactoryBean {

	protected final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(MailSenderFactoryBean.class);
	
    private static final String MAIL_PREFIX = "mail";
    private static final String USERNAME_PROPERTY = "mail.smtp.username";
    private static final String PASSWORD_PROPERTY = "mail.smtp.password";
    private static final String HOST_PROPERTY = "mail.smtp.host";
    
    private Session mailSession;
    
    @Override
    protected Object createInstance() throws Exception {
	    // Retrieve "mail.*" properties from the configuration system and construct a Properties object
		Properties properties = new Properties();
		Properties configProps = ConfigContext.getCurrentContextConfig().getProperties();
		for (Object keyObj : configProps.keySet()) {
		    if (keyObj instanceof String) {
		    	String key = (String)keyObj;
		    	if (key.startsWith(MAIL_PREFIX)){
		    		properties.put(key, configProps.get(key));
		    	}
		    }
		}
		
		// Construct an appropriate Java Mail Session
		// If username and password properties are found, construct a Session with SMTP authentication
		String username = properties.getProperty(USERNAME_PROPERTY);
		String password = properties.getProperty(PASSWORD_PROPERTY);
		if (username != null && password != null) {
			mailSession = Session.getInstance(properties, new SimpleAuthenticator(username, password));
			LOG.info("Rice Mailer being used. Username and Pass were found");
		} else {
			mailSession = Session.getInstance(properties);
			LOG.info("Rice Mailer being used. Username and Pass were not found");
		}
		
		// Construct and return a Spring Java Mail Sender
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(properties.getProperty(HOST_PROPERTY));
		mailSender.setSession(mailSession);
		
		return mailSender;
    }
    
    private class SimpleAuthenticator extends Authenticator {
    	
    	private final PasswordAuthentication passwordAuthentication;

        private SimpleAuthenticator(String username, String password) {
        	this.passwordAuthentication = new PasswordAuthentication(username, password);
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return passwordAuthentication;
        }
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public Class getObjectType() {
	return Properties.class;
    }

}
