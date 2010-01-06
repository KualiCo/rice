/*
 * Copyright 2005-2007 The Kuali Foundation
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
package org.kuali.rice.kns.datadictionary.validation.charlevel;

import org.junit.Test;
import org.kuali.rice.kns.datadictionary.validation.ValidationTestUtils;
import org.kuali.test.KNSTestCase;

public class NumericValidationPatternTest extends KNSTestCase {
    private NumericValidationPattern pattern;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        pattern = new NumericValidationPattern();
    }


    @Test public final void testMatch_allowDefault() {
        boolean[] expected = { true, // ""
                false, // "!!!"
                false, // "[a-9]"
                false, // "^A-Z"
                false, // "abc"
                false, // "a bc"
                false, // "a_bc"
                true, // "123"
                false, // "12 3"
                false, // "12_3"
                false, // "a1b2c3"
                false, // "a1b2_c3"
                false, // "a 1b2c3"
                false, // "a 1b2_c3"
        };

        ValidationTestUtils.assertPatternMatches(pattern, expected);
    }
}
