package org.kuali.rice.kew.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.kew.doctype.DocumentTypePolicy;
import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.doctype.service.DocumentTypeService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple utility class which can parse a target specification for a set of defined Document Types.
 *
 * @author Eric Westfall
 */
public class DocumentTypeWindowTargets {

    private static final Logger LOG = Logger.getLogger(DocumentTypeWindowTargets.class);
    private static final String DEFAULT_KEY = "*";

    private final Map<String, String> targetMappings;
    private final DocumentTypeService documentTypeService;

    private final String defaultDocumentTarget;
    private final String defaultRouteLogTarget;

    private final Map<String, String> documentTargetCache = new ConcurrentHashMap<>();
    private final Map<String, String> routeLogTargetCache = new ConcurrentHashMap<>();

    public DocumentTypeWindowTargets(String targetSpec, String defaultDocumentTarget, String defaultRouteLogTarget, DocumentTypeService documentTypeService) {
        if (StringUtils.isBlank(defaultDocumentTarget)) {
            throw new IllegalArgumentException("defaultDocumentTarget must not be blank");
        }
        if (StringUtils.isBlank(defaultRouteLogTarget)) {
            throw new IllegalArgumentException("defaultRouteLogTarget must not be blank");
        }
        if (documentTypeService == null) {
            throw new IllegalArgumentException("documentTypeService must not be nulll");
        }
        this.targetMappings = new HashMap<>();
        this.documentTypeService = documentTypeService;
        this.defaultDocumentTarget = defaultDocumentTarget;
        this.defaultRouteLogTarget = defaultRouteLogTarget;

        parseTargetSpec(targetSpec);
    }

    private void parseTargetSpec(String targetSpec) {
        if (!StringUtils.isBlank(targetSpec)) {
            String[] entries = targetSpec.split(",");
            for (String entry : entries) {
                String[] docTypeTarget = entry.split(":");
                if (docTypeTarget.length != 2) {
                    LOG.warn("Encountered an invalid entry in target spec, ignoring: " + entry);
                } else {
                    targetMappings.put(docTypeTarget[0], docTypeTarget[1]);
                }
            }
        }
    }

    public String getDocumentTarget(String documentTypeName) {
        return getTargetInternal(documentTypeName, false, 0);
    }

    public String getRouteLogTarget(String documentTypeName) {
        return getTargetInternal(documentTypeName, true, 0);
    }

    private String getTargetInternal(String documentTypeName, boolean isRouteLog, int depth) {
        if (StringUtils.isBlank(documentTypeName)) {
            throw new IllegalArgumentException("Document type name must not be blank");
        }
        Map<String, String> targetCache = isRouteLog ? routeLogTargetCache : documentTargetCache;
        if (!targetCache.containsKey(documentTypeName)) {
            String target = targetMappings.get(documentTypeName);
            if (target == null) {
                // first if we are at depth 0, check if the doc type has a policy on it
                if (depth == 0) {
                    DocumentType documentType = documentTypeService.findByName(documentTypeName);
                    if (documentType != null) {
                        DocumentTypePolicy docSearchTarget = documentType.getDocSearchTarget();
                        if (docSearchTarget.getPolicyStringValue() != null) {
                            target = docSearchTarget.getPolicyStringValue();
                        }
                    }
                }
            }
            // if there was no policy, check the parent document type if there is one
            if (target == null) {
                String parentDocumentTypeName = documentTypeService.findParentNameByName(documentTypeName);
                if (parentDocumentTypeName != null) {
                    target = getTargetInternal(parentDocumentTypeName, isRouteLog, depth + 1);
                }
            }
            // fall back to the defaults if no other target can be determined
            if (target == null) {
                target = targetMappings.get(DEFAULT_KEY);
                if (target == null) {
                    target = isRouteLog ? defaultRouteLogTarget : defaultDocumentTarget;
                }
            }
            targetCache.put(documentTypeName, target);
        }
        return targetCache.get(documentTypeName);
    }

}