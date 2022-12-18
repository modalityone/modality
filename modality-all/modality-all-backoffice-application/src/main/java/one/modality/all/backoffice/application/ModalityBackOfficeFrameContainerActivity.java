package one.modality.all.backoffice.application;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import one.modality.base.client.application.ModalityClientFrameContainerActivity;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Organization;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.backoffice.event.fx.FXEvent;
import one.modality.event.backoffice.event.fx.FXShowEvent;

/**
 * @author Bruno Salmon
 */
public class ModalityBackOfficeFrameContainerActivity extends ModalityClientFrameContainerActivity {

    @Override
    protected HBox createContainerHeaderCenterItem() {
        // Creating the organization selector
        EntityButtonSelector<Organization> organizationSelector = new EntityButtonSelector<>(
                "{class: 'Organization', alias: 'o', where: 'exists(select Event where organization=o)'}",
                this, containerPane, getDataSourceModel()
        );
        // Doing a bidirectional binding with FXOrganization
        FXOrganization.organizationProperty().bindBidirectional(organizationSelector.selectedItemProperty());
        Button organizationButton = organizationSelector.getButton();
        // Creating the event selector
        EntityButtonSelector<Event> eventSelector = new EntityButtonSelector<>(
                "{class: 'Event'}",
                this, containerPane, getDataSourceModel()
        );
        // Doing a bidirectional binding with FXOrganization
        FXEvent.eventProperty().bindBidirectional(eventSelector.selectedItemProperty());
        Button eventButton = eventSelector.getButton();
        // Updating the event condition when organisation changes
        FXProperties.runNowAndOnPropertiesChange(() ->
                eventSelector.setJsonOrClass("{class: 'Event', alias: 'e', columns: 'icon,name,dateIntervalFormat(startDate,endDate)', where: 'organization=" + Entities.getPrimaryKey(organizationSelector.getSelectedItem()) + "', orderBy: 'startDate desc'}")
        , organizationSelector.selectedItemProperty());
        //
        eventButton.visibleProperty().bind(FXShowEvent.showEventProperty());
        eventButton.managedProperty().bind(eventButton.visibleProperty());
        // Returning the button of that organization selector
        return new HBox(5, organizationButton, eventButton);
    }
}
