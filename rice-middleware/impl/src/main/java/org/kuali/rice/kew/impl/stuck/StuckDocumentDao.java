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

import java.util.List;

/**
 * Created by ewestfal on 5/18/17.
 */
public interface StuckDocumentDao {

    List<String> findAllStuckDocumentIds();

    List<StuckDocument> findAllStuckDocuments();

    StuckDocumentIncident findIncident(String stuckDocumentIncidentId);

    StuckDocumentIncident saveIncident(StuckDocumentIncident incident);

    void deleteIncident(StuckDocumentIncident incident);

    List<StuckDocumentIncident> findAllIncidents(int maxIncidents);

    List<StuckDocumentIncident> findIncidentsByStatus(int maxIncidents, StuckDocumentIncident.Status status);

    List<StuckDocumentFixAttempt> findAllFixAttempts(String stuckDocumentIncidentId);

    StuckDocumentFixAttempt saveFixAttempt(StuckDocumentFixAttempt incident);

    List<String> identifyNewStuckDocuments();

    List<StuckDocumentIncident> identifyStillStuckDocuments(List<String> incidentIds);

}
