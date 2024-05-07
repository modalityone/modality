package one.modality.event.client.activity.eventdependent;

import dev.webfx.stack.orm.domainmodel.activity.domain.DomainActivityContext;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentActivityMixin;
import one.modality.event.client.event.fx.FXEventId;

/**
 * @author Bruno Salmon
 */
public interface EventDependentActivityMixin
        <C extends DomainActivityContext<C> & UiRouteActivityContext<C>>

        extends OrganizationDependentActivityMixin<C>,
        EventDependentPresentationModelMixin
{

    default void updateEventDependentPresentationModelFromContextParameters() {
        Object eventId = getParameter("eventId");
        if (eventId != null)
            setEventId(eventId);
        else
            eventIdProperty().bind(FXEventId.eventIdProperty());
        updateOrganizationDependentPresentationModelFromContextParameters();
    }

}
