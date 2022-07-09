package org.modality_project.ecommerce.frontoffice.activities.contactus.routing;

import org.modality_project.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class ContactUsRouting {

    private final static String PATH = "/contact-us/:documentId";

    public static String getPath() {
        return PATH;
    }

    public static String getContactUsPath(Object documentId) {
        return ModalityRoutingUtil.interpolateDocumentIdInPath(documentId, getPath());
    }

}
