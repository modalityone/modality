package one.modality.event.backoffice.activities.recurringevents;

import dev.webfx.extras.filepicker.FilePicker;
import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.shape.ShapeTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.*;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public final class ManageRecurringEventView {

    private final VisualGrid eventTable = new VisualGrid();
    protected BorderPane mainFrame;
    protected Label title;
    private Region mainFrameHeader;
    private Region mainFrameFooter;
    private Pane dialogArea;
    private Node profilePanel;
    private Insets breathingPadding;
    private GridPane eventDetailsPane = new GridPane();
    private TextField nameOfEventTextField = new TextField();
    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
    private final EntityStore entityStore = EntityStore.create(dataSourceModel);
    private List<ScheduledItem> scheduledItemsReadFromDatabase = new ArrayList<>();
    private ObservableList<ScheduledItem> workingScheduledItems = FXCollections.observableArrayList();
    //private Entity timeLine;
    private DatesPicker datesPicker;
    private EventCalendarPane calendarPane;
    private final static String OPERATION_COLUMNS = "[" +
            "{expression: 'name', label: 'Name'}," +
            "{expression: 'startDate', label: 'Start Date'}," +
            "{expression: 'endDate', label: 'End Date'}," +
            "{expression: 'organization', label: 'Organisation'}," +
            "{expression: 'feesBottomLabel', label:'Note'}" +
            "]";

    private Timeline timeline = new Timeline();

    private UpdateStore updateStore = UpdateStore.createAbove(entityStore);
    final String TRASH_SVG_PATH = "M8 3.58537H12C12 3.042 11.7893 2.52088 11.4142 2.13666C11.0391 1.75244 10.5304 1.53659 10 1.53659C9.46957 1.53659 8.96086 1.75244 8.58579 2.13666C8.21071 2.52088 8 3.042 8 3.58537ZM6.5 3.58537C6.5 3.11453 6.59053 2.6483 6.76642 2.21331C6.94231 1.77831 7.20012 1.38306 7.52513 1.05013C7.85013 0.717197 8.23597 0.453101 8.66061 0.27292C9.08525 0.0927383 9.54037 0 10 0C10.4596 0 10.9148 0.0927383 11.3394 0.27292C11.764 0.453101 12.1499 0.717197 12.4749 1.05013C12.7999 1.38306 13.0577 1.77831 13.2336 2.21331C13.4095 2.6483 13.5 3.11453 13.5 3.58537H19.25C19.4489 3.58537 19.6397 3.66631 19.7803 3.81039C19.921 3.95448 20 4.14989 20 4.35366C20 4.55742 19.921 4.75284 19.7803 4.89692C19.6397 5.04101 19.4489 5.12195 19.25 5.12195H17.93L16.76 17.5283C16.6702 18.479 16.238 19.3612 15.5477 20.0031C14.8573 20.645 13.9583 21.0004 13.026 21H6.974C6.04186 21.0001 5.1431 20.6446 4.45295 20.0027C3.7628 19.3609 3.33073 18.4788 3.241 17.5283L2.07 5.12195H0.75C0.551088 5.12195 0.360322 5.04101 0.21967 4.89692C0.0790175 4.75284 0 4.55742 0 4.35366C0 4.14989 0.0790175 3.95448 0.21967 3.81039C0.360322 3.66631 0.551088 3.58537 0.75 3.58537H6.5ZM8.5 8.45122C8.5 8.24746 8.42098 8.05204 8.28033 7.90795C8.13968 7.76387 7.94891 7.68293 7.75 7.68293C7.55109 7.68293 7.36032 7.76387 7.21967 7.90795C7.07902 8.05204 7 8.24746 7 8.45122V16.1341C7 16.3379 7.07902 16.5333 7.21967 16.6774C7.36032 16.8215 7.55109 16.9024 7.75 16.9024C7.94891 16.9024 8.13968 16.8215 8.28033 16.6774C8.42098 16.5333 8.5 16.3379 8.5 16.1341V8.45122ZM12.25 7.68293C12.0511 7.68293 11.8603 7.76387 11.7197 7.90795C11.579 8.05204 11.5 8.24746 11.5 8.45122V16.1341C11.5 16.3379 11.579 16.5333 11.7197 16.6774C11.8603 16.8215 12.0511 16.9024 12.25 16.9024C12.4489 16.9024 12.6397 16.8215 12.7803 16.6774C12.921 16.5333 13 16.3379 13 16.1341V8.45122C13 8.24746 12.921 8.05204 12.7803 7.90795C12.6397 7.76387 12.4489 7.68293 12.25 7.68293Z";

    public Node buildContainer(Object mixin)
    {
        mainFrame = new BorderPane();
      //  LuminanceTheme.createSecondaryPanelFacet(mainFrame).style();

        //Displaying The title of the frame
        title = new Label();
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);
        TextTheme.createPrimaryTextFacet(title).setRequestedFont(FontDef.font(32)).style();
        I18nControls.bindI18nProperties(title,"EventTitle");
        BorderPane.setAlignment(title, Pos.CENTER);
        mainFrame.setPadding(new Insets(0,0,30,0));
        mainFrame.setTop(title);

        //Displaying the list of events
        Label currentEventLabel = new Label();
        I18nControls.bindI18nProperties(currentEventLabel,"ListEvents");
        currentEventLabel.setPadding(new Insets(0,0,20,0));
        TextTheme.createSecondaryTextFacet(currentEventLabel).setRequestedFont(FontDef.font(16)).style();
        eventTable.setMinHeight(50);
        eventTable.setFullHeight(true);

        Label titleEventDetails = new Label();
        titleEventDetails.setPadding(new Insets(30,0,20,0));
        TextTheme.createSecondaryTextFacet(titleEventDetails).style();
        I18nControls.bindI18nProperties(titleEventDetails,"EventDetailsTitle");

        //-----------------------------------------------------
        // ----FlowPane---------------------------------------|
        //|--GridPane----------------||---------Vbox---------||
        //|                          ||                      ||
        //| Name of event ...        ||  Time of event ...   ||
        //| Description   ...        ||  Date ...            ||
        //| Picture ...              ||  Booking avail. ...  ||
        //| Picture ...              ||  Booking kbs ...     ||
        //| Price ...                ||  Booking link ...    ||
        //|--------------------------||----------------------||
        //-----------------------------------------------------
        FlowPane eventDetailsPane = new FlowPane();
        eventDetailsPane.setVgap(20);
        eventDetailsPane.setHgap(50);
        GridPane leftGridPane = new GridPane();
        VBox rightPaneVBox = new VBox();


        Label nameOfEventLabel = new Label();
        I18nControls.bindI18nProperties(nameOfEventLabel,"NameOfTheEvent");
        I18nControls.bindI18nProperties(nameOfEventTextField,"NameOfTheEvent");
        Label descriptionLabel = new Label();
        I18nControls.bindI18nProperties(descriptionLabel,"Description");
        GridPane.setValignment(descriptionLabel,VPos.TOP);
        TextArea descriptionTextArea = new TextArea();
        descriptionTextArea.setMinHeight(250);
        I18nControls.bindI18nProperties(descriptionTextArea,"Description");
        descriptionTextArea.setPrefHeight(120);
        descriptionTextArea.setPrefWidth(300);
        //ImageView imageView = new ImageView(file.getObjectURL());
        HBox uploadImageBox = new HBox();
        Button uploadButton = new Button();
        SVGPath uploadSVGPath = new SVGPath();
        uploadSVGPath.setContent("M14 24V7.7L8.8 12.9L6 10L16 0L26 10L23.2 12.9L18 7.7V24H14ZM4 32C2.9 32 1.958 31.608 1.174 30.824C0.390003 30.04 -0.00132994 29.0987 3.39559e-06 28V22H4V28H28V22H32V28C32 29.1 31.608 30.042 30.824 30.826C30.04 31.61 29.0987 32.0013 28 32H4Z");
        uploadSVGPath.setScaleX(0.5);
        uploadSVGPath.setScaleY(0.5);
        uploadSVGPath.setStrokeWidth(1);
        uploadButton.setBackground(Background.EMPTY);
        ShapeTheme.createPrimaryShapeFacet(uploadSVGPath).style();
        uploadButton.setGraphic(uploadSVGPath);
        FilePicker filePicker = FilePicker.create();
        filePicker.getSelectedFiles().addListener((InvalidationListener) obs -> {
            ObservableList<dev.webfx.platform.file.File> fileList = filePicker.getSelectedFiles();
        });
        HtmlText uploadText = new HtmlText();
        uploadText.setPadding(new Insets(0,10,0,0));
        uploadText.setText(I18n.getI18nText("UploadFileDescription"));
        Label uploadButtonDescription = new Label();
        I18nControls.bindI18nProperties(uploadButtonDescription,"SelectYourFile");
        uploadButtonDescription.setFont(new Font(10));
        TextTheme.createPrimaryTextFacet(uploadButtonDescription).style();
        uploadImageBox.setAlignment(Pos.BASELINE_CENTER);
        uploadImageBox.setMinHeight(100);
        uploadImageBox.getChildren().setAll(uploadText,filePicker.getView(),uploadButtonDescription);

        StackPane imageStackPane = new StackPane();
        imageStackPane.setBackground(Background.fill(Color.LIGHTGRAY));
        imageStackPane.setMaxSize(100,100);
        Label emptyPicture = new Label();
        I18nControls.bindI18nProperties(emptyPicture,"NoPictureSelected");
        TextTheme.createSecondaryTextFacet(emptyPicture).style();
        emptyPicture.setFont(new Font(9));
        SVGPath trash = new SVGPath();
        trash.setContent(TRASH_SVG_PATH);
        trash.setStrokeWidth(1);
        trash.setScaleX(0.7);
        trash.setScaleY(0.7);
        trash.setOnMouseClicked(event ->  {
        });
        ShapeTheme.createSecondaryShapeFacet(trash).style();
        imageStackPane.getChildren().setAll(emptyPicture,trash);
        StackPane.setAlignment(emptyPicture, Pos.CENTER);
        StackPane.setAlignment(trash, Pos.BOTTOM_RIGHT);

        filePicker.setGraphic(uploadButton);
        leftGridPane.setAlignment(Pos.TOP_LEFT);
        leftGridPane.setHgap(50);
        leftGridPane.setVgap(20);
        leftGridPane.add(nameOfEventLabel, 0, 0);
        leftGridPane.add(nameOfEventTextField, 1, 0);
        leftGridPane.add(descriptionLabel, 0, 1);
        leftGridPane.add(descriptionTextArea, 1, 1);
        leftGridPane.add(imageStackPane, 0, 2);
        GridPane.setHalignment(imageStackPane, HPos.CENTER);
        leftGridPane.add(uploadImageBox, 1, 2);

        //The right pane (VBox)
        Label timeOfEventLabel = new Label();
        timeOfEventLabel.setPadding(new Insets(0,50,0,0));
        I18nControls.bindI18nProperties(timeOfEventLabel,"TimeOfTheEvent");
        TextField timeOfTheEventTextField = new TextField();
        timeOfTheEventTextField.setMaxWidth(60);
        I18nControls.bindI18nProperties(timeOfTheEventTextField,"TimeOfTheEvent");
        Label durationLabel = new Label();
        I18nControls.bindI18nProperties(durationLabel,"Duration");
        TextField durationTextField = new TextField();
        durationTextField.setMaxWidth(60);
        durationLabel.setPadding(new Insets(0,50,0,50));
        I18nControls.bindI18nProperties(durationTextField,"Duration");
//        Label sameTimeForAllLabel = new Label();
//        I18nControls.bindI18nProperties(sameTimeForAllLabel,"SameTimeForAll");
//        sameTimeForAllLabel.setMaxWidth(100);
//        sameTimeForAllLabel.setPadding(new Insets(0,5,0,20));
//        Label editTimeEveryEventLabel = new Label();
//        editTimeEveryEventLabel.setPadding(new Insets(0,5,0,20));
//        I18nControls.bindI18nProperties(editTimeEveryEventLabel,"EditTimeEveryEvent");
        Label selectTheDayLabel = new Label();
 //       I18nControls.bindI18nProperties(selectTheDayLabel,"Select each day for this recurrence");
   //     Label daySelectedLabel = new Label();
//        CheckBox sameTimeForAllCheckBox = new CheckBox();
//        CheckBox editTimeForEveryEventCheckBox = new CheckBox();
        HBox line1 = new HBox();
        line1.setAlignment(Pos.BASELINE_CENTER);
        line1.getChildren().addAll(timeOfEventLabel,timeOfTheEventTextField,durationLabel,durationTextField);
        line1.setPadding(new Insets(0,0,20,0));
        Label datesOfTheEventLabel = new Label();
        I18nControls.bindI18nProperties(datesOfTheEventLabel,"Dates");
        datesOfTheEventLabel.setPadding(new Insets(0,0,5,0));
        calendarPane = new EventCalendarPane();
        rightPaneVBox.getChildren().setAll(line1,datesOfTheEventLabel,calendarPane);
        eventDetailsPane.getChildren().setAll(leftGridPane,rightPaneVBox);

        VBox eventDetailVBox = new VBox(currentEventLabel,eventTable,titleEventDetails,eventDetailsPane);
        mainFrame.setCenter(eventDetailVBox);



        return mainFrame;
    }
    public List<ScheduledItem> getScheduledItemsReadFromDatabase() {
        return scheduledItemsReadFromDatabase;
    }

    public void setScheduledItemsReadFromDatabase(List<ScheduledItem> scheduledItems) {
        this.scheduledItemsReadFromDatabase = scheduledItems;
    }
    public void startLogic(Object mixin)
    {
        ReactiveVisualMapper.<Event>createPushReactiveChain(mixin)
                .always("{class: 'Event', alias: 'e'}")
                .always(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=?", o))
                .always(DqlStatement.where("type.recurring and kbs3"))
                .setEntityColumns(OPERATION_COLUMNS)
                .setVisualSelectionProperty(eventTable.visualSelectionProperty())
                .setSelectedEntityHandler(e -> displayEventDetails(e))
                .visualizeResultInto(eventTable.visualResultProperty())
                .start();
    }

    private void displayEventDetails(Event e)
    {
        nameOfEventTextField.setText(e.getName());
        entityStore.<ScheduledItem>executeQuery("select item,date,startTime, endTime from ScheduledItem where timeline.site.event=?",e.getId())
                .onFailure(error -> Console.log("Error while reading ScheduledItem", error))
                .onSuccess(scheduledItemList -> Platform.runLater(() -> {
                    //We take the selected date from the database, and transform the result in a list of LocalDate, that we pass to the datePicker so
                    //they appear selected in the calendar
                    scheduledItemsReadFromDatabase = scheduledItemList;
                    List<LocalDate> list = scheduledItemsReadFromDatabase.stream().map(scheduledItem -> scheduledItem.getDate()).collect(Collectors.toList());
                    calendarPane.getDatesPicker().getSelectedDates().setAll(list);
                    //We display on the calendar the month containing the first date of the recurring event
                    LocalDate oldestDate = Collections.min(list);
                    calendarPane.getDatesPicker().refreshForMonth(YearMonth.of(oldestDate.getYear(), oldestDate.getMonthValue()));
                }));
    }

    /**
     * This private class is used to display the calendar
     */
    private class EventCalendarPane extends Pane
    {
        HBox line2 = new HBox();
        Label datesOfTheEventLabel = new Label();
        Label daySelected = new Label();
        Label selectEachDayLabel = new Label();
        Line verticalLine = new Line();
        VBox recurringEventsVBox = new VBox();
        ScrollPane recurringEventsScrollPane = new ScrollPane();

        public EventCalendarPane() {
            TextTheme.createSecondaryTextFacet(selectEachDayLabel).style();
            I18nControls.bindI18nProperties(selectEachDayLabel,"SelectTheDays");
            daySelected = new Label();
            I18nControls.bindI18nProperties(daySelected,"DaysSelected");
            TextTheme.createSecondaryTextFacet(daySelected).style();
            workingScheduledItems.addListener(new ListChangeListener<ScheduledItem>() {
                @Override
                public void onChanged(Change<? extends ScheduledItem> change) {updateDaysSelected(change);
                }
            });

            verticalLine = new Line();
            verticalLine.setStartY(0);
            verticalLine.setEndY(180);
            //ShapeTheme.createSecondaryShapeFacet(verticalLine).setFillProperty(verticalLine.strokeProperty()).style();
            verticalLine.setStroke(Color.LIGHTGRAY);
            this.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY,
                    BorderStrokeStyle.SOLID, new CornerRadii(10), BorderWidths.DEFAULT)));

            recurringEventsScrollPane.setContent(recurringEventsVBox);
            recurringEventsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
         //  recurringEventsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            recurringEventsScrollPane.setMaxHeight(180);
            getChildren().setAll(selectEachDayLabel,daySelected,verticalLine,datesPicker.getCalendarPane(),recurringEventsScrollPane);
            ObservableLists.bindConverted(workingScheduledItems,datesPicker.getSelectedDates(),this::convertLocalDateToScheduledItem);
            ObservableLists.bindConverted(recurringEventsVBox.getChildren(),workingScheduledItems,this::drawScheduledItem);
        }

        /**
         * The function that is called when the observable List Date is modified
         *
         * @param date
         * @return
         */
            public ScheduledItem convertLocalDateToScheduledItem(LocalDate date)
            {
                //First, we check the date, and if the scheduledItemsReadFromDatabase List already contain this date
                //and if so, we take the values from there
                ScheduledItem scheduledItem = null;
                for(ScheduledItem currentScheduledItem : scheduledItemsReadFromDatabase) {
                    if (currentScheduledItem.getDate().equals(date)) {
                        scheduledItem = updateStore.updateEntity(currentScheduledItem);
                        break;
                    }
                }
                if(scheduledItem==null)
                {
                    //Here we haven't found it in the scheduledItemsReadFromDatabase, so we create it.
                        scheduledItem = updateStore.insertEntity(ScheduledItem.class);
                        scheduledItem.setDate(date);
                }
                return scheduledItem;
            }

            public BorderPane drawScheduledItem(ScheduledItem scheduledItem)
            {
                LocalDate currentDate = scheduledItem.getDate();
                SVGPath trash = new SVGPath();
                trash.setContent(TRASH_SVG_PATH);
                trash.setStrokeWidth(1);
                trash.setScaleX(0.7);
                trash.setScaleY(0.7);
                trash.setTranslateY(2);
                trash.setOnMouseClicked(event ->  {
                    datesPicker.getSelectedDates().remove(currentDate);
                });
                ShapeTheme.createSecondaryShapeFacet(trash).style();
                Text currentDateValue = new Text(currentDate.format(DateTimeFormatter.ofPattern("MMM dd")));
                TextField currentEventStartTime = new TextField();
                currentEventStartTime.setPromptText("Hh-Mm");
                currentEventStartTime.setAlignment(Pos.CENTER);
                currentEventStartTime.setMaxWidth(90);
                LocalTime time =  (LocalTime) scheduledItem.getFieldValue("startTime");
                if(time!=null) currentEventStartTime.setText(time.format(DateTimeFormatter.ISO_TIME));
                BorderPane currentLineBorderPane = new BorderPane();
                BorderPane.setMargin(currentDateValue, new Insets(0,20,0,10));
                currentLineBorderPane.setLeft(trash);
                currentLineBorderPane.setCenter(currentDateValue);
                currentLineBorderPane.setRight(currentEventStartTime);
                recurringEventsVBox.getChildren().add(currentLineBorderPane);
                return currentLineBorderPane;
            }

        /**
         * The function that is called when we click on a specific date
         * @param change
         */
        public void updateDaysSelected(ListChangeListener.Change<? extends ScheduledItem> change)
        {
            recurringEventsVBox.getChildren().clear();
        }

        public DatesPicker getDatesPicker() {
            return datesPicker;
        }

        public void setDatesPicker(DatesPicker datesPicker) {
            this.datesPicker = datesPicker;
        }

        DatesPicker datesPicker = new DatesPicker(YearMonth.now());

        //The EventCalendarPane comprises different element
        protected void layoutChildren() {
            layoutInArea(selectEachDayLabel, 20, 0, 260, 30, 0, HPos.CENTER, VPos.CENTER);
            layoutInArea(daySelected, 280, 0, 250, 30, 0, HPos.CENTER, VPos.CENTER);
            layoutInArea(datesPicker.getCalendarPane(), 0, 20, 280, 500, 0, HPos.CENTER, VPos.CENTER);
            layoutInArea(verticalLine, 280, 35, 10, 250, 0, HPos.CENTER, VPos.TOP);
            layoutInArea(recurringEventsScrollPane, 300, 35, 200, 180, 0, HPos.CENTER, VPos.TOP);
        }
        protected double computePrefHeight(double width) {
            return super.computePrefHeight(width)+10;
        }


    }
};

