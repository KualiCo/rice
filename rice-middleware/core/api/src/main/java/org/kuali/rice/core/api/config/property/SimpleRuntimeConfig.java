/**
 * Copyright 2005-2019 The Kuali Foundation
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

    @Override
    public void setValue(String value) {
        this.value = value;
        notifyListeners();
    }

    @Override
    public void fetch() {
        // does nothing, since the value is stored in this object
    }

}
