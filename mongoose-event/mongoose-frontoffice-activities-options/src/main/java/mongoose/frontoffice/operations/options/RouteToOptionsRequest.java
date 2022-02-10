package mongoose.frontoffice.operations.options;

import mongoose.client.businessdata.preselection.ActiveOptionsPreselectionsByEventStore;
import mongoose.client.businessdata.preselection.OptionsPreselection;
import mongoose.client.businessdata.workingdocument.ActiveWorkingDocumentsByEventStore;
import mongoose.client.businessdata.workingdocument.WorkingDocument;
import mongoose.frontoffice.activities.options.routing.OptionsRouting;
import dev.webfx.framework.client.operations.route.RoutePushRequest;
import dev.webfx.framework.shared.orm.entity.EntityId;
import dev.webfx.platform.client.services.windowhistory.spi.BrowsingHistory;

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

    public RouteToOptionsRequest(WorkingDocument workingDocument, OptionsPreselection optionsPreselection, BrowsingHistory history) {
        this(prepareEventServiceAndReturnEventId(workingDocument, optionsPreselection), history);
    }

    private static EntityId prepareEventServiceAndReturnEventId(WorkingDocument workingDocument, OptionsPreselection optionsPreselection) {
        EntityId eventId = workingDocument.getDocument().getEventId();
        if (eventId == null)
            eventId = workingDocument.getEventAggregate().getEvent().getId();
        ActiveOptionsPreselectionsByEventStore.setActiveOptionsPreselection(optionsPreselection, eventId);
        ActiveWorkingDocumentsByEventStore.setEventActiveWorkingDocument(optionsPreselection == null ? workingDocument : null, eventId);
        return eventId;
    }

}
