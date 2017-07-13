package org.kuali.rice.kew.impl.stuck;

import org.springframework.beans.factory.annotation.Required;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Eric Westfall
 */
public class StuckDocumentDaoJpa implements StuckDocumentDao {

    private static final String NEW_STUCK_DOCUMENT_ACTION_ITEM_SQL =
            "select DH.DOC_HDR_ID from KREW_DOC_HDR_T DH " +
                    "left outer join KREW_ACTN_ITM_T AI on DH.DOC_HDR_ID=AI.DOC_HDR_ID " +
                    "left outer join (select DOC_HDR_ID, STATUS, START_DT, END_DT from KREW_STUCK_DOC_INCIDENT_T where (DOC_HDR_ID, START_DT) in (select DOC_HDR_ID, MAX(START_DT) from KREW_STUCK_DOC_INCIDENT_T group by DOC_HDR_ID)) SD " +
                    "on DH.DOC_HDR_ID=SD.DOC_HDR_ID " +
                    "where DH.DOC_HDR_STAT_CD='R' AND AI.DOC_HDR_ID IS NULL and " +
                    "(SD.DOC_HDR_ID IS NULL OR SD.STATUS='FIXED' OR (SD.STATUS='FAILED' AND DH.STAT_MDFN_DT > SD.END_DT))";

    private static final String NEW_STUCK_DOCUMENT_ACTION_REQUEST_SQL =
            "select DH.DOC_HDR_ID from KREW_DOC_HDR_T DH " +
                    "left outer join (select DOC_HDR_ID, STAT_CD from KREW_ACTN_RQST_T AR where AR.STAT_CD='A') AR on DH.DOC_HDR_ID=AR.DOC_HDR_ID " +
                    "left outer join (select DOC_HDR_ID, STATUS, START_DT, END_DT from KREW_STUCK_DOC_INCIDENT_T where (DOC_HDR_ID, START_DT) in (select DOC_HDR_ID, MAX(START_DT) from KREW_STUCK_DOC_INCIDENT_T group by DOC_HDR_ID)) as SD " +
                    "on DH.DOC_HDR_ID=SD.DOC_HDR_ID " +
                    "where DH.DOC_HDR_STAT_CD='R' and AR.DOC_HDR_ID IS NULL and " +
                    "(SD.DOC_HDR_ID IS NULL OR SD.STATUS='FIXED' OR (SD.STATUS='FAILED' AND DH.STAT_MDFN_DT > SD.END_DT))";

    private static final String ALL_STUCK_DOCUMENT_ACTION_ITEM_SQL =
            "select DH.DOC_HDR_ID from KREW_DOC_HDR_T DH " +
                    "left outer join KREW_ACTN_ITM_T AI on DH.DOC_HDR_ID=AI.DOC_HDR_ID " +
                    "where DH.DOC_HDR_STAT_CD='R' AND AI.DOC_HDR_ID IS NULL";

    private static final String ALL_STUCK_DOCUMENT_ACTION_REQUEST_SQL =
            "select DH.DOC_HDR_ID from KREW_DOC_HDR_T DH left outer join KREW_ACTN_RQST_T AR on " +
                    "DH.DOC_HDR_ID=AR.DOC_HDR_ID AND AR.STAT_CD = 'A' where DH.DOC_HDR_STAT_CD='R' " +
                    "AND AR.DOC_HDR_ID IS NULL";

    private static final String IS_STUCK_DOCUMENT_ACTION_ITEM_SQL =
            ALL_STUCK_DOCUMENT_ACTION_ITEM_SQL + " AND DH.DOC_HDR_ID = ?";

    private static final String IS_STUCK_DOCUMENT_ACTION_REQUEST_SQL =
            ALL_STUCK_DOCUMENT_ACTION_REQUEST_SQL + " AND DH.DOC_HDR_ID = ?";



    private EntityManager entityManager;

    @Override
    public List<String> findAllStuckDocumentIds() {
        Set<String> documentIds = new HashSet<>();
        documentIds.addAll(entityManager.createNativeQuery(ALL_STUCK_DOCUMENT_ACTION_ITEM_SQL).getResultList());
        documentIds.addAll(entityManager.createNativeQuery(ALL_STUCK_DOCUMENT_ACTION_REQUEST_SQL).getResultList());
        return Collections.unmodifiableList(new ArrayList<>(documentIds));
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
        Set<String> documentIds = new HashSet<>();
        documentIds.addAll(entityManager.createNativeQuery(NEW_STUCK_DOCUMENT_ACTION_ITEM_SQL).getResultList());
        documentIds.addAll(entityManager.createNativeQuery(NEW_STUCK_DOCUMENT_ACTION_REQUEST_SQL).getResultList());
        return Collections.unmodifiableList(new ArrayList<>(documentIds));
    }

    public boolean isStuck(String documentId) {
        Query aiQuery = entityManager.createNativeQuery(IS_STUCK_DOCUMENT_ACTION_ITEM_SQL);
        Query arQuery = entityManager.createNativeQuery(IS_STUCK_DOCUMENT_ACTION_REQUEST_SQL);
        aiQuery.setParameter(1, documentId);
        arQuery.setParameter(1, documentId);
        return !aiQuery.getResultList().isEmpty() || !arQuery.getResultList().isEmpty();
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