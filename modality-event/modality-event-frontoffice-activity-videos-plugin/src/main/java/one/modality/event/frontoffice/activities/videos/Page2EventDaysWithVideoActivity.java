package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.cell.renderer.ValueRenderer;
import dev.webfx.extras.panes.GoldenRatioPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.visual.VisualColumnBuilder;
import dev.webfx.extras.visual.VisualResultBuilder;
import dev.webfx.extras.visual.VisualStyle;
import dev.webfx.extras.visual.controls.grid.SkinnedVisualGrid;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.KnownItem;
import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.markers.EntityHasStartAndEndTime;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * This is the second activity from the Livestream menu (after VideosActivity) when people click on a specific event.
 * So it displays a table of all days with videos of the event with the following columns: date, status, name, UK time &
 * remarks, and the last column displays a button to watch the video when applicable.
 *
 * @author David Hello
 * @author Bruno Salmon
 */
final class Page2EventDaysWithVideoActivity extends ViewDomainActivityBase {

    private static final boolean USE_VISUAL_GRID = true;

    private static final double IMAGE_HEIGHT = 240;
    private static final double DAY_BUTTON_WIDTH = 150;

    private final ObjectProperty<Object> pathEventIdProperty = new SimpleObjectProperty<>(); // The event id from the path
    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>(); // The event loaded from the event id
    private final ObservableList<ScheduledItem> videoScheduledItems = FXCollections.observableArrayList(); // The list of all videos of that event

    // The day selected by the user from which we should display the videos (null means all days)
    private final ObjectProperty<LocalDate> selectedDayProperty = new SimpleObjectProperty<>();
    private final HashMap<LocalDate, Button> correspondenceDateButton = new HashMap<>();

    // Creating an intermediate observable list of Page2EventDayScheduleView, each element being a view for 1 day with all its videos
    private final ObservableList<Page2EventDayScheduleView> scheduleViewsForSelectedDay = FXCollections.observableArrayList(); // will be populated below

    private final Label videoExpirationLabel = new Label();
    private final Button selectAllDaysButton = Bootstrap.primaryButton(I18nControls.newButton(VideosI18nKeys.ViewAllDays));
    private final Label selectTheDayBelowLabel = I18nControls.newLabel(VideosI18nKeys.SelectTheDayBelow);
    private EntityStore entityStore;

    private final VBox selectedVideosVBox = new VBox(20);

    @Override
    protected void updateModelFromContextParameters() {
        pathEventIdProperty.set(getParameter(Page2EventDaysWithVideoRouting.PATH_EVENT_ID_PARAMETER_NAME));
    }

    @Override
    protected void startLogic() {
        entityStore = EntityStore.create(getDataSourceModel());
        // Creating our own entity store to hold the loaded data without interfering with other activities
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Object eventId = pathEventIdProperty.get();
            EntityId userPersonId = FXUserPersonId.getUserPersonId();
            if (eventId == null || userPersonId == null) {
                videoScheduledItems.clear();
                eventProperty.set(null);
            } else {
                entityStore.<Event>executeQuery("select name, label.(de,en,es,fr,pt), shortDescription, audioExpirationDate, startDate, endDate, livestreamUrl, vodExpirationDate, repeatVideo, recurringWithVideo, repeatedEvent" +
                        " from Event where id=? limit 1", eventId)
                    .onFailure(Console::log)
                    .onSuccess(events -> {
                        Event currentEvent = events.get(0);
                        Event eventContainingVideos = Objects.coalesce(currentEvent.getRepeatedEvent(), currentEvent);
                        // We load all video scheduled items booked by the user for the event (booking must be confirmed
                        // and paid). They will be grouped by day in the UI.
                        // Note: double dots such as programScheduledItem.timeline..startTime means we do a left join, that allow null value (if the type of event is recurring, the timeline of the programScheduledItem is null
                        entityStore.<ScheduledItem>executeQuery("select name, date, expirationDate, programScheduledItem.(name, startTime, endTime, timeline.(startTime, endTime)), published, event.(name, type.recurringItem, livestreamUrl, recurringWithVideo), vodDelayed, " +
                                " (exists(select MediaConsumption where scheduledItem=si and attendance.documentLine.document.person=?) as attended), " +
                                " (select id from Attendance where scheduledItem=si.bookableScheduledItem and documentLine.document.person=? limit 1) as attendanceId " +
                                " from ScheduledItem si " +
                                " where event=?" +
                                " and bookableScheduledItem.item.family.code=?" +
                                " and item.code=?" +
                                " and exists(select Attendance a where scheduledItem=si.bookableScheduledItem and documentLine.(!cancelled and document.(person=? and event=? and confirmed and price_balance<=0)))" +
                                " order by date, programScheduledItem.timeline..startTime",
                                userPersonId, userPersonId, eventContainingVideos, KnownItemFamily.TEACHING.getCode(), KnownItem.VIDEO.getCode(), userPersonId, currentEvent)
                            .onFailure(Console::log)
                            .onSuccess(scheduledItemList -> Platform.runLater(() -> {
                                videoScheduledItems.setAll(scheduledItemList);
                                eventProperty.set(currentEvent);
                            }));
                    });
            }
        }, pathEventIdProperty, FXUserPersonId.userPersonIdProperty());
    }

    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************
        MonoPane pageContainer = new MonoPane(); // Will hold either the loading indicator or the loaded content

        Node loadingContentIndicator = new GoldenRatioPane(Controls.createProgressIndicator(100));

        // Building the loaded content, starting with the header
        MonoPane eventImageContainer = new MonoPane();
        Label eventLabel = Bootstrap.h2(Bootstrap.strong(I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", eventProperty), eventProperty)));
        eventLabel.setWrapText(true);
        eventLabel.setTextAlignment(TextAlignment.CENTER);
        eventLabel.setPadding(new Insets(0, 0, 12, 0));
        HtmlText eventDescriptionHtmlText = new HtmlText();
        I18n.bindI18nTextProperty(eventDescriptionHtmlText.textProperty(), new I18nSubKey("expression: i18n(shortDescription)", eventProperty), eventProperty);

        eventDescriptionHtmlText.setMaxHeight(60);
        videoExpirationLabel.setPadding(new Insets(30, 0, 0, 0));
        VBox titleVBox = new VBox(eventLabel, eventDescriptionHtmlText, videoExpirationLabel);

        HBox headerHBox = new HBox(50, eventImageContainer, titleVBox);
        headerHBox.setPadding(new Insets(0, 20, 0, 20));
        headerHBox.setMaxWidth(1024);

        //We display this box only if the current Date is in the list of date in the video Scheduled Item list
        VBox todayVideosVBox = new VBox(30); // Will be populated later (see reacting code below)
        todayVideosVBox.setAlignment(Pos.CENTER);
        Label todayVideosLabel = Bootstrap.strong(Bootstrap.textPrimary(Bootstrap.h3(new Label())));
        todayVideosLabel.setPadding(new Insets(100, 0, 40, 0));

        Label eventScheduleLabel = Bootstrap.h3(I18nControls.newLabel(VideosI18nKeys.EventSchedule));
        VBox selectTheDayBelowVBox = new VBox(5, eventScheduleLabel, selectTheDayBelowLabel);
        selectTheDayBelowVBox.setAlignment(Pos.CENTER);
        selectTheDayBelowVBox.setPadding(new Insets(100, 0, 0, 0));

        VBox loadedContentVBox = new VBox(40,
            headerHBox, // contains the event image and the event title
            //todayVideosVBox, // contains the videos for today (if any)
            selectTheDayBelowVBox, // contains the title of the schedule and the select the day below label (will be hidden if not applicable)
            selectedVideosVBox // contains the videos for the selected day (or all days)
        );
        loadedContentVBox.setAlignment(Pos.TOP_CENTER);
        loadedContentVBox.getStyleClass().add("livestream");

        selectAllDaysButton.setMinWidth(DAY_BUTTON_WIDTH);
        selectAllDaysButton.setOnAction(e -> {
            selectedDayProperty.set(null);
            showSelectedVideos(false);
        });

        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************

        // Reacting to data loading (initially or when the event changes or on login/logout)
        ObservableLists.runNowAndOnListOrPropertiesChange(change -> {
            // We display the loading indicator while the data is loading
            Event event = eventProperty.get();
            if (event == null) { // this indicates that the data has not finished loaded
                pageContainer.setContent(loadingContentIndicator); // TODO display something else (ex: next online events to book) when the user is not logged in, or registered
                return;
            }
            // Otherwise we display loadedContentVBox and update its content
            pageContainer.setContent(loadedContentVBox);
            // Loading the event image in the header
            String eventCloudImagePath = ModalityCloudinary.eventCoverImagePath(event, I18n.getLanguage());
            ModalityCloudinary.loadImage(eventCloudImagePath, eventImageContainer, -1, IMAGE_HEIGHT, SvgIcons::createVideoIconPath);

            // Updating the expiration date in the header
            LocalDateTime vodExpirationDate = event.getVodExpirationDate();
            if (vodExpirationDate == null) {
                videoExpirationLabel.setVisible(false);
            } else {
                videoExpirationLabel.setVisible(true);
                LocalDateTime nowInEventTimezone = Event.nowInEventTimezone();
                boolean available = nowInEventTimezone.isBefore(vodExpirationDate);
                I18nControls.bindI18nProperties(videoExpirationLabel, available ? VideosI18nKeys.EventAvailableUntil1 : VideosI18nKeys.VideoExpiredSince1,
                    LocalizedTime.formatLocalDateTimeProperty(vodExpirationDate, FrontOfficeTimeFormats.VOD_EXPIRATION_DATE_TIME_FORMAT));
            }

            // There are 2 modes of visualization: one with multiple videos per day (ex: Festivals), and one for with one video per day (ex: recurring events like STTP)
            Event eventContainingVideos = Objects.coalesce(event.getRepeatedEvent(), event);
            boolean singleVideosPerDay = eventContainingVideos.isRecurringWithVideo();
            LocalDate todayInEventTimezone = Event.todayInEventTimezone();

            // In single video per day mode (ex: STTP), we don't display the "Select the day below" label, because we automatically show all videos
            Layouts.setManagedAndVisibleProperties(selectTheDayBelowLabel, !singleVideosPerDay);
            if (singleVideosPerDay) {
                selectAllDaysButton.fire(); // Automatically selecting all days to show all videos
            }

            // Populating the videos for today
            I18nControls.bindI18nProperties(todayVideosLabel, VideosI18nKeys.ScheduleForSpecificDate1,
                LocalizedTime.formatMonthDay(todayInEventTimezone, FrontOfficeTimeFormats.VOD_TODAY_MONTH_DAY_FORMAT));
            List<ScheduledItem> todayVideoItems = Collections.filter(videoScheduledItems, item -> item.getDate().equals(todayInEventTimezone));
            Region todayView = new Page2EventDayScheduleView(todayInEventTimezone, todayVideoItems, getHistory(), true).getView();
            todayVideosVBox.getChildren().setAll(todayVideosLabel, todayView);

        }, videoScheduledItems, eventProperty);

        // Populating dayVideosWallViews from videoScheduledItems = flat list of all videos of the event (not yet grouped by day)
        ObservableLists.runNowAndOnListOrPropertiesChange(change -> showSelectedVideos(true)
            , videoScheduledItems);

        // Now that we have dayVideosWallViews populated, we can populate the final VBox showing all days and their videos
        ObservableLists.runNowAndOnListChange(change -> {
            if (scheduleViewsForSelectedDay.isEmpty()) {
                Label noContentLabel = Bootstrap.h3(Bootstrap.textWarning(I18nControls.newLabel(VideosI18nKeys.NoVideosForThisEvent)));
                noContentLabel.setPadding(new Insets(150, 0, 100, 0));
                selectedVideosVBox.getChildren().setAll(noContentLabel);
            } else {
                selectedVideosVBox.getChildren().setAll(Collections.map(scheduleViewsForSelectedDay, Page2EventDayScheduleView::getView));
            }
        }, scheduleViewsForSelectedDay);

        FXProperties.runOnPropertyChange(this::updateDaysButtonStyle, selectedDayProperty);

        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************

        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(pageContainer);
    }

    private void refreshVideosTable() {
        VisualGrid videoTable = new SkinnedVisualGrid();
        ValueRenderer dateRenderer = (date, context) -> {
            Text text = new Text();
            text.textProperty().bind(LocalizedTime.formatMonthDayProperty((LocalDate) date, FrontOfficeTimeFormats.VOD_TODAY_MONTH_DAY_FORMAT));
            return text;
        };
        ValueRenderer nameRenderer = (name, context) -> {
            Label label = new Label(name.toString());
            label.setWrapText(true);
            label.getStyleClass().add("ellipsis");
            return label;
        };
        ValueRenderer timeRangeRenderer = (timeRange, context) -> {
            LocalTime[] times = (LocalTime[]) timeRange;
            Text text = new Text();
            text.textProperty().bind(LocalizedTime.formatLocalTimeRangeProperty(times[0], times[1], FrontOfficeTimeFormats.VIDEO_DAY_TIME_FORMAT));
            return text;
        };
        ValueRenderer statusRenderer = (value, context) -> {
            return Bootstrap.textSuccess(I18n.newText(VideosI18nKeys.OnTime));
        };
        ValueRenderer buttonRenderer = (value, context) -> {
            return Bootstrap.dangerButton(I18nControls.newButton(VideosI18nKeys.Watch));
        };
        VisualResultBuilder rsb = VisualResultBuilder.create(videoScheduledItems.size(),
            VisualColumnBuilder.create("Date").setValueRenderer(dateRenderer).build(),
            VisualColumnBuilder.create("Status").setValueRenderer(statusRenderer).setStyle(VisualStyle.CENTER_STYLE).build(),
            VisualColumnBuilder.create("Name").setValueRenderer(nameRenderer).build(),
            VisualColumnBuilder.create("UK time").setValueRenderer(timeRangeRenderer).setStyle(VisualStyle.CENTER_STYLE).build(),
            VisualColumnBuilder.create().setValueRenderer(buttonRenderer).setStyle(VisualStyle.CENTER_STYLE).build()
        );
        for (int i = 0; i < videoScheduledItems.size(); i++) {
            ScheduledItem video = videoScheduledItems.get(i);
            rsb.setValue(i, 0, video.getDate());
            rsb.setValue(i, 1, null);
            rsb.setValue(i, 2, Strings.isBlank(video.getName()) ? video.getProgramScheduledItem().getName() : video.getName());
            EntityHasStartAndEndTime startEndHolder = video.getEvent().isRecurringWithVideo() ? video.getProgramScheduledItem() : video;
            rsb.setValue(i, 3, new LocalTime[] {startEndHolder.getStartTime(), startEndHolder.getEndTime()});
            rsb.setValue(i, 4, null);
        }
        videoTable.setVisualResult(rsb.build());
        videoTable.setFullHeight(true);
        videoTable.setHeaderVisible(true);
        selectedVideosVBox.getChildren().setAll(videoTable);
    }

    private void showSelectedVideos(boolean initialLoading) {
        if (USE_VISUAL_GRID) {
            refreshVideosTable();
            return;
        }
        scheduleViewsForSelectedDay.clear();
        correspondenceDateButton.clear();
        correspondenceDateButton.put(null, selectAllDaysButton);
        boolean[] isFirst = { true };
        LocalDate[] firstDay = { null };

        Map<LocalDate, Button> dayButtonMap = new HashMap<>();

        // Grouping videos per day
        Map<LocalDate, List<ScheduledItem>> perDayGroups =
            videoScheduledItems.stream().collect(Collectors.groupingBy(ScheduledItem::getDate));
        new TreeMap<>(perDayGroups) // The purpose of using a TreeMap is to sort the groups by keys (= days)
            .forEach((day, scheduledItems) -> {
                scheduleViewsForSelectedDay.add(
                    // Passing the day, the videos of that day, and the history (for backward navigation)
                    new Page2EventDayScheduleView(day, scheduledItems, getHistory(), isFirst[0]));
                if (firstDay[0] == null)
                    firstDay[0] = day;
                Button dateButton;
                dateButton = Bootstrap.primaryButton(new Button());
                dateButton.textProperty().bind(LocalizedTime.formatMonthDayProperty(day, FrontOfficeTimeFormats.VOD_BUTTON_DATE_FORMAT));
                dateButton.setMinWidth(DAY_BUTTON_WIDTH);
                correspondenceDateButton.put(day, dateButton);
                dayButtonMap.put(day, dateButton);

                dateButton.setOnAction(e -> {
                    scheduleViewsForSelectedDay.setAll(new Page2EventDayScheduleView(day, scheduledItems, getHistory(), true));
                    selectedDayProperty.set(day);
                });
                isFirst[0] = false;
            });

        if (initialLoading && !dayButtonMap.isEmpty()) {
            Button firstDayButton = dayButtonMap.get(firstDay[0]); // Retrieve the button for the first day
            if (firstDayButton != null) {
                firstDayButton.fire(); // Simulate a button click to initially display the videos of the first day only
            }
        }
    }

    private void updateDaysButtonStyle() {
        LocalDate selectedDate = selectedDayProperty.get();
        for (Map.Entry<LocalDate, Button> entry : correspondenceDateButton.entrySet()) {
            Button currentButton = entry.getValue();
            boolean primary = Objects.areEquals(entry.getKey(), selectedDate);
            currentButton.getStyleClass().setAll(Bootstrap.BTN, primary ? Bootstrap.BTN_PRIMARY : ModalityStyle.BTN_WHITE);
        }
    }
}
