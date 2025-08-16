package one.modality.event.frontoffice.activities.videostreaming;

import dev.webfx.extras.aria.AriaToggleGroup;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.*;
import dev.webfx.extras.player.Player;
import dev.webfx.extras.player.Players;
import dev.webfx.extras.player.StartOptionsBuilder;
import dev.webfx.extras.player.multi.all.AllPlayers;
import dev.webfx.extras.responsive.ResponsiveDesign;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
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
import dev.webfx.platform.substitution.Substitutor;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.EntitiesToVisualResultMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.VisualEntityColumnFactory;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.knownitems.KnownItem;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.crm.frontoffice.help.HelpPanel;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.event.client.i18n.EventI18nKeys;
import one.modality.event.frontoffice.eventheader.EventHeader;
import one.modality.event.frontoffice.eventheader.MediaEventHeader;
import one.modality.event.frontoffice.medias.EventThumbnail;
import one.modality.event.frontoffice.medias.MediaConsumptionRecorder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is the activity for video streaming where people can watch the livestream and videos on demand.
 *
 * @author David Hello
 * @author Bruno Salmon
 */
final class VideoStreamingActivity extends ViewDomainActivityBase {

    private static final boolean DISABLE_COLLAPSE_VIDEO_PLAY = Booleans.booleanValue(Substitutor.substitute("${{DISABLE_COLLAPSE_VIDEO_PLAY}}"));

    private static final double STRAIGHT_MOBILE_LAYOUT_UNDER_WIDTH = 400; // mainly to reduce responsive computation on low-end devices
    private static final int MIN_NUMBER_OF_SESSION_PER_DAY_BEFORE_DISPLAYING_DAILY_PROGRAM = 3;
    private static final double COLUMN_MIN_WIDTH = 200;
    private static final double COLUMN_MAX_WIDTH = 530; // Max width = unscaled thumbnail (533 px)

    // Holding an observable list of events with videos booked by the user (changes on login & logout)
    private final ObservableList<Event> eventsWithBookedVideos = FXCollections.observableArrayList();
    // Holding a boolean property to know if the loading of the eventsWithBookedVideos is in progress
    private final BooleanProperty eventsWithBookedVideosLoadingProperty = new SimpleBooleanProperty();

    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>(); // The event loaded from the event id
    private final ObservableList<ScheduledItem> videoScheduledItems = FXCollections.observableArrayList(); // The list of all videos for that event
    private final ObservableList<ScheduledItem> displayedVideoScheduledItems = FXCollections.observableArrayList(); // The list of all videos for that event

    private final ObjectProperty<ScheduledItem> watchingVideoItemProperty = new SimpleObjectProperty<>(); // the Livestream or VOD to watch
    private final List<Media> watchMedias = new ArrayList<>(); // the medias of the VOD to watch

    private final CollapsePane videoCollapsePane = new CollapsePane(); // contains the video player(s): 1 for livestream, 1 per media for VOD
    private final StackPane decoratedLivestreamCollapsePane = CollapsePane.decorateCollapsePane(videoCollapsePane, true);
    private final List<MediaConsumptionRecorder> videoConsumptionRecorders = new ArrayList<>(); // the video consumption recorders (1 per player)
    private Player lastVideoPlayingPlayer; // the last playing player from this activity

    private final CollapsePane eventsSelectionPane = new CollapsePane();
    private final Label selectTheDayBelowLabel = I18nControls.newLabel(VideoStreamingI18nKeys.SelectTheDayBelow);
    private EntityStore entityStore;
    private final VisualGrid videoGrid =
        Screen.getPrimary().getVisualBounds().getWidth() <= STRAIGHT_MOBILE_LAYOUT_UNDER_WIDTH ?
            VisualGrid.createVisualGridWithMonoColumnLayoutSkin() :
            VisualGrid.createVisualGridWithResponsiveSkin();

    private final ObjectProperty<LocalDate> selectedDayProperty = new SimpleObjectProperty<>();
    private final MonoPane pageContainer = new MonoPane(); // Will hold either the loading indicator or the loaded content
    private final MonoPane responsiveDaySelectionMonoPane = new MonoPane();
    private final DaySwitcher daySwitcher = new DaySwitcher(pageContainer, VideoStreamingI18nKeys.EventSchedule);

    private boolean displayingDailyProgram;
    private EntityColumn<ScheduledItem>[] dailyProgramVideoColumns;
    private EntityColumn<ScheduledItem>[] allProgramVideoColumns;

    final AriaToggleGroup<ScheduledItem> watchButtonsGroup = new AriaToggleGroup<>();

    public VideoStreamingActivity() {
        //We relaunch the request every 14 hours (in case the user never closes the page, and to make sure the coherence of MediaConsumption is ok)
        Scheduler.schedulePeriodic(14 * 3600 * 1000, this::startLogic);
        eventsSelectionPane.collapse(); // initially collapsed
        videoCollapsePane.collapse(); // initially collapsed - might be automatically expanded by scheduleAutoLivestream()
        decoratedLivestreamCollapsePane.setVisible(false); // will be visible if it contains at least a video or livestream
        //We bind the currentDate of the daySwitcher to the currentDaySelected so the video appearing are linked to the day selected in the day switcher
        selectedDayProperty.bind(daySwitcher.selectedDateProperty());
    }


    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        entityStore = EntityStore.create(getDataSourceModel());
        // Loading the list of events with videos booked by the user and put it into eventsWithBookedVideos
        FXProperties.runNowAndOnPropertyChange(modalityUserPrincipal -> {
            eventsWithBookedVideos.clear();
            if (modalityUserPrincipal != null) {
                Object userAccountId = modalityUserPrincipal.getUserAccountId();
                eventsWithBookedVideosLoadingProperty.set(true);
                // we look for the scheduledItem having a `bookableScheduledItem` which is an audio type (case of festival)
                entityStore.<DocumentLine>executeQueryWithCache("cache-video-streaming-document-lines",
                        "select document.event.(name, label, shortDescription, shortDescriptionLabel, audioExpirationDate, startDate, endDate, livestreamUrl, vodExpirationDate, repeatVideo, recurringWithVideo, repeatedEvent), item.(code, family.code)" +
                        // We look if there are published audio ScheduledItem of type video, whose bookableScheduledItem has been booked
                        ", (exists(select ScheduledItem where item.family.code=$2 and bookableScheduledItem.(event=coalesce(dl.document.event.repeatedEvent, dl.document.event) and item=dl.item))) as published " +
                        // We check if the user has booked, not cancelled and paid the recordings
                        " from DocumentLine dl where !cancelled  and dl.document.(person.frontendAccount=$1 and confirmed and price_balance<=0) " +
                        " and dl.document.event.(kbs3 and (repeatedEvent = null or repeatVideo))" +
                        // we check if :
                        " and (" +
                        // 1/ there is a ScheduledItem of `video` family type whose `bookableScheduledItem` has been booked (KBS3 setup)
                        " exists(select Attendance a where documentLine=dl and exists(select ScheduledItem where bookableScheduledItem=a.scheduledItem and item.family.code=$2))" +
                        // 2/ Or KBS3 / KBS2 setup (this allows displaying the videos that have been booked in the past with KBS2 events, event if we can't display them)
                        " or item.family.code=$2)" +
                        // we display only the events that have not expired or expired since less than 21 days.
                        " and (document.event.(vodExpirationDate = null or date_part('epoch', now()) < date_part('epoch', vodExpirationDate)+21*24*60*60)) " +
                        // Ordering with the most relevant events, the first event will be the selected one by default.
                        " order by " +
                        // 1) Something happening today
                        " (exists(select Attendance where documentLine=dl and date=CURRENT_DATE)) desc" +
                        // 2) today is within event
                        ", document.event.(CURRENT_DATE >= startDate and CURRENT_DATE <= endDate) desc" +
                        // 3) Not expired
                        ", document.event.(vodExpirationDate = null or now() <= vodExpirationDate)" +
                        // 4) Smallest event (ex: favor Spring Festival over STTP)
                        ", document.event.(endDate - startDate)",
                        userAccountId, KnownItemFamily.VIDEO.getCode())
                    .onFailure(Console::log)
                    .inUiThread()
                    .onCacheAndOrSuccess(documentLines -> {
                        // Extracting the events with videos from the document lines
                        eventsWithBookedVideos.setAll(
                            Collections.map(documentLines, dl -> dl.getDocument().getEvent()));
                        // If there are 2 events with videos or more, we populate the events selection pane
                        if (eventsWithBookedVideos.size() < 2) {
                            eventsSelectionPane.setContent(null);
                        } else {
                            ColumnsPane columnsPane = new ColumnsPane(20, 50);
                            columnsPane.setMinColumnWidth(COLUMN_MIN_WIDTH);
                            columnsPane.setMaxColumnWidth(COLUMN_MAX_WIDTH);
                            columnsPane.setMaxWidth(Double.MAX_VALUE);
                            columnsPane.getStyleClass().add("audio-library"); // is audio-library good? (must be to have the same CSS rules as audio)
                            for (Event event : eventsWithBookedVideos) {
                                EventThumbnail thumbnail = new EventThumbnail(event, KnownItem.VIDEO.getCode(), EventThumbnail.ItemType.ITEM_TYPE_VIDEO, true);
                                Button actionButton = thumbnail.getViewButton();
                                actionButton.setCursor(Cursor.HAND);
                                actionButton.setOnAction(e -> {
                                    eventProperty.set(event);
                                    eventsSelectionPane.collapse();
                                });
                                columnsPane.getChildren().add(thumbnail.getView());
                            }
                            eventsSelectionPane.setContent(columnsPane);
                        }
                        // Selecting the most relevant event to show on start (the first one from order by)
                        eventProperty.set(Collections.first(eventsWithBookedVideos));
                        eventsWithBookedVideosLoadingProperty.set(false);
                    });
            }
        }, FXModalityUserPrincipal.modalityUserPrincipalProperty());
        // Initial data loading for the event specified in the path
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Event event = eventProperty.get();
            ModalityUserPrincipal modalityUserPrincipal = FXModalityUserPrincipal.getModalityUserPrincipal();
            videoScheduledItems.clear();
            if (event != null && modalityUserPrincipal != null) {
                Object userAccountId = modalityUserPrincipal.getUserAccountId();
                Event eventContainingVideos = Objects.coalesce(event.getRepeatedEvent(), event);
                // We load all video scheduledItems booked by the user for the event (booking must be confirmed
                // and paid). They will be grouped by day in the UI.
                // Note: double dots such as `programScheduledItem.timeline..startTime` means we do a left join that allows null value (if the event is recurring, the timeline of the programScheduledItem is null)
                entityStore.<ScheduledItem>executeQueryWithCache("cache-video-streaming-scheduled-items",
                        """
                            select name, label, date, comment, commentLabel, expirationDate, programScheduledItem.(name, label, startTime, endTime, timeline.(startTime, endTime), cancelled), published, event.(name, type.recurringItem, livestreamUrl, recurringWithVideo), vodDelayed,
                                (exists(select MediaConsumption where scheduledItem=si and attendance.documentLine.document.person.frontendAccount=$1) as attended),
                                (select id from Attendance where scheduledItem=si.bookableScheduledItem and documentLine.document.person.frontendAccount=$1 limit 1) as attendanceId
                             from ScheduledItem si
                             where event=$2
                                and bookableScheduledItem.item.family.code=$3
                                and item.code=$4
                                and exists(select Attendance a
                                 where scheduledItem=si.bookableScheduledItem
                                    and documentLine.(!cancelled and document.(person.frontendAccount=$1 and event=$5 and confirmed and price_balance<=0)))
                             order by date, programScheduledItem.timeline..startTime""",
                        /*$1*/ userAccountId, /*$2*/ eventContainingVideos, /*$3*/ KnownItemFamily.TEACHING.getCode(), /*$4*/ KnownItem.VIDEO.getCode(), /*$5*/ event)
                    .onFailure(Console::log)
                    .inUiThread()
                    .onCacheAndOrSuccess(scheduledItems -> {
                        videoScheduledItems.setAll(scheduledItems); // Will trigger the build of the video table.
                        watchingVideoItemProperty.set(null);
                        // We are now ready to populate the videos, but we postpone this for the 2 following reasons:
                        // 1) The UI may not be completely built yet on low-end devices, and loading a video player now
                        // could be heavy and freeze the UI even more.
                        // 2) If there is a livestream now (or close), scheduleAutoLivestream() will auto-expand the
                        // video player and auto-scroll to it, but the auto-scroll target position may not be stable at
                        // this time (ex: the video table not finished building), causing a wrong final scroll position.
                        UiScheduler.scheduleDelay(2000, () -> { // 2 seconds is a reasonable waiting time
                            populateVideoPlayers(false); // will load the video player
                            scheduleAutoLivestream(); // may auto-expand the video player if now is an appropriate time
                        });
                    });
            }
        }, eventProperty, FXUserPersonId.userPersonIdProperty());

        // Later Media loading when the user wants to watch a specific video (this sets watchVideoItemProperty)
        FXProperties.runOnPropertiesChange(() -> {
            EntityId userPersonId = FXUserPersonId.getUserPersonId();
            watchMedias.clear();
            if (userPersonId == null || isUserWatchingLivestream()) {
                populateVideoPlayers(true); // livestream
            } else { // The VOD requires additional Media loading
                loadMediaAndWatch();
            }
        }, watchingVideoItemProperty, FXUserPersonId.userPersonIdProperty());

        VideoFormattersAndRenderers.registerRenderers();
        // The columns (and groups) displayed for events with a daily program (such as Festivals)
        dailyProgramVideoColumns = VisualEntityColumnFactory.get().fromJsonArray( // language=JSON5
            """      
            [
                {expression: 'date', format: 'videoDate', role: 'group'},
                {expression: 'this', label: '"Session"', renderer: 'videoName', minWidth: 200, styleClass: 'name'},
                {expression: '[coalesce(startTime, timeline.startTime, programScheduledItem.startTime, programScheduledItem.timeline.startTime), coalesce(endTime, timeline.endTime, programScheduledItem.endTime, programScheduledItem.timeline.endTime)]', label: 'Time', format: 'videoTimeRange', textAlign: 'center', hShrink: false, styleClass: 'time'},
                {expression: 'this', label: 'Status', renderer: 'videoStatus', textAlign: 'center', hShrink: false, styleClass: 'status'}
            ]""".replace("\"Session\"", EventI18nKeys.Session.toString()), getDomainModel(), "ScheduledItem");
        // The columns (and groups) displayed for recurring events with 1 or just a few sessions per day (such as STTP)
        allProgramVideoColumns = VisualEntityColumnFactory.get().fromJsonArray( // language=JSON5
            """
            [
                {expression: 'this', format: 'allProgramGroup', textAlign: 'center', styleClass: 'status', role: 'group'},
                {expression: 'date', label: 'Date', format: 'videoDate', hShrink: false, styleClass: 'date'},
                {expression: '[coalesce(startTime, timeline.startTime, programScheduledItem.startTime, programScheduledItem.timeline.startTime), coalesce(endTime, timeline.endTime, programScheduledItem.endTime, programScheduledItem.timeline.endTime)]', label: 'Time', format: 'videoTimeRange', textAlign: 'center', hShrink: false, styleClass: 'time'},
                {expression: 'this', label: '"Session"', renderer: 'videoName', minWidth: 200, styleClass: 'name'},
                {expression: 'this', label: 'Status', renderer: 'videoStatus', textAlign: 'center', hShrink: false, styleClass: 'status'}
            ]""".replace("\"Session\"", EventI18nKeys.Session.toString()), getDomainModel(), "ScheduledItem");
    }

    private void scheduleAutoLivestream() {
        // We don't interrupt the user if he is already watching a video
        if (videoCollapsePane.isExpanded())
            return;

        // The livestream url is always the same for the event, but we still need to determine which
        // session is being played for the MediaConsumption management. To do this, we will set
        // `scheduledVideoItemProperty` with the scheduled item corresponding to the played session.
        for (ScheduledItem videoScheduledItem : videoScheduledItems) { // iterating video sessions
            VideoLifecycle videoLifecycle = new VideoLifecycle(videoScheduledItem);

            //If we're 20 minutes before or 30 minutes after the teaching, we display the livestream window
            if (isTimeToShowVideoAsLivestream(videoLifecycle)) {
                watchingVideoItemProperty.set(videoScheduledItem); // Stopping possible VOD and showing livestream instead
                videoCollapsePane.expand(); // Ensures the livestream player is showing
                UiScheduler.scheduleDelay(videoLifecycle.durationMillisBetweenNowAndShowLivestreamEnd(), this::scheduleAutoLivestream);
                return;
            } else if (videoLifecycle.isNowBeforeShowLivestreamStart()) {
                UiScheduler.scheduleDelay(videoLifecycle.durationMillisBetweenNowAndShowLivestreamStart(), this::scheduleAutoLivestream);
            }
        }
        // If we reach this point, it's because there is no livestream to show at the moment, so we collapse
        // videoCollapsePane (unless the user switched to a VOD in the meantime)
        if (isUserWatchingLivestream()) // livestream (not VOD)
            videoCollapsePane.collapse();
    }

    private void loadMediaAndWatch() {
        entityStore.<Media>executeQuery("select url from Media where scheduledItem=? order by id", getWatchingVideoItem())
            .onFailure(Console::log)
            .inUiThread()
            .onSuccess(mediaLists -> {
                Collections.setAll(watchMedias, mediaLists);
                populateVideoPlayers(true); // VOD
                videoCollapsePane.expand();
            });
    }

    private boolean isUserWatchingLivestream() {
        ScheduledItem watchingVideoItem = getWatchingVideoItem();
        return watchingVideoItem == null || isTimeToShowVideoAsLivestream(new VideoLifecycle(watchingVideoItem));
    }

    private boolean isTimeToShowVideoAsLivestream(VideoLifecycle videoLifecycle) {
        ScheduledItem videoScheduledItem = videoLifecycle.getVideoScheduledItem();
        return videoScheduledItem != null && !VideoState.isVideoCancelled(videoScheduledItem) && !videoScheduledItem.isPublished() && videoLifecycle.isNowBetweenShowLivestreamStartAndShowLivestreamEnd();
    }

    // Called by the "Watch" button from the VideoFormattersAndRenderers
    void setWatchingVideo(VideoLifecycle watchingVideoLifecycle) {
        // If it's a different video from the one currently watched by the user, we set the watchingVideoItemProperty
        // and this will trigger all necessary consequent events (loading of media, expanding videoCollapsePane and
        // auto-scrolling to the video player).
        ScheduledItem watchingVideoItem = watchingVideoLifecycle.getVideoScheduledItem();
        if (!isSameVideoAsAlreadyWatching(watchingVideoLifecycle)) {
            watchingVideoItemProperty.set(watchingVideoItem);
        } else { // But if it's the same video, the next step depends on its current state.
            // Let's start with the particular case where the user just received the push notification that the video
            // has been published (the livestream video became a VOD). While this push updated its published field,
            // the associated media are still unloaded, so we need now to load them and start the first media.
            if (watchingVideoItem.isPublished() && watchMedias.isEmpty()) { // detection of the case explained above
                loadMediaAndWatch();
            } else if (videoCollapsePane.isExpanded()) // otherwise, if the player is already expanded and with the
                // correct video already inside, the only remaining thing to do is to scroll to that video player
                scrollToVideoPlayer();
            else // if it is collapsed, we expand it first, and the auto-scroll will happen just after that - see buildUi()
                videoCollapsePane.expand();
        }
    }

    boolean isSameVideoAsAlreadyWatching(VideoLifecycle videoLifecycle) {
        return Objects.areEquals(videoLifecycle.getVideoScheduledItem(), getWatchingVideoItem())
               || isUserWatchingLivestream() && !videoLifecycle.getVideoScheduledItem().isPublished() && videoLifecycle.isNowBetweenLiveNowStartAndSessionEnd();
    }

    ScheduledItem getWatchingVideoItem() {
        return watchingVideoItemProperty.get();
    }

    ObjectProperty<ScheduledItem> watchingVideoItemProperty() {
        return watchingVideoItemProperty;
    }

    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        videoCollapsePane.prefWidthProperty().bind(pageContainer.widthProperty());

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************

        Node loadingContentIndicator = new GoldenRatioPane(Controls.createProgressIndicator(100));

        // We display an event selection section only if there are more than 1 event with videos booked by the user
        Hyperlink selectEventLink = I18nControls.newHyperlink("Select another event");
        selectEventLink.setOnAction(e -> eventsSelectionPane.toggleCollapse());
        VBox eventsSelectionVBox = new VBox(10, selectEventLink, eventsSelectionPane);
        Layouts.setMinMaxHeightToPref(eventsSelectionVBox); // No need to compute min/max height as different to pref (layout computation optimization)
        eventsSelectionVBox.setAlignment(Pos.CENTER);
        eventsSelectionPane.setPadding(new Insets(50, 0, 200, 0));
        // Making the section visible only if there are more than 1 event with videos
        eventsSelectionVBox.visibleProperty().bind(Bindings.size(eventsWithBookedVideos).greaterThan(1));
        Layouts.bindManagedToVisibleProperty(eventsSelectionVBox);

        // Building the loaded content, starting with the header
        EventHeader eventHeader = new MediaEventHeader(true);
        eventHeader.eventProperty().bind(eventProperty);

        //The monoPane that manage the program day selection
        new ResponsiveDesign(responsiveDaySelectionMonoPane)
            // 1. Table layout (for desktops)
            .addResponsiveLayout(/* applicability test: */ width -> {
                    //If the grid skin is a table, we're in the desktop mode, otherwise we're in the mobile mode
                    return VisualGrid.isTableLayout(videoGrid);
                }, /* apply method: */ () -> responsiveDaySelectionMonoPane.setContent(daySwitcher.getDesktopView()),
                /* test dependencies: */ videoGrid.skinProperty())
            // 2. Vertical layout (for mobiles)
            .addResponsiveLayout(
                /* apply method: */ () -> responsiveDaySelectionMonoPane.setContent(daySwitcher.getMobileViewContainer())
            ).start();

        //We display this box only if the current Date is in the list of date in the video Scheduled Item list
        VBox todayProgramVBox = new VBox(30); // Will be populated later (see reacting code below)
        Layouts.setMinMaxHeightToPref(todayProgramVBox); // No need to compute min/max height as different to pref (layout computation optimization)
        todayProgramVBox.setAlignment(Pos.CENTER);
        Label todayVideosLabel = Bootstrap.strong(Bootstrap.textPrimary(Bootstrap.h3(new Label())));
        todayVideosLabel.setPadding(new Insets(100, 0, 40, 0));

        Label eventScheduleLabel = Bootstrap.h3(I18nControls.newLabel(VideoStreamingI18nKeys.EventSchedule));
        VBox selectTheDayBelowVBox = new VBox(5, eventScheduleLabel, selectTheDayBelowLabel);
        Layouts.setMinMaxHeightToPref(selectTheDayBelowVBox); // No need to compute min/max height as different to pref (layout computation optimization)
        selectTheDayBelowVBox.setAlignment(Pos.CENTER);
        selectTheDayBelowVBox.setPadding(new Insets(100, 0, 0, 0));

        videoGrid.setMinRowHeight(48);
        videoGrid.setPrefRowHeight(Region.USE_COMPUTED_SIZE);
        videoGrid.setMonoCellMargin(new Insets(5, 10, 5, 10)); // top and bottom are more for mono colum layout (no real effect on table layout)
        videoGrid.setFullHeight(true);
        videoGrid.setHeaderVisible(true);
        videoGrid.setAppContext(this); // Passing this VideosActivity as appContext to the value renderers

        HtmlText festivalShopText = Bootstrap.strong(new HtmlText());
        I18n.bindI18nTextProperty(festivalShopText.textProperty(), VideoStreamingI18nKeys.FestivalShop);

        VBox loadedContentVBox = new VBox(40,
            eventsSelectionVBox,
            eventHeader.getView(), // contains the event image and the event title
            decoratedLivestreamCollapsePane,
            responsiveDaySelectionMonoPane,
            videoGrid, // contains the videos for the selected day (or all days)
            festivalShopText,
            HelpPanel.createEmailHelpPanel(VideoStreamingI18nKeys.VideosHelp, "kbs@kadampa.net")
            // For Festivals:
            //HelpPanel.createHelpPanel(VideoStreamingI18nKeys.VideosHelp, VideoStreamingI18nKeys.VideosHelpSecondary) // temporarily hardcoded i18n message for Festivals
        );
        Layouts.setMinMaxHeightToPref(loadedContentVBox); // No need to compute min/max height as different to pref (layout computation optimization)
        loadedContentVBox.setAlignment(Pos.TOP_CENTER);


        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************

        // Reacting to data loading (initially or when the event changes or on login/logout)
        ObservableLists.runNowAndOnListOrPropertiesChange(change -> {
            // We display the loading indicator while the data is loading
            if (eventsWithBookedVideosLoadingProperty.get()) { // this indicates that the data has not finished loaded
                pageContainer.setContent(loadingContentIndicator); // TODO display something else (ex: next online events to book) when the user is not logged in, or registered
                return;
            }
            // If the user didn't book any event with videos, we display "no content"
            if (eventsWithBookedVideos.isEmpty()) {
                Label noContentTitleLabel = Bootstrap.h3(I18nControls.newLabel(VideoStreamingI18nKeys.NoVideoInYourLibrary));
                noContentTitleLabel.setContentDisplay(ContentDisplay.TOP);
                noContentTitleLabel.setGraphicTextGap(20);
                Label noContentText = I18nControls.newLabel(VideoStreamingI18nKeys.YourNextLiveStreamEventWillAppearHere);

                VBox noContentVBox = new VBox(30, noContentTitleLabel, noContentText);
                noContentVBox.setAlignment(Pos.TOP_CENTER);
                pageContainer.setContent(noContentVBox);
                return;
            }
            // Otherwise, we display the videos and the program (loadedContentVBox with updated content)
            Event event = eventProperty.get();
            pageContainer.setContent(loadedContentVBox);

            // There are 2 modes of visualization: one with multiple videos per day (ex: Festivals), and one for with one video per day (ex: recurring events like STTP)
            Event eventContainingVideos = Objects.coalesce(event.getRepeatedEvent(), event);
            assert eventContainingVideos != null;

            //Here we check if we should display the list of the days, so we can select to display the program just for one day.
            displayingDailyProgram = videoScheduledItems.stream()
                .collect(Collectors.groupingBy(ScheduledItem::getDate, Collectors.counting()))
                .values().stream()
                .anyMatch(count -> count > MIN_NUMBER_OF_SESSION_PER_DAY_BEFORE_DISPLAYING_DAILY_PROGRAM);
            Layouts.setManagedAndVisibleProperties(selectTheDayBelowLabel, displayingDailyProgram);
            Layouts.setManagedAndVisibleProperties(responsiveDaySelectionMonoPane, displayingDailyProgram);
            Layouts.setManagedAndVisibleProperties(festivalShopText, displayingDailyProgram);

        }, videoScheduledItems, eventProperty, eventsWithBookedVideosLoadingProperty);

        // Updating the dates in daySwitch after the video sessions have been loaded
        ObservableLists.runNowAndOnListOrPropertiesChange(change ->
                daySwitcher.setAvailableDates(videoScheduledItems.stream()
                    .map(ScheduledItem::getDate)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList())),
            videoScheduledItems);

        // Showing selected videos for the currentSelected Day
        FXProperties.runNowAndOnPropertiesChange(() -> {
            LocalDate selectedDay = selectedDayProperty.get();
            if (!displayingDailyProgram || selectedDay == null)
                displayedVideoScheduledItems.setAll(videoScheduledItems);
            else
                displayedVideoScheduledItems.setAll(Collections.filter(videoScheduledItems, item -> selectedDay.equals(item.getDate())));
        }, selectedDayProperty, ObservableLists.versionNumber(videoScheduledItems));

        // Showing selected videos once they are loaded
        ObservableLists.runNowAndOnListOrPropertiesChange(change -> {
            VisualResult vr;
            // Because we group video sessions in the table, it's important sessions are sorted by group
            if (displayingDailyProgram) { // daily-program (ex: Festivals) => group = date
                // Sessions are already correctly sorted by the database query (by ascending date)
                vr = EntitiesToVisualResultMapper.mapEntitiesToVisualResult(displayedVideoScheduledItems, dailyProgramVideoColumns);
            } else { // all-program (ex: STTP) => group = LiveNow, Today, Upcoming or Past
                // Sorting sessions by group, but for Past session, we reverse the chronological order
                List<ScheduledItem> sortedVideoScheduledItems = displayedVideoScheduledItems.sorted((v1, v2) -> {
                    Object group1 = VideoState.getAllProgramVideoGroupI18nKey(v1); // LiveNow, Today, Upcoming or Past
                    Object group2 = VideoState.getAllProgramVideoGroupI18nKey(v2); // LiveNow, Today, Upcoming or Past
                    // Sorting Past sessions in chronological reverse order (the most recent first)
                    if (BaseI18nKeys.Past.equals(group1) && BaseI18nKeys.Past.equals(group2)) {
                        return -v1.getDate().compareTo(v2.getDate());
                    }
                    // Otherwise sorting by group order (keeping the original sort in groups other than Past - i.e., ascending date)
                    return Integer.compare(VideoState.getAllProgramVideoGroupOrder(group1), VideoState.getAllProgramVideoGroupOrder(group2));
                });
                vr = EntitiesToVisualResultMapper.mapEntitiesToVisualResult(sortedVideoScheduledItems, allProgramVideoColumns);
            }
            videoGrid.setVisualResult(vr);
        }, displayedVideoScheduledItems);

        // When the livestream collapse pane is collapsed, we pause the livestreamPlayer so the full-screen orange button
        // is not displayed
        if (!DISABLE_COLLAPSE_VIDEO_PLAY) {
            FXProperties.runNowAndOnPropertiesChange(() -> Platform.runLater(() -> { // Postponed to consider only the final state when both properties are changed
                Player playingPlayer = Players.getGlobalPlayerGroup().getPlayingPlayer();
                // commented as this prevents pausing VODs
                // if (playingPlayer != null && SceneUtil.hasAncestor(playingPlayer.getMediaView(), videoCollapsePane)) {
                lastVideoPlayingPlayer = playingPlayer;
                //}
                if (lastVideoPlayingPlayer != null) {
                    if (videoCollapsePane.isCollapsed()) {
                        Console.log("Pausing " + lastVideoPlayingPlayer);
                        lastVideoPlayingPlayer.pause();
                    } else {
                        MediaConsumptionRecorder videoConsumptionRecorder = Collections.findFirst(videoConsumptionRecorders,
                            vcr -> Players.sameSelectedPlayer(vcr.getPlayer(), lastVideoPlayingPlayer));
                        if (videoConsumptionRecorder != null)
                            videoConsumptionRecorder.start();
                        if (!lastVideoPlayingPlayer.isPlaying()) {
                            Console.log("⛔️⛔️⛔️⛔️⛔️ Playing " + lastVideoPlayingPlayer);
                            lastVideoPlayingPlayer.play();
                        }
                    }
                }
            }), videoCollapsePane.collapsedProperty(), Players.getGlobalPlayerGroup().playingPlayerProperty());
        }

        // Auto-scroll to the video player when it is expanded or watching a new video
        FXProperties.runNowAndOnPropertiesChange(() -> {
            if (videoCollapsePane.isExpanded()) { // auto-scroll only when expanded (might be automatically expanded before)
                UiScheduler.scheduleDeferred(() -> // the transition may start just after collapsedProperty is set, so we defer the call
                    // we wait for the possible transition to finish, and once finished, we scroll to the video player
                    FXProperties.onPropertyEquals(videoCollapsePane.transitingProperty(), Boolean.FALSE, x ->
                        scrollToVideoPlayer()));
            }
        }, videoCollapsePane.collapsedProperty(), watchingVideoItemProperty);

        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************

        pageContainer.getStyleClass().add("livestream");
        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(pageContainer);
    }

    private void scrollToVideoPlayer() {
        Controls.setVerticalScrollNodeWishedPosition(videoCollapsePane, VPos.CENTER);
        SceneUtil.scrollNodeToBeVerticallyVisibleOnScene(videoCollapsePane, false, true);
    }

    private void populateVideoPlayers(boolean willAutoplay) {
        // If some previous videos were consumed, we stop their consumption recorders
        videoConsumptionRecorders.forEach(MediaConsumptionRecorder::stop);
        videoConsumptionRecorders.clear();
        Node videoContent = null;
        VBox videoVBox = new VBox(20);
        videoVBox.setAlignment(Pos.CENTER);

        boolean autoPlay = willAutoplay || videoCollapsePane.isExpanded();
        if (isUserWatchingLivestream()) { // Livestream
            Event event = eventProperty.get();
            String livestreamUrl = event == null ? null : event.getLivestreamUrl();
            if (livestreamUrl != null) {
                // Checking that the user has access to a live session for today
                if (videoScheduledItems.stream().map(VideoLifecycle::new).anyMatch(VideoLifecycle::isLiveToday))
                    videoContent = createVideoView(livestreamUrl, null, autoPlay);
            }
        } else { // VOD
            // Creating a Player for each Media and initializing it.
            VBox videoMediasVBox = new VBox(10);
            String comment = getWatchingVideoItem().getComment();
            one.modality.base.shared.entities.Label commentLabel = getWatchingVideoItem().getCommentLabel();
            Label commentUILabel = null;
            if (commentLabel != null) {
                commentUILabel = I18nEntities.newExpressionLabel(commentLabel, "i18n(this)");
            } else if (comment != null) {
                commentUILabel = new Label(comment);
            }
            if (commentUILabel != null) {
                commentUILabel.getStyleClass().add("video-comment");
                commentUILabel.setTextAlignment(TextAlignment.CENTER);
                Controls.setupTextWrapping(commentUILabel, true, false);
                videoVBox.getChildren().add(commentUILabel);
            }

            for (Media media : watchMedias) {
                Node videoView = createVideoView(media.getUrl(), media, autoPlay);
                videoMediasVBox.getChildren().add(videoView);
                // we autoplay only the first video
                autoPlay = false;
            }
            Layouts.setMinMaxHeightToPref(videoMediasVBox); // No need to compute min/max height as different to pref (layout computation optimization)
            videoContent = videoMediasVBox;
        }
        if (videoContent == null) {
            videoCollapsePane.setContent(null);
        } else {
            ScalePane videoContainer = new ScalePane(ScaleMode.FIT_WIDTH, videoContent);
            videoVBox.getChildren().add(videoContainer);
            videoCollapsePane.setContent(videoVBox);
        }
        decoratedLivestreamCollapsePane.setVisible(videoContent != null);
    }

    private Node createVideoView(String url, Media media, boolean autoPlay) {
        Player videoPlayer = AllPlayers.createAllVideoPlayer();
        videoPlayer.setMedia(videoPlayer.acceptMedia(url));
        // Aspect ratio should be read from metadata but hardcoded for now
        double aspectRatio = 16d / 9d;
        if (url.contains("wistia"))   // Wistia is used only for the Festival play so far
            aspectRatio = 1085d / 595d; // This is the aspect ratio for the Life of Buddha play
        videoPlayer.setStartOptions(new StartOptionsBuilder()
            .setAutoplay(autoPlay)
            .setAspectRatio(aspectRatio)
            .build());
        videoPlayer.displayVideo();
        boolean livestream = media == null;
        if (livestream)
            lastVideoPlayingPlayer = videoPlayer;
        MediaConsumptionRecorder videoConsumptionRecorder = new MediaConsumptionRecorder(videoPlayer, livestream, watchingVideoItemProperty::get, () -> media);
        videoConsumptionRecorders.add(videoConsumptionRecorder);
        if (autoPlay)
            videoConsumptionRecorder.start();
        // We embed the video player in a 16/9 aspect ratio pane, so its vertical size is immediately known, which is
        // important for the correct computation of the auto-scroll position.
        return new AspectRatioPane(aspectRatio, videoPlayer.getMediaView());
    }

}
