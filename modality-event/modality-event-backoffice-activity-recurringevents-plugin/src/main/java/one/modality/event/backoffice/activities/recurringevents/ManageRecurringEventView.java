package one.modality.event.backoffice.activities.recurringevents;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.filepicker.FilePicker;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.theme.shape.ShapeTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.visual.VisualSelection;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.extras.webtext.HtmlTextEditor;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.conf.ConfigLoader;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.file.File;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.cloud.image.CloudImageService;
import dev.webfx.stack.cloud.image.impl.cloudinary.Cloudinary;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import one.modality.base.client.validation.ModalityValidationSupport;
import one.modality.base.shared.entities.*;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static dev.webfx.extras.webtext.HtmlTextEditor.Mode.STANDARD;

public final class ManageRecurringEventView {
    private final VisualGrid eventTable = new VisualGrid();
    private final VisualGrid siteTable = new VisualGrid();
    protected BorderPane mainFrame;
    protected Label title;
    private final GridPane eventDetailsPane = new GridPane();
    private final TextField nameOfEventTextField = new TextField();
    private final HtmlTextEditor descriptionHtmlEditor = new HtmlTextEditor();
    private final TextField timeOfTheEventTextField = new TextField();
    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
    private final EntityStore entityStore = EntityStore.create(dataSourceModel);
    private List<ScheduledItem> scheduledItemsReadFromDatabase = new ArrayList<>();
    private ObservableList<ScheduledItem> workingScheduledItems = FXCollections.observableArrayList();
    private final TextField durationTextField = new TextField();
    private final TextField bookingOpeningDateTextField = new TextField();
    private final TextField bookingOpeningTimeTextField = new TextField();
    private final TextField externalLinkTextFied = new TextField();
    private Label datesOfTheEventLabel;
    private Label titleEventDetailsLabel;
    private Label siteLabel;
    private  Button submitButton;
    private Button addButton;
    private Button saveDraftButton;
    private VBox leftPaneVBox;
    private VBox eventDetailsVBox;
    private HBox locationHBox;
    private ImageView imageView;
    private StackPane imageStackPane;
    //private Entity timeLine;
    private SVGPath trashImage;
    private  ListChangeListener onChangeDateListener;
    private EventCalendarPane calendarPane;
    private static final String EVENT_COLUMNS = "[" +
            "{expression: 'state', label: 'Status', renderer: 'eventStateRenderer'}," +
            "{expression: 'name', label: 'Name'}," +
            "{expression: 'type', label: 'TypeOfEvent'}," +
            "{expression: 'location', label: 'Location'}," +
            "{expression: 'dateIntervalFormat(startDate, endDate)', label: 'Dates'}," +
            "{expression: 'organization', label: 'Organisation'}," +
            "{expression: 'this', label: 'Action', renderer: 'editEventRenderer'}" +
            "]";

    static {
        ValueRendererRegistry.registerValueRenderer("eventStateRenderer", (value, context) -> {
            EventState state = EventState.of((String) value);
            return new Text(Strings.toString(state));
        });
        ValueRendererRegistry.registerValueRenderer("editEventRenderer", (value, context) -> {
            Event event = (Event) value;
            Hyperlink hyperlink = new Hyperlink("Edit");
            hyperlink.setOnAction(e -> System.out.println(event));
            return hyperlink;
        });
    }

    private Timeline eventTimeline;
    private Event currentEvent;
    private Event selectedEvent;
    private Site eventSite;
    //The Item recurring item is  needed when we create a ScheduledItem as one of his parameter
    private Item recurringItem;
    private UpdateStore updateStore = UpdateStore.createAbove(entityStore);
    final String TRASH_SVG_PATH = "M8 3.58537H12C12 3.042 11.7893 2.52088 11.4142 2.13666C11.0391 1.75244 10.5304 1.53659 10 1.53659C9.46957 1.53659 8.96086 1.75244 8.58579 2.13666C8.21071 2.52088 8 3.042 8 3.58537ZM6.5 3.58537C6.5 3.11453 6.59053 2.6483 6.76642 2.21331C6.94231 1.77831 7.20012 1.38306 7.52513 1.05013C7.85013 0.717197 8.23597 0.453101 8.66061 0.27292C9.08525 0.0927383 9.54037 0 10 0C10.4596 0 10.9148 0.0927383 11.3394 0.27292C11.764 0.453101 12.1499 0.717197 12.4749 1.05013C12.7999 1.38306 13.0577 1.77831 13.2336 2.21331C13.4095 2.6483 13.5 3.11453 13.5 3.58537H19.25C19.4489 3.58537 19.6397 3.66631 19.7803 3.81039C19.921 3.95448 20 4.14989 20 4.35366C20 4.55742 19.921 4.75284 19.7803 4.89692C19.6397 5.04101 19.4489 5.12195 19.25 5.12195H17.93L16.76 17.5283C16.6702 18.479 16.238 19.3612 15.5477 20.0031C14.8573 20.645 13.9583 21.0004 13.026 21H6.974C6.04186 21.0001 5.1431 20.6446 4.45295 20.0027C3.7628 19.3609 3.33073 18.4788 3.241 17.5283L2.07 5.12195H0.75C0.551088 5.12195 0.360322 5.04101 0.21967 4.89692C0.0790175 4.75284 0 4.55742 0 4.35366C0 4.14989 0.0790175 3.95448 0.21967 3.81039C0.360322 3.66631 0.551088 3.58537 0.75 3.58537H6.5ZM8.5 8.45122C8.5 8.24746 8.42098 8.05204 8.28033 7.90795C8.13968 7.76387 7.94891 7.68293 7.75 7.68293C7.55109 7.68293 7.36032 7.76387 7.21967 7.90795C7.07902 8.05204 7 8.24746 7 8.45122V16.1341C7 16.3379 7.07902 16.5333 7.21967 16.6774C7.36032 16.8215 7.55109 16.9024 7.75 16.9024C7.94891 16.9024 8.13968 16.8215 8.28033 16.6774C8.42098 16.5333 8.5 16.3379 8.5 16.1341V8.45122ZM12.25 7.68293C12.0511 7.68293 11.8603 7.76387 11.7197 7.90795C11.579 8.05204 11.5 8.24746 11.5 8.45122V16.1341C11.5 16.3379 11.579 16.5333 11.7197 16.6774C11.8603 16.8215 12.0511 16.9024 12.25 16.9024C12.4489 16.9024 12.6397 16.8215 12.7803 16.6774C12.921 16.5333 13 16.3379 13 16.1341V8.45122C13 8.24746 12.921 8.05204 12.7803 7.90795C12.6397 7.76387 12.4489 7.68293 12.25 7.68293Z";
    protected final ModalityValidationSupport validationSupport = new ModalityValidationSupport();
    private boolean validationSupportInitialised = false;
    private boolean areDataInitialised = false;
    private BooleanProperty isWorkingScheduledItemEmpty = new SimpleBooleanProperty(true);
    private BooleanProperty isPictureDisplayed = new SimpleBooleanProperty(false);

    private EventState previousEventState;

    private static final int EDITMODE = 1;
    private static final int ADDMODE = -1;
    private IntegerProperty currentMode = new SimpleIntegerProperty() {
        @Override
        protected void invalidated() {
            boolean isShowing = get() == ADDMODE;
            locationHBox.setVisible(isShowing);
            locationHBox.setManaged(isShowing);
        }
    };
    private EntityButtonSelector<Site> siteSelector;
    private final ButtonFactoryMixin mixin;
    private CloudImageService cloudinary;
    private Font fontUsed;
    private File pictureToUpload;
    private BooleanProperty isPictureToBeDeleted = new SimpleBooleanProperty(false);
    private BooleanProperty isPictureToBeUploaded = new SimpleBooleanProperty(false);;
    private BooleanBinding updateStoreOrPictureHasChanged;
    private BooleanBinding updateTrashButtonOnPictureDisplayed;


    public ManageRecurringEventView(ButtonFactoryMixin mixin) {
        this.mixin = mixin;
    }

    /**
     * This method is used to initialise the parameters for the form validation
     */
    protected void initFormValidation() {
        if(!validationSupportInitialised) {
            validationSupport.addRequiredInput(nameOfEventTextField);
            validationSupport.addValidationRule(FXProperties.compute(timeOfTheEventTextField.textProperty(), s1 -> {
                return isLocalTimeTextValid(timeOfTheEventTextField.getText());
            }), timeOfTheEventTextField, I18n.getI18nText("ValidationTimeFormatIncorrect"));
            validationSupport.addValidationRule(FXProperties.compute(durationTextField.textProperty(), s -> {
                return isIntegerValid(durationTextField.getText());}), durationTextField, I18n.getI18nText("ValidationDurationIncorrect"));
            validationSupport.addValidationRule(isWorkingScheduledItemEmpty.not(),datesOfTheEventLabel,I18n.getI18nText("ValidationSelectOneDate"));
            validationSupport.addValidationRule(FXProperties.compute(externalLinkTextFied.textProperty(), s1 -> {
                return isValidUrl(externalLinkTextFied.getText());
            }), externalLinkTextFied, I18n.getI18nText("ValidationUrlIncorrect"));
            validationSupportInitialised = true;
        }
    }

    /**
     * This method is used to initialise the Logic
     */
    public void startLogic()
    {
        ReactiveVisualMapper.<Event>createPushReactiveChain(mixin)
                .always("{class: 'Event', alias: 'e', fields: 'name, openingDate, description, type.recurringItem,(select site.name from Timeline where event=e limit 1) as location'}")
                .always(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=?", o))
                .always(DqlStatement.where("type.recurringItem!=null and kbs3"))
                .setEntityColumns(EVENT_COLUMNS)
                .setVisualSelectionProperty(eventTable.visualSelectionProperty())
                .setSelectedEntityHandler(this::displayEventDetails)
                .visualizeResultInto(eventTable.visualResultProperty())
                .start();

        ConfigLoader.onConfigLoaded("modality.cloudinary", config ->
            cloudinary = new Cloudinary(config.getString("cloudName"), config.getString("apiKey"), config.getString("apiSecret"))
        );
        /*
        We create a booleanBinding that will be used by the submit and draft button if either the updateStoreChanged, or the pictures needs to be uploaded
        or deleted
         */
        updateStoreOrPictureHasChanged = new BooleanBinding() {
        {
            super.bind(updateStore.hasChangesProperty(),isPictureToBeUploaded,isPictureToBeDeleted);
        }
        @Override
        protected boolean computeValue() {
            return updateStore.hasChangesProperty().getValue() || isPictureToBeUploaded.getValue() || isPictureToBeDeleted.getValue();
        }
    };
         /*
        We create a booleanBinding that will be used by the trash button. We display it only if a picture is displayed if either the updateStoreChanged, or the pictures needs to be uploaded
        or deleted
         */
        updateTrashButtonOnPictureDisplayed = new BooleanBinding() {
            {
                super.bind(isPictureDisplayed);
            }
            @Override
            protected boolean computeValue() {
                return isPictureDisplayed.getValue();
            }
        };

    }

    /**
     * This method is called when we select an event, it takes the info in the database
     * and initialise the class variable.
     * @param e the Event from who we want the data
     */
    private void displayEventDetails(Event e)
    {
        eventDetailsVBox.setVisible(true);
        eventDetailsVBox.setManaged(true);
        eventTable.setVisualSelection(null);
        selectedEvent = e;
        previousEventState = e.getState();
        currentMode.set(EDITMODE);
        //First we remove the listener on the List of selected date, because we'll initialise the dates here.
        //We'll add the listener after the initialisation
        calendarPane.getDatesPicker().getSelectedDates().removeListener(onChangeDateListener);
        //We unbind the submit button with the updateStore before loading the date
        submitButton.disableProperty().unbind();
//
        resetUpdateStoreAndOtherComponents();
        //We execute the query in batch, otherwise we can have synchronisation problem between the different threads
        entityStore.executeQueryBatch(
                        new EntityStoreQuery("select item,date,startTime, site, endTime from ScheduledItem where event=?", new Object[] { e.getId() } ),
                        new EntityStoreQuery("select startTime, endTime, site, event.(openingDate, type.recurringItem) from Timeline where event=?", new Object[] { e.getId() })
                        )
                .onFailure(Console::log)
                .onSuccess(queryLists -> Platform.runLater(() -> {
                            EntityList<ScheduledItem> scheduledItemList = queryLists[0];
                            EntityList<Timeline> timelineList = queryLists[1];
                                //we test if the selectedEvent==e, because, if a user click very fast from en event to another, there
                                //can be a sync pb between the result of the request from the databse and the code executed
                                if (selectedEvent == e) {
                                    //We take the selected date from the database, and transform the result in a list of LocalDate, that we pass to the datePicker so
                                    //they appear selected in the calendar
                                    scheduledItemsReadFromDatabase = scheduledItemList;
                                    List<LocalDate> list = scheduledItemsReadFromDatabase.stream().map(scheduledItem -> scheduledItem.getDate()).collect(Collectors.toList());
                                    calendarPane.getDatesPicker().getSelectedDates().setAll(list);
                                    //We add the listener only when the date have been initialised
                                    calendarPane.getDatesPicker().getSelectedDates().addListener(onChangeDateListener);

                                    //We display on the calendar the month containing the first date of the recurring event
                                    if (list.size() > 0) {
                                        LocalDate oldestDate = Collections.min(list);
                                        calendarPane.getDatesPicker().focusOnMonth(YearMonth.of(oldestDate.getYear(), oldestDate.getMonthValue()));
                                    }

                                    //Then we get the timeline and event, there should be just one timeline per recurring event
                                    eventTimeline = timelineList.get(0);
                                    eventSite = eventTimeline.getSite();
                                    //We initialize the recurringItem, that will be needed as a parameter when creating new ScheduledItem
                                    recurringItem = eventTimeline.getEvent().getType().getRecurringItem();
                                    LocalTime startTime = eventTimeline.getStartTime();
                                    LocalTime endTime = eventTimeline.getEndTime();
                                    String duration = String.valueOf(startTime.until(endTime, ChronoUnit.MINUTES));
                                    durationTextField.setText(duration);
                                    timeOfTheEventTextField.setText(eventTimeline.getStartTime().toString());
                                    titleEventDetailsLabel.setText(I18n.getI18nText("EditEventInformation",e.getName()));
                                    //We read and format the opening date value
                                    if (e.getOpeningDate() != null) {
                                        LocalDateTime openingDate = e.getOpeningDate();
                                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                                        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                                        bookingOpeningDateTextField.setText(openingDate.format(dateFormatter));
                                        bookingOpeningTimeTextField.setText(openingDate.format(timeFormatter));
                                    }

                                    //We add the event and timeline to the updateStore, so they will be modified when changed
                                    currentEvent = updateStore.updateEntity(e);
                                    eventTimeline = updateStore.updateEntity(timelineList.get(0));
                                    initialiseDataFromDatabase();
                                    //and finally, we fill the UI with the values from the database
                                    nameOfEventTextField.setText(currentEvent.getName());
                                    descriptionHtmlEditor.setText(currentEvent.getDescription());
                                    //We try to load the image from cloudinary if it exists
                                    int imageTag = (short) currentEvent.getId().getPrimaryKey();
                                    isPictureForEventExistingOnMediaProvider(imageTag)
                                            .onFailure(ex -> {
                                                Console.log(ex);
                                                imageView.setImage(null);
                                            })
                                            .onSuccess(exists -> Platform.runLater(() -> {
                                                if (!exists) {
                                                    imageView.setImage(null);
                                                    isPictureDisplayed.setValue(false);
                                                }
                                                else {
                                                    String url = cloudinary.url(String.valueOf(imageTag), 200, -1);
                                                    Image imageToDisplay = new Image(url, true);
                                                    imageView.setImage(imageToDisplay);
                                                    isPictureDisplayed.setValue(true);
                                                }
                                            }));
                                }
                    submitButton.disableProperty().bind(updateStoreOrPictureHasChanged.not());
                    saveDraftButton.disableProperty().bind(updateStoreOrPictureHasChanged.not());
                    trashImage.visibleProperty().bind(updateTrashButtonOnPictureDisplayed);
                }));
        }


   /**
    * This method is used to reset the different components in this class
    */
    private void resetUpdateStoreAndOtherComponents()
    {
        isPictureToBeDeleted.setValue(false);
        isPictureToBeUploaded.setValue(false);
        pictureToUpload = null;
        areDataInitialised = false;
        validationSupportInitialised = false;
        calendarPane.getDatesPicker().getSelectedDates().clear();
        workingScheduledItems.clear();
        updateStore.cancelChanges();
        imageView.setImage(null);
        isPictureDisplayed.setValue(false);
    }
    /**
     * This method is used to reset the text fields
     */
    private void resetTextFields()
    {
        nameOfEventTextField.setText("");
        durationTextField.setText("");
        timeOfTheEventTextField.setText("");
        descriptionHtmlEditor.setText("");
        bookingOpeningDateTextField.setText("");
        bookingOpeningTimeTextField.setText("");
    }

    /**
     * This method is used to initialise the data from the database,
     * in particular we initialise the workingScheduledItems list, and the event site.
     */
    public void initialiseDataFromDatabase()
    {
        if(!areDataInitialised) {
            workingScheduledItems.setAll(scheduledItemsReadFromDatabase.stream().map(updateStore::updateEntity).collect(Collectors.toList()));
            sortWorkingScheduledItemsByDate();
            ScheduledItem si = null;
            if(workingScheduledItems.size()>0) si = workingScheduledItems.get(0);
            if(si!=null) eventSite = si.getSite();
            areDataInitialised = true;
        }
    }

    /**
     * We validate the form
     * @return true if all the validation is success, false otherwise
     */
    public boolean validateForm() {
        if (!validationSupportInitialised) {
            initFormValidation();
            validationSupportInitialised = true;
        }
        return validationSupport.isValid();
    }

    public void uploadPictureIfNecessary(int eventId)
    {
        if(isPictureToBeUploaded.getValue())
        {
            cloudinary.destroy(String.valueOf(eventId), true)
                    .onFailure(Console::log);
            cloudinary.upload(pictureToUpload,
                            String.valueOf(eventId),true)
                    .onFailure(Console::log)
                    .onSuccess(uploadResult -> {
                    });
        }
    }
    public void deletePictureIfNecessary(int eventId)
    {
        if(isPictureToBeDeleted.getValue()) {
            //We delete the pictures, and all the cached picture in cloudinary that can have been transformed, related
            //to this assets
            cloudinary.destroy(String.valueOf(eventId), true)
                    .onFailure(Console::log);
        }
    }

    private Future<Boolean> isPictureForEventExistingOnMediaProvider(int eventId) {
        return cloudinary.exists("" + eventId);
    }

    public BooleanProperty isWorkingScheduledItemEmptyProperty() {
        return isWorkingScheduledItemEmpty;
    }
    /**
     * The entry point of the class
     *
     * @return A scrollablePane that is the UI for this class
     */
    public Node buildContainer()
    {
        mainFrame = new BorderPane();
        //Displaying The title of the frame
        title = new Label();
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);
        TextTheme.createPrimaryTextFacet(title).style();
        title.getStyleClass().add("font-size-35px");


        fontUsed = title.getFont();
        I18nControls.bindI18nProperties(title,"EventTitle");
        BorderPane.setAlignment(title, Pos.CENTER);
        mainFrame.setPadding(new Insets(0,0,30,0));
        mainFrame.setTop(title);

        //Displaying the list of events
        Label currentEventLabel = new Label();
        I18nControls.bindI18nProperties(currentEventLabel,"ListEvents");
        currentEventLabel.setPadding(new Insets(0,0,20,0));
        TextTheme.createSecondaryTextFacet(currentEventLabel).style();
        currentEventLabel.getStyleClass().add("font-size-16px");

        //While waiting the fix on the visual grid for autoexpend
        eventTable.setMaxHeight(200);


        addButton = new Button();
        I18nControls.bindI18nProperties(addButton,"AddEventInformationButton");
        //We manage the property of the button in css
        addButton.setId("newEvent");
        addButton.getStyleClass().add("recurringEventButton");

        addButton.setOnAction((event -> {
            eventDetailsVBox.setVisible(true);
            eventDetailsVBox.setManaged(true);
            eventTable.setVisualSelection(VisualSelection.createRowsSelection(new int[0]));
            resetUpdateStoreAndOtherComponents();
            resetTextFields();
            currentEvent = updateStore.insertEntity(Event.class);
            entityStore.executeQuery("select recurringItem, organization from EventType where recurringItem!=null and organization=?", FXOrganization.getOrganization())
                    .onFailure(Console::log)
                    .onSuccess(e->Platform.runLater(()->{
                        EventType eventType = (EventType) e.get(0);
                        recurringItem = eventType.getRecurringItem();
                        eventTimeline = updateStore.insertEntity(Timeline.class);
                        eventTimeline.setEvent(currentEvent);
                        eventTimeline.setItem(recurringItem);
                        currentEvent.setOrganization(eventType.getOrganization());
                        currentEvent.setCorporation(1);
                        currentEvent.setType(eventType);
                        currentEvent.setKbs3(true);
                        eventTimeline.setSite(eventSite);
                        currentMode.set(ADDMODE);
                        titleEventDetailsLabel.setText(I18n.getI18nText("AddEventInformation"));
                        siteSelector = new EntityButtonSelector<Site>(
                                "{class: 'Site', alias: 's', where: 'event=null', orderBy :'name'}",
                                mixin, eventDetailsVBox, dataSourceModel
                        ) { // Overriding the button content to add the possibility of Adding a new siteprefix text
                            private final BorderPane bp = new BorderPane();
                            TextField searchTextField = super.getSearchTextField();
                            private UpdateStore updateStoreForSite = UpdateStore.create(DataSourceModelService.getDefaultDataSourceModel());

                            Text addSiteText = new Text(I18n.getI18nText("AddNewLocation"));
                            MonoPane addSitePane = new MonoPane(addSiteText);
                            {
                                addSiteText.setFill(Color.BLUE);
                                addSitePane.setMaxWidth(Double.MAX_VALUE);
                                addSitePane.setMinHeight(20);
                                addSitePane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
                                addSitePane.setCursor(Cursor.HAND);
                                addSitePane.setOnMousePressed(event -> {
                                    Site site = updateStoreForSite.insertEntity(Site.class);
                                    site.setName(searchTextField.getText());
                                    site.setForeignField("organization",FXOrganization.getOrganization());
                                    site.setFieldValue("asksForPassport",false);
                                    site.setFieldValue("online",false);
                                    site.setFieldValue("hideDates",true);
                                    site.setFieldValue("forceSoldout",false);
                                    site.setFieldValue("main",true);
                                    //TODO to change the hardCodedValue (3 is for teachings)
                                    site.setFieldValue("itemFamily",3);
                                    //We had in the database the site now (otherwise too complicated to manage with the actual components)
                                    updateStoreForSite.submitChanges().onSuccess((batch -> Platform.runLater(()-> {
                                                Object newSiteId = batch.getArray()[0].getGeneratedKeys()[0];
                                                Site newSite = updateStoreForSite.createEntity(Site.class, newSiteId);
                                                //The createEntity doesn't load the name, so we need to set it up manually
                                                newSite.setName(site.getName());
                                                setSelectedItem(newSite);
                                            }
                                    )));
                                });
                            }
                            protected Region getOrCreateDialogContent() {
                                bp.setBottom(super.getOrCreateDialogContent());
                                bp.setCenter(addSitePane);
                                return bp;
                            }
                            @Override
                            //We had a listener on the search textfield to purpose to add the input as a new site if not existing
                            protected Node getOrCreateButtonContentFromSelectedItem() {
                                ObservableList<Site> siteList = siteSelector.getObservableEntities();
                                searchTextField.textProperty().addListener(new ChangeListener<String>() {
                                    @Override
                                    public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                                        List<String> siteNames = siteList.stream()
                                                .map(Site::getName) // Utilisez la méthode getter pour 'name'
                                                .collect(Collectors.toList());
                                        if(searchTextField.getText()!=null && searchTextField.getText().length()>2 && !siteNames.contains(searchTextField.getText())) {
                                            System.out.println("We add : " + searchTextField.getText());
                                            addSitePane.setVisible(true);
                                            addSitePane.setManaged(true);
                                        }
                                        else {
                                            addSitePane.setVisible(false);
                                            addSitePane.setManaged(false);
                                        }
                                    }
                                });
                                return super.getOrCreateButtonContentFromSelectedItem();
                            }
                        }.always(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=?", o)).autoSelectFirstEntity();
                        siteSelector.selectedItemProperty().addListener(new InvalidationListener() {
                            public void invalidated(Observable observable) {
                                eventSite = siteSelector.getSelectedItem();
                                //We update the timeline and working scheduled item
                                eventTimeline.setSite(eventSite);
                                for(ScheduledItem si:workingScheduledItems)
                                {
                                    si.setSite(eventSite);
                                }
                            }
                        });

                        // Doing a bidirectional binding with FXOrganization
                        locationHBox.getChildren().setAll(siteLabel,siteSelector.getButton());
                    }));
        }));



        titleEventDetailsLabel = new Label();
        titleEventDetailsLabel.setPadding(new Insets(30,0,20,0));
        TextTheme.createSecondaryTextFacet(titleEventDetailsLabel).style();
        titleEventDetailsLabel.setText(I18n.getI18nText("EventDetailsTitle"));

        //-----------------------------------------------------
        // ----FlowPane---------------------------------------|
        //|--VBox--------------------||---------Vbox---------||
        //|                          ||                      ||
        //| Name of event ...        ||  Time of event ...   ||
        //| Description   ...        ||  Date ...            ||
        //| Picture ...              ||  Booking openi. ...  ||
        //|--------------------------||----------------------||
        //-----------------------------------------------------
        FlowPane eventDetailsPane = new FlowPane();
        eventDetailsPane.setVgap(20);
        eventDetailsPane.setHgap(50);
        leftPaneVBox = new VBox();
        VBox rightPaneVBox = new VBox();

        int LABEL_WIDTH = 150;
        HBox line1InLeftPanel = new HBox();
        Label nameOfEventLabel = new Label();
        nameOfEventLabel.setMinWidth(LABEL_WIDTH);
        I18nControls.bindI18nProperties(nameOfEventLabel,"NameOfTheEvent");
        I18nControls.bindI18nProperties(nameOfEventTextField,"NameOfTheEvent");
        /* Temporarily commented as not yet supported by WebFX
        nameOfEventTextField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.isContentChange() && change.getControlNewText().length() > 128) {
                return null;
            }
            return change;}));*/
        nameOfEventTextField.setMinWidth(500);
        nameOfEventTextField.textProperty().addListener((InvalidationListener) obs -> {
            if(currentEvent!=null) {
                currentEvent.setName(nameOfEventTextField.getText());
            }});
        line1InLeftPanel.getChildren().setAll(nameOfEventLabel,nameOfEventTextField);

        locationHBox = new HBox();
        siteLabel = new Label();
        siteLabel.setText(I18n.getI18nText("Location"));
        siteLabel.setMinWidth(LABEL_WIDTH);
        locationHBox.getChildren().setAll(siteLabel);
        locationHBox.setPadding(new Insets(20,0,0,0));


        HBox line3InLeftPanel = new HBox();
        Label descriptionLabel = new Label();
        descriptionLabel.setMinWidth(LABEL_WIDTH);
        I18nControls.bindI18nProperties(descriptionLabel,"Description");
        descriptionHtmlEditor.setMode(STANDARD);
        descriptionHtmlEditor.setMinHeight(500);
        descriptionHtmlEditor.setMinWidth(500);
        descriptionHtmlEditor.setPrefHeight(120);
        descriptionHtmlEditor.setPrefWidth(300);
        descriptionHtmlEditor.textProperty().addListener((InvalidationListener) obs -> {
            if(currentEvent!=null) {
            currentEvent.setDescription(descriptionHtmlEditor.getText());
        }});
        line3InLeftPanel.setPadding(new Insets(20,0,0,0));
        line3InLeftPanel.getChildren().setAll(descriptionLabel,descriptionHtmlEditor);

        HBox line4InLeftPanel = new HBox();
        line4InLeftPanel.setAlignment(Pos.CENTER_LEFT);
        Label uploadPictureLabel = new Label();
        uploadPictureLabel.setMinWidth(LABEL_WIDTH);
        I18nControls.bindI18nProperties(uploadPictureLabel,"UploadFileDescription");

        HtmlText uploadText = new HtmlText();
        uploadText.setText(I18n.getI18nText("UploadFileDescription"));
        VBox uploadTextVBox = new VBox();
        uploadTextVBox.getChildren().setAll(uploadText);
        uploadTextVBox.setAlignment(Pos.CENTER_LEFT);
        uploadTextVBox.setPadding(new Insets(0,50,0,0));

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
        filePicker.setGraphic(uploadButton);
        filePicker.getSelectedFiles().addListener((InvalidationListener) obs -> {
            ObservableList<File> fileList = filePicker.getSelectedFiles();
            pictureToUpload = fileList.get(0);
            Image imageToDisplay = new Image(pictureToUpload.getObjectURL().toString());
            isPictureToBeUploaded.setValue(true);
            imageView.setImage(imageToDisplay);
            imageToDisplay.getHeight();
            isPictureDisplayed.setValue(true);
        });

        Label uploadButtonDescription = new Label();
        I18nControls.bindI18nProperties(uploadButtonDescription,"SelectYourFile");
        uploadButtonDescription.getStyleClass().add("font-size-9px");

        TextTheme.createPrimaryTextFacet(uploadButtonDescription).style();

        VBox uploadButtonVBox = new VBox(filePicker.getView(),uploadButtonDescription);
        uploadButtonVBox.setAlignment(Pos.CENTER);
        uploadButtonVBox.setPadding(new Insets(0,30,0,0));

        VBox imageAndTrashVBox = new VBox();
        imageAndTrashVBox.setSpacing(2);
        imageStackPane = new StackPane();
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(200);
        imageStackPane.setMaxSize(200,200);
        imageStackPane.setMinHeight(100);
      //  imageStackPane.setPrefSize(200,210);
        imageStackPane.setAlignment(Pos.CENTER);
        Label emptyPictureLabel = new Label();
        I18nControls.bindI18nProperties(emptyPictureLabel,"NoPictureSelected");
        TextTheme.createSecondaryTextFacet(emptyPictureLabel).style();
        emptyPictureLabel.getStyleClass().add("font-size-9px");

        trashImage = new SVGPath();
        trashImage.setContent(TRASH_SVG_PATH);
        trashImage.setStrokeWidth(1);
        trashImage.setScaleX(0.7);
        trashImage.setScaleY(0.7);
        trashImage.setOnMouseClicked(event ->  {
            isPictureToBeDeleted.setValue(true);
            isPictureToBeUploaded.setValue(false);
            imageView.setImage(null);
            isPictureDisplayed.setValue(false);
        });
        ShapeTheme.createSecondaryShapeFacet(trashImage).style();
        imageStackPane.getChildren().setAll(imageView,emptyPictureLabel);
        StackPane.setAlignment(emptyPictureLabel, Pos.CENTER);
        StackPane.setAlignment(trashImage, Pos.BOTTOM_RIGHT);
        imageView.toFront();
        emptyPictureLabel.toBack();
        imageAndTrashVBox.getChildren().setAll(imageStackPane,trashImage);
        imageAndTrashVBox.setAlignment(Pos.TOP_RIGHT);
        line4InLeftPanel.getChildren().setAll(uploadTextVBox,uploadButtonVBox,imageAndTrashVBox);
        line4InLeftPanel.setPadding(new Insets(20, 0,0,0));


        leftPaneVBox.getChildren().setAll(line1InLeftPanel,locationHBox,line3InLeftPanel,line4InLeftPanel);

        //The right pane (VBox)
        Label timeOfEventLabel = new Label();
        timeOfEventLabel.setPadding(new Insets(0,50,0,0));
        I18nControls.bindI18nProperties(timeOfEventLabel,"TimeOfTheEvent");
        timeOfTheEventTextField.setMaxWidth(60);
        I18nControls.bindI18nProperties(timeOfTheEventTextField,"TimeOfTheEvent");
        //We initialise the listener
        timeOfTheEventTextField.textProperty().addListener((InvalidationListener) obs -> {
            if(isLocalTimeTextValid(timeOfTheEventTextField.getText())) {
                LocalTime startTime = LocalTime.parse(timeOfTheEventTextField.getText());
                eventTimeline.setStartTime(startTime);
                }
        });

        Label durationLabel = new Label();
        I18nControls.bindI18nProperties(durationLabel,"Duration");
        durationTextField.setMaxWidth(40);
        durationLabel.setPadding(new Insets(0,50,0,50));
        I18nControls.bindI18nProperties(durationTextField,"Duration");
        durationTextField.textProperty().addListener((InvalidationListener) obs -> {
                    //Here, when we change the duration, we have to update all the workingScheduledItem list
                    // and the timeline (we need to calculate the endTime and update it)
                    if (isIntegerValid(durationTextField.getText())) {
                        eventTimeline.setEndTime(eventTimeline.getStartTime().plusMinutes(Integer.parseInt(durationTextField.getText())));
                        for (ScheduledItem schedItem : workingScheduledItems) {
                            if(schedItem.getStartTime()!=null)
                            schedItem.setEndTime(schedItem.getStartTime().plusMinutes(Integer.parseInt(durationTextField.getText())));
                        }
                    }
                }
        );

        HBox line1 = new HBox();
        line1.setAlignment(Pos.CENTER_LEFT);
        line1.getChildren().addAll(timeOfEventLabel,timeOfTheEventTextField,durationLabel,durationTextField);
        line1.setPadding(new Insets(0,0,20,0));
        datesOfTheEventLabel = new Label();
        I18nControls.bindI18nProperties(datesOfTheEventLabel,"Dates");
        datesOfTheEventLabel.setPadding(new Insets(0,0,5,0));
        calendarPane = new EventCalendarPane();
        calendarPane.getDatesPicker().getSelectedDates().addListener(onChangeDateListener);

//        HBox line3 = new HBox();
//        line3.setPadding(new Insets(20,0,0,0));
//        line3.setAlignment(Pos.CENTER_LEFT);
//        Label bookingTimeLabel = new Label();
//        bookingTimeLabel.setPadding(new Insets(0,140,0,0));
//        I18nControls.bindI18nProperties(bookingTimeLabel,"BookingAvailableAt");
//        I18nControls.bindI18nProperties(bookingOpeningDateTextField,"BookingAvailableAt");
//        bookingOpeningDateTextField.setMaxWidth(110);
//        bookingOpeningDateTextField.textProperty().addListener((InvalidationListener) obs -> {
//            try {
//                //TODO add the hour and minutes.
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//                String dateString = bookingOpeningDateTextField.getText();
//                LocalDateTime openingDate = LocalDateTime.parse(dateString,formatter);
//                currentEvent.setOpeningDate(openingDate);
//            }
//            catch (DateTimeParseException e)
//            {
//                //If we can't parse the date time, we do nothing
//                System.out.println("Error while parsing" + e.toString());
//            }
//        });
//
//        bookingOpeningTimeTextField.setMaxWidth(80);
//        I18nControls.bindI18nProperties(bookingOpeningTimeTextField,"BookingOpeningTime");
//        line3.setSpacing(20);
//
//        line3.getChildren().addAll(bookingTimeLabel,bookingOpeningDateTextField,bookingOpeningTimeTextField);

        HBox line4 = new HBox();
        line4.setPadding(new Insets(20,0,0,0));
        line4.setAlignment(Pos.CENTER_LEFT);
        Label externalLinkLabel = new Label();
        externalLinkLabel.setPadding(new Insets(0,20,0,0));
        I18nControls.bindI18nProperties(externalLinkLabel,"ExternalLink");
        I18nControls.bindI18nProperties(externalLinkTextFied,"ExternalLink");
        externalLinkTextFied.setPrefWidth(400);
        externalLinkTextFied.textProperty().addListener((InvalidationListener) obs -> {
                currentEvent.setExternalLink(externalLinkTextFied.getText());
        });
        line4.getChildren().addAll(externalLinkLabel,externalLinkTextFied);

        HBox line5 = new HBox();
        line5.setPadding(new Insets(50,0,0,0));
        line5.setAlignment(Pos.CENTER);
        Button discardButton = new Button();
        discardButton.getStyleClass().add("recurringEventButton");
        discardButton.getStyleClass().add("darkGreyButton");
        discardButton.getStyleClass().add("font-white");
        I18nControls.bindI18nProperties(discardButton,"DiscardButton");

        discardButton.setOnAction(event -> {
          eventDetailsVBox.setVisible(false);
          eventDetailsVBox.setManaged(false);
            eventTable.setVisualSelection(VisualSelection.createRowsSelection(new int[0]));
        });
        saveDraftButton = new Button();
        I18nControls.bindI18nProperties(saveDraftButton,"SaveDraftButton");
        saveDraftButton.setGraphicTextGap(10);
        saveDraftButton.getStyleClass().add("recurringEventButton");
        saveDraftButton.getStyleClass().add("blueButton");
        saveDraftButton.getStyleClass().add("font-white");
        saveDraftButton.setOnAction(event -> {
            if(validateForm())
            {
                calendarPane.getDatesPicker().getSelectedDates().removeListener(onChangeDateListener);

                if(previousEventState==null) currentEvent.setState(EventState.DRAFT);
                if(previousEventState==EventState.OPEN) currentEvent.setState(EventState.ON_HOLD);
                updateStore.submitChanges()
                        .onFailure(Console::log)
                        .onSuccess(x -> Platform.runLater(()->{
                            deletePictureIfNecessary((short) currentEvent.getId().getPrimaryKey());
                            uploadPictureIfNecessary((short) currentEvent.getId().getPrimaryKey());
                            eventDetailsVBox.setVisible(false);
                            eventDetailsVBox.setManaged(false);
                        }));
            }
        });

        submitButton = new Button();
        I18nControls.bindI18nProperties(submitButton,"UpdateEventButton");
        submitButton.setGraphicTextGap(10);
        submitButton.getStyleClass().add("recurringEventButton");
        submitButton.getStyleClass().add("greenButton");
        submitButton.getStyleClass().add("font-white");

        submitButton.setOnAction(event -> {
            if(validateForm())
            {
                calendarPane.getDatesPicker().getSelectedDates().removeListener(onChangeDateListener);
                currentEvent.setState(EventState.OPEN);
                updateStore.submitChanges()
                                .onFailure(Console::log)
                                        .onSuccess(x -> Platform.runLater(()->{
                                            deletePictureIfNecessary((short) currentEvent.getId().getPrimaryKey());
                                            uploadPictureIfNecessary((short) currentEvent.getId().getPrimaryKey());
                                            eventDetailsVBox.setVisible(false);
                                            eventDetailsVBox.setManaged(false);
            }));
            }
        });

        line5.setSpacing(30);
        line5.getChildren().addAll(discardButton,saveDraftButton,submitButton);

        HBox line6 = new HBox();
        line6.setPadding(new Insets(10,0,0,0));
        Button deleteButton = new Button();
        I18nControls.bindI18nProperties(deleteButton,"Delete");
        deleteButton.setId("Delete");
        deleteButton.setGraphicTextGap(10);
        deleteButton.getStyleClass().add("recurringEventButton");
        deleteButton.setOnAction(event -> {
                updateStore.cancelChanges();
                scheduledItemsReadFromDatabase.forEach(element -> updateStore.deleteEntity(element));
                updateStore.deleteEntity(currentEvent);
                updateStore.submitChanges();
                });

        Button advertiseButton = new Button();
        I18nControls.bindI18nProperties(advertiseButton,"Advertise");
        advertiseButton.setId("Advertise");
        advertiseButton.setGraphicTextGap(10);
        advertiseButton.getStyleClass().add("greenButton");
        advertiseButton.setOnAction(event -> {
            updateStore.cancelChanges();
            updateStore.updateEntity(currentEvent);
            currentEvent.setFieldValue("active",true);
            updateStore.submitChanges();
        });


        line6.setSpacing(30);
        line6.getChildren().addAll(deleteButton,advertiseButton);
        rightPaneVBox.getChildren().setAll(line1,datesOfTheEventLabel,calendarPane,line4InLeftPanel,line4,line5,line6);
        eventDetailsPane.getChildren().setAll(leftPaneVBox,rightPaneVBox);
        HBox labelLine = new HBox();
        labelLine.setAlignment(Pos.TOP_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.SOMETIMES);
        labelLine.getChildren().setAll(currentEventLabel,spacer, addButton);
        eventDetailsVBox = new VBox(titleEventDetailsLabel,eventDetailsPane);
        //When we launch the window, we don't disply this VBox wich contains an event details
        eventDetailsVBox.setVisible(false);
        eventDetailsVBox.setManaged(false);
        VBox mainVBox = new VBox(labelLine,eventTable, eventDetailsVBox);
        eventTable.setFullHeight(true);
        mainFrame.setCenter(mainVBox);

        initFormValidation();
        ScrollPane scrollPane = new ScrollPane(mainFrame);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));
        return scrollPane;
    }




    public List<ScheduledItem> getScheduledItemsReadFromDatabase() {
        return scheduledItemsReadFromDatabase;
    }
    public void setScheduledItemsReadFromDatabase(List<ScheduledItem> scheduledItems) {
        this.scheduledItemsReadFromDatabase = scheduledItems;
    }

    /**
     * This method is used to sort the list workingScheduledItems by Date
     */
    private void sortWorkingScheduledItemsByDate()
    {
        Collections.sort(workingScheduledItems, new Comparator<ScheduledItem>() {
            @Override
            public int compare(ScheduledItem item1, ScheduledItem item2) {
                return item1.getDate().compareTo(item2.getDate());
            }
        });
    }
    /**
     * This private class is used to display the calendar
     * containing the datePicker, the separator vertical line, and the list of start time
     */
    private class EventCalendarPane extends Pane
    {
        Label daySelected = new Label();
        Label selectEachDayLabel = new Label();
        Line verticalLine = new Line();
        VBox recurringEventsVBox = new VBox();
        ScrollPane recurringEventsScrollPane = new ScrollPane();
        DatesPicker datesPicker = new DatesPicker(YearMonth.now());

        public EventCalendarPane() {
            TextTheme.createSecondaryTextFacet(selectEachDayLabel).style();
            I18nControls.bindI18nProperties(selectEachDayLabel,"SelectTheDays");
            daySelected = new Label();
            I18nControls.bindI18nProperties(daySelected,"DaysSelected");
            TextTheme.createSecondaryTextFacet(daySelected).style();
            workingScheduledItems.addListener(new ListChangeListener<ScheduledItem>() {
                @Override
                public void onChanged(Change<? extends ScheduledItem> change) {
                    recurringEventsVBox.getChildren().clear();
                    isWorkingScheduledItemEmpty.set(workingScheduledItems.isEmpty());
                    List<LocalDate> dates = workingScheduledItems.stream().map(scheduledItem -> scheduledItem.getDate()).collect(Collectors.toList());
                    if(isWorkingScheduledItemEmpty.not().getValue()) {
                        currentEvent.setStartDate(Collections.min(dates));
                        currentEvent.setEndDate(Collections.max(dates));
                    }
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

            onChangeDateListener = new ListChangeListener<LocalDate>() {
                @Override
                public void onChanged(Change<? extends LocalDate> change) {
                    while(change.next()) {
                        if (change.wasAdded()) {
                            LocalDate date = change.getAddedSubList().get(0);
                            //Here we haven't found it in the scheduledItemsReadFromDatabase, so we create it.
                            ScheduledItem scheduledItem = updateStore.insertEntity(ScheduledItem.class);
                            scheduledItem.setDate(date);
                            scheduledItem.setSite(eventSite);
                            scheduledItem.setEvent(currentEvent);
                            scheduledItem.setTimeLine(eventTimeline);
                            scheduledItem.setItem(recurringItem);
                            //scheduledItem.setSite(eventTimeline.getSite());
                            workingScheduledItems.add(scheduledItem);
                            sortWorkingScheduledItemsByDate();
                        }
                        if (change.wasRemoved()) {
                            //We remove from the updateStore and the ScheduledItem
                            LocalDate date = change.getRemoved().get(0);
                            for (ScheduledItem currentScheduledItem : workingScheduledItems) {
                                if (currentScheduledItem.getDate().equals(date)) {
                                    updateStore.deleteEntity(currentScheduledItem);
                                    workingScheduledItems.remove(currentScheduledItem);
                                    break;
                                }
                            }
                        }
                    }

                }
            };
            //We bind the workingScheduledItems and children of the recurringEventsVBox
            //so when the workingScheduledItems is updated, we call the drawScheduledItem method
            //the is used to display date and start time of a scheduledItm
            ObservableLists.bindConverted(recurringEventsVBox.getChildren(),workingScheduledItems,this::drawScheduledItem);
        }

            /**
             * This method is used to return a BorderPane that contains
             * UI elements of the scheduledItem, the Date is read Only,
             * the startTime is editable.
             */
            private BorderPane drawScheduledItem(ScheduledItem scheduledItem)
            {
                LocalDate currentDate = scheduledItem.getDate();
                SVGPath trashDate = new SVGPath();
                trashDate.setContent(TRASH_SVG_PATH);
                trashDate.setStrokeWidth(1);
                trashDate.setScaleX(0.7);
                trashDate.setScaleY(0.7);
                trashDate.setTranslateY(2);
                trashDate.setOnMouseClicked(event ->  {
                    datesPicker.getSelectedDates().remove(currentDate);
                });
                ShapeTheme.createSecondaryShapeFacet(trashDate).style();
                Text currentDateValue = new Text(currentDate.format(DateTimeFormatter.ofPattern("MMM dd")));
                TextField currentEventStartTime = new TextField();
                timeOfTheEventTextField.textProperty().addListener((InvalidationListener) obs -> {
                    if(isLocalTimeTextValid(timeOfTheEventTextField.getText()) && isIntegerValid(durationTextField.getText())) {
                        currentEventStartTime.setPromptText(timeOfTheEventTextField.getText());
                        LocalTime startTime = LocalTime.parse(timeOfTheEventTextField.getText());
                        int duration = Integer.valueOf(durationTextField.getText());
                        LocalTime endTime = startTime.plusMinutes(duration);
                        eventTimeline.setStartTime(startTime);
                        eventTimeline.setEndTime(endTime);
                    }
                });

                if(!timeOfTheEventTextField.getText().equals(""))
                    currentEventStartTime.setPromptText(timeOfTheEventTextField.getText());
                else currentEventStartTime.setPromptText(timeOfTheEventTextField.getPromptText());
                //We add a listener to update the value of the scheduled item when the textfield is changed
                currentEventStartTime.textProperty().addListener((InvalidationListener) obs -> {
                    if ((isLocalTimeTextValid(currentEventStartTime.getText())&&(isIntegerValid(durationTextField.getText())))) {
                        LocalTime startTime = LocalTime.parse(currentEventStartTime.getText());
                        scheduledItem.setStartTime(startTime);
                        scheduledItem.setEndTime(startTime.plusMinutes(Integer.parseInt(durationTextField.getText())));
                      }
                });

                currentEventStartTime.setAlignment(Pos.CENTER);
                currentEventStartTime.setMaxWidth(90);
                LocalTime time =  (LocalTime) scheduledItem.getStartTime();
                if(time!=null) currentEventStartTime.setText(time.format(DateTimeFormatter.ofPattern("HH:mm")));
                BorderPane currentLineBorderPane = new BorderPane();
                BorderPane.setMargin(currentDateValue, new Insets(0,20,0,10));
                currentLineBorderPane.setLeft(trashDate);
                currentLineBorderPane.setCenter(currentDateValue);
                currentLineBorderPane.setRight(currentEventStartTime);
                currentLineBorderPane.setPadding(new Insets(0,0,3,0));
                recurringEventsVBox.getChildren().add(currentLineBorderPane);
                return currentLineBorderPane;
            }


        public DatesPicker getDatesPicker() {
            return datesPicker;
        }

        public void setDatesPicker(DatesPicker datesPicker) {
            this.datesPicker = datesPicker;
        }


        /**
         * We override this method to display the Pane as we want
         */
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

    /**
     * Test if a string is a format that is ready to be converted in LocalTime by the method LocalTime.parse
     * @param text the string to be tested
     * @return true, is the string parameter can be converted in LocalTime, false otherwise
     */
    static protected boolean isLocalTimeTextValid(String text)
    {
        try {
            LocalTime.parse(text);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * We test here if a url is valid
     * @param url
     * @return true if the url is valid, false otherwise
     */
    public static boolean isValidUrl(String url) {
        if(url=="") return true;
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            // URL is not valid
            return false;
        }
    }
    /**
     * Test if a string is a format that is ready to be converted in an Integer by the method Integer.parseInt
     * @param text the string to be tested
     * @return true, is the string parameter can be converted in Integer, false otherwise
     */
    static protected boolean isIntegerValid(String text)
    {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

