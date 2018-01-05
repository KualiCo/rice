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
