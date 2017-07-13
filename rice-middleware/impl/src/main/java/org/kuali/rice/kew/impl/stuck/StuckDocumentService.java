package org.kuali.rice.kew.impl.stuck;

import java.util.List;

/**
 * @author Eric Westfall
 */
public interface StuckDocumentService {

    List<String> findAllStuckDocumentIds();

    StuckDocumentIncident findIncident(String stuckDocumentIncidentId);

    List<StuckDocumentIncident> findIncidents(List<String> stuckDocumentIncidentIds);

    List<StuckDocumentIncident> recordNewStuckDocumentIncidents();

    StuckDocumentFixAttempt recordNewIncidentFixAttempt(StuckDocumentIncident stuckDocumentIncident);

    List<StuckDocumentIncident> resolveIncidentsIfPossible(List<String> stuckDocumentIncidentIds);

    StuckDocumentIncident recordIncidentFailure(StuckDocumentIncident stuckDocumentIncident);

    StuckDocumentIncident startFixingIncident(StuckDocumentIncident stuckDocumentIncident);

}
