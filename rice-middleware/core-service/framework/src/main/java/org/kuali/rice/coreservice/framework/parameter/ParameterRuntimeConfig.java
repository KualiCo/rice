package org.kuali.rice.coreservice.framework.parameter;

import org.kuali.rice.core.api.config.property.RuntimeConfig;
import org.kuali.rice.coreservice.api.parameter.Parameter;
import org.kuali.rice.coreservice.api.parameter.ParameterKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Eric Westfall
 */
public class ParameterRuntimeConfig implements RuntimeConfig {

    private final String namespaceCode;
    private final String componentCode;
    private final String name;
    private final String defaultValue;
    private final List<Consumer<RuntimeConfig>> listeners;
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
        this.listeners = new ArrayList<>();
        this.parameter = this.parameterService.getParameter(namespaceCode, componentCode, name);
        this.parameterService.watchParameter(namespaceCode, componentCode, name, this::parameterChanged);
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
    public synchronized void listen(Consumer<RuntimeConfig> consumer) {
        listeners.add(consumer);
    }

    @Override
    public synchronized void fetch() {
        String currentValue = parameter == null ? null : parameter.getValue();
        Parameter newParameter = this.parameterService.getParameter(namespaceCode, componentCode, name);
        String newValue = newParameter == null ? null : newParameter.getValue();
        if (!Objects.equals(currentValue, newValue)) {
            this.parameter = newParameter;
            notifyListeners();
        }
    }

    private synchronized void notifyListeners() {
        listeners.forEach(listener -> listener.accept(this));
    }

    private synchronized void parameterChanged(Parameter parameter) {
        this.parameter = parameter;
        notifyListeners();
    }

}
