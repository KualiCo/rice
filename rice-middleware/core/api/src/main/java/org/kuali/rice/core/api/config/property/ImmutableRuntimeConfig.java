package org.kuali.rice.core.api.config.property;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Implementation of a {@link RuntimeConfig} which represents a config value that is immutable and will never change.
 *
 * @author Eric Westfall
 */
public class ImmutableRuntimeConfig implements RuntimeConfig {

    private final String value;

    public ImmutableRuntimeConfig(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void listen(Consumer<RuntimeConfig> consumer) {
        // does nothing, since the value cannot change
    }

    @Override
    public void sync() {
        // does nothing, since there are never any changes to sync
    }
}
