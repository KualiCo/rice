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
package org.kuali.rice.coreservice.framework.parameter;

import org.kuali.rice.core.api.config.property.AbstractRuntimeConfig;
import org.kuali.rice.core.api.config.property.RuntimeConfig;
import org.kuali.rice.coreservice.api.parameter.Parameter;
import org.kuali.rice.coreservice.api.parameter.ParameterKey;
import org.kuali.rice.coreservice.api.parameter.ParameterType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Eric Westfall
 */
public class ParameterRuntimeConfig extends AbstractRuntimeConfig {

    private final String namespaceCode;
    private final String componentCode;
    private final String name;
    private final String defaultValue;
    private final ParameterService parameterService;

    private Parameter parameter;

    public ParameterRuntimeConfig(String namespaceCode, String componentCode, String name, String defaultValue, ParameterService parameterService) {
        if (parameterService == null) {
            throw new IllegalArgumentException("No parameterService was provided");
        }
        this.namespaceCode = namespaceCode;
        this.componentCode = componentCode;
        this.name = name;
        this.defaultValue = defaultValue;
        this.parameterService = parameterService;
        this.parameter = parameterService.getParameter(namespaceCode, componentCode, name);
        parameterService.watchParameter(namespaceCode, componentCode, name, this::parameterChanged);
    }

    public ParameterRuntimeConfig(String namespaceCode, String componentCode, String name, ParameterService parameterService) {
        this(namespaceCode, componentCode, name, null, parameterService);
    }

    @Override
    public synchronized String getValue() {
        if (parameter == null) {
            return defaultValue;
        }
        return parameter.getValue();
    }

    @Override
    public synchronized void setValue(String value) {
        if (parameter == null) {
            if (value != null) {
                Parameter.Builder builder = Parameter.Builder.create(
                        parameterService.getApplicationId(),
                        namespaceCode,
                        componentCode,
                        name,
                        ParameterType.Builder.create("CONFG")
                );
                builder.setValue(value);
                parameter = parameterService.createParameter(builder.build());
            }
        } else {
            Parameter.Builder builder = Parameter.Builder.create(parameter);
            builder.setValue(value);
            parameter = parameterService.updateParameter(builder.build());
        }
    }

    @Override
    public synchronized void fetch() {
        String currentValue = parameter == null ? null : parameter.getValue();
        Parameter newParameter = parameterService.getParameter(namespaceCode, componentCode, name);
        String newValue = newParameter == null ? null : newParameter.getValue();
        if (!Objects.equals(currentValue, newValue)) {
            parameter = newParameter;
            notifyListeners();
        }
    }

    private synchronized void parameterChanged(Parameter parameter) {
        this.parameter = parameter;
        notifyListeners();
    }

}
