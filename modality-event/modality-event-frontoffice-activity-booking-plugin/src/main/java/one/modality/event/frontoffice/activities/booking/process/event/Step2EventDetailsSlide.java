package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.panes.FlexPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.ScaleMode;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.cloud.image.CloudImageService;
import dev.webfx.stack.cloud.image.impl.client.ClientImageService;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.event.client.event.fx.FXEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

 class Step2EventDetailsSlide extends StepSlide {

    private final RecurringEventSchedule recurringEventSchedule = new RecurringEventSchedule();
    private final HtmlText eventShortDescriptionInEventSlide = controller.bindI18nEventExpression(new HtmlText(), "shortDescription");
    private final BooleanProperty isOptionsSelectedEmptyProperty = new SimpleBooleanProperty();
    private final HtmlText eventDescription = controller.bindI18nEventExpression(new HtmlText(), "description");
    private final ImageView imageView = new ImageView();
    private final CloudImageService cloudImageService = new ClientImageService();

    public Step2EventDetailsSlide(SlideController control, BookEventData bed) {
        super(control, bed);
        controller.setStep2EventDetailsSlide(this);
    }

    public void buildUi() {
        Text title = I18n.bindI18nProperties(new Text(),"GPEvent");
        title.getStyleClass().addAll("book-event-primary-title");
        VBox.setMargin(title, new Insets(0,0,5,0));

        Text eventCentreLocationText = controller.bindI18nEventExpression(new Text(),
                "'[At] ' + coalesce(i18n(venue), i18n(organization))");

        eventShortDescriptionInEventSlide.getStyleClass().setAll("subtitle-grey");
        MonoPane shortDescriptionPane = new MonoPane(eventShortDescriptionInEventSlide);
        shortDescriptionPane.setAlignment(Pos.TOP_LEFT);
        VBox.setMargin(shortDescriptionPane, new Insets(5,0,15,0));

        ScalePane imageScalePane = new ScalePane(ScaleMode.FIT_WIDTH, imageView);
        imageScalePane.setCanGrow(false);

        VBox.setMargin(eventDescription, new Insets(20,0,0,0));
        eventDescription.getStyleClass().add("description-text");

        Text scheduleText = I18n.bindI18nProperties(new Text(),"Schedule");
        scheduleText.getStyleClass().addAll("book-event-primary-title");
        VBox.setMargin(scheduleText, new Insets(20,0,10,0));

        Text selectTheCourseText = I18n.bindI18nProperties(new Text(),"SelectTheEvent");
        VBox.setMargin(selectTheCourseText, new Insets(0, 0, 5, 0));

        recurringEventSchedule.setScheduledItems(bookEventData.getScheduledItemsOnEvent());
        // We add a listener on the date to update the BooleanProperty bound to the disable property of the checkout button
        recurringEventSchedule.getSelectedDates().addListener((ListChangeListener<LocalDate>) change -> setIsOptionsSelectedEmptyProperty(getRecurringEventSchedule().getSelectedDates().isEmpty()));
        setIsOptionsSelectedEmptyProperty(getRecurringEventSchedule().getSelectedDates().isEmpty());
        setIsOptionsSelectedEmptyProperty(bookEventData.calculateBalance()==0);
        List<LocalDate> unselectableDate = new ArrayList<>();
        List<LocalDate> alreadyBookedDate = new ArrayList<>();

        bookEventData.getScheduledItemsOnEvent().forEach(si-> {
            LocalDate localDate = si.getDate();
            if(bookEventData.getCurrentBooking().getLastestDocumentAggregate().getAttendancesStream()
                    .map(Attendance::getScheduledItem)
                    .map(ScheduledItem::getDate)
                    .anyMatch(date -> date.equals(localDate))) {
                //Here there is already a date booked in this booking
                unselectableDate.add(localDate);
                alreadyBookedDate.add(localDate);
            }
            else if(bookEventData.getScheduledItemsAlreadyBooked().stream().map(ScheduledItem::getDate).
                    anyMatch(date -> date.equals(localDate))) {
                //Here there is already a date booked in this another booking
                unselectableDate.add(localDate);
                alreadyBookedDate.add(localDate);
            }
            else if(localDate.isBefore(LocalDate.now())) {
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
        });

        //If the date in unselectable for any reason listed above, we select another css property for this element
        recurringEventSchedule.setUnselectedDateCssGetter((localDate -> {
            if(unselectableDate.contains(localDate)) {
                return "date-non-selectable";
            }
            return recurringEventSchedule.getUnselectedDateCssClass();
        }));

        recurringEventSchedule.setComputeNodeForExistingBookedDateFunction((localDate -> {
            if(alreadyBookedDate.contains(localDate)) {
                Label toReturn = new Label("*");
                //TODO define the  colors according to the different case and add a legend and put in css file
                if(bookEventData.isDepositOnPreviousBookingComplete())
                    toReturn.setStyle("-fx-text-fill: green;");
                else
                    toReturn.setStyle("-fx-text-fill: red;");
                return toReturn;
            }
            return null;
        }));

        FlexPane dateFlexPane = recurringEventSchedule.buildUi();
        dateFlexPane.setPadding(new Insets(20, 0, 20, 0));

        //We create a list of local date, that will contain all the selectable date, ie the one that are not in the past, and not already booked
        List<LocalDate> selectableDates = bookEventData.getScheduledItemsOnEvent().stream().map(ScheduledItem::getDate).collect(Collectors.toList());
        selectableDates.removeAll(unselectableDate);

        Hyperlink selectAllClassesHyperlink = I18nControls.bindI18nTextProperty(new Hyperlink(), "SelectAllClasses");
        selectAllClassesHyperlink.setAlignment(Pos.CENTER);
        selectAllClassesHyperlink.getStyleClass().addAll("primary-text", "title4");
        selectAllClassesHyperlink.setOnAction((event -> recurringEventSchedule.selectDates(selectableDates)));
        Text priceText = new Text(I18n.getI18nText("PricePerClass", EventPriceFormatter.formatWithCurrency(bookEventData.getRate(), FXEvent.getEvent())));
        priceText.getStyleClass().add("subtitle-grey");
        VBox.setMargin(priceText, new Insets(20, 0, 5, 0));
        Button checkoutButton = I18nControls.bindI18nProperties(new Button(), "ProceedCheckout");
        //We manage the property of the button in css
        checkoutButton.getStyleClass().addAll("event-button", "success-button");
        checkoutButton.setMaxWidth(300);
        checkoutButton.setOnAction((event -> {
            bookEventData.getCurrentBooking().cancelChanges();
            bookEventData.getCurrentBooking().bookScheduledItems(recurringEventSchedule.getSelectedScheduledItem());
            controller.getStep3CheckoutSlide().buildUi();
            controller.displayNextSlide();
        }));

        checkoutButton.disableProperty().bind(Bindings.createBooleanBinding(()->bookEventData.isDepositOnPreviousBookingComplete()).
                                            and(isOptionsSelectedEmptyProperty));

        mainVbox.setPadding(new Insets(30, 50, 20, 50));
        mainVbox.setAlignment(Pos.TOP_CENTER);
        mainVbox.getChildren().setAll(
                title,
                eventCentreLocationText,
                shortDescriptionPane,
                imageScalePane,
                eventDescription,
                scheduleText,
                selectTheCourseText,
                dateFlexPane,
                selectAllClassesHyperlink,
                priceText,
                checkoutButton
        );

    }

    public void loadData(Event e) {
        Object imageTag = e.getId().getPrimaryKey();
        String pictureId = String.valueOf(imageTag);
        bookEventData.setBalanceProperty(0);
        cloudImageService.exists(pictureId)
                .onFailure(Console::log)
                .onSuccess(exists -> Platform.runLater(() -> {
                    Console.log("exists: " + exists);
                    if (exists) {
                        //First, we need to get the zoom factor of the screen
                        double zoomFactor = Screen.getPrimary().getOutputScaleX();
                        String url = cloudImageService.url(String.valueOf(imageTag), (int) (imageView.getFitWidth() * zoomFactor), -1);
                        Image imageToDisplay = new Image(url, true);
                        imageView.setImage(imageToDisplay);
                    }
                }));

        controller.setEventDataLoaded(false);
        e.onExpressionLoaded("name, shortDescription, description, venue.(name, label, address)")
                .onFailure(ex -> controller.displayErrorMessage(ex.getMessage()))
                .onSuccess(x -> controller.setEventDataLoaded(true));
    }

    public void reset() {
        recurringEventSchedule.getSelectedDates().clear();
        imageView.setImage(null);
        super.reset();
    }

    public RecurringEventSchedule getRecurringEventSchedule() {
        return recurringEventSchedule;
    }

    public void setIsOptionsSelectedEmptyProperty(boolean isOptionsSelectedEmptyProperty) {
        this.isOptionsSelectedEmptyProperty.set(isOptionsSelectedEmptyProperty);
    }
}