package one.modality.base.client.activity.eventdependent;

import dev.webfx.stack.orm.domainmodel.activity.domain.DomainActivityContext;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.platform.async.Future;
import one.modality.base.client.aggregates.event.EventAggregate;
import one.modality.base.client.aggregates.event.EventAggregateMixin;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentActivityMixin;
import one.modality.ecommerce.client.businessdata.feesgroup.FeesGroup;
import one.modality.ecommerce.client.businessdata.feesgroup.FeesGroupsByEventStore;
import one.modality.ecommerce.client.businessdata.preselection.ActiveOptionsPreselectionsByEventStore;
import one.modality.ecommerce.client.businessdata.preselection.OptionsPreselection;
import one.modality.ecommerce.client.businessdata.workingdocument.ActiveWorkingDocumentsByEventStore;
import one.modality.ecommerce.client.businessdata.workingdocument.WorkingDocument;

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
        setEventId(getParameter("eventId"));
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
