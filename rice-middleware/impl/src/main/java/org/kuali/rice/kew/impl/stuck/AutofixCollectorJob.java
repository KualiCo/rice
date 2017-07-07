package org.kuali.rice.kew.impl.stuck;

import com.google.common.collect.Lists;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.quartz.DateBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Eric Westfall
 */
public class AutofixCollectorJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(AutofixCollectorJob.class);
    private static final int PARTITION_SIZE = 50;
    private static final String AUTOFIX_JOB_KEY_PREFIX = "Autofix Documents Job - ";

    static final String AUTOFIX_QUIET_PERIOD_KEY = "autofixQuietPeriod";
    static final String AUTOFIX_MAX_ATTEMPTS_KEY = "autofixMaxAttempts";

    private volatile StuckDocumentService stuckDocumentService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (dependenciesAvailable()) {
            List<StuckDocumentIncident> newIncidents =
                    getStuckDocumentService().recordNewStuckDocumentIncidents();
            if (!newIncidents.isEmpty()) {
                LOG.info("Identified " + newIncidents.size() + " new stuck documents");
                LOG.info("Scheduling jobs to attempt to fix the following documents: "
                        + newIncidents.stream().map(StuckDocumentIncident::getDocumentId).collect(Collectors.joining(", ")));
                partitionAndScheduleAutofixJobs(newIncidents, context);
            }
        }
    }

    /**
     * Checks if needed dependencies are available in order to run this job. Due to the fact that this is a quartz job,
     * it could trigger while the system is offline and then immediately get fired when the system starts up and due to
     * the startup process it could attempt to execute while not all of the necessary services are fully initialized.
     */
    private boolean dependenciesAvailable() {
        return getStuckDocumentService() != null;
    }

    private void partitionAndScheduleAutofixJobs(List<StuckDocumentIncident> incidents, JobExecutionContext context) {
        Lists.partition(incidents, PARTITION_SIZE).forEach(incidentsPartition -> scheduleAutofixJobs(incidentsPartition, context));
    }

    private void scheduleAutofixJobs(List<StuckDocumentIncident> incidents, JobExecutionContext context) {
        List<String> incidentIds = incidents.stream().map(StuckDocumentIncident::getStuckDocumentIncidentId).collect(Collectors.toList());
        String jobKey = generateAutofixJobKey();
        int autofixQuietPeriod = autofixQuietPeriod(context);
        int autofixMaxAttempts = autofixMaxAttempts(context);
        JobDetail job = JobBuilder.newJob(AutofixDocumentsJob.class)
                .withIdentity(jobKey)
                .usingJobData(context.getMergedJobDataMap())
                .usingJobData(AutofixDocumentsJob.INCIDENT_IDS, String.join(",", incidentIds))
                .usingJobData(AutofixDocumentsJob.CURRENT_AUTOFIX_COUNT, 0)
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(job)
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(autofixQuietPeriod)
                        .withRepeatCount(autofixMaxAttempts)
                        .withMisfireHandlingInstructionNextWithExistingCount())
                .startAt(DateBuilder.futureDate(autofixQuietPeriod, DateBuilder.IntervalUnit.SECOND))
                .build();
        try {
            context.getScheduler().scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to schedule autofix job", e);
        }
    }

    private String generateAutofixJobKey() {
        return AUTOFIX_JOB_KEY_PREFIX + UUID.randomUUID().toString();
    }

    private int autofixMaxAttempts(JobExecutionContext context) {
        return context.getMergedJobDataMap().getInt(AUTOFIX_MAX_ATTEMPTS_KEY);
    }

    private int autofixQuietPeriod(JobExecutionContext context) {
        return context.getMergedJobDataMap().getInt(AUTOFIX_QUIET_PERIOD_KEY);
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
