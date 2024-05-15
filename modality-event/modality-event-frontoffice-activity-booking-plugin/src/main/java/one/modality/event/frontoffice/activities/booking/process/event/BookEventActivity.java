package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.carrousel.Carrousel;
import dev.webfx.extras.panes.FlexPane;
import dev.webfx.extras.panes.ScaleMode;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
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
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Event;
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


/**
 * @author Bruno Salmon
 */
public final class BookEventActivity extends ViewDomainActivityBase {

    private static final int MAX_WIDTH = 600;

    private final BooleanProperty eventLoadedProperty = new SimpleBooleanProperty();
    private final Text eventTitle = bindI18nEventExpression(new Text(), "i18n(this)");
    private final HtmlText eventShortDescription = bindI18nEventExpression(new HtmlText(), "shortDescription");
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
    private final VBox scheduledItemVBox = new VBox();
    private final BooleanProperty isOptionsSelectedEmptyProperty = new SimpleBooleanProperty();
    private WorkingBooking currentBooking;
    private final ImageView imageView = new ImageView();
    private final Carrousel carrousel = new Carrousel();
    private PolicyAggregate policyAggregate;
    private DocumentAggregate documentAggregate;

    private PriceCalculator priceCalculator;

    @Override
    public Node buildUi() {
        Label waitLabel = new Label("Please wait...");
        //Image loadingImage = new Image("path/to/your/loading_image.png");
        //ImageView imageView = new ImageView(loadingImage);
        loadingVBox.setSpacing(10);
        loadingVBox.setAlignment(Pos.CENTER);
        loadingVBox.getChildren().addAll(waitLabel);//,imageView);

        Label errorLabel = new Label("Error while loading data");
        errorVBox.setSpacing(10);
        errorVBox.setAlignment(Pos.CENTER);
        errorVBox.getChildren().addAll(errorLabel);//,imageView);

        buildEventDetailVBox();
        carrousel.setSlides(loadingVBox,eventDetailVBox, checkoutVBox,errorVBox);
        carrousel.setLoop(false);
        Region carrouselContainer = carrousel.getContainer();
        carrouselContainer.setMaxWidth(MAX_WIDTH);
        return ControlUtil.createVerticalScrollPaneWithPadding(10, new BorderPane(carrouselContainer));
    }

    private void buildCheckoutVBox() {
        checkoutVBox.getChildren().clear();
        checkoutVBox.setAlignment(Pos.TOP_CENTER);
        Label bookedEventTitleText = bindI18nEventExpression(new Label(), "i18n(this)");
        bookedEventTitleText.getStyleClass().add("title-blue-bold");
        BorderPane line1 = new BorderPane(bookedEventTitleText);
        line1.setCenter(bookedEventTitleText);
        bookedEventTitleText.setAlignment(Pos.CENTER_LEFT);
        //bookedEventTitleText.setPrefWidth(MAX_WIDTH - 50);
        line1.setPadding(new Insets(0,0,30,0));
        checkoutVBox.getChildren().add(line1);

        SVGPath locationIcon = SvgIcons.createPinpointSVGPath();
        locationIcon.setFill(Color.web("#BBBBBB"));
        venueAddress.getStyleClass().add("checkout-address");
        venueAddress.setGraphicTextGap(5);
        venueAddress.setGraphic(locationIcon);
        BorderPane addressBorderPane = new BorderPane();
        addressBorderPane.setCenter(venueAddress);
        venueAddress.setAlignment(Pos.CENTER_LEFT);
        //venueAddress.setPrefWidth(MAX_WIDTH - 50);
        addressBorderPane.setPadding(new Insets(0,0,30,0));
        checkoutVBox.getChildren().addAll(addressBorderPane);

        BorderPane header = new BorderPane();
        Label summaryLabel = I18nControls.bindI18nProperties(new Label(),"Summary");
        summaryLabel.setPrefWidth(290);
        Label priceLabel = I18nControls.bindI18nProperties(new Label(),"Price");
        priceLabel.setPrefWidth(70);
        //The actionLabel is used only because we need to put a graphic element in the right part of the borderpane
        //so it's balanced with the content that is shown bellow
        Label actionLabel = new Label();
        actionLabel.setPrefWidth(40);
        header.setLeft(summaryLabel);
        header.setCenter(priceLabel);
        header.setRight(actionLabel);
        header.setMaxWidth(400);
        header.setPadding(new Insets(0,0,5,0));

        scheduledItemVBox.setAlignment(Pos.CENTER);
        scheduledItemVBox.setPadding(new Insets(0,0,40,0));
        //The scheduledItemPane containing the details of the checkout will be populated by the function drawScheduledItemInCheckoutView
        //which is called throw the binding
        documentAggregate = currentBooking.getLastestDocumentAggregate();

        documentAggregate.getAttendances().forEach(a-> {
            BorderPane currentScheduledItemBorderPane = new BorderPane();
            currentScheduledItemBorderPane.setMaxWidth(400); //300+55+45
            String dateFormatted = I18n.getI18nText("DateFormatted", I18n.getI18nText(a.getScheduledItem().getDate().getMonth().name()),a.getScheduledItem().getDate().getDayOfMonth());
            Label name = new Label(a.getScheduledItem().getItem().getName() + " - " + dateFormatted);
            Label price = new Label(EventPriceFormatter.formatWithCurrency(policyAggregate.getRates().get(0).getPrice(),FXEvent.getEvent()));
            name.getStyleClass().add("subtitle-grey");
            price.getStyleClass().add("subtitle-grey");

            Hyperlink trashOption = new Hyperlink();
            SVGPath svgTrash = SvgIcons.createTrashSVGPath();
            svgTrash.setFill(Color.RED);
            trashOption.setGraphic(svgTrash);
            name.setPrefWidth(300);
            price.setPrefWidth(55);
            trashOption.setPrefWidth(45);
            trashOption.setOnAction(event -> {
                currentBooking.removeAttendance(a);
                recurringEventSchedule.getSelectedDates().remove(a.getScheduledItem().getDate());
                scheduledItemVBox.getChildren().remove(currentScheduledItemBorderPane);
                totalPriceLabel.setText(EventPriceFormatter.formatWithCurrency(priceCalculator.calculateTotalPrice(currentBooking.getLastestDocumentAggregate()),FXEvent.getEvent()));
                    });
            currentScheduledItemBorderPane.setLeft(name);
            currentScheduledItemBorderPane.setCenter(price);
            currentScheduledItemBorderPane.setRight(trashOption);
            scheduledItemVBox.getChildren().add(currentScheduledItemBorderPane);
            //Now we calculate the price and update the graphic related to the price
            //totalPriceProperty.setValue(String.valueOf(priceCalculator.calculateTotalPrice(documentAggregate)));
        });
        checkoutVBox.getChildren().addAll(header, scheduledItemVBox);


        HBox totalHBox = new HBox();
        totalHBox.getStyleClass().add("line-total");
        //totalHBox.setMaxWidth(MAX_WIDTH);
        Label totalLabel = I18nControls.bindI18nProperties(new Label(),"Total");
        totalLabel.setPadding(new Insets(5,0,5,50));
        totalLabel.setPrefWidth(350);
        totalPriceLabel.setPadding(new Insets(5,0,5,0));
        totalPriceLabel.setText(EventPriceFormatter.formatWithCurrency(priceCalculator.calculateTotalPrice(currentBooking.getLastestDocumentAggregate()),FXEvent.getEvent()));

        totalHBox.getChildren().addAll(totalLabel,totalPriceLabel);
        checkoutVBox.getChildren().add(totalHBox);

        HBox separatorHBox = new HBox();
        separatorHBox.setPadding(new Insets(0,0,50,0));
        checkoutVBox.getChildren().add(separatorHBox);

        Button backButton = I18nControls.bindI18nProperties(new Button(), "Pay");
        //We manage the property of the button in css
        backButton.setGraphicTextGap(30);
        backButton.getStyleClass().addAll("green-button");
        backButton.setMaxWidth(150);
        backButton.setOnAction((event -> {
            
        }));

        checkoutVBox.getChildren().add(backButton);
    }


    /**
     * In this method, we update the UI according to the event
     * @param e the event that has been selected
     */
    private void loadEventDetails(Event e)
    {
        recurringEventSchedule.getSelectedDates().clear();
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
                .onFailure(Console::log)
                .onSuccess(x -> eventLoadedProperty.set(true));
    }

    private void buildEventDetailVBox() {

        Text title = I18n.bindI18nProperties(new Text(),"GPEvent");
        title.getStyleClass().add("title-blue-bold");
        HBox line1 = new HBox(title);
        line1.setPadding(new Insets(0,0,5,0));
        line1.setAlignment(Pos.BASELINE_CENTER);

        eventDetailVBox.getChildren().add(line1);

        Text eventCentreLocationText = bindI18nEventExpression(new Text(),
                "'[At] ' + coalesce(i18n(venue), i18n(organization))");

        eventDetailVBox.getChildren().add(eventCentreLocationText);

        eventShortDescription.getStyleClass().add("subtitle-grey");
      //  eventTitle.getStyleClass().add("subtitle-grey");
        HBox line2 = new HBox(eventShortDescription);
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
        scheduleText.getStyleClass().add("title-blue-bold");
        HBox line4 = new HBox(scheduleText);
        line4.setPadding(new Insets(20,0,10,0));
        line4.setAlignment(Pos.BASELINE_CENTER);
        eventDetailVBox.getChildren().add(line4);

        Text selectTheCourseText = I18n.bindI18nProperties( new Text(),"SelectTheEvent");
        HBox line5 = new HBox(selectTheCourseText);
        line5.setPadding(new Insets(0, 0, 5, 0));
        line5.setAlignment(Pos.BASELINE_CENTER);
        eventDetailVBox.getChildren().add(line5);

        FlexPane dateFlexPane = recurringEventSchedule.buildUi();
        dateFlexPane.setPadding(new Insets(20, 0, 20, 0));
        eventDetailVBox.getChildren().add(dateFlexPane);

        Hyperlink selectAllClassesHyperlink = I18nControls.bindI18nTextProperty(new Hyperlink(),"SelectAllClasses");
        selectAllClassesHyperlink.setAlignment(Pos.BASELINE_CENTER);
        selectAllClassesHyperlink.getStyleClass().add("subtitle-blue");
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
        checkoutButton.getStyleClass().addAll("green-button");
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
                    carrousel.displaySlide(0);
                });
            }
            //When the data are loaded, we go to the slide showing the data
            if(newValue) {
                Platform.runLater(() -> {
                   carrousel.displaySlide(1);
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
                            documentAggregate = currentBooking.getInitialDocumentAggregate();
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
