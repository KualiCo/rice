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

import java.util.function.Consumer;

/**
 * Represents a configuration value that could potentially change at runtime.
 *
 * @author Eric Westfall
 */
public interface RuntimeConfig {

    String getValue();

    Boolean getValueAsBoolean();

    Integer getValueAsInteger();

    Long getValueAsLong();

    void setValue(String value);

    void listen(Consumer<RuntimeConfig> consumer);

    void fetch();

}
