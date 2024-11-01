package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import one.modality.base.frontoffice.utility.activity.FrontOfficeActivityUtil;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.event.frontoffice.medias.EventThumbnailView;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class VideosActivity extends ViewDomainActivityBase {

    private static final double PAGE_TOP_BOTTOM_PADDING = 100;
    private static final int BOX_WIDTH = 450;

    // Holding an observable list of events with videos booked by the user (changes on login & logout)
    private final ObservableList<Event> eventsWithBookedVideos = FXCollections.observableArrayList();

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        // Loading the list of events with videos booked by the user and put it into eventsWithBookedVideos
        FXProperties.runNowAndOnPropertiesChange(() -> {
            eventsWithBookedVideos.clear();
            EntityId userPersonId = FXUserPersonId.getUserPersonId();
            if (userPersonId != null) {
                entityStore.<Event>executeQuery(
                    "select name, label.(de,en,es,fr,pt), shortDescription, audioExpirationDate, startDate, endDate, livestreamUrl, vodExpirationDate" +
                    " from Event e" +
                    " where exists(select DocumentLine where !cancelled and document.(event=e and person=? and price_balance<=0) and item.family.code=?)",
                        userPersonId, KnownItemFamily.VIDEO.getCode())
                    .onFailure(Console::log)
                    .onSuccess(events -> Platform.runLater(() -> eventsWithBookedVideos.setAll(events)));
            }
        }, FXUserPersonId.userPersonIdProperty());
    }

    @Override
    public Node buildUi() {
        Label headerLabel = Bootstrap.h2(Bootstrap.strong(I18nControls.newLabel(VideosI18nKeys.VideosHeader)));
        VBox.setMargin(headerLabel, new Insets(0,0,50,0));

        ColumnsPane columnsPane = new ColumnsPane(20, 50);
        columnsPane.setFixedColumnWidth(BOX_WIDTH);

        // Showing a thumbnail in the columns pane for each event with videos
        ObservableLists.bindConverted(columnsPane.getChildren(), eventsWithBookedVideos, event -> {
            VBox container = new EventThumbnailView(event).getView();
            container.setCursor(Cursor.HAND);
            container.setOnMouseClicked(e -> showEventVideosWall(event));
            return container;
        });

        VBox pageContainer = new VBox(
            headerLabel,
            columnsPane
        );

        pageContainer.setPadding(new Insets(PAGE_TOP_BOTTOM_PADDING, 0, PAGE_TOP_BOTTOM_PADDING, 0));

        return FrontOfficeActivityUtil.createActivityPageScrollPane(pageContainer, false);
    }

    private void showEventVideosWall(Event event) {
        getHistory().push(EventVideosWallRouting.getEventVideosWallPath(event));
    }

}
