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

    private void listenToConfigs(List<RuntimeConfig> configs) {
        configs.forEach(config -> config.listen(this::consumeConfigChange));
    }

    private void consumeConfigChange(RuntimeConfig changedConfig) {
        configs.forEach(config -> {
            if (config != changedConfig) {
                changedConfig.fetch();
            }
        });
        listeners.forEach(listener -> listener.accept(this));
    }

}
