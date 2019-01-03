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
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Unit test for {@link AutofixCollectorJob}
 *
 * @author Eric Westfall
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({KEWServiceLocator.class})
public class AutofixCollectorJobTest {

    @Mock
    private StuckDocumentService stuckDocumentService;
    @Mock
    private JobExecutionContext context;
    @Mock
    private Scheduler scheduler;

    private JobDataMap jobDataMap;

    @InjectMocks
    private AutofixCollectorJob autofixCollectorJob;

    @Before
    public void setup() {
        when(context.getScheduler()).thenReturn(scheduler);
        this.jobDataMap = new JobDataMap();
        when(context.getMergedJobDataMap()).thenReturn(jobDataMap);
        setAutofixQuietPeriod(60);
        setMaxAutofixAttempts(2);
    }

    /**
     * Tests that the job will retrieve and cache the StuckDocumentService from the service locator
     */
    @Test
    public void testGetStuckDocumentService_FromKewServiceLocator() {
        AutofixCollectorJob autofixCollectorJob = new AutofixCollectorJob();
        StuckDocumentService stuckDocumentService1 = mock(StuckDocumentService.class);
        StuckDocumentService stuckDocumentService2 = mock(StuckDocumentService.class);

        // initially, mock out KEWServiceLocator.getStuckDocumentService to return null
        mockStatic(KEWServiceLocator.class);
        when(KEWServiceLocator.getStuckDocumentService()).thenReturn(null);

        // make sure it returns null
        assertNull(autofixCollectorJob.getStuckDocumentService());
        when(KEWServiceLocator.getStuckDocumentService()).thenReturn(stuckDocumentService1);
        assertEquals(stuckDocumentService1, autofixCollectorJob.getStuckDocumentService());

        // now inject it and make sure that one's our guy
        autofixCollectorJob.setStuckDocumentService(stuckDocumentService2);
        assertEquals(stuckDocumentService2, autofixCollectorJob.getStuckDocumentService());
    }

    /**
     * Tests that the job is failsafe if no {@link StuckDocumentService} is available.
     */
    @Test(expected = JobExecutionException.class)
    public void testExecute_NoDependenciesAvailable() throws JobExecutionException {
        // first, set the injected service to null
        autofixCollectorJob.setStuckDocumentService(null);

        // now mock out the KEWServiceLocator so that it returns null as well
        mockStatic(KEWServiceLocator.class);
        when(KEWServiceLocator.getStuckDocumentService()).thenReturn(null);

        // try to execute it, no exceptions should be thrown
        autofixCollectorJob.execute(context);
    }

    @Test
    public void testExecute_NoStuckDocuments() throws JobExecutionException {
        setNumberOfStuckDocumentIncidents(0);

        autofixCollectorJob.execute(context);

        verify(stuckDocumentService, times(1)).recordNewStuckDocumentIncidents();
        verifyZeroInteractions(scheduler);

    }

    @Test
    public void testExecute_StuckDocuments_NoPartitioning() throws JobExecutionException, SchedulerException {
        Set<String> generatedIncidentIds = setNumberOfStuckDocumentIncidents(5).stream().map(StuckDocumentIncident::getStuckDocumentIncidentId).collect(Collectors.toSet());

        autofixCollectorJob.execute(context);

        ArgumentCaptor<JobDetail> jobCaptor = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(scheduler, times(1)).scheduleJob(jobCaptor.capture(), triggerCaptor.capture());

        // first, examime the job

        JobDetail jobDetail = jobCaptor.getValue();

        // check the incident ids
        String incidentIdsString = jobDetail.getJobDataMap().getString(AutofixDocumentsJob.INCIDENT_IDS);
        Set<String> incidentIds = new HashSet<>(Arrays.asList(incidentIdsString.split(",")));
        assertEquals(generatedIncidentIds, incidentIds);

        // check the autofix count
        int currentAutofixCount = jobDetail.getJobDataMap().getInt(AutofixDocumentsJob.CURRENT_AUTOFIX_COUNT);
        assertEquals(0, currentAutofixCount);

        // check autofix quiet period
        int autofixQuietPeriod = jobDetail.getJobDataMap().getInt(AutofixCollectorJob.AUTOFIX_QUIET_PERIOD_KEY);
        assertEquals(60, autofixQuietPeriod);

        // check maxAutofixAttempts
        int maxAutofixAttempts = jobDetail.getJobDataMap().getInt(AutofixCollectorJob.AUTOFIX_MAX_ATTEMPTS_KEY);
        assertEquals(2, maxAutofixAttempts);

        // next, examine the trigger, it's really hard to test this against the values passed in unfortunately, we need
        // to cast it to a SimpleTrigger instance

        Trigger trigger = triggerCaptor.getValue();
        assertTrue(trigger instanceof SimpleTrigger);
        SimpleTrigger simpleTrigger = (SimpleTrigger)trigger;
        assertEquals(2, simpleTrigger.getRepeatCount());
        assertEquals(60 * 1000, simpleTrigger.getRepeatInterval());
        assertEquals(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT, simpleTrigger.getMisfireInstruction());
        assertEquals(0, simpleTrigger.getTimesTriggered());

    }

    @Test
    public void testExecute_StuckDocuments_Partitioning() throws JobExecutionException, SchedulerException {
        Set<String> generatedIncidentIds = setNumberOfStuckDocumentIncidents(425).stream().map(StuckDocumentIncident::getStuckDocumentIncidentId).collect(Collectors.toSet());

        autofixCollectorJob.execute(context);

        ArgumentCaptor<JobDetail> jobCaptor = ArgumentCaptor.forClass(JobDetail.class);
        verify(scheduler, times(9)).scheduleJob(jobCaptor.capture(), any(Trigger.class));

        Set<String> incidentIds = new HashSet<>();
        jobCaptor.getAllValues().forEach(jobDetail -> {
            String incidentIdsString = jobDetail.getJobDataMap().getString(AutofixDocumentsJob.INCIDENT_IDS);
            incidentIds.addAll(Arrays.asList(incidentIdsString.split(",")));
        });

        assertEquals(generatedIncidentIds, incidentIds);
    }

    @Test
    public void testExecute_StuckDocuments_SchedulerException() throws JobExecutionException, SchedulerException {
        setNumberOfStuckDocumentIncidents(5);

        when(scheduler.scheduleJob(any(JobDetail.class), any(Trigger.class))).thenThrow(new SchedulerException());

        try {
            autofixCollectorJob.execute(context);
            fail("IllegalStateException should have been thrown");
        } catch (IllegalStateException e) {
            assertTrue(e.getCause() instanceof SchedulerException);
        }
    }


    private void setAutofixQuietPeriod(int autofixQuietPeriod) {
        jobDataMap.put(AutofixCollectorJob.AUTOFIX_QUIET_PERIOD_KEY, autofixQuietPeriod);
    }

    private void setMaxAutofixAttempts(int maxAutofixAttempts) {
        jobDataMap.put(AutofixCollectorJob.AUTOFIX_MAX_ATTEMPTS_KEY, maxAutofixAttempts);
    }

    private List<StuckDocumentIncident> setNumberOfStuckDocumentIncidents(int numberOfIncidents) {
        List<StuckDocumentIncident> incidents = new ArrayList<>();
        for (int i = 0; i < numberOfIncidents; i++) {
            incidents.add(generateIncident());
        }
        when(stuckDocumentService.recordNewStuckDocumentIncidents()).thenReturn(incidents);
        return incidents;
    }

    private StuckDocumentIncident generateIncident() {
        StuckDocumentIncident incident =  StuckDocumentIncident.startNewIncident(UUID.randomUUID().toString());
        incident.setStuckDocumentIncidentId(UUID.randomUUID().toString());
        return incident;
    }


}
