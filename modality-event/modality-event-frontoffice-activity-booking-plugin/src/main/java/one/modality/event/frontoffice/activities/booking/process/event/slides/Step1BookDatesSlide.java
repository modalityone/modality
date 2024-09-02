package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.panes.ScaleMode;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.ui.controls.button.ButtonFactory;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.RecurringEventSchedule;
import one.modality.event.frontoffice.activities.booking.process.event.WorkingBookingProperties;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

final class Step1BookDatesSlide extends StepSlide {

    private final HtmlText eventDescription = new HtmlText();
    private final RecurringEventSchedule recurringEventSchedule = new RecurringEventSchedule();
    private final BooleanProperty noDatesBookedProperty = new SimpleBooleanProperty();
    private final Hyperlink selectAllClassesHyperlink = I18nControls.bindI18nTextProperty(new Hyperlink(), "SelectAllClasses");

    Step1BookDatesSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
        noDatesBookedProperty.bind(ObservableLists.isEmpty(recurringEventSchedule.getSelectedDates()));
    }

    @Override
    void buildSlideUi() {
        bindI18nEventExpression(eventDescription, "description");
        VBox.setMargin(eventDescription, new Insets(20, 0, 0, 0));
        eventDescription.fontProperty().bind(getBookEventActivity().mediumFontProperty());
        eventDescription.getStyleClass().add("event-description-text");
        eventDescription.setMinWidth(0);

        Button personToBookButton = createPersonToBookButton();
        ScalePane personToBookScalePane = new ScalePane(ScaleMode.FIT_WIDTH, personToBookButton);
        personToBookScalePane.setCanGrow(false);
        personToBookScalePane.setMaxWidth(Double.MAX_VALUE);
        personToBookScalePane.managedProperty().bind(personToBookButton.managedProperty());
        VBox.setMargin(personToBookScalePane, new Insets(30, 0, 20, 0));

        Text scheduleText = I18n.bindI18nProperties(new Text(), "Schedule");
        Bootstrap.textPrimary(Bootstrap.h4(scheduleText));
        VBox.setMargin(scheduleText, new Insets(20, 0, 10, 0));

        Label selectTheCourseText = I18nControls.bindI18nProperties(new Label(), "SelectTheEvent");
        selectTheCourseText.setTextAlignment(TextAlignment.CENTER);
        selectTheCourseText.setWrapText(true);
        VBox.setMargin(selectTheCourseText, new Insets(0, 0, 5, 0));

        Pane schedule = recurringEventSchedule.buildUi();
        VBox.setMargin(schedule, new Insets(30, 0, 30, 0));

        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();

        Bootstrap.textPrimary(Bootstrap.h4(selectAllClassesHyperlink));
        selectAllClassesHyperlink.setAlignment(Pos.CENTER);

        Text priceText = new Text(I18n.getI18nText("PricePerClass", EventPriceFormatter.formatWithCurrency(workingBookingProperties.getDailyRatePrice(), getEvent())));
        priceText.getStyleClass().add("subtitle-grey");
        VBox.setMargin(priceText, new Insets(20, 0, 0, 0));

        Button checkoutButton = Bootstrap.largeSuccessButton(I18nControls.bindI18nProperties(new Button(), "ProceedCheckout"));
        checkoutButton.setMinWidth(300);
        ButtonFactory.resetDefaultButton(checkoutButton);
        ScalePane checkoutScalePane = new ScalePane(ScaleMode.FIT_WIDTH, checkoutButton);
        checkoutScalePane.setCanGrow(false);
        checkoutScalePane.setMaxWidth(Double.MAX_VALUE);

        checkoutButton.disableProperty().bind(Bindings.createBooleanBinding(
            () -> workingBookingProperties.getBalance() <= 0 && noDatesBookedProperty.get(),
            workingBookingProperties.balanceProperty(), noDatesBookedProperty)
        );
        checkoutButton.setOnAction((event -> displayCheckoutSlide()));
        VBox.setMargin(checkoutScalePane, new Insets(20, 0, 20, 0)); // in addition to VBox bottom margin 80

        mainVbox.getChildren().setAll(
            eventDescription,
            personToBookScalePane,
            scheduleText,
            selectTheCourseText,
            schedule,
            selectAllClassesHyperlink,
            priceText,
            checkoutScalePane
        );

        // Adding a message if there is a discount for the whole series
        int allClassesPrice = workingBookingProperties.getWholeEventPrice();
        int allClassesNoDiscountPrice = workingBookingProperties.getWholeEventNoDiscountPrice();
        if (allClassesPrice < allClassesNoDiscountPrice) {
            Text allClassesText = new Text(I18n.getI18nText("AllClasses:"));
            allClassesText.getStyleClass().add("subtitle-grey");
            Text noDiscountPriceText = new Text(EventPriceFormatter.formatWithCurrency(allClassesNoDiscountPrice, getEvent()));
            noDiscountPriceText.setStrikethrough(true);
            noDiscountPriceText.getStyleClass().add("subtitle-grey");
            Text discountPriceText = new Text(EventPriceFormatter.formatWithCurrency(allClassesPrice, getEvent()));
            discountPriceText.getStyleClass().add("subtitle-grey");
            HBox hBox = new HBox(5, allClassesText, noDiscountPriceText, discountPriceText);
            hBox.setAlignment(Pos.CENTER);
            VBox.setMargin(hBox, new Insets(5, 0, 0, 0));
            mainVbox.getChildren().add(7, hBox);
        }
    }

    void onWorkingBookingLoaded() {
        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();

        List<ScheduledItem> scheduledItemsOnEvent = workingBookingProperties.getScheduledItemsOnEvent();

        // Computing non-selectable and already booked dates for the purpose of styling the event schedule
        List<LocalDate> nonSelectableDate = new ArrayList<>();
        List<LocalDate> alreadyBookedDate = new ArrayList<>();
        scheduledItemsOnEvent.forEach(si -> {
            LocalDate localDate = si.getDate();
            if (workingBookingProperties.getScheduledItemsAlreadyBooked().stream()
                .map(ScheduledItem::getDate)
                .anyMatch(date -> date.equals(localDate))) {
                //Here there is already a date booked in this another booking
                nonSelectableDate.add(localDate);
                alreadyBookedDate.add(localDate);
            } else if (localDate.isBefore(LocalDate.now())) {
                //here the date is past
                nonSelectableDate.add(localDate);
            }
        });

        // If the date in not selectable for any reason listed above, we do nothing when we click on the date
        recurringEventSchedule.setOnDateClicked(localDate -> {
            if (nonSelectableDate.contains(localDate)) {
                return;
            }
            recurringEventSchedule.processClickedDate(localDate);
            workingBookingProperties.updateAll();
        });

        // If the date in not selectable for any reason listed above, we select another css property for this element
        recurringEventSchedule.setUnselectedDateCssGetter((localDate -> {
            if (alreadyBookedDate.contains(localDate)) {
                return "date-already-booked";
            }
            if (nonSelectableDate.contains(localDate)) {
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

        // Synchronizing the event schedule from the working booking (will select the dates newly added in the working booking)
        getBookEventActivity().syncEventScheduleFromWorkingBooking();

        // Arming the "Select all classes" hyperlink. We create a list of dates that will contain all the selectable
        // dates = the ones that are not in the past, and not already booked
        List<LocalDate> allSelectableDates = Collections.map(scheduledItemsOnEvent, ScheduledItem::getDate);
        allSelectableDates.removeAll(nonSelectableDate);
        selectAllClassesHyperlink.setOnAction((event -> recurringEventSchedule.addClickedDates(allSelectableDates)));
    }

    RecurringEventSchedule getRecurringEventSchedule() {
        return recurringEventSchedule;
    }

}
