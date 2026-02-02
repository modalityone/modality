package one.modality.booking.frontoffice.bookingpage.pages.payment;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.FlipPane;
import dev.webfx.extras.panes.GoldenRatioPane;
import dev.webfx.extras.panes.GrowingPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.util.border.BorderFactory;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.windowhistory.WindowHistory;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.client.i18n.EcommerceI18nKeys;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingelements.BookingElements;
import one.modality.booking.frontoffice.bookingform.BookingForm;
import one.modality.booking.frontoffice.bookingform.BookingFormActivityCallback;
import one.modality.booking.frontoffice.bookingform.BookingFormI18nKeys;
import one.modality.booking.frontoffice.bookingform.GatewayPaymentForm;
import one.modality.booking.frontoffice.bookingpage.BookingFormPage;
import one.modality.event.frontoffice.activities.book.BookI18nKeys;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;

import java.util.function.Function;

/**
 * @author Bruno Salmon
 */
public final class PaymentPage implements BookingFormPage {

    private final BookingForm bookingForm;
    private final GridPane gridPane = BookingElements.createOptionsGridPane(false);
    private final MonoPane embeddedLoginContainer = new MonoPane();
    private final Button saveButton = BookingElements.createPrimaryButton(BookingFormI18nKeys.SaveBooking);
    private final Button payButton = BookingElements.createBlackButton(BookingFormI18nKeys.PayNow1);
    private final IntegerProperty selectedAmountProperty = new SimpleIntegerProperty();
    private final Label selectedAmountCurrencyLabel = new Label();
    private final TextField selectedAmountValueTextField = BookingElements.createPriceTextField();
    private final Label paymentBottomLabel = BookingElements.createSecondaryWordingLabel(BookingFormI18nKeys.PaymentBottomMessage);
    private final ValidationSupport validationSupport = new ValidationSupport();
    private final FlipPane flipPane = new FlipPane();
    private final BooleanProperty canGoBackProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty canGoForwardProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty endReachedProperty = new SimpleBooleanProperty(false);
    private WorkingBookingProperties workingBookingProperties;

    public PaymentPage(BookingForm bookingForm) {
        this.bookingForm = bookingForm;
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.NEVER);
        gridPane.getColumnConstraints().setAll(c1, c2, c2);
        gridPane.setHgap(5);
        gridPane.setVgap(20);
        gridPane.setMaxWidth(450);
        VBox spinnerButtons = new VBox(3, createAmountSpinner(true), createAmountSpinner(false));
        HBox selectedAmountHBox = new HBox(selectedAmountCurrencyLabel, selectedAmountValueTextField, spinnerButtons);
        HBox selectAmountHBox = new HBox(20,
            BookingElements.createWordingLabel(BookingFormI18nKeys.SelectPaymentAmount),
            selectedAmountHBox);
        selectAmountHBox.setAlignment(Pos.CENTER);
        selectedAmountHBox.setAlignment(Pos.CENTER);
        selectedAmountHBox.setBorder(BorderFactory.newBorder(Color.BLACK, 10, 2));
        HBox.setMargin(selectedAmountCurrencyLabel, new Insets(10, 0, 10, 15));
        selectAmountHBox.setFillHeight(false); // This is to prevent `selectedAmountHBox` growing in height
        selectedAmountValueTextField.setPadding(Insets.EMPTY); // This is to remove the default padding set by WebFX
        // in the web version (see WebFX TextField implementation)
        VBox selectedAmountForm = BookingElements.createFormPageVBox(true,
            BookingElements.createWordingLabel(BookingFormI18nKeys.PaymentTopMessage),
            gridPane,
            selectAmountHBox,
            embeddedLoginContainer,
            BookingElements.twoLabels(20, true, saveButton, payButton),
            paymentBottomLabel
        );
        flipPane.setFront(selectedAmountForm);
    }

    @Override
    public Object getTitleI18nKey() {
        return BookingFormI18nKeys.Payment;
    }

    @Override
    public Node getView() {
        return flipPane;
    }

    @Override
    public MonoPane getEmbeddedLoginContainer() {
        return embeddedLoginContainer;
    }

    @Override
    public boolean isShowingOwnSubmitButton() {
        return true;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
        Event event = workingBookingProperties.getEvent();
        int total = workingBookingProperties.getTotal();
        int minDeposit = workingBookingProperties.getMinDeposit();
        int deposit = workingBookingProperties.getDeposit();
        int balance = total - deposit;
        int maxAmount = Math.max(0, balance);
        int minAmount = Math.min(maxAmount, Math.max(100, minDeposit - deposit));
        int initialAmount = deposit < minDeposit ? minAmount : maxAmount;
        String currencySymbol = EventPriceFormatter.getEventCurrencySymbol(event);
        Function<Number, String> priceWithCurrencyFormatter = amount -> EventPriceFormatter.formatWithCurrency(amount, event);
        Function<Number, String> priceWithoutCurrencyFormatter = EventPriceFormatter::formatWithoutCurrency;
        gridPane.getChildren().clear();
        gridPane.add(BookingElements.createPricePromptLabel(EcommerceI18nKeys.Total, false), 0, 0);
        gridPane.add(BookingElements.createPricePromptLabel(EcommerceI18nKeys.MinDeposit, false), 0, 1);
        gridPane.add(BookingElements.createPricePromptLabel(EcommerceI18nKeys.Deposit, false), 0, 2);
        gridPane.add(BookingElements.createPricePromptLabel(EcommerceI18nKeys.Balance, false), 0, 3);
        gridPane.add(createPriceLabel(currencySymbol), 1, 0);
        gridPane.add(createPriceLabel(priceWithoutCurrencyFormatter.apply(total)), 2, 0);
        gridPane.add(createPriceLabel(currencySymbol), 1, 1);
        gridPane.add(createPriceLabel(priceWithoutCurrencyFormatter.apply(minDeposit)), 2, 1);
        gridPane.add(createPriceLabel(currencySymbol), 1, 2);
        gridPane.add(createPriceLabel(priceWithoutCurrencyFormatter.apply(deposit)), 2, 2);
        gridPane.add(createPriceLabel(currencySymbol), 1, 3);
        gridPane.add(createPriceLabel(priceWithoutCurrencyFormatter.apply(balance)), 2, 3);
        // For now because the context is only online Festivals so far, the amount to pay is necessarily the whole balance
        selectedAmountProperty.set(initialAmount);
        selectedAmountCurrencyLabel.setText(currencySymbol);
        selectedAmountValueTextField.setText(priceWithoutCurrencyFormatter.apply(initialAmount));
        selectedAmountValueTextField.setDisable(maxAmount == 0);
        BookingFormActivityCallback activityCallback = bookingForm.getActivityCallback();
        // We hide the save button if there are no changes
        Layouts.setManagedAndVisibleProperties(saveButton, workingBookingProperties.hasChanges());
        // Same with paymentBottomLabel because it says: If you save the booking for later, it will be saved in your orders until you complete the minimum payment.
        Layouts.bindManagedAndVisiblePropertiesTo(saveButton.visibleProperty(), paymentBottomLabel);
        // And when it's visible, we disable if the booking is not ready to submit
        BooleanBinding disableSubmit = activityCallback.readyToSubmitBookingProperty().not();
        saveButton.disableProperty().bind(disableSubmit);
        // When it's visible and enabled, the user can submit the changes but with no deposit to pay
        saveButton.setOnAction(e -> activityCallback.submitBooking(0, saveButton, payButton));
        // We show the amount to pay in the button itself
        I18nControls.bindI18nProperties(payButton, BookingFormI18nKeys.PayNow1, selectedAmountProperty.map(priceWithCurrencyFormatter));
        // But it is disabled if the booking is not ready to submit or if there is nothing to pay
        payButton.disableProperty().bind(disableSubmit.or(selectedAmountProperty.lessThanOrEqualTo(0)));
        // When it's enabled, the user can submit the changes and pay the selected amount
        payButton.setOnAction(e -> activityCallback.submitBooking(selectedAmountProperty.get(), this::displayGatewayPaymentForm, payButton, saveButton));
        //selectedAmountValueTextField.setDisable(maxAmount <= minAmount);
        StringProperty errorMessageProperty = new SimpleStringProperty();
        validationSupport.addValidationRule(
            Bindings.createBooleanBinding(() -> {
                try {
                    int amount = parseSelectedAmountValue();
                    if (amount < minAmount || amount > maxAmount) {
                        errorMessageProperty.bind(I18n.i18nTextProperty(BookingPageI18nKeys.MustBeInRange2, priceWithCurrencyFormatter.apply(minAmount), priceWithCurrencyFormatter.apply(maxAmount)));
                        return false;
                    }
                    selectedAmountProperty.set(amount);
                    return true;
                } catch (NumberFormatException e) {
                    errorMessageProperty.bind(I18n.i18nTextProperty(BookingPageI18nKeys.IncorrectPriceFormat));
                    return false;
                }
            }, selectedAmountValueTextField.textProperty()),
            selectedAmountValueTextField,
            errorMessageProperty
        );
        FXProperties.runOnPropertyChange(validationSupport::isValid, selectedAmountValueTextField.textProperty());
    }

    @Override
    public void onTransitionFinished() {
        selectedAmountValueTextField.requestFocus();
    }

    @Override
    public ObservableBooleanValue canGoBackProperty() {
        return canGoBackProperty;
    }

    private static Label createPriceLabel(String currencySymbol) {
        Label priceLabel = BookingElements.createPriceLabel(currencySymbol);
        GridPane.setHalignment(priceLabel, HPos.RIGHT);
        return priceLabel;
    }

    private Node createAmountSpinner(boolean up) {
        SVGPath svgPath = up ? SvgIcons.createRoundTriangleUp() : SvgIcons.createRoundTriangleDown();
        svgPath.setFill(null);
        SvgIcons.setSVGPathStroke(svgPath, Color.BLACK, 1);
        MonoPane buttonPane = SvgIcons.armButton(SvgIcons.createButtonPane(svgPath), () -> spinAmount(up));
        buttonPane.setPadding(new Insets(up ? 10 : 0, 10, up ? 0 : 10, 10));
        return buttonPane;
    }

    private int parseSelectedAmountValue() {
        return (int) (100 * Double.parseDouble(selectedAmountValueTextField.getText().trim()));
    }

    private void spinAmount(boolean up) {
        try {
            int amount = parseSelectedAmountValue();
            amount = ((amount + 99) / 100) * 100; // Rounding the amount
            amount += up ? 100 : -100; // Incrementing or decrementing the amount
            selectedAmountValueTextField.setText(EventPriceFormatter.formatWithoutCurrency(amount));
        } catch (NumberFormatException ignored) {
        }
    }

    private void displayGatewayPaymentForm(GatewayPaymentForm gatewayPaymentForm) {
        // Embedding the payment form in a GrowingPane so when we unload it, that doesn't change the size of the flipPane
        GrowingPane growingPane = new GrowingPane(gatewayPaymentForm.getView());
        flipPane.setBack(growingPane);
        flipPane.setPadding(new Insets(30, 0, 0, 0));
        flipPane.flipToBack();
        // Preventing the user going back (will disable the back button)
        canGoBackProperty.set(false);
        // Showing the cancellation message if the user cancelled the payment
        gatewayPaymentForm.setCancelPaymentResultHandler(ar -> {
            VBox vBox = new VBox(50,
                BookingElements.createWordingLabel(BookingPageI18nKeys.PaymentCancelledBookingSaved),
                BookingElements.createOrderLink(BookI18nKeys.BookingNumber1, workingBookingProperties, WindowHistory.getProvider())
            );
            vBox.setAlignment(Pos.CENTER);
            flipPane.setFront(new GoldenRatioPane(vBox));
            flipPane.flipToFront(() ->         // Flipping to the cancellation side
                growingPane.setContent(null)); // Ensuring the payment form is unloaded
            endReachedProperty.set(true);
        });
    }

    @Override
    public ObservableBooleanValue canGoForwardProperty() {
        return canGoForwardProperty;
    }

    @Override
    public ObservableBooleanValue endReachedProperty() {
        return endReachedProperty;
    }
}
