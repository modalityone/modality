package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.windowhistory.WindowHistory;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Person;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Site;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.ecommerce.payment.PaymentService;
import one.modality.ecommerce.payment.client.ClientPaymentUtil;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.frontoffice.activities.booking.process.account.CheckoutAccountRouting;

import java.time.LocalDate;

public class Step3CheckoutSlide extends StepSlide {
    private final HtmlText eventShortDescriptionInCheckoutSlide = controller.bindI18nEventExpression(new HtmlText(), "shortDescription");
    private final Label venueAddress = controller.bindI18nEventExpression(new Label(), "venue.address");
    private final VBox pastScheduledItemVBox = new VBox();
    private final VBox scheduledItemVBox = new VBox();
    private final Label previousTotalPriceLabel = new Label();
    private final Label totalPriceLabel = new Label();
    // Node property that will be managed by the sub-router to mount the CheckoutAccountActivity (when routed)
    private final ObjectProperty<Node> checkoutAccountMountNodeProperty = new SimpleObjectProperty<>();

    public Step3CheckoutSlide(SlideController control, BookEventData bed) {
        super(control, bed);
        controller.setStep3CheckoutSlide(this);

    }

    // Exposing accountMountNodeProperty for the sub-routing binding (done in BookEventActivity)
    public ObjectProperty<Node> accountMountNodeProperty() {
        return checkoutAccountMountNodeProperty;
    }

    public void buildUi() {
        mainVbox.getChildren().clear();
        bookEventData.setBalanceProperty(0);
        mainVbox.setAlignment(Pos.TOP_CENTER);
        Label bookedEventTitleText = controller.bindI18nEventExpression(new Label(), "i18n(this)");
        bookedEventTitleText.getStyleClass().addAll("book-event-primary-title", "emphasize");

        MonoPane topPane = new MonoPane(bookedEventTitleText);
        topPane.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(topPane, new Insets(20,0,0,50));
        mainVbox.getChildren().add(topPane);

        eventShortDescriptionInCheckoutSlide.getStyleClass().add("subtitle-grey");
        eventShortDescriptionInCheckoutSlide.setMaxWidth(350); // The idea is to balance the description over 2 lines of the same length TODO: see if this can be made generic whatever the description
        MonoPane shortDescriptionMonoPane = new MonoPane(eventShortDescriptionInCheckoutSlide);
        topPane.setAlignment(Pos.BASELINE_LEFT);
        VBox.setMargin(topPane, new Insets(10,0,0,50));
        mainVbox.getChildren().add(shortDescriptionMonoPane);

        SVGPath locationIcon = SvgIcons.createPinpointSVGPath();
        venueAddress.getStyleClass().add("checkout-address");
        venueAddress.setGraphicTextGap(5);
        venueAddress.setGraphic(locationIcon);
        HBox addressBorderHBox = new HBox();
        addressBorderHBox.getChildren().add(venueAddress);
        addressBorderHBox.setAlignment(Pos.CENTER_LEFT);
        venueAddress.setPadding(new Insets(0, 0, 30, 50));
        mainVbox.getChildren().addAll(addressBorderHBox);

        int previousTotalPrice;
        int balance;
        //FIRST PART: WHAT HAS BEEN ALREADY BOOKED FOR THIS EVENT IN THE PAST
        if(!bookEventData.getCurrentBooking().isNewBooking()) {
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

            pastScheduledItemVBox.setAlignment(Pos.CENTER);
            pastScheduledItemVBox.setPadding(new Insets(0, 0, 40, 0));
            pastScheduledItemVBox.getChildren().clear();
            bookEventData.getDocumentAggregate().getExistingAttendancesStream().forEach(a -> {
                HBox currentScheduledItemHBox = new HBox();
                //currentScheduledItemHBox.setMaxWidth(500); //300+55+45
                ScheduledItem scheduledItem = a.getScheduledItem();
                LocalDate date = scheduledItem.getDate();
                Item item = scheduledItem.getItem();
                Site site = scheduledItem.getSite();
                String dateFormatted = I18n.getI18nText("DateFormatted", I18n.getI18nText(date.getMonth().name()), date.getDayOfMonth());
                Label name = new Label(item.getName() + " - " + dateFormatted + " (already booked)");
                Region spacer1 = new Region();
                Region spacer2 = new Region();
                spacer1.setPrefWidth(80);
                HBox.setHgrow(spacer2, Priority.ALWAYS);
                Label price = new Label(EventPriceFormatter.formatWithCurrency(bookEventData.getPolicyAggregate().getSiteItemRates(site, item).get(0).getPrice(), FXEvent.getEvent()));
                price.setPrefWidth(55);
                name.getStyleClass().add("checkout-address");
                price.getStyleClass().add("checkout-address");
                currentScheduledItemHBox.getChildren().addAll(spacer1, name, spacer2, price);
                pastScheduledItemVBox.getChildren().add(currentScheduledItemHBox);
            });
            HBox previousTotalHBox = new HBox();
            previousTotalHBox.getStyleClass().add("line-total");
            //totalHBox.setMaxWidth(MAX_WIDTH);
            Label previousTotalLabel = I18nControls.bindI18nProperties(new Label(), "Total");
            Region spacerPreviousTotal = new Region();
            previousTotalLabel.setPadding(new Insets(5, 0, 5, 50));
            HBox.setHgrow(spacerPreviousTotal, Priority.ALWAYS);
            previousTotalLabel.setPadding(new Insets(5, 75, 5, 0));
            previousTotalPrice = bookEventData.getPriceCalculatorForPastOption().calculateTotalPrice();
            int deposit = bookEventData.getDocumentAggregate().getDeposit();
            balance = previousTotalPrice - deposit;
            bookEventData.setBalanceProperty(balance);
            previousTotalPriceLabel.setText("Total: " + EventPriceFormatter.formatWithCurrency(previousTotalPrice, FXEvent.getEvent()) + " - Already paid: " + EventPriceFormatter.formatWithCurrency(deposit, FXEvent.getEvent()) + " - Balance: " + EventPriceFormatter.formatWithCurrency(balance, FXEvent.getEvent()));
            previousTotalHBox.getChildren().addAll(previousTotalLabel, spacerPreviousTotal, previousTotalPriceLabel);
            HBox sepHBox = new HBox();
            sepHBox.setPadding(new Insets(0, 0, 30, 0));
            pastScheduledItemVBox.getChildren().add(previousTotalPriceLabel);
            mainVbox.getChildren().addAll(headerHBox,pastScheduledItemVBox);
        } else {
            previousTotalPrice = 0;
        }
        HBox sepHBox = new HBox();
        sepHBox.setPadding(new Insets(0, 0, 30, 0));
        scheduledItemVBox.getChildren().add(sepHBox);


        //SECOND PART: WHAT WE BOOK AT THIS STEP
        HBox header2HBox = new HBox();
        Region spacer2InHeader1 = new Region();
        spacer2InHeader1.setPrefWidth(80);
        Label summary2Label = I18nControls.bindI18nProperties(new Label(), "Summary");
        Region spacer2InHeader2 = new Region();
        HBox.setHgrow(spacer2InHeader2, Priority.ALWAYS);
        Label price2Label = I18nControls.bindI18nProperties(new Label(), "Price");
        price2Label.setPrefWidth(70);
        //The actionLabel is used only because we need to put a graphic element in the right part of the borderpane,
        //so it's balanced with the content that is shown bellow
        Label action2Label = new Label();
        action2Label.setPrefWidth(40);
        header2HBox.getChildren().addAll(spacer2InHeader1, summary2Label, spacer2InHeader2, price2Label, action2Label);
        // headerHBox.setMaxWidth(400);
        header2HBox.setPadding(new Insets(0, 0, 5, 0));

        scheduledItemVBox.setAlignment(Pos.CENTER);
        scheduledItemVBox.setPadding(new Insets(0, 0, 40, 0));
        scheduledItemVBox.getChildren().clear();
        bookEventData.getDocumentAggregate().getNewAttendancesStream().forEach(a -> {
            HBox currentScheduledItemHBox = new HBox();
            //currentScheduledItemHBox.setMaxWidth(500); //300+55+45
            ScheduledItem scheduledItem = a.getScheduledItem();
            LocalDate date = scheduledItem.getDate();
            Item item = scheduledItem.getItem();
            Site site = scheduledItem.getSite();
            String dateFormatted = I18n.getI18nText("DateFormatted", I18n.getI18nText(date.getMonth().name()), date.getDayOfMonth());
            Label name = new Label(item.getName() + " - " + dateFormatted);
            Region spacer1 = new Region();
            Region spacer2 = new Region();
            spacer1.setPrefWidth(80);
            Label price = new Label(EventPriceFormatter.formatWithCurrency(bookEventData.getPolicyAggregate().getSiteItemRates(site, item).get(0).getPrice(), FXEvent.getEvent()));
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
                controller.getRecurringEventSchedule().getSelectedDates().remove(date);
                scheduledItemVBox.getChildren().remove(currentScheduledItemHBox);
                int newTotalPrice = bookEventData.getPriceCalculatorForCurrentOption().calculateTotalPrice();
                int balanc = newTotalPrice - bookEventData.getDocumentAggregate().getDeposit();
                bookEventData.setBalanceProperty(balanc);
                totalPriceLabel.setText(EventPriceFormatter.formatWithCurrency(newTotalPrice, FXEvent.getEvent()));
            });
            currentScheduledItemHBox.getChildren().addAll(spacer1, name, spacer2, price, trashOption);
            scheduledItemVBox.getChildren().add(currentScheduledItemHBox);
            //Now we calculate the price and update the graphic related to the price
        });
        // TODO: We display
        // - in the first past the total price for past option, the deposit and what's remain to be paid
        // - in the second part, what's remain to be paid.
        // - the pay button is the total of those twp
        // + in the previous screen, if there are already booked option, we display a different message,
        // and if the balance is not null, we allow to pay with no need to select another date.

        HBox totalHBox = new HBox();
        totalHBox.getStyleClass().add("line-total");
        //totalHBox.setMaxWidth(MAX_WIDTH);
        Label totalLabel = I18nControls.bindI18nProperties(new Label(), "Total");
        Region spacerTotal = new Region();
        totalLabel.setPadding(new Insets(5, 0, 5, 50));
        HBox.setHgrow(spacerTotal, Priority.ALWAYS);
        totalPriceLabel.setPadding(new Insets(5, 75, 5, 0));
        int totalPrice = bookEventData.getPriceCalculatorForCurrentOption().calculateTotalPrice() - previousTotalPrice;
        int deposit = bookEventData.getDocumentAggregate().getDeposit();
        balance = bookEventData.getBalance() + totalPrice-deposit;
        bookEventData.setBalanceProperty(balance);
        totalPriceLabel.setText(EventPriceFormatter.formatWithCurrency(totalPrice, FXEvent.getEvent())  + " | Total Balance (previous and current booking date) : " + EventPriceFormatter.formatWithCurrency(balance, FXEvent.getEvent()));
        totalHBox.getChildren().addAll(totalLabel, spacerTotal, totalPriceLabel);

        HBox sep2HBox = new HBox();
        sep2HBox.setPadding(new Insets(0, 0, 30, 0));
        scheduledItemVBox.getChildren().add(sep2HBox);

        scheduledItemVBox.getChildren().add(totalHBox);
        mainVbox.getChildren().addAll(header2HBox, scheduledItemVBox);

        HBox separatorHBox = new HBox();
        separatorHBox.setPadding(new Insets(0, 0, 50, 0));
        mainVbox.getChildren().add(separatorHBox);
        Button payButton = I18nControls.bindI18nProperties(new Button(), "Pay",bookEventData.getFormattedBalanceProperty());
        //We manage the property of the button in css
        payButton.setGraphicTextGap(30);
        payButton.getStyleClass().addAll("event-button", "success-button");
        payButton.setMaxWidth(150);

        payButton.setOnAction(event -> {
            Person userPerson = FXUserPerson.getUserPerson();
            if (userPerson == null) { // Means that the user is not logged in, or logged in via SSO but without an account in Modality
                WindowHistory.getProvider().push(CheckoutAccountRouting.getPath());
                return;
            }
            turnOnButtonWaitMode(payButton);
            bookEventData.getCurrentBooking().submitChanges("Booked Online")
                    .onFailure(result -> Platform.runLater(() -> {
                            controller.displayErrorMessage("ErrorWhileInsertingBooking");
                            Console.log(result);
                    }))
                    .onSuccess(result -> Platform.runLater(() -> {
                        bookEventData.setBookingReference(result.getDocumentRef());
                        Object documentPrimaryKey = result.getDocumentPrimaryKey();
                        int priceToPay = bookEventData.getPriceCalculatorForCurrentOption().calculateBalanceToPay();
                        PaymentService.initiatePayment(
                                        ClientPaymentUtil.createInitiatePaymentArgument(bookEventData.getBalance(), documentPrimaryKey)
                                )
                                .onFailure(paymentResult -> Platform.runLater(() -> {
                                    controller.displayErrorMessage("ErrorWhileInitiatingPayment");
                                    Console.log(result);
                                }))
                                .onSuccess(paymentResult -> Platform.runLater(() -> {
                                    turnOffButtonWaitMode(payButton, "Pay");
                                    WebPaymentForm webPaymentForm = new WebPaymentForm(paymentResult, userPerson);
                                    controller.getStep4PaymentSlide().setWebPaymentForm(webPaymentForm);
                                    controller.displayNextSlide();
                                }));
                    }));
        });
        mainVbox.getChildren().add(payButton);

        // Adding the container that will display the CheckoutAccountActivity (and eventually the login page before)
        MonoPane accountActivityContainer = new MonoPane();
        accountActivityContainer.contentProperty().bind(checkoutAccountMountNodeProperty); // managed by sub-router
        VBox.setMargin(accountActivityContainer, new Insets(50, 0, 50, 0)); // Some good margins before and after
        mainVbox.getChildren().add(accountActivityContainer);
    }
}
