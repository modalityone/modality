package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.panes.GoldenRatioPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.player.Player;
import dev.webfx.extras.player.audio.javafxmedia.JavaFXMediaAudioPlayer;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.Entities;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.*;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Bruno Salmon
 */
final class EventAudioPlaylistActivity extends ViewDomainActivityBase {

    private static final double IMAGE_HEIGHT = 188;

    private final ObjectProperty<Object> pathEventIdProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            Console.log("EventAudioPlaylistActivity.eventId = " + get());
        }
    };
    private final StringProperty pathItemCodeProperty = new SimpleStringProperty();

    //private final CloudImageService cloudImageService = new ClientImageService();

    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>();
    private final ObservableList<ScheduledItem> scheduledAudioItems = FXCollections.observableArrayList();
    private final List<Media> publishedMedias = new ArrayList<>(); // No need to be observable (reacting to scheduledAudioItems is enough)

    private Label audioExpirationLabel;
    private final StringProperty dateFormattedProperty = new SimpleStringProperty();

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
                entityStore.<Event>executeQuery("select name, label.(de,en,es,fr,pt), shortDescription, audioExpirationDate, startDate, endDate, livestreamUrl, vodExpirationDate, repeatAudio, repeatedEvent" +
                        " from Event where id=? limit 1", eventId)
                    .onFailure(Console::log)
                    .onSuccess(events -> {
                        Event currentEvent = events.get(0);
                        Object eventIdContainingAudios =  Entities.getPrimaryKey(currentEvent);
                        if(currentEvent.getRepeatedEventId()!=null) {
                            eventIdContainingAudios = Entities.getPrimaryKey(currentEvent.getRepeatedEventId());
                        }
                        entityStore.executeQueryBatch(
                                //Index 0: we look for the scheduledItem having a bookableScheduledItem which is a audio type (case of festival)
                                new EntityStoreQuery("select date, programScheduledItem.(name, timeline.(startTime, endTime)), published, event, " +
                                    " (select id from Attendance where scheduledItem=si.bookableScheduledItem and documentLine.document.person=? limit 1) as attendanceId, " +
                                    " (exists(select MediaConsumption where media.scheduledItem=si and attendance.documentLine.document.person=? and played) as alreadyPlayed), " +
                                    " (exists(select MediaConsumption where media.scheduledItem=si and attendance.documentLine.document.person=? and downloaded) as alreadyDownloaded) " +
                                    " from ScheduledItem si" +
                                    " where event=? and bookableScheduledItem.item.family.code=? and item.code=? and exists(select Attendance where scheduledItem=si.bookableScheduledItem and documentLine.(!cancelled and document.(person=? and event=? and confirmed and price_balance<=0)))" +
                                    " order by date",
                                    new Object[]{userPersonId,userPersonId,userPersonId, eventIdContainingAudios, KnownItemFamily.AUDIO_RECORDING.getCode(), pathItemCodeProperty.get(), userPersonId,currentEvent}),
                                //Index 1: we look for the scheduledItem of audio type having a bookableScheduledItem which is a teaching type (case of STTP)
                                // TODO: for now we take only the English audio recording scheduledItem in that case. We should take the language default of the organization instead
                                new EntityStoreQuery("select name, date, programScheduledItem.(name, timeline.(startTime, endTime)), published, event, " +
                                    " (select id from Attendance where scheduledItem=si.bookableScheduledItem and documentLine.document.person=? limit 1) as attendanceId, " +
                                    " (exists(select MediaConsumption where media.scheduledItem=si and attendance.documentLine.document.person=? and played) as alreadyPlayed), " +
                                    " (exists(select MediaConsumption where media.scheduledItem=si and attendance.documentLine.document.person=? and downloaded) as alreadyDownloaded) " +
                                    " from ScheduledItem si" +
                                    " where event=? and bookableScheduledItem.item.family.code=? and item.code=? and exists(select Attendance where scheduledItem=si.bookableScheduledItem and documentLine.(!cancelled and document.(person=? and event=? and confirmed and price_balance<=0)))" +
                                    " order by date",
                                    new Object[]{userPersonId,userPersonId,userPersonId, eventIdContainingAudios, KnownItemFamily.TEACHING.getCode(), KnownItem.AUDIO_RECORDING_ENGLISH.getCode(), userPersonId,currentEvent}),
                                //Index 2: the medias
                                new EntityStoreQuery("select url, scheduledItem.(date, event), scheduledItem.name, scheduledItem.published, durationMillis " +
                                    " from Media" +
                                    " where scheduledItem.(event=? and (item.code=? or item.code=?) and online) and scheduledItem.published",
                                    new Object[]{eventIdContainingAudios, pathItemCodeProperty.get(), KnownItem.AUDIO_RECORDING_ENGLISH.getCode()}))
                            .onFailure(Console::log)
                            .onSuccess(entityLists -> Platform.runLater(() -> {
                                eventProperty.set(currentEvent); // will update i18n bindings
                                Collections.setAll(publishedMedias, entityLists[2]);
                                scheduledAudioItems.setAll(entityLists[0]);
                                scheduledAudioItems.addAll(entityLists[1]);// will trigger UI update
                            }));
                    });
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
        MonoPane imageMonoPane = new MonoPane();
        ImageView imageView = new ImageView();

        headerHBox.getChildren().add(imageMonoPane);
        Label eventLabel = Bootstrap.h2(Bootstrap.strong(I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", eventProperty), eventProperty)));

        eventLabel.setWrapText(true);
        eventLabel.setTextAlignment(TextAlignment.CENTER);
        eventLabel.setPadding(new Insets(0, 0, 12, 0));
        HtmlText eventDescriptionHTMLText = new HtmlText();
        I18n.bindI18nTextProperty(eventDescriptionHTMLText.textProperty(), new I18nSubKey("expression: shortDescription", eventProperty), eventProperty);
        eventDescriptionHTMLText.managedProperty().bind(eventDescriptionHTMLText.textProperty().isNotEmpty());
        eventDescriptionHTMLText.setMaxHeight(60);
        audioExpirationLabel = Bootstrap.textSuccess(I18nControls.newLabel(AudioRecordingsI18nKeys.AvailableUntil, dateFormattedProperty));
        audioExpirationLabel.setPadding(new Insets(30, 0, 0, 0));
        VBox titleVBox = new VBox(eventLabel, eventDescriptionHTMLText, audioExpirationLabel);
        titleVBox.setMinWidth(200);

        headerHBox.getChildren().add(titleVBox);

        VBox audioTracksVBox = new VBox(20);

        Label listOfTrackLabel = I18nControls.newLabel(AudioRecordingsI18nKeys.ListOfTracks);
        listOfTrackLabel.getStyleClass().add("list-tracks-title");
        Player audioPlayer = new JavaFXMediaAudioPlayer();

        VBox loadedContentVBox = new VBox(40,
            headerHBox,
            audioPlayer.getMediaView(),
            listOfTrackLabel,
            audioTracksVBox
        );
        loadedContentVBox.setMaxWidth(SessionAudioTrackView.MAX_WIDTH);
        loadedContentVBox.setAlignment(Pos.CENTER);

        Node loadingContentIndicator = new GoldenRatioPane(Controls.createProgressIndicator(100));

        MonoPane pageContainer = new MonoPane();

        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************

        ObservableLists.runNowAndOnListOrPropertiesChange(change -> {
            // We display the loading indicator while the data is loading
            Event event = eventProperty.get();
            if (event == null) { // this indicates that the data has not finished loaded
                pageContainer.setContent(loadingContentIndicator);
                // TODO display something else (ex: next online events to book) when the user is not logged in, or registered
            } else { // otherwise we display loadedContentVBox and set the content of audioTracksVBox
                pageContainer.setContent(new ScalePane(loadedContentVBox));
                String lang = extractLang(pathItemCodeProperty.get());
                String cloudImagePath = ModalityCloudinary.eventCoverImagePath(event, lang);
                ModalityCloudinary.loadImage(cloudImagePath, imageMonoPane, -1, IMAGE_HEIGHT, SvgIcons::createAudioCoverPath);
                LocalDateTime audioExpirationDate = event.getAudioExpirationDate();
                if (audioExpirationDate != null) {
                    dateFormattedProperty.bind(LocalizedTime.formatLocalDateProperty(audioExpirationDate, FrontOfficeTimeFormats.AUDIO_PLAYLIST_DATE_FORMAT));
                    audioExpirationLabel.setVisible(true);
                } else {
                    FXProperties.setEvenIfBound(dateFormattedProperty, null);
                    audioExpirationLabel.setVisible(false);
                }

                LocalDateTime nowInEventTimezone = Event.nowInEventTimezone();
                if (audioExpirationDate == null || audioExpirationDate.isAfter(nowInEventTimezone)) {
                    // Does this event have audio recordings, and did the person booked and paid for them?
                    if (!scheduledAudioItems.isEmpty()) { // yes => we show them as a list of playable tracks
                        audioTracksVBox.getChildren().setAll(
                            IntStream.range(0, scheduledAudioItems.size())
                                .mapToObj(index ->
                                    new SessionAudioTrackView(
                                        scheduledAudioItems.get(index),
                                        publishedMedias,
                                        audioPlayer,
                                        index + 1, //The index is used to be display in the title, to number the different tracks
                                        scheduledAudioItems.size()).getView()
                                )
                                .collect(Collectors.toList()) // Collect the result as a List<Node>
                        );

                    } else { // no => we indicate that there is no recordings for that event
                        Label noContentLabel = Bootstrap.h3(Bootstrap.textWarning(I18nControls.newLabel(AudioRecordingsI18nKeys.NoAudioRecordingForThisEvent)));
                        noContentLabel.setPadding(new Insets(150, 0, 100, 0));
                        audioTracksVBox.getChildren().setAll(noContentLabel);
                        // TODO display another message when the event actually has audio recordings, but the user didn't book them
                    }
                } else {
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
        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(pageContainer);
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
