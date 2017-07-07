package org.kuali.rice.kew.impl.stuck;

import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class AutofixDocumentsJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(AutofixDocumentsJob.class);

    static final String INCIDENT_IDS = "incidentIds";
    static final String CURRENT_AUTOFIX_COUNT = "currentAutofixCount";

    private volatile StuckDocumentService stuckDocumentService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        int currentAutofixCount = incrementAutofixCount(context);
        List<String> incidentIds = incidentIds(context);
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
        tryToFix(incident);
    }

    private void tryToFix(StuckDocumentIncident incident) {
        getStuckDocumentService().recordNewIncidentFixAttempt(incident);
        String docId = incident.getDocumentId();
        DocumentType documentType = KEWServiceLocator.getDocumentTypeService().findByDocumentId(docId);
        KewApiServiceLocator.getDocumentProcessingQueue(docId, documentType.getApplicationId()).process(docId);
    }

    private int autofixMaxAttempts(JobExecutionContext context) {
        return context.getMergedJobDataMap().getInt(AutofixCollectorJob.AUTOFIX_MAX_ATTEMPTS_KEY);
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
