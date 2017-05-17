package org.kuali.rice.kew.impl.stuck;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ewestfal on 5/16/17.
 */
public class StuckDocumentJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(StuckDocumentJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // TODO
        LOG.info("Checking for stuck documents...");
    }

}
