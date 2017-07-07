package org.kuali.rice.kew.impl.stuck;

import org.springframework.beans.factory.annotation.Required;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Eric Westfall
 */
public class StuckDocumentDaoJpa implements StuckDocumentDao {

    private static final String NEW_STUCK_DOCUMENT_SQL =
            "select DH.DOC_HDR_ID from KREW_DOC_HDR_T DH " +
                    "left outer join KREW_ACTN_ITM_T AI on DH.DOC_HDR_ID=AI.DOC_HDR_ID " +
                    "left outer join KREW_STUCK_DOC_INCIDENT_T SD on DH.DOC_HDR_ID=SD.DOC_HDR_ID " +
                    "where DH.DOC_HDR_STAT_CD='R' AND AI.DOC_HDR_ID IS NULL and " +
                    "(SD.DOC_HDR_ID IS NULL OR SD.STATUS='FIXED' OR (SD.STATUS='FAILED' AND DH.STAT_MDFN_DT > SD.END_DT))";

    private static final String EXISTING_STUCK_DOCUMENT_SQL =
            "select DH.DOC_HDR_ID from KREW_DOC_HDR_T DH " +
                    "left outer join KREW_ACTN_ITM_T AI on DH.DOC_HDR_ID=AI.DOC_HDR_ID " +
                    "where DH.DOC_HDR_STAT_CD='R' AND AI.DOC_HDR_ID IS NULL AND DH.DOC_HDR_ID = ?";

    private static final String ALL_STUCK_DOCUMENT_SQL =
            "select DH.DOC_HDR_ID from KREW_DOC_HDR_T DH " +
                    "left outer join KREW_ACTN_ITM_T AI on DH.DOC_HDR_ID=AI.DOC_HDR_ID " +
                    "where DH.DOC_HDR_STAT_CD='R' AND AI.DOC_HDR_ID IS NULL";

    private EntityManager entityManager;

    @Override
    public List<String> findAllStuckDocumentIds() {
        Query query = entityManager.createNativeQuery(ALL_STUCK_DOCUMENT_SQL);
        return Collections.unmodifiableList((List<String>)query.getResultList());
    }

    @Override
    public StuckDocumentIncident findIncident(String stuckDocumentIncidentId) {
        return entityManager.find(StuckDocumentIncident.class, stuckDocumentIncidentId);
    }

    @Override
    public StuckDocumentIncident saveIncident(StuckDocumentIncident incident) {
        return entityManager.merge(incident);
    }

    @Override
    public StuckDocumentFixAttempt findFixAttempt(String stuckDocumentAuditEntryId) {
        return entityManager.find(StuckDocumentFixAttempt.class, stuckDocumentAuditEntryId);
    }

    @Override
    public StuckDocumentFixAttempt saveFixAttempt(StuckDocumentFixAttempt auditEntry) {
        return entityManager.merge(auditEntry);
    }

    @Override
    public List<String> identifyNewStuckDocuments() {
        Query query = entityManager.createNativeQuery(NEW_STUCK_DOCUMENT_SQL);
        return Collections.unmodifiableList((List<String>)query.getResultList());
    }

    public boolean isStuck(String documentId) {
        Query query = entityManager.createNativeQuery(EXISTING_STUCK_DOCUMENT_SQL);
        query.setParameter(1, documentId);
        return !query.getResultList().isEmpty();
    }

    @Override
    public List<StuckDocumentIncident> identifyStillStuckDocuments(List<String> incidentIds) {
        return incidentIds.stream().map(this::findIncident).filter(incident -> isStuck(incident.getDocumentId())).collect(Collectors.toList());
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Required
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}