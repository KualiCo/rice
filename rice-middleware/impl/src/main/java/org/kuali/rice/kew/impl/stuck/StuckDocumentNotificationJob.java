package org.kuali.rice.kew.impl.stuck;

import org.kuali.rice.kew.service.KEWServiceLocator;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 * Created by ewestfal on 6/28/17.
 */
public class StuckDocumentNotificationJob implements Job {

    private volatile StuckDocumentService stuckDocumentService;
    private volatile StuckDocumentNotifier notifier;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        List<String> stuckDocumentIds = getStuckDocumentService().findAllStuckDocumentIds();
        if (!stuckDocumentIds.isEmpty()) {
            getNotifier().notify(stuckDocumentIds);
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
