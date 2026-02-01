package one.modality.event.frontoffice.activities.audiolibrary;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Booleans;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityStore;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;
import one.modality.event.frontoffice.medias.EventThumbnail;

import static one.modality.event.frontoffice.activities.audiolibrary.AudioLibraryCssSelectors.audio_library;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class AudioLibraryActivity extends ViewDomainActivityBase {

    private static final double COLUMN_MIN_WIDTH = 255;

    // Holding an observable list of events with audio recordings booked by the user (changes on login & logout)
    private final ObservableList<DocumentLine> documentLinesWithBookedAudios = FXCollections.observableArrayList();

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        FXProperties.runNowAndOnPropertyChange(modalityUserPrincipal -> {
            documentLinesWithBookedAudios.clear();
            if (modalityUserPrincipal != null) {
                // Here there are 2 cases of event:
                // 1) Events where we buy the recordings through an audioRecordingsDayTicket (ex: Festival)
                // 2) Events where the audios are linked to a teachingDayTicket (case of STTP)
                // See in the backoffice ProgramActivity doc directory for more information
                entityStore.<DocumentLine>executeQueryWithCache("modality/event/audio-library/document-lines",
                        "select document.event.(name,label, shortDescription, shortDescriptionLabel, audioExpirationDate, startDate, endDate, repeatedEvent), item.(code, family.code)," +
                       // We look if there are published audio ScheduledItem of type audio, whose bookableScheduledItem has been booked
                       " (exists(select ScheduledItem where item.family.code=$1 and published and bookableScheduledItem.(event=coalesce(dl.document.event.repeatedEvent, dl.document.event) and item=dl.item))) as published " +
                       // We check if the user has booked, not cancelled and paid the recordings
                       " from DocumentLine dl where !cancelled and dl.document.(accountCanAccessPersonMedias($2, person)) " +
                       " and dl.document.event.(kbs3 and (repeatedEvent = null or repeatAudio))" +
                       // we check if :
                       " and (" +
                       // 1/ there is a ScheduledItem of type audio whose bookableScheduledItem has been booked (KBS3 setup)
                       " exists(select Attendance a where documentLine=dl and exists(select ScheduledItem where bookableScheduledItem=a.scheduledItem and item.family.code=$1))" +
                       // 2/ Or KBS3 / KBS2 setup (this allows displaying the audios that have been booked in the past with KBS2 events, event if we can't display them)
                       " or item.family.code=$1)" +
                       " order by document.event.startDate desc",
                        new Object[]{ KnownItemFamily.AUDIO_RECORDING.getCode(), modalityUserPrincipal.getUserAccountId()})
                    .onFailure(Console::log)
                    .inUiThread()
                    .onCacheAndOrSuccess(documentLinesWithBookedAudios::setAll);
            }
        }, FXModalityUserPrincipal.modalityUserPrincipalProperty());
    }

    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************

        Label headerLabel = Bootstrap.h2(Bootstrap.strong(I18nControls.newLabel(AudioLibraryI18nKeys.AudioLibraryHeader)));
        Label checkoutLabel = I18nControls.newLabel(AudioLibraryI18nKeys.CheckoutAudioRecordings);
        Controls.setupTextWrapping(checkoutLabel, true, false);

        ColumnsPane columnsPane = new ColumnsPane(30, 50);
        columnsPane.setMinColumnWidth(COLUMN_MIN_WIDTH);
        columnsPane.setAlignment(Pos.TOP_CENTER);
        columnsPane.setPadding(new Insets(50,0,0,0));

        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************

        // Showing a thumbnail in the columns pane for each event with videos
        ObservableLists.bindConverted(columnsPane.getChildren(), documentLinesWithBookedAudios, dl -> {
            Event event = dl.getDocument().getEvent();
            String itemCode = dl.getItem().getCode();
            // If the itemCode is null, we take the family code
            if (itemCode == null) {
                // Might be 'teach' and not 'record', for ex STTP where the attendance is linked to a teaching bookableScheduledItem
                itemCode = dl.getItem().getFamily().getCode();
            }
            boolean published = Booleans.isTrue(dl.getBooleanFieldValue("published"));
            EventThumbnail eventTbView = new EventThumbnail(event, itemCode, EventThumbnail.ItemType.ITEM_TYPE_AUDIO, published);
            VBox container = eventTbView.getView();
            Button actionButton = eventTbView.getViewButton();
            actionButton.setCursor(Cursor.HAND);
            String finalItemCode = itemCode;
            actionButton.setOnAction(e -> showEventAudioWall(event, finalItemCode));
            return container;
        });

        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************
        VBox noContentVBox = new VBox(30);
        Label noContentTitleLabel = Bootstrap.h3(I18nControls.newLabel(AudioLibraryI18nKeys.NoAudioRecordingInYourLibrary));
        noContentTitleLabel.setContentDisplay(ContentDisplay.TOP);
        noContentTitleLabel.setGraphicTextGap(20);
        Label noContentText = I18nControls.newLabel(AudioLibraryI18nKeys.YourPurchasedRecordingsWillAppearHere);

        noContentVBox.setAlignment(Pos.TOP_CENTER);
        noContentVBox.getChildren().addAll(noContentTitleLabel,noContentText);
        BooleanExpression displayNoContentBinding = ObservableLists.isEmpty(columnsPane.getChildren());
        Layouts.bindManagedAndVisiblePropertiesTo(displayNoContentBinding, noContentVBox);
        Layouts.bindManagedAndVisiblePropertiesTo(displayNoContentBinding.not(), headerLabel);
        Layouts.bindManagedAndVisiblePropertiesTo(displayNoContentBinding.not(), checkoutLabel);

        noContentTitleLabel.setPadding(new Insets(75,0,0,0));

        // We embed headerLabel in a ScalePane because it can be too wide on mobiles.
        ScalePane headerScalePane = new ScalePane(headerLabel);
        headerScalePane.setAlignment(Pos.TOP_LEFT); // VBox will stretch the ScalePane, but we position the label on the left inside
        headerLabel.setMinWidth(Region.USE_PREF_SIZE); // This is to make it shrink when it doesn't fit in width
        VBox pageContainer = new VBox(
            headerScalePane,
            checkoutLabel,
            noContentVBox,
            columnsPane
        );
        pageContainer.getStyleClass().addAll(audio_library);

        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(pageContainer);
    }

    private void showEventAudioWall(Event event,String itemCode) {
        getHistory().push(EventAudioLibraryRouting.getEventRecordingsPlaylistPath(event,itemCode));
    }
}
