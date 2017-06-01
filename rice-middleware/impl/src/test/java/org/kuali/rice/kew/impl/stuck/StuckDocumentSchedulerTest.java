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

    private SimpleRuntimeConfig enabled = new SimpleRuntimeConfig("false");
    private SimpleRuntimeConfig autofix = new SimpleRuntimeConfig("false");
    private SimpleRuntimeConfig checkFrequency = new SimpleRuntimeConfig("120");
    private SimpleRuntimeConfig autofixQuietPeriod = new SimpleRuntimeConfig("60");
    private SimpleRuntimeConfig maxAutofixAttempts = new SimpleRuntimeConfig("1");

    @InjectMocks
    private StuckDocumentScheduler stuckDocumentScheduler;

    @Before
    public void setup() {
        stuckDocumentScheduler.setEnabled(enabled);
        stuckDocumentScheduler.setAutofix(autofix);
        stuckDocumentScheduler.setCheckFrequency(checkFrequency);
        stuckDocumentScheduler.setAutofixQuietPeriod(autofixQuietPeriod);
        stuckDocumentScheduler.setMaxAutofixAttempts(maxAutofixAttempts);
    }

    @Test
    public void testInitialization_NotEnabled() throws Exception {
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);

        stuckDocumentScheduler.onApplicationEvent(contextRefreshedEvent);

        verify(scheduler).checkExists(any(JobKey.class));
        verify(scheduler, never()).deleteJob(any(JobKey.class));
        verifyNoMoreInteractions(scheduler);
    }

    /**
     * Make sure that we delete the job if one exists upon initialization.
     */
    @Test
    public void testInitialization_ExistingJob() throws Exception {
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);

        stuckDocumentScheduler.onApplicationEvent(contextRefreshedEvent);

        verify(scheduler).checkExists(any(JobKey.class));
        verify(scheduler).deleteJob(any(JobKey.class));
    }

    @Test
    public void testEnableAfterInitialization() throws Exception {

        stuckDocumentScheduler.onApplicationEvent(contextRefreshedEvent);

        verify(scheduler, never()).deleteJob(any(JobKey.class));

        // now let's enable the stuck document scheduler, after doing this it should get scheduled
        ArgumentCaptor<JobDetail> jobCaptor = ArgumentCaptor.forClass(JobDetail.class);

        enabled.setValue("true");

        verify(scheduler, times(1)).scheduleJob(jobCaptor.capture(), any(Trigger.class));

        JobDetail jobDetail = jobCaptor.getValue();
        JobDataMap jobData = jobDetail.getJobDataMap();
        assertEquals("false", jobData.getString("autofix"));
        assertEquals("120", jobData.getString("checkFrequency"));
        assertEquals("60", jobData.getString("autofixQuietPeriod"));
        assertEquals("1", jobData.getString("maxAutofixAttempts"));

    }

    @Test(expected = IllegalStateException.class)
    public void testInitialization_withSchedulerException() throws Exception {
        when(scheduler.checkExists(any(JobKey.class))).thenThrow(new SchedulerException());
        stuckDocumentScheduler.onApplicationEvent(contextRefreshedEvent);
    }


}
