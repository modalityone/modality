package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.CenteredPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.player.video.web.GenericWebVideoPlayer;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.frontoffice.utility.activity.FrontOfficeActivityUtil;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.event.client.mediaview.Players;

/**
 * @author Bruno Salmon
 */
final class SessionVideoPlayerActivity extends ViewDomainActivityBase {

    private static final double PAGE_TOP_BOTTOM_PADDING = 100;

    private final ObjectProperty<Object> scheduledVideoItemIdProperty = new SimpleObjectProperty<>();

    private final ObjectProperty<ScheduledItem> scheduledVideoItemProperty = new SimpleObjectProperty<>();
    private final ObservableList<Media> publishedMedias = FXCollections.observableArrayList();

    private final Label sessionTitleLabel = Bootstrap.h2(Bootstrap.strong(new Label()));
    private final GenericWebVideoPlayer sessionVideoPlayer = new GenericWebVideoPlayer();

    @Override
    protected void updateModelFromContextParameters() {
        scheduledVideoItemIdProperty.set(Numbers.toInteger(getParameter(SessionVideoPlayerRouting.SCHEDULED_VIDEO_ITEM_ID_PARAMETER_NAME)));
    }

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Object scheduledVideoItemId = scheduledVideoItemIdProperty.get();
            EntityId userPersonId = FXUserPersonId.getUserPersonId();
            if (scheduledVideoItemId == null || userPersonId == null) {
                publishedMedias.clear();
                scheduledVideoItemProperty.set(null);
            } else {
                entityStore.executeQueryBatch(
                        new EntityStoreQuery("select parent.name" +
                                             " from ScheduledItem si" +
                                             " where id=? and exists(select Attendance where scheduledItem=si and documentLine.(!cancelled and document.(person=? and price_balance<=0)))",
                            new Object[]{scheduledVideoItemId, userPersonId}),
                        new EntityStoreQuery("select url" +
                                             " from Media" +
                                             " where scheduledItem.(id=? and online) and published",
                            new Object[]{scheduledVideoItemId}))
                    .onFailure(Console::log)
                    .onSuccess(entityLists -> Platform.runLater(() -> {
                        publishedMedias.setAll(entityLists[1]);
                        scheduledVideoItemProperty.set((ScheduledItem) Collections.first(entityLists[0]));
                    }));
            }
        }, scheduledVideoItemIdProperty, FXUserPersonId.userPersonIdProperty());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restarting the session video player (if relevant) when reentering this activity. This will also ensure that
        // any possible previous playing player (ex: podcast) will be paused if/when the session video player restarts.
        updateSessionTitleAndVideoPlayerState();
    }

    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************

        // Back arrow and event title
        MonoPane backArrow = SvgIcons.createButtonPane(SvgIcons.createBackArrow(), getHistory()::goBack);

        sessionTitleLabel.setWrapText(true);
        sessionTitleLabel.setTextAlignment(TextAlignment.CENTER);

        CenteredPane backArrowAndTitlePane = new CenteredPane();
        backArrowAndTitlePane.setLeft(backArrow);
        backArrowAndTitlePane.setCenter(sessionTitleLabel);

        Node videoView = sessionVideoPlayer.getVideoView();
        /*if (videoView instanceof Region) {
            Region videoRegion = (Region) videoView;
            videoRegion.prefHeightProperty().bind(FXProperties.compute(videoRegion.widthProperty(), w -> w.doubleValue() / 16d * 9d));
        }*/

        VBox pageContainer = new VBox(40,
            backArrowAndTitlePane,
            videoView
        );


        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************

        // Auto starting the video for each requested session
        FXProperties.runNowAndOnPropertiesChange(this::updateSessionTitleAndVideoPlayerState,
            scheduledVideoItemProperty);


        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************

        pageContainer.setPadding(new Insets(PAGE_TOP_BOTTOM_PADDING, 0, PAGE_TOP_BOTTOM_PADDING, 0));
        return FrontOfficeActivityUtil.createActivityPageScrollPane(pageContainer, true);
    }

    private void updateSessionTitleAndVideoPlayerState() {
        ScheduledItem scheduledVideoItem = scheduledVideoItemProperty.get();
        Media firstMedia = Collections.first(publishedMedias);
        if (scheduledVideoItem != null && firstMedia != null) { // may not yet be loaded on first call
            String title = scheduledVideoItem.getParent().getName();
            String url = firstMedia.getUrl();
            sessionTitleLabel.setText(title);
            sessionVideoPlayer.getPlaylist().setAll(url);
            sessionVideoPlayer.play();
            Players.setPlayingPlayer(sessionVideoPlayer);
        }
    }
}
