package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.shape.ShapeTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.pickers.DatePicker;
import dev.webfx.extras.time.pickers.DatePickerOptions;
import dev.webfx.extras.util.OptimizedObservableListWrapper;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.validation.ModalityValidationSupport;
import one.modality.base.shared.entities.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class DayTemplatePanel {

    private final DayTemplate dayTemplate;
    private final ProgramPanel programPanel;

    private final List<Timeline> initialWorkingTemplateTimelines = new ArrayList<>();
    private final ObservableList<Timeline> workingTemplateTimelines = new OptimizedObservableListWrapper<>();
    private final ObservableList<DayTemplateTimelineView> workingDayTemplateTimelineViews = FXCollections.observableArrayList();
    {
        ObservableLists.bindConverted(workingDayTemplateTimelineViews, workingTemplateTimelines, timeline -> new DayTemplateTimelineView(timeline, this));
    }

    private final DatePicker datePicker = new DatePicker(new DatePickerOptions()
        .setMultipleSelectionAllowed(true)
        .setPastDatesSelectionAllowed(false)
        .setApplyBorderStyle(false)
        .setApplyMaxSize(false)
        .setSortSelectedDates(true)
    );
    private final ObservableList<DayTemplateDateView> workingDayTemplateDateViews = FXCollections.observableArrayList();
    {
        ObservableLists.bindConverted(workingDayTemplateDateViews, datePicker.getSelectedDates(), date -> new DayTemplateDateView(date, datePicker));
    }

    private final BorderPane mainContainer;
    private final TextField templateNameTextField = new TextField();

    DayTemplatePanel(DayTemplate dayTemplate, ProgramPanel programPanel) {
        this.dayTemplate = dayTemplate;
        this.programPanel = programPanel;
        mainContainer = buildUi();
        initFormValidation();
        LocalDate eventStartDate = getEvent().getStartDate();
        datePicker.setDisplayedYearMonth(YearMonth.of(eventStartDate.getYear(), eventStartDate.getMonth()));
        startLogic();
    }

    BorderPane getPanel() {
        return mainContainer;
    }

    public DayTemplate getDayTemplate() {
        return dayTemplate;
    }

    public ProgramPanel getProgramPanel() {
        return programPanel;
    }

    private Event getEvent() {
        return dayTemplate.getEvent();
    }

    ModalityValidationSupport getValidationSupport() {
        return programPanel.validationSupport;
    }

    private Site getSite() {
        return programPanel.programSite;
    }

    private Item getVideoItem() {
        return programPanel.videoItem;
    }

    private UpdateStore getUpdateStore() {
        return programPanel.updateStore;
    }

    private void startLogic() {
        //We read the value of the database for the child elements only if the dayTemplate is already existing in the database (ie not in cache)
        if (!dayTemplate.getId().isNew()) {
            programPanel.entityStore.<Timeline>executeQuery(
                    "select item, dayTemplate, startTime, endTime, videoOffered, audioOffered, name, site, eventTimeline from Timeline where dayTemplate=? order by startTime"
                    , dayTemplate
                )
                .onFailure(Console::log)
                .onSuccess(timelines -> Platform.runLater(() -> {
                    Collections.setAll(initialWorkingTemplateTimelines, timelines.stream().map(getUpdateStore()::updateEntity).collect(Collectors.toList()));
                    resetModelAndUiToInitial();
                }));
        }
    }

    void resetModelAndUiToInitial() {
        workingTemplateTimelines.setAll(initialWorkingTemplateTimelines);
        workingDayTemplateTimelineViews.forEach(DayTemplateTimelineView::resetModelAndUiToInitial);
        syncUiFromModel();
    }

    private void syncUiFromModel() {
        syncTemplateNameUiFromModel();
        syncSelectedDatesUiFromModel();
        //Here we test if the scheduled item have already been generated, ie if at least one of the workingTemplateTimelines got an eventTimeLine not null
        boolean hasEventTimeline = workingTemplateTimelines.stream()
            .anyMatch(timeline -> timeline.getEventTimeline() != null);
        if (hasEventTimeline)
            programPanel.programGeneratedProperty.setValue(true);
    }

    private void syncModelFromUi() {
        syncTemplateNameModelFromUi();
    }

    private void syncTemplateNameUiFromModel() {
        templateNameTextField.setText(dayTemplate.getName());
    }

    private void syncTemplateNameModelFromUi() {
        dayTemplate.setName(templateNameTextField.getText());
    }

    private void syncSelectedDatesUiFromModel() {
        if (dayTemplate.getDates() != null) {
            datePicker.setSelectedDates(DatesToStringConversion.getDateList(dayTemplate.getDates()));
        }
    }


    private BorderPane buildUi() {
        //****************************  TIMELINES VBOX ******************************************//
        VBox timelinesContainer = new VBox(5);
        timelinesContainer.setFillWidth(true);
        ObservableLists.bindConverted(timelinesContainer.getChildren(), workingDayTemplateTimelineViews, DayTemplateTimelineView::getView);

        //****************************  SELECTED DATES VBOX  ******************************************//
        VBox listOfSelectedDatesVBox = new VBox();
        listOfSelectedDatesVBox.setAlignment(Pos.CENTER);
        ObservableLists.bindConverted(listOfSelectedDatesVBox.getChildren(), workingDayTemplateDateViews, DayTemplateDateView::getView);

        //****************************  TOP  ******************************************//
        Label duplicateButton = I18nControls.bindI18nProperties(new Label(), "DuplicateIcon");
        duplicateButton.setOnMouseClicked(e -> duplicate());
        duplicateButton.setCursor(Cursor.HAND);

        //templateNameTextField.setMinWidth(350);
        Separator topSeparator = new Separator();
        topSeparator.setPadding(new Insets(10, 0, 10, 0));

        templateNameTextField.setPromptText("Name this template");
        HBox.setHgrow(templateNameTextField, Priority.ALWAYS);
        syncTemplateNameUiFromModel();
        FXProperties.runOnPropertiesChange(this::syncTemplateNameModelFromUi, templateNameTextField.textProperty());

        HBox topLine = new HBox(20, templateNameTextField, duplicateButton);

        Line verticalLine = new Line();
        verticalLine.setStartY(0);
        verticalLine.setEndY(180);
        verticalLine.setStroke(Color.LIGHTGRAY);

        //****************************  CENTER  ******************************************//
        BorderPane centerBorderPane = new BorderPane();
        centerBorderPane.setTop(timelinesContainer);
        SVGPath plusButton = SvgIcons.createPlusPath();
        //TODO change when a generic function has been created
        plusButton.setFill(Color.web("#0096D6"));
        MonoPane buttonContainer = new MonoPane(plusButton);
        buttonContainer.setOnMouseClicked(e -> addTemplateTimeline());
        buttonContainer.setCursor(Cursor.HAND);
        buttonContainer.setPadding(new Insets(10, 0, 0, 0));
        BorderPane.setAlignment(buttonContainer, Pos.CENTER_LEFT);
        centerBorderPane.setCenter(buttonContainer);
        BorderPane.setAlignment(buttonContainer, Pos.TOP_LEFT);

        Label deleteDayTemplate = I18nControls.bindI18nProperties(new Label(), "DeleteDayTemplate");
        deleteDayTemplate.setPadding(new Insets(10, 0, 5, 0));
        deleteDayTemplate.getStyleClass().add(Bootstrap.SMALL);
        deleteDayTemplate.getStyleClass().add(Bootstrap.TEXT_DANGER);
        deleteDayTemplate.setOnMouseClicked(e -> deleteDayTemplate());
        deleteDayTemplate.setCursor(Cursor.HAND);
        deleteDayTemplate.setPadding(new Insets(30, 0, 0, 0));

        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        VBox bottomVBox = new VBox(deleteDayTemplate, separator);
        centerBorderPane.setBottom(bottomVBox);
        BorderPane.setAlignment(bottomVBox, Pos.BOTTOM_LEFT);
        centerBorderPane.setMaxHeight(Region.USE_PREF_SIZE);

        //****************************  BOTTOM  ******************************************//
        BorderPane bottomBorderPane = new BorderPane();
        BorderPane.setAlignment(bottomBorderPane, Pos.CENTER);
        bottomBorderPane.setMaxWidth(600);
        Label assignDateLabel = I18nControls.bindI18nProperties(new Label(), "AssignDay");
        TextTheme.createPrimaryTextFacet(assignDateLabel).style();
        assignDateLabel.getStyleClass().add(Bootstrap.SMALL);
        assignDateLabel.setPadding(new Insets(5, 0, 10, 0));

        ScrollPane listOfSelectedDatesVBoxScrollPane = new ScrollPane(listOfSelectedDatesVBox);
        listOfSelectedDatesVBoxScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        listOfSelectedDatesVBoxScrollPane.setMaxHeight(150);
        listOfSelectedDatesVBoxScrollPane.setMinWidth(120);

        bottomBorderPane.setTop(assignDateLabel);
        BorderPane.setAlignment(assignDateLabel, Pos.CENTER);
        bottomBorderPane.setCenter(datePicker.getView());
        BorderPane.setAlignment(datePicker.getView(), Pos.CENTER);
        Separator verticalSeparator = new Separator(Orientation.VERTICAL);
        verticalSeparator.setPadding(new Insets(0, 0, 0, 40));
        HBox listOfDatesHBox = new HBox(verticalSeparator, listOfSelectedDatesVBoxScrollPane);
        listOfDatesHBox.setSpacing(40);
        listOfDatesHBox.setAlignment(Pos.CENTER);
        bottomBorderPane.setRight(listOfDatesHBox);
        BorderPane.setAlignment(listOfDatesHBox, Pos.CENTER);

        //We define the behaviour when we add or remove a date
        datePicker.getSelectedDates().addListener((ListChangeListener<LocalDate>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    // Handle added dates
                    for (LocalDate date : change.getAddedSubList()) {
                        dayTemplate.setDates(DatesToStringConversion.addDate(dayTemplate.getDates(), date));
                    }
                }
                if (change.wasRemoved()) {
                    // Handle removed dates
                    for (LocalDate date : change.getRemoved()) {
                        dayTemplate.setDates(DatesToStringConversion.removeDate(dayTemplate.getDates(), date));
                    }
                }
            }
        });

        BorderPane mainContainer = new BorderPane();
        mainContainer.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY,
            BorderStrokeStyle.SOLID, new CornerRadii(10), BorderWidths.DEFAULT)));
        mainContainer.setPadding(new Insets(10, 10, 10, 10));
        mainContainer.setTop(new VBox(topLine, topSeparator));
        mainContainer.setCenter(centerBorderPane);
        mainContainer.setMaxHeight(Region.USE_PREF_SIZE);
        mainContainer.setBottom(bottomBorderPane);

        return mainContainer;
    }

    void initFormValidation() {
        getValidationSupport().addRequiredInput(templateNameTextField);
    }

    private void duplicate() {
        DayTemplate duplicateDayTemplate = getUpdateStore().insertEntity(DayTemplate.class);
        duplicateDayTemplate.setName(dayTemplate.getName() + " - copy");
        duplicateDayTemplate.setEvent(dayTemplate.getEvent());
        programPanel.workingDayTemplates.add(duplicateDayTemplate);
        DayTemplatePanel newDTP = Collections.last(programPanel.workingDayTemplatePanels);
        for (Timeline timelineItem : workingTemplateTimelines) {
            Timeline newTimeline = getUpdateStore().insertEntity(Timeline.class);
            newTimeline.setItem(timelineItem.getItem());
            newTimeline.setStartTime(timelineItem.getStartTime());
            newTimeline.setEndTime(timelineItem.getEndTime());
            newTimeline.setAudioOffered(timelineItem.isAudioOffered());
            newTimeline.setVideoOffered(timelineItem.isVideoOffered());
            newTimeline.setSite(timelineItem.getSite());
            newTimeline.setName(timelineItem.getName());
            newTimeline.setDayTemplate(duplicateDayTemplate);
            newDTP.workingTemplateTimelines.add(newTimeline);
        }
    }

    private void addTemplateTimeline() {
        Timeline newTimeLine = getUpdateStore().insertEntity(Timeline.class);
        newTimeLine.setAudioOffered(false);
        newTimeLine.setVideoOffered(false);
        newTimeLine.setDayTemplate(dayTemplate);
        newTimeLine.setSite(getSite());
        workingTemplateTimelines.add(newTimeLine);
    }

    ScheduledItem addTeachingsScheduledItemsForDateAndTimeline(LocalDate date, String name, Timeline timeline, UpdateStore currentUpdateStore) {
        ScheduledItem teachingScheduledItem = currentUpdateStore.insertEntity(ScheduledItem.class);
        teachingScheduledItem.setEvent(getEvent());
        teachingScheduledItem.setSite(getSite());
        teachingScheduledItem.setDate(date);
        teachingScheduledItem.setName(name);
        teachingScheduledItem.setTimeLine(timeline);
        teachingScheduledItem.setItem(timeline.getItem());
        return teachingScheduledItem;
    }

    private void deleteDayTemplate() {
        programPanel.deleteDayTemplate(this);
    }

    void deleteTimelines(UpdateStore updateStore) {
        workingTemplateTimelines.forEach(currentTemplateTimeline -> {
            Timeline templateTimelineToUpdate = updateStore.updateEntity(currentTemplateTimeline);
            templateTimelineToUpdate.setEventTimeline(null);
            Timeline eventTimeLine = currentTemplateTimeline.getEventTimeline();
            updateStore.deleteEntity(eventTimeLine);
        });
    }

    void removeTemplateTimeLineLinkedToDayTemplate() {
        workingTemplateTimelines.removeIf(timeline -> (timeline.getDayTemplate() == dayTemplate));
    }

    void removeTemplateTimeLine(Timeline timeline) {
        getUpdateStore().deleteEntity(timeline);
        workingTemplateTimelines.remove(timeline);
    }

    void generateProgram(List<Timeline> newlyCreatedEventTimelines, UpdateStore updateStore) {
        workingTemplateTimelines.forEach(templateTimeline -> {
            LocalTime startTime = templateTimeline.getStartTime();
            LocalTime endTime = templateTimeline.getEndTime();
            Item item = templateTimeline.getItem();
            Timeline templateTimelineToEdit = updateStore.updateEntity(templateTimeline);
            Timeline eventTimeLine = newlyCreatedEventTimelines.stream()
                .filter(timeline ->
                    timeline.getStartTime().equals(startTime) &&
                    timeline.getEndTime().equals(endTime) &&
                    timeline.getItem().equals(item)
                )
                .findFirst()
                .orElse(null);
            if (eventTimeLine == null) {
                //Here we create an event timeline
                eventTimeLine = updateStore.insertEntity(Timeline.class);
                eventTimeLine.setEvent(getEvent());
                eventTimeLine.setSite(templateTimeline.getSite());
                eventTimeLine.setItem(item);
                eventTimeLine.setStartTime(startTime);
                eventTimeLine.setEndTime(endTime);
                eventTimeLine.setItemFamily(templateTimeline.getItemFamily());
                newlyCreatedEventTimelines.add(eventTimeLine);
            }
            templateTimelineToEdit.setEventTimeline(eventTimeLine);
            //Now, we create the associated scheduledItem
            for (LocalDate date : datePicker.getSelectedDates()) {
                ScheduledItem teachingScheduledItem = addTeachingsScheduledItemsForDateAndTimeline(date, templateTimeline.getName(), eventTimeLine, updateStore);
                if (templateTimeline.isAudioOffered()) {
                    addAudioScheduledItemsForDate(date, teachingScheduledItem, updateStore);
                }
                if (templateTimeline.isVideoOffered()) {
                    addVideoScheduledItemsForDate(date, teachingScheduledItem);
                }
            }
        });
    }

    void addAudioScheduledItemsForDate(LocalDate date, ScheduledItem parentTeachingScheduledItem, UpdateStore currentUpdateStore) {
        //Here we add for each language not deprecated the scheduledItemAssociated to the date and parent scheduledItem*
        programPanel.languageAudioItems.forEach(languageItem -> {
            ScheduledItem audioScheduledItem = currentUpdateStore.insertEntity(ScheduledItem.class);
            audioScheduledItem.setEvent(getEvent());
            audioScheduledItem.setSite(getSite());
            audioScheduledItem.setDate(date);
            audioScheduledItem.setParent(parentTeachingScheduledItem);
            audioScheduledItem.setItem(languageItem);
        });
    }

    void addVideoScheduledItemsForDate(LocalDate date, ScheduledItem parentTeachingScheduledItem) {
        ScheduledItem videoScheduledItem = getUpdateStore().insertEntity(ScheduledItem.class);
        videoScheduledItem.setEvent(getEvent());
        videoScheduledItem.setSite(getSite());
        videoScheduledItem.setItem(getVideoItem());
        videoScheduledItem.setDate(date);
        videoScheduledItem.setParent(parentTeachingScheduledItem);
    }

}
