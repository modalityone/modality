package one.modality.ecommerce.frontoffice.activities.summary.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class SummaryRouting {

  private static final String PATH = "/book/event/:eventId/summary";

  public static String getPath() {
    return PATH;
  }

  public static String getSummaryPath(Object eventId) {
    return ModalityRoutingUtil.interpolateEventIdInPath(eventId, PATH);
  }
}
