package org.kuali.rice.core.framework.persistence.platform;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.util.ClassLoaderUtils;
import org.springframework.beans.factory.FactoryBean;

/**
 * Factory bean that allows for loading a {@link DatabasePlatform} from a classname specified by a specified
 * configProperty. If the configProperty has no value, then the provided defaultPlatform will be returned from this
 * factory bean instead.
 *
 * @author Eric Westfall
 */
public class ConfigurableDatabasePlatform implements FactoryBean<DatabasePlatform> {

    private String configProperty;
    private DatabasePlatform defaultPlatform;

    @Override
    public DatabasePlatform getObject() throws Exception {
        if (!StringUtils.isBlank(configProperty)) {
            String platformClassName = ConfigContext.getCurrentContextConfig().getProperty(configProperty);
            if (!StringUtils.isBlank(platformClassName)) {
                return (DatabasePlatform)ClassLoaderUtils.getClass(platformClassName).newInstance();
            }
        }
        if (defaultPlatform == null) {
            throw new IllegalArgumentException("No value for config property '" + configProperty + "' and no default platform specified.");
        }
        return defaultPlatform;
    }

    @Override
    public Class<?> getObjectType() {
        return DatabasePlatform.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setConfigProperty(String configProperty) {
        this.configProperty = configProperty;
    }

    public void setDefaultPlatform(DatabasePlatform defaultPlatform) {
        this.defaultPlatform = defaultPlatform;
    }

}
