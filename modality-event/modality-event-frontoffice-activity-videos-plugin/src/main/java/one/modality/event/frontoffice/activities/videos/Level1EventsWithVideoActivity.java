package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
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
import one.modality.base.shared.entities.KnownItem;
import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.event.frontoffice.medias.EventThumbnailView;

import java.util.stream.Collectors;

/**
 * This is the first activity displayed when people click on the Livestream menu. It displays the list of all video
 * series booked by the user over all events. There is typically only one video series per event, represented by a
 * single DocumentLine. For each event, we display a thumbnail and a description of the event.
 *
 * @author David Hello
 * @author Bruno Salmon
 */
final class Level1EventsWithVideoActivity extends ViewDomainActivityBase {

    private static final double BOX_WIDTH = 263;

    // Holding an observable list of events with videos booked by the user (changes on login & logout)
    private final ObservableList<Event> documentLinesWithBookedVideos = FXCollections.observableArrayList();

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        // Loading the list of events with videos booked by the user and put it into eventsWithBookedVideos
        FXProperties.runNowAndOnPropertyChange(userPersonId -> {
            documentLinesWithBookedVideos.clear();
            if (userPersonId != null) {
                // we look for the scheduledItem having a bookableScheduledItem which is an audio type (case of festival)
                entityStore.<DocumentLine>executeQuery(
                        "select document.event.(name,label.(de,en,es,fr,pt), shortDescription, vodExpirationDate, startDate, endDate, repeatedEvent), item.code, item.family.code, " +
                            //We look if there are published audio ScheduledItem of type video, whose bookableScheduledItem has  been booked
                            " (exists(select ScheduledItem where item.family.code=? and bookableScheduledItem.(event=coalesce(dl.document.event.repeatedEvent, dl.document.event) and item=dl.item))) as published " +
                            //We check if the user has booked, not cancelled and paid the recordings
                            " from DocumentLine dl where !cancelled  and dl.document.(person=? and confirmed and price_balance<=0) " +
                            " and dl.document.event.(repeatedEvent = null or repeatVideo)" +
                            //we check if :
                            " and ("+
                            // 1/ there is a ScheduledItem of video family type whose bookableScheduledItem has been booked (KBS3 setup)
                            " exists (select ScheduledItem videoSI where item.family.code=? and exists(select Attendance where documentLine=dl and scheduledItem=videoSI.bookableScheduledItem))" +
                            // 2/ Or KBS3 / KBS2 setup (this allows to display the audios that have been booked in the past with KBS2 events, event if we can't display them)
                            " or item.family.code=?) and document.event.kbs3=true " +
                            " order by document.event.startDate desc",
                        new Object[]{ KnownItemFamily.VIDEO.getCode(), userPersonId, KnownItemFamily.VIDEO.getCode(), KnownItemFamily.VIDEO.getCode()})
                    .onFailure(Console::log)
                    .onSuccess(documentLines -> Platform.runLater(() -> documentLinesWithBookedVideos.setAll(
                        documentLines.stream()
                            .map(documentLine -> documentLine.getDocument().getEvent())  // Extract events from DocumentLine
                            .distinct()
                            .collect(Collectors.toList()))));
            }
        }, FXUserPersonId.userPersonIdProperty());
    }

    @Override
    public Node buildUi() {
        Label headerLabel = Bootstrap.h2(Bootstrap.strong(I18nControls.newLabel(VideosI18nKeys.VideosHeader)));
        VBox.setMargin(headerLabel, new Insets(0, 0, 50, 0));

        ColumnsPane columnsPane = new ColumnsPane(20, 50);
        columnsPane.setFixedColumnWidth(BOX_WIDTH);
        columnsPane.getStyleClass().add("media-library");
        // Showing a thumbnail in the columns pane for each event with videos
        ObservableLists.bindConverted(columnsPane.getChildren(), documentLinesWithBookedVideos, event -> {
            EventThumbnailView eventTbView = new EventThumbnailView(event, KnownItem.VIDEO.getCode(), EventThumbnailView.ItemType.ITEM_TYPE_VIDEO, true);
            VBox container = eventTbView.getView();
            Button actionButton = eventTbView.getActionButton();
            actionButton.setCursor(Cursor.HAND);
            //1st case: Livestream only events (ie vodExpirationDate is null)
            if (event.getVodExpirationDate() == null)
                actionButton.setOnAction(e -> showLivestreamVideo(event));
            else //2dn case: events with recordings
                actionButton.setOnAction(e -> showEventVideosWall(event));
            return container;
        });

        VBox noContentVBox = new VBox(30);
        Label noContentTitleLabel = Bootstrap.h3(I18nControls.newLabel(VideosI18nKeys.NoVideoInYourLibrary));
        noContentTitleLabel.setContentDisplay(ContentDisplay.TOP);
        noContentTitleLabel.setGraphicTextGap(20);
        Label noContentText = (I18nControls.newLabel(VideosI18nKeys.YourNextLiveStreamEventWillAppearHere));

        noContentVBox.setAlignment(Pos.TOP_CENTER);
        noContentVBox.getChildren().addAll(noContentTitleLabel,noContentText);
        BooleanExpression displayNoContentBinding = ObservableLists.isEmpty(columnsPane.getChildren());
        Layouts.bindManagedAndVisiblePropertiesTo(displayNoContentBinding, noContentVBox);
        Layouts.bindManagedAndVisiblePropertiesTo(displayNoContentBinding.not(), headerLabel);

        noContentTitleLabel.setPadding(new Insets(75,0,0,0));

        VBox pageContainer = new VBox(
            headerLabel,
            columnsPane,
            noContentVBox
        );

        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(pageContainer);
        //return FrontOfficeActivityUtil.createActivityPageScrollPane(pageContainer, false);
    }

    private void showLivestreamVideo(Event event) {
        getHistory().push(Level3LivestreamPlayerRouting.getLivestreamPath(event));
    }
    private void showEventVideosWall(Event event) {
        getHistory().push(Level2EventDaysWithVideoRouting.getEventVideosWallPath(event));
    }

}
