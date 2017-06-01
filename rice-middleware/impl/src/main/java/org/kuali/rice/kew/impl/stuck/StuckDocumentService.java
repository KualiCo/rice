package org.kuali.rice.kew.impl.stuck;

import java.util.List;

/**
 * @author Eric Westfall
 */
public interface StuckDocumentService {

    StuckDocumentIncident find(String stuckDocumentIncidentId);

    List<StuckDocumentIncident> findAll(List<String> stuckDocumentIds);

    List<StuckDocumentIncident> identifyAndRecordNewStuckDocuments();

    StuckDocumentFixAttempt recordNewFixAttempt(StuckDocumentIncident incident);

    List<StuckDocumentIncident> resolveIfPossible(List<String> incidentIds);

    StuckDocumentIncident recordFailure(StuckDocumentIncident incident);

    StuckDocumentIncident startFixing(StuckDocumentIncident stuckDocumentIncident);

}
