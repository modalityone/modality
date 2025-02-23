package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.FlipPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.windowhistory.WindowHistory;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.client.i18n.ModalityI18nKeys;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.client.i18n.EcommerceI18nKeys;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.client.workingbooking.FXPersonToBook;
import one.modality.event.client.recurringevents.RecurringEventsI18nKeys;
import one.modality.ecommerce.client.workingbooking.WorkingBooking;
import one.modality.ecommerce.client.workingbooking.WorkingBookingHistoryHelper;
import one.modality.event.frontoffice.activities.booking.BookingI18nKeys;
import one.modality.event.frontoffice.activities.booking.fx.FXGuestToBook;
import one.modality.event.frontoffice.activities.booking.process.account.CheckoutAccountRouting;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.ecommerce.client.workingbooking.WorkingBookingProperties;

import java.time.LocalDate;
import java.util.stream.Stream;

final class Step2CheckoutSlide extends StepSlide {

    private static final double MAX_SLIDE_WIDTH = 800;

    private final GridPane summaryGridPane = new GridPane();
    // Node property that will be managed by the sub-router to mount the CheckoutAccountActivity (when routed)
    private final ObjectProperty<Node> checkoutAccountMountNodeProperty = new SimpleObjectProperty<>();
    private final GuestPanel guestPanel = new GuestPanel();
    private final Button submitButton = Bootstrap.largeSuccessButton(I18nControls.newButton(ModalityI18nKeys.Submit));
    private final BooleanProperty step1PersonToBookWasShownProperty = new SimpleBooleanProperty();
    private boolean bookAsGuestAllowed = true;
    private boolean partialEventAllowed = true;

    public Step2CheckoutSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
        summaryGridPane.setMaxWidth(10000); // Workaround for a bug in GridPane layout OpenJFX implementation. Indeed,
        // the default value Double.MAX is causing infinite loop with TransitionPane
    }

    public void setBookAsGuestAllowed(boolean bookAsGuestAllowed) {
        this.bookAsGuestAllowed = bookAsGuestAllowed;
    }

    public void setPartialEventAllowed(boolean partialEventAllowed) {
        this.partialEventAllowed = partialEventAllowed;
    }

    // Exposing accountMountNodeProperty for the sub-routing binding (done in SlideController)
    ObjectProperty<Node> accountMountNodeProperty() {
        return checkoutAccountMountNodeProperty;
    }

    public void setStep1PersonToBookWasShown(boolean step1PersonToBookWasShown) {
        step1PersonToBookWasShownProperty.set(step1PersonToBookWasShown);
    }

    @Override
    public void buildSlideUi() { // Called only once
        mainVbox.setMaxWidth(MAX_SLIDE_WIDTH);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.SOMETIMES);
        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(15);
        summaryGridPane.getColumnConstraints().setAll(c1, c2, c3);
        summaryGridPane.setVgap(5);
        summaryGridPane.setPadding(new Insets(20, 0, 0, 0));
        rebuildSummaryGridPane();

        Button personToBookButton = createPersonToBookButton();
        MonoPane personToBookMonoPane = new MonoPane(personToBookButton);
        Layouts.bindManagedAndVisiblePropertiesTo(step1PersonToBookWasShownProperty.not(), personToBookMonoPane);
        VBox.setMargin(personToBookMonoPane, new Insets(20, 0, 20, 0));

        // Adding the container that will display the CheckoutAccountActivity (and eventually the login page before)
        BorderPane signInContainer = new BorderPane();
        MonoPane signInContent = new MonoPane();
        Label loginLabel = Bootstrap.textPrimary(Bootstrap.strong(I18nControls.newLabel(BookingI18nKeys.LoginBeforeBooking)));
        VBox signIntopVBox = new VBox(10, loginLabel);
        signIntopVBox.setAlignment(Pos.TOP_CENTER);
        BorderPane.setMargin(signIntopVBox, new Insets(0, 0, 20, 0));
        signInContainer.setTop(signIntopVBox);
        signInContainer.setCenter(signInContent);

        FlipPane flipPane = new FlipPane();
        flipPane.setAlignment(Pos.TOP_CENTER);
        flipPane.setFront(signInContainer);

        // TODO: make this reactive with a property (as this setting depends on the event, and this same UI is reused)
        if (bookAsGuestAllowed) {
            Hyperlink orGuestLink = Bootstrap.textPrimary(I18nControls.newHyperlink(BookingI18nKeys.OrBookAsGuest));
            orGuestLink.setOnAction(e -> {
                flipPane.flipToBack();
                guestPanel.onShowing();
            });
            signIntopVBox.getChildren().add(orGuestLink);

            flipPane.setBack(guestPanel.getContainer());
            Hyperlink orAccountLink = Bootstrap.textPrimary(I18nControls.newHyperlink(BookingI18nKeys.OrBookUsingAccount));
            orAccountLink.setOnAction(e -> {
                flipPane.flipToFront();
                guestPanel.onHiding();
            });
            guestPanel.addTopNode(orAccountLink);
            guestPanel.setOnSubmit(event -> {
                Document document = getWorkingBooking().getLastestDocumentAggregate().getDocument();
                document.setFirstName(guestPanel.getFirstName());
                document.setLastName(guestPanel.getLastName());
                document.setEmail(guestPanel.getEmail());
                document.setCountry(getEvent().getOrganization().getCountry());
                FXGuestToBook.setGuestToBook(document);
                submit();
            });
        }

        // Facility fee checkbox for events with facility fees rates
        boolean hasFacilityFees = getWorkingBooking().getPolicyAggregate().hasFacilityFees();
        CheckBox facilityFeeCheckBox = I18nControls.newCheckBox(BookingI18nKeys.FacilityFee);
        facilityFeeCheckBox.setVisible(hasFacilityFees);
        facilityFeeCheckBox.setManaged(hasFacilityFees);
        VBox.setMargin(facilityFeeCheckBox, new Insets(50, 0, 50, 0));
        Document document = getWorkingBooking().getLastestDocumentAggregate().getDocument();
        facilityFeeCheckBox.setSelected(Booleans.isTrue(document.isPersonFacilityFee()));
        facilityFeeCheckBox.setOnAction(e -> {
            getWorkingBooking().applyFacilityFeeRate(facilityFeeCheckBox.isSelected());
            rebuildSummaryGridPane();
        });

        submitButton.setOnAction(event -> submit());
        // The submit button is disabled if there is nothing to pay and no new changes on the booking
        submitButton.disableProperty().bind(FXProperties.combine(
            getWorkingBookingProperties().balanceProperty(), ObservableLists.isEmpty(getWorkingBooking().getDocumentChanges()),
            (balance, empty) -> balance.intValue() <= 0 && empty));
        VBox.setMargin(submitButton, new Insets(20, 0, 20, 0));

        mainVbox.getChildren().setAll(
            summaryGridPane,
            personToBookMonoPane,
            facilityFeeCheckBox,
            submitButton,
            flipPane
        );

        signInContent.contentProperty().bind(checkoutAccountMountNodeProperty); // managed by sub-router

        FXProperties.runNowAndOnPropertyChange(personToBook -> {
            boolean loggedIn = personToBook != null; // Means that the user is logged in with an account in Modality
            if (!loggedIn && signInContent.getContent() == null) {
                WindowHistory.getProvider().push(CheckoutAccountRouting.getPath());
            }
            flipPane.setVisible(!loggedIn);
            flipPane.setManaged(!loggedIn);
            submitButton.setVisible(loggedIn);
            submitButton.setManaged(loggedIn);
            submitButton.setDefaultButton(loggedIn);
        }, FXPersonToBook.personToBookProperty());
    }

    @Override
    void reset() {
        // super.reset(); // No, we don't rebuild the whole UI, as this can raise an exception with the mount node binding
        rebuildSummaryGridPane(); // We just need to rebuild the summary grid pane
    }

    private void rebuildSummaryGridPane() {
        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        WorkingBooking workingBooking = getWorkingBooking();
        DocumentAggregate documentAggregate = getDocumentAggregate();

        workingBookingProperties.updateAll();

        summaryGridPane.getChildren().clear();
        addRow(
            Bootstrap.textPrimary(Bootstrap.strong(I18nControls.newLabel(BookingI18nKeys.Summary))),
            Bootstrap.textPrimary(Bootstrap.strong(I18nControls.newLabel(EcommerceI18nKeys.Price))),
            new Label()
        );

        int noDiscountTotalPrice = 0;
        // FIRST PART: WHAT HAS BEEN ALREADY BOOKED FOR THIS EVENT IN THE PAST
        if (!workingBooking.isNewBooking()) {
            noDiscountTotalPrice += addAttendanceRows(documentAggregate.getExistingAttendancesStream(), true);
            addExistingTotalLine();
        }

        boolean isRecurring = getEvent().isRecurring();
        if (isRecurring || workingBooking.isNewBooking()) {
            // SECOND PART: WHAT WE BOOK AT THIS STEP - we add this only if it's a new booking or if it's a GP (recurringEvent).
            noDiscountTotalPrice += addAttendanceRows(documentAggregate.getNewAttendancesStream(), false);
        }
        // THIRD PART: DISCOUNT, IF ANY
        int total = workingBookingProperties.getTotal();
        if (total < noDiscountTotalPrice) {
            Label price = new Label(EventPriceFormatter.formatWithCurrency(total - noDiscountTotalPrice, workingBooking.getEvent()));
            addRow(
                I18nControls.newLabel(EcommerceI18nKeys.Discount),
                price,
                new Label()
            );
        }

        addNewTotalLine();
    }

    private int addAttendanceRows(Stream<Attendance> attendanceStream, boolean existing) {
        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        WorkingBooking workingBooking = getWorkingBooking();

        int[] totalPrice = {0};
        if (partialEventAllowed) { // Ex: GP
            attendanceStream.forEach(a -> {
                ScheduledItem scheduledItem = a.getScheduledItem();
                LocalDate date = scheduledItem.getDate();
                Item item = scheduledItem.getItem();
                String dateFormatted = I18n.getI18nText(RecurringEventsI18nKeys.DateFormatted1, I18n.getI18nText(date.getMonth().name()), date.getDayOfMonth());
                Label name = new Label(item.getName() + " - " + dateFormatted + (existing ? " (already booked)" : ""));
                int dailyRatePrice = workingBookingProperties.getDailyRatePrice();
                totalPrice[0] += dailyRatePrice;
                Label price = new Label(EventPriceFormatter.formatWithCurrency(dailyRatePrice, getEvent()));

                Hyperlink trashOption = new Hyperlink();
                trashOption.setGraphic(SvgIcons.setSVGPathFill(SvgIcons.createTrashSVGPath(), Color.RED));
                trashOption.setOnAction(event -> {
                    workingBooking.removeAttendance(a);
                    if (!existing) {
                        getBookableDatesUi().removeClickedDate(date);
                    }
                    rebuildSummaryGridPane();
                });

                if (existing) {
                    trashOption.disableProperty().bind(FXProperties.compute(workingBookingProperties.previousBalanceProperty(), previousBalance ->
                        previousBalance.intValue() <= 0 || date.isBefore(LocalDate.now())
                    ));
                }

                addRow(name, price, trashOption);
                GridPane.setHalignment(trashOption, HPos.CENTER);
            });
        } else { // Ex: STTP
            workingBookingProperties.updateAll();
            Label name = new Label(getEvent().getName() + (existing ? " - (already booked)" : ""));
            //TODO; calculate the price with the PriceCalculatorMethod using the list of attendance
            int price = workingBookingProperties.getTotal();
            Label priceLabel = new Label(EventPriceFormatter.formatWithCurrency(price, getEvent()));
            addRow(name, priceLabel, null);
            totalPrice[0] += price;
        }

        return totalPrice[0];
    }

    private void addExistingTotalLine() {
        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        addTotalLine(
            BookingI18nKeys.TotalOnPreviousBooking, workingBookingProperties.formattedPreviousTotalProperty(),
            BookingI18nKeys.Deposit, workingBookingProperties.formattedDepositProperty(),
            BookingI18nKeys.BalanceOnPreviousBooking, workingBookingProperties.formattedPreviousBalanceProperty()
        );
    }

    private void addNewTotalLine() {
        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        addTotalLine(
            BookingI18nKeys.TotalPrice, workingBookingProperties.formattedTotalProperty(),
            BookingI18nKeys.Deposit, workingBookingProperties.formattedDepositProperty(),
            BookingI18nKeys.GeneralBalance, workingBookingProperties.formattedBalanceProperty()
        );
    }

    private void addRow(Node node1, Node node2, Node node3) {
        int rowIndex = summaryGridPane.getRowCount();
        GridPane.setMargin(node1, new Insets(0, 0, rowIndex == 0 ? 20 : 0, 20));
        summaryGridPane.add(node1, 0, rowIndex);
        summaryGridPane.add(node2, 1, rowIndex);
        if (node3 != null)
            summaryGridPane.add(node3, 2, rowIndex);
        GridPane.setHalignment(node2, HPos.RIGHT); // price column
    }

    private void addTotalLine(String col1I18n, Object col1Amount, String col2I18n, Object col2Amount, String col3I18n, Object col3Amount) {
        Label col1Label = I18nControls.newLabel(col1I18n, col1Amount);
        Label col2Label = I18nControls.newLabel(col2I18n, col2Amount);
        Label col3Label = I18nControls.newLabel(col3I18n, col3Amount);
        ColumnsPane totalPane = new ColumnsPane(col1Label, col2Label, col3Label);
        totalPane.setMaxWidth(Double.MAX_VALUE);
        totalPane.setPadding(new Insets(7));
        totalPane.getStyleClass().add("line-total");
        GridPane.setMargin(totalPane, new Insets(15, 0, 15, 0));
        int rowIndex = summaryGridPane.getRowCount();
        summaryGridPane.add(totalPane, 0, rowIndex, 3, 1);
    }

    @Override
    void turnOnWaitMode() {
        turnOnButtonWaitMode(submitButton);
        guestPanel.turnOnButtonWaitMode();
    }

    @Override
    void turnOffWaitMode() {
        turnOffButtonWaitMode(submitButton, ModalityI18nKeys.Submit);
        guestPanel.turnOffButtonWaitMode();
    }

    private void submit() {
        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        WorkingBooking workingBooking = getWorkingBooking();
        // Three cases here:
        // 1) we pay an old balance with no new option, the currentBooking has no changes
        if (!workingBooking.hasChanges()) {
            initiateNewPaymentAndDisplayPaymentSlide(); // Will go to payment page on success
        } else {
            // 2) the currentBooking has new option
            // We look at the changes to fill the history
            WorkingBookingHistoryHelper historyHelper = new WorkingBookingHistoryHelper(workingBooking.getAttendanceAdded(), workingBooking.getAttendanceRemoved());
            turnOnWaitMode();
            workingBooking.submitChanges(historyHelper.buildHistory())
                .onFailure(result -> UiScheduler.runInUiThread(() -> {
                    turnOffWaitMode();
                    displayErrorMessage(BookingI18nKeys.ErrorWhileInsertingBooking);
                    Console.log(result);
                }))
                .onSuccess(result -> UiScheduler.runInUiThread(() -> {
                    turnOffWaitMode();
                    workingBookingProperties.setBookingReference(result.getDocumentRef());
                    if (workingBookingProperties.getBalance() > 0) {
                        initiateNewPaymentAndDisplayPaymentSlide();
                    } else {
                        displayThankYouSlide();
                    }
                }));
        }
    }
}
