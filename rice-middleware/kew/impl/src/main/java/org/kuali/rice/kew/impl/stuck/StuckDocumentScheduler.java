package org.kuali.rice.kew.impl.stuck;

import org.kuali.rice.core.api.config.property.RuntimeConfig;
import org.kuali.rice.core.api.config.property.RuntimeConfigSet;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Eric Westfall
 */
public class StuckDocumentScheduler implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(StuckDocumentScheduler.class);
    private static final JobKey JOB_KEY = JobKey.jobKey("Checker", "StuckDocuments");

    private Scheduler scheduler;
    private StuckDocumentNotifier notifier;

    private RuntimeConfig enabled;
    private RuntimeConfig autofix;
    private RuntimeConfig checkFrequency;
    private RuntimeConfig autofixQuietPeriod;
    private RuntimeConfig maxAutofixAttempts;

    private RuntimeConfigSet configSet;

    @Override
    public void afterPropertiesSet() throws Exception {
        configSet = new RuntimeConfigSet(enabled, autofix, checkFrequency, autofixQuietPeriod, maxAutofixAttempts);
        configChanged(configSet);
        configSet.listen(this::configChanged);
    }

    private void configChanged(RuntimeConfigSet configSet) {
        try {
            LOG.info("StuckDocumentScheduler config was changed, rebuilding job.");
            rebuildJob();
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to handle configuration change to stuck documents job", e);
        }
    }

    private void rebuildJob() throws SchedulerException {
        if (scheduler.checkExists(JOB_KEY)) {
            scheduler.deleteJob(JOB_KEY);
        }
        if (Boolean.valueOf(enabled.getValue())) {
            JobDetail job = JobBuilder.newJob(StuckDocumentJob.class)
                    .withIdentity(JOB_KEY)
                    .usingJobData("autofix", autofix.getValue())
                    .usingJobData("checkFrequency", autofix.getValue())
                    .usingJobData("autofixQuietPeriod", autofixQuietPeriod.getValue())
                    .usingJobData("maxAutofixAttempts", maxAutofixAttempts.getValue()).build();

            int checkFrequencySeconds = Integer.parseInt(checkFrequency.getValue());
            LOG.info("Stuck Documents job is enabled, scheduling for a frequency of " + checkFrequencySeconds + " seconds");
            ScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                    .repeatForever()
                    .withIntervalInSeconds(checkFrequencySeconds);
            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(job)
                    .startNow()
                    .withSchedule(scheduleBuilder).build();
            scheduler.scheduleJob(job, trigger);
        } else {
            LOG.info("Stuck Documents job is disabled.");
        }
    }

    @Required
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

//    @Required
//    public void setNotifier(StuckDocumentNotifier notifier) {
//        this.notifier = notifier;
//    }

    @Required
    public void setEnabled(RuntimeConfig enabled) {
        this.enabled = enabled;
    }

    @Required
    public void setAutofix(RuntimeConfig autofix) {
        this.autofix = autofix;
    }

    @Required
    public void setCheckFrequency(RuntimeConfig checkFrequency) {
        this.checkFrequency = checkFrequency;
    }

    @Required
    public void setAutofixQuietPeriod(RuntimeConfig autofixQuietPeriod) {
        this.autofixQuietPeriod = autofixQuietPeriod;
    }

    @Required
    public void setMaxAutofixAttempts(RuntimeConfig maxAutofixAttempts) {
        this.maxAutofixAttempts = maxAutofixAttempts;
    }

}
