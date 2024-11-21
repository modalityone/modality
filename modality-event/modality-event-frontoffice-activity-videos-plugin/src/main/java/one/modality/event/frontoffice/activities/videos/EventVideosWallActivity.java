package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.CenteredPane;
import dev.webfx.extras.panes.CollapsePane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.player.StartOptionsBuilder;
import dev.webfx.extras.player.multi.MultiPlayer;
import dev.webfx.extras.player.multi.all.AllPlayers;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.frontoffice.utility.activity.FrontOfficeActivityUtil;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
final class EventVideosWallActivity extends ViewDomainActivityBase {

    static final String VIDEO_SCHEDULED_ITEM_DYNAMIC_BOOLEAN_FIELD_HAS_PUBLISHED_MEDIAS = "hasPublishedMedias";
    private static final double PAGE_TOP_BOTTOM_PADDING = 100;

    private final ObjectProperty<Object> pathEventIdProperty = new SimpleObjectProperty<>();

    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>();
    private final ObservableList<ScheduledItem> videoScheduledItems = FXCollections.observableArrayList();

    private final VBox livestreamVBox = new VBox(20);
    private final MultiPlayer livestreamVideoPlayer = AllPlayers.createAllVideoPlayer();

    @Override
    protected void updateModelFromContextParameters() {
        pathEventIdProperty.set(Numbers.toInteger(getParameter(EventVideosWallRouting.PATH_EVENT_ID_PARAMETER_NAME)));
    }

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Object eventId = pathEventIdProperty.get();
            EntityId userPersonId = FXUserPersonId.getUserPersonId();
            if (eventId == null || userPersonId == null) {
                videoScheduledItems.clear();
                eventProperty.set(null);
            } else {
                entityStore.executeQueryBatch(
                        new EntityStoreQuery("select name, label.(de,en,es,fr,pt), shortDescription, audioExpirationDate, startDate, endDate, livestreamUrl, vodExpirationDate" +
                                             " from Event" +
                                             " where id=? limit 1",
                            new Object[]{eventId}),
                        new EntityStoreQuery("select date, expirationDate, event, vodDelayed, parent.(name, timeline.(startTime, endTime), item.imageUrl)," +
                                             " exists(select Media where scheduledItem=si and published) as " + VIDEO_SCHEDULED_ITEM_DYNAMIC_BOOLEAN_FIELD_HAS_PUBLISHED_MEDIAS +
                                             " from ScheduledItem si" +
                                             " where item.family.code=? and online and exists(select Attendance where scheduledItem=si and documentLine.(!cancelled and document.(event= ? and person=? and price_balance<=0)))" +
                                             " order by date, parent.timeline.startTime",
                            new Object[]{KnownItemFamily.VIDEO.getCode(), eventId, userPersonId}))
                    .onFailure(Console::log)
                    .onSuccess(entityLists -> Platform.runLater(() -> {
                        videoScheduledItems.setAll(entityLists[1]);
                        eventProperty.set((Event) Collections.first(entityLists[0]));
                    }));
            }
        }, pathEventIdProperty, FXUserPersonId.userPersonIdProperty());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restarting the livestream video player (if relevant) when reentering this activity. This will also ensure that
        // any possible previous playing player (ex: podcast) will be paused if/when the livestream video player restarts.
        updateLivestreamVideoPlayerStateAndVisibility();
    }

    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************

        // Back arrow and event title
        MonoPane backArrow = SvgIcons.createButtonPane(SvgIcons.createBackArrow(), getHistory()::goBack);

        Label eventLabel = Bootstrap.h2(Bootstrap.strong(I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", eventProperty), eventProperty)));
        eventLabel.setWrapText(true);
        eventLabel.setTextAlignment(TextAlignment.CENTER);

        Label eventDescriptionLabel = I18nControls.newLabel(new I18nSubKey("expression: shortDescription", eventProperty), eventProperty);
        eventDescriptionLabel.setWrapText(true);
        eventDescriptionLabel.setTextAlignment(TextAlignment.CENTER);
        eventDescriptionLabel.managedProperty().bind(FXProperties.compute(eventDescriptionLabel.textProperty(), Strings::isNotEmpty));

        VBox titleVBox = new VBox(
            eventLabel,
            eventDescriptionLabel
        );
        titleVBox.setAlignment(Pos.CENTER);

        CenteredPane backArrowAndTitlePane = new CenteredPane();
        backArrowAndTitlePane.setLeft(backArrow);
        backArrowAndTitlePane.setCenter(titleVBox);

        // Livestream box
        Label livestreamLabel = Bootstrap.h4(Bootstrap.strong(I18nControls.newLabel(VideosI18nKeys.LivestreamTitle)));
        livestreamLabel.setWrapText(true);

        livestreamVideoPlayer.setStartOptions(new StartOptionsBuilder()
            .setAutoplay(true)
            .setAspectRatioTo16by9() // should be read from metadata but hardcoded for now
            .build());
        Node livestreamVideoView = livestreamVideoPlayer.getMediaView();

        livestreamVBox.getChildren().setAll(
            livestreamLabel,
            livestreamVideoView
        );

        // VBox showing all days and their videos (each node = container with day label + all videos of that day)
        VBox dayVideosWallVBox = new VBox(30); // Will be populated later (see reacting code below)

        Label pastVideoLabel = Bootstrap.h4(Bootstrap.strong(I18nControls.newLabel(VideosI18nKeys.PastRecordings)));
        Label noContentLabel = Bootstrap.h3(Bootstrap.textWarning(I18nControls.newLabel(VideosI18nKeys.NoVideosForThisEvent)));
        noContentLabel.setPadding(new Insets(150, 0, 100, 0));

        // Assembling all together in the page container
        VBox pageContainer = new VBox(50,
            backArrowAndTitlePane,
            livestreamVBox,
            dayVideosWallVBox
        );


        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************

        // Showing / hiding the livestream box (in dependence of the event)
        FXProperties.runNowAndOnPropertyChange(this::updateLivestreamVideoPlayerStateAndVisibility, eventProperty);

        // Creating an intermediate observable list of DayVideosWallView, each element being a view for 1 day with all its videos
        ObservableList<DayVideosWallView> dayVideosWallViews = FXCollections.observableArrayList(); // will be populated below

        // Creating a global chevron to collapse or expand all video days all together
        BooleanProperty collapsedAllProperty = FXProperties.newBooleanProperty(collapsed ->
                dayVideosWallViews.forEach(view -> view.setCollapsed(collapsed))
        );
        Node globalChevron = CollapsePane.armChevron(CollapsePane.createBlackChevron(), collapsedAllProperty);
        HBox pastVideoLabelAndChevronLine = new HBox(30, pastVideoLabel, globalChevron);
        pastVideoLabelAndChevronLine.setAlignment(Pos.CENTER_LEFT); // so pastVideoLabel & globalChevron are vertically aligned

        // Populating dayVideosWallViews from videoScheduledItems = flat list of all videos of the event (not yet grouped by day)
        ObservableLists.runNowAndOnListChange(change -> {
            // Grouping videos per day
            Map<LocalDate, List<ScheduledItem>> perDayGroups =
                videoScheduledItems.stream().collect(Collectors.groupingBy(ScheduledItem::getDate));
            dayVideosWallViews.clear();
            new TreeMap<>(perDayGroups) // The purpose of using a TreeMap is to sort the groups by keys (= days)
                .forEach((day, dayScheduledVideos) -> dayVideosWallViews.add(
                    // Passing the day, the videos of that day, and the history (for backward navigation)
                    new DayVideosWallView(day, dayScheduledVideos, getHistory())
                ));
        }, videoScheduledItems);

        // Now that we have dayVideosWallViews populated, we can populate the final VBox showing all days and their videos
        ObservableLists.runNowAndOnListChange(change -> {
            if (dayVideosWallViews.isEmpty()) {
                dayVideosWallVBox.getChildren().setAll(noContentLabel);
            } else {
                dayVideosWallVBox.getChildren().setAll(pastVideoLabelAndChevronLine);
                dayVideosWallVBox.getChildren().addAll(Collections.map(dayVideosWallViews, DayVideosWallView::getView));
            }
        }, dayVideosWallViews);

        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************

        pageContainer.setPadding(new Insets(PAGE_TOP_BOTTOM_PADDING, 0, PAGE_TOP_BOTTOM_PADDING, 0));
        return FrontOfficeActivityUtil.restrictToMaxPageWidth(pageContainer, true);
        //return FrontOfficeActivityUtil.createActivityPageScrollPane(pageContainer, true);
    }

    private void updateLivestreamVideoPlayerStateAndVisibility() {
        Event event = eventProperty.get();
        //If the event has a GlobalLiveStreamLink, and the event is not finished, we display the livestream screen.
        //TODO see how to we manage the timezone of the user.
        String eventLivestreamUrl = event == null || Times.isPast(event.getEndDate()) ? null : event.getLivestreamUrl();
        boolean showLivestream = Strings.isNotEmpty(eventLivestreamUrl);
        if (showLivestream) {
            livestreamVideoPlayer.setMedia(livestreamVideoPlayer.acceptMedia(eventLivestreamUrl));
            livestreamVideoPlayer.play(); // Will display and start the video (silent if before or after session)
            // The livestream player (Castr) doesn't support notification (unfortunately), so we don't wait onPlay()
            // to be called, we just inform right now that the livestream player is now playing, which will stop
            // any possible previous player (such as podcasts) immediately.
            //MediaPlayers.setPlayingPlayer(livestreamVideoPlayer);
        } else {
            // If there is no livestream, we pause the player (will actually stop it because pause is not supported)
            //MediaPlayers.pausePlayer(livestreamVideoPlayer); // ensures we silent the possible previous playing livestream
            livestreamVideoPlayer.pause();
        }
        livestreamVBox.setVisible(showLivestream);
        livestreamVBox.setManaged(showLivestream);
    }

}
