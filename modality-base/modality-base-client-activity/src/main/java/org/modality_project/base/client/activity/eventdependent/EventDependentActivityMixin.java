package org.modality_project.base.client.activity.eventdependent;

import dev.webfx.stack.orm.domainmodel.activity.domain.DomainActivityContext;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.platform.async.Future;
import org.modality_project.base.client.activity.organizationdependent.OrganizationDependentActivityMixin;
import org.modality_project.ecommerce.client.businessdata.feesgroup.FeesGroup;
import org.modality_project.ecommerce.client.businessdata.feesgroup.FeesGroupsByEventStore;
import org.modality_project.ecommerce.client.businessdata.preselection.ActiveOptionsPreselectionsByEventStore;
import org.modality_project.ecommerce.client.businessdata.preselection.OptionsPreselection;
import org.modality_project.ecommerce.client.businessdata.workingdocument.ActiveWorkingDocumentsByEventStore;
import org.modality_project.ecommerce.client.businessdata.workingdocument.WorkingDocument;
import org.modality_project.base.client.aggregates.event.EventAggregate;
import org.modality_project.base.client.aggregates.event.EventAggregateMixin;

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
