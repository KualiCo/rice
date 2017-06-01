package org.kuali.rice.core.api.config.property;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Implementation of a {@link RuntimeConfig} which represents a config value that is immutable and will never change.
 *
 * @author Eric Westfall
 */
public class SimpleRuntimeConfig implements RuntimeConfig {

    private String value;
    private List<Consumer<RuntimeConfig>> listeners;

    public SimpleRuntimeConfig() {
        this(null);
    }

    public SimpleRuntimeConfig(String value) {
        this.value = value;
        this.listeners = new ArrayList<>();
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        notifyListeners();
    }

    private void notifyListeners() {
        listeners.forEach(listener -> listener.accept(this));
    }

    @Override
    public void listen(Consumer<RuntimeConfig> consumer) {
        listeners.add(consumer);
    }

    @Override
    public void fetch() {
        // does nothing, since the value is stored in this object
    }

}
