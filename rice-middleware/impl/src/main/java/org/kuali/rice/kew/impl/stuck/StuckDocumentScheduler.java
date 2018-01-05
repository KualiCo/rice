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

import org.kuali.rice.core.api.config.property.RuntimeConfig;
import org.kuali.rice.core.api.config.property.RuntimeConfigSet;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author Eric Westfall
 */
public class StuckDocumentScheduler implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(StuckDocumentScheduler.class);

    protected static final JobKey NOTIFICATION_JOB_KEY = JobKey.jobKey("StuckDocuments", "Notification");
    protected static final JobKey AUTOFIX_COLLECTOR_JOB_KEY = JobKey.jobKey("StuckDocuments", "AutofixCollector");

    private Scheduler scheduler;

    private RuntimeConfig notificationEnabled;
    private RuntimeConfig notificationCronExpression;

    private RuntimeConfig autofixEnabled;
    private RuntimeConfig autofixCronExpression;
    private RuntimeConfig autofixQuietPeriod;
    private RuntimeConfig autofixMaxAttempts;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        achieveDesiredState();
        RuntimeConfigSet configSet = new RuntimeConfigSet(notificationEnabled, notificationCronExpression, autofixEnabled, autofixCronExpression, autofixQuietPeriod, autofixMaxAttempts);
        configSet.listen(this::configChanged);
    }

    private void configChanged(RuntimeConfigSet configSet) {
        LOG.info("StuckDocumentScheduler config was changed, rebuilding job.");
        achieveDesiredState();
    }

    private void achieveDesiredState() {
        try {
            scheduleNotificationJob();
            scheduleAutofixCollectorJob();
        } catch (SchedulerException e) {
            throw new IllegalStateException("Scheduling failure when attempting to configure Stuck Document jobs", e);
        }
    }

    private void scheduleNotificationJob() throws SchedulerException {
        unscheduleJobIfExists(NOTIFICATION_JOB_KEY);
        if (notificationEnabled.getValueAsBoolean()) {
            JobDetail job = JobBuilder.newJob(StuckDocumentNotificationJob.class)
                    .withIdentity(NOTIFICATION_JOB_KEY).build();

            LOG.info("Stuck Documents Notification job is enabled, scheduling with cron expression " + notificationCronExpression.getValue());
            CronScheduleBuilder scheduleBuilder =
                    CronScheduleBuilder.cronSchedule(notificationCronExpression.getValue())
                            .withMisfireHandlingInstructionDoNothing();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(job)
                    .startNow()
                    .withSchedule(scheduleBuilder).build();
            scheduler().scheduleJob(job, trigger);
        } else {
            LOG.info("Stuck Documents Notification job is disabled.");
        }
    }

    private void scheduleAutofixCollectorJob() throws SchedulerException {
        unscheduleJobIfExists(AUTOFIX_COLLECTOR_JOB_KEY);
        if (autofixEnabled.getValueAsBoolean()) {
            JobDetail job = JobBuilder.newJob(AutofixCollectorJob.class)
                    .withIdentity(AUTOFIX_COLLECTOR_JOB_KEY)
                    .usingJobData(AutofixCollectorJob.AUTOFIX_QUIET_PERIOD_KEY, autofixQuietPeriod.getValueAsInteger())
                    .usingJobData(AutofixCollectorJob.AUTOFIX_MAX_ATTEMPTS_KEY, autofixMaxAttempts.getValueAsInteger()).build();

            LOG.info("Stuck Documents Autofix job is enabled, scheduling with cron expression " + autofixCronExpression.getValue());
            CronScheduleBuilder scheduleBuilder =
                    CronScheduleBuilder.cronSchedule(autofixCronExpression.getValue())
                            .withMisfireHandlingInstructionDoNothing();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(job)
                    .startNow()
                    .withSchedule(scheduleBuilder).build();
            scheduler().scheduleJob(job, trigger);
        } else {
            LOG.info("Stuck Documents Autofix job is disabled.");
        }
    }

    private void unscheduleJobIfExists(JobKey jobKey) throws SchedulerException {
       if (scheduler != null && scheduler().checkExists(jobKey)) {
            scheduler().deleteJob(jobKey);
        }
    }

    private Scheduler scheduler() {
        if (this.scheduler == null) {
            throw new IllegalStateException("StuckDocumentScheduler is trying to use the Scheduler but none exists!");
        }
        return this.scheduler;
    }

    /**
     * Not marked as required because it may be null if running KSB in THIN mode. This is mostly to accomodate the way
     * that KFS has Rice wired up in their integration tests.
     */
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Required
    public void setNotificationEnabled(RuntimeConfig notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    @Required
    public void setNotificationCronExpression(RuntimeConfig notificationCronExpression) {
        this.notificationCronExpression = notificationCronExpression;
    }

    @Required
    public void setAutofixEnabled(RuntimeConfig autofixEnabled) {
        this.autofixEnabled = autofixEnabled;
    }

    @Required
    public void setAutofixCronExpression(RuntimeConfig autofixCronExpression) {
        this.autofixCronExpression = autofixCronExpression;
    }

    @Required
    public void setAutofixQuietPeriod(RuntimeConfig autofixQuietPeriod) {
        this.autofixQuietPeriod = autofixQuietPeriod;
    }

    @Required
    public void setAutofixMaxAttempts(RuntimeConfig autofixMaxAttempts) {
        this.autofixMaxAttempts = autofixMaxAttempts;
    }

}
