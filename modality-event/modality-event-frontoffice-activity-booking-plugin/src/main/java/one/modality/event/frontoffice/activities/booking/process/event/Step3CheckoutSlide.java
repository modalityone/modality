package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.ecommerce.payment.InitiatePaymentArgument;
import one.modality.ecommerce.payment.PaymentService;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.client.event.fx.FXEvent;

public class Step3CheckoutSlide extends StepSlide {
    private final HtmlText eventShortDescriptionInCheckoutSlide = controller.bindI18nEventExpression(new HtmlText(), "shortDescription");
    private final Label venueAddress = controller.bindI18nEventExpression(new Label(), "venue.address");
    private final VBox scheduledItemVBox = new VBox();
    private final Label totalPriceLabel = new Label();

    public Step3CheckoutSlide(SlideController control, BookEventData bed) {
        super(control, bed);
        controller.setStep3CheckoutSlide(this);

    }

    public void buildUi() {
        mainVbox.getChildren().clear();
        mainVbox.setAlignment(Pos.TOP_CENTER);
        Label bookedEventTitleText = controller.bindI18nEventExpression(new Label(), "i18n(this)");
        bookedEventTitleText.getStyleClass().addAll("book-event-primary-title", "emphasize");
        HBox line1 = new HBox(bookedEventTitleText);
        line1.setAlignment(Pos.CENTER_LEFT);
        //bookedEventTitleText.setPrefWidth(MAX_WIDTH - 50);
        line1.setPadding(new Insets(20, 0, 0, 50));
        mainVbox.getChildren().add(line1);
        eventShortDescriptionInCheckoutSlide.getStyleClass().add("subtitle-grey");
        eventShortDescriptionInCheckoutSlide.setMaxWidth(300);
        HBox line2 = new HBox(eventShortDescriptionInCheckoutSlide);
        line2.setAlignment(Pos.BASELINE_LEFT);
        line2.setPadding(new Insets(10, 0, 0, 50));
        mainVbox.getChildren().add(line2);

        SVGPath locationIcon = SvgIcons.createPinpointSVGPath();
        venueAddress.getStyleClass().add("checkout-address");
        venueAddress.setGraphicTextGap(5);
        venueAddress.setGraphic(locationIcon);
        HBox addressBorderHBox = new HBox();
        addressBorderHBox.getChildren().add(venueAddress);
        addressBorderHBox.setAlignment(Pos.CENTER_LEFT);
        //venueAddress.setPrefWidth(MAX_WIDTH - 50);
        venueAddress.setPadding(new Insets(0, 0, 30, 50));
        mainVbox.getChildren().addAll(addressBorderHBox);

        HBox headerHBox = new HBox();
        Region spacerInHeader1 = new Region();
        spacerInHeader1.setPrefWidth(80);
        Label summaryLabel = I18nControls.bindI18nProperties(new Label(), "Summary");
        Region spacerInHeader2 = new Region();
        HBox.setHgrow(spacerInHeader2, Priority.ALWAYS);
        Label priceLabel = I18nControls.bindI18nProperties(new Label(), "Price");
        priceLabel.setPrefWidth(70);
        //The actionLabel is used only because we need to put a graphic element in the right part of the borderpane,
        //so it's balanced with the content that is shown bellow
        Label actionLabel = new Label();
        actionLabel.setPrefWidth(40);
        headerHBox.getChildren().addAll(spacerInHeader1, summaryLabel, spacerInHeader2, priceLabel, actionLabel);
        // headerHBox.setMaxWidth(400);
        headerHBox.setPadding(new Insets(0, 0, 5, 0));

        scheduledItemVBox.setAlignment(Pos.CENTER);
        scheduledItemVBox.setPadding(new Insets(0, 0, 40, 0));
        //The scheduledItemPane containing the details of the checkout will be populated by the function drawScheduledItemInCheckoutView
        //which is called throw the binding
        bookEventData.setDocumentAggregate(bookEventData.getCurrentBooking().getLastestDocumentAggregate());
        scheduledItemVBox.getChildren().clear();
        bookEventData.getDocumentAggregate().getAttendances().forEach(a -> {
            HBox currentScheduledItemHBox = new HBox();
            //currentScheduledItemHBox.setMaxWidth(500); //300+55+45
            String dateFormatted = I18n.getI18nText("DateFormatted", I18n.getI18nText(a.getScheduledItem().getDate().getMonth().name()), a.getScheduledItem().getDate().getDayOfMonth());
            Label name = new Label(a.getScheduledItem().getItem().getName() + " - " + dateFormatted);
            Region spacer1 = new Region();
            Region spacer2 = new Region();
            spacer1.setPrefWidth(80);
            Label price = new Label(EventPriceFormatter.formatWithCurrency(bookEventData.getCurrentBooking().getPolicyAggregate().getRates().get(0).getPrice(), FXEvent.getEvent()));
            name.getStyleClass().add("subtitle-grey");
            price.getStyleClass().add("subtitle-grey");

            Hyperlink trashOption = new Hyperlink();
            SVGPath svgTrash = SvgIcons.createTrashSVGPath();
            svgTrash.setFill(Color.RED);
            trashOption.setGraphic(svgTrash);
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            price.setPrefWidth(55);
            trashOption.setPrefWidth(45);
            trashOption.setOnAction(event -> {
                bookEventData.getCurrentBooking().removeAttendance(a);
                controller.getRecurringEventSchedule().getSelectedDates().remove(a.getScheduledItem().getDate());
                scheduledItemVBox.getChildren().remove(currentScheduledItemHBox);
                totalPriceLabel.setText(EventPriceFormatter.formatWithCurrency(bookEventData.getPriceCalculator().calculateTotalPrice(bookEventData.getCurrentBooking().getLastestDocumentAggregate()), FXEvent.getEvent()));
            });
            currentScheduledItemHBox.getChildren().addAll(spacer1, name, spacer2, price, trashOption);
            scheduledItemVBox.getChildren().add(currentScheduledItemHBox);
            //Now we calculate the price and update the graphic related to the price
            //totalPriceProperty.setValue(String.valueOf(priceCalculator.calculateTotalPrice(documentAggregate)));
        });

        HBox totalHBox = new HBox();
        totalHBox.getStyleClass().add("line-total");
        //totalHBox.setMaxWidth(MAX_WIDTH);
        Label totalLabel = I18nControls.bindI18nProperties(new Label(), "Total");
        Region spacerTotal = new Region();
        totalLabel.setPadding(new Insets(5, 0, 5, 50));
        HBox.setHgrow(spacerTotal, Priority.ALWAYS);
        totalPriceLabel.setPadding(new Insets(5, 75, 5, 0));
        int totalPrice = bookEventData.getPriceCalculator().calculateTotalPrice(bookEventData.getCurrentBooking().getLastestDocumentAggregate());
        totalPriceLabel.setText(EventPriceFormatter.formatWithCurrency(totalPrice, FXEvent.getEvent()));
        totalHBox.getChildren().addAll(totalLabel, spacerTotal, totalPriceLabel);

        HBox sepHBox = new HBox();
        sepHBox.setPadding(new Insets(0, 0, 30, 0));
        scheduledItemVBox.getChildren().add(sepHBox);

        scheduledItemVBox.getChildren().add(totalHBox);
        mainVbox.getChildren().addAll(headerHBox, scheduledItemVBox);

        HBox separatorHBox = new HBox();
        separatorHBox.setPadding(new Insets(0, 0, 50, 0));
        mainVbox.getChildren().add(separatorHBox);

        Button payButton = I18nControls.bindI18nProperties(new Button(), "Pay");
        //We manage the property of the button in css
        payButton.setGraphicTextGap(30);
        payButton.getStyleClass().addAll("event-button", "success-button");
        payButton.setMaxWidth(150);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(20, 20);
        progressIndicator.setStyle("-fx-progress-color: white;");

        payButton.setOnAction(event -> {
            payButton.graphicProperty().unbind();
            payButton.setGraphic(progressIndicator);
            bookEventData.getCurrentBooking().submitChanges("Booked Online")
                    .onFailure(result -> Platform.runLater(() -> {
                            controller.displayErrorMessage("ErrorWhileInsertingBooking");
                            Console.log(result);
                    }))
                    .onSuccess(result -> Platform.runLater(() -> {
                        I18nControls.bindI18nProperties(payButton, "Pay");
                        bookEventData.setBookingNumber(Integer.parseInt(result.getDocumentRef().toString()));
                        Object documentPrimaryKey = result.getDocumentPrimaryKey();
                        bookEventData.setDocumentPrimaryKey(documentPrimaryKey);
                        bookEventData.setTotalPrice(totalPrice);
                        PaymentService.initiatePayment(
                                        new InitiatePaymentArgument(totalPrice, documentPrimaryKey)
                                )
                                .onFailure(paymentResult -> Platform.runLater(() -> {
                                    controller.displayErrorMessage("ErrorWhileInitiatingPayment");
                                    Console.log(result);
                                }))
                                .onSuccess(paymentResult -> Platform.runLater(() -> {
                                    WebPaymentForm webPaymentForm = new WebPaymentForm(paymentResult, FXUserPerson.getUserPerson());
                                    controller.getStep4PaymentSlide().setWebPaymentForm(webPaymentForm);
                                    controller.displayNextSlide();
                                }));
                    }));
        });
            // controller.displayNextSlide();}));
            mainVbox.getChildren().add(payButton);
    }
}
