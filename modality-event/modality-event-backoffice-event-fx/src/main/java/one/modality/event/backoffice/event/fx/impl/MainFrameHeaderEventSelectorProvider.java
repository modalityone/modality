package one.modality.event.backoffice.event.fx.impl;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import one.modality.base.backoffice.mainframe.headernode.MainFrameHeaderNodeProvider;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.gantt.fx.visibility.GanttVisibility;
import one.modality.base.shared.entities.Event;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.backoffice.event.fx.FXEvent;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * @author Bruno Salmon
 */
public class MainFrameHeaderEventSelectorProvider implements MainFrameHeaderNodeProvider {

    private EntityButtonSelector<Event> eventSelector;

    @Override
    public String getName() {
        return "eventSelector";
    }

    @Override
    public Node getHeaderNode(ButtonFactoryMixin buttonFactory, Pane frameContainer, DataSourceModel dataSourceModel) {
        if (eventSelector == null) {
            // Creating the event selector
            eventSelector = new EntityButtonSelector<Event>(
                    "{class: 'Event', alias: 'e', columns: 'icon,name,dateIntervalFormat(startDate,endDate)', orderBy: 'startDate desc'}",
                    buttonFactory, frameContainer, dataSourceModel)
                    .ifNotNullOtherwiseEmpty(FXOrganization.organizationProperty(), o -> where("organization = ?", o))
            ;
            EntityStore store = eventSelector.getStore();
            Event nullEvent = store.createEntity(Event.class);
            nullEvent.setName("<No event selected>");
            eventSelector.setVisualNullEntity(nullEvent);
            // Doing a bidirectional binding with FXOrganization
            FXEvent.eventProperty().bindBidirectional(eventSelector.selectedItemProperty());
            Button eventButton = eventSelector.getButton();
            eventButton.visibleProperty().bind(FXProperties.compute(FXGanttVisibility.ganttVisibilityProperty(), value -> value == GanttVisibility.EVENTS));
            eventButton.managedProperty().bind(eventButton.visibleProperty());
        }
        return eventSelector.getButton();
    }
}
