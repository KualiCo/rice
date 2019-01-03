/**
 * Copyright 2005-2019 The Kuali Foundation
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

import java.util.List;

/**
 * @author Eric Westfall
 */
public interface StuckDocumentService {

    List<String> findAllStuckDocumentIds();

    List<StuckDocument> findAllStuckDocuments();

    StuckDocumentIncident findIncident(String stuckDocumentIncidentId);

    List<StuckDocumentIncident> findIncidents(List<String> stuckDocumentIncidentIds);

    List<StuckDocumentIncident> findAllIncidents(int maxIncidents);

    List<StuckDocumentIncident> findIncidentsByStatus(int maxIncidents, StuckDocumentIncident.Status status);

    List<StuckDocumentIncident> recordNewStuckDocumentIncidents();

    StuckDocumentFixAttempt recordNewIncidentFixAttempt(StuckDocumentIncident stuckDocumentIncident);

    List<StuckDocumentFixAttempt> findAllFixAttempts(String stuckDocumentIncidentId);

    List<StuckDocumentIncident> resolveIncidentsIfPossible(List<String> stuckDocumentIncidentIds);

    StuckDocumentIncident recordIncidentFailure(StuckDocumentIncident stuckDocumentIncident);

    StuckDocumentIncident startFixingIncident(StuckDocumentIncident stuckDocumentIncident);

}
