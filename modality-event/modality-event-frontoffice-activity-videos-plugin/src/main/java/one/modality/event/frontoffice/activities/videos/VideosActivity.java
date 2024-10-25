package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.extras.panes.transitions.CircleTransition;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.ControlUtil;
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
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.event.frontoffice.medias.EventThumbnailView;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class VideosActivity extends ViewDomainActivityBase {

    private static final int BOX_WIDTH = 450;

    private EntityStore entityStore;
    // Holding an observable list of events with videos booked by the user (changes on login & logout)
    private final ObservableList<Event> eventsWithBookedVideos = FXCollections.observableArrayList();

    private final VBox mainVBox =  new VBox();
    private final TransitionPane transitionPane = new TransitionPane();

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        // Loading the list of events with videos booked by the user and put it into bookedVideoEvents
        FXProperties.runNowAndOnPropertiesChange(() -> {
            eventsWithBookedVideos.clear();
            EntityId userPersonId = FXUserPersonId.getUserPersonId();
            if (userPersonId != null) {
                entityStore.<Event>executeQuery(
                    "select name, label.(de,en,es,fr,pt), shortDescription, audioExpirationDate, startDate, endDate, livestreamUrl, vodExpirationDate" +
                    " from Event e where exists(select DocumentLine where document.(event=e and person=? and price_balance<=0) and item.family.code=?)",
                        userPersonId, KnownItemFamily.VIDEO.getCode())
                    .onFailure(Console::log)
                    .onSuccess(events -> Platform.runLater(() -> eventsWithBookedVideos.setAll(events)));
            }
        }, FXUserPersonId.userPersonIdProperty());
    }

    @Override
    public Node buildUi() {
        Label videoLabel = Bootstrap.h2(Bootstrap.strong(I18nControls.bindI18nProperties(new Label(), VideosI18nKeys.VideoTitle)));
        VBox.setMargin(videoLabel, new Insets(0,0,50,0));

        ColumnsPane columnsPane = new ColumnsPane();
        columnsPane.setMaxWidth(BOX_WIDTH * 2 + 50);
        columnsPane.setMaxColumnCount(2);
        columnsPane.setHgap(20);
        columnsPane.setVgap(50);
        columnsPane.setAlignment(Pos.TOP_LEFT);

        // Showing a thumbnail in the columns pane for each event with videos
        ObservableLists.bindConverted(columnsPane.getChildren(), eventsWithBookedVideos, event -> {
            VBox container = new EventThumbnailView(event).getView();
            container.setMaxWidth(BOX_WIDTH);
            container.setMinWidth(BOX_WIDTH);
            container.setCursor(Cursor.HAND);
            container.setOnMouseClicked(e -> showEventVideosWall(event));
            return container;
        });

        mainVBox.getChildren().setAll(
            videoLabel,
            columnsPane
        );

        transitionPane.setPadding(new Insets(100));
        transitionPane.setTransition(new CircleTransition());
        transitionPane.setScrollToTop(true);
        transitionPane.transitToContent(mainVBox);

        return ControlUtil.createVerticalScrollPane(transitionPane);
    }

    private void showEventVideosWall(Event event) {
        EventVideosWallView eventVideosWallView = new EventVideosWallView(event, () -> transitionPane.transitToContent(mainVBox), transitionPane::transitToContent);
        transitionPane.transitToContent(eventVideosWallView.getView());
    }

}
