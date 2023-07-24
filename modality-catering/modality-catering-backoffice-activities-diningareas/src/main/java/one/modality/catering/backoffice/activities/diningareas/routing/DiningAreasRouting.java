package one.modality.catering.backoffice.activities.diningareas.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class DiningAreasRouting {

  private static final String PATH = "/dining-areas/event/:eventId";

  public static String getPath() {
    return PATH;
  }

  public static String getEventPath(Object eventId) {
    return ModalityRoutingUtil.interpolateEventIdInPath(eventId, PATH);
  }
}
