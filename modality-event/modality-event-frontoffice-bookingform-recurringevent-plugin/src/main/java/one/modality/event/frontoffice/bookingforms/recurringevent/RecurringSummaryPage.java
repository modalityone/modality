package one.modality.event.frontoffice.bookingforms.recurringevent;

import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.platform.util.Booleans;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.client.i18n.EcommerceI18nKeys;
import one.modality.ecommerce.client.workingbooking.PriceCalculator;
import one.modality.ecommerce.client.workingbooking.WorkingBooking;
import one.modality.ecommerce.client.workingbooking.WorkingBookingProperties;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.event.frontoffice.activities.booking.BookingI18nKeys;
import one.modality.event.frontoffice.activities.booking.process.event.bookingform.multipages.BookingFormPage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bruno Salmon
 */
final class RecurringSummaryPage implements BookingFormPage {

    private final RecurringEventBookingForm bookingForm;
    private final GridPane summaryGridPane = new GridPane();
    private final CheckBox facilityFeeCheckBox = I18nControls.newCheckBox(BookingI18nKeys.FacilityFee);
    private final MonoPane embeddedLoginContainer = new MonoPane();
    private final VBox container = new VBox(50,
        summaryGridPane,
        facilityFeeCheckBox,
        embeddedLoginContainer
    );

    public RecurringSummaryPage(RecurringEventBookingForm bookingForm) {
        this.bookingForm = bookingForm;
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.SOMETIMES);
        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(15);
        summaryGridPane.getColumnConstraints().setAll(c1, c2, c3);
        summaryGridPane.setVgap(5);
        summaryGridPane.setPadding(new Insets(20, 0, 0, 0));
        summaryGridPane.setMaxWidth(10000); // Workaround for a bug in GridPane layout OpenJFX implementation. Indeed,
        // the default value Double.MAX is causing an infinite loop with TransitionPane
        container.setAlignment(Pos.TOP_CENTER);
    }

    @Override
    public Object getTitleI18nKey() {
        return null;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public MonoPane getEmbeddedLoginContainer() {
        return embeddedLoginContainer;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        // Facility fee checkbox for events with facility fees rates
        boolean hasFacilityFees = workingBookingProperties.getPolicyAggregate().hasFacilityFees();
        Layouts.setManagedAndVisibleProperties(facilityFeeCheckBox, hasFacilityFees);
        Document document = workingBooking.getLastestDocumentAggregate().getDocument();
        facilityFeeCheckBox.setSelected(Booleans.isTrue(document.isPersonFacilityFee()));
        facilityFeeCheckBox.setOnAction(e -> {
            workingBooking.applyFacilityFeeRate(facilityFeeCheckBox.isSelected());
            rebuildSummaryGridPane();
        });
        rebuildSummaryGridPane();
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return bookingForm.getActivity().getWorkingBookingProperties().submittableProperty();
    }

    private void rebuildSummaryGridPane() {
        WorkingBookingProperties workingBookingProperties = bookingForm.getActivity().getWorkingBookingProperties();
        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        Event event = bookingForm.getEvent();

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
            noDiscountTotalPrice += addAttendanceRows(true);
            addExistingTotalLine();
        }

        boolean isRecurring = Booleans.isTrue(event.isRecurring());
        if (isRecurring || workingBooking.isNewBooking()) {
            // SECOND PART: WHAT WE BOOK AT THIS STEP - we add this only if it's a new booking or if it's a GP (recurringEvent).
            noDiscountTotalPrice += addAttendanceRows(false);
        }
        // THIRD PART: DISCOUNT, IF ANY
        int total = workingBookingProperties.getTotal();
        if (total < noDiscountTotalPrice) {
            Label price = new Label(EventPriceFormatter.formatWithCurrency(total - noDiscountTotalPrice, event));
            addRow(
                I18nControls.newLabel(EcommerceI18nKeys.Discount),
                price,
                new Label()
            );
        } else { // Invisible row - to always keep the same number of rows and prevent a vertical shift when (un)ticking the facility fee
            addRow(new Label(" "), new Label(), new Label()); // Note: the " " is for the web version (otherwise its height is 0 as opposed to OpenJFX) TODO: correct this in webfx
        }

        addNewTotalLine();
    }

    private int addAttendanceRows(boolean existing) {
        WorkingBookingProperties workingBookingProperties = bookingForm.getActivity().getWorkingBookingProperties();
        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        DocumentAggregate documentAggregate = workingBooking.getLastestDocumentAggregate();
        Stream<Attendance> attendancesStream = existing ? documentAggregate.getExistingAttendancesStream() : documentAggregate.getNewAttendancesStream();
        Event event = bookingForm.getEvent();

        int[] totalPrice = {0};
        if (bookingForm.getSettings().partialEventAllowed()) { // Ex: GP
            attendancesStream.forEach(a -> {
                ScheduledItem scheduledItem = a.getScheduledItem();
                LocalDate date = scheduledItem.getDate();
                Item item = scheduledItem.getItem();
                Label scheduledItemLabel = I18nEntities.newExpressionLabel(item,
                    "i18n(this) + ' - {0}' " + (existing ? " + ' ([" + BookingI18nKeys.alreadyBooked + "])'" : ""),
                    LocalizedTime.formatMonthDayProperty(date, FrontOfficeTimeFormats.BOOKING_CHECKOUT_DATE_FORMAT));
                int dailyRatePrice = workingBookingProperties.getDailyRatePrice();
                totalPrice[0] += dailyRatePrice;
                Label price = new Label(EventPriceFormatter.formatWithCurrency(dailyRatePrice, event));

                Hyperlink trashOption = new Hyperlink();
                trashOption.setGraphic(SvgIcons.setSVGPathFill(SvgIcons.createTrashSVGPath(), Color.RED));
                trashOption.setOnAction(e -> {
                    workingBooking.removeAttendance(a);
                    if (!existing) {
                        bookingForm.getRecurringEventSchedule().removeClickedDate(date);
                    }
                    rebuildSummaryGridPane();
                });

                if (existing) {
                    trashOption.disableProperty().bind(workingBookingProperties.previousBalanceProperty().map(previousBalance ->
                        previousBalance.intValue() <= 0 || date.isBefore(LocalDate.now())
                    ));
                }

                addRow(scheduledItemLabel, price, trashOption);
                GridPane.setHalignment(trashOption, HPos.CENTER);
            });
        } else { // Ex: STTP
            Label name = new Label(event.getName() + (existing ? " - (already booked)" : ""));
            List<Attendance> attendances = attendancesStream.collect(Collectors.toList());
            int price = new PriceCalculator(documentAggregate).calculateLinePrice(attendances);
            Label priceLabel = new Label(EventPriceFormatter.formatWithCurrency(price, event));
            addRow(name, priceLabel, null);
            totalPrice[0] += price;
        }

        return totalPrice[0];
    }

    private void addExistingTotalLine() {
        WorkingBookingProperties workingBookingProperties = bookingForm.getActivity().getWorkingBookingProperties();
        addTotalLine(
            BookingI18nKeys.TotalOnPreviousBooking, workingBookingProperties.formattedPreviousTotalProperty(),
            EcommerceI18nKeys.Deposit, workingBookingProperties.formattedDepositProperty(),
            BookingI18nKeys.BalanceOnPreviousBooking, workingBookingProperties.formattedPreviousBalanceProperty()
        );
    }

    private void addNewTotalLine() {
        WorkingBookingProperties workingBookingProperties = bookingForm.getActivity().getWorkingBookingProperties();
        addTotalLine(
            EcommerceI18nKeys.TotalPrice, workingBookingProperties.formattedTotalProperty(),
            EcommerceI18nKeys.Deposit, workingBookingProperties.formattedDepositProperty(),
            EcommerceI18nKeys.Balance, workingBookingProperties.formattedBalanceProperty()
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

    private void addTotalLine(Object col1I18n, Object col1Amount, Object col2I18n, Object col2Amount, Object col3I18n, Object col3Amount) {
        Label col1Label = I18nControls.newLabel(I18nKeys.embedInString("[0] {0}", I18nKeys.appendColons(col1I18n)), col1Amount);
        Label col2Label = I18nControls.newLabel(I18nKeys.embedInString("[0] {0}", I18nKeys.appendColons(col2I18n)), col2Amount);
        Label col3Label = I18nControls.newLabel(I18nKeys.embedInString("[0] {0}", I18nKeys.appendColons(col3I18n)), col3Amount);
        ColumnsPane totalPane = new ColumnsPane(col1Label, col2Label, col3Label);
        totalPane.setMaxWidth(Double.MAX_VALUE);
        totalPane.setPadding(new Insets(7));
        totalPane.getStyleClass().add("line-total");
        GridPane.setMargin(totalPane, new Insets(15, 0, 0, 0));
        int rowIndex = summaryGridPane.getRowCount();
        summaryGridPane.add(totalPane, 0, rowIndex, 3, 1);
    }

}
