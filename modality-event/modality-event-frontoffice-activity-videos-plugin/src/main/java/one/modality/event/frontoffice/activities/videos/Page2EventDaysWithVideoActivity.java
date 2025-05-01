package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.*;
import dev.webfx.extras.player.Player;
import dev.webfx.extras.player.Players;
import dev.webfx.extras.player.StartOptionsBuilder;
import dev.webfx.extras.player.multi.all.AllPlayers;
import dev.webfx.extras.responsive.ResponsiveDesign;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.util.scene.SceneUtil;
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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.*;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.event.frontoffice.medias.MediaConsumptionRecorder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    private static final double IMAGE_HEIGHT = 240;
    private static final int MIN_NUMBER_OF_SESSION_PER_DAY_BEFORE_DISPLAYING_DAILY_PROGRAM = 3;
    private final ObjectProperty<Object> pathEventIdProperty = new SimpleObjectProperty<>(); // The event id from the path
    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>(); // The event loaded from the event id
    private final ObservableList<ScheduledItem> videoScheduledItems = FXCollections.observableArrayList(); // The list of all videos for that event
    private final ObservableList<ScheduledItem> displayedVideoScheduledItems = FXCollections.observableArrayList(); // The list of all videos for that event

    private final ObjectProperty<ScheduledItem> watchVideoItemProperty = new SimpleObjectProperty<>(); // the VOD to watch (null for livestream)
    private final List<Media> watchMedias = new ArrayList<>(); // the medias of the VOD to watch


    private final CollapsePane videoCollapsePane = new CollapsePane(); // contains the video player(s): 1 for livestream, 1 per media for VOD
    private final List<MediaConsumptionRecorder> videoConsumptionRecorders = new ArrayList<>(); // the video consumption recorders (1 per player)
    private Player lastVideoPlayingPlayer; // the last playing player from this activity


    private final Label videoExpirationLabel = new Label();
    private final Label selectTheDayBelowLabel = I18nControls.newLabel(VideosI18nKeys.SelectTheDayBelow);
    private EntityStore entityStore;
    private EntityColumn<ScheduledItem>[] videoColumns;
    private final VisualGrid videoGrid = VisualGrid.createVisualGridWithResponsiveSkin();


    private final ObjectProperty<LocalDate> currentDaySelectedProperty = new SimpleObjectProperty<>();
    private MonoPane pageContainer;
    private DaySwitcher daySwitcher;

    public Page2EventDaysWithVideoActivity() {
        //We relaunch every 14 hours the request (in case the user never close the page, and to make sure the coherence of MediaConsumption is ok)
        Scheduler.schedulePeriodic(14 * 3600 * 1000, this::startLogic);
        videoCollapsePane.collapse(); // initially collapsed - might be automatically expanded by scheduleAutoLivestream()
    }

    @Override
    protected void updateModelFromContextParameters() {
        pathEventIdProperty.set(getParameter(Page2EventDaysWithVideoRouting.PATH_EVENT_ID_PARAMETER_NAME));
    }

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        entityStore = EntityStore.create(getDataSourceModel());

        // Initial data loading for the event specified in the path
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Object eventId = pathEventIdProperty.get();
            EntityId userPersonId = FXUserPersonId.getUserPersonId();
            if (eventId == null || userPersonId == null) {
                videoScheduledItems.clear();
                eventProperty.set(null);
            } else {
                entityStore.<Event>executeQuery("select name, label.(de,en,es,fr,pt), shortDescription, shortDescriptionLabel, audioExpirationDate, startDate, endDate, livestreamUrl, vodExpirationDate, repeatVideo, recurringWithVideo, repeatedEvent" +
                        " from Event where id=?", eventId)
                    .onFailure(Console::log)
                    .onSuccess(events -> {
                        Event currentEvent = events.get(0);
                        Event eventContainingVideos = Objects.coalesce(currentEvent.getRepeatedEvent(), currentEvent);
                        // We load all video scheduledItems booked by the user for the event (booking must be confirmed
                        // and paid). They will be grouped by day in the UI.
                        // Note: double dots such as programScheduledItem.timeline..startTime means we do a left join that allows null value (if the event is recurring, the timeline of the programScheduledItem is null)
                        entityStore.<ScheduledItem>executeQuery("select name, date, expirationDate, programScheduledItem.(name, startTime, endTime, timeline.(startTime, endTime), cancelled), published, event.(name, type.recurringItem, livestreamUrl, recurringWithVideo), vodDelayed, " +
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
                            .onSuccess(scheduledItems -> Platform.runLater(() -> {
                                videoScheduledItems.setAll(scheduledItems);
                                eventProperty.set(currentEvent);
                                populateVideos();
                                scheduleAutoLivestream();
                                LocalDate today = Event.todayInEventTimezone();
                                //If we are during the event, we position the currentSelectedDay to today
                                if(today.isAfter(currentEvent.getStartDate().minusDays(1))&&today.isBefore(currentEvent.getEndDate().plusDays(1))) {
                                    daySwitcher.setDay(today);
                                }
                            }));
                    });
            }
        }, pathEventIdProperty, FXUserPersonId.userPersonIdProperty());

        // Later Media loading when the user wants to watch a specific video (this sets watchVideoItemProperty)
        FXProperties.runOnPropertiesChange(() -> {
            ScheduledItem watchVideoItem = watchVideoItemProperty.get();
            EntityId userPersonId = FXUserPersonId.getUserPersonId();
            if (watchVideoItem == null || userPersonId == null) {
                watchMedias.clear();
                populateVideos(); // livestream
            } else {
                entityStore.<Media>executeQuery("select url from Media where scheduledItem=?", watchVideoItem)
                    .onFailure(Console::log)
                    .onSuccess(mediaLists -> Platform.runLater(() -> {
                        Collections.setAll(watchMedias, mediaLists);
                        populateVideos(); // VOD
                        videoCollapsePane.expand();
                    }));
            }
        }, watchVideoItemProperty, FXUserPersonId.userPersonIdProperty());

        VideoColumnsFormattersAndRenderers.registerRenderers();
        videoColumns = VisualEntityColumnFactory.get().fromJsonArray("""
            [
            {expression: 'date', label: 'Date', format: 'videoDate', hShrink: false, styleClass: 'date'},
            {expression: '[coalesce(startTime, timeline.startTime, programScheduledItem.startTime, programScheduledItem.timeline.startTime), coalesce(endTime, timeline.endTime, programScheduledItem.endTime, programScheduledItem.timeline.endTime)]', label: 'UK time', format: 'videoTimeRange', textAlign: 'center', hShrink: false, styleClass: 'time'},
            {expression: 'this', label: 'Name', renderer: 'videoName', minWidth: 200, styleClass: 'name'},
            {expression: 'this', label: 'Status', renderer: 'videoStatus', textAlign: 'center', hShrink: false, styleClass: 'status'}
            ]""", getDomainModel(), "ScheduledItem");
    }

    private void scheduleAutoLivestream() {
        LocalDateTime nowInEventTimezone = Event.nowInEventTimezone();
        // The livestream url is always the same for the event, but we still need to determine which
        // session is being played for the MediaConsumption management. To do this, we will set
        // scheduledVideoItemProperty with the scheduled item corresponding to the played session.
        for (ScheduledItem videoScheduledItem : videoScheduledItems) { // iterating video sessions
            VideoTimes videoTimes = new VideoTimes(videoScheduledItem);

            //If we're 20 minutes before or 30 minutes after the teaching, we display the livestream window
            if (videoTimes.isNowBetweenShowLivestreamStartAndShowLivestreamEnd() && !videoScheduledItem.getProgramScheduledItem().isCancelled()) {
                watchVideoItemProperty.set(null); // Stopping possible VOD and showing livestream instead
                videoCollapsePane.expand(); // Ensures the livestream player is showing
                UiScheduler.scheduleDelay(videoTimes.durationMillisBetweenNowAndShowLivestreamEnd(), this::scheduleAutoLivestream);
                return;
            } else if (videoTimes.isNowBeforeShowLivestreamStart()) {
                UiScheduler.scheduleDelay(videoTimes.durationMillisBetweenNowAndShowLivestreamStart(), this::scheduleAutoLivestream);
            }
        }
        // If we reach this point, it's because there is no livestream to show at the moment, so we collapse
        // videoCollapsePane (unless the user switched to a VOD in the meantime)
        if (isCollapsePaneContainingLivestream()) // livestream (not VOD)
            videoCollapsePane.collapse();
    }

    private boolean isCollapsePaneContainingLivestream() {
        return watchVideoItemProperty.get() == null;
    }

    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************

        Node loadingContentIndicator = new GoldenRatioPane(Controls.createProgressIndicator(100));
        pageContainer = new MonoPane(); // Will hold either the loading indicator or the loaded content

        // Building the loaded content, starting with the header
        MonoPane eventImageContainer = new MonoPane();
        Label eventLabel = Bootstrap.strong(I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", eventProperty), eventProperty));
        eventLabel.setWrapText(true);
        eventLabel.setTextAlignment(TextAlignment.CENTER);
        eventLabel.setPadding(new Insets(0, 0, 12, 0));

        HtmlText eventDescriptionHTMLText = new HtmlText();
        I18n.bindI18nTextProperty(eventDescriptionHTMLText.textProperty(), new I18nSubKey("expression: i18n(shortDescriptionLabel)", eventProperty), eventProperty);
        eventDescriptionHTMLText.managedProperty().bind(eventDescriptionHTMLText.textProperty().isNotEmpty());
        eventDescriptionHTMLText.setMaxHeight(60);

        videoExpirationLabel.setWrapText(true);
        videoExpirationLabel.setPadding(new Insets(30, 0, 0, 0));
        VBox titleVBox = new VBox(eventLabel, eventDescriptionHTMLText, videoExpirationLabel);

        MonoPane responsiveHeader = new MonoPane();
        new ResponsiveDesign(responsiveHeader)
            // 1. Horizontal layout (for desktops) - as far as TitleVBox is not higher than the image
            .addResponsiveLayout(/* applicability test: */ width -> {
                    double spacing = width * 0.05;
                    HBox.setMargin(titleVBox, new Insets(0, 0, 0, spacing));
                    double titleVBoxWidth = width - eventImageContainer.getWidth() - spacing;
                    //Here we resize the font according to the size of the window
                    double fontSizeFactor = Double.max(0.75, Double.min(1, titleVBoxWidth * 0.0042));
                    System.out.println("fontSizeFactor = " + fontSizeFactor);
                    //In JavaFX, the CSS has priority on Font, that's why we do a setStyle after. In web, the Font has priority on CSS
                    eventLabel.setFont(Font.font(fontSizeFactor * 30));
                    eventLabel.setStyle("-fx-font-size: " + fontSizeFactor * 30);
                    eventDescriptionHTMLText.setFont(Font.font(fontSizeFactor * 18));
                    return fontSizeFactor > 0.75;
                }, /* apply method: */ () -> responsiveHeader.setContent(new HBox(eventImageContainer, titleVBox))
                , /* test dependencies: */ eventImageContainer.widthProperty())
            // 2. Vertical layout (for mobiles) - when TitleVBox is too high (always applicable if 1. is not)
            .addResponsiveLayout(/* apply method: */ () -> {
                VBox vBox = new VBox(10, eventImageContainer, titleVBox);
                vBox.setAlignment(Pos.CENTER);
                VBox.setMargin(titleVBox, new Insets(5, 10, 5, 10)); // Same as cell padding => vertically aligned with cell content
                responsiveHeader.setContent(vBox);
            }).start();

         daySwitcher = new DaySwitcher(videoScheduledItems.stream()
            .map(ScheduledItem::getDate)
            .distinct()
            .sorted()
            .collect(Collectors.toList()),currentDaySelectedProperty.get(),pageContainer,VideosI18nKeys.EventSchedule);
         //We bind the currentDate of the daySwitcher to the currentDaySelected so the video appearing are linked to the day selected in the day switcher
        currentDaySelectedProperty.bind(daySwitcher.currentDateProperty());

        //The monoPane that manage the program day selection
        MonoPane responsiveDaySelectionMonoPane = new MonoPane();
        new ResponsiveDesign(responsiveDaySelectionMonoPane)
            // 1. Horizontal layout (for desktops)
            .addResponsiveLayout(/* applicability test: */ width -> {
                    //If we are instance of ResponsiveLayout, we're in the laptop more, otherwise we're in the mobile mode
                    return videoGrid.getSkin().getClass().getName().contains("Table");
                }, /* apply method: */ () -> responsiveDaySelectionMonoPane.setContent(daySwitcher.getDesktopView()))
            // 2. Vertical layout (for mobiles)
            .addResponsiveLayout(/* apply method: */ () -> {
                responsiveDaySelectionMonoPane.setContent(daySwitcher.getMobileViewContainer());
            }).start();


        StackPane decoratedLivestreamCollapsePane = CollapsePane.decorateCollapsePane(videoCollapsePane, true);
        //We display this box only if the current Date is in the list of date in the video Scheduled Item list
        VBox todayProgramVBox = new VBox(30); // Will be populated later (see reacting code below)
        todayProgramVBox.setAlignment(Pos.CENTER);
        Label todayVideosLabel = Bootstrap.strong(Bootstrap.textPrimary(Bootstrap.h3(new Label())));
        todayVideosLabel.setPadding(new Insets(100, 0, 40, 0));

        Label eventScheduleLabel = Bootstrap.h3(I18nControls.newLabel(VideosI18nKeys.EventSchedule));
        VBox selectTheDayBelowVBox = new VBox(5, eventScheduleLabel, selectTheDayBelowLabel);
        selectTheDayBelowVBox.setAlignment(Pos.CENTER);
        selectTheDayBelowVBox.setPadding(new Insets(100, 0, 0, 0));
        selectTheDayBelowVBox.setPadding(new Insets(100, 0, 0, 0));


        videoGrid.setMinRowHeight(48);
        videoGrid.setPrefRowHeight(Region.USE_COMPUTED_SIZE);
        videoGrid.setCellMargin(new Insets(5, 10, 5, 10));
        videoGrid.setFullHeight(true);
        videoGrid.setHeaderVisible(true);
        videoGrid.setAppContext(watchVideoItemProperty); // Passing watchVideoItemProperty as appContext to the value renderers

        VBox loadedContentVBox = new VBox(40,
            responsiveHeader, // contains the event image and the event title
            decoratedLivestreamCollapsePane,
            responsiveDaySelectionMonoPane,
            videoGrid // contains the videos for the selected day (or all days)
        );
        loadedContentVBox.setAlignment(Pos.TOP_CENTER);


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
            ModalityCloudinary.loadImage(eventCloudImagePath, eventImageContainer, -1, IMAGE_HEIGHT, SvgIcons::createVideoIconPath)
                .onFailure(error-> {
                    //If we can't find the picture of the cover for the selected language, we display the default image
                    ModalityCloudinary.loadImage(ModalityCloudinary.eventCoverImagePath(event, null), eventImageContainer, -1, IMAGE_HEIGHT, SvgIcons::createVideoIconPath);
                });
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

            // In single video per day mode (ex: STTP), we don't display the "Select the day below" label, because we automatically show all videos
            Layouts.setManagedAndVisibleProperties(selectTheDayBelowLabel, false);

            //Here we check if we should display the list of the days so we can select to display the program just for one day.
            if(videoScheduledItems.stream().collect(Collectors.groupingBy(ScheduledItem::getDate, Collectors.counting())).values().stream().anyMatch(count -> count > MIN_NUMBER_OF_SESSION_PER_DAY_BEFORE_DISPLAYING_DAILY_PROGRAM)) {
                responsiveDaySelectionMonoPane.setVisible(true);
                responsiveDaySelectionMonoPane.setManaged(true);

            } else {
                responsiveDaySelectionMonoPane.setVisible(false);
                responsiveDaySelectionMonoPane.setManaged(false);
            }



            // Showing selected videos for the currentSelected Day
            FXProperties.runNowAndOnPropertyChange(() -> {
                if(currentDaySelectedProperty.get()==null)
                    displayedVideoScheduledItems.setAll( videoScheduledItems);
                else
                    displayedVideoScheduledItems.setAll( videoScheduledItems.stream()
                        .filter(item -> item.getDate().equals(currentDaySelectedProperty.get()))
                        .collect(Collectors.toList()));
            }, currentDaySelectedProperty);

        }, videoScheduledItems, eventProperty);


        ObservableLists.runNowAndOnListOrPropertiesChange(change ->
            daySwitcher.populateDates(videoScheduledItems.stream()
                .map(ScheduledItem::getDate)
                .distinct()
                .sorted()
                .collect(Collectors.toList())),
            videoScheduledItems);

        // Showing selected videos once they are loaded
        ObservableLists.runNowAndOnListOrPropertiesChange(change -> {
            // Moving expired videos at the end of the list
            SortedList<ScheduledItem> videoScheduledItemsExpiredLast = displayedVideoScheduledItems.sorted((v1, v2) ->
                Boolean.compare(VideoState.isVideoExpired(v1), VideoState.isVideoExpired(v2)));
            VisualResult vr = EntitiesToVisualResultMapper.mapEntitiesToVisualResult(videoScheduledItemsExpiredLast, videoColumns);
            videoGrid.setVisualResult(vr);
        }, displayedVideoScheduledItems);

        // When the livestream collapse pane is collapsed, we pause the livestreamPlayer so the full-screen orange button
        // is not displayed
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Player playingPlayer = Players.getGlobalPlayerGroup().getPlayingPlayer();
            if (playingPlayer != null && SceneUtil.hasAncestor(playingPlayer.getMediaView(), videoCollapsePane)) {
                lastVideoPlayingPlayer = playingPlayer;
            }
            if (lastVideoPlayingPlayer != null) {
                if (videoCollapsePane.isCollapsed())
                    lastVideoPlayingPlayer.pause();
                else {
                    MediaConsumptionRecorder videoConsumptionRecorder = Collections.findFirst(videoConsumptionRecorders, vcr -> vcr.getPlayer() == lastVideoPlayingPlayer);
                    if (videoConsumptionRecorder != null) {
                        videoConsumptionRecorder.start();
                    }
                    lastVideoPlayingPlayer.play();
                }
            }
        }, videoCollapsePane.collapsedProperty(), Players.getGlobalPlayerGroup().playingPlayerProperty());





        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************

        pageContainer.getStyleClass().add("livestream");
        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(pageContainer);
    }



    private void populateVideos() {
        // If some previous videos were consumed, we stop their consumption recorders
        videoConsumptionRecorders.forEach(MediaConsumptionRecorder::stop);
        videoConsumptionRecorders.clear();
        Node videoContent = null;
        boolean autoPlay = videoCollapsePane.isExpanded();
        if (isCollapsePaneContainingLivestream()) { // Livestream
            Event event = eventProperty.get();
            String livestreamUrl = event.getLivestreamUrl();
            if (livestreamUrl != null) {
                videoContent = createVideoView(livestreamUrl, null, autoPlay);
            }
        } else { // VOD
            // Creating a Player for each Media and initializing it.
            VBox videoMediasVBox = new VBox(10);
            for (Media media : watchMedias) {
                Node videoView = createVideoView(media.getUrl(), media, autoPlay);
                videoMediasVBox.getChildren().add(videoView);
                // we autoplay only the first video
                autoPlay = false;
            }
            videoContent = videoMediasVBox;
        }
        videoCollapsePane.setContent(new ScalePane(ScaleMode.FIT_WIDTH, videoContent));
    }

    private Node createVideoView(String url, Media media, boolean autoPlay) {
        Player videoPlayer = AllPlayers.createAllVideoPlayer();
        videoPlayer.setMedia(videoPlayer.acceptMedia(url));
        videoPlayer.setStartOptions(new StartOptionsBuilder()
            .setAutoplay(autoPlay)
            .setAspectRatioTo16by9() // should be read from metadata but hardcoded for now
            .build());
        videoPlayer.displayVideo();
        boolean livestream = media == null;
        if (livestream)
            lastVideoPlayingPlayer = videoPlayer;
        MediaConsumptionRecorder videoConsumptionRecorder = new MediaConsumptionRecorder(videoPlayer, livestream, watchVideoItemProperty::get, () -> media);
        videoConsumptionRecorders.add(videoConsumptionRecorder);
        if (autoPlay)
            videoConsumptionRecorder.start();
        return videoPlayer.getMediaView();
    }

}
