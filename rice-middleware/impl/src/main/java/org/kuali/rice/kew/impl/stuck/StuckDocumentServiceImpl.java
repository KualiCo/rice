package org.kuali.rice.kew.impl.stuck;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Eric Westfall
 */
public class StuckDocumentServiceImpl implements StuckDocumentService {

    private StuckDocumentDao stuckDocumentDao;

    @Override
    public StuckDocumentIncident find(String stuckDocumentIncidentId) {
        return stuckDocumentDao.findIncident(stuckDocumentIncidentId);
    }

    @Override
    public List<StuckDocumentIncident> findAll(List<String> stuckDocumentIncidentIds) {
        List<StuckDocumentIncident> incidents = new ArrayList<>(stuckDocumentIncidentIds.size());
        for (String stuckDocumentIncidentId : stuckDocumentIncidentIds) {
            StuckDocumentIncident incident = find(stuckDocumentIncidentId);
            if (incident != null) {
                incidents.add(incident);
            }
        }
        return incidents;
    }

    @Override
    public List<StuckDocumentIncident> identifyAndRecordNewStuckDocuments() {
        List<String> newStuckDocuments = getStuckDocumentDao().identifyNewStuckDocuments();
        return newStuckDocuments.stream().map(documentId -> getStuckDocumentDao().saveIncident(StuckDocumentIncident.startNewIncident(documentId))).collect(Collectors.toList());
    }

    @Override
    public StuckDocumentFixAttempt recordNewFixAttempt(StuckDocumentIncident incident) {
        StuckDocumentFixAttempt auditEntry = new StuckDocumentFixAttempt();
        auditEntry.setStuckDocumentIncidentId(incident.getStuckDocumentIncidentId());
        auditEntry.setTimestamp(new Timestamp(System.currentTimeMillis()));
        return getStuckDocumentDao().saveFixAttempt(auditEntry);
    }

    @Override
    public List<StuckDocumentIncident> resolveIfPossible(List<String> incidentIds) {
        List<StuckDocumentIncident> stuckIncidents = getStuckDocumentDao().identifyStillStuckDocuments(incidentIds);
        List<String> stuckIncidentIds = stuckIncidents.stream().map(StuckDocumentIncident::getStuckDocumentIncidentId).collect(Collectors.toList());
        // let's find the ones that aren't stuck so that we can resolve them
        List<String> notStuckIncidentIds = new ArrayList<>(incidentIds);
        notStuckIncidentIds.removeAll(stuckIncidentIds);
        if (!notStuckIncidentIds.isEmpty()) {
            List<StuckDocumentIncident> notStuckIncidents = findAll(notStuckIncidentIds);
            notStuckIncidents.forEach(incident -> {
                incident.setStatus(StuckDocumentIncident.Status.FIXED);
                incident.setEndDate(new Timestamp(System.currentTimeMillis()));
            });
        }
        return stuckIncidents;
    }

    public StuckDocumentIncident resolve(StuckDocumentIncident stuckDocumentIncident) {
        stuckDocumentIncident.setStatus(StuckDocumentIncident.Status.FIXED);
        stuckDocumentIncident.setEndDate(new Timestamp(System.currentTimeMillis()));
        return getStuckDocumentDao().saveIncident(stuckDocumentIncident);
    }

    @Override
    public StuckDocumentIncident startFixing(StuckDocumentIncident stuckDocumentIncident) {
        stuckDocumentIncident.setStatus(StuckDocumentIncident.Status.FIXING);
        return getStuckDocumentDao().saveIncident(stuckDocumentIncident);
    }

    @Override
    public StuckDocumentIncident recordFailure(StuckDocumentIncident stuckDocumentIncident) {
        stuckDocumentIncident.setStatus(StuckDocumentIncident.Status.FAILED);
        stuckDocumentIncident.setEndDate(new Timestamp(System.currentTimeMillis()));
        return getStuckDocumentDao().saveIncident(stuckDocumentIncident);
    }

    public StuckDocumentDao getStuckDocumentDao() {
        return stuckDocumentDao;
    }

    public void setStuckDocumentDao(StuckDocumentDao stuckDocumentDao) {
        this.stuckDocumentDao = stuckDocumentDao;
    }

}
