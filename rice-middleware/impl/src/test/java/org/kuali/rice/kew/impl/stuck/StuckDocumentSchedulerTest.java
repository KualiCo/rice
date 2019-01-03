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
package org.kuali.rice.kew.impl.stuck;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.rice.core.api.config.property.SimpleRuntimeConfig;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.context.event.ContextRefreshedEvent;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link StuckDocumentScheduler}
 *
 * @author Eric Westfall
 */
@RunWith(MockitoJUnitRunner.class)
public class StuckDocumentSchedulerTest {

    @Mock
    private Scheduler scheduler;
    @Mock
    private ContextRefreshedEvent contextRefreshedEvent;

    private SimpleRuntimeConfig notificationEnabled = new SimpleRuntimeConfig("false");
    private SimpleRuntimeConfig notificationCronExpression = new SimpleRuntimeConfig("0 0 0/1 1/1 * ? *");
    private SimpleRuntimeConfig autofixEnabled = new SimpleRuntimeConfig("false");
    private SimpleRuntimeConfig autofixCronExpression = new SimpleRuntimeConfig("0 0/15 * 1/1 * ? *");
    private SimpleRuntimeConfig autofixQuietPeriod = new SimpleRuntimeConfig("60");
    private SimpleRuntimeConfig autofixMaxAttempts = new SimpleRuntimeConfig("1");

    @InjectMocks
    private StuckDocumentScheduler stuckDocumentScheduler;

    @Before
    public void setup() throws Exception {
        stuckDocumentScheduler.setNotificationEnabled(notificationEnabled);
        stuckDocumentScheduler.setNotificationCronExpression(notificationCronExpression);
        stuckDocumentScheduler.setAutofixEnabled(autofixEnabled);
        stuckDocumentScheduler.setAutofixCronExpression(autofixCronExpression);
        stuckDocumentScheduler.setAutofixQuietPeriod(autofixQuietPeriod);
        stuckDocumentScheduler.setAutofixMaxAttempts(autofixMaxAttempts);
        when(scheduler.checkExists(StuckDocumentScheduler.NOTIFICATION_JOB_KEY)).thenReturn(false);
        when(scheduler.checkExists(StuckDocumentScheduler.AUTOFIX_COLLECTOR_JOB_KEY)).thenReturn(false);
    }

    @Test
    public void testInitialization_NotificationNotEnabled() throws Exception {
        stuckDocumentScheduler.onApplicationEvent(contextRefreshedEvent);

        JobKey jobKey = StuckDocumentScheduler.NOTIFICATION_JOB_KEY;
        verify(scheduler).checkExists(jobKey);
        verify(scheduler, never()).deleteJob(jobKey);
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    /**
     * Make sure that we delete the job if one exists upon initialization.
     */
    @Test
    public void testInitialization_ExistingNotificationJob() throws Exception {
        JobKey jobKey = StuckDocumentScheduler.NOTIFICATION_JOB_KEY;
        when(scheduler.checkExists(jobKey)).thenReturn(true);

        stuckDocumentScheduler.onApplicationEvent(contextRefreshedEvent);

        verify(scheduler).checkExists(jobKey);
        verify(scheduler).deleteJob(jobKey);
    }

    @Test
    public void testEnableAfterInitialization_NotificationJob() throws Exception {

        stuckDocumentScheduler.onApplicationEvent(contextRefreshedEvent);

        verify(scheduler, never()).deleteJob(StuckDocumentScheduler.NOTIFICATION_JOB_KEY);

        // now let's enable the stuck document scheduler, after doing this it should get scheduled
        ArgumentCaptor<JobDetail> jobCaptor = ArgumentCaptor.forClass(JobDetail.class);

        notificationEnabled.setValue("true");

        verify(scheduler, times(1)).scheduleJob(jobCaptor.capture(), any(Trigger.class));

        JobDetail jobDetail = jobCaptor.getValue();
        JobDataMap jobData = jobDetail.getJobDataMap();
        assertEquals(StuckDocumentScheduler.NOTIFICATION_JOB_KEY, jobDetail.getKey());
//        assertEquals(false, jobData.getBoolean("autofix"));
//        assertEquals(120, jobData.getInt("checkFrequency"));
//        assertEquals(60, jobData.getInt("autofixQuietPeriod"));
//        assertEquals(1, jobData.getInt("maxAutofixAttempts"));

    }

    @Test(expected = IllegalStateException.class)
    public void testInitialization_withSchedulerException() throws Exception {
        when(scheduler.checkExists(any(JobKey.class))).thenThrow(new SchedulerException());
        stuckDocumentScheduler.onApplicationEvent(contextRefreshedEvent);
    }


}
