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

import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.DocumentRefreshQueue;
import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.doctype.service.DocumentTypeService;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.opensaml.xmlsec.signature.P;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.SchedulerException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class AutofixDocumentsJob implements Job {

    private static final Logger LOG = LogManager.getLogger(AutofixDocumentsJob.class);

    static final String INCIDENT_IDS = "incidentIds";
    static final String CURRENT_AUTOFIX_COUNT = "currentAutofixCount";

    private volatile StuckDocumentService stuckDocumentService;
    private volatile DocumentTypeService documentTypeService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        List<String> incidentIds = incidentIds(context);
        checkDependenciesAvailable(incidentIds);
        int currentAutofixCount = incrementAutofixCount(context);
        List<StuckDocumentIncident> stillStuck = getStuckDocumentService().resolveIncidentsIfPossible(incidentIds);
        if (stillStuck.isEmpty()) {
            LOG.info("All stuck document incidents have been resolved, deleting autofix job.");
            JobKey key = context.getJobDetail().getKey();
            try {
                context.getScheduler().deleteJob(key);
            } catch (SchedulerException e) {
                throw new IllegalStateException("Failed to delete job with key: " + key, e);
            }
        } else if (currentAutofixCount > autofixMaxAttempts(context)) {
            // at this point any remaining stuck docs are failures
            LOG.info("Exceeded autofixMaxAttempts of " + autofixMaxAttempts(context) + ". Marking remaining " + stillStuck.size() + " stuck document incidents as failures.");
            stillStuck.forEach(stuck -> getStuckDocumentService().recordIncidentFailure(stuck));
        } else {
            // let's update the list of stuck doc incident ids and we know we have more iterations of this job scheduled so it should try again
            LOG.info("There are " + stillStuck.size() + " stuck documents still remaining at autofix attempt " + currentAutofixCount + ", will try again.");
            updateIncidentIds(stillStuck, context);
            stillStuck.forEach(this::processIncident);
        }
    }

    /**
     * Checks if needed dependencies are available in order to run this job. Due to the fact that this is a quartz job,
     * it could trigger while the system is offline and then immediately get fired when the system starts up and due to
     * the startup process it could attempt to execute while not all of the necessary services are fully initialized.
     */
    private void checkDependenciesAvailable(List<String> incidentIds) throws JobExecutionException {
        if (getStuckDocumentService() == null) {
            String message = "Dependencies are not available for this autofix documents job for stuck document incidents: " + incidentIds.toString();
            LOG.warn(message);
            throw new JobExecutionException(message);
        }
    }


    private int incrementAutofixCount(JobExecutionContext context) {
        int currentAutofixCount = context.getJobDetail().getJobDataMap().getInt(CURRENT_AUTOFIX_COUNT);
        currentAutofixCount++;
        context.getJobDetail().getJobDataMap().put(CURRENT_AUTOFIX_COUNT, currentAutofixCount);
        return currentAutofixCount;
    }

    private List<String> incidentIds(JobExecutionContext context) {
        String incidentIdValues = context.getJobDetail().getJobDataMap().getString(INCIDENT_IDS);
        String[] incidentIds = incidentIdValues.split(",");
        return Arrays.asList(incidentIds);
    }

    private void updateIncidentIds(List<StuckDocumentIncident> incidents, JobExecutionContext context) {
        List<String> incidentIds = incidents.stream().map(StuckDocumentIncident::getStuckDocumentIncidentId).collect(Collectors.toList());
        context.getJobDetail().getJobDataMap().put(INCIDENT_IDS, incidentIds.stream().collect(Collectors.joining(",")));
    }

    private void processIncident(StuckDocumentIncident incident) {
        if (StuckDocumentIncident.Status.PENDING.equals(incident.getStatus())) {
            incident = getStuckDocumentService().startFixingIncident(incident);
        }
        try {
            tryToFix(incident);
        } catch (Throwable t) {
            // we catch and log here because we don't want one bad apple to ruin the whole bunch!
            LOG.error("Error occurred when attmpting to fix stuck document incident for doc id " + incident.getDocumentId(), t);
        }
    }

    private void tryToFix(StuckDocumentIncident incident) {
        getStuckDocumentService().recordNewIncidentFixAttempt(incident);
        String docId = incident.getDocumentId();
        DocumentType documentType = getDocumentTypeService().findByDocumentId(docId);
        DocumentRefreshQueue drq = KewApiServiceLocator.getDocumentRequeuerService(documentType.getApplicationId(), docId, 0);
        drq.refreshDocument(docId);
    }

    private int autofixMaxAttempts(JobExecutionContext context) {
        return context.getMergedJobDataMap().getInt(AutofixCollectorJob.AUTOFIX_MAX_ATTEMPTS_KEY);
    }

    protected DocumentTypeService getDocumentTypeService() {
        if (this.documentTypeService == null) {
            this.documentTypeService = KEWServiceLocator.getDocumentTypeService();
        }
        return this.documentTypeService;
    }

    public void setDocumentTypeService(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }

    protected StuckDocumentService getStuckDocumentService() {
        if (this.stuckDocumentService == null) {
            this.stuckDocumentService = KEWServiceLocator.getStuckDocumentService();
        }
        return this.stuckDocumentService;
    }

    public void setStuckDocumentService(StuckDocumentService stuckDocumentService) {
        this.stuckDocumentService = stuckDocumentService;
    }
}
