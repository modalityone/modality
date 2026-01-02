package one.modality.event.frontoffice.activities.videostreaming;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.GoldenRatioPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.knownitems.KnownItem;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.crm.frontoffice.help.HelpPanel;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.event.frontoffice.eventheader.EventHeader;
import one.modality.event.frontoffice.eventheader.MediaEventHeader;
import one.modality.event.frontoffice.medias.NoContentView;
import one.modality.event.frontoffice.medias.NotConfirmedView;
import one.modality.event.frontoffice.medias.PaymentPendingView;
import one.modality.event.frontoffice.medias.TimeZoneSwitch;


/**
 * This is the activity for video streaming where people can watch the livestream and videos on demand.
 *
 * @author Bruno Salmon
 * @author David Hello
 */
final class VideoStreamingActivity extends ViewDomainActivityBase {

    public enum VideoContentState {
        LOADING,
        NO_CONTENT,
        PAYMENT_PENDING,
        NOT_CONFIRMED,
        CONTENT_READY
    }

    private final SimpleObjectProperty<VideoContentState> contentStateProperty =
        new SimpleObjectProperty<>(VideoContentState.LOADING);


    // Observable list of events with videos booked by the user (changes on login & logout)
    private final ObservableList<Event> eventsWithBookedVideos = FXCollections.observableArrayList();
    // A boolean property to know if the loading of the eventsWithBookedVideos is in progress
    private final BooleanProperty eventsWithBookedVideosLoadingProperty = new SimpleBooleanProperty();

    // Event property to tell from which event we display the videos (can be changed by the event selector)
    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>();
    private final BooleanProperty reloadProperty = new SimpleBooleanProperty();

    // The list of all videos loaded for that event
    private final ObservableList<ScheduledItem> videoScheduledItems = FXCollections.observableArrayList();

    // Main UI elements
    private final MonoPane pageContainer = new MonoPane(); // Will hold either the loading indicator or the loaded content
    private final VBox mediaContentContainer = new VBox(40); //Will hold the players, track list, or warning or error message
    final EventSelector eventSelector = new EventSelector(eventProperty, eventsWithBookedVideos);
    final LivestreamAndVideoPlayers livestreamAndVideoPlayers = new LivestreamAndVideoPlayers(eventProperty, videoScheduledItems);
    final Timetable timetable = new Timetable(videoScheduledItems, pageContainer, this);

    boolean isPaymentCurrentlyProcessing = false;

    public VideoStreamingActivity() {
        //We relaunch the request every 14 hours (in case the user never closes the page, and to make sure the coherence of MediaConsumption is ok)
        Scheduler.schedulePeriodic(14 * 3600 * 1000, this::startLogic);
    }

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel());
        reloadProperty.setValue(true);
        // Loading the list of events with videos booked by the user and put it into eventsWithBookedVideos
        FXProperties.runNowAndOnPropertiesChange(() -> {
            if (reloadProperty.get()) {
                ModalityUserPrincipal modalityUserPrincipal = FXModalityUserPrincipal.getModalityUserPrincipal();
                eventsWithBookedVideos.clear();
                if (modalityUserPrincipal != null) {
                    Object userAccountId = modalityUserPrincipal.getUserAccountId();
                    eventsWithBookedVideosLoadingProperty.set(true);
                    // we look for the scheduledItem having a `bookableScheduledItem` which is an audio type (case of festival)
                    entityStore.<DocumentLine>executeQueryWithCache("modality/event/video-streaming/document-lines",
                            "select document.(price_net, price_deposit, confirmed), document.event.(name, label, shortDescription, shortDescriptionLabel, audioExpirationDate, startDate, endDate, livestreamUrl, timezone, vodExpirationDate, repeatVideo, recurringWithVideo, repeatedEvent, livestreamMessageLabel), item.(code, family.code)" +
                            // We look if there are published audio ScheduledItem of type video, whose bookableScheduledItem has been booked
                            ", (exists(select ScheduledItem where item.family.code=$2 and bookableScheduledItem.(event=coalesce(dl.document.event.repeatedEvent, dl.document.event) and item=dl.item))) as published " +
                            // We check if the user has booked, not cancelled and paid the recordings
                            " from DocumentLine dl where !cancelled  and dl.document.(accountCanAccessPersonMedias($1, dl.document.person)) " +
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
                            // Extracting the events with videos from the document lines.
                            for (DocumentLine dl : documentLines) {
                                Event event = dl.getDocument().getEvent();
                                event.setFieldValue("document", dl.getDocument());
                                event.setFieldValue("balance", dl.getDocument().getPriceNet() - dl.getDocument().getPriceDeposit());
                                event.setFieldValue("isConfirmed", dl.getDocument().isConfirmed());
                                if (!eventsWithBookedVideos.contains(event)) {
                                    eventsWithBookedVideos.add(event);
                                }
                            }
                            if (eventProperty.get() == null)
                                eventProperty.set(Collections.first(eventsWithBookedVideos));
                            updateContentStateProperty();
                        });
                }
            }
        }, FXModalityUserPrincipal.modalityUserPrincipalProperty(), reloadProperty);

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
                                    and documentLine.(!cancelled and document.(event=$5 and accountCanAccessPersonMedias($1, person))))
                             order by date, programScheduledItem.timeline..startTime""",
                        /*$1*/ userAccountId, /*$2*/ eventContainingVideos, /*$3*/ KnownItemFamily.TEACHING.getCode(), /*$4*/ KnownItem.VIDEO.getCode(), /*$5*/ event)
                    .onFailure(Console::log)
                    .inUiThread()
                    .onCacheAndOrSuccess(videoScheduledItems::setAll); // Will trigger the build of the video table.
            }
        }, eventProperty, FXUserPersonId.userPersonIdProperty());

        livestreamAndVideoPlayers.startLogic(entityStore);
        timetable.startLogic(entityStore);
    }

    private void updateContentStateProperty() {
        if (eventsWithBookedVideosLoadingProperty.get()) {
            contentStateProperty.set(VideoContentState.LOADING);
        } else if (eventsWithBookedVideos.isEmpty()) {
            contentStateProperty.set(VideoContentState.NO_CONTENT);
        }
        int balance = 0;
        boolean isConfirmed = true;
        Event event = eventProperty.get();
        if (event != null) {
            balance = event.getIntegerFieldValue("balance");
            isConfirmed = event.getBooleanFieldValue("isConfirmed");
        }
        if (balance > 0) {
            contentStateProperty.set(VideoContentState.PAYMENT_PENDING);
        } else if (!isConfirmed) {
            contentStateProperty.set(VideoContentState.NOT_CONFIRMED);
        } else {
            contentStateProperty.set(VideoContentState.CONTENT_READY);
        }
    }

    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************

        EventHeader eventHeader = new MediaEventHeader(true);
        eventHeader.eventProperty().bind(eventProperty);

        // Warning banner for livestream-only events
        Label livestreamOnlyWarning = Bootstrap.alertWarning(I18nControls.newLabel(VideoStreamingI18nKeys.LivestreamOnlyWarning));
        livestreamOnlyWarning.setWrapText(true);
        livestreamOnlyWarning.setTextAlignment(TextAlignment.CENTER);
        livestreamOnlyWarning.setMaxWidth(800);
        VBox.setMargin(livestreamOnlyWarning, new Insets(0, 0, 20, 0));

        mediaContentContainer.getChildren().addAll(livestreamAndVideoPlayers.buildUi(pageContainer),
            timetable.buildUi());
        VBox loadedContentVBox = new VBox(40,
            eventSelector.buildUi(),
            eventHeader.getView(), // contains the event image and the event title
            livestreamOnlyWarning,
            mediaContentContainer,
            // General help panel
            HelpPanel.createEmailHelpPanel(VideoStreamingI18nKeys.VideosHelp, "kbs@kadampa.net")
            // For Festivals:
            //HelpPanel.createHelpPanel(VideoStreamingI18nKeys.VideosHelp, VideoStreamingI18nKeys.VideosHelpSecondary) // temporarily hardcoded i18n message for Festivals
        );
        Layouts.setMinMaxHeightToPref(loadedContentVBox); // No need to compute min/max height as different to pref (layout computation optimization)
        loadedContentVBox.setAlignment(Pos.TOP_CENTER);


        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************

        // Show/hide the livestream-only warning banner based on the event's vodExpirationDate
        FXProperties.runNowAndOnPropertyChange(event -> {
            boolean isLivestreamOnly = event != null && event.getVodExpirationDate() == null;
            Console.log("Event changed: " + (event != null ? event.getName() : "null") +
                        ", vodExpirationDate: " + (event != null ? event.getVodExpirationDate() : "N/A") +
                        ", isLivestreamOnly: " + isLivestreamOnly);
            Layouts.setManagedAndVisibleProperties(livestreamOnlyWarning, isLivestreamOnly);
        }, eventProperty);

        // Reacting to data loading (initially or when the event changes or on login/logout)
        ObservableLists.runNowAndOnListOrPropertiesChange(changes -> {
            // If not yet initialized, we select the most relevant event to show on start (the first one from order by)
            if (eventProperty.get() == null) eventProperty.set(Collections.first(eventsWithBookedVideos));
            eventsWithBookedVideosLoadingProperty.set(false);
            updateContentStateProperty();
        }, videoScheduledItems, eventProperty, eventsWithBookedVideosLoadingProperty);

        FXProperties.runNowAndOnPropertyChange(this::updateMainContent, contentStateProperty);

        // Some reactions to changes in the timetable couldn't be done in timetable.buildUi() but must be done now,
        // i.e., after the above code, because timetable.updateProgramDisplayMode() has to be called before the other
        // reactions to changes.
        timetable.reactToChanges();

        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************

        // Otherwise, we display the loaded content
        pageContainer.setContent(loadedContentVBox);
        // and ensure the program is displayed in the appropriate mode (Ex: Festival or STTP)
        timetable.updateProgramDisplayMode();
        pageContainer.getStyleClass().add("livestream");
        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(pageContainer);
    }

    private void updateMainContent(VideoContentState state) {
        Node loadingContentSpinner = new GoldenRatioPane(Controls.createSectionSizeSpinner());
        EventHeader eventHeader = new MediaEventHeader(true);
        eventHeader.eventProperty().bind(eventProperty);

        // We display the loading indicator while the data is loading
        if (state == VideoContentState.LOADING) { // this indicates that the data has not finished loaded
            mediaContentContainer.getChildren().setAll(loadingContentSpinner);
            return;
        }

        // If the user didn't book any event with videos, we display "no content"
        if (state == VideoContentState.NO_CONTENT) {
            NoContentView content = new NoContentView("VIDEO");
            mediaContentContainer.getChildren().setAll(content.getView());
            return;
        }

        if (state == VideoContentState.PAYMENT_PENDING) {
            Event event = eventProperty.get();
            int balance = event.getIntegerFieldValue("balance");
            Document orderDocument = event.getForeignEntity("document");
            PaymentPendingView paymentView = new PaymentPendingView(
                balance,
                event,
                orderDocument,
                () -> isPaymentCurrentlyProcessing = true,
                "VIDEO"
            );
            mediaContentContainer.getChildren().setAll(paymentView.getView());
            return;
        }
        //Case where the event is not confirmed
        if (state == VideoContentState.NOT_CONFIRMED) {
            NotConfirmedView notConfirmedView = new NotConfirmedView();
            mediaContentContainer.getChildren().setAll(notConfirmedView.getView());
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        //Ajouter un test si le paiement est en cours, et s'il a abouti, on recharge les infos avec un property que l'on fait changer de valeur
        if (isPaymentCurrentlyProcessing) {
            reloadProperty.setValue(false);
            reloadProperty.setValue(true);
            isPaymentCurrentlyProcessing = false;
        }
    }
}
