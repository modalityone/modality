package org.modality_project.event.frontoffice.activities.fees;

import dev.webfx.extras.cell.collator.NodeCollatorRegistry;
import dev.webfx.extras.cell.collator.grid.GridCollator;
import dev.webfx.extras.cell.renderer.TextRenderer;
import dev.webfx.extras.cell.renderer.ValueRenderingContext;
import dev.webfx.extras.imagestore.ImageStore;
import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.type.SpecializedTextType;
import dev.webfx.extras.visual.*;
import dev.webfx.extras.visual.controls.grid.SkinnedVisualGrid;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.framework.client.services.i18n.Dictionary;
import dev.webfx.stack.framework.client.services.i18n.I18n;
import dev.webfx.stack.framework.client.services.i18n.I18nControls;
import dev.webfx.stack.framework.client.ui.util.layout.LayoutUtil;
import dev.webfx.stack.framework.shared.orm.entity.EntityList;
import dev.webfx.kit.util.properties.Properties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.platform.json.Json;
import dev.webfx.stack.platform.json.JsonObject;
import dev.webfx.stack.platform.json.WritableJsonObject;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.tuples.Pair;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.modality_project.base.client.entities.util.Labels;
import org.modality_project.base.client.icons.ModalityIcons;
import org.modality_project.ecommerce.client.activity.bookingprocess.BookingProcessActivity;
import org.modality_project.ecommerce.client.businessdata.feesgroup.FeesGroup;
import org.modality_project.ecommerce.client.businessdata.preselection.OptionsPreselection;
import org.modality_project.event.client.controls.sectionpanel.SectionPanelFactory;
import org.modality_project.base.client.activity.ModalityButtonFactoryMixin;
import org.modality_project.base.client.aggregates.event.EventAggregate;
import org.modality_project.base.shared.entities.Option;
import org.modality_project.base.shared.entities.Person;
import org.modality_project.event.frontoffice.operations.options.RouteToOptionsRequest;

import java.util.function.Consumer;

import static dev.webfx.stack.framework.client.ui.util.image.JsonImageViews.createImageView;

/**
 * @author Bruno Salmon
 */
final class FeesActivity extends BookingProcessActivity {

    private GridCollator feesGroupsCollator;

    @Override
    protected void createViewNodes() {
        super.createViewNodes();
        feesGroupsCollator = new GridCollator(this::toFeesGroupPanel, nodes -> new VBox(20, nodes));
        verticalStack.getChildren().setAll(feesGroupsCollator, LayoutUtil.setMaxWidthToInfinite(backButton));

        feesGroupsCollator.visualResultProperty().bind(rsProperty);
    }

    private Node toFeesGroupPanel(Node... nodes) {
        BorderPane borderPane = buildFeesSectionPanel(nodes[0]);
        borderPane.setCenter(nodes[1]);
        borderPane.setBottom(nodes[2]);
        return borderPane;
    }

    private BorderPane buildFeesSectionPanel(Node... nodes) {
        return SectionPanelFactory.createSectionPanelWithHeaderNodes(nodes);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (lastLoadedEventOptions != null && getEventOptions() != lastLoadedEventOptions)
            loadAndDisplayFeesGroups();
    }

    @Override
    protected void startLogic() {
        // Load and display fees groups now but also on event change
        Properties.runNowAndOnPropertiesChange(this::loadAndDisplayFeesGroups, eventIdProperty());

        lastDictionary = I18n.getDictionary();
        Properties.consume(Properties.filter(Properties.combine(I18n.dictionaryProperty(), activeProperty(),
                Pair::new), // combine function
                pair -> pair.get2()), // filter function (GWT doesn't compile method reference in this case)
                pair -> refreshOnDictionaryChanged());
    }

    private Dictionary lastDictionary;

    private void refreshOnDictionaryChanged() {
        Dictionary newDictionary = I18n.getDictionary();
        if (lastDictionary != newDictionary) {
            lastDictionary = newDictionary;
            displayFeesGroups();
        }
    }

    private EntityList<Option> lastLoadedEventOptions;

    private void loadAndDisplayFeesGroups() {
        lastLoadedEventOptions = null;
        onEventFeesGroups()
                .onFailure(Console::log)
                .onSuccess(this::displayFeesGroupsAndRefreshAvailabilities);
    }

    private final Property<VisualResult> rsProperty = new SimpleObjectProperty<>();
    private FeesGroup[] feesGroups;

    private void displayFeesGroupsAndRefreshAvailabilities(FeesGroup[] feesGroups) {
        this.feesGroups = feesGroups;
        onEventAvailabilities().onComplete(ar -> {
            if (ar.succeeded())
                displayFeesGroups();
        });
        if (getEventAvailabilities() == null)
            displayFeesGroups();
    }

    private void displayFeesGroups() {
        if (getEvent() == null || feesGroups == null) // This can happen when reacting to active property while the event has just changed and is not yet loaded
            return; // We return to avoid NPE (this method will be called again once the event is loaded)
        UiScheduler.runOutUiThread(this::displayFeesGroupsNow);
    }

    private void displayFeesGroupsNow() {
        int n = feesGroups.length;
        VisualResultBuilder rsb = VisualResultBuilder.create(n, new VisualColumn[]{
                VisualColumn.create((value, context) -> renderFeesGroupHeader((Pair<JsonObject, String>) value)),
                VisualColumn.create((value, context) -> renderFeesGroupBody((VisualResult) value)),
                VisualColumn.create(null, SpecializedTextType.HTML)});
        WritableJsonObject jsonImage = Json.parseObject(ModalityIcons.priceTagColorSvg16JsonUrl);
        ColumnWidthCumulator[] cumulators = {new ColumnWidthCumulator(), new ColumnWidthCumulator(), new ColumnWidthCumulator()};
        for (int i = 0; i < n; i++) {
            FeesGroup feesGroup = feesGroups[i];
            rsb.setValue(i, 0, new Pair<>(jsonImage, feesGroup.getDisplayName()));
            rsb.setValue(i, 1, generateFeesGroupVisualResult(feesGroup, this::onBookButtonPressed, cumulators));
            if (i == n - 1) // Showing the fees bottom text only on the last fees group
                rsb.setValue(i, 2, feesGroup.getFeesBottomText());
        }
        VisualResult rs = rsb.build();
        rsProperty.setValue(rs);
    }

    private VisualResult generateFeesGroupVisualResult(FeesGroup feesGroup, Consumer<OptionsPreselection> bookHandler, ColumnWidthCumulator[] cumulators) {
        ModalityButtonFactoryMixin buttonFactory = this;
        EventAggregate eventAggregate = this;
        boolean showBadges = Objects.areEquals(eventAggregate.getEvent().getOrganizationId().getPrimaryKey(), 2); // For now only showing badges on KMCF courses
        OptionsPreselection[] optionsPreselections = feesGroup.getOptionsPreselections();
        int optionsCount = optionsPreselections.length;
        boolean singleOption = optionsCount == 1;
        VisualResultBuilder rsb = VisualResultBuilder.create(optionsCount, new VisualColumn[]{
                VisualColumnBuilder.create(I18n.getI18nText(singleOption ? (feesGroup.isFestival() ? "Festival" : "Course") : "Accommodation"), PrimType.STRING).setCumulator(cumulators[0]).build(),
                VisualColumnBuilder.create(I18n.getI18nText("Fee"), PrimType.INTEGER).setStyle(VisualStyle.CENTER_STYLE).setCumulator(cumulators[1]).build(),
                VisualColumnBuilder.create(I18n.getI18nText("Availability")).setStyle(VisualStyle.CENTER_STYLE).setCumulator(cumulators[2])
                        .setValueRenderer((p, context) -> {
                            Pair<Object, OptionsPreselection> pair = (Pair<Object, OptionsPreselection>) p;
                            if (pair == null || !eventAggregate.areEventAvailabilitiesLoaded())
                                return new ImageView(ImageStore.getOrCreateImage(ModalityIcons.spinnerIcon16Url, 16, 16));
                            Object availability = pair.get1();
                            OptionsPreselection optionsPreselection = pair.get2();
                            // Availability is null when there is no online room at all. In this case...
                            if (availability == null && optionsPreselection.hasAccommodationExcludingSharing()) // ... if it's an accommodation option (but not just sharing)
                                availability = 0; // we show it as sold out - otherwise (if it's a sharing option or no accommodation) we show it as available
                            boolean soldout = availability != null && Numbers.doubleValue(availability) <= 0 || // Showing sold out if the availability is zero
                                    optionsPreselection.isForceSoldout() || // or if the option has been forced as sold out in the back-office
                                    feesGroup.isForceSoldout(); // or if the whole FeesGroup has been forced as sold out
                            if (soldout)
                                return buttonFactory.newSoldoutButton();
                            Button button = buttonFactory.newBookButton();
                            button.setOnAction(e -> bookHandler.accept(optionsPreselection));
                            if (availability == null || !showBadges)
                                return button;
                            HBox hBox = (HBox) NodeCollatorRegistry.hBoxCollator().collateNodes(createBadge(TextRenderer.SINGLETON.renderValue(availability, ValueRenderingContext.DEFAULT_READONLY_CONTEXT)), button);
                            hBox.setAlignment(Pos.CENTER);
                            return hBox;
                        }).build()});
        int rowIndex = 0;
        for (OptionsPreselection optionsPreselection : optionsPreselections) {
            rsb.setValue(rowIndex,   0, singleOption ? /* Showing course name instead of 'NoAccommodation' when single line */ Labels.instantTranslateLabel(Objects.coalesce(feesGroup.getLabel(), Labels.bestLabelOrName(feesGroup.getEvent()))) : /* Otherwise showing accommodation type */ optionsPreselection.getDisplayName());
            rsb.setValue(rowIndex,   1, optionsPreselection.getDisplayPrice());
            rsb.setValue(rowIndex++, 2, new Pair<>(optionsPreselection.getDisplayAvailability(eventAggregate), optionsPreselection));
        }
        return rsb.build();
    }

    private static Node createBadge(Node... badgeNodes) {
        return new HBox(badgeNodes);
    }


    private Node renderFeesGroupHeader(Pair<JsonObject, String> pair) {
        boolean hasUnemployedRate = hasUnemployedRate();
        boolean hasFacilityFeeRate = hasFacilityFeeRate();
        boolean hasDiscountRates = hasUnemployedRate || hasFacilityFeeRate;
        RadioButton noDiscountRadio  = hasDiscountRates   ? I18nControls.setI18nProperties(new RadioButton(), "NoDiscount") : null;
        RadioButton unemployedRadio  = hasUnemployedRate  ? I18nControls.setI18nProperties(new RadioButton(), "UnemployedDiscount") : null;
        RadioButton facilityFeeRadio = hasFacilityFeeRate ? I18nControls.setI18nProperties(new RadioButton(), "FacilityFeeDiscount") : null;
        Person person = getPersonAggregate().getPreselectionProfilePerson();
        if (unemployedRadio != null) {
            unemployedRadio.setSelected(Booleans.isTrue(person.isUnemployed()));
            unemployedRadio.selectedProperty().addListener((observable, oldValue, unemployed) -> {
                person.setUnemployed(unemployed);
                if (unemployed)
                    person.setFacilityFee(false);
                displayFeesGroups();
            });
        }
        if (facilityFeeRadio != null) {
            facilityFeeRadio.setSelected(Booleans.isTrue(person.isFacilityFee()));
            facilityFeeRadio.selectedProperty().addListener((observable, oldValue, facilityFee) -> {
                person.setFacilityFee(facilityFee);
                if (facilityFee)
                    person.setUnemployed(false);
                displayFeesGroups();
            });
        }
        if (noDiscountRadio != null) {
            noDiscountRadio.setSelected(Booleans.isNotTrue(person.isUnemployed()) && Booleans.isNotTrue(person.isFacilityFee()));
            noDiscountRadio.selectedProperty().addListener((observable, oldValue, noDiscount) -> {
                if (noDiscount) {
                    person.setUnemployed(false);
                    person.setFacilityFee(false);
                }
                displayFeesGroups();
            });
        }
        Label feesGroupLabel = new Label(pair.get2());
        Node[] nodes = {createImageView(pair.get1()), feesGroupLabel, noDiscountRadio, unemployedRadio, facilityFeeRadio};
        FlowPane header = new FlowPane(Arrays.nonNulls(Node[]::new, nodes));
        header.setHgap(5d);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(5));
        return header;
    }

    private Node renderFeesGroupBody(VisualResult rs) {
        VisualGrid visualGrid = new SkinnedVisualGrid(rs); //LayoutUtil.setMinMaxHeightToPref(new DataGrid(rs));
        visualGrid.setFullHeight(true);
        visualGrid.setSelectionMode(SelectionMode.DISABLED);
        return visualGrid;
    }

    private void onBookButtonPressed(OptionsPreselection optionsPreselection) {
        new RouteToOptionsRequest(optionsPreselection, getHistory()).execute();
    }
}
