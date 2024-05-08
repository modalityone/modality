package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.panes.FlexPane;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
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
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.client.event.fx.FXEventId;
import one.modality.event.frontoffice.activities.booking.WorkingBooking;
import one.modality.event.frontoffice.activities.booking.fx.FXEventAggregate;
import one.modality.event.frontoffice.activities.booking.process.EventAggregate;

import java.time.LocalDate;
import java.util.Optional;


/**
 * @author Bruno Salmon
 */
public final class BookEventActivity extends ViewDomainActivityBase {

    private Event currentEvent = FXEvent.getEvent();
    private Text eventTitle = new Text();
    private HtmlText eventDescription = new HtmlText();
    private Button checkoutButton;
    private DoubleProperty totalPriceProperty = new SimpleDoubleProperty(0);
    private final Label totalPriceLabel = new Label();
    private final CloudImageService cloudImageService = new ClientImageService();
    private RecurringEventSchedule recurringEventSchedule = new RecurringEventSchedule();
    private VBox eventDetailVBox = new VBox();
    private VBox checkoutVBox = new VBox();
    private BorderPane containerBorderPane = new BorderPane();
    private final VBox scheduledItemVBox = new VBox();
    private BooleanProperty isOptionsSelectedEmptyProperty = new SimpleBooleanProperty();
    private WorkingBooking currentBooking = new WorkingBooking();
    private ImageView imageView = new ImageView();

    final int maxWidth = 500;


    @Override
    public Node buildUi() {
        switchToDisplayEventDetails();
        buildEventDetailVBox();
        ScrollPane scrollPane = new ScrollPane(containerBorderPane);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));
        return scrollPane;
    }

    private void computeCheckoutVBox() {
        switchToCheckoutView();
        checkoutVBox.getChildren().clear();
        checkoutVBox.setAlignment(Pos.CENTER);
        Hyperlink backLink = new Hyperlink("<-");
        backLink.setOnAction(event->switchToDisplayEventDetails());
        backLink.setAlignment(Pos.CENTER_LEFT);
        checkoutVBox.getChildren().add(backLink);
        Text bookedEventTitleText = new Text(FXEvent.getEvent().getName());
        bookedEventTitleText.getStyleClass().add("title-blue-bold");
        HBox line1 = new HBox(bookedEventTitleText);
        line1.setAlignment(Pos.BASELINE_CENTER);
        line1.setPadding(new Insets(5,0,30,0));

        checkoutVBox.getChildren().add(line1);

        SVGPath locationIcon = SvgIcons.createPinpointSVGPath(Color.GRAY);
        Text eventCentreLocationText = new Text();
        I18n.bindI18nProperties(eventCentreLocationText, new I18nSubKey("expression: '[At] ' + coalesce(i18n(venue), i18n(organization))", currentEvent));
        HBox addressLine = new HBox(locationIcon,eventCentreLocationText);
        addressLine.setAlignment(Pos.CENTER);
        addressLine.setSpacing(5);
        addressLine.setPadding(new Insets(0,0,30,0));
        checkoutVBox.getChildren().add(addressLine);

        // We filter the scheduledItems to have only the one which date in the selectedDates list
        currentBooking.setScheduledItems(recurringEventSchedule.getSelectedScheduledItem());

        BorderPane header = new BorderPane();
        Label summmaryLabel = I18nControls.bindI18nProperties(new Label(),"Summary");
        summmaryLabel.setPrefWidth(290);
        Label priceLabel = I18nControls.bindI18nProperties(new Label(),"Price");
        priceLabel.setPrefWidth(70);
        //The actionLabel is used only because we need to put a graphic element in the right part of the borderpane
        //so it's balanced with the content that is shown bellow
        Label actionLabel = new Label();
        actionLabel.setPrefWidth(40);
        header.setLeft(summmaryLabel);
        header.setCenter(priceLabel);
        header.setRight(actionLabel);
        header.setMaxWidth(400);
        header.setPadding(new Insets(0,0,5,0));

        scheduledItemVBox.setAlignment(Pos.CENTER);
        scheduledItemVBox.setPadding(new Insets(0,0,40,0));
        //The scheduledItemPane containing the details of the checkout will be populated by the function drawScheduledItemInCheckoutView
        //which is called throw the binding
        checkoutVBox.getChildren().addAll(header, scheduledItemVBox);

        HBox totalHBox = new HBox();
        totalHBox.getStyleClass().add("line-total");
        totalHBox.setMaxWidth(maxWidth);
        Label totalLabel = I18nControls.bindI18nProperties(new Label(),"Total");
        totalLabel.setPadding(new Insets(5,0,5,50));
        totalLabel.setPrefWidth(350);
        totalPriceLabel.setPadding(new Insets(5,0,5,0));
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

    private void switchToDisplayEventDetails() {
        containerBorderPane.setCenter(eventDetailVBox);
    }

    private void switchToCheckoutView() {
        containerBorderPane.setCenter(checkoutVBox);

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
                .onFailure(ex -> {
                    Console.log(ex);
                })
                .onSuccess(exists -> Platform.runLater(() -> {
                    Console.log("exists: " + exists);
                    if (!exists) {
                    }
                    else {
                        //First, we need to get the zoom factor of the screen
                        double zoomFactor = Screen.getPrimary().getOutputScaleX();
                        String url = cloudImageService.url(String.valueOf(imageTag), (int) (imageView.getFitWidth()*zoomFactor), -1);
                        Image imageToDisplay = new Image(url, true);
                        imageView.setImage(imageToDisplay);
                    }
                }));


        e.onExpressionLoaded("name, description").onFailure(Console::log)
                .onSuccess(x -> Platform.runLater(()-> {
                    eventTitle.setText(e.getName());
                    eventDescription.setText(e.getDescription());
                }));
    }

    private void buildEventDetailVBox() {

        Text title = I18n.bindI18nProperties( new Text(),"GPEvent");
        title.getStyleClass().add("title-blue-bold");
        HBox line1 = new HBox(title);
        line1.setPadding(new Insets(0,0,5,0));
        line1.setAlignment(Pos.BASELINE_CENTER);

        eventDetailVBox.getChildren().add(line1);

        Text eventCentreLocationText = new Text();
        I18n.bindI18nProperties(eventCentreLocationText, new I18nSubKey("expression: '[At] ' + coalesce(i18n(venue), i18n(organization))", currentEvent));

        eventDetailVBox.getChildren().add(eventCentreLocationText);

        eventTitle.getStyleClass().add("subtitle-grey");
        HBox line2 = new HBox(eventTitle);
        line2.setAlignment(Pos.BASELINE_CENTER);
        line2.setPadding(new Insets(5,0,15,0));

        eventDetailVBox.getChildren().add(line2);

        imageView.setPreserveRatio(true);
        imageView.setFitWidth(maxWidth);

        eventDetailVBox.getChildren().add(imageView);

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
        line5.setPadding(new Insets(0,0,5,0));
        line5.setAlignment(Pos.BASELINE_CENTER);
        eventDetailVBox.getChildren().add(line5);

        FlexPane dateFlexPane = recurringEventSchedule.buildUi();
        dateFlexPane.setPadding(new Insets(20,0,5,0));
        eventDetailVBox.getChildren().add(dateFlexPane);

        Hyperlink selectAllClassesHyperlink = I18nControls.bindI18nTextProperty(new Hyperlink(),"SelectAllClasses");
        selectAllClassesHyperlink.setAlignment(Pos.BASELINE_CENTER);
        selectAllClassesHyperlink.getStyleClass().add("subtitle-blue");
        selectAllClassesHyperlink.setOnAction((event ->recurringEventSchedule.selectAllDates()));
        eventDetailVBox.getChildren().add(selectAllClassesHyperlink);

        //TODO: retrieve the price, for now we harcode it
        Text priceText = new Text(I18n.getI18nText( "PricePerClass",7,"£"));
        HBox line6 = new HBox(priceText);
        priceText.getStyleClass().add("subtitle-grey");
        line6.setPadding(new Insets(20,0,5,0));
        line6.setAlignment(Pos.BASELINE_CENTER);
        eventDetailVBox.getChildren().add(line6);


        //TODO: retrieve the discount, for now we harcode it
        Text priceForAllClassesText = new Text(I18n.getI18nText( "DiscountForAllSeries",15));
        HBox line7 = new HBox(priceForAllClassesText);
        priceForAllClassesText.getStyleClass().add("subtitle-grey");
        line7.setAlignment(Pos.BASELINE_CENTER);

        line7.setPadding(new Insets(0,0,20,0));
        eventDetailVBox.getChildren().add(line7);

        checkoutButton = I18nControls.bindI18nProperties(new Button(), "ProceedCheckout");
        //We manage the property of the button in css
        checkoutButton.getStyleClass().addAll("green-button");
        checkoutButton.setMaxWidth(300);
        checkoutButton.setOnAction((event -> {
            computeCheckoutVBox();
        }));

        eventDetailVBox.getChildren().add(checkoutButton);

        eventDetailVBox.setPadding(new Insets(30,50,20,50));
        eventDetailVBox.setMaxWidth(maxWidth);
        eventDetailVBox.setAlignment(Pos.TOP_CENTER);

        checkoutButton.disableProperty().bind(isOptionsSelectedEmptyProperty);
        ObservableLists.bindConverted(scheduledItemVBox.getChildren(),recurringEventSchedule.getSelectedDates(),this::drawScheduledItemInCheckoutView);
        totalPriceLabel.textProperty().bind(Bindings.format("%.2f£", totalPriceProperty));
    }


    private BorderPane drawScheduledItemInCheckoutView(LocalDate date) {
        Optional<ScheduledItem> result = FXEventAggregate.getEventAggregate().getScheduledItems().stream()
                .filter(item -> item.getDate().equals(date))
                .findFirst();

        ScheduledItem currentScheduledItem;
        // Check if an element was found
        if (result.isPresent()) {
            currentScheduledItem = result.get();
        } else {
            currentScheduledItem = null;
            // Handle the case where no element with the target date was found
            Console.log("Scheduled Item not found in the list");
            return null;
        }

        BorderPane currentScheduledItemBorderPane = new BorderPane();
        currentScheduledItemBorderPane.setMaxWidth(400); //300+55+45
        String dateFormatted = I18n.getI18nText("DateFormatted",I18n.getI18nText(currentScheduledItem.getDate().getMonth().name()),currentScheduledItem.getDate().getDayOfMonth());
        Label name = new Label(currentScheduledItem.getItem().getName() + " - "+ dateFormatted);
        Label price = new Label("7£");
        name.getStyleClass().add("subtitle-grey");
        price.getStyleClass().add("subtitle-grey");

        Hyperlink trashOption = new Hyperlink();
        SVGPath svgTrash = SvgIcons.createTrashSVGPath();
        svgTrash.setFill(Color.INDIANRED);
        trashOption.setGraphic(svgTrash);
        name.setPrefWidth(300);
        price.setPrefWidth(55);
        trashOption.setPrefWidth(45);
        trashOption.setOnAction(event ->  recurringEventSchedule.getSelectedDates().remove(currentScheduledItem.getDate()));
        currentScheduledItemBorderPane.setLeft(name);
        currentScheduledItemBorderPane.setCenter(price);
        currentScheduledItemBorderPane.setRight(trashOption);
        scheduledItemVBox.getChildren().add(currentScheduledItemBorderPane);

        //Now we calculate the price and update the graphic related to the price
        totalPriceProperty.setValue(recurringEventSchedule.getSelectedDates().size()*7);
        return currentScheduledItemBorderPane;
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
        FXProperties.runNowAndOnPropertiesChange(() -> {
            EventAggregate eventAggregate = FXEventAggregate.getEventAggregate();
            if (eventAggregate != null) {
                eventAggregate.load()
                        .onFailure(Console::log)
                        .onSuccess(ignored -> {
                            loadEventDetails(FXEvent.getEvent());
                            recurringEventSchedule.setScheduledItems(FXEventAggregate.getEventAggregate().getScheduledItems());
                            //We add a listener on the date to update the BooleanProperty binded with the disable property of the checkout button
                            recurringEventSchedule.getSelectedDates().addListener((ListChangeListener<LocalDate>) change -> {
                                isOptionsSelectedEmptyProperty.set(recurringEventSchedule.getSelectedDates().isEmpty());
                            });

                            isOptionsSelectedEmptyProperty.set(recurringEventSchedule.getSelectedDates().isEmpty());
                        });
            }
        }, FXEventAggregate.eventAggregateProperty());
    }
}
