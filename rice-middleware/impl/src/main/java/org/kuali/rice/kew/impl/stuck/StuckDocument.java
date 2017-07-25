package org.kuali.rice.kew.impl.stuck;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StuckDocument {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String documentId;
    private final String documentTypeLabel;
    private final LocalDateTime createDate;

    public StuckDocument(String documentId, String documentTypeLabel, LocalDateTime createDate) {
        this.documentId = documentId;
        this.documentTypeLabel = documentTypeLabel;
        this.createDate = createDate;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getDocumentTypeLabel() {
        return documentTypeLabel;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public String getFormattedCreateDate() {
        return getCreateDate().format(FORMATTER);
    }

}
