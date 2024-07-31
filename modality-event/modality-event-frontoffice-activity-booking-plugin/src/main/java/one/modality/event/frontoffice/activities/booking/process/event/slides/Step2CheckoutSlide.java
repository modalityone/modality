package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.time.Times;
import dev.webfx.platform.windowhistory.WindowHistory;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Person;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.payment.PaymentService;
import one.modality.ecommerce.payment.client.ClientPaymentUtil;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.frontoffice.activities.booking.WorkingBooking;
import one.modality.event.frontoffice.activities.booking.process.account.CheckoutAccountRouting;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.WorkingBookingProperties;

import java.time.LocalDate;
import java.util.stream.Stream;

final class Step2CheckoutSlide extends StepSlide {

    private static final double MAX_SLIDE_WIDTH = 800;

    private final GridPane summaryGridPane = new GridPane();
    // Node property that will be managed by the sub-router to mount the CheckoutAccountActivity (when routed)
    private final ObjectProperty<Node> checkoutAccountMountNodeProperty = new SimpleObjectProperty<>();
    private final Button submitButton = Bootstrap.largeSuccessButton(new Button());

    public Step2CheckoutSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
        summaryGridPane.setMaxWidth(10000);
    }

    // Exposing accountMountNodeProperty for the sub-routing binding (done in SlideController)
    ObjectProperty<Node> accountMountNodeProperty() {
        return checkoutAccountMountNodeProperty;
    }

    @Override
    void buildSlideUi() {
        mainVbox.getChildren().clear();
        mainVbox.setMaxWidth(MAX_SLIDE_WIDTH);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.SOMETIMES);
        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(15);
        summaryGridPane.getColumnConstraints().setAll(c1, c2, c3);
        summaryGridPane.setVgap(5);
        summaryGridPane.setPadding(new Insets(20, 0, 50, 0));
        rebuildSummaryGridPane();
        mainVbox.getChildren().add(summaryGridPane);

        I18nControls.bindI18nProperties(submitButton, "Submit",
                getWorkingBookingProperties().formattedBalanceProperty());
        submitButton.setOnAction(event -> submit());
        mainVbox.getChildren().add(submitButton);

        // Adding the container that will display the CheckoutAccountActivity (and eventually the login page before)
        MonoPane accountActivityContainer = new MonoPane();
        accountActivityContainer.contentProperty().bind(checkoutAccountMountNodeProperty); // managed by sub-router
        VBox.setMargin(accountActivityContainer, new Insets(50, 0, 50, 0)); // Some good margins before and after
        mainVbox.getChildren().add(accountActivityContainer);
    }

    private void rebuildSummaryGridPane() {
        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        DocumentAggregate documentAggregate = workingBookingProperties.getDocumentAggregate();

        summaryGridPane.getChildren().clear();
        addRow( Bootstrap.textPrimary(Bootstrap.strong(I18nControls.bindI18nProperties(new Label(), "Summary"))),
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
            Label name = new Label(item.getName() + " - " + dateFormatted + (existing? " (already booked)" : ""));
            Label price = new Label(EventPriceFormatter.formatWithCurrency(workingBookingProperties.getRate(), workingBooking.getEvent()));

            Hyperlink trashOption = new Hyperlink();
            SVGPath svgTrash = SvgIcons.createTrashSVGPath();
            svgTrash.setFill(Color.RED);
            trashOption.setGraphic(svgTrash);
            trashOption.setOnAction(event -> {
                workingBooking.removeAttendance(a);
                if (!existing) {
                    getRecurringEventSchedule().getSelectedDates().remove(date);
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
        GridPane.setMargin(node1, new Insets(0, 0, rowIndex == 0 ? 20 : 0,20));
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

    private void submit() {
        Person userPerson = FXUserPerson.getUserPerson();
        if (userPerson == null) { // Means that the user is not logged in, or logged in via SSO but without an account in Modality
            WindowHistory.getProvider().push(CheckoutAccountRouting.getPath());
            return;
        }
        turnOnButtonWaitMode(submitButton);

        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();

        // Three cases here:
        // 1) we pay an old balance with no new option, the currentBooking has no changes
        if (!workingBooking.hasChanges()) {
            initiatePayment(workingBooking.getDocumentPrimaryKey());
        } else {
            // 2) the currentBooking has new option
            // We look at the changes to fill the history
            StringBuilder history = new StringBuilder();
            boolean first = true;
            if (workingBooking.getAttendanceAdded().length != 0) {
                history.append("Booked ");
                for (Attendance attendance : workingBooking.getAttendanceAdded()) {
                    if (!first)
                        history.append(", ");
                    // We get the date throw the scheduledItem associated to the attendance, because the
                    // attendance date is not loaded from the database if it comes from a previous booking
                    history.append(Times.format(attendance.getScheduledItem().getDate(), "dd/MM"));
                    first = false;
                }
            }

            if (workingBooking.getAttendanceRemoved().length != 0) {
                history.append(first ? "Removed " : " & removed ");
                first = true;
                for (Attendance attendance : workingBooking.getAttendanceRemoved()) {
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
                            initiatePayment(result.getDocumentPrimaryKey());
                        } else {
                            displayThankYouSlide();
                        }
                    }));
        }
    }

    private void initiatePayment(Object documentPrimaryKey) {
        Person userPerson = FXUserPerson.getUserPerson();
        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        PaymentService.initiatePayment(
                        ClientPaymentUtil.createInitiatePaymentArgument(workingBookingProperties.getBalance(), documentPrimaryKey)
                )
                .onFailure(paymentResult -> UiScheduler.runInUiThread(() -> {
                    displayErrorMessage("ErrorWhileInitiatingPayment");
                    Console.log(paymentResult);
                }))
                .onSuccess(paymentResult -> UiScheduler.runInUiThread(() -> {
                    turnOffButtonWaitMode(submitButton, "Submit");
                    WebPaymentForm webPaymentForm = new WebPaymentForm(paymentResult, userPerson);
                    displayPaymentSlide(webPaymentForm);
                }));
    }

}
