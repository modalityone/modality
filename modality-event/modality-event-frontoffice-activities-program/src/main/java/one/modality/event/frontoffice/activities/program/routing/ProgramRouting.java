package one.modality.event.frontoffice.activities.program.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class ProgramRouting {

  private static final String PATH = "/book/event/:eventId/program";

  public static String getPath() {
    return PATH;
  }

  public static String getProgramPath(Object eventId) {
    return ModalityRoutingUtil.interpolateEventIdInPath(eventId, PATH);
  }
}
