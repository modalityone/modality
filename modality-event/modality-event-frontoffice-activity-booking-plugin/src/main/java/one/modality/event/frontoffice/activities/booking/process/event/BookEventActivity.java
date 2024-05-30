package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.carrousel.Carrousel;
import dev.webfx.extras.imagestore.ImageStore;
import dev.webfx.extras.panes.FlexPane;
import dev.webfx.extras.panes.ScaleMode;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Numbers;
import dev.webfx.stack.cloud.image.CloudImageService;
import dev.webfx.stack.cloud.image.impl.client.ClientImageService;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityId;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.LoadPolicyArgument;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.client.event.fx.FXEventId;
import one.modality.event.frontoffice.activities.booking.PriceCalculator;
import one.modality.event.frontoffice.activities.booking.WorkingBooking;

import java.time.LocalDate;
import java.util.List;


/**
 * @author Bruno Salmon
 */
public final class BookEventActivity extends ViewDomainActivityBase {

    private static final int MAX_WIDTH = 600;

    private final BooleanProperty eventLoadedProperty = new SimpleBooleanProperty();
    private final Text eventTitle = bindI18nEventExpression(new Text(), "i18n(this)");
    private final Text errorMessage = new Text();//bindI18nEventExpression(new Text(), "i18n(this)");
    private final HtmlText eventShortDescriptionInEventSlide = bindI18nEventExpression(new HtmlText(), "shortDescription");
    private final HtmlText eventShortDescriptionInCheckoutSlide = bindI18nEventExpression(new HtmlText(), "shortDescription");

    private final Label venueAddress = bindI18nEventExpression(new Label(), "venue.address");
    private final HtmlText eventDescription = bindI18nEventExpression(new HtmlText(), "description");
    private final Label totalPriceLabel = new Label();
    private final StringProperty totalPriceProperty = new SimpleStringProperty("");

    private final CloudImageService cloudImageService = new ClientImageService();
    private final RecurringEventSchedule recurringEventSchedule = new RecurringEventSchedule();
    private final VBox eventDetailVBox = new VBox();
    private final VBox checkoutVBox = new VBox();
    private final VBox loadingVBox = new VBox();
    private final VBox errorVBox = new VBox();
    private final VBox bookingConfirmedVBox = new VBox();
    private final VBox scheduledItemVBox = new VBox();
    private final BooleanProperty isOptionsSelectedEmptyProperty = new SimpleBooleanProperty();
    private WorkingBooking currentBooking;
    private final ImageView imageView = new ImageView();
    private final Carrousel carrousel = new Carrousel();
    private PolicyAggregate policyAggregate;
    private DocumentAggregate documentAggregate;

    private PriceCalculator priceCalculator;
    private Object bookingNumber;
    private List<ScheduledItem> sheduledItemsAlreadyBooked;

    @Override
    public Node buildUi() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(100,100);
        loadingVBox.setSpacing(10);
        loadingVBox.setAlignment(Pos.CENTER);
        loadingVBox.getChildren().add(progressIndicator);


        errorVBox.setSpacing(10);
        errorVBox.setAlignment(Pos.CENTER);
        errorVBox.getChildren().addAll(errorMessage);//,imageView);

        buildEventDetailVBox();
        buildBookingConfirmedVBox();
        carrousel.setSlides(loadingVBox,eventDetailVBox, checkoutVBox,bookingConfirmedVBox,errorVBox);
        carrousel.setLoop(false);
        Region carrouselContainer = carrousel.getContainer();
        carrouselContainer.setMaxWidth(MAX_WIDTH);
        ScrollPane scrollPaneToReturn =  ControlUtil.createVerticalScrollPaneWithPadding(10, new BorderPane(carrouselContainer));

        loadingVBox.prefHeightProperty().bind(scrollPaneToReturn.heightProperty());
        checkoutVBox.prefHeightProperty().bind(scrollPaneToReturn.heightProperty());
        bookingConfirmedVBox.prefHeightProperty().bind(scrollPaneToReturn.heightProperty());
        errorVBox.prefHeightProperty().bind(scrollPaneToReturn.heightProperty());
        return scrollPaneToReturn;
    }

    private void buildCheckoutVBox() {
        checkoutVBox.getChildren().clear();
        checkoutVBox.setAlignment(Pos.TOP_CENTER);
        Label bookedEventTitleText = bindI18nEventExpression(new Label(), "i18n(this)");
        bookedEventTitleText.getStyleClass().addAll("book-event-primary-title","emphasize");
        HBox line1 = new HBox(bookedEventTitleText);
        line1.setAlignment(Pos.CENTER_LEFT);
        //bookedEventTitleText.setPrefWidth(MAX_WIDTH - 50);
        line1.setPadding(new Insets(20,0,0,50));
        checkoutVBox.getChildren().add(line1);

        eventShortDescriptionInCheckoutSlide.getStyleClass().add("subtitle-grey");
        eventShortDescriptionInCheckoutSlide.setMaxWidth(300);
        HBox line2 = new HBox(eventShortDescriptionInCheckoutSlide);
        line2.setAlignment(Pos.BASELINE_LEFT);
        line2.setPadding(new Insets(10,0,0,50));
        checkoutVBox.getChildren().add(line2);

        SVGPath locationIcon = SvgIcons.createPinpointSVGPath();
        venueAddress.getStyleClass().add("checkout-address");
        venueAddress.setGraphicTextGap(5);
        venueAddress.setGraphic(locationIcon);
        HBox addressBorderHBox = new HBox();
        addressBorderHBox.getChildren().add(venueAddress);
        addressBorderHBox.setAlignment(Pos.CENTER_LEFT);
        //venueAddress.setPrefWidth(MAX_WIDTH - 50);
        venueAddress.setPadding(new Insets(0,0,30,50));
        checkoutVBox.getChildren().addAll(addressBorderHBox);

        HBox headerHBox = new HBox();
        Region spacerInHeader1 = new Region();
        spacerInHeader1.setPrefWidth(80);
        Label summaryLabel = I18nControls.bindI18nProperties(new Label(),"Summary");
        Region spacerInHeader2 = new Region();
        HBox.setHgrow(spacerInHeader2,Priority.ALWAYS);
        Label priceLabel = I18nControls.bindI18nProperties(new Label(),"Price");
        priceLabel.setPrefWidth(70);
        //The actionLabel is used only because we need to put a graphic element in the right part of the borderpane
        //so it's balanced with the content that is shown bellow
        Label actionLabel = new Label();
        actionLabel.setPrefWidth(40);
        headerHBox.getChildren().addAll(spacerInHeader1,summaryLabel,spacerInHeader2,priceLabel,actionLabel);
       // headerHBox.setMaxWidth(400);
        headerHBox.setPadding(new Insets(0,0,5,0));

        scheduledItemVBox.setAlignment(Pos.CENTER);
        scheduledItemVBox.setPadding(new Insets(0,0,40,0));
        //The scheduledItemPane containing the details of the checkout will be populated by the function drawScheduledItemInCheckoutView
        //which is called throw the binding
        documentAggregate = currentBooking.getLastestDocumentAggregate();
        scheduledItemVBox.getChildren().clear();
        documentAggregate.getAttendances().forEach(a-> {
            HBox currentScheduledItemHBox = new HBox();
            //currentScheduledItemHBox.setMaxWidth(500); //300+55+45
            String dateFormatted = I18n.getI18nText("DateFormatted", I18n.getI18nText(a.getScheduledItem().getDate().getMonth().name()),a.getScheduledItem().getDate().getDayOfMonth());
            Label name = new Label(a.getScheduledItem().getItem().getName() + " - " + dateFormatted);
            Region spacer1 = new Region();
            Region spacer2 = new Region();
            spacer1.setPrefWidth(80);
            Label price = new Label(EventPriceFormatter.formatWithCurrency(policyAggregate.getRates().get(0).getPrice(),FXEvent.getEvent()));
            name.getStyleClass().add("subtitle-grey");
            price.getStyleClass().add("subtitle-grey");

            Hyperlink trashOption = new Hyperlink();
            SVGPath svgTrash = SvgIcons.createTrashSVGPath();
            svgTrash.setFill(Color.RED);
            trashOption.setGraphic(svgTrash);
            HBox.setHgrow(spacer2,Priority.ALWAYS);
            price.setPrefWidth(55);
            trashOption.setPrefWidth(45);
            trashOption.setOnAction(event -> {
                currentBooking.removeAttendance(a);
                recurringEventSchedule.getSelectedDates().remove(a.getScheduledItem().getDate());
                scheduledItemVBox.getChildren().remove(currentScheduledItemHBox);
                totalPriceLabel.setText(EventPriceFormatter.formatWithCurrency(priceCalculator.calculateTotalPrice(currentBooking.getLastestDocumentAggregate()),FXEvent.getEvent()));
                    });
            currentScheduledItemHBox.getChildren().addAll(spacer1,name,spacer2,price,trashOption);
            scheduledItemVBox.getChildren().add(currentScheduledItemHBox);
            //Now we calculate the price and update the graphic related to the price
            //totalPriceProperty.setValue(String.valueOf(priceCalculator.calculateTotalPrice(documentAggregate)));
        });


        HBox totalHBox = new HBox();
        totalHBox.getStyleClass().add("line-total");
        //totalHBox.setMaxWidth(MAX_WIDTH);
        Label totalLabel = I18nControls.bindI18nProperties(new Label(),"Total");
        Region spacerTotal = new Region();
        totalLabel.setPadding(new Insets(5,0,5,50));
        HBox.setHgrow(spacerTotal,Priority.ALWAYS);
        totalPriceLabel.setPadding(new Insets(5,75,5,0));
        totalPriceLabel.setText(EventPriceFormatter.formatWithCurrency(priceCalculator.calculateTotalPrice(currentBooking.getLastestDocumentAggregate()),FXEvent.getEvent()));
        totalHBox.getChildren().addAll(totalLabel,spacerTotal,totalPriceLabel);

        HBox sepHBox = new HBox();
        sepHBox.setPadding(new Insets(0,0,30,0));
        scheduledItemVBox.getChildren().add(sepHBox);

        scheduledItemVBox.getChildren().add(totalHBox);
        checkoutVBox.getChildren().addAll(headerHBox, scheduledItemVBox);

        HBox separatorHBox = new HBox();
        separatorHBox.setPadding(new Insets(0,0,50,0));
        checkoutVBox.getChildren().add(separatorHBox);

        Button payButton = I18nControls.bindI18nProperties(new Button(), "Pay");
        //We manage the property of the button in css
        payButton.setGraphicTextGap(30);
        payButton.getStyleClass().addAll("event-button","success-button");
        payButton.setMaxWidth(150);
        payButton.setOnAction((event -> {
            currentBooking.submitChanges("Booked Online")
                    .onFailure(Console::log)
                    .onSuccess(result -> Platform.runLater(() -> {
                        bookingNumber = result.getCartPrimaryKey();
                        carrousel.displaySlide(3);}));
        }));

        checkoutVBox.getChildren().add(payButton);
    }


    /**
     * In this method, we update the UI according to the event
     * @param e the event that has been selected
     */
    private void loadEventDetails(Event e) {
        recurringEventSchedule.getSelectedDates().clear();
        errorMessage.setText("");
        Object imageTag = e.getId().getPrimaryKey();
        String pictureId = String.valueOf(imageTag);
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


        eventLoadedProperty.set(false);
        e.onExpressionLoaded("name, shortDescription, description, venue.(name, label, address)")
                .onFailure(x -> displayErrorMessage(x.toString()))
                .onSuccess(x -> eventLoadedProperty.set(true));

        //TODO : récupérer dans la bases les scheduledItem déjà booké pour éviter de reserver deux fois
       /* e.getStore().executeQuery(
                        new EntityStoreQuery("select scheduledItem si where exists(select attendance where scheduledItem=si and documentLine.document.person=? and documentLine.document.event=?)", new Object[]{e,FXUserPerson.getUserPerson()}))
                .onFailure(Console::log)
                .onSuccess(query -> Platform.runLater(() -> {
                    EntityList<ScheduledItem> scheduledItemList = query.getStore().getEntityList(query.getListId());
                }));*/
    }

    private void displayErrorMessage(String message) {
        errorMessage.setText(message);
        Platform.runLater(() -> {
            carrousel.displaySlide(4);
        });
    }

    private void buildEventDetailVBox() {

        Text title = I18n.bindI18nProperties(new Text(),"GPEvent");
        title.getStyleClass().addAll("book-event-primary-title");
        HBox line1 = new HBox(title);
        line1.setPadding(new Insets(0,0,5,0));
        line1.setAlignment(Pos.BASELINE_CENTER);

        eventDetailVBox.getChildren().add(line1);

        Text eventCentreLocationText = bindI18nEventExpression(new Text(),
                "'[At] ' + coalesce(i18n(venue), i18n(organization))");

        eventDetailVBox.getChildren().add(eventCentreLocationText);

        eventShortDescriptionInEventSlide.getStyleClass().add("subtitle-grey");
      //  eventTitle.getStyleClass().add("subtitle-grey");
        HBox line2 = new HBox(eventShortDescriptionInEventSlide);
        line2.setAlignment(Pos.BASELINE_CENTER);
        line2.setPadding(new Insets(5,0,15,0));

        eventDetailVBox.getChildren().add(line2);

        //imageView.setPreserveRatio(true);
        ScalePane scalePane = new ScalePane(ScaleMode.FIT_WIDTH, imageView);
        scalePane.setCanGrow(false);

        eventDetailVBox.getChildren().add(scalePane);

        eventDescription.setPadding(new Insets(20,0,0,0));
        eventDescription.getStyleClass().add("description-text");
        eventDetailVBox.getChildren().add(eventDescription);

        Text scheduleText = I18n.bindI18nProperties( new Text(),"Schedule");
        scheduleText.getStyleClass().addAll("book-event-primary-title");
        HBox line4 = new HBox(scheduleText);
        line4.setPadding(new Insets(20,0,10,0));
        line4.setAlignment(Pos.BASELINE_CENTER);
        eventDetailVBox.getChildren().add(line4);

        Text selectTheCourseText = I18n.bindI18nProperties( new Text(),"SelectTheEvent");
        HBox line5 = new HBox(selectTheCourseText);
        line5.setPadding(new Insets(0, 0, 5, 0));
        line5.setAlignment(Pos.BASELINE_CENTER);
        eventDetailVBox.getChildren().add(line5);

        recurringEventSchedule.setOnDateClicked(localDate -> {
            if(localDate.isBefore(LocalDate.now())) {
                return;
            }
            //TODO: on va chercher dans la base si la personne est déjà inscrite à l'événement.
            if(currentBooking.getLastestDocumentAggregate().getAttendances().stream()
                    .map(one.modality.base.shared.entities.Attendance::getScheduledItem)
                    .map(one.modality.base.shared.entities.ScheduledItem::getDate)
                    .anyMatch(date -> date.equals(localDate))) {
                return;
            }
            recurringEventSchedule.processDateSelected(localDate);
        });
        recurringEventSchedule.setUnselectedDateCssGetter((localDate -> {
            if(currentBooking.getLastestDocumentAggregate().getAttendances().stream()
                    .map(one.modality.base.shared.entities.Attendance::getScheduledItem) // Obtenir le ScheduledItem
                    .map(one.modality.base.shared.entities.ScheduledItem::getDate) // Obtenir la date
                    .anyMatch(date -> date.equals(localDate))) {
                return "date-already-booked";
            }
            //If the date of the scheduledItem is before today, we change the color
            if(localDate.isBefore(LocalDate.now())) {
                return "date-past-not-selected";
            }
            return recurringEventSchedule.getUnselectedDateCssClass();
        }));


        FlexPane dateFlexPane = recurringEventSchedule.buildUi();
        dateFlexPane.setPadding(new Insets(20, 0, 20, 0));
        eventDetailVBox.getChildren().add(dateFlexPane);

        Hyperlink selectAllClassesHyperlink = I18nControls.bindI18nTextProperty(new Hyperlink(),"SelectAllClasses");
        selectAllClassesHyperlink.setAlignment(Pos.BASELINE_CENTER);
        selectAllClassesHyperlink.getStyleClass().addAll("primary-text","title4");
        selectAllClassesHyperlink.setOnAction((event ->recurringEventSchedule.selectAllDates()));
        eventDetailVBox.getChildren().add(selectAllClassesHyperlink);

        //TODO: retrieve the price, for now we hardcode it
        Text priceText = new Text(I18n.getI18nText( "PricePerClass", 7, "£"));
        HBox line6 = new HBox(priceText);
        priceText.getStyleClass().add("subtitle-grey");
        line6.setPadding(new Insets(20, 0, 5, 0));
        line6.setAlignment(Pos.BASELINE_CENTER);
        eventDetailVBox.getChildren().add(line6);

        //TODO: retrieve the discount, for now we hardcode it
        Text priceForAllClassesText = new Text(I18n.getI18nText( "DiscountForAllSeries", 15));
        HBox line7 = new HBox(priceForAllClassesText);
        priceForAllClassesText.getStyleClass().add("subtitle-grey");
        line7.setAlignment(Pos.BASELINE_CENTER);

        line7.setPadding(new Insets(0, 0, 20, 0));
        eventDetailVBox.getChildren().add(line7);

        Button checkoutButton = I18nControls.bindI18nProperties(new Button(), "ProceedCheckout");
        //We manage the property of the button in css
        checkoutButton.getStyleClass().addAll("event-button","success-button");
        checkoutButton.setMaxWidth(300);
        checkoutButton.setOnAction((event -> {
            currentBooking.cancelChanges();
            currentBooking.bookScheduledItems(recurringEventSchedule.getSelectedScheduledItem());
            buildCheckoutVBox();
            carrousel.moveForward();
        }));

        eventDetailVBox.getChildren().add(checkoutButton);

        eventDetailVBox.setPadding(new Insets(30, 50, 20, 50));
        //eventDetailVBox.setMaxWidth(MAX_WIDTH);
        eventDetailVBox.setAlignment(Pos.TOP_CENTER);

        checkoutButton.disableProperty().bind(isOptionsSelectedEmptyProperty);

        //totalPriceLabel.textProperty().bind(Bindings.format("%.2f£", totalPriceProperty)); // Not supported by WebFX
    }

    private void buildBookingConfirmedVBox() {
        bookingConfirmedVBox.setAlignment(Pos.TOP_CENTER);
        bookingConfirmedVBox.setSpacing(40);
        String headerImageUrl = SourcesConfig.getSourcesRootConfig().childConfigAt("modality.event.frontoffice.activity.bookevent").getString("headerImageUrl");
        ImageView headerImageView = ImageStore.createImageView(headerImageUrl);
        ScalePane headerImageScalePane = new ScalePane(headerImageView);
        headerImageScalePane.setPadding(new Insets(30,0,50,0));
        bookingConfirmedVBox.getChildren().add(headerImageScalePane);

        Label bookingConfirmedLabel = I18nControls.bindI18nProperties(new Label(), "BookingSubmitted");
        bookingConfirmedLabel.setContentDisplay(ContentDisplay.TOP);
        bookingConfirmedLabel.setGraphicTextGap(10);
      //  bookingConfirmedLabel.getGraphic().getStyleClass().add("success-text");
        bookingConfirmedLabel.getStyleClass().addAll("success-text","emphasize");
        bookingConfirmedVBox.getChildren().add(bookingConfirmedLabel);

        HBox thankYouHBox = new HBox();
        thankYouHBox.setAlignment(Pos.CENTER);
        Label thankYouLabel = I18nControls.bindI18nProperties(new Label(), "ThankYouForBooking");
        thankYouLabel.setWrapText(true);
        thankYouLabel.setTextAlignment(TextAlignment.CENTER);
        thankYouLabel.getStyleClass().add("secondary-text");

        //thankYouHBox.maxWidthProperty().bind(headerImageScalePane.widthProperty().multiply(0.8));
        thankYouHBox.maxWidthProperty().bind(FXProperties.compute(headerImageScalePane.widthProperty(), w -> w.doubleValue() * 0.8));

        thankYouHBox.getChildren().add(thankYouLabel);
        bookingConfirmedVBox.getChildren().add(thankYouHBox);

        HBox bookingNumberHBox = new HBox();
        bookingNumberHBox.setAlignment(Pos.CENTER);
        //TODO: replace by the Booking Number when the insert function will be implemented
        Label bookingNumber = I18nControls.bindI18nProperties(new Label(), "BookingNumber",12);
        bookingNumber.setWrapText(true);
        bookingNumber.setTextAlignment(TextAlignment.CENTER);
        bookingNumber.setMaxWidth(MAX_WIDTH*0.8);
        bookingNumberHBox.getChildren().add(bookingNumber);
        bookingConfirmedVBox.getChildren().add(bookingNumberHBox);

    }

    @Override
    protected void updateContextParametersFromRoute() {
        super.updateContextParametersFromRoute();
    }

    @Override
    protected void updateModelFromContextParameters() {
        Object eventId = Numbers.toShortestNumber((Object) getParameter("eventId"));
        FXEventId.setEventId(EntityId.create(Event.class, eventId));
    }

    @Override
    protected void startLogic() {
        eventLoadedProperty.addListener((observable, oldValue, newValue) -> {
            //Here the data are not loaded
            if(!newValue){
                Platform.runLater(() -> {
                    carrousel.displaySlide(0,false);
                });
            }
            //When the data are loaded, we go to the slide showing the data
            if(newValue) {
                Platform.runLater(() -> {
                   carrousel.displaySlide(1,false);
                });
            }
        });

        FXProperties.runNowAndOnPropertiesChange(() -> {
            eventLoadedProperty.setValue(false);
            DocumentService.loadPolicy(new LoadPolicyArgument(FXEventId.getEventId().getPrimaryKey())).onFailure(Console::log)
                        .onSuccess(pa -> {
                            policyAggregate = pa;
                            priceCalculator = new PriceCalculator(policyAggregate);
                            currentBooking = new WorkingBooking(policyAggregate);
                            loadEventDetails(FXEvent.getEvent());
                            documentAggregate = currentBooking.getLastestDocumentAggregate();
                            recurringEventSchedule.setScheduledItems(policyAggregate.getScheduledItems());
                            // We add a listener on the date to update the BooleanProperty bound to the disable property of the checkout button
                            recurringEventSchedule.getSelectedDates().addListener((ListChangeListener<LocalDate>) change -> {
                                isOptionsSelectedEmptyProperty.set(recurringEventSchedule.getSelectedDates().isEmpty());
                            });
                            isOptionsSelectedEmptyProperty.set(recurringEventSchedule.getSelectedDates().isEmpty());
                        });
        }, FXEventId.eventIdProperty());
    }

    // I18n utility methods

    private void bindI18nEventExpression(Property<String> textProperty, String eventExpression) {
        I18n.bindI18nTextProperty(textProperty, new I18nSubKey("expression: " + eventExpression, FXEvent.eventProperty()), eventLoadedProperty);
    }

    private <T extends Text> T bindI18nEventExpression(T text, String eventExpression) {
        bindI18nEventExpression(text.textProperty(), eventExpression);
        return text;
    }

    private <L extends Labeled> L bindI18nEventExpression(L text, String eventExpression) {
        bindI18nEventExpression(text.textProperty(), eventExpression);
        return text;
    }

    private HtmlText bindI18nEventExpression(HtmlText text, String eventExpression) {
        bindI18nEventExpression(text.textProperty(), eventExpression);
        return text;
    }

}
