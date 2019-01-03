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

import org.kuali.rice.krad.data.jpa.PortableSequenceGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * @author Eric Westfall
 */
@Entity
@Table(name = "KREW_STUCK_DOC_INCIDENT_T")
public class StuckDocumentIncident {

    public static StuckDocumentIncident startNewIncident(String documentId) {
        StuckDocumentIncident incident = new StuckDocumentIncident();
        incident.setDocumentId(documentId);
        incident.setStartDate(new Timestamp(System.currentTimeMillis()));
        incident.setStatus(Status.PENDING);
        return incident;
    }

    @Id
    @GeneratedValue(generator = "KREW_STUCK_DOC_INCIDENT_S")
    @PortableSequenceGenerator(name = "KREW_STUCK_DOC_INCIDENT_S")
    @Column(name = "STUCK_DOC_INCIDENT_ID", nullable = false)
    private String stuckDocumentIncidentId;

    @Column(name = "DOC_HDR_ID", nullable = false)
    private String documentId;

    @Column(name = "START_DT", nullable = false)
    private Timestamp startDate;

    @Column(name = "END_DT")
    private Timestamp endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private Status status;

    public String getStuckDocumentIncidentId() {
        return stuckDocumentIncidentId;
    }

    public void setStuckDocumentIncidentId(String stuckDocumentIncidentId) {
        this.stuckDocumentIncidentId = stuckDocumentIncidentId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public static enum Status {
        PENDING, FIXING, FAILED, FIXED
    }

}
