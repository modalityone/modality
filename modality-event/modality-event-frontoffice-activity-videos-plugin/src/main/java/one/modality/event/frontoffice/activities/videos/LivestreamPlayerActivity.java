package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.player.StartOptionsBuilder;
import dev.webfx.extras.player.multi.MultiPlayer;
import dev.webfx.extras.player.multi.all.AllPlayers;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Numbers;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Event;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;

/**
 * @author Bruno Salmon
 */
final class LivestreamPlayerActivity extends AbstractVideoPlayerActivity {

    private final ObjectProperty<Object> eventIdProperty = new SimpleObjectProperty<>();
    private final MultiPlayer sessionVideoPlayer = AllPlayers.createAllVideoPlayer();
    private final SimpleObjectProperty<String> livestreamUrlProperty = new SimpleObjectProperty<>();
    private Event currentEvent;

    @Override
    protected void updateModelFromContextParameters() {
        eventIdProperty.set(Numbers.toInteger(getParameter(LivestreamPlayerRouting.EVENT_ID_PARAMETER_NAME)));
    }

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Object eventId = eventIdProperty.get();
            // EntityId userPersonId = FXUserPersonId.getUserPersonId();
            //TODO add the verification to check if the person is registered for this event and has pay.
            entityStore.executeQuery(
                    new EntityStoreQuery("select name, shortDescription, livestreamUrl" +
                        " from event" +
                        " where id=?", // and exists(select Attendance where document.(person=? and price_balance<=0)))",
                        new Object[]{eventId}))//, userPersonId}))
                .onFailure(Console::log)
                .onSuccess(entity -> Platform.runLater(() -> {
                    if (!entity.isEmpty()) {
                        currentEvent = (Event) entity.get(0);
                        livestreamUrlProperty.set(currentEvent.getLivestreamUrl());
                    }
                }));

        }, eventIdProperty, FXUserPersonId.userPersonIdProperty());
    }

    public Node buildUi() {

        Node node = super.buildUi();
        //On the livestream view, we have another element between the title and the video, which is a
        //VBox to display the live message
        VBox liveMessageVBox = new VBox(20);
        liveMessageVBox.setAlignment(Pos.CENTER);
        Label liveMessageTitleLabel = I18nControls.newLabel(VideosI18nKeys.LiveAnnoucementsTitle);
        Label liveMessageLabel = I18nControls.newLabel(VideosI18nKeys.LiveAnnoucements);
        liveMessageLabel.setWrapText(true);
        liveMessageLabel.setAlignment(Pos.CENTER);
        liveMessageVBox.getChildren().addAll(liveMessageTitleLabel,liveMessageLabel);

        sessionDescriptionVBox.setPadding(new Insets(0, 20, 0, 20));
        sessionCommentLabel.managedProperty().bind(sessionCommentLabel.textProperty().isNotEmpty());

        //We add it after the headerHBox
        pageContainer.getChildren().add(pageContainer.getChildren().indexOf(headerHBox)+1,liveMessageVBox);
        Node videoView = sessionVideoPlayer.getMediaView();
        playersVBoxContainer.getChildren().add(videoView);


        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************
        // Auto starting the video for each requested session
        FXProperties.runNowAndOnPropertyChange(this::updateSessionTitleAndVideoPlayerState, livestreamUrlProperty);
        return node;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restarting the session video player (if relevant) when reentering this activity. This will also ensure that
        // any possible previous playing player (ex: podcast) will be paused if/when the session video player restarts.
        //updateSessionTitleAndVideoPlayerState();
    }


    @Override
    protected void syncHeader() {
        String title = "Livestream";
        if(currentEvent!= null) {
            eventLabel.setText(currentEvent.getName());
            eventDescriptionHtmlText.setText(currentEvent.getShortDescription());
            if (currentEvent != null)
                updatePicture(currentEvent);
        }
        sessionTitleLabel.setText("Livestream Session");
    }

    @Override
    protected void syncPlayerContent() {
        if (livestreamUrlProperty.get() != null) {
            boolean autoPlay = true;
            sessionVideoPlayer.getMediaView().setVisible(true);
            sessionVideoPlayer.setMedia(sessionVideoPlayer.acceptMedia(livestreamUrlProperty.get()));
            sessionVideoPlayer.setStartOptions(new StartOptionsBuilder()
                .setAutoplay(autoPlay)
                .setAspectRatioTo16by9() // should be read from metadata but hardcoded for now
                .build());
            sessionVideoPlayer.play();
        } else {
            sessionVideoPlayer.setMedia(null);
            sessionVideoPlayer.resetToInitialState();
        }
    }
}
