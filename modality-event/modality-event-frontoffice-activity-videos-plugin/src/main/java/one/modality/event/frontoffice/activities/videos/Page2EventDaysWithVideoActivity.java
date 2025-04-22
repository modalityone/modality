package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.extras.panes.GoldenRatioPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.player.Player;
import dev.webfx.extras.player.StartOptionsBuilder;
import dev.webfx.extras.player.multi.all.AllPlayers;
import dev.webfx.extras.responsive.ResponsiveDesign;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.EntitiesToVisualResultMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.VisualEntityColumnFactory;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.KnownItem;
import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.event.frontoffice.medias.MediaConsumptionRecorder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * This is the second activity from the Livestream menu (after VideosActivity) when people click on a specific event.
 * So it displays a table of all days with videos of the event with the following columns: date, status, name, UK time &
 * remarks, and the last column displays a button to watch the video when applicable.
 *
 * @author David Hello
 * @author Bruno Salmon
 */
final class Page2EventDaysWithVideoActivity extends ViewDomainActivityBase {

    private static final double IMAGE_HEIGHT = 240;

    private final ObjectProperty<Object> pathEventIdProperty = new SimpleObjectProperty<>(); // The event id from the path
    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>(); // The event loaded from the event id
    private final ObservableList<ScheduledItem> videoScheduledItems = FXCollections.observableArrayList(); // The list of all videos of that event


    private final Label videoExpirationLabel = new Label();
    private final Button selectAllDaysButton = Bootstrap.primaryButton(I18nControls.newButton(VideosI18nKeys.ViewAllDays));
    private final Label selectTheDayBelowLabel = I18nControls.newLabel(VideosI18nKeys.SelectTheDayBelow);
    private EntityStore entityStore;
    private EntityColumn<ScheduledItem>[] videoColumns;

    private final VBox selectedVideosVBox = new VBox(20);

    private final SimpleObjectProperty<String> livestreamUrlProperty = new SimpleObjectProperty<>();
    private final Player livestreamPlayer = AllPlayers.createAllVideoPlayer();
    private MediaConsumptionRecorder mediaConsumptionRecorder;
    private final ObjectProperty<ScheduledItem> scheduledVideoItemProperty = new SimpleObjectProperty<>();
    private final BooleanProperty displayLivestreamVideoProperty = new SimpleBooleanProperty(false);
    private final VBox livestreamVBox = new VBox(20);
    private CollapsePane livestreamCollapsePane;

    public Page2EventDaysWithVideoActivity() {
        //We relaunch every 14 hours the request (in case the user never close the page, and to make sure the coherence of MediaConsumption is ok)
        Scheduler.schedulePeriodic(14 * 3600 * 1000, this::startLogic);
    }

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
            LocalDateTime nowInEventTimezone = Event.nowInEventTimezone();
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
                                livestreamUrlProperty.set(currentEvent.getLivestreamUrl());
                                // The livestream url is always the same for the event, but we still need to determine which
                                // session is being played for the MediaConsumption management. To do this, we will set
                                // scheduledVideoItemProperty with the scheduled item corresponding to the played session.
                                LocalDate today = Event.todayInEventTimezone();
                                List<ScheduledItem> todayVideoScheduledItems = Collections.filter(videoScheduledItems,
                                    item -> item.getDate().isEqual(today));
                                todayVideoScheduledItems.forEach(scheduledItem -> { // iterating today sessions
                                    ScheduledItem programScheduledItem = scheduledItem.getProgramScheduledItem();
                                    LocalDateTime scheduledItemStart = scheduledItem.getDate().atTime(programScheduledItem.getStartTime());
                                    LocalDateTime scheduledItemEnd = scheduledItem.getDate().atTime(programScheduledItem.getEndTime());

                                    //If we're 20 minutes before or 30 minutes after the teaching, we display the livestream window
                                    if (Times.isBetween(nowInEventTimezone, scheduledItemStart.minusMinutes(20), scheduledItemEnd.plusMinutes(30))) {
                                        displayLivestreamVideoProperty.set(true);
                                        long endPlus30MinutesInMs = ChronoUnit.MILLIS.between(nowInEventTimezone,scheduledItemEnd.plusMinutes(30));
                                        UiScheduler.scheduleDelay(endPlus30MinutesInMs, () -> displayLivestreamVideoProperty.set(false));
                                    }
                                    if (nowInEventTimezone.isBefore(scheduledItemStart.minusMinutes(20))) {
                                        displayLivestreamVideoProperty.set(false);
                                        long startMinus20MinutesInMs = ChronoUnit.MILLIS.between(nowInEventTimezone,scheduledItemStart.minusMinutes(20));
                                        UiScheduler.scheduleDelay(startMinus20MinutesInMs, () -> displayLivestreamVideoProperty.set(true));

                                    }
                                    //If we arrive in the middle of the session, we trigger the MediaConsumptionRecorder by setting the scheduledVideoItemProperty
                                    if (Times.isBetween(nowInEventTimezone, scheduledItemStart, scheduledItemEnd)) {
                                        scheduledVideoItemProperty.set(scheduledItem);
                                    }
                                    //If we arrive before the start of the session, we postpone the triggering of the MediaConsumptionRecorder by setting the scheduledVideoItemProperty
                                    //at the start of the session
                                    if (nowInEventTimezone.isBefore(scheduledItemStart)) {
                                        long startInMs = ChronoUnit.MILLIS.between(nowInEventTimezone,scheduledItemStart);
                                        UiScheduler.scheduleDelay(startInMs, () -> scheduledVideoItemProperty.set(scheduledItem));
                                    }
                                });
                            }));
                    });
            }
        }, pathEventIdProperty, FXUserPersonId.userPersonIdProperty());
        VideoColumnsFormattersAndRenderers.registerRenderers(getHistory());
        videoColumns = VisualEntityColumnFactory.get().fromJsonArray("""
            [
            {expression: 'date', label: 'Date', format: 'videoDate'},
            {expression: '[coalesce(startTime, programScheduledItem.startTime), coalesce(endTime, programScheduledItem.endTime)]', label: 'UK time', format: 'videoTimeRange', textAlign: 'center'},
            {expression: 'coalesce(name, programScheduledItem.name)', label: 'Name', renderer: 'ellipsisLabel', minWidth: 200},
            {expression: 'this', label: 'Status', renderer: 'videoStatus', textAlign: 'center'}
            ]""", getDomainModel(), "ScheduledItem");
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

        MonoPane responsiveHeader = new MonoPane();
        new ResponsiveDesign(responsiveHeader)
            // 1. Horizontal layout (for desktops) - as far as TitleVBox is not higher than the image
            .addResponsiveLayout(/* applicability test: */ width -> {
                    double titleVBoxWidth = width - eventImageContainer.getWidth() - 50 /* HBox spacing */;
                    return titleVBox.prefHeight(titleVBoxWidth) <= IMAGE_HEIGHT && eventImageContainer.getWidth() > 0; // also image must be loaded
                }, /* apply method: */ () -> responsiveHeader.setContent(new HBox(50, eventImageContainer, titleVBox))
                , /* test dependencies: */ eventImageContainer.widthProperty())
            // 2. Vertical layout (for mobiles) - when TitleVBox is too high (always applicable if 1. is not)
            .addResponsiveLayout(/* apply method: */ () -> {
                VBox vBox = new VBox(10, eventImageContainer, titleVBox);
                vBox.setAlignment(Pos.CENTER);
                VBox.setMargin(titleVBox, new Insets(5, 10, 5, 10)); // Same as cell padding => vertically aligned with cells content
                responsiveHeader.setContent(vBox);
            }).start();

        livestreamCollapsePane = new CollapsePane(new ScalePane(livestreamPlayer.getMediaView()));

        StackPane collapsePaneContainer = CollapsePane.decorateCollapsePane(livestreamCollapsePane, true);
        livestreamVBox.getChildren().add(new VBox(20, collapsePaneContainer));
        //We display this box only if the current Date is in the list of date in the video Scheduled Item list
        VBox todayProgramVBox = new VBox(30); // Will be populated later (see reacting code below)
        todayProgramVBox.setAlignment(Pos.CENTER);
        Label todayVideosLabel = Bootstrap.strong(Bootstrap.textPrimary(Bootstrap.h3(new Label())));
        todayVideosLabel.setPadding(new Insets(100, 0, 40, 0));

        Label eventScheduleLabel = Bootstrap.h3(I18nControls.newLabel(VideosI18nKeys.EventSchedule));
        VBox selectTheDayBelowVBox = new VBox(5, eventScheduleLabel, selectTheDayBelowLabel);
        selectTheDayBelowVBox.setAlignment(Pos.CENTER);
        selectTheDayBelowVBox.setPadding(new Insets(100, 0, 0, 0));

        VBox loadedContentVBox = new VBox(40,
            responsiveHeader, // contains the event image and the event title
            livestreamVBox,
            selectedVideosVBox // contains the videos for the selected day (or all days)
        );
        loadedContentVBox.setAlignment(Pos.TOP_CENTER);
        loadedContentVBox.getStyleClass().add("livestream");

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
            assert eventContainingVideos != null;
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
            todayProgramVBox.getChildren().setAll(todayVideosLabel, todayView);

        }, videoScheduledItems, eventProperty);

        // Showing selected videos once they are loaded
        ObservableLists.runNowAndOnListOrPropertiesChange(change ->
                refreshVideosTable()
            , videoScheduledItems);

        FXProperties.runNowAndOnPropertyChange(this::syncPlayerContent, livestreamUrlProperty);
        FXProperties.runOnPropertyChange(()->{
            if (mediaConsumptionRecorder != null)
                mediaConsumptionRecorder.stop();
            mediaConsumptionRecorder = new MediaConsumptionRecorder(livestreamPlayer, true, scheduledVideoItemProperty::get, () -> null);
            mediaConsumptionRecorder.start();
        }, scheduledVideoItemProperty);

        FXProperties.runNowAndOnPropertyChange(this::displayHideLiveStreamBox, displayLivestreamVideoProperty);

        //When the livestream collapse pane is collapsed, we pause the livestreamPlayer so the full screen orange pane
        //in not displayed
        FXProperties.runNowAndOnPropertyChange(()-> {
            if (livestreamCollapsePane.collapsedProperty().get())
                livestreamPlayer.pause();
            else
                livestreamPlayer.play();}, livestreamCollapsePane.collapsedProperty());

        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************

        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(pageContainer);
    }

    private void displayHideLiveStreamBox() {
        if(displayLivestreamVideoProperty.get())
           livestreamCollapsePane.expand();
        else
            livestreamCollapsePane.collapse();
    }

    private void syncPlayerContent() {
        String livestreamUrl = livestreamUrlProperty.get();
        if (livestreamUrl != null) {
            boolean autoPlay = true;
            livestreamPlayer.getMediaView().setVisible(true);
            livestreamPlayer.setMedia(livestreamPlayer.acceptMedia(livestreamUrl));
            livestreamPlayer.setStartOptions(new StartOptionsBuilder()
                .setAutoplay(autoPlay)
                .setAspectRatioTo16by9() // should be read from metadata but hardcoded for now
                .build());
            livestreamPlayer.play();
        } else {
            livestreamPlayer.setMedia(null);
            livestreamPlayer.resetToInitialState();
        }
    }

    private void refreshVideosTable() {
        VisualGrid videoTable = VisualGrid.createVisualGridWithResponsiveSkin();
        videoTable.setRowHeight(48);
        videoTable.setCellMargin(new Insets(5, 10, 5, 10));
        videoTable.setFullHeight(true);
        videoTable.setHeaderVisible(true);
        // Moving expired videos at the end of the list
        SortedList<ScheduledItem> videoScheduledItemsExpiredLast = videoScheduledItems.sorted((v1, v2) ->
            Boolean.compare(VideoState.isVideoExpired(v1), VideoState.isVideoExpired(v2)));
        VisualResult rs = EntitiesToVisualResultMapper.mapEntitiesToVisualResult(videoScheduledItemsExpiredLast, videoColumns);
        videoTable.setVisualResult(rs);
        selectedVideosVBox.getChildren().setAll(videoTable);
    }
}
