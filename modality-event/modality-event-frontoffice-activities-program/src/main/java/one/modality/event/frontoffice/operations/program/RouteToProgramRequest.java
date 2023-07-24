package one.modality.event.frontoffice.operations.program;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import one.modality.event.frontoffice.activities.program.routing.ProgramRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToProgramRequest extends RoutePushRequest {

  public RouteToProgramRequest(Object eventId, BrowsingHistory history) {
    super(ProgramRouting.getProgramPath(eventId), history);
  }
}
