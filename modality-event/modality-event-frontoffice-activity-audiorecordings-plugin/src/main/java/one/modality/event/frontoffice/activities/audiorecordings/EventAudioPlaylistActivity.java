package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.panes.GoldenRatioPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.player.audio.javafxmedia.JavaFXMediaAudioPlayer;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.cloud.image.CloudImageService;
import dev.webfx.stack.cloud.image.impl.client.ClientImageService;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Bruno Salmon
 */
final class EventAudioPlaylistActivity extends ViewDomainActivityBase {

    private static final double PAGE_TOP_BOTTOM_PADDING = 100;
    private static final double IMAGE_HEIGHT = 188;

    private final ObjectProperty<Object> pathEventIdProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            Console.log("eventId = " + get());
        }
    };
    private final StringProperty pathItemCodeProperty = new SimpleStringProperty();

    private final CloudImageService cloudImageService = new ClientImageService();

    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>();
    private final ObservableList<ScheduledItem> scheduledAudioItems = FXCollections.observableArrayList();
    private final List<Media> publishedMedias = new ArrayList<>(); // No need to be observable (reacting to scheduledAudioItems is enough)

    private Label audioExpirationLabel;
    private StringProperty dateFormattedProperty = new SimpleStringProperty();

    @Override
    protected void updateModelFromContextParameters() {
        pathEventIdProperty.set(Numbers.toInteger(getParameter(EventAudioPlaylistRouting.PATH_EVENT_ID_PARAMETER_NAME)));
        pathItemCodeProperty.set(getParameter(EventAudioPlaylistRouting.PATH_ITEM_CODE_PARAMETER_NAME));
    }

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        // Loading the required data in dependence of the request event and the user account
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Object eventId = pathEventIdProperty.get();
            EntityId userPersonId = FXUserPersonId.getUserPersonId();
            if (eventId == null || userPersonId == null) { // No access if the user is not logged in or registered
                publishedMedias.clear();
                scheduledAudioItems.clear(); // will trigger UI update
                eventProperty.set(null); // will update i18n bindings
            } else {
                entityStore.executeQueryBatch(
                        new EntityStoreQuery("select name, label.(de,en,es,fr,pt), shortDescription, audioExpirationDate, startDate, endDate, livestreamUrl, vodExpirationDate" +
                                             " from Event" +
                                             " where id=? limit 1",
                            new Object[]{eventId}),
                        new EntityStoreQuery("select date, parent.(name, timeline.(startTime, endTime)), published, event" +
                                             " from ScheduledItem si" +
                                             " where event=? and item.family.code=? and item.code=? and exists(select Attendance where scheduledItem=si and documentLine.(!cancelled and document.(person=? and price_balance<=0)))" +
                                             " order by date",
                            new Object[]{eventId, KnownItemFamily.AUDIO_RECORDING.getCode(), pathItemCodeProperty.get(), userPersonId}),
                        new EntityStoreQuery("select url, scheduledItem.(date, event), scheduledItem.published, durationMillis" +
                                             " from Media" +
                                             " where scheduledItem.(event=? and item.code=? and online) and scheduledItem.published",
                            new Object[]{eventId, pathItemCodeProperty.get()}))
                    .onFailure(Console::log)
                    .onSuccess(entityLists -> Platform.runLater(() -> {
                        Collections.setAll(publishedMedias, entityLists[2]);
                        scheduledAudioItems.setAll(entityLists[1]); // will trigger UI update
                        eventProperty.set((Event) Collections.first(entityLists[0])); // will update i18n bindings
                    }));
            }
        }, pathEventIdProperty, pathItemCodeProperty, FXUserPersonId.userPersonIdProperty());
    }

    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************
        HBox headerHBox = new HBox();
        headerHBox.setSpacing(50);
        headerHBox.setPadding(new Insets(0,20,0,20));
        headerHBox.setMaxWidth(1024);
        MonoPane imageMonoPane = new MonoPane();
        ImageView imageView = new ImageView();

        headerHBox.getChildren().add(imageMonoPane);
        Label eventLabel = Bootstrap.h2(Bootstrap.strong(I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", eventProperty), eventProperty)));

        eventLabel.setWrapText(true);
        eventLabel.setTextAlignment(TextAlignment.CENTER);
        eventLabel.setPadding(new Insets(0,0,12,0));
        Label eventDescriptionLabel = I18nControls.newLabel(new I18nSubKey("expression: shortDescription", eventProperty), eventProperty);
        eventDescriptionLabel.setWrapText(true);
        eventDescriptionLabel.setTextAlignment(TextAlignment.LEFT);
        eventDescriptionLabel.managedProperty().bind(FXProperties.compute(eventDescriptionLabel.textProperty(), Strings::isNotEmpty));
        eventDescriptionLabel.setMaxHeight(60);
        audioExpirationLabel = Bootstrap.textSuccess(I18nControls.newLabel(AudioRecordingsI18nKeys.AvailableUntil,dateFormattedProperty));
        audioExpirationLabel.setPadding(new Insets(30,0,0,0));
        VBox titleVBox = new VBox(eventLabel, eventDescriptionLabel,audioExpirationLabel);

        headerHBox.getChildren().add(titleVBox);

        VBox audioTracksVBox = new VBox(20);
        audioTracksVBox.setMaxWidth(SessionAudioTrackView.MAX_WIDTH);


        Label listOfTrackLabel = I18nControls.newLabel(AudioRecordingsI18nKeys.ListOfTracks);
        listOfTrackLabel.getStyleClass().add("list-tracks-title");
        JavaFXMediaAudioPlayer audioPlayer = new JavaFXMediaAudioPlayer();

        VBox loadedContentVBox = new VBox(40,
            headerHBox,
            audioPlayer.getMediaView(),
            listOfTrackLabel,
            audioTracksVBox
        );
        loadedContentVBox.setAlignment(Pos.CENTER);

        Node loadingContentIndicator = new GoldenRatioPane(ControlUtil.createProgressIndicator(100));

        MonoPane pageContainer = new MonoPane();


        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************

        ObservableLists.runNowAndOnListOrPropertiesChange(change -> {
            // We display the loading indicator while the data is loading
            if (eventProperty.get() == null) { // this indicates that the data has not finished loaded
                pageContainer.setContent(loadingContentIndicator);
                // TODO display something else (ex: next online events to book) when the user is not logged in, or registered
            } else { // otherwise we display loadedContentVBox and set the content of audioTracksVBox
                pageContainer.setContent(loadedContentVBox);
                Object imageTag;
                if(pathItemCodeProperty.get()==null ||"en".equals(extractLang(pathItemCodeProperty.get()))) {
                    //We do add .jpg even if the image is not jpg, because for some reason, if we don't put an extension file, cloudinary doesn't always find the image, but it works when adding .jpg.
                    imageTag = eventProperty.get().getId().getPrimaryKey() + "-cover.jpg";
                } else {
                    imageTag = eventProperty.get().getId().getPrimaryKey() + "-cover-"+ extractLang(pathItemCodeProperty.get())+".jpg";
                }
                String pictureId = String.valueOf(imageTag);

                cloudImageService.exists(pictureId)
                    .onFailure(Console::log)
                    .onSuccess(exists -> Platform.runLater(() -> {
                        Console.log("exists: " + exists);
                        if (exists) {
                            imageMonoPane.setBackground(null);
                            //First, we need to get the zoom factor of the screen
                            double zoomFactor = Screen.getPrimary().getOutputScaleX();
                            String url = cloudImageService.url(pictureId, -1, (int) (IMAGE_HEIGHT*zoomFactor));
                            imageView.setFitHeight(IMAGE_HEIGHT);
                            imageView.setPreserveRatio(true);
                            Image imageToDisplay = new Image(url, true);
                            imageView.setImage(imageToDisplay);
                            imageMonoPane.getChildren().setAll(imageView);
                        }
                        else {
                            SVGPath audioCoverPath = SvgIcons.createAudioCoverPath();
                            imageMonoPane.setBackground(new Background(
                                new BackgroundFill(Color.LIGHTGRAY, null, null)
                            ));
                            imageMonoPane.getChildren().setAll(audioCoverPath);
                            imageMonoPane.setAlignment(Pos.CENTER);
                        }
                    }));
                if(eventProperty.get().getAudioExpirationDate()!=null) {
                    dateFormattedProperty.set(eventProperty.get().getAudioExpirationDate().format(DateTimeFormatter.ofPattern("d MMMM, yyyy")));
                    audioExpirationLabel.setVisible(true);
                }
                else {
                    audioExpirationLabel.setVisible(false);
                }
                if(eventProperty.get().getAudioExpirationDate().isAfter(LocalDateTime.now())) {
                    // Does this event have audio recordings, and did the person booked and paid for them?
                    if (!scheduledAudioItems.isEmpty()) { // yes => we show them as a list of playable tracks
                        audioTracksVBox.getChildren().setAll(
                            IntStream.range(0, scheduledAudioItems.size())
                                .mapToObj(index ->
                                    new SessionAudioTrackView(
                                        scheduledAudioItems.get(index),
                                        publishedMedias,
                                        audioPlayer,
                                        index+1 //The index is used to be display in the title, to number the different tracks
                                    ).getView()
                                )
                                .collect(Collectors.toList()) // Collect the result as a List<Node>
                        );

                    } else { // no => we indicate that there is no recordings for that event
                        Label noContentLabel = Bootstrap.h3(Bootstrap.textWarning(I18nControls.newLabel(AudioRecordingsI18nKeys.NoAudioRecordingForThisEvent)));
                        noContentLabel.setPadding(new Insets(150, 0, 100, 0));
                        audioTracksVBox.getChildren().setAll(noContentLabel);
                        // TODO display another message when the event actually has audio recordings, but the user didn't book them
                    }
                }
                else {
                    Label noContentLabel = Bootstrap.h3(Bootstrap.textWarning(I18nControls.newLabel(AudioRecordingsI18nKeys.ContentExpired)));
                    noContentLabel.setPadding(new Insets(150, 0, 100, 0));
                    audioTracksVBox.getChildren().setAll(noContentLabel);
                }
            }
        }, scheduledAudioItems, eventProperty);


        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************

        // Setting a max width for big desktop screens
        pageContainer.setPadding(new Insets(PAGE_TOP_BOTTOM_PADDING, 0, PAGE_TOP_BOTTOM_PADDING, 0));
        return pageContainer;
    }

    private String extractLang(String itemCode) {
        //the itemCode is in the form audio-fr
        if (itemCode == null || !itemCode.contains("-")) {
            return null;
        }
        int dashIndex = itemCode.indexOf("-");
        return itemCode.substring(dashIndex + 1);
    }

}
