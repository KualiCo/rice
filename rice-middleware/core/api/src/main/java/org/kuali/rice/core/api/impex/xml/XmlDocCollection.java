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
package org.kuali.rice.core.api.impex.xml;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Interface describing an abstract XML document source collection
 * @see org.kuali.rice.core.api.impex.xml.batch.XmlDoc
 * @see org.kuali.rice.core.api.impex.xml.kew.batch.BaseXmlDocCollection
 * @see org.kuali.rice.core.api.impex.xml.kew.batch.FileXmlDocCollection
 * @see org.kuali.rice.kew.batch.DirectoryXmlDocCollection
 * @see org.kuali.rice.core.api.impex.xml.kew.batch.ZipXmlDocCollection
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface XmlDocCollection {
    File getFile();
    List<? extends XmlDoc> getXmlDocs();
    void close() throws IOException;
}
