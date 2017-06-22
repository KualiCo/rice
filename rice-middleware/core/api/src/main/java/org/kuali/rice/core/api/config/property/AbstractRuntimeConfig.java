package org.kuali.rice.core.api.config.property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractRuntimeConfig implements RuntimeConfig {

    private final List<Consumer<RuntimeConfig>> listeners = Collections.synchronizedList(new ArrayList<>());

    @Override
    public Boolean getValueAsBoolean() {
        return Boolean.valueOf(getValue());
    }

    @Override
    public Integer getValueAsInteger() {
        return Integer.parseInt(getValue());
    }

    @Override
    public Long getValueAsLong() {
        return Long.parseLong(getValue());
    }

    @Override
    public void listen(Consumer<RuntimeConfig> consumer) {
        listeners.add(consumer);
    }

    protected void notifyListeners() {
        listeners.forEach(listener -> listener.accept(this));
    }


}
