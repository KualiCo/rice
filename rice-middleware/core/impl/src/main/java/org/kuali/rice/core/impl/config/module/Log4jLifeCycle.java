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
package org.kuali.rice.core.impl.config.module;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.builder.api.Component;
import org.apache.logging.log4j.core.config.properties.PropertiesConfiguration;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.kuali.rice.core.api.config.property.Config;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.lifecycle.BaseLifecycle;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;


/**
 * Lifecycle implementation that initializes and shuts down Log4J logging
 */
class Log4jLifeCycle extends BaseLifecycle {

	private static final String LOG4J_FILE_NOT_FOUND = "log4j settings file not found at location: ";

	@Override
	public void start() throws Exception {

		final Config config = ConfigContext.getCurrentContextConfig();

		final String log4jFilePath =  config.getProperty(Config.LOG4J_SETTINGS_PATH);
		boolean log4jFileExists = checkPropertiesFileExists(log4jFilePath);

		final String log4jConfigXml = config.getProperty(Config.LOG4J_SETTINGS_XML);
		final String log4jConfigProps = config.getProperty(Config.LOG4J_SETTINGS_PROPS);

		if (StringUtils.isNotBlank(log4jConfigXml)) {
			LoggerContext context = (LoggerContext) LogManager.getContext(false);
			context.start(new XmlConfiguration(context, new ConfigurationSource(new ByteArrayInputStream(log4jConfigXml.getBytes()))));
		} else if (StringUtils.isNotBlank(log4jConfigProps)) {
			LoggerContext context = (LoggerContext) LogManager.getContext(false);
			context.start(new PropertiesConfiguration(context, new ConfigurationSource(new ByteArrayInputStream(log4jConfigProps.getBytes())), new Component()));
		} else if (log4jFileExists) {
			LoggerContext context = (LoggerContext) LogManager.getContext(false);
			context.setConfigLocation(ResourceUtils.getFile(log4jFilePath).toURI());
		}
		super.start();
	}

	/**
	 * Checks if the passed in file exists.
	 *
	 * @param log4jSettingsPath the file
	 * @return true if exists
	 */
	private boolean checkPropertiesFileExists(String log4jSettingsPath) {
		if (StringUtils.isBlank(log4jSettingsPath)) {
			return false;
		}

		boolean exists;

		try {
			exists = ResourceUtils.getFile(log4jSettingsPath).exists();
		} catch (FileNotFoundException e) {
			exists = false;
		}

		if (!exists) {
			System.out.println(LOG4J_FILE_NOT_FOUND + log4jSettingsPath);
		}

		return exists;
	}

}
