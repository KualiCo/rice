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
package org.kuali.rice.kew.impl.stuck;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.DocumentProcessingQueue;
import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.doctype.service.DocumentTypeService;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Unit test for {@link AutofixDocumentsJob}
 *
 * @author Eric Westfall
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({KEWServiceLocator.class, KewApiServiceLocator.class})
public class AutofixDocumentsJobTest {

    @Mock
    private StuckDocumentService stuckDocumentService;
    @Mock
    private DocumentTypeService documentTypeService;
    @Mock
    private DocumentProcessingQueue documentProcessingQueue;

    private JobExecutionContext context;
    private Scheduler scheduler;
    private JobDetail jobDetail;
    private JobKey jobKey;
    private DocumentType documentType;

    private Map<String, StuckDocumentIncident> incidentDatabase;

    @InjectMocks
    private AutofixDocumentsJob autofixDocumentsJob;

    @Before
    public void setup() {
        this.incidentDatabase = new HashMap<>();
        this.context = mock(JobExecutionContext.class);
        this.scheduler = mock(Scheduler.class);
        this.jobKey = new JobKey(getClass().getName());
        this.jobDetail = JobBuilder.newJob(AutofixDocumentsJob.class).withIdentity(jobKey).build();
        when(context.getMergedJobDataMap()).thenReturn(this.jobDetail.getJobDataMap());
        when(context.getScheduler()).thenReturn(this.scheduler);
        when(context.getJobDetail()).thenReturn(this.jobDetail);
        setCurrentAutofixCount(0);

        // setup the static mocks for the "tryToFix" method
        this.documentType = mock(DocumentType.class);
        when(this.documentType.getApplicationId()).thenReturn("COVFEFE");
        when(documentTypeService.findByDocumentId(any())).thenReturn(this.documentType);
        mockStatic(KEWServiceLocator.class);
        mockStatic(KewApiServiceLocator.class);

        when(KEWServiceLocator.getDocumentTypeService()).thenReturn(documentTypeService);
        when(KewApiServiceLocator.getDocumentProcessingQueue(any(), eq("COVFEFE"))).thenReturn(documentProcessingQueue);

    }

    /**
     * Tests that the job will retrieve and cache the StuckDocumentService from the service locator
     */
    @Test
    public void testGetStuckDocumentService_FromKewServiceLocator() {
        AutofixDocumentsJob autofixDocumentsJob = new AutofixDocumentsJob();
        StuckDocumentService stuckDocumentService1 = mock(StuckDocumentService.class);
        StuckDocumentService stuckDocumentService2 = mock(StuckDocumentService.class);

        // initially, mock out KEWServiceLocator.getStuckDocumentService to return null
        mockStatic(KEWServiceLocator.class);
        when(KEWServiceLocator.getStuckDocumentService()).thenReturn(null);

        // make sure it returns null
        assertNull(autofixDocumentsJob.getStuckDocumentService());
        when(KEWServiceLocator.getStuckDocumentService()).thenReturn(stuckDocumentService1);
        assertEquals(stuckDocumentService1, autofixDocumentsJob.getStuckDocumentService());

        // now inject it and make sure that one's our guy
        autofixDocumentsJob.setStuckDocumentService(stuckDocumentService2);
        assertEquals(stuckDocumentService2, autofixDocumentsJob.getStuckDocumentService());
    }

    @Test
    public void testExecute_NoneStillStuck() throws Exception {
        List<StuckDocumentIncident> incidents = generateAndSaveIncidents(2);
        setIncidentIds(incidents.get(0).getStuckDocumentIncidentId(), incidents.get(1).getStuckDocumentIncidentId());

        // indicate that no documents are stuck when asked
        when(stuckDocumentService.resolveIncidentsIfPossible(any())).thenReturn(new ArrayList<>());

        autofixDocumentsJob.execute(context);

        // ensure that it deleted the job
        verify(scheduler).deleteJob(jobKey);
    }

    @Test
    public void testExecute_SomeStillStuck_RetriesLeft() throws Exception {
        setMaxAutofixAttempts(1);
        List<StuckDocumentIncident> incidents = generateAndSaveIncidents(3);
        StuckDocumentIncident incident1 = incidents.get(0);
        StuckDocumentIncident incident2 = incidents.get(1);
        setIncidentIds(incident1.getStuckDocumentIncidentId(), incident2.getStuckDocumentIncidentId());

        // indicate that the second stuck doc is still stuck when asked, but the first one is resolved
        when(stuckDocumentService.resolveIncidentsIfPossible(any())).thenReturn(Collections.singletonList(incident2));
        // just return the incident when we start fixing it
        when(stuckDocumentService.startFixingIncident(any())).then(invocation -> invocation.getArgumentAt(0, StuckDocumentIncident.class));

        autofixDocumentsJob.execute(context);

        // check that only our 1 still stuck incident is left
        List<String> stillStuckDocs = getIncidentIds();
        assertEquals(1, stillStuckDocs.size());
        assertEquals(incident2.getStuckDocumentIncidentId(), stillStuckDocs.get(0));

        verify(stuckDocumentService).startFixingIncident(any());
        verify(stuckDocumentService).recordNewIncidentFixAttempt(any());
        verify(stuckDocumentService, never()).recordIncidentFailure(any());

        // now execute again, this time it should detect that we are past our number of attempts and will record failure
        autofixDocumentsJob.execute(context);

        verify(stuckDocumentService).recordIncidentFailure(any());

    }

    @Test(expected = IllegalStateException.class)
    public void testExecute_SchedulerException() throws Exception {
        when(scheduler.deleteJob(any())).thenThrow(new SchedulerException());

        // make it so there are no stuck documents
        setIncidentIds();

        // now execute, when it tries to delete the job a SchedulerException should be thrown
        // which we up throw as an IllegalStateException
        autofixDocumentsJob.execute(context);
    }


    private List<StuckDocumentIncident> generateAndSaveIncidents(int numberOfIncidents) {
        List<StuckDocumentIncident> incidents = Lists.newArrayList();
        for (int i = 0; i < numberOfIncidents; i++) {
            incidents.add(generateAndSaveIncident());
        }
        return incidents;
    }

    private StuckDocumentIncident generateAndSaveIncident() {
        String incidentId = UUID.randomUUID().toString();
        StuckDocumentIncident incident = StuckDocumentIncident.startNewIncident(UUID.randomUUID().toString());
        incident.setStuckDocumentIncidentId(incidentId);
        incidentDatabase.put(incidentId, incident);
        return incident;
    }

    private void setCurrentAutofixCount(int count) {
        this.jobDetail.getJobDataMap().put(AutofixDocumentsJob.CURRENT_AUTOFIX_COUNT, count);
    }

    private void setIncidentIds(String... incidentIds) {
        this.jobDetail.getJobDataMap().put(AutofixDocumentsJob.INCIDENT_IDS, String.join(",", incidentIds));
    }

    private List<String> getIncidentIds() {
        return Arrays.asList(this.jobDetail.getJobDataMap().getString(AutofixDocumentsJob.INCIDENT_IDS).split(","));
    }

    private void setMaxAutofixAttempts(int maxAutofixAttempts) {
        this.jobDetail.getJobDataMap().put(AutofixCollectorJob.AUTOFIX_MAX_ATTEMPTS_KEY, maxAutofixAttempts);
    }



}
