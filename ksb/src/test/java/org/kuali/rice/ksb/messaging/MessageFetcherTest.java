/*
 * Copyright 2007 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.kuali.rice.ksb.messaging;

import java.util.List;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.kuali.rice.core.config.ConfigContext;
import org.kuali.rice.ksb.messaging.remotedservices.TestHarnessSharedTopic;
import org.kuali.rice.ksb.messaging.service.KSBJavaService;
import org.kuali.rice.ksb.service.KSBServiceLocator;
import org.kuali.rice.ksb.test.KSBTestCase;
import org.kuali.rice.ksb.util.KSBConstants;

/**
 * Tests {@link MessageFetcher}. Turn messaging off but leave persistence on.
 * this will result in messages being persisted to db but not delivered. from
 * there we start up the {@link MessageFetcher} and make sure he does his job.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 * 
 */
public class MessageFetcherTest extends KSBTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		ConfigContext.getCurrentContextConfig().overrideProperty(
				KSBConstants.MESSAGING_OFF, "true");
		TestHarnessSharedTopic.CALL_COUNT = 0;
	}

	@Override
	public void tearDown() throws Exception {
		TestHarnessSharedTopic.CALL_COUNT = 0;
	}

	@Test
	public void testRequeueMessages() throws Exception {

		List<PersistedMessage> messages = KSBServiceLocator.getRouteQueueService().getNextDocuments(null);
		assertEquals("Should have no messages in the queue.", 0, messages.size());
		
		// this number is way over the top but we're going to see if it works in
		// an overworked CI env.
		TestHarnessSharedTopic.CALL_COUNT_NOTIFICATION_THRESHOLD = 500;

		for (int i = 0; i < TestHarnessSharedTopic.CALL_COUNT_NOTIFICATION_THRESHOLD; i++) {
			sendMessage();
		}

		messages = KSBServiceLocator.getRouteQueueService().getNextDocuments(null);
		assertEquals("Should have 500 messages in the queue.", 500, messages.size());
		
		turnOnMessaging();
		new MessageFetcher((Integer) null).run();
		synchronized (TestHarnessSharedTopic.LOCK) {
			TestHarnessSharedTopic.LOCK.wait(5 * 60 * 1000);
		}
		// sleep here for half a second because the notify above is executed from inside the database transaction in TestHarnessSharedTopic,
		// we need to give that transaction time to be fully committed.
		Thread.sleep(500);

		assertTrue(
				"Service not called by message fetcher",
				TestHarnessSharedTopic.CALL_COUNT == TestHarnessSharedTopic.CALL_COUNT_NOTIFICATION_THRESHOLD);
		
		messages = KSBServiceLocator.getRouteQueueService().getNextDocuments(null);
		assertEquals("Should still have no messages in the queue.", 0, messages.size());
	}

	private void sendMessage() {
		QName serviceName = QName.valueOf("{testAppsSharedTopic}sharedTopic");
		KSBJavaService testJavaAsyncService = (KSBJavaService) KSBServiceLocator
				.getMessageHelper().getServiceAsynchronously(serviceName);
		testJavaAsyncService.invoke(new ClientAppServiceSharedPayloadObj(
				"message content", false));
	}

	private void turnOnMessaging() {
		ConfigContext.getCurrentContextConfig().overrideProperty(
				KSBConstants.MESSAGING_OFF, "false");
	}

	@Test
	public void testRequeueSingleMessage() throws Exception {
		sendMessage();
		sendMessage();
		PersistedMessage message = KSBServiceLocator.getRouteQueueService()
				.getNextDocuments(null).get(0);
		assertNotNull("message should have been persisted", message);
		turnOnMessaging();
		new MessageFetcher(message.getRouteQueueId()).run();
		synchronized (TestHarnessSharedTopic.LOCK) {
			TestHarnessSharedTopic.LOCK.wait(3 * 1000);
		}
		// sleep here for half a second because the notify above is executed from inside the database transaction in TestHarnessSharedTopic,
		// we need to give that transaction time to be fully committed.
		Thread.sleep(500);

		assertTrue(
				"Service not called by message fetcher corrent number of times",
				1 == TestHarnessSharedTopic.CALL_COUNT);
		for (int i = 0; i < 10; i++) {
			if (KSBServiceLocator.getRouteQueueService().getNextDocuments(null)
					.size() == 1) {
				break;
			}
			Thread.sleep(1000);
		}
		assertEquals(
				"Message Queue should have a single remaining message because only single message was resent",
				1, KSBServiceLocator.getRouteQueueService().getNextDocuments(
						null).size());
	}

}
