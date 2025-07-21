package one.modality.ecommerce.frontoffice.bookingelements;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.utility.tyler.TextUtility;
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

    private static Label createLabel() {
        return new Label();
    }

    public static Label createStrongLabel() {
        return Bootstrap.strong(createLabel());
    }

    public static Label createWordingLabel() {
        return createWordingLabel(true);
    }

    public static Label createWordingLabel(boolean strong) {
        Label label = strong ? createStrongLabel() : createLabel();
        label.getStyleClass().add("wording-label");
        label.setTextAlignment(TextAlignment.CENTER);
        Controls.setupTextWrapping(label, true, false);
        return label;
    }

    public static Label createWordingLabel(Object i18nKey) {
        return I18nControls.bindI18nProperties(createWordingLabel(), i18nKey);
    }

    public static Label createSecondaryWordingLabel() {
        return Bootstrap.textSecondary(createWordingLabel(false));
    }

    public static Label createSecondaryWordingLabel(Object i18nKey) {
        return I18nControls.bindI18nProperties(createSecondaryWordingLabel(), i18nKey);
    }

    public static Region twoLabels(Label label1, Label label2) {
        HBox hBox = new HBox(5, label1, label2);
        hBox.setAlignment(Pos.CENTER);
        return hBox;
    }

    public static Region buttonBar(Button button1, Button button2) {
        HBox buttonBar = new HBox(20, button1, button2);
        buttonBar.setMaxWidth(Region.USE_PREF_SIZE);
        return buttonBar;
    }

    public static GridPane createOptionsGridPane(boolean largeVGap) {
        GridPane gridPane = new GridPane(24, largeVGap ? 24 : 12);
        gridPane.setAlignment(Pos.CENTER);
        return gridPane;
    }

    public static VBox createPageVBox(String pageStyleClass, boolean largeSpacing, Node... children) {
        VBox vBox = new VBox(largeSpacing ? 48 : 24, children);
        //vBox.getStyleClass().add(pageStyleClass);
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
        VBox.setMargin(textArea, new Insets(0, 95, 0, 95));
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
        return createStyledLabel("price-label");
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

    public static Label createPriceLabel(StringProperty formattedPriceProperty) {
        Label priceLabel = createPriceLabel();
        priceLabel.textProperty().bind(formattedPriceProperty);
        return priceLabel;
    }

    private static Label createStyledLabel(String styleClass) {
        Label label = new Label();
        label.getStyleClass().add(styleClass);
        return label;
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
                    buttonContent = new HBox(20, TextUtility.createText(CrmI18nKeys.PersonToBook + ":", Color.GRAY), buttonContent);
                return buttonContent;
            }
        }
            .ifNotNullOtherwiseEmpty(FXModalityUserPrincipal.modalityUserPrincipalProperty(), mup -> DqlStatement.where("frontendAccount=?", mup.getUserAccountId()))
            .appendNullEntity(false)
            .setSearchCondition(null) // Temporarily disabling search because it doesn't work TODO fix this
            ;
        // Creating a virtual teacher named "All" that will be used to select all teachers
        EntityStore store = personSelector.getStore();
        Person anotherPerson = store.createEntity(Person.class);
        anotherPerson.setFirstName("Someone else"); // TODO make this i18n
        personSelector.setVisualNullEntity(anotherPerson);
        personSelector.selectedItemProperty().bindBidirectional(FXPersonToBook.personToBookProperty());
        Button personButton = Bootstrap.largeButton(personSelector.getButton());
        personButton.setMinWidth(300);
        personButton.setMaxWidth(Region.USE_PREF_SIZE);
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
