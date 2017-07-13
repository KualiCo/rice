package org.kuali.rice.kew.impl.stuck;

import org.kuali.rice.kew.service.KEWServiceLocator;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by ewestfal on 6/28/17.
 */
public class StuckDocumentNotificationJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(StuckDocumentNotificationJob.class);

    private volatile StuckDocumentService stuckDocumentService;
    private volatile StuckDocumentNotifier notifier;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        checkDependenciesAvailable();
        List<String> stuckDocumentIds = getStuckDocumentService().findAllStuckDocumentIds();
        if (!stuckDocumentIds.isEmpty()) {
            getNotifier().notify(stuckDocumentIds);
        }
    }

    /**
     * Checks if needed dependencies are available in order to run this job. Due to the fact that this is a quartz job,
     * it could trigger while the system is offline and then immediately get fired when the system starts up and due to
     * the startup process it could attempt to execute while not all of the necessary services are fully initialized.
     */
    private void checkDependenciesAvailable() throws JobExecutionException {
        if (getStuckDocumentService() == null || getNotifier() == null) {
            String message = "Dependencies are not available for the stuck document notification job";
            LOG.warn(message);
            throw new JobExecutionException(message);
        }
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


    protected StuckDocumentNotifier getNotifier() {
        if (this.notifier == null) {
            this.notifier = KEWServiceLocator.getStuckDocumentNotifier();
        }
        return notifier;
    }

    public void setNotifier(StuckDocumentNotifier notifier) {
        this.notifier = notifier;
    }
}
