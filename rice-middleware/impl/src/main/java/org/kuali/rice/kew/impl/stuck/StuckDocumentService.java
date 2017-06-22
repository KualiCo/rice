package org.kuali.rice.kew.impl.stuck;

import java.util.List;

/**
 * @author Eric Westfall
 */
public interface StuckDocumentService {

    StuckDocumentIncident find(String stuckDocumentIncidentId);

    List<StuckDocumentIncident> findAll(List<String> stuckDocumentIncidentIds);

    List<StuckDocumentIncident> identifyAndRecordNewStuckDocuments();

    StuckDocumentFixAttempt recordNewFixAttempt(StuckDocumentIncident stuckDocumentIncident);

    List<StuckDocumentIncident> resolveIfPossible(List<String> stuckDocumentIncidentIds);

    StuckDocumentIncident recordFailure(StuckDocumentIncident stuckDocumentIncident);

    StuckDocumentIncident startFixing(StuckDocumentIncident stuckDocumentIncident);

}
