package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.shape.ShapeTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.pickers.DatePicker;
import dev.webfx.extras.time.pickers.DatePickerOptions;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
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
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.validation.ModalityValidationSupport;
import one.modality.base.shared.entities.*;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class DayTemplatePanel {

    private final DayTemplate dayTemplate;
    private final ProgramPanel programPanel;
    private final BorderPane mainContainer = new BorderPane();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final TextField templateNameTextField;
    final ObservableList<Timeline> workingTemplateTimelines = FXCollections.observableArrayList();
    private final VBox listOfSelectedDatesVBox;
    DatePicker datePicker;

    DayTemplatePanel(DayTemplate dayTemplate, ProgramPanel programPanel) {
        this.dayTemplate = dayTemplate;
        this.programPanel = programPanel;

        VBox timelinesContainer = new VBox();

        ObservableLists.bindConverted(timelinesContainer.getChildren(), workingTemplateTimelines, this::drawSelectedDatesAsLine);

        //We read the value of the database for the child elements only if the dayTemplate is already existing in the database (ie not in cache)
        if (!dayTemplate.getId().isNew()) {
            programPanel.entityStore.<Timeline>executeQuery(
                    "select item, dayTemplate, startTime, endTime, videoOffered, audioOffered, name, site, eventTimeline from Timeline where dayTemplate=? order by startTime"
                    , dayTemplate
                )
                .onFailure(Console::log)
                .onSuccess(timelines -> Platform.runLater(() -> {
                    //timelinesReadFromDatabase =
                    workingTemplateTimelines.setAll(timelines);
                    if (dayTemplate.getDates() != null) {
                        datePicker.setSelectedDates(DatesToStringConversion.getDateList(dayTemplate.getDates()));
                    }
                    //Here we test if the scheduled item have already been generated, ie if at least one of the workingTemplateTimelines got an eventTimeLine not null
                    boolean hasEventTimeline = workingTemplateTimelines.stream()
                        .anyMatch(timeline -> timeline.getEventTimeline() != null);
                    if (hasEventTimeline)
                        programPanel.programGeneratedProperty.setValue(true);
                }));
        } else {
            //Here we want only the language
        }


        //****************************  TOP ******************************************//
        HBox topLine = new HBox();

        Label duplicateButton = I18nControls.bindI18nProperties(new Label(), "DuplicateIcon");
        duplicateButton.setOnMouseClicked(e -> {
            DayTemplate duplicateDayTemplate = getUpdateStore().insertEntity(DayTemplate.class);
            duplicateDayTemplate.setName(dayTemplate.getName() + " - copy");
            duplicateDayTemplate.setEvent(dayTemplate.getEvent());
            programPanel.workingDayTemplates.add(duplicateDayTemplate);
            DayTemplatePanel newDTP = programPanel.getOrCreateDayTemplatePanel(duplicateDayTemplate);
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
        });
        duplicateButton.setCursor(Cursor.HAND);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.SOMETIMES);

        templateNameTextField = new TextField();
        templateNameTextField.setMinWidth(350);
        Separator topSeparator = new Separator();
        topSeparator.setPadding(new Insets(10, 0, 10, 0));
        templateNameTextField.setPromptText("Name this template");
        templateNameTextField.setText(dayTemplate.getName());
        templateNameTextField.setOnMouseExited(e -> dayTemplate.setName(templateNameTextField.getText()));
        topLine.getChildren().setAll(templateNameTextField, spacer, duplicateButton);

        Line verticalLine = new Line();
        verticalLine.setStartY(0);
        verticalLine.setEndY(180);
        verticalLine.setStroke(Color.LIGHTGRAY);

        mainContainer.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY,
            BorderStrokeStyle.SOLID, new CornerRadii(10), BorderWidths.DEFAULT)));
        mainContainer.setPadding(new Insets(10, 10, 10, 10));
        mainContainer.setTop(new VBox(topLine, topSeparator));

        //****************************  CENTER ******************************************//
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
        deleteDayTemplate.setOnMouseClicked(e -> deleteDayTemplate(dayTemplate));
        deleteDayTemplate.setCursor(Cursor.HAND);
        deleteDayTemplate.setPadding(new Insets(30, 0, 0, 0));

        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        VBox bottomVBox = new VBox(deleteDayTemplate, separator);
        centerBorderPane.setBottom(bottomVBox);
        BorderPane.setAlignment(bottomVBox, Pos.BOTTOM_LEFT);
        centerBorderPane.setMaxHeight(Region.USE_PREF_SIZE);

        mainContainer.setCenter(centerBorderPane);
        mainContainer.setMaxHeight(Region.USE_PREF_SIZE);

        //****************************  BOTTOM ******************************************//
        BorderPane bottomBorderPane = new BorderPane();
        bottomBorderPane.setMaxWidth(600);
        Label assignDateLabel = I18nControls.bindI18nProperties(new Label(), "AssignDay");
        TextTheme.createPrimaryTextFacet(assignDateLabel).style();
        assignDateLabel.getStyleClass().add(Bootstrap.SMALL);
        assignDateLabel.setPadding(new Insets(5, 0, 10, 0));
        datePicker = new DatePicker(new DatePickerOptions()
            .setMultipleSelectionAllowed(true)
            .setPastDatesSelectionAllowed(false)
            .setApplyBorderStyle(false)
            .setApplyMaxSize(false)
            .setSortSelectedDates(true)
        );
        LocalDate eventStartDate = dayTemplate.getEvent().getStartDate();
        datePicker.setDisplayedYearMonth(YearMonth.of(eventStartDate.getYear(), eventStartDate.getMonth()));

        listOfSelectedDatesVBox = new VBox();
        listOfSelectedDatesVBox.setAlignment(Pos.CENTER);
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

        mainContainer.setBottom(bottomBorderPane);
        BorderPane.setAlignment(bottomBorderPane, Pos.CENTER);

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

        ObservableLists.bindConverted(listOfSelectedDatesVBox.getChildren(), datePicker.getSelectedDates(), this::drawSelectedDatesAsLine);

        initFormValidation();
    }

    BorderPane getPanel() {
        return mainContainer;
    }


    private Event getEvent() {
        return dayTemplate.getEvent();
    }

    private ModalityValidationSupport getValidationSupport() {
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

    private BorderPane drawSelectedDatesAsLine(LocalDate currentDate) {
        SVGPath trashDate = SvgIcons.createTrashSVGPath();
        trashDate.setTranslateY(2);
        Text currentDateValue = new Text(currentDate.format(DateTimeFormatter.ofPattern("MMM dd")));
        trashDate.setOnMouseClicked(event -> datePicker.getSelectedDates().remove(currentDate));
        ShapeTheme.createSecondaryShapeFacet(trashDate).style();
        BorderPane currentLineBorderPane = new BorderPane();
        BorderPane.setMargin(currentDateValue, new Insets(0, 20, 0, 10));
        currentLineBorderPane.setLeft(trashDate);
        currentLineBorderPane.setCenter(currentDateValue);
        currentLineBorderPane.setPadding(new Insets(0, 0, 3, 0));
        listOfSelectedDatesVBox.getChildren().add(currentLineBorderPane);
        return currentLineBorderPane;
    }

    void initFormValidation() {
        getValidationSupport().addRequiredInput(templateNameTextField);
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

    private void deleteDayTemplate(DayTemplate dayTemplate) {
        programPanel.deleteDayTemplate(dayTemplate);
    }

    void removeTemplateTimeLineLinkedToDayTemplate(DayTemplate dayTemplate) {
        workingTemplateTimelines.removeIf(timeline -> (timeline.getDayTemplate() == dayTemplate));
    }

    private void removeTemplateTimeLine(Timeline timeline) {
        getUpdateStore().deleteEntity(timeline);
        workingTemplateTimelines.remove(timeline);
    }


    private HBox drawSelectedDatesAsLine(Timeline timeline) {
        ButtonSelector<Item> itemSelector = new EntityButtonSelector<Item>(
            "{class: 'Item', alias: 's', where: 'family.code=`teach`', orderBy :'name'}",
            programPanel, FXMainFrameDialogArea.getDialogArea(), timeline.getStore().getDataSourceModel()
        )
            .always(FXOrganization.organizationProperty(), o -> DqlStatement.where("organization=?", o));

        itemSelector.setSelectedItem(timeline.getItem());
        itemSelector.getButton().setMaxWidth(200);
        itemSelector.getButton().setMinWidth(200);

        //When the value is changed, we update the timeline and the teaching scheduledItem associated
        itemSelector.selectedItemProperty().addListener(observable -> {
            timeline.setItem(itemSelector.getSelectedItem());
        });
        getValidationSupport().addValidationRule(FXProperties.compute(itemSelector.selectedItemProperty(), s1 -> itemSelector.getSelectedItem() != null), itemSelector.getButton(), I18n.getI18nText("ItemSelectedShouldntBeNull"));

        MonoPane selectorPane = new MonoPane(itemSelector.getButton());
        TextField fromTextField = new TextField();
        fromTextField.setMaxWidth(60);
        fromTextField.setPromptText("8:46");
        getValidationSupport().addValidationRule(FXProperties.compute(fromTextField.textProperty(), s1 -> isLocalTimeTextValid(fromTextField.getText())), fromTextField, I18n.getI18nText("ValidationTimeFormatIncorrect"));

        if (timeline.getStartTime() != null)
            fromTextField.setText(timeline.getStartTime().format(timeFormatter));
        fromTextField.textProperty().addListener(obs -> {
            if (isLocalTimeTextValid(fromTextField.getText())) {
                timeline.setStartTime(LocalTime.parse(fromTextField.getText()));
            }
        });

        Label toLabel = I18nControls.bindI18nProperties(new Label(), "To");
        //TextTheme.createSecondaryTextFacet(subtitle).style();

        TextField untilTextField = new TextField();
        untilTextField.setMaxWidth(60);
        untilTextField.setPromptText("13:00");
        if (timeline.getEndTime() != null)
            untilTextField.setText(timeline.getEndTime().format(timeFormatter));
        untilTextField.textProperty().addListener(obs -> {
            if (isLocalTimeTextValid(untilTextField.getText())) {
                timeline.setEndTime(LocalTime.parse(untilTextField.getText()));
            }
        });
        getValidationSupport().addValidationRule(FXProperties.compute(untilTextField.textProperty(), s1 -> isLocalTimeTextValid(untilTextField.getText())), untilTextField, I18n.getI18nText("ValidationTimeFormatIncorrect"));

        TextField nameTextField = new TextField();
        nameTextField.setMaxWidth(150);
        nameTextField.setPromptText(ProgramI18nKeys.NameThisLine);
        if (timeline.getName() != null)
            nameTextField.setText(timeline.getName());
        nameTextField.textProperty().addListener(obs -> timeline.setName(nameTextField.getText()));

        SVGPath audioAvailableIcon = SvgIcons.createSoundIconPath();
        audioAvailableIcon.setFill(Color.GREEN);
        SVGPath audioUnavailable = SvgIcons.createSoundIconInactivePath();
        audioUnavailable.setFill(Color.RED);
        MonoPane audioMonoPane = new MonoPane();
        int iconWith = 30;
        audioMonoPane.setMinWidth(iconWith);
        audioMonoPane.setAlignment(Pos.CENTER);
        audioMonoPane.setCursor(Cursor.HAND);
        if (timeline.isAudioOffered() != null) {
            if (timeline.isAudioOffered())
                audioMonoPane.getChildren().setAll(audioAvailableIcon);
            else
                audioMonoPane.getChildren().setAll(audioUnavailable);
        }
        audioMonoPane.setOnMouseClicked(e -> {
            timeline.setAudioOffered(!timeline.isAudioOffered());
            if (timeline.isAudioOffered()) {
                audioMonoPane.getChildren().setAll(audioAvailableIcon);
                //We add the recording scheduledItem for those date and associated timeline
//                    datePicker.getSelectedDates().forEach(date-> workingTeachingScheduledItems.forEach(teachingScheduledItem -> {
//                        if(teachingScheduledItem.getTimeline().equals(timeline) && teachingScheduledItem.getDate().equals(date)) {
//                            addAudioScheduledItemsForDate(date,teachingScheduledItem);
//                        }
//                    }));
            } else {
                audioMonoPane.getChildren().setAll(audioUnavailable);
                //We remove the recording scheduledItem for those date and associated timeline
//                    datePicker.getSelectedDates().forEach(date-> workingTeachingScheduledItems.forEach(teachingScheduledItem -> {
//                        if(teachingScheduledItem.getTimeline().equals(timeline) && teachingScheduledItem.getDate().equals(date)) {
//                            removeAudioScheduledItem(teachingScheduledItem);
//                        }
//                    }));
            }
        });

        SVGPath videoAvailableIcon = SvgIcons.createVideoIconPath();
        videoAvailableIcon.setFill(Color.GREEN);
        SVGPath videoUnavailableIcon = SvgIcons.createVideoIconInactivePath();
        videoUnavailableIcon.setFill(Color.RED);

        MonoPane videoMonoPane = new MonoPane();
        videoMonoPane.setCursor(Cursor.HAND);
        if (timeline.isVideoOffered() != null) {
            if (timeline.isVideoOffered())
                videoMonoPane.getChildren().setAll(videoAvailableIcon);
            else
                videoMonoPane.getChildren().setAll(videoUnavailableIcon);
        }

        videoMonoPane.setOnMouseClicked(e -> {
            timeline.setVideoOffered(!timeline.isVideoOffered());
            if (timeline.isVideoOffered()) {
                videoMonoPane.getChildren().setAll(videoAvailableIcon);
                //We add the recording scheduledItem for those date and associated timeline
//                    datePicker.getSelectedDates().forEach(date-> workingTeachingScheduledItems.forEach(teachingScheduledItem -> {
//                        if(teachingScheduledItem.getTimeline().equals(timeline) && teachingScheduledItem.getDate().equals(date)) {
//                            addVideoScheduledItemsForDate(date,teachingScheduledItem);
//                        }
//                    }));
            } else {
                videoMonoPane.getChildren().setAll(videoUnavailableIcon);
                //We remove the recording scheduledItem for those date and associated timeline
//                    datePicker.getSelectedDates().forEach(date-> workingTeachingScheduledItems.forEach(teachingScheduledItem -> {
//                        if(teachingScheduledItem.getTimeline().equals(timeline) && teachingScheduledItem.getDate().equals(date)) {
//                            removeVideoScheduledItem(teachingScheduledItem);
//                        }
//                    }));
            }
        });

        SVGPath trashImage = SvgIcons.createTrashSVGPath();
        MonoPane trashContainer = new MonoPane(trashImage);
        trashContainer.setCursor(Cursor.HAND);
        trashContainer.setOnMouseClicked(event -> removeTemplateTimeLine(timeline));
        ShapeTheme.createSecondaryShapeFacet(trashImage).style();

        HBox afterItemSelectorLine = new HBox(fromTextField, toLabel, untilTextField, nameTextField, audioMonoPane, videoMonoPane, trashContainer);
        afterItemSelectorLine.setAlignment(Pos.CENTER_RIGHT);
        afterItemSelectorLine.setSpacing(10);
        selectorPane.setPadding(new Insets(0, 20, 0, 0));
        HBox line = new HBox(selectorPane, afterItemSelectorLine);
        line.setAlignment(Pos.CENTER_LEFT);
        line.setPadding(new Insets(5, 0, 5, 0));
        return line;
    }

    //TODO: this method is a repetition of a method in ManageRecurringEventView - move it to a shared place
    private static boolean isLocalTimeTextValid(String text) {
        try {
            LocalTime.parse(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
