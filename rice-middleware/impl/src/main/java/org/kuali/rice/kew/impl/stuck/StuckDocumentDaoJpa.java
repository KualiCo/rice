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

import org.springframework.beans.factory.annotation.Required;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
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
            "select DH.DOC_HDR_ID, DH.CRTE_DT, DT.LBL from KREW_DOC_HDR_T DH " +
                    "left outer join KREW_ACTN_ITM_T AI on DH.DOC_HDR_ID=AI.DOC_HDR_ID " +
                    "join KREW_DOC_TYP_T DT on DH.DOC_TYP_ID=DT.DOC_TYP_ID " +
                    "where DH.DOC_HDR_STAT_CD='R' AND AI.DOC_HDR_ID IS NULL";

    private static final String ALL_STUCK_DOCUMENT_ACTION_REQUEST_SQL =
            "select DH.DOC_HDR_ID, DH.CRTE_DT, DT.LBL from KREW_DOC_HDR_T DH " +
                    "left outer join KREW_ACTN_RQST_T AR on DH.DOC_HDR_ID=AR.DOC_HDR_ID AND AR.STAT_CD = 'A' " +
                    "join KREW_DOC_TYP_T DT on DH.DOC_TYP_ID=DT.DOC_TYP_ID " +
                    "where DH.DOC_HDR_STAT_CD='R' AND AR.DOC_HDR_ID IS NULL";

    private static final String IS_STUCK_DOCUMENT_ACTION_ITEM_SQL =
            ALL_STUCK_DOCUMENT_ACTION_ITEM_SQL + " AND DH.DOC_HDR_ID = ?";

    private static final String IS_STUCK_DOCUMENT_ACTION_REQUEST_SQL =
            ALL_STUCK_DOCUMENT_ACTION_REQUEST_SQL + " AND DH.DOC_HDR_ID = ?";

    static final String FIX_ATTEMPTS_FOR_INCIDENT_NAME = "StuckDocumentFixAttempt.FixAttemptsForIncident";
    static final String FIX_ATTEMPTS_FOR_INCIDENT_QUERY = "select fa from StuckDocumentFixAttempt fa where fa.stuckDocumentIncidentId = :stuckDocumentIncidentId";



    private EntityManager entityManager;

    @Override
    public List<String> findAllStuckDocumentIds() {
        return findAllStuckDocuments().stream().map(StuckDocument::getDocumentId).collect(Collectors.toList());
    }

    @Override
    public List<StuckDocument> findAllStuckDocuments() {
        List<Object[]> stuckDocumentResults = new ArrayList<>();
        stuckDocumentResults.addAll(entityManager.createNativeQuery(ALL_STUCK_DOCUMENT_ACTION_ITEM_SQL).getResultList());
        stuckDocumentResults.addAll(entityManager.createNativeQuery(ALL_STUCK_DOCUMENT_ACTION_REQUEST_SQL).getResultList());
        List<StuckDocument> unfilteredStuckDocuments = stuckDocumentResults.stream().map(result -> new StuckDocument((String)result[0], (String)result[2], ((Timestamp)result[1]).toLocalDateTime())).collect(Collectors.toList());
        return filterDuplicateStuckDocuments(unfilteredStuckDocuments);
    }

    private List<StuckDocument> filterDuplicateStuckDocuments(List<StuckDocument> unfilteredStuckDocuments) {
        Set<String> processedDocumentIds = new HashSet<>();
        return unfilteredStuckDocuments.stream().filter(stuckDocument -> {
            if (processedDocumentIds.contains(stuckDocument.getDocumentId())) {
                return false;
            }
            processedDocumentIds.add(stuckDocument.getDocumentId());
            return true;
        }).collect(Collectors.toList());
    }

    @Override
    public StuckDocumentIncident findIncident(String stuckDocumentIncidentId) {
        return entityManager.find(StuckDocumentIncident.class, stuckDocumentIncidentId);
    }

    @Override
    public StuckDocumentIncident saveIncident(StuckDocumentIncident incident) {
        return entityManager.merge(incident);
    }

    public void deleteIncident(StuckDocumentIncident incident) {
        entityManager.remove(incident);
    }

    @Override
    public List<StuckDocumentIncident> findAllIncidents(int maxIncidents) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<StuckDocumentIncident> q = cb.createQuery(StuckDocumentIncident.class);
        Root<StuckDocumentIncident> incident = q.from(StuckDocumentIncident.class);
        q.select(incident).orderBy(cb.desc(incident.get("startDate")));
        TypedQuery<StuckDocumentIncident> query = getEntityManager().createQuery(q);
        query.setMaxResults(maxIncidents);
        return new ArrayList<>(query.getResultList());
    }

    @Override
    public List<StuckDocumentIncident> findIncidentsByStatus(int maxIncidents, StuckDocumentIncident.Status status) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<StuckDocumentIncident> q = cb.createQuery(StuckDocumentIncident.class);
        Root<StuckDocumentIncident> incident = q.from(StuckDocumentIncident.class);
        q.select(incident).orderBy(cb.desc(incident.get("startDate")));
        ParameterExpression<StuckDocumentIncident.Status> statusParameter = cb.parameter(StuckDocumentIncident.Status.class);
        q.where(cb.equal(incident.get("status"), statusParameter));
        TypedQuery<StuckDocumentIncident> query = getEntityManager().createQuery(q);
        query.setParameter(statusParameter, status);
        query.setMaxResults(maxIncidents);
        return new ArrayList<>(query.getResultList());
    }

    @Override
    public List<StuckDocumentFixAttempt> findAllFixAttempts(String stuckDocumentIncidentId) {
        TypedQuery<StuckDocumentFixAttempt> query = getEntityManager().createNamedQuery(FIX_ATTEMPTS_FOR_INCIDENT_NAME, StuckDocumentFixAttempt.class);
        query.setParameter("stuckDocumentIncidentId", stuckDocumentIncidentId);
        return new ArrayList<>(query.getResultList());
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