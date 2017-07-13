package org.kuali.rice.kew.impl.stuck;

import org.kuali.rice.core.api.config.property.RuntimeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Eric Westfall
 */
public class StuckDocumentServiceImpl implements StuckDocumentService {

    private StuckDocumentDao stuckDocumentDao;

    @Override
    public List<String> findAllStuckDocumentIds() {
        return getStuckDocumentDao().findAllStuckDocumentIds();
    }

    @Override
    public StuckDocumentIncident findIncident(String stuckDocumentIncidentId) {
        checkNotNull(stuckDocumentIncidentId);
        return getStuckDocumentDao().findIncident(stuckDocumentIncidentId);
    }

    @Override
    public List<StuckDocumentIncident> findIncidents(List<String> stuckDocumentIncidentIds) {
        checkNotNull(stuckDocumentIncidentIds);
        List<StuckDocumentIncident> incidents = new ArrayList<>(stuckDocumentIncidentIds.size());
        for (String stuckDocumentIncidentId : stuckDocumentIncidentIds) {
            StuckDocumentIncident incident = findIncident(stuckDocumentIncidentId);
            if (incident != null) {
                incidents.add(incident);
            }
        }
        return incidents;
    }

    @Override
    public List<StuckDocumentIncident> recordNewStuckDocumentIncidents() {
        List<String> newStuckDocuments = getStuckDocumentDao().identifyNewStuckDocuments();
        return newStuckDocuments.stream().map(documentId -> getStuckDocumentDao().saveIncident(StuckDocumentIncident.startNewIncident(documentId))).collect(Collectors.toList());
    }

    @Override
    public StuckDocumentFixAttempt recordNewIncidentFixAttempt(StuckDocumentIncident stuckDocumentIncident) {
        checkNotNull(stuckDocumentIncident);
        StuckDocumentFixAttempt auditEntry = new StuckDocumentFixAttempt();
        auditEntry.setStuckDocumentIncidentId(stuckDocumentIncident.getStuckDocumentIncidentId());
        auditEntry.setTimestamp(new Timestamp(System.currentTimeMillis()));
        return getStuckDocumentDao().saveFixAttempt(auditEntry);
    }

    @Override
    public List<StuckDocumentIncident> resolveIncidentsIfPossible(List<String> stuckDocumentIncidentIds) {
        checkNotNull(stuckDocumentIncidentIds);
        List<StuckDocumentIncident> stuckIncidents = getStuckDocumentDao().identifyStillStuckDocuments(stuckDocumentIncidentIds);
        List<String> stuckIncidentIds = stuckIncidents.stream().map(StuckDocumentIncident::getStuckDocumentIncidentId).collect(Collectors.toList());
        // let's find the ones that aren't stuck so that we can resolve them
        List<String> notStuckIncidentIds = new ArrayList<>(stuckDocumentIncidentIds);
        notStuckIncidentIds.removeAll(stuckIncidentIds);
        if (!notStuckIncidentIds.isEmpty()) {
            List<StuckDocumentIncident> notStuckIncidents = findIncidents(notStuckIncidentIds);
            notStuckIncidents.forEach(this::resolve);
        }
        return stuckIncidents;
    }

    protected StuckDocumentIncident resolve(StuckDocumentIncident stuckDocumentIncident) {
        checkNotNull(stuckDocumentIncident);
        stuckDocumentIncident.setStatus(StuckDocumentIncident.Status.FIXED);
        stuckDocumentIncident.setEndDate(new Timestamp(System.currentTimeMillis()));
        return getStuckDocumentDao().saveIncident(stuckDocumentIncident);
    }

    @Override
    public StuckDocumentIncident startFixingIncident(StuckDocumentIncident stuckDocumentIncident) {
        checkNotNull(stuckDocumentIncident);
        stuckDocumentIncident.setStatus(StuckDocumentIncident.Status.FIXING);
        return getStuckDocumentDao().saveIncident(stuckDocumentIncident);
    }

    @Override
    public StuckDocumentIncident recordIncidentFailure(StuckDocumentIncident stuckDocumentIncident) {
        checkNotNull(stuckDocumentIncident);
        stuckDocumentIncident.setStatus(StuckDocumentIncident.Status.FAILED);
        stuckDocumentIncident.setEndDate(new Timestamp(System.currentTimeMillis()));
        return getStuckDocumentDao().saveIncident(stuckDocumentIncident);
    }

    protected StuckDocumentDao getStuckDocumentDao() {
        return stuckDocumentDao;
    }

    @Required
    public void setStuckDocumentDao(StuckDocumentDao stuckDocumentDao) {
        this.stuckDocumentDao = stuckDocumentDao;
    }

}
