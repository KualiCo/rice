/**
 * Copyright 2005-2014 The Kuali Foundation
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
package org.kuali.rice.krad.test.document;

import org.kuali.rice.core.api.util.RiceKeyConstants;
import org.kuali.rice.krad.rules.MaintenanceDocumentRuleBase;
import org.kuali.rice.krad.rules.rule.event.RuleEvent;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;

/**
 * setup a custom method to process a custom rule event
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class AccountRules extends MaintenanceDocumentRuleBase {

    /**
     * set a global variable to verify invocation of the custom method's processing of the custom rule event
     *
     * @param event - the custom rule event to process
     * @return
     */
    public boolean processEvent( RuleEvent event ) {
        GlobalVariables.getMessageMap().putInfo( event.getName(), event.getRuleMethodName() );
        return true;
    }
}
