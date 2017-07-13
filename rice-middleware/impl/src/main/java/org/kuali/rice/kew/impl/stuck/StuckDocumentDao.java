package org.kuali.rice.kew.impl.stuck;

import java.util.List;

/**
 * Created by ewestfal on 5/18/17.
 */
public interface StuckDocumentDao {

    List<String> findAllStuckDocumentIds();

    StuckDocumentIncident findIncident(String stuckDocumentIncidentId);

    StuckDocumentIncident saveIncident(StuckDocumentIncident incident);

    StuckDocumentFixAttempt findFixAttempt(String stuckDocumentIncidentId);

    StuckDocumentFixAttempt saveFixAttempt(StuckDocumentFixAttempt incident);

    List<String> identifyNewStuckDocuments();

    List<StuckDocumentIncident> identifyStillStuckDocuments(List<String> incidentIds);

}
