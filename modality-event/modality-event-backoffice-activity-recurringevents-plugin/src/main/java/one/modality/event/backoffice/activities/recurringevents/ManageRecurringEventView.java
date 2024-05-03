package one.modality.event.backoffice.activities.recurringevents;

import dev.webfx.extras.filepicker.FilePicker;
import dev.webfx.extras.panes.FlexPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.theme.shape.ShapeTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.extras.webtext.HtmlTextEditor;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.file.File;
import dev.webfx.stack.cloud.image.CloudImageService;
import dev.webfx.stack.cloud.image.impl.client.ClientImageService;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.masterslave.MasterSlaveLinker;
import dev.webfx.stack.orm.entity.controls.entity.masterslave.SlaveEditor;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.dialog.DialogUtil;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameDialogArea;
import one.modality.base.client.validation.ModalityValidationSupport;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.markers.EntityHasLocalDate;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.backoffice.event.fx.FXEvent;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static dev.webfx.extras.webtext.HtmlTextEditor.Mode.STANDARD;

/**
 *
 */
public final class ManageRecurringEventView {
    private final VisualGrid eventTable = new VisualGrid();
    private final TextField nameOfEventTextField = I18nControls.bindI18nProperties( new TextField(),"NameOfTheEvent");
    private final HtmlTextEditor descriptionHtmlEditor = new HtmlTextEditor();
    private final TextField timeOfTheEventTextField = I18nControls.bindI18nProperties(new TextField(),"TimeOfTheEvent");
    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
    private final EntityStore entityStore = EntityStore.create(dataSourceModel);
    private List<ScheduledItem> scheduledItemsReadFromDatabase = new ArrayList<>();
    private final ObservableList<ScheduledItem> workingScheduledItems = FXCollections.observableArrayList();
    private final TextField durationTextField = I18nControls.bindI18nProperties(new TextField(),"Duration");
    private final TextField bookingOpeningDateTextField = new TextField();
    private final TextField bookingOpeningTimeTextField = new TextField();
    private final TextField externalLinkTextField = I18nControls.bindI18nProperties(new TextField(),"ExternalLink");
    private Label datesOfTheEventLabel;
    private Label titleEventDetailsLabel;
    private Switch advertisedSwitch;
    private Switch registrationOpenSwitch;
    private Label siteLabel;
    private Button saveButton;
    private Button cancelButton;
    private Button deleteButton;
    private VBox eventDetailsVBox;
    private HBox locationHBox;
    private ImageView imageView;
    private SVGPath trashImage;
    private ListChangeListener<LocalDate> onChangeDateListener;
    private EventCalendarPane calendarPane;
    private static final String EVENT_COLUMNS = "[" +
            "{expression: 'state', label: 'Status', renderer: 'eventStateRenderer'}," +
            "{expression: 'advertised', label: 'Advertised'},"+
            "{expression: 'name', label: 'Name'}," +
            "{expression: 'type', label: 'TypeOfEvent'}," +
            "{expression: 'location', label: 'Location'}," +
            "{expression: 'dateIntervalFormat(startDate, endDate)', label: 'Dates'}" +
            "]";

    private Timeline eventTimeline;
    private Event currentEditedEvent;
    private Event currentSelectedEvent;
    private Event currentObservedEvent;
    private Site eventSite;
    private Item recurringItem;
    private final UpdateStore updateStore = UpdateStore.createAbove(entityStore);
    private final ModalityValidationSupport validationSupport = new ModalityValidationSupport();
    private boolean validationSupportInitialised = false;
    private final BooleanExpression isWorkingScheduledItemEmpty = ObservableLists.isEmpty(workingScheduledItems);
    private final BooleanProperty isPictureDisplayed = new SimpleBooleanProperty(false);
    private final BooleanProperty isEventDeletable = new SimpleBooleanProperty(true);
    private EventState previousEventState;
    private static final int EDIT_MODE = 1;
    private static final int ADD_MODE = -1;
    private final IntegerProperty currentMode = new SimpleIntegerProperty() {
        @Override
        protected void invalidated() {
            boolean isShowing = get() == ADD_MODE;
            isEventDeletable.setValue(!isShowing);
            locationHBox.setVisible(isShowing);
            locationHBox.setManaged(isShowing);
            cancelButton.setVisible(!isShowing);
        }
    };
    private EntityButtonSelector<Site> siteSelector;
    private final ButtonFactoryMixin mixin;
    private final CloudImageService cloudImageService = new ClientImageService();
    private File cloudPictureFileToUpload;
    private final BooleanProperty isCloudPictureToBeDeleted = new SimpleBooleanProperty(false);
    private final BooleanProperty isCloudPictureToBeUploaded = new SimpleBooleanProperty(false);
    private Object recentlyUploadedCloudPictureId;
    private BooleanBinding updateStoreOrPictureHasChanged;
    private BooleanBinding updateTrashButtonOnPictureDisplayed;
    private ReactiveVisualMapper<Event> eventVisualMapper;
    private boolean areWeDeleting = false;

    private final SlaveEditor<Event> eventDetailsSlaveEditor = new SlaveEditor<>() {
        @Override
        public void showChangeApprovalDialog(Runnable onApprovalCallback) {
            Text titleConfirmationText = I18n.bindI18nProperties(new Text(),"AreYouSure");
            titleConfirmationText.getStyleClass().add("font-green");
            titleConfirmationText.getStyleClass().add("font-size-22px");
            titleConfirmationText.getStyleClass().add("bold");
            BorderPane dialog = new BorderPane();
            dialog.setTop(titleConfirmationText);
            BorderPane.setAlignment(titleConfirmationText, Pos.CENTER);
            Text confirmationText = I18n.bindI18nProperties(new Text(),"CancelChangesConfirmation");
            dialog.setCenter(confirmationText);
            BorderPane.setAlignment(confirmationText, Pos.CENTER);
            BorderPane.setMargin(confirmationText, new Insets(30, 0, 30, 0));
            Button okButton = I18nControls.bindI18nProperties(new Button(),"Confirm");
            okButton.getStyleClass().addAll("recurringEventButton", "background-red", "font-white");
            Button cancelActionButton = I18nControls.bindI18nProperties(new Button(),"Cancel");
            cancelActionButton.getStyleClass().addAll("recurringEventButton", "background-darkGrey", "font-white");
            HBox buttonsHBox = new HBox(cancelActionButton, okButton);
            buttonsHBox.setAlignment(Pos.CENTER);
            buttonsHBox.setSpacing(30);
            dialog.setBottom(buttonsHBox);
            BorderPane.setAlignment(buttonsHBox, Pos.CENTER);
            DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
            okButton.setOnAction(l -> {
                dialogCallback.closeDialog();
                onApprovalCallback.run();
            });
            cancelActionButton.setOnAction(l -> dialogCallback.closeDialog());
        }

        /**
         * This method is called by the master controller when we change the event we're editing
         *
         * @param approvedEntity the approved Entity
         */
        @Override
        public void setSlave(Event approvedEntity) {
            displayEventDetails(approvedEntity);
            currentEditedEvent = approvedEntity;
        }

        @Override
        public Event getSlave() {
            return currentEditedEvent;
        }

        @Override
        public boolean hasChanges() {
            if (areWeDeleting) return false;
            return updateStore.hasChanges() || updateStoreOrPictureHasChanged.get();
        }
    };
    //This parameter will allow us to manage the interaction and behaviour of the Panel that display the details of an event and the event selected
    final private MasterSlaveLinker<Event> masterSlaveEventLinker = new MasterSlaveLinker<>(eventDetailsSlaveEditor);

    public ManageRecurringEventView(ButtonFactoryMixin mixin) {
        this.mixin = mixin;
    }

    /**
     * This method is used to initialise the parameters for the form validation
     */
    private void initFormValidation() {
        if(!validationSupportInitialised) {
            validationSupport.addRequiredInput(nameOfEventTextField);
            validationSupport.addValidationRule(FXProperties.compute(timeOfTheEventTextField.textProperty(), s1 -> isLocalTimeTextValid(timeOfTheEventTextField.getText())), timeOfTheEventTextField, I18n.getI18nText("ValidationTimeFormatIncorrect"));
            validationSupport.addValidationRule(FXProperties.compute(durationTextField.textProperty(), s -> isIntegerValid(durationTextField.getText())), durationTextField, I18n.getI18nText("ValidationDurationIncorrect"));
            validationSupport.addValidationRule(isWorkingScheduledItemEmpty.not(),datesOfTheEventLabel, I18n.getI18nText("ValidationSelectOneDate"));
            validationSupport.addValidationRule(FXProperties.compute(externalLinkTextField.textProperty(), s1 -> isValidUrl(externalLinkTextField.getText())), externalLinkTextField, I18n.getI18nText("ValidationUrlIncorrect"));
            validationSupportInitialised = true;
        }
    }

    /**
     * This method is used to initialise the Logic
     */
    public void startLogic()
    {
        EventRenderers.registerRenderers();

        eventVisualMapper = ReactiveVisualMapper.<Event>createPushReactiveChain(mixin)
                .always("{class: 'Event', alias: 'e', fields: 'name, openingDate, description, type.recurringItem, (select site.name from Timeline where event=e limit 1) as location'}")
                .always(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=?", o))
                .always(DqlStatement.where("type.recurringItem!=null and kbs3"))
                .setEntityColumns(EVENT_COLUMNS)
                .setStore(entityStore)
                .setVisualSelectionProperty(eventTable.visualSelectionProperty())
                .visualizeResultInto(eventTable.visualResultProperty())
                .start();

        /*
        We create a booleanBinding that will be used by the submit and draft button if either the updateStoreChanged, or the pictures needs to be uploaded
        or deleted
         */
        updateStoreOrPictureHasChanged = new BooleanBinding() {
        {
            super.bind(updateStore.hasChangesProperty(), isCloudPictureToBeUploaded, isCloudPictureToBeDeleted);
        }
        @Override
        protected boolean computeValue() {
            return updateStore.hasChanges() || isCloudPictureToBeUploaded.getValue() || isCloudPictureToBeDeleted.getValue();
        }
    };
         /* We create a booleanBinding that will be used by the trash button. We display it only if a picture is displayed if either the updateStoreChanged, or the pictures needs to be uploaded
        or deleted */
        updateTrashButtonOnPictureDisplayed = new BooleanBinding() {
            {
                super.bind(isPictureDisplayed);
            }
            @Override
            protected boolean computeValue() {
                return isPictureDisplayed.getValue();
            }
        };
        updateStore.hasChangesProperty().addListener(observable -> updateStore.hasChangesProperty().getValue());
        //Now we bind the different element (FXEvent, Visual Mapper, and MasterSlaveController)
        eventVisualMapper.requestedSelectedEntityProperty().bindBidirectional(FXEvent.eventProperty());
        masterSlaveEventLinker.masterEntityProperty().bindBidirectional(eventVisualMapper.selectedEntityProperty());
    }
    /**
     * This method is called when we select an event, it takes the info in the database
     * and initialise the class variable.
     * @param e the Event from who we want the data
     */
    private void displayEventDetails(Event e)
    {
        currentSelectedEvent = e;
        //Event e can be null if for example we select on the gantt graph an event that is not a recurring event
        if(e==null)
        {
            eventDetailsVBox.setVisible(false);
            eventDetailsVBox.setManaged(false);
            return;
        }
        //First we reset everything
        resetUpdateStoreAndOtherComponents();
        previousEventState = e.getState();
        eventDetailsVBox.setVisible(true);
        eventDetailsVBox.setManaged(true);

        currentMode.set(EDIT_MODE);
        //We execute the query in batch, otherwise we can have synchronisation problem between the different threads
        entityStore.executeQueryBatch(
                        new EntityStoreQuery ("select item,date,startTime, site, endTime, (select id from Attendance where scheduledItem=si limit 1) as attendance from ScheduledItem si where event=?", new Object[] { e}),
                        new EntityStoreQuery("select startTime, endTime, site, event.(openingDate, type.recurringItem, externalLink) from Timeline where event=?", new Object[] { e.getId() })
                        )
                .onFailure(Console::log)
                .onSuccess(queryLists -> Platform.runLater(() -> {
                    //First we remove the listener on the List of selected date, because we'll initialise the dates here. (the listener has been removed before, but because it's async,
                    //We'll add the listener after the initialisation
                    EntityList<ScheduledItem> scheduledItemList = queryLists[0];
                    EntityList<Timeline> timelineList = queryLists[1];
                    //we test if the selectedEvent==e, because, if a user click very fast from en event to another, there
                    //can be a sync pb between the result of the request from the database and the code executed
                    if (currentSelectedEvent == e) {
                        //We take the selected date from the database, and transform the result in a list of LocalDate, that we pass to the datePicker,
                        //so they appear selected in the calendar
                        scheduledItemsReadFromDatabase = scheduledItemList;
                        List<LocalDate> list = scheduledItemsReadFromDatabase.stream().map(EntityHasLocalDate::getDate).collect(Collectors.toList());

                        calendarPane.getDatesPicker().setDateColorGetter(localDate -> {
                            ScheduledItem scheduledItem = scheduledItemList.stream().filter(si->localDate.equals(si.getDate())).findFirst().orElse(null);
                            if(scheduledItem!=null && scheduledItem.getFieldValue("attendance")!=null) {
                                isEventDeletable.setValue(false);
                                return Color.LIGHTGRAY;
                            }
                            else {
                                return calendarPane.getDatesPicker().getSelectedDateColor();
                            }
                        });
                        calendarPane.getDatesPicker().getSelectedDates().setAll(list);
                        calendarPane.getDatesPicker().setOnDateClicked(localDate -> {
                            ScheduledItem scheduledItem = scheduledItemList.stream().filter(si->localDate.equals(si.getDate())).findFirst().orElse(null);
                            if(!(scheduledItem!=null && scheduledItem.getFieldValue("attendance")!=null)) {
                                calendarPane.getDatesPicker().processDateSelected(localDate);
                            }
                        });

                        //We display on the calendar the month containing the first date of the recurring event
                        if (!list.isEmpty()) {
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
                        I18nControls.bindI18nTextProperty(titleEventDetailsLabel, "EditEventInformation", e.getName());
                        //We read and format the opening date value
                        LocalDateTime openingDate = e.getOpeningDate();
                        if (openingDate != null) {
                            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                            bookingOpeningDateTextField.setText(openingDate.format(dateFormatter));
                            bookingOpeningTimeTextField.setText(openingDate.format(timeFormatter));
                        }

                        //We add the event and timeline to the updateStore, so they will be modified when changed
                        currentEditedEvent = updateStore.updateEntity(e);
                        eventTimeline = updateStore.updateEntity(timelineList.get(0));

                        workingScheduledItems.setAll(scheduledItemsReadFromDatabase.stream().map(updateStore::updateEntity).collect(Collectors.toList()));
                        sortWorkingScheduledItemsByDate();
                        ScheduledItem si = null;
                        if(!workingScheduledItems.isEmpty()) si = workingScheduledItems.get(0);
                        if(si!=null) eventSite = si.getSite();

                        //and finally, we fill the UI with the values from the database
                        nameOfEventTextField.setText(currentEditedEvent.getName());
                        descriptionHtmlEditor.setText(currentEditedEvent.getDescription());
                        externalLinkTextField.setText(currentEditedEvent.getExternalLink());
                        boolean isAdvertised;
                        if(currentEditedEvent.isAdvertised()==null) isAdvertised = false;
                        else isAdvertised = currentEditedEvent.isAdvertised();
                        advertisedSwitch.setSelected(isAdvertised);
                        registrationOpenSwitch.setSelected(currentEditedEvent.getState()==EventState.OPEN);
                        //We try to load the image from cloudinary if it exists
                        loadEventImageIfExists();
                    }
                    saveButton.disableProperty().bind(updateStoreOrPictureHasChanged.not());
                    cancelButton.disableProperty().bind(updateStoreOrPictureHasChanged.not());
                    trashImage.visibleProperty().bind(updateTrashButtonOnPictureDisplayed);
                    deleteButton.disableProperty().bind(isEventDeletable.not());
                    currentObservedEvent=currentEditedEvent;
                }));
        }

   /**
    * This method is used to reset the different components in this class
    */
    private void resetUpdateStoreAndOtherComponents() {
        validationSupport.reset();
        isEventDeletable.setValue(true);
        currentObservedEvent = null;
        isCloudPictureToBeDeleted.setValue(false);
        isCloudPictureToBeUploaded.setValue(false);
        cloudPictureFileToUpload = null;
        validationSupportInitialised = false;
        areWeDeleting = false;
        workingScheduledItems.clear();
        calendarPane.getDatesPicker().getSelectedDates().clear();
        calendarPane.getDatesPicker().setMonth(YearMonth.now());
        //We put the default behaviour on the datePicker, otherwise it won't reset the behaviour define in the previous event selected
        calendarPane.getDatesPicker().setOnDateClicked(localDate -> calendarPane.getDatesPicker().processDateSelected(localDate));
        calendarPane.getDatesPicker().setDateColorGetter(localDate -> calendarPane.getDatesPicker().getSelectedDateColor());
        calendarPane.getDatesPicker().initializeDaysSelected();
        updateStore.cancelChanges();
        imageView.setImage(null);
        isPictureDisplayed.setValue(false);
    }
    /**
     * This method is used to reset the text fields
     */
    private void resetTextFields() {
        nameOfEventTextField.setText("");
        durationTextField.setText("");
        timeOfTheEventTextField.setText("");
        descriptionHtmlEditor.setText("");
        bookingOpeningDateTextField.setText("");
        bookingOpeningTimeTextField.setText("");
    }

    private void loadEventImageIfExists() {
        Object imageTag = currentEditedEvent.getId().getPrimaryKey();
        doesCloudPictureExist(imageTag)
                .onFailure(ex -> {
                    Console.log(ex);
                    imageView.setImage(null);
                    isPictureDisplayed.setValue(false);
                })
                .onSuccess(exists -> Platform.runLater(() -> {
                    //Console.log("exists: " + exists);
                    if (!exists) {
                        imageView.setImage(null);
                        isPictureDisplayed.setValue(false);
                    }
                    else {
                        //First, we need to get the zoom factor of the screen
                        double zoomFactor = Screen.getPrimary().getOutputScaleX();
                        String url = cloudImageService.url(String.valueOf(imageTag), (int) (imageView.getFitWidth()*zoomFactor), -1);
                        Image imageToDisplay = new Image(url, true);
                        imageView.setImage(imageToDisplay);
                        isPictureDisplayed.setValue(true);
                    }
                }));
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

    public void uploadCloudPictureIfNecessary(Object eventId) {
        if(isCloudPictureToBeUploaded.getValue())
        {
            String pictureId = String.valueOf(eventId);
            cloudImageService.upload(cloudPictureFileToUpload, pictureId,true)
                    .onFailure(Console::log)
                    .onSuccess(ok -> {
                        isCloudPictureToBeUploaded.set(false);
                        recentlyUploadedCloudPictureId = pictureId;
                        loadEventImageIfExists();
                    });
        }
    }

    public void deleteCloudPictureIfNecessary(Object eventId) {
        if(isCloudPictureToBeDeleted.getValue()) {
            //We delete the pictures, and all the cached picture in cloudinary that can have been transformed, related
            //to this assets
            String pictureId = String.valueOf(eventId);
            cloudImageService.delete(pictureId, true)
                    .onFailure(Console::log)
                    .onSuccess(ok -> {
                        isCloudPictureToBeDeleted.set(false);
                        if (Objects.equals(pictureId, recentlyUploadedCloudPictureId))
                            recentlyUploadedCloudPictureId = null;
                    });
        }
    }

    private Future<Boolean> doesCloudPictureExist(Object eventId) {
        String pictureId = String.valueOf(eventId);
        if (Objects.equals(pictureId, recentlyUploadedCloudPictureId))
            return Future.succeededFuture(true);
        return cloudImageService.exists(pictureId);
    }


    /**
     * The entry point of the class
     *
     * @return A scrollablePane that is the UI for this class
     */
    public Node buildContainer() {
        BorderPane mainFrame = new BorderPane();
        //Displaying The title of the frame
        Label title = I18nControls.bindI18nProperties(new Label(), "EventTitle");
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);
        TextTheme.createPrimaryTextFacet(title).style();
        title.getStyleClass().add("font-size-35px");

        BorderPane.setAlignment(title, Pos.CENTER);
        mainFrame.setPadding(new Insets(0,0,30,0));
        //mainFrame.setTop( title);

        //Displaying the list of events
        Label currentEventLabel = I18nControls.bindI18nProperties(new Label(),"ListEvents");
        currentEventLabel.setPadding(new Insets(15,0,20,0));
        TextTheme.createSecondaryTextFacet(currentEventLabel).style();
        currentEventLabel.getStyleClass().add("font-size-16px");

        Button addButton = I18nControls.bindI18nProperties(new Button(), "AddEventInformationButton");
        //We manage the property of the button in css
        addButton.getStyleClass().addAll("recurringEventButton", "background-green", "font-white");

        addButton.setOnAction((event -> masterSlaveEventLinker.checkSlaveEntityChangeApproval(true, () -> {
            resetTextFields();
            resetUpdateStoreAndOtherComponents();
            eventDetailsVBox.setVisible(true);
            eventDetailsVBox.setManaged(true);
            currentEditedEvent = updateStore.insertEntity(Event.class);
            currentObservedEvent = currentEditedEvent;
            entityStore.executeQuery("select recurringItem, organization from EventType where recurringItem!=null and organization=?", FXOrganization.getOrganization())
                    .onFailure(Console::log)
                    .onSuccess(e->Platform.runLater(()->{
                        EventType eventType = (EventType) e.get(0);
                        recurringItem = eventType.getRecurringItem();
                        eventTimeline = updateStore.insertEntity(Timeline.class);
                        eventTimeline.setEvent(currentEditedEvent);
                        eventTimeline.setItem(recurringItem);
                        currentEditedEvent.setOrganization(eventType.getOrganization());
                        currentEditedEvent.setCorporation(1);
                        currentEditedEvent.setType(eventType);
                        currentEditedEvent.setKbs3(true);
                        eventTimeline.setSite(eventSite);
                        currentEditedEvent.setState(EventState.DRAFT);
                        currentEditedEvent.setAdvertised(false);
                        currentMode.set(ADD_MODE);
                        I18nControls.bindI18nTextProperty(titleEventDetailsLabel, "AddEventInformation");
                        siteSelector = new EntityButtonSelector<Site>(
                                "{class: 'Site', alias: 's', where: 'event=null', orderBy :'name'}",
                                mixin, eventDetailsVBox, dataSourceModel
                        ) { // Overriding the button content to add the possibility of Adding a new site prefix text
                            private final BorderPane bp = new BorderPane();
                            final TextField searchTextField = super.getSearchTextField();
                            private final UpdateStore updateStoreForSite = UpdateStore.create(DataSourceModelService.getDefaultDataSourceModel());

                            final Text addSiteText = I18n.bindI18nProperties(new Text(), "AddNewLocation");
                            final MonoPane addSitePane = new MonoPane(addSiteText);
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
                            //We had a listener on the search text field to purpose to add the input as a new site if not existing
                            protected Node getOrCreateButtonContentFromSelectedItem() {
                                ObservableList<Site> siteList = siteSelector.getObservableEntities();
                                searchTextField.textProperty().addListener((observableValue, s, t1) -> {
                                    List<String> siteNames = siteList.stream()
                                            .map(Site::getName)
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
                                });
                                return super.getOrCreateButtonContentFromSelectedItem();
                            }
                        }.always(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=?", o)).autoSelectFirstEntity();
                        siteSelector.selectedItemProperty().addListener(observable -> {
                            eventSite = siteSelector.getSelectedItem();
                            //We update the timeline and working scheduled item
                            eventTimeline.setSite(eventSite);
                            for(ScheduledItem si:workingScheduledItems)
                            {
                                si.setSite(eventSite);
                            }
                        });
                        locationHBox.getChildren().setAll(siteLabel,siteSelector.getButton());
                    }));
        })));

        titleEventDetailsLabel = I18nControls.bindI18nProperties(new Label(), "EventDetailsTitle");
        titleEventDetailsLabel.setPadding(new Insets(30,0,20,0));
        TextTheme.createSecondaryTextFacet(titleEventDetailsLabel).style();


        // ----FlexPane---------------------------------------|
        //|--VBox--------------------||---------Vbox---------||
        //|                          ||                      ||
        //| Name of event ...        ||  Time of event ...   ||
        //| Description   ...        ||  Date ...            ||
        //| Picture ...              ||                      ||
        //|--------------------------||----------------------||
        FlexPane eventDetailsPane = new FlexPane();
        eventDetailsPane.setHorizontalSpace(50);
        eventDetailsPane.setVerticalSpace(20);
        eventDetailsPane.setFlexLastRow(true);
        VBox leftPaneVBox = new VBox();
        leftPaneVBox.setPadding(new Insets(0,10,0,0));
        VBox rightPaneVBox = new VBox();

        leftPaneVBox.setMaxWidth(Double.MAX_VALUE);

        final int LABEL_WIDTH = 150;
        HBox line1InLeftPanel = new HBox();
        Label nameOfEventLabel = I18nControls.bindI18nProperties(new Label(),"NameOfTheEvent");
        nameOfEventLabel.setMinWidth(LABEL_WIDTH);
        /* Temporarily commented as not yet supported by WebFX
        nameOfEventTextField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.isContentChange() && change.getControlNewText().length() > 128) {
                return null;
            }
            return change;}));*/
        nameOfEventTextField.setMinWidth(500);
        HBox.setHgrow(nameOfEventTextField,Priority.ALWAYS);

        nameOfEventTextField.textProperty().addListener(obs -> {
            if(currentEditedEvent!=null) {
                currentEditedEvent.setName(nameOfEventTextField.getText());
            }});
        line1InLeftPanel.getChildren().setAll(nameOfEventLabel,nameOfEventTextField);

        siteLabel = I18nControls.bindI18nProperties(new Label(), "Location");
        siteLabel.setMinWidth(LABEL_WIDTH);
        locationHBox = new HBox(siteLabel);
        locationHBox.setPadding(new Insets(20,0,0,0));


        HBox line3InLeftPanel = new HBox();
        Label descriptionLabel = I18nControls.bindI18nProperties(new Label(),"Description");
        descriptionLabel.setMinWidth(LABEL_WIDTH);
        descriptionHtmlEditor.setMode(STANDARD);

        descriptionHtmlEditor.textProperty().addListener(obs -> {
            if(currentEditedEvent!=null) {
                currentEditedEvent.setDescription(descriptionHtmlEditor.getText());
            }});
        line3InLeftPanel.setPadding(new Insets(20,0,0,0));
        line3InLeftPanel.getChildren().setAll(descriptionLabel,descriptionHtmlEditor);

        HBox line4InLeftPanel = new HBox();
        line4InLeftPanel.setAlignment(Pos.CENTER_LEFT);
        Label uploadPictureLabel = I18nControls.bindI18nProperties(new Label(),"UploadFileDescription");
        uploadPictureLabel.setMinWidth(LABEL_WIDTH);

        HtmlText uploadText = new HtmlText();
        I18n.bindI18nTextProperty(uploadText.textProperty(), "UploadFileDescription");
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
            cloudPictureFileToUpload = fileList.get(0);
            Image imageToDisplay = new Image(cloudPictureFileToUpload.getObjectURL());
            isCloudPictureToBeUploaded.setValue(true);
            imageView.setImage(imageToDisplay);
            isPictureDisplayed.setValue(true);
        });

        Label uploadButtonDescription = I18nControls.bindI18nProperties(new Label(),"SelectYourFile");
        uploadButtonDescription.getStyleClass().add("font-size-9px");

        TextTheme.createPrimaryTextFacet(uploadButtonDescription).style();

        VBox uploadButtonVBox = new VBox(filePicker.getView(),uploadButtonDescription);
        uploadButtonVBox.setAlignment(Pos.CENTER);
        uploadButtonVBox.setPadding(new Insets(0,30,0,0));

        HBox imageAndTrashVBox = new HBox();
        imageAndTrashVBox.setSpacing(2);
        StackPane imageStackPane = new StackPane();
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageStackPane.setMaxSize(200,200);
        imageStackPane.setMinHeight(100);
      //  imageStackPane.setPrefSize(200,210);
        imageStackPane.setAlignment(Pos.CENTER);
        Label emptyPictureLabel = I18nControls.bindI18nProperties(new Label(),"NoPictureSelected");
        TextTheme.createSecondaryTextFacet(emptyPictureLabel).style();
        emptyPictureLabel.getStyleClass().add("font-size-9px");

        trashImage = SvgIcons.createTrashSVGPath();
        trashImage.setOnMouseClicked(event ->  {
            isCloudPictureToBeDeleted.setValue(true);
            isCloudPictureToBeUploaded.setValue(false);
            imageView.setImage(null);
            isPictureDisplayed.setValue(false);
        });
        ShapeTheme.createSecondaryShapeFacet(trashImage).style();
        imageStackPane.getChildren().setAll(imageView,emptyPictureLabel);
        StackPane.setAlignment(emptyPictureLabel, Pos.CENTER);
        imageView.toFront();
        emptyPictureLabel.toBack();
        imageAndTrashVBox.getChildren().setAll(imageStackPane,trashImage);
        imageAndTrashVBox.setAlignment(Pos.BOTTOM_CENTER);
        line4InLeftPanel.getChildren().setAll(uploadTextVBox,uploadButtonVBox,imageAndTrashVBox);
        line4InLeftPanel.setPadding(new Insets(20, 0,0,0));


        leftPaneVBox.getChildren().setAll(line1InLeftPanel,locationHBox,line3InLeftPanel,line4InLeftPanel);

        //The right pane (VBox)
        Label timeOfEventLabel = I18nControls.bindI18nProperties(new Label(),"TimeOfTheEvent");
        timeOfEventLabel.setPadding(new Insets(0,50,0,0));
        timeOfTheEventTextField.setMaxWidth(60);
        //We initialise the listener
        timeOfTheEventTextField.textProperty().addListener(obs -> {
            if(isLocalTimeTextValid(timeOfTheEventTextField.getText())) {
                LocalTime startTime = LocalTime.parse(timeOfTheEventTextField.getText());
                eventTimeline.setStartTime(startTime);
            }
        });

        Label durationLabel = I18nControls.bindI18nProperties(new Label(),"Duration");
        durationLabel.setPadding(new Insets(0,50,0,50));
        durationTextField.setMaxWidth(40);
        durationTextField.textProperty().addListener(obs -> {
                    //Here, when we change the duration, we have to update all the workingScheduledItem list
                    // and the timeline (we need to calculate the endTime and update it)
                    if (isIntegerValid(durationTextField.getText())) {
                        eventTimeline.setEndTime(eventTimeline.getStartTime().plusMinutes(Integer.parseInt(durationTextField.getText())));
                        for (ScheduledItem scheduledItem : workingScheduledItems) {
                            if(scheduledItem.getStartTime()!=null)
                                scheduledItem.setEndTime(scheduledItem.getStartTime().plusMinutes(Integer.parseInt(durationTextField.getText())));
                        }
                    }
                }
        );

        HBox line1 = new HBox();
        line1.setAlignment(Pos.CENTER_LEFT);
        line1.getChildren().addAll(timeOfEventLabel,timeOfTheEventTextField,durationLabel,durationTextField);
        line1.setPadding(new Insets(0,0,20,0));
        datesOfTheEventLabel = I18nControls.bindI18nProperties(new Label(),"Dates");
        datesOfTheEventLabel.setPadding(new Insets(0,0,5,0));
        calendarPane = new EventCalendarPane();
        calendarPane.getDatesPicker().getSelectedDates().addListener(onChangeDateListener);

        final int labelWidht = 200;
        HBox line4 = new HBox();
        line4.setPadding(new Insets(20,0,0,0));
        line4.setAlignment(Pos.CENTER_LEFT);
        Label externalLinkLabel = I18nControls.bindI18nProperties(new Label(),"ExternalLink");
        externalLinkLabel.setPadding(new Insets(0,20,0,0));
        externalLinkLabel.setPrefWidth(labelWidht);
        externalLinkTextField.setPrefWidth(400);
        externalLinkTextField.textProperty().addListener(obs -> currentEditedEvent.setExternalLink(externalLinkTextField.getText()));
        line4.getChildren().addAll(externalLinkLabel, externalLinkTextField);

        HBox line5 = new HBox();
        line5.setPadding(new Insets(20,0,0,0));
        line5.setAlignment(Pos.CENTER_LEFT);
        Label advertisedLabel = I18nControls.bindI18nProperties(new Label(),"Advertised");
        advertisedLabel.setPadding(new Insets(0,20,0,0));
        advertisedLabel.setPrefWidth(labelWidht);
        advertisedSwitch = new Switch();
        advertisedSwitch.selectedProperty().addListener(obs ->currentEditedEvent.setAdvertised(advertisedSwitch.selectedProperty().get()));
        line5.getChildren().addAll(advertisedLabel, advertisedSwitch);

        HBox line6 = new HBox();
        line6.setPadding(new Insets(20,0,0,0));
        line6.setAlignment(Pos.CENTER_LEFT);
        Label publishLabel = I18nControls.bindI18nProperties(new Label(),"Published");
        publishLabel.setPadding(new Insets(0,20,0,0));
        publishLabel.setPrefWidth(labelWidht);
        registrationOpenSwitch = new Switch();
        registrationOpenSwitch.selectedProperty().addListener(obs -> {
            if (registrationOpenSwitch.selectedProperty().get()) {
                currentEditedEvent.setState(EventState.OPEN);
            } else {
                if (previousEventState == EventState.DRAFT) {
                    currentEditedEvent.setState(EventState.DRAFT);
                } else {
                    currentEditedEvent.setState(EventState.ON_HOLD);
                }
            }
        });
        line6.getChildren().addAll(publishLabel, registrationOpenSwitch);



        HBox buttonsLine = new HBox();
        buttonsLine.setPadding(new Insets(50,0,0,0));
        buttonsLine.setAlignment(Pos.CENTER);
        cancelButton = I18nControls.bindI18nProperties(new Button(),"CancelButton");
        cancelButton.getStyleClass().addAll("recurringEventButton", "background-darkGrey", "font-white");

        cancelButton.setOnAction(e -> displayEventDetails(currentEditedEvent));
        cancelButton.disableProperty().bind(FXProperties.compute(currentMode, mode -> mode.intValue() == ADD_MODE));

        saveButton = I18nControls.bindI18nProperties(new Button(),"SaveButton");
        saveButton.setGraphicTextGap(10);
        saveButton.getStyleClass().addAll("recurringEventButton", "background-blue", "font-white");
        saveButton.setOnAction(event -> {
            if(validateForm())
            {
                if(previousEventState==null) {
                    previousEventState=EventState.DRAFT;
                    currentEditedEvent.setState(EventState.DRAFT);
                }
                submitUpdateStoreChanges();
                //If we add a new Event, put the selection on this event.
               // if(currentMode.get()== ADD_MODE) eventVisualMapper.setSelectedEntity(currentEditedEvent);
            }
        });

        deleteButton = I18nControls.bindI18nProperties(new Button(),"DeleteEvent");
        deleteButton.setGraphicTextGap(10);
        //We manage the property of the button in css
        deleteButton.setId("DeleteEvent");
        deleteButton.getStyleClass().addAll("recurringEventButton", "background-red", "font-white");
        deleteButton.setOnAction(event -> {
            //If the event is null, it means the selection has been removed from the visual mapper from the visual mapper.
            if(event!=null) {
                //We open a dialog box asking if we want to delete the event
                Text titleConfirmationText = I18n.bindI18nProperties(new Text(),"AreYouSure");
                titleConfirmationText.getStyleClass().add("font-green");
                titleConfirmationText.getStyleClass().add("font-size-22px");
                titleConfirmationText.getStyleClass().add("bold");
                BorderPane dialog = new BorderPane();
                dialog.setTop(titleConfirmationText);
                BorderPane.setAlignment(titleConfirmationText,Pos.CENTER);
                Text confirmationText = I18n.bindI18nProperties(new Text(),"DeleteConfirmation");
                dialog.setCenter(confirmationText);
                BorderPane.setAlignment(confirmationText,Pos.CENTER);
                BorderPane.setMargin(confirmationText,new Insets(30,0,30,0));
                Button okDeleteButton = I18nControls.bindI18nProperties(new Button(),"Confirm");
                okDeleteButton.getStyleClass().addAll("recurringEventButton", "background-red", "font-white");

                Button cancelActionButton = I18nControls.bindI18nProperties(new Button(),"Cancel");
                cancelActionButton.getStyleClass().addAll("recurringEventButton", "background-darkGrey", "font-white");
                HBox buttonsHBox = new HBox(cancelActionButton,okDeleteButton);
                buttonsHBox.setAlignment(Pos.CENTER);
                buttonsHBox.setSpacing(30);
                dialog.setBottom(buttonsHBox);
                BorderPane.setAlignment(buttonsHBox,Pos.CENTER);
                DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
                okDeleteButton.setOnAction(l -> {
                    try {
                        areWeDeleting = true;
                        updateStore.cancelChanges();
                        scheduledItemsReadFromDatabase.forEach(updateStore::deleteEntity);
                        updateStore.deleteEntity(eventTimeline);
                        updateStore.deleteEntity(currentEditedEvent);
                        updateStore.submitChanges()
                                .onFailure(x->Platform.runLater(() -> {
                                    areWeDeleting = false;
                                    Text infoText = I18n.bindI18nProperties(new Text(),"Error");
                                    infoText.getStyleClass().add("font-green");
                                    infoText.getStyleClass().add("font-size-22px");
                                    infoText.getStyleClass().add("bold");
                                    BorderPane errorDialog = new BorderPane();
                                    errorDialog.setTop(infoText);
                                    BorderPane.setAlignment(titleConfirmationText,Pos.CENTER);
                                    Text deleteErrorTest = I18n.bindI18nProperties(new Text(),"ErrorWhileDeletingEvent");
                                    errorDialog.setCenter(deleteErrorTest);
                                    BorderPane.setAlignment(deleteErrorTest,Pos.CENTER);
                                    BorderPane.setMargin(deleteErrorTest,new Insets(30,0,30,0));
                                    Button okErrorButton = I18nControls.bindI18nProperties(new Button(),"Ok");
                                    okErrorButton.getStyleClass().addAll("recurringEventButton", "background-red", "font-white");
                                    DialogCallback errorMessageCallback = DialogUtil.showModalNodeInGoldLayout(errorDialog, FXMainFrameDialogArea.getDialogArea());
                                    okErrorButton.setOnAction(m-> errorMessageCallback.closeDialog());
                                    errorDialog.setBottom(okErrorButton);
                                    BorderPane.setAlignment(okErrorButton,Pos.CENTER);
                        }))
                                .onSuccess(x -> Platform.runLater(() -> {
                                    Object imageTag = currentEditedEvent.getId().getPrimaryKey();
                                    deleteCloudPictureIfNecessary(imageTag);
                                    uploadCloudPictureIfNecessary(imageTag);
                                }));
                        dialogCallback.closeDialog();
                    }
                    catch (Exception e)
                    {
                       Console.log(e.toString());
                    }
                });
                cancelActionButton.setOnAction(l -> dialogCallback.closeDialog());
        }});
        buttonsLine.setSpacing(30);
        buttonsLine.getChildren().addAll(cancelButton, saveButton,deleteButton);
        buttonsLine.setAlignment(Pos.CENTER);

        rightPaneVBox.getChildren().setAll(line1,datesOfTheEventLabel,calendarPane,line4InLeftPanel,line4,line5,line6);
        eventDetailsPane.getChildren().setAll(leftPaneVBox,rightPaneVBox);
        HBox labelLine = new HBox();
        labelLine.setAlignment(Pos.BASELINE_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.SOMETIMES);
        labelLine.getChildren().setAll(currentEventLabel,spacer, addButton);

        eventDetailsVBox = new VBox(titleEventDetailsLabel,eventDetailsPane,buttonsLine);
        //When we launch the window, we don't display this VBox which contains an event details
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

    private void submitUpdateStoreChanges() {
        updateStore.submitChanges()
                .onFailure(Console::log)

                .onSuccess(x -> Platform.runLater(() -> {
                    Object imageTag = currentEditedEvent.getId().getPrimaryKey();
                    deleteCloudPictureIfNecessary(imageTag);
                    uploadCloudPictureIfNecessary(imageTag);
                    isCloudPictureToBeDeleted.setValue(false);
                    isCloudPictureToBeUploaded.setValue(false);
                    cloudPictureFileToUpload = null;
                    eventVisualMapper.requestSelectedEntity(currentEditedEvent);
                    displayEventDetails(currentEditedEvent);
                }));
    }

    /**
     * This method is used to sort the list workingScheduledItems by Date
     * This method is used to sort the list workingScheduledItems by Date
     */
    private void sortWorkingScheduledItemsByDate() {
        workingScheduledItems.sort(Comparator.comparing(EntityHasLocalDate::getDate));
    }
    /**
     * This private class is used to display the calendar
     * containing the datePicker, the separator vertical line, and the list of start time
     */
    private class EventCalendarPane extends Pane {
        Label daySelected = I18nControls.bindI18nProperties(new Label(),"DaysSelected");
        Label selectEachDayLabel = I18nControls.bindI18nProperties(new Label(),"SelectTheDays");
        Line verticalLine;
        VBox recurringEventsVBox = new VBox();
        ScrollPane recurringEventsScrollPane = new ScrollPane();
        DatesPicker datesPicker = new DatesPicker(YearMonth.now());

        public EventCalendarPane() {
            TextTheme.createSecondaryTextFacet(selectEachDayLabel).style();
            TextTheme.createSecondaryTextFacet(daySelected).style();
            setMaxWidth(500);
            workingScheduledItems.addListener((ListChangeListener<ScheduledItem>) change -> {
                //We call the listener only when the object has been loaded and not during the construction
                //ie when currentEditedEvent=currentSelectedEvent
              //  isWorkingScheduledItemEmpty.set(workingScheduledItems.isEmpty());
                if(currentEditedEvent!= null && (currentEditedEvent==currentObservedEvent)) {
                    recurringEventsVBox.getChildren().clear();
                    List<LocalDate> dates = workingScheduledItems.stream().map(EntityHasLocalDate::getDate).collect(Collectors.toList());
                    if (isWorkingScheduledItemEmpty.not().getValue()) {
                        if(!Collections.min(dates).equals(currentEditedEvent.getStartDate()))
                            currentEditedEvent.setStartDate(Collections.min(dates));
                        if(!Collections.max(dates).equals(currentEditedEvent.getEndDate()))
                            currentEditedEvent.setEndDate(Collections.max(dates));
                    }
                }
            });

            verticalLine = new Line();
            verticalLine.setStartY(0);
            verticalLine.setEndY(180);
            verticalLine.setStroke(Color.LIGHTGRAY);
            this.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY,
                    BorderStrokeStyle.SOLID, new CornerRadii(10), BorderWidths.DEFAULT)));

            recurringEventsScrollPane.setContent(recurringEventsVBox);
            recurringEventsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            recurringEventsScrollPane.setMaxHeight(180);
            getChildren().setAll(selectEachDayLabel,daySelected,verticalLine,datesPicker.getCalendarPane(),recurringEventsScrollPane);

            onChangeDateListener = change -> {
                if(currentEditedEvent!= null && currentEditedEvent==currentObservedEvent) {
                    while (change.next()) {
                        if (change.wasAdded()) {
                            LocalDate date = change.getAddedSubList().get(0);
                            //Here we haven't found it in the scheduledItemsReadFromDatabase, so we create it.
                            ScheduledItem scheduledItem = updateStore.insertEntity(ScheduledItem.class);
                            scheduledItem.setDate(date);
                            scheduledItem.setSite(eventSite);
                            scheduledItem.setEvent(currentEditedEvent);
                            scheduledItem.setTimeLine(eventTimeline);
                            scheduledItem.setItem(recurringItem);
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

                        List<LocalDate> localDatesSorted = calendarPane.getDatesPicker().getSelectedDates().stream().sorted().collect(Collectors.toList());
                        if (!localDatesSorted.isEmpty()) {
                            currentEditedEvent.setStartDate(localDatesSorted.get(0));
                            currentEditedEvent.setEndDate(localDatesSorted.get(localDatesSorted.size() - 1));
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
            private BorderPane drawScheduledItem(ScheduledItem scheduledItem) {
                LocalDate currentDate = scheduledItem.getDate();
                SVGPath trashDate = SvgIcons.createTrashSVGPath();
                trashDate.setTranslateY(2);



                Text currentDateValue = new Text(currentDate.format(DateTimeFormatter.ofPattern("MMM dd")));
                TextField currentEventStartTime = new TextField();
                timeOfTheEventTextField.textProperty().addListener(obs -> {
                    if(isLocalTimeTextValid(timeOfTheEventTextField.getText()) && isIntegerValid(durationTextField.getText())) {
                        currentEventStartTime.setPromptText(timeOfTheEventTextField.getText());
                        LocalTime startTime = LocalTime.parse(timeOfTheEventTextField.getText());
                        int duration = Integer.parseInt(durationTextField.getText());
                        LocalTime endTime = startTime.plusMinutes(duration);
                        eventTimeline.setStartTime(startTime);
                        eventTimeline.setEndTime(endTime);
                    }
                });

                if(scheduledItem.getFieldValue("attendance")==null) {
                    trashDate.setOnMouseClicked(event -> datesPicker.getSelectedDates().remove(currentDate));
                    ShapeTheme.createSecondaryShapeFacet(trashDate).style();
                }
                else
                {
                    trashDate.setFill(Color.LIGHTGRAY);
                    currentEventStartTime.setDisable(true);
                }
                if(!timeOfTheEventTextField.getText().isEmpty())
                    currentEventStartTime.setPromptText(timeOfTheEventTextField.getText());
                else currentEventStartTime.setPromptText(timeOfTheEventTextField.getPromptText());
                //We add a listener to update the value of the scheduled item when the text field is changed
                currentEventStartTime.textProperty().addListener(obs -> {
                    if ((isLocalTimeTextValid(currentEventStartTime.getText())&&(isIntegerValid(durationTextField.getText())))) {
                        LocalTime startTime = LocalTime.parse(currentEventStartTime.getText());
                        scheduledItem.setStartTime(startTime);
                        scheduledItem.setEndTime(startTime.plusMinutes(Integer.parseInt(durationTextField.getText())));
                      }
                });

                currentEventStartTime.setAlignment(Pos.CENTER);
                currentEventStartTime.setMaxWidth(90);
                LocalTime time = scheduledItem.getStartTime();
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
     * Test if a string is a format that is ready to be converted in LocalTime by the method LocalTime.Parse
     * @param text the string to be tested
     * @return true, is the string parameter can be converted in LocalTime, false otherwise
     */
    private static boolean isLocalTimeTextValid(String text) {
        try {
            return LocalTime.parse(text)!=null;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * We test here if url is valid
     * @param url the url to test
     * @return true if the url is valid, false otherwise
     */
    public static boolean isValidUrl(String url) {
        if(Objects.equals(url, "") || url==null)
            return true;
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
    private static boolean isIntegerValid(String text)
    {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

