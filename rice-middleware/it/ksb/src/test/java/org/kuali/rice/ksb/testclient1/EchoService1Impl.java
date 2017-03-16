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
package org.kuali.rice.ksb.testclient1;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.kuali.rice.ksb.messaging.remotedservices.EchoService;
import org.kuali.rice.ksb.messaging.remotedservices.ServiceCallInformationHolder;

import java.util.List;
import java.util.Map;

public class EchoService1Impl implements EchoService {
	public String echo(String string) {
		return string;
	}

	public String trueEcho(String string) {
		System.out.println("I Was echoed!!!!");
		return "hi mom";
	}

    public void captureHeaders() {
        ServiceCallInformationHolder.multiValues = (Map<String, List<String>>)PhaseInterceptorChain.getCurrentMessage().get(Message.PROTOCOL_HEADERS);
    }
}
