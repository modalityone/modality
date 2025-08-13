package one.modality.event.frontoffice.activities.audiolibrary;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.GoldenRatioPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.ScaleMode;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.player.Player;
import dev.webfx.extras.player.audio.javafxmedia.JavaFXMediaAudioPlayer;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.visual.SelectionMode;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.EntitiesToVisualResultMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.VisualEntityColumnFactory;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.knownitems.KnownItem;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.crm.frontoffice.help.HelpPanel;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;
import one.modality.event.frontoffice.eventheader.EventHeader;
import one.modality.event.frontoffice.eventheader.MediaEventHeader;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class EventAudioLibraryActivity extends ViewDomainActivityBase {

    private final ObjectProperty<Object> pathEventIdProperty = new SimpleObjectProperty<>();
    private final StringProperty pathItemCodeProperty = new SimpleStringProperty();

    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>();
    private final ObservableList<ScheduledItem> scheduledAudioItems = FXCollections.observableArrayList();
    private final List<Media> publishedMedias = new ArrayList<>(); // No need to be observable (reacting to scheduledAudioItems is enough)

    private EntityColumn<ScheduledItem>[] audioColumns;

    private final Player audioPlayer = new JavaFXMediaAudioPlayer();
    private final VisualGrid audioGrid =
        // Using mono-column skin on mobiles with screen < 600 px to prevent unnecessary table computing (can cause performance issue on low-end mobiles)
        Screen.getPrimary().getVisualBounds().getWidth() < 600 ?
            VisualGrid.createVisualGridWithMonoColumnLayoutSkin() :
            // Otherwise, we use the responsive skin, which will decide between table and mono-column skin
            VisualGrid.createVisualGridWithResponsiveSkin();

    @Override
    protected void updateModelFromContextParameters() {
        pathEventIdProperty.set(Numbers.toInteger(getParameter(EventAudioLibraryRouting.PATH_EVENT_ID_PARAMETER_NAME)));
        pathItemCodeProperty.set(getParameter(EventAudioLibraryRouting.PATH_ITEM_CODE_PARAMETER_NAME));
    }

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        // Loading the required data in dependence of the request event and the user account
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Object eventId = pathEventIdProperty.get();
            if (eventId == null || FXModalityUserPrincipal.getModalityUserPrincipal() == null) { // No access if the user is not logged in or registered
                publishedMedias.clear();
                scheduledAudioItems.clear(); // will trigger UI update
                eventProperty.set(null); // will update i18n bindings
            } else {
                entityStore.<Event>executeQueryWithCache("cache-audio-library-event",
                        "select name, label, shortDescription, shortDescriptionLabel, audioExpirationDate, startDate, endDate, livestreamUrl, vodExpirationDate, repeatAudio, repeatedEvent" +
                        " from Event where id=? limit 1", eventId)
                    .onFailure(Console::log)
                    .onSuccess(events -> {
                        Event event = events.get(0);
                        UiScheduler.runInUiThread(() -> eventProperty.set(event)); // will update i18n bindings
                        Object eventIdContainingAudios = Entities.getPrimaryKey(event);
                        if (event.getRepeatedEventId() != null) {
                            eventIdContainingAudios = Entities.getPrimaryKey(event.getRepeatedEventId());
                        }
                        Object userAccountId = FXModalityUserPrincipal.getModalityUserPrincipal().getUserAccountId();
                        Object eventId1 = event.getPrimaryKey();
                        event.getStore().executeQueryBatchWithCache("cache-audio-library-scheduled-items-medias",
                                // Index 0: we look for the scheduledItem having a `bookableScheduledItem` which is an audio type (case of festival)
                                new EntityStoreQuery("""
                                    select name, label, date, expirationDate, programScheduledItem.(name, label, startTime, endTime, timeline.(startTime, endTime), cancelled), published, event.(name, type.recurringItem, recurringWithAudio), \
                                     (select id from Attendance where scheduledItem=si.bookableScheduledItem and documentLine.document.person.frontendAccount=$1 limit 1) as attendanceId, \
                                     (exists(select MediaConsumption where media.scheduledItem=si and attendance.documentLine.document.person.frontendAccount=$1 and played) as alreadyPlayed), \
                                     (exists(select MediaConsumption where media.scheduledItem=si and attendance.documentLine.document.person.frontendAccount=$1 and downloaded) as alreadyDownloaded) \
                                     from ScheduledItem si\
                                     where event=$2 and bookableScheduledItem.item.family.code=$3 and item.code=$4 and programScheduledItem is not null and exists(select Attendance where scheduledItem=si.bookableScheduledItem and documentLine.(!cancelled and document.(person.frontendAccount=$1 and event=$5 and confirmed and price_balance<=0)))\
                                     order by date, startTime, programScheduledItem.timeline..startTime""",
                                    new Object[]{userAccountId, eventIdContainingAudios, KnownItemFamily.AUDIO_RECORDING.getCode(), pathItemCodeProperty.get(), eventId1}),
                                // Index 1: we look for the scheduledItem of audio type having a `bookableScheduledItem` which is a teaching type (case of STTP)
                                // TODO: for now we take only the English audio recording scheduledItem in that case. We should take the default language of the organization instead
                                new EntityStoreQuery("""
                                    select name, label, date, expirationDate, programScheduledItem.(name, label, startTime, endTime, timeline.(startTime, endTime), cancelled), published, event.(name, type.recurringItem, recurringWithAudio), \
                                     (select id from Attendance where scheduledItem=si.bookableScheduledItem and documentLine.document.person.frontendAccount=$1 limit 1) as attendanceId, \
                                     (exists(select MediaConsumption where media.scheduledItem=si and attendance.documentLine.document.person.frontendAccount=$1 and played) as alreadyPlayed), \
                                     (exists(select MediaConsumption where media.scheduledItem=si and attendance.documentLine.document.person.frontendAccount=$1 and downloaded) as alreadyDownloaded) \
                                     from ScheduledItem si\
                                     where event=$2 and bookableScheduledItem.item.family.code=$3 and item.code=$4 and exists(select Attendance where scheduledItem=si.bookableScheduledItem and documentLine.(!cancelled and document.(person.frontendAccount=$1 and event=$5 and confirmed and price_balance<=0)))\
                                     order by date, startTime, programScheduledItem.timeline..startTime""",
                                    new Object[]{userAccountId, eventIdContainingAudios, KnownItemFamily.TEACHING.getCode(), KnownItem.AUDIO_RECORDING_ENGLISH.getCode(), eventId1}),
                                // Index 2: the medias
                                new EntityStoreQuery("""
                                    select url, scheduledItem.(date, event), scheduledItem.name, scheduledItem.published, durationMillis \
                                     from Media\
                                     where scheduledItem.(event=? and online and published and item.code in (?,?))""",
                                    new Object[]{eventIdContainingAudios, pathItemCodeProperty.get(), KnownItem.AUDIO_RECORDING_ENGLISH.getCode()}))
                            .onFailure(Console::log)
                            .inUiThread()
                            .onSuccess(entityLists -> {
                                Collections.setAll(publishedMedias, entityLists[2]);
                                entityLists[0].addAll(entityLists[1]);
                                scheduledAudioItems.setAll(entityLists[0]);// will trigger UI update
                            });
                    });
            }
        }, pathEventIdProperty, pathItemCodeProperty, FXModalityUserPrincipal.modalityUserPrincipalProperty());

        AudioColumnsRenderers.registerRenderers();
        audioColumns = VisualEntityColumnFactory.get().fromJsonArray("""
            [
            {expression: 'this', renderer: 'audioName', minWidth: 300},
            {expression: 'this', renderer: 'audioButtons', textAlign: 'center', hShrink: false, hGrow: false}
            ]""", getDomainModel(), "ScheduledItem");
    }

    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************
        EventHeader eventHeader = new MediaEventHeader(false);
        eventHeader.eventProperty().bind(eventProperty);
        eventHeader.languageProperty().bind(pathItemCodeProperty.map(EventAudioLibraryActivity::extractLang));

        MonoPane audioTracksContainer = new MonoPane();

        Text listOfTrackLabel = I18n.newText(AudioLibraryI18nKeys.ListOfTracks);
        VBox.setMargin(listOfTrackLabel, new Insets(30, 0, 0, 0));
        listOfTrackLabel.getStyleClass().add("list-tracks-title");

        VBox loadedContentVBox = new VBox(40,
            eventHeader.getView(),
            new ScalePane(ScaleMode.FIT_WIDTH, audioPlayer.getMediaView()),
            new ScalePane(listOfTrackLabel),
            audioTracksContainer,
            HelpPanel.createEmailHelpPanel(AudioLibraryI18nKeys.AudioLibraryHelp, "kbs@kadampa.net")
        );
        loadedContentVBox.setAlignment(Pos.TOP_CENTER);

        audioGrid.setMinRowHeight(48);
        audioGrid.setPrefRowHeight(Region.USE_COMPUTED_SIZE);
        audioGrid.setCellMargin(new Insets(15, 10, 5, 0));
        audioGrid.setFullHeight(true);
        audioGrid.setHeaderVisible(false);
        audioGrid.setSelectionMode(SelectionMode.DISABLED);
        audioGrid.setAppContext(EventAudioLibraryActivity.this); // passing the activity to AudioColumnsRenderers

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
            } else { // otherwise we display loadedContentVBox and set the content of audioTracksContainer
                pageContainer.setContent(loadedContentVBox);

                LocalDateTime audioExpirationDate = event.getAudioExpirationDate();
                LocalDateTime nowInEventTimezone = Event.nowInEventTimezone();
                Object noContentI18nKey = null;
                if (audioExpirationDate == null || audioExpirationDate.isAfter(nowInEventTimezone)) {
                    // Does this event have audio recordings, and did the person book and pay for them?
                    if (!scheduledAudioItems.isEmpty()) { // yes => we show them as a list of playable tracks
                        VisualResult vr = EntitiesToVisualResultMapper.mapEntitiesToVisualResult(scheduledAudioItems, audioColumns);
                        audioGrid.setVisualResult(vr);
                        audioTracksContainer.setContent(audioGrid);
                    } else { // no => we indicate that there are no recordings for that event
                        noContentI18nKey = AudioLibraryI18nKeys.NoAudioRecordingForThisEvent;
                    }
                } else {
                    noContentI18nKey = AudioLibraryI18nKeys.ContentExpired;
                }
                if (noContentI18nKey != null) {
                    Label noContentLabel = Bootstrap.h3(Bootstrap.textWarning(I18nControls.newLabel(noContentI18nKey)));
                    noContentLabel.setPadding(new Insets(150, 0, 100, 0));
                    audioTracksContainer.setContent(noContentLabel);
                }
            }
        }, scheduledAudioItems, eventProperty);


        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************

        pageContainer.getStyleClass().addAll("audio-library");
        // Setting a max width for big desktop screens
        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(pageContainer);
    }

    // Getters called by AudioColumnsRenderer:

    List<Media> getPublishedMedias() {
        return publishedMedias;
    }

    Player getAudioPlayer() {
        return audioPlayer;
    }

    private static String extractLang(String itemCode) {
        //the itemCode is in the form audio-fr
        if (itemCode == null || !itemCode.contains("-")) {
            return null;
        }
        int dashIndex = itemCode.indexOf("-");
        return itemCode.substring(dashIndex + 1);
    }

}
