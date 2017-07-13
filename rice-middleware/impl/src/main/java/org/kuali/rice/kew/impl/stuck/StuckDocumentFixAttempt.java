package org.kuali.rice.kew.impl.stuck;

import org.kuali.rice.krad.data.jpa.PortableSequenceGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "KREW_STUCK_DOC_FIX_ATTMPT_T")
public class StuckDocumentFixAttempt {

    @Id
    @GeneratedValue(generator = "KREW_STUCK_DOC_FIX_ATTMPT_S")
    @PortableSequenceGenerator(name = "KREW_STUCK_DOC_FIX_ATTMPT_S")
    @Column(name = "STUCK_DOC_FIX_ATTMPT_ID", nullable = false)
    private String stuckDocumentFixAttemptId;

    @Column(name = "STUCK_DOC_INCIDENT_ID", nullable = false)
    private String stuckDocumentIncidentId;

    @Column(name = "TIMESTAMP", nullable = false)
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
