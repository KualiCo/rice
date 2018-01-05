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
    private StuckDocumentNotifier notifier;
    private RuntimeConfig failureNotificationEnabled;

    @Override
    public List<String> findAllStuckDocumentIds() {
        return getStuckDocumentDao().findAllStuckDocumentIds();
    }

    @Override
    public List<StuckDocument> findAllStuckDocuments() {
        return getStuckDocumentDao().findAllStuckDocuments();
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
    public List<StuckDocumentIncident> findAllIncidents(int maxIncidents) {
        return getStuckDocumentDao().findAllIncidents(maxIncidents);
    }

    @Override
    public List<StuckDocumentIncident> findIncidentsByStatus(int maxIncidents, StuckDocumentIncident.Status status) {
        return getStuckDocumentDao().findIncidentsByStatus(maxIncidents, status);
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
    public List<StuckDocumentFixAttempt> findAllFixAttempts(String stuckDocumentIncidentId) {
        return getStuckDocumentDao().findAllFixAttempts(stuckDocumentIncidentId);
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
        if (stuckDocumentIncident.getStatus().equals(StuckDocumentIncident.Status.PENDING)) {
            // if it was pending that means we just went through the quiet period and the document unstuck itself,
            // let's get rid of it's incident since it's just noise
            getStuckDocumentDao().deleteIncident(stuckDocumentIncident);
            return stuckDocumentIncident;
        } else {
            stuckDocumentIncident.setStatus(StuckDocumentIncident.Status.FIXED);
            stuckDocumentIncident.setEndDate(new Timestamp(System.currentTimeMillis()));
            return getStuckDocumentDao().saveIncident(stuckDocumentIncident);
        }
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
        stuckDocumentIncident = getStuckDocumentDao().saveIncident(stuckDocumentIncident);
        notifyIncidentFailure(stuckDocumentIncident);
        return stuckDocumentIncident;
    }

    protected void notifyIncidentFailure(StuckDocumentIncident stuckDocumentIncident) {
        if (getFailureNotificationEnabled().getValueAsBoolean()) {
            List<StuckDocumentFixAttempt> attempts = getStuckDocumentDao().findAllFixAttempts(stuckDocumentIncident.getStuckDocumentIncidentId());
            notifier.notifyIncidentFailure(stuckDocumentIncident, attempts);
        }
    }

    protected StuckDocumentDao getStuckDocumentDao() {
        return stuckDocumentDao;
    }

    @Required
    public void setStuckDocumentDao(StuckDocumentDao stuckDocumentDao) {
        this.stuckDocumentDao = stuckDocumentDao;
    }

    protected StuckDocumentNotifier getNotifier() {
        return notifier;
    }

    @Required
    public void setNotifier(StuckDocumentNotifier notifier) {
        this.notifier = notifier;
    }

    protected RuntimeConfig getFailureNotificationEnabled() {
        return failureNotificationEnabled;
    }

    @Required
    public void setFailureNotificationEnabled(RuntimeConfig failureNotificationEnabled) {
        this.failureNotificationEnabled = failureNotificationEnabled;
    }
}
