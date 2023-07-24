package one.modality.ecommerce.frontoffice.activities.contactus.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class ContactUsRouting {

  private static final String PATH = "/contact-us/:documentId";

  public static String getPath() {
    return PATH;
  }

  public static String getContactUsPath(Object documentId) {
    return ModalityRoutingUtil.interpolateDocumentIdInPath(documentId, getPath());
  }
}
