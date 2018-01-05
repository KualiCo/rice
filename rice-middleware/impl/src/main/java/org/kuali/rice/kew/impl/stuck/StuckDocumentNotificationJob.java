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
        List<StuckDocument> stuckDocuments = getStuckDocumentService().findAllStuckDocuments();
        if (!stuckDocuments.isEmpty()) {
            getNotifier().notify(stuckDocuments);
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
