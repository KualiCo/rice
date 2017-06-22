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
public class SimpleRuntimeConfig extends AbstractRuntimeConfig {

    private String value;

    public SimpleRuntimeConfig() {}

    public SimpleRuntimeConfig(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        notifyListeners();
    }

    @Override
    public void fetch() {
        // does nothing, since the value is stored in this object
    }

}
