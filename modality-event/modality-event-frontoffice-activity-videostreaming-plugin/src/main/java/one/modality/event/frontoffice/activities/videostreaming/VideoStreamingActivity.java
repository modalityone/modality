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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
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
import one.modality.event.frontoffice.eventheader.EventHeader;
import one.modality.event.frontoffice.eventheader.MediaEventHeader;
import one.modality.event.frontoffice.medias.TimeZoneSwitch;

/**
 * This is the activity for video streaming where people can watch the livestream and videos on demand.
 *
 * @author Bruno Salmon
 * @author David Hello
 */
final class VideoStreamingActivity extends ViewDomainActivityBase {

    // Observable list of events with videos booked by the user (changes on login & logout)
    private final ObservableList<Event> eventsWithBookedVideos = FXCollections.observableArrayList();
    // A boolean property to know if the loading of the eventsWithBookedVideos is in progress
    private final BooleanProperty eventsWithBookedVideosLoadingProperty = new SimpleBooleanProperty();

    // Event property to tell from which event we display the videos (can be changed by the event selector)
    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>();
    // The list of all videos loaded for that event
    private final ObservableList<ScheduledItem> videoScheduledItems = FXCollections.observableArrayList();

    // Main UI elements
    private final MonoPane pageContainer = new MonoPane(); // Will hold either the loading indicator or the loaded content
    final EventSelector eventSelector = new EventSelector(eventProperty, eventsWithBookedVideos);
    final LivestreamAndVideoPlayers livestreamAndVideoPlayers = new LivestreamAndVideoPlayers(eventProperty, videoScheduledItems);
    final Timetable timetable = new Timetable(videoScheduledItems, pageContainer, this);

    public VideoStreamingActivity() {
        //We relaunch the request every 14 hours (in case the user never closes the page, and to make sure the coherence of MediaConsumption is ok)
        Scheduler.schedulePeriodic(14 * 3600 * 1000, this::startLogic);
    }

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel());

        // Loading the list of events with videos booked by the user and put it into eventsWithBookedVideos
        FXProperties.runNowAndOnPropertyChange(modalityUserPrincipal -> {
            eventsWithBookedVideos.clear();
            if (modalityUserPrincipal != null) {
                Object userAccountId = modalityUserPrincipal.getUserAccountId();
                eventsWithBookedVideosLoadingProperty.set(true);
                // we look for the scheduledItem having a `bookableScheduledItem` which is a video type
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
                        // Extracting the events with videos from the document lines.
                        eventsWithBookedVideos.setAll( // Note: this will update the event selector as well
                            Collections.map(documentLines, dl -> dl.getDocument().getEvent())
                        );
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
        timetable.startLogic(entityStore);
    }

    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************

        // Building the loaded content, starting with the header
        Node loadingContentIndicator = new GoldenRatioPane(Controls.createProgressIndicator(100));
        EventHeader eventHeader = new MediaEventHeader(true);
        eventHeader.eventProperty().bind(eventProperty);

        VBox loadedContentVBox = new VBox(40,
            eventSelector.buildUi(),
            eventHeader.getView(), // contains the event image and the event title
            livestreamAndVideoPlayers.buildUi(pageContainer),
            timetable.buildUi(),
            // General help panel
            // HelpPanel.createEmailHelpPanel(VideoStreamingI18nKeys.VideosHelp, "kbs@kadampa.net")
            // For Festivals:
            HelpPanel.createHelpPanel(VideoStreamingI18nKeys.VideosHelp, VideoStreamingI18nKeys.VideosHelpSecondary) // temporarily hardcoded i18n message for Festivals
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

            // Otherwise, we display the loaded content
            pageContainer.setContent(loadedContentVBox);
            // and ensure the program is displayed in the appropriate mode (Ex: Festival or STTP)
            timetable.updateProgramDisplayMode();

        }, videoScheduledItems, eventProperty, eventsWithBookedVideosLoadingProperty);

        // Some reactions to changes in the timetable couldn't be done in timetable.buildUi() but must be done now,
        // i.e., after the above code, because timetable.updateProgramDisplayMode() has to be called before the other
        // reactions to changes.
        timetable.reactToChanges();

        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************

        pageContainer.getStyleClass().add("livestream");
        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(pageContainer);
    }

}
