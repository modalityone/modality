package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.event.frontoffice.medias.EventThumbnailView;

final class AudioRecordingsActivity extends ViewDomainActivityBase {

    private static final int BOX_WIDTH = 263;

    // Holding an observable list of events with audio recordings booked by the user (changes on login & logout)
    private final ObservableList<DocumentLine> eventsWithBookedAudios = FXCollections.observableArrayList();

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        FXProperties.runNowAndOnPropertyChange(userPersonId -> {
            eventsWithBookedAudios.clear();
            if (userPersonId != null) {
                //Here there is different cases:
                // 1) the events where we buy the recordings throw a audioRecordingsDayTicket (case of the Festival)
                // 2) the events where the audios are linked to a teachingDayTicket (case of STTP)
                // See in backoffice ProgramActivity doc directory for more information
                entityStore.<DocumentLine>executeQuery(
                   "select document.event.(name,label.(de,en,es,fr,pt), shortDescription, audioExpirationDate, startDate, endDate), item.code, item.family.code, " +
                       //We look if there are published audio ScheduledItem of type audio, whose bookableScheduledItem has  been booked
                       " (exists(select ScheduledItem where item.family.code=? and published and bookableScheduledItem.(event=dl.document.event and item=dl.item))) as published " +
                       //We check if the user has booked, not cancelled and paid the recordings
                        " from DocumentLine dl where !cancelled  and dl.document.(person=? and price_balance<=0) " +
                       //we check if :
                       " and ("+
                       // 1/ there is a ScheduledItem of audio family type whose bookableScheduledItem has been booked (KBS3 setup)
                       " exists (select ScheduledItem audioSi where item.family.code=? and exists(select Attendance where documentLine=dl and scheduledItem=audioSi.bookableScheduledItem))" +
                       // 2/ Or KBS3 / KBS2 setup (this allows to display the audios that have been booked in the pase with KBS2 events, event if we can't display them)
                       " or item.family.code=?) and document.event.kbs3=true " +
                       " order by document.event.startDate desc",
                        new Object[]{ KnownItemFamily.AUDIO_RECORDING.getCode(), userPersonId, KnownItemFamily.AUDIO_RECORDING.getCode(), KnownItemFamily.AUDIO_RECORDING.getCode()})
                    .onFailure(Console::log)
                    .onSuccess(events -> Platform.runLater(() -> eventsWithBookedAudios.setAll(events)));
            }
        }, FXUserPersonId.userPersonIdProperty());
    }
    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************

        Label headerLabel = Bootstrap.h2(Bootstrap.strong(I18nControls.newLabel(AudioRecordingsI18nKeys.AudioRecordingsHeader)));
        Label checkoutLabel = I18nControls.newLabel(AudioRecordingsI18nKeys.CheckoutAudioRecordings);

        ColumnsPane columnsPane = new ColumnsPane(20, 50);
        columnsPane.setFixedColumnWidth(BOX_WIDTH);
        columnsPane.getStyleClass().add("media-library");
        columnsPane.setPadding(new Insets(50,0,0,0));

        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************

        // Showing a thumbnail in the columns pane for each event with videos
        ObservableLists.bindConverted(columnsPane.getChildren(), eventsWithBookedAudios, dl -> {
            Event event = dl.getDocument().getEvent();
            String itemCode = dl.getItem().getCode();
            //If itemCode
            if(itemCode == null) {
                /// If the itemCode is null, we take the family
                /// For the case of STTP (the attendance is linked to a teaching bookable scheduledItem), the family is teach
                itemCode = dl.getItem().getFamily().getCode();
            }
            boolean published = dl.getBooleanFieldValue("published");
            EventThumbnailView eventTbView = new EventThumbnailView(event, itemCode, EventThumbnailView.ItemType.ITEM_TYPE_AUDIO, published);
            VBox container = eventTbView.getView();
            Button actionButton = eventTbView.getActionButton();
            actionButton.setCursor(Cursor.HAND);
            String finalItemCode = itemCode;
            actionButton.setOnAction(e -> showEventAudioWall(event, finalItemCode));
            return container;
        });

        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************

        VBox pageContainer = new VBox(
            headerLabel,
            checkoutLabel,
            columnsPane
        );

        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(pageContainer);
    }

    private void showEventAudioWall(Event event,String itemCode) {
        getHistory().push(EventAudioPlaylistRouting.getEventRecordingsPlaylistPath(event,itemCode));
    }
}
