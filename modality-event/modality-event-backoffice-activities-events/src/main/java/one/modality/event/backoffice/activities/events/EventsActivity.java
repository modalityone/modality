package one.modality.event.backoffice.activities.events;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContextMixin;
import javafx.scene.Node;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.activity.table.GenericTable;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.ecommerce.backoffice.operations.routes.bookings.RouteToBookingsRequest;

import static dev.webfx.stack.orm.dql.DqlStatement.limit;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * @author Bruno Salmon
 */
final class EventsActivity extends ViewDomainActivityBase
        implements UiRouteActivityContextMixin<ViewDomainActivityContextFinal>,
        ModalityButtonFactoryMixin {

    private final EventsPresentationModel pm = new EventsPresentationModel();
    private final GenericTable<EventsPresentationModel> genericTable = new GenericTable<>(pm, this);
    @Override
    public Node buildUi() {
        return genericTable.assemblyViewNodes();
    }

    @Override
    public void onResume() {
        super.onResume();
        genericTable.onResume(); // will request focus for the search box
    }

    @Override
    protected void updateModelFromContextParameters() {
        Object organizationId = getParameter("organizationId");
        if (organizationId != null)
            pm.setOrganizationId(organizationId);
        else
            pm.organizationIdProperty().bind(FXOrganizationId.organizationIdProperty());
    }

    @Override
    protected void startLogic() {
        ReactiveVisualMapper.createPushReactiveChain(this)
                .always("{class: 'Event', alias: 'e', fields2: '(select count(1) from Document where !cancelled and event=e) as bookingsCount', where: 'active', orderBy: 'startDate desc,id desc'}")
                // Search box condition
                .ifTrimNotEmpty(pm.searchTextProperty(), s -> where("lower(name) like ?", "%" + s.toLowerCase() + "%"))
                .ifNotNull(pm.organizationIdProperty(), o -> where("organization=?", o))
                // Limit condition
                .ifPositive(pm.limitProperty(), l -> limit("?", l))
                .setEntityColumns("[" +
                        //"{label: 'Image', expression: 'image(`images/calendar.svg`)'}," +
                        //"{label: 'Event', expression: 'icon, name + ` ~ ` + dateIntervalFormat(startDate,endDate) + ` (` + bookingsCount + `)`'}" +
                        "{label: 'Event', expression: 'icon, name + ` ~ ` + dateIntervalFormat(startDate,endDate)`'}," +
                        "'type'," +
                        "{role: 'background', expression: 'type.background'}" +
                        "]")
                .visualizeResultInto(pm.genericVisualResultProperty())
                .setVisualSelectionProperty(pm.genericVisualSelectionProperty())
                .setSelectedEntityHandler(event -> new RouteToBookingsRequest(event, getHistory()).execute())
                .start();
    }
}
