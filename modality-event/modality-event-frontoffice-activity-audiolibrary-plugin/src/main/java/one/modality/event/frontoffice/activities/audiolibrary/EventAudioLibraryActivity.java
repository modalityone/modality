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
import dev.webfx.platform.ast.AstArray;
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
import javafx.application.Platform;
import javafx.beans.property.*;
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
import one.modality.base.shared.entities.impl.ScheduledItemImpl;
import one.modality.base.shared.knownitems.KnownItem;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.crm.frontoffice.help.HelpPanel;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;
import one.modality.event.frontoffice.eventheader.MediaEventHeader;
import one.modality.event.frontoffice.medias.NoContentView;
import one.modality.event.frontoffice.medias.NotConfirmedView;
import one.modality.event.frontoffice.medias.PaymentPendingView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class EventAudioLibraryActivity extends ViewDomainActivityBase {

    private MediaEventHeader eventHeader;
    private VBox loadedContentVBox;
    private final SimpleBooleanProperty reloadProperty = new SimpleBooleanProperty();

    public enum AudioContentState {
        LOADING,
        NO_CONTENT,
        PAYMENT_PENDING,
        NOT_CONFIRMED,
        CONTENT_READY
    }

    private final SimpleObjectProperty<AudioContentState> audioStateProperty =
            new SimpleObjectProperty<>(AudioContentState.LOADING);
    private final ObjectProperty<Object> pathEventIdProperty = new SimpleObjectProperty<>();
    private final StringProperty pathItemCodeProperty = new SimpleStringProperty();

    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>();
    private final ObservableList<ScheduledItem> scheduledAudioItems = FXCollections.observableArrayList();
    private final List<Media> publishedMedias = new ArrayList<>(); // No need to be observable (reacting to scheduledAudioItems is enough)
    boolean isPaymentCurrentlyProcessing = false;
    private EntityColumn<ScheduledItem>[] audioColumns;

    private final Player audioPlayer = new JavaFXMediaAudioPlayer();
    private final VisualGrid audioGrid =
        // Using mono-column skin on mobiles with screen < 600 px to prevent unnecessary table computing (can cause performance issue on low-end mobiles)
        Screen.getPrimary().getVisualBounds().getWidth() < 600 ?
            VisualGrid.createVisualGridWithMonoColumnLayoutSkin() :
            // Otherwise, we use the responsive skin, which will decide between table and mono-column skin
            VisualGrid.createVisualGridWithResponsiveSkin();


    private int balance = 0;
    private boolean isRegistrationConfirmed = true;
    private final MonoPane pageContainer = new MonoPane();
    private final MonoPane audioTracksContainer = new MonoPane();



    @Override
    protected void updateModelFromContextParameters() {
        pathEventIdProperty.set(Numbers.toInteger(getParameter(EventAudioLibraryRouting.PATH_EVENT_ID_PARAMETER_NAME)));
        pathItemCodeProperty.set(getParameter(EventAudioLibraryRouting.PATH_ITEM_CODE_PARAMETER_NAME));
    }

    @Override
    protected void startLogic() {
        reloadProperty.setValue(false);
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
                audioStateProperty.set(AudioContentState.LOADING);
                entityStore.<Event>executeQueryWithCache("modality/event/audio-library/event", """
                            select name, label, shortDescription, shortDescriptionLabel, audioExpirationDate
                                    , startDate, endDate, livestreamUrl, vodExpirationDate, repeatAudio, repeatedEvent
                                from Event
                                where id=?
                                limit 1""", eventId)
                    .onFailure(Console::log)
                    .onCacheAndOrSuccess(events -> {
                        Event event = events.get(0);
                        UiScheduler.runInUiThread(() -> eventProperty.set(event)); // will update i18n bindings
                        Object eventIdContainingAudios = Entities.getPrimaryKey(event);
                        if (event.getRepeatedEventId() != null) {
                            eventIdContainingAudios = Entities.getPrimaryKey(event.getRepeatedEventId());
                        }
                        Object userAccountId = FXModalityUserPrincipal.getModalityUserPrincipal().getUserAccountId();
                        event.getStore().executeQueryBatchWithCache("modality/event/audio-library/scheduled-items-medias",
                                // Index 0: we look for the scheduledItem having a `bookableScheduledItem` which is an audio type (case of festival)
                                //TODO: optimize the request so we don t need to repeat three times the (select ... from Attendance)
                                        new EntityStoreQuery("""
                                    select name, label, date, expirationDate, programScheduledItem.(name, label, startTime, endTime, timeline.(startTime, endTime), cancelled), published, event.(name, type.recurringItem, recurringWithAudio)
                                        , (select jsonb_build_array(documentLine.document.(id,price_deposit,price_net,confirmed)) from Attendance where scheduledItem=si.bookableScheduledItem and accountCanAccessPersonMedias($1, documentLine.document.person) limit 1) as paymentAndConfirmedInfoJSonArray
                                        , (exists(select MediaConsumption where media.scheduledItem=si and accountCanAccessPersonMedias($1, attendance.documentLine.document.person) and played) as alreadyPlayed)
                                        , (exists(select MediaConsumption where media.scheduledItem=si and accountCanAccessPersonMedias($1, attendance.documentLine.document.person) and downloaded) as alreadyDownloaded)
                                        from ScheduledItem si
                                        where event=$2
                                            and bookableScheduledItem.item.family.code=$3
                                            and item.code=$4
                                            and programScheduledItem is not null
                                            and exists(select Attendance
                                                where scheduledItem=si.bookableScheduledItem
                                                    and documentLine.(!cancelled and document.(accountCanAccessPersonMedias($1, person) and event=$5)))
                                        order by date, startTime, programScheduledItem.timeline..startTime""",
                                    userAccountId, eventIdContainingAudios, KnownItemFamily.AUDIO_RECORDING.getCode(), pathItemCodeProperty.get(), event),
                                // Index 1: we look for the scheduledItem of audio type having a `bookableScheduledItem` which is a teaching type (case of STTP)
                                // TODO: for now we take only the English audio recording scheduledItem in that case. We should take the default language of the organization instead
                                new EntityStoreQuery("""
                                    select name, label, date, expirationDate, programScheduledItem.(name, label, startTime, endTime, timeline.(startTime, endTime), cancelled), published, event.(name, type.recurringItem, recurringWithAudio),
                                        (select jsonb_build_array(documentLine.document.(id,price_deposit,price_net,confirmed)) from Attendance where scheduledItem=si.bookableScheduledItem and accountCanAccessPersonMedias($1, documentLine.document.person) limit 1) as paymentAndConfirmedInfoJSonArray,
                                        (exists(select MediaConsumption where media.scheduledItem=si and accountCanAccessPersonMedias($1, attendance.documentLine.document.person) and played) as alreadyPlayed),
                                        (exists(select MediaConsumption where media.scheduledItem=si and accountCanAccessPersonMedias($1, attendance.documentLine.document.person) and downloaded) as alreadyDownloaded)
                                     from ScheduledItem si
                                     where event=$2
                                        and bookableScheduledItem.item.family.code=$3
                                        and item.code=$4
                                        and exists(select Attendance
                                            where scheduledItem=si.bookableScheduledItem
                                                and documentLine.(!cancelled and document.(accountCanAccessPersonMedias($1, person) and event=$5)))
                                     order by date, startTime, programScheduledItem.timeline..startTime""",
                                    userAccountId, eventIdContainingAudios, KnownItemFamily.TEACHING.getCode(), KnownItem.AUDIO_RECORDING_ENGLISH.getCode(), event),
                                // Index 2: the medias
                                new EntityStoreQuery("""
                                    select url, durationMillis, scheduledItem.(date, event, name, published)
                                     from Media
                                     where scheduledItem.(event=? and online and published and item.code in (?,?))""",
                                    eventIdContainingAudios, pathItemCodeProperty.get(), KnownItem.AUDIO_RECORDING_ENGLISH.getCode()))
                            .onFailure(Console::log)
                            .inUiThread()
                            .onCacheAndOrSuccess(entityLists -> {
                                AstArray parameters;
                                int paid = 0;
                                int price = 0;
                                if(!entityLists[0].isEmpty() && entityLists[0].get(0) != null) {
                                    parameters = (AstArray) ((ScheduledItemImpl) entityLists[0].get(0)).getFieldValue("paymentAndConfirmedInfoJSonArray");
                                    event.setFieldValue("documentPk",parameters.getInteger(0));
                                    paid = parameters.getInteger(1);
                                    price = parameters.getInteger(2);
                                    balance = price - paid;
                                    isRegistrationConfirmed = parameters.getBoolean(3);
                                }
                                if(!entityLists[1].isEmpty() && entityLists[1].get(0) != null) {
                                    parameters = (AstArray) ((ScheduledItemImpl) entityLists[1].get(0)).getFieldValue("paymentAndConfirmedInfoJSonArray");
                                    event.setFieldValue("documentPk",parameters.getInteger(0));
                                    paid = parameters.getInteger(1);
                                    price = parameters.getInteger(2);
                                    balance = price - paid;
                                    isRegistrationConfirmed = parameters.getBoolean(3);
                                }
                                updateContentStateProperty(paid,price,isRegistrationConfirmed, publishedMedias.isEmpty());
                                //noinspection unchecked
                                Collections.setAll(publishedMedias, entityLists[2]);
                                //noinspection unchecked
                                entityLists[0].addAll(entityLists[1]);
                                //noinspection unchecked
                                scheduledAudioItems.setAll(entityLists[0]); // will trigger UI update
                            });
                    });
            }
        }, pathEventIdProperty, pathItemCodeProperty, reloadProperty, FXModalityUserPrincipal.modalityUserPrincipalProperty());

        AudioColumnsRenderers.registerRenderers();
        audioColumns = VisualEntityColumnFactory.get().fromJsonArray( // language=JSON5
            """
            [
              {expression: 'this', renderer: 'audioName', minWidth: 300},
              {expression: 'this', renderer: 'audioButtons', textAlign: 'center', hShrink: false, hGrow: false}
            ]""", getDomainModel(), "ScheduledItem");
    }

    private void updateContentStateProperty(int paid, int price, boolean isConfirmed, boolean emptyContent) {
            if (emptyContent) {
                audioStateProperty.set(AudioContentState.NO_CONTENT);
            }
            if(price>paid){
                audioStateProperty.set(AudioContentState.PAYMENT_PENDING);
            } else if(!isConfirmed){
                audioStateProperty.set(AudioContentState.NOT_CONFIRMED);
            } else {
                audioStateProperty.set(AudioContentState.CONTENT_READY);}
    }

    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************
        eventHeader = new MediaEventHeader(false);
        eventHeader.eventProperty().bind(eventProperty);
        eventHeader.languageProperty().bind(pathItemCodeProperty.map(EventAudioLibraryActivity::extractLang));

        Text listOfTrackLabel = I18n.newText(AudioLibraryI18nKeys.ListOfTracks);
        VBox.setMargin(listOfTrackLabel, new Insets(30, 0, 0, 0));
        listOfTrackLabel.getStyleClass().add("list-tracks-title");

        loadedContentVBox = new VBox(40,
            eventHeader.getView(),
            new ScalePane(ScaleMode.FIT_WIDTH, audioPlayer.getMediaView()),
            new ScalePane(listOfTrackLabel),
            audioTracksContainer,
            HelpPanel.createEmailHelpPanel(AudioLibraryI18nKeys.AudioLibraryHelp, "kbs@kadampa.net")
        );
        loadedContentVBox.setAlignment(Pos.TOP_CENTER);
        pageContainer.setContent(loadedContentVBox);

        audioGrid.setMinRowHeight(48);
        audioGrid.setPrefRowHeight(Region.USE_COMPUTED_SIZE);
        audioGrid.setCellMargin(new Insets(15, 10, 5, 0));
        audioGrid.setFullHeight(true);
        audioGrid.setHeaderVisible(false);
        audioGrid.setSelectionMode(SelectionMode.DISABLED);
        audioGrid.setAppContext(EventAudioLibraryActivity.this); // passing the activity to AudioColumnsRenderers

        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************

        ObservableLists.runNowAndOnListOrPropertiesChange(change -> Platform.runLater(this::updateMainContent), scheduledAudioItems, eventProperty,audioStateProperty);

        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************

        pageContainer.getStyleClass().addAll("audio-library");
        // Setting a max width for big desktop screens
        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(pageContainer);
    }

    private void updateMainContent() {
        
        Node loadingContentIndicator = new GoldenRatioPane(Controls.createProgressIndicator(100));
        eventHeader.eventProperty().bind(eventProperty);
        AudioContentState audioState = audioStateProperty.get();

        // We display the loading indicator while the data is loading
        if (audioState== AudioContentState.LOADING) { // this indicates that the data has not finished loaded
            audioTracksContainer.getChildren().setAll(loadingContentIndicator);
            return;
        }

        // If the user didn't book any event with videos, we display "no content"
        if (audioState== AudioContentState.NO_CONTENT) {
            NoContentView content = new NoContentView("VIDEO");
            audioTracksContainer.getChildren().setAll(content.getView());
            return;
        }

        if(audioState== AudioContentState.PAYMENT_PENDING){
            int documentPk =  (int) eventProperty.get().getFieldValue("documentPk");
            PaymentPendingView paymentView = new PaymentPendingView(
                balance,
                eventProperty.get(),
                documentPk,
                () -> isPaymentCurrentlyProcessing = true,
                "VIDEO"
            );
            audioTracksContainer.getChildren().setAll(paymentView.getView());
            return;
        }
        //Case where the event is not confirmed
        if(audioState== AudioContentState.NOT_CONFIRMED){
            NotConfirmedView notConfirmedView = new NotConfirmedView();
            audioTracksContainer.getChildren().setAll(notConfirmedView.getView());
            return;
        }

        // We display the loading indicator while the data is loading
        Event event = eventProperty.get();
        if (event == null) { // this indicates that the data has not finished loaded
            pageContainer.setContent(loadingContentIndicator);
            // TODO display something else (ex: next online events to book) when the user is not logged in, or registered
        } else { // otherwise we display loadedContentVBox and set the content of audioTracksContainer
            pageContainer.setContent(loadedContentVBox);

            LocalDateTime audioExpirationDate = event.getAudioExpirationDate();
            LocalDateTime nowInEventTimezone = event.nowInEventTimezone();
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

    @Override
    public void onResume() {
        super.onResume();
        //Ajouter un test si le paiement est en cours, et s'il a aboutit, on recharge les infos avec un property que l'on fait changer de valeur
        if(isPaymentCurrentlyProcessing) {
            reloadProperty.setValue(false);
            reloadProperty.setValue(true);
            isPaymentCurrentlyProcessing = false;
        }
    }
}
