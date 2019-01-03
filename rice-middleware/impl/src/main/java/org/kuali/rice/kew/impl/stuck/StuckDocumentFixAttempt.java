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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "KREW_STUCK_DOC_FIX_ATTMPT_T")
@NamedQueries({
        @NamedQuery(name = StuckDocumentDaoJpa.FIX_ATTEMPTS_FOR_INCIDENT_NAME,
                query = StuckDocumentDaoJpa.FIX_ATTEMPTS_FOR_INCIDENT_QUERY)
})
public class StuckDocumentFixAttempt {

    @Id
    @GeneratedValue(generator = "KREW_STUCK_DOC_FIX_ATTMPT_S")
    @PortableSequenceGenerator(name = "KREW_STUCK_DOC_FIX_ATTMPT_S")
    @Column(name = "STUCK_DOC_FIX_ATTMPT_ID", nullable = false)
    private String stuckDocumentFixAttemptId;

    @Column(name = "STUCK_DOC_INCIDENT_ID", nullable = false)
    private String stuckDocumentIncidentId;

    @Column(name = "ATTMPT_TS", nullable = false)
    private Timestamp timestamp;

    public String getStuckDocumentFixAttemptId() {
        return stuckDocumentFixAttemptId;
    }

    public void setStuckDocumentFixAttemptId(String stuckDocumentFixAttemptId) {
        this.stuckDocumentFixAttemptId = stuckDocumentFixAttemptId;
    }

    public String getStuckDocumentIncidentId() {
        return stuckDocumentIncidentId;
    }

    public void setStuckDocumentIncidentId(String stuckDocumentIncidentId) {
        this.stuckDocumentIncidentId = stuckDocumentIncidentId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

}
