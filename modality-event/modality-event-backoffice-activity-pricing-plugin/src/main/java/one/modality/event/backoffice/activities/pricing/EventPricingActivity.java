package one.modality.event.backoffice.activities.pricing;


import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.masterslave.MasterSlaveLinker;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import dev.webfx.stack.ui.operation.OperationUtil;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
import one.modality.base.client.i18n.ModalityI18nKeys;
import one.modality.base.client.util.masterslave.ModalitySlaveEditor;
import one.modality.base.shared.entities.Event;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.LoadPolicyArgument;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.event.client.event.fx.FXEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
final class EventPricingActivity extends ViewDomainActivityBase {

    private final ObjectProperty<PolicyAggregate> eventPolicyProperty = new SimpleObjectProperty<>();
    private UpdateStore updateStore;
    private final Button saveButton = Bootstrap.largeSuccessButton(I18nControls.newButton(ModalityI18nKeys.Save));
    private final Button cancelButton = Bootstrap.largeSecondaryButton(I18nControls.newButton(ModalityI18nKeys.Cancel));
    private final MasterSlaveLinker<Event> masterSlaveLinker = new MasterSlaveLinker<>(new ModalitySlaveEditor<>() {
        @Override
        public void setSlave(Event event) {
            super.setSlave(event);
            reload();
        }

        @Override
        public boolean hasChanges() {
            return updateStore != null && updateStore.hasChanges();
        }
    });

    private void reload() {
        Event event = masterSlaveLinker.getSlave();
        if (event == null) {
            eventPolicyProperty.set(null);
        } else {
            DocumentService.loadPolicy(new LoadPolicyArgument(event))
                .onSuccess(eventPolicy -> UiScheduler.runInUiThread(() -> {
                    eventPolicy.rebuildEntities(event);
                    updateStore = UpdateStore.createAbove(eventPolicy.getEntityStore());
                    BooleanBinding hasNoChanges = EntityBindings.hasChangesProperty(updateStore).not();
                    saveButton.disableProperty().bind(hasNoChanges);
                    cancelButton.disableProperty().bind(hasNoChanges);
                    eventPolicyProperty.set(eventPolicy);
                }));
        }
    }

    @Override
    protected void startLogic() {
        FXEvent.eventProperty().bindBidirectional(masterSlaveLinker.masterProperty());
    }

    @Override
    public Node buildUi() {
        VBox sectionContainers = new VBox(20);

        List<AbstractPricing> pricings = new ArrayList<>();
        FXProperties.runNowAndOnPropertyChange(eventPolicy -> {
            if (eventPolicy == null)
                pricings.clear();
            else {
                Collections.setAll(pricings, new TeachingsPricing(eventPolicy, updateStore));
                sectionContainers.getChildren().setAll(Collections.map(pricings, this::createCollapsablePricingSection));
            }
        }, eventPolicyProperty);

        saveButton.setOnAction(e -> OperationUtil.turnOnButtonsWaitModeDuringExecution(
            updateStore.submitChanges()
                .onFailure(Console::log)
            , saveButton));

        cancelButton.setOnAction(e -> {
            updateStore.cancelChanges();
            pricings.forEach(AbstractPricing::onChangesCancelled);
        });

        HBox buttonBar = new HBox(10, saveButton, cancelButton);
        buttonBar.setAlignment(Pos.CENTER);

        VBox container = new VBox(50,
            sectionContainers,
            buttonBar
        );
        container.setAlignment(Pos.TOP_CENTER);
        return container;
    }

    private Node createCollapsablePricingSection(AbstractPricing pricing) {
        CollapsePane contentContainer = new CollapsePane(pricing.getContentNode());
        contentContainer.setMaxWidth(Double.MAX_VALUE);
        contentContainer.setPadding(new Insets(20));
        contentContainer.setBorder(Border.stroke(Color.GRAY));
        return new VBox(10,
            new HBox(10, pricing.getHeaderNode(), CollapsePane.armChevron(CollapsePane.createBlackChevron(), contentContainer)),
            contentContainer
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        //FXGanttVisibility.showEvents();
        //FXGanttInterstice.showGanttInterstice();
        FXEventSelector.showEventSelector();
    }

    @Override
    public void onPause() {
        //FXGanttVisibility.resetToDefault();
        //FXGanttInterstice.resetToDefault();
        FXEventSelector.resetToDefault();
        super.onPause();
    }

}
