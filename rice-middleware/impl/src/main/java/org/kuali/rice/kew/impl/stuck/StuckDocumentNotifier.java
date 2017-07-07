package org.kuali.rice.kew.impl.stuck;

import java.util.List;

public interface StuckDocumentNotifier {

    void notify(List<String> documentIds);

}
