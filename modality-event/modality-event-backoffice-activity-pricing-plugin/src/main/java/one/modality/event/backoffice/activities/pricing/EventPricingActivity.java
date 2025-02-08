package one.modality.event.backoffice.activities.pricing;


import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.masterslave.MasterSlaveLinker;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
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
    private final List<ItemFamilyPricing> pricings = new ArrayList<>();
    private final MasterSlaveLinker<Event> masterSlaveLinker = new MasterSlaveLinker<>(new ModalitySlaveEditor<>() {
        @Override
        public void setSlave(Event event) {
            super.setSlave(event);
            reload();
        }

        @Override
        public boolean hasChanges() {
            return pricings.stream().anyMatch(ItemFamilyPricing::hasChanges);
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
                    eventPolicyProperty.set(eventPolicy);
                }));
        }
    }

    @Override
    protected void startLogic() {
        // Note: although it's a bidirectional binding, the order is important because the left property is initialised
        // to the value of the right property (we don't want to erase FXEvent).
        masterSlaveLinker.masterProperty().bindBidirectional(FXEvent.eventProperty());
    }

    @Override
    public Node buildUi() {
        VBox sectionContainers = new VBox(20);

        FXProperties.runNowAndOnPropertyChange(eventPolicy -> {
            if (eventPolicy == null)
                pricings.clear();
            else {
                Collections.setAll(pricings,
                    new TeachingsPricing(eventPolicy),
                    new AudioRecordingPricing(eventPolicy)
                );
                sectionContainers.getChildren().setAll(Collections.map(pricings, this::createCollapsablePricingSection));
            }
        }, eventPolicyProperty);

        VBox container = new VBox(50,
            sectionContainers
        );
        container.setAlignment(Pos.TOP_CENTER);
        return Controls.createVerticalScrollPane(container);
    }

    private Node createCollapsablePricingSection(ItemFamilyPricing pricing) {
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
