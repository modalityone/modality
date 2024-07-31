package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.frontoffice.activities.booking.WorkingBooking;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.RecurringEventSchedule;
import one.modality.event.frontoffice.activities.booking.process.event.WorkingBookingProperties;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class Step1BookDatesSlide extends StepSlide {

    private final HtmlText eventDescription = new HtmlText();
    private final RecurringEventSchedule recurringEventSchedule = new RecurringEventSchedule();
    private final BooleanProperty noDatesBookedProperty = new SimpleBooleanProperty();

    Step1BookDatesSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
    }

    @Override
    void buildSlideUi() {
        getBookEventActivity().bindI18nEventExpression(eventDescription, "description");
        VBox.setMargin(eventDescription, new Insets(20,0,0,0));
        eventDescription.fontProperty().bind(getBookEventActivity().mediumFontProperty());
        eventDescription.getStyleClass().add("event-description-text");

        Text scheduleText = I18n.bindI18nProperties(new Text(),"Schedule");
        Bootstrap.textPrimary(Bootstrap.h4(scheduleText));
        VBox.setMargin(scheduleText, new Insets(20,0,10,0));

        Text selectTheCourseText = I18n.bindI18nProperties(new Text(),"SelectTheEvent");
        VBox.setMargin(selectTheCourseText, new Insets(0, 0, 5, 0));

        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();

        List<LocalDate> unselectableDate = new ArrayList<>();
        List<LocalDate> alreadyBookedDate = new ArrayList<>();
        workingBookingProperties.getScheduledItemsOnEvent().forEach(si-> {
            LocalDate localDate = si.getDate();
            if (workingBooking.getLastestDocumentAggregate().getAttendancesStream()
                    .map(Attendance::getScheduledItem)
                    .map(ScheduledItem::getDate)
                    .anyMatch(date -> date.equals(localDate))) {
                // Here there is already a date booked in this booking
                unselectableDate.add(localDate);
                alreadyBookedDate.add(localDate);
            } else if (workingBookingProperties.getScheduledItemsAlreadyBooked().stream()
                    .map(ScheduledItem::getDate)
                    .anyMatch(date -> date.equals(localDate))) {
                //Here there is already a date booked in this another booking
                unselectableDate.add(localDate);
                alreadyBookedDate.add(localDate);
            } else if (localDate.isBefore(LocalDate.now())) {
                //here the date is past
                unselectableDate.add(localDate);
            }
        });

        //If the date in unselectable for any reason listed above, we do nothing when we click on the date
        recurringEventSchedule.setOnDateClicked(localDate -> {
            if(unselectableDate.contains(localDate)) {
                return;
            }
            recurringEventSchedule.processDateSelected(localDate);
            workingBookingProperties.updateAll();
        });

        //If the date in unselectable for any reason listed above, we select another css property for this element
        recurringEventSchedule.setUnselectedDateCssGetter((localDate -> {
            if (alreadyBookedDate.contains(localDate)) {
                return "date-already-booked";
            }
            if (unselectableDate.contains(localDate)) {
                return "date-non-selectable";
            }
            return recurringEventSchedule.getUnselectedDateCssClass();
        }));

        /* UX Designer asked to remove this
        recurringEventSchedule.setComputeNodeForExistingBookedDateFunction((localDate -> {
            if(alreadyBookedDate.contains(localDate)) {
                Label toReturn = new Label("*");
                //TODO add a legend and put in css file (waiting UX Designer)
                if(bookEventData.isDepositOnPreviousBookingComplete())
                    toReturn.getStyleClass().setAll(Bootstrap.SUCCESS_TEXT);
                else
                    toReturn.getStyleClass().setAll(Bootstrap.DANGER_TEXT);
                return toReturn;
            }
            return null;
        }));*/

        recurringEventSchedule.setScheduledItems(workingBookingProperties.getScheduledItemsOnEvent());
        noDatesBookedProperty.bind(ObservableLists.isEmpty(recurringEventSchedule.getSelectedDates()));

        Pane schedule = recurringEventSchedule.buildUi();
        VBox.setMargin(schedule, new Insets(30, 0, 30, 0));

        // We create a list of local date, that will contain all the selectable date, ie the one that are not in the past, and not already booked
        List<LocalDate> selectableDates = workingBookingProperties.getScheduledItemsOnEvent().stream().map(ScheduledItem::getDate).collect(Collectors.toList());
        selectableDates.removeAll(unselectableDate);

        Hyperlink selectAllClassesHyperlink = I18nControls.bindI18nTextProperty(new Hyperlink(), "SelectAllClasses");
        Bootstrap.textPrimary(Bootstrap.h4(selectAllClassesHyperlink));
        selectAllClassesHyperlink.setAlignment(Pos.CENTER);
        selectAllClassesHyperlink.setOnAction((event -> recurringEventSchedule.selectDates(selectableDates)));
        Text priceText = new Text(I18n.getI18nText("PricePerClass", EventPriceFormatter.formatWithCurrency(workingBookingProperties.getRate(), FXEvent.getEvent())));
        priceText.getStyleClass().add("subtitle-grey");
        VBox.setMargin(priceText, new Insets(20, 0, 20, 0));

        Button checkoutButton = Bootstrap.largeSuccessButton(I18nControls.bindI18nProperties(new Button(), "ProceedCheckout"));
        checkoutButton.setMaxWidth(300);
        checkoutButton.setOnAction((event -> {
            workingBooking.cancelChanges();
            workingBooking.bookScheduledItems(recurringEventSchedule.getSelectedScheduledItem());
            displayCheckoutSlide();
        }));

        checkoutButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> workingBookingProperties.getBalance() <= 0 && noDatesBookedProperty.get(),
                workingBookingProperties.balanceProperty(), noDatesBookedProperty)
        );

        //mainVbox.setPadding(new Insets(30, 0, 20, 0));
        mainVbox.getChildren().setAll(
                eventDescription,
                scheduleText,
                selectTheCourseText,
                schedule,
                selectAllClassesHyperlink,
                priceText,
                checkoutButton
        );
    }

    void reset() {
        recurringEventSchedule.getSelectedDates().clear();
        super.reset();
    }

    RecurringEventSchedule getRecurringEventSchedule() {
        return recurringEventSchedule;
    }

}
