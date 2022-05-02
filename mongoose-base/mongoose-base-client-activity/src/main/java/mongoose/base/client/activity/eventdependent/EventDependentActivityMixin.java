package mongoose.base.client.activity.eventdependent;

import dev.webfx.framework.client.activity.impl.elementals.domain.DomainActivityContext;
import dev.webfx.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.platform.shared.async.Future;
import mongoose.base.client.activity.organizationdependent.OrganizationDependentActivityMixin;
import mongoose.base.client.aggregates.event.EventAggregate;
import mongoose.base.client.aggregates.event.EventAggregateMixin;
import mongoose.ecommerce.client.businessdata.feesgroup.FeesGroup;
import mongoose.ecommerce.client.businessdata.feesgroup.FeesGroupsByEventStore;
import mongoose.ecommerce.client.businessdata.preselection.ActiveOptionsPreselectionsByEventStore;
import mongoose.ecommerce.client.businessdata.preselection.OptionsPreselection;
import mongoose.ecommerce.client.businessdata.workingdocument.ActiveWorkingDocumentsByEventStore;
import mongoose.ecommerce.client.businessdata.workingdocument.WorkingDocument;

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
