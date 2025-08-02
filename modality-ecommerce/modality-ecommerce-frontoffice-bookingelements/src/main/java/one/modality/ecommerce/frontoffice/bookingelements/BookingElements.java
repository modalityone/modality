package one.modality.ecommerce.frontoffice.bookingelements;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.responsive.ResponsiveDesign;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Person;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.crm.client.i18n.CrmI18nKeys;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;
import one.modality.ecommerce.client.workingbooking.FXPersonToBook;
import one.modality.ecommerce.client.workingbooking.WorkingBooking;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.ecommerce.shared.pricecalculator.PriceCalculator;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class BookingElements {

    public static <N extends Node> N styleBookingElementsContainer(N node, boolean bookingForm) {
        if (bookingForm)
            style(node, "booking-form");
        return style(node, "booking-elements");
    }

    private static <N extends Node> N style(N node, String styleClass) {
        node.getStyleClass().add(styleClass);
        return node;
    }


    public static Label createWordingLabel() {
        return createWordingLabel(null);
    }

    public static Label createWordingLabel(Object i18nKey) {
        return wordingLabel(new Label(), i18nKey);
    }

    public static <L extends Labeled> L wordingLabel(L label) {
        return wordingLabel(label, null);
    }

    public static <L extends Labeled> L wordingLabel(L label, Object i18nKey) {
        return wordingLabel(label, true, i18nKey);
    }

    private static <L extends Labeled> L wordingLabel(L label, boolean strong, Object i18nKey) {
        label.getStyleClass().add("wording-label");
        label.setTextAlignment(TextAlignment.CENTER);
        Controls.setupTextWrapping(label, true, false);
        if (strong)
            Bootstrap.strong(label);
        if (i18nKey != null)
            I18nControls.bindI18nProperties(label, i18nKey);
        return label;
    }

    public static Label createSecondaryWordingLabel() {
        return createSecondaryWordingLabel(null);
    }

    public static Label createSecondaryWordingLabel(Object i18nKey) {
        return secondaryWordingLabel(new Label(), i18nKey);
    }

    public static <L extends Labeled> L secondaryWordingLabel(L label) {
        return secondaryWordingLabel(label, null);
    }

    public static <L extends Labeled> L secondaryWordingLabel(L label, Object i18nKey) {
        return wordingLabel(Bootstrap.textSecondary(label), false, i18nKey);
    }

    public static <L extends Labeled> L optionLabel(L label) {
        return optionLabel(label, false);
    }

    private static <L extends Labeled> L optionLabel(L label, boolean secondary) {
        // We ignore graphics such as the French flag for French audio recording
        if (label instanceof Label) // We don't ignore graphic for checkboxes or radio buttons, of course
            label.setContentDisplay(ContentDisplay.TEXT_ONLY);
        label.setPadding(new Insets(5));
        return style(label, secondary ? "option-label-secondary" : "option-label");
    }

    public static <L extends Labeled> L secondaryOptionLabel(L label) {
        return optionLabel(label, true);
    }

    public static Region twoLabels(double spacing, boolean verticalSwap, Labeled label1, Labeled label2) {
        MonoPane monoPane = new MonoPane();
        new ResponsiveDesign(monoPane)
            // 1. Horizontal layout (when labels can be on the same line without wrapping)
            .addResponsiveLayout(/* applicability test: */ width ->
                    width >= label1.prefWidth(-1) + label2.prefWidth(-1) + 5,
                /* apply method: */ () -> {
                    label1.setWrapText(false);
                    label2.setWrapText(false);
                    HBox hBox = new HBox(spacing, label1, label2);
                    hBox.setAlignment(Pos.CENTER);
                    monoPane.setContent(hBox);
                }, /* test dependencies: */ label1.textProperty(), label2.textProperty())
            // 2. Vertical layout (when the labels can't be on the same line without wrapping)
            .addResponsiveLayout(/* apply method: */ () -> {
                    label1.setWrapText(true);
                    label2.setWrapText(true);
                    VBox vBox = verticalSwap ? new VBox(spacing, label2, label1) : new VBox(spacing, label1, label2);
                    vBox.setAlignment(Pos.CENTER);
                    monoPane.setContent(vBox);
                }
            ).start();
        monoPane.setMinWidth(0);
        return monoPane;
    }

    public static GridPane createOptionsGridPane(boolean largeVGap) {
        GridPane gridPane = new GridPane(24, largeVGap ? 24 : 12);
        gridPane.setAlignment(Pos.CENTER);
        return gridPane;
    }

    public static VBox createFormPageVBox(boolean largeSpacing, Node... children) {
        return formPageVBox(new VBox(children), largeSpacing);
    }

    public static VBox formPageVBox(VBox vBox, boolean largeSpacing) {
        vBox.setSpacing(largeSpacing ? 48 : 24);
        vBox.setPadding(new Insets(48, 0, 48, 0));
        vBox.setAlignment(Pos.CENTER);
        vBox.setMinHeight(Region.USE_PREF_SIZE);
        return vBox;
    }

    public static Button createPrimaryButton(Object i18nKey) {
        return Bootstrap.largeButton(Bootstrap.primaryButton(I18nControls.newButton(i18nKey)));
    }

    public static Button createBlackButton(Object i18nKey) {
        return Bootstrap.largeButton(ModalityStyle.blackButton(I18nControls.newButton(i18nKey)));
    }

    public static TextArea createTextArea() {
        TextArea textArea = new TextArea();
        textArea.setMinHeight(130);
        textArea.setMaxWidth(600);
        return textArea;
    }

    public static Label createPeriodLabel() {
        return createStyledLabel("period-label");
    }

    public static Label createPeriodLabel(String period) {
        Label periodLabel = createPeriodLabel();
        periodLabel.setText(period);
        return periodLabel;
    }

    public static Label createPricePromptLabel(Object i18nKey, boolean appendColons) {
        if (appendColons)
            i18nKey = I18nKeys.appendColons(i18nKey);
        return Bootstrap.strong(I18nControls.newLabel(i18nKey));
    }

    public static Label createPriceLabel() {
        Label priceLabel = createStyledLabel("price-label");
        // Preventing price labels to shrink on mobiles
        priceLabel.setMinWidth(Region.USE_PREF_SIZE);
        return priceLabel;
    }

    public static Label createPriceLabel(String formattedPrice) {
        Label priceAmountLabel = createPriceLabel();
        priceAmountLabel.setText(formattedPrice);
        return priceAmountLabel;
    }

    public static Label createSubPriceLabel() {
        return createStyledLabel("sub-price-label");
    }

    public static Label createSubPriceLabel(String formattedPrice) {
        Label priceAmountLabel = createSubPriceLabel();
        priceAmountLabel.setText(formattedPrice);
        return priceAmountLabel;
    }

    public static Label createPriceLabel(ObservableValue<String> formattedPriceProperty) {
        Label priceLabel = createPriceLabel();
        priceLabel.textProperty().bind(formattedPriceProperty);
        return priceLabel;
    }

    private static Label createStyledLabel(String styleClass) {
        return style(new Label(), styleClass);
    }

    public static TextField createPriceTextField() {
        TextField priceTextField = style(new TextField(), "price-textfield");
        priceTextField.setAlignment(Pos.CENTER_RIGHT);
        priceTextField.setMinWidth(50);
        priceTextField.setMaxWidth(80);
        Controls.setHtmlInputType(priceTextField, "number");
        return priceTextField;
    }

    public static void setupPeriodOption(List<ScheduledItem> bookableScheduledItems, Label priceLabel, BooleanProperty selectedProperty, WorkingBooking workingBooking) {
        selectedProperty.set(workingBooking.areScheduledItemsBooked(bookableScheduledItems));
        FXProperties.runOnPropertyChange(selected -> {
            if (selected)
                workingBooking.bookScheduledItems(bookableScheduledItems, false);
            else
                workingBooking.unbookScheduledItems(bookableScheduledItems);
        }, selectedProperty);
        PolicyAggregate policyAggregate = workingBooking.getPolicyAggregate();
        WorkingBooking periodWorkingBooking = new WorkingBooking(policyAggregate, workingBooking.getInitialDocumentAggregate());
        periodWorkingBooking.unbookScheduledItems(bookableScheduledItems);
        int unbookedTotalPrice = new PriceCalculator(periodWorkingBooking.getLastestDocumentAggregate()).calculateTotalPrice();
        periodWorkingBooking.bookScheduledItems(bookableScheduledItems, false);
        int bookedTotalPrice = new PriceCalculator(periodWorkingBooking.getLastestDocumentAggregate()).calculateTotalPrice();
        int optionPrice = bookedTotalPrice - unbookedTotalPrice;
        priceLabel.setText(EventPriceFormatter.formatWithCurrency(optionPrice, policyAggregate.getEvent()));
    }

    public static Button createPersonToBookButton(boolean embedPersonToBookText) {
        return createPersonToBookSelector(embedPersonToBookText).getButton();
    }

    public static EntityButtonSelector<Person> createPersonToBookSelector(boolean embedPersonToBookText) {
        return createPersonToBookSelector(embedPersonToBookText, new ButtonFactoryMixin() {}, DataSourceModelService.getDefaultDataSourceModel());
    }

    private static EntityButtonSelector<Person> createPersonToBookSelector(boolean embedPersonToBookText, ButtonFactoryMixin buttonFactory, DataSourceModel dataSourceModel) {
        EntityButtonSelector<Person> personSelector = new EntityButtonSelector<Person>(
            "{class: 'Person', alias: 'p', columns: [{expression: '[firstName,lastName]'}], fields: 'email,phone,postCode,cityName,country,organization', orderBy: 'id'}",
            buttonFactory, FXMainFrameDialogArea::getDialogArea, dataSourceModel
        ) { // Overriding the button content to add the "Teacher" prefix text
            @Override
            protected Node getOrCreateButtonContentFromSelectedItem() {
                Node buttonContent = super.getOrCreateButtonContentFromSelectedItem();
                if (embedPersonToBookText)
                    buttonContent = new HBox(20, I18n.newText(I18nKeys.appendColons(CrmI18nKeys.PersonToBook)), buttonContent);
                return buttonContent;
            }
        }
            .setDialogPrefRowHeight(37)
            .setDialogFullHeight(true)
            .setDialogCellMargin(new Insets(17))
            .setSearchCondition(null) // Temporarily disabling search because it doesn't work TODO fix this
            .setDialogStyleClass("person-to-book-dialog")
            .ifNotNullOtherwiseEmpty(FXModalityUserPrincipal.modalityUserPrincipalProperty(), mup -> DqlStatement.where("frontendAccount=?", mup.getUserAccountId()))
            .appendNullEntity(false)
            ;
        // Creating a virtual teacher named "All" that will be used to select all teachers
        EntityStore store = personSelector.getStore();
        Person anotherPerson = store.createEntity(Person.class);
        I18n.bindI18nTextProperty(EntityBindings.getStringFieldProperty(anotherPerson, "firstName"), "addNewPerson"); // BookingFormI18nKeys.addNewPerson TODO: move in this module
        personSelector.setVisualNullEntity(anotherPerson);
        personSelector.selectedItemProperty().bindBidirectional(FXPersonToBook.personToBookProperty());
        Button personButton = Bootstrap.largeButton(personSelector.getButton());
        personButton.setMinWidth(300);
        personButton.setMaxWidth(Region.USE_PREF_SIZE);
        personButton.getStyleClass().add("person-to-book-button");
        VBox.setMargin(personButton, new Insets(20, 0, 20, 0));
        Layouts.bindManagedAndVisiblePropertiesTo(FXModalityUserPrincipal.loggedInProperty(), personButton);
        return personSelector;
    }

    public static Label createTestModeLabel() {
        return Bootstrap.strong(Bootstrap.textDanger(new Label("TEST MODE")));
    }

    public static Label createTestModeBadge() {
        return Bootstrap.dangerBadge(createTestModeLabel());
    }

}
