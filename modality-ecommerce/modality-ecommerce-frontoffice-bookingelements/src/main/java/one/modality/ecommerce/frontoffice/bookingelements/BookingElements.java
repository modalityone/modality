package one.modality.ecommerce.frontoffice.bookingelements;

import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
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
import javafx.scene.text.TextAlignment;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.client.workingbooking.WorkingBooking;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.ecommerce.shared.pricecalculator.PriceCalculator;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class BookingElements {

    public static Label createStrongLabel() {
        return Bootstrap.strong(createLabel());
    }

    public static Label createStrongLabel(Object i18nKey) {
        return I18nControls.bindI18nProperties(createStrongLabel(), i18nKey);
    }

    public static Label createSecondaryLabel() {
        return Bootstrap.textSecondary(createLabel());
    }

    public static Label createSecondaryLabel(Object i18nKey) {
        return I18nControls.bindI18nProperties(createSecondaryLabel(), i18nKey);
    }

    private static Label createLabel() {
        Label label = new Label();
        label.setTextAlignment(TextAlignment.CENTER);
        Controls.setupTextWrapping(label, true, false);
        return label;
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
        //vBox.getStyleClass().addAll(pageStyleClass);
        vBox.setPadding(new Insets(48, 0, 48, 0));
        vBox.setAlignment(Pos.CENTER);
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

    public static Label createPricePromptLabel(Object i18nKey, boolean appendColons) {
        if (appendColons)
            i18nKey = I18nKeys.appendColons(i18nKey);
        return Bootstrap.strong(I18nControls.newLabel(i18nKey));
    }

    public static Label createPriceAmountLabel() {
        return createStyledLabel("price-label");
    }

    public static Label createPriceAmountLabel(StringProperty formattedPriceProperty) {
        Label priceLabel = createPriceAmountLabel();
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
}
