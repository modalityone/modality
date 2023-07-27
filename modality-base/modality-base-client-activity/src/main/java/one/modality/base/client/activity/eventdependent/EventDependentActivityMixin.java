package one.modality.base.client.activity.eventdependent;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.domainmodel.activity.domain.DomainActivityContext;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentActivityMixin;
import one.modality.base.client2018.aggregates.event.EventAggregate;
import one.modality.base.client2018.aggregates.event.EventAggregateMixin;
import one.modality.ecommerce.client2018.businessdata.feesgroup.FeesGroup;
import one.modality.ecommerce.client2018.businessdata.feesgroup.FeesGroupsByEventStore;
import one.modality.ecommerce.client2018.businessdata.preselection.ActiveOptionsPreselectionsByEventStore;
import one.modality.ecommerce.client2018.businessdata.preselection.OptionsPreselection;
import one.modality.ecommerce.client2018.businessdata.workingdocument.ActiveWorkingDocumentsByEventStore;
import one.modality.ecommerce.client2018.businessdata.workingdocument.WorkingDocument;
import one.modality.event.backoffice.event.fx.FXEventId;

/**
 * @author Bruno Salmon
 */
public interface EventDependentActivityMixin
        <C extends DomainActivityContext<C> & UiRouteActivityContext<C>>

        extends OrganizationDependentActivityMixin<C>,
        EventAggregateMixin,
        EventDependentPresentationModelMixin
{

    default EventAggregate getEventService() {
        return EventAggregate.getOrCreate(getEventId(), getDataSourceModel());
    }

    default void updateEventDependentPresentationModelFromContextParameters() {
        Object eventId = getParameter("eventId");
        if (eventId != null)
            setEventId(eventId);
        else
            eventIdProperty().bind(FXEventId.eventIdProperty());
        updateOrganizationDependentPresentationModelFromContextParameters();
    }

    default WorkingDocument getEventActiveWorkingDocument() {
        return ActiveWorkingDocumentsByEventStore.getEventActiveWorkingDocument(this);
    }

    default Future<FeesGroup[]> onEventFeesGroups() {
        return FeesGroupsByEventStore.onEventFeesGroups(this);
    }

    default OptionsPreselection getEventActiveOptionsPreselection() {
        return ActiveOptionsPreselectionsByEventStore.getActiveOptionsPreselection(this);
    }

}
