package one.modality.event.frontoffice.activities.videostreaming;

import dev.webfx.extras.aria.AriaToggleGroup;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.GoldenRatioPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.responsive.ResponsiveDesign;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.EntitiesToVisualResultMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.VisualEntityColumnFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;
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
import one.modality.event.frontoffice.medias.TimeZoneSwitch;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is the activity for video streaming where people can watch the livestream and videos on demand.
 *
 * @author Bruno Salmon
 * @author David Hello
 */
final class VideoStreamingActivity extends ViewDomainActivityBase {

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
    final LivestreamAndVideoPlayers livestreamAndVideoPlayers = new LivestreamAndVideoPlayers(eventProperty, videoScheduledItems);

    public VideoStreamingActivity() {
        //We relaunch the request every 14 hours (in case the user never closes the page, and to make sure the coherence of MediaConsumption is ok)
        Scheduler.schedulePeriodic(14 * 3600 * 1000, this::startLogic);
        eventsSelectionPane.collapse(); // initially collapsed
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
                entityStore.<DocumentLine>executeQueryWithCache("modality/event/video-streaming/document-lines",
                        "select document.event.(name, label, shortDescription, shortDescriptionLabel, audioExpirationDate, startDate, endDate, livestreamUrl, timezone, vodExpirationDate, repeatVideo, recurringWithVideo, repeatedEvent, livestreamMessageLabel), item.(code, family.code)" +
                        // We look if there are published audio ScheduledItem of type video, whose bookableScheduledItem has been booked
                        ", (exists(select ScheduledItem where item.family.code=$2 and bookableScheduledItem.(event=coalesce(dl.document.event.repeatedEvent, dl.document.event) and item=dl.item))) as published " +
                        // We check if the user has booked, not cancelled and paid the recordings
                        " from DocumentLine dl where !cancelled  and dl.document.(confirmed and price_balance<=0 and accountCanAccessPersonMedias($1, person)) " +
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
                        if (eventsWithBookedVideos.stream().filter(e -> Times.isFuture(e.getVodExpirationDate())).count() < 2) {
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
                TimeZoneSwitch.getGlobal().setEventZoneId(event.getEventZoneId());
                Object userAccountId = modalityUserPrincipal.getUserAccountId();
                Event eventContainingVideos = Objects.coalesce(event.getRepeatedEvent(), event);
                // We load all video scheduledItems booked by the user for the event (booking must be confirmed
                // and paid). They will be grouped by day in the UI.
                // Note: double dots such as `programScheduledItem.timeline..startTime` means we do a left join that allows null value (if the event is recurring, the timeline of the programScheduledItem is null)
                entityStore.<ScheduledItem>executeQueryWithCache("modality/event/video-streaming/scheduled-items",
                        """
                            select name, label, date, comment, commentLabel, expirationDate, programScheduledItem.(name, label, startTime, endTime, timeline.(startTime, endTime), cancelled), published, event.(name, type.recurringItem, livestreamUrl, recurringWithVideo, livestreamMessageLabel), vodDelayed,
                                (exists(select MediaConsumption where scheduledItem=si and accountCanAccessPersonMedias($1, attendance.documentLine.document.person)) as attended),
                                (select id from Attendance where scheduledItem=si.bookableScheduledItem and accountCanAccessPersonMedias($1, documentLine.document.person) limit 1) as attendanceId
                             from ScheduledItem si
                             where event=$2
                                and bookableScheduledItem.item.family.code=$3
                                and item.code=$4
                                and exists(select Attendance a
                                 where scheduledItem=si.bookableScheduledItem
                                    and documentLine.(!cancelled and document.(event=$5 and confirmed and price_balance<=0 and accountCanAccessPersonMedias($1, person))))
                             order by date, programScheduledItem.timeline..startTime""",
                        /*$1*/ userAccountId, /*$2*/ eventContainingVideos, /*$3*/ KnownItemFamily.TEACHING.getCode(), /*$4*/ KnownItem.VIDEO.getCode(), /*$5*/ event)
                    .onFailure(Console::log)
                    .inUiThread()
                    .onCacheAndOrSuccess(videoScheduledItems::setAll); // Will trigger the build of the video table.
            }
        }, eventProperty, FXUserPersonId.userPersonIdProperty());

        livestreamAndVideoPlayers.startLogic(entityStore);

        VideoFormattersAndRenderers.registerRenderers();
        // The columns (and groups) displayed for events with a daily program (such as Festivals)
        dailyProgramVideoColumns = VisualEntityColumnFactory.get().fromJsonArray( // language=JSON5
            """      
            [
                {expression: 'this', format: 'videoDate', role: 'group'},
                {expression: 'this', label: '"Session"', renderer: 'videoName', minWidth: 200, styleClass: 'name'},
                {expression: 'this', label: 'Time', format: 'videoTimeRange', textAlign: 'center', hShrink: false, styleClass: 'time'},
                {expression: 'this', label: 'Status', renderer: 'videoStatus', textAlign: 'center', hShrink: false, styleClass: 'status'}
            ]""".replace("\"Session\"", EventI18nKeys.Session.toString()), getDomainModel(), "ScheduledItem");
        // The columns (and groups) displayed for recurring events with 1 or just a few sessions per day (such as STTP)
        allProgramVideoColumns = VisualEntityColumnFactory.get().fromJsonArray( // language=JSON5
            """
            [
                {expression: 'this', format: 'allProgramGroup', textAlign: 'center', styleClass: 'status', role: 'group'},
                {expression: 'this', label: 'Date', format: 'videoDate', hShrink: false, styleClass: 'date'},
                {expression: 'this', label: 'Time', format: 'videoTimeRange', textAlign: 'center', hShrink: false, styleClass: 'time'},
                {expression: 'this', label: '"Session"', renderer: 'videoName', minWidth: 200, styleClass: 'name'},
                {expression: 'this', label: 'Status', renderer: 'videoStatus', textAlign: 'center', hShrink: false, styleClass: 'status'}
            ]""".replace("\"Session\"", EventI18nKeys.Session.toString()), getDomainModel(), "ScheduledItem");
    }


    // Called by the "Watch" button from the VideoFormattersAndRenderers
    public LivestreamAndVideoPlayers getVideosPlayer() {
        return livestreamAndVideoPlayers;
    }

    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************

        Node loadingContentIndicator = new GoldenRatioPane(Controls.createProgressIndicator(100));

        // We display an event selection section only if there are more than 1 event with videos booked by the user
        Hyperlink selectEventLink = I18nControls.newHyperlink(VideoStreamingI18nKeys.SelectAnotherEvent);
        selectEventLink.setOnAction(e -> eventsSelectionPane.toggleCollapse());
        VBox eventsSelectionVBox = new VBox(10, selectEventLink, eventsSelectionPane);
        Layouts.setMinMaxHeightToPref(eventsSelectionVBox); // No need to compute min/max height as different to pref (layout computation optimization)
        eventsSelectionVBox.setAlignment(Pos.CENTER);
        eventsSelectionPane.setPadding(new Insets(50, 0, 200, 0));
        // Making the section visible only if there are more than 1 event with videos
        eventsSelectionVBox.visibleProperty().bind(eventsSelectionPane.contentProperty().isNotNull());
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
            livestreamAndVideoPlayers.buildUi(pageContainer),
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
        }, displayedVideoScheduledItems
            // We also rebuild the whole table on time zone change, because the video date groups may start at different rows for different timezones
            , TimeZoneSwitch.getGlobal().eventLocalTimeSelectedProperty());

        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************

        pageContainer.getStyleClass().add("livestream");
        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(pageContainer);
    }

}
