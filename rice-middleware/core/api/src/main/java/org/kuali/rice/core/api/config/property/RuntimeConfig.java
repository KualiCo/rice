package org.kuali.rice.core.api.config.property;

import java.util.function.Consumer;

/**
 * Represents a configuration value that could potentially change at runtime.
 *
 * @author Eric Westfall
 */
public interface RuntimeConfig {

    String getValue();

    void listen(Consumer<RuntimeConfig> consumer);

    void sync();

}
