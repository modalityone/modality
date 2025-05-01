package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Booleans;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.application.Platform;
import javafx.beans.binding.BooleanExpression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.event.frontoffice.medias.EventThumbnailView;

/**
 * @author David Hello
 */
final class AudioRecordingsActivity extends ViewDomainActivityBase {

    private static final int BOX_WIDTH = 263;

    // Holding an observable list of events with audio recordings booked by the user (changes on login & logout)
    private final ObservableList<DocumentLine> documentLinesWithBookedAudios = FXCollections.observableArrayList();

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        FXProperties.runNowAndOnPropertyChange(userPersonId -> {
            documentLinesWithBookedAudios.clear();
            if (userPersonId != null) {
                //Here there is different cases:
                // 1) the events where we buy the recordings throw a audioRecordingsDayTicket (case of the Festival)
                // 2) the events where the audios are linked to a teachingDayTicket (case of STTP)
                // See in backoffice ProgramActivity doc directory for more information
                entityStore.<DocumentLine>executeQuery(
                   "select document.event.(name,label.(de,en,es,fr,pt), shortDescription, shortDescriptionLabel, audioExpirationDate, startDate, endDate, repeatedEvent), item.code, item.family.code, " +
                       //We look if there are published audio ScheduledItem of type audio, whose bookableScheduledItem has been booked
                       " (exists(select ScheduledItem where item.family.code=? and published and bookableScheduledItem.(event=coalesce(dl.document.event.repeatedEvent, dl.document.event) and item=dl.item))) as published " +
                       //We check if the user has booked, not cancelled and paid the recordings
                       " from DocumentLine dl where !cancelled and dl.document.(person=? and confirmed and price_balance<=0) " +
                       " and dl.document.event.(repeatedEvent = null or repeatAudio)" +
                       //we check if :
                       " and (" +
                       // 1/ there is a ScheduledItem of audio family type whose bookableScheduledItem has been booked (KBS3 setup)
                       " exists (select ScheduledItem audioSi where item.family.code=? and exists(select Attendance where documentLine=dl and scheduledItem=audioSi.bookableScheduledItem))" +
                       // 2/ Or KBS3 / KBS2 setup (this allows displaying the audios that have been booked in the past with KBS2 events, event if we can't display them)
                       " or item.family.code=?) and document.event.kbs3=true " +
                       " order by document.event.startDate desc",
                        new Object[]{ KnownItemFamily.AUDIO_RECORDING.getCode(), userPersonId, KnownItemFamily.AUDIO_RECORDING.getCode(), KnownItemFamily.AUDIO_RECORDING.getCode()})
                    .onFailure(Console::log)
                    .onSuccess(documentLines -> Platform.runLater(() -> documentLinesWithBookedAudios.setAll(documentLines)));
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
        ObservableLists.bindConverted(columnsPane.getChildren(), documentLinesWithBookedAudios, dl -> {
            Event event = dl.getDocument().getEvent();
            String itemCode = dl.getItem().getCode();
            //If itemCode
            if (itemCode == null) {
                /// If the itemCode is null, we take the family
                /// For the case of STTP (the attendance is linked to a teaching bookable scheduledItem), the family is "teach"
                itemCode = dl.getItem().getFamily().getCode();
            }
            boolean published = Booleans.isTrue(dl.getBooleanFieldValue("published"));
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
        VBox noContentVBox = new VBox(30);
        Label noContentTitleLabel = Bootstrap.h3(I18nControls.newLabel(AudioRecordingsI18nKeys.NoAudioRecordingInYourLibrary));
        noContentTitleLabel.setContentDisplay(ContentDisplay.TOP);
        noContentTitleLabel.setGraphicTextGap(20);
        Label noContentText = (I18nControls.newLabel(AudioRecordingsI18nKeys.YourPurchasedRecordingsWillAppearHere));

        noContentVBox.setAlignment(Pos.TOP_CENTER);
        noContentVBox.getChildren().addAll(noContentTitleLabel,noContentText);
        BooleanExpression displayNoContentBinding = ObservableLists.isEmpty(columnsPane.getChildren());
        Layouts.bindManagedAndVisiblePropertiesTo(displayNoContentBinding, noContentVBox);
        Layouts.bindManagedAndVisiblePropertiesTo(displayNoContentBinding.not(), headerLabel);
        Layouts.bindManagedAndVisiblePropertiesTo(displayNoContentBinding.not(), checkoutLabel);

        noContentTitleLabel.setPadding(new Insets(75,0,0,0));

        VBox pageContainer = new VBox(
            headerLabel,
            checkoutLabel,
            noContentVBox,
            columnsPane
        );
        pageContainer.getStyleClass().addAll("audio-library");

        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(pageContainer);
    }

    private void showEventAudioWall(Event event,String itemCode) {
        getHistory().push(EventAudioPlaylistRouting.getEventRecordingsPlaylistPath(event,itemCode));
    }
}
