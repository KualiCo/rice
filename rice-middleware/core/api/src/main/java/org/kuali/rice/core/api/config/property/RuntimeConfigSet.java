package org.kuali.rice.core.api.config.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Specifies a set of related {@link RuntimeConfig}. Whenever there is a change to one of them, any listeners will be
 * notified and attempts will be made to ensure the entire set of runtime configs are as up to date as possible prior
 * to performing the notification.
 *
 * @author Eric Westfall
 */
public class RuntimeConfigSet {

    private final List<RuntimeConfig> configs;
    private final List<Consumer<RuntimeConfigSet>> listeners;

    public RuntimeConfigSet() {
        this.configs = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    public RuntimeConfigSet(RuntimeConfig... config) {
        this(Arrays.asList(config));
    }

    public RuntimeConfigSet(List<RuntimeConfig> configs) {
        this();
        this.configs.addAll(configs);
        listenToConfigs(this.configs);
    }

    public List<RuntimeConfig> getConfigs() {
        return Collections.unmodifiableList(this.configs);
    }

    public void listen(Consumer<RuntimeConfigSet> consumer) {
        listeners.add(consumer);
    }

    public void unlisten(Consumer<RuntimeConfigSet> consumer) {
        listeners.remove(consumer);
    }

    private void listenToConfigs(List<RuntimeConfig> configs) {
        configs.forEach(config -> config.listen(this::consumeConfigChange));
    }

    private void consumeConfigChange(RuntimeConfig changedConfig) {
        configs.forEach(config -> {
            if (config != changedConfig) {
                changedConfig.sync();
            }
        });
        listeners.forEach(listener -> listener.accept(this));
    }

}
