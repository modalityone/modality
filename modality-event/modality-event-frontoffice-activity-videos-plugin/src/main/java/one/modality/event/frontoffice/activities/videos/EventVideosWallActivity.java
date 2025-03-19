package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.*;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.cloud.image.CloudImageService;
import dev.webfx.stack.cloud.image.impl.client.ClientImageService;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.KnownItem;
import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
final class EventVideosWallActivity extends ViewDomainActivityBase {

    private static final double IMAGE_HEIGHT = 240;
    private static final double DAY_BUTTON_WIDTH = 150;
    private static final double DAY_COLUMN_PANE_MAX_WIDTH = 1100;

    private final ObjectProperty<Object> pathEventIdProperty = new SimpleObjectProperty<>();

    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>();
    private final ObservableList<ScheduledItem> scheduledItems = FXCollections.observableArrayList();

    private final CollapsePane daysCollapsePane = new CollapsePane();
    private final ColumnsPane daysColumnPane = new ColumnsPane();

    private Label videoExpirationLabel;
    private final CloudImageService cloudImageService = new ClientImageService();

    private final ObjectProperty<LocalDate> currentDaySelectedProperty = new SimpleObjectProperty<>();
    private final HashMap<LocalDate, Button> correspondenceDateButton = new HashMap<>();

    // Creating an intermediate observable list of DayVideosWallView, each element being a view for 1 day with all its videos
    private final ObservableList<VideosDayScheduleView> videosDayScheduleViews = FXCollections.observableArrayList(); // will be populated below
    private Button selectAllDaysButton;
    private EntityStore entityStore;
    private Label scheduleSubTitleLabel;

    @Override
    protected void updateModelFromContextParameters() {
        pathEventIdProperty.set(Numbers.toInteger(getParameter(EventVideosWallRouting.PATH_EVENT_ID_PARAMETER_NAME)));
    }

    @Override
    protected void startLogic() {
        entityStore = EntityStore.create(getDataSourceModel());
        // Creating our own entity store to hold the loaded data without interfering with other activities
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Object eventId = pathEventIdProperty.get();
            EntityId userPersonId = FXUserPersonId.getUserPersonId();
            if (eventId == null || userPersonId == null) {
                scheduledItems.clear();
                eventProperty.set(null);
            } else {
                entityStore.<Event>executeQuery("select name, label.(de,en,es,fr,pt), shortDescription, audioExpirationDate, startDate, endDate, livestreamUrl, vodExpirationDate, repeatVideo, recurringWithVideo, repeatedEvent" +
                        " from Event where id=? limit 1", eventId)
                    .onFailure(Console::log)
                    .onSuccess(events -> {
                        Event currentEvent = events.get(0);
                        Object eventIdContainingVideos = Entities.getPrimaryKey(currentEvent);
                        if (currentEvent.getRepeatedEventId() != null) {
                            eventIdContainingVideos = Entities.getPrimaryKey(currentEvent.getRepeatedEventId());
                        }
                        // In this code: programScheduledItem.timeline..startTime, the double . means we do a left join, that allow null value (if the type of event is recurring, the timeline of the programScheduledItem is null
                        entityStore.<ScheduledItem>executeQuery("select name, date, expirationDate, programScheduledItem.(name, startTime, endTime, timeline.(startTime, endTime)), published, event.(name, type.recurringItem, livestreamUrl, recurringWithVideo), vodDelayed, " +
                                " (exists(select MediaConsumption where scheduledItem=si and attendance.documentLine.document.person=?) as attended), " +
                                " (select id from Attendance where scheduledItem=si.bookableScheduledItem and documentLine.document.person=? limit 1) as attendanceId " +
                                " from ScheduledItem si " +
                                " where event=?" +
                                " and bookableScheduledItem.item.family.code=?" +
                                " and item.code=?" +
                                " and exists(select Attendance a where scheduledItem=si.bookableScheduledItem and documentLine.(!cancelled and document.(person=? and event=? and confirmed and price_balance<=0)))" +
                                " order by date, programScheduledItem.timeline..startTime",
                                userPersonId, userPersonId, eventIdContainingVideos, KnownItemFamily.TEACHING.getCode(), KnownItem.VIDEO.getCode(), userPersonId, currentEvent)
                            .onFailure(Console::log)
                            .onSuccess(scheduledItemList -> Platform.runLater(() -> {
                                scheduledItems.setAll(scheduledItemList);
                                eventProperty.set(currentEvent);
                            }));
                    });
            }
        }, pathEventIdProperty, FXUserPersonId.userPersonIdProperty());
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************
        HBox headerHBox = new HBox();
        headerHBox.setSpacing(50);
        headerHBox.setPadding(new Insets(0, 20, 0, 20));
        headerHBox.setMaxWidth(1024);
        MonoPane imageMonoPane = new MonoPane();
        ImageView imageView = new ImageView();

        headerHBox.getChildren().add(imageMonoPane);

        Label eventLabel = Bootstrap.h2(Bootstrap.strong(I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", eventProperty), eventProperty)));

        eventLabel.setWrapText(true);
        eventLabel.setTextAlignment(TextAlignment.CENTER);
        eventLabel.setPadding(new Insets(0, 0, 12, 0));
        HtmlText eventDescriptionHtmlText = new HtmlText();
        I18n.bindI18nTextProperty(eventDescriptionHtmlText.textProperty(), new I18nSubKey("expression: i18n(shortDescription)", eventProperty), eventProperty);

        eventDescriptionHtmlText.setMaxHeight(60);
        videoExpirationLabel = I18nControls.newLabel(VideosI18nKeys.EventAvailableUntil1);
        videoExpirationLabel.setPadding(new Insets(30, 0, 0, 0));
        VBox titleVBox = new VBox(eventLabel, eventDescriptionHtmlText, videoExpirationLabel);

        headerHBox.getChildren().add(titleVBox);

        Node loadingContentIndicator = new GoldenRatioPane(Controls.createProgressIndicator(100));
        MonoPane pageContainer = new MonoPane();

        Label noContentLabel = Bootstrap.h3(Bootstrap.textWarning(I18nControls.newLabel(VideosI18nKeys.NoVideosForThisEvent)));
        noContentLabel.setPadding(new Insets(150, 0, 100, 0));

        //We display this box only if the current Date is in the list of date in the video Scheduled Item list
        VBox currentDayScheduleVBox = new VBox(30); // Will be populated later (see reacting code below)
        Label scheduleForTodayTitleLabel = Bootstrap.strong(Bootstrap.textPrimary(Bootstrap.h3(new Label())));
        scheduleForTodayTitleLabel.setPadding(new Insets(100, 0, 40, 0));
        currentDayScheduleVBox.getChildren().add(scheduleForTodayTitleLabel);
        currentDayScheduleVBox.setAlignment(Pos.CENTER);

        Label scheduleTitleLabel = Bootstrap.h3(I18nControls.newLabel(VideosI18nKeys.EventSchedule));
        scheduleSubTitleLabel = I18nControls.newLabel(VideosI18nKeys.SelectTheDayBelow);
        VBox scheduleTitleVBox = new VBox(5, scheduleTitleLabel, scheduleSubTitleLabel);
        scheduleTitleVBox.setAlignment(Pos.CENTER);
        scheduleTitleVBox.setPadding(new Insets(100, 0, 0, 0));

        daysColumnPane.setHgap(7);
        daysColumnPane.setVgap(15);
        daysColumnPane.setMaxColumnCount(8);
        daysColumnPane.setMinColumnWidth(DAY_BUTTON_WIDTH);
        daysColumnPane.setMinWidth(DAY_COLUMN_PANE_MAX_WIDTH);
        daysColumnPane.setPadding(new Insets(0, 0, 30, 0));

        VBox videoScheduleVBox = new VBox(20); // Will be populated later (see reacting code below)

        VBox loadedContentVBox = new VBox(40,
            headerHBox,
            currentDayScheduleVBox,
            scheduleTitleVBox,
            //   daysColumnPane,
            new ScalePane(videoScheduleVBox)
        );
        loadedContentVBox.setAlignment(Pos.CENTER);

        daysCollapsePane.setPrefWidth(loadedContentVBox.getPrefWidth());
        loadedContentVBox.setAlignment(Pos.TOP_CENTER);
        loadedContentVBox.getStyleClass().add("livestream");

        selectAllDaysButton = Bootstrap.primaryButton(I18nControls.newButton(VideosI18nKeys.ViewAllDays));
        selectAllDaysButton.setMinWidth(DAY_BUTTON_WIDTH);
        selectAllDaysButton.setOnAction(e -> {
            currentDaySelectedProperty.set(null);
            handleVideoChanges(false);
        });

        // *************************************************************************************************************
        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************

        ObservableLists.runNowAndOnListOrPropertiesChange(change -> {
            Layouts.setManagedAndVisibleProperties(currentDayScheduleVBox, false);
            // We display the loading indicator while the data is loading
            Event event = eventProperty.get();
            if (event == null) { // this indicates that the data has not finished loaded
                pageContainer.setContent(loadingContentIndicator);
                // TODO display something else (ex: next online events to book) when the user is not logged in, or registered
            } else { // otherwise we display loadedContentVBox and set the content of audioTracksVBox
                pageContainer.setContent(loadedContentVBox);
                Object imageTag = ModalityCloudinary.getEventCoverImageTag(Entities.getPrimaryKey(event), I18n.getLanguage());
                if (event.getRepeatedEvent() != null)
                    imageTag = ModalityCloudinary.getEventCoverImageTag(Entities.getPrimaryKey(event.getRepeatedEvent()), I18n.getLanguage());

                String pictureId = String.valueOf(imageTag);
                cloudImageService.exists(pictureId)
                    .onFailure(Console::log)
                    .onSuccess(exists -> Platform.runLater(() -> {
                        Console.log("exists: " + exists);
                        if (exists) {
                            imageMonoPane.setBackground(null);
                            //First, we need to get the zoom factor of the screen
                            double zoomFactor = Screen.getPrimary().getOutputScaleX();
                            String url = cloudImageService.url(pictureId, -1, (int) (IMAGE_HEIGHT * zoomFactor));
                            imageView.setFitHeight(IMAGE_HEIGHT);
                            imageView.setPreserveRatio(true);
                            Image imageToDisplay = new Image(url, true);
                            imageView.setImage(imageToDisplay);
                            imageMonoPane.getChildren().setAll(imageView);
                        } else {
                            SVGPath videoCoverPath = SvgIcons.createVideoIconPath();
                            imageMonoPane.setBackground(new Background(
                                new BackgroundFill(Color.LIGHTGRAY, null, null)
                            ));
                            imageMonoPane.getChildren().setAll(videoCoverPath);
                            imageMonoPane.setAlignment(Pos.CENTER);
                        }
                    }));
                if (event.getVodExpirationDate() != null) {
                    LocalDateTime nowInEventTimezone = Event.nowInEventTimezone();
                    boolean available = nowInEventTimezone.isBefore(event.getVodExpirationDate());
                    I18nControls.bindI18nProperties(videoExpirationLabel, available ? VideosI18nKeys.EventAvailableUntil1 : VideosI18nKeys.VideoExpiredSince1,
                        LocalizedTime.formatLocalDateTimeProperty(event.getVodExpirationDate(), FrontOfficeTimeFormats.VOD_EXPIRATION_DATE_TIME_FORMAT));
                    videoExpirationLabel.setVisible(true);
                } else {
                    videoExpirationLabel.setVisible(false);
                }
            }

            LocalDate todayInEventTimezone = Event.todayInEventTimezone();
            I18nControls.bindI18nProperties(scheduleForTodayTitleLabel, VideosI18nKeys.ScheduleForSpecificDate1,
                LocalizedTime.formatMonthDay(todayInEventTimezone, FrontOfficeTimeFormats.VOD_TODAY_MONTH_DAY_FORMAT));

            //If it's a recurring event, we don't display the daysColumnPane because it's one video per day
            boolean isRecurring = !scheduledItems.isEmpty() && scheduledItems.get(0).getEvent().isRecurringWithVideo();
            Layouts.setManagedAndVisibleProperties(daysColumnPane, !isRecurring);
            Layouts.setManagedAndVisibleProperties(scheduleSubTitleLabel, !isRecurring);
            if (isRecurring) {
                selectAllDaysButton.fire();
            }

            Map<LocalDate, List<ScheduledItem>> perDayGroups =
                scheduledItems.stream()
                    .filter(item -> item.getDate().equals(todayInEventTimezone)) // Filter for the target day
                    .collect(Collectors.groupingBy(ScheduledItem::getDate));
            new TreeMap<>(perDayGroups) // The purpose of using a TreeMap is to sort the groups by keys (= days)
                .forEach((day, dayScheduledVideos) -> {
                    Layouts.setManagedAndVisibleProperties(currentDayScheduleVBox, true);
                    // Passing the day, the videos of that day, and the history (for backward navigation)
                    ScalePane currentScalePane = new ScalePane(new VideosDayScheduleView(day, dayScheduledVideos, getHistory(), true).getView());
                    currentDayScheduleVBox.getChildren().setAll(scheduleForTodayTitleLabel, currentScalePane);
                });

        }, scheduledItems, eventProperty);

        // Populating dayVideosWallViews from videoScheduledItems = flat list of all videos of the event (not yet grouped by day)
        ObservableLists.runNowAndOnListOrPropertiesChange(change -> handleVideoChanges(true), scheduledItems);

        // Now that we have dayVideosWallViews populated, we can populate the final VBox showing all days and their videos
        ObservableLists.runNowAndOnListChange(change -> {
            if (videosDayScheduleViews.isEmpty()) {
                videoScheduleVBox.getChildren().setAll(noContentLabel);
            } else {
                videoScheduleVBox.getChildren().setAll(Collections.map(videosDayScheduleViews, VideosDayScheduleView::getView));
            }
        }, videosDayScheduleViews);

        FXProperties.runOnPropertyChange(this::updateDaysButtonStyle, currentDaySelectedProperty);

        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************

        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(pageContainer);
        //return FrontOfficeActivityUtil.createActivityPageScrollPane(pageContainer, true);
    }


    private void handleVideoChanges(boolean initialLoading) {
        // Grouping videos per day
        Map<LocalDate, List<ScheduledItem>> perDayGroups =
            scheduledItems.stream().collect(Collectors.groupingBy(ScheduledItem::getDate));
        videosDayScheduleViews.clear();
        daysColumnPane.getChildren().clear();

        correspondenceDateButton.clear();
        correspondenceDateButton.put(null, selectAllDaysButton);
        final boolean[] isFirst = {true};
        final LocalDate[] firstDay = {null};

        Map<LocalDate, Button> dayButtonMap = new HashMap<>();
        new TreeMap<>(perDayGroups) // The purpose of using a TreeMap is to sort the groups by keys (= days)
            .forEach((day, scheduledItems) -> {
                videosDayScheduleViews.add(
                    // Passing the day, the videos of that day, and the history (for backward navigation)
                    new VideosDayScheduleView(day, scheduledItems, getHistory(), isFirst[0]));
                if (firstDay[0] == null) firstDay[0] = day;
                Button dateButton;
                dateButton = Bootstrap.primaryButton(new Button());
                dateButton.textProperty().bind(LocalizedTime.formatMonthDayProperty(day, FrontOfficeTimeFormats.VOD_BUTTON_DATE_FORMAT));
                dateButton.setMinWidth(DAY_BUTTON_WIDTH);
                correspondenceDateButton.put(day, dateButton);
                dayButtonMap.put(day, dateButton);

                dateButton.setOnAction(e -> {
                    videosDayScheduleViews.clear();
                    videosDayScheduleViews.add(new VideosDayScheduleView(day, scheduledItems, getHistory(), true));
                    currentDaySelectedProperty.set(day);
                });
                daysColumnPane.getChildren().add(dateButton);
                isFirst[0] = false;
            });

        if (initialLoading && !dayButtonMap.isEmpty()) {
            LocalDate firstDayOfList = new TreeMap<>(perDayGroups).firstKey(); // Get the first day
            Button firstDayButton = dayButtonMap.get(firstDayOfList); // Retrieve the button for the first day
            if (firstDayButton != null) {
                firstDayButton.fire(); // Simulate a button click to initially display the videos of the first day only
            }
        }
        daysColumnPane.getChildren().add(selectAllDaysButton);

        //Here we resize daysColumnPane
        int numberOfChild = daysColumnPane.getChildren().size();
        double theoreticalColumnPaneWith = (DAY_BUTTON_WIDTH + daysColumnPane.getHgap()) * (numberOfChild);
        double columnPaneWidth = Math.min(theoreticalColumnPaneWith, DAY_COLUMN_PANE_MAX_WIDTH);
        daysColumnPane.setMaxWidth(columnPaneWidth);
        daysColumnPane.setMinWidth(columnPaneWidth);
    }

    private void updateDaysButtonStyle() {
        LocalDate selectedDate = currentDaySelectedProperty.get();
        for (Map.Entry<LocalDate, Button> entry : correspondenceDateButton.entrySet()) {
            Button currentButton = entry.getValue();
            if (entry.getKey() == null && selectedDate == null) {
                currentButton.getStyleClass().setAll("button", Bootstrap.BTN, Bootstrap.BTN_PRIMARY);
            } else if (entry.getKey() != null && entry.getKey().equals(selectedDate)) {
                currentButton.getStyleClass().setAll("button", Bootstrap.BTN, Bootstrap.BTN_PRIMARY);
            } else {
                currentButton.getStyleClass().setAll("button", Bootstrap.BTN, ModalityStyle.BTN_WHITE);
            }
        }
    }
}
