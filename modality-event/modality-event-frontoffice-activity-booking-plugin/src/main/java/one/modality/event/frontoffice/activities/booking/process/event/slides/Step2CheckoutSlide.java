package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.FlipPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.time.Times;
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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.event.frontoffice.activities.booking.WorkingBooking;
import one.modality.event.frontoffice.activities.booking.fx.FXGuestToBook;
import one.modality.event.frontoffice.activities.booking.fx.FXPersonToBook;
import one.modality.event.frontoffice.activities.booking.process.account.CheckoutAccountRouting;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.WorkingBookingProperties;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

final class Step2CheckoutSlide extends StepSlide {

    private static final double MAX_SLIDE_WIDTH = 800;

    private final GridPane summaryGridPane = new GridPane();
    // Node property that will be managed by the sub-router to mount the CheckoutAccountActivity (when routed)
    private final ObjectProperty<Node> checkoutAccountMountNodeProperty = new SimpleObjectProperty<>();
    private final GuestPanel guestPanel = new GuestPanel();
    private final Button submitButton = Bootstrap.largeSuccessButton(I18nControls.bindI18nProperties(new Button(), "Submit"));
    private final BooleanProperty step1PersonToBookWasShownProperty = new SimpleBooleanProperty();

    public Step2CheckoutSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
        summaryGridPane.setMaxWidth(10000); // Workaround for a bug in GridPane layout OpenJFX implementation. Indeed,
        // the default value Double.MAX is causing infinite loop with TransitionPane
    }

    // Exposing accountMountNodeProperty for the sub-routing binding (done in SlideController)
    ObjectProperty<Node> accountMountNodeProperty() {
        return checkoutAccountMountNodeProperty;
    }

    public void setStep1PersonToBookWasShown(boolean step1PersonToBookWasShown) {
        step1PersonToBookWasShownProperty.set(step1PersonToBookWasShown);
    }

    @Override
    void buildSlideUi() { // Called only once
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
        personToBookMonoPane.visibleProperty().bind(step1PersonToBookWasShownProperty.not());
        personToBookMonoPane.managedProperty().bind(step1PersonToBookWasShownProperty.not());
        VBox.setMargin(personToBookMonoPane, new Insets(20, 0, 20, 0));

        submitButton.setOnAction(event -> submit());
        submitButton.disableProperty().bind(FXProperties.compute(getWorkingBookingProperties().balanceProperty(), balance -> balance.intValue() <= 0));
        VBox.setMargin(submitButton, new Insets(20, 0, 20, 0));

        // Adding the container that will display the CheckoutAccountActivity (and eventually the login page before)
        BorderPane signInContainer = new BorderPane();
        Label loginLabel = Bootstrap.textPrimary(Bootstrap.strong(I18nControls.bindI18nProperties(new Label(), "LoginBeforeBooking")));
        Hyperlink orGuestLink = Bootstrap.textPrimary(I18nControls.bindI18nProperties(new Hyperlink(), "OrBookAsGuest"));
        VBox signIntopVBox = new VBox(10, loginLabel, orGuestLink);
        signIntopVBox.setAlignment(Pos.TOP_CENTER);
        BorderPane.setMargin(signIntopVBox, new Insets(0, 0, 20, 0));
        signInContainer.setTop(signIntopVBox);

        guestPanel.setOnSubmit(event -> {
            Document document = getWorkingBookingProperties().getWorkingBooking().getLastestDocumentAggregate().getDocument();
            document.setFirstName(guestPanel.getFirstName());
            document.setLastName(guestPanel.getLastName());
            document.setEmail(guestPanel.getEmail());
            document.setCountry(getEvent().getOrganization().getCountry());
            FXGuestToBook.setGuestToBook(document);
            submit();
        });

        FlipPane flipPane = new FlipPane();
        flipPane.setAlignment(Pos.TOP_CENTER);
        flipPane.setFront(signInContainer);
        flipPane.setBack(guestPanel.getContainer());
        orGuestLink.setOnAction(e -> {
            flipPane.flipToBack();
            guestPanel.onShowing();
        });
        Hyperlink orAccountLink = Bootstrap.textPrimary(I18nControls.bindI18nProperties(new Hyperlink(), "OrBookUsingAccount"));
        guestPanel.addTopNode(orAccountLink);
        orAccountLink.setOnAction(e -> {
            flipPane.flipToFront();
            guestPanel.onHiding();
        });

        mainVbox.getChildren().setAll(
            summaryGridPane,
            personToBookMonoPane,
            submitButton,
            flipPane
        );

        signInContainer.centerProperty().bind(checkoutAccountMountNodeProperty); // managed by sub-router

        FXProperties.runNowAndOnPropertiesChange(() -> {
            Person personToBook = FXPersonToBook.getPersonToBook();
            boolean loggedIn = personToBook != null; // Means that the user is logged in with an account in Modality
            if (!loggedIn && signInContainer.getCenter() == null) {
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
        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        DocumentAggregate documentAggregate = workingBookingProperties.getDocumentAggregate();

        summaryGridPane.getChildren().clear();
        addRow(Bootstrap.textPrimary(Bootstrap.strong(I18nControls.bindI18nProperties(new Label(), "Summary"))),
            Bootstrap.textPrimary(Bootstrap.strong(I18nControls.bindI18nProperties(new Label(), "Price"))),
            new Label());

        //FIRST PART: WHAT HAS BEEN ALREADY BOOKED FOR THIS EVENT IN THE PAST
        if (!workingBooking.isNewBooking()) {
            addAttendanceRows(documentAggregate.getExistingAttendancesStream(), true);
        }

        //SECOND PART: WHAT WE BOOK AT THIS STEP
        addAttendanceRows(documentAggregate.getNewAttendancesStream(), false);

        workingBookingProperties.updateBalance();
    }

    private void addAttendanceRows(Stream<Attendance> attendanceStream, boolean existing) {
        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();

        attendanceStream.forEach(a -> {
            ScheduledItem scheduledItem = a.getScheduledItem();
            LocalDate date = scheduledItem.getDate();
            Item item = scheduledItem.getItem();
            String dateFormatted = I18n.getI18nText("DateFormatted", I18n.getI18nText(date.getMonth().name()), date.getDayOfMonth());
            Label name = new Label(item.getName() + " - " + dateFormatted + (existing ? " (already booked)" : ""));
            Label price = new Label(EventPriceFormatter.formatWithCurrency(workingBookingProperties.getDailyRatePrice(), getEvent()));

            Hyperlink trashOption = new Hyperlink();
            SVGPath svgTrash = SvgIcons.createTrashSVGPath();
            svgTrash.setFill(Color.RED);
            trashOption.setGraphic(svgTrash);
            trashOption.setOnAction(event -> {
                workingBooking.removeAttendance(a);
                if (!existing) {
                    getRecurringEventSchedule().removeClickedDate(date);
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

        workingBookingProperties.updateAll();
        if (existing) {
            addTotalLine(
                "TotalOnPreviousBooking", workingBookingProperties.formattedPreviousTotalProperty(),
                "Deposit", workingBookingProperties.formattedDepositProperty(),
                "BalanceOnPreviousBooking", workingBookingProperties.formattedPreviousBalanceProperty()
            );
        } else {
            addTotalLine(
                "TotalPrice", workingBookingProperties.formattedTotalProperty(),
                "Deposit", workingBookingProperties.formattedDepositProperty(),
                "GeneralBalance", workingBookingProperties.formattedBalanceProperty()
            );
        }
    }

    private void addRow(Node node1, Node node2, Node node3) {
        int rowIndex = summaryGridPane.getRowCount();
        GridPane.setMargin(node1, new Insets(0, 0, rowIndex == 0 ? 20 : 0, 20));
        summaryGridPane.add(node1, 0, rowIndex);
        summaryGridPane.add(node2, 1, rowIndex);
        summaryGridPane.add(node3, 2, rowIndex);
    }

    private void addTotalLine(String col1I18n, Object col1Amount, String col2I18n, Object col2Amount, String col3I18n, Object col3Amount) {
        Label col1Label = I18nControls.bindI18nProperties(new Label(), col1I18n, col1Amount);
        Label col2Label = I18nControls.bindI18nProperties(new Label(), col2I18n, col2Amount);
        Label col3Label = I18nControls.bindI18nProperties(new Label(), col3I18n, col3Amount);
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
        turnOffButtonWaitMode(submitButton, "Submit");
        guestPanel.turnOffButtonWaitMode();
    }

    private void submit() {
        turnOnWaitMode();

        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();

        // Three cases here:
        // 1) we pay an old balance with no new option, the currentBooking has no changes
        if (!workingBooking.hasChanges()) {
            initiateNewPaymentAndDisplayPaymentSlide(); // Will go to payment page on success
        } else {
            // 2) the currentBooking has new option
            // We look at the changes to fill the history
            StringBuilder history = new StringBuilder();
            boolean first = true;
            List<Attendance> attendanceAdded = workingBooking.getAttendanceAdded();
            if (!attendanceAdded.isEmpty()) {
                history.append("Booked ");
                for (Attendance attendance : attendanceAdded) {
                    if (!first)
                        history.append(", ");
                    // We get the date throw the scheduledItem associated to the attendance, because the
                    // attendance date is not loaded from the database if it comes from a previous booking
                    history.append(Times.format(attendance.getScheduledItem().getDate(), "dd/MM"));
                    first = false;
                }
            }

            List<Attendance> attendanceRemoved = workingBooking.getAttendanceRemoved();
            if (!attendanceRemoved.isEmpty()) {
                history.append(first ? "Removed " : " & removed ");
                first = true;
                for (Attendance attendance : attendanceRemoved) {
                    if (!first)
                        history.append(", ");
                    history.append(Times.format(attendance.getScheduledItem().getDate(), "dd/MM"));
                    first = false;
                }
            }

            workingBooking.submitChanges(history.toString())
                .onFailure(result -> UiScheduler.runInUiThread(() -> {
                    displayErrorMessage("ErrorWhileInsertingBooking");
                    Console.log(result);
                }))
                .onSuccess(result -> UiScheduler.runInUiThread(() -> {
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
