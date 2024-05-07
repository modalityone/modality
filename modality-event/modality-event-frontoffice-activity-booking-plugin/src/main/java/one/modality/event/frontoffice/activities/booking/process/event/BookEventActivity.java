package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.panes.FlexPane;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.cloud.image.CloudImageService;
import dev.webfx.stack.cloud.image.impl.client.ClientImageService;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.event.frontoffice.activities.booking.WorkingBooking;
import one.modality.event.frontoffice.activities.booking.fx.FXEvent;
import one.modality.event.frontoffice.activities.booking.fx.FXEventAggregate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Bruno Salmon
 */
public final class BookEventActivity extends ViewDomainActivityBase {

    private Event currentEvent = FXEvent.getEvent();
    private Text eventTitle = new Text();
    private HtmlText eventDescription = new HtmlText();
    private Button checkoutButton;
    private final CloudImageService cloudImageService = new ClientImageService();
    private RecurringEventSchedule recurringEventSchedule = new RecurringEventSchedule();
    private VBox eventDetailVBox = new VBox();
    private VBox checkoutVBox = new VBox();
    private BorderPane containerBorderPane = new BorderPane();
    private BooleanProperty isOptionsSelectedEmptyProperty = new SimpleBooleanProperty();
    private WorkingBooking currentBooking = new WorkingBooking();


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
        line1.setPadding(new Insets(5,0,15,0));

        checkoutVBox.getChildren().add(line1);

        List<LocalDate> selectedDates = recurringEventSchedule.getSelectedDates();
        List<ScheduledItem> allScheduledItems = FXEventAggregate.getEventAggregate().getScheduledItems();

        // We fliter the scheduledItems to have only the one which date in the selectedDates list
        List<ScheduledItem> selectedScheduledItems = allScheduledItems.stream()
                .filter(item -> selectedDates.contains(item.getDate()))
                .collect(Collectors.toList());

        currentBooking.setScheduledItems(selectedScheduledItems);
        GridPane detailsGridPane = new GridPane();
        ColumnConstraints col1Constraints = new ColumnConstraints();
        col1Constraints.setMinWidth(300);
        col1Constraints.setMaxWidth(400);
        col1Constraints.setPrefWidth(300);

        ColumnConstraints col2Constraints = new ColumnConstraints();
        col2Constraints.setMinWidth(30);
        col2Constraints.setMaxWidth(50);
        col2Constraints.setPrefWidth(30);

        ColumnConstraints col3Constraints = new ColumnConstraints();
        col3Constraints.setMinWidth(10);
        col3Constraints.setMaxWidth(10);
        col3Constraints.setPrefWidth(10);

        detailsGridPane.getColumnConstraints().addAll(col1Constraints, col2Constraints,col3Constraints);

        detailsGridPane.add(I18n.bindI18nProperties( new Text(),"Summary"),0,0);
        detailsGridPane.add(I18n.bindI18nProperties( new Text(),"Price"),1,0);
        detailsGridPane.setAlignment(Pos.CENTER);

        for (int i = 0; i < currentBooking.getScheduledItems().size(); i++) {
            ScheduledItem currentScheduledItem = currentBooking.getScheduledItems().get(i);
            String dateFormatted = I18n.getI18nText("DateFormatted",I18n.getI18nText(currentScheduledItem.getDate().getMonth().name()),currentScheduledItem.getDate().getDayOfMonth());
            Text name = new Text(currentScheduledItem.getItem().getName() + " - "+ dateFormatted);
            Text price = new Text("7£");
            name.getStyleClass().add("subtitle-grey");
            price.getStyleClass().add("subtitle-grey");
            SVGPath trashOption = SvgIcons.createTrashSVGPath();
            trashOption.setFill(Color.RED);
            trashOption.setOnMouseClicked(event ->  {
                currentBooking.getScheduledItems().remove(currentScheduledItem);
                recurringEventSchedule.getSelectedDates().remove(currentScheduledItem.getDate());
            });
            detailsGridPane.add(name,0,i+1);
            detailsGridPane.add(price,1,i+1);
            detailsGridPane.add(trashOption,2,i+1);
        }

        detailsGridPane.setPadding(new Insets(0,0,40,0));
        checkoutVBox.getChildren().add(detailsGridPane);
        Button backButton = I18nControls.bindI18nProperties(new Button(), "Pay");
        //We manage the property of the button in css
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

    private void buildEventDetailVBox() {

        Text title = I18n.bindI18nProperties( new Text(),"GPEvent");
        title.getStyleClass().add("title-blue-bold");
        HBox line1 = new HBox(title);
        line1.setPadding(new Insets(0,0,5,0));
        line1.setAlignment(Pos.BASELINE_CENTER);

        eventTitle.getStyleClass().add("subtitle-grey");
        HBox line2 = new HBox(eventTitle);
        line2.setAlignment(Pos.BASELINE_CENTER);
        line2.setPadding(new Insets(5,0,15,0));

        final int maxWidth = 500;
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(maxWidth);
        Object imageTag = currentEvent.getId().getPrimaryKey();
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

        eventDescription.setPadding(new Insets(20,0,0,0));

        eventDescription.getStyleClass().add("description-text");
        currentEvent.onExpressionLoaded("name, description").onFailure(Console::log)
                .onSuccess(x -> Platform.runLater(()-> {
                    eventTitle.setText(currentEvent.getName());
                    eventDescription.setText(currentEvent.getDescription());
                }));

        Text scheduleText = I18n.bindI18nProperties( new Text(),"Schedule");
        scheduleText.getStyleClass().add("title-blue-bold");
        HBox line4 = new HBox(scheduleText);
        line4.setPadding(new Insets(20,0,10,0));
        line4.setAlignment(Pos.BASELINE_CENTER);

        Text selectTheCourseText = I18n.bindI18nProperties( new Text(),"SelectTheEvent");
        HBox line5 = new HBox(selectTheCourseText);
        line5.setPadding(new Insets(0,0,5,0));
        line5.setAlignment(Pos.BASELINE_CENTER);

        FlexPane dateFlexPane = recurringEventSchedule.buildUi();
        dateFlexPane.setPadding(new Insets(20,0,5,0));

        Hyperlink selectAllClassesHyperlink = I18nControls.bindI18nTextProperty(new Hyperlink(),"SelectAllClasses");
        selectAllClassesHyperlink.setAlignment(Pos.BASELINE_CENTER);
        selectAllClassesHyperlink.getStyleClass().add("subtitle-blue");
        selectAllClassesHyperlink.setOnAction((event ->recurringEventSchedule.selectAllDates()));

        //TODO: retrieve the price, for now we harcode it
        Text priceText = new Text(I18n.getI18nText( "PricePerClass",7,"£"));
        HBox line6 = new HBox(priceText);
        priceText.getStyleClass().add("subtitle-grey");
        line6.setPadding(new Insets(20,0,5,0));
        line6.setAlignment(Pos.BASELINE_CENTER);

        //TODO: retrieve the discount, for now we harcode it
        Text priceForAllClassesText = new Text(I18n.getI18nText( "DiscountForAllSeries",15));
        HBox line7 = new HBox(priceForAllClassesText);
        priceForAllClassesText.getStyleClass().add("subtitle-grey");
        line7.setAlignment(Pos.BASELINE_CENTER);

        line7.setPadding(new Insets(0,0,20,0));
        checkoutButton = I18nControls.bindI18nProperties(new Button(), "ProceedCheckout");
        //We manage the property of the button in css
        checkoutButton.getStyleClass().addAll("green-button");
        checkoutButton.setMaxWidth(300);
        checkoutButton.setOnAction((event -> {
            computeCheckoutVBox();
        }));
        eventDetailVBox.getChildren().setAll(line1, line2,imageView,eventDescription,line4,line5,recurringEventSchedule.buildUi(),selectAllClassesHyperlink,line6,line7,checkoutButton);
        eventDetailVBox.setPadding(new Insets(30,50,20,50));
        eventDetailVBox.setMaxWidth(maxWidth);
        eventDetailVBox.setAlignment(Pos.TOP_CENTER);

        checkoutButton.disableProperty().bind(isOptionsSelectedEmptyProperty);

    }


    @Override
    protected void updateContextParametersFromRoute() {
        super.updateContextParametersFromRoute();
    }

    @Override
    protected void updateModelFromContextParameters() {
        super.updateModelFromContextParameters();
    }

    @Override
    protected void startLogic() {
        FXEventAggregate.getEventAggregate().load()
                .onFailure(Console::log)
                .onSuccess(ignored -> {
                    recurringEventSchedule.setScheduledItems(FXEventAggregate.getEventAggregate().getScheduledItems());
                    //We add a listener on the date to update the BooleanProperty binded with the disable property of the checkout button
                    recurringEventSchedule.getSelectedDates().addListener((ListChangeListener<LocalDate>) change -> {
                        isOptionsSelectedEmptyProperty.set(recurringEventSchedule.getSelectedDates().isEmpty());
                    });

                    isOptionsSelectedEmptyProperty.set(recurringEventSchedule.getSelectedDates().isEmpty());


                });
    }
}
