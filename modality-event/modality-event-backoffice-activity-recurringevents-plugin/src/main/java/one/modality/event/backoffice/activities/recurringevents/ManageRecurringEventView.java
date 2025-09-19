package one.modality.event.backoffice.activities.recurringevents;

import dev.webfx.extras.filepicker.FilePicker;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.operation.OperationUtil;
import dev.webfx.extras.panes.FlexPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.theme.Facet;
import dev.webfx.extras.theme.shape.ShapeTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.time.pickers.DatePicker;
import dev.webfx.extras.time.pickers.DatePickerOptions;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.extras.util.dialog.builder.DialogBuilderUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.extras.util.masterslave.MasterSlaveLinker;
import dev.webfx.extras.util.masterslave.SlaveEditor;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.extras.webtext.HtmlTextEditor;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.file.File;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.*;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
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
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.util.dialog.ModalityDialog;
import one.modality.base.client.util.masterslave.ModalitySlaveEditor;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.markers.EntityHasLocalDate;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.client.event.fx.FXEvent;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dev.webfx.extras.webtext.HtmlTextEditor.Mode.BASIC;
import static dev.webfx.extras.webtext.HtmlTextEditor.Mode.STANDARD;
import static one.modality.base.client.time.BackOfficeTimeFormats.*;

/**
 * @author David Hello
 */
final class ManageRecurringEventView {

    private static final double EVENT_IMAGE_WIDTH = 200;
    private static final double EVENT_IMAGE_HEIGHT = 200;

    private static final String EVENT_COLUMNS = // language=JSON5
        """
            [
                {expression: 'state', label: 'Status', renderer: 'eventStateRenderer'},
                {expression: 'advertised', label: 'Advertised'},
                {expression: 'name', label: 'Name'},
                {expression: 'type', label: 'TypeOfEvent'},
                {expression: 'venue.name', label: 'Location'},
                {expression: 'dateIntervalFormat(startDate, endDate)', label: 'Dates'}
            ]""";

    private final RecurringEventsActivity activity;
    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private final VisualGrid eventTable = new VisualGrid();
    private final TextField nameOfEventTextField = I18nControls.bindI18nProperties(new TextField(), RecurringEventsI18nKeys.NameOfTheEvent);
    private final HtmlTextEditor shortDescriptionHtmlEditor = new HtmlTextEditor();
    private final HtmlTextEditor descriptionHtmlEditor = new HtmlTextEditor();
    private final TextField timeOfTheEventTextField = I18nControls.bindI18nProperties(new TextField(), RecurringEventsI18nKeys.TimeOfTheEvent);
    private final EntityStore entityStore = EntityStore.create();
    private List<ScheduledItem> teachingsScheduledItemsReadFromDatabase = new ArrayList<>();
    private List<ScheduledItem> audioScheduledItemsReadFromDatabase = new ArrayList<>();
    private List<ScheduledItem> videoScheduledItemsReadFromDatabase = new ArrayList<>();
    private final ObservableList<ScheduledItem> teachingsWorkingScheduledItems = FXCollections.observableArrayList();
    private final ObservableList<ScheduledItem> audioWorkingScheduledItems = FXCollections.observableArrayList();
    private final ObservableList<ScheduledItem> videoWorkingScheduledItems = FXCollections.observableArrayList();
    private final TextField durationTextField = I18nControls.bindI18nProperties(new TextField(), RecurringEventsI18nKeys.RecurringEventDuration);
    private final TextField bookingOpeningDateTextField = new TextField();
    private final TextField bookingOpeningTimeTextField = new TextField();
    private final TextField externalLinkTextField = I18nControls.bindI18nProperties(new TextField(), RecurringEventsI18nKeys.ExternalLink);
    private final MonoPane eventImageContainer = new MonoPane();
    private Label datesOfTheEventLabel;
    private Label titleEventDetailsLabel;
    private Switch advertisedSwitch;
    private Switch registrationOpenSwitch;
    private Switch audioCorrespondanceProgramSwitch;
    private Switch videoCorrespondanceProgramSwitch;
    private Label siteLabel;
    private Button saveButton;
    private Button cancelButton;
    private Button deleteButton;
    private VBox eventDetailsVBox;
    private HBox locationHBox;
    private SVGPath trashImage;
    private ListChangeListener<LocalDate> onChangeDateListener;
    private EventCalendarPane calendarPane;

    private Event currentEditedEvent;
    private Event currentSelectedEvent;
    private Event currentObservedEvent;
    private Site eventSite;
    private Item recurringItem;
    private Object videoItemId;
    private Object audioItemId;
    private final UpdateStore updateStore = UpdateStore.createAbove(entityStore);
    private final ValidationSupport validationSupport = new ValidationSupport();
    private final BooleanExpression isTeachingsWorkingScheduledItemEmpty = ObservableLists.isEmpty(teachingsWorkingScheduledItems);
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
    private File cloudPictureFileToUpload;
    private final BooleanProperty isCloudPictureToBeDeleted = new SimpleBooleanProperty(false);
    private final BooleanProperty isCloudPictureToBeUploaded = new SimpleBooleanProperty(false);
    private Object recentlyUploadedCloudPictureId;
    private BooleanBinding updateStoreOrPictureHasChanged;
    private ReactiveVisualMapper<Event> eventVisualMapper;
    private boolean areWeDeleting = false;
    private LocalTime defaultStartTime;
    private LocalTime defaultEndTime;

    //When the duration or the event time is changed, we call this listener.
    private final InvalidationListener changeOnStartTimeOrDurationListener = observable -> {
        if (isLocalTimeTextValid(timeOfTheEventTextField.getText()) && isIntegerValid(durationTextField.getText())) {
            LocalTime startTime = LocalTime.parse(timeOfTheEventTextField.getText());
            int duration = Integer.parseInt(durationTextField.getText());
            LocalTime endTime = startTime.plusMinutes(duration);
            for (int i = 0; i < teachingsWorkingScheduledItems.size(); i++) {
                ScheduledItem si = teachingsWorkingScheduledItems.get(i);
                //If the scheduledItem has attendance, we don't change the time.
                if (si.getFieldValue("attendance") == null) {
                    BorderPane currentBorderPane = (BorderPane) calendarPane.getRecurringEventsVBox().getChildren().get(i);
                    TextField currentScheduleItemStartTimeTextField = (TextField) currentBorderPane.getRight();
                    currentScheduleItemStartTimeTextField.setPromptText(timeOfTheEventTextField.getText());
                    if (defaultStartTime == null) defaultStartTime = startTime;
                    if (defaultEndTime == null) defaultEndTime = endTime;
                    // if (defaultStartTime.equals(si.getStartTime())) {
                    si.setStartTime(startTime);
                    si.setEndTime(endTime);
                    // }
                }
            }
            defaultStartTime = startTime;
            defaultEndTime = endTime;
        }
    };


    private final SlaveEditor<Event> eventDetailsSlaveEditor = new ModalitySlaveEditor<>() {
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

    public ManageRecurringEventView(RecurringEventsActivity activity) {
        this.activity = activity;
    }

    /**
     * This method is used to initialise the parameters for the form validation
     */
    private void initFormValidation() {
        if (validationSupport.isEmpty()) {
            validationSupport.addRequiredInput(nameOfEventTextField);
            validationSupport.addValidationRule(timeOfTheEventTextField.textProperty().map(ManageRecurringEventView::isLocalTimeTextValid), timeOfTheEventTextField, I18n.i18nTextProperty("ValidationTimeFormatIncorrect")); // ???
            validationSupport.addValidationRule(durationTextField.textProperty().map(ManageRecurringEventView::isIntegerValid), durationTextField, I18n.i18nTextProperty("ValidationDurationIncorrect")); // ???
            validationSupport.addValidationRule(isTeachingsWorkingScheduledItemEmpty.not(), datesOfTheEventLabel, I18n.i18nTextProperty(RecurringEventsI18nKeys.ValidationSelectOneDate));
            validationSupport.addValidationRule(externalLinkTextField.textProperty().map(ManageRecurringEventView::isValidUrl), externalLinkTextField, I18n.i18nTextProperty("ValidationUrlIncorrect")); // ???
        }
    }

    void setActive(boolean active) {
        activeProperty.set(active);
    }

    /**
     * This method is used to initialise the Logic
     */
    public void startLogic() {
        RecurringEventRenderers.registerRenderers();

        eventVisualMapper = ReactiveVisualMapper.<Event>createPushReactiveChain(activity)
            .always( // language=JSON5
                "{class: 'Event', alias: 'e', fields: 'type.recurringItem, recurringWithAudio, recurringWithVideo, (select site.name from Timeline where event=e limit 1) as location'}")
            .always(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=?", o))
            .always(DqlStatement.where("type.recurringItem!=null and kbs3"))
            .setEntityColumns(EVENT_COLUMNS)
            .setStore(entityStore)
            .setVisualSelectionProperty(eventTable.visualSelectionProperty())
            .visualizeResultInto(eventTable.visualResultProperty())
            .bindActivePropertyTo(Bindings.and(activity.activeProperty(), activeProperty))
            .addEntitiesHandler(entityList -> Console.log("Reactive visual Mapper loaded"))
            .start();

        //We need initialize the audio and video Item Id.
        entityStore.executeQueryBatch(
                //Index 0: the video Item (we should have exactly 1)
                new EntityStoreQuery("select Item where family=? and organization=?", new Object[]{KnownItemFamily.VIDEO.getPrimaryKey(), FXOrganization.getOrganization()}),
                //Index 1: the audio Item (we should have exactly one that has the same language as the default language of the organization)
                new EntityStoreQuery("select Item where family=? and organization=? and language=organization.language", new Object[]{KnownItemFamily.AUDIO_RECORDING.getPrimaryKey(), FXOrganization.getOrganization()}))
            .onFailure(Console::log)
            .inUiThread()
            .onSuccess(entityLists -> {
                    EntityList<Item> videoItems = entityLists[0];
                    EntityList<Item> audioItems = entityLists[1];
                    if (!videoItems.isEmpty()) videoItemId = Entities.getPrimaryKey(videoItems.get(0));
                    if (!audioItems.isEmpty()) audioItemId = Entities.getPrimaryKey(audioItems.get(0));

                });
        /*
        We create a booleanBinding that will be used by the submit and draft button if either the updateStoreChanged, or the pictures needs to be uploaded
        or deleted
         */
        updateStoreOrPictureHasChanged = new BooleanBinding() {
            {
                super.bind(EntityBindings.hasChangesProperty(updateStore), isCloudPictureToBeUploaded, isCloudPictureToBeDeleted);
            }

            @Override
            protected boolean computeValue() {
                boolean value = updateStore.hasChanges() || isCloudPictureToBeUploaded.getValue() || isCloudPictureToBeDeleted.getValue();
                Console.log("value = " + value);
                return value;
            }
        };

        //Now we bind the different element (FXEvent, Visual Mapper, and MasterSlaveController)
        eventVisualMapper.requestedSelectedEntityProperty().bindBidirectional(FXEvent.eventProperty());
        masterSlaveEventLinker.masterProperty().bindBidirectional(eventVisualMapper.selectedEntityProperty());
    }

    /**
     * This method is called when we select an event, it takes the info in the database
     * and initialise the class variable.
     *
     * @param e the Event from who we want the data
     */
    private void displayEventDetails(Event e) {
        //Console.log("Display Event Called");
        currentSelectedEvent = e;

        //Event e can be null if for example we select on the gantt graph an event that is not a recurring event
        if (e == null) {
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
        //We execute the query in batch, otherwise we can have a synchronisation problem between the different threads
        entityStore.executeQueryBatch(
                // Index 0: the scheduledItem
                new EntityStoreQuery("""
                    select item,date,startTime, site, programScheduledItem, bookableScheduledItem, endTime, event.(openingDate, shortDescription, description, state, advertised, kbs3, type.recurringItem, externalLink, venue.name), (select id from Attendance \
                     where scheduledItem=si limit 1) as attendance \
                     from ScheduledItem si where event=?""", e),
                //Index 1: the video Item (we should have exactly 1)
                new EntityStoreQuery("select Item where family=? and organization=?",
                    KnownItemFamily.VIDEO.getPrimaryKey(), FXOrganization.getOrganization()),
                //Index 2: the audio Item (we should have exactly one that has the same language as the default language of the organization)
                new EntityStoreQuery("select Item where family=? and organization=? and language=organization.language",
                    KnownItemFamily.AUDIO_RECORDING.getPrimaryKey(), FXOrganization.getOrganization()))
            .onFailure(Console::log)
            .inUiThread()
            .onSuccess(entityLists -> {
                EntityList<ScheduledItem> scheduledItems = entityLists[0];
                EntityList<Item> videoItems = entityLists[1];
                EntityList<Item> audioItems = entityLists[2];
                if (!videoItems.isEmpty()) videoItemId = Entities.getPrimaryKey(videoItems.get(0));
                if (!audioItems.isEmpty()) audioItemId = Entities.getPrimaryKey(audioItems.get(0));
                // we test if the selectedEvent==e, because, if a user click very fast from en event to another, there
                // can be a sync pb between the result of the request from the database and the code executed
                if (currentSelectedEvent == e) {
                    DatePicker datePicker = calendarPane.getDatePicker();
                    // We take the selected date from the database, and transform the result in a list of LocalDate,
                    // that we pass to the datePicker, so they appear selected in the calendar
                    teachingsScheduledItemsReadFromDatabase = scheduledItems.stream()
                        .filter(item -> KnownItemFamily.TEACHING.getCode().equals(item.getItem().getFamily().getCode())) // Adjust the getter methods as needed
                        .collect(Collectors.toList());
                    audioScheduledItemsReadFromDatabase = scheduledItems.stream()
                        .filter(item -> KnownItemFamily.AUDIO_RECORDING.getCode().equals(item.getItem().getFamily().getCode())) // Adjust the getter methods as needed
                        .collect(Collectors.toList());
                    videoScheduledItemsReadFromDatabase = scheduledItems.stream()
                        .filter(item -> KnownItemFamily.VIDEO.getCode().equals(item.getItem().getFamily().getCode())) // Adjust the getter methods as needed
                        .collect(Collectors.toList());
                    List<LocalDate> bookedDates = teachingsScheduledItemsReadFromDatabase.stream().map(EntityHasLocalDate::getDate).collect(Collectors.toList());
                    datePicker.setSelectedDates(bookedDates);

                    Function<LocalDate, Boolean> isDateBooked = localDate -> {
                        ScheduledItem scheduledItem = scheduledItems.stream().filter(si -> localDate.equals(si.getDate())).findFirst().orElse(null);
                        return scheduledItem != null && scheduledItem.getFieldValue("attendance") != null;
                    };

                    datePicker.setGetDateStyleClassFunction((localDate -> {
                        if (isDateBooked.apply(localDate)) {
                            isEventDeletable.setValue(false);
                            return "date-secondary";
                        } else {
                            return datePicker.getDateStyleClassDefault(localDate);
                        }
                    }));
                    datePicker.setIsDateSelectableFunction(localDate ->
                        datePicker.isDateSelectableDefault(localDate) && !isDateBooked.apply(localDate)
                    );

                    //We display on the calendar the month containing the first date of the recurring event
                    if (!bookedDates.isEmpty()) {
                        LocalDate oldestDate = Collections.min(bookedDates);
                        datePicker.setDisplayedYearMonth(YearMonth.of(oldestDate.getYear(), oldestDate.getMonthValue()));
                    }
                    //Then we get the timeline and event, there should be just one timeline per recurring event
                    eventSite = currentSelectedEvent.getVenue();
                    //We initialize the recurringItem, that will be needed as a parameter when creating new ScheduledItem
                    recurringItem = currentSelectedEvent.getType().getRecurringItem();
                    //to initialise the start time and end time that will be used for the event, we take the values from the scheduledItem and we use the most used
                    Optional<LocalTime> mostFrequentStartTime = scheduledItems.stream()
                        .filter(scheduledItem -> scheduledItem.getFieldValue("attendance") == null)
                        .map(ScheduledItem::getStartTime)
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                        .entrySet().stream()
                        .max(Comparator.comparingLong(Map.Entry::getValue))
                        .map(Map.Entry::getKey);
                    String duration = "";
                    if (!teachingsScheduledItemsReadFromDatabase.isEmpty()) {
                        ScheduledItem firstScheduledItem = teachingsScheduledItemsReadFromDatabase.get(0);
                        LocalTime startTime = firstScheduledItem.getStartTime();
                        LocalTime endTime = firstScheduledItem.getEndTime();
                        duration = startTime == null || endTime == null ? "" : String.valueOf(ChronoUnit.MINUTES.between(startTime, endTime));
                        durationTextField.setText(duration);
                    }
                    //We initialise the value of defaultStartTime after the setText on durationTextField, otherwise the listener called in durationTextField will overide the value we want to set
                    if (mostFrequentStartTime.isPresent()) {
                        defaultStartTime = mostFrequentStartTime.get();
                    } else {
                        //If we're, it means all the scheduledItem have at least one attendance. So we look for the defaultStartTime in all the scheduledItem and not
                        //only in the one with no attendance.
                        Optional<LocalTime> mostFrequentStartTimeInScheduledItemWithAttendance = scheduledItems.stream()
                            .map(ScheduledItem::getStartTime)
                            .filter(Objects::nonNull)
                            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                            .entrySet().stream()
                            .max(Comparator.comparingLong(Map.Entry::getValue))
                            .map(Map.Entry::getKey);
                        defaultStartTime = mostFrequentStartTimeInScheduledItemWithAttendance.orElse(null);
                    }
                    defaultEndTime = defaultStartTime == null ? null : defaultStartTime.plusMinutes(Long.parseLong(duration));
                    timeOfTheEventTextField.setText(defaultStartTime == null ? null : defaultStartTime.toString());
                    I18nControls.bindI18nTextProperty(titleEventDetailsLabel, RecurringEventsI18nKeys.EditEventInformation0, e.getName());
                    //We read and format the opening date value
                    LocalDateTime openingDate = e.getOpeningDate();
                    if (openingDate != null) {
                        DateTimeFormatter dateFormatter = LocalizedTime.dateFormatter(RECURRING_EVENT_OPENING_DATE_FORMAT);
                        DateTimeFormatter timeFormatter = LocalizedTime.timeFormatter(RECURRING_EVENT_OPENING_TIME_FORMAT);
                        bookingOpeningDateTextField.setText(openingDate.format(dateFormatter));
                        bookingOpeningTimeTextField.setText(openingDate.format(timeFormatter));
                    }

                    //We add the event and timeline to the updateStore, so they will be modified when changed
                    currentEditedEvent = updateStore.updateEntity(e);

                    teachingsWorkingScheduledItems.setAll(teachingsScheduledItemsReadFromDatabase.stream().map(updateStore::updateEntity).collect(Collectors.toList()));
                    audioWorkingScheduledItems.setAll(audioScheduledItemsReadFromDatabase.stream().map(updateStore::updateEntity).collect(Collectors.toList()));
                    videoWorkingScheduledItems.setAll(videoScheduledItemsReadFromDatabase.stream().map(updateStore::updateEntity).collect(Collectors.toList()));

                    sortTeachingWorkingScheduledItemsByDate();
                    ScheduledItem si = null;
                    if (!teachingsWorkingScheduledItems.isEmpty()) si = teachingsWorkingScheduledItems.get(0);
                    if (si != null) eventSite = si.getSite();
                    else eventSite = currentEditedEvent.getVenue();
                    //In case the scheduledItem has no startTime and endTime, we position this value from the timeline
                    teachingsWorkingScheduledItems.forEach(scheduledItem -> {
                        if (scheduledItem.getStartTime() == null) {
                            scheduledItem.setStartTime(defaultStartTime);
                            scheduledItem.setEndTime(defaultEndTime);
                        }
                    });

                    //and finally, we fill the UI with the values from the database
                    nameOfEventTextField.setText(currentEditedEvent.getName());
                    shortDescriptionHtmlEditor.setText(currentEditedEvent.getShortDescription());
                    descriptionHtmlEditor.setText(currentEditedEvent.getDescription());
                    externalLinkTextField.setText(currentEditedEvent.getExternalLink());
                    boolean isAdvertised;
                    if (currentEditedEvent.isAdvertised() == null) isAdvertised = false;
                    else isAdvertised = currentEditedEvent.isAdvertised();
                    advertisedSwitch.setSelected(isAdvertised);
                    registrationOpenSwitch.setSelected(currentEditedEvent.getState() == EventState.OPEN);
                    audioCorrespondanceProgramSwitch.setSelected(currentEditedEvent.isRecurringWithAudio() != null && currentEditedEvent.isRecurringWithAudio());
                    videoCorrespondanceProgramSwitch.setSelected(currentEditedEvent.isRecurringWithVideo() != null && currentEditedEvent.isRecurringWithAudio());

                    //We try to load the image from cloudinary if it exists
                    loadEventImageIfExists();
                }
                currentObservedEvent = currentEditedEvent;
                bindButtons();
            });
    }

    private void bindButtons() {
        saveButton.disableProperty().bind(updateStoreOrPictureHasChanged.not());
        cancelButton.disableProperty().bind(updateStoreOrPictureHasChanged.not());
        trashImage.visibleProperty().bind(isPictureDisplayed);
        deleteButton.disableProperty().bind(isEventDeletable.not());
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
        defaultStartTime = null;
        defaultEndTime = null;
        areWeDeleting = false;
        teachingsWorkingScheduledItems.clear();
        // We reset the datePicker, otherwise it will keep the settings of the previous event
        DatePicker datePicker = calendarPane.getDatePicker();
        datePicker.clearSelection();
        datePicker.setDisplayedYearMonth(YearMonth.now());
        //datesPicker.updateDatesBackground();
        updateStore.cancelChanges();
        eventImageContainer.setContent(null);
        isPictureDisplayed.setValue(false);
    }

    /**
     * This method is used to reset the text fields
     */
    private void resetTextFields() {
        nameOfEventTextField.setText("");
        durationTextField.setText("");
        timeOfTheEventTextField.setText("");
        shortDescriptionHtmlEditor.setText("");
        descriptionHtmlEditor.setText("");
        bookingOpeningDateTextField.setText("");
        bookingOpeningTimeTextField.setText("");
    }

    private void loadEventImageIfExists() {
        String cloudImagePath = ModalityCloudinary.eventImagePath(currentEditedEvent);
        if (Objects.equals(cloudImagePath, recentlyUploadedCloudPictureId))
            return;
        ModalityCloudinary.loadHdpiImage(cloudImagePath, EVENT_IMAGE_WIDTH, EVENT_IMAGE_HEIGHT, eventImageContainer, () -> null)
            .onComplete(ar -> isPictureDisplayed.setValue(eventImageContainer.getContent() != null));
    }


    /**
     * We validate the form
     *
     * @return true if all the validation is success, false otherwise
     */
    public boolean validateForm() {
        initFormValidation();
        return validationSupport.isValid();
    }

    public void uploadCloudPictureIfNecessary(String cloudImagePath) {
        if (isCloudPictureToBeUploaded.getValue()) {
            ModalityCloudinary.uploadImage(cloudImagePath, cloudPictureFileToUpload)
                .onFailure(Console::log)
                .onSuccess(ok -> {
                    isCloudPictureToBeUploaded.set(false);
                    recentlyUploadedCloudPictureId = cloudImagePath;
                    loadEventImageIfExists();
                });
        }
    }

    public void deleteCloudPictureIfNecessary(String cloudImagePath) {
        if (isCloudPictureToBeDeleted.getValue()) {
            //We delete the pictures, and all the cached picture in cloudinary that can have been transformed, related
            //to this assets
            ModalityCloudinary.deleteImage(cloudImagePath)
                .onFailure(Console::log)
                .onSuccess(ok -> {
                    isCloudPictureToBeDeleted.set(false);
                    if (Objects.equals(cloudImagePath, recentlyUploadedCloudPictureId))
                        recentlyUploadedCloudPictureId = null;
                });
        }
    }


    /**
     * The entry point of the class
     *
     * @return A scrollablePane that is the UI for this class
     */
    public Node buildContainer() {
        BorderPane mainFrame = new BorderPane();
        mainFrame.getStyleClass().add("recurring-event");
        //Displaying The title of the frame
        Label title = I18nControls.newLabel(RecurringEventsI18nKeys.EventTitle);
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);
        TextTheme.createPrimaryTextFacet(title).style();
        title.getStyleClass().add("title");

        BorderPane.setAlignment(title, Pos.CENTER);
        mainFrame.setPadding(new Insets(0, 0, 30, 0));
        //mainFrame.setTop( title);

        //Displaying the list of events
        Label currentEventLabel = Bootstrap.h3(I18nControls.newLabel(RecurringEventsI18nKeys.ListEvents));
        currentEventLabel.setPadding(new Insets(15, 0, 20, 0));
        TextTheme.createSecondaryTextFacet(currentEventLabel).style();

        Button addButton = Bootstrap.successButton(I18nControls.newButton(RecurringEventsI18nKeys.AddEventInformationButton));

        addButton.setOnAction((event -> masterSlaveEventLinker.checkSlaveSwitchApproval(true, () -> {
            resetTextFields();
            resetUpdateStoreAndOtherComponents();
            eventDetailsVBox.setVisible(true);
            eventDetailsVBox.setManaged(true);
            currentEditedEvent = updateStore.insertEntity(Event.class);
            currentObservedEvent = currentEditedEvent;
            entityStore.executeQuery("select recurringItem, organization from EventType where recurringItem!=null and organization=?", FXOrganization.getOrganization())
                .onFailure(Console::log)
                .inUiThread()
                .onSuccess(e -> {
                    //TODO: if there is several type of recurring EventType for an organization, create an UI that allow to select which one we choose.
                    EventType eventType = (EventType) e.get(0);
                    recurringItem = eventType.getRecurringItem();
                    currentEditedEvent.setOrganization(eventType.getOrganization());
                    currentEditedEvent.setCorporation(1);
                    currentEditedEvent.setType(eventType);
                    currentEditedEvent.setKbs3(true);
                    currentEditedEvent.setVenue(eventSite);
                    currentEditedEvent.setState(EventState.DRAFT);
                    currentEditedEvent.setAdvertised(false);
                    currentMode.set(ADD_MODE);
                    I18nControls.bindI18nTextProperty(titleEventDetailsLabel, RecurringEventsI18nKeys.EventDetailsTitle);
                    siteSelector = new EntityButtonSelector<Site>( // language=JSON5
                        "{class: 'Site', alias: 's', where: 'event=null', orderBy :'name'}",
                        activity, eventDetailsVBox, DataSourceModelService.getDefaultDataSourceModel()
                    ) { // Overriding the button content to add the possibility of Adding a new site prefix text
                        private final BorderPane bp = new BorderPane();
                        final TextField searchTextField = super.getSearchTextField();
                        private final UpdateStore updateStoreForSite = UpdateStore.create();

                        final Text addSiteText = I18n.newText(RecurringEventsI18nKeys.AddNewLocation);
                        final MonoPane addSitePane = new MonoPane(addSiteText);

                        {
                            addSiteText.setFill(Color.BLUE);
                            addSitePane.setMaxWidth(Double.MAX_VALUE);
                            addSitePane.setMinHeight(20);
                            addSitePane.setBackground(Background.fill(Color.WHITE));
                            addSitePane.setCursor(Cursor.HAND);
                            addSitePane.setOnMousePressed(event -> {
                                Site site = updateStoreForSite.insertEntity(Site.class);
                                site.setName(searchTextField.getText());
                                site.setOrganization(FXOrganization.getOrganization());
                                site.setItemFamily(KnownItemFamily.TEACHING.getPrimaryKey());
                                site.setMain(true);
                                site.setFieldValue("online", false);
                                site.setFieldValue("forceSoldout", false);
                                site.setFieldValue("hideDates", true);
                                site.setFieldValue("asksForPassport", false);
                                //We had in the database the site now (otherwise too complicated to manage with the actual components)
                                updateStoreForSite.submitChanges()
                                    .inUiThread()
                                    .onSuccess((result -> {
                                        Object newSiteId = result.getGeneratedKey();
                                        Site newSite = updateStoreForSite.createEntity(Site.class, newSiteId);
                                        //The createEntity doesn't load the name, so we need to set it up manually
                                        newSite.setName(site.getName());
                                        setSelectedItem(newSite);
                                    }));
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
                            FXProperties.runOnPropertyChange(searchText -> {
                                List<String> siteNames = siteList.stream()
                                    .map(Site::getName)
                                    .collect(Collectors.toList());
                                if (searchTextField.getText() != null && searchTextField.getText().length() > 2 && !siteNames.contains(searchTextField.getText())) {
                                    Console.log("We add : " + searchTextField.getText());
                                    addSitePane.setVisible(true);
                                    addSitePane.setManaged(true);
                                } else {
                                    addSitePane.setVisible(false);
                                    addSitePane.setManaged(false);
                                }
                            }, searchTextField.textProperty());
                            return super.getOrCreateButtonContentFromSelectedItem();
                        }
                    }.always(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=?", o)).autoSelectFirstEntity();
                    siteSelector.selectedItemProperty().addListener(observable -> {
                        eventSite = siteSelector.getSelectedItem();
                        currentEditedEvent.setVenue(eventSite);
                        teachingsWorkingScheduledItems.forEach(si -> si.setSite(eventSite));
                    });
                    locationHBox.getChildren().setAll(siteLabel, siteSelector.getButton());
                });
        })));

        titleEventDetailsLabel = I18nControls.newLabel(RecurringEventsI18nKeys.EventDetailsTitle);
        titleEventDetailsLabel.setPadding(new Insets(30, 0, 20, 0));
        TextTheme.createSecondaryTextFacet(titleEventDetailsLabel).style();

        deleteButton = Bootstrap.dangerButton(I18nControls.newButton(RecurringEventsI18nKeys.DeleteEvent));
        deleteButton.setGraphicTextGap(10);
        //We manage the property of the button in css
        deleteButton.setId("DeleteEvent");
        deleteButton.setOnAction(event -> {
            //If the event is null, it means the selection has been removed from the visual mapper from the visual mapper.
            if (event != null) {
                //We open a dialog box asking if we want to delete the event
                ModalityDialog.showConfirmationDialog(RecurringEventsI18nKeys.DeleteConfirmation, () -> {
                    areWeDeleting = true;
                    updateStore.cancelChanges();
                    teachingsScheduledItemsReadFromDatabase.forEach(updateStore::deleteEntity);
                    updateStore.deleteEntity(currentEditedEvent);
                    return updateStore.submitChanges()
                        .inUiThread()
                        .onFailure(x -> {
                            areWeDeleting = false;
                            Text infoText = I18n.newText(RecurringEventsI18nKeys.ErrorWhileDeletingEvent);
                            Bootstrap.textSuccess(Bootstrap.strong(Bootstrap.h3(infoText)));
                            BorderPane errorDialog = new BorderPane();
                            errorDialog.setTop(infoText);
                            BorderPane.setAlignment(infoText, Pos.CENTER);
                            Text deleteErrorTest = I18n.newText(RecurringEventsI18nKeys.ErrorWhileDeletingEventDetails);
                            errorDialog.setCenter(deleteErrorTest);
                            BorderPane.setAlignment(deleteErrorTest, Pos.CENTER);
                            BorderPane.setMargin(deleteErrorTest, new Insets(30, 0, 30, 0));
                            Button okErrorButton = Bootstrap.largeDangerButton(I18nControls.newButton(BaseI18nKeys.Ok));

                            DialogCallback errorMessageCallback = DialogUtil.showModalNodeInGoldLayout(errorDialog, FXMainFrameDialogArea.getDialogArea());
                            okErrorButton.setOnAction(m -> errorMessageCallback.closeDialog());
                            errorDialog.setBottom(okErrorButton);
                            BorderPane.setAlignment(okErrorButton, Pos.CENTER);
                        })
                        .onSuccess(x -> {
                            String cloudImagePath = ModalityCloudinary.eventImagePath(currentEditedEvent);
                            deleteCloudPictureIfNecessary(cloudImagePath);
                            uploadCloudPictureIfNecessary(cloudImagePath);
                        });
                });
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.SOMETIMES);
        HBox titleHox = new HBox(titleEventDetailsLabel, spacer, deleteButton);
        titleHox.setAlignment(Pos.BASELINE_LEFT);

        final int LABEL_WIDTH = 150;
        HBox line1InLeftPanel = new HBox();
        Label nameOfEventLabel = I18nControls.newLabel(RecurringEventsI18nKeys.NameOfTheEvent);
        nameOfEventLabel.setMinWidth(LABEL_WIDTH);
        /* Temporarily commented as not yet supported by WebFX
        nameOfEventTextField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.isContentChange() && change.getControlNewText().length() > 128) {
                return null;
            }
            return change;}));*/
        nameOfEventTextField.setMinWidth(500);
        HBox.setHgrow(nameOfEventTextField, Priority.ALWAYS);

        nameOfEventTextField.textProperty().addListener(obs -> {
            if (currentEditedEvent != null) {
                currentEditedEvent.setName(nameOfEventTextField.getText());
            }
        });
        line1InLeftPanel.getChildren().setAll(nameOfEventLabel, nameOfEventTextField);

        siteLabel = I18nControls.newLabel(RecurringEventsI18nKeys.Location);
        siteLabel.setMinWidth(LABEL_WIDTH);
        locationHBox = new HBox(siteLabel);
        locationHBox.setPadding(new Insets(20, 0, 0, 0));

        Label shortDescriptionLabel = I18nControls.newLabel(RecurringEventsI18nKeys.ShortDescription);
        shortDescriptionLabel.setMinWidth(LABEL_WIDTH);
        shortDescriptionHtmlEditor.setMode(BASIC);
        shortDescriptionHtmlEditor.setPrefHeight(150);
        shortDescriptionHtmlEditor.textProperty().addListener(obs -> {
            if (currentEditedEvent != null) {
                currentEditedEvent.setShortDescription(shortDescriptionHtmlEditor.getText());
            }
        });
        HBox shortDescriptionLineInLeftPanel = new HBox(shortDescriptionLabel, shortDescriptionHtmlEditor);
        shortDescriptionLineInLeftPanel.setPadding(new Insets(20, 0, 0, 0));

        Label descriptionLabel = I18nControls.newLabel(RecurringEventsI18nKeys.Description);
        descriptionLabel.setMinWidth(LABEL_WIDTH);
        descriptionHtmlEditor.setMode(STANDARD);
        descriptionHtmlEditor.setMaxHeight(400);
        descriptionHtmlEditor.textProperty().addListener(obs -> {
            if (currentEditedEvent != null) {
                currentEditedEvent.setDescription(descriptionHtmlEditor.getText());
            }
        });
        HBox line3InLeftPanel = new HBox(descriptionLabel, descriptionHtmlEditor);
        line3InLeftPanel.setPadding(new Insets(20, 0, 0, 0));

        HBox line4InLeftPanel = new HBox();
        line4InLeftPanel.setAlignment(Pos.CENTER_LEFT);
        Label uploadPictureLabel = I18nControls.newLabel(RecurringEventsI18nKeys.UploadFileDescription);
        uploadPictureLabel.setMinWidth(LABEL_WIDTH);

        HtmlText uploadText = new HtmlText();
        I18n.bindI18nTextProperty(uploadText.textProperty(), RecurringEventsI18nKeys.UploadFileDescription);
        VBox uploadTextVBox = new VBox(uploadText);
        uploadTextVBox.setAlignment(Pos.CENTER_LEFT);
        uploadTextVBox.setPadding(new Insets(0, 50, 0, 0));

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
        filePicker.getAcceptedExtensions().addAll("image/*");
        filePicker.setGraphic(uploadButton);
        filePicker.getSelectedFiles().addListener((InvalidationListener) obs -> {
            ObservableList<File> fileList = filePicker.getSelectedFiles();
            cloudPictureFileToUpload = fileList.get(0);
            Image imageToDisplay = new Image(cloudPictureFileToUpload.getObjectURL());
            isCloudPictureToBeUploaded.setValue(true);
            eventImageContainer.setContent(new ImageView(imageToDisplay));
            isPictureDisplayed.setValue(true);
        });

        Label uploadButtonDescription = Bootstrap.small(I18nControls.newLabel(RecurringEventsI18nKeys.SelectYourFile));

        TextTheme.createPrimaryTextFacet(uploadButtonDescription).style();

        VBox uploadButtonVBox = new VBox(filePicker.getView(), uploadButtonDescription);
        uploadButtonVBox.setAlignment(Pos.CENTER);
        uploadButtonVBox.setPadding(new Insets(0, 30, 0, 0));

        HBox imageAndTrashVBox = new HBox();
        imageAndTrashVBox.setSpacing(2);
        StackPane imageStackPane = new StackPane();
        imageStackPane.setMaxSize(EVENT_IMAGE_WIDTH, EVENT_IMAGE_HEIGHT);
        imageStackPane.setMinHeight(100);
        imageStackPane.setAlignment(Pos.CENTER);
        Label emptyPictureLabel = Bootstrap.small(I18nControls.newLabel(RecurringEventsI18nKeys.NoPictureSelected));
        TextTheme.createSecondaryTextFacet(emptyPictureLabel).style();

        trashImage = SvgIcons.armButton(SvgIcons.createTrashSVGPath(), () -> {
            isCloudPictureToBeDeleted.setValue(true);
            isCloudPictureToBeUploaded.setValue(false);
            eventImageContainer.setContent(null);
            isPictureDisplayed.setValue(false);
        });
        ShapeTheme.createSecondaryShapeFacet(trashImage).style(); // Make it gray

        imageStackPane.getChildren().setAll(emptyPictureLabel, eventImageContainer);
        StackPane.setAlignment(emptyPictureLabel, Pos.CENTER);
        imageAndTrashVBox.getChildren().setAll(imageStackPane, trashImage);
        imageAndTrashVBox.setAlignment(Pos.BOTTOM_CENTER);
        line4InLeftPanel.getChildren().setAll(uploadTextVBox, uploadButtonVBox, imageAndTrashVBox);
        line4InLeftPanel.setPadding(new Insets(20, 0, 0, 0));

        VBox leftPaneVBox = new VBox(line1InLeftPanel, locationHBox, shortDescriptionLineInLeftPanel, line3InLeftPanel, line4InLeftPanel);
        leftPaneVBox.setPadding(new Insets(0, 10, 0, 0));
        leftPaneVBox.setMaxWidth(Double.MAX_VALUE);
        // Ensuring the description html editor will grow horizontally and vertically
        HBox.setHgrow(shortDescriptionHtmlEditor, Priority.ALWAYS);
        HBox.setHgrow(descriptionHtmlEditor, Priority.ALWAYS); // horizontally
        VBox.setVgrow(line3InLeftPanel, Priority.ALWAYS); // vertically (via line3InLeftPanel)

        //The right pane (VBox)
        Label timeOfEventLabel = I18nControls.newLabel(RecurringEventsI18nKeys.TimeOfTheEvent);
        timeOfEventLabel.setPadding(new Insets(0, 50, 0, 0));
        timeOfTheEventTextField.setMaxWidth(60);
        timeOfTheEventTextField.textProperty().addListener(changeOnStartTimeOrDurationListener);


        Label durationLabel = I18nControls.newLabel(RecurringEventsI18nKeys.RecurringEventDuration);
        durationLabel.setPadding(new Insets(0, 50, 0, 50));
        durationTextField.setMaxWidth(40);
        durationTextField.textProperty().addListener(changeOnStartTimeOrDurationListener);


        HBox line1 = new HBox(timeOfEventLabel, timeOfTheEventTextField, durationLabel, durationTextField);
        line1.setAlignment(Pos.CENTER_LEFT);
        line1.setPadding(new Insets(0, 0, 20, 0));
        datesOfTheEventLabel = I18nControls.newLabel(BaseI18nKeys.Dates);
        datesOfTheEventLabel.setPadding(new Insets(0, 0, 5, 0));
        calendarPane = new EventCalendarPane();
        calendarPane.getDatePicker().getSelectedDates().addListener(onChangeDateListener);

        final int labelWidth = 200;
        Label externalLinkLabel = I18nControls.newLabel(RecurringEventsI18nKeys.ExternalLink);
        externalLinkLabel.setPadding(new Insets(0, 20, 0, 0));
        externalLinkLabel.setPrefWidth(labelWidth);
        externalLinkTextField.setPrefWidth(400);
        externalLinkTextField.textProperty().addListener(obs -> currentEditedEvent.setExternalLink(externalLinkTextField.getText()));
        HBox line4 = new HBox(externalLinkLabel, externalLinkTextField);
        line4.setPadding(new Insets(20, 0, 0, 0));
        line4.setAlignment(Pos.CENTER_LEFT);

        Label advertisedLabel = I18nControls.newLabel(RecurringEventsI18nKeys.Advertised);
        advertisedLabel.setPadding(new Insets(0, 20, 0, 0));
        advertisedLabel.setPrefWidth(labelWidth + 50);
        advertisedSwitch = new Switch();
        advertisedSwitch.selectedProperty().addListener(obs -> currentEditedEvent.setAdvertised(advertisedSwitch.selectedProperty().get()));

        Label publishLabel = I18nControls.newLabel(RecurringEventsI18nKeys.Published);
        publishLabel.setPadding(new Insets(0, 20, 0, 40));
        publishLabel.setPrefWidth(labelWidth + 60);
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

        HBox line5 = new HBox(advertisedLabel, advertisedSwitch, publishLabel, registrationOpenSwitch);
        line5.setPadding(new Insets(20, 0, 0, 0));
        line5.setAlignment(Pos.CENTER_LEFT);

        Label audioCorrespondanceProgramLabel = I18nControls.newLabel(RecurringEventsI18nKeys.AudioCorrespondenceProgram);
        audioCorrespondanceProgramLabel.setPadding(new Insets(0, 20, 0, 0));
        audioCorrespondanceProgramLabel.setPrefWidth(labelWidth + 50);
        audioCorrespondanceProgramSwitch = new Switch();
        audioCorrespondanceProgramSwitch.selectedProperty().addListener(obs -> currentEditedEvent.setRecurringWithAudio(audioCorrespondanceProgramSwitch.selectedProperty().get()));

        Label videoCorrespondanceProgramLabel = I18nControls.newLabel(RecurringEventsI18nKeys.VideoCorrespondenceProgram);
        videoCorrespondanceProgramLabel.setPadding(new Insets(0, 20, 0, 40));
        videoCorrespondanceProgramLabel.setPrefWidth(labelWidth + 60);
        videoCorrespondanceProgramSwitch = new Switch();
        videoCorrespondanceProgramSwitch.selectedProperty().addListener(obs -> currentEditedEvent.setRecurringWithVideo(videoCorrespondanceProgramSwitch.selectedProperty().get()));
        HBox line6 = new HBox(audioCorrespondanceProgramLabel, audioCorrespondanceProgramSwitch, videoCorrespondanceProgramLabel, videoCorrespondanceProgramSwitch);
        line6.setPadding(new Insets(20, 0, 0, 0));
        line6.setAlignment(Pos.CENTER_LEFT);

        VBox rightPaneVBox = new VBox(
            line1,
            datesOfTheEventLabel,
            calendarPane,
            line4InLeftPanel,
            line4,
            line5,
            line6);

        // ----FlexPane---------------------------------------|
        //|--VBox--------------------||---------Vbox---------||
        //|                          ||                      ||
        //| Name of event ...        ||  Time of event ...   ||
        //| Description   ...        ||  Date ...            ||
        //| Picture ...              ||                      ||
        //|--------------------------||----------------------||
        FlexPane eventDetailsPane = new FlexPane(leftPaneVBox, rightPaneVBox);
        eventDetailsPane.setHorizontalSpace(50);
        eventDetailsPane.setVerticalSpace(20);

        cancelButton = Bootstrap.largeSecondaryButton(I18nControls.newButton(RecurringEventsI18nKeys.CancelButton));
        cancelButton.setOnAction(e -> displayEventDetails(currentEditedEvent));
        cancelButton.disableProperty().bind(currentMode.map(mode -> mode.intValue() == ADD_MODE));

        saveButton = Bootstrap.largeSuccessButton(I18nControls.newButton(RecurringEventsI18nKeys.SaveButton));
        saveButton.setOnAction(event -> {
            if (validateForm()) {
                if (previousEventState == null) {
                    previousEventState = EventState.DRAFT;
                    currentEditedEvent.setState(EventState.DRAFT);
                }
                synchroniseAudioVideoScheduledItemLists();
                submitUpdateStoreChanges();
            }
        });

        HBox buttonsLine = new HBox(30, cancelButton, saveButton);
        buttonsLine.setPadding(new Insets(50, 0, 0, 0));
        buttonsLine.setAlignment(Pos.CENTER);

        HBox labelLine = new HBox();
        labelLine.setAlignment(Pos.BASELINE_LEFT);
        Region anotherSpacer = new Region();
        HBox.setHgrow(anotherSpacer, Priority.SOMETIMES);
        labelLine.getChildren().setAll(currentEventLabel, anotherSpacer, addButton);

        eventDetailsVBox = new VBox(titleHox, eventDetailsPane, buttonsLine);
        //When we launch the window, we don't display this VBox which contains an event details
        eventDetailsVBox.setVisible(false);
        eventDetailsVBox.setManaged(false);
        VBox mainVBox = new VBox(labelLine, eventTable, eventDetailsVBox);
        eventTable.setFullHeight(true);
        mainFrame.setCenter(mainVBox);

        initFormValidation();

        return Controls.createVerticalScrollPaneWithPadding(10, mainFrame);
    }

    private void synchroniseAudioVideoScheduledItemLists() {
        //We synchronise the audios and videos scheduled Item with the teachings ScheduledItem in the program
        teachingsWorkingScheduledItems.forEach(teachingScheduledItem -> {
            //First the audio
            if (currentEditedEvent.isRecurringWithAudio() != null && currentEditedEvent.isRecurringWithAudio()) {
                //Here we synchronise the audio
                //1st case, we find an audioScheduledItem whose teachingScheduledItem is the current teaching ScheduledItem or the date is the same
                if (audioWorkingScheduledItems.stream().anyMatch(currentAudioScheduledItem -> currentAudioScheduledItem.getProgramScheduledItem().equals(teachingScheduledItem) || currentAudioScheduledItem.getDate().equals(teachingScheduledItem.getDate()))) {
                    ScheduledItem audioScheduledItem = audioWorkingScheduledItems.stream().filter(currentAudioScheduledItem -> currentAudioScheduledItem.getDate().equals(teachingScheduledItem.getDate())).findFirst().get();
                    //We define the link to the teachingScheduledItem again in case the teaching scheduleItem has been deleted or recreated, as long it is the same date, we know it refers to the teachings ScheduledItem
                    audioScheduledItem.setProgramScheduledItem(teachingScheduledItem);
                    audioScheduledItem.setBookableScheduledItem(teachingScheduledItem);
                } else {
                    //we need to create one
                    ScheduledItem audioScheduledItem = updateStore.insertEntity(ScheduledItem.class);
                    audioScheduledItem.setDate(teachingScheduledItem.getDate());
                    audioScheduledItem.setSite(teachingScheduledItem.getSite());
                    audioScheduledItem.setEvent(teachingScheduledItem.getEvent());
                    audioScheduledItem.setItem(audioItemId);
                    audioScheduledItem.setBookableScheduledItem(teachingScheduledItem);
                    audioScheduledItem.setProgramScheduledItem(teachingScheduledItem);
                    audioWorkingScheduledItems.add(audioScheduledItem);
                }
            }
            //Then the video
            if (currentEditedEvent.isRecurringWithVideo() != null && currentEditedEvent.isRecurringWithVideo()) {
                //1st case, we find an videoScheduledItem whose teachingScheduledItem is the current teaching ScheduledItem or the date is the same
                if (videoWorkingScheduledItems.stream().anyMatch(currentVideoScheduledItem -> currentVideoScheduledItem.getProgramScheduledItem().equals(teachingScheduledItem) || currentVideoScheduledItem.getDate().equals(teachingScheduledItem.getDate()))) {
                    ScheduledItem videoScheduledItem = videoWorkingScheduledItems.stream().filter(currentVideoScheduledItem -> currentVideoScheduledItem.getDate().equals(teachingScheduledItem.getDate())).findFirst().get();
                    //We define the link to the teachingScheduledItem again in case the teaching scheduleItem has been deleted or recreated, as long it is the same date, we know it refers to the teachings ScheduledItem
                    videoScheduledItem.setProgramScheduledItem(teachingScheduledItem);
                    videoScheduledItem.setBookableScheduledItem(teachingScheduledItem);
                } else {
                    //we need to create one
                    ScheduledItem videoScheduledItem = updateStore.insertEntity(ScheduledItem.class);
                    videoScheduledItem.setDate(teachingScheduledItem.getDate());
                    videoScheduledItem.setSite(teachingScheduledItem.getSite());
                    videoScheduledItem.setEvent(teachingScheduledItem.getEvent());
                    videoScheduledItem.setItem(videoItemId);
                    videoScheduledItem.setBookableScheduledItem(teachingScheduledItem);
                    videoScheduledItem.setProgramScheduledItem(teachingScheduledItem);
                    videoWorkingScheduledItems.add(videoScheduledItem);
                }
            }
        });

        //Then we delete the one that are not in the program
        //First audio
        if (currentEditedEvent.isRecurringWithAudio() != null && currentEditedEvent.isRecurringWithAudio()) {
            audioWorkingScheduledItems.forEach(audioScheduledItem -> {
                if (teachingsWorkingScheduledItems.stream().noneMatch(currentTeachingScheduledItem -> audioScheduledItem.getDate().equals(currentTeachingScheduledItem.getDate()))) {
                    updateStore.deleteEntity(audioScheduledItem);
                }
            });
        }
        //then video
        if (currentEditedEvent.isRecurringWithVideo() != null && currentEditedEvent.isRecurringWithVideo()) {
            videoWorkingScheduledItems.forEach(videoScheduledItem -> {
                if (teachingsWorkingScheduledItems.stream().noneMatch(currentTeachingScheduledItem -> videoScheduledItem.getDate().equals(currentTeachingScheduledItem.getDate()))) {
                    updateStore.deleteEntity(videoScheduledItem);
                }
            });
        }

        //Case where the recurringEvent has no audio associated
        if (currentEditedEvent.isRecurringWithAudio() == null || !currentEditedEvent.isRecurringWithAudio()) {
            audioWorkingScheduledItems.forEach(updateStore::deleteEntity);
            audioWorkingScheduledItems.clear();
        }
        //Case where the recurringEvent has no video associated
        if (currentEditedEvent.isRecurringWithVideo() == null || !currentEditedEvent.isRecurringWithVideo()) {
            videoWorkingScheduledItems.forEach(updateStore::deleteEntity);
            videoWorkingScheduledItems.clear();
        }
    }

    private void submitUpdateStoreChanges() {
        // Unbinding buttons, so they can be displayed as disabled during the process
        saveButton.disableProperty().unbind();
        cancelButton.disableProperty().unbind();
        OperationUtil.turnOnButtonsWaitModeDuringExecution(
            updateStore.submitChanges()
                .inUiThread()
                .onFailure(ex -> {
                    DialogContent dialog = DialogContent.createConfirmationDialog("Error", "Operation failed", ex.getMessage());
                    dialog.setOk();
                    DialogBuilderUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
                    dialog.getPrimaryButton().setOnAction(a -> dialog.getDialogCallback().closeDialog());
                    Console.log(ex);
                })
                .onSuccess(x -> {
                    String cloudImagePath = ModalityCloudinary.eventImagePath(currentEditedEvent);
                    deleteCloudPictureIfNecessary(cloudImagePath);
                    uploadCloudPictureIfNecessary(cloudImagePath);
                    isCloudPictureToBeDeleted.setValue(false);
                    isCloudPictureToBeUploaded.setValue(false);
                    cloudPictureFileToUpload = null;
                    eventVisualMapper.requestSelectedEntity(currentEditedEvent);
                    // displayEventDetails(currentEditedEvent);
                })
                // Reestablishing buttons binding
                .onComplete(ar -> bindButtons())
            , saveButton, cancelButton);
    }

    /**
     * This method is used to sort the list workingScheduledItems by Date
     */
    private void sortTeachingWorkingScheduledItemsByDate() {
        teachingsWorkingScheduledItems.sort(Comparator.comparing(EntityHasLocalDate::getDate));
    }

    /**
     * This private class is used to display the calendar
     * containing the datePicker, the separator vertical line, and the list of start time
     */
    private class EventCalendarPane extends Pane {
        Label daySelected = I18nControls.newLabel(RecurringEventsI18nKeys.DaysSelected);
        Label selectEachDayLabel = I18nControls.newLabel(RecurringEventsI18nKeys.SelectTheDays);
        Line verticalLine;
        VBox recurringEventsVBox = new VBox();
        ScrollPane recurringEventsScrollPane = new ScrollPane();
        DatePicker datePicker = new DatePicker(new DatePickerOptions()
            .setMultipleSelectionAllowed(true)
            .setPastDatesSelectionAllowed(true)
            .setApplyBorderStyle(false)
            .setApplyMaxSize(false)
            .setSortSelectedDates(true)
        );

        public EventCalendarPane() {
            TextTheme.createSecondaryTextFacet(selectEachDayLabel).style();
            TextTheme.createSecondaryTextFacet(daySelected).style();
            setMaxWidth(500);
            setMinWidth(500);
            teachingsWorkingScheduledItems.addListener((ListChangeListener<ScheduledItem>) change -> {
                // We call the listener only when the object has been loaded and not during the construction
                // ie when currentEditedEvent=currentSelectedEvent
                // isWorkingScheduledItemEmpty.set(workingScheduledItems.isEmpty());
                if (currentEditedEvent != null && (currentEditedEvent == currentObservedEvent)) {
                    recurringEventsVBox.getChildren().clear();
                    List<LocalDate> dates = teachingsWorkingScheduledItems.stream().map(EntityHasLocalDate::getDate).collect(Collectors.toList());
                    if (isTeachingsWorkingScheduledItemEmpty.not().getValue()) {
                        if (!Collections.min(dates).equals(currentEditedEvent.getStartDate()))
                            currentEditedEvent.setStartDate(Collections.min(dates));
                        if (!Collections.max(dates).equals(currentEditedEvent.getEndDate()))
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
            getChildren().setAll(
                selectEachDayLabel,
                daySelected,
                verticalLine,
                datePicker.getView(),
                recurringEventsScrollPane
            );

            onChangeDateListener = change -> {
                if (currentEditedEvent != null && currentEditedEvent == currentObservedEvent) {
                    while (change.next()) {
                        if (change.wasAdded()) {
                            LocalDate date = change.getAddedSubList().get(0);
                            //Here we haven't found it in the scheduledItemsReadFromDatabase, so we create it.
                            ScheduledItem teachingScheduledItem = updateStore.insertEntity(ScheduledItem.class);
                            teachingScheduledItem.setDate(date);
                            teachingScheduledItem.setSite(eventSite);
                            teachingScheduledItem.setEvent(currentEditedEvent);
                            teachingScheduledItem.setItem(recurringItem);
                            teachingScheduledItem.setStartTime(defaultStartTime);
                            teachingScheduledItem.setEndTime(defaultEndTime);
                            //We set up the name of the scheduledItem to the name of the event. This name will be used for the audios and videos linked scheduledItem if there are
                            teachingScheduledItem.setName(currentEditedEvent.getName());
                            teachingsWorkingScheduledItems.add(teachingScheduledItem);
                            sortTeachingWorkingScheduledItemsByDate();
                        }
                        if (change.wasRemoved()) {
                            //We remove from the updateStore and the ScheduledItem
                            LocalDate date = change.getRemoved().get(0);
                            for (ScheduledItem currentScheduledItem : teachingsWorkingScheduledItems) {
                                if (currentScheduledItem.getDate().equals(date)) {
                                    updateStore.deleteEntity(currentScheduledItem);
                                    teachingsWorkingScheduledItems.remove(currentScheduledItem);
                                    break;
                                }
                            }
                        }

                        List<LocalDate> localDatesSorted = calendarPane.getDatePicker().getSelectedDates().stream().sorted().collect(Collectors.toList());
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
            ObservableLists.bindConverted(recurringEventsVBox.getChildren(), teachingsWorkingScheduledItems, this::drawScheduledItem);
        }
        //TODO, we replace the listener added in the function bellow by a class Listener qui parcours la liste des workingScheduledItems,
        // et récupère le textField Correspondant dans la récurringEventsVBox (même indice i), puis on travaille avec cet élément.

        /**
         * This method is used to return a BorderPane that contains
         * UI elements of the scheduledItem, the Date is read Only,
         * the startTime is editable.
         */
        private BorderPane drawScheduledItem(ScheduledItem scheduledItem) {
            LocalDate date = scheduledItem.getDate();
            Text dateText = new Text();
            dateText.textProperty().bind(LocalizedTime.formatMonthDayProperty(date, RECURRING_EVENT_SCHEDULED_ITEM_DATE_FORMAT));

            SVGPath trashDate = SvgIcons.createTrashSVGPath();
            Facet facet = ShapeTheme.createSecondaryShapeFacet(trashDate).style();

            TextField currentScheduleItemStartTime = new TextField();

            if (scheduledItem.getFieldValue("attendance") == null) {
                SvgIcons.armButton(trashDate, () -> datePicker.getSelectedDates().remove(date));
            } else {
                facet.setDisabled(true);
                currentScheduleItemStartTime.setDisable(true);
            }

            //We add a listener to update the value of the scheduled item when the text field is changed
            currentScheduleItemStartTime.textProperty().addListener(obs -> {
                if ((isLocalTimeTextValid(currentScheduleItemStartTime.getText()) && (isIntegerValid(durationTextField.getText())))) {
                    LocalTime startTime = LocalTime.parse(currentScheduleItemStartTime.getText());
                    scheduledItem.setStartTime(startTime);
                    scheduledItem.setEndTime(startTime.plusMinutes(Integer.parseInt(durationTextField.getText())));
                }
            });

            currentScheduleItemStartTime.setAlignment(Pos.CENTER);
            currentScheduleItemStartTime.setMaxWidth(90);

            if (scheduledItem.getStartTime() == null) {
                scheduledItem.setStartTime(defaultStartTime);
                scheduledItem.setEndTime(defaultEndTime);
            }
            //If we're still at null here, it means the defaultStartTime is set to null
            if (scheduledItem.getStartTime() == null) {
                currentScheduleItemStartTime.setPromptText("HH:mm");
            } else if (scheduledItem.getStartTime().equals(defaultStartTime)) {
                currentScheduleItemStartTime.setPromptText(LocalizedTime.formatLocalTime(scheduledItem.getStartTime(), RECURRING_EVENT_SCHEDULED_ITEM_TIME_FORMAT));
            } else {
                currentScheduleItemStartTime.setText(LocalizedTime.formatLocalTime(scheduledItem.getStartTime(), RECURRING_EVENT_SCHEDULED_ITEM_TIME_FORMAT));
            }
            BorderPane currentLineBorderPane = new BorderPane();
            BorderPane.setMargin(dateText, new Insets(0, 20, 0, 10));
            currentLineBorderPane.setLeft(trashDate);
            currentLineBorderPane.setCenter(dateText);
            currentLineBorderPane.setRight(currentScheduleItemStartTime);
            currentLineBorderPane.setPadding(new Insets(0, 0, 3, 0));
            recurringEventsVBox.getChildren().add(currentLineBorderPane);
            return currentLineBorderPane;
        }


        public DatePicker getDatePicker() {
            return datePicker;
        }

        public VBox getRecurringEventsVBox() {
            return recurringEventsVBox;
        }

        /**
         * We override this method to display the Pane as we want
         */
        protected void layoutChildren() {
            layoutInArea(selectEachDayLabel, 20, 0, 260, 30, 0, HPos.CENTER, VPos.CENTER);
            layoutInArea(daySelected, 280, 0, 250, 30, 0, HPos.CENTER, VPos.CENTER);
            layoutInArea(datePicker.getView(), 0, 20, 280, 500, 0, HPos.CENTER, VPos.CENTER);
            layoutInArea(verticalLine, 280, 35, 10, 250, 0, HPos.CENTER, VPos.TOP);
            layoutInArea(recurringEventsScrollPane, 300, 35, 200, 180, 0, HPos.CENTER, VPos.TOP);
        }

        protected double computePrefHeight(double width) {
            return super.computePrefHeight(width) + 10;
        }

    }

    /**
     * Test if a string is a format that is ready to be converted in LocalTime by the method LocalTime.Parse
     *
     * @param text the string to be tested
     * @return true, is the string parameter can be converted in LocalTime, false otherwise
     */
    private static boolean isLocalTimeTextValid(String text) {
        try {
            LocalTime.parse(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * We test here if url is valid
     *
     * @param url the url to test
     * @return true if the url is valid, false otherwise
     */
    public static boolean isValidUrl(String url) {
        if (Objects.equals(url, "") || url == null)
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
     *
     * @param text the string to be tested
     * @return true, is the string parameter can be converted in Integer, false otherwise
     */
    private static boolean isIntegerValid(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

