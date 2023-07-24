package one.modality.event.frontoffice.operations.options;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import one.modality.ecommerce.client.businessdata.preselection.ActiveOptionsPreselectionsByEventStore;
import one.modality.ecommerce.client.businessdata.preselection.OptionsPreselection;
import one.modality.ecommerce.client.businessdata.workingdocument.ActiveWorkingDocumentsByEventStore;
import one.modality.ecommerce.client.businessdata.workingdocument.WorkingDocument;
import one.modality.event.frontoffice.activities.options.routing.OptionsRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToOptionsRequest extends RoutePushRequest {

  public RouteToOptionsRequest(Object eventId, BrowsingHistory history) {
    super(OptionsRouting.getEventOptionsPath(eventId), history);
  }

  public RouteToOptionsRequest(WorkingDocument workingDocument, BrowsingHistory history) {
    this(workingDocument, null, history);
  }

  public RouteToOptionsRequest(OptionsPreselection optionsPreselection, BrowsingHistory history) {
    this(optionsPreselection.getWorkingDocument(), optionsPreselection, history);
  }

  public RouteToOptionsRequest(
      WorkingDocument workingDocument,
      OptionsPreselection optionsPreselection,
      BrowsingHistory history) {
    this(prepareEventServiceAndReturnEventId(workingDocument, optionsPreselection), history);
  }

  private static EntityId prepareEventServiceAndReturnEventId(
      WorkingDocument workingDocument, OptionsPreselection optionsPreselection) {
    EntityId eventId = workingDocument.getDocument().getEventId();
    if (eventId == null) eventId = workingDocument.getEventAggregate().getEvent().getId();
    ActiveOptionsPreselectionsByEventStore.setActiveOptionsPreselection(
        optionsPreselection, eventId);
    ActiveWorkingDocumentsByEventStore.setEventActiveWorkingDocument(
        optionsPreselection == null ? workingDocument : null, eventId);
    return eventId;
  }
}
