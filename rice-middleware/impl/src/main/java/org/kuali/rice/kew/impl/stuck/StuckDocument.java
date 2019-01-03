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
